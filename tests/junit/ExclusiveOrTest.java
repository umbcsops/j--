package junit;

import junit.framework.TestCase;
import pass.ExclusiveOr;

public class ExclusiveOrTest extends TestCase {
  private ExclusiveOr exclusiveOr;

  protected void setUp() throws Exception {
    super.setUp();
    exclusiveOr = new ExclusiveOr();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testExclusiveOr() {
    assertEquals(exclusiveOr.exclusiveOr(1, 2), 3);
    assertEquals(exclusiveOr.exclusiveOr(1, -2), -1);
    assertEquals(exclusiveOr.exclusiveOr(-1, 2), -1);
    assertEquals(exclusiveOr.exclusiveOr(-1, -2), 1);
    assertEquals(exclusiveOr.exclusiveOr(1, 1), 0);
  }
}

