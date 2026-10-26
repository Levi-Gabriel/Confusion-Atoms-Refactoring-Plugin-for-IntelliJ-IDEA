package com.anon.aocrefactor.ConditionalOperator;

import com.anon.aocrefactor.inspection.ConditionalOperatorInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.anon.aocrefactor.util.AtomTestUtil;
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

class ConditionalOperatorInspectionTest extends AtomTestBase {

    private static final String ATOM = "ConditionalOperator";

    private static final String MESSAGE = "Ternary operator may cause confusion.";


    private static final List<String> WARNING_FILES = List.of("ConditionalOperator.java");


    private static final Map<String, Integer> COUNT_OVERRIDES = Map.of(
            "twoSeparateStatements", 2,
            "nestedInElseBranch", 2,
            "nestedInThenBranch", 2,
            "nestedInCondition", 2,
            "tripleChain", 3
    );


    private static final Map<String, Integer> WARNINGS_OUTSIDE_METHODS = Map.of(
            "InspectionOnly.java", 1
    );

    ConditionalOperatorInspectionTest() {

        super(ATOM, new ConditionalOperatorInspection(), HighlightSeverity.WEAK_WARNING);
    }

    static Stream<String> warningFiles() {
        return WARNING_FILES.stream();
    }

    static Stream<Arguments> warningFilesAndMethods() {
        return WARNING_FILES.stream().flatMap(file ->
                AtomTestUtil.methodNames(ATOM, file).map(method -> Arguments.of(file, method)));
    }

    private static int expectedCount(String methodName) {
        return COUNT_OVERRIDES.getOrDefault(methodName, 1);
    }


    @Test
    void doesNotReportCodeWithoutTernary() {
        getFixture().configureByFile("NoWarning.java");

        assertTrue(warnings().isEmpty());
    }


    @ParameterizedTest(name = "{0} > {1}")
    @MethodSource("warningFilesAndMethods")
    void reportsTernaryInMethod(String file, String methodName) {
        getFixture().configureByFile(file);

        List<HighlightInfo> warnings = warningsInMethod(methodName);

        assertEquals(expectedCount(methodName), warnings.size());
        warnings.forEach(w -> assertEquals(MESSAGE, w.getDescription()));
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("warningFiles")
    void reportsNothingBeyondExpectedWarnings(String file) {
        getFixture().configureByFile(file);

        int expectedTotal = AtomTestUtil.methodNames(ATOM, file)
                .mapToInt(ConditionalOperatorInspectionTest::expectedCount)
                .sum() + WARNINGS_OUTSIDE_METHODS.getOrDefault(file, 0);

        assertEquals(expectedTotal, warnings().size());
    }
}