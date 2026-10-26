package com.anon.aocrefactor.PreIncrementDecrement;

import com.anon.aocrefactor.inspection.PreIncrementDecrementInspection;
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

class PreIncrementDecrementInspectionTest extends AtomTestBase {

    private static final String WITH_FIX = "PreIncrementDecrement.java";
    private static final String NO_FIX   = "NoFix.java";

    private static final String INC = "pre-increment (++)";
    private static final String DEC = "pre-decrement (--)";

    /**
     * Aviso esperado. A mensagem é verificada por trechos (e não por igualdade) porque ela é HTML
     * e a descrição do HighlightInfo pode vir com ou sem as tags.
     */
    private record Expect(String operator, boolean fixable) {
        boolean matches(String description) {
            String head = operator + " operator may cause confusion.";
            String tail = "";
            if (!fixable) {
                tail = "No automatic fix";
            }
            if (!(description.contains(head))) return false;
            return description.contains(tail);
        }
    }

    private static Expect fix(String operator) {
        return new Expect(operator, true);
    }

    private static Expect noFix(String operator) {
        return new Expect(operator, false);
    }

    /** Avisos com quick fix esperados (na ordem do código) para cada método de PreIncrementDecrementInspectionTest.java. */
    private static final Map<String, List<Expect>> FIXABLE = Map.ofEntries(
            Map.entry("assignmentWithPreIncrement", List.of(fix(INC))),
            Map.entry("assignmentWithPreDecrement", List.of(fix(DEC))),
            Map.entry("declarationInitializer", List.of(fix(INC))),
            Map.entry("arrayIndexInReturn", List.of(fix(INC))),
            Map.entry("incrementAsLeftOperand", List.of(fix(INC))),
            Map.entry("incrementAsRightOperand", List.of(fix(INC))),
            Map.entry("incrementAsArgument", List.of(fix(INC))),
            Map.entry("incrementInIfCondition", List.of(fix(INC))),
            Map.entry("incrementInFirstOperandOfAnd", List.of(fix(INC))),
            Map.entry("incrementInTernaryCondition", List.of(fix(INC))),
            Map.entry("twoSeparateStatements", List.of(fix(INC), fix(DEC))),
            Map.entry("fieldInDeclaration", List.of(fix(INC))),
            Map.entry("fieldWithThis", List.of(fix(INC))),
            Map.entry("staticFieldWithClassName", List.of(fix(DEC))),
            Map.entry("fieldInIfCondition", List.of(fix(INC))),
            Map.entry("fieldAsArrayIndex", List.of(fix(INC)))
    );

    /** Avisos sem quick fix esperados para cada método de NoFix.java. */
    private static final Map<String, List<Expect>> NOT_FIXABLE = Map.ofEntries(
            Map.entry("variableUsedTwice", List.of(noFix(INC))),
            Map.entry("variableUsedTwiceDecrement", List.of(noFix(DEC))),
            Map.entry("whileCondition", List.of(noFix(INC))),
            Map.entry("doWhileCondition", List.of(noFix(INC))),
            Map.entry("forCondition", List.of(noFix(INC))),
            Map.entry("forInitialization", List.of(noFix(INC))),
            Map.entry("forUpdate", List.of(noFix(INC))),
            Map.entry("assertStatement", List.of(noFix(INC))),
            Map.entry("localDeclaredInSameStatement", List.of(noFix(INC))),
            Map.entry("arrayElementOperand", List.of(noFix(INC))),
            Map.entry("operandQualifiedByOtherObject", List.of(noFix(INC))),
            Map.entry("fieldInAssignment", List.of(noFix(INC))),
            Map.entry("fieldInMethodCall", List.of(noFix(INC)))
    );

    PreIncrementDecrementInspectionTest() {
        super("PreIncrementDecrement", new PreIncrementDecrementInspection(), HighlightSeverity.WEAK_WARNING);
    }

    static Stream<Arguments> fixableWarnings() {
        return FIXABLE.entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    static Stream<Arguments> notFixableWarnings() {
        return NOT_FIXABLE.entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    /** Postfixo, atribuição composta, outros operadores prefixados e instruções isoladas não são reportados. */
    @Test
    void doesNotReportOtherOperators() {
        getFixture().configureByFile("NoWarning.java");

        assertTrue(warnings().isEmpty());
    }

    /** Casos seguros: aviso com a mensagem "Consider separating the operation." */
    @ParameterizedTest(name = "{0}")
    @MethodSource("fixableWarnings")
    void reportsFixableWarningsInMethod(String methodName, List<Expect> expected) {
        assertMethodWarnings(WITH_FIX, methodName, expected);
    }

    /** Casos inseguros: aviso com a mensagem "No automatic fix". */
    @ParameterizedTest(name = "{0}")
    @MethodSource("notFixableWarnings")
    void reportsNotFixableWarningsInMethod(String methodName, List<Expect> expected) {
        assertMethodWarnings(NO_FIX, methodName, expected);
    }

    /** Garante que não há avisos fora dos métodos listados em FIXABLE (ex.: método novo no testData sem expectativa). */
    @Test
    void reportsNothingBeyondExpectedFixableWarnings() {
        getFixture().configureByFile(WITH_FIX);

        int expectedTotal = FIXABLE.values().stream().mapToInt(List::size).sum();

        assertEquals(expectedTotal, warnings().size());
    }

    /** Garante que não há avisos fora dos métodos listados em NOT_FIXABLE. */
    @Test
    void reportsNothingBeyondExpectedNotFixableWarnings() {
        getFixture().configureByFile(NO_FIX);

        int expectedTotal = NOT_FIXABLE.values().stream().mapToInt(List::size).sum();

        assertEquals(expectedTotal, warnings().size());
    }

    private void assertMethodWarnings(String file, String methodName, List<Expect> expected) {
        getFixture().configureByFile(file);

        List<String> descriptions = warningsInMethod(methodName).stream()
                .map(HighlightInfo::getDescription)
                .toList();

        assertEquals(expected.size(), descriptions.size(),
                "Quantidade de avisos em " + methodName + ": " + descriptions);
        for (int i = 0; i < expected.size(); i++) {
            Expect e = expected.get(i);
            String description = descriptions.get(i);
            assertTrue(e.matches(description),
                    () -> "Aviso inesperado em " + methodName + ": esperado " + e + " mas foi: " + description);
        }
    }
}
