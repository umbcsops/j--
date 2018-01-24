// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package junit;

import junit.framework.TestCase;
import pass.GCD;

public class GCDTest extends TestCase {

    private GCD gcd;

    protected void setUp() throws Exception {
        super.setUp();
        gcd = new GCD();
    }

    public void testCompute() {
        assertEquals(gcd.compute(10, 4), 2);
        assertEquals(gcd.compute(4, 9), 1);
        assertEquals(gcd.compute(0, 42), 42);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
