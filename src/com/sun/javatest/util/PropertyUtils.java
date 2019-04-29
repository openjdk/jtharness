/*
 * $Id$
 *
 * Copyright (c) 1996, 2010, Oracle and/or its affiliates. All rights reserved.
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


import java.io.*;
import java.util.*;

/**
 * A collection of utility methods related to {@code java.util.Properties} loading, saving and transformation.
 */
public class PropertyUtils {

    /**
     * Utility method that writes the given map of strings to the given output stream with provided comments
     * using {@code java.util.Properties.store(java.io.OutputStream, String)} method.
     */
    public static void store(Map<String, String> stringProps, OutputStream out, String comments) throws IOException {
        java.util.Properties properties = new java.util.Properties();
        properties.putAll(stringProps);
        properties.store(out, comments);
    }

    /**
     * Reads a property list from the given input stream
     * using {@code java.util.Properties.load(java.io.InputStream inStream)} method
     * and stores properties into a {@code Map<String, String>} that is returned.
     */
    public static Map<String, String> load(InputStream inputStream) throws IOException {
        return populate(new HashMap<String, String>(), inputStream);
    }

    /**
     * Reads a property list with the given reader
     * using {@code java.util.Properties.load(java.io.Reader reader)} method
     * and stores properties into a {@code Map<String, String>} that is returned.
     */
    public static Map<String, String> load(Reader reader) throws IOException {
        return load0(new HashMap<String, String>(), new LineReader(reader));
    }

    /**
     * Reads a property list from the given input stream
     * using {@code java.util.Properties.load(java.io.InputStream inStream)} method
     * and stores string properties into a {@code SortedMap<String, String>} that is returned.
     */
    public static SortedMap<String, String> loadSorted(InputStream inputStream) throws IOException {
        return populate(new TreeMap<String, String>(), inputStream);
    }

    /**
     * Populated a map of strings using {@code java.util.Properties.load(java.io.InputStream)} method
     */
    private static <M extends Map<String, String>> M populate(M stringProps, InputStream inputStream) throws IOException {
        return load0(stringProps, new LineReader(inputStream));
    }

    /**
     * Converts the given properties to {@code Map<String, String>} instance
     * picking only string properties from the given {@code java.util.Properties} instance.
     */
    public static Map<String, String> convertToStringProps(java.util.Properties properties) {
        return extractStringPropsTo(new HashMap<String, String>(), properties);
    }

    private static <M extends Map<String, String>> M extractStringPropsTo(M stringProps, java.util.Properties p) {
        for (String name : p.stringPropertyNames()) {
            stringProps.put(name, p.getProperty(name));
        }
        return stringProps;
    }

    static Vector<String> load0(Reader in, boolean breakOnEmptyLine) throws IOException {

// This could be used if we switch to JDK 1.6, where Properties support I/O
// through Reader and Writer
//        StringBuffer buff = new StringBuffer();
//        int lineCount = 0;
//        int ch = '\n';
//
//        while (ch >= 0) {
//            buff.append((char)ch);
//            if (ch == '\n') {
//                if (++lineCount > 1) {
//                    break;
//                }
//            }
//            else if (ch != '\r' && ch != '\t' && ch != ' ') {
//                lineCount = 0;
//            }
//            ch = in.read();
//        }
//        Reader r = new StringReader(buff.toString());
//        java.util.Properties props = new java.util.Properties();
//        props.load(r);
//
//        return getArray(props);
//
//
//

        Vector<String> v = new Vector<>();
        int ch = 0;

        //// eat any preceding whitespace
        //do {
        //    ch = in.read();
        //} while ((ch == '\n') || (ch == '\r') || (ch == '\t') || (ch == ' '));

        ch = '\n';
        // pretend to have read newline, so that if an initial
        // blank line is read, it is treated as the end of an empty props object

        parsing:
        while (true) {
            // parse until EOF or a blank line
            switch (ch) {
                case -1:
                    // EOF
                    break parsing;

                case '#':
                case '!':
                    // comment: skip to end of line
                    do {
                        ch = in.read();
                    } while ((ch >= 0) && (ch != '\n') && (ch != '\r'));
                    continue;

                case '\n':
                case '\t':
                case '\r':
                case ' ':
                    // skip whitespace but count newlines along the way;
                    // if there is more than one, we're done. This is intended
                    // to work on both \n and \r\n systems
                    int lines = 0;
                    do {
                        if (breakOnEmptyLine && ch == '\n' && (++lines > 1)) {
                            break parsing;
                        }
                        ch = in.read();
                    } while ((ch == '\n') || (ch == '\r') || (ch == '\t') || (ch == ' '));
                    continue;

                default:


//               key=value
//               start by reading the key; stop at newline (unless escaped in value)
                    StringBuilder key = new StringBuilder();
                    StringBuilder val = new StringBuilder();

                    boolean hasSep = false;
                    boolean precedingBackslash = false;
                    while (ch >= 0 && (ch != '\n') && (ch != '\r')) {
                        //need check if escaped.
                        if ((ch == '=' || ch == ':') && !precedingBackslash) {
//                        valueStart = keyLen + 1;
                            ch = in.read();
                            hasSep = true;
                            break;
                        } else if ((ch == ' ' || ch == '\t' || ch == '\f') && !precedingBackslash) {
//                        valueStart = keyLen + 1;
                            ch = in.read();
                            break;
                        }
                        if (ch == '\\') {
                            precedingBackslash = !precedingBackslash;
                        } else {
                            precedingBackslash = false;
                        }
//                    keyLen++;
                        key.append((char) ch);
                        ch = in.read();
                    }

                    // handle multi-char separator: prop  =    value
                    while (ch >= 0 && (ch != '\n') && (ch != '\r')) {
//                    c = lr.lineBuf[valueStart];
                        if (ch != ' ' && ch != '\t' && ch != '\f') {
                            if (!hasSep && (ch == '=' || ch == ':')) {
                                hasSep = true;
                            } else {
                                break;
                            }
                        }
//                    valueStart++;
                        ch = in.read();
                    }
//                while (ch >=0 && (ch != '\n') && (ch != '\r')) {
//                    val.append((char)ch);
//                    ch = in.read();
//                }

                    // Read the value
                    while ((ch >= 0) && (ch != '\n') && (ch != '\r')) {
                        if (ch == '\\') {
                            switch (ch = in.read()) {
                                case '\r':
                                    if (((ch = in.read()) == '\n') ||
                                            (ch == ' ') || (ch == '\t')) {
                                        // fall thru to '\n' case
                                    } else {
                                        continue;
                                    }
                                case '\n':
                                    while (((ch = in.read()) == ' ') || (ch == '\t')) {
                                        ;
                                    }
                                    continue;
                                default:
                                    val.append('\\');
                                    break;
                            }
                        }
                        val.append((char) ch);
                        ch = in.read();
                    }

                    char[] cKey = new char[key.length()];
                    char[] cVal = new char[val.length()];
                    key.getChars(0, key.length(), cKey, 0);
                    val.getChars(0, val.length(), cVal, 0);

                    v.add(loadConvert(cKey));
                    v.add(loadConvert(cVal));

            }
        }

        return v;
    }


    private static String loadConvert(char... in/*, int off, int len, char[] convtBuf*/) {
//        if (convtBuf.length < len) {
//            int newLen = len * 2;
//            if (newLen < 0) {
//              newLen = Integer.MAX_VALUE;
//          }
//          convtBuf = new char[newLen];
//        }
//        char aChar;
//        char[] out = convtBuf;
//        int outLen = 0;
//        int end = off + len;
        char aChar;
        int len = in.length;
        char[] out = new char[len];
        int outLen = 0;
        int off = 0;

        while (off < len) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed \\uxxxx encoding.");
                        }
                    }
                    out[outLen++] = (char) value;
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = aChar;
            }
        }
        return new String(out, 0, outLen);
    }


    // Copied from JDK 1.6
    static String saveConvert(String theString,
                              boolean escapeSpace,
                              boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuilder outBuffer = new StringBuilder(bufLen);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(' ');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\');
                    outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex(aChar & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Convert a nibble to a hex character
     *
     * @param nibble the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[nibble & 0xF];
    }

    /**
     * A table of hex digits
     */
    private static char[] hexDigit = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static <M extends Map<String, String>> M load0(M map, LineReader lr) throws IOException {
        char[] convtBuf = new char[1024];
        int limit;
        int keyLen;
        int valueStart;
        char c;
        boolean hasSep;
        boolean precedingBackslash;

        while ((limit = lr.readLine()) >= 0) {
            c = 0;
            keyLen = 0;
            valueStart = limit;
            hasSep = false;

            //System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
            precedingBackslash = false;
            while (keyLen < limit) {
                c = lr.lineBuf[keyLen];
                //need check if escaped.
                if ((c == '=' || c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                } else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < limit) {
                c = lr.lineBuf[valueStart];
                if (c != ' ' && c != '\t' && c != '\f') {
                    if (!hasSep && (c == '=' || c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
            String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
            map.put(key, value);
        }
        return map;
    }


    /*
     * Converts encoded &#92;uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    private static String loadConvert(char[] in, int off, int len, char... convtBuf) {
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convtBuf = new char[newLen];
        }
        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed \\uxxxx encoding.");
                        }
                    }
                    out[outLen++] = (char) value;
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = aChar;
            }
        }
        return new String(out, 0, outLen);
    }


    /* Read in a "logical line" from an InputStream/Reader, skip all comment
     * and blank lines and filter out those leading whitespace characters
     * (\u0020, \u0009 and \u000c) from the beginning of a "natural line".
     * Method returns the char length of the "logical line" and stores
     * the line in "lineBuf".
     */
    private static class LineReader {
        public LineReader(InputStream inStream) {
            this.inStream = inStream;
            inByteBuf = new byte[8192];
        }

        public LineReader(Reader reader) {
            this.reader = reader;
            inCharBuf = new char[8192];
        }

        byte[] inByteBuf;
        char[] inCharBuf;
        char[] lineBuf = new char[1024];
        int inLimit = 0;
        int inOff = 0;
        InputStream inStream;
        Reader reader;

        int readLine() throws IOException {
            int len = 0;
            char c = 0;

            boolean skipWhiteSpace = true;
            boolean isCommentLine = false;
            boolean isNewLine = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
            boolean skipLF = false;

            while (true) {
                if (inOff >= inLimit) {
                    inLimit = (inStream == null) ? reader.read(inCharBuf)
                            : inStream.read(inByteBuf);
                    inOff = 0;
                    if (inLimit <= 0) {
                        if (len == 0 || isCommentLine) {
                            return -1;
                        }
                        if (precedingBackslash) {
                            len--;
                        }
                        return len;
                    }
                }
                if (inStream != null) {
                    //The line below is equivalent to calling a
                    //ISO8859-1 decoder.
                    c = (char) (inByteBuf[inOff++] & 0xFF);
                } else {
                    c = inCharBuf[inOff++];
                }
                if (skipLF) {
                    skipLF = false;
                    if (c == '\n') {
                        continue;
                    }
                }
                if (skipWhiteSpace) {
                    if (c == ' ' || c == '\t' || c == '\f') {
                        continue;
                    }
                    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                        continue;
                    }
                    skipWhiteSpace = false;
                    appendedLineBegin = false;
                }
                if (isNewLine) {
                    isNewLine = false;
                    if (c == '#' || c == '!') {
                        // Comment, quickly consume the rest of the line,
                        // resume on line-break and backslash.
                        if (inStream != null) {
                            while (inOff < inLimit) {
                                byte b = inByteBuf[inOff++];
                                if (b == '\n' || b == '\r' || b == '\\') {
                                    c = (char) (b & 0xFF);
                                    break;
                                }
                            }
                        } else {
                            while (inOff < inLimit) {
                                c = inCharBuf[inOff++];
                                if (c == '\n' || c == '\r' || c == '\\') {
                                    break;
                                }
                            }
                        }
                        isCommentLine = true;
                    }
                }

                if (c != '\n' && c != '\r') {
                    lineBuf[len++] = c;
                    if (len == lineBuf.length) {
                        int newLength = lineBuf.length * 2;
                        if (newLength < 0) {
                            newLength = Integer.MAX_VALUE;
                        }
                        char[] buf = new char[newLength];
                        System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
                        lineBuf = buf;
                    }
                    //flip the preceding backslash flag
                    if (c == '\\') {
                        precedingBackslash = !precedingBackslash;
                    } else {
                        precedingBackslash = false;
                    }
                } else {
                    // reached EOL
                    if (isCommentLine || len == 0) {
                        isCommentLine = false;
                        isNewLine = true;
                        skipWhiteSpace = true;
                        len = 0;
                        continue;
                    }
                    if (inOff >= inLimit) {
                        inLimit = (inStream == null)
                                ? reader.read(inCharBuf)
                                : inStream.read(inByteBuf);
                        inOff = 0;
                        if (inLimit <= 0) {
                            if (precedingBackslash) {
                                len--;
                            }
                            return len;
                        }
                    }
                    if (precedingBackslash) {
                        len -= 1;
                        //skip the leading whitespace characters in following line
                        skipWhiteSpace = true;
                        appendedLineBegin = true;
                        precedingBackslash = false;
                        if (c == '\r') {
                            skipLF = true;
                        }
                    } else {
                        return len;
                    }
                }
            }
        }
    }

}
