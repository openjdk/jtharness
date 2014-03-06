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
package com.sun.interview;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A {@link Question question} to which the response is one of a number of choices.
 */
public abstract class ChoiceQuestion extends Question
{
    /**
     * Create a question with a nominated tag.
     * If this constructor is used, the choices must be supplied separately.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     */
    protected ChoiceQuestion(Interview interview, String tag) {
        super(interview, tag);
        // don't call clear() until the choices have been set
    }

    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     * @param choices The set of legal values for responses to this question.
     */
    protected ChoiceQuestion(Interview interview, String tag, String[] choices) {
        super(interview, tag);
        setChoices(choices, choices); // will call clear
    }

    /**
     * Set the set of legal responses for this question. If the current
     * value is one of the choices (string equality), it will be set
     * identically equal to that choice; otherwise, the current value
     * will be set to the first choice.
     * @param choices The set of possible responses for this question.
     * @see #getChoices
     * @see #setChoices(String[], boolean)
     * @see #setChoices(String[], String[])
     * @throws NullPointerException if choices is null.
     */
    protected void setChoices(String[] choices) {
        setChoices(choices, choices);
    }

    /**
     * Set the set of legal responses for this question. If the current
     * value is one of the choices (string equality), it will be set
     * identically equal to that choice; otherwise, the current value
     * will be set to the first choice.
     * @param choices An array of strings identifying the set of
     * legal responses for this question. Depending on the value of
     * the 'localize' argument, the strings will be used literally, or
     * will be used to construct keys to look up resources in the
     * containing interview's resource bundle, in order to get the
     * display strings. In both cases, the current value will always
     * be one of the values in the choices array.
     * @param localize if false, the choices will be used directly
     * as the display choices; otherwise the choices will be used
     * to construct keys to get localized values from the interview's
     * resource bundle.
     * @see #getChoices
     * @see #setChoices(String[])
     * @see #setChoices(String[], String[])
     * @throws NullPointerException if choices is null.
     */
    protected void setChoices(String[] choices, boolean localize) {
        setChoices(choices, (localize ? null : choices));
    }

    /**
     * Set the set of legal responses for this question. If the current
     * value is one of the choices (string equality), it will be set
     * identically equal to that choice; otherwise, the current value
     * will be set to the first choice.
     * @param choices An array of strings identifying the set of
     * legal responses for this question.
     * @param displayChoices An array of strings to be presented to
     * the user that identify the legal responses to this question.
     * The value can also be null, to indicate that the display choices
     * should be determined automatically by obtaining localized values
     * for the entries in the choices array.
     * @throws NullPointerException if choices is null.
     * @throws IllegalArgumentException if displayChoices is not null
     * and is a different length than choices.
     * @see #getChoices
     * @see #setChoices(String[])
     * @see #setChoices(String[], boolean)
     */
    protected void setChoices(String[] choices, String[] displayChoices) {
        if (choices == null)
            throw new NullPointerException();

        if (displayChoices != null && choices.length != displayChoices.length)
            throw new IllegalArgumentException();

        boolean needClear = (this.choices == null);

        this.choices = choices;
        this.displayChoices = displayChoices;
        defaultValue = choices[0];

        if (needClear && (interview.getInterviewSemantics() > Interview.SEMANTIC_PRE_32))
            clear();
        else {
            // backward compatible behavior
            if (value == null) {
                value = choices[0];
                interview.updatePath(this);
                interview.setEdited(true);
            } else {
                for (int i = 0; i < choices.length; i++) {
                    if (value.equals(choices[i])) {
                        value = choices[i];
                        return;
                    }
                }
                value = choices[0];
                interview.updatePath(this);
                interview.setEdited(true);
            }
        }
    }

    /**
     * Set the set of legal responses for this question, using the
     * standard resource bundle for localization.
     * @param choices The set of possible responses for this question.
     *   Each entry in the argument array is localized by looking up
     *   <em>question-key</em>.<em>choices[i]</em> in the standard
     *   resource bundle used by the parent interview. If an appropriate
     *   entry is not found, the array value (<em>choices[i]</em>) is
     *   used instead.
     * @see #getChoices
     * @see #setChoices
     * @deprecated Use the other setChoices() methods, which support i18n as
     *     an option.
     * @see Interview#getResourceBundle
     */
    protected void setI18NChoices(String[] choices) {
        setChoices(choices, true);
    }


    /**
     * Get the set of legal responses for this question.
     * @return The set of possible responses for this question.
     * @see #setChoices
     * @see #getDisplayChoices
     */
    public String[] getChoices() {
        return choices;
    }


    /**
     * Get the display values for the set of legal responses for this question.
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
                    try {
                        displayChoices[i] = (c == null ? null : b.getString(key + "." + c));
                    }
                    catch (MissingResourceException e) {
                        displayChoices[i] = c;
                    }
                }
            }
        }

        return displayChoices;
    }

    /**
     * Get the default response for this question. It defaults to the
     * first choice in the array of choices set with setChoices.
     * @return the default response for this question.
     *
     * @see #setDefaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default response for this question,
     * used by the clear method.
     * @param v the default response for this question.
     *
     * @see #getDefaultValue
     */
    public void setDefaultValue(String v) {
        defaultValue = v;
    }

    /**
     * Get the internal value for the current (default or latest)
     * response to this question.
     * @return The current value.
     * @see #setValue
     * @see #getDisplayChoices
     */
    public String getValue() {
        return value;
    }


    /**
     * Get the display string for the current (default or latest)
     * response to this question.
     * @return The display string for the current value.
     * @see #setValue
     * @see #getDisplayChoices
     */
    public String getDisplayValue() {
        getDisplayChoices(); // ensure initialized

        String v = getValue();
        for (int i = 0; i < choices.length; i++) {
            if (v == null ? choices[i] == null : v.equals(choices[i]))
                return displayChoices[i];
        }

        return v;
    }

    /**
     * Verify this question is on the current path, and if it is,
     * return the current value.
     * @return the current value of this question
     * @throws Interview.NotOnPathFault if this question is not on the
     * current path
     * @see #getValue
     */
    public String getValueOnPath()
        throws Interview.NotOnPathFault
    {
        interview.verifyPathContains(this);
        return getValue();
    }

    public String getStringValue() {
        return getValue();
    }


    /**
     * Set the current value.
     * @param newValue The value to be set. It must be one of the valid
     * choices for this question, as distinct from the display choices.
     * @see #getValue
     */
    public void setValue(String newValue) {
        if (choices == null)
            return;

        if (newValue == null) {
            if (value != null) {
                value = null;
                interview.updatePath(this);
                interview.setEdited(true);
            }
        }
        else {
            // try and canonicalize newValue to one of the specified choices
            for (int i = 0; i < choices.length; i++) {
                if (newValue.equals(choices[i])) {
                    newValue = choices[i];
                    break;
                }
            }

            if (!newValue.equals(value)) {
                value = newValue;
                interview.updatePath(this);
                interview.setEdited(true);
            }
        }
    }

    public boolean isValueValid() {
        // value is valid if it matches one of the specified choices
        for (int i = 0; i < choices.length; i++) {
            if (value == null ? choices[i] == null : value.equals(choices[i]))
                return true;
        }
        return false;
    }

    public boolean isValueAlwaysValid() {
        return false;
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
        if (o instanceof String)
            setValue((String)o);
    }

    /**
     * Save the value for this question in a dictionary, using
     * the tag as the key.
     * @param data The map in which to save the value for this question.
     */
    protected void save(Map data) {
        if (value != null)
            data.put(tag, value);
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
    protected String value;

    /**
     * The default response for this question.
     */
    private String defaultValue;

}
