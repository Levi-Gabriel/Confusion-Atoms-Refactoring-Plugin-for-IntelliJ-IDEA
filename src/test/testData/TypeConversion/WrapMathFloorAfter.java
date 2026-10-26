package TypeConversion;


public class WrapMathFloorAfter {

    void doubleToInt(double d) {
        int x = (int) Math.floor(d);
    }

    void doubleToLong(double d) {
        long x = (long) Math.floor(d);
    }

    void doubleToByte(double d) {
        byte b = (byte) Math.floor(d);
    }

    void doubleToShort(double d) {
        short s = (short) Math.floor(d);
    }

    void doubleToChar(double d) {
        char c = (char) Math.floor(d);
    }

    void floatToInt(float f) {
        int x = (int) Math.floor(f);
    }

    void floatToLong(float f) {
        long x = (long) Math.floor(f);
    }

    void arithmeticOperand(double a) {
        int x = (int) Math.floor((a * 1.5));
    }

    void literalWithFraction() {
        int x = (int) Math.floor(3.7);
    }

    void literalOutOfRange() {
        int x = (int) Math.floor(1e10);
    }

    int inReturn(double d) {
        return (int) Math.floor(d);
    }

    void inMethodArgument(double d) {
        System.out.println((int) Math.floor(d));
    }

    void inIfCondition(double d) {
        if ((int) Math.floor(d) > 0) {
            return;
        }
    }

    void twoSeparateStatements(double d, float f) {
        int x = (int) Math.floor(d);
        long y = (long) Math.floor(f);
    }
}