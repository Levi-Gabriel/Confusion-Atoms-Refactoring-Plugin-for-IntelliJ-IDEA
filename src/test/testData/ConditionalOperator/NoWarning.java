import java.util.List;

public class NoWarning {

    void ifElseInsteadOfTernary(boolean c) {
        int x;
        if (c) {
            x = 1;
        } else {
            x = 2;
        }
    }

    void wildcardIsNotATernary(List<? extends Number> numbers) {
        Number first = numbers.get(0);
    }

    void lambdaArrowIsNotATernary(Runnable r) {
        Runnable other = () -> r.run();
    }

    boolean logicalOperatorsOnly(boolean a, boolean b) {
        return a && !b || b;
    }
}