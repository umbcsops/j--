// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * An interface supported by class (or later, interface) declarations.
 */

interface JTypeDecl {

    /**
     * Even before {@code preAnalyze}, declare this type in the parent context 
     * so that it is available in the {@code preAnalyze} method of other types.
     * 
     * @param context
     *            the compilation unit context in which we're declaring types.
     */

    public void declareThisType(Context context);

    /**
     * Pre-analyzes the members of this declaration in the parent context.
     * Pre-analysis extends to the member headers (including method headers) but
     * not into the bodies (if any).
     * 
     * @param context
     *            the parent (compilation unit) context.
     */

    public void preAnalyze(Context context);

    /**
     * Returns the name of this type declaration.
     * 
     * @return the name of this type declaration.
     */

    public String name();

    /**
     * Returns the super class' type.
     * 
     * @return the super class' type.
     */

    public Type superType();

    /**
     * Returns the type that this type declaration defines.
     * 
     * @return the type defined by this type declaration.
     */

    public Type thisType();

}
