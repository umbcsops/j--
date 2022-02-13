package junit;

import junit.framework.TestCase;
import pass.UnaryPlus;

public class UnaryPlusTest extends TestCase {
  private UnaryPlus unaryPlus;

  protected void setUp() throws Exception {
    super.setUp();
    unaryPlus = new UnaryPlus();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testUnaryPlus() {
    assertEquals(unaryPlus.unaryPlus(0), 0);
    assertEquals(unaryPlus.unaryPlus(1), 1);
    assertEquals(unaryPlus.unaryPlus(+1), 1);
    assertEquals(unaryPlus.unaryPlus(-1), -1);
    assertEquals(unaryPlus.unaryPlus(+(-1)), -1);
  }
}

