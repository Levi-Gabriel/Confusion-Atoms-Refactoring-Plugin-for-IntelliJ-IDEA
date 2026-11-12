package PostIncrementDecrement;

public class NoFix {

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
}