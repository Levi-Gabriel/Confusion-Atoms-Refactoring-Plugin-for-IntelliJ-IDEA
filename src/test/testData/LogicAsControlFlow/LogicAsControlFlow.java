package LogicAsControlFlow;

import java.util.function.Supplier;

public class LogicAsControlFlow {

    private boolean flag;

    // local variable

    void localVariableAnd(boolean a, String s) {
        boolean r = a && s.isEmpty();
    }

    void localVariableOr(boolean a, String s) {
        boolean r = a || s.isEmpty();
    }

    void localVariableChain(boolean a, String s, String t) {
        boolean r = a && s.isEmpty() && t.isEmpty();
    }

    void localVariableCallInComparison(boolean a, String s) {
        boolean r = a && s.length() > 0;
    }

    void localVariableIncrementOperand(boolean a, int count) {
        boolean r = a && ++count > 0;
    }

    // assignment

    void assignmentAnd(boolean a, String s) {
        boolean r;
        r = a && s.isEmpty();
    }

    void assignmentOrChain(boolean a, boolean b, String s) {
        boolean r;
        r = a || b || s.isEmpty();
    }

    void fieldAssignmentWithThis(boolean a, String s) {
        this.flag = a && s.isEmpty();
    }

    // return / lambda

    boolean returnAnd(boolean a, String s) {
        return a && s.isEmpty();
    }

    boolean returnOrChain(boolean a, boolean b, String s) {
        return a || b || s.isEmpty();
    }

    void lambdaBody(boolean a, String s) {
        Supplier<Boolean> f = () -> a && s.isEmpty();
    }

    //if

    void ifAndWithoutElse(boolean a, String s, int count) {
        if (a && s.isEmpty()) {
            count++;
        }
    }

    void ifAndWithElse(boolean a, String s, int count) {
        if (a && s.isEmpty()) {
            count++;
        } else {
            count--;
        }
    }

    void ifOrWithoutElse(boolean a, String s, int count) {
        if (a || s.isEmpty()) {
            count++;
        }
    }

    void ifOrWithElse(boolean a, String s, int count) {
        if (a || s.isEmpty()) {
            count++;
        } else {
            count--;
        }
    }

    void ifOrThreeOperands(boolean a, boolean b, String s, int count) {
        if (a || b || s.isEmpty()) {
            count++;
        }
    }

    // ---------------------------------------------------------------- while

    void whileAnd(boolean a, String s, int count) {
        while (a && s.isEmpty()) {
            count++;
        }
    }

    void whileOr(boolean a, String s, int count) {
        while (a || s.isEmpty()) {
            count++;
        }
    }
}