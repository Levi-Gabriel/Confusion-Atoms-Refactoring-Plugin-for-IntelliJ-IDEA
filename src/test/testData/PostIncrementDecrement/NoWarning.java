package PostIncrementDecrement;

public class NoWarning {


    void compoundAssignment(int i) {
        i += 1;
        i -= 1;
    }

    void otherUnaryOperators(int i, boolean flag) {
        int negated = -i;
        int plain = +i;
        int inverted = ~i;
        boolean not = !flag;
    }

    void standaloneStatements(int i) {
        i++;
        i--;
    }

    void forLoopUpdate(int n) {
        for (int j = 0; j < n; j++) {
        }
        for (int j = n; j > 0; j--) {
        }
    }
}
