package com.anon.aocrefactor.InfixOperatorPrecedence;

import com.anon.aocrefactor.inspection.InfixOperatorPrecedenceInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.anon.aocrefactor.util.AtomTestUtil;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class InfixOperatorPrecedenceQuickFixTest extends AtomTestBase {

    private static final String ATOM     = "InfixOperatorPrecedence";
    private static final String BEFORE   = "InfixOperatorPrecedence.java";
    private static final String AFTER    = "InfixOperatorPrecedenceAfter.java";
    private static final String FIX_NAME = "Add parentheses for clarity";

    InfixOperatorPrecedenceQuickFixTest() {
        super(ATOM, new InfixOperatorPrecedenceInspection(), HighlightSeverity.WEAK_WARNING);
    }

    /** Um caso de teste por método declarado em InfixOperatorPrecedence.java. */
    static Stream<String> methodNames() {
        return AtomTestUtil.methodNames(ATOM, BEFORE);
    }

    /**
     * Aplica o quick fix apenas nos avisos do método indicado e compara o texto
     * desse método com o mesmo método em InfixOperatorPrecedenceAfter.java.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("methodNames")
    void wrapsMismatchedOperandsInMethod(String methodName) {
        assertQuickFixMatchesAfter(BEFORE, AFTER, methodName, FIX_NAME);
    }
}