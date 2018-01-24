// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * The AST node for a statement (includes expressions). The mother of all
 * statements.
 */

abstract class JStatement extends JAST {

    /**
     * Construct an AST node for a statement given its line number.
     * 
     * @param line
     *            line in which the statement occurs in the source file.
     */

    protected JStatement(int line) {
        super(line);
    }

}
