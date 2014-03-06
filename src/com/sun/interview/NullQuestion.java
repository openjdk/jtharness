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
package com.sun.interview;

import java.util.Map;
import java.util.ResourceBundle;

/**
 * A "null" question with no response. In effect, this
 * posts the text, which must simply be acknowledged.
 */
public abstract class NullQuestion extends Question
{
    /**
     * Create a null question.
     * @param interview The interview containing this question.
     */
    protected NullQuestion(Interview interview) {
        super(interview);
    }

    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     */
    protected NullQuestion(Interview interview, String tag) {
        super(interview, tag);
    }

    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     * @param level The "level" of this question.
     */
    protected NullQuestion(Interview interview, String tag, int level) {
        super(interview, tag);
        this.level = level;
    }

    /**
     * Set the heading level of this question.
     * This is roughly equivalent to heading levels in HTML, where heading 1
     * is the largest, for a document title or chapter.  As the number grows,
     * the heading is semantically finer grained.
     *
     * For compatibility purposes a question has heading level zero by
     * default, this is the JT Harness 3.x style of rendering.  Level 1
     * is the strongest heading level in JT Harness 4.x and later.  A
     * "none" heading level is also available, meaning that a null
     * question is requested, but has no more semantic importance than
     * any other question.
     *
     * @param val One of the level constants defined in this class.
     * @throws IllegalArgumentException If the parameter is out of range.
     * @since 4.0
     * @see #LEVEL_NONE
     * @see #LEVEL_1
     * @see #LEVEL_2
     * @see #LEVEL_3
     */
    public void setLevel(int val) {
        if (val < LEVEL_NONE || val > MAX_LEVEL)
            throw new IllegalArgumentException();

        level = val;
    }

    /**
     * Get the current heading level.
     * @return The heading level, as defined by one of this class' constants.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Clear any response to this question, resetting the value
     * back to its initial state. Since this question has no response,
     * there is no value, and so no action is performed.
     */
    public void clear() {
    }

    /**
     * A NullQuestion does not have a value, and so this method always
     * returns null.
     * @return null
     */
    public String getStringValue() {
        return null;
    }

    /**
     * Set the response to this question to the value represented by
     * a string-valued argument. For this question, no value is
     * appropriate and an exception is always thrown.
     * @throws UnsupportedOperationException always
     */
    public void setValue(String ignore) {
        throw new UnsupportedOperationException();
    }

    /**
     * A NullQuestion does not have a value, and so this method always
     * returns true.
     * @return true
     */
    public boolean isValueValid() {
        return true;
    }


    /**
     * A NullQuestion does not have a value, and so this method always
     * returns true.
     * @return true
     */
    public boolean isValueAlwaysValid() {
        return true;
    }

    /**
     * Load the value for this question from a dictionary, using
     * the tag as the key.
     * @param data ignored
     */
    protected void load(Map data) {
    }

    /**
     * Save the value for this question in a dictionary, using
     * the tag as the key. Since there is no value, this method is a no-op.
     * @param data ignored
     */
    protected void save(Map data) {
    }

    public static final int LEVEL_NONE = -1;
    public static final int LEVEL_LEGACY = 0;
    public static final int LEVEL_1 = 1;
    public static final int LEVEL_2 = 2;
    public static final int LEVEL_3 = 3;
    private static final int MAX_LEVEL = LEVEL_3;

    private int level = LEVEL_LEGACY;      // default, pre JT4.0 rendering
    private static final ResourceBundle i18n = Interview.i18n;
}
