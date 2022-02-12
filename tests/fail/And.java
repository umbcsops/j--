package fail;

import java.lang.System;

public class And {
  public static void main(String[] args) {
    // & only works with intS or anything that can be coerced to an int
    System.out.println(1 & "x");
  }
}


