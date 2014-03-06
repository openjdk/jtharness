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
package com.sun.interview;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A {@link Question question} to which the response is one of a number of choices.
 */
public abstract class ChoiceArrayQuestion extends Question
{
    /**
     * Create a question with a nominated tag.
     * If this constructor is used, the choices must be supplied separately.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     */
    protected ChoiceArrayQuestion(Interview interview, String tag) {
        super(interview, tag);
    }

    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     * @param choices The names of the choices, which can each be selected (true)
     * or not (false).
     * @throws NullPointerException if choices is null
     */
    protected ChoiceArrayQuestion(Interview interview, String tag, String[] choices) {
        super(interview, tag);
        setChoices(choices, choices);
    }

    /**
     * Set the names of the choices for this question.
     * The choices will also be used as the display choices.
     * The current value will be set to all false;
     * @param choices The set of names for the choices for this question.
     * @see #getChoices
     * @see #getDisplayChoices
     * @throws NullPointerException if choices is null
     */
    protected void setChoices(String[] choices) {
        setChoices(choices, choices);
    }

    /**
     * Set the names of the choices for this question.
     * The current value will be set to all false;
     * @param choices The set of names of the choices for this question.
     * @param localize if false, the choices will be used directly
     * as the display choices; otherwise the choices will be used
     * to construct keys to get localized values from the interview's
     * resource bundle.
     * @see #getChoices
     * @see #getDisplayChoices
     * @throws NullPointerException if choices is null
     */
    protected void setChoices(String[] choices, boolean localize) {
        setChoices(choices, (localize ? null : choices));
    }

    /**
     * Set the names of the choices for this question.
     * @param choices The set of names of the choices for this question.
     * @param displayChoices An array of strings to be presented to
     * the user that identify the choices for this question.
     * The value can also be null, to indicate that the display choices
     * should be determined automatically by obtaining localized values
     * for the entries in the choices array.
     * @throws NullPointerException if choices is null.
     * @throws IllegalArgumentException if displayChoices is not null
     * and is a different length than choices.
     * @see #getChoices
     * @see #getDisplayChoices
     */
    protected void setChoices(String[] choices, String[] displayChoices) {
        if (choices == null)
            throw new NullPointerException();

        for (int i = 0; i < choices.length; i++) {
            if (choices[i] == null)
                throw new NullPointerException();
        }

        if (displayChoices != null) {
            if (choices.length != displayChoices.length)
                throw new IllegalArgumentException();

            for (int i = 0; i < displayChoices.length; i++) {
                if (displayChoices[i] == null)
                    throw new NullPointerException();
            }
        }

        this.choices = choices;
        this.displayChoices = displayChoices;
        value = new boolean[choices.length];

        if (defaultValue != null && defaultValue.length != choices.length)
            defaultValue = null;
    }

    /**
     * Get the set of legal responses for this question.
     * @return The set of possible responses for this question.
     * @see #setChoices
     */
    public String[] getChoices() {
        return choices;
    }

    /**
     * Get the display values for the set of choices for this question.
     * The display values will typically be different from the standard values
     * if they have been localized.
     * @return The display values for the set of possible responses for this question.
     * @see #setChoices
     * @see #getDisplayChoices
     */
    public String[] getDisplayChoices() {
        if (displayChoices == null) {
            ResourceBundle b = interview.getResourceBundle();
            if (b == null)
                return choices;
            else {
                displayChoices = new String[choices.length];
                for (int i = 0; i < choices.length; i++) {
                    String c = choices[i];
                    String rn = key + "." + c;
                    try {
                        displayChoices[i] = (c == null ? null : b.getString(rn));
                    }
                    catch (MissingResourceException e) {
                        System.err.println("WARNING: missing resource " + rn);
                        displayChoices[i] = c;
                    }
                }
            }
        }

        return displayChoices;
    }

