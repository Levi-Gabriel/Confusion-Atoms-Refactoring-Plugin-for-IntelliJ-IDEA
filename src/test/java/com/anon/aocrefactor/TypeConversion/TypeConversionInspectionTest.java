package com.anon.aocrefactor.TypeConversion;

import com.anon.aocrefactor.inspection.TypeConversionInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.anon.aocrefactor.util.AtomTestUtil;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeConversionInspectionTest extends AtomTestBase {

    private static final String ATOM = "TypeConversion";

    private static final String MESSAGE =
            "Confusion atom: narrowing type conversion may cause precision loss or unexpected results.";

    /** Arquivos em que todo método deve ser reportado. Cada um corresponde a um quick fix. */
    private static final List<String> WARNING_FILES = List.of(
            "AddComment.java", "ClampRange.java", "WrapMathFloor.java");

    /** Todo método tem exatamente um aviso, exceto este, que tem dois (instruções separadas). */
    private static final String TWO_WARNINGS_METHOD = "twoSeparateStatements";

    TypeConversionInspectionTest() {
        super(ATOM, new TypeConversionInspection());
    }

    static Stream<String> warningFiles() {
        return WARNING_FILES.stream();
    }

    static Stream<Arguments> warningFilesAndMethods() {
        return WARNING_FILES.stream().flatMap(file ->
                AtomTestUtil.methodNames(ATOM, file).map(method -> Arguments.of(file, method)));
    }

    private static int expectedCount(String methodName) {
        return TWO_WARNINGS_METHOD.equals(methodName) ? 2 : 1;
    }

    /** Alargamentos, tipos referência, literais no intervalo, Math seguro, módulo e clamp explícito não são reportados. */
    @Test
    void doesNotReportSafeCasts() {
        getFixture().configureByFile("NoWarning.java");

        assertTrue(warnings().isEmpty());
    }

    /** Cada método recebe exatamente os avisos esperados, todos com a mesma mensagem. */
    @ParameterizedTest(name = "{0} > {1}")
    @MethodSource("warningFilesAndMethods")
    void reportsNarrowingConversionInMethod(String file, String methodName) {
        getFixture().configureByFile(file);

        List<HighlightInfo> warnings = warningsInMethod(methodName);

        assertEquals(expectedCount(methodName), warnings.size());
        warnings.forEach(w -> assertEquals(MESSAGE, w.getDescription()));
    }

    /** Garante que não há avisos fora dos métodos esperados (ex.: método novo no testData sem expectativa). */
    @ParameterizedTest(name = "{0}")
    @MethodSource("warningFiles")
    void reportsNothingBeyondExpectedWarnings(String file) {
        getFixture().configureByFile(file);

        int expectedTotal = AtomTestUtil.methodNames(ATOM, file)
                .mapToInt(TypeConversionInspectionTest::expectedCount)
                .sum();

        assertEquals(expectedTotal, warnings().size());
    }
}