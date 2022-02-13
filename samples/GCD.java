// Ex 1.11
import java.lang.System;
import java.lang.Integer;

public class GCD {
  public static void main(String[] args) {
    int n = Integer.parseInt(args[0]);
    int m = Integer.parseInt(args[1]);
    System.out.println(gcd(n, m));
  }

  static int gcd(int x, int y) {
    if (y == 0) {
      return x;
    }

    return gcd(y, x % y);
  }
}
