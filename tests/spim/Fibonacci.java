// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

import spim.SPIM;

// Prints to STDOUT all Fibonacci numbers below 10000.

public class Fibonacci {

    // Entry point; prints to STDOUT all Fibonacci numbers below
    // 10000.

    public static void main(String[] args) {
        int lo = 0;
        int hi = 1;
        while (hi <= 9999) {
            hi = hi + lo;
            lo = hi - lo;
            SPIM.printInt(lo);
            SPIM.printChar('\n');
        }
    }

}
