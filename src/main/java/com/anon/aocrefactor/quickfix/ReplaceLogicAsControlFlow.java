package com.anon.aocrefactor.quickfix;

import com.anon.aocrefactor.inspection.LogicAsControlFlowInspection;
import com.anon.aocrefactor.util.OperatorUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReplaceLogicAsControlFlow implements LocalQuickFix {

    private static final int LARGE_BODY_STATEMENT_THRESHOLD = 3;

    // Acima disso, mesmo com corpos pequenos, a cascata de ifs (||) ou o
    // aninhamento (&&) fica ilegível o suficiente para preferir o comentário.
    private static final int MAX_OPERANDS_THRESHOLD = 4;

    @Override
    public @NotNull String getName() {
        return "Replace logic operator with explicit control flow statement";
    }

    @Override
    public @NotNull String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        if (!(descriptor.getPsiElement() instanceof PsiExpression expression)) return;

        PsiExpression root = findRootLogicalExpression(expression);
        IElementType operator = OperatorUtils.getOperator(root);
        if (operator == null) return;

        boolean isOr = operator == JavaTokenType.OROR;
        List<PsiExpression> operands = OperatorUtils.getOperands(root);
        if (operands.size() < 2) return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

        PsiWhileStatement whileStatement = PsiTreeUtil.getParentOfType(root, PsiWhileStatement.class);
        if (whileStatement != null) {
            if (isConditionOf(root, whileStatement.getCondition())) {
                applyToWhileStatement(factory, whileStatement, operands, isOr);
                return;
            }
        }

        PsiIfStatement ifStatement = PsiTreeUtil.getParentOfType(root, PsiIfStatement.class);
        if (ifStatement != null) {
            if (isConditionOf(root, ifStatement.getCondition())) {
                applyToIfStatement(factory, ifStatement, operands, isOr);
                return;
            }
        }

        PsiElement bodyOwner = climbParens(root);
        PsiElement parent = bodyOwner.getParent();

        if (parent instanceof PsiExpressionStatement statement) {
            applyToExpressionStatement(factory, statement, root, operands, isOr);
        } else if (parent instanceof PsiReturnStatement returnStatement) {
            applyToReturnStatement(factory, returnStatement, root, operands, isOr);
        } else if (parent instanceof PsiLambdaExpression lambda) {
            if (lambda.getBody() == bodyOwner) {
                applyToLambdaExpression(factory, lambda, root, operands, isOr);
            }
        }
    }

    // Caso 1: expressão isolada como instrução. Ex: "cond1 && cond2 && acao();"
    private void applyToExpressionStatement(PsiElementFactory factory,
                                            PsiExpressionStatement statement,
                                            PsiExpression anchor,
                                            List<PsiExpression> operands,
                                            boolean isOr) {
        if (tooManyConditions(operands)) {
            insertExplanatoryComment(factory, statement, operands, isOr);
            return;
        }

        PsiExpression action = operands.get(operands.size() - 1);
        List<PsiExpression> conditions = operands.subList(0, operands.size() - 1);
        String sep = isOr ? " || " : " && ";
        String joined = conditions.stream().map(PsiElement::getText).collect(Collectors.joining(sep));

        // O payload (ação) só roda quando o curto-circuito "cai" até ele:
        // para &&, isso é quando todas as condições são verdadeiras (guarda pura);
        // para ||, quando todas são falsas (guarda negada).
        String guard = isOr ? "!(" + joined + ")" : joined;

        String ifText = "if (" + guard + ") {\n    " + action.getText() + ";\n}";
        statement.replace(factory.createStatementFromText(ifText, anchor));
    }

    // Caso 2: expressão no valor de um "return"
    private void applyToReturnStatement(PsiElementFactory factory,
                                        PsiReturnStatement returnStatement,
                                        PsiExpression anchor,
                                        List<PsiExpression> operands,
                                        boolean isOr) {
        PsiElement parent = returnStatement.getParent();
        if (!(parent instanceof PsiCodeBlock block)) return;

        if (tooManyConditions(operands)) {
            insertExplanatoryComment(factory, returnStatement, operands, isOr);
            return;
        }

        PsiExpression lastOperand = operands.get(operands.size() - 1);
        String returnText = "return " + lastOperand.getText() + ";";
        List<PsiExpression> conditions = operands.subList(0, operands.size() - 1);

        // Cada condição de guarda pode decidir o valor cedo: true assim que
        // alguma condição bate para ||, false assim que alguma falha para &&.
        // Se nenhuma decidir, o valor final é o próprio último operando.
        String generatedText;
        if (isOr) {
            StringBuilder sb = new StringBuilder();
            for (PsiExpression condition : conditions) {
                sb.append("if (").append(condition.getText()).append(") return true;\n");
            }
            sb.append(returnText);
            generatedText = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            for (PsiExpression condition : conditions) {
                sb.append("if (!(").append(condition.getText()).append(")) return false;\n");
            }
            sb.append(returnText);
            generatedText = sb.toString();
        }

        PsiCodeBlock generated = factory.createCodeBlockFromText("{\n" + generatedText + "\n}", returnStatement);
        for (PsiStatement generatedStatement : generated.getStatements()) {
            block.addBefore(generatedStatement, returnStatement);
        }
        returnStatement.delete();
    }

    // Caso: corpo de expressão de uma lambda. Ex.: "x -> cond1 && cond2"
    private void applyToLambdaExpression(PsiElementFactory factory,
                                         PsiLambdaExpression lambda,
                                         PsiExpression anchor,
                                         List<PsiExpression> operands,
                                         boolean isOr) {
        if (tooManyConditions(operands)) {
            insertExplanatoryCommentInLambdaBody(factory, lambda, anchor, operands, isOr);
            return;
        }

        PsiExpression lastOperand = operands.get(operands.size() - 1);
        String returnText = "return " + lastOperand.getText() + ";";
        List<PsiExpression> conditions = operands.subList(0, operands.size() - 1);

        // Mesma lógica de guarda de applyToReturnStatement: cada condição
        // decide o valor cedo (true por || assim que uma bate, false por &&
        // assim que uma falha); se nenhuma decidir, vale o último operando.
        String generatedText;
        if (isOr) {
            StringBuilder sb = new StringBuilder();
            for (PsiExpression condition : conditions) {
                sb.append("if (").append(condition.getText()).append(") return true;\n");
            }
            sb.append(returnText);
            generatedText = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            for (PsiExpression condition : conditions) {
                sb.append("if (!(").append(condition.getText()).append(")) return false;\n");
            }
            sb.append(returnText);
            generatedText = sb.toString();
        }

        replaceLambdaBody(factory, lambda, "{\n" + generatedText + "\n}");
    }

    // Caso: condição de um "if" existente
    private void applyToIfStatement(PsiElementFactory factory,
                                    PsiIfStatement ifStatement,
                                    List<PsiExpression> operands,
                                    boolean isOr) {
        PsiStatement thenBranch = ifStatement.getThenBranch();
        if (thenBranch == null) return;
        PsiStatement elseBranch = ifStatement.getElseBranch();

        if (shouldFallbackToComment(operands, isOr, thenBranch, elseBranch)) {
            insertExplanatoryComment(factory, ifStatement, operands, isOr);
            return;
        }

        String elseText = elseBranch == null ? null : getBodyText(elseBranch);
        String newText = buildIfCascade(operands, getBodyText(thenBranch), isOr, elseText);
        ifStatement.replace(factory.createStatementFromText(newText, ifStatement));
    }

    // Caso: condição de um laço "while"
    private void applyToWhileStatement(PsiElementFactory factory,
                                       PsiWhileStatement whileStatement,
                                       List<PsiExpression> operands,
                                       boolean isOr) {
        PsiStatement body = whileStatement.getBody();
        if (body == null) return;

        if (tooManyConditions(operands)) {
            insertExplanatoryComment(factory, whileStatement, operands, isOr);
            return;
        } else if (isBodyTooLargeToDuplicate(body)) {
            insertExplanatoryComment(factory, whileStatement, operands, isOr);
            return;
        }

        StringBuilder guards = new StringBuilder();
        if (isOr) {
            String innerBreak = "break;\n";
            for (int i = operands.size() - 1; i >= 0; i--) {
                innerBreak = "if (!(" + operands.get(i).getText() + ")) {\n" + innerBreak + "}\n";
            }
            guards.append(innerBreak);
        } else {
            for (PsiExpression operand : operands) {
                guards.append("if (!(").append(operand.getText()).append(")) {\n    break;\n}\n");
            }
        }
        guards.append(getBodyText(body));

        String newText = "while (true) {\n" + guards + "\n}";
        whileStatement.replace(factory.createStatementFromText(newText, whileStatement));
    }

    // Sobe expressões lógicas encadeadas do mesmo operador (A || B || C), atravessando parênteses.
    private PsiExpression findRootLogicalExpression(PsiExpression expr) {
        PsiExpression current = expr;
        while (true) {
            PsiElement parent = climbParens(current).getParent();
            IElementType currentOp = OperatorUtils.getOperator(current);

            // noinspection LogicAsControlFlow -- Confusion atom (Logic as Control Flow): currentOp != null, currentOp.equals(OperatorUtils.getOperator(parentExpr)) só são avaliado(s) condicionalmente, por curto-circuito do '&&'. Refatoração automática evitada (corpo grande demais para duplicar com segurança, condições demais, ou mudaria a ordem de avaliação) — revisão manual recomendada.
            if (parent instanceof PsiExpression parentExpr
                    && currentOp != null
                    && currentOp.equals(OperatorUtils.getOperator(parentExpr))) {
                current = parentExpr;
            } else {
                break;
            }
        }
        return current;
    }

    // Desce: remove parênteses redundantes que envolvem diretamente
    private PsiExpression unwrapDown(PsiExpression element) {
        PsiExpression current = element;
        while (current instanceof PsiParenthesizedExpression parenthesized) {
            PsiExpression inner = parenthesized.getExpression();
            if (inner == null) break;
            current = inner;
        }
        return current;
    }

    // Sobe: encontra o wrapper de parênteses mais externo, se houver.
    private PsiElement climbParens(PsiElement element) {
        PsiElement current = element;
        while (current.getParent() instanceof PsiParenthesizedExpression parenthesized) {
            current = parenthesized;
        }
        return current;
    }

    // '||' vira cascata else-if terminando no "else" original (se houver);
    // '&&' vira ifs aninhados, cada nível reaplicando o "else" original
    // (curto-circuito: qualquer condição falsa cai no mesmo else).
    private String buildIfCascade(List<PsiExpression> operands, String bodyText, boolean isOr, String elseText) {
        if (isOr) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < operands.size(); i++) {
                sb.append(i == 0 ? "if (" : "else if (")
                        .append(operands.get(i).getText())
                        .append(") {\n").append(bodyText).append("\n}\n");
            }
            if (elseText != null) {
                sb.append("else {\n").append(elseText).append("\n}\n");
            }
            return sb.toString();
        }

        String elseBlock = elseText == null ? null : "else {\n" + elseText + "\n}";
        String inner = bodyText;
        for (int i = operands.size() - 1; i >= 0; i--) {
            inner = "if (" + operands.get(i).getText() + ") {\n" + inner + "\n}"
                    + (elseBlock == null ? "" : " " + elseBlock);
        }
        return inner;
    }

    private String getBodyText(PsiStatement statement) {
        if (statement instanceof PsiBlockStatement block) {
            return Arrays.stream(block.getCodeBlock().getStatements())
                    .map(PsiElement::getText)
                    .collect(Collectors.joining("\n"));
        }
        return statement.getText();
    }

    // É, integralmente, a condição informada — não apenas parte dela.
    private boolean isConditionOf(PsiExpression current, PsiExpression condition) {
        if (condition == null) return false;
        return unwrapDown(condition) == unwrapDown(current);
    }

    private boolean tooManyConditions(List<PsiExpression> operands) {
        return operands.size() > MAX_OPERANDS_THRESHOLD;
    }

    private boolean isBodyTooLargeToDuplicate(PsiStatement statement) {
        if (!(statement instanceof PsiBlockStatement block)) return false;
        return block.getCodeBlock().getStatements().length > LARGE_BODY_STATEMENT_THRESHOLD;
    }

    //
    // Decide se o "if" deve virar comentário em vez de cascata/aninhamento:
    // condições demais, corpo do "then" grande demais para duplicar, corpo do
    // "else" grande demais, ou (caso &&) um "else" não-trivial que seria
    // duplicado em cada nível de aninhamento — mesmo pequeno, a duplicação
    // repetida N vezes vira bloat.

    private boolean shouldFallbackToComment(List<PsiExpression> operands,
                                            boolean isOr,
                                            PsiStatement thenBranch,
                                            PsiStatement elseBranch) {
        if (tooManyConditions(operands)) return true;
        if (isBodyTooLargeToDuplicate(thenBranch)) return true;
        if (elseBranch == null) return false;
        if (isBodyTooLargeToDuplicate(elseBranch)) return true;
        // && com else e mais de uma condição duplica o else em cada nível.
        if (isOr) return false;
        return operands.size() > 1;
    }

    // Fallback para corpos grandes demais pra duplicar com segurança
    private void insertExplanatoryComment(PsiElementFactory factory,
                                          PsiStatement anchor,
                                          List<PsiExpression> operands,
                                          boolean isOr) {
        PsiElement blockChild = findDirectChildOfEnclosingBlock(anchor);
        if (blockChild == null) return;
        PsiCodeBlock block = (PsiCodeBlock) blockChild.getParent();

        String opText = isOr ? "||" : "&&";
        List<PsiExpression> guarded = operands.subList(1, operands.size());
        String guardedText = guarded.stream().map(PsiElement::getText).collect(Collectors.joining(", "));

        PsiComment comment = factory.createCommentFromText(buildConfusionAtomComment(guardedText, guarded.size(), opText), anchor);
        block.addBefore(comment, blockChild);
    }

    //Fallback para corpo de expressão de lambda. Diferente dos outros casos,
    private void insertExplanatoryCommentInLambdaBody(PsiElementFactory factory,
                                                      PsiLambdaExpression lambda,
                                                      PsiExpression anchor,
                                                      List<PsiExpression> operands,
                                                      boolean isOr) {
        String opText = isOr ? "||" : "&&";
        List<PsiExpression> guarded = operands.subList(1, operands.size());
        String guardedText = guarded.stream().map(PsiElement::getText).collect(Collectors.joining(", "));

        String bodyText = buildConfusionAtomComment(guardedText, guarded.size(), opText)
                + "\nreturn " + anchor.getText() + ";";
        replaceLambdaBody(factory, lambda, "{\n" + bodyText + "\n}");
    }

    // Reconstrói a lambda inteira com um novo corpo em bloco e substitui o elemento original.
    private void replaceLambdaBody(PsiElementFactory factory, PsiLambdaExpression lambda, String blockText) {
        String paramsText = lambda.getParameterList().getText();
        String lambdaText = paramsText + " -> " + blockText;
        PsiExpression newLambda = factory.createExpressionFromText(lambdaText, lambda);
        lambda.replace(newLambda);
    }

    private String buildConfusionAtomComment(String guardedText, int guardedCount, String opText) {
        return "// noinspection " + LogicAsControlFlowInspection.SHORT_NAME + " -- Confusion atom (Logic as Control Flow): " + guardedText
                + " só " + (guardedCount > 1 ? "são" : "é") + " avaliado(s) condicionalmente,"
                + " por curto-circuito do '" + opText + "'."
                + " Refatoração automática evitada (corpo grande demais para duplicar com"
                + " segurança, condições demais, ou mudaria a ordem de avaliação) — revisão"
                + " manual recomendada.";
    }

    // Sobe a partir de element até encontrar o ancestral
    private PsiElement findDirectChildOfEnclosingBlock(PsiElement element) {
        PsiElement current = element;
        while (true) {
            if (current == null) {
                break;
            }
            if (current.getParent() instanceof PsiCodeBlock) {
                break;
            }
            current = current.getParent();
            if (current == null || current instanceof PsiFile) return null;
        }
        return current;
    }

}