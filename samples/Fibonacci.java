// Ex 1.10
import java.lang.System;
import java.lang.Integer;

public class Fibonacci {
  public static void main(String[] args) {
    int n = Integer.parseInt(args[0]);
    System.out.println(fibonacci(n));
  }

  private static int fibonacci(int n) {
    if (n <= 0) {
      return 0;
    } else if (n == 1) {
      return 1;
    } else {
      return fibonacci(n - 1) + fibonacci(n - 2);
    }
  }
}
