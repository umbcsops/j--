// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

import spim.SPIM;

// Prints GCD(a, b) to STDOUT.

public class GCD {

    // Return the remainder of a and b from an implied division; a is
    // the dividend and b is the divisor.

    public static int mod(int a, int b) {
        if (b == 0) {
            return a;
        } else if (a == b) {
            return 0;
        } else if (a > b) {
            return mod(a - b, b);
        }
        return a;
    }

    // Compute the GCD(a, b) using the Euclidean algorithm and return
    // the value.

    public static int compute(int a, int b) {
        if (a == 0) {
            return b;
        }
        return compute(b, mod(a, b));
    }

    // Entry point; prints GCD(a, b) to STDOUT.

    public static void main(String[] args) {
        int a = 25;
        int b = 60;
        SPIM.printInt(GCD.compute(a, b));
        SPIM.printChar('\n');
    }

}
