package com.anon.aocrefactor.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class ReplaceConditionalOperatorQuickFix implements LocalQuickFix {

    @Override
    public @NotNull String getName() {
        return "Replace with if-else";
    }

    @Override
    public @NotNull String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project,
                         @NotNull ProblemDescriptor descriptor) {

        PsiConditionalExpression ternary =
                (PsiConditionalExpression) descriptor.getPsiElement();

        PsiExpression condition = ternary.getCondition();
        PsiExpression thenExpr  = ternary.getThenExpression();
        PsiExpression elseExpr  = ternary.getElseExpression();

        if (thenExpr == null || elseExpr == null) return;

        PsiStatement parentStatement =
                PsiTreeUtil.getParentOfType(ternary, PsiStatement.class);
        if (parentStatement == null) return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

        // Captura os textos antes de qualquer modificação da PSI
        String conditionText = condition.getText();
        String thenText = thenExpr.getText();
        String elseText = elseExpr.getText();

        // Sobe pelos parênteses: "(cond ? a : b)" também conta como o ternário inteiro
        PsiExpression topExpr = ternary;
        while (topExpr.getParent() instanceof PsiParenthesizedExpression) {
            topExpr = (PsiExpression) topExpr.getParent();
        }

        // Case 1: int x = cond ? a : b;  (o ternário é o inicializador inteiro)
        if (parentStatement instanceof PsiDeclarationStatement declStmt) {
            if (declStmt.getDeclaredElements().length == 1) {
                if (declStmt.getDeclaredElements()[0] instanceof PsiLocalVariable localVar) {
                    if (localVar.getInitializer() == topExpr) {
                        String varName = localVar.getName();
                        String varType = localVar.getType().getCanonicalText();
                        PsiStatement declaration = factory.createStatementFromText(
                                varType + " " + varName + ";", parentStatement);
                        PsiStatement ifElse = factory.createStatementFromText(
                                String.format("if (%s) { %s = %s; } else { %s = %s; }",
                                        conditionText, varName, thenText, varName, elseText),
                                parentStatement);
                        PsiElement parent = parentStatement.getParent();
                        parent.addBefore(declaration, parentStatement);
                        parent.addBefore(ifElse, parentStatement);
                        parentStatement.delete();
                        return;
                    }
                }
            }
        }

        // Case 2: return cond ? a : b;  (o ternário é o return inteiro)
        if (parentStatement instanceof PsiReturnStatement returnStmt
                && returnStmt.getReturnValue() == topExpr) {

            PsiStatement ifElse = factory.createStatementFromText(
                    String.format("if (%s) { return %s; } else { return %s; }",
                            conditionText, thenText, elseText),
                    parentStatement);
            parentStatement.replace(ifElse);
            return;
        }

        // Case 3: genérico — ternário dentro de uma expressão maior
        // ex.: return (c ? 31 : 1) * diameter;  /  println(c ? a : b);
        PsiType type = ternary.getType();
        String typeName;
        if ((type != null)) {
            typeName = type.getCanonicalText();
        } else {
            typeName = "java.lang.Object";
        }

        String tempVar = JavaCodeStyleManager.getInstance(project)
                .suggestUniqueVariableName("temp", parentStatement, true);

        PsiStatement tempDecl = factory.createStatementFromText(
                typeName + " " + tempVar + ";", parentStatement);

        PsiStatement ifElse = factory.createStatementFromText(
                String.format("if (%s) { %s = %s; } else { %s = %s; }",
                        conditionText, tempVar, thenText, tempVar, elseText),
                parentStatement);

        // Substitui o ternário (e os parênteses que o envolvem sozinhos) pela variável temporária
        PsiExpression tempVarExpr = factory.createExpressionFromText(tempVar, ternary);
        topExpr.replace(tempVarExpr);

        PsiElement parent = parentStatement.getParent();
        parent.addBefore(tempDecl, parentStatement);
        parent.addBefore(ifElse, parentStatement);
    }
}