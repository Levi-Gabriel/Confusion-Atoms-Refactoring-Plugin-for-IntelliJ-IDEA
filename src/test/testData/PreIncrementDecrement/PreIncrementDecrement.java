package PreIncrementDecrement;

public class PreIncrementDecrement {

    int count;
    static int total;
    int[] values = new int[10];

    // local variables and parameters

    void assignmentWithPreIncrement(int a, int b) {
        a = ++b;
    }

    void assignmentWithPreDecrement(int a, int b) {
        a = --b;
    }

    void declarationInitializer(int b) {
        int a = ++b;
    }

    int arrayIndexInReturn(int[] arr, int i) {
        return arr[++i];
    }

    void incrementAsLeftOperand(int a, int b) {
        int result = ++a + b;
    }

    void incrementAsRightOperand(int a, int b) {
        int result = b + ++a;
    }

    void incrementAsArgument(int i) {
        System.out.println(++i);
    }

    void incrementInIfCondition(int i) {
        if (++i > 0) {
            return;
        }
    }

    void incrementInFirstOperandOfAnd(int i, int j) {
        boolean result = ++i > 0 && j > 0;
    }

    void incrementInTernaryCondition(int i) {
        int result = ++i > 0 ? 1 : 2;
    }

    void twoSeparateStatements(int a, int b, int c, int d) {
        a = ++b;
        c = --d;
    }

    // fields

    void fieldInDeclaration() {
        int a = ++count;
    }

    void fieldWithThis() {
        int a = ++this.count;
    }

    void staticFieldWithClassName() {
        int a = --PreIncrementDecrement.total;
    }

    void fieldInIfCondition() {
        if (++count > 10) {
            return;
        }
    }

    int fieldAsArrayIndex() {
        return values[++count];
    }
}

