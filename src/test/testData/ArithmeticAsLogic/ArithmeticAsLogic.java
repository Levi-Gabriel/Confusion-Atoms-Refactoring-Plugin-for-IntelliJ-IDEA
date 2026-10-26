package ArithmeticAsLogic;

public class ArithmeticAsLogic {

    boolean subtractionEqualsZero(int a, int b) {
        return a - b == 0;
    }

    boolean subtractionNotEqualsZero(int a, int b) {
        return a - b != 0;
    }

    boolean additionEqualsZero(int a, int b) {
        return a + b == 0;
    }

    boolean additionNotEqualsZero(int a, int b) {
        return a + b != 0;
    }

    boolean multiplicationEqualsZero(int a, int b) {
        return a * b == 0;
    }

    boolean multiplicationNotEqualsZero(int a, int b) {
        return a * b != 0;
    }

    boolean zeroOnTheLeft(int a, int b) {
        return 0 == a - b;
    }

    boolean parenthesizedArithmetic(int a, int b) {
        return (a - b) == 0;
    }

    boolean tripleMultiplication(int a, int b, int c) {
        return a * b * c == 0;
    }

    boolean nestedMultiplication(int a, int b, int c) {
        return (a * b) * c != 0;
    }

    boolean multiplicationWithSubtractionFactor(int a, int b, int c) {
        return a * (b - c) == 0;
    }

    boolean multiplicationWithAdditionFactor(int a, int b, int c) {
        return a * (b + c) != 0;
    }

    boolean subtractionWithParenthesizedOperand(int a, int b, int c) {
        return a - (b + c) == 0;
    }

}