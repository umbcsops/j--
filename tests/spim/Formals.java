// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

import spim.SPIM;

// A test to check if argument passing is done correctly with calls 
// to functions with more than 4 arguments.

public class Formals {

    // Return the sum of the arguments and a local variable.

    public static int sum(int a, int b, int c, int d, int e, int f) {
        int g = 7;
        return a + b + c + d + e + f + g;
    }

    // Return the product of the arguments and a local variable.

    public static int product(int a, int b, int c, int d, int e, int f) {
        int g = 7;
        return a * b * c * d * e * f * g;
    }

    // Entry point; calls sum() and product() to print the sum
    // and product of the first 7 integers.

    public static void main(String[] args) {
        SPIM.printInt(Formals.sum(1, 2, 3, 4, 5, 6));
        SPIM.printChar('\n');
        SPIM.printInt(Formals.product(1, 2, 3, 4, 5, 6));
        SPIM.printChar('\n');
    }

}
