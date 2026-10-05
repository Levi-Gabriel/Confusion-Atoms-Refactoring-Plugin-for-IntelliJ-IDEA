package com.anon.aocrefactor.quickfix;

import com.anon.aocrefactor.util.FixUtils;
import com.anon.aocrefactor.util.OperatorUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Separates a post-increment/decrement from the expression that uses it.
 *
 * Two strategies, both semantics-preserving:
 *  - AFTER: "a = b++;"  ->  "a = b; b += 1;"   (only for plain assignments/initializers)
 *  - TEMP:  "return a[i++];" -> "int oldI = i; i += 1; return a[oldI];"
 *
 * Supported operands: local variables, parameters and fields ("x", "this.x", "Cls.x").
 * Fields are only accepted when nothing else in the same statement could read or
 * write them (no calls, no assignments, no other ++/--).
 *
 * The fix is only valid when {@link #isSafeToSeparate} returns true. The inspection
 * must only offer this fix in that case.
 */
public class ReplacePostIncrementQuickFix implements LocalQuickFix {

    /** Everything the fix needs to know, computed once by {@link #analyze}. */
    private record Plan(PsiStatement statement, PsiVariable variable, String operandText, boolean after) {}

    @Override
    public @NotNull String getName() {
        return "Separate post-increment/decrement into its own statement";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Separate post-increment/decrement";
    }

    public static boolean isSafeToSeparate(@NotNull PsiPostfixExpression expr) {
        return analyze(expr) != null;
    }

    // ------------------------------------------------------------------
    // Analysis
    // ------------------------------------------------------------------

    private static @Nullable Plan analyze(@NotNull PsiPostfixExpression expr) {
        // 1. Operand must be a local variable, a parameter or a field ("x", "this.x", "Cls.x").
        PsiExpression operand = PsiUtil.skipParenthesizedExprDown(expr.getOperand());
        if (!(operand instanceof PsiReferenceExpression ref)) return null;
        if (!isPlainQualifier(ref.getQualifierExpression())) return null;
        if (!(ref.resolve() instanceof PsiVariable variable)) return null;
        boolean isLocal = variable instanceof PsiLocalVariable || variable instanceof PsiParameter;
        if (!isLocal && !(variable instanceof PsiField)) return null;

        // 2. A standalone "i++;" has nothing to separate.
        PsiElement top = PsiUtil.skipParenthesizedExprUp(expr);
        if (top.getParent() instanceof PsiExpressionStatement) return null;

        // 3. Walk up to the enclosing statement, rejecting conditional evaluation.
        PsiElement child = expr;
        PsiElement parent = expr.getParent();
        while (parent != null && !(parent instanceof PsiStatement)) {
            if (parent instanceof PsiLambdaExpression || parent instanceof PsiClass) return null;
            if (parent instanceof PsiPolyadicExpression poly) {
                IElementType t = poly.getOperationTokenType();
                boolean shortCircuit = t == JavaTokenType.ANDAND || t == JavaTokenType.OROR;
                if (shortCircuit && poly.getOperands()[0] != child) return null;
            }
            if (parent instanceof PsiConditionalExpression cond && cond.getCondition() != child) return null;
            child = parent;
            parent = parent.getParent();
        }
        if (!(parent instanceof PsiStatement stmt)) return null;

        // 4. Only statements that evaluate the expression exactly once, unconditionally.
        //    Loop conditions are excluded on purpose: they are re-evaluated each iteration.
        PsiElement scope;
        if (stmt instanceof PsiIfStatement ifStmt) {
            scope = ifStmt.getCondition();
        } else if (stmt instanceof PsiSwitchStatement sw) {
            scope = sw.getExpression();
        } else if (stmt instanceof PsiExpressionStatement
                || stmt instanceof PsiDeclarationStatement
                || stmt instanceof PsiReturnStatement
                || stmt instanceof PsiThrowStatement
                || stmt instanceof PsiYieldStatement) {
            scope = stmt;
        } else {
            return null;
        }
        if (scope == null) return null;

        // for (init; cond; update): init/update statements cannot be moved.
        if (stmt.getParent() instanceof PsiForStatement f
                && (f.getInitialization() == stmt || f.getUpdate() == stmt)) {
            return null;
        }

        // 5. The variable must appear exactly once in the evaluated part
        //    (otherwise "x = i++ + i" or "a[i++] = b[i]" would change meaning).
        long uses = PsiTreeUtil.findChildrenOfType(scope, PsiReferenceExpression.class).stream()
                .filter(r -> r.isReferenceTo(variable))
                .count();
        if (uses != 1) return null;

        // 6. A field can be observed by anything evaluated in the same statement
        //    (method calls, constructors, assignments, other ++/--). Locals cannot.
        if (!isLocal && hasSideEffects(scope, expr)) return null;

        return new Plan(stmt, variable, ref.getText(), canIncrementAfter(top));
    }

    /** Accepts no qualifier, "this" / "Outer.this", or a class name (static field). */
    private static boolean isPlainQualifier(@Nullable PsiExpression qualifier) {
        if (qualifier == null) return true;
        if (qualifier instanceof PsiThisExpression) return true;
        return qualifier instanceof PsiReferenceExpression q && q.resolve() instanceof PsiClass;
    }

    private static boolean hasSideEffects(@NotNull PsiElement scope, @NotNull PsiPostfixExpression self) {
        if (!PsiTreeUtil.findChildrenOfAnyType(scope,
                PsiCallExpression.class, PsiAssignmentExpression.class).isEmpty()) {
            return true;
        }
        return PsiTreeUtil.findChildrenOfType(scope, PsiUnaryExpression.class).stream()
                .filter(u -> u != self)
                .anyMatch(u -> OperatorUtils.isIncrementOrDecrement(u.getOperationTokenType()));
    }

    /**
     * True for "x = i++;" and "T x = i++;": nothing between evaluating i++ and
     * storing the result can throw or observe i, so "x = i; i += 1;" is equivalent.
     */
    private static boolean canIncrementAfter(@NotNull PsiElement top) {
        PsiElement holder = top.getParent();

        if (holder instanceof PsiLocalVariable lv) {
            if (lv.getInitializer() == top) {
                if (lv.getParent() instanceof PsiDeclarationStatement) {
                    return true;
                }
            }
        }

        if (!(holder instanceof PsiAssignmentExpression asg)) return false;
        if (!(asg.getOperationTokenType() == JavaTokenType.EQ)) return false;
        if (!(asg.getRExpression() == top)) return false;
        if (!(asg.getLExpression() instanceof PsiReferenceExpression lhs)) return false;
        if (!(lhs.getQualifierExpression() == null)) return false;
        if (!(lhs.resolve() instanceof PsiVariable)) return false;
        return asg.getParent() instanceof PsiExpressionStatement;
    }

    // ------------------------------------------------------------------
    // Fix
    // ------------------------------------------------------------------

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiPostfixExpression postfix =
                FixUtils.findSelfOrChild(descriptor.getPsiElement(), PsiPostfixExpression.class);
        if (postfix == null) return;

        // Re-check: the code may have changed since the inspection ran.
        Plan plan = analyze(postfix);
        if (plan == null) return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiStatement stmt = plan.statement();
        PsiVariable variable = plan.variable();
        String name = plan.operandText();   // "count", "this.count", "Counter.count"
        String op = postfix.getOperationTokenType() == JavaTokenType.PLUSPLUS ? "+=" : "-=";

        // "b += 1;" instead of "b++;" so the refactored code no longer contains the atom.
        PsiStatement incStatement = factory.createStatementFromText(name + " " + op + " 1;", stmt);

        // TEMP strategy: "T oldX = x;" placed before, usage replaced by oldX.
        PsiStatement tempDecl = null;
        String replacementText = name;
        if (!plan.after()) {
            String tempName = JavaCodeStyleManager.getInstance(project)
                    .suggestUniqueVariableName("old" + capitalize(variable.getName()), stmt, true);
            tempDecl = factory.createStatementFromText(
                    variable.getType().getCanonicalText() + " " + tempName + " = " + name + ";", stmt);
            replacementText = tempName;
        }

        // Replace "i++" by the plain value.
        postfix.replace(factory.createExpressionFromText(replacementText, stmt));

        PsiElement container = stmt.getParent();
        if (container instanceof PsiCodeBlock) {
            if (plan.after()) {
                container.addAfter(incStatement, stmt);
            } else {
                PsiElement addedDecl = container.addBefore(tempDecl, stmt);
                container.addBefore(incStatement, stmt);
                JavaCodeStyleManager.getInstance(project).shortenClassReferences(addedDecl);
            }
            return;
        }

        // Statement is the body of an if/else/loop/label/arrow-case: wrap it in a block.
        PsiBlockStatement block = (PsiBlockStatement) factory.createStatementFromText("{}", stmt);
        PsiCodeBlock body = block.getCodeBlock();
        PsiElement rBrace = body.getRBrace();
        if (plan.after()) {
            body.addBefore(stmt.copy(), rBrace);
            body.addBefore(incStatement, rBrace);
        } else {
            body.addBefore(tempDecl, rBrace);
            body.addBefore(incStatement, rBrace);
            body.addBefore(stmt.copy(), rBrace);
        }
        PsiBlockStatement placed = (PsiBlockStatement) stmt.replace(block);
        if (!plan.after()) {
            JavaCodeStyleManager.getInstance(project)
                    .shortenClassReferences(placed.getCodeBlock().getStatements()[0]);
        }
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}