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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class implements an output stream that has additional methods for
 * printing. It is similar to java.io.PrintStream except that it does not swallow
 * exceptions.
 */
public class TextStream extends FilterOutputStream
{
    /**
     * Creates a new TextStream.
     * @param out       the output stream
     */
    public TextStream(OutputStream out) {
        super(out);
    }

    /**
     * Creates a new TextStream.
     * @param out       the output stream
     * @param autoflush set to true to flush the stream after each newline character
     * is written
     */
    public TextStream(OutputStream out, boolean autoflush) {
        super(out);
        this.autoflush = autoflush;
    }

    /**
     * Prints a String.
     * @param s the String to be printed
     * @throws IOException if there is a problem writing to the stream
     */
    synchronized public void print(String s) throws IOException {
        if (s == null) {
            s = "null";
        }

        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            char c = s.charAt(i);
            write(c);
            if (autoflush && c == '\n')
                out.flush();
        }
    }

    /**
     * Prints a newline.
     * @throws IOException if there is a problem writing to the stream
     */
    public void println() throws IOException {
        write('\n');
    }

    /**
     * Prints a string followed by a newline.
     * @param s the String to be printed
     * @throws IOException if there is a problem writing to the stream
     */
    synchronized public void println(String s) throws IOException {
        print(s);
        write('\n');
    }

    private boolean autoflush;
}
