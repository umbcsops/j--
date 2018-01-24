// Copyright 2011 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for an instanceof expression, having two
 * arguments: an expression and a reference type.
 */

class JInstanceOfOp
    extends JExpression {

    /** The expression denoting the value to be tested. */
    private JExpression expr;

    /** The reference type we are testing for. */
    private Type typeSpec;

    /**
     * Construct an AST node for an instanceof expression given
     * its line number, the relational expression and reference
     * type.
     * 
     * @param line
     *                the line in which the instanceof expression
     *                occurs in the source file.
     * @param expr
     *                the expression denoting the value to be
     *                tested.
     * @param typeSpec
     *                the reference type we are testing for.
     */

    public JInstanceOfOp(int line, JExpression expr, Type typeSpec) {
        super(line);
        this.expr = expr;
        this.typeSpec = typeSpec;
    }

    /**
     * Analysis of an instanceof operation requires analyzing the
     * expression to be tested, resolving the type was are
     * testing for, and determining if the test is legal, or if
     * the answer can be determined at compile time.
     * 
     * @param context
     *                context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JInstanceOfOp analyze(Context context) {
        expr = (JExpression) expr.analyze(context);
        typeSpec = typeSpec.resolve(context);
        if (!typeSpec.isReference()) {
            JAST.compilationUnit.reportSemanticError(line(),
                "Type argument to instanceof "
                    + "operator must be a reference type");
        } else if (!(expr.type() == Type.NULLTYPE
            || expr.type() == Type.ANY || expr.type().isReference())) {
            JAST.compilationUnit.reportSemanticError(line(),
                "operand to instanceof "
                    + "operator must be a reference type");
        } else if (expr.type().isReference()
            && !typeSpec.isJavaAssignableFrom(expr.type())) {
            JAST.compilationUnit.reportSemanticError(line(),
                "It is impossible for the expression "
                    + "to be an instance of this type");
        }
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * Generate code for the type test.
     * 
     * @param output
     *                the code emitter (basically an abstraction
     *                for producing the .class file).
     */

    public void codegen(CLEmitter output) {
        expr.codegen(output);
        output.addReferenceInstruction(INSTANCEOF, typeSpec
            .toDescriptor());
    }

    /**
     * Short-circuiting branching for instanceof.
     *                                                                         
     * @param output   
     *       code emitter.
     * @param targetLabel                                             
     *            the label to which we should branch.                         
     * @param onTrue
     *            do we branch on true?   
     */

    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
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
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JInstanceOfOp line=\"%d\" type=\"%s\">\n", line(),
            ((type == null) ? "" : type.toString()));
        p.indentRight();
        p.printf("<RelationalExpression>\n");
        p.indentRight();
        expr.writeToStdOut(p);
        p.indentLeft();
        p.printf("</RelationalExpression>\n");
        p.printf("<ReferenceType value=\"%s\"/>\n",
            ((typeSpec == null) ? "" : typeSpec.toString()));
        p.indentLeft();
        p.printf("</JInstanceOfOp>\n");
    }
}
