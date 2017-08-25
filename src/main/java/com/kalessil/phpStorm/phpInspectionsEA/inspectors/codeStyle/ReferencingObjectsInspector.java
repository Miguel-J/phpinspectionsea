package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ReferencingObjectsInspector extends BasePhpInspection {
    private static final String messageParameter  = "Objects are always passed by reference; please correct '& $%p%'.";
    private static final String messageAssignment = "Objects are always passed by reference; please correct '= & new '.";

    private static final PhpType php7Types = new PhpType();
    static {
        php7Types.add(PhpType.STRING);
        php7Types.add(PhpType.INT);
        php7Types.add(PhpType.FLOAT);
        php7Types.add(PhpType.BOOLEAN);
        php7Types.add(PhpType.ARRAY);
    }

    @NotNull
    public String getShortName() {
        return "ReferencingObjectsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* re-dispatch to inspector */
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                this.inspectCallable(method);
            }
            @Override
            public void visitPhpFunction(@NotNull Function function) {
                this.inspectCallable(function);
            }

            private void inspectCallable (@NotNull Function callable) {
                if (NamedElementUtil.getNameIdentifier(callable) != null) {
                    Arrays.stream(callable.getParameters())
                        .filter(p -> {
                            final PhpType declared = p.getDeclaredType();
                            return p.isPassByRef() && !declared.isEmpty() && !PhpType.isSubType(declared, php7Types);
                        })
                        .forEach(p -> {
                            final String message = messageParameter.replace("%p%", p.getName());
                            holder.registerProblem(p, message, new ParameterLocalFix(p));
                        });
                }
            }

            @Override
            public void visitPhpNewExpression(@NotNull NewExpression expression) {
                final PsiElement parent = expression.getParent();
                if (parent instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) parent;
                    if (assignment.getValue() == expression) {
                        PsiElement operation = assignment.getValue().getPrevSibling();
                        if (operation instanceof PsiWhiteSpace) {
                            operation = operation.getPrevSibling();
                        }

                        if (operation != null && operation.getText().replaceAll("\\s+","").equals("=&")) {
                            holder.registerProblem(expression, messageAssignment, new InstantiationLocalFix(operation));
                        }
                    }
                }
            }
        };
    }

    private static class InstantiationLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<PsiElement> assignOperator;

        InstantiationLocalFix(@NotNull PsiElement assignOperator) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(assignOperator.getProject());

            this.assignOperator = factory.createSmartPsiElementPointer(assignOperator);
        }

        @NotNull
        @Override
        public String getName() {
            return "Replace with regular assignment";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement assignOperator = this.assignOperator.getElement();
            if (null != assignOperator) {
                LeafPsiElement replacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "=");
                //noinspection ConstantConditions - expression is hardcoded so we safe from NPE here
                assignOperator.replace(replacement);
            }
        }
    }

    private static class ParameterLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<Parameter> parameter;

        ParameterLocalFix(@NotNull Parameter parameter) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(parameter.getProject());

            this.parameter = factory.createSmartPsiElementPointer(parameter);
        }

        @NotNull
        @Override
        public String getName() {
            return "Cleanup parameter definition";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final Parameter parameter = this.parameter.getElement();
            final PsiElement nameNode = NamedElementUtil.getNameIdentifier(parameter);
            if (null != nameNode) {
                PsiElement previous = nameNode.getPrevSibling();
                if (previous instanceof PsiWhiteSpace) {
                    previous = previous.getPrevSibling();
                    previous.getNextSibling().delete();
                }

                previous.delete();
            }
        }
    }
}
