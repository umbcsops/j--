// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * The AST node for a string literal.
 */

class JLiteralString extends JExpression {

    /** Representation of the string. */
    private String text;

    /**
     * Construct an AST node for a string literal given its line number and
     * string representation.
     * 
     * @param line
     *            line in which the literal occurs in the source file.
     * @param text
     *            representation of the literal.
     */

    public JLiteralString(int line, String text) {
        super(line);
        this.text = text;
    }

    /**
     * Analyzing a String literal is trivial.
     * 
     * @param context
     *            context in which names are resolved (ignored here).
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        type = Type.STRING;
        return this;
    }

    /**
     * Generating code for a string literal means generating code to push it
     * onto the stack.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        // Unescape the escaped escapes
        String s = Util.unescape(text);

        // The string representation is padded (by hand-written
        // and JavaCC scanner) with double quotes, so we substring
        String literal = s.substring(1, s.length() - 1);
        output.addLDCInstruction(literal);
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JLiteralString line=\"%d\" type=\"%s\" "
                + "value=\"%s\"/>\n", line(), ((type == null) ? "" : type
                .toString()), Util.escapeSpecialXMLChars(text));
    }

}
