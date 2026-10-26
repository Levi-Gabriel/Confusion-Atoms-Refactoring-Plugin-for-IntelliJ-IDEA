package InfixOperatorPrecedence;

public class InfixOperatorPrecedenceAfter {

    // ---------------------------------------------------------------- aritmética

    void additionWithMultiplicationOnRight(int a, int b, int c) {
        int result = a + (b * c);
    }

    void additionWithMultiplicationOnLeft(int a, int b, int c) {
        int result = (a * b) + c;
    }

    void subtractionWithDivision(int a, int b, int c) {
        int result = a - (b / c);
    }

    void additionWithModulo(int a, int b, int c) {
        int result = a + (b % c);
    }

    void multiplicationOnBothSides(int a, int b, int c, int d) {
        int result = (a * b) + (c * d);
    }

    void polyadicAddition(int a, int b, int c, int d) {
        int result = a + (b * c) + d;
    }

    void multipleFactorsInProduct(int a, int b, int c, int d) {
        int result = a + (b * c * d);
    }

    void mixedAdditionAndSubtraction(int a, int b, int c, int d) {
        int result = a + (b * c) - d;
    }

    void arithmeticInAssignment(int a, int b, int c) {
        int result;
        result = a + (b * c);
    }

    void arithmeticAsArgument(int a, int b, int c) {
        int result = Math.abs(a + (b * c));
    }

    int arithmeticInReturn(int a, int b, int c) {
        return a + (b * c);
    }

    void arithmeticInsideComparison(int a, int b, int c) {
        boolean positive = a + (b * c) > 0;
    }

    void twoIndependentStatements(int a, int b, int c) {
        int first = a + (b * c);
        int second = (a * b) + c;
    }

    // ---------------------------------------------------------------- lógica

    void orWithAndOnRight(boolean a, boolean b, boolean c) {
        boolean result = a || (b && c);
    }

    void orWithAndOnLeft(boolean a, boolean b, boolean c) {
        boolean result = (a && b) || c;
    }

    void orWithAndOnBothSides(boolean a, boolean b, boolean c, boolean d) {
        boolean result = (a && b) || (c && d);
    }

    void polyadicOrWithAnd(boolean a, boolean b, boolean c, boolean d) {
        boolean result = a || (b && c) || d;
    }

    void polyadicAndInsideOr(boolean a, boolean b, boolean c, boolean d) {
        boolean result = (a && b && c) || d;
    }

    void logicalInCondition(boolean a, boolean b, boolean c) {
        if (a || (b && c)) {
            return;
        }
    }

    // ---------------------------------------------------------------- aritmética e lógica

    void arithmeticAndLogicalInSeparateStatements(int a, int b, int c, boolean d, boolean e, boolean f) {
        int sum = a + (b * c);
        boolean flag = d || (e && f);
    }
}