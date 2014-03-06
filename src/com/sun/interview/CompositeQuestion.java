/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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

/**
 * A specialized base class for questions which have more than one value to be
 * manipulated by the user.
 * @since 4.0
 */
public abstract class CompositeQuestion extends Question
{
    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param baseTag A name to uniquely identify this question within its interview.
     */
    protected CompositeQuestion(Interview interview, String baseTag) {
        super(interview, baseTag);
    }

    /**
     * Create a question with no identifying tag.
     * @param interview The interview containing this question.
     */
    protected CompositeQuestion(Interview interview) {
        super(interview);
    }

    /**
     * Set the response to this question to the value represented by
     * a string-valued argument. Subtypes of Question will typically
     * have type-specific methods to set the value as well.
     * @param s A string containing a value value appropriate for the
     * particular type of question whose value is being set.
     * @throws Interview.Fault (retained for compatibility; should not be thrown)
     * @see #getStringValue
     */
    public abstract void setValue(String id, String s) throws Interview.Fault;
}
