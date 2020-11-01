/*
 * $Id$
 *
 * Copyright (c) 2001, 2020, Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.tck.lib.autd2;

import com.oracle.tck.lib.autd2.processors.Processor;

import java.io.PrintWriter;
import java.util.*;

/**
 * Context for the whole testgroup.
 * Accumulates all the information needed for successful testgroup processing.
 */
public class TestGroupContext extends Context<Processor.TestGroupProcessor, TestGroupContext.TestGroupLifePhase> {

    /**
     * Various life phases of a testgroup.
     */
    public static enum TestGroupLifePhase implements LifePhase<TestGroupContext> {
        LOGGING_INIT,
        ARGUMENT_PROCESSING,
        BEFORE_TESTGROUP,
        TESTCASE_ADDING,
        TESTCASE_REMOVING,
        RUNNING_TESTCASES,
        AFTER_TESTGROUP
    }

    private static final String MT_TC_ORDER_PROP_NAME = "multitest.testcaseOrder";
    private static final String MT_TC_ORDER_PROP_VALUE_REVERSE = "reverseSorted";
    private static final boolean reverseTestCaseOrder =
            MT_TC_ORDER_PROP_VALUE_REVERSE.equals(System.getProperty(MT_TC_ORDER_PROP_NAME));

    private static final Comparator<String> TEST_METHOD_COMPARATOR = (firstName, secondName) -> {
            int result = firstName.compareTo(secondName);
            if (reverseTestCaseOrder) { result *= -1; }
            return result;
    };

    private PrintWriter log;
    private PrintWriter ref;
    private final TreeMap<String, TestCaseContext> tcContexts = new TreeMap<>(TEST_METHOD_COMPARATOR);
    private Object testGroupInstance;
    private String[] executionArgs;
    private Set<Processor<?,?>> allUsedProcessors;
    private boolean quietOutput = false;

    /**
     * Switches the printing to output to quiet mode
     */
    public void setQuietOutput() {
        this.quietOutput = true;
    }

    /**
     * Removes particular testcase context from the list of contexts.
     */
    public void removeTestCaseContext(String excludedTestCase) {
        tcContexts.remove(excludedTestCase);
    }

    /**
     * Installs a 'log' stream to this context.
     */
    public void setLog(PrintWriter log) {
        this.log = log;
    }

    /**
     * Installs a 'ref' stream to this context.
     */
    public void setRef(PrintWriter ref) {
        this.ref = ref;
    }

    /**
     * Returns a new macro-testcase result accumulator instance.
     */
    @Override
    protected TestResultAccumulator createTestResultAccumulator() {
        return new TestResultAccumulator() {
            @Override
            TestResult getFinalResult() {
                String summary = getSummaryPrefix();
                int numberOfFailed = failedTestNames.size();
                if (numberOfFailed > 0) {
                    if (numberOfFailed != getTotalTestCount()) {
                        return TestResult.failure(summary + FAILED + failedTestNames);
                    } else {
                        return TestResult.failure(summary);
                    }
                }
                // no tests failed

                return allTestsAreNotApplicable() ? new InapplicableTestResult(summary) : TestResult.ok(summary);
            }
        };
    }

    /**
     * Returns testgroup instance that is being run.
     */
    @Override
    public Object getTestGroupInstance() {
        return testGroupInstance;
    }

    /**
     * Returns testgroup execution arguments.
     */
    @Override
    public String[] getExecutionArgs() {
        return this.executionArgs != null
                ? Arrays.copyOf(executionArgs, executionArgs.length)
                : null;
    }

    /**
     * Adds a testcase context to the list of testcases that should be run.
     */
    public void addTestCaseContext(TestCaseContext tc) {
        String testCaseName = tc.getTestCaseName();
        if(tcContexts.get(testCaseName) != null)  {
            Class<?> existing = tcContexts.get(testCaseName).getTestCaseMethod().getDeclaringClass();
            Class<?> newClass = tc.getTestCaseMethod().getDeclaringClass();
            if (existing == newClass) {
                // if methods are overloaded
                throw new IllegalArgumentException(
                        "Overloaded testcases are not supported. Method \""
                                + testCaseName + "\" is overloaded.");
            } else {
                if (existing.isAssignableFrom(newClass)) {
                    // replacing with the most lately overridden method
                    tcContexts.put(testCaseName, tc);
                }
            }
        } else {
            tcContexts.put(testCaseName, tc);
        }
    }

    /**
     * Installs a testgroup instance to be run into this context.
     */
    void setTestGroupInstance(Object testGroupInstance) {
        this.testGroupInstance = testGroupInstance;
    }

    /**
     * Installs testgroup execution arguments into this context.
     */
    void setExecutionArgs(String[] executionArgs) {
        this.executionArgs = executionArgs;
    }

    /**
     * Returns a collection of testcase contexts to iterate over.
     */
    public Collection<TestCaseContext> getTestCaseContexts() {
        return tcContexts.values();
    }

    /**
     * Sets all the processors that are going to be used
     * for running this testgroup.
     */
    public void setAllUsedProcessors(Set<Processor<?,?>> allUsedProcessors) {
        this.allUsedProcessors = allUsedProcessors;
    }

    /**
     * Returns all the processors that are used
     * for running this testgroup.
     */
    public Set<Processor<?,?>> getAllUsedProcessors() {
        return allUsedProcessors;
    }

    @Override
    public void addExecutionResult(String tcName, TestResult result) {
        super.addExecutionResult(tcName, result);
        PrintWriter log = getLog();
        if (log != null && (!result.isOK() || !quietOutput)) {
            log.println(tcName + ": " + result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrintWriter getLog() {
        return log;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrintWriter getRef() {
        return ref;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printlnToLog(String message) {
        if (log != null) {
            log.println(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printlnToRef(String message) {
        if (ref != null) {
            ref.println(message);
        }
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void printStackTraceToLog(Throwable thrownException) {
        if (log != null) {
            thrownException.printStackTrace(log);
        }
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void printStackTraceToRef(Throwable thrownException) {
        if (log != null) {
            thrownException.printStackTrace(ref);
        }
    }

}
