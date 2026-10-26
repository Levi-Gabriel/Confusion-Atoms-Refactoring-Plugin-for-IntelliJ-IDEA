package OmittedCurlyBraces;

public class NoWarning {

    void withBlocks(boolean cond, int x, int[] values) {
        if (cond) {
            x++;
        } else if (x > 0) {
            x--;
        } else {
            x = 0;
        }
        while (x < 10) {
            x++;
        }
        do {
            x++;
        } while (x < 10);
        for (int i = 0; i < 3; i++) {
            x += i;
        }
        for (int v : values) {
            x += v;
        }
    }

    void emptyStatement(boolean cond) {
        if (cond);
    }

    void terminalStatements(boolean cond, int x, int[] values) {
        if (cond) return;
        for (int v : values) continue;
        while (x < 10) break;
        if (cond) throw new IllegalStateException();
    }
}