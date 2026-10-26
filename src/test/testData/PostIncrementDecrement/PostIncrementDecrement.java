package PostIncrementDecrement;

public class PostIncrementDecrement {

    int count;
    static int total;
    int[] values = new int[10];

    // AFTER

    void assignmentWithPostIncrement(int a, int b) {
        a = b++;
    }

    void assignmentWithPostDecrement(int a, int b) {
        a = b--;
    }

    void declarationInitializer(int b) {
        int a = b++;
    }

    void assignmentToField(int i) {
        count = i++;
    }

    void fieldInDeclaration() {
        int a = count++;
    }

    void fieldWithThis() {
        int a = this.count++;
    }

    void staticFieldWithClassName() {
        int a = PostIncrementDecrement.total--;
    }

    void twoSeparateStatements(int a, int b, int c, int d) {
        a = b++;
        c = d--;
    }

    // TEMP

    int arrayIndexInReturn(int[] arr, int i) {
        return arr[i++];
    }

    int arrayIndexDecrement(int[] arr, int i) {
        return arr[i--];
    }

    void incrementAsLeftOperand(int a, int b) {
        int result = a++ + b;
    }

    void incrementAsRightOperand(int a, int b) {
        int result = b + a++;
    }

    void incrementAsArgument(int i) {
        System.out.println(i++);
    }

    void incrementInIfCondition(int i) {
        if (i++ > 0) {
            return;
        }
    }

    void incrementInFirstOperandOfAnd(int i, int j) {
        boolean result = i++ > 0 && j > 0;
    }

    void incrementInTernaryCondition(int i) {
        int result = i++ > 0 ? 1 : 2;
    }

    void compoundAssignment(int a, int b) {
        a += b++;
    }

    void qualifiedAssignmentTarget(int i) {
        this.count = i++;
    }

    void arrayElementAssignmentTarget(int i) {
        values[0] = i++;
    }

    void longVariable(long n) {
        long x = n++ * 2;
    }

    void tempNameAlreadyInUse(int i, int oldI) {
        int result = i++ + oldI;
    }

    int fieldAsArrayIndex() {
        return values[count++];
    }

    void fieldInIfCondition() {
        if (count++ > 10) {
            return;
        }
    }


}