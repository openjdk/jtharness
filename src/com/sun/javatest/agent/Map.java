/*
 * $Id$
 *
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.agent;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import com.sun.javatest.util.StringArray;
import java.io.PrintStream;

/**
 * A map provides a simple translation table for between configuration values
 * as provided by JT Harness and as required by an Agent. Typically, this is for
 * adjusting filenames because of differences introduced by different mount points
 * for various file systems.
 * <p>
 * This facility has been largely superceded by the map substitution mechanism
 * provided by environment files.
 */
public class Map {
    /**
     * Read a map from a specified file.  This code is deliberately
     * written to tolerate Java platforms without a file system ...
     * as in some versions of Personal Java. Any problems caused by
     * the file system not being present are returned as IOExceptions
     * with a suitable detail message.
     * @param name The name of the file to read
     * @return The map read from the given file
     * @throws IOException if any errors occurred reading the file
     */
    public static Map readFile(String name) throws IOException {
        try {
            Class c = Class.forName("java.io.FileReader"); // optional API in Jersonal Java
            Constructor m = c.getConstructor(new Class[] {String.class});
            Reader r = (Reader)(m.newInstance(new Object[] {name}));
            return new Map(r);
        }
        catch (ClassNotFoundException e) {
            throw new IOException("file system not accessible (" + e + ")");
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof IOException)
                throw (IOException)t;
            else
                throw fileSystemProblem(t);
        }
        catch (IllegalAccessException e) {
            throw fileSystemProblem(e);
        }
        catch (InstantiationException e) {
            throw fileSystemProblem(e);
        }
        catch (NoSuchMethodException e) {
            throw fileSystemProblem(e);
        }
        catch (Error e) {
            throw fileSystemProblem(e);
        }
    }

    /**
     * Read a map from a URL.
     * @param u The URL to read
     * @return The map read from the given URL
     * @throws IOException if any errors occurred reading the URL
     */
    public static Map readURL(URL u) throws IOException {
        return new Map(new InputStreamReader(u.openStream()));
    }


    /**
     * Read a map from a local file (if the name does not begin with http:)
     * or from a URL if it does.  The method is simply a wrapper that delegates
     * to either {@link #readFile} or {@link #readURL} depending on the
     * argument.
     * @param name The name of the file or URL to read
     * @return The map read from the given location
     * @throws IOException if any errors occurred reading the map
     */
    public static Map readFileOrURL(String name) throws IOException {
        if (name.length() > 5 && name.substring(0, 5).equalsIgnoreCase("http:"))
            return readURL(new URL(name));
        else
            return readFile(name);
    }

    private static IOException fileSystemProblem(Throwable t) {
        return new IOException("problem accessing file system: " + t);
    }

    /**
     * Create a map by reading it from a given stream.
     * @param r         The reader from which to read the map data. The reader is closed
     *                  after it has been completely read
     * @throws IOException if problems occur while reading the map data.
     */
    public Map(Reader r) throws IOException {
        BufferedReader in =
            (r instanceof BufferedReader ? (BufferedReader)r : new BufferedReader(r))
;
        // data arrives in rows, but we want it in columns
        Vector from = new Vector();
        Vector to = new Vector();
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0  &&  !line.startsWith("#")) {
                String[] row = StringArray.split(line);
                if (row.length < 2)
                    throw new IOException("format error in map file, line is: " + line);
                from.addElement(row[0]);
                to.addElement(row[1]);
            }
        }
        in.close();

        fromValues = new String[from.size()];
        from.copyInto(fromValues);
        toValues = new String[to.size()];
        to.copyInto(toValues);
    }

    /**
     * Translate the strings according to values in the map.
     * The strings are updated in place.
     * @param args      An array of strings to be translated according to the data
     *                  the map.
     */
    public void map(String[] args) {
        if (fromValues == null)
            return; // empty table

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            for (int j = 0; j < fromValues.length; j++) {
                String f = fromValues[j];
                String t = toValues[j];
                for (int index = arg.indexOf(f);
                     index != -1;
                     index = arg.indexOf(f, index + t.length())) {
                    arg = arg.substring(0, index) + t + arg.substring(index + f.length());
                }
            }
            if (tracing && arg != args[i]) {
                traceOut.println("MAPARG-from: " + args[i]);
                traceOut.println("MAPARG-to:   " + arg);
            }
            args[i] = arg;
        }
    }

    /**
     * Enumerate the entries of the map.
     * @return an enumeration of the translation entries within the map
     */
    public Enumeration enumerate() {
        Vector v = new Vector(fromValues.length);
        for (int i = 0; i < fromValues.length; i++) {
            v.addElement(new String[] {fromValues[i], toValues[i]});
        }
        return v.elements();
    }

    public void setTracing(boolean state, PrintStream out) {
        tracing = state;
        if (state) {
            traceOut = out;
        }
        else {
            traceOut = null;
        }
    }

    private boolean tracing;
    private PrintStream traceOut;
    private String[] fromValues;
    private String[] toValues;
}
