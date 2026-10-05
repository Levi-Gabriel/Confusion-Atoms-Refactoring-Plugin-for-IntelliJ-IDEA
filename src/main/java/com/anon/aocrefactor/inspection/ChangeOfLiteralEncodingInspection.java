package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.ReplaceChangeOfLiteralEncoding;
import com.anon.aocrefactor.util.ExpressionUtils;
import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class ChangeOfLiteralEncodingInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            @NotNull ProblemsHolder holder,
            boolean isOnTheFly) {

        return new JavaElementVisitor() {

            // Case 1 - literal used in assignment/declation
            @Override
            public void visitLiteralExpression(@NotNull PsiLiteralExpression expression) {
                super.visitLiteralExpression(expression);

                String text = expression.getText();
                String encoding = null;

                if (ExpressionUtils.isOctalText(text)) {
                    encoding = "octal";
                } else if (ExpressionUtils.isHexText(text)) {
                    encoding = "hexadecimal";
                } else if (ExpressionUtils.isBinaryText(text)) {
                    encoding = "binary";
                }

                if (encoding != null) {
                    PsiElement parent = expression.getParent();
                    if (parent instanceof PsiAssignmentExpression
                            || parent instanceof PsiLocalVariable
                            || parent instanceof PsiField) {
                        holder.registerProblem(
                                expression,
                                "Confusion atom: change of literal encoding (" + encoding
                                        + " literal used in assignment/variable).",
                                ProblemHighlightType.WARNING,
                                new ReplaceChangeOfLiteralEncoding(ReplaceChangeOfLiteralEncoding.TargetEncoding.DECIMAL)
                        );
                    }
                }
            }
            // Case 2 - decimal literal used in bitwise/shift operation
            @Override
            public void visitBinaryExpression(@NotNull PsiBinaryExpression expression) {
                super.visitBinaryExpression(expression);

                IElementType op = expression.getOperationTokenType();

                if (op.equals(JavaTokenType.AND)
                        || op.equals(JavaTokenType.OR)
                        || op.equals(JavaTokenType.XOR)) {

                    PsiExpression leftOperand = expression.getLOperand();
                    PsiExpression rightOperand = expression.getROperand();

                    checkBitwiseOperand(leftOperand);
                    checkBitwiseOperand(rightOperand);
                }

                if (op.equals(JavaTokenType.LTLT)
                        || op.equals(JavaTokenType.GTGT)
                        || op.equals(JavaTokenType.GTGTGT)) {

                    // since it's a bitwise shift, it doesn't matter if the right operand is decimal
                    PsiExpression leftOperand = expression.getLOperand();
                    checkBitwiseOperand(leftOperand);
                }
            }

            private void checkBitwiseOperand(PsiExpression operand) {

                if (ExpressionUtils.isDecimalLiteral(operand)) {
                    holder.registerProblem(
                            operand,
                            "Confusion atom: change of literal encoding (bitwise/shift operation with decimal literal).",
                            ProblemHighlightType.WARNING,
                            new ReplaceChangeOfLiteralEncoding(ReplaceChangeOfLiteralEncoding.TargetEncoding.HEX),
                            new ReplaceChangeOfLiteralEncoding(ReplaceChangeOfLiteralEncoding.TargetEncoding.BINARY)
                    );
                }
            }
        };
    }
}