package fail;

import java.lang.System;

public class Modulus {
  public static void main(String[] args) {
    // only intS are supported for the modulus operator
    System.out.println(42 % 'x');
  }
}
