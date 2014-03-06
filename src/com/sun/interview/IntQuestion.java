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

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A {@link Question question} to which the response is an integer.
 */
public abstract class IntQuestion extends Question
{
    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     */
    protected IntQuestion(Interview interview, String tag) {
        super(interview, tag);
        clear();
        setDefaultValue(value);
    }

    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     * @param min The inclusive lower bound for responses to this question
     * @param max The inclusive upper bound for responses to this question
     * @throws IllegalArgumentException if <code>min</code> is greater than
     * or equal to <code>max</code>.
     */
    protected IntQuestion(Interview interview, String tag, int min, int max) {
        super(interview, tag);
        setBounds(min, max);
        clear();
        setDefaultValue(value);
    }

    /**
     * Set the bounds for the response to this question.
     * If the current value is valid and not within bounds, it will
     * be adjusted to the nearest bound.
     * @param min The inclusive lower bound for responses to this question
     * @param max The inclusive upper bound for responses to this question
     * @throws IllegalArgumentException if <code>min</code> is greater than
     * or equal to <code>max</code>.
     */
    protected void setBounds(int min, int max) {
        if (min >= max)
            throw new IllegalArgumentException("invalid bounds");
        this.min = min;
        this.max = max;
        // warning, may change result of isValid()
    }

    /**
     * Get the inclusive lower bound for responses to this question.
     * @return the lower bound
     */
    public int getLowerBound() {
        return min;
    }

    /**
     * Get the inclusive upper bound for responses to this question.
     * @return the upper bound
     */
    public int getUpperBound() {
        return max;
    }

    /**
     * Get the suggested responses to this question, or null if none.
     * @return The suggestions.
     *
     * @see #setSuggestions
     */
    public int[] getSuggestions() {
        return suggestions;
    }

    /**
     * Set the set of suggested responses.
     * @param newSuggestions The values to be set, or null if none
     * @see #getSuggestions
     */
    public void setSuggestions(int[] newSuggestions) {
        suggestions = newSuggestions;
    }

    /**
     * Get the default response for this question.
     * @return the default response for this question.
     *
     * @see #setDefaultValue
     */
    public int getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default response for this question,
     * used by the clear method.
     * @param v the default response for this question.
     *
     * @see #getDefaultValue
     */
    public void setDefaultValue(int v) {
        defaultValue = v;
    }

    /**
     * Check if the value is set. Unset values are represented by the
     * special value <code>Integer.MIN_VALUE</code>.
     * @return true if a value has been set for this question and is within the
     * specified bounds
     * @see #clear
     * @deprecated see #isValueValid
     */
    public boolean isValid() {
        // conceptually, we want to do the following:
        //      return (min <= value && value <= max);
        // but if min = Integer.MIN_VALUE, that behaves like -0 (negative zero)
        // and can lead to false results, so check for that case specially
        return ((min == Integer.MIN_VALUE || min <= value) && (value <= max));
    }

