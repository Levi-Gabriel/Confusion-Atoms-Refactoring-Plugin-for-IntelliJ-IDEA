package com.anon.aocrefactor.ChangeOfLiteralEncoding;

import com.anon.aocrefactor.inspection.ChangeOfLiteralEncodingInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.anon.aocrefactor.util.AtomTestUtil;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ChangeOfLiteralEncodingQuickFixTest extends AtomTestBase {

    private static final String ATOM = "ChangeOfLiteralEncoding";

    private static final String ENCODED_BEFORE = "ChangeOfLiteralEncoding.java";
    private static final String ENCODED_AFTER  = "ChangeOfLiteralEncodingAfter.java";

    private static final String BITWISE_BEFORE       = "BitwiseDecimal.java";
    private static final String BITWISE_AFTER_HEX    = "BitwiseDecimalHexAfter.java";
    private static final String BITWISE_AFTER_BINARY = "BitwiseDecimalBinaryAfter.java";

    private static final String FIX_DECIMAL = "Convert literal to decimal";
    private static final String FIX_HEX     = "Convert literal to hexadecimal";
    private static final String FIX_BINARY  = "Convert literal to binary";

    ChangeOfLiteralEncodingQuickFixTest() {
        super(ATOM, new ChangeOfLiteralEncodingInspection(), HighlightSeverity.WEAK_WARNING);
    }

    static Stream<String> encodedMethodNames() {
        return AtomTestUtil.methodNames(ATOM, ENCODED_BEFORE);
    }

    static Stream<String> bitwiseMethodNames() {
        return AtomTestUtil.methodNames(ATOM, BITWISE_BEFORE);
    }

    /** Literal octal/hex/binário -> decimal, método a método. */
    @ParameterizedTest(name = "decimal: {0}")
    @MethodSource("encodedMethodNames")
    void convertsEncodedLiteralToDecimal(String methodName) {
        assertQuickFixMatchesAfter(ENCODED_BEFORE, ENCODED_AFTER, methodName, FIX_DECIMAL);
    }

    /** Literal decimal em operação bitwise/shift -> hexadecimal. */
    @ParameterizedTest(name = "hex: {0}")
    @MethodSource("bitwiseMethodNames")
    void convertsBitwiseOperandToHex(String methodName) {
        assertQuickFixMatchesAfter(BITWISE_BEFORE, BITWISE_AFTER_HEX, methodName, FIX_HEX);
    }

    /** Literal decimal em operação bitwise/shift -> binário. */
    @ParameterizedTest(name = "binary: {0}")
    @MethodSource("bitwiseMethodNames")
    void convertsBitwiseOperandToBinary(String methodName) {
        assertQuickFixMatchesAfter(BITWISE_BEFORE, BITWISE_AFTER_BINARY, methodName, FIX_BINARY);
    }
}