package com.anon.aocrefactor.quickfix;

import com.anon.aocrefactor.util.FixUtils;
import com.anon.aocrefactor.util.OperatorUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReplacePreIncrementQuickFix implements LocalQuickFix {

    /** Everything the fix needs to know, computed once by {@link #analyze}. */
    private record Plan(PsiStatement statement, String operandText) {}

    @Override
    public @NotNull String getName() {
        return "Separate pre-increment/decrement into its own statement";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Separate pre-increment/decrement";
    }

    public static boolean isSafeToSeparate(@NotNull PsiPrefixExpression expr) {
        return analyze(expr) != null;
    }

    // ------------------------------------------------------------------
    // Analysis
    // ------------------------------------------------------------------

    private static @Nullable Plan analyze(@NotNull PsiPrefixExpression expr) {
        // 0. Prefix also covers "-x", "!x", "~x": only ++/-- matter here.
        if (!OperatorUtils.isIncrementOrDecrement(expr.getOperationTokenType())) return null;

        // 1. Operand must be a local variable, a parameter or a field ("x", "this.x", "Cls.x").
        PsiExpression operand = PsiUtil.skipParenthesizedExprDown(expr.getOperand());
        if (!(operand instanceof PsiReferenceExpression ref)) return null;
        if (!isPlainQualifier(ref.getQualifierExpression())) return null;
        if (!(ref.resolve() instanceof PsiVariable variable)) return null;
        boolean isLocal = variable instanceof PsiLocalVariable || variable instanceof PsiParameter;
        if (!isLocal && !(variable instanceof PsiField)) return null;

        // 2. A standalone "++i;" has nothing to separate.
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
        if (stmt.getParent() instanceof PsiForStatement f) {
            if ((f.getInitialization() == stmt || f.getUpdate() == stmt)) {
                return null;
            }
        }

        // "case X -> foo(++i);" inside a switch *expression*: wrapping in a block
        // would require a "yield", so we do not offer the fix.
        if (stmt.getParent() instanceof PsiSwitchLabeledRuleStatement rule) {
            if (rule.getEnclosingSwitchBlock() instanceof PsiSwitchExpression) {
                return null;
            }
        }

        // "int a = 0, b = ++a;": the increment would be moved before the declaration
        // of the variable it modifies.
        if (isLocal) {
            if (PsiTreeUtil.isAncestor(stmt, variable, true)) return null;
        }

        // 5. The variable must appear exactly once in the evaluated part
        //    (otherwise "x = i + ++i" would change meaning).
        long uses = PsiTreeUtil.findChildrenOfType(scope, PsiReferenceExpression.class).stream()
                .filter(r -> r.isReferenceTo(variable))
                .count();
        if (uses != 1) return null;

        // 6. A field can be observed by anything evaluated in the same statement
        //    (method calls, constructors, assignments, other ++/--). Locals cannot.
        if (!isLocal) {
            if (hasSideEffects(scope, expr)) {
                return null;
            }
        }

        return new Plan(stmt, ref.getText());
    }

    /** Accepts no qualifier, "this" / "Outer.this", or a class name (static field). */
    private static boolean isPlainQualifier(@Nullable PsiExpression qualifier) {
        if (qualifier == null) return true;
        if (qualifier instanceof PsiThisExpression) return true;
        if (!(qualifier instanceof PsiReferenceExpression q)) return false;
        return q.resolve() instanceof PsiClass;
    }

    private static boolean hasSideEffects(@NotNull PsiElement scope, @NotNull PsiPrefixExpression self) {
        if (!PsiTreeUtil.findChildrenOfAnyType(scope,
                PsiCallExpression.class, PsiAssignmentExpression.class).isEmpty()) {
            return true;
        }
        return PsiTreeUtil.findChildrenOfType(scope, PsiUnaryExpression.class).stream()
                .filter(u -> u != self)
                .anyMatch(u -> OperatorUtils.isIncrementOrDecrement(u.getOperationTokenType()));
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiPrefixExpression prefix =
                FixUtils.findSelfOrChild(descriptor.getPsiElement(), PsiPrefixExpression.class);
        if (prefix == null) return;

        // Re-check: the code may have changed since the inspection ran.
        Plan plan = analyze(prefix);
        if (plan == null) return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiStatement stmt = plan.statement();
        String name = plan.operandText();   // "count", "this.count", "Counter.count"
        String op = prefix.getOperationTokenType() == JavaTokenType.PLUSPLUS ? "+=" : "-=";

        // "b += 1;" instead of "++b;" so the refactored code no longer contains the atom.
        PsiStatement incStatement = factory.createStatementFromText(name + " " + op + " 1;", stmt);

        // Replace "++i" by the plain variable.
        prefix.replace(factory.createExpressionFromText(name, stmt));

        PsiElement container = stmt.getParent();
        if (container instanceof PsiCodeBlock) {
            container.addBefore(incStatement, stmt);
            return;
        }

        // Statement is the body of an if/else/loop/label/arrow-case: wrap it in a block.
        PsiBlockStatement block = (PsiBlockStatement) factory.createStatementFromText("{}", stmt);
        PsiCodeBlock body = block.getCodeBlock();
        PsiElement rBrace = body.getRBrace();
        body.addBefore(incStatement, rBrace);
        body.addBefore(stmt.copy(), rBrace);
        stmt.replace(block);
    }
}