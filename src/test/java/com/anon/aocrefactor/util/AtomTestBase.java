package com.anon.aocrefactor.util;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

/**
 * Base para testes de um átomo: configura o diretório de testData, habilita a inspeção
 * e expõe atalhos que já usam a severidade da inspeção (WARNING ou WEAK_WARNING).
 */
public abstract class AtomTestBase extends LightJavaCodeInsightFixtureTestCase5 {

    private final String atom;
    private final InspectionProfileEntry inspection;
    private final HighlightSeverity severity;

    protected AtomTestBase(String atom, InspectionProfileEntry inspection) {
        this(atom, inspection, AtomTestUtil.DEFAULT_SEVERITY);
    }

    /** Use WEAK_WARNING aqui se a inspeção reporta com ProblemHighlightType.WEAK_WARNING. */
    protected AtomTestBase(String atom, InspectionProfileEntry inspection, HighlightSeverity severity) {
        this.atom = atom;
        this.inspection = inspection;
        this.severity = severity;
    }

    protected String atom() {
        return atom;
    }

    protected HighlightSeverity severity() {
        return severity;
    }

    // O padrão aponta para dentro dos fontes do IntelliJ e faz o setUp() lançar exceção.
    @Override
    protected @NotNull String getRelativePath() {
        return "";
    }

    @BeforeEach
    protected void setUpAtom() {
        getFixture().setTestDataPath(AtomTestUtil.testDataDir(atom));
        getFixture().enableInspections(inspection);
    }

    /** Todos os avisos do arquivo atual, com a severidade mínima do átomo. */
    protected List<HighlightInfo> warnings() {
        return AtomTestUtil.warnings(getFixture(), severity);
    }

    /** Avisos dentro do método, na ordem do código. */
    protected List<HighlightInfo> warningsInMethod(String methodName) {
        return AtomTestUtil.warningsInMethod(getFixture(), methodName, severity);
    }

    /** Aplica o quick fix nos avisos do método em before e compara o método com o de after. */
    protected void assertQuickFixMatchesAfter(String beforeFile, String afterFile,
                                              String methodName, String fixName) {
        AtomTestUtil.assertQuickFixMatchesAfter(
                getFixture(), atom, beforeFile, afterFile, methodName, fixName, severity);
    }
}