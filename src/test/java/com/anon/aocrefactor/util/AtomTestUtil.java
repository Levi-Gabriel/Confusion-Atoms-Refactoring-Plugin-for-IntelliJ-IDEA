package com.anon.aocrefactor.util;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Helpers compartilhados pelos testes de inspeção e quick fix de cada átomo.
 * Convenção de diretórios: src/test/testData/{Atom}/{Before}.java e {After}.java
 */
public final class AtomTestUtil {

    public static final String TEST_DATA_ROOT = "src/test/testData/";

    /** Severidade padrão. Inspeções que usam WEAK_WARNING precisam informar a severidade explicitamente. */
    public static final HighlightSeverity DEFAULT_SEVERITY = HighlightSeverity.WARNING;

    /** Método declarado com 4 espaços de indentação: "    <tipo de retorno> nome(". */
    public static final Pattern DEFAULT_METHOD_DECLARATION =
            Pattern.compile("^ {4}[\\w<>\\[\\]]+\\s+(\\w+)\\s*\\(");

    private AtomTestUtil() {
    }

    // ------------------------------------------------------------------ caminhos

    public static String testDataDir(String atom) {
        return TEST_DATA_ROOT + atom + "/";
    }

    public static Path testDataPath(String atom, String fileName) {
        return Path.of(testDataDir(atom), fileName);
    }

    // ------------------------------------------------------------------ leitura de arquivos

    public static String read(String atom, String fileName) {
        try {
            return Files.readString(testDataPath(atom, fileName));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Nomes dos métodos declarados no arquivo "before"; útil para @MethodSource. */
    public static Stream<String> methodNames(String atom, String beforeFile) {
        return methodNames(atom, beforeFile, DEFAULT_METHOD_DECLARATION);
    }

    /** Igual a {@link #methodNames(String, String)}, com regex própria (o grupo 1 deve capturar o nome). */
    public static Stream<String> methodNames(String atom, String beforeFile, Pattern declaration) {
        try {
            return Files.readAllLines(testDataPath(atom, beforeFile)).stream()
                    .map(declaration::matcher)
                    .filter(Matcher::find)
                    .map(m -> m.group(1));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ------------------------------------------------------------------ PSI

    public static PsiMethod findMethod(PsiFile file, String name) {
        return PsiTreeUtil.findChildrenOfType(file, PsiMethod.class).stream()
                .filter(m -> name.equals(m.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Método '" + name + "' não encontrado em " + file.getName()));
    }

    /** Texto do método dentro de um conteúdo Java qualquer (ex.: o arquivo "after"). */
    public static String methodTextFromSource(JavaCodeInsightTestFixture fixture,
                                              String fileName, String source, String methodName) {
        return ReadAction.compute(() -> {
            PsiFile psi = PsiFileFactory.getInstance(fixture.getProject())
                    .createFileFromText(fileName, JavaFileType.INSTANCE, source);
            return findMethod(psi, methodName).getText();
        });
    }

    // ------------------------------------------------------------------ highlighting

    public static List<HighlightInfo> warnings(JavaCodeInsightTestFixture fixture, HighlightSeverity severity) {
        return fixture.doHighlighting(severity);
    }

    /** Avisos dentro do método, na ordem do código. */
    public static List<HighlightInfo> warningsInMethod(JavaCodeInsightTestFixture fixture,
                                                       String methodName, HighlightSeverity severity) {
        return warningsInMethod(fixture, methodName, severity,
                Comparator.comparingInt(HighlightInfo::getStartOffset));
    }

    /** Avisos dentro do método, do último para o primeiro (offsets anteriores permanecem válidos ao aplicar fixes). */
    public static List<HighlightInfo> warningsInMethodReversed(JavaCodeInsightTestFixture fixture,
                                                               String methodName, HighlightSeverity severity) {
        return warningsInMethod(fixture, methodName, severity,
                Comparator.comparingInt(HighlightInfo::getStartOffset).reversed());
    }

    private static List<HighlightInfo> warningsInMethod(JavaCodeInsightTestFixture fixture, String methodName,
                                                        HighlightSeverity severity,
                                                        Comparator<HighlightInfo> order) {
        TextRange range = ReadAction.compute(
                () -> findMethod(fixture.getFile(), methodName).getTextRange());
        return fixture.doHighlighting(severity).stream()
                .filter(w -> range.containsOffset(w.getStartOffset()))
                .sorted(order)
                .toList();
    }

    // ------------------------------------------------------------------ quick fix

    /** Aplica o quick fix em todos os avisos do método e falha se não houver nenhum. */
    public static void applyFixToAllWarningsInMethod(JavaCodeInsightTestFixture fixture, String methodName,
                                                     String fixName, HighlightSeverity severity) {
        List<HighlightInfo> warnings = warningsInMethodReversed(fixture, methodName, severity);
        assertFalse(warnings.isEmpty(), "Nenhum aviso encontrado no método " + methodName);

        for (HighlightInfo warning : warnings) {
            ApplicationManager.getApplication().invokeAndWait(
                    () -> fixture.getEditor().getCaretModel().moveToOffset(warning.getStartOffset()));
            IntentionAction fix = fixture.findSingleIntention(fixName);
            assertNotNull(fix, "'" + fixName + "' não oferecido no offset " + warning.getStartOffset());
            fixture.launchAction(fix);
        }
    }

    /**
     * Fluxo completo: abre o arquivo "before", aplica o fix nos avisos do método
     * e compara o método resultante com o mesmo método do arquivo "after".
     */
    public static void assertQuickFixMatchesAfter(JavaCodeInsightTestFixture fixture, String atom,
                                                  String beforeFile, String afterFile,
                                                  String methodName, String fixName,
                                                  HighlightSeverity severity) {
        fixture.configureByFile(beforeFile);
        applyFixToAllWarningsInMethod(fixture, methodName, fixName, severity);

        String actual = normalize(ReadAction.compute(
                () -> findMethod(fixture.getFile(), methodName).getText()));
        String expected = normalize(methodTextFromSource(
                fixture, afterFile, read(atom, afterFile), methodName));

        assertEquals(expected, actual,
                () -> "Método '" + methodName + "' difere de " + testDataPath(atom, afterFile)
                        + " na linha " + (firstDiffLine(expected, actual) + 1) + " do método");
    }

    // ------------------------------------------------------------------ comparação de texto

    /** Remove indentação e linhas vazias para comparar apenas o conteúdo. */
    public static String normalize(String text) {
        return text.lines().map(String::strip).filter(l -> !l.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    /** Índice (base 0) da primeira linha diferente entre os dois textos. */
    public static int firstDiffLine(String a, String b) {
        String[] la = a.split("\n"), lb = b.split("\n");
        int n = Math.min(la.length, lb.length);
        for (int i = 0; i < n; i++) {
            if (!la[i].equals(lb[i])) return i;
        }
        return n;
    }
}