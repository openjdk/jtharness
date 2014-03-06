/*
 * $Id$
 *
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

class InterviewQuestion extends Question
{
    /**
     * An internal class used to indicate that a nested interview should
     * be executed.
     * @param interview The interview for this question.
     * @param targetInterview The subinterview to be invoked
     * @param next The next question to be invoked when the subinterview is completed
     * @throws NullPointerException if either argument is null
     */
    InterviewQuestion(Interview interview, Interview targetInterview, Question next) {
        super(interview);

        if (targetInterview == null)
            throw new NullPointerException();

        if (next == null)
            throw new NullPointerException();

        this.targetInterview = targetInterview;
        this.next = next;
    }

    /**
     * Get the nested interview to be called.
     * @return the nested interview
     */
    Interview getTargetInterview() {
        return targetInterview;
    }

    /**
     * Get the next question in the series. This is the question
     * that follows the nested interview.
     * @return The question that follows the interview
     */
    protected Question getNext() {
        return next;
    }

    public boolean isValueValid() {
        return true;
    }

    public boolean isValueAlwaysValid() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    /**
     * Clear any response to this question, resetting the value
     * back to its initial state. Since this question
     * is just used to mark a call to a sub-interview,
     * there is no value, and so no action is performed.
     */
    public void clear() {
    }

    public String getStringValue() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the response to this question to the value represented by
     * a string-valued argument. For this question, no value is
     * appropriate and an exception is always thrown.
     */
    public void setValue(String ignore) {
        throw new UnsupportedOperationException();
    }

    /**
     * Load the state, if any, for this question from a dictionary.
     * Since this question is just used to wrap an interview,
     * there is no value, and so no action is performed.
     * @param data The map from which to load the state for this question.
     */
    protected void load(Map data) {
        // no need to super.load(data)
    }

    /**
     * Save the state, if any, for this question to a dictionary.
     * Since this question is just used to wrap an interview,
     * there is no value, and so no action is performed.
     * @param data The map from which to load the state for this question.
     */
    protected void save(Map data) {
        // no need to super.save(data)
    }

    private Interview targetInterview;
    private Question next;
}
