// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package junit;

import junit.framework.TestCase;
import pass.Series;

public class SeriesTest extends TestCase {

    private Series series;

    protected void setUp() throws Exception {
        super.setUp();
        series = new Series(1, 1, 100);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testComputeSum() {
        this.assertEquals(series.computeSum(Series.ARITHMETIC), 5050);
        this.assertEquals(series.computeSum(Series.GEOMETRIC), 100);
    }

}
