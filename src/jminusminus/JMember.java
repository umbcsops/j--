// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * An interface supported by all class (or later, interface) members.
 */

interface JMember {

    /**
     * Declare the member name(s) in the specified (class) context. Generate the
     * member header(s) in the (partial) class. All members must support this
     * method.
     * 
     * @param context
     *            class context in which names are resolved.
     * @param partial
     *            the code emitter (basically an abstraction for producing the
     *            partial class).
     */

    public void preAnalyze(Context context, CLEmitter partial);

}
