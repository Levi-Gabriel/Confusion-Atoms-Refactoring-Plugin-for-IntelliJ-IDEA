package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.ReplacePostIncrementQuickFix;
import com.anon.aocrefactor.util.FixUtils;
import com.anon.aocrefactor.util.OperatorUtils;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class PostIncrementDecrementInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final String MESSAGE_WITH_FIX =
            "%s operator may cause confusion.";
    private static final String MESSAGE_NO_FIX =
            "%s operator may cause confusion. "
                    + "No automatic fix: it cannot be separated without changing behavior.";

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitPostfixExpression(@NotNull PsiPostfixExpression expression) {
                super.visitPostfixExpression(expression);

                IElementType op = expression.getOperationTokenType();
                if (!OperatorUtils.isIncrementOrDecrement(op)) return;

                PsiElement parent = expression.getParent();
                if (!FixUtils.isValidIncrementDecrementContext(parent)) return;

                String opName;
                if (op == JavaTokenType.PLUSPLUS) {
                    opName = "post-increment (++)";
                } else {
                    opName = "post-decrement (--)";
                }

                PsiElement highlightTarget =
                        FixUtils.getIncrementDecrementHighlightTarget(parent, expression);

                // Offer the fix only when it is guaranteed to preserve behavior.
                boolean safe = ReplacePostIncrementQuickFix.isSafeToSeparate(expression);
                LocalQuickFix[] fixes;
                if (safe) {
                    fixes = new LocalQuickFix[]{new ReplacePostIncrementQuickFix()};
                } else {
                    fixes = LocalQuickFix.EMPTY_ARRAY;
                }

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