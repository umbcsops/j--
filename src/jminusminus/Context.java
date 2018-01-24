// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * A Context encapsulates the environment in which an AST is analyzed. It
 * represents a scope; the scope of a variable is captured by its context. It's
 * the symbol table.
 * 
 * Because scopes are lexically nested in Java (and so in j--), the environment
 * can be seen as a stack of contexts, each of which is a mapping from names to
 * their definitions (IDefns). A Context keeps track of it's (most closely)
 * surrounding context, its surrounding class context, and its surrounding
 * compilation unit context, as well as a map of from names to definitions in
 * the level of scope the Context represents. Contexts are created for the
 * compilation unit (a CompilationUnitContext), a class (a ClassContext), each
 * method (a MethodContext), and each block (a LocalContext). If we were to add
 * the for-statement to j--, we would necessarily create a (local) context.
 * 
 * From the outside, the structure looks like a tree strung over the AST. But
 * from any location on the AST, that is from any point along a particular
 * branch, it looks like a stack of context objects leading back to the root of
 * the AST, that is, back to the JCompilationUnit object at the root.
 * 
 * Part of this structure is built during pre-analysis; pre-analysis reaches
 * only into the type (eg class) declaration for typing the members;
 * pre-analysis does not reach into the method bodies. The rest of it is built
 * during analysis.
 */

class Context {

    /** The surrounding context (scope). */
    protected Context surroundingContext;

    /** The surrounding class context. */
    protected ClassContext classContext;

    /**
     * The compilation unit context (for the whole source program or file).
     */
    protected CompilationUnitContext compilationUnitContext;

    /**
     * Map of (local variable, formal parameters, type) names to their
     * definitions.
     */
    protected Map<String, IDefn> entries;

    /**
     * Construct a Context.
     * 
     * @param surrounding
     *            the surrounding context (scope).
     * @param classContext
     *            the surrounding class context.
     * @param compilationUnitContext
     *            the compilation unit context (for the whole source program or
     *            file).
     */

    protected Context(Context surrounding, ClassContext classContext,
            CompilationUnitContext compilationUnitContext) {
        this.surroundingContext = surrounding;
        this.classContext = classContext;
        this.compilationUnitContext = compilationUnitContext;
        this.entries = new HashMap<String, IDefn>();
    }

    /**
     * Add an entry to the symbol table, binding a name to its definition in the
     * current context.
     * 
     * @param name
     *            the name being declared.
     * @param definition
     *            and its definition.
     */

    public void addEntry(int line, String name, IDefn definition) {
        if (entries.containsKey(name)) {
            JAST.compilationUnit.reportSemanticError(line, "redefining name: "
                    + name);
        } else {
            entries.put(name, definition);
        }
    }

    /**
     * Return the definition for a name in the environment. If it's not found in
     * this context, we look for it in the surrounding context(s).
     * 
     * @param name
     *            the name whose definition we're looking for.
     * @return the definition (or null, if not found).
     */

    public IDefn lookup(String name) {
        IDefn iDefn = (IDefn) entries.get(name);
        return iDefn != null ? iDefn
                : surroundingContext != null ? surroundingContext.lookup(name)
                        : null;
    }

    /**
     * Return the definition for a type name in the environment. For now, we
     * look for types only in the CompilationUnitContext.
     * 
     * @param name
     *            the name of the type whose definition we're looking for.
     * @return the definition (or null, if not found).
     */

    public Type lookupType(String name) {
        TypeNameDefn defn = (TypeNameDefn) compilationUnitContext.lookup(name);
        return defn == null ? null : defn.type();
    }

    /**
     * Add the type to the environment.
     * 
     * @param line
     *            line number of type declaration.
     * @param type
     *            the type we are declaring.
     */

    public void addType(int line, Type type) {
        IDefn iDefn = new TypeNameDefn(type);
        compilationUnitContext.addEntry(line, type.simpleName(), iDefn);
        if (!type.toString().equals(type.simpleName())) {
            compilationUnitContext.addEntry(line, type.toString(), iDefn);
        }
    }

    /**
     * The type that defines this context (used principally for checking
     * acessibility).
     * 
     * @return the type that defines this context.
     */

    public Type definingType() {
        return ((JTypeDecl) classContext.definition()).thisType();
    }

    /**
     * Return the surrounding context (scope) in the stack of contexts.
     * 
     * @return the surrounding context.
     */

    public Context surroundingContext() {
        return surroundingContext;
    }

    /**
     * Return the surrounding class context.
     * 
     * @return the surrounding class context.
     */

    public ClassContext classContext() {
        return classContext;
    }

    /**
     * Return the surrounding compilation unit context. This is where imported
     * types and other types defined in the compilation unit are declared.
     * 
     * @return the compilation unit context.
     */

    public CompilationUnitContext compilationUnitContext() {
        return compilationUnitContext;
    }

    /**
     * Return the closest surrounding method context. Return null if we're not
     * within a method.
     * 
     * @return the method context.
     */

    public MethodContext methodContext() {
        Context context = this;
        while (context != null && !(context instanceof MethodContext)) {
            context = context.surroundingContext();
        }
        return (MethodContext) context;
    }

