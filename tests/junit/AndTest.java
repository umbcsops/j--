package junit;

import junit.framework.TestCase;
import pass.And;

public class AndTest extends TestCase {
  private And and;

  protected void setUp() throws Exception {
    super.setUp();
    and = new And();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testAnd() {
    assertEquals(and.and(1, 2), 0);
    assertEquals(and.and(1, -2), 0);
    assertEquals(and.and(-1, 2), 2);
    assertEquals(and.and(-1, -2), -2);
    assertEquals(and.and(1, 1), 1);
  }
}


