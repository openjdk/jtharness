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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

/**
 * This class loader loads classes from files a specified directory.
 * @see com.sun.javatest.util.PathClassLoader
 * @deprecated use PathClassLoader
 */
public class DirectoryClassLoader extends ClassLoader
{
    /**
     * Constructor that provides the name of the directory in which to search
     * for a given class.
     *
     * @param dir  The File specifying the directory to search.
     */
    public DirectoryClassLoader(File dir) {
        loadDir = dir;
    }

    /**
     * Constructor that provides the name of the directory in which to search
     * for a given class.
     *
     * @param dirName  A file pathname specifying the directory to search.
     */
    public DirectoryClassLoader(String dirName) {
        loadDir = new File(dirName);
    }

    //----------ClassLoader methods---------------------------------------------

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

        // check the cache first
        Class cl = (Class)classes.get(name);

        // not found in the cache?
        if (cl == null) {
            // see if it is a system class
            try {
                cl = findSystemClass(name);
            }
            catch (ClassNotFoundException e) {
                // It's not a system class, look in the directory for this
                // class loader.  If there is an IO problem, we'll throw
                // ClassNotFoundException; if the class file is read correctly
                // but is an invalid class, we'll get a LinkageError
                cl = locateClass(name);
            }
        }

        if (resolve)
            resolveClass(cl);

        return cl;

    }

    /**
     * Returns an input stream for reading the specified resource.
     *
     * @param  name the resource name
     * @return an input stream for reading the resource, or <code>null</code>
     *         if the resource could not be found
     *
     *  redundant in 1.2
     */
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Finds the resource with the given name. A resource is some data
     * (images, audio, text, etc) that can be accessed by class code
     * in a way that is independent of the location of the code.
     * <p>
     * The name of a resource is a "/"-separated path name that identifies
     * the resource.
     * <p>
     * This method will first search the parent class loader for the
     * resource, then call <code>findResource</code> to find the
     * resource.
     *
     * @param  name resource name
     * @return a URL for reading the resource, or <code>null</code> if
     *         the resource could not be found or the caller doesn't have
     *          adequate privileges to get the resource.
     *
     *  redundant in 1.2
     */
    public URL getResource(String name) {
        // the ordering of this code looks very strange, but it
        // corresponds to the implementation in JDK 1.2.
        URL url = getSystemResource(name);
        if (url == null) {
            url = findResource(name);
        }
        return url;
    }

    /**
     * Finds the resource with the given name. Class loader
     * implementations should override this method to specify where to
     * find resources.
     *
     * @param  name the resource name
     * @return a URL for reading the resource, or <code>null</code>
     *         if the resource could not be found
     * @since  JDK1.2
     */
    public URL findResource(String name) {
       try {
           File f = new File(loadDir, name);
           if (f.exists())
               return new URL("file:" + f.getAbsolutePath());
           else
               return null;
       }
       catch (java.net.MalformedURLException e) {
           return null;
       }
    }

    //----------internal methods------------------------------------------------

    private synchronized Class locateClass(String name)
        throws ClassNotFoundException {
        //This check is currently necessary; we just
        // check the cache at the one call site, but that was not
        // synchronized, so there is a very small remote chance another
        // caller has just loaded this class.
        Class cl = (Class)classes.get(name);
        if (cl != null)
            return cl;

        String cname = name.replace('.', '/') + ".class";

        File file = new File(loadDir, cname);

        try {
            InputStream in = new FileInputStream(file);
            byte[] data;
            try {
                int len = in.available();
                data = new byte[len];
                for (int total = 0; total < data.length; ) {
                    total += in.read(data, total, data.length - total);
                }
            }
            finally {
                if (in != null)
                    in.close();
            }
            // the next line may throw LinkageError, which we let
            // escape to the caller
            cl = defineClass(name, data, 0, data.length);
            classes.put(name, cl);
            return cl;
        }
        catch (FileNotFoundException e) {
            // ClassNotFoundException is obviously the correct
            // exception to throw in this case
            throw new ClassNotFoundException(name);
        }
        catch (IOException e) {
            // This one is more iffy; we found a file, but had trouble
            // reading it; still use ClassNotFoundException for now.
            throw new ClassNotFoundException(name);
        }

    }

    //----------Data members----------------------------------------------------

    private File loadDir;
    private Hashtable classes = new Hashtable();
}
