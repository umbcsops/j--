// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

/**
 * The AST node for a field declaration.
 */

class JFieldDeclaration extends JAST implements JMember {

    /** Field modifiers. */
    private ArrayList<String> mods;

    /** Variable declarators. */
    private ArrayList<JVariableDeclarator> decls;

    /** Variable initializations. */
    private ArrayList<JStatement> initializations;

    /**
     * Construct an AST node for a field declaration given the line number,
     * modifiers, and the variable declarators.
     * 
     * @param line
     *            line in which the variable declaration occurs in the source
     *            file.
     * @param mods
     *            field modifiers.
     * @param decls
     *            variable declarators.
     */

    public JFieldDeclaration(int line, ArrayList<String> mods,
            ArrayList<JVariableDeclarator> decls) {
        super(line);
        this.mods = mods;
        this.decls = decls;
        initializations = new ArrayList<JStatement>();
    }

    /**
     * Return the list of modifiers.
     * 
     * @return list of modifiers.
     */

    public ArrayList<String> mods() {
        return mods;
    }

    /**
     * Declare fields in the parent's (partial) class.
     * 
     * @param context
     *            the parent (class) context.
     * @param partial
     *            the code emitter (basically an abstraction for producing the
     *            partial class).
     */

    public void preAnalyze(Context context, CLEmitter partial) {
        // Fields may not be declared abstract.
        if (mods.contains("abstract")) {
            JAST.compilationUnit.reportSemanticError(line(),
                    "Field cannot be declared abstract");
        }

        for (JVariableDeclarator decl : decls) {
            // Add field to (partial) class
            decl.setType(decl.type().resolve(context));
            partial.addField(mods, decl.name(), decl.type().toDescriptor(),
                    false);
        }
    }

    /**
     * Analysis of field declaration involves rewriting initializations (if any)
     * as assignment statements.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed JFieldDeclaration subtree.
     */

    public JFieldDeclaration analyze(Context context) {
        for (JVariableDeclarator decl : decls) {
            // All initializations must be turned into assignment
            // statements and analyzed
            if (decl.initializer() != null) {
                JAssignOp assignOp = new JAssignOp(decl.line(), new JVariable(
                        decl.line(), decl.name()), decl.initializer());
                assignOp.isStatementExpression = true;
                initializations.add(new JStatementExpression(decl.line(),
                        assignOp).analyze(context));
            }
        }
        return this;
    }

    /**
     * Generate code for any field initializations (now rewritten as assignment
     * statements).
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegenInitializations(CLEmitter output) {
        for (JStatement initialization : initializations) {
            initialization.codegen(output);
        }
    }

    /**
     * Code generation for field declaration involves generate field the header.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        for (JVariableDeclarator decl : decls) {
            // Add field to class
            output.addField(mods, decl.name(), decl.type().toDescriptor(),
                    false);
        }
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JFieldDeclaration line=\"%d\"/>\n", line());
        p.indentRight();
        if (mods != null) {
            p.println("<Modifiers>");
            p.indentRight();
            for (String mod : mods) {
                p.printf("<Modifier name=\"%s\"/>\n", mod);
            }
            p.indentLeft();
            p.println("</Modifiers>");
        }
        if (decls != null) {
            p.println("<VariableDeclarators>");
            for (JVariableDeclarator decl : decls) {
                p.indentRight();
                decl.writeToStdOut(p);
                p.indentLeft();
            }
            p.println("<VariableDeclarators>");
        }
        p.indentLeft();
        p.println("</JFieldDeclaration>");
    }

}
