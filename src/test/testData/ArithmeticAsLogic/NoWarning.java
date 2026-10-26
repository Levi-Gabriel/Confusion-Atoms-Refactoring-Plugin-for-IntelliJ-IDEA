package ArithmeticAsLogic;

public class NoWarning {

    int compute(int x) {
        return x;
    }

    boolean comparisonWithNonZero(int a, int b) {
        return a - b == 1 || a + b != 5 || a * b == 2;
    }

    boolean plainOperandAgainstZero(int a) {
        return a == 0 || a != 0;
    }

    boolean relationalOperatorsAgainstZero(int a, int b) {
        return a - b > 0 || a + b >= 0 || a * b < 0 || a - b <= 0;
    }

    boolean arithmeticAgainstAnotherOperand(int a, int b, int c) {
        return a - b == c || a * b != c;
    }

    boolean divisionAndModulo(int a, int b) {
        return a / b == 0 || a % b != 0;
    }

    boolean methodCallAgainstZero(int a) {
        return compute(a) == 0;
    }
}