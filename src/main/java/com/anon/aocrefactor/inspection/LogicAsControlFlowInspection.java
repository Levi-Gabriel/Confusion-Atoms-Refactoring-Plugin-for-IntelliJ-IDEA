package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.ReplaceLogicAsControlFlow;
import com.anon.aocrefactor.util.OperatorUtils;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LogicAsControlFlowInspection extends AbstractBaseJavaLocalInspectionTool {

    // Usado pelo mecanismo de supressão via comentário
    // "// noinspection LogicAsControlFlow" inserido pelo próprio quick fix
    // quando ele decide apenas lançar um comentário em vez de refatorá-lo.
    public static final String SHORT_NAME = "LogicAsControlFlow";

    @Override
    public @NotNull String getShortName() {
        return SHORT_NAME;
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            @NotNull ProblemsHolder holder,
            boolean isOnTheFly) {

        return new JavaElementVisitor() {

            @Override
            public void visitBinaryExpression(@NotNull PsiBinaryExpression expression) {
                super.visitBinaryExpression(expression);
                checkLogicAsControlFlow(expression, holder);
            }

            @Override
            public void visitPolyadicExpression(@NotNull PsiPolyadicExpression expression) {
                super.visitPolyadicExpression(expression);
                // Evita duplo processamento já que PsiBinaryExpression herda de PsiPolyadicExpression
                if (expression instanceof PsiBinaryExpression) return;
                checkLogicAsControlFlow(expression, holder);
            }

            private void checkLogicAsControlFlow(PsiExpression expression, ProblemsHolder holder) {
                IElementType op = OperatorUtils.getOperator(expression);

                if (op == null || !OperatorUtils.isLogicalOperator(op)) return;

                // Evita alertas duplicados em sub-árvores lógicas aninhadas
                // delega o warning para o nó raiz da expressão lógica
                if (isRootLogicalExpression(expression)) return;

                List<PsiExpression> operands = OperatorUtils.getOperands(expression);
                if (operands.size() < 2) return;

                for (int i = 1; i < operands.size(); i++) {
                    PsiExpression dependentOperand = operands.get(i);
                    if (containsSideEffectOrComputation(dependentOperand)) {
                        holder.registerProblem(
                                expression,
                                "Logic as Control Flow atom detected: short-circuit evaluation masks conditional execution. Consider refactoring into an explicit control flow statement.",
                                ProblemHighlightType.WARNING,
                                new ReplaceLogicAsControlFlow()
                        );
                        return; // Dispara um único aviso para a expressão lógica completa
                    }
                }
            }

            // Verifica se a expressão atual é um operando secundário (não o primeiro)
            // dentro de uma expressão lógica pai, indicando que o processamento deve ser
            // delegado para o nó pai para evitar alertas duplicados.

            private boolean isRootLogicalExpression(PsiExpression expression) {

                PsiElement immediateChild;

                if (expression.getParent() instanceof PsiParenthesizedExpression paren) {
                    immediateChild = paren;
                } else {
                    immediateChild = expression;
                }

                // Sobe um nó pulando eventuais parênteses para encontrar o nó pai real.
                PsiElement parent = PsiUtil.skipParenthesizedExprUp(expression.getParent());

                if (!(parent instanceof PsiExpression parentExpr)) return false;
                if (!OperatorUtils.isLogicalOperator(OperatorUtils.getOperator(parentExpr))) return false;

                List<PsiExpression> parentOperands = OperatorUtils.getOperands(parentExpr);
                int index = parentOperands.indexOf(immediateChild);
                return index >= 1;
            }

            private boolean containsSideEffectOrComputation(PsiExpression operand) {
                if (isSideEffectOrComputation(operand)) return true;

                // Busca recursiva na árvore do operando por invocações, atribuições, mutações ou instanciações
                // cima -> baixo
                return PsiTreeUtil.findChildrenOfAnyType(
                        operand,
                        PsiCallExpression.class,
                        PsiAssignmentExpression.class,
                        PsiUnaryExpression.class
                ).stream().anyMatch(this::isSideEffectOrComputation);
            }


            private boolean isSideEffectOrComputation(PsiElement element) {
                if (element instanceof PsiCallExpression) return true;
                if (element instanceof PsiAssignmentExpression) return true;
                if (element instanceof PsiUnaryExpression unary) {
                    return OperatorUtils.isIncrementOrDecrement(unary.getOperationTokenType());
                }
                return false;
            }
        };
    }
}