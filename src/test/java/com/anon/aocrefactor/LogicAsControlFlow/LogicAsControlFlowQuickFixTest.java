package com.anon.aocrefactor.LogicAsControlFlow;

import com.anon.aocrefactor.inspection.LogicAsControlFlowInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.anon.aocrefactor.util.AtomTestUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class LogicAsControlFlowQuickFixTest extends AtomTestBase {

    private static final String ATOM     = "LogicAsControlFlow";
    private static final String BEFORE   = "LogicAsControlFlow.java";
    private static final String AFTER    = "LogicAsControlFlowAfter.java";
    private static final String FIX_NAME = "Replace logic operator with explicit control flow statement";

    LogicAsControlFlowQuickFixTest() {
        super(ATOM, new LogicAsControlFlowInspection(), HighlightSeverity.WEAK_WARNING);
    }

    /** Um caso de teste por método declarado em LogicAsControlFlow.java. */
    static Stream<String> methodNames() {
        return AtomTestUtil.methodNames(ATOM, BEFORE);
    }

    /**
     * Aplica o quick fix apenas nos avisos do método indicado e compara o texto
     * desse método com o mesmo método em LogicAsControlFlowAfter.java.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("methodNames")
    void replacesLogicWithControlFlowInMethod(String methodName) {
        assertQuickFixMatchesAfter(BEFORE, AFTER, methodName, FIX_NAME);
    }
}