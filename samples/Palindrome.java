// Ex 1.14

import java.lang.System;

public class Palindrome {
  public static void main(String[] args) {
    if (checkPalindrome(args[0])) {
      System.out.println("Palindrome");
    } else {
      System.out.println("Not a palindrome");
    }
  }

  static boolean checkPalindrome(String s) {
    s = s.toLowerCase();
    int i = 0, j = s.length() - 1;

    while (i <= j) {
      if (!(s.charAt(i) == s.charAt(j))) {
        return false;
      }
      ++i;
      j--;
    }
    return true;
  }
}

