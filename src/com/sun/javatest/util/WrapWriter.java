/*
 * $Id$
 *
 * Copyright (c) 2004, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A writer that will automatically word wrap lines to fit within
 * left and right margins.
 */
public class WrapWriter extends Writer
{
    /**
     * Create a WrapWriter object that will write to a given stream.
     * @param out the stream to which the WrapWriter will write
     */
    public WrapWriter(OutputStream out) {
        this.out = new OutputStreamWriter(out);
    }

    /**
     * Create a WrapWriter object that will write to a given stream.
     * @param out the stream to which the WrapWriter will write
     */
    public WrapWriter(Writer out) {
        this.out = out;
    }

    /**
     * Set the position for the left margin for the text stream.
     * @param m the position for the left margin
     * @throws IllegalArgumentException if the value is negative or
     * greater than the current value of the right margin
     * @see #getLeftMargin
     */
    public void setLeftMargin(int m) {
        if (m < 0 || m >= rightMargin)
            throw new IllegalArgumentException();
        leftMargin = m;
    }

    /**
     * Get the position for the left margin for the text stream.
     * @return the position for the left margin
     * @see #setLeftMargin
     */
    public int getLeftMargin() {
        return leftMargin;
    }

    /**
     * Set the position for the right margin for the text stream.
     * @param m the position for the right margin
     * @throws IllegalArgumentException if the value is
     * less than the current value of the left margin
     * @see #getRightMargin
     */
    public void setRightMargin(int m) {
        if (m <= leftMargin)
            throw new IllegalArgumentException();
        rightMargin = m;
    }

    /**
     * Get the position for the right margin for the text stream.
     * @return the position for the right margin
     * @see #setRightMargin
     */
    public int getRightMargin() {
        return rightMargin;
    }

    /**
     * Get the number of characters that have been written so far
     * on the current line. This will not include any characters in
     * the last word that have not yet been written. Use flush() or
     * write a white space character to force out the last word
     * written.
     * @return the number of characters that have been written
     * so far on the line
     */
    public int getCharsOnLineSoFar() {
        return charsOnLineSoFar;
    }

    public void close() throws IOException {
        flush();
        out.close();
    }

    public void flush() throws IOException {
        write(' ');
        out.flush();
    }

    public void write(char cbuf[], int off, int len) throws IOException {
        for (int i = off; i < off + len; i++)
            write(cbuf[i]);
    }

    /**
     * Write a character to the stream. Non-white-space characters will be buffered
     * until a white space character is written. When a white space character is
     * written, the buffered characters will be written on the current line, if
     * they will fit before the right margin, otherwise a newline and spaces will be
     * inserted so that the buffered characters appear on the next line starting at
     * the left margin.
     * @param c the character to be written
     * @throws IOException if there is a problem writing to the underlying stream
     */
    public void write(char c) throws IOException {
        if (Character.isWhitespace(c)) {
            if (pending.length() > 0) {
                // if writing the pending text would overrun the margin,
                // wrap to a new line
                if (Math.max(leftMargin, charsOnLineSoFar + 1) + pending.length() > rightMargin)
                    newLine();

                // if the pending text is the first on the line, space to
                // the left margin, otherwise just write a space to separate
                // from the new text from previously written text
                if (charsOnLineSoFar < leftMargin) {
                    for (int i = charsOnLineSoFar; i < leftMargin; i++)
                        out.write(' ');
                    charsOnLineSoFar = leftMargin;
                }
                else if (charsOnLineSoFar > leftMargin) {
                    out.write(' ');
                    charsOnLineSoFar++;
                }

                out.write(pending.toString());
                charsOnLineSoFar += pending.length();
                pending.setLength(0);
            }

            if (c == '\n') {
                newLine();
            }
            else if (c == '\t') {
                out.write(' ');
                charsOnLineSoFar++;
                while (charsOnLineSoFar % 8 != 0) {
                    out.write(' ');
                    charsOnLineSoFar++;
                }
            }
        }
        else
            pending.append(c);
    }

    private void newLine() throws IOException {
        out.write(lineSeparator);
        charsOnLineSoFar = 0;
    }

    private Writer out;
    private int leftMargin = 0;
    private int rightMargin = Integer.getInteger("javatest.console.width", 80).intValue();
    private StringBuffer pending = new StringBuffer();
    private int charsOnLineSoFar = 0;
    private String lineSeparator = System.getProperty("line.separator", "\n");
}
