package OmittedCurlyBraces;

public class WithoutBraces {

    void ifThen(boolean cond, int x) {
        if (cond) {
            x++;
        }
    }

    void ifElse(boolean cond, int x) {
        if (cond) {
            x++;
        }
        else {
            x--;
        }
    }

    // "else if" is not reported itself; only the branches inside it are.
    void elseIf(boolean cond, int x) {
        if (cond) {
            x++;
        }
        else if (x > 0) {
            x--;
        }
        else {
            x = 0;
        }
    }

    void whileLoop(int x) {
        while (x < 10) {
            x++;
        }
    }

    void doWhileLoop(int x) {
        do {
            x++;
        } while (x < 10);
    }

    void forLoop(int x) {
        for (int i = 0; i < 3; i++) {
            x += i;
        }
    }

    void foreachLoop(int x, int[] values) {
        for (int v : values) {
            x += v;
        }
    }

    void ifInsideWhile(boolean cond, int x) {
        while (x < 10) { if (cond) {
            x++;
        }
        }
    }
}