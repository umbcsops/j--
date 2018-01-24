// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.Integer;
import java.lang.System;

public class IntFactorial {

    public int factorial(int n) {
        if (n <= 1) {
            return n;
        } else {
            return (n * factorial(n - 1));
        }
    }

    public static void main(String[] args) {
        IntFactorial f = new IntFactorial();
        int n = Integer.parseInt(args[0]);
        System.out.println("Factorial( " + n + " ) = " + f.factorial(n));
    }

}
