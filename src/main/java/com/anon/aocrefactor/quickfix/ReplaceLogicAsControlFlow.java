package com.anon.aocrefactor.quickfix;

import com.anon.aocrefactor.util.ExpressionUtils;
import com.anon.aocrefactor.util.OperatorUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReplaceLogicAsControlFlow implements LocalQuickFix {

    private static final int LARGE_BODY_STATEMENT_THRESHOLD = 3;
    private static final int MAX_DUPLICATION_OPERANDS = 4;

    // CASCADE: '||' -> else-if (duplicates the then branch, only when it is small)
    // NESTED:  '&&' -> nested ifs (without an else nothing is duplicated; with an else, the else is duplicated, only when it is small)
    private enum IfStrategy {CASCADE, NESTED}

    private record Target(PsiElement host, List<PsiExpression> operands, boolean isOr) {
    }

    @Override
    public @NotNull String getName() {
        return "Replace logic operator with explicit control flow statement";
    }

    @Override
    public @NotNull String getFamilyName() {
        return getName();
    }

    // Used by the inspection to only offer the fix when it can refactor safely.
    public static boolean isApplicable(@NotNull PsiExpression expression) {
        return resolve(expression) != null;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        if (!(descriptor.getPsiElement() instanceof PsiExpression expression)) return;

        Target target = resolve(expression);
        if (target == null) return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiElement host = target.host();
        List<PsiExpression> operands = target.operands();
        boolean isOr = target.isOr();

        if (host instanceof PsiWhileStatement w) {
            replaceStatement(factory, w, buildWhile(w, operands, isOr));
        } else if (host instanceof PsiIfStatement i) {
            replaceStatement(factory, i, buildIf(i, operands, isOr));
        } else if (host instanceof PsiExpressionStatement s) {
            replaceStatement(factory, s, buildGuardedAction(operands, isOr));
        } else if (host instanceof PsiAssignmentExpression assignment) {
            String lhs = assignment.getLExpression().getText();
            replaceStatement(factory, (PsiStatement) assignment.getParent(),
                    buildGuards(operands, isOr, v -> lhs + " = " + v + ";"));
        } else if (host instanceof PsiLocalVariable variable) {
            PsiIdentifier id = variable.getNameIdentifier();
            String decl = variable.getText().substring(0, id.getStartOffsetInParent() + id.getTextLength()) + ";\n";
            replaceWithStatements(factory, (PsiStatement) variable.getParent(),
                    decl + buildGuards(operands, isOr, v -> variable.getName() + " = " + v + ";"));
        } else if (host instanceof PsiReturnStatement r) {
            replaceWithStatements(factory, r, buildReturnGuards(operands, isOr));
        } else if (host instanceof PsiLambdaExpression lambda) {
            replaceLambdaBody(factory, lambda, "{\n" + buildReturnGuards(operands, isOr) + "\n}");
        }
    }

    private static Target resolve(PsiExpression expression) {
        PsiExpression root = findRootLogicalExpression(expression);
        IElementType operator = OperatorUtils.getOperator(root);
        List<PsiExpression> operands = OperatorUtils.getOperands(root);
        PsiElement host = findHost(root);
        if (host == null) return null;
        if (operator == null) return null;
        if (operands.size() < 2) return null;

        boolean isOr = operator == JavaTokenType.OROR;
        if (host instanceof PsiWhileStatement w) {
            if (w.getBody() == null) {
                return null;
            }
        }
        if (host instanceof PsiIfStatement i) {
            if (chooseIfStrategy(i, operands, isOr) == null) {
                return null;
            }
        }
        return new Target(host, operands, isOr);
    }

    // Climbs chained logical expressions with the same operator (A || B || C), going through parentheses.
    private static PsiExpression findRootLogicalExpression(PsiExpression expr) {
        IElementType operator = OperatorUtils.getOperator(expr);
        if (operator == null) return expr;

        PsiExpression current = expr;
        while (ExpressionUtils.climbParens(current).getParent() instanceof PsiExpression parent) {
            if (!(operator.equals(OperatorUtils.getOperator(parent)))) {
                break;
            }
            current = parent;
        }
        return current;
    }

    // Element that "owns" the expression and will be rewritten; null if there is no supported case.
    @SuppressWarnings("LogicAsControlFlow")
    private static PsiElement findHost(PsiExpression root) {
        PsiElement owner = ExpressionUtils.climbParens(root);
        PsiElement parent = owner.getParent();

        if (parent instanceof PsiWhileStatement w && w.getCondition() == owner) return w;
        if (parent instanceof PsiIfStatement i && i.getCondition() == owner) return i;
        if (parent instanceof PsiExpressionStatement) return parent;
        if (parent instanceof PsiReturnStatement && parent.getParent() instanceof PsiCodeBlock) return parent;
        if (parent instanceof PsiLambdaExpression l && l.getBody() == owner) return l;

        // "x = a && b;" / "this.x = a && b;" (com "arr[i++] = ..." o lado esquerdo seria avaliado depois)
        if (parent instanceof PsiAssignmentExpression asg
                && asg.getRExpression() == owner
                && asg.getOperationTokenType() == JavaTokenType.EQ
                && asg.getParent() instanceof PsiExpressionStatement
                && asg.getLExpression() instanceof PsiReferenceExpression lhs
                && (lhs.getQualifierExpression() == null
                || lhs.getQualifierExpression() instanceof PsiThisExpression)
                && lhs.resolve() instanceof PsiVariable) {
            return asg;
        }
        // "boolean x = a && b;" (uma variável só, dentro de bloco, sem "var")
        if (parent instanceof PsiLocalVariable v && v.getInitializer() == owner
                && v.getParent() instanceof PsiDeclarationStatement d
                && d.getDeclaredElements().length == 1
                && d.getParent() instanceof PsiCodeBlock
                && !v.getTypeElement().isInferredType()) {
            return v;
        }
        return null;
    }


    private static IfStrategy chooseIfStrategy(PsiIfStatement ifStatement, List<PsiExpression> operands, boolean isOr) {
        PsiStatement thenBranch = ifStatement.getThenBranch();
        if (thenBranch == null) return null;
        PsiStatement elseBranch = ifStatement.getElseBranch();
        boolean fewOperands = operands.size() <= MAX_DUPLICATION_OPERANDS;

        if (isOr) return fewOperands && !isLargeBody(thenBranch) ? IfStrategy.CASCADE : null;
        if (elseBranch == null) return IfStrategy.NESTED;
        return fewOperands && !isLargeBody(elseBranch) ? IfStrategy.NESTED : null;
    }




    // The payload only runs when short-circuiting falls through to it: all true (&&) or all false (||).
    private static String buildGuardedAction(List<PsiExpression> operands, boolean isOr) {
        String joined = joinText(operands.subList(0, operands.size() - 1), isOr ? " || " : " && ");
        return "if (" + (isOr ? "!(" + joined + ")" : joined) + ") {\n" + lastOperand(operands).getText() + ";\n}";
    }

    // Each condition decides the value early: true as soon as one matches (||), false as soon as one fails (&&).
    // If none decides, the value is the last operand. Used by "return" and by expression lambdas.
    private static String buildReturnGuards(List<PsiExpression> operands, boolean isOr) {
        StringBuilder sb = new StringBuilder();
        for (PsiExpression c : operands.subList(0, operands.size() - 1)) {
            sb.append(isOr
                    ? "if (" + c.getText() + ") return true;\n"
                    : "if (!(" + c.getText() + ")) return false;\n");
        }
        return sb.append("return ").append(lastOperand(operands).getText()).append(";").toString();
    }

    // Cascade for assignments: the value is decided as soon as one operand decides (true in ||, false in &&);
    // if none decides, the value is the last one. "emit" defines the destination: "x = v;".
    private static String buildGuards(List<PsiExpression> operands, boolean isOr, Function<String, String> emit) {
        StringBuilder sb = new StringBuilder();
        for (PsiExpression c : operands.subList(0, operands.size() - 1)) {
            sb.append(sb.length() == 0 ? "if (" : " else if (")
                    .append(isOr ? c.getText() : "!(" + c.getText() + ")")
                    .append(") {\n").append(emit.apply(String.valueOf(isOr))).append("\n}");
        }
        return sb.append(" else {\n").append(emit.apply(lastOperand(operands).getText())).append("\n}").toString();
    }


    private static String buildWhile(PsiWhileStatement whileStatement, List<PsiExpression> operands, boolean isOr) {
        String body = getBodyText(whileStatement.getBody());
        return isOr ? buildWhileOr(operands, body) : buildWhileAnd(operands, body);
    }


    private static String buildWhileAnd(List<PsiExpression> operands, String body) {
        StringBuilder guards = new StringBuilder();
        for (PsiExpression operand : operands.subList(1, operands.size())) {
            guards.append("if (!(").append(operand.getText()).append(")) {\nbreak;\n}\n");
        }
        return "while (" + operands.get(0).getText() + ") {\n" + guards + body + "\n}";
    }


    private static String buildWhileOr(List<PsiExpression> operands, String body) {
        String nested = "break;\n";
        for (int i = operands.size() - 1; i >= 0; i--) {
            nested = "if (!(" + operands.get(i).getText() + ")) {\n" + nested + "}\n";
        }
        return "while (true) {\n" + nested + body + "\n}";
    }

    private static String buildIf(PsiIfStatement ifStatement, List<PsiExpression> operands, boolean isOr) {
        String thenText = getBodyText(ifStatement.getThenBranch());
        PsiStatement elseBranch = ifStatement.getElseBranch();
        String elseText = elseBranch == null ? null : getBodyText(elseBranch);

        return switch (chooseIfStrategy(ifStatement, operands, isOr)) {
            case CASCADE -> buildCascade(operands, thenText, elseText);
            case NESTED -> buildNested(operands, thenText, elseText);
        };
    }


    private static String buildCascade(List<PsiExpression> operands, String thenText, String elseText) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < operands.size(); i++) {
            sb.append(i == 0 ? "if (" : " else if (")
                    .append(operands.get(i).getText())
                    .append(") {\n").append(thenText).append("\n}");
        }
        return appendElse(sb, elseText).toString();
    }


    private static String buildNested(List<PsiExpression> operands, String thenText, String elseText) {
        String inner = thenText;
        for (int i = operands.size() - 1; i >= 0; i--) {
            StringBuilder level = new StringBuilder("if (")
                    .append(operands.get(i).getText())
                    .append(") {\n").append(inner).append("\n}");
            inner = appendElse(level, elseText).toString();
        }
        return inner;
    }

    private static StringBuilder appendElse(StringBuilder sb, String elseText) {
        return elseText == null ? sb : sb.append(" else {\n").append(elseText).append("\n}");
    }



    private static void replaceStatement(PsiElementFactory factory, PsiStatement old, String text) {
        old.replace(factory.createStatementFromText(text, old));
    }

    // Replaces one statement with several (return, local variable declaration).
    private static void replaceWithStatements(PsiElementFactory factory, PsiStatement old, String text) {
        PsiElement block = old.getParent();
        for (PsiStatement s : factory.createCodeBlockFromText("{\n" + text + "\n}", old).getStatements()) {
            block.addBefore(s, old);
        }
        old.delete();
    }

    // Rebuilds the whole lambda with a new block body.
    private static void replaceLambdaBody(PsiElementFactory factory, PsiLambdaExpression lambda, String blockText) {
        lambda.replace(factory.createExpressionFromText(lambda.getParameterList().getText() + " -> " + blockText, lambda));
    }

    private static PsiExpression lastOperand(List<PsiExpression> operands) {
        return operands.get(operands.size() - 1);
    }


    private static String joinText(List<PsiExpression> expressions, String separator) {
        return expressions.stream().map(PsiElement::getText).collect(Collectors.joining(separator));
    }


    private static String getBodyText(PsiStatement statement) {
        if (statement instanceof PsiBlockStatement block) {
            return Arrays.stream(block.getCodeBlock().getStatements())
                    .map(PsiElement::getText)
                    .collect(Collectors.joining("\n"));
        }
        return statement.getText();
    }


    // A chained "else if" counts as large: copying it several times pollutes more than it helps.
    private static boolean isLargeBody(PsiStatement statement) {
        if (statement instanceof PsiIfStatement) return true;
        if (!(statement instanceof PsiBlockStatement block)) return false;
        return block.getCodeBlock().getStatements().length > LARGE_BODY_STATEMENT_THRESHOLD;
    }
}