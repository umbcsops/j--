// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.System;

public class Strings {

    String strf = "foo ";

    static String staticstrf = "bar";

    public String foo() {
        String[] strs = { "cat", "fish" };
        String str = "bowl";

        str += " and plate";
        strs[1] += str;
        strf += str + "cat";
        staticstrf += str;
        return str + strs[0] + strs[1] + strf + staticstrf;
    }

    public static void main(String[] args) {
        System.out.println((new Strings()).foo());
    }

}
