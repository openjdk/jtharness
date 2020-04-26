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
import com.oracle.tck.lib.autd2.processors.Processor;
import com.sun.tck.lib.ExpectedExceptions;
import com.sun.tck.lib.NotApplicableException;
import com.sun.tck.lib.tgf.SomethingIsWrong;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

import static java.text.MessageFormat.format;

/**
 * Processor responsible for situations when an exception is expected to be thrown by a testcase.
 */
public class ExceptionsExpected extends Processor.TestCaseProcessor {

    // todo combine with DefaultExecutionResult

    private static final TestResult RESULT_OK = TestResult.ok("OK");


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
    public boolean hasHigherPriorityThan(Processor anotherProc) {
        return true;
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

        Class<? extends Throwable>[] expExTypes = getExpectedExceptionTypes(context);

        TestResult result = null;
        Throwable t = AUTD2Utils.unwrapCoreExceptionFromITEs(context);

        if (t == null) {
            String m = expectedExceptionsNotThrown(context.getTestCaseName(), expExTypes);
            if (context.getTestCaseInvocationArgValues() != null) {
                context.printlnToLog(m);
            }
            result = TestResult.failure(m);
        } else {
            if (t instanceof SomethingIsWrong) {
                context.printlnToLog(t.getMessage());
                result = TestResult.failure(t.getMessage());
            } else if (t instanceof NotApplicableException) {
                String message = t.getMessage();
                context.recordNotApplicable();
                result = new InapplicableTestResult(
                        "Not applicable." + (message != null ? " Reason: " + message : ""));
            } else {
                for (Class<? extends Throwable> expExType : expExTypes) {
                    if (expExType.isAssignableFrom(t.getClass())) {
                        result = RESULT_OK;
                    }
                }
                if (result != RESULT_OK) {
                    String message = expectedExceptionsNotThrown(
                            context.getTestCaseName(), expExTypes);
                    context.printlnToLog(message);
                    context.printlnToLog(format("\"{0}\" was thrown instead", t));
                    context.printStackTraceToLog(t);
                    result = TestResult.failure("Wrong type of exception was thrown");
                }
            }
        }

        context.addExecutionResult(context.getTestCaseName(), result);
        context.clearTestCaseResult();
    }

    private String expectedExceptionsNotThrown(String name, Class<? extends Throwable>[] expExTypes) {
        final String message;
        if (expExTypes.length == 1) {
            message = format("Expected exception {0} was not thrown by testcase \"{1}\"",
                    expExTypes[0].getName(), name);
        } else {
            message = format("None of expected exceptions (or descendants) of {0} was thrown by testcase \"{1}\"",
                    Arrays.toString(expExTypes), name);
        }
        return message;
    }

    /**
     * Returns an array of expected exception types.
     */
    protected Class<? extends Throwable>[] getExpectedExceptionTypes(TestCaseContext context) {
        Class<? extends Throwable>[] expExTypes = null;
        Set<Annotation> annotations = getAnnotationsInterestedIn(context);
        for (Annotation annotation : annotations) {
            if (ExpectedExceptions.class.equals(annotation.annotationType())) {
                ExpectedExceptions exceptions = (ExpectedExceptions) annotation;
                expExTypes = exceptions.value();
                if (expExTypes.length == 0) {
                    throw new SomethingIsWrong(
                            "@ExpectedExceptions attached to " +
                                    context.getTestCaseName() + " contains empty array of classes.");
                }
            }
        }
        return expExTypes;
    }

}
