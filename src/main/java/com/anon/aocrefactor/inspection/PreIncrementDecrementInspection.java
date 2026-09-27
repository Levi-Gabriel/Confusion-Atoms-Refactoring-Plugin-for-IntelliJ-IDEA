package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.ReplacePreIncrementQuickFix;
import com.anon.aocrefactor.util.OperatorUtils;
import com.anon.aocrefactor.util.FixUtils;
import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class PreIncrementDecrementInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            @NotNull ProblemsHolder holder,
            boolean isOnTheFly) {

        return new JavaElementVisitor() {
            @Override
            public void visitPrefixExpression(@NotNull PsiPrefixExpression expression) {
                super.visitPrefixExpression(expression);

                IElementType op = expression.getOperationTokenType();
                if (!OperatorUtils.isIncrementOrDecrement(op)) return;

                PsiElement parent = expression.getParent();
                if (!FixUtils.isValidIncrementDecrementContext(parent)) return;

                String opName = op == JavaTokenType.PLUSPLUS
                        ? "pre-increment (++)" : "pre-decrement (--)";

                PsiElement highlightTarget = FixUtils.getIncrementDecrementHighlightTarget(parent, expression);

                holder.registerProblem(
                        highlightTarget,
                        "Confusion atom: " + opName + " operator may cause confusion. Consider separating the operation.",
                        ProblemHighlightType.WARNING,
                        new ReplacePreIncrementQuickFix()
                );
            }
        };
    }
}