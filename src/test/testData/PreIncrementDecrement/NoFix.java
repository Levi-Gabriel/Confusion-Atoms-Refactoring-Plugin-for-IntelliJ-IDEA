package PreIncrementDecrement;

public class NoFix {

    int count;
    int[] values = new int[10];
    Point point = new Point();

    static class Point {
        int x;
    }

    interface IntSource {
        int get();
    }

    // ---------------------------------------------------------------- variable used more than once

    void variableUsedTwice(int i) {
        int x = i + ++i;
    }

    void variableUsedTwiceDecrement(int i) {
        int x = i + --i;
    }

    // ---------------------------------------------------------------- evaluated more than once / cannot be moved

    void whileCondition(int i) {
        while (++i < 10) {
        }
    }

    void doWhileCondition(int i) {
        do {
        } while (++i < 10);
    }

    void forCondition(int i) {
        for (int j = 0; ++i < 10; j++) {
        }
    }

    void forInitialization(int i) {
        int j;
        for (j = ++i; j < 10; j++) {
        }
    }

    void forUpdate(int i) {
        for (int j = 0; j < 10; j = ++i) {
        }
    }

    void assertStatement(int i) {
        assert ++i > 0;
    }

    void localDeclaredInSameStatement() {
        int a = 0, b = ++a;
    }

    // ---------------------------------------------------------------- unsupported operand

    void arrayElementOperand(int[] arr) {
        int a = ++arr[0];
    }

    void operandQualifiedByOtherObject() {
        int a = ++point.x;
    }

    // ---------------------------------------------------------------- field with possible side effects

    void fieldInAssignment(int a) {
        a = ++count;
    }

    void fieldInMethodCall() {
        int a = Math.abs(++count);
    }

}