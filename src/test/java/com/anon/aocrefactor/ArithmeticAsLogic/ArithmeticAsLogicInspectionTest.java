package com.anon.aocrefactor.ArithmeticAsLogic;

import com.anon.aocrefactor.inspection.ArithmeticAsLogicInspection;
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

class ArithmeticAsLogicInspectionTest extends AtomTestBase {

    private static final String MULT = "Multiplication compared to zero is used as a logical expression";
    private static final String SUB  = "Subtraction compared to zero is used as a logical expression";
    private static final String ADD  = "Addition compared to zero is used as a logical expression";

    /** Mensagens esperadas (na ordem do código) para cada método de ArithmeticAsLogic.java. */
    private static final Map<String, List<String>> EXPECTED = Map.ofEntries(
            Map.entry("subtractionEqualsZero", List.of(SUB)),
            Map.entry("subtractionNotEqualsZero", List.of(SUB)),
            Map.entry("additionEqualsZero", List.of(ADD)),
            Map.entry("additionNotEqualsZero", List.of(ADD)),
            Map.entry("multiplicationEqualsZero", List.of(MULT)),
            Map.entry("multiplicationNotEqualsZero", List.of(MULT)),
            Map.entry("zeroOnTheLeft", List.of(SUB)),
            Map.entry("parenthesizedArithmetic", List.of(SUB)),
            Map.entry("tripleMultiplication", List.of(MULT)),
            Map.entry("nestedMultiplication", List.of(MULT)),
            Map.entry("multiplicationWithSubtractionFactor", List.of(MULT)),
            Map.entry("multiplicationWithAdditionFactor", List.of(MULT)),
            Map.entry("subtractionWithParenthesizedOperand", List.of(SUB))
    );

    ArithmeticAsLogicInspectionTest() {
        // A inspeção usa ProblemHighlightType.WEAK_WARNING, então doHighlighting(WARNING) não retornaria nada.
        super("ArithmeticAsLogic", new ArithmeticAsLogicInspection(), HighlightSeverity.WEAK_WARNING);
    }

    static Stream<Arguments> expectedWarnings() {
        return EXPECTED.entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    /** Comparações que não envolvem "aritmética == 0" não são reportadas. */
    @Test
    void doesNotReportSafeComparisons() {
        getFixture().configureByFile("NoWarning.java");

        assertTrue(warnings().isEmpty());
    }

    /** Cada método de ArithmeticAsLogic.java recebe exatamente os avisos esperados, com a mensagem e a severidade corretas. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("expectedWarnings")
    void reportsExpectedMessagesInMethod(String methodName, List<String> expectedMessages) {
        getFixture().configureByFile("ArithmeticAsLogic.java");

        List<HighlightInfo> warnings = warningsInMethod(methodName);

        assertEquals(expectedMessages, warnings.stream().map(HighlightInfo::getDescription).toList());
        warnings.forEach(w -> assertEquals(HighlightSeverity.WEAK_WARNING, w.getSeverity()));
    }

    /** Garante que não há avisos fora dos métodos listados em EXPECTED (ex.: método novo no testData sem expectativa). */
    @Test
    void reportsNothingBeyondExpectedWarnings() {
        getFixture().configureByFile("ArithmeticAsLogic.java");

        int expectedTotal = EXPECTED.values().stream().mapToInt(List::size).sum();

        assertEquals(expectedTotal, warnings().size());
    }
}