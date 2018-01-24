// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a "super" expression. It serves as a target of some field
 * selection or message.
 */

class JSuper extends JExpression {

    /**
     * Construct an AST node for a "super" expression given its line number.
     * 
     * @param line
     *            line in which the expression occurs in the source file.
     */

    public JSuper(int line) {
        super(line);
    }

    /**
     * Analysis involves determining the super class to that in which we are in;
     * this becomes the type.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        type = ((JClassDeclaration) context.classContext.definition())
                .thisType();
        if (type.isReference() && type.superClass() != null) {
            type = type.superClass();
        } else {
            JAST.compilationUnit.reportSemanticError(line(),
                    "No super class for type " + type.toString());
        }
        return this;
    }

    /**
     * Load "this" onto the stack (even if we treat it as its super class.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        output.addNoArgInstruction(ALOAD_0);
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.println("<JSuper/>");
    }

}
