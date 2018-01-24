// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

/**
 * The abstract syntax tree (AST) node representing a compilation unit, and so
 * the root of the AST.
 * 
 * It keeps track of the name of the source file, its package name, a list of
 * imported types, a list of type (eg class) declarations, and a flag indicating
 * if a semantic error has been detected in analysis or code generation. It also
 * maintains a CompilationUnitContext (built in pre-analysis) for declaring both
 * imported and declared types.
 * 
 * The AST is produced by the Parser. Once the AST has been built, three
 * successive methods are invoked:
 * 
 * (1) Method preAnalyze() is invoked for making a first pass at type analysis,
 * recursively reaching down to the member headers for declaring types and
 * member interfaces in the environment (contexts). preAnalyze() also creates a
 * partial class file (in memory) for recording member header information, using
 * the partialCodegen() method.
 * 
 * (2) Method analyze() is invoked for type-checking field initializations and
 * method bodies, and determining the types of all expressions. A certain amount
 * of tree surgery is also done here. And stack frame offsets are computed for
 * method parameters and local variables.
 * 
 * (3) Method codegen() is invoked for generating code for the compilation unit
 * to a class file. For each type declaration, it instantiates a CLEmmiter
 * object (an abstraction of the class file) and then invokes methods on that
 * CLEmitter for generating instructions. At the end of each type declaration, a
 * method is invoked on the CLEmitter which writes the class out to the file
 * system either as .class file or as a .s (SPIM) file. Of course, codegen()
 * makes recursive calls down the tree, to the codegen() methods at each node,
 * for generating the appropriate instructions.
 */

class JCompilationUnit extends JAST {

    /** Name of the source file. */
    private String fileName;

    /** Package name. */
    private TypeName packageName;

    /** List of imports. */
    private ArrayList<TypeName> imports;

    /** List of type declarations. */
    private ArrayList<JAST> typeDeclarations;

    /**
     * List of CLFile objects corresponding to the type declarations in this
     * compilation unit.
     */
    private ArrayList<CLFile> clFiles;

    /** For imports and type declarations. */
    private CompilationUnitContext context;

    /** Whether a semantic error has been found. */
    private boolean isInError;

    /**
     * Construct an AST node for a compilation unit given a file name, class
     * directory, line number, package name, list of imports, and type
     * declarations.
     * 
     * @param fileName
     *            the name of the source file.
     * @param line
     *            line in which the compilation unit occurs in the source file.
     * @param packageName
     *            package name.
     * @param imports
     *            a list of imports.
     * @param typeDeclarations
     *            type declarations.
     */

    public JCompilationUnit(String fileName, int line, TypeName packageName,
            ArrayList<TypeName> imports, ArrayList<JAST> typeDeclarations) {
        super(line);
        this.fileName = fileName;
        this.packageName = packageName;
        this.imports = imports;
        this.typeDeclarations = typeDeclarations;
        clFiles = new ArrayList<CLFile>();
        compilationUnit = this;
    }

    /**
     * The package in which this compilation unit is defined.
     * 
     * @return the package name.
     */

    public String packageName() {
        return packageName == null ? "" : packageName.toString();
    }

    /**
     * Has a semantic error occurred up to now?
     * 
     * @return true or false.
     */

    public boolean errorHasOccurred() {
        return isInError;
    }

    /**
     * Report a semantic error.
     * 
     * @param line
     *            line in which the error occurred in the source file.
     * @param message
     *            message identifying the error.
     * @param arguments
     *            related values.
     */

    public void reportSemanticError(int line, String message,
            Object... arguments) {
        isInError = true;
        System.err.printf("%s:%d: ", fileName, line);
        System.err.printf(message, arguments);
        System.err.println();
    }

    /**
     * Construct a context for the compilation unit, initializing it with
     * imported types. Then pre-analyze the unit's type declarations, adding
     * their types to the context.
     */

    public void preAnalyze() {
        context = new CompilationUnitContext();

        // Declare the two implicit types java.lang.Object and
        // java.lang.String
        context.addType(0, Type.OBJECT);
        context.addType(0, Type.STRING);

        // Declare any imported types
        for (TypeName imported : imports) {
            try {
                Class<?> classRep = Class.forName(imported.toString());
                context.addType(imported.line(), Type.typeFor(classRep));
            } catch (Exception e) {
                JAST.compilationUnit.reportSemanticError(imported.line(),
                        "Unable to find %s", imported.toString());
            }
        }

        // Declare the locally declared type(s)
        CLEmitter.initializeByteClassLoader();
        for (JAST typeDeclaration : typeDeclarations) {
            ((JTypeDecl) typeDeclaration).declareThisType(context);
        }

        // Pre-analyze the locally declared type(s). Generate
        // (partial) Class instances, reflecting only the member
        // interface type information
        CLEmitter.initializeByteClassLoader();
        for (JAST typeDeclaration : typeDeclarations) {
            ((JTypeDecl) typeDeclaration).preAnalyze(context);
        }
    }

    /**
     * Perform semantic analysis on the AST in the specified context.
     * 
     * @param context
     *            context in which names are resolved (ignored here).
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JAST analyze(Context context) {
        for (JAST typeDeclaration : typeDeclarations) {
            typeDeclaration.analyze(this.context);
        }
        return this;
    }

    /**
     * Generating code for a compilation unit means generating code for each of
     * the type declarations.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        for (JAST typeDeclaration : typeDeclarations) {
            typeDeclaration.codegen(output);
            output.write();
            clFiles.add(output.clFile());
        }
    }

    /**
     * Return the list of CLFile objects corresponding to the type declarations
     * in this compilation unit.
     * 
     * @return list of CLFile objects.
     */

    public ArrayList<CLFile> clFiles() {
        return clFiles;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        p.printf("<JCompilationUnit line=\"%d\">\n", line());
        p.indentRight();
        p.printf("<Source fileName=\"%s\"/>\n", fileName);
        if (context != null) {
            context.writeToStdOut(p);
        }
        if (packageName != null) {
            p.printf("<Package name=\"%s\"/>\n", packageName());
        }
        if (imports != null) {
            p.println("<Imports>");
            p.indentRight();
            for (TypeName imported : imports) {
                p.printf("<Import name=\"%s\"/>\n", imported.toString());
            }
            p.indentLeft();
            p.println("</Imports>");
        }
        if (typeDeclarations != null) {
            p.println("<TypeDeclarations>");
            p.indentRight();
            for (JAST typeDeclaration : typeDeclarations) {
                typeDeclaration.writeToStdOut(p);
            }
            p.indentLeft();
            p.println("</TypeDeclarations>");
        }
        p.indentLeft();
        p.println("</JCompilationUnit>");
    }

}
