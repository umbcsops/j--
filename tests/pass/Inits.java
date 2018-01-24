// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.System;

public class Inits {

    int i = 5;

    int[][] ia = { { 1, 2, 3 }, { 4, i, 6 }, null };

    public static void main(String[] args) {
        Inits obj = new Inits();
        System.out.println(obj.i);
        System.out.println(obj.ia[0][0]);
        System.out.println(obj.ia[1][1]);
        System.out.println(obj.ia[1][2]);
        System.out.println((Object) obj.ia[2]);
    }

}
