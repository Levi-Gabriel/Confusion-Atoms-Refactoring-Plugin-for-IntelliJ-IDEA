package ChangeOfLiteralEncoding;

public class NoWarning {


    void decimalInDeclarationAndAssignment() {
        int x = 31;
        x = 16;
    }

    void decimalInArithmeticAndComparison(int a) {
        int sum = a + 16;
        int product = a * 32;
        boolean greater = a > 16;
    }

    // O operando da direita de um shift não é reportado.
    void decimalAsShiftCount(int a) {
        int left = a << 4;
        int right = a >> 2;
        int unsigned = a >>> 1;
    }

    void encodedLiteralsInBitwise(int flags) {
        int first = flags & 0x0F;
        int second = flags | 0b1000;
        int third = flags ^ 017;
    }

    void hexInMethodArgument() {
        int r = Math.abs(0x10);
    }

    int hexInReturn() {
        return 0x10;
    }

    void hexInParentheses() {
        int x = (0x10);
    }

    void negativeHex() {
        int x = -0x10;
    }

    void hexInArrayInitializer() {
        int[] values = {0x10, 0x20};
    }

    void textThatLooksLikeALiteral() {
        String s = "0x10";
    }
}