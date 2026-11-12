package ConditionalOperator;

import java.util.List;

public class ConditionalOperatorAfter {

    private boolean extraCheese;
    private int diameter;

    // declaration

    void declarationInt(boolean c) {
        int x;
        if (c) {
            x = 1;
        } else {
            x = 2;
        }
    }

    void declarationString(boolean c) {
        String s;
        if (c) {
            s = "yes";
        } else {
            s = "no";
        }
    }

    void declarationDouble(boolean c) {
        double d;
        if (c) {
            d = 1.5;
        } else {
            d = 2.5;
        }
    }

    void declarationBoolean(boolean c, boolean a, boolean b) {
        boolean r;
        if (c) {
            r = a;
        } else {
            r = b;
        }
    }

    void declarationWithComparison(int a, int b) {
        int max;
        if (a > b) {
            max = a;
        } else {
            max = b;
        }
    }

    void declarationWithCalls(String s) {
        int n;
        if (s.isEmpty()) {
            n = 0;
        } else {
            n = s.length();
        }
    }

    void declarationWithGenericType(boolean c, List<String> a, List<String> b) {
        List<String> list;
        if (c) {
            list = a;
        } else {
            list = b;
        }
    }

    void declarationParenthesized(boolean c) {
        int x;
        if (c) {
            x = 1;
        } else {
            x = 2;
        }
    }

    // return

    int returnInt(boolean c) {
        if (c) {
            return 1;
        } else {
            return 2;
        }
    }

    String returnString(boolean c) {
        if (c) {
            return "yes";
        } else {
            return "no";
        }
    }

    int returnWithComparison(int a, int b) {
        if (a > b) {
            return a;
        } else {
            return b;
        }
    }

    int returnParenthesized(boolean c) {
        if (c) {
            return 1;
        } else {
            return 2;
        }
    }

    // ternary is only part of a larger expression (must not lose the rest)
    public int hashCode() {
        int temp;
        if (extraCheese) {
            temp = 31;
        } else {
            temp = 1;
        }
        return temp * diameter;
    }

    int returnWithOuterExpression(boolean c, int y) {
        int temp;
        if (c) {
            temp = 1;
        } else {
            temp = 2;
        }
        return temp * y;
    }

    void declarationWithOuterExpression(boolean c, int y) {
        int temp;
        if (c) {
            temp = 1;
        } else {
            temp = 2;
        }
        int x = temp * y;
    }

    // temp

    void argumentOfCall(boolean c) {
        int temp;
        if (c) {
            temp = 1;
        } else {
            temp = 2;
        }
        System.out.println(temp);
    }

    void stringArgument(boolean c) {
        String temp;
        if (c) {
            temp = "yes";
        } else {
            temp = "no";
        }
        System.out.println(temp);
    }

    void assignment(boolean c, int x) {
        int temp;
        if (c) {
            temp = 1;
        } else {
            temp = 2;
        }
        x = temp;
    }

    void arithmeticAssignment(boolean c, int x, int y) {
        int temp;
        if (c) {
            temp = 1;
        } else {
            temp = 2;
        }
        y = x + temp;
    }

    void inIfCondition(boolean c, boolean a, boolean b) {
        boolean temp;
        if (c) {
            temp = a;
        } else {
            temp = b;
        }
        if (temp) {
            return;
        }
    }

    void tempNameCollision(boolean c) {
        int temp = 0;
        int temp1;
        if (c) {
            temp1 = 1;
        } else {
            temp1 = 2;
        }
        System.out.println(temp + temp1);
    }

    // others

    void twoSeparateStatements(boolean c) {
        int a;
        if (c) {
            a = 1;
        } else {
            a = 2;
        }
        String b;
        if (c) {
            b = "x";
        } else {
            b = "y";
        }
    }
}