package pass;

public class Shift {
  public int shiftLeft(int x, int y) {
    return x << y;
  }

  public int shiftRight(int x, int y) {
    return x >> y;
  }

  public int shiftRightWithZeroFill(int x, int y) {
    return x >>> y;
  }
}