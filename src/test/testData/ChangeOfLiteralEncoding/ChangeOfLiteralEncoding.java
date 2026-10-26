package ChangeOfLiteralEncoding;

public class ChangeOfLiteralEncoding {

    // declaration

    void octalInLocalVariable() {
        int x = 017;
    }

    void hexInLocalVariable() {
        int x = 0x1F;
    }

    void binaryInLocalVariable() {
        int x = 0b101;
    }

    // assignment

    void octalInAssignment() {
        int x;
        x = 020;
    }

    void hexInAssignment() {
        int x;
        x = 0x20;
    }

    void binaryInAssignment() {
        int x;
        x = 0b10000;
    }

    void hexLongLiteral() {
        long x = 0x10L;
    }

    void hexInCompoundAssignment(int x) {
        x |= 0x10;
    }

    // others

    void twoLiteralsInSeparateStatements() {
        int a = 0x10;
        int b = 0b101;
    }
}