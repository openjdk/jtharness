/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Vector;

/**
 * A class to convert to and from a single string with space separated
 * substrings.
 */
public class StringArray
{
    /**
     * Convert a single whitespace separated string into it's component
     * substrings.  The substrings can be separated by one or more instances of
     * a space (' '), tab ('\t') or newline ('\n').
     * @param s The string to break apart.  A null string will not
     *          cause an error.
     * @return Provides the substrings of the given parameter.  If the
     *         provided string was null or all whitespace, an
     *         empty array (length==0) is returned.
     */
    public static String[] split(String s) {
        if (s == null)
            return empty;

        Vector v = new Vector();
        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            if (white(s.charAt(i))) {
                if (start != -1)
                    v.addElement(s.substring(start, i));
                start = -1;
            } else
                if (start == -1)
                    start = i;
        }
        if (start != -1)
            v.addElement(s.substring(start));
        if (v.size() == 0)
            return empty;
        String[] a = new String[v.size()];
        v.copyInto(a);
        return a;
    }

    /**
     * Converts an array of strings into a single space separated string.
     * The strings are appended to the resulting string in ascending index
     * order, left to right.  A single space character is used to delimit the
     * values in the resulting string.
     * @param ss The strings which should be concatenated together.  A zero
     *           length or null value will not cause an error.
     * @return The string which is made up of all the strings provided.
     *         The return value is a zero length string if the input value
     *         was null or zero length.
     */
    public static String join(String[] ss) {
        return join(ss, " ");
    }

    /**
     * Converts an array of strings into a single string, * using a specified
     * separator.
     * The strings are appended to the resulting string in
     * ascending index order, left to right.  A specified separator is used to
     * delimit the values in the resulting string.
     * @param ss The strings which should be concatenated together.  A zero
     *           length or null value will not cause an error.
     * @param sep The separator to place between the elements of the string
     *          array in the concatenated result
     * @return The string which is made up of all the strings provided.
     *         The return value is a zero length string if the input value
     *         was null or zero length.
     */
    public static String join(String[] ss, String sep) {
        if (ss == null || ss.length == 0)
            return "";

        int l = (ss.length - 1) * sep.length();
        for (int i = 0; i < ss.length; i++)
            l += (ss[i] == null ? 0 : ss[i].length());

        StringBuffer sb = new StringBuffer(l);
        sb.append(ss[0]);
        for (int i = 1; i < ss.length; i++) {
            sb.append(sep);
            sb.append(ss[i]);
        }

        return sb.toString();
    }

    /**
     * Split up a comma separated list of values.
     * Whitespace after each delimiter is removed.
     * @param list The string to parse for items.  Null or zero
     *             length strings ok.
     * @param delim The string which separates items in the list.
     *              Must be non-null and have a length greater than
     *              zero.
     * @return The extracted items from the list.  Will only be
     *         null if the input string is null or zero length.
     */
    public static String[] splitList(String list, String delim) {
        if (list == null || list.length() == 0)
            return null;

        Vector v = new Vector();
        int pos = 0;
        while (true) {
            int nextD = list.indexOf(delim, pos);

            if (nextD != -1) {
                v.addElement(list.substring(pos, nextD));
                pos = nextD + delim.length();
                pos = skipWhite(list, pos);
            }
            else
                break;
        }

        if (pos < list.length())
            v.addElement(list.substring(pos));

        if (v.size() == 0)
            return new String[0];

        String[] a = new String[v.size()];
        v.copyInto(a);
        return a;
    }

    /**
     * Does the string array contain the target string?
     * Since the list is assumed to have no particular structure, the performance of this
     * search is O(n).
     * @param list The list of strings to search, null is ok.
     * @return True if it does, false otherwise.  Will the false if the list was null.
     */
    public static boolean contains(String[] list, String target) {
        if (list == null || list.length == 0)
            return false;

        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(target))
                return true;
        }   // for

        return false;
    }

    /**
     * Does the list contain the target string?
     * This method is string parsing intensive, use with caution.
     * @return True if it does, false otherwise.
     */
    public static boolean contains(String list, String target) {
        return contains(split(list), target);
    }

    /**
     * Find the index of the next non-whitespace character
     * in the string.
     */
    private static int skipWhite(String s, int start) {
        for (int i = start; i < s.length(); i++)
            if (!white(s.charAt(i)))
                return i;

        return s.length();
    }

    private static boolean white(char c) {
        return (c == ' '  ||  c == '\t'  ||  c == '\n');
    }

    private static final String[] empty = { };
}
