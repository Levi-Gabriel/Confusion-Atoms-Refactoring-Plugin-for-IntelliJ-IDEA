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
                                buildEncodedMessage(encoding, expression),
                                ProblemHighlightType.WEAK_WARNING,
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
                            buildBitwiseMessage(operand),
                            ProblemHighlightType.WARNING,
                            new ReplaceChangeOfLiteralEncoding(ReplaceChangeOfLiteralEncoding.TargetEncoding.HEX),
                            new ReplaceChangeOfLiteralEncoding(ReplaceChangeOfLiteralEncoding.TargetEncoding.BINARY)
                    );
                }
            }

            private static String buildBitwiseMessage(PsiExpression operand) {
                String operator;
                if (operand.getParent() instanceof PsiBinaryExpression binary) {
                    operator = " '" + binary.getOperationSign().getText() + "'";
                } else {
                    operator = "";
                }

                return "The decimal literal '" + operand.getText()
                        + "' is used as an operand of the bitwise/shift operator" + operator
                        + ", where the bit pattern matters more than the numeric value. "
                        + "Consider writing it in hexadecimal or binary.";
            }

            private static String buildEncodedMessage(String encoding, PsiLiteralExpression literal) {
                String reason = encoding.equals("octal")
                        ? "a leading zero changes the base, so it is easily misread as a decimal number"
                        : "the encoding hides the numeric value from the reader";

                String value = literal.getValue() instanceof Number n ? " (value " + n + ")" : "";

                return "The " + encoding + " literal '"
                        + literal.getText() + "'" + value + " is assigned to a variable; " + reason
                        + ". Consider writing it in decimal.";
            }
        };
    }
}