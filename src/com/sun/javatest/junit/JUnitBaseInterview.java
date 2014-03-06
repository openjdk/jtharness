/*
 * $Id$
 *
 * Copyright (c) 2008, 2009, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.junit;

import com.sun.interview.Question;
import com.sun.javatest.Parameters;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.interview.BasicInterviewParameters;
import java.util.HashMap;

/**
 * A basic interview for JUnit test suite.  Doesn't require input from the user.
 */
public class JUnitBaseInterview extends BasicInterviewParameters implements Parameters.EnvParameters {

    /** Creates a new instance of JUnitBaseInterview */
    public JUnitBaseInterview() throws Fault {
        super("junit");
    }

    protected Question getEnvFirstQuestion() {
        return getEnvSuccessorQuestion();
    }

    public EnvParameters getEnvParameters() {
        return this;
    }

    public TestEnvironment getEnv() {
        try {
            return new TestEnvironment("junitenv", new HashMap(), "junit");
        }
        catch (TestEnvironment.Fault f) {
           f.printStackTrace();
           throw new RuntimeException();
        }
    }
}
