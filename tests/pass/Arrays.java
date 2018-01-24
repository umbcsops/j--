// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.System;

public class Arrays {

    int[] ia = { 1, 2, 3, (int) '4' };

    int[] ia2 = { 3, 4, 5, 6, 7 };

    int[][] iaa = { ia, ia2 };

    int[][] iaa2 = { { 1, 2, 3 }, { 1, 2, 3, 4 }, {} };

    public static void main(String[] args) {
        int[][] iaa3 = { { 1, 2, 3 }, { 1, 2, 3, 4 }, {} };
        System.out.println((Object) iaa3);
    }

}
