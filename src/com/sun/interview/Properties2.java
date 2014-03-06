/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

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
 * This class is similar to java.util.Properties, but has upgraded
 * capabilities.
 */

public class Properties2 extends Hashtable {
    /**
     * A property list that contains default values for any keys not
     * found in this property list.
     */
    protected Properties2 defaults;

    /**
     * Creates an empty property list with no default values.
     */
    public Properties2() {
        this(null);
    }

    /**
     * Creates an empty property list with the specified defaults.
     * @param   defaults   the defaults.
     */
    public Properties2(Properties2 defaults) {
        this.defaults = defaults;
    }

    public void load(java.util.Properties source) {
        Enumeration e = source.propertyNames();
        while(e.hasMoreElements()) {
            Object next = e.nextElement();
            put( ((String)next), source.get(next) );
        }   // while
    }


    /**
     * Reads a property list from an input stream.
     *
     * @param      in   the input stream.
     * @exception  IOException  if an error occurred when reading from the
     *               input stream.
     */
    public synchronized void load(Reader in) throws IOException {
        int ch = in.read();
        while (true) {
            switch (ch) {
              case -1:
                return;

              case '#':
              case '!':
                do {
                    ch = in.read();
                } while ((ch >= 0) && (ch != '\n') && (ch != '\r'));
                continue;

              case '\n':
              case '\r':
              case ' ':
              case '\t':
                ch = in.read();
                continue;
            }

            // Read the key
            StringBuffer key = new StringBuffer();
            while ((ch >= 0) && (ch != '=') && (ch != ':') &&
                   (ch != ' ') && (ch != '\t') && (ch != '\n') && (ch != '\r')) {
                key.append((char)ch);
                ch = in.read();
            }
            while ((ch == ' ') || (ch == '\t')) {
                ch = in.read();
            }
            if ((ch == '=') || (ch == ':')) {
                ch = in.read();
            }
            while ((ch == ' ') || (ch == '\t')) {
                ch = in.read();
            }

            // Read the value
            StringBuffer val = new StringBuffer();
            while ((ch >= 0) && (ch != '\n') && (ch != '\r')) {
                int next = 0;
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
                      case 't': ch = '\t'; next = in.read(); break;
                      case 'n': ch = '\n'; next = in.read(); break;
                      case 'r': ch = '\r'; next = in.read(); break;
                      case 'u': {
                        while ((ch = in.read()) == 'u');
                        int d = 0;
                      loop:
                        for (int i = 0 ; i < 4 ; i++) {
                            next = in.read();
                            switch (ch) {
                              case '0': case '1': case '2': case '3': case '4':
                              case '5': case '6': case '7': case '8': case '9':
                                d = (d << 4) + ch - '0';
                                break;
                              case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                                d = (d << 4) + 10 + ch - 'a';
                                break;
                              case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                                d = (d << 4) + 10 + ch - 'A';
                                break;
                              default:
                                break loop;
                            }
                            ch = next;
                        }
                        ch = d;
                        break;
                      }
                      default: next = in.read(); break;
                    }
                } else {
                    next = in.read();
                }
                val.append((char)ch);
                ch = next;
            }

            //System.out.println(key + " = '" + val + "'");
            put(key.toString(), val.toString());
        }
    }

    /**
     * Stores this property list to the specified output stream. The
     * string header is printed as a comment at the beginning of the stream.
     *
     * @param   out      an output stream.
     * @param   header   a description of the property list.
     */
    public synchronized void save(Writer out, String header) {
        save(out, header, false);
    }

    private void save(Writer out, String header, boolean localize) {
        PrintWriter prnt = (out instanceof PrintWriter ? (PrintWriter)out :
                            new PrintWriter(out, false));

        if (header != null) {
            prnt.write('#');
            prnt.println(header);
        }
        prnt.write('#');
        prnt.println(new Date());

        for (Enumeration e = keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            prnt.print(key);
            prnt.write('=');

            String val = (String)get(key);
            int len = val.length();
            boolean empty = false;

            for (int i = 0 ; i < len ; i++) {
                int ch = val.charAt(i);

                switch (ch) {
                  case '\\': prnt.write('\\'); prnt.write('\\'); break;
                  case '\t': prnt.write('\\'); prnt.write('t'); break;
                  case '\n': prnt.write('\\'); prnt.write('n'); break;
                  case '\r': prnt.write('\\'); prnt.write('r'); break;

                  default:
                    if ((ch < ' ') || (ch >= 127) || (empty && (ch == ' '))) {
                        if ((ch > 255) && localize) {
                            prnt.write(ch);
                        } else {
                            prnt.write('\\');
                            prnt.write('u');
                            prnt.write(toHex((ch >> 12) & 0xF));
                            prnt.write(toHex((ch >>  8) & 0xF));
                            prnt.write(toHex((ch >>  4) & 0xF));
                            prnt.write(toHex((ch >>  0) & 0xF));
                        }
                    } else {
                        prnt.write(ch);
                    }
                }
                empty = false;
            }
            prnt.write('\n');
        }
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * <code>null</code> if the property is not found.
     *
     * @param   key   the property key.
     * @return  the value in this property list with the specified key value.
     * @see     com.sun.interview.Properties2#defaults
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
     * @see     com.sun.interview.Properties2#defaults
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
     * @see     com.sun.interview.Properties2#defaults
     */
    public Enumeration propertyNames() {
        Hashtable h = new Hashtable();
        enumerate(h);
        return h.keys();
    }


    /**
     * Prints this property list out to the specified output stream.
     * This method is useful for debugging.
     *
     * @param   out   an output stream.
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
