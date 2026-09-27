package com.anon.aocrefactor.quickfix;

import com.anon.aocrefactor.util.FixUtils;
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class ReplacePreIncrementQuickFix implements LocalQuickFix {

    @Override
    public @NotNull String getName() {
        return "Separate pre-increment/decrement into its own line";
    }

    @Override
    public @NotNull String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project,
                         @NotNull ProblemDescriptor descriptor) {

        PsiElement element = descriptor.getPsiElement();
        PsiPrefixExpression prefixExpr = FixUtils.findSelfOrChild(element, PsiPrefixExpression.class);
        if (prefixExpr == null) return;

        IElementType op = prefixExpr.getOperationTokenType();
        PsiExpression operand = prefixExpr.getOperand();
        if (operand == null) return;

        String varName = operand.getText();
        String opSymbol = op == JavaTokenType.PLUSPLUS ? "++" : "--";

        PsiStatement parentStatement =
                PsiTreeUtil.getParentOfType(prefixExpr, PsiStatement.class);
        if (parentStatement == null) return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

        PsiStatement incrementStatement = factory.createStatementFromText(
                varName + opSymbol + ";", null
        );

        PsiExpression simpleVar = factory.createExpressionFromText(varName, null);
        prefixExpr.replace(simpleVar);

        //Increment vai ANTES do statement pai.
        PsiElement parent = parentStatement.getParent();
        parent.addBefore(incrementStatement, parentStatement);
    }
}