package ChangeOfLiteralEncoding;

public class BitwiseDecimalHexAfter {

    //&, |, ^

    void andWithDecimalOnRight(int flags) {
        int r = flags & 0x10;
    }

    void andWithDecimalOnLeft(int flags) {
        int r = 0x20 & flags;
    }

    void orWithDecimal(int flags) {
        int r = flags | 0x40;
    }

    void xorWithDecimal(int flags) {
        int r = flags ^ 0x80;
    }

    // shifts (only left operand)

    void shiftLeftWithDecimalOperand(int n) {
        int r = 0x10 << n;
    }

    void shiftRightWithDecimalOperand(int n) {
        int r = 0x40 >> n;
    }

    void unsignedShiftRightWithDecimalOperand(int n) {
        int r = 0x40 >>> n;
    }

    //combinations

    void decimalOnBothSides() {
        int r = 0x10 & 0x20;
    }

    void mixedBitwiseOperators(int flags) {
        int r = flags & 0x10 | 0x20;
    }

    void bitwiseInCondition(int flags) {
        if ((flags & 0x10) != 0) {
            return;
        }
    }
}