// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package junit;

import java.io.File;
import junit.framework.TestCase;
import jminusminus.JavaCCMain;

/**
 * JUnit test case for running the j-- compiler on the j-- test programs under
 * tests/pass and tests/fail folders using JavaCC frontend.
 */

public class JMinusMinusTestJavaCC extends TestCase {

    /**
     * Construct a JMinusMinusTestJavaCC object.
     */

    public JMinusMinusTestJavaCC() {
        super("JUnit test case for the j-- compiler");
    }

    /**
     * Run the j-- compiler against each pass-test file under the folder
     * specified by PASS_TESTS_DIR property in the build.xml file.
     */

    public void testPass() {
        File passTestsDir = new File(System.getProperty("PASS_TESTS_DIR"));
        File genClassDir = new File(System.getProperty("GEN_CLASS_DIR"));
        File[] files = passTestsDir.listFiles();
        boolean errorHasOccurred = false;
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].toString().endsWith(".java")) {
                String[] args = null;
                System.out
                        .printf("Running j-- (with "
                                + "javacc frontend) on %s ...\n\n", files[i]
                                .toString());
                args = new String[] { "-d", genClassDir.getAbsolutePath(),
                        files[i].toString() };
                JavaCCMain.main(args);
                System.out.printf("\n\n");

                // true even if a single test fails
                errorHasOccurred |= JavaCCMain.errorHasOccurred();
            }
        }

        // We want all tests to pass
        assertFalse(errorHasOccurred);
    }

    /**
     * Run the j-- compiler against each fail-test file under the folder
     * specified by FAIL_TESTS_DIR property in the build.xml file.
     */

    public void testFail() {
        File failTestsDir = new File(System.getProperty("FAIL_TESTS_DIR"));
        File genClassDir = new File(System.getProperty("GEN_CLASS_DIR"));
        File[] files = failTestsDir.listFiles();
        boolean errorHasOccurred = true;
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].toString().endsWith(".java")) {
                String[] args = null;
                System.out
                        .printf("Running j-- (with "
                                + "javacc frontend) on %s ...\n\n", files[i]
                                .toString());
                args = new String[] { "-d", genClassDir.getAbsolutePath(),
                        files[i].toString() };
                JavaCCMain.main(args);
                System.out.printf("\n\n");

                // true only if all tests fail
                errorHasOccurred &= JavaCCMain.errorHasOccurred();
            }
        }

        // We want all tests to fail
        assertTrue(errorHasOccurred);
    }

    /**
     * Entry point.
     * 
     * @param args
     *            command-line arguments.
     */

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JMinusMinusTestJavaCC.class);
    }

}
