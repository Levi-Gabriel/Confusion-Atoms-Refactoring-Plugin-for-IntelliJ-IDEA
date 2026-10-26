package TypeConversion;


public class WrapMathFloor {

    void doubleToInt(double d) {
        int x = (int) d;
    }

    void doubleToLong(double d) {
        long x = (long) d;
    }

    void doubleToByte(double d) {
        byte b = (byte) d;
    }

    void doubleToShort(double d) {
        short s = (short) d;
    }

    void doubleToChar(double d) {
        char c = (char) d;
    }

    void floatToInt(float f) {
        int x = (int) f;
    }

    void floatToLong(float f) {
        long x = (long) f;
    }

    void arithmeticOperand(double a) {
        int x = (int) (a * 1.5);
    }

    void literalWithFraction() {
        int x = (int) 3.7;
    }

    void literalOutOfRange() {
        int x = (int) 1e10;
    }

    int inReturn(double d) {
        return (int) d;
    }

    void inMethodArgument(double d) {
        System.out.println((int) d);
    }

    void inIfCondition(double d) {
        if ((int) d > 0) {
            return;
        }
    }

    void twoSeparateStatements(double d, float f) {
        int x = (int) d;
        long y = (long) f;
    }
}
