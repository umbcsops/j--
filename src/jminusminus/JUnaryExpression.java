// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * This abstract base class is the AST node for an unary expression.
 * A unary expression has a single operand.
 */

abstract class JUnaryExpression extends JExpression {

    /** The operator. */
    private String operator;

    /** The operand. */
    protected JExpression arg;

    /**
     * Constructs an AST node for an unary expression given its line number, the
     * unary operator, and the operand.
     * 
     * @param line
     *            line in which the unary expression occurs in the source file.
     * @param operator
     *            the unary operator.
     * @param arg
     *            the operand.
     */

    protected JUnaryExpression(int line, String operator, JExpression arg) {
        super(line);
        this.operator = operator;
        this.arg = arg;
    }

    /**
     * {@inheritDoc}
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JUnaryExpression line=\"%d\" type=\"%s\" "
                + "operator=\"%s\">\n", line(), ((type == null) ? "" : type
                .toString()), Util.escapeSpecialXMLChars(operator));
        p.indentRight();
        p.printf("<Operand>\n");
        p.indentRight();
        arg.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Operand>\n");
        p.indentLeft();
        p.printf("</JUnaryExpression>\n");
    }

}

/**
 * The AST node for a unary negation (-) expression.
 */
class JNegateOp extends JUnaryExpression {

    /**
     * Constructs an AST node for a negation expression given its line number,
     * and the operand.
     * 
     * @param line
     *            line in which the negation expression occurs in the source
     *            file.
     * @param arg
     *            the operand.
     */

    public JNegateOp(int line, JExpression arg) {
        super(line, "-", arg);
    }

    /**
     * Analyzing the negation operation involves analyzing its operand, checking
     * its type and determining the result type.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        arg = arg.analyze(context);
        arg.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * Generating code for the negation operation involves generating code for
     * the operand, and then the negation instruction.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        arg.codegen(output);
        output.addNoArgInstruction(INEG);
    }
}

/**
 * The AST node for a unary plus (+) expression.
 */
class JUnaryPlusOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a unary plus expression given its line number,
     * and the operand.
     * 
     * @param line
     *            line in which the negation expression occurs in the source
     *            file.
     * @param arg
     *            the operand.
     */

    public JUnaryPlusOp(int line, JExpression arg) {
        super(line, "-", arg);
    }

    /**
     * Analyzing the unary plus operation involves analyzing its operand, checking
     * its type and determining the result type.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        arg = arg.analyze(context);
        arg.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * Generating code for the negation operation involves generating code for
     * the operand, and then a nop.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        arg.codegen(output);
        output.addNoArgInstruction(NOP);
    }
}

/**
 * The AST node for a logical NOT (!) expression.
 */

class JLogicalNotOp extends JUnaryExpression {

    /**
     * Constructs an AST for a logical NOT expression given its line number, and
     * the operand.
     * 
     * @param line
     *            line in which the logical NOT expression occurs in the source
     *            file.
     * @param arg
     *            the operand.
     */

    public JLogicalNotOp(int line, JExpression arg) {
        super(line, "!", arg);
    }

    /**
     * Analyzing a logical NOT operation means analyzing its operand, insuring
     * it's a boolean, and setting the result to boolean.
     * 
     * @param context
     *            context in which names are resolved.
     */

    public JExpression analyze(Context context) {
        arg = (JExpression) arg.analyze(context);
        arg.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * Generates code for the case where we actually want a boolean value 
     * ({@code true} or {@code false}) computed onto the stack. For example,
     * assignment to a boolean variable.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        String elseLabel = output.createLabel();
        String endIfLabel = output.createLabel();
        this.codegen(output, elseLabel, false);
        output.addNoArgInstruction(ICONST_1); // true
        output.addBranchInstruction(GOTO, endIfLabel);
        output.addLabel(elseLabel);
        output.addNoArgInstruction(ICONST_0); // false
        output.addLabel(endIfLabel);
    }

    /**
     * The code generation necessary for branching simply flips the condition on
     * which we branch.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        arg.codegen(output, targetLabel, !onTrue);
    }
}

/**
 * The AST node for an expr--.
 */

class JPostDecrementOp extends JUnaryExpression {

    /**
     * Constructs an AST node for an expr-- expression given its line number, and
     * the operand.
     * 
     * @param line
     *            line in which the expression occurs in the source file.
     * @param arg
     *            the operand.
     */

    public JPostDecrementOp(int line, JExpression arg) {
        super(line, "post--", arg);
    }

