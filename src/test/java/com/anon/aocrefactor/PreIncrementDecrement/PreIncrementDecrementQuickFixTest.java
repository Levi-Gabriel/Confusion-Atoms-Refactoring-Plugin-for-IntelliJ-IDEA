package com.anon.aocrefactor.PreIncrementDecrement;

import com.anon.aocrefactor.inspection.PreIncrementDecrementInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.anon.aocrefactor.util.AtomTestUtil;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class PreIncrementDecrementQuickFixTest extends AtomTestBase {

    private static final String ATOM     = "PreIncrementDecrement";
    private static final String BEFORE   = "PreIncrementDecrement.java";
    private static final String AFTER    = "PreIncrementDecrementAfter.java";
    private static final String NO_FIX   = "NoFix.java";
    private static final String FIX_NAME = "Separate pre-increment/decrement into its own statement";

    PreIncrementDecrementQuickFixTest() {
        super(ATOM, new PreIncrementDecrementInspection(), HighlightSeverity.WEAK_WARNING);
    }

    /** Um caso de teste por método declarado em PreIncrementDecrement.java. */
    static Stream<String> methodNames() {
        return AtomTestUtil.methodNames(ATOM, BEFORE);
    }

    /** Um caso de teste por método declarado em NoFix.java. */
    static Stream<String> unsafeMethodNames() {
        return AtomTestUtil.methodNames(ATOM, NO_FIX);
    }

    /**
     * Aplica o quick fix apenas nos avisos do método indicado e compara o texto
     * desse método com o mesmo método em PreIncrementDecrementAfter.java.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("methodNames")
    void separatesIncrementInMethod(String methodName) {
        assertQuickFixMatchesAfter(BEFORE, AFTER, methodName, FIX_NAME);
    }

    /** Nos casos em que separar mudaria o comportamento, o aviso existe, mas o quick fix não é oferecido. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("unsafeMethodNames")
    void doesNotOfferFixInMethod(String methodName) {
        getFixture().configureByFile(NO_FIX);

        List<HighlightInfo> warnings = warningsInMethod(methodName);
        assertFalse(warnings.isEmpty(), "Nenhum aviso encontrado no método " + methodName);

        for (HighlightInfo warning : warnings) {
            ApplicationManager.getApplication().invokeAndWait(
                    () -> getFixture().getEditor().getCaretModel().moveToOffset(warning.getStartOffset()));
            assertNull(getFixture().getAvailableIntention(FIX_NAME),
                    "'" + FIX_NAME + "' não deveria ser oferecido no offset " + warning.getStartOffset());
        }
    }
}
