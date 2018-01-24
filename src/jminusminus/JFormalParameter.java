// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * The AST node for a formal parameter declaration. All analysis and code
 * generation is done in a parent AST.
 */

class JFormalParameter extends JAST {

    /** Parameter name. */
    private String name;

    /** Parameter type. */
    private Type type;

    /**
     * Construct an AST node for a formal parameter declaration given its line
     * number, name, and type.
     * 
     * @param line
     *            line in which the parameter occurs in the source file.
     * @param name
     *            parameter name.
     * @param type
     *            parameter type.
     */

    public JFormalParameter(int line, String name, Type type) {
        super(line);
        this.name = name;
        this.type = type;
    }

    /**
     * Return the parameter's name.
     * 
     * @return the parameter's name.
     */

    public String name() {
        return name;
    }

    /**
     * Return the parameter's type.
     * 
     * @return the parameter's type.
     */

    public Type type() {
        return type;
    }

    /**
     * Set the type to the specified type.
     * 
     * @param newType
     *            the new type.
     * @return return the new type.
     */

    public Type setType(Type newType) {
        return type = newType;
    }

    /**
     * No analysis done here.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JAST analyze(Context context) {
        // Nothing to do
        return this;
    }

    /**
     * No code generated here.
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
        p.printf("<JFormalParameter line=\"%d\" name=\"%s\" "
                + "type=\"%s\"/>\n", line(), name, (type == null) ? "" : type
                .toString());
    }

}