    /**
     * Get the default response for this question.
     * @return the default response for this question.
     *
     * @see #setDefaultValue
     */
    public boolean[] getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default response for this question,
     * used by the clear method.
     * @param v the default response for this question.
     *
     * @see #getDefaultValue
     */
    public void setDefaultValue(boolean[] v) {
        defaultValue = v;
    }

    /**
     * Get the current (default or latest) response to this question.
     * @return The current value.
     * @see #setValue
     * @throws IllegalStateException if no choices have been set, defining
     *   the set of responses to this question
     * @see #setChoices
     */
    public boolean[] getValue() {
        if (value == null)
            throw new IllegalStateException();

        // returns a copy of the data
        boolean[] cp = null;
        if (value != null) {
            cp = new boolean[value.length];
            for (int i = 0; i < value.length; i++)
                cp[i] = value[i];
        }

        return cp;
    }

    /**
     * Verify this question is on the current path, and if it is,
     * return the current value.
     * @return the current value of this question
     * @throws Interview.NotOnPathFault if this question is not on the
     * current path
     * @see #getValue
     */
    public boolean[] getValueOnPath()
        throws Interview.NotOnPathFault
    {
        interview.verifyPathContains(this);
        return getValue();
    }

    public String getStringValue() {
        if (value == null)
            return "";

        if (value.length != choices.length)
            throw new IllegalStateException();

        StringBuffer sb = new StringBuffer();
        if (value != null) {
            for (int i = 0; i < value.length; i++) {
                if (value[i]) {
                    if (sb.length() > 0)
                        sb.append(' ');
                    sb.append(choices[i].replace(' ', '_'));
                }
            }
        }

        return sb.toString();
    }


    /**
     * Set the current value.
     * @param newValue The value to be set. The value is broken into words,
     * and each word must identify one of the set of choices for this question.
     * The set of choices so identified will be set to true.
     * Invalid choices are ignored.
     * @see #getValue
     */
    public void setValue(String newValue) {
        if (choices == null)
            return;

        boolean[] bb = new boolean[choices.length];

        int start = -1;
        for (int i = 0; i < newValue.length(); i++) {
            if (white(newValue.charAt(i))) {
                if (start != -1)
                    set(bb, newValue.substring(start, i));
                start = -1;
            } else
                if (start == -1)
                    start = i;
        }
        if (start != -1)
            set(bb, newValue.substring(start));

        setValue(bb);
    }

    public boolean isValueValid() {
        return true;
    }

    public boolean isValueAlwaysValid() {
        return false;
    }

    private void set(boolean[] bb, String s) {
        for (int i = 0; i < choices.length; i++) {
            if (s.equals(choices[i].replace(' ', '_'))) {
                bb[i] = true;
                return;
            }
        }
    }

    private static boolean white(char c) {
        return (c == ' '  ||  c == '\t'  ||  c == '\n');
    }

    /**
     * Set the current value.
     * @param newValue The new value: one boolean per choice, indicating whether
     * the corresponding choice is selected or not.
     * @see #getValue
     */
    public void setValue(boolean[] newValue) {
        if (choices == null)
            return;

        boolean changed = false;

        if (newValue == null) {
            for (int i = 0; i < value.length; i++) {
                if (!changed)
                    changed = (value[i] != false);
                value[i] = false;
            }
        }
        else {
            for (int i = 0; i < Math.min(newValue.length, value.length); i++) {
                if (!changed)
                    changed = (value[i] != newValue[i]);
                value[i] = newValue[i];
            }
        }

        if (changed) {
            interview.updatePath(this);
            interview.setEdited(true);
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
        if (o != null && o instanceof String)
            setValue((String)o);
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
     * The set of legal responses for this question.
     */
    private String[] choices;

    /**
     * The localized values to display, corresponding 1-1 to the
     * set of legal responses to this question.
     */
    private String[] displayChoices;

    /**
     * The current (default or latest) response to this question.
     */
    protected boolean[] value;

    /**
     * The default response for this question.
     */
    private boolean[] defaultValue;
}
