package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.WrapWithParenthesesQuickFix;
import com.anon.aocrefactor.util.ExpressionUtils;
import com.anon.aocrefactor.util.OperatorUtils;
import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InfixOperatorPrecedenceInspection extends AbstractBaseJavaLocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            @NotNull ProblemsHolder holder,
            boolean isOnTheFly) {

        return new JavaElementVisitor() {
            @Override
            public void visitPolyadicExpression(@NotNull PsiPolyadicExpression expression) {
                super.visitPolyadicExpression(expression);
                checkExpression(expression, holder);
            }
        };
    }

    @SuppressWarnings("LogicAsControlFlow")
    private void checkExpression(PsiExpression expression, ProblemsHolder holder) {

        IElementType op = OperatorUtils.getOperator(expression);
        if (op == null) return;

        boolean isLogicalParent = OperatorUtils.isLogicalOperator(op);
        boolean isLowArithmeticParent = OperatorUtils.isLowPrecedenceArithmetic(op);
        if (!isLogicalParent && !isLowArithmeticParent) return;

        PsiElement parent = expression.getParent();
        if (parent instanceof PsiParenthesizedExpression) return;

        List<PsiExpression> arithmeticMismatches = new ArrayList<>();
        List<PsiExpression> logicalMismatches = new ArrayList<>();

        for (PsiExpression operand : OperatorUtils.getOperands(expression)) {
            IElementType childOp = OperatorUtils.getOperator(operand);
            if (childOp == null) continue;

            boolean arithmeticMismatch = isLowArithmeticParent && OperatorUtils.isHighPrecedenceArithmetic(childOp);
            boolean logicalMismatch = isLogicalParent && OperatorUtils.isLogicalOperator(childOp);

            if (arithmeticMismatch) {
                if (ExpressionUtils.isStringType(expression)) continue;
                arithmeticMismatches.add(operand);
            } else if (logicalMismatch) {
                logicalMismatches.add(operand);
            }
        }

        //arithmetic case
        if (!arithmeticMismatches.isEmpty()) {
            holder.registerProblem(
                    expression,
                    "Confusion atom: arithmetic operator precedence. Add parentheses for clarity.",
                    ProblemHighlightType.WARNING,
                    new WrapWithParenthesesQuickFix(expression, arithmeticMismatches)
            );
        }

        // logic case
        if (!logicalMismatches.isEmpty()) {
            holder.registerProblem(
                    expression,
                    "Confusion atom: logical operator precedence. Add parentheses for clarity.",
                    ProblemHighlightType.WARNING,
                    new WrapWithParenthesesQuickFix(expression, logicalMismatches)
            );
        }
    }
}