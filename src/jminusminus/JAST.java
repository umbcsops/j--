// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * The abstract superclass of all nodes in the abstract syntax tree (AST).
 */

abstract class JAST {

    /** Current compilation unit (set in JCompilationUnit()). */
    public static JCompilationUnit compilationUnit;

    /** Line in which the source for the AST was found. */
    protected int line;

    /**
     * Construct an AST node the given its line number in the source file.
     * 
     * @param line
     *            line in which the source for the AST was found.
     */

    protected JAST(int line) {
        this.line = line;
    }

    /**
     * Return the line in which the source for the AST was found.
     * 
     * @return the line number.
     */

    public int line() {
        return line;
    }

    /**
     * Perform semantic analysis on this AST. In some instances a new returned
     * AST reflects surgery.
     * 
     * @param context
     *            the environment (scope) in which code is analyzed.
     * @return a (rarely modified) AST.
     */

    public abstract JAST analyze(Context context);

    /**
     * Generate a partial class for this type, reflecting only the member
     * information required to do analysis.
     * 
     * @param context
     *            the parent (class) context.
     * @param partial
     *            the code emitter (basically an abstraction for producing the
     *            partial class).
     */

    public void partialCodegen(Context context, CLEmitter partial) {
        // A dummy -- redefined where necessary.
    }

    /**
     * Perform code generation for this AST.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public abstract void codegen(CLEmitter output);

    /**
     * Write the information pertaining to this AST to STDOUT.
     * 
     * @param p
     *            for pretty printing with indentation.
     */

    public abstract void writeToStdOut(PrettyPrinter p);

}
