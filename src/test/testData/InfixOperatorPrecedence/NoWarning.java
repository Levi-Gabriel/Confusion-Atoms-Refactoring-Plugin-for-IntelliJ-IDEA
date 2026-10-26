package InfixOperatorPrecedence;

public class NoWarning {

    // ---------------------------------------------------------------- aritmética

    void samePrecedenceAddition(int a, int b, int c) {
        int result = a + b + c;
    }

    void samePrecedenceAdditionAndSubtraction(int a, int b, int c) {
        int result = a + b - c;
    }

    void samePrecedenceMultiplication(int a, int b, int c) {
        int result = a * b * c;
    }

    void samePrecedenceMultiplicationAndDivision(int a, int b, int c) {
        int result = a * b / c % 3;
    }

    void singleOperation(int a, int b) {
        int sum = a + b;
        int product = a * b;
    }

    void explicitParenthesesOnOperand(int a, int b, int c) {
        int first = a + (b * c);
        int second = (a * b) + c;
        int third = (a + b) * c;
    }

    // A expressão inteira já está entre parênteses.
    void parenthesizedWholeExpression(int a, int b, int c) {
        int result = (a + b * c);
    }

    // Concatenação de String não é reportada.
    void stringConcatenation(String label, int price, int quantity) {
        String text = label + price * quantity;
    }

    void comparisonOfArithmetic(int a, int b, int c, int d) {
        boolean result = a + b > c * d;
    }

    // ---------------------------------------------------------------- lógica

    void singleLogicalOperator(boolean a, boolean b) {
        boolean first = a && b;
        boolean second = a || b;
    }

    void sameLogicalOperatorRepeated(boolean a, boolean b, boolean c) {
        boolean first = a && b && c;
        boolean second = a || b || c;
    }

    void explicitParenthesesInLogic(boolean a, boolean b, boolean c, boolean d) {
        boolean first = a || (b && c);
        boolean second = (a && b) || c;
        boolean third = (a && b) || (c && d);
    }

    void relationalOperatorsWithLogic(int a, int b, int c, int d) {
        boolean result = a < b && c > d;
    }
}