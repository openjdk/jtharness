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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.sun.javatest.Status;
import com.sun.javatest.Test;
import com.sun.javatest.util.WriterStream;

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
 * <strong>{@code public Status methodName( )}</strong>
 * </blockquote>
 */

public class MultiTest implements Test {
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
         * Creates SetupException object which indicates that
         * this test is not applicable. The cases when it is needed
         * are rare, so please think twice whether you really need it.
         *
         * @param msg the string containing a comment
         */
        public static SetupException notApplicable(String msg) {
            SetupException e = new SetupException("Test not applicable: " + msg);
            e.passed = true;
            return e;
        }

        /**
         * Determines whether this SetupException signals failure or not.
         */
        public boolean isPassed() {
            return passed;

        }

        /**
         * Indicate whether this exception was the result of calling notApplicable(String).
         *
         * @serial
         */
        private boolean passed = false;
    }


    /**
     * Run the test contained in this object.
     * <p>
     * Implements Test.run using reflection to call test methods specified
     * in the testMethods array.  Each test method specified in the testMethods
     * array can take no arguments.
     * <p>
     * This method is a convenience wrapper around the primary run method
     * which takes PrintWriters: this variant takes PrintStreams and wraps
     * them into PrintWriters.
     *
     * @param argv Execute arguments passed in from either the
     *             command line or the execution harness.
     * @param log  Output stream for general messages from the tests.
     * @param ref  Output stream for reference output from the tests.
     * @return Overall status of running all of the test cases.
     * @see #decodeAllArgs
     * @see #init
     */
    public final Status run(String[] argv, PrintStream log, PrintStream ref) {
        PrintWriter pwLog = new PrintWriter(new OutputStreamWriter(log, StandardCharsets.UTF_8));
        PrintWriter pwRef = new PrintWriter(new OutputStreamWriter(ref, StandardCharsets.UTF_8));
        try {
            return run(argv, pwLog, pwRef);
        } finally {
            pwLog.flush();
            pwRef.flush();
        }
    }


    /**
     * Run the test contained in this object.
     * <p>
     * Implements Test.run using reflection to call test methods specified
     * in the testMethods array.  Each test method specified in the testMethods
     * array can take no arguments.
     *
     * <P>This method calls the decodeAllArgs method  with the value of argv to
     * decode command line arguments, and then calls the init method to perform
     * any other initialization.
     * To add parsing for new arguments you need to override the decodeArg method.
     * The init method may also be overridden in case you need additional
     * initialization.
     *
     * @param argv Execute arguments passed in from either the
     *             command line or the execution harness.
     * @param log  Output stream for general messages from the tests.
     *             Is assigned to this.log.
     * @param ref  Output stream for reference output from the tests.
     *             Is assigned to this.ref.
     * @return Overall status of running all of the test cases.
     * @see #decodeAllArgs
     * @see #init
     */
    public Status run(String[] argv, PrintWriter log, PrintWriter ref) {
        MultiStatus ms = new MultiStatus(log);

        // assign log and reference output streams
        this.ref = ref;
        this.log = log;

        Status initStatus = init(argv);
        if (testNotApplicable
                || (initStatus != null && initStatus.getType() != Status.PASSED)) {
            return initStatus;
        }

        /* Loop through the array of test cases
         * Each test case should have a method
         * with the same name that takes no arguments
         */
        if (testMethods == null) {
            return Status.failed("No test cases supplied");
        }

        for (int i = 0; i < testMethods.length; ++i) {
            Status status = null;

            if (testMethods[i] == null) {
                status = Status.failed(
                        "Test method is null for test case # " + i);
            } else {
                try {
                    Method m = testMethods[i];

                    if (excludeTestCases.contains(m.getName())) {
                        continue;
                    }

                    status = invokeTestCase(testMethods[i]);
                } catch (IllegalAccessException e) {
                    status = Status.failed("Could not execute test case: " +
                            testMethods[i]);
                } catch (InvocationTargetException e) {
                    printStackTrace(e.getTargetException());
                    status = Status.failed("Test case throws exception: " +
                            e.getTargetException().toString());
                } catch (RuntimeException e) {
                    printStackTrace(e);
                    status = Status.failed("Could not access the test case: " +
                            e.toString());
                } catch (ThreadDeath t) {
                    printStackTrace(t);
                    throw t;
                } catch (Throwable t) {
                    printStackTrace(t);
                    status = Status.failed("Unexpected Throwable: " + t);
                }
            }

            ms.add(testMethods[i].getName(), status);
        }

        return ms.getStatus();
    }


    /**
     * Parses the arguments passed to the test.
     * <p>
     * This method embodies the main for loop for all of the
     * execute arguments. It calls <CODE>decodeArg</CODE>
     * for successive arguments in the argv array.
     *
     * @param argv execute arguments from the test harness or from the
     *             command line.
     * @throws SetupException raised when an invalid parameter is passed,
     *                        or another error occurred.
     * @see #decodeArg
     */
    protected final void decodeAllArgs(String argv[]) throws SetupException {
        /* Please note, we do not increment i
         * that happens when decodeArg returns the
         * number of array elements consumed
         */
        for (int i = 0; i < argv.length; ) {
            int elementsConsumed = decodeArg(argv, i);
            if (elementsConsumed == 0) {
                // The argument was not recognized.
                throw new SetupException("Could not recognize argument: " + argv[i]);
            }
            i += elementsConsumed;
        }
    }


    /**
     * Decode the next argument in the argument array.
     * May be overridden to parse additional execute arguments.
     * <p>
     * The default behavior of decodeArg( String argv[], int index )
     * is to parse test case IDs starting from <CODE>index</CODE>
     * from execute arguments <CODE>argv</CODE>.
     * <P>The derived class may override this method to provide
     * parsing of additional arguments. However, it is recommended
     * for this method to return super.decodeArg() if it
     * does not recognize the argument. So it has to parse
     * specific for derived class arguments only.
     *
     * <P>The required syntax for using this method is the execute
     * argument <EM>-TestCaseID</EM> followed by a space and then a
     * space delimited list of one or more test case method names.
     * Using the test case method name <STRONG>ALL</STRONG> specifies
     * that all of the individual test case methods that match the
     * required test method signature should be executed.  The method
     * getAllTestCases() is called to gather the list of all methods
     * that match the test method signature.
     *
     * <P>Once the execute argument <EM>-TestCaseID</EM> is found, all
     * subsequent execute arguments will be treated as test case method
     * names until either the execute argument array is exhausted or
     * an execute argument that begins with <EM>-</EM> is found.
     *
     * @param argv  execute arguments from the test harness or from the
     *              command line
     * @param index current index into argv.
     * @throws SetupException raised when an invalid argument is passed,
     *                        or another error occurred.
     * @see #decodeAllArgs
     * @see #testMethods
     */
    protected int decodeArg(String argv[], int index) throws SetupException {
        if (argv[index].equals("-exclude")) {
            split(argv[index + 1], excludeTestCases);
            return 2;
        }

        if (argv[index].equals("-autoFlush")) {
            // turn on automatic stream flushing on println()
            ref = new PrintWriter(ref, true);
            log = new PrintWriter(ref, true);
            return 1;
        }

        if (!argv[index].equals("-TestCaseID")) {
            return 0;
        }

        /* consume elements until it is done
         * creating the array of test case id's
         * return the number of elements consumed
         */
        int i = index + 1;
        if (i < argv.length && argv[i].equals("ALL")) {
            getAllTestCases();
            return 2;
        }

        Vector<Method> tests = new Vector<>();
        while (i < argv.length && !argv[i].startsWith("-")) {
            tests.addElement(getTestCase(argv[i++]));
        }

        if (tests.size() <= 0) {
            throw new SetupException("No test case(s) specified");
        }

        testMethods = new Method[tests.size()];
        tests.copyInto(testMethods);

        return i - index;
    }

    private void printStackTrace(Throwable t) {
        PrintStream ps = Deprecated.createPrintStream(new WriterStream(log));
        t.printStackTrace(ps);
        ps.close();
    }

    private void split(String s, Vector<String> v) {
        int start = 0;
        for (int i = s.indexOf(','); i != -1; i = s.indexOf(',', start)) {
            v.addElement(s.substring(start, i));
            start = i + 1;
        }
        if (start != s.length()) {
            v.addElement(s.substring(start));
        }
    }

    /**
     * A setup method called after argument decoding is complete,
     * and before the test cases are executed. By default, it does
     * nothing; it may be overridden to provide additional behavior.
     *
     * @throws SetupException if processing should not continue.
     *                        This may be due to some inconsistency in the arguments,
     *                        or if it is determined the test should not execute for
     *                        some reason.
     */
    protected void init() throws SetupException {
    }


    /**
     * Creates a test case method using the name of the method.
     * <p>
     * This method uses reflection to find the method with given name
     * that implement the standard test case method signature:
     *
     * <BLOCKQUOTE>
     * <STRONG><CODE>public Status methodName( )</CODE></STRONG>
     * </BLOCKQUOTE>
     * <p>
     * If no such method could be found, a SetupException is thrown.
     *
     * <P><STRONG>NOTE</STRONG>: You will want to override this method
     * if there are methods in your test class that match the test
     * method signature that should <STRONG>not be executed</STRONG>
     * as part of the test.  As an example, see the SerializeTests
     * base class.
     *
     * @throws SetupException raised when no method with given name
     *                        and signature is found, or another error occurred.
     * @see #decodeArg
     */
    protected Method getTestCase(String testCase) throws SetupException {
        try {
            Method test = testClass.getMethod(testCase, testArgTypes);
            if (!Status.class.isAssignableFrom(test.getReturnType())) {
                throw new SetupException("Method for test case '" +
                        testCase + "' has wrong return type");
            }
            return test;
        } catch (NoSuchMethodException e) {
            throw new SetupException("Could not find test case: " + testCase);
        } catch (SecurityException e) {
            throw new SetupException("Failed during setup: " + e.toString());
        }
    }

    protected Status invokeTestCase(Method m)
            throws IllegalAccessException, InvocationTargetException {
        Object[] testArgs = {};
        return (Status) (m.invoke(this, testArgs));
    }


    /**
     * Generates a list of all test case methods.
     * <p>
     * This method uses reflection to examine all of the public
     * methods of the test class to find those methods that implement
     * the standard test case method signature:
     *
     * <BLOCKQUOTE>
     * <STRONG><CODE>public Status methodName( )</CODE></STRONG>
     * </BLOCKQUOTE>
     * <p>
     * If no methods matching this signature could be found, a
     * SetupException is thrown.  If at least one method
     * matching the signature is found, this method assigns that list
     * to the testMethods array.
     *
     * <P><STRONG>NOTE</STRONG>: You will want to override this method
     * if there are methods in your test class that match the test
     * method signature that should <STRONG>not be executed</STRONG>
     * as part of the test.  As an example, see the SerializeTests
     * base class.
     *
     * @throws SetupException raised when no methods matching the test
     *                        method signature is found, or another error occurred.
     * @see #testMethods
     */
    protected void getAllTestCases() throws SetupException {
        Vector<Method> tests = new Vector<>();
        Vector<Method> sortedTests = new Vector<>();
        Vector<Method> reversed = new Vector<>();

        try {
            /* Get public methods for this class
             * Loop through them to get methods that return Status
             * and have no parameters
             */
            Method[] methods = testClass.getMethods();
            for (int i = 0; i < methods.length; ++i) {

                Class<?>[] paramTypes = methods[i].getParameterTypes();
                Class<?> returnType = methods[i].getReturnType();
                if ((paramTypes.length == 0) &&
                        Status.class.isAssignableFrom(returnType)) {
                    tests.addElement(methods[i]);
                }
            }

            // Check if testcases should be run in sorted order
            // (For internal testing only)
            try {
                testcaseOrder = System.getProperty("multitest.testcaseOrder");
            } catch (SecurityException e) {
                log.println("Cannot read system property 'multitest.testcaseOrder': " + e);
            }

            if ((tests.size() > 0) && (testcaseOrder != null) &&
                    (testcaseOrder.equals("sorted") ||
                            testcaseOrder.equals("reverseSorted"))) {
                Object[] methodNameArray = new Object[tests.size()];
                Hashtable<String, Method> ht = new Hashtable<>();
                Method m;

                for (Enumeration<Method> e = tests.elements(); e.hasMoreElements(); ) {
                    m = e.nextElement();
                    ht.put(m.getName(), m);
                }

                int j = 0;
                for (Enumeration<Method> e = tests.elements(); e.hasMoreElements(); ) {
                    methodNameArray[j] = e.nextElement().getName();
                    j++;
                }

                Arrays.sort(methodNameArray);

                for (int i = 0; i < methodNameArray.length; i++) {
                    sortedTests.addElement(ht.get(methodNameArray[i]));
                }

                if (testcaseOrder.equals("reverseSorted")) {
                    reversed = reverse(sortedTests);
                    sortedTests = reversed;
                }
            }
        } catch (SecurityException e) {
            throw new SetupException("Failed during setup: " + e.toString());
        }

        /* Check size of vector, if <= 0, no methods match signature
         * if > 0, copy values into testMethods array
         */
        if (tests.size() <= 0) {
            throw new SetupException("No methods match signature: \"public Status methodName()\"");
        }

        testMethods = new Method[tests.size()];
        if ((testcaseOrder != null) &&
                (testcaseOrder.equals("sorted") ||
                        testcaseOrder.equals("reverseSorted"))) {
            sortedTests.copyInto(testMethods);
        } else {
            tests.copyInto(testMethods);
        }
    }

    // Reverse a vector containing methods to run
    public Vector<Method> reverse(Vector<Method> v) {
        Vector<Method> reversed = new Vector<>();

        for (int i = v.size() - 1; i >= 0; i--) {
            reversed.addElement(v.elementAt(i));
        }

        return reversed;
    }


    /**
     * Initialize the test from the given arguments.
     *
     * @deprecated Use decodeArg(String and init() instead.
     */
    protected Status init(String[] argv) {
        /* Decode test arguments and delegate all other
         * test setup to the init method provided by
         * derived classes
         */
        try {
            decodeAllArgs(argv);

            if (testMethods == null) {
                /* Assuming that all test cases should be run
                 * if no "-TestCaseID" argument is specified.
                 */
                getAllTestCases();
            }

            init();
            return null;
        } catch (SetupException e) {
            testNotApplicable = true;
            return (e.isPassed()
                    ? Status.passed(e.getMessage())
                    : Status.failed(e.getMessage()));
        }
    }


    /* Convenience variables, also added to improve performance
     */
    protected final Class<?> testClass = this.getClass();

    protected static final Class<?>[] testArgTypes = {};


    /**
     * Output to be logged to result file.
     * <p>
     * Output to this PrintWriter is not used during golden file comparison.
     * Also used to output the Status from each individual test case.
     */
    protected PrintWriter log;


    /**
     * Output that can be used as reference.
     * <p>
     * Output to this PrintWriter is used during golden file comparison.
     */
    protected PrintWriter ref;


    /**
     * The list of test case methods to be executed.
     * <p>
     * An array of test case methods to be executed. It is usually filled
     * up with the values by the decodeArg method.
     */
    protected Method testMethods[] = null;

    // Order of testcase execution (for internal testing only)
    private String testcaseOrder;

    /**
     * Formerly served as the list of test case methods to be executed.
     * <p>
     * Left for backward compatibility only. Due to be deleted soon.
     *
     * @deprecated
     */
    protected String testCases[] = null;

    /**
     * The set of test cases to be excluded.
     */
    protected Vector<String> excludeTestCases = new Vector<>();

    // may be set if SetupException is thrown during decodeArgs() or init
    private boolean testNotApplicable = false;
}
