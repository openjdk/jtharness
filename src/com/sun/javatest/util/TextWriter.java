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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * This class implements an output stream that has additional methods for
 * printing. It is based on PrintStream except that it does not swallow
 * exceptions.
 */

public class TextWriter extends FilterWriter {

    /**
     * Create a new TextWriter, without automatic line flushing.
     *
     * @param  out        A character-output stream
     */
    public TextWriter(Writer out) {
        this(out, false);
    }

    /**
     * Create a new TextWriter.
     *
     * @param  out        A character-output stream
     * @param  autoFlush  A boolean; if true, the println() methods will flush
     *                    the output buffer
     */
    public TextWriter(Writer out, boolean autoFlush) {
        // this next call establishes "out" as the value of the
        // protected variable "lock"
        super(out);
        this.autoFlush = autoFlush;
        lineSeparator = System.getProperty("line.separator");
    }

    /* Methods that do not terminate lines */

    /**
     * Print a boolean.
     * @param b the boolean to be printed
     * @throws IOException if there is a problem writing to the stream
     */
    public void print(boolean b) throws IOException {
        write(b ? "true" : "false");
    }

    /**
     * Print a character.
     * @param c the character to be printed
     * @throws IOException if there is a problem writing to the stream
     */
    public void print(char c) throws IOException {
        write(String.valueOf(c));
    }

    /**
     * Print an integer.
     * @param i the integer to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void print(int i) throws IOException {
        write(String.valueOf(i));
    }

    /**
     * Print a long.
     * @param l the long to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void print(long l) throws IOException {
        write(String.valueOf(l));
    }

    /**
     * Print a float.
     * @param f the float to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void print(float f) throws IOException {
        write(String.valueOf(f));
    }

    /**
     * Print a double.
     * @param d the double to be printed
     * @throws IOException if an error occurred while writing to the stream
     **/
    public void print(double d) throws IOException {
        write(String.valueOf(d));
    }

    /**
     * Print an array of characters.
     * @param s the array of characters to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void print(char s[]) throws IOException {
        write(s);
    }

    /**
     * Print a string.
     * @param s the string to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void print(String s) throws IOException {
        if (s == null) {
            s = "null";
        }
        write(s);
    }

    /**
     * Print an object.
     * @param obj the object to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void print(Object obj) throws IOException {
        write(String.valueOf(obj));
    }


    /* Methods that do terminate lines */

    /**
     * Finish the line.
     * @throws IOException if an error occurred while writing to the stream
     */
    public void println() throws IOException {
        synchronized (lock) {
            out.write(lineSeparator);
            if (autoFlush)
                out.flush();
        }
    }

    /**
     * Print a boolean, and then finish the line.
     * @param b the boolean to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void println(boolean b) throws IOException {
        synchronized (lock) {
            print(b);
            println();
        }
    }

    /**
     * Print a character, and then finish the line.
     * @param c the character to beprinted
     * @throws IOException if an error occurred while writing to the stream
     */
    public void println(char c) throws IOException {
        synchronized (lock) {
            print(c);
            println();
        }
    }

    /**
     * Print an integer, and then finish the line.
     * @param i the int to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void println(int i) throws IOException {
        synchronized (lock) {
            print(i);
            println();
        }
    }

    /**
     * Print a long, and then finish the line.
     * @param l the long to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void println(long l) throws IOException {
        synchronized (lock) {
            print(l);
            println();
        }
    }

    /**
     * Print a float, and then finish the line.
     * @param f the float to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void println(float f) throws IOException {
        synchronized (lock) {
            print(f);
            println();
        }
    }

    /**
     * Print a double, and then finish the line.
     * @param d the double to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void println(double d) throws IOException {
        synchronized (lock) {
            print(d);
            println();
        }
    }

    /**
     * Print an array of characters, and then finish the line.
     * @param c the array of characters to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void println(char[] c) throws IOException {
        synchronized (lock) {
            print(c);
            println();
        }
    }

    /**
     * Print a String, and then finish the line.
     * @param s the string to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void println(String s) throws IOException {
        synchronized (lock) {
            print(s);
            println();
        }
    }

    /**
     * Print an Object, and then finish the line.
     * @param obj the object to be printed
     * @throws IOException if an error occurred while writing to the stream
     */
    public void println(Object obj) throws IOException {
        synchronized (lock) {
            print(obj);
            println();
        }
    }

    private boolean autoFlush = false;

    /**
     * Line separator string.  This is the value of the line.separator
     * property at the moment that the stream was created.
     */
    private String lineSeparator;
}
