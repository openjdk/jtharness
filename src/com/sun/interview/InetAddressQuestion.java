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
package com.sun.interview;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A {@link Question question} to which the response is an IP address.
 * Both IPv4 and IPv6 addresses are supported.
 */
public abstract class InetAddressQuestion extends Question
{

    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     */
    protected InetAddressQuestion(Interview interview, String tag) {
        super(interview, tag);
        clear();
        setDefaultValue(value);
    }

    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     * @param type A value to (IPv4 or IPv6) to indicate the type of
     * address used by the question.
     */
    protected InetAddressQuestion(Interview interview, String tag, int type) {
        super(interview, tag);
        setType(type);
        clear();
        setDefaultValue(value);
    }

    /**
     * A constant to indicate that only IPv4 addresses should be used.
     */
    public static final int IPv4 = 0;

    /**
     * A constant to indicate that IPv6 addresses should be used. Note that
     * IPv6 includes IPv4 addresses.
     */
    public static final int IPv6 = 1;

    /**
     * Get the type of addresses (IPv4 or IPv6) that will be accepted by this question.
     * @return the type of address that will be accepted by this question
     * @see #setType
     */
    public int getType() {
        return type;
    }

    /**
     * Set the type of addresses (IPv4 or IPv6) that should be accepted by this question.
     * @param type the type of address that should be accepted by this question
     * @see #getType
     */
    protected void setType(int type) {
        if (type != IPv4 && type != IPv6)
            throw new IllegalArgumentException();

        this.type = type;
    }

    /**
     * Get the presentation style (IPv4 or IPv6) for this question.
     * This is only a hint. Setting the type to IPv6 or setting suggestions
     * may cause the actual presentation style to be IPv6.
     * In IPv4 style, the data entry is four dotted decimal fields.
     * In IPv6 style, the data entry is a single type in field, with
     * an associated lookup button.
     * @return the presentation style for this question
     * @see #setStyle
     */
    public int getStyle() {
        return style;
    }

    /**
     * Set the presentation style (IPv4 or IPv6) for this question.
     * In IPv4 style, the data entry is four dotted decimal fields.
     * In IPv6 style, the data entry is a single type in field, with
     * an associated lookup button.
     * @param style the presentation style for this question
     * @see #getStyle
     */
    protected void setStyle(int style) {
        if (style != IPv4 && style != IPv6)
            throw new IllegalArgumentException();

        this.style = style;
    }

    /**
     * Get the suggested responses to this question, or null if none.
     * @return The suggestions.
     *
     * @see #setSuggestions
     */
    public InetAddress[] getSuggestions() {
        return suggestions;
    }

    /**
     * Set the set of suggested responses.
     * @param newSuggestions The values to be set, or null if none
     * @throws IllegalArgumentException if any except the first of the values
     * in the array are null
     *
     * @see #getSuggestions
     */
    public void setSuggestions(InetAddress[] newSuggestions) {
        if (newSuggestions != null) {
            // allow the first entry to be null
            for (int i = 1; i < newSuggestions.length; i++) {
                if (newSuggestions[i] == null)
                    throw new IllegalArgumentException();
            }
        }

        suggestions = newSuggestions;
    }

    /**
     * Get the default response for this question.
     * @return the default response for this question.
     *
     * @see #setDefaultValue
     */
    public InetAddress getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default response for this question,
     * used by the clear method.
     * @param v the default response for this question.
     *
     * @see #getDefaultValue
     */
    public void setDefaultValue(InetAddress v) {
        defaultValue = v;
    }


    /**
     * Get the current (default or latest) response to this question.
     * If the question type is set to IPv4, a valid response will be
     * an Inet4Address; otherwise, if the question type is set to IPv6,
     * a valid response will be an Inet4Address or an Inet6Address
     * @return The current value.
     * @see #setValue
     */
    public InetAddress getValue() {
        return value;
    }

    /**
     * Verify this question is on the current path, and if it is,
     * return the current value.
     * @return the current value of this question
     * @throws Interview.NotOnPathFault if this question is not on the
     * current path
     * @see #getValue
     */
    public InetAddress getValueOnPath()
        throws Interview.NotOnPathFault
    {
        interview.verifyPathContains(this);
        return getValue();
    }

    public String getStringValue() {
        if (stringValue == null && value != null)
            stringValue = value.getHostAddress();

        return stringValue;
    }


