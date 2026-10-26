package LogicAsControlFlow;

public class NoWarning {

    void pureOperands(boolean a, boolean b, boolean c) {
        boolean first = a && b;
        boolean second = a || b || c;
    }

    void negatedOperands(boolean a, boolean b) {
        boolean result = !a && !b;
    }

    void comparisonsWithoutCalls(boolean a, boolean b, int count) {
        boolean result = a && count > 0 || b;
    }

    void parenthesizedPureOperands(boolean a, boolean b, boolean c) {
        boolean result = (a && b) || c;
    }

    // Chamada só no primeiro operando: ela sempre executa, então não há execução condicional mascarada.
    void callOnlyInFirstOperand(boolean a, String s) {
        boolean first = s.isEmpty() && a;
        boolean second = s.isEmpty() || a;
    }

    void incrementOnlyInFirstOperand(boolean a, int count) {
        boolean result = count++ > 0 && a;
    }

    void callInFirstParenthesizedGroupOnly(boolean a, boolean b, String s) {
        boolean result = (s.isEmpty() && a) || b;
    }

    // Operador de igualdade não é operador lógico.
    void equalityWithCall(boolean a, String s) {
        boolean result = a == s.isEmpty();
    }

    void singleCallWithoutLogic(String s) {
        boolean result = s.isEmpty();
    }
}