    /**
     * Analyzes the operand as a lhs (since there is a side effect), checks types
     * and determines the type of the result.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        if (!(arg instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line,
                    "Operand to expr-- must have an LValue.");
            type = Type.ANY;
        } else {
            arg = (JExpression) arg.analyze(context);
            arg.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        }
        return this;
    }

    /**
     * In generating code for a post-decrement operation, we treat simple
     * variable ({@link JVariable}) operands specially since the JVM has an 
     * increment instruction. 
     * Otherwise, we rely on the {@link JLhs} code generation support for
     * generating the proper code. Notice that we distinguish between
     * expressions that are statement expressions and those that are not; we
     * insure the proper value (before the decrement) is left atop the stack in
     * the latter case.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        if (arg instanceof JVariable) {
            // A local variable; otherwise analyze() would
            // have replaced it with an explicit field selection.
            int offset = ((LocalVariableDefn) ((JVariable) arg).iDefn())
                    .offset();
            if (!isStatementExpression) {
                // Loading its original rvalue
                arg.codegen(output);
            }
            output.addIINCInstruction(offset, -1);
        } else {
            ((JLhs) arg).codegenLoadLhsLvalue(output);
            ((JLhs) arg).codegenLoadLhsRvalue(output);
            if (!isStatementExpression) {
                // Loading its original rvalue
                ((JLhs) arg).codegenDuplicateRvalue(output);
            }
            output.addNoArgInstruction(ICONST_1);
            output.addNoArgInstruction(ISUB);
            ((JLhs) arg).codegenStore(output);
        }
    }

}

/**
 * The AST node for a ++expr expression.
 */

class JPreIncrementOp extends JUnaryExpression {

    /**
     * Constructs an AST node for a ++expr given its line number, and the
     * operand.
     * 
     * @param line
     *            line in which the expression occurs in the source file.
     * @param arg
     *            the operand.
     */

    public JPreIncrementOp(int line, JExpression arg) {
        super(line, "++pre", arg);
    }

    /**
     * Analyzes the operand as a lhs (since there is a side effect), check types
     * and determine the type of the result.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        if (!(arg instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line,
                    "Operand to ++expr must have an LValue.");
            type = Type.ANY;
        } else {
            arg = (JExpression) arg.analyze(context);
            arg.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        }
        return this;
    }

    /**
     * In generating code for a pre-increment operation, we treat simple
     * variable ({@link JVariable}) operands specially since the JVM has an 
     * increment instruction. 
     * Otherwise, we rely on the {@link JLhs} code generation support for
     * generating the proper code. Notice that we distinguish between
     * expressions that are statement expressions and those that are not; we
     * insure the proper value (after the increment) is left atop the stack in
     * the latter case.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        if (arg instanceof JVariable) {
            // A local variable; otherwise analyze() would
            // have replaced it with an explicit field selection.
            int offset = ((LocalVariableDefn) ((JVariable) arg).iDefn())
                    .offset();
            output.addIINCInstruction(offset, 1);
            if (!isStatementExpression) {
                // Loading its original rvalue
                arg.codegen(output);
            }
        } else {
            ((JLhs) arg).codegenLoadLhsLvalue(output);
            ((JLhs) arg).codegenLoadLhsRvalue(output);
            output.addNoArgInstruction(ICONST_1);
            output.addNoArgInstruction(IADD);
            if (!isStatementExpression) {
                // Loading its original rvalue
                ((JLhs) arg).codegenDuplicateRvalue(output);
            }
            ((JLhs) arg).codegenStore(output);
        }
    }
}

class JBitwiseNotOp extends JUnaryExpression {

    /**
     * Constructs an AST node for a ~expr given its line number, and the
     * operand.
     * 
     * @param line
     *            line in which the expression occurs in the source file.
     * @param arg
     *            the operand.
     */

    public JBitwiseNotOp(int line, JExpression arg) {
        super(line, "~pre", arg);
    }

    /**
     * Analyzes the operand as a lhs (since there is a side effect), check types
     * and determine the type of the result.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        if (!(arg instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line,
                    "Operand to ~expr must have an LValue.");
            type = Type.ANY;
        } else {
            arg = (JExpression) arg.analyze(context);
            arg.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        }
        return this;
    }

    /**
     * In generating code for a bitwise not operation. We rely on the 
     * {@link JLhs} code generation support for generating the proper code.
     * Notice that we distinguish between
     * expressions that are statement expressions and those that are not; we
     * insure the proper value (after the increment) is left atop the stack in
     * the latter case.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
      ((JLhs) arg).codegenLoadLhsLvalue(output);
      ((JLhs) arg).codegenLoadLhsRvalue(output);
      output.addNoArgInstruction(ICONST_M1);
      output.addNoArgInstruction(IXOR);
      if (!isStatementExpression) {
        // Loading its original rvalue
        ((JLhs) arg).codegenDuplicateRvalue(output);
      }
      ((JLhs) arg).codegenStore(output);
    }
}
