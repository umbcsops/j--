// Ex 1.12

import java.lang.System;
import java.lang.Integer;

public class Primes {
  public static void main(String[] args) {
    int n  = Integer.parseInt(args[0]);
    printPrimes(n);
  }

  private static void printPrimes(int n) {
    int[] ps = sieve(n);
    int i = 0;
    while (i <= ps.length - 1) {
      System.out.print(ps[i] +  " ");
      ++i;
    }
    System.out.println();
  }

  private static int[] sieve(int n) {
    int[] bs = new int[n + 1];

    int i = 0;
    while (i <= n) {
      bs[i] = 1;
      ++i;
    }

    int lim = Math.isqrt(n);
    int j = 2;
    while (j <= lim) {
      if (bs[j] == 1) {
        int k = j * j;
        while (k <= n) {
          bs[k] = 0;
          k += j;
        }
      }
      ++j;
    }

    i = 2;
    int len = 0;
    while (i <= n) {
      if (bs[i] == 1) {
        ++len;
      }
      ++i;
    }

    int[] ps = new int[len];
    i = 2;
    j = -1;
    while (i <= n) {
      if (bs[i] == 1) {
        ps[++j] = i;
      }
      ++i;
    }

    return ps;
  }
}

class Math {
  public static int isqrt(int n) {
    int i = 1;
    while (i * i <= n-1) {
      ++i;
    }

    return i;
  }
}
