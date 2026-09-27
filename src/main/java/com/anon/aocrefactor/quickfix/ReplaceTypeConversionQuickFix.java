package com.anon.aocrefactor.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class ReplaceTypeConversionQuickFix implements LocalQuickFix {

    public enum Mode {
        ADD_COMMENT,
        CLAMP_RANGE,
        WRAP_MATH_FLOOR
    }

    private final Mode mode;

    public ReplaceTypeConversionQuickFix(Mode mode) {
        this.mode = mode;
    }

    @Override
    public @NotNull String getName() {
        return switch (mode) {
            case ADD_COMMENT -> "Add comment flagging intentional narrowing conversion";
            case CLAMP_RANGE -> "Clamp value to target type's range";
            case WRAP_MATH_FLOOR -> "Wrap operand with Math.floor() to make truncation explicit";
        };
    }

    @Override
    public @NotNull String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project,
                         @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        if(!(element instanceof PsiTypeCastExpression castExpr) || !castExpr.isValid()) return;

        PsiTypeElement castTypeElement = castExpr.getCastType();
        PsiType castType = castTypeElement != null
                ? castTypeElement.getType() : null;
        PsiExpression operand = castExpr.getOperand();
        if (castType == null || operand == null) return;

        switch (mode) {
            case ADD_COMMENT -> applyAddComment(project, castExpr, operand, castType);
            case CLAMP_RANGE -> applyClampRange(project, operand, castType);
            case WRAP_MATH_FLOOR -> applyWrapMathFloor(project, operand, castType);
        }
    }

    private void applyAddComment(Project project, PsiTypeCastExpression castExpr,
                                 PsiExpression operand, PsiType castType) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiParserFacade parserFacade = PsiParserFacade.getInstance(project);

        String fromType = operand.getType() != null ? operand.getType().getPresentableText() : "?";
        String toType = castType.getPresentableText();
        String commentText = String.format(
                "// Narrowing conversion: %s -> %s (possible precision loss)",
                fromType, toType
        );
        PsiComment descriptiveComment = factory.createCommentFromText(commentText, null);
        // Sem supressão, o warning continua ativo mesmo após o comentário.
        // Este modo é documental, não altera comportamento nem silencia a inspeção.

        // Caso 1: cast dentro de um statement normal (método, bloco, etc.)
        PsiStatement parentStatement = PsiTreeUtil.getParentOfType(castExpr, PsiStatement.class);
        if (parentStatement != null) {
            insertBefore(parserFacade, parentStatement.getParent(), parentStatement, descriptiveComment);
            return;
        }

        // Caso 2: cast dentro de um inicializador de campo (sem statement ancestral)
        PsiField enclosingField = PsiTreeUtil.getParentOfType(castExpr, PsiField.class);
        if (enclosingField != null) {
            insertBefore(parserFacade, enclosingField.getParent(), enclosingField, descriptiveComment);
        }
    }

    private void insertBefore(PsiParserFacade parserFacade, PsiElement parent, PsiElement anchor,
                              PsiComment descriptiveComment) {
        if (parent == null) return;

        parent.addBefore(descriptiveComment, anchor);
        parent.addBefore(parserFacade.createWhiteSpaceFromText("\n"), anchor);
    }

    private void applyClampRange(Project project, PsiExpression operand, PsiType castType) {
        String[] bounds = rangeConstantsFor(castType);
        if (bounds == null) return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        String operandText = operand.getText();
        String minConst = bounds[0];
        String maxConst = bounds[1];

        String clampedText = String.format(
                "Math.max(%s, Math.min(%s, %s))",
                minConst, maxConst, operandText
        );
        PsiExpression clampedExpr = factory.createExpressionFromText(clampedText, operand);
        operand.replace(clampedExpr);
        // A inspeção agora reconhece esse padrão estruturalmente e não vai mais marcar o warning.
    }

    private void applyWrapMathFloor(Project project, PsiExpression operand, PsiType castType) {
        PsiType operandType = operand.getType();
        if (operandType == null) return;

        String operandTypeName = operandType.getCanonicalText();

        boolean isFloatingPoint;
        if (operandTypeName.equals("float")) {
            isFloatingPoint = true;
        } else if (operandTypeName.equals("double")) {
            isFloatingPoint = true;
        } else {
            isFloatingPoint = false;
        }
        if (!isFloatingPoint) return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        String operandText = operand.getText();
        PsiExpression floorCall = factory.createExpressionFromText(
                "Math.floor(" + operandText + ")", operand
        );

        operand.replace(floorCall);
        // Math.floor já está em SAFE_CONVERSION_METHODS na inspeção, então o warning
        // já some sozinho na próxima análise.
    }

    // Retorna {constante mínima, constante máxima} pro tipo alvo, ou null se não aplicável
    private String[] rangeConstantsFor(PsiType targetType) {
        return switch (targetType.getCanonicalText()) {
            case "byte"  -> new String[]{"Byte.MIN_VALUE", "Byte.MAX_VALUE"};
            case "short" -> new String[]{"Short.MIN_VALUE", "Short.MAX_VALUE"};
            case "char"  -> new String[]{"(int) Character.MIN_VALUE", "(int) Character.MAX_VALUE"};
            case "int"   -> new String[]{"Integer.MIN_VALUE", "Integer.MAX_VALUE"};
            case "long"  -> new String[]{"Long.MIN_VALUE", "Long.MAX_VALUE"};
            case "float" -> new String[]{"-Float.MAX_VALUE", "Float.MAX_VALUE"};
            default -> null;
        };
    }

}