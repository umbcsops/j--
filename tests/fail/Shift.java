package fail;

import java.lang.System;

public class Shift {
  public static void main(String[] args) {
    // only intS are supported
    System.out.println(10 << 'x');
    System.out.println(10 >> 'x');
    System.out.println(10 >>> 'x');
  }
}
