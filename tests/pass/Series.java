// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.Integer;
import java.lang.System;

public class Series {

    public static int ARITHMETIC = 1;

    public static int GEOMETRIC = 2;

    private int a; // first term

    private int d; // common sum or multiple

    private int n; // number of terms

    public Series() {
        this(1, 1, 10);
    }

    public Series(int a, int d, int n) {
        this.a = a;
        this.d = d;
        this.n = n;
    }

    public int computeSum(int kind) {
        int sum = a, t = a, i = n;
        while (i-- > 1) {
            if (kind == ARITHMETIC) {
                t += d;
            } else if (kind == GEOMETRIC) {
                t = t * d;
            }
            sum += t;
        }
        return sum;
    }

    public static void main(String[] args) {
        int a = Integer.parseInt(args[0]);
        int d = Integer.parseInt(args[1]);
        int n = Integer.parseInt(args[2]);
        Series s = new Series(a, d, n);
        System.out.println("Arithmetic sum = "
                + s.computeSum(Series.ARITHMETIC));
        System.out.println("Geometric sum = " + s.computeSum(Series.GEOMETRIC));
    }

}