    /**
     * Get the current (default or latest) response to this question.
     * If the value is unset, the result will be <code>Integer.MIN_VALUE</code>.
     * @return The current value.
     * @see #isValid
     * @see #setValue
     */
    public int getValue() {
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
    public int getValueOnPath()
        throws Interview.NotOnPathFault
    {
        interview.verifyPathContains(this);
        return getValue();
    }

    public String getStringValue() {
        if (stringValue == null) {
            if (value == Integer.MIN_VALUE)
                stringValue = "";
            else {
                NumberFormat fmt = NumberFormat.getIntegerInstance(Locale.getDefault());  // will be locale-specific
                stringValue = fmt.format(new Integer(value));
            }
        }

        return stringValue;
    }

    /**
     * Set the response to this question to the value represented by
     * a string-valued argument. Argument is decoded against current locale.
     * @param s A string containing the numeric value to be set.
     * The number should be in the range of valid values defined for
     * this question; if it is not, the value will be retained,
     * but isValueValid() will return false.
     * @throws Interview.Fault This exception is just retained for backwards
     * compatibility; it should never actually be thrown.
     * @see #getValue
     * @see #setValue(String, Locale)
     */
    public void setValue(String s) throws Interview.Fault {
        setValue(s, Locale.getDefault());
    }

    /**
     * Set the response to this question to the value represented by
     * a string-valued argument, given in certain locale.
     * @param s A string containing the numeric value to be set.
     * The number should be in the range of valid values defined for
     * this question; if it is not, the value will be retained,
     * but isValueValid() will return false.
     * @param l A locale that should be used to decode numeric value
     * from given string parameter
     * @throws Interview.Fault This exception is just retained for backwards
     * compatibility; it should never actually be thrown.
     * @see #getValue
     * @see #setValue(String)
     */
    public void setValue(String s, Locale l) throws Interview.Fault {
        //String s1 = s;
        int i;

        if (s != null)
            s = s.trim();

        if (s == null || s.length() == 0)
            i = Integer.MIN_VALUE;
        else {
            NumberFormat fmt = NumberFormat.getIntegerInstance(l); //in given locale
            ParsePosition pos = new ParsePosition(0);
            Number num = fmt.parse(s, pos);
            if (pos.getIndex() != s.length()) {
               fmt = NumberFormat.getIntegerInstance(Locale.getDefault()); //aternative
               pos = new ParsePosition(0);
               num = fmt.parse(s, pos);
            }

            if (num != null && (pos.getIndex() == s.length())) {
                //number was parsed successfully. Save value and convert string into current locale.
                i = num.intValue();
                //below is equal to getStringValue()
                fmt = NumberFormat.getIntegerInstance(Locale.getDefault());  //in current locale
                s = fmt.format(new Integer(i));
            } else {
                i = Integer.MIN_VALUE;
            }
        }

        // It would be nice to introduce a new protected setValue(i, s) that allows
        // the original text to be preserved as well. Instead, for now we pass a covert
        // parameter into setValue
        newStringValue = s;
        setValue(i);
        //System.out.println(tag + " [" + s1 + ", in " + l + "] -> [" + i + ", [" + s + "]]" );
    }

    /**
     * Set the current value.
     * @param newValue The value to be set. It should be in the range
     * of valid values defined for this question.
     * @see #getValue
     */
    public void setValue(int newValue) {
        int oldValue = value;
        value = newValue;
        stringValue = newStringValue;  // only non-null if called from setValue(String s)
        newStringValue = null;
        if (value != oldValue) {
            interview.updatePath(this);
            interview.setEdited(true);
        }
    }

    public boolean isValueValid() {
        return isValid();
    }

    public boolean isValueAlwaysValid() {
        return false;
    }

    /**
     * Set hints for the rendering system for the values that might
     * be labelled.
     * @param start The lowest value to be labelled
     * @param increment The increment for successive labels
     */
    protected void setLabelHints(int start, int increment) {
        labelStart = start;
        labelIncrement = increment;
    }

    /**
     * Get a hint for the rendering system for the lowest value
     * that might be labelled.
     * @return The lowest value that might be labelled.
     */
    public int getLabelStartHint() {
        return labelStart;
    }

    /**
     * Get a hint for the rendering system for the increment between
     * labels.
     * @return The increment between values that might be labelled.
     */
    public int getLabelIncrementHint() {
        return labelIncrement;
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
        if (o == null)
            clear();
        else if (o instanceof Integer)
            setValue(((Integer)o).intValue());
        else if (o instanceof String) {
            //get locate to parse string with
            Locale l = Interview.readLocale(data);
            try {
                setValue((String) o, l);
            }
            catch (Interview.Fault e) {
                throw new Error(e); // should not happen
            }
        }
    }

    /**
     * Save the value for this question in a dictionary, using
     * the tag as the key.
     * @param data The map in which to save the value for this question.
     */
    protected void save(Map data) {
        data.put(tag, getStringValue());
    }

    /**
     * The current response for this question.
     * MIN_VALUE is reserved as an integer NotANumber.
     * This field should be treated as read-only.
     * Use setValue to change the value.
     */
    protected int value = Integer.MIN_VALUE;

    /**
     * Suggested values for this question.
     */
    protected int[] suggestions;

    /**
     * The default response for this question.
     */
    private int defaultValue = Integer.MIN_VALUE;

    /**
     * The cached string value for this question
     */
    private String stringValue;

    /**
     * A temporary value, used to avoid changing the API for setValue/setStringValue
     */
    private transient String newStringValue;

    /**
     * The lower bound for responses to this question
     */
    private int min = Integer.MIN_VALUE + 1;

    /**
     * The upper bound for responses to this question
     */
    private int max = Integer.MAX_VALUE;

    /**
     * The hint for the lowest label that might be displayed
     */
    private int labelStart;

    /**
     * The hint for the increment between labels that might be displayed
     */
    private int labelIncrement;

    /**
     *  The string representation for a value that has not been set
     */
    private static final String UNSET = "unset";

    private static final ResourceBundle i18n = Interview.i18n;
}
