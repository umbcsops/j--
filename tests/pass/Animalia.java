// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.System;

abstract class Animal {

    protected String scientificName;

    protected Animal(String scientificName) {
        this.scientificName = scientificName;
    }

    public String scientificName() {
        return scientificName;
    }

}

class FruitFly extends Animal {

    public FruitFly() {
        super("Drosophila melanogaster");
    }

}

class Tiger extends Animal {

    public Tiger() {
        super("Panthera tigris corbetti");
    }

}

public class Animalia {

    public static void main(String[] args) {
        FruitFly fruitFly = new FruitFly();
        Tiger tiger = new Tiger();
        System.out.println(fruitFly.scientificName());
        System.out.println(tiger.scientificName());
    }

}
