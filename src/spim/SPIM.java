// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package spim;

/**
 * This is a Java wrapper class for the SPIM runtime object SPIM.s. Any j--
 * program that's compiled for the SPIM target must import this class for (file
 * and console) IO operations. Note that the functions have no implementations
 * here which means that if the programs using this class are compiled using
 * j--, they will compile fine but won't function as desired when run against
 * the JVM. Such programs must be compiled using the j-- compiler for the SPIM
 * target and must be run against the SPIM simulator.
 */

public class SPIM {

    /** Wrapper for SPIM.printInt(). */

    public static void printInt(int value) {
    }

    /** Wrapper for SPIM.printFloat(). */

    public static void printFloat(float value) {
    }

    /** Wrapper for SPIM.printDouble(). */

    public static void printDouble(double value) {
    }

    /** Wrapper for SPIM.printString(). */

    public static void printString(String value) {
    }

    /** Wrapper for SPIM.printChar(). */

    public static void printChar(char value) {
    }

    /** Wrapper for SPIM.readInt(). */

    public static int readInt() {
        return 0;
    }

    /** Wrapper for SPIM.readFloat(). */

    public static float readFloat() {
        return 0;
    }

    /** Wrapper for SPIM.readDouble(). */

    public static double readDouble() {
        return 0;
    }

    /** Wrapper for SPIM.readString(). */

    public static String readString(int length) {
        return null;
    }

    /** Wrapper for SPIM.readChar(). */

    public static char readChar() {
        return ' ';
    }

    /** Wrapper for SPIM.open(). */

    public static int open(String filename, int flags, int mode) {
        return 0;
    }

    /** Wrapper for SPIM.read(). */

    public static String read(int fd, int length) {
        return null;
    }

    /** Wrapper for SPIM.write(). */

    public static int write(int fd, String buffer, int length) {
        return 0;
    }

    /** Wrapper for SPIM.close(). */

    public static void close(int fd) {
    }

    /** Wrapper for SPIM.exit(). */

    public static void exit() {
    }

    /** Wrapper for SPIM.exit2(). */

    public static void exit2(int status) {
    }

}
