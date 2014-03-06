/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.javatest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A class loader for loading classes from a path of directories,
 * zip files and jar files.
 */
public class PathClassLoader extends ClassLoader
{
    /**
     * Create a PathClassLoader, specifying a path.
     * @param pathString a string containing a sequence of
     *   file paths separated by the platform-specific file
     *   separator, identifying a sequence of locations in which
     *   to look for classes to be loaded
     * @see File#pathSeparator
     */
    public PathClassLoader(String pathString) {
        this.path = split(pathString);
    }

    /**
     * Create a PathClassLoader, specifying a path and a
     * base directory for any relative files on the path.
     * @param baseDir the base directory for any relative
     * files on the path
     * @param pathString a string containing a sequence of
     *   file paths separated by the platform-specific file
     *   separator, identifying a sequence of locations in which
     *   to look for classes to be loaded
     * @see File#pathSeparator
     */
    public PathClassLoader(File baseDir, String pathString) {
        path = split(pathString);
        for (int i = 0; i < path.length; i++) {
            File f = path[i];
            if (!f.isAbsolute())
                path[i] = new File(baseDir, f.getPath());
        }
    }

    /**
     * Create a PathCloader, specifying an array of files for the path.
     * @param path an array of files, identifying a sequence of locations in which
     *   to look for classes to be loaded
     */
    public PathClassLoader(File[] path) {
        this.path = path;
    }

    /**
     * Attempt to load a class if it is not already loaded, and optionally
     * resolve any imports it might have.
     *
     * @param name The fully-qualified name of the class to load.
     * @param resolve True if imports should be resolved, false otherwise.
     * @return the class that was loaded
     * @throws ClassNotFoundException if the class was not found.
     */
    protected Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {

            Class cl = (Class)classes.get(name);

            if (cl == null) {
                try {
                    cl = findSystemClass(name);
                }
                catch (ClassNotFoundException e) {
                    cl = locateClass(name);
                }
            }

            if (resolve)
                resolveClass(cl);

            return cl;
    }


    private synchronized Class locateClass(String name)
        throws ClassNotFoundException {
        //System.err.println("locateClass: " + name);
        Class c = (Class)classes.get(name);
        if (c != null)
            return c;

        for (int i = 0; i < path.length; i++) {
            if (path[i].isDirectory())
                c = locateClassInDir(name, path[i]);
            else
                c = locateClassInJar(name, path[i]);

            if (c != null) {
                classes.put(name, c);
                return c;
            }
        }

        throw new ClassNotFoundException(name);
    }

    private Class locateClassInDir(String name, File dir)
        throws ClassNotFoundException {
        //System.err.println("locateClassInDir: " + name + " " + dir);
        String cname = name.replace('.', '/') + ".class";
        try {
            File file = new File(dir, cname);
            return readClass(name, new FileInputStream(file), (int)(file.length()));
        }
        catch (IOException e) {
            //System.err.println("locateClassInDir: " + e);
            return null;
        }
    }

    private Class locateClassInJar(String name, File jarFile)
        throws ClassNotFoundException {
        //System.err.println("locateClassInJar: " + name + " " + jarFile);
        String cname = name.replace('.', '/') + ".class";
        try {
            ZipFile z = (ZipFile)zips.get(jarFile);
            if (z == null) {
                z = new ZipFile(jarFile);
                zips.put(jarFile, z);
            }
            ZipEntry ze = z.getEntry(cname);
            if (ze == null)
                return null;
            return readClass(name, z.getInputStream(ze), (int)(ze.getSize()));
        }
        catch (IOException e) {
            //System.err.println("locateClassInJar: " + e);
            return null;
        }
    }

    private Class readClass(String name, InputStream in, int size) throws IOException {
        byte[] data = new byte[size];
        try {
            for (int total = 0; total < size; ) {
                total += in.read(data, total, size - total);
            }
        }
        finally {
            in.close();
        }
        return defineClass(name, data, 0, data.length);
    }

    private File[] split(String s) {
        char pathCh = File.pathSeparatorChar;
        Vector v = new Vector();
        int start = 0;
        for (int i = s.indexOf(pathCh); i != -1; i = s.indexOf(pathCh, start)) {
            add(s.substring(start, i), v);
            start = i + 1;
        }
        if (start != s.length())
            add(s.substring(start), v);
        File[] path = new File[v.size()];
        v.copyInto(path);
        return path;
    }

    private void add(String s, Vector v) {
        if (s.length() != 0)
            v.addElement(new File(s));
    }

    private File[] path;
    private Hashtable classes = new Hashtable();
    private Hashtable zips = new Hashtable();
}
