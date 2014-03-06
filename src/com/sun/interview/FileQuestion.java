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

import java.io.File;
import java.util.Map;

/**
 * A {@link Question question} to which the response is a filename.
 */
public abstract class FileQuestion extends Question
{
    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     */
    protected FileQuestion(Interview interview, String tag) {
        super(interview, tag);

        if (interview.getInterviewSemantics() > Interview.SEMANTIC_PRE_32)
            clear();

        setDefaultValue(value);
    }

    /**
     * Get the suggested responses to this question, or null if none.
     * @return The suggestions.
     *
     * @see #setSuggestions
     */
    public File[] getSuggestions() {
        return suggestions;
    }

    /**
     * Set the set of suggested responses.
     * @param newSuggestions The values to be set, or null if none
     * @throws IllegalArgumentException if any of the values in the array
     * are null
     * @see #getSuggestions
     */
    public void setSuggestions(File[] newSuggestions) {
        if (newSuggestions != null) {
            for (int i = 0; i < newSuggestions.length; i++) {
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
    public File getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default response for this question,
     * used by the clear method.
     * @param v the default response for this question.
     *
     * @see #getDefaultValue
     */
    public void setDefaultValue(File v) {
        defaultValue = v;
    }

    /**
     * Get the current (default or latest) response to this question.
     * @return The current value.
     * @see #setValue
     */
    public File getValue() {
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
    public File getValueOnPath()
        throws Interview.NotOnPathFault
    {
        interview.verifyPathContains(this);
        return getValue();
    }

    public String getStringValue() {
        return (value == null ? null : value.getPath());
    }

    /**
     * Set the response to this question to the value represented by
     * a string-valued argument.
     * @see #getValue
     */
    public void setValue(String path) {
        setValue(path == null ? (File)null : new File(path));
    }

    /**
     * Set the current value.
     * @param newValue The value to be set.
     * @see #getValue
     */
    public void setValue(File newValue) {
        File oldValue = value;
        value = newValue;
        if (!equal(value, oldValue)) {
            interview.updatePath(this);
            interview.setEdited(true);
        }
    }

    public boolean isValueValid() {
        if (value == null) {
            return true;
        }
        if (baseRelativeOnly && baseDir != null
            && !value.getPath().startsWith(baseDir.getPath())) {
            return false;
        }

        if (filters != null) {
            for (FileFilter f : filters) {
                if (!f.accept(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isValueAlwaysValid() {
        return false;
    }

    /**
     * Get the filters used to select valid files for a response
     * to this question.
     * @return An array of filters
     * @see #setFilter
     * @see #setFilters
     */
    public FileFilter[] getFilters() {
        return filters;
    }

    /**
     * Set a filter used to select valid files for a response
     * to this question.
     * @param filter a filter used to select valid files for a response
     * to this question
     * @see #getFilters
     * @see #setFilters
     */
    public void setFilter(FileFilter filter) {
        filters = new FileFilter[] { filter };
    }

    /**
     * Set the filters used to select valid files for a response
     * to this question.
     * @param filters An array of filters used to select valid files for a response
     * to this question
     * @see #getFilters
     * @see #setFilter
     */
    public void setFilters(FileFilter[] filters) {
        this.filters = filters;
    }

    /**
     * Get the default directory for files for a response to this question.
     * @return the default directory in which files should be found/placed
     * @see #setBaseDirectory
     * @see #isBaseRelativeOnly
     */
    public File getBaseDirectory() {
        return baseDir;
    }

    /**
     * Set the default directory for files for a response to this question.
     * @param dir the default directory in which files should be found/placed
     * @see #getBaseDirectory
     */
    public void setBaseDirectory(File dir) {
        baseDir = dir;
    }

    /**
     * Determine whether all valid responses to this question should be
     * relative to the base directory (i.e. in or under it.)  False by
     * default.
     * @return true if all valid responses to this question should be
     * relative to the base directory
     * @see #setBaseRelativeOnly
     */
    public boolean isBaseRelativeOnly() {
        return baseRelativeOnly;
    }

    /**
     * Specify whether all valid responses to this question should be
     * relative to the base directory (in or under it).
     * @param b this parameter should be true if all valid responses
     * to this question should be relative to the base directory
     * @see #setBaseRelativeOnly
     */
    public void setBaseRelativeOnly(boolean b) {
        baseRelativeOnly = b;
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
        if (o instanceof File)
            setValue((File)o);
        else if (o instanceof String)
            setValue(new File((String)o));
    }

    /**
     * Save the value for this question in a dictionary, using
     * the tag as the key.
     * @param data The map in which to save the value for this question.
     */
    protected void save(Map data) {
        if (value != null)
            data.put(tag, value.toString());
    }


    /**
     * Determine if two filenames are equal.
     * @param f1 the first filename to be compared
     * @param f2 the other filename to be compared
     * @return true if both filenames are null, or if both
     * identify the same filename
     */
    protected static boolean equal(File f1, File f2) {
        return (f1 == null ? f2 == null : f1.equals(f2));
    }

    /**
     * The current (default or latest) response to this question.
     */
    protected File value;

    /**
     * Suggested values for this question.
     */
    protected File[] suggestions;

    /**
     * The default response for this question.
     */
    private File defaultValue;

    private File baseDir;

    private boolean baseRelativeOnly;

    private FileFilter[] filters;
}
