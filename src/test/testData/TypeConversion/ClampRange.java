package TypeConversion;

public class ClampRange {

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

    void parenthesizedOperand(int x, int y) {
        byte b = (byte) (x + y);
    }

    void literalOutOfRange() {
        byte b = (byte) 300;
    }

    void longLiteralOutOfRange() {
        int x = (int) 3000000000L;
    }

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

    void twoSeparateStatements(int i, long l) {
        byte b = (byte) i;
        int x = (int) l;
    }
}
