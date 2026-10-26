package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.OmittedCurlyBracesQuickFix;
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
                // Ignore "else if": this elseBranch is another PsiIfStatement and will be
                // checked when the visitor visits it directly (avoids duplication)
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

            // Marks the statement with a warning if it is empty,
            // is not inside a { } block,
            // or the branch is null.

            private void checkBranch(PsiStatement branch) {
                if (branch == null) return;
                if (branch instanceof PsiBlockStatement) return;
                if (branch instanceof PsiEmptyStatement) return; // ex: "if (a);"
                if (isTerminalStatement(branch)) return;
                holder.registerProblem(
                        branch,
                        "Omitted curly braces may cause ambiguity about which statements belong to this block.",
                        ProblemHighlightType.WEAK_WARNING,
                        new OmittedCurlyBracesQuickFix()
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