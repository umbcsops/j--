// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * The AST node for a "wild" expression. A wild expression is a placeholder
 * expression, used when there is a syntax error.
 */

class JWildExpression extends JExpression {

    /**
     * Construct an AST node for a "wild" expression given its line number.
     * 
     * @param line
     *            line in which the "wild" expression occurs occurs in the
     *            source file.
     */

    public JWildExpression(int line) {
        super(line);
    }

    /**
     * Simply set the type to ANY (a wild type matching everything).
     * 
     * @param context
     *            context in which names are resolved (ignored here).
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        type = Type.ANY;
        return this;
    }

    /**
     * No code generation.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        // Nothing to do
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JWildExpression line=\"%d\"/>\n", line());
    }

}
