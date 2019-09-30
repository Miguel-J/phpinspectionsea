package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class NullCoalescingOperatorCorrectnessStrategy {
    private static final String messagePattern = "The operation results to '%s', the right operand can be omitted.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (expression.getOperationType() == PhpTokenTypes.opCOALESCE) {
            final PsiElement left = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getLeftOperand());
            if (left instanceof UnaryExpression) {
                final PsiElement operation = ((UnaryExpression) left).getOperation();
                if (operation != null) {
                    final IElementType operator = operation.getNode().getElementType();
                    if (result = (operator == PhpTokenTypes.opNOT || PhpTokenTypes.tsCAST_OPS.contains(operator))) {
                        holder.registerProblem(left, String.format(messagePattern, left.getText()));
                    }
                }
            } else if (left instanceof BinaryExpression) {
                final IElementType operator = ((BinaryExpression) left).getOperationType();
                if (result = (operator != PhpTokenTypes.opCOALESCE)) {
                    holder.registerProblem(left, String.format(messagePattern, left.getText()));
                }
            }
        }
        return result;
    }
}
