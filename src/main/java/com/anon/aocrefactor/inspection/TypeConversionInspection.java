package com.anon.aocrefactor.inspection;

import com.anon.aocrefactor.quickfix.ReplaceTypeConversionQuickFix;
import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TypeConversionInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final java.util.Set<String> SAFE_CONVERSION_METHODS =
            java.util.Set.of("round", "floor", "ceil", "rint");

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            @NotNull ProblemsHolder holder,
            boolean isOnTheFly) {

        return new JavaElementVisitor() {

            @Override
            public void visitTypeCastExpression(@NotNull PsiTypeCastExpression expression) {
                super.visitTypeCastExpression(expression);

                PsiTypeElement castTypeElement = expression.getCastType();
                PsiExpression operand = expression.getOperand();
                if (castTypeElement == null || operand == null) return;

                PsiType castType = castTypeElement.getType();
                PsiType operandType = operand.getType();
                if (operandType == null) return;

                // Verifica se é narrowing conversion entre primitivos
                if (!isNarrowingConversion(operandType, castType)) return;

                // Regra 1: não deve ter invocação de método "seguro" no operando
                if (hasSafeConversionMethodInvocation(operand)) return;
                // Regra 2: não deve ter operação de módulo no operando
                if (hasModuloOperation(operand)) return;
                // Regra 3: já é um clamp explícito pro range do tipo alvo? Não marca.
                if (isClampedToRange(operand, castType)) return;

                // Regra 4: se for literal, só marca se o valor estiver fora do range do tipo alvo
                if (operand instanceof PsiLiteralExpression literal) {
                    if (!isProblematicLiteral(literal, operandType, castType)) return;
                }

                List<LocalQuickFix> fixes = new ArrayList<>();
                fixes.add(new ReplaceTypeConversionQuickFix(ReplaceTypeConversionQuickFix.Mode.ADD_COMMENT));

                boolean sourceIsFloatingPoint = sourceIsFloating(operandType);

                if (sourceIsFloatingPoint) {
                    fixes.add(new ReplaceTypeConversionQuickFix(ReplaceTypeConversionQuickFix.Mode.WRAP_MATH_FLOOR));
                } else {
                    fixes.add(new ReplaceTypeConversionQuickFix(ReplaceTypeConversionQuickFix.Mode.CLAMP_RANGE));
                }

                holder.registerProblem(
                        expression,
                        "Confusion atom: narrowing type conversion may cause precision loss or unexpected results.",
                        ProblemHighlightType.WARNING,
                        fixes.toArray(new LocalQuickFix[0])
                );
            }

            // Mapa de narrowing conversion conforme o algoritmo
            private boolean isNarrowingConversion(PsiType from, PsiType to) {
                String f = from.getCanonicalText();
                String t = to.getCanonicalText();

                return switch (f) {
                    case "byte" -> t.equals("char");
                    case "short" -> t.equals("byte") || t.equals("char");
                    case "char" -> t.equals("byte") || t.equals("short");
                    case "int" -> t.equals("byte") || t.equals("short") || t.equals("char");
                    case "long" -> t.equals("byte") || t.equals("short") || t.equals("char") || t.equals("int");
                    case "float" -> t.equals("byte") || t.equals("short") || t.equals("char")
                            || t.equals("int") || t.equals("long");
                    case "double" -> t.equals("byte") || t.equals("short") || t.equals("char")
                            || t.equals("int") || t.equals("long") || t.equals("float");
                    default -> false;
                };
            }

            // Verifica se há invocação de um método "seguro" (ex: Math.round(x)) no operando
            private boolean hasSafeConversionMethodInvocation(PsiExpression expr) {
                if (expr instanceof PsiMethodCallExpression call && isMathMethodCall(call, SAFE_CONVERSION_METHODS)) {
                    return true;
                }
                for (PsiElement child : expr.getChildren()) {
                    if (child instanceof PsiExpression childExpr && hasSafeConversionMethodInvocation(childExpr)) {
                        return true;
                    }
                }
                return false;
            }

            // Verifica se há operação de módulo no operando (ex: x % 256)
            private boolean hasModuloOperation(PsiExpression expr) {
                if (expr instanceof PsiBinaryExpression bin) {
                    if (bin.getOperationTokenType() == JavaTokenType.PERC) return true;
                    return hasModuloOperation(bin.getLOperand())
                            || (bin.getROperand() != null && hasModuloOperation(bin.getROperand()));
                }
                if (expr instanceof PsiParenthesizedExpression paren) {
                    PsiExpression inner = paren.getExpression();
                    return inner != null && hasModuloOperation(inner);
                }
                return false;
            }

            // Reconhece estruturalmente o padrão gerado pelo quick fix CLAMP_RANGE
            // Evitar continuar com o warning após aplicar o clamp

            private boolean isClampedToRange(PsiExpression operand, PsiType castType) {
                String[] bounds = rangeConstantsFor(castType);
                if (bounds == null) return false;

                PsiExpression outer = unwrapParentheses(operand);
                if (!(outer instanceof PsiMethodCallExpression maxCall)) return false;
                if (!isMathMethodCall(maxCall, "max")) return false;

                PsiExpression[] maxArgs = maxCall.getArgumentList().getExpressions();
                if (maxArgs.length != 2) return false;

                PsiExpression minBoundArg = maxArgs[0];
                PsiExpression inner = unwrapParentheses(maxArgs[1]);
                if (!(inner instanceof PsiMethodCallExpression minCall)) return false;
                if (!isMathMethodCall(minCall, "min")) return false;

                PsiExpression[] minArgs = minCall.getArgumentList().getExpressions();
                if (minArgs.length != 2) return false;

                PsiExpression maxBoundArg = minArgs[0];

                return textMatches(minBoundArg, bounds[0]) && textMatches(maxBoundArg, bounds[1]);
            }

            private boolean isMathMethodCall(PsiMethodCallExpression call, String methodName) {
                String referenceName = call.getMethodExpression().getReferenceName();
                return isMathMethodCall(call, java.util.Set.of(methodName)) && methodName.equals(referenceName);
            }

            private boolean isMathMethodCall(PsiMethodCallExpression call, java.util.Set<String> methodNames) {
                String methodName = call.getMethodExpression().getReferenceName();
                if (methodName == null || !methodNames.contains(methodName)) return false;

                PsiMethod resolved = call.resolveMethod();
                if (resolved == null) return false;

                PsiClass containingClass = resolved.getContainingClass();
                return containingClass != null && "java.lang.Math".equals(containingClass.getQualifiedName());
            }

            private PsiExpression unwrapParentheses(PsiExpression expr) {
                while (expr instanceof PsiParenthesizedExpression paren) {
                    PsiExpression inner = paren.getExpression();
                    if (inner == null) break;
                    expr = inner;
                }
                return expr;
            }

            // Compara ignorando espaços em branco, já que o texto é gerado a partir de
            // operand.getText() e pode ter sido reformatado (mas não reescrito) pelo usuário

            private boolean textMatches(PsiExpression expr, String expectedText) {
                String actual = expr.getText().replaceAll("\\s+", "");
                String expected = expectedText.replaceAll("\\s+", "");
                return actual.equals(expected);
            }

            // Verifica se o valor literal está fora do range do tipo alvo
            private boolean isOutOfRange(double d, PsiType targetType) {
                // imprecisão para long
                return switch (targetType.getCanonicalText()) {
                    case "byte" -> d < Byte.MIN_VALUE || d > Byte.MAX_VALUE;
                    case "short" -> d < Short.MIN_VALUE || d > Short.MAX_VALUE;
                    case "char" -> d < Character.MIN_VALUE || d > Character.MAX_VALUE;
                    case "int" -> d < Integer.MIN_VALUE || d > Integer.MAX_VALUE;
                    case "long" -> d < Long.MIN_VALUE || d > Long.MAX_VALUE;
                    case "float" -> d < -Float.MAX_VALUE || d > Float.MAX_VALUE;
                    default -> false;
                };
            }

            private boolean isProblematicLiteral(PsiLiteralExpression literal, PsiType operandType, PsiType targetType) {
                Object value = literal.getValue();
                double d;
                if (value instanceof Number num) {
                    d = num.doubleValue();
                } else if (value instanceof Character ch) {
                    d = ch;
                } else {
                    return false;
                }

                // Situação 1: overflow — o valor não cabe no tipo alvo
                if (isOutOfRange(d, targetType)) return true;

                // Situação 2: ponto flutuante -> integral, com parte fracionária que será truncada
                return isFloatingToIntegralConversion(operandType, targetType) && hasFraction(d);
            }

            private boolean isFloatingToIntegralConversion(PsiType operandType, PsiType targetType) {
                return (targetIsIntegral(targetType) && sourceIsFloating(operandType));
            }

            private boolean targetIsIntegral(PsiType targetType) {
                String targetName = targetType.getCanonicalText();

                return switch (targetName) {
                    case "byte", "short", "char", "long", "int" -> true;
                    default -> false;
                };
            }

            private boolean sourceIsFloating(PsiType operandType) {
                String sourceName = operandType.getCanonicalText();

                return switch (sourceName) {
                    case "float", "double" -> true;
                    default -> false;
                };
            }

            private boolean hasFraction(double d) { return d != Math.floor(d);
            }

            // Retorna {constante mínima, constante máxima} pro tipo alvo, ou null se não aplicável
            private String[] rangeConstantsFor(PsiType targetType) {
                return switch (targetType.getCanonicalText()) {
                    case "byte"  -> new String[]{"Byte.MIN_VALUE", "Byte.MAX_VALUE"};
                    case "short" -> new String[]{"Short.MIN_VALUE", "Short.MAX_VALUE"};
                    case "char"  -> new String[]{"(int) Character.MIN_VALUE", "(int) Character.MAX_VALUE"};
                    case "int"   -> new String[]{"Integer.MIN_VALUE", "Integer.MAX_VALUE"};
                    case "long"  -> new String[]{"Long.MIN_VALUE", "Long.MAX_VALUE"};
                    case "float" -> new String[]{"-Float.MAX_VALUE", "Float.MAX_VALUE"};
                    default -> null;
                };
            }

        };
    }
}