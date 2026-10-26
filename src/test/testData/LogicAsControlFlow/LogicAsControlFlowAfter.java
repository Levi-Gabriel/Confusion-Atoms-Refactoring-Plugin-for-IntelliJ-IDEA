package LogicAsControlFlow;

import java.util.function.Supplier;

public class LogicAsControlFlowAfter {

    private boolean flag;

    // local variable

    void localVariableAnd(boolean a, String s) {
        boolean r;
        if (!(a)) {
            r = false;
        } else {
            r = s.isEmpty();
        }
    }

    void localVariableOr(boolean a, String s) {
        boolean r;
        if (a) {
            r = true;
        } else {
            r = s.isEmpty();
        }
    }

    void localVariableChain(boolean a, String s, String t) {
        boolean r;
        if (!(a)) {
            r = false;
        } else if (!(s.isEmpty())) {
            r = false;
        } else {
            r = t.isEmpty();
        }
    }

    void localVariableCallInComparison(boolean a, String s) {
        boolean r;
        if (!(a)) {
            r = false;
        } else {
            r = s.length() > 0;
        }
    }

    void localVariableIncrementOperand(boolean a, int count) {
        boolean r;
        if (!(a)) {
            r = false;
        } else {
            r = ++count > 0;
        }
    }

    // assignment

    void assignmentAnd(boolean a, String s) {
        boolean r;
        if (!(a)) {
            r = false;
        } else {
            r = s.isEmpty();
        }
    }

    void assignmentOrChain(boolean a, boolean b, String s) {
        boolean r;
        if (a) {
            r = true;
        } else if (b) {
            r = true;
        } else {
            r = s.isEmpty();
        }
    }

    void fieldAssignmentWithThis(boolean a, String s) {
        if (!(a)) {
            this.flag = false;
        } else {
            this.flag = s.isEmpty();
        }
    }

    // return / lambda

    boolean returnAnd(boolean a, String s) {
        if (!(a)) return false;
        return s.isEmpty();
    }

    boolean returnOrChain(boolean a, boolean b, String s) {
        if (a) return true;
        if (b) return true;
        return s.isEmpty();
    }

    void lambdaBody(boolean a, String s) {
        Supplier<Boolean> f = () -> {
            if (!(a)) return false;
            return s.isEmpty();
        };
    }

    // if

    void ifAndWithoutElse(boolean a, String s, int count) {
        if (a) {
            if (s.isEmpty()) {
                count++;
            }
        }
    }

    void ifAndWithElse(boolean a, String s, int count) {
        if (a) {
            if (s.isEmpty()) {
                count++;
            } else {
                count--;
            }
        } else {
            count--;
        }
    }

    void ifOrWithoutElse(boolean a, String s, int count) {
        if (a) {
            count++;
        } else if (s.isEmpty()) {
            count++;
        }
    }

    void ifOrWithElse(boolean a, String s, int count) {
        if (a) {
            count++;
        } else if (s.isEmpty()) {
            count++;
        } else {
            count--;
        }
    }

    void ifOrThreeOperands(boolean a, boolean b, String s, int count) {
        if (a) {
            count++;
        } else if (b) {
            count++;
        } else if (s.isEmpty()) {
            count++;
        }
    }

    // while

    void whileAnd(boolean a, String s, int count) {
        while (a) {
            if (!(s.isEmpty())) {
                break;
            }
            count++;
        }
    }

    void whileOr(boolean a, String s, int count) {
        while (true) {
            if (!(a)) {
                if (!(s.isEmpty())) {
                    break;
                }
            }
            count++;
        }
    }
}