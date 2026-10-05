package com.anon.aocrefactor.util;

import com.intellij.psi.*;

public final class ExpressionUtils {

    public static boolean isZero(PsiExpression expr) {
        if (expr instanceof PsiLiteralExpression literal
                && literal.getValue() instanceof Number num) {
            return num.doubleValue() == 0;
        }
        return false;
    }

    // Remove parênteses externos: (expr) → expr
    public static PsiExpression unwrapParentheses(PsiExpression expr) {
        while (expr instanceof PsiParenthesizedExpression paren) {
            PsiExpression inner = paren.getExpression();
            if (inner == null) break;
            expr = inner;
        }
        return expr;
    }

    public static boolean isStringType(PsiExpression expr) {
        PsiType type = expr.getType();
        return type != null && type.equalsToText("java.lang.String");
    }

    public static boolean isOctalText(String text) {
        if (text == null || text.length() < 2 || text.charAt(0) != '0') {
            return false;
        }
        char second = text.charAt(1);
        if (second == 'x' || second == 'X' || second == 'b' || second == 'B' || second == '.') {
            return false;
        }
        for (int i = 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == 'L' || c == 'l') {
                break;
            }
            if (c < '0' || c > '7') {
                return false;
            }
        }
        return true;
    }

    public static boolean isDecimalLiteral(PsiExpression operand) {
        if (!(operand instanceof PsiLiteralExpression)) {
            return false;
        }
        String text = operand.getText();
        if (text == null || !Character.isDigit(text.charAt(0))) {
            return false;
        }
        return !text.startsWith("0") || text.equals("0");
    }

    public static char literalSuffix(String text) {
        if (text == null || text.isEmpty()) {
            return '\0';
        }
        char last = text.charAt(text.length() - 1);
        return (last == 'L' || last == 'l') ? last : '\0';
    }

    public static String toDecimalLiteralText(PsiLiteralExpression literal, Number value) {
        char suffix = literalSuffix(literal.getText());
        return value.longValue() + (suffix != '\0' ? String.valueOf(suffix) : "");
    }

    public static String toOctalLiteralText(PsiLiteralExpression literal, Number value) {
        char suffix = literalSuffix(literal.getText());
        boolean isLong = suffix != '\0';
        long longValue = value.longValue();
        String octalDigits = Long.toOctalString(isLong ? longValue : (int) longValue);
        return "0" + octalDigits + (isLong ? String.valueOf(suffix) : "");
    }

    public static boolean isHexText(String text) {
        return text != null && (text.startsWith("0x") || text.startsWith("0X"));
    }

    public static boolean isBinaryText(String text) {
        return text != null && (text.startsWith("0b") || text.startsWith("0B"));
    }

    /**
     * Checks whether the given expression is a hexadecimal integer literal (0x... / 0X...).
     */
    public static boolean isHexLiteral(PsiExpression expression) {
        if (!(expression instanceof PsiLiteralExpression)) {
            return false;
        }
        PsiLiteralExpression literal = (PsiLiteralExpression) expression;
        return isHexText(literal.getText());
    }

    public static String toHexLiteralText(PsiLiteralExpression literal, Number value) {
        boolean isLong = value instanceof Long;
        long longValue = value.longValue();
        String suffix = literal.getText().matches(".*[lL]$") ? (literal.getText().endsWith("L") ? "L" : "l") : "";

        String hexDigits = isLong
                ? Long.toHexString(longValue)
                : Integer.toHexString((int) longValue);

        return "0x" + hexDigits + suffix;
    }

    public static String toBinaryLiteralText(PsiLiteralExpression literal, Number value) {
        char suffix = literalSuffix(literal.getText());
        boolean isLong = suffix != '\0';
        long longValue = value.longValue();

        String binaryDigits = isLong
                ? Long.toBinaryString(longValue)
                : Integer.toBinaryString((int) longValue);

        return "0b" + binaryDigits + (isLong ? String.valueOf(suffix) : "");
    }
    public static PsiElement climbParens(PsiElement element) {
        PsiElement current = element;
        while (current.getParent() instanceof PsiParenthesizedExpression parenthesized) {
            current = parenthesized;
        }
        return current;
    }

}