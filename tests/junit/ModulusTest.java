package junit;

import junit.framework.TestCase;
import pass.Modulus;

public class ModulusTest extends TestCase {
  private Modulus modulus;

  protected void setUp() throws Exception {
    super.setUp();
    modulus = new Modulus();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testModulus() {
    assertEquals(modulus.getRemainder(11, 3), 2);
    assertEquals(modulus.getRemainder(11, 2), 1);
    assertEquals(modulus.getRemainder(-11, 3), -2);
    assertEquals(modulus.getRemainder(11, -3), 2);
    assertEquals(modulus.getRemainder(-11, -3), -2);
    assertEquals(modulus.getRemainder(22, 2), 0);
  }
}
