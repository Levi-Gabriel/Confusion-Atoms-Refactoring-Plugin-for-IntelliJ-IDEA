package com.anon.aocrefactor.quickfix;

import com.anon.aocrefactor.util.FixUtils;
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

public class ReplacePostIncrementQuickFix implements LocalQuickFix {

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

    private static @Nullable Plan analyze(@NotNull PsiPostfixExpression expr) {
        // Operand must be a local variable, a parameter or a field ("x", "this.x", "Cls.x", "obj.x").
        PsiExpression operand = PsiUtil.skipParenthesizedExprDown(expr.getOperand());
        if (!(operand instanceof PsiReferenceExpression ref)) return null;
        if (!isSimpleOperandQualifier(ref.getQualifierExpression())) return null;
        if (!(ref.resolve() instanceof PsiVariable variable)) return null;
        boolean isLocal = variable instanceof PsiLocalVariable || variable instanceof PsiParameter;
        if (!isLocal && !(variable instanceof PsiField)) return null;

        // A standalone "i++;" has nothing to separate.
        PsiElement top = PsiUtil.skipParenthesizedExprUp(expr);
        if (top.getParent() instanceof PsiExpressionStatement) return null;

        // Walk up to the enclosing statement, rejecting conditional evaluation.
        PsiElement child = expr;
        PsiElement parent = expr.getParent();
        while (parent != null && !(parent instanceof PsiStatement)) {
            if (parent instanceof PsiLambdaExpression || parent instanceof PsiClass) return null;
            if (parent instanceof PsiPolyadicExpression poly) {
                IElementType t = poly.getOperationTokenType();
                boolean shortCircuit = t == JavaTokenType.ANDAND || t == JavaTokenType.OROR;
                if (shortCircuit) {
                    if (poly.getOperands()[0] != child) return null;
                }
            }
            if (parent instanceof PsiConditionalExpression cond) {
                if (cond.getCondition() != child) return null;
            }
            child = parent;
            parent = parent.getParent();
        }
        if (!(parent instanceof PsiStatement stmt)) return null;

        //    Only statements that evaluate the expression exactly once, unconditionally.
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
            if (f.getInitialization() == stmt) {
                return null;
            } else if (f.getUpdate() == stmt) {
                return null;
            }
        }

        // 5. The variable must appear exactly once in the evaluated part
        //    (otherwise "x = i++ + i" or "a[i++] = b[i]" would change meaning).
        if (countReferences(scope, variable) != 1) return null;

        // The objects in the qualifier ("point" in "point.x++") must also appear only once,
        // so that nothing in the statement can redirect them between the read and the write.
        if (!qualifierVariablesUsedOnce(ref, scope)) return null;

        boolean after = canIncrementAfter(top, false);

        // 6. A field can be observed by anything evaluated BEFORE the "i++" in the same
        //    statement (method calls, constructors, array reads...). Moving the increment
        //    to BEFORE the statement would then change behavior. In that case the only
        //    safe option is to increment AFTER the statement, which requires that nothing
        //    evaluated after the "i++" can observe the field or throw.
        //    Locals cannot be observed, so they are exempt.
        if (!isLocal) {
            if (!after) {
                if (hasObservingExpressionBefore(scope, expr)) {
                    after = canIncrementAfter(top, true);
                    if (!after) return null;
                }
            }
        }

