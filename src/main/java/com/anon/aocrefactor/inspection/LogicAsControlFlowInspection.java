package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.ReplaceLogicAsControlFlow;
import com.anon.aocrefactor.util.ExpressionUtils;
import com.anon.aocrefactor.util.OperatorUtils;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LogicAsControlFlowInspection extends AbstractBaseJavaLocalInspectionTool {

    public static final String SHORT_NAME = "LogicAsControlFlow";

    private static final String MESSAGE = "Logic as Control Flow atom detected: short-circuit evaluation "
            + "masks conditional execution. Consider refactoring into an explicit control flow statement.";

    @Override
    public @NotNull String getShortName() {
        return SHORT_NAME;
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitBinaryExpression(@NotNull PsiBinaryExpression expression) {
                super.visitBinaryExpression(expression);
                check(expression);
            }

            @Override
            public void visitPolyadicExpression(@NotNull PsiPolyadicExpression expression) {
                super.visitPolyadicExpression(expression);
                // PsiBinaryExpression inherits from PsiPolyadicExpression
                // avoids double processing.
                if (!(expression instanceof PsiBinaryExpression)) check(expression);
            }

            private void check(PsiExpression expression) {
                IElementType op = OperatorUtils.getOperator(expression);

                if (op == null) return;
                if(!OperatorUtils.isLogicalOperator(op)) return;
                if (isSecondaryOperand(expression)) return;

                List<PsiExpression> operands = OperatorUtils.getOperands(expression);
                if (operands.stream().skip(1).noneMatch(this::containsSideEffectOrComputation)) return;

                // Sem fix quando nenhuma refatoração segura existe: o aviso continua, o fix não aparece.
                LocalQuickFix[] fixes;
                if (ReplaceLogicAsControlFlow.isApplicable(expression)) {
                    fixes = new LocalQuickFix[]{new ReplaceLogicAsControlFlow()};
                } else {
                    fixes = LocalQuickFix.EMPTY_ARRAY;
                }
                holder.registerProblem(expression, MESSAGE, ProblemHighlightType.WARNING, fixes);
            }

            // Operando não-inicial de uma expressão lógica pai: o aviso é delegado ao nó pai.
            private boolean isSecondaryOperand(PsiExpression expression) {
                PsiElement outermost = ExpressionUtils.climbParens(expression);
                if (!(outermost.getParent() instanceof PsiExpression parent)) return false;
                if (!OperatorUtils.isLogicalOperator(OperatorUtils.getOperator(parent))) return false;
                return OperatorUtils.getOperands(parent).indexOf(outermost) >= 1;
            }

            private boolean containsSideEffectOrComputation(PsiExpression operand) {
                if (isSideEffectOrComputation(operand)) return true;
                return PsiTreeUtil.findChildrenOfAnyType(
                                operand,
                                PsiCallExpression.class,
                                PsiAssignmentExpression.class,
                                PsiUnaryExpression.class)
                        .stream().anyMatch(this::isSideEffectOrComputation);
            }

            private boolean isSideEffectOrComputation(PsiElement element) {
                if (element instanceof PsiCallExpression || element instanceof PsiAssignmentExpression) return true;
                if (!(element instanceof PsiUnaryExpression unary)) return false;
                return OperatorUtils.isIncrementOrDecrement(unary.getOperationTokenType());
            }

        };
    }
}