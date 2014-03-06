/*
 * $Id$
 *
 * Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.Status;

import java.io.PrintWriter;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Class to execute tests using the JUnit annotation style of test execution.
 * Or more accurately, JUnit 4.x style execution rather than 3.x style.  The
 * JUnitCore class is used to execute the test described in the TestDescription.
 */
public class JUnitAnnotationMultiTest extends JUnitMultiTest {

    public JUnitAnnotationMultiTest(ClassLoader cl) {
        super(cl);
    }

    public Status run(String[] argv, PrintWriter stdout, PrintWriter stderr) {
        this.log = stderr;
        this.ref = stdout;
        setup(argv[0]);
        return run0(argv);
    }

    /**
     * Common method for running the test, used by all entry points.
     */
    public Status run0(String[] argv) {
        JUnitCore core = new JUnitCore();
        Result junitresult = core.run(testCaseClass);

        if (junitresult.wasSuccessful())
            return Status.passed("All test cases passed.");
        else
            return Status.failed("Test cases failed: " + junitresult.getFailureCount());
    }


    /**
     * Entry point for direct execution, not used by the harness.
     */
    public static void main(String args[]) {
        String executeClass = System.getProperty("javaTestExecuteClass");
        JUnitAnnotationMultiTest multiTest = new JUnitAnnotationMultiTest(ClassLoader.getSystemClassLoader());
        multiTest.setup(executeClass);
        multiTest.run0(args);
    }

    /**
     * Entry point for standalone mode.
     */
    protected void setup(String executeClass) {

        try {
            Class junitTestCaseClass = getClassLoader().loadClass(executeClass);
            testCaseClass = junitTestCaseClass;


        } catch (ClassNotFoundException e){
            log.println("Cannot find test: " + executeClass + " (" + exceptionToString(e) + ")");
        }
    }

    protected void printStackTrace(Throwable t) {
        t.printStackTrace(log);
    }

    protected Class testCaseClass;
}
