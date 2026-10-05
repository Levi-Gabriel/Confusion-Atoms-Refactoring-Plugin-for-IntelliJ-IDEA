package com.anon.aocrefactor.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class AddCurlyBracesQuickFix implements LocalQuickFix {

    @Override
    public @NotNull String getName() {
        return "Add curly braces";
    }

    @Override
    public @NotNull String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {

        PsiElement element = descriptor.getPsiElement();

        if (!(element instanceof PsiStatement branch)) return;
        if (!branch.isValid()) return;

        if (branch instanceof PsiBlockStatement) return; // já tem chaves, nada a fazer

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

        String blockText = "{\n" + branch.getText() + "\n}";

        PsiStatement blockStatement = factory.createStatementFromText(blockText, branch);
        branch.replace(blockStatement);
    }
}