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
package com.sun.javatest.interview;

import com.sun.interview.Question;
import com.sun.javatest.Parameters;
import com.sun.javatest.TestSuite;

/**
 * A configuration interview for legacy test suites that use environment
 * (.jte) files to define the environment used to run tests.
 */
public class LegacyParameters extends BasicInterviewParameters
{
    /**
     * Create a configuration interview for legacy tests suites.
     * @throws Interview.Fault if there is a problem instantiating the
     * interview.
     */
    public LegacyParameters()
        throws Fault
    {
        super("jtwiz");
        setHelpSet("/com/sun/javatest/moreInfo/moreInfo.hs");
        setResourceBundle("i18n");
        iEnvironment = new EnvironmentInterview(this);
    }

    /**
     * Create a configuration interview for legacy tests suites.
     * @param testSuite The test suite for which this interview applies
     * @throws Interview.Fault if there is a problem instantiating the
     * interview.
     */
    public LegacyParameters(TestSuite testSuite)
        throws Fault
    {
        super("jtwiz", testSuite);
        setHelpSet("/com/sun/javatest/moreInfo/moreInfo.hs");
        setResourceBundle("i18n");
        iEnvironment = new EnvironmentInterview(this);
    }

    //--------------------------------------------------------------------------

    public Parameters.EnvParameters getEnvParameters() {
        return iEnvironment;
    }

    /**
     * Get the first question to be asked concerning the environment to be
     * set up and used for each test to be run. For legacy test suites,
     * questions are asked to determine environment files to be read
     * and the name of an environment to be found in those files.
     * @return the first question to be asked concerning the environment to be
     * set up and used for each test to be run.
     * @see #getEnvSuccessorQuestion
     */
    protected Question getEnvFirstQuestion() {
        return callInterview(iEnvironment, getEnvSuccessorQuestion());
    }

    //--------------------------------------------------------------------------

    private EnvironmentInterview iEnvironment;
}
