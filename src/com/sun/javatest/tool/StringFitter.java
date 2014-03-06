/*
 * $Id$
 *
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.tool;

import java.awt.Component;
import java.awt.FontMetrics;
import javax.swing.JLabel;

public class StringFitter {
    // a string for replacing truncated parts in the beginning of the string
    private String leftReplaceString = "...";
    // width of leftReplaceString in current FontMetrics
    private int leftRSWidth;
    // a string for replacing truncated parts in the ending of the string
    private String rightReplaceString = "...";
    // width of leftReplaceString in current FontMetrics
    private int rightRSWidth;
    // a string for replacing truncated parts in the middle of the string
    // it equals leftReplaceString in case leftReplaceString equals rightReplaceString
    // and in other case it equals leftReplaceString+rightReplaceString
    private String middleReplaceString = "...";
    // width of middleReplaceString in current FontMetrics
    private int middleRSWidth;
    // a string which replaces truncated parts
    private String splitString = "...";
    // width of splitString in current FontMetrics
    private int splitWidth;
    // a default StringFitter
    private static StringFitter defaultInstance;
    // current FontMetrics
    private FontMetrics fm;

    /**
     * Creates default StringFitter. Uses current system FontMetrics, "..." for all
     * replace strings and "" for split string
     */
    public StringFitter() {
        this(null, "...", "...", "");
    }

    /**
     * Creates a StringFitter with universal replace string and specified split string.
     * Uses current system FontMetrics.
     * @param replaceString an universal string for replacing truncated parts. It is
     * used to replace beginning, ending and middle parts
     * @param splitString a string to provide discrete truncation. E.g. if split
     * string is "/" - string "/some/long/file/path/that/needs/to/be/fit" will
     * become "/some/long/file/path/..." and not "/some/long/file/path/th..."
     * after truncateEnding()
     */
    public StringFitter(String replaceString, String splitString) {
        this(null, replaceString, replaceString, splitString);
    }

    /**
     * Creates a StringFitter with replace strings for beginning and ending of the
     * truncated string and also with specified split string. Uses current
     * system FontMetrics.
     * This constructor is useful if it is needed to make different behavior
     * when truncating from the beginning and from the ending
     * @param leftReplaceString a string for replacing truncated parts in the
     * beginning of the truncated string. If rightReplaceString and
     * leftReplaceString are equal leftReplaceString is used to replace middle
     * parts too. A concatenation of two replace strings is used otherwise.
     * @param rightReplaceString a string for replacing truncated parts in the
     * ending of the truncated string. If rightReplaceString and
     * leftReplaceString are equal leftReplaceString is used to replace middle
     * parts too. A concatenation of two replace strings is used otherwise.
     * @param splitString a string to provide discrete truncation. E.g. if split
     * string is "/" - string "/some/long/file/path/that/needs/to/be/fit" will
     * become "/some/long/file/path/..." and not "/some/long/file/path/th..."
     * after truncateEnding()
     */
    public StringFitter(String leftReplaceString, String rightReplaceString, String splitString) {
        this(null, leftReplaceString, rightReplaceString, splitString);
    }

    /**
     * Creates a StringFitter with specified FontMetrics. Uses "..." for all
     * replace strings and "" for split string
     * @param fm FontMetrics to use in this StringFitter. Current system
     * FontMetrics is used if it is null
     */
    public StringFitter(FontMetrics fm) {
        this(fm, "...", "...", "");
    };

    /**
     * Creates a StringFitter with universal replace string, specified split string
     * and FontMetrics.
     * @param fm FontMetrics to use in this StringFitter. Current system
     * FontMetrics is used if it is null
     * @param replaceString an universal string for replacing truncated parts. It is
     * used to replace beginning, ending and middle parts
     * @param splitString a string to provide discrete truncation. E.g. if split
     * string is "/" - string "/some/long/file/path/that/needs/to/be/fit" will
     * become "/some/long/file/path/..." and not "/some/long/file/path/th..."
     * after truncateEnding()
     */
    public StringFitter(FontMetrics fm, String replaceString, String splitString) {
        this(fm, replaceString, replaceString, splitString);
    }

    /**
     * Creates a StringFitter with replace strings for beginning and ending of the
     * truncated string and also with specified split string and FontMetrics.
     * This constructor is useful if it is needed to make different behavior
     * when truncating from the beginning and from the ending
     * @param fm FontMetrics to use in this StringFitter. Current system
     * FontMetrics is used if it is null
     * @param leftReplaceString a string for replacing truncated parts in the
     * beginning of the truncated string. If rightReplaceString and
     * leftReplaceString are equal leftReplaceString is used to replace middle
     * parts too. A concatenation of two replace strings is used otherwise.
     * @param rightReplaceString a string for replacing truncated parts in the
     * ending of the truncated string. If rightReplaceString and
     * leftReplaceString are equal leftReplaceString is used to replace middle
     * parts too. A concatenation of two replace strings is used otherwise.
     * @param splitString a string to provide discrete truncation. E.g. if split
     * string is "/" - string "/some/long/file/path/that/needs/to/be/fit" will
     * become "/some/long/file/path/..." and not "/some/long/file/path/th..."
     * after truncateEnding()
     */
    public StringFitter(FontMetrics fm, String leftReplaceString, String rightReplaceString, String splitString) {
        if(fm == null) {
            JLabel temp = new JLabel();
            fm = temp.getFontMetrics(temp.getFont());
            temp = null;
        }
        this.fm = fm;

        this.leftReplaceString = leftReplaceString;
        leftRSWidth = fm.stringWidth(leftReplaceString);
        this.rightReplaceString = rightReplaceString;
        rightRSWidth = fm.stringWidth(rightReplaceString);
        if(leftReplaceString.equals(rightReplaceString)) {
            middleReplaceString = leftReplaceString;
        } else {
            middleReplaceString = leftReplaceString + rightReplaceString;
        }
        middleRSWidth = fm.stringWidth(middleReplaceString);
        this.splitString = splitString;
        splitWidth = fm.stringWidth(splitString);
    }

    /**
     * get or create and get an instance of default StringFitter with "..." for
     * replace strings and "" for split string
     */
    public static StringFitter getDefaultFitter() {
        if(defaultInstance == null)
            defaultInstance = new StringFitter();
        return defaultInstance;
    }

    /**
     * get current replacing string for middle parts of truncated string
     */
    public String getMiddleReplaceString() {
        return middleReplaceString;
    }

    /**
     * Set a string for replacing truncated parts in the middle of truncated string ("..." by default)
     * @param s new replace string
     */
    public void setMiddleReplaceString(String s) {
        middleReplaceString = s;
        middleRSWidth = fm.stringWidth(s);
    }

    /**
     * get current replace string for beginning of truncated string
     */
    public String getLeftReplaceString() {
        return leftReplaceString;
    }

    /**
     * get current replace string for ending of truncated string
     */
    public String getRightReplaceString() {
        return rightReplaceString;
    }

    /**
     * get current split string
     */
    public String getSplitString() {
        return splitString;
    }

    /**
     * get current FontMetrics
     */
    public FontMetrics getFontMetrics() {
        return fm;
    }

    /**
     * set replace string for replacing beginning of the truncated string
     * @param s new replace string
     */
    public void setLeftReplaceString(String s) {
        leftReplaceString = s;
        leftRSWidth = fm.stringWidth(s);
    }

    /**
     * set replace string for replacing ending of the truncated string
     * @param s new replace string
     */
    public void setRightReplaceString(String s) {
        rightReplaceString = s;
        rightRSWidth = fm.stringWidth(s);
    }

    /**
     * set split string
     * @param s a string to provide discrete truncation. E.g. if split
     * string is "/" - string "/some/long/file/path/that/needs/to/be/fit" will
     * become "/some/long/file/path/..." and not "/some/long/file/path/th..."
     * after truncateEnding()
     */
    public void setSplitString(String s) {
        splitString = s;
        splitWidth = fm.stringWidth(s);
    }

    /**
     * set FontMetrics
     * @param fm new FontMetrics used to calculate widths of strings
     */
    public void setFontMetrics(FontMetrics fm) {
        this.fm = fm;
        leftRSWidth = fm.stringWidth(leftReplaceString);
        rightRSWidth = fm.stringWidth(rightReplaceString);
        splitWidth = fm.stringWidth(splitString);
    }

    /**
     * set FontMetrics through providing Component that will display truncated
     * string
     * @param c Component containing FontMetrics used to calculate widths of strings
     */
    public void setFontMetrics(Component c) {
        this.fm = c.getFontMetrics(c.getFont());
        leftRSWidth = fm.stringWidth(leftReplaceString);
        rightRSWidth = fm.stringWidth(rightReplaceString);
        splitWidth = fm.stringWidth(splitString);
    }

    /**
     * Truncate a String to fit into Component. The string is truncated from the
     * beginning
     * @param s a String to truncate
     * @param c Component that will contains String s
     */
    public String truncateBeginning(String s, Component c) {
        return truncateBeginning(s, c.getWidth());
    }

    /**
     * Truncate a String to fit into some width. The string is truncated from
     * the beginning
     * @param s a String to truncate
     * @param width available space for the string
     */
    public String truncateBeginning(String s, int width) {
        int size = fm.stringWidth(s) + leftRSWidth;

        // if component is wider than the string - it isn't modified
        if(size <= width)
            return s;

        // using String.split(String, int) to avoid cutting empty parts ("///".split("/") -> empty array)
        String parts[] = s.split(splitString, s.length() + 1);
        // truncating only 2+ parts of string
        if(parts.length < 2)
            return s;
        size += splitWidth;

        int i = 0;
        // truncating (length-1) parts of initial string - the last one will not be truncated
        // i appears to be index of the last truncated part
        for(; i < parts.length - 1 && size > width; i++) {
            size -= fm.stringWidth(parts[i]) + splitWidth;
        }
        StringBuilder result = new StringBuilder(leftReplaceString);
        // at least one part of string must be in result
        result.append(splitString).append(parts[i]);
        for(i++; i < parts.length; i++) {
            result.append(splitString).append(parts[i]);
        }
        return result.toString();
    }

    /**
     * Truncate a String to fit into Component. The string is truncated from the
     * ending
     * @param s a String to truncate
     * @param c Component that will contains String s
     */
    public String truncateEnding(String s, Component c) {
        return truncateEnding(s, c.getWidth());
    }

    /**
     * Truncate a String to fit into some width. The string is truncated from
     * the ending.
     * @param s a String to truncate
     * @param width available space for the string
     */
    public String truncateEnding(String s, int width) {
        int size = fm.stringWidth(s) + rightRSWidth;
        // if component is wider than the string - it isn't modified
        if(size <= width)
            return s;

        // using String.split(String, int) to avoid cutting empty parts
        String parts[] = s.split(splitString, s.length() + 1);
        // truncating only 2+ parts of string
        if(parts.length < 2)
            return s;
        size += splitWidth;

        int i = parts.length - 1;
        // truncating (length-1) parts of initial string - the last one will not be truncated
        // i appears to be index of the last truncated part
        for(; i > 0 && size >= width; i--)
            size -= fm.stringWidth(parts[i]) + splitWidth;

        StringBuilder result = new StringBuilder();
        for(int j = 0; j <= i; j++)
            result.append(parts[j]).append(splitString);

        return result.append(rightReplaceString).toString();
    }

    /**
     * Truncate a String to fit into Component. The string is truncated from the
     * middle
     * @param s a String to truncate
     * @param c Component that will contains String s
     */
    public String truncateMiddle(String s, Component c) {
        return truncateMiddle(s, c.getWidth());
    }

    /**
     * Truncate a String to fit into some width. The string is truncated from
     * the middle
     * @param s a String to truncate
     * @param width available space for the string
     */
    public String truncateMiddle(String s, int width) {
        // if component is wider than the string - it isn't modified
        if(fm.stringWidth(s) + middleRSWidth <= width)
            return s;

        String parts[] = s.split(splitString, s.length() + 1);
        // truncating only 2+ parts of string
        if(parts.length < 2)
            return s;

        // begI, endI - indexes of ending/starting of remaining parts of the string
        int begI = 0, endI = parts.length - 1;
        // beg, end - size of result string at beginning, ending
        int beg = fm.stringWidth(parts[begI]) + splitWidth, end = fm.stringWidth(parts[endI]) + splitWidth;
        StringBuffer begS = new StringBuffer(parts[begI]).append(splitString), endS = new StringBuffer(splitString).append(parts[endI]);
        while(beg + end <= width) {
            while(beg <= end && beg + end <= width) {
                begI++;
                beg += fm.stringWidth(parts[begI]) + splitWidth;
                if(beg + end + middleRSWidth <= width) {
                    begS.append(parts[begI] + splitString);
                }
            }
            while(end < beg && beg + end <= width) {
                endI--;
                end += fm.stringWidth(parts[endI]) + splitWidth;
                if(beg + end + middleRSWidth <= width) {
                    endS.insert(0, splitString + parts[endI]);
                }
            }
        }

        return begS.append(middleReplaceString).append(endS).toString();
    }
}
