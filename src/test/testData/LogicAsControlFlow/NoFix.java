package LogicAsControlFlow;

public class NoFix {

    // "||" in an if: the then branch would be duplicated, but it has more than 3 statements.
    void ifOrWithLargeThen(boolean a, String s, int count) {
        if (a || s.isEmpty()) {
            count++;
            count++;
            count++;
            count++;
        }
    }

    // "||" in an if with more than 4 operands: duplicating the then branch would add too much noise.
    void ifOrWithTooManyOperands(boolean a, boolean b, boolean c, boolean d, String s, int count) {
        if (a || b || c || d || s.isEmpty()) {
            count++;
        }
    }

    // "&&" in an if with an else: the else branch would be duplicated, but it has more than 3 statements.
    void ifAndWithLargeElse(boolean a, String s, int count) {
        if (a && s.isEmpty()) {
            count++;
        } else {
            count--;
            count--;
            count--;
            count--;
        }
    }

    // A chained "else if" counts as a large body.
    void ifAndWithElseIf(boolean a, boolean b, String s, int count) {
        if (a && s.isEmpty()) {
            count++;
        } else if (b) {
            count--;
        }
    }

    // Unsupported host: method argument.
    void methodArgument(boolean a, String s) {
        System.out.println(a && s.isEmpty());
    }

    // Unsupported host: declaration with more than one variable.
    void multipleDeclarators(boolean a, String s) {
        boolean x = a, y = a && s.isEmpty();
    }

    // Unsupported host: the left-hand side is not a simple variable reference.
    void arrayElementAssignment(boolean[] arr, boolean a, String s) {
        arr[0] = a && s.isEmpty();
    }

    // Unsupported host: the expression is the condition of a ternary.
    void ternaryCondition(boolean a, String s) {
        int x = a && s.isEmpty() ? 1 : 0;
    }

    // Unsupported host: return outside a code block.
    boolean returnWithoutBlock(boolean a, String s) {
        if (a) return a && s.isEmpty();
        return false;
    }
}