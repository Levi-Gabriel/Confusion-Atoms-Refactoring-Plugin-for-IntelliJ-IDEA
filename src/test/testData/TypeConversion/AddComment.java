package TypeConversion;

public class AddComment {

    // int

    void intToByte(int i) {
        byte b = (byte) i;
    }

    void intToShort(int i) {
        short s = (short) i;
    }

    void intToChar(int i) {
        char c = (char) i;
    }

    void longToInt(long l) {
        int x = (int) l;
    }

    void longToByte(long l) {
        byte b = (byte) l;
    }

    void shortToByte(short s) {
        byte b = (byte) s;
    }

    void charToByte(char c) {
        byte b = (byte) c;
    }

    void charToShort(char c) {
        short s = (short) c;
    }

    void byteToChar(byte b) {
        char c = (char) b;
    }

    // float number

    void doubleToInt(double d) {
        int x = (int) d;
    }

    void doubleToLong(double d) {
        long x = (long) d;
    }

    void doubleToFloat(double d) {
        float f = (float) d;
    }

    void floatToInt(float f) {
        int x = (int) f;
    }

    void floatToLong(float f) {
        long x = (long) f;
    }

    void floatToByte(float f) {
        byte b = (byte) f;
    }

    void arithmeticOperand(double a) {
        int x = (int) (a * 1.5);
    }

    // problematic literals

    void literalOutOfRange() {
        byte b = (byte) 300;
    }

    void literalWithFraction() {
        int x = (int) 3.7;
    }

    void longLiteralOutOfRange() {
        int x = (int) 3000000000L;
    }

    // others
    byte inReturn(int i) {
        return (byte) i;
    }

    void inMethodArgument(int i) {
        System.out.println((byte) i);
    }

    void inIfCondition(int i) {
        if ((byte) i > 0) {
            return;
        }
    }

    void twoSeparateStatements(int i, double d) {
        byte b = (byte) i;
        int x = (int) d;
    }
}