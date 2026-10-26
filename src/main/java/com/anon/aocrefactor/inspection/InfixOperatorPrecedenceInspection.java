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

    private static final String LOGICAL_MESSAGE =
            "'&&' binds tighter than '||'";

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

        // Arithmetic case: e.g. "a + b * c" -> the product is evaluated before the sum.
        if (!arithmeticMismatches.isEmpty()) {
            holder.registerProblem(
                    expression,
                    buildArithmeticMessage(expression),
                    ProblemHighlightType.WEAK_WARNING,
                    new WrapWithParenthesesQuickFix(expression, arithmeticMismatches)
            );
        }

        // Logical case: e.g. "a || b && c" -> the "&&" group is evaluated before the "||".
        if (!logicalMismatches.isEmpty()) {
            holder.registerProblem(
                    expression,
                    LOGICAL_MESSAGE,
                    ProblemHighlightType.WEAK_WARNING,
                    new WrapWithParenthesesQuickFix(expression, logicalMismatches)
            );
        }
    }

    // Only the surrounding operator varies: "*", "/" and "%" always bind tighter than "+" and "-".
    private static String buildArithmeticMessage(PsiExpression expression) {
        return "'*', '/' and '%' bind tighter than '"
                + operatorSign(expression) + "'";
    }

    // Operator text of a binary/polyadic expression (the sign between its first two operands), "" if unavailable.
    private static String operatorSign(PsiExpression expression) {
        if (!(expression instanceof PsiPolyadicExpression polyadic)) return "";
        PsiExpression[] operands = polyadic.getOperands();
        if (operands.length < 2) return "";
        PsiJavaToken token = polyadic.getTokenBeforeOperand(operands[1]);
        return token == null ? "" : token.getText();
    }
}