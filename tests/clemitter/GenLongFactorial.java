// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

import jminusminus.CLEmitter;
import static jminusminus.CLConstants.*;
import java.util.ArrayList;

/**
 * This class programatically generates the class file for the 
 * following Java application:
 * 
 * public class LongFactorial
 * {
 *     public long factorial( long n )
 *     {
 *         if ( n &lt;= 1 ) {
 *             return n;
 *         }
 *         else {
 *             return ( n * factorial( n - 1 ) );
 *         }
 *     }
 *     
 *     public static void main( String[] args )
 *     {
 *         try {
 *             LongFactorial f = new LongFactorial();
 *             long n = Long.parseLong( args[ 0 ] );
 *             System.out.println( "Factorial( " + n + " ) = " + 
 *                 f.factorial( n ) );
 *         }
 *         catch ( NumberFormatException e ) {
 *             System.err.println( "Invalid number " + n );
 *         }
 *     }
 * }
 */

public class GenLongFactorial {

    public static void main(String[] args) {
        CLEmitter e = new CLEmitter(true);
        ArrayList<String> accessFlags = new ArrayList<String>();

        // Add LongFactorial class
        accessFlags.add("public");
        e
                .addClass(accessFlags, "LongFactorial", "java/lang/Object",
                        null, true);

        // Add the implicit no-arg constructor LongFactorial()
        accessFlags.clear();
        accessFlags.add("public");
        e.addMethod(accessFlags, "<init>", "()V", null, true);
        e.addNoArgInstruction(ALOAD_0);
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/Object",
                "<init>", "()V");
        e.addNoArgInstruction(RETURN);

        // Add factorial() method to LongFactorial
        accessFlags.clear();
        accessFlags.add("public");
        e.addMethod(accessFlags, "factorial", "(J)J", null, true);
        e.addNoArgInstruction(LLOAD_1);
        e.addNoArgInstruction(LCONST_1);
        e.addNoArgInstruction(LCMP);
        e.addBranchInstruction(IFGT, "falseLabel");
        e.addNoArgInstruction(LLOAD_1);
        e.addNoArgInstruction(LRETURN);
        e.addLabel("falseLabel");
        e.addNoArgInstruction(LLOAD_1);
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(LLOAD_1);
        e.addNoArgInstruction(LCONST_1);
        e.addNoArgInstruction(LSUB);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "LongFactorial",
                "factorial", "(J)J");
        e.addNoArgInstruction(LMUL);
        e.addNoArgInstruction(LRETURN);

        // Add main() method to LongFactorial
        accessFlags.clear();
        accessFlags.add("public");
        accessFlags.add("static");
        e.addMethod(accessFlags, "main", "([Ljava/lang/String;)V", null, true);
        e.addExceptionHandler("tryStart", "tryEnd", "catch",
                "java/lang/NumberFormatException");
        e.addLabel("tryStart");
        e.addReferenceInstruction(NEW, "LongFactorial");
        e.addNoArgInstruction(DUP);
        e.addMemberAccessInstruction(INVOKESPECIAL, "LongFactorial", "<init>",
                "()V");
        e.addNoArgInstruction(ASTORE_1);
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ICONST_0);
        e.addNoArgInstruction(AALOAD);
        e.addMemberAccessInstruction(INVOKESTATIC, "java/lang/Long",
                "parseLong", "(Ljava/lang/String;)J");
        e.addNoArgInstruction(LSTORE_2);
        e.addMemberAccessInstruction(GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;");
        e.addReferenceInstruction(NEW, "java/lang/StringBuffer");
        e.addNoArgInstruction(DUP);
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/StringBuffer",
                "<init>", "()V");
        e.addLDCInstruction("Factorial(");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        e.addNoArgInstruction(LLOAD_2);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "append", "(J)Ljava/lang/StringBuffer;");
        e.addLDCInstruction(") = ");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        e.addNoArgInstruction(ALOAD_1);
        e.addNoArgInstruction(LLOAD_2);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "LongFactorial",
                "factorial", "(J)J");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "append", "(J)Ljava/lang/StringBuffer;");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "toString", "()Ljava/lang/String;");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/io/PrintStream",
                "println", "(Ljava/lang/String;)V");
        e.addLabel("tryEnd");
        e.addBranchInstruction(GOTO, "done");
        e.addLabel("catch");
        e.addNoArgInstruction(ASTORE_1);
        e.addMemberAccessInstruction(GETSTATIC, "java/lang/System", "err",
                "Ljava/io/PrintStream;");
        e.addReferenceInstruction(NEW, "java/lang/StringBuffer");
        e.addNoArgInstruction(DUP);
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/StringBuffer",
                "<init>", "()V");
        e.addLDCInstruction("Invalid number ");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ICONST_0);
        e.addNoArgInstruction(AALOAD);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "toString", "()Ljava/lang/String;");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/io/PrintStream",
                "println", "(Ljava/lang/String;)V");
        e.addLabel("done");
        e.addNoArgInstruction(RETURN);

        // Write LongFactorial.class to file system
        e.write();
    }

}
