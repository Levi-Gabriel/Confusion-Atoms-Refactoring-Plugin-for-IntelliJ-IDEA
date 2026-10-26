package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.ReplacePreIncrementQuickFix;
import com.anon.aocrefactor.util.FixUtils;
import com.anon.aocrefactor.util.OperatorUtils;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class PreIncrementDecrementInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final String MESSAGE_WITH_FIX =
            "%s operator may cause confusion.";
    private static final String MESSAGE_NO_FIX =
            "%s operator may cause confusion."
                    + "No automatic fix: it cannot be separated without changing behavior.";

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitPrefixExpression(@NotNull PsiPrefixExpression expression) {
                super.visitPrefixExpression(expression);

                IElementType op = expression.getOperationTokenType();
                if (!OperatorUtils.isIncrementOrDecrement(op)) return;

                PsiElement parent = expression.getParent();
                if (!FixUtils.isValidIncrementDecrementContext(parent)) return;

                String opName;
                if (op == JavaTokenType.PLUSPLUS) {
                    opName = "pre-increment (++)";
                } else {
                    opName = "pre-decrement (--)";
                }

                PsiElement highlightTarget =
                        FixUtils.getIncrementDecrementHighlightTarget(parent, expression);

                // Offer the fix only when it is guaranteed to preserve behavior.
                boolean safe = ReplacePreIncrementQuickFix.isSafeToSeparate(expression);
                LocalQuickFix[] fixes;
                if (safe) {
                    fixes = new LocalQuickFix[]{new ReplacePreIncrementQuickFix()};
                } else {
                    fixes = LocalQuickFix.EMPTY_ARRAY;
                }

                //noinspection ConditionalOperator
                holder.registerProblem(
                        highlightTarget,
                        String.format(safe ? MESSAGE_WITH_FIX : MESSAGE_NO_FIX, opName),
                        ProblemHighlightType.WEAK_WARNING,
                        fixes
                );
            }
        };
    }
}