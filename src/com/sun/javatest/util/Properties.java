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

// Modified from JDK java.util.Properties to use Reader/Writer as well as
// InputStream/OutputStream

import java.io.BufferedWriter;
import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The <code>Properties</code> class represents a persistent set of
 * properties. The <code>Properties</code> can be saved to a stream
 * or loaded from a stream. Each key and its corresponding value in
 * the property list is a string.
 * <p>
 * A property list can contain another property list as its
 * "defaults"; this second property list is searched if
 * the property key is not found in the original property list.
 *
 * <p>
 * Modified from JDK java.util.Properties to use Reader/Writer as well as
 * InputStream/OutputStream, to fix locale/codepage problems.
 */
public
class Properties extends Hashtable {
    /**
     * A property list that contains default values for any keys not
     * found in this property list.
     *
     * @since   JDK1.0
     */
    protected Properties defaults;

    /**
     * Creates an empty property list with no default values.
     *
     * @since   JDK1.0
     */
    public Properties() {
        this(null);
    }

    /**
     * Creates an empty property list with the specified defaults.
     *
     * @param   defaults   the defaults.
     * @since   JDK1.0
     */
    public Properties(Properties defaults) {
        this.defaults = defaults;
    }

//    /**
//     * Reads a property list from an input stream.
//     *
//     * @param      in   the input stream.
//     * @exception  IOException  if an error occurred when reading from the
//     *               input stream.
//     * @since   JDK1.0
//     */
//    public synchronized void load(InputStream in) throws IOException {
//      in = Runtime.getRuntime().getLocalizedInputStream(in);
//      load(new InputStreamReader(in));
//    }


    /**
     * Reads a property list from an input stream.
     *
     * @param      in   the input stream.
     * @exception  IOException  if an error occurred when reading from the
     *               input stream.
     */
    public synchronized void load(Reader in) throws IOException {
        Vector v = load0(in, false);
        for (int i = 0; i < v.size(); i+=2) {
            put(v.elementAt(i), v.elementAt(i + 1));
        }
    }


    static Vector load0(Reader in, boolean breakOnEmptyLine) throws IOException {

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

        Vector v = new Vector();
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
                    if (breakOnEmptyLine && ch == '\n' && (++lines > 1))
                        break parsing;
                    ch = in.read();
                } while ((ch == '\n') || (ch == '\r') || (ch == '\t') || (ch == ' '));
                continue;

            default:



//               key=value
//               start by reading the key; stop at newline (unless escaped in value)
                StringBuffer key = new StringBuffer();
                StringBuffer val = new StringBuffer();

                boolean hasSep = false;
                boolean precedingBackslash = false;
                while (ch >=0 && (ch != '\n') && (ch != '\r')) {
                    //need check if escaped.
                    if ((ch == '=' ||  ch == ':') && !precedingBackslash) {
//                        valueStart = keyLen + 1;
                        ch = in.read();
                        hasSep = true;
                        break;
                    } else if ((ch == ' ' || ch == '\t' ||  ch == '\f') && !precedingBackslash) {
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
                    key.append((char)ch);
                    ch = in.read();
                }

                // handle multi-char separator: prop  =    value
                while (ch >=0 && (ch != '\n') && (ch != '\r')) {
//                    c = lr.lineBuf[valueStart];
                    if (ch != ' ' && ch != '\t' &&  ch != '\f') {
                        if (!hasSep && (ch == '=' ||  ch == ':')) {
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
                        } else continue;
                      case '\n':
                        while (((ch = in.read()) == ' ') || (ch == '\t'));
                        continue;
                      default:
                          val.append('\\');
                          break;
                    }
                }
                val.append((char)ch);
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




    private static String loadConvert (char[] in/*, int off, int len, char[] convtBuf*/) {
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
                if(aChar == 'u') {
                    // Read the xxxx
                    int value=0;
                    for (int i=0; i<4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                          case '0': case '1': case '2': case '3': case '4':
                          case '5': case '6': case '7': case '8': case '9':
                             value = (value << 4) + aChar - '0';
                             break;
                          case 'a': case 'b': case 'c':
                          case 'd': case 'e': case 'f':
                             value = (value << 4) + 10 + aChar - 'a';
                             break;
                          case 'A': case 'B': case 'C':
                          case 'D': case 'E': case 'F':
                             value = (value << 4) + 10 + aChar - 'A';
                             break;
                          default:
                              throw new IllegalArgumentException(
                                           "Malformed \\uxxxx encoding.");
                        }
                     }
                    out[outLen++] = (char)value;
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = (char)aChar;
            }
        }
        return new String (out, 0, outLen);
    }






