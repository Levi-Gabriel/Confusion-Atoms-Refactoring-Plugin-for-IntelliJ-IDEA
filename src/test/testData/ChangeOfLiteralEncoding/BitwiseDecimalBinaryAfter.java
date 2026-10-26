package ChangeOfLiteralEncoding;

public class BitwiseDecimalBinaryAfter {

    // &, |, ^

    void andWithDecimalOnRight(int flags) {
        int r = flags & 0b10000;
    }

    void andWithDecimalOnLeft(int flags) {
        int r = 0b100000 & flags;
    }

    void orWithDecimal(int flags) {
        int r = flags | 0b1000000;
    }

    void xorWithDecimal(int flags) {
        int r = flags ^ 0b10000000;
    }

    // shifts (only left operand)

    void shiftLeftWithDecimalOperand(int n) {
        int r = 0b10000 << n;
    }

    void shiftRightWithDecimalOperand(int n) {
        int r = 0b1000000 >> n;
    }

    void unsignedShiftRightWithDecimalOperand(int n) {
        int r = 0b1000000 >>> n;
    }

    // combinations

    void decimalOnBothSides() {
        int r = 0b10000 & 0b100000;
    }

    void mixedBitwiseOperators(int flags) {
        int r = flags & 0b10000 | 0b100000;
    }

    void bitwiseInCondition(int flags) {
        if ((flags & 0b10000) != 0) {
            return;
        }
    }
}