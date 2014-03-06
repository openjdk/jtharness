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
package com.sun.javatest.lib;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import com.sun.javatest.Status;
import com.sun.javatest.Test;

/**
 * A handler for the set of test cases in a test.
 * Test cases are those methods with no args that return a
 * {@link com.sun.javatest.Status status}.
 * Test cases can be explicitly selected into or excluded from the
 * set.
 */
public class TestCases {
    /**
     * Exception used to report internal errors.
     */
    public static class Fault extends Exception {
        /**
         * Construct a new Fault object that signals failure
         * with a corresponding message.
         *
         * @param s the string containing a comment
         */
        public Fault(String s) {
            super(s);
        }
    }

    /**
     * Create an object to handle the test cases of the given test.
     * @param t The test containing the test cases.
     * @param log An optional stream to which to write log messages.
     *   Use null if messages are not desired.
     */
    public TestCases(Test t, PrintWriter log) {
        test = t;
        this.log = log;
        testClass = t.getClass();
    }

    /**
     * Explicitly select a set of test cases by name. Subsequent calls
     * are cumulative; if no selections are made, the default is all
     * test cases are selected. Excluded tests will be excluded from the
     * selection; the order of select and exclude calls does not matter.
     * @param testCaseNames a comma-separated list of test cases names.
     * Each name must identify a method in the test object, that takes
     * no arguments and returns a {@link com.sun.javatest.Status status}.
     * @throws TestCases.Fault if any of the test case names are invalid.
     */
    public void select(String testCaseNames) throws Fault {
        select(split(testCaseNames));
    }


    /**
     * Explicitly select a set of test cases by name. Subsequent calls
     * are cumulative; if no selections are made, the default is all
     * test cases are selected. Excluded tests will be excluded from the
     * selection; the order of select and exclude calls does not matter.
     * @param testCaseNames an array of test cases names.
     * Each name must identify a method in the test object, that takes
     * no arguments and returns a {@link com.sun.javatest.Status status}.
     * @throws TestCases.Fault if any of the test case names are invalid.
     */
    public void select(String[] testCaseNames) throws Fault  {
        for (int i = 0; i < testCaseNames.length; i++) {
            String t = testCaseNames[i];
            selectedCases.put(t, getTestCase(t));
        }
    }


    /**
     * Explicitly exclude a set of test cases by name. Subsequent calls
     * are cumulative; by default, no test cases are excluded.
     * @param testCaseNames a comma-separated list of test cases names.
     * Each name must identify a method in the test object, that takes
     * no arguments and returns a {@link com.sun.javatest.Status status}.
     * @throws TestCases.Fault if any of the test case names are invalid.
     */
    public void exclude(String testCaseNames) throws Fault  {
        exclude(split(testCaseNames));
    }


    /**
     * Explicitly exclude a set of test cases by name. Subsequent calls
     * are cumulative; by default, no test cases are excluded.
     * @param testCaseNames an array of test cases names.
     * Each name must identify a method in the test object, that takes
     * no arguments and returns a {@link com.sun.javatest.Status status}.
     * @throws TestCases.Fault if any of the test case names are invalid.
     */
    public void exclude(String[] testCaseNames) throws Fault  {
        for (int i = 0; i < testCaseNames.length; i++) {
            String t = testCaseNames[i];
            excludedCases.put(t, getTestCase(t));
        }
    }


    /**
     * Return an enumeration of the selected test cases, based on the
     * select and exclude calls that have been made, if any.
     * @return An enumeration of the test cases.
     */
    public Enumeration enumerate() {
        Vector v = new Vector();
        if (selectedCases.isEmpty()) {
            Method[] methods = testClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                if (excludedCases.get(m.getName()) == null) {
                    Class[] paramTypes = m.getParameterTypes();
                    Class returnType = m.getReturnType();
                    if ((paramTypes.length == 0) && Status.class.isAssignableFrom(returnType))
                        v.addElement(m);
                }
            }
        }
        else {
            for (Enumeration e = selectedCases.elements(); e.hasMoreElements(); ) {
                Method m = (Method)(e.nextElement());
                if (excludedCases.get(m.getName()) == null)
                    v.addElement(m);
            }
        }
        return v.elements();
    }


    /**
     * Invoke each of the selected test cases, based upon the select and exclude
     * calls that have been made, if any.
     * If the test object provides a public method
     * {@link com.sun.javatest.Status}invokeTestCase({@link java.lang.reflect.Method})
     * that method will be called to invoke the test cases; otherwise, the test
     * cases will be invoked directly.
     * It is an error if no test cases are selected, (or if they have all been excluded.)
     * @return the combined result of executing all the test cases.
     */
    public Status invokeTestCases() {
        // see if test object provides  Status invokeTestCase(Method m)
        Method invoker;
        try {
            invoker = testClass.getMethod("invokeTestCase", new Class[] {Method.class});
            if (!Status.class.isAssignableFrom(invoker.getReturnType()))
                invoker = null;
        }
        catch (NoSuchMethodException e) {
            invoker = null;
        }

        MultiStatus ms = new MultiStatus(log);
        for (Enumeration e = enumerate(); e.hasMoreElements(); ) {
            Method m = (Method)(e.nextElement());
            Status s;
            try {
                if (invoker != null)
                    s = (Status)invoker.invoke(test, new Object[] {m});
                else
                    s = (Status)m.invoke(test, noArgs);
            }
            catch (IllegalAccessException ex) {
                s = Status.failed("Could not access test case: " + m.getName());
            }
            catch (InvocationTargetException ex) {
                printStackTrace(ex.getTargetException());
                s = Status.failed("Exception from test case: " +
                                       ex.getTargetException().toString());
            }
            catch (ThreadDeath t) {
                printStackTrace(t);
                throw t;
            }
            catch (Throwable t) {
                printStackTrace(t);
                s = Status.failed("Unexpected Throwable: " + t);
            }

            ms.add(m.getName(), s);
        }
        if (ms.getTestCount() == 0)
            return Status.passed("Test passed by default: no test cases executed.");
        else
            return ms.getStatus();
    }

    /**
     * Print a stack trace for an exception to the log.
     * @param t The Throwable for which to print the trace
     */
    protected void printStackTrace(Throwable t) {
        if (log != null)
            t.printStackTrace(log);
    }

    /**
     * Look up a test case in the test object.
     * @param name the name of the test case; it must identify a method
     *          Status name()
     * @return the selected method
     * @throws Fault if the name does not identify an appropriate method.
     */
    private Method getTestCase(String name) throws Fault {
        try {
            Method m = testClass.getMethod(name, noArgTypes);
            if (!Status.class.isAssignableFrom(m.getReturnType()))
                throw new Fault("Method for test case '" + name + "' has wrong return type" );
            return m;
        }
        catch (NoSuchMethodException e) {
            throw new Fault("Could not find test case: " + name);
        }
        catch (SecurityException e) {
            throw new Fault(e.toString());
        }
    }

    private String[] split(String s) {
        Vector v = new Vector();
        int start = 0;
        for (int i = s.indexOf(','); i != -1; i = s.indexOf(',', start)) {
            v.addElement(s.substring(start, i));
            start = i + 1;
        }
        if (start != s.length())
            v.addElement(s.substring(start));
        String[] ss = new String[v.size()];
        v.copyInto(ss);
        return ss;
    }

    private Object test;
    private Class testClass;
    private Hashtable selectedCases = new Hashtable();
    private Hashtable excludedCases = new Hashtable();
    private PrintWriter log;

    private static final Object[] noArgs = { };
    private static final Class[] noArgTypes = { };
}
