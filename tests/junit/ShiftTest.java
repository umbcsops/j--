package junit;

import junit.framework.TestCase;
import pass.Shift;

public class ShiftTest extends TestCase {
  private Shift shift;

  protected void setUp() throws Exception {
    super.setUp();
    shift = new Shift();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testShift() {
    assertEquals(shift.shiftLeft(1, 10), 1024);
    assertEquals(shift.shiftLeft(-1, 10), -1024);
    assertEquals(shift.shiftLeft(1, -10), 4194304);
    assertEquals(shift.shiftLeft(-1, -10), -4194304);

    assertEquals(shift.shiftRight(10, 2), 2);
    assertEquals(shift.shiftRight(-10, 2), -3);
    assertEquals(shift.shiftRight(10, -2), 0);
    assertEquals(shift.shiftRight(-10, -2), -1);

    assertEquals(shift.shiftRightWithZeroFill(10, 2), 2);
    assertEquals(shift.shiftRightWithZeroFill(-10, 2), 1073741821);
    assertEquals(shift.shiftRightWithZeroFill(10, -2), 0);
    assertEquals(shift.shiftRightWithZeroFill(-10, -2), 3);
  }
}
