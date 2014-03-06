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
package com.sun.javatest.lib;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import com.sun.javatest.lib.TestCases;
import com.sun.javatest.Status;
import com.sun.javatest.Test;

/**
 * Base class for tests with multiple sub test cases.
 * This base class implements the standard com.sun.javatest.Test
 * features so that you can provide the additional test cases without concern about
 * the boilerplate needed to execute individual test case methods.
 *
 * <P>You must add individual test case methods to your derived test class
 * to create a useful test class. Each test case method must take no
 * arguments.  If you need to pass an argument into a test method, you should
 * design a wrapper test case to calculate the argument values and then call
 * the test method with the correct arguments.  The test case methods must
 * implement this interface:
 * <blockquote>
 * <strong><code>public Status methodName( )</code></strong>
 * </blockquote>
 *
 * @see com.sun.javatest.Test
 * @see com.sun.javatest.lib.TestCases
 */
public class MultiTest implements Test
{
    /**
     * This exception is thrown when a problem occurs initializing the test.
     * It may also be used to indicate that the test is not applicable in the
     * current circumstances and should not be run.
     */
    public static class SetupException extends Exception {
        /**
         * Construct a new SetupException object that signals failure
         * with a corresponding message.
         *
         * @param s the string containing a comment
         */
        public SetupException(String s) {
            super(s);
        }

        /**
         * Creates a SetupException object which indicates that
         * this test is not applicable. The cases when it is needed
         * are rare, so please think twice whether you really need it.
         *
         * @param msg a detail string, explaining why the test is "not applicable".
         * @return an exception object that indicates the test should not be run
         *   because it is not applicable.
         */
        public static SetupException notApplicable(String msg) {
            SetupException e = new SetupException("Test not applicable: " + msg);
            e.passed = true;
            return e;
        }

        /**
         * Determines whether this SetupException signals failure or not.
         * @return true if and only if the test is not applicable and should be
         * deemed to have "passed, by default".
         *
         */
        public boolean isPassed() {
            return passed;

        }

        /**
         * Indicate whether this exception was the result of calling {@link #notApplicable}.
         * @serial
         */
        private boolean passed = false;
    }


    /**
     * Run the test cases contained in this object. The test cases are determined
     * and invoked via reflection. The set of test cases can be specified with
     * -select case1,case2,case3... and/or restricted with -exclude case1,case2,case3...
     *
     * @see #decodeAllArgs
     * @see #init
     *
     * @param args Execute arguments passed in from either the
     *             command line or the execution harness.
     * @param log Output stream for general messages from the tests.
     * @param ref Output stream for reference output from the tests.
     * @return Overall status of running all of the test cases.
     */
    public Status run(String[] args, PrintWriter log, PrintWriter ref) {
        this.log = log;
        this.ref = ref;
        testCases = new TestCases(this, log);

        Status initStatus = init(args);
        if (testNotApplicable
            || (initStatus != null && initStatus.getType( ) != Status.PASSED)) {
            return initStatus;
        }

        return testCases.invokeTestCases();
    }

    /**
     * Run the test cases contained in this object
     *
     * This method is a convenience wrapper around the primary run method
     * which takes PrintWriters: this variant takes PrintStreams and wraps
     * them into PrintWriters.
     *
     * @see #decodeAllArgs
     * @see #init
     *
     * @param argv Execute arguments passed in from either the
     *             command line or the execution harness.
     * @param log Output stream for general messages from the tests.
     * @param ref Output stream for reference output from the tests.
     * @return Overall status of running all of the test cases.
     */
    public final Status run(String[] argv, PrintStream log, PrintStream ref) {
        PrintWriter pwLog = new PrintWriter(new OutputStreamWriter(log));
        PrintWriter pwRef = new PrintWriter(new OutputStreamWriter(ref));
        try {
            return run(argv, pwLog, pwRef);
        }
        finally {
            pwLog.flush();
            pwRef.flush();
        }
    }


