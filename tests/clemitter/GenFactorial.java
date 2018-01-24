// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

import jminusminus.CLEmitter;
import static jminusminus.CLConstants.*;
import java.util.ArrayList;

/**
 * This class programatically generates the class file for the 
 * following Java application using CLEmitter:
 *
 * This application computes the factorial of a number recursively.
 *
 * public class Factorial
 * {
 *     public int compute( int n )
 *     {
 *         if ( n <= 1 ) {
 *             return n;
 *         }
 *         else {
 *             return ( n * compute( n - 1 ) );
 *         }
 *     }
 * 
 *     public static void main( String[] args )
 *     {
 *         try {
 *	       Factorial f = new Factorial();
 *	       int n = Int.parseInt( args[ 0 ] );
 *	       System.out.println( "Factorial( " + n + " ) = " + 
 *                                 f.compute( n ) );
 *	   }
 *	   catch ( NumberFormatException nfe ) {
 *	       System.err.println( "Invalid number " + args[ 0 ] );
 *	   }
 *     }
 * }
 */

public class GenFactorial {

    public static void main(String[] args) {
        CLEmitter e = new CLEmitter(true);
        ArrayList<String> accessFlags = new ArrayList<String>();

        // Add Factorial class
        accessFlags.add("public");
        e.addClass(accessFlags, "Factorial", "java/lang/Object", null, true);

        // Add the implicit no-arg constructor Factorial() to
        // Factorial
        accessFlags.clear();
        accessFlags.add("public");
        e.addMethod(accessFlags, "<init>", "()V", null, true);
        e.addNoArgInstruction(ALOAD_0);
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/Object",
                "<init>", "()V");
        e.addNoArgInstruction(RETURN);

        // Add compute() method to Factorial
        accessFlags.clear();
        accessFlags.add("public");
        e.addMethod(accessFlags, "compute", "(I)I", null, true);
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(ICONST_1);
        e.addBranchInstruction(IF_ICMPGT, "falseLabel");
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(IRETURN);
        e.addLabel("falseLabel");
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(ICONST_1);
        e.addNoArgInstruction(ISUB);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "Factorial", "compute",
                "(I)I");
        e.addNoArgInstruction(IMUL);
        e.addNoArgInstruction(IRETURN);

        // Add main() method to Factorial
        accessFlags.clear();
        accessFlags.add("public");
        accessFlags.add("static");
        e.addMethod(accessFlags, "main", "([Ljava/lang/String;)V", null, true);
        e.addExceptionHandler("tryStart", "tryEnd", "catch",
                "java/lang/NumberFormatException");
        e.addLabel("tryStart");
        e.addReferenceInstruction(NEW, "Factorial");
        e.addNoArgInstruction(DUP);
        e.addMemberAccessInstruction(INVOKESPECIAL, "Factorial", "<init>",
                "()V");
        e.addNoArgInstruction(ASTORE_1);
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ICONST_0);
        e.addNoArgInstruction(AALOAD);
        e.addMemberAccessInstruction(INVOKESTATIC, "java/lang/Integer",
                "parseInt", "(Ljava/lang/String;)I");
        e.addNoArgInstruction(ISTORE_2);
        e.addMemberAccessInstruction(GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;");
        e.addReferenceInstruction(NEW, "java/lang/StringBuffer");
        e.addNoArgInstruction(DUP);
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/StringBuffer",
                "<init>", "()V");
        e.addLDCInstruction("Factorial(");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        e.addNoArgInstruction(ILOAD_2);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "append", "(I)Ljava/lang/StringBuffer;");
        e.addLDCInstruction(") = ");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        e.addNoArgInstruction(ALOAD_1);
        e.addNoArgInstruction(ILOAD_2);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "Factorial", "compute",
                "(I)I");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "append", "(I)Ljava/lang/StringBuffer;");
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

        // Write Factorial.class to file system
        e.write();
    }

}
