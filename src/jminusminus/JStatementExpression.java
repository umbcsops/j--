// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * The AST node for an expression that appears as a statement. Only the
 * expressions that have a side-effect are valid statement expressions.
 */

class JStatementExpression extends JStatement {

    /** The expression. */
    JExpression expr;

    /**
     * Construct an AST node for a statement expression given its line number,
     * and expression.
     * 
     * @param line
     *            line in which the expression occurs in the source file.
     * @param expr
     *            the expression.
     */

    public JStatementExpression(int line, JExpression expr) {
        super(line);
        this.expr = expr;
    }

    /**
     * Analysis involves analyzing the encapsulated expression if indeed it is a
     * statement expression, i.e., one with a side effect.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JStatement analyze(Context context) {
        if (expr.isStatementExpression) {
            expr = expr.analyze(context);
        }
        return this;
    }

    /**
     * Generating code for the statement expression involves simply generating
     * code for the encapsulated expression.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        expr.codegen(output);
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JStatementExpression line=\"%d\">\n", line());
        p.indentRight();
        expr.writeToStdOut(p);
        p.indentLeft();
        p.printf("</JStatementExpression>\n");
    }

}
