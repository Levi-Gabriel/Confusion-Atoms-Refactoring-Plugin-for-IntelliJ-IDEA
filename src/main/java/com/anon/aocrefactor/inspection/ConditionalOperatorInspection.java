package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.ReplaceConditionalOperatorQuickFix;
import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class ConditionalOperatorInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            @NotNull ProblemsHolder holder,
            boolean isOnTheFly) {

        return new JavaElementVisitor() {
            @Override
            public void visitConditionalExpression(
                    @NotNull PsiConditionalExpression expression) {
                super.visitConditionalExpression(expression);
                holder.registerProblem(
                        expression,
                        "Ternary operator may cause confusion.",
                        ProblemHighlightType.WEAK_WARNING,
                        new ReplaceConditionalOperatorQuickFix()
                );
            }
        };

    }

}