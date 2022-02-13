package junit;

import junit.framework.TestCase;
import pass.BitwiseNot;

public class BitwiseNotTest extends TestCase {
  private BitwiseNot bitwiseNot;

  protected void setUp() throws Exception {
    super.setUp();
    bitwiseNot = new BitwiseNot();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testBitwiseNot() {
    assertEquals(bitwiseNot.bitwiseNot(0), -1);
    assertEquals(bitwiseNot.bitwiseNot(1), -2);
    assertEquals(bitwiseNot.bitwiseNot(~1), 1);
    assertEquals(bitwiseNot.bitwiseNot(-2), 1);
  }
}
