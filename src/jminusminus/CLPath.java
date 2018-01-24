// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class can be used to locate and load system, extension, and user-defined
 * class files from directories and zip (jar) files. The code for this class has
 * been adapted from the Kopi (http://www.dms.at/kopi/) project.
 */

class CLPath {

    /**
     * Stores the individual directories, zip, and jar files from the class
     * path.
     */
    private ArrayList<String> dirs;

    /**
     * Return a list of conceptual directories defining the class path.
     * 
     * @param classPath
     *            the directory names defining the class path.
     * @return a list of conceptual directories defining the class path.
     */

    private ArrayList<String> loadClassPath(String classPath) {
        ArrayList<String> container = new ArrayList<String>();

        // Add directories/jars/zips from the classpath
        StringTokenizer entries = new StringTokenizer(classPath,
                File.pathSeparator);
        while (entries.hasMoreTokens()) {
            container.add(entries.nextToken());
        }

        // Add system directories
        if (System.getProperty("sun.boot.class.path") != null) {
            entries = new StringTokenizer(System
                    .getProperty("sun.boot.class.path"), File.pathSeparator);
            while (entries.hasMoreTokens()) {
                container.add(entries.nextToken());
            }
        } else {
            float version = Float
                    .parseFloat(System.getProperty("java.version"));
            if (version > 1.1) {
                String dir = System.getProperty("java.home")
                        + File.separatorChar + "lib" + File.separatorChar
                        + "rt.jar";
                container.add(dir);
            }
        }
        return container;
    }

    /**
     * Construct a CLPath object.
     */

    public CLPath() {
        this(null, null);
    }

    /**
     * Construct a CLPath object.
     * 
     * @param path
     *            the directory names defining the class path, separated by path
     *            separator.
     * @param extdir
     *            the directory for the Java extension classes.
     */

    public CLPath(String path, String extdir) {
        if (path == null) {
            // No path specified, use CLASSPATH
            path = System.getProperty("java.class.path");
        }
        if (path == null) {
            // Last resort, use current directory
            path = ".";
        }
        dirs = loadClassPath(path);
        if (extdir == null) {
            // Java extension classes
            extdir = System.getProperty("java.ext.dirs");
        }
        if (extdir != null) {
            File extDirectory = new File(extdir);
            if (extDirectory.isDirectory()) {
                File[] extFiles = extDirectory.listFiles();
                for (int i = 0; i < extFiles.length; i++) {
                    File file = extFiles[i];
                    if (file.isFile()
                            && (file.getName().endsWith(".zip") || file
                                    .getName().endsWith(".jar"))) {
                        dirs.add(file.getName());
                    } else {
                        // Wrong suffix; ignore
                    }
                }
            }
        }
    }

    /**
     * Return a CLInputStream instance for the class with specified name
     * (fully-qualified; tokens separated by '/') or null if the class was not
     * found.
     * 
     * @param name
     *            the fully-qualified name of the class -- java/util/ArrayList
     *            for example.
     * @return a CLInputStream instance for the class with specified name or
     *         null if the class was not found.
     */

    public CLInputStream loadClass(String name) {
        CLInputStream reader = null;
        for (int i = 0; i < dirs.size(); i++) {
            String dir = dirs.get(i);
            File file = new File(dir);
            if (file.isDirectory()) {
                File theClass = new File(dir, name.replace('/',
                        File.separatorChar)
                        + ".class");
                if (theClass.canRead()) {
                    try {
                        reader = new CLInputStream(new BufferedInputStream(
                                new FileInputStream(theClass)));
                    } catch (FileNotFoundException e) {
                        // Ignore
                    }
                }
            } else if (file.isFile()) {
                try {
                    ZipFile zip = new ZipFile(dir);
                    ZipEntry entry = zip.getEntry(name + ".class");
                    if (entry != null) {
                        reader = new CLInputStream(zip.getInputStream(entry));
                    }
                } catch (IOException e) {
                    // Ignore
                }
            } else {
                // Bogus entry; ignore
            }
        }
        return reader;
    }

}
