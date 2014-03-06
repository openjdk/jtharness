/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.interview;

import com.sun.javatest.TestSuite;

/**
 * A basic implementation of InterviewParameters that uses standard
 * interviews for all the various interview sections, except the environment
 * section, which remains to be implemented by subtypes.
 * @deprecated Use BasicInterviewParameters
 */
public abstract class DefaultInterviewParameters extends BasicInterviewParameters
{
    /**
     * Create a BasicInterviewParameters object.
     * The test suite for which this interview applies should be set
     * with setTestSuite.
     * @param tag the tag used to qualify questions in this interview
     * @throws Interview.Fault if there is a problem creating this object
     */
    protected DefaultInterviewParameters(String tag)
        throws Fault
    {
        super(tag);
    }

    /**
     * Create a BasicInterviewParameters object.
     * @param tag the tag used to qualify questions in this interview
     * @param ts The test suite to which this interview applies.
     * @throws Interview.Fault if there is a problem creating this object
     */
    protected DefaultInterviewParameters(String tag, TestSuite ts)
        throws Fault
    {
        super(tag, ts);
    }
}
