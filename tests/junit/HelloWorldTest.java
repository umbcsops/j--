// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package junit;

import junit.framework.TestCase;
import pass.HelloWorld;

public class HelloWorldTest extends TestCase {

    public void testMessage() {
        this.assertEquals(HelloWorld.message(), "Hello, World!");
    }

}
