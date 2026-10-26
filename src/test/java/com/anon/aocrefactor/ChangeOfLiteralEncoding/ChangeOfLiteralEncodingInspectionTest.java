package com.anon.aocrefactor.ChangeOfLiteralEncoding;

import com.anon.aocrefactor.inspection.ChangeOfLiteralEncodingInspection;
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

class ChangeOfLiteralEncodingInspectionTest extends AtomTestBase {

    private static final String ENCODED_FILE = "ChangeOfLiteralEncoding.java";
    private static final String BITWISE_FILE = "BitwiseDecimal.java";
    private static final String FIELDS_FILE  = "Fields.java";

    /** Case 1: expected message for an octal/hex/binary literal used in an assignment or declaration. */
    private static String encoded(String encoding, String text, String value) {
        String reason = encoding.equals("octal")
                ? "a leading zero changes the base, so it is easily misread as a decimal number"
                : "the encoding hides the numeric value from the reader";

        return "The " + encoding + " literal '" + text + "' (value " + value
                + ") is assigned to a variable; " + reason + ". Consider writing it in decimal.";
    }

    /** Case 2: expected message for a decimal literal used in a bitwise/shift operation. */
    private static String bitwise(String text, String operator) {
        return "The decimal literal '" + text + "' is used as an operand of the bitwise/shift operator '"
                + operator + "', where the bit pattern matters more than the numeric value. "
                + "Consider writing it in hexadecimal or binary.";
    }

    /** Case 1: octal/hex/binary literal in an assignment or declaration. Messages in source order. */
    private static final Map<String, List<String>> EXPECTED_ENCODED = Map.ofEntries(
            Map.entry("octalInLocalVariable", List.of(encoded("octal", "017", "15"))),
            Map.entry("hexInLocalVariable", List.of(encoded("hexadecimal", "0x1F", "31"))),
            Map.entry("binaryInLocalVariable", List.of(encoded("binary", "0b101", "5"))),
            Map.entry("octalInAssignment", List.of(encoded("octal", "020", "16"))),
            Map.entry("hexInAssignment", List.of(encoded("hexadecimal", "0x20", "32"))),
            Map.entry("binaryInAssignment", List.of(encoded("binary", "0b10000", "16"))),
            Map.entry("hexLongLiteral", List.of(encoded("hexadecimal", "0x10L", "16"))),
            Map.entry("hexInCompoundAssignment", List.of(encoded("hexadecimal", "0x10", "16"))),
            Map.entry("twoLiteralsInSeparateStatements", List.of(
                    encoded("hexadecimal", "0x10", "16"),
                    encoded("binary", "0b101", "5")))
    );

    /** Case 2: decimal literal as an operand of a bitwise/shift operation. */
    private static final Map<String, List<String>> EXPECTED_BITWISE = Map.ofEntries(
            Map.entry("andWithDecimalOnRight", List.of(bitwise("16", "&"))),
            Map.entry("andWithDecimalOnLeft", List.of(bitwise("32", "&"))),
            Map.entry("orWithDecimal", List.of(bitwise("64", "|"))),
            Map.entry("xorWithDecimal", List.of(bitwise("128", "^"))),
            Map.entry("shiftLeftWithDecimalOperand", List.of(bitwise("16", "<<"))),
            Map.entry("shiftRightWithDecimalOperand", List.of(bitwise("64", ">>"))),
            Map.entry("unsignedShiftRightWithDecimalOperand", List.of(bitwise("64", ">>>"))),
            Map.entry("decimalOnBothSides", List.of(bitwise("16", "&"), bitwise("32", "&"))),
            Map.entry("mixedBitwiseOperators", List.of(bitwise("16", "&"), bitwise("32", "|"))),
            Map.entry("bitwiseInCondition", List.of(bitwise("16", "&")))
    );

    ChangeOfLiteralEncodingInspectionTest() {
        super("ChangeOfLiteralEncoding", new ChangeOfLiteralEncodingInspection(), HighlightSeverity.WEAK_WARNING);
    }

    private static Stream<Arguments> cases(String file, Map<String, List<String>> expected) {
        return expected.entrySet().stream().map(e -> Arguments.of(file, e.getKey(), e.getValue()));
    }

    static Stream<Arguments> expectedWarnings() {
        return Stream.concat(cases(ENCODED_FILE, EXPECTED_ENCODED), cases(BITWISE_FILE, EXPECTED_BITWISE));
    }

    static Stream<Arguments> expectedTotals() {
        return Stream.of(
                Arguments.of(ENCODED_FILE, total(EXPECTED_ENCODED)),
                Arguments.of(BITWISE_FILE, total(EXPECTED_BITWISE)));
    }

    private static int total(Map<String, List<String>> expected) {
        return expected.values().stream().mapToInt(List::size).sum();
    }

    /** Decimal literals outside bitwise operations, encoded literals outside assignments, and shift counts are not reported. */
    @Test
    void doesNotReportClearExpressions() {
        getFixture().configureByFile("NoWarning.java");

        assertTrue(warnings().isEmpty());
    }

    /** Each method gets exactly the expected warnings, in source order. */
    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("expectedWarnings")
    void reportsExpectedMessagesInMethod(String file, String methodName, List<String> expectedMessages) {
        getFixture().configureByFile(file);

        List<HighlightInfo> warnings = warningsInMethod(methodName);

        assertEquals(expectedMessages, warnings.stream().map(HighlightInfo::getDescription).toList());
    }

    /** Ensures there are no warnings outside the listed methods (e.g. a new test data method without an expectation). */
    @ParameterizedTest(name = "{0}")
    @MethodSource("expectedTotals")
    void reportsNothingBeyondExpectedWarnings(String file, int expectedTotal) {
        getFixture().configureByFile(file);

        assertEquals(expectedTotal, warnings().size());
    }

    /** Field initializers (PsiField) are also reported. */
    @Test
    void reportsEncodedLiteralsInFieldInitializers() {
        getFixture().configureByFile(FIELDS_FILE);

        assertEquals(
                List.of(encoded("hexadecimal", "0x1F", "31"),
                        encoded("binary", "0b101", "5"),
                        encoded("octal", "017", "15")),
                warnings().stream().map(HighlightInfo::getDescription).toList());
    }
}