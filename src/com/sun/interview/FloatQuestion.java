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
 * A {@link Question question} to which the response is an floating point number.
 */
public abstract class FloatQuestion extends Question
{
    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     */
    protected FloatQuestion(Interview interview, String tag) {
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
     * @param resolution The resolution for responses to this question.
     * @throws IllegalArgumentException if <code>min</code> is greater than
     * or equal to <code>max</code>.
     */
    protected FloatQuestion(Interview interview, String tag, float min, float max, float resolution) {
        super(interview, tag);
        setBounds(min, max);
        setResolution(resolution);
        clear();
        setDefaultValue(value);
    }

    /**
     * Set the bounds for the response to this question.
     * @param min The inclusive lower bound for responses to this question
     * @param max The inclusive upper bound for responses to this question
     * @throws IllegalArgumentException if <code>min</code> is greater than
     * or equal to <code>max</code>.
     */
    protected void setBounds(float min, float max) {
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
    public float getLowerBound() {
        return min;
    }

    /**
     * Get the inclusive lower bound for responses to this question.
     * @return the upper bound
     */
    public float getUpperBound() {
        return max;
    }

    /**
     * Check if the value is set and within bounds.
     * @return true if a value has been set for this question and is within the
     * specified bounds
     * @see #clear
     * @deprecated see #isValueValid
     */
    public boolean isValid() {
        // conceptually, we want to do the following:
        //      return (!Float.isNaN(value) && min <= value && value <= max);
        // but if min = Float.MIN_VALUE, that behaves like -0 (negative zero)
        // and can lead to false results, so take care with that case
        return (!Float.isNaN(value)
                && (min == Float.MIN_VALUE || min <= value)
                && (value <= max));
    }

    /**
     * Set the resolution for responses to this question. Responses
     * may be rounded to the nearest multiple of the resolution.
     * @param resolution the resolution for responses to this question
     * @see #getResolution
     * @see #setValue
     */
    public void setResolution(float resolution) {
        this.resolution = resolution;
    }


    /**
     * Get the resolution for responses to this question. Responses
     * may be rounded to the nearest multiple of the resolution.
     * @return the resolution for responses to this question
     * @see #setResolution
     * @see #setValue
     */
    public float getResolution() {
        return resolution;
    }

    /**
     * Get the suggested responses to this question, or null if none.
     * @return The suggestions.
     *
     * @see #setSuggestions
     */
    public float[] getSuggestions() {
        return suggestions;
    }

    /**
     * Set the set of suggested responses.
     * @param newSuggestions The values to be set, or null if none
     * @see #getSuggestions
     */
    public void setSuggestions(float[] newSuggestions) {
        suggestions = newSuggestions;
    }

    /**
     * Get the default response for this question.
     * @return the default response for this question.
     *
     * @see #setDefaultValue
     */
    public float getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default response for this question,
     * used by the clear method.
     * @param v the default response for this question.
     *
     * @see #getDefaultValue
     */
    public void setDefaultValue(float v) {
        defaultValue = v;
    }

    /**
     * Get the current (default or latest) response to this question.
     * @return The current value.
     * @see #setValue
     */
    public float getValue() {
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
    public float getValueOnPath()
        throws Interview.NotOnPathFault
    {
        interview.verifyPathContains(this);
        return getValue();
    }

    public String getStringValue() {
        if (stringValue == null) {
            if (Float.isNaN(value)) {
                stringValue = "";
            }
            else {
                NumberFormat fmt = NumberFormat.getNumberInstance(Locale.getDefault());  // will be locale-specific
                stringValue = fmt.format(new Double(value));
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
     * @see #getValue
     * @throws Interview.Fault This exception is just retained for backwards
     * compatibility; it should never actually be thrown.
     * @see #getValue()
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
     * @see #getValue
     * @throws Interview.Fault This exception is just retained for backwards
     * compatibility; it should never actually be thrown.
     * @see #getValue()
     * @see #setValue(String, Locale)
     */
    public void setValue(String s, Locale l) throws Interview.Fault {
        //String s1 = s;
        float f;

        if (s != null)
            s = s.trim();

        if (s == null || s.length() == 0 || s.equals("NaN"))
            f = Float.NaN;
        else {
            NumberFormat fmt = NumberFormat.getNumberInstance(l); //in given locale
            ParsePosition pos = new ParsePosition(0);
            Number num = fmt.parse(s, pos);
            if (pos.getIndex() != s.length()) {
               fmt = NumberFormat.getNumberInstance(Locale.getDefault()); //aternative
               pos = new ParsePosition(0);
               num = fmt.parse(s, pos);
            }

            if (num != null && (pos.getIndex() == s.length())) {
                //number was parsed successfully. Save value and convert string into current locale.
                f = num.floatValue();
                //below is equal to getStringValue()
                fmt = NumberFormat.getNumberInstance(Locale.getDefault());  // in current locale
                s = fmt.format(new Double(f));
            } else {
                f = Float.NaN;
            }
        }

        // It would be nice to introduce a new protected setValue(f, s) that allows
        // the original text to be preserved as well. Instead, for now we pass a covert
        // parameter into setValue
        newStringValue = s;
        setValue(f);
        //System.out.println(tag + " [" + s1 + ", in " + l + "] -> [" + f + ", [" + s + "]]" );
    }

    /**
     * Set the current value.
     * @param newValue The value to be set. It should be in the range
     * of valid values defined for this question.
     * @see #getValue
     */
    public void setValue(float newValue) {
        float oldValue = value;
        value = newValue;
        stringValue = newStringValue;  // only non-null if called from setValue(String s)
        newStringValue = null;
        if (Float.isNaN(value) ? !Float.isNaN(oldValue) : (value != oldValue)) {
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
    protected void setLabelHints(float start, float increment) {
        labelStart = start;
        labelIncrement = increment;
    }

    /**
     * Get a hint for the rendering system for the lowest value
     * that might be labelled.
     * @return The lowest value that might be labelled.
     */
    public float getLabelStartHint() {
        return labelStart;
    }

    /**
     * Get a hint for the rendering system for the increment between
     * labels.
     * @return The increment between values that might be labelled.
     */
    public float getLabelIncrementHint() {
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
        else if (o instanceof Float)
            setValue(((Float)o).floatValue());
        else if (o instanceof String) {
            //get locate to parse string with
            Locale l = Interview.readLocale(data);
            try {
                setValue((String) o, l);
            }
            catch (Interview.Fault e) {
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
        data.put(tag, getStringValue());
    }

    /**
     * The current response for this question.
     * This field should be treated as read-only.
     * Use setValue to change the value.
     */
    protected float value = Float.NaN;

    /**
     * Suggested values for this question.
     */
    protected float[] suggestions;

    /**
     * The default response for this question.
     */
    private float defaultValue = Float.NaN;

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
    private float min = Float.MIN_VALUE;

    /**
     * The upper bound for responses to this question
     */
    private float max = Float.MAX_VALUE;

    /**
     * The resolution for responses to this question
     */
    private float resolution;

    /**
     * The hint for the lowest label that might be displayed
     */
    private float labelStart;

    /**
     * The hint for the increment between labels that might be displayed
     */
    private float labelIncrement;


    private static final ResourceBundle i18n = Interview.i18n;
}
