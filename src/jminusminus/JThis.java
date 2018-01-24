// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST for a "this" expression. It serves as a target of some field
 * selection or message.
 */

class JThis extends JExpression {

    /**
     * Construct an AST node for a "this" expression given its line number.
     * 
     * @param line
     *            line in which the expression occurs in the source file.
     */

    public JThis(int line) {
        super(line);
    }

    /**
     * Analysis involves simply determining the type in which we are, since that
     * determines the type of this target.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        type = ((JClassDeclaration) context.classContext.definition())
                .thisType();
        return this;
    }

    /**
     * Simply generate code to load "this" onto the stack.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        output.addNoArgInstruction(ALOAD_0);
    }

    /**
     * inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.println("<JThis/>");
    }

}
