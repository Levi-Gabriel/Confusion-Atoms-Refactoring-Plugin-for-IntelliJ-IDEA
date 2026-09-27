package com.anon.aocrefactor.util;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;

public final class FixUtils {

    // Retorna o elemento se já for do tipo T, senão procura o primeiro filho desse tipo.
    public static <T extends PsiElement> T findSelfOrChild(PsiElement element, Class<T> type) {
        if (type.isInstance(element)) return type.cast(element);
        return PsiTreeUtil.findChildOfType(element, type);
    }

    public static boolean isValidIncrementDecrementContext(PsiElement parent) {
        return parent instanceof PsiAssignmentExpression
                || parent instanceof PsiLocalVariable
                || parent instanceof PsiBinaryExpression
                || parent instanceof PsiPolyadicExpression
                || parent instanceof PsiExpressionList
                || parent instanceof PsiArrayAccessExpression
                || parent instanceof PsiReturnStatement;
    }

    // Pra highlight de ++/-- cobrir a expressão pai inteira quando fizer sentido

    public static PsiElement getIncrementDecrementHighlightTarget(PsiElement parent, PsiExpression expression) {
        if (parent instanceof PsiAssignmentExpression assignment) return assignment;
        if (parent instanceof PsiLocalVariable localVar) return localVar;
        if (parent instanceof PsiBinaryExpression binary) return binary;
        if (parent instanceof PsiPolyadicExpression polyadic) return polyadic;
        return expression;
    }

}