    /**
     * The names declared in this context.
     * 
     * @return the set of declared names.
     */

    public Set<String> names() {
        return entries.keySet();
    }

    /**
     * Write the contents of this context to STDOUT.
     * 
     * @param p
     *            for pretty printing with indentation.
     */

    public void writeToStdOut(PrettyPrinter p) {
        // Nothing to write here
    }

}

/**
 * The compilation unit context is always the outermost context, and is where
 * imported types and locally defined types (classes) are declared.
 */

class CompilationUnitContext extends Context {

    /**
     * Construct a new compilation unit context. There are no surrounding
     * contexts.
     */

    public CompilationUnitContext() {
        super(null, null, null);
        compilationUnitContext = this;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.println("<CompilationUnitContext>");
        p.indentRight();
        p.println("<Entries>");
        if (entries != null) {
            p.indentRight();
            for (String key : names()) {
                p.println("<Entry>" + key + "</Entry>");
            }
            p.indentLeft();
        }
        p.println("</Entries>");
        p.indentLeft();
        p.println("</CompilationUnitContext>");
    }

}

/**
 * Represents the context (scope, environment, symbol table) for a type, eg a
 * class, in j--. It also keeps track of its surrounding context(s), and the
 * type whose context it represents.
 */

class ClassContext extends Context {

    /** AST node of the type that this class represents. */
    private JAST definition;

    /**
     * Construct a class context.
     * 
     * @param definition
     *            the AST node of the type that this class represents.
     * @param surrounding
     *            the surrounding context(s).
     */

    public ClassContext(JAST definition, Context surrounding) {
        super(surrounding, null, surrounding.compilationUnitContext());
        classContext = this;
        this.definition = definition;
    }

    /**
     * Return the AST node of the type defined by this class.
     * 
     * @return the AST of the type defined by this class.
     */

    public JAST definition() {
        return definition;
    }

}

/**
 * A local context is a context (scope) in which local variables (including
 * formal parameters) can be declared. Local variables are allocated at fixed
 * offsets from the base of the current method's stack frame; this is done
 * during anaysis. The definitions for local variables record these offsets. The
 * offsets are used in code generation.
 */

class LocalContext extends Context {

    /** Next offset for a local variable. */
    protected int offset;

    /**
     * Construct a local context. A local context is constructed for each block.
     * 
     * @param surrounding
     *            the surrounding context.
     */

    public LocalContext(Context surrounding) {
        super(surrounding, surrounding.classContext(), surrounding
                .compilationUnitContext());
        offset = (surrounding instanceof LocalContext) ? ((LocalContext) surrounding)
                .offset()
                : 0;
    }

    /**
     * The "next" offset. A simple getter. Not to be used for allocating new
     * offsets (nextOffset() is used for that).
     * 
     * @return the next available offset.
     */

    public int offset() {
        return offset;
    }

    /**
     * Allocate a new offset (eg for a parameter or local variable).
     * 
     * @return the next allocated offset.
     */

    public int nextOffset() {
        return offset++;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.println("<LocalContext>");
        p.indentRight();
        p.println("<Entries>");
        if (entries != null) {
            p.indentRight();
            for (String key : names()) {
                IDefn defn = entries.get(key);
                if (defn instanceof LocalVariableDefn) {
                    p.printf("<Entry name=\"%s\" " + "offset=\"%d\"/>\n", key,
                            ((LocalVariableDefn) defn).offset());
                }
            }
            p.indentLeft();
        }
        p.println("</Entries>");
        p.indentLeft();
        p.println("</LocalContext>");
    }

}

/**
 * A method context is where formal parameters are declared. Also, it's where we
 * start computing the offsets for local variables (formal parameters included),
 * which are allocated in the current stack frame (for a method invocation).
 */

class MethodContext extends LocalContext {

    /** Is this method static? */
    private boolean isStatic;

    /** Return type of this method. */
    private Type methodReturnType;

    /** Does (non-void) method have at least one return? */
    private boolean hasReturnStatement = false;

    /**
     * Construct a method context.
     * 
     * @param surrounding
     *            the surrounding (class) context.
     * @param isStatic
     *            is this method static?
     * @param methodReturnType
     *            return type of this method.
     */

    public MethodContext(Context surrounding, boolean isStatic,
            Type methodReturnType) {
        super(surrounding);
        this.isStatic = isStatic;
        this.methodReturnType = methodReturnType;
        offset = 0;
    }

    /**
     * Is this method static?
     * 
     * @return true or false.
     */

    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Record fact that (non-void) method has at least one return.
     */

    public void confirmMethodHasReturn() {
        hasReturnStatement = true;
    }

    /**
     * Does this (non-void) method have at least one return?
     * 
     * @return true or false.
     */

    public boolean methodHasReturn() {
        return hasReturnStatement;
    }

    /**
     * Return the return type of this method.
     * 
     * @return return type of this method.
     */

    public Type methodReturnType() {
        return methodReturnType;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.println("<MethodContext>");
        p.indentRight();
        super.writeToStdOut(p);
        p.indentLeft();
        p.println("</MethodContext>");
    }

}
