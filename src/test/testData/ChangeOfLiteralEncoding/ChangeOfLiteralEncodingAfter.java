package ChangeOfLiteralEncoding;

public class ChangeOfLiteralEncodingAfter {

    // declaration

    void octalInLocalVariable() {
        int x = 15;
    }

    void hexInLocalVariable() {
        int x = 31;
    }

    void binaryInLocalVariable() {
        int x = 5;
    }

    // assignment

    void octalInAssignment() {
        int x;
        x = 16;
    }

    void hexInAssignment() {
        int x;
        x = 32;
    }

    void binaryInAssignment() {
        int x;
        x = 16;
    }

    void hexLongLiteral() {
        long x = 16L;
    }

    void hexInCompoundAssignment(int x) {
        x |= 16;
    }

    // others

    void twoLiteralsInSeparateStatements() {
        int a = 16;
        int b = 5;
    }
}