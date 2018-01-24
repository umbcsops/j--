// Copyright 2011 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST for an array indexing operation. It has an expression
 * denoting an array object and an expression denoting an integer
 * index.
 */

class JArrayExpression
    extends JExpression implements JLhs {

    /** The array. */
    private JExpression theArray;

    /** The array index expression. */
    private JExpression indexExpr;

    /**
     * Construct an AST node for an array indexing operation.
     * 
     * @param line
     *                line in which the operation occurs in the
     *                source file.
     * @param theArray
     *                the array we're indexing.
     * @param indexExpr
     *                the index expression.
     */

    public JArrayExpression(int line, JExpression theArray,
        JExpression indexExpr) {
        super(line);
        this.theArray = theArray;
        this.indexExpr = indexExpr;
    }

    /**
     * Perform semantic analysis on an array indexing expression
     * such as A[i].
     * 
     * @param context
     *                context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        theArray = (JExpression) theArray.analyze(context);
        indexExpr = (JExpression) indexExpr.analyze(context);
        if (!(theArray.type().isArray())) {
            JAST.compilationUnit.reportSemanticError(line(),
                "attempt to index a non-array object");
            this.type = Type.ANY;
        } else {
            this.type = theArray.type().componentType();
        }
        indexExpr.type().mustMatchExpected(line(), Type.INT);
        return this;
    }

    /**
     * Analyzing the array expression as an Lvalue is like
     * analyzing it for its Rvalue.
     * 
     * @param context
     *                context in which names are resolved.
     */

    public JExpression analyzeLhs(Context context) {
        analyze(context);
        return this;
    }

    /**
     * Perform code generation from the JArrayExpression using
     * the specified code emitter. Generate the code necessary
     * for loading the Rvalue.
     * 
     * @param output
     *                the code emitter (basically an abstraction
     *                for producing the .class file).
     */

    public void codegen(CLEmitter output) {
        theArray.codegen(output);
        indexExpr.codegen(output);
        if (type == Type.INT) {
            output.addNoArgInstruction(IALOAD);
	} else if (type == Type.BOOLEAN) {
            output.addNoArgInstruction(BALOAD);
	} else if (type == Type.CHAR) {
            output.addNoArgInstruction(CALOAD);
        } else if (!type.isPrimitive()) {
            output.addNoArgInstruction(AALOAD);
        }
    }

    /**
     * Generate the code required for setting up an Lvalue, eg
     * for use in an assignment. Here, this requires loading the
     * array and the index.
     * 
     * @param output
     *                the code emitter (basically an abstraction
     *                for producing the .class file).
     */

    public void codegenLoadLhsLvalue(CLEmitter output) {
        // Load the lvalue onto the stack: the array and the
        // index.
        theArray.codegen(output);
        indexExpr.codegen(output);
    }

    /**
     * Generate the code required for loading an Rvalue for this
     * variable, eg for use in a +=. Here, this requires
     * duplicating the array and the index on the stack and doing
     * an array load.
     * 
     * @param output
     *                the code emitter (basically an abstraction
     *                for producing the .class file).
     */

    public void codegenLoadLhsRvalue(CLEmitter output) {
        // Load rvalue onto stack, by duplicating the lvalue,
        // and fetching it's content
        if (type == Type.STRING) {
            output.addNoArgInstruction(DUP2_X1);
        } else {
            output.addNoArgInstruction(DUP2);
        }
	if (type == Type.INT) {
	    output.addNoArgInstruction(IALOAD);
	} else if (type == Type.BOOLEAN) {
	    output.addNoArgInstruction(BALOAD);
	} else if (type == Type.CHAR) {
	    output.addNoArgInstruction(CALOAD);
        } else if (!type.isPrimitive()) {
            output.addNoArgInstruction(AALOAD);
        }
    }

    /**
     * Generate the code required for duplicating the Rvalue that
     * is on the stack becuase it is to be used in a surrounding
     * expression, as in a[i] = x = <expr> or x = y--. Here this
     * means copying it down two locations (beneath the array and
     * index).
     * 
     * @param output
     *                the code emitter (basically an abstraction
     *                for producing the .class file).
     */

    public void codegenDuplicateRvalue(CLEmitter output) {
        // It's copied down below the array and index
        output.addNoArgInstruction(DUP_X2);
    }

    /**
     * Generate the code required for doing the actual
     * assignment. Here, this requires an array store.
     * 
     * @param output
     *                the code emitter (basically an abstraction
     *                for producing the .class file).
     */

    public void codegenStore(CLEmitter output) {
	if (type == Type.INT) {
	    output.addNoArgInstruction(IASTORE);
	} else if (type == Type.BOOLEAN) {
	    output.addNoArgInstruction(BASTORE);
	} else if (type == Type.CHAR) {
	    output.addNoArgInstruction(CASTORE);
        } else if (!type.isPrimitive()) {
            output.addNoArgInstruction(AASTORE);
        }

    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.println("<JArrayExpression>");
        p.indentRight();
        if (theArray != null) {
            p.println("<TheArray>");
            p.indentRight();
            theArray.writeToStdOut(p);
            p.indentLeft();
            p.println("</TheArray>");
        }
        if (indexExpr != null) {
            p.println("<IndexExpression>");
            p.indentRight();
            indexExpr.writeToStdOut(p);
            p.indentLeft();
            p.println("</IndexExpression>");
        }
        p.indentLeft();
        p.println("</JArrayExpression>");
    }
}
