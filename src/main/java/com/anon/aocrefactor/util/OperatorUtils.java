package com.anon.aocrefactor.util;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class OperatorUtils {

    public static IElementType getOperator(PsiElement element) {
        if (element instanceof PsiBinaryExpression bin) {
            return bin.getOperationTokenType();
        }
        if (element instanceof PsiPolyadicExpression poly) {
            return poly.getOperationTokenType();
        }
        return null;
    }

    public static List<PsiExpression> getOperands(PsiExpression expression) {
        List<PsiExpression> operands = new ArrayList<>();
        if (expression instanceof PsiBinaryExpression bin) {
            operands.add(bin.getLOperand());
            operands.add(bin.getROperand());
        } else if (expression instanceof PsiPolyadicExpression poly) {
            operands.addAll(List.of(poly.getOperands()));
        }
        operands.removeIf(Objects::isNull);
        return operands;
    }

    public static boolean isLogicalOperator(IElementType op) {
        return op == JavaTokenType.ANDAND || op == JavaTokenType.OROR;
    }

    public static boolean isEqualityOperator(IElementType op) {
        return op == JavaTokenType.EQEQ || op == JavaTokenType.NE;
    }

    public static boolean isHighPrecedenceArithmetic(IElementType op) {
        return op == JavaTokenType.ASTERISK || op == JavaTokenType.DIV || op == JavaTokenType.PERC;
    }

    public static boolean isLowPrecedenceArithmetic(IElementType op) {
        return op == JavaTokenType.PLUS || op == JavaTokenType.MINUS;
    }

    public static boolean isIncrementOrDecrement(IElementType op) {
        return op == JavaTokenType.PLUSPLUS || op == JavaTokenType.MINUSMINUS;
    }

}