        return new Plan(stmt, variable, ref.getText(), after);
    }

    private static long countReferences(@NotNull PsiElement scope, @NotNull PsiVariable variable) {
        return PsiTreeUtil.findChildrenOfType(scope, PsiReferenceExpression.class).stream()
                .filter(r -> r.isReferenceTo(variable))
                .count();
    }

    private static boolean qualifierVariablesUsedOnce(@NotNull PsiReferenceExpression ref,
                                                      @NotNull PsiElement scope) {
        PsiExpression q = ref.getQualifierExpression();
        while (q instanceof PsiReferenceExpression qr) {
            if (qr.resolve() instanceof PsiVariable qv) {
                if (countReferences(scope, qv) != 1) return false;
            }
            q = qr.getQualifierExpression();
        }
        return true;
    }

    /**
     * Qualifier of the incremented operand: none, "this" / "Outer.this", a class name
     * (static field), or a chain of plain variables / fields ("point", "this.point", "a.b").
     */
    private static boolean isSimpleOperandQualifier(@Nullable PsiExpression qualifier) {
        if (qualifier == null) return true;
        if (qualifier instanceof PsiThisExpression) return true;
        if (!(qualifier instanceof PsiReferenceExpression q)) return false;
        PsiElement target = q.resolve();
        if (target instanceof PsiClass) return true;
        if (!(target instanceof PsiVariable)) return false;
        return isSimpleOperandQualifier(q.getQualifierExpression());
    }

    /** Accepts no qualifier, "this" / "Outer.this", or a class name (static field). */
    private static boolean isPlainQualifier(@Nullable PsiExpression qualifier) {
        if (qualifier == null) return true;
        if (qualifier instanceof PsiThisExpression) return true;
        if (!(qualifier instanceof PsiReferenceExpression q)) return false;
        return q.resolve() instanceof PsiClass;
    }

    /**
     * True if something evaluated before "i++" could observe or modify the field, or throw
     * (method calls, constructors, array reads, qualified accesses...). Trivial expressions and
     * ++/-- on plain locals/parameters are harmless: they cannot touch a field.
     */
    private static boolean hasObservingExpressionBefore(@NotNull PsiElement scope,
                                                        @NotNull PsiPostfixExpression self) {
        int start = self.getTextRange().getStartOffset();
        for (PsiExpression e : PsiTreeUtil.findChildrenOfType(scope, PsiExpression.class)) {
            if (e.getTextRange().getEndOffset() > start) continue; // not entirely before
            if (!isTrivial(e)) {
                if (!isLocalIncrement(e)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isTrivial(@NotNull PsiExpression e) {
        if (e instanceof PsiLiteralExpression
                || e instanceof PsiThisExpression
                || e instanceof PsiParenthesizedExpression) {
            return true;
        }
        if (e instanceof PsiReferenceExpression ref) {
            // "blocks", "this.blocks", "Cls.blocks": no NPE, no side effects
            return isPlainQualifier(ref.getQualifierExpression());
        }
        return false;
    }

    /** "++x", "--x", "x++", "x--" where x is a plain local variable or parameter. */
    private static boolean isLocalIncrement(@NotNull PsiExpression e) {
        PsiExpression operand;
        IElementType token;
        if (e instanceof PsiPrefixExpression p) {
            operand = p.getOperand();
            token = p.getOperationTokenType();
        } else if (e instanceof PsiPostfixExpression p) {
            operand = p.getOperand();
            token = p.getOperationTokenType();
        } else {
            return false;
        }
        if (token != JavaTokenType.PLUSPLUS && token != JavaTokenType.MINUSMINUS) return false;
        operand = PsiUtil.skipParenthesizedExprDown(operand);
        if (!(operand instanceof PsiReferenceExpression r)) return false;
        if (r.getQualifierExpression() != null) return false;
        PsiElement target = r.resolve();
        return target instanceof PsiLocalVariable || target instanceof PsiParameter;
    }

    /**
     * True when "x = i++;" / "T x = i++;" (and, with allowChain, "T x = a() + i++;" and
     * similar) can become "x = i; i += 1;": after "i++" is evaluated, nothing but plain
     * arithmetic and the final store remain, so nothing can throw or observe the field.
     *
     * With allowChain the "i++" must be the last operand of +, - or * over primitive operands
     * (division could throw), optionally wrapped in parentheses, primitive casts or unary +/-/~.
     */
    private static boolean canIncrementAfter(@NotNull PsiElement top, boolean allowChain) {
        PsiElement child = top;
        PsiElement holder = top.getParent();

        if (allowChain) {
            while (true) {
                if (holder instanceof PsiParenthesizedExpression) {
                    // transparent
                } else if (holder instanceof PsiPolyadicExpression poly) {
                    IElementType t = poly.getOperationTokenType();
                    if (t != JavaTokenType.PLUS && t != JavaTokenType.MINUS && t != JavaTokenType.ASTERISK) {
                        return false;
                    }
                    PsiExpression[] operands = poly.getOperands();
                    if (operands[operands.length - 1] != child) return false;
                    for (PsiExpression o : operands) {
                        if (!(o.getType() instanceof PsiPrimitiveType)) return false;
                    }
                } else if (holder instanceof PsiTypeCastExpression cast) {
                    if (!(cast.getType() instanceof PsiPrimitiveType)) return false;
                } else if (holder instanceof PsiPrefixExpression prefix) {
                    IElementType t = prefix.getOperationTokenType();
                    if (t != JavaTokenType.MINUS && t != JavaTokenType.PLUS && t != JavaTokenType.TILDE) {
                        return false;
                    }
                } else {
                    break;
                }
                child = holder;
                holder = holder.getParent();
            }
        }

        if (holder instanceof PsiLocalVariable lv) {
            if (lv.getInitializer() == child) {
                if (lv.getParent() instanceof PsiDeclarationStatement) {
                    return true;
                }
            }
        }

        if (!(holder instanceof PsiAssignmentExpression asg)) return false;
        if (!(asg.getOperationTokenType() == JavaTokenType.EQ)) return false;
        if (!(asg.getRExpression() == child)) return false;
        if (!(asg.getLExpression() instanceof PsiReferenceExpression lhs)) return false;
        if (!(lhs.getQualifierExpression() == null)) return false;
        if (!(lhs.resolve() instanceof PsiVariable)) return false;
        return asg.getParent() instanceof PsiExpressionStatement;
    }

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
        String name = plan.operandText();   // "count", "this.count", "Counter.count", "point.x"
        String op;
        if (postfix.getOperationTokenType() == JavaTokenType.PLUSPLUS) {
            op = "+=";
        } else {
            op = "-=";
        }

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
        if (s.isEmpty()) {
            return s;
        } else {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }
}