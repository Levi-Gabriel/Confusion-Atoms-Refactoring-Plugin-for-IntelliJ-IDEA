package ChangeOfLiteralEncoding;

public class BitwiseDecimal {

    // &, |, ^

    void andWithDecimalOnRight(int flags) {
        int r = flags & 16;
    }

    void andWithDecimalOnLeft(int flags) {
        int r = 32 & flags;
    }

    void orWithDecimal(int flags) {
        int r = flags | 64;
    }

    void xorWithDecimal(int flags) {
        int r = flags ^ 128;
    }

    // shifts (only left operand)

    void shiftLeftWithDecimalOperand(int n) {
        int r = 16 << n;
    }

    void shiftRightWithDecimalOperand(int n) {
        int r = 64 >> n;
    }

    void unsignedShiftRightWithDecimalOperand(int n) {
        int r = 64 >>> n;
    }

    // combinations

    void decimalOnBothSides() {
        int r = 16 & 32;
    }

    void mixedBitwiseOperators(int flags) {
        int r = flags & 16 | 32;
    }

    void bitwiseInCondition(int flags) {
        if ((flags & 16) != 0) {
            return;
        }
    }
}