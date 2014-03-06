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

import java.io.File;
import java.util.Map;
import java.util.Vector;

/**
 * A {@link Question question} to which the response is one or more filenames.
 */
public abstract class FileListQuestion extends Question
{
    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     */
    protected FileListQuestion(Interview interview, String tag) {
        super(interview, tag);

        if (interview.getInterviewSemantics() > Interview.SEMANTIC_PRE_32)
            clear();

        setDefaultValue(value);
    }

    /**
     * Get the default response for this question.
     * @return the default response for this question.
     *
     * @see #setDefaultValue
     */
    public File[] getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default response for this question,
     * used by the clear method.
     * @param v the default response for this question.
     *
     * @see #getDefaultValue
     */
    public void setDefaultValue(File[] v) {
        defaultValue = v;
    }

    /**
     * Specify whether or not duplicates should be allowed in the list.
     * By default, duplicates are allowed.
     * @param b true if duplicates should be allowed, and false otherwise
     * @see #isDuplicatesAllowed
     */
    public void setDuplicatesAllowed(boolean b) {
        duplicatesAllowed = b;
    }

    /**
     * Check whether or not duplicates should be allowed in the list.
     * @return true if duplicates should be allowed, and false otherwise
     * @see #setDuplicatesAllowed
     */
    public boolean isDuplicatesAllowed() {
        return duplicatesAllowed;
    }

    /**
     * Get the current (default or latest) response to this question.
     * @return The current value.
     * @see #setValue
     */
    public File[] getValue() {
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
    public File[] getValueOnPath()
        throws Interview.NotOnPathFault
    {
        interview.verifyPathContains(this);
        return getValue();
    }

    public String getStringValue() {
        return join(value);
    }

    /**
     * Set the response to this question to the value represented by
     * a string-valued argument.
     * @see #getValue
     */
    public void setValue(String paths) {
        setValue(paths == null ? (File[])null : split(paths));
    }

    /**
     * Set the current value.
     * @param newValue The value to be set.
     * @see #getValue
     */
    public void setValue(File[] newValue) {
        File[] oldValue = value;
        value = newValue;
        if (!equal(value, oldValue)) {
            interview.updatePath(this);
            interview.setEdited(true);
        }
    }

    public boolean isValueValid() {
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
     * @see #setFilters
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
     * relative to the base directory (i.e. in or under it.)
     * @return true if all valid responses to this question should be
     * relative to the base directory
     * @see #setBaseRelativeOnly
     */
    public boolean isBaseRelativeOnly() {
        return baseRelativeOnly;
    }

    /**
     * Specify whether all valid responses to this question should be
     * relative to the base directory (i.e. in or under it.)
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
        if (o instanceof File[])
            setValue((File[])o);
        else if (o instanceof String)
            setValue(split((String)o));
    }

    /**
     * Break apart a string containing a white-space separate list of file
     * names into an array of individual files.
     * If the string is null or empty, an empty array is returned.
     * The preferred separator is a newline character;
     * if there are no newline characters in the string, then
     * (for backward compatibility) space is accepted instead.
     * @param s The string to be broken apart
     * @return An array of files determined from the parameter string.
     * @see #join
     */
    public static File[] split(String s) {
        if (s == null)
            return empty;

        char sep = (s.indexOf('\n') == -1 ? ' ' : '\n');

        Vector v = new Vector();
        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == sep) {
                if (start != -1)
                    v.addElement(new File(s.substring(start, i)));
                start = -1;
            } else
                if (start == -1)
                    start = i;
        }
        if (start != -1)
            v.addElement(new File(s.substring(start)));
        if (v.size() == 0)
            return empty;
        File[] a = new File[v.size()];
        v.copyInto(a);
        return a;
    }

    private static final File[] empty = { };

    /**
     * Save the value for this question in a dictionary, using
     * the tag as the key.
     * @param data The map in which to save the value for this question.
     */
    protected void save(Map data) {
        if (value != null)
            data.put(tag, join(value));
    }

    /**
     * Convert a list of filenames to a newline separated string.
     * @param ff an array of filenames
     * @return a string containing the filenames separated by newline
     * characters.
     * If there is just one filename, and if it contains space characters
     * in its path,
     * the list is terminated by a newline as well.
     * If the parameter array is null or empty, an empty string is returned.
     * @see #split
     */
    public static String join(File[] ff) {
        if (ff == null || ff.length == 0)
            return "";

        int l = ff.length - 1; // allow for spaces between words
        for (int i = 0; i < ff.length; i++)
            l += ff[i].getPath().length();

        StringBuffer sb = new StringBuffer(l);

        String ff0p = ff[0].getPath();
        sb.append(ff0p);

        if (ff.length == 1 && ff0p.indexOf(' ') != -1) {
            // if there is just one file, and if it contains space characters,
            // then force a newline character for subsequent split to recognize
            sb.append('\n');
        }
        else {
            // if there is more than one file, separate them with newlines
            for (int i = 1; i < ff.length; i++) {
                sb.append('\n');
                sb.append(ff[i].getPath());
            }
        }

        return sb.toString();
    }

    /**
     * Determine if two arrays of filenames are equal.
     * @param f1 the first array to be compared
     * @param f2 the other array to be compared
     * @return true if both arrays are null, or if neither are null and if
     * their contents match, element for element, in order
     */
    protected static boolean equal(File[] f1, File[] f2) {
        if (f1 == null || f2 == null)
            return (f1 == f2);

        if (f1.length != f2.length)
            return false;

        for (int i = 0; i < f1.length; i++) {
            if (f1[i] != f2[i])
                return false;
        }

        return true;
    }

    /**
     * The current (default or latest) response to this question.
     */
    protected File[] value;

    /**
     * The default response for this question.
     */
    private File[] defaultValue;

    private File baseDir;

    private boolean baseRelativeOnly;

    private FileFilter[] filters;

    private boolean duplicatesAllowed = true;
}
