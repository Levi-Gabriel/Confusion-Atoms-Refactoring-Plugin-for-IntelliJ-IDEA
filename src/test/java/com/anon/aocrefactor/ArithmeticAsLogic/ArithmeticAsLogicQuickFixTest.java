package com.anon.aocrefactor.ArithmeticAsLogic;

import com.anon.aocrefactor.inspection.ArithmeticAsLogicInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.anon.aocrefactor.util.AtomTestUtil;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ArithmeticAsLogicQuickFixTest extends AtomTestBase {

    private static final String ATOM     = "ArithmeticAsLogic";
    private static final String BEFORE   = "ArithmeticAsLogic.java";
    private static final String AFTER    = "ArithmeticAsLogicAfter.java";
    private static final String FIX_NAME = "Replace with equivalent logical expression";

    ArithmeticAsLogicQuickFixTest() {
        // A inspeção usa WEAK_WARNING; com WARNING o quick fix nunca seria encontrado.
        super(ATOM, new ArithmeticAsLogicInspection(), HighlightSeverity.WEAK_WARNING);
    }

    /** Um caso de teste por método declarado em ArithmeticAsLogic.java. */
    static Stream<String> methodNames() {
        return AtomTestUtil.methodNames(ATOM, BEFORE);
    }

    /**
     * Aplica o quick fix apenas nos avisos do método indicado e compara o texto
     * desse método com o mesmo método em ArithmeticAsLogicAfter.java.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("methodNames")
    void replacesArithmeticComparisonsInMethod(String methodName) {
        assertQuickFixMatchesAfter(BEFORE, AFTER, methodName, FIX_NAME);
    }
}