    /**
     *
     * Initialize the test from the given arguments. The arguments will
     * be passed to <code>decodeAllArgs</code>, and then <code>init()</code>
     * will be called.
     *
     * @param args The arguments for the test, passed to <code>decodeArgs</code>.
     * @return null if initialization is successful, or a status indicating why
     * initialization was not successful.
     *
     * @see #decodeAllArgs
     * @see #decodeArg
     * @see #init()
     *
     * @deprecated Use <code>decodeArg(String)</code> and <code>init()</code> instead.
     */
    protected Status init(String[] args) {
        try {
            decodeAllArgs(args);
            init();
            return null;
        }
        catch (SetupException e) {
            testNotApplicable = true;
            return (e.isPassed()
                    ? Status.passed(e.getMessage())
                    : Status.failed(e.getMessage()) );
        }
    }

    /**
     * A setup method called after argument decoding is complete,
     * and before the test cases are executed. By default, it does
     * nothing; it may be overridden to provide additional behavior.
     *
     * @throws MultiTest.SetupException if processing should not continue.
     * This may be due to some inconsistency in the arguments,
     * or if it is determined the test should not execute for
     * some reason.
     */
    protected void init() throws SetupException { }

    /**
     * Parses the arguments passed to the test.
     *
     * This method embodies the main loop for all of the test's arguments.
     * It calls <code>decodeArg</code> for successive arguments in the
     * argument array.
     *
     * @param args arguments passed to the test.
     *
     * @throws MultiTest.SetupException raised when an invalid parameter is passed,
     * or another error occurred.
     *
     * @see #decodeArg
     */
    protected final void decodeAllArgs(String args[]) throws SetupException {
        int i = 0;
        while (i < args.length) {
            int elementsConsumed = decodeArg(args, i);
            if (elementsConsumed == 0 ) {
                // The argument was not recognized.
                throw new SetupException("Could not recognize argument: " + args[i]);
            }
            i += elementsConsumed;
        }
    }

    /**
     * Decode the next argument in the argument array.  This will typically be
     * overridden by subtypes that wish to decode additional arguments. If an
     * overriding method does not recognize an argument, it should return
     * <code>super.decodeArg(args, index)</code> to give supertypes a change
     * to decode the argument as well.
     *
     * @param args The array containing all the arguments
     * @param index The position of the next argument to be decoded.
     * @return the number of elements in the array were "consumed" by this call.
     *
     * @throws MultiTest.SetupException is there is a problem decoding the
     *   argument.
     */
    protected int decodeArg(String[] args, int index) throws SetupException {
        try {
            if (args[index].equals("-select") && index + 1 < args.length) {
                testCases.select(args[index + 1]);
                return 2;
            }
            else if (args[index].equals("-exclude") && index + 1 < args.length) {
                testCases.exclude(args[index + 1]);
                return 2;
            }
            // support -TestCaseID for historical compatibility
            else if (args[index].equals("-TestCaseID")) {
                if (index + 1 < args.length && args[index + 1].equals("ALL")) {
                    // ignore -TestCaseID ALL, since it is the default
                    return 2;
                }
                else {
                    int i;
                    for (i = index + 1; i < args.length && !args[i].startsWith("-"); i++)
                        testCases.select(args[i]);
                    return (i - index);
                }

            }
            else if (args[index].equals("-autoFlush")) {
                ref = new PrintWriter(ref, true);
                log = new PrintWriter(ref, true);
                return 1;
            }
            else
                return 0;
        }
        catch (TestCases.Fault e) {
            throw new SetupException(e.getMessage());
        }
    }

    /**
     * Default way to invoke a specified test case.
     * @param m The method to be invoked.
     * @return The result of invoking the specified test case.
     * @throws IllegalAccessException if there was a problem accessing the specified method
     * @throws InvocationTargetException if the specified method threw an exception when
     * it was invoked.
     */
    protected Status invokeTestCase(Method m)
                throws IllegalAccessException, InvocationTargetException {
        Object[] testArgs = { };
        return (Status) (m.invoke(this, testArgs));
    }

    // the set of test cases to be executed
    private TestCases testCases;

    // may be set if SetupException is thrown during decodeArgs() or init
    private boolean testNotApplicable;

    /**
     * Output to be logged to result file.
     */
    protected PrintWriter ref;

    /**
     * Output to be logged to result file.
     */
    protected PrintWriter log;
}
