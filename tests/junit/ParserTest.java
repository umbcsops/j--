// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package junit;

import junit.framework.TestCase;
import jminusminus.Main;
import java.io.File;

/**
 * JUnit test case for the parser.
 */

public class ParserTest extends TestCase {

    /**
     * Construct a ParserTest object.
     */

    public ParserTest() {
        super("JUnit test case for the parser");
    }

    /**
     * Run the parser against each pass-test file under the folder specified by
     * PASS_TESTS_DIR property. FRONT_END property determines the frontend
     * (handwritten or JavaCC) to use.
     */

    public void testPass() {
        File passTestsDir = new File(System.getProperty("PASS_TESTS_DIR"));
        File[] files = passTestsDir.listFiles();
        boolean errorHasOccurred = false;
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].toString().endsWith(".java")) {
                String[] args = null;
                System.out.printf("Running handwritten parser on %s ...\n\n",
                        files[i].toString());
                args = new String[] { "-p", files[i].toString() };
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
        junit.textui.TestRunner.run(ParserTest.class);
    }

}
