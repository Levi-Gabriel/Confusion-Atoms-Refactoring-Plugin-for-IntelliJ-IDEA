package com.anon.aocrefactor.ConditionalOperator;

import com.anon.aocrefactor.inspection.ConditionalOperatorInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.anon.aocrefactor.util.AtomTestUtil;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ReadAction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConditionalOperatorQuickFixTest extends AtomTestBase {

    private static final String ATOM     = "ConditionalOperator";
    private static final String BEFORE   = "ConditionalOperator.java";
    private static final String AFTER    = "ConditionalOperatorAfter.java";
    private static final String FIX_NAME = "Replace with if-else";

    ConditionalOperatorQuickFixTest() {

        super(ATOM, new ConditionalOperatorInspection(), HighlightSeverity.WEAK_WARNING);
    }

    /** Um caso de teste por método declarado em ConditionalOperator.java. */
    static Stream<String> methodNames() {
        return AtomTestUtil.methodNames(ATOM, BEFORE);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("methodNames")
    void replacesTernaryWithIfElseInMethod(String methodName) {
        getFixture().configureByFile(BEFORE);
        AtomTestUtil.applyFixToAllWarningsInMethod(getFixture(), methodName, FIX_NAME, severity());

        String actual = withoutWhitespace(ReadAction.compute(
                () -> AtomTestUtil.findMethod(getFixture().getFile(), methodName).getText()));
        String expected = withoutWhitespace(AtomTestUtil.methodTextFromSource(
                getFixture(), AFTER, AtomTestUtil.read(ATOM, AFTER), methodName));

        assertEquals(expected, actual, () -> "Método '" + methodName + "' difere de "
                + AtomTestUtil.testDataPath(ATOM, AFTER));
    }

    private static String withoutWhitespace(String text) {
        return text.replaceAll("\\s+", "");
    }
}