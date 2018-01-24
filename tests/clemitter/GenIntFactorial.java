// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

import jminusminus.CLEmitter;
import static jminusminus.CLConstants.*;
import java.util.ArrayList;

/**
 * This class programatically generates the class file for 
 * the following Java application:
 * 
 * public class IntFactorial
 * {
 *     public int factorial( int n )
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
 *         IntFactorial f = new IntFactorial();
 *         int n = Integer.parseInt( args[ 0 ] );
 *         System.out.println( "Factorial( " + n + 
 *             " ) = " + f.factorial( n ) );
 *     }
 * }
 */

public class GenIntFactorial {

    public static void main(String[] args) {
        CLEmitter e = new CLEmitter(true);
        ArrayList<String> accessFlags = new ArrayList<String>();

        // Add IntFactorial class
        accessFlags.add("public");
        e.addClass(accessFlags, "IntFactorial", "java/lang/Object", null, true);

        // Add the implicit no-arg constructor IntFactorial()
        accessFlags.clear();
        accessFlags.add("public");
        e.addMethod(accessFlags, "<init>", "()V", null, true);
        e.addNoArgInstruction(ALOAD_0);
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/Object",
                "<init>", "()V");
        e.addNoArgInstruction(RETURN);

        // Add factorial() method to IntFactorial
        accessFlags.clear();
        accessFlags.add("public");
        e.addMethod(accessFlags, "factorial", "(I)I", null, true);
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(ICONST_1);
        e.addBranchInstruction(IF_ICMPGT, "Label1");
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(IRETURN);
        e.addLabel("Label1");
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(ICONST_1);
        e.addNoArgInstruction(ISUB);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "IntFactorial",
                "factorial", "(I)I");
        e.addNoArgInstruction(IMUL);
        e.addNoArgInstruction(IRETURN);

        // Add main() method to IntFactorial
        accessFlags.clear();
        accessFlags.add("public");
        accessFlags.add("static");
        e.addMethod(accessFlags, "main", "([Ljava/lang/String;)V", null, true);
        e.addReferenceInstruction(NEW, "IntFactorial");
        e.addNoArgInstruction(DUP);
        e.addMemberAccessInstruction(INVOKESPECIAL, "IntFactorial", "<init>",
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
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "IntFactorial",
                "factorial", "(I)I");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "append", "(I)Ljava/lang/StringBuffer;");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "toString", "()Ljava/lang/String;");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/io/PrintStream",
                "println", "(Ljava/lang/String;)V");
        e.addNoArgInstruction(RETURN);

        // Write IntFactorial.class to file system
        e.write();
    }

}