    /**
     * Set the current value.
     * Although any value can be set, if the value is to be considered valid
     * it must be an Inet4Address if the question type is set to IPv4,
     * or either an Inet4Address or an Inet6Address if the question type
     * is set to IPv6.
     * @param newValue The value to be set.
     * @see #getValue
     */
    public void setValue(InetAddress newValue) {
        InetAddress oldValue = value;
        value = newValue;
        stringValue = newStringValue;  // only non-null if called from setValue(String s)
        newStringValue = null;

        valid = (value == null ? false
                 : type == IPv4 ? (value instanceof Inet4Address)
                 : true);

        if (!equal(value, oldValue)) {
            interview.updatePath(this);
            interview.setEdited(true);
        }
    }

    public boolean isValueValid() {
        return valid; // set by setValue
    }

    public boolean isValueAlwaysValid() {
        return false;
    }

    /**
     * Set the current value.
     * @param newValue The value to be set.
     * @throws Interview.Fault (retained for compatibility; should not be thrown)
     * @see #getValue
     */
    public void setValue(String newValue) throws Interview.Fault {
        InetAddress v = parse(newValue);

        newStringValue = newValue;
        setValue(v);
    }

    private InetAddress parse(String s) {
        if (s == null || s.length() == 0)
            return null;

        // scan the string to see if it looks reasonable:
        // - an IPv4 string contains only digits and dots
        // - an IPv6 string contains hex digits, dots and colons
        //   with at least one colon
        // this is intended to prevent calling erroneously calling
        // InetAddress.getByName with a hostname and getting a
        // false positive result

        boolean seenAlpha = false;
        boolean seenDigit = false;
        boolean seenDot = false;
        boolean seenColon = false;
        int slen = s.length();
        for (int i = 0; i < slen; i++) {
            char c = s.charAt(i);
            switch (c) {
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                // always valid
                seenDigit = true;
                break;

            case '.':
                // always valid
                seenDot = true;
                break;

            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                // invalid for IPv4, valid for IPv6
                if (type == IPv4)
                    return null;

                seenAlpha = true;
                break;

            case ':':
                // invalid for IPv4; at least 1 required for IPv6
                if (type == IPv4)
                    return null;

                seenColon = true;
                break;

            default:
                return null;
            }
        }

        if (type == IPv6 && !(seenColon || seenDot && seenDigit && !seenAlpha))
            return null;

        try {
            return InetAddress.getByName(s);
        }
        catch (UnknownHostException e) {
            return null;
        }
    }



    /**
     * Clear any response to this question, resetting the value
     * back to its initial state.
     */
    public void clear() {
        setValue(defaultValue);
    }

    /**
     * Load the value for this question from a dictionary, using
     * the tag as the key.
     * @param data The map from which to load the value for this question.
     */
    protected void load(Map data) {
        Object o = data.get(tag);
        if (o instanceof InetAddress) {
            setValue((InetAddress) o);
        }
        else if (o instanceof String) {
            try {
                setValue((String) o);
            }
            catch (Interview.Fault e) {
                // never thrown. but just in case...
                throw new Error(e);
            }
        }
    }

    /**
     * Save the value for this question in a dictionary, using
     * the tag as the key.
     * @param data The map in which to save the value for this question.
     */
    protected void save(Map data) {
        String s = getStringValue();
        if (s != null)
            data.put(tag, s);
    }

    /**
     * Compare two network address objects for equality.
     * @param i1 The first address to be compared, or null.
     * @param i2 The other address to be compared, or null.
     * @return true if both arguments are null, or if both represent the
     * same network address.
     *
     */
    protected static boolean equal(InetAddress i1, InetAddress i2) {
        return (i1 == null ? i2 == null : i1.equals(i2));
    }

    /**
     * The current (default or latest) response to this question.
     */
    protected InetAddress value;

    /**
     * The cached string value for this question
     */
    private String stringValue;

    /**
     * A temporary value, used to avoid changing the API for setValue/setStringValue
     */
    private transient String newStringValue;

    private InetAddress[] suggestions;

    /**
     * The default response for this question.
     */
    private InetAddress defaultValue;

    private boolean valid;

    private int type = IPv4;
    private int style = IPv4;


    private static final ResourceBundle i18n = Interview.i18n;
}
