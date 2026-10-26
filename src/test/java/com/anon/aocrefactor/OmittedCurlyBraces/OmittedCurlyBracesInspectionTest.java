package com.anon.aocrefactor.OmittedCurlyBraces;

import com.anon.aocrefactor.inspection.OmittedCurlyBracesInspection;
import com.anon.aocrefactor.util.AtomTestBase;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OmittedCurlyBracesInspectionTest extends AtomTestBase {

    private static final String MESSAGE =
            "Omitted curly braces may cause ambiguity about which statements belong to this block.";

    OmittedCurlyBracesInspectionTest() {
        super("OmittedCurlyBraces", new OmittedCurlyBracesInspection(), HighlightSeverity.WEAK_WARNING);
    }

    /** Blocks, empty statements and return/break/continue/throw are not reported. */
    @Test
    void doesNotReportBlocksEmptyOrTerminalStatements() {
        getFixture().configureByFile("NoWarning.java");

        assertTrue(warnings().isEmpty());
    }

    /**
     * Every branch without braces in WithoutBraces.java is reported with the expected message.
     * Which branches are covered is verified by the quick fix test, which compares against WithoutBracesAfter.java.
     */
    @Test
    void reportsExpectedMessage() {
        getFixture().configureByFile("WithoutBraces.java");

        List<HighlightInfo> warnings = warnings();

        assertFalse(warnings.isEmpty());
        warnings.forEach(w -> assertEquals(MESSAGE, w.getDescription()));
    }
}