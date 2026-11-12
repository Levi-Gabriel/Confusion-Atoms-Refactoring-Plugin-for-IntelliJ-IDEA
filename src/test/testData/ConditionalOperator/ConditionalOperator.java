package ConditionalOperator;

import java.util.List;

public class ConditionalOperator {

    private boolean extraCheese;
    private int diameter;

    // declaration

    void declarationInt(boolean c) {
        int x = c ? 1 : 2;
    }

    void declarationString(boolean c) {
        String s = c ? "yes" : "no";
    }

    void declarationDouble(boolean c) {
        double d = c ? 1.5 : 2.5;
    }

    void declarationBoolean(boolean c, boolean a, boolean b) {
        boolean r = c ? a : b;
    }

    void declarationWithComparison(int a, int b) {
        int max = a > b ? a : b;
    }

    void declarationWithCalls(String s) {
        int n = s.isEmpty() ? 0 : s.length();
    }

    void declarationWithGenericType(boolean c, List<String> a, List<String> b) {
        List<String> list = c ? a : b;
    }

    void declarationParenthesized(boolean c) {
        int x = (c ? 1 : 2);
    }

    // return

    int returnInt(boolean c) {
        return c ? 1 : 2;
    }

    String returnString(boolean c) {
        return c ? "yes" : "no";
    }

    int returnWithComparison(int a, int b) {
        return a > b ? a : b;
    }

    int returnParenthesized(boolean c) {
        return (c ? 1 : 2);
    }

    // ternary is only part of a larger expression (must not lose the rest)
    public int hashCode() {
        return (extraCheese ? 31 : 1) * diameter;
    }

    int returnWithOuterExpression(boolean c, int y) {
        return (c ? 1 : 2) * y;
    }

    void declarationWithOuterExpression(boolean c, int y) {
        int x = (c ? 1 : 2) * y;
    }

    // temp

    void argumentOfCall(boolean c) {
        System.out.println(c ? 1 : 2);
    }

    void stringArgument(boolean c) {
        System.out.println(c ? "yes" : "no");
    }

    void assignment(boolean c, int x) {
        x = c ? 1 : 2;
    }

    void arithmeticAssignment(boolean c, int x, int y) {
        y = x + (c ? 1 : 2);
    }

    void inIfCondition(boolean c, boolean a, boolean b) {
        if (c ? a : b) {
            return;
        }
    }

    void tempNameCollision(boolean c) {
        int temp = 0;
        System.out.println(temp + (c ? 1 : 2));
    }

    // others

    void twoSeparateStatements(boolean c) {
        int a = c ? 1 : 2;
        String b = c ? "x" : "y";
    }
}