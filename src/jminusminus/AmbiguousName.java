// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.StringTokenizer;

/**
 * This class is used to encapsulate ambiguous names that the parser can't
 * distinguish and disambiguates them during the analysis phase. Ambiguous 
 * names are meant to deal with snippets like:
 *
 * <pre>
 *   x.y.z
 *   a.b.c()
 * </pre>
 *
 * Clearly, z is a field and c is a method. But what about <em>x.y</em> and 
 * <em>a.b</em>? x could be a package name and y a type, making for a (static) 
 * class field selection. Or, x could be a local variable and y an instance 
 * field. The parser cannot know how to parse these.
 * <p>
 * Disambiguating the ambiguity must wait until analysis time. The parser can,
 * with <em>x.y.z</em>, treat the <em>.z</em> as a field selection, but 
 * constructs an AmbiguousName object encapsulating the <em>x.y</em>. And it 
 * can, with <em>a.b.c()</em>, treat <em>.c()</em> as a message expression, 
 * but constructs an AbiguousName object encapsulating <em>a.b</em>.
 * <p>
 * The {@code reclassify} method is called upon in 
 * {@link JFieldSelection#analyze(Context)} and 
 * {@link JMessageExpression#analyze(Context)} to reclassify the components and
 * construct the proper AST, following the rules for names in the Java Language
 * Specification (Third Edition), section 6.5.2. In j--, both <em>x.y</em> and 
 * <em>a.b</em> are clearly expressions in these contexts. If inner types were 
 * to be introduced, their meaning and their reclassification would necessarily
 * be more complicated.
 */

class AmbiguousName {

    /**
     * Line in which the ambiguous name occurs in the source file.
     */
    private int line;

    /** The ambiguous part, for example x.y */
    private String name;

    /**
     * Constructs an encapsulation of the ambiguous portion of a snippet like
     * <em>x.y.z</em>.
     * 
     * @param line
     *            line in which the ambiguous name occurs in the source file.
     * @param name
     *            the ambiguous part.
     */

    public AmbiguousName(int line, String name) {
        this.line = line;
        this.name = name;
    }

    /**
     * Reclassifies the name according to the rules in the Java Language
     * Specification.
     * 
     * @param context
     *            context in which we look up the component names.
     * @return the properly parsed AST.
     */

    public JExpression reclassify(Context context) {
        // Easier because we require all types to be imported.
        JExpression result = null;
        StringTokenizer st = new StringTokenizer(name, ".");

        // Firstly, find a variable or Type.
        String newName = st.nextToken();
        IDefn iDefn = null;

        do {
            iDefn = context.lookup(newName);
            if (iDefn != null) {
                result = new JVariable(line, newName);
                break;
            } else if (!st.hasMoreTokens()) {
                // Nothing found. :(
                JAST.compilationUnit.reportSemanticError(line,
                        "Cannot find name " + newName);
                return null;
            } else {
                newName += "." + st.nextToken();
            }
        } while (true);

        // For now we can assume everything else are fields.
        while (st.hasMoreTokens()) {
            result = new JFieldSelection(line, result, st.nextToken());
        }
        return result;
    }

}
