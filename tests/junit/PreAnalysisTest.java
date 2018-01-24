// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package junit;

import junit.framework.TestCase;
import jminusminus.Main;
import java.io.File;

/**
 * JUnit test case for pre-analysis of the AST.
 */

public class PreAnalysisTest extends TestCase {

    /**
     * Construct a PreAnalysisTest object.
     */

    public PreAnalysisTest() {
        super("JUnit test case for pre-analysis of the AST");
    }

    /**
     * Run the compiler against each pass-test file under the folder specified
     * by PASS_TESTS_DIR property.
     */

    public void testPass() {
        File passTestsDir = new File(System.getProperty("PASS_TESTS_DIR"));
        String frontEnd = System.getProperty("FRONT_END");
        File[] files = passTestsDir.listFiles();
        boolean errorHasOccurred = false;
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].toString().endsWith(".java")) {
                System.out.printf(
                        "Running compiler (up to pre-analysis) on %s ...\n\n",
                        files[i].toString());
                String[] args = { "-pa", files[i].toString() };
                Main.main(args);
                System.out.printf("\n\n");

                // true even if a single test fails
                errorHasOccurred |= Main.errorHasOccurred();
            }
        }

        // We want all tests to pass
        assertFalse(errorHasOccurred);
    }

    /**
     * Entry point.
     * 
     * @param args
     *            command-line arguments.
     */

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PreAnalysisTest.class);
    }

}
