package com.anon.aocrefactor.quickfix;

import com.anon.aocrefactor.util.ExpressionUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

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

        if (!(arithmeticSide instanceof PsiBinaryExpression arithmetic)) return;

        // Constrói a expressão lógica equivalente completa, recursivamente,
        // resolvendo qualquer nível de multiplicação encadeada de uma só vez
        String replacement = transformEqualsZero(arithmetic, isEquals);
        if (replacement == null) return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiExpression newExpr = factory.createExpressionFromText(replacement, expression);
        expression.replace(newExpr);
    }

    private String transformEqualsZero(PsiExpression expr, boolean isEquals) {
        expr = ExpressionUtils.unwrapParentheses(expr);
        String cmp = isEquals ? "==" : "!=";

        if (expr instanceof PsiBinaryExpression bin) {
            IElementType op = bin.getOperationTokenType();
            PsiExpression left = bin.getLOperand();
            PsiExpression right = bin.getROperand();

            if (right != null) {
                if (op == JavaTokenType.ASTERISK) {
                    String logical = isEquals ? "||" : "&&";
                    String leftPart = transformEqualsZero(left, isEquals);
                    String rightPart = transformEqualsZero(right, isEquals);
                    return String.format("%s %s %s", leftPart, logical, rightPart);
                    // primeiro caso base
                } else if (op == JavaTokenType.MINUS) {
                    return String.format("%s %s %s", left.getText(), cmp, right.getText());
                    // segundo caso base
                } else if (op == JavaTokenType.PLUS) {
                    return String.format("%s %s -(%s)", left.getText(), cmp, right.getText());
                }
            }
        }

        // Terceiro caso base: não é uma composição reconhecida (*, -, +),
        // apenas compara a expressão inteira com 0.
        return String.format("%s %s 0", expr.getText(), cmp);
    }
}