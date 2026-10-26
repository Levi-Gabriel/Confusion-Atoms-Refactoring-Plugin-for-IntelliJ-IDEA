package PostIncrementDecrement;


public class PostIncrementDecrementAfter {

    int count;
    static int total;
    int[] values = new int[10];

    // after

    void assignmentWithPostIncrement(int a, int b) {
        a = b;
        b += 1;
    }

    void assignmentWithPostDecrement(int a, int b) {
        a = b;
        b -= 1;
    }

    void declarationInitializer(int b) {
        int a = b;
        b += 1;
    }

    void assignmentToField(int i) {
        count = i;
        i += 1;
    }

    void fieldInDeclaration() {
        int a = count;
        count += 1;
    }

    void fieldWithThis() {
        int a = this.count;
        this.count += 1;
    }

    void staticFieldWithClassName() {
        int a = PostIncrementDecrement.total;
        PostIncrementDecrement.total -= 1;
    }

    void twoSeparateStatements(int a, int b, int c, int d) {
        a = b;
        b += 1;
        c = d;
        d -= 1;
    }

    // TEMP

    int arrayIndexInReturn(int[] arr, int i) {
        int oldI = i;
        i += 1;
        return arr[oldI];
    }

    int arrayIndexDecrement(int[] arr, int i) {
        int oldI = i;
        i -= 1;
        return arr[oldI];
    }

    void incrementAsLeftOperand(int a, int b) {
        int oldA = a;
        a += 1;
        int result = oldA + b;
    }

    void incrementAsRightOperand(int a, int b) {
        int oldA = a;
        a += 1;
        int result = b + oldA;
    }

    void incrementAsArgument(int i) {
        int oldI = i;
        i += 1;
        System.out.println(oldI);
    }

    void incrementInIfCondition(int i) {
        int oldI = i;
        i += 1;
        if (oldI > 0) {
            return;
        }
    }

    void incrementInSwitchSelector(int i) {
        int oldI = i;
        i += 1;
        switch (oldI) {
            case 1:
                break;
            default:
                break;
        }
    }

    void incrementInFirstOperandOfAnd(int i, int j) {
        int oldI = i;
        i += 1;
        boolean result = oldI > 0 && j > 0;
    }

    void incrementInTernaryCondition(int i) {
        int oldI = i;
        i += 1;
        int result = oldI > 0 ? 1 : 2;
    }

    void compoundAssignment(int a, int b) {
        int oldB = b;
        b += 1;
        a += oldB;
    }

    void qualifiedAssignmentTarget(int i) {
        int oldI = i;
        i += 1;
        this.count = oldI;
    }

    void arrayElementAssignmentTarget(int i) {
        int oldI = i;
        i += 1;
        values[0] = oldI;
    }

    void longVariable(long n) {
        long oldN = n;
        n += 1;
        long x = oldN * 2;
    }

    void tempNameAlreadyInUse(int i, int oldI) {
        int oldI1 = i;
        i += 1;
        int result = oldI1 + oldI;
    }

    int fieldAsArrayIndex() {
        int oldCount = count;
        count += 1;
        return values[oldCount];
    }

    void fieldInIfCondition() {
        int oldCount = count;
        count += 1;
        if (oldCount > 10) {
            return;
        }
    }
}