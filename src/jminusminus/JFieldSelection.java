// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a field selection operation. It has a target object, a field
 * name, and the Field it defines.
 */

class JFieldSelection extends JExpression implements JLhs {

    /** The target expression. */
    protected JExpression target;

    /** The ambiguous part that is reclassified in analyze(). */
    private AmbiguousName ambiguousPart;

    /** The field name. */
    private String fieldName;

    /** The Field representing this field. */
    private Field field;

    /**
     * Construct an AST node for a field selection without an ambiguous part.
     * 
     * @param line
     *            the line number of the selection.
     * @param target
     *            the target of the selection.
     * @param fieldName
     *            the field name.
     */

    public JFieldSelection(int line, JExpression target, String fieldName) {
        this(line, null, target, fieldName);
    }

    /**
     * Construct an AST node for a field selection having an ambiguous part.
     * 
     * @param line
     *            line in which the field selection occurs in the source file.
     * @param ambiguousPart
     *            the ambiguous part.
     * @param target
     *            the target of the selection.
     * @param fieldName
     *            the field name.
     */

    public JFieldSelection(int line, AmbiguousName ambiguousPart,
            JExpression target, String fieldName) {
        super(line);
        this.ambiguousPart = ambiguousPart;
        this.target = target;
        this.fieldName = fieldName;
    }

    /**
     * Analyzing a field selection expression involves, (1) reclassifying any
     * ambiguous part, (2) analyzing the target, (3) treating "length" field of
     * arrays specially, or computing the Field object, (4) checking the access
     * rules, and (5) computing the resultant type.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        // Reclassify the ambiguous part.
        if (ambiguousPart != null) {
            JExpression expr = ambiguousPart.reclassify(context);
            if (expr != null) {
                if (target == null)
                    target = expr;
                else {
                    // Can't even happen syntactically
                    JAST.compilationUnit.reportSemanticError(line(),
                            "Badly formed suffix");
                }
            }
        }
        target = (JExpression) target.analyze(context);
        Type targetType = target.type();

        // We use a workaround for the "length" field of arrays.
        if ((targetType.isArray()) && fieldName.equals("length")) {
            type = Type.INT;
        } else {
            // Other than that, targetType has to be a
            // ReferenceType
            if (targetType.isPrimitive()) {
                JAST.compilationUnit.reportSemanticError(line(),
                        "Target of a field selection must "
                                + "be a defined type");
                type = Type.ANY;
                return this;
            }
            field = targetType.fieldFor(fieldName);
            if (field == null) {
                JAST.compilationUnit.reportSemanticError(line(),
                        "Cannot find a field: " + fieldName);
                type = Type.ANY;
            } else {
                context.definingType().checkAccess(line, (Member) field);
                type = field.type();

                // Non-static field cannot be referenced from a static context.
                if (!field.isStatic()) {
                    if (target instanceof JVariable
                            && ((JVariable) target).iDefn() instanceof TypeNameDefn) {
                        JAST.compilationUnit
                                .reportSemanticError(
                                        line(),
                                        "Non-static field "
                                                + fieldName
                                                + " cannot be referenced from a static context");
                    }
                }
            }
        }
        return this;
    }

    /**
     * Analyze the field selection expression for use on the lhs of an
     * assignment. Although the final keyword is not in j--, we do make use of
     * the Java api and so must repect its constraints.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyzeLhs(Context context) {
        JExpression result = analyze(context);
        if (field.isFinal()) {
            JAST.compilationUnit.reportSemanticError(line, "The field "
                    + fieldName + " in type " + target.type.toString()
                    + " is declared final.");
        }
        return result;
    }

    /**
     * Generate the code necessary to load the Rvalue for this field selection.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        target.codegen(output);

        // We use a workaround for the "length" field of arrays
        if ((target.type().isArray()) && fieldName.equals("length")) {
            output.addNoArgInstruction(ARRAYLENGTH);
        } else {
            int mnemonic = field.isStatic() ? GETSTATIC : GETFIELD;
            output.addMemberAccessInstruction(mnemonic,
                    target.type().jvmName(), fieldName, type.toDescriptor());
        }
    }

    /**
     * The semantics of j-- require that we implement short-circuiting branching
     * in implementing field selections.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     * @param targetLabel
     *            the label to which we should branch.
     * @param onTrue
     *            do we branch on true?
     */

    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        // Push the value
        codegen(output);

        if (onTrue) {
            // Branch on true
            output.addBranchInstruction(IFNE, targetLabel);
        } else {
            // Branch on false
            output.addBranchInstruction(IFEQ, targetLabel);
        }
    }

    /**
     * Generate the code required for setting up an Lvalue, eg, for use in an
     * assignment.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegenLoadLhsLvalue(CLEmitter output) {
        // Nothing to do for static fields.
        if (!field.isStatic()) {
            // Just load the target
            target.codegen(output);
        }
    }

    /**
     * Generate the code required for loading an Rvalue for this variable, eg
     * for use in a +=. Here, this requires either a getstatic or getfield.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegenLoadLhsRvalue(CLEmitter output) {
        String descriptor = field.type().toDescriptor();
        if (field.isStatic()) {
            output.addMemberAccessInstruction(GETSTATIC, target.type()
                    .jvmName(), fieldName, descriptor);
        } else {
            output.addNoArgInstruction(type == Type.STRING ? DUP_X1 : DUP);
            output.addMemberAccessInstruction(GETFIELD,
                    target.type().jvmName(), fieldName, descriptor);
        }
    }

    /**
     * Generate the code required for duplicating the Rvalue that is on the
     * stack becuase it is to be used in a surrounding expression, as in a[i] =
     * x = <expr> or x = y--. Here this means copying it down
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegenDuplicateRvalue(CLEmitter output) {
        if (field.isStatic()) {
            output.addNoArgInstruction(DUP);
        } else {
            output.addNoArgInstruction(DUP_X1);
        }
    }

    /**
     * Generate the code required for doing the actual assignment.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegenStore(CLEmitter output) {
        String descriptor = field.type().toDescriptor();
        if (field.isStatic()) {
            output.addMemberAccessInstruction(PUTSTATIC, target.type()
                    .jvmName(), fieldName, descriptor);
        } else {
            output.addMemberAccessInstruction(PUTFIELD,
                    target.type().jvmName(), fieldName, descriptor);
        }
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JFieldSelection line=\"%d\" name=\"%s\"/>\n", line(),
                fieldName);
        p.indentRight();
        if (target != null) {
            p.println("<Target>");
            p.indentRight();
            target.writeToStdOut(p);
            p.indentLeft();
            p.println("</Target>");
        }
        p.indentLeft();
        p.println("</JFieldSelection>");
    }

}
