// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

import spim.SPIM;

// Prints factorial of a number computed using recursive and iterative 
// algorithms.

public class Factorial {

    // Return the factorial of the given number computed recursively.

    public static int computeRec(int n) {
        if (n <= 0) {
            return 1;
        } else {
            return n * computeRec(n - 1);
        }
    }

    // Return the factorial of the given number computed iteratively.

    public static int computeIter(int n) {
        int result = 1;
        while (n > 0) {
            result = result * n--;
        }
        return result;
    }

    // Entry point; print factorial of a number computed using
    // recursive and iterative algorithms.

    public static void main(String[] args) {
        int n = 7;
        SPIM.printInt(Factorial.computeRec(n));
        SPIM.printChar('\n');
        SPIM.printInt(Factorial.computeIter(n));
        SPIM.printChar('\n');
    }

}
