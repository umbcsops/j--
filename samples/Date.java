// Ex 1.13

import java.lang.System;
import java.lang.Integer;

public class Date {
  private static String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

  public static void main(String[] args) {
    String[] dateComponents = args[0].split("-");
    String year = dateComponents[0];
    int month = Integer.parseInt(dateComponents[1]);
    String day = dateComponents[2];

    System.out.println(months[month - 1] + " "  + day + ", " + year);
  }
}
