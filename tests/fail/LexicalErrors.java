// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package fail;

import java.lang.System;

// This program has lexical errors and shouldn't compile.

public class LexicalErrors {

    # Illegal comment.
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }

}
