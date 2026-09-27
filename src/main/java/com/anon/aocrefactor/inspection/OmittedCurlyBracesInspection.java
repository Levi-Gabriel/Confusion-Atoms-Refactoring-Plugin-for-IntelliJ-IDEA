package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.AddCurlyBracesQuickFix;
import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class OmittedCurlyBracesInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            @NotNull ProblemsHolder holder,
            boolean isOnTheFly) {

        return new JavaElementVisitor() {

            @Override
            public void visitIfStatement(@NotNull PsiIfStatement statement) {
                super.visitIfStatement(statement);

                checkBranch(statement.getThenBranch());

                PsiStatement elseBranch = statement.getElseBranch();
                // Ignora "else if": esse elseBranch é outro PsiIfStatement e será
                // verificado quando o visitor visitar ele diretamente (evita duplicidade)
                if (elseBranch != null && !(elseBranch instanceof PsiIfStatement)) {
                    checkBranch(elseBranch);
                }
            }

            @Override
            public void visitWhileStatement(@NotNull PsiWhileStatement statement) {
                super.visitWhileStatement(statement);
                checkBranch(statement.getBody());
            }

            @Override
            public void visitDoWhileStatement(@NotNull PsiDoWhileStatement statement) {
                super.visitDoWhileStatement(statement);
                checkBranch(statement.getBody());
            }

            @Override
            public void visitForStatement(@NotNull PsiForStatement statement) {
                super.visitForStatement(statement);
                checkBranch(statement.getBody());
            }

            @Override
            public void visitForeachStatement(@NotNull PsiForeachStatement statement) {
                super.visitForeachStatement(statement);
                checkBranch(statement.getBody());
            }

            // Marca o statement com warning se ele for vazio,
            // não estiver dentro de um bloco { },
            // ou a branch for null.

            private void checkBranch(PsiStatement branch) {
                if (branch == null) return;
                if (branch instanceof PsiBlockStatement) return;
                if (branch instanceof PsiEmptyStatement) return; // ex: "if (a);"
                if (isTerminalStatement(branch)) return;
                holder.registerProblem(
                        branch,
                        "Confusion atom: omitted curly braces may cause ambiguity about which statements belong to this block.",
                        ProblemHighlightType.WARNING,
                        new AddCurlyBracesQuickFix()
                );
            }
            private boolean isTerminalStatement(PsiStatement branch) {
                return branch instanceof PsiReturnStatement
                        || branch instanceof PsiBreakStatement
                        || branch instanceof PsiContinueStatement
                        || branch instanceof PsiThrowStatement;
            }
        };
    }
}