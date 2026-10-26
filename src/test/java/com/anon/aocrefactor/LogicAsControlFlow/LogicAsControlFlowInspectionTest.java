package com.anon.aocrefactor.LogicAsControlFlow;

import com.anon.aocrefactor.inspection.LogicAsControlFlowInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.anon.aocrefactor.util.AtomTestUtil;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogicAsControlFlowInspectionTest extends AtomTestBase {

    private static final String ATOM = "LogicAsControlFlow";
    private static final String NO_FIX_FILE = "NoFix.java";
    private static final String FIX_NAME = "Replace logic operator with explicit control flow statement";

    private static final String MESSAGE = "Short-circuit evaluation masks conditional execution.";

    /** Mensagens esperadas (na ordem do código) para cada método de LogicAsControlFlow.java. */
    private static final Map<String, List<String>> EXPECTED = Map.ofEntries(
            Map.entry("localVariableAnd", List.of(MESSAGE)),
            Map.entry("localVariableOr", List.of(MESSAGE)),
            Map.entry("localVariableChain", List.of(MESSAGE)),
            Map.entry("localVariableCallInComparison", List.of(MESSAGE)),
            Map.entry("localVariableIncrementOperand", List.of(MESSAGE)),
            Map.entry("assignmentAnd", List.of(MESSAGE)),
            Map.entry("assignmentOrChain", List.of(MESSAGE)),
            Map.entry("fieldAssignmentWithThis", List.of(MESSAGE)),
            Map.entry("returnAnd", List.of(MESSAGE)),
            Map.entry("returnOrChain", List.of(MESSAGE)),
            Map.entry("lambdaBody", List.of(MESSAGE)),
            Map.entry("ifAndWithoutElse", List.of(MESSAGE)),
            Map.entry("ifAndWithElse", List.of(MESSAGE)),
            Map.entry("ifOrWithoutElse", List.of(MESSAGE)),
            Map.entry("ifOrWithElse", List.of(MESSAGE)),
            Map.entry("ifOrThreeOperands", List.of(MESSAGE)),
            Map.entry("whileAnd", List.of(MESSAGE)),
            Map.entry("whileOr", List.of(MESSAGE))
    );

    LogicAsControlFlowInspectionTest() {
        super(ATOM, new LogicAsControlFlowInspection(), HighlightSeverity.WEAK_WARNING);
    }

    static Stream<Arguments> expectedWarnings() {
        return EXPECTED.entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    /** Um caso de teste por método declarado em NoFix.java. */
    static Stream<String> noFixMethodNames() {
        return AtomTestUtil.methodNames(ATOM, NO_FIX_FILE);
    }

    /** Operandos sem efeito colateral/chamada, ou com chamada só no primeiro operando, não são reportados. */
    @Test
    void doesNotReportExpressionsWithoutSideEffects() {
        getFixture().configureByFile("NoWarning.java");

        assertTrue(warnings().isEmpty());
    }

    /** Cada método de LogicAsControlFlow.java recebe exatamente os avisos esperados, na ordem do código. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("expectedWarnings")
    void reportsExpectedMessagesInMethod(String methodName, List<String> expectedMessages) {
        getFixture().configureByFile("LogicAsControlFlow.java");

        List<HighlightInfo> warnings = warningsInMethod(methodName);

        assertEquals(expectedMessages, warnings.stream().map(HighlightInfo::getDescription).toList());
    }

    /** Garante que não há avisos fora dos métodos listados em EXPECTED. */
    @Test
    void reportsNothingBeyondExpectedWarnings() {
        getFixture().configureByFile("LogicAsControlFlow.java");

        int expectedTotal = EXPECTED.values().stream().mapToInt(List::size).sum();

        assertEquals(expectedTotal, warnings().size());
    }

    /** Quando nenhuma refatoração segura existe, o aviso continua mas o quick fix não é oferecido. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("noFixMethodNames")
    void reportsWithoutQuickFixWhenNoSafeRefactoringExists(String methodName) {
        getFixture().configureByFile(NO_FIX_FILE);

        List<HighlightInfo> warnings = warningsInMethod(methodName);

        assertEquals(List.of(MESSAGE), warnings.stream().map(HighlightInfo::getDescription).toList());
        warnings.forEach(w -> assertNull(w.findRegisteredQuickFix(
                (descriptor, range) -> FIX_NAME.equals(descriptor.getAction().getText()) ? descriptor : null)));
    }
}