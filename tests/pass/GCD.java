// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.Integer;
import java.lang.System;

// Accepts two ints (a and b) as command-line arguments and prints
// their greatest common divisor (GCD) to STDOUT.

public class GCD {

    // Return the remainder of a and b from an implied division;
    // a is the dividend and b is the divisor.

    private int mod(int a, int b) {
        if (b == 0) {
            return a;
        } else if (a == b) {
            return 0;
        } else if (a > b) {
            return mod(a - b, b);
        }
        return a;
    }

    // Compute the GCD(a, b) using the Euclidean algorithm and
    // return the value.

    public int compute(int a, int b) {
        if (a == 0) {
            return b;
        }
        return compute(b, mod(a, b));
    }

    // Entry point; accepts two ints (a and b) as command-line
    // arguments and prints GCD(a, b) to STDOUT.

    public static void main(String[] args) {
        GCD gcd = new GCD();
        int a = Integer.parseInt(args[0]);
        int b = Integer.parseInt(args[1]);
        System.out.println("GCD(" + a + ", " + b + ") = " + gcd.compute(a, b));
    }

}
