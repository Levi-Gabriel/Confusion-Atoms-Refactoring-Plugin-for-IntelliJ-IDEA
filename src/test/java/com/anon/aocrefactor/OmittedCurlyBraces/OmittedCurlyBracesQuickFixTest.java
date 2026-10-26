package com.anon.aocrefactor.OmittedCurlyBraces;

import com.anon.aocrefactor.inspection.OmittedCurlyBracesInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.anon.aocrefactor.util.AtomTestUtil;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class OmittedCurlyBracesQuickFixTest extends AtomTestBase {

    private static final String ATOM     = "OmittedCurlyBraces";
    private static final String BEFORE   = "WithoutBraces.java";
    private static final String AFTER    = "WithoutBracesAfter.java";
    private static final String FIX_NAME = "Add curly braces";

    OmittedCurlyBracesQuickFixTest() {
        super(ATOM, new OmittedCurlyBracesInspection(), HighlightSeverity.WEAK_WARNING);
    }

    /** Um caso de teste por método declarado em WithoutBraces.java. */
    static Stream<String> methodNames() {
        return AtomTestUtil.methodNames(ATOM, BEFORE);
    }

    /**
     * Aplica o quick fix apenas nos avisos do método indicado e compara o texto
     * desse método com o mesmo método em WithoutBracesAfter.java.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("methodNames")
    void wrapsBranchesInMethod(String methodName) {
        assertQuickFixMatchesAfter(BEFORE, AFTER, methodName, FIX_NAME);
    }
}