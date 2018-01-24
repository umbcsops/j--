// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.System;

public class Classes {

    public static String message() {
        return A.a + ", " + (new B()).b;
    }

    public static void main(String[] args) {
        System.out.println(Classes.message());
    }

}

class A {

    public static String a = "Hello";

}

class B {

    public String b = "World!";

}
