package com.anon.aocrefactor.quickfix;

import com.anon.aocrefactor.util.ExpressionUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ReplaceArithmeticAsLogicQuickFix implements LocalQuickFix {

    @Override
    public @NotNull String getName() {
        return "Replace with equivalent logical expression";
    }

    @Override
    public @NotNull String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project,
                         @NotNull ProblemDescriptor descriptor) {

        PsiBinaryExpression expression = (PsiBinaryExpression) descriptor.getPsiElement();

        IElementType equalityOp = expression.getOperationTokenType();
        boolean isEquals = equalityOp == JavaTokenType.EQEQ;

        PsiExpression lhs = expression.getLOperand();
        PsiExpression rhs = expression.getROperand();
        if (rhs == null) return;

        PsiExpression arithmeticSide = ExpressionUtils.isZero(rhs) ? lhs : rhs;
        arithmeticSide = ExpressionUtils.unwrapParentheses(arithmeticSide);

        if (!(arithmeticSide instanceof PsiPolyadicExpression arithmetic)) return;

        // Recursively constructs the complete equivalent logical expression,
        // resolving any level of nested multiplication in a single step.
        String replacement = transformEqualsZero(arithmetic, isEquals);
        if (replacement == null) return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiExpression newExpr = factory.createExpressionFromText(replacement, expression);
        expression.replace(newExpr);
    }

    private String transformEqualsZero(PsiExpression expr, boolean isEquals) {
        expr = ExpressionUtils.unwrapParentheses(expr);
        String cmp = isEquals ? "==" : "!=";

        if (expr instanceof PsiPolyadicExpression poly) {
            IElementType op = poly.getOperationTokenType();
            PsiExpression[] operands = poly.getOperands();

            // a product is zero when any factor is zero
            if (op == JavaTokenType.ASTERISK && operands.length >= 2) {
                String logical = isEquals ? "||" : "&&";
                return Arrays.stream(operands)
                        .map(operand -> transformEqualsZero(operand, isEquals))
                        .collect(Collectors.joining(" " + logical + " "));
            }

            if (operands.length == 2) {
                String left = operands[0].getText();
                String right = operands[1].getText();
                if (op == JavaTokenType.MINUS) {
                    return String.format("%s %s %s", left, cmp, right);
                }
                if (op == JavaTokenType.PLUS) {
                    return String.format("%s %s -(%s)", left, cmp, right);
                }
            }
        }

        // not a recognized composition: compare the whole expression with 0
        return String.format("%s %s 0", expr.getText(), cmp);
    }
}