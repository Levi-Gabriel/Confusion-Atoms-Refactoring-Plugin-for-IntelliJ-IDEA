package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.ReplaceArithmeticAsLogicQuickFix;
import com.anon.aocrefactor.util.ExpressionUtils;
import com.anon.aocrefactor.util.OperatorUtils;
import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class ArithmeticAsLogicInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            @NotNull ProblemsHolder holder,
            boolean isOnTheFly) {

        return new JavaElementVisitor() {
            @Override
            public void visitBinaryExpression(@NotNull PsiBinaryExpression expression) {
                super.visitBinaryExpression(expression);

                IElementType op = expression.getOperationTokenType();
                if (!OperatorUtils.isEqualityOperator(op)) return;

                PsiExpression lhs = expression.getLOperand();
                PsiExpression rhs = expression.getROperand();
                if (rhs == null) return;

                boolean lhsIsZero = ExpressionUtils.isZero(lhs);
                boolean rhsIsZero = ExpressionUtils.isZero(rhs);
                if (!lhsIsZero && !rhsIsZero) return;
                PsiExpression arithmeticSide = rhsIsZero ? lhs : rhs;
                arithmeticSide = ExpressionUtils.unwrapParentheses(arithmeticSide);

                if (!isArithmeticAsLogicExpression(arithmeticSide)) return;

                holder.registerProblem(
                        expression,
                        describe(arithmeticSide),
                        ProblemHighlightType.WEAK_WARNING,
                        new ReplaceArithmeticAsLogicQuickFix()
                );
            }
        };
    }

    private boolean isArithmeticAsLogicExpression(PsiExpression expr) {
        if (expr instanceof PsiPolyadicExpression poly) {
            IElementType op = poly.getOperationTokenType();
            if (op == JavaTokenType.ASTERISK) return true;
            if(poly instanceof PsiBinaryExpression) return (OperatorUtils.isLowPrecedenceArithmetic(op));
            }
        return false;
    }
    private static String describe(PsiExpression arithmetic) {
        IElementType op = ((PsiPolyadicExpression) arithmetic).getOperationTokenType();
        String kind;
        if (op == JavaTokenType.ASTERISK) {
            kind = "Multiplication";
        } else {
            String _temp;
            if (op == JavaTokenType.MINUS) {
                _temp = "Subtraction";
            } else {
                _temp = "Addition";
            }
            kind = _temp;
        }
        return kind + " compared to zero is used as a logical expression";
    }
}