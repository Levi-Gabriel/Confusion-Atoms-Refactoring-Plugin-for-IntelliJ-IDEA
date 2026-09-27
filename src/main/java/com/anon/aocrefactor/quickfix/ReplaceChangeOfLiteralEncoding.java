package com.anon.aocrefactor.quickfix;

import com.anon.aocrefactor.util.ExpressionUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class ReplaceChangeOfLiteralEncoding implements LocalQuickFix {

    public enum TargetEncoding {
        DECIMAL,
        HEX,
        BINARY
    }

    private final TargetEncoding target;

    public ReplaceChangeOfLiteralEncoding(TargetEncoding target) {
        this.target = target;
    }

    @Override
    public @NotNull String getName() {
        switch (target) {
            case DECIMAL:
                return "Convert literal to decimal";
            case HEX:
                return "Convert literal to hexadecimal";
            case BINARY:
                return "Convert literal to binary";
            default:
                return "Convert literal encoding";
        }
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Convert literal encoding";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        PsiLiteralExpression literal = (PsiLiteralExpression) element;

        Object value = literal.getValue();
        Number number = (Number) value;

        String newText;
        switch (target) {
            case DECIMAL:
                newText = ExpressionUtils.toDecimalLiteralText(literal, number);
                break;
            case HEX:
                newText = ExpressionUtils.toHexLiteralText(literal, number);
                break;
            case BINARY:
                newText = ExpressionUtils.toBinaryLiteralText(literal, number);
                break;
            default:
                return;
        }

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiExpression newExpr = factory.createExpressionFromText(newText, element);
        element.replace(newExpr);
    }
}