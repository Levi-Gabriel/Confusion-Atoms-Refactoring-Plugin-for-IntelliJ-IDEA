package ConditionalOperator;

import java.util.List;

public class ConditionalOperatorAfter {

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

    // temp

    void argumentOfCall(boolean c) {
        int _temp;
        if (c) {
            _temp = 1;
        } else {
            _temp = 2;
        }
        System.out.println(_temp);
    }

    void stringArgument(boolean c) {
        String _temp;
        if (c) {
            _temp = "yes";
        } else {
            _temp = "no";
        }
        System.out.println(_temp);
    }

    void assignment(boolean c, int x) {
        int _temp;
        if (c) {
            _temp = 1;
        } else {
            _temp = 2;
        }
        x = _temp;
    }

    void arithmeticAssignment(boolean c, int x, int y) {
        int _temp;
        if (c) {
            _temp = 1;
        } else {
            _temp = 2;
        }
        y = x + (_temp);
    }

    void inIfCondition(boolean c, boolean a, boolean b) {
        boolean _temp;
        if (c) {
            _temp = a;
        } else {
            _temp = b;
        }
        if (_temp) {
            return;
        }
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