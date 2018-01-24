// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for the null literal.
 */

class JLiteralNull extends JExpression {

    /**
     * Construct an AST node for the null literal given its line number.
     * 
     * @param line
     *            line in which the literal occurs in the source file.
     */

    public JLiteralNull(int line) {
        super(line);
    }

    /**
     * Analyzing the null literal is trivial.
     * 
     * @param context
     *            context in which names are resolved (ignored here).
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        type = Type.NULLTYPE;
        return this;
    }

    /**
     * Generating code for a null literal means generating code to push it onto
     * the stack.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        output.addNoArgInstruction(ACONST_NULL);
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JLiteralNull line=\"%d\" type=\"%s\"/>\n", line(),
                ((type == null) ? "" : type.toString()));
    }

}
