package PostIncrementDecrement;

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

    //evaluated more than once / cannot be moved

    void whileCondition(int i) {
        while (i++ < 10) {
        }
    }

    void doWhileCondition(int i) {
        do {
        } while (i++ < 10);
    }

    void forCondition(int i) {
        for (int j = 0; i++ < 10; j += 1) {
        }
    }

    void forInitialization(int i) {
        int j;
        for (j = i++; j < 10; j += 1) {
        }
    }

    void forUpdate(int i) {
        for (int j = 0; j < 10; j = i++) {
        }
    }

    // unsupported operand

    void arrayElementOperand(int[] arr) {
        int a = arr[0]++;
    }

    void operandQualifiedByOtherObject() {
        int a = point.x++;
    }

    // field with possible side effects

    void fieldInAssignment(int a) {
        a = count++;
    }

    void fieldInMethodCall() {
        int a = Math.abs(count++);
    }

    void fieldWithOtherIncrement(int other) {
        int a = count++ + --other;
    }
}