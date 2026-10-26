package TypeConversion;

public class ClampRangeAfter {

    void intToByte(int i) {
        byte b = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, i));
    }

    void intToShort(int i) {
        short s = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, i));
    }

    void intToChar(int i) {
        char c = (char) Math.max((int) Character.MIN_VALUE, Math.min((int) Character.MAX_VALUE, i));
    }

    void longToInt(long l) {
        int x = (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, l));
    }

    void longToByte(long l) {
        byte b = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, l));
    }

    void shortToByte(short s) {
        byte b = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, s));
    }

    void charToByte(char c) {
        byte b = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, c));
    }

    void charToShort(char c) {
        short s = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, c));
    }

    void byteToChar(byte b) {
        char c = (char) Math.max((int) Character.MIN_VALUE, Math.min((int) Character.MAX_VALUE, b));
    }

    void parenthesizedOperand(int x, int y) {
        byte b = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, (x + y)));
    }

    void literalOutOfRange() {
        byte b = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, 300));
    }

    void longLiteralOutOfRange() {
        int x = (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, 3000000000L));
    }

    byte inReturn(int i) {
        return (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, i));
    }

    void inMethodArgument(int i) {
        System.out.println((byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, i)));
    }

    void inIfCondition(int i) {
        if ((byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, i)) > 0) {
            return;
        }
    }

    void twoSeparateStatements(int i, long l) {
        byte b = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, i));
        int x = (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, l));
    }
}