/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;

/**
 * A class to parse lines of words in a stream. Words may be unquoted sequences
 * of non-blank characters, or may be quoted strings. Comments can be introduced
 * by '#' and extend to the next newline character. Lines may be terminated
 * by newline, semicolon or a comment.
 */
public class LineParser
{
    /**
     * This exception is used to report problems while using a line parser.
     */
    public static class Fault extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        Fault(I18NResourceBundle i18n, String s, Object[] o) {
            super(i18n.getString(s, o));
        }
    }

    /**
     * Create a line parser, reading the data in a given file.
     * @param file the file to be read
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if there is some problem opening the file
     * or reading the initial characters of the file
     */
    public LineParser(File file)
        throws FileNotFoundException, IOException
    {
        this(file, new BufferedReader(new FileReader(file)));
    }

    /**
     * Create a line parser, reading data from an anonymous stream.
     * @param in the stream from which to read the data
     */
    public LineParser(Reader in)
    {
        this(null, in);
    }

    /**
     * Create a line parser, reading the data from a file for which a stream
     * has already been opened. The name of the file will be included in any
     * appropriate error messages.
     * @param file the file from which the data is being read
     * @param in the stream from which to read the data
     */
    private LineParser(File file, Reader in) {
        this.file = file;
        this.in = in;
        currLine = new Vector();
        lineNumber = 1;
        ch = ' ';
    }

    /**
     * Get the file being read, or null if it is not available.
     * @return the file being read, or null if not available
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the current line number within the stream being read.
     * @return the current line number within nthe stream being read
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Read the next line of words from the input stream.
     * @return the next line of words from the input stream
     * @throws LineParser.Fault if there is a problem reading the required data --
     * such as an unterminated string
     */
    public String[] readLine() throws Fault {
        try {
            while (ch != -1) {
                switch (ch) {
                case ' ':
                case '\t':
                    // end current word
                    endWord_nextCh();
                    break;

                case '\r':
                case '\n':
                    // end current word, return if curr line not empty
                    endWord_nextCh();
                    if (currLine.size() > 0)
                        return endLine();
                    break;

                case ';':
                    // end current word, return if curr line not empty
                    endWord_nextCh();
                    if (currLine.size() > 0)
                        return endLine();
                    break;

                case '#':
                    // skip to end of line
                    // return if curr line not empty
                    endWord_nextCh();
                    while (ch != -1 && ch != '\r' && ch != '\n')
                        nextCh();
                    if (currLine.size() > 0)
                        return endLine();
                    break;

                case '\\':
                    // read next character; if newline, skip whitespace
                    // else add next character uninterpreted
                    nextCh();
                    if (ch == '\r')
                        nextCh();
                    if (ch == '\n') {
                        nextCh();
                        while (ch == ' ' || ch == '\t')
                            nextCh();
                    }
                    else
                        append_nextCh();
                    break;

                case '"':
                case '\'':
                    readString((char) ch);
                    break;

                default:
                    append_nextCh();
                    break;

                }
            }

            // at end of file ...
            // flush last word found, if any
            if (currWord != null) {
                currLine.add(currWord.toString());
                currWord = null;
            }

            return (currLine.size() > 0 ? endLine() : null);
        }
        catch (IOException e) {
            throw new Fault(i18n, "lineParser.ioError",
                            new Object[] {new Integer(file == null ? 0 : 1),
                                          file,
                                          new Integer(lineNumber),
                                          e } );
        }
    }

    private void readString(char termCh) throws IOException, Fault {
        if (currWord == null)
            currWord = new StringBuffer();
        nextCh();
        while (ch != -1) {
            switch (ch) {
            case '\r':
            case '\n':
                throw new Fault(i18n, "lineParser.unterminatedString",
                                new Object[] {new Integer(file == null ? 0 : 1),
                                              file,
                                              new Integer(lineNumber) } );

            case '\\':
                nextCh();
                append_nextCh();
                break;

            default:
                if (ch == termCh) {
                    nextCh();
                    return;
                }
                else
                    append_nextCh();
            }
        }
    }

    private void append_nextCh() throws IOException {
        if (currWord == null)
            currWord = new StringBuffer();
        currWord.append((char) ch);
        nextCh();
    }

    private void endWord_nextCh() throws IOException {
        if (currWord != null) {
            //System.err.println("endWord_nextCh: `" + currWord + "'");
            currLine.add(currWord.toString());
            currWord = null;
        }
        nextCh();
    }

    private String[] endLine() {
        String[] line = new String[currLine.size()];
        currLine.copyInto(line);
        currLine.setSize(0);
        return line;
    }

    private void nextCh() throws IOException {
        ch = in.read();
        if (ch == '\n')
            lineNumber++;
    }

    private File file;
    private Reader in;
    private int ch;
    private int lineNumber;
    private StringBuffer currWord;
    private Vector currLine;

    private static final I18NResourceBundle i18n =
        I18NResourceBundle.getBundleForClass(LineParser.class);
}
