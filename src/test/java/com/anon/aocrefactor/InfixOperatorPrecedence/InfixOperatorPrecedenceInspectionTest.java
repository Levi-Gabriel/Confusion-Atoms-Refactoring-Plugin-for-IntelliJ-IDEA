package com.anon.aocrefactor.InfixOperatorPrecedence;

import com.anon.aocrefactor.inspection.InfixOperatorPrecedenceInspection;
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

class InfixOperatorPrecedenceInspectionTest extends AtomTestBase {

    private static final String LOGIC =
            "'&&' binds tighter than '||'";

    /** Expected arithmetic message; only the surrounding operator ('+' or '-') varies. */
    private static String arith(String parentOp) {
        return "'*', '/' and '%' bind tighter than '"
                + parentOp + "'";
    }

    /** Expected messages (in source order) for each method of InfixOperatorPrecedence.java. */
    private static final Map<String, List<String>> EXPECTED = Map.ofEntries(
            Map.entry("additionWithMultiplicationOnRight", List.of(arith("+"))),
            Map.entry("additionWithMultiplicationOnLeft", List.of(arith("+"))),
            Map.entry("subtractionWithDivision", List.of(arith("-"))),
            Map.entry("additionWithModulo", List.of(arith("+"))),
            Map.entry("multiplicationOnBothSides", List.of(arith("+"))),
            Map.entry("polyadicAddition", List.of(arith("+"))),
            Map.entry("multipleFactorsInProduct", List.of(arith("+"))),
            Map.entry("mixedAdditionAndSubtraction", List.of(arith("+"))),
            Map.entry("arithmeticInAssignment", List.of(arith("+"))),
            Map.entry("arithmeticAsArgument", List.of(arith("+"))),
            Map.entry("arithmeticInReturn", List.of(arith("+"))),
            Map.entry("arithmeticInsideComparison", List.of(arith("+"))),
            Map.entry("twoIndependentStatements", List.of(arith("+"), arith("+"))),
            Map.entry("orWithAndOnRight", List.of(LOGIC)),
            Map.entry("orWithAndOnLeft", List.of(LOGIC)),
            Map.entry("orWithAndOnBothSides", List.of(LOGIC)),
            Map.entry("polyadicOrWithAnd", List.of(LOGIC)),
            Map.entry("polyadicAndInsideOr", List.of(LOGIC)),
            Map.entry("logicalInCondition", List.of(LOGIC)),
            Map.entry("arithmeticAndLogicalInSeparateStatements", List.of(arith("+"), LOGIC))
    );

    InfixOperatorPrecedenceInspectionTest() {
        super("InfixOperatorPrecedence", new InfixOperatorPrecedenceInspection(), HighlightSeverity.WEAK_WARNING);
    }

    static Stream<Arguments> expectedWarnings() {
        return EXPECTED.entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    /** Same-precedence operators, explicit parentheses, String concatenation and comparisons are not reported. */
    @Test
    void doesNotReportClearExpressions() {
        getFixture().configureByFile("NoWarning.java");

        assertTrue(warnings().isEmpty());
    }

    /** Each method of InfixOperatorPrecedence.java gets exactly the expected warnings, in source order. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("expectedWarnings")
    void reportsExpectedMessagesInMethod(String methodName, List<String> expectedMessages) {
        getFixture().configureByFile("InfixOperatorPrecedence.java");

        List<HighlightInfo> warnings = warningsInMethod(methodName);

        assertEquals(expectedMessages, warnings.stream().map(HighlightInfo::getDescription).toList());
    }

    /** Ensures there are no warnings outside the methods listed in EXPECTED (e.g. a new test data method without an expectation). */
    @Test
    void reportsNothingBeyondExpectedWarnings() {
        getFixture().configureByFile("InfixOperatorPrecedence.java");

        int expectedTotal = EXPECTED.values().stream().mapToInt(List::size).sum();

        assertEquals(expectedTotal, warnings().size());
    }
}