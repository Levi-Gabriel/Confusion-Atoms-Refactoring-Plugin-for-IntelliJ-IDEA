package com.anon.aocrefactor.TypeConversion;

import com.anon.aocrefactor.inspection.TypeConversionInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.anon.aocrefactor.util.AtomTestUtil;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.openapi.application.ApplicationManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TypeConversionQuickFixTest extends AtomTestBase {

    private static final String ATOM = "TypeConversion";

    private static final String ADD_COMMENT_FILE  = "AddComment.java";
    private static final String CLAMP_FILE        = "ClampRange.java";
    private static final String WRAP_FILE         = "WrapMathFloor.java";

    private static final String ADD_COMMENT_FIX = "Add comment flagging intentional narrowing conversion";
    private static final String CLAMP_FIX       = "Clamp value to target type's range";
    private static final String WRAP_FIX        = "Wrap operand with Math.floor() to make truncation explicit";

    TypeConversionQuickFixTest() {
        super(ATOM, new TypeConversionInspection());
    }

    static Stream<String> addCommentMethods() {
        return AtomTestUtil.methodNames(ATOM, ADD_COMMENT_FILE);
    }

    static Stream<String> clampMethods() {
        return AtomTestUtil.methodNames(ATOM, CLAMP_FILE);
    }

    static Stream<String> wrapMethods() {
        return AtomTestUtil.methodNames(ATOM, WRAP_FILE);
    }

    // ---------------------------------------------------------------- resultado de cada fix

    /** O comentário é inserido antes da instrução. A inspeção continua reportando (o fix serve para documentar). */
    @ParameterizedTest(name = "{0}")
    @MethodSource("addCommentMethods")
    void addsCommentInMethod(String methodName) {
        assertQuickFixMatchesAfter(ADD_COMMENT_FILE, "AddCommentAfter.java", methodName, ADD_COMMENT_FIX);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("clampMethods")
    void clampsRangeInMethod(String methodName) {
        assertQuickFixMatchesAfter(CLAMP_FILE, "ClampRangeAfter.java", methodName, CLAMP_FIX);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("wrapMethods")
    void wrapsWithFloorInMethod(String methodName) {
        assertQuickFixMatchesAfter(WRAP_FILE, "WrapMathFloorAfter.java", methodName, WRAP_FIX);
    }

    // ---------------------------------------------------------------- quais fixes são oferecidos

    /** Origem não flutuante: "Add comment" + "Clamp"; nunca "Math.floor". */
    @ParameterizedTest(name = "{0}")
    @MethodSource("clampMethods")
    void offersClampButNotFloorForIntegralSource(String methodName) {
        assertFixesOffered(CLAMP_FILE, methodName,
                List.of(ADD_COMMENT_FIX, CLAMP_FIX), List.of(WRAP_FIX));
    }

    /** Origem em ponto flutuante: "Add comment" + "Math.floor"; nunca "Clamp". */
    @ParameterizedTest(name = "{0}")
    @MethodSource("wrapMethods")
    void offersFloorButNotClampForFloatingSource(String methodName) {
        assertFixesOffered(WRAP_FILE, methodName,
                List.of(ADD_COMMENT_FIX, WRAP_FIX), List.of(CLAMP_FIX));
    }

    private void assertFixesOffered(String file, String methodName, List<String> offered, List<String> notOffered) {
        getFixture().configureByFile(file);

        List<HighlightInfo> warnings = warningsInMethod(methodName);
        assertFalse(warnings.isEmpty(), "Nenhum aviso encontrado no método " + methodName);

        for (HighlightInfo warning : warnings) {
            ApplicationManager.getApplication().invokeAndWait(
                    () -> getFixture().getEditor().getCaretModel().moveToOffset(warning.getStartOffset()));
            for (String name : offered) {
                assertNotNull(getFixture().getAvailableIntention(name),
                        "'" + name + "' deveria ser oferecido no offset " + warning.getStartOffset());
            }
            for (String name : notOffered) {
                assertNull(getFixture().getAvailableIntention(name),
                        "'" + name + "' não deveria ser oferecido no offset " + warning.getStartOffset());
            }
        }
    }
}