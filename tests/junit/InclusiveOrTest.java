package junit;

import junit.framework.TestCase;
import pass.InclusiveOr;

public class InclusiveOrTest extends TestCase {
  private InclusiveOr inclusiveOr;

  protected void setUp() throws Exception {
    super.setUp();
    inclusiveOr = new InclusiveOr();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testInclusiveOr() {
    assertEquals(inclusiveOr.inclusiveOr(1, 2), 3);
    assertEquals(inclusiveOr.inclusiveOr(1, -2), -1);
    assertEquals(inclusiveOr.inclusiveOr(-1, 2), -1);
    assertEquals(inclusiveOr.inclusiveOr(-1, -2), -1);
    assertEquals(inclusiveOr.inclusiveOr(1, 1), 1);
  }
}
