package ArithmeticAsLogic;

public class ArithmeticAsLogic {

    boolean subtractionEqualsZero(int a, int b) {
        return a == b;
    }

    boolean subtractionNotEqualsZero(int a, int b) {
        return a != b;
    }

    boolean additionEqualsZero(int a, int b) {
        return a == -(b);
    }

    boolean additionNotEqualsZero(int a, int b) {
        return a != -(b);
    }

    boolean multiplicationEqualsZero(int a, int b) {
        return a == 0 || b == 0;
    }

    boolean multiplicationNotEqualsZero(int a, int b) {
        return a != 0 && b != 0;
    }

    boolean zeroOnTheLeft(int a, int b) {
        return a == b;
    }

    boolean parenthesizedArithmetic(int a, int b) {
        return a == b;
    }

    boolean tripleMultiplication(int a, int b, int c) {
        return a == 0 || b == 0 || c == 0;
    }

    boolean nestedMultiplication(int a, int b, int c) {
        return a != 0 && b != 0 && c != 0;
    }

    boolean multiplicationWithSubtractionFactor(int a, int b, int c) {
        return a == 0 || b == c;
    }

    boolean multiplicationWithAdditionFactor(int a, int b, int c) {
        return a != 0 && b != -(c);
    }

    boolean subtractionWithParenthesizedOperand(int a, int b, int c) {
        return a == (b + c);
    }

}