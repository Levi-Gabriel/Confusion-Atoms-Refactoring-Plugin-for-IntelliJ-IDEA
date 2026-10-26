package PreIncrementDecrement;

public class PreIncrementDecrementAfter {

    int count;
    static int total;
    int[] values = new int[10];

    // local variables and parameters

    void assignmentWithPreIncrement(int a, int b) {
        b += 1;
        a = b;
    }

    void assignmentWithPreDecrement(int a, int b) {
        b -= 1;
        a = b;
    }

    void declarationInitializer(int b) {
        b += 1;
        int a = b;
    }

    int arrayIndexInReturn(int[] arr, int i) {
        i += 1;
        return arr[i];
    }

    void incrementAsLeftOperand(int a, int b) {
        a += 1;
        int result = a + b;
    }

    void incrementAsRightOperand(int a, int b) {
        a += 1;
        int result = b + a;
    }

    void incrementAsArgument(int i) {
        i += 1;
        System.out.println(i);
    }

    void incrementInIfCondition(int i) {
        i += 1;
        if (i > 0) {
            return;
        }
    }

    void incrementInSwitchSelector(int i) {
        i += 1;
        switch (i) {
            case 1:
                break;
            default:
                break;
        }
    }

    void incrementInFirstOperandOfAnd(int i, int j) {
        i += 1;
        boolean result = i > 0 && j > 0;
    }

    void incrementInTernaryCondition(int i) {
        i += 1;
        int result = i > 0 ? 1 : 2;
    }

    void twoSeparateStatements(int a, int b, int c, int d) {
        b += 1;
        a = b;
        d -= 1;
        c = d;
    }

    // fields

    void fieldInDeclaration() {
        count += 1;
        int a = count;
    }

    void fieldWithThis() {
        this.count += 1;
        int a = this.count;
    }

    void staticFieldWithClassName() {
        PreIncrementDecrement.total -= 1;
        int a = PreIncrementDecrement.total;
    }

    void fieldInIfCondition() {
        count += 1;
        if (count > 10) {
            return;
        }
    }

    int fieldAsArrayIndex() {
        count += 1;
        return values[count];
    }

    // without curly braces

    void ifBodyWithoutBraces(boolean c, int a, int b) {
        if (c) {
            b += 1;
            a = b;
        }
    }

    void elseBodyWithoutBraces(boolean c, int a, int b) {
        if (c) {
            a = 0;
        } else {
            b += 1;
            a = b;
        }
    }

    void loopBodyWithoutBraces(boolean c, int a, int b) {
        while (c) {
            b += 1;
            a = b;
        }
    }
}
