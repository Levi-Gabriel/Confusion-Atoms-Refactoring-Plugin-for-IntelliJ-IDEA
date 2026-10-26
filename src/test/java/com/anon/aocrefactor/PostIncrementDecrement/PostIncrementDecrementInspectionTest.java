package com.anon.aocrefactor.PostIncrementDecrement;

import com.anon.aocrefactor.inspection.PostIncrementDecrementInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostIncrementDecrementInspectionTest extends AtomTestBase {

    private static final String WITH_FIX_FILE = "PostIncrementDecrement.java";
    private static final String NO_FIX_FILE   = "NoFix.java";

    private static final String INC = "post-increment (++)";
    private static final String DEC = "post-decrement (--)";

    private static final String WITH_FIX_MESSAGE =
            "%s operator may cause confusion.";
    private static final String NO_FIX_MESSAGE =
            "%s operator may cause confusion. "
                    + "No automatic fix: it cannot be separated without changing behavior.";

    private static String fix(String operator) {
        return String.format(WITH_FIX_MESSAGE, operator);
    }

    private static String noFix(String operator) {
        return String.format(NO_FIX_MESSAGE, operator);
    }

    /** Mensagens esperadas (na ordem do código) para cada método de PostIncrementDecrement.java. */
    private static final Map<String, List<String>> FIXABLE = Map.ofEntries(
            Map.entry("assignmentWithPostIncrement", List.of(fix(INC))),
            Map.entry("assignmentWithPostDecrement", List.of(fix(DEC))),
            Map.entry("declarationInitializer", List.of(fix(INC))),
            Map.entry("assignmentToField", List.of(fix(INC))),
            Map.entry("fieldInDeclaration", List.of(fix(INC))),
            Map.entry("fieldWithThis", List.of(fix(INC))),
            Map.entry("staticFieldWithClassName", List.of(fix(DEC))),
            Map.entry("twoSeparateStatements", List.of(fix(INC), fix(DEC))),
            Map.entry("arrayIndexInReturn", List.of(fix(INC))),
            Map.entry("arrayIndexDecrement", List.of(fix(DEC))),
            Map.entry("incrementAsLeftOperand", List.of(fix(INC))),
            Map.entry("incrementAsRightOperand", List.of(fix(INC))),
            Map.entry("incrementAsArgument", List.of(fix(INC))),
            Map.entry("incrementInIfCondition", List.of(fix(INC))),
            Map.entry("incrementInFirstOperandOfAnd", List.of(fix(INC))),
            Map.entry("incrementInTernaryCondition", List.of(fix(INC))),
            Map.entry("compoundAssignment", List.of(fix(INC))),
            Map.entry("qualifiedAssignmentTarget", List.of(fix(INC))),
            Map.entry("arrayElementAssignmentTarget", List.of(fix(INC))),
            Map.entry("longVariable", List.of(fix(INC))),
            Map.entry("tempNameAlreadyInUse", List.of(fix(INC))),
            Map.entry("fieldAsArrayIndex", List.of(fix(INC))),
            Map.entry("fieldInIfCondition", List.of(fix(INC)))
    );

    /** Mensagens esperadas para cada método de NoFix.java. */
    private static final Map<String, List<String>> NOT_FIXABLE = Map.ofEntries(
            Map.entry("whileCondition", List.of(noFix(INC))),
            Map.entry("doWhileCondition", List.of(noFix(INC))),
            Map.entry("forCondition", List.of(noFix(INC))),
            Map.entry("forInitialization", List.of(noFix(INC))),
            Map.entry("forUpdate", List.of(noFix(INC))),
            Map.entry("arrayElementOperand", List.of(noFix(INC))),
            Map.entry("operandQualifiedByOtherObject", List.of(noFix(INC))),
            Map.entry("fieldInAssignment", List.of(noFix(INC))),
            Map.entry("fieldInMethodCall", List.of(noFix(INC))),
            Map.entry("fieldWithOtherIncrement", List.of(noFix(INC)))
    );

    PostIncrementDecrementInspectionTest() {
        super("PostIncrementDecrement", new PostIncrementDecrementInspection(), HighlightSeverity.WEAK_WARNING);
    }

    static Stream<Arguments> fixableWarnings() {
        return FIXABLE.entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    static Stream<Arguments> notFixableWarnings() {
        return NOT_FIXABLE.entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    /** Prefixo, atribuição composta, outros operadores unários e instruções isoladas não são reportados. */
    @Test
    void doesNotReportOtherOperators() {
        getFixture().configureByFile("NoWarning.java");

        assertTrue(warnings().isEmpty());
    }

    /** Casos seguros: aviso com a mensagem "Consider separating the operation." */
    @ParameterizedTest(name = "{0}")
    @MethodSource("fixableWarnings")
    void reportsFixableWarningsInMethod(String methodName, List<String> expectedMessages) {
        assertMethodWarnings(WITH_FIX_FILE, methodName, expectedMessages);
    }

    /** Casos inseguros: aviso com a mensagem "No automatic fix". */
    @ParameterizedTest(name = "{0}")
    @MethodSource("notFixableWarnings")
    void reportsNotFixableWarningsInMethod(String methodName, List<String> expectedMessages) {
        assertMethodWarnings(NO_FIX_FILE, methodName, expectedMessages);
    }

    /** Garante que não há avisos fora dos métodos listados em FIXABLE (ex.: método novo no testData sem expectativa). */
    @Test
    void reportsNothingBeyondExpectedFixableWarnings() {
        getFixture().configureByFile(WITH_FIX_FILE);

        int expectedTotal = FIXABLE.values().stream().mapToInt(List::size).sum();

        assertEquals(expectedTotal, warnings().size());
    }

    /** Garante que não há avisos fora dos métodos listados em NOT_FIXABLE. */
    @Test
    void reportsNothingBeyondExpectedNotFixableWarnings() {
        getFixture().configureByFile(NO_FIX_FILE);

        int expectedTotal = NOT_FIXABLE.values().stream().mapToInt(List::size).sum();

        assertEquals(expectedTotal, warnings().size());
    }

    private void assertMethodWarnings(String file, String methodName, List<String> expectedMessages) {
        getFixture().configureByFile(file);

        List<HighlightInfo> warnings = warningsInMethod(methodName);

        assertEquals(expectedMessages, warnings.stream().map(HighlightInfo::getDescription).toList());
    }
}