//    /**
//     * Stores this property list to the specified output stream. The
//     * string header is printed as a comment at the beginning of the stream.
//     *
//     * @param   out      an output stream.
//     * @param   header   a description of the property list.
//     * @since   JDK1.0
//     */
//    public synchronized void save(OutputStream out, String header) {
//      OutputStream localOut = Runtime.getRuntime().getLocalizedOutputStream(out);
//      boolean localize = localOut != out;
//      save(new OutputStreamWriter(localOut), header, localize);
//    }

    /**
     * Stores this property list to the specified output stream. The
     * string header is printed as a comment at the beginning of the stream.
     *
     * @param   out      an output stream.
     * @param   header   a description of the property list.
     * @since   JDK1.0
     */
    public synchronized void save(Writer out, String header) throws IOException {
        // From JDK 1.6 java.util.Properties
        BufferedWriter bout = (out instanceof BufferedWriter)?(BufferedWriter)out
                                                 : new BufferedWriter(out);

        PrintWriter prnt = new PrintWriter(bout);
        if (header != null) {
            prnt.write('#');
            prnt.println(header);
        }
        prnt.write('#');
        prnt.println(new Date());

        for (Enumeration e = keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            String val = (String)get(key);
            key = saveConvert(key, true, false);
            /* No need to escape embedded and trailing spaces for value, hence
             * pass false to flag.
             */
            val = saveConvert(val, false, false);
            bout.write(key + "=" + val);
            bout.newLine();
        }
        bout.flush();
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
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\'); outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch(aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                          break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                          break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                          break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                          break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\'); outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode ) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }


    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * <code>null</code> if the property is not found.
     *
     * @param   key   the property key.
     * @return  the value in this property list with the specified key value.
     * @see     com.sun.javatest.util.Properties#defaults
     * @since   JDK1.0
     */
    public String getProperty(String key) {
        Object oval = super.get(key);
        String sval = (oval instanceof String) ? (String)oval : null;
        return ((sval == null) && (defaults != null)) ? defaults.getProperty(key) : sval;
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns the
     * default value argument if the property is not found.
     *
     * @param   key            the hashtable key.
     * @param   defaultValue   a default value.
     *
     * @return  the value in this property list with the specified key value.
     * @see     com.sun.javatest.util.Properties#defaults
     * @since   JDK1.0
     */
    public String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return (val == null) ? defaultValue : val;
    }

    /**
     * Returns an enumeration of all the keys in this property list, including
     * the keys in the default property list.
     *
     * @return  an enumeration of all the keys in this property list, including
     *          the keys in the default property list.
     * @see     java.util.Enumeration
     * @see     com.sun.javatest.util.Properties#defaults
     * @since   JDK1.0
     */
    public Enumeration propertyNames() {
        Hashtable h = new Hashtable();
        enumerate(h);
        return h.keys();
    }

//    /**
//     * Prints this property list out to the specified output stream.
//     * This method is useful for debugging.
//     *
//     * @param   out   an output stream.
//     * @since   JDK1.0
//     */
//    public void list(PrintStream out) {
//      list(new PrintWriter(out));
//    }

    /**
     * Prints this property list out to the specified output stream.
     * This method is useful for debugging.
     *
     * @param   out   an output stream.
     * @since   JDK1.1
     */
    /*
     * Rather than use an anonymous inner class to share common code, this
     * method is duplicated in order to ensure that a non-1.1 compiler can
     * compile this file.
     */
    public void list(PrintWriter out) {
        out.println("-- listing properties --");
        Hashtable h = new Hashtable();
        enumerate(h);
        for (Enumeration e = h.keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            String val = (String)h.get(key);
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    /**
     * Enumerates all key/value pairs in the specified hastable.
     * @param h the hashtable
     */
    private synchronized void enumerate(Hashtable h) {
        if (defaults != null) {
            defaults.enumerate(h);
        }
        for (Enumeration e = keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            h.put(key, get(key));
        }
    }

    /**
     * Convert a nibble to a hex character
     * @param   nibble  the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static char[] hexDigit = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };
}
