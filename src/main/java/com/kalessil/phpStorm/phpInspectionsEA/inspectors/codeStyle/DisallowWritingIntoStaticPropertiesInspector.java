package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Funivan <alotofall@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DisallowWritingIntoStaticPropertiesInspector extends PhpInspection {
    // Inspection options.
    public boolean ALLOW_WRITE_FROM_SOURCE_CLASS = true;

    private static final String messageDisallowExternalWrites = "Static properties should be modified only inside the source class.";
    private static final String messageDisallowAnyWrites      = "Static properties should not be modified.";

    @NotNull
    @Override
    public String getShortName() {
        return "DisallowWritingIntoStaticPropertiesInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Disallow writing into static properties";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean b) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignmentExpression) {
                if (this.shouldSkipAnalysis(assignmentExpression, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

                final PsiElement candidate = assignmentExpression.getVariable();
                if (candidate instanceof FieldReference) {
                    final FieldReference fieldReference = (FieldReference) candidate;
                    final String fieldName              = fieldReference.getName();
                    if (fieldName != null && fieldReference.getReferenceType().isStatic()) {
                        /* short cut: report all static fields writes */
                        if (!ALLOW_WRITE_FROM_SOURCE_CLASS) {
                            holder.registerProblem(assignmentExpression, ReportingUtil.wrapReportedMessage(messageDisallowAnyWrites));
                            return;
                        }
                        /* analyze only external writes */
                        final String className = this.getReferencedClassName(fieldReference);
                        if (className != null && !className.equals("self")) {
                            final Function scope = ExpressionSemanticUtil.getScope(assignmentExpression);
                            if (scope instanceof Method) {
                                /* method context, ensure that caller shares the referenced field */
                                final PhpClass   caller          = ((Method) scope).getContainingClass();
                                final PsiElement referencedField = OpenapiResolveUtil.resolveReference((FieldReference) candidate);
                                if (referencedField != null && caller != ((Field) referencedField).getContainingClass()) {
                                    holder.registerProblem(assignmentExpression, ReportingUtil.wrapReportedMessage(messageDisallowExternalWrites));
                                }
                            } else {
                                /* global, late static bind and function contexts */
                                holder.registerProblem(assignmentExpression, ReportingUtil.wrapReportedMessage(messageDisallowExternalWrites));
                            }
                        }
                    }
                }
            }

            @Nullable
            private String getReferencedClassName(@NotNull FieldReference reference) {
                String result = null;
                final PsiElement classReference = reference.getClassReference();
                if (classReference instanceof ClassReference) {
                    result = ((ClassReference) classReference).getName();
                }
                return result;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Allow write from the source class", ALLOW_WRITE_FROM_SOURCE_CLASS, (isSelected) -> ALLOW_WRITE_FROM_SOURCE_CLASS = isSelected)
        );
    }
}
