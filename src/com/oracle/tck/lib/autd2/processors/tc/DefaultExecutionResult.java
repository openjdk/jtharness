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

package com.oracle.tck.lib.autd2.processors.tc;

import com.oracle.tck.lib.autd2.*;
import com.oracle.tck.lib.autd2.processors.InterestedInAnnotations;
import com.oracle.tck.lib.autd2.processors.Processor;
import com.sun.tck.lib.AssertionFailedException;
import com.sun.tck.lib.NotApplicableException;
import com.sun.tck.lib.TestFailedException;
import com.sun.tck.lib.tgf.SomethingIsWrong;
import com.oracle.tck.lib.autd2.TestResult;
import com.sun.tck.test.TestCase;


import java.lang.reflect.Method;
import java.util.Arrays;

import static java.text.MessageFormat.format;

/**
 * Processor is responsible for handling the default
 * execution result - no exceptions expected, nothing special.
 */
@InterestedInAnnotations(TestCase.class)
public class DefaultExecutionResult extends Processor.TestCaseProcessor {

    // TODO combine this class with ExpectedExceptionsResult processing - create common superclass

    private static final TestResult OK_RESULT = TestResult.ok("OK");

    /**
     * {@inheritDoc}
     */
    @Override
    public TestCaseContext.TestCaseLifePhase[] getLifePhasesInterestedIn() {
        return new TestCaseContext.TestCaseLifePhase[]{TestCaseContext.TestCaseLifePhase.PROCESSING_RESULT};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PhaseOwnership getPhaseOwnership(TestCaseContext.TestCaseLifePhase interestedLifePhase) {
        return PhaseOwnership.ONE_PER_PHASE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasHigherPriorityThan(Processor<TestCaseContext, TestCaseContext.TestCaseLifePhase> anotherProc) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Readiness isReadyToWork(TestCaseContext.TestCaseLifePhase currentPhase, TestCaseContext context) {
        if (context.getTestCaseResult() != null) {
            return Readiness.READY;
        } else {
            return Readiness.NOTHING_FOR_ME;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(TestCaseContext.TestCaseLifePhase lifePhase, TestCaseContext context) {

        TestResult result;
        Throwable t = AUTD2Utils.unwrapCoreExceptionFromITEs(context);

        if (t == null) {
            Object testCaseReturnedResult = context.getTestCaseResult().getResult();
            if (testCaseReturnedResult != null) {
                // setting tight policy for what a testcase method can return by default
                // to catch mistakes since a non-void return value is not processed
                // anyhow by this processor and would be basically lost
                // even if the returned result reflects a failure.
                String nonVoidResult =
                    format("Testcase \"{0}\" returned unrecognized value {1}" +
                            ", but it is expected to return void", context.getTestCaseName(), testCaseReturnedResult);
                result = TestResult.failure(nonVoidResult);
            } else {
                result = OK_RESULT;
            }
        } else {
            if (t instanceof ClassCastException ||
                t instanceof IllegalArgumentException ) {
                // todo share this with exp exceptions processor
                result = TestResult.failure(
                        unmatchedArgsExceptionThrown(t.getClass().getSimpleName(),
                                context, t, context.getTestCaseMethod(), context.getTestCaseInvocationArgValues()));
            } else if (t instanceof SomethingIsWrong) {
                // todo share this with exp exceptions processor
                context.printlnToLog(t.getMessage());
                result = TestResult.failure(t.getMessage());
            } else if (t instanceof NotApplicableException) {
                // todo share this with exp exceptions processor
                String message = t.getMessage();
                context.recordNotApplicable();
                result = new InapplicableTestResult(
                        "Not applicable." + (message != null ? " Reason: " + message : ""), message);
            } else if (t instanceof TestFailedException) {
                testFailed(context.getTestCaseNameWithIndex(), t, context);
                result = TestResult.failure(t.getMessage());
            } else {
                String unexpectedThrown =
                        format("Testcase \"{0}\" has thrown an unexpected exception {1}", context.getTestCaseNameWithIndex(), t);
                context.getParentContext().printlnToLog(unexpectedThrown);
                context.getParentContext().printStackTraceToLog(t);
                result = TestResult.failure(unexpectedThrown);
            }
        }

        context.addExecutionResult(context.getTestCaseName(), result);
        context.clearTestCaseResult();
    }

    // todo share with another processor
    /**
     * {@inheritDoc}
     */
    static String unmatchedArgsExceptionThrown(String exceptionName, Context<?, ?> c, Throwable iae, Method method, Object... args) {
        final String message = format(
                exceptionName + " was thrown when data values {0} were passed to method \"{1}\" that has parameter types {2}",
                Arrays.deepToString(args), method.getName(), Arrays.toString(method.getParameterTypes()));
        c.printlnToLog(message);
        c.printStackTraceToLog(iae);
        return message;
    }


    private void testFailed(String name, Throwable t, TestCaseContext testCaseContext) {
        String message = t.getMessage();
        testCaseContext.getParentContext().printlnToLog(format("Testcase \"{0}\" failed with message: {1}", name, message));
        if (t instanceof AssertionFailedException) {
            printAssertionFailureStacktrace((AssertionFailedException) t, testCaseContext);
        } else {
            testCaseContext.getParentContext().printStackTraceToLog(t);
        }
        Throwable cause = t.getCause();
        if (cause != null) {
            testCaseContext.getParentContext().printlnToLog(format("Testcase \"{0}\" provided additional exception as failure info: ", name));
            testCaseContext.getParentContext().printStackTraceToLog(cause);
        }
    }


    private void printAssertionFailureStacktrace(AssertionFailedException t, TestCaseContext testCaseContext) {
            StackTraceElement[] trace = t.getStackTrace();
            boolean testClassStarted = false;
            boolean testClassFinished = false;
            for (StackTraceElement element : trace) {
                String className = element.getClassName();
                if (testCaseContext.getTestGroupInstance().getClass().getName().equals(className)) {
                    testClassStarted = true;
                } else if (testClassStarted) {
                    testClassFinished = true;
                }
                if (!testClassFinished) {
                    testCaseContext.getParentContext().printlnToLog("\tat " + element);
                }
            }
    }



}
