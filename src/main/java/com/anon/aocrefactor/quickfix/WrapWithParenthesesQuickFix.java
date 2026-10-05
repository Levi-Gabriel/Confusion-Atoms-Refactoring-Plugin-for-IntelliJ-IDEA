package com.anon.aocrefactor.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WrapWithParenthesesQuickFix implements LocalQuickFix {

    private final List<TextRange> operandRanges; // ranges relative to the parent expression

    public WrapWithParenthesesQuickFix(PsiExpression parentExpression, List<PsiExpression> operands) {
        this.operandRanges = new ArrayList<>(operands.size());
        int parentStart = parentExpression.getTextRange().getStartOffset();
        for (PsiExpression operand : operands) {
            TextRange r = operand.getTextRange();
            operandRanges.add(r.shiftLeft(parentStart)); // relative to father's text
        }
    }

    @Override
    public @NotNull String getName() {
        return "Add parentheses for clarity";
    }

    @Override
    public @NotNull String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiExpression expression = (PsiExpression) descriptor.getPsiElement();
        String original = expression.getText();
        // Builds the new text from back to front,
        // so as not to invalidate offsets that have already been used
        StringBuilder sb = new StringBuilder(original);
        List<TextRange> sorted = new ArrayList<>(operandRanges);
        sorted.sort((a, b) -> b.getStartOffset() - a.getStartOffset());

        for (TextRange range : sorted) {
            String operandText = range.substring(original);
            sb.replace(range.getStartOffset(), range.getEndOffset(), "(" + operandText + ")");
        }

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiExpression wrapped = factory.createExpressionFromText(sb.toString(), expression);
        expression.replace(wrapped);
    }
}