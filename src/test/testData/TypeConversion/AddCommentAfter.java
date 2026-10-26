package TypeConversion;

public class AddCommentAfter {

    // int

    void intToByte(int i) {
        // Narrowing conversion: int -> byte (possible precision loss)
        byte b = (byte) i;
    }

    void intToShort(int i) {
        // Narrowing conversion: int -> short (possible precision loss)
        short s = (short) i;
    }

    void intToChar(int i) {
        // Narrowing conversion: int -> char (possible precision loss)
        char c = (char) i;
    }

    void longToInt(long l) {
        // Narrowing conversion: long -> int (possible precision loss)
        int x = (int) l;
    }

    void longToByte(long l) {
        // Narrowing conversion: long -> byte (possible precision loss)
        byte b = (byte) l;
    }

    void shortToByte(short s) {
        // Narrowing conversion: short -> byte (possible precision loss)
        byte b = (byte) s;
    }

    void charToByte(char c) {
        // Narrowing conversion: char -> byte (possible precision loss)
        byte b = (byte) c;
    }

    void charToShort(char c) {
        // Narrowing conversion: char -> short (possible precision loss)
        short s = (short) c;
    }

    void byteToChar(byte b) {
        // Narrowing conversion: byte -> char (possible precision loss)
        char c = (char) b;
    }

    // float number

    void doubleToInt(double d) {
        // Narrowing conversion: double -> int (possible precision loss)
        int x = (int) d;
    }

    void doubleToLong(double d) {
        // Narrowing conversion: double -> long (possible precision loss)
        long x = (long) d;
    }

    void doubleToFloat(double d) {
        // Narrowing conversion: double -> float (possible precision loss)
        float f = (float) d;
    }

    void floatToInt(float f) {
        // Narrowing conversion: float -> int (possible precision loss)
        int x = (int) f;
    }

    void floatToLong(float f) {
        // Narrowing conversion: float -> long (possible precision loss)
        long x = (long) f;
    }

    void floatToByte(float f) {
        // Narrowing conversion: float -> byte (possible precision loss)
        byte b = (byte) f;
    }

    void arithmeticOperand(double a) {
        // Narrowing conversion: double -> int (possible precision loss)
        int x = (int) (a * 1.5);
    }

    // problematic literals

    void literalOutOfRange() {
        // Narrowing conversion: int -> byte (possible precision loss)
        byte b = (byte) 300;
    }

    void literalWithFraction() {
        // Narrowing conversion: double -> int (possible precision loss)
        int x = (int) 3.7;
    }

    void longLiteralOutOfRange() {
        // Narrowing conversion: long -> int (possible precision loss)
        int x = (int) 3000000000L;
    }

    // others
    byte inReturn(int i) {
        // Narrowing conversion: int -> byte (possible precision loss)
        return (byte) i;
    }

    void inMethodArgument(int i) {
        // Narrowing conversion: int -> byte (possible precision loss)
        System.out.println((byte) i);
    }

    void inIfCondition(int i) {
        // Narrowing conversion: int -> byte (possible precision loss)
        if ((byte) i > 0) {
            return;
        }
    }

    void twoSeparateStatements(int i, double d) {
        // Narrowing conversion: int -> byte (possible precision loss)
        byte b = (byte) i;
        // Narrowing conversion: double -> int (possible precision loss)
        int x = (int) d;
    }
}