// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * The (dummy) AST node for representing the empty statement. Nothing needs to
 * be done during analysis or code generation. It simply represents empty
 * statements that are denoted by the ";" in j--.
 */

class JEmptyStatement extends JStatement {

    /**
     * Construct an AST node for an empty statement.
     * 
     * @param line
     *            line in which the empty statement occurs in the source file.
     */

    protected JEmptyStatement(int line) {
        super(line);
    }

    /**
     * @inheritDoc
     */

    public JAST analyze(Context context) {
        // Nothing to do.
        return this;
    }

    /**
     * @inheritDoc
     */

    public void codegen(CLEmitter output) {
        // Nothing to do.
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JEmptyStatement line=\"%d\"/>\n", line());
    }

}
