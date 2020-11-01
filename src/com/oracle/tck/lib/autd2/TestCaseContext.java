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
import com.sun.tck.lib.tgf.ReflectionUtils;
import com.sun.tck.test.TestedAPI;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

import static com.sun.tck.lib.tgf.TGFUtils.*;
import static java.text.MessageFormat.format;

/**
 * Context for a testcase.
 * Contains all the information needed for working with particular testcase
 * from setting up the environment to processing execution results.
 */
public class TestCaseContext extends Context<Processor.TestCaseProcessor, TestCaseContext.TestCaseLifePhase> {

    /**
     * Represents various testcase life phases
     */
    public enum TestCaseLifePhase implements LifePhase<TestCaseContext> {
        BEFORE_TESTCASE,
        SETTING_WHAT_TO_CALL,
        BEFORE_INVOCATION,
        CALLING_TESTCASE,
        AFTER_INVOCATION,
        PROCESSING_RESULT,
        AFTER_TESTCASE
    }

    private TestGroupContext parentContext;
    private Method testCaseMethod;
    private Callable<?> callableTestCase;
    private TestCaseResult testCaseResult;
    private Object[] testCaseInvocationArgValues;
    private long rowIndex = -1;

    /**
     * A constructor which creates a context instance
     * with the given parent testgroup context.
     */
    public TestCaseContext(TestGroupContext parentContext) {
        this.parentContext = parentContext;
    }

    /**
     * {@inheritDoc}
     * Call is delegated to the parent testgroup context.
     */
    @Override
    public Object getTestGroupInstance() {
        return parentContext.getTestGroupInstance();
    }

    /**
     * {@inheritDoc}
     * Call is delegated to the parent testgroup context.
     */
    @Override
    public PrintWriter getLog() {
        return parentContext.getLog();
    }

    /**
     * {@inheritDoc}
     * Call is delegated to the parent testgroup context.
     */
    @Override
    public PrintWriter getRef() {
        return parentContext.getRef();
    }

    /**
     * {@inheritDoc}
     * Call is delegated to the parent testgroup context.
     */
    @Override
    public void printlnToLog(String message) {
        parentContext.printlnToLog(message);
    }

    /**
     * {@inheritDoc}
     * Call is delegated to the parent testgroup context.
     */
    @Override
    public void printlnToRef(String message) {
        parentContext.printlnToRef(message);
    }

    /**
     * {@inheritDoc}
     * Call is delegated to the parent testgroup context.
     */
    @Override
    public void printStackTraceToLog(Throwable thrownException) {
        parentContext.printStackTraceToLog(thrownException);
    }

    /**
     * {@inheritDoc}
     * Call is delegated to the parent testgroup context.
     */
    @Override
    public void printStackTraceToRef(Throwable thrownException) {
        parentContext.printStackTraceToRef(thrownException);
    }

    /**
     * {@inheritDoc}
     * Call is delegated to the parent testgroup context.
     */
    @Override
    public String[] getExecutionArgs() {
        return parentContext.getExecutionArgs();
    }

    /**
     * Returns argument values passed to a testcase that is being executed.
     */
    public Object[] getTestCaseInvocationArgValues() {
        return this.testCaseInvocationArgValues != null
                ? Arrays.copyOf(testCaseInvocationArgValues, testCaseInvocationArgValues.length)
                : null;
    }

    /**
     * Sets the testcase argument values.
     */
    public void setTestCaseInvocationArgValues(Object[] objects) {
        this.testCaseInvocationArgValues =
                objects == null
                        ? null
                        // to avoid malicious code vulnerability
                        : Arrays.copyOf(objects, objects.length);
    }

    public void setRowIndex(long rowIndex) {
        this.rowIndex = rowIndex;
    }

    /**
     * Records an exception that was thrown by a testcase.
     */
    public void setThrownException(Throwable e) {
        if (testCaseResult == null) {
            testCaseResult = new TestCaseResult();
        }
        testCaseResult.setThrownException(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addExecutionResult(String tcName, TestResult result) {
        super.addExecutionResult(tcName, result);
        if (result instanceof InapplicableTestResult) {
            if (getTestCaseInvocationArgValues() != null) {
                String message = ((InapplicableTestResult) result).getReasonOfInapplicability();
                if (message != null) {
                    String reason = ", reason: " + message;
                    printlnToLog(format("Testcase \"{0}\" is not applicable with arguments {1}{2}",
                            tcName, Arrays.deepToString(getTestCaseInvocationArgValues()), reason));
                }
            }
        }
        if (!result.isOK()) {
            if (getTestCaseInvocationArgValues() != null) {
                printlnToLog(format("Testcase \"{0}\" failed with arguments {1}",
                        getTestCaseNameWithIndex(), Arrays.deepToString(getTestCaseInvocationArgValues())));
            }
            handleTestedStatements(tcName);
        }
    }

    /**
     * If <code>@TestedStatement</code> annotations are attached to the method of the failed testcase
     * all the available data is printed to the log
     * @param tcName name of the testcase
     */
    private void handleTestedStatements(String tcName) {
        List<TestedStatement> statements = new ArrayList<>();
        // first adding all @TestedStatement present at testgroup level and all super classes/interfaces
        ReflectionUtils.getClassHierarchy(getTestGroupInstance().getClass()).forEach(c ->
                statements.addAll(Arrays.asList(c.getAnnotationsByType(TestedStatement.class)))
        );

        // then processing @TestedStatements present at testcase level
        statements.addAll(Arrays.asList(testCaseMethod.getAnnotationsByType(TestedStatement.class)));

        if (!statements.isEmpty()) {
            printlnToLog(format("Testcase \"{0}\" is based on the following statements: ", tcName));
            for (TestedStatement statement : statements) {
                StringJoiner texts = new StringJoiner(", ");
                for (String text : statement.value()) {
                    texts.add("\"" + text + "\"");
                }
                printlnToLog(" - " + texts.toString());
                if ( !TestedStatement.DEFAULT_SOURCE.equals(statement.source())) {
                    printlnToLog(" -- from source: \"" + statement.source() + "\"");
                } else {
                    TestedAPI testedAPI = parentContext.getTestGroupInstance().getClass().getAnnotation(TestedAPI.class);
                    if (testedAPI != null) {
                        String[] classes = testedAPI.testedClass();
                        String[] members = testedAPI.testedMember();
                        String[] packages = testedAPI.testedPackage();
                        String source = beautifulizeArrayToString(packages)
                                // filtering out the case when tested class or member are not specified - by default it's an array of {""}
                                + (Arrays.equals(classes, new String[] {""}) ? "" : "." + beautifulizeArrayToString(classes))
                                + (Arrays.equals(members, new String[] {""}) ? "" : "." + beautifulizeArrayToString(members));
                        printlnToLog(" -- from source: " + source );
                    }
                }
            }
        }
    }

    /**
     * If there's only one element - not using Arrays.toString() which embraces content in "[]"
     * but rather return that single element
     * @param stringArray array of strings to be transformed to a single string
     */
    private String beautifulizeArrayToString(String[] stringArray) {
        return stringArray.length == 1 ? stringArray[0] : Arrays.toString(stringArray);
    }

    /**
     * Returns a new micro-testcase result accumulator instance.
     */
    @Override
    protected TestResultAccumulator createTestResultAccumulator() {
        return new TestResultAccumulator() {
            @Override
            TestResult getFinalResult() {
                if (getTotalTestCount() == 1) {
                    if (getNumberOfNotApplicable() > 0) {
                        return new InapplicableTestResult(getLastAddedResult().getMessage());
                    }
                    return getLastAddedResult();
                }
                String summary = getSummaryPrefix();

                if (failedTestNames.size() > 0) {
                    return TestResult.failure(summary);
                }
                // no tests failed
                return allTestsAreNotApplicable() ? new InapplicableTestResult(summary) : TestResult.ok(summary);
            }
        };
    }

    /**
     * Returns testcase method that is used.
     */
    public Method getTestCaseMethod() {
        return testCaseMethod;
    }

    /**
     * A shortcut for getting testcase method name.
     */
    public String getTestCaseName() {
        return testCaseMethod.getName();
    }

    /**
     * A shortcut for getting testcase method name followed by a suffix containing encoded invocation index.
     */
    public String getTestCaseNameWithIndex() {
        String indexString = rowIndex < 0 ? "" : INDICES_START + rowIndex + INDICES_END;
        return testCaseMethod.getName() + indexString;
    }

    /**
     * Sets method that is associated with particular testcase.
     */
    public void setTestCaseMethod(Method testCaseMethod) {
        this.testCaseMethod = testCaseMethod;
    }

    /**
     * Returns parent testgroup context.
     */
    public TestGroupContext getParentContext() {
        return parentContext;
    }

    /**
     * Sets parent testgroup context.
     */
    public void setParentContext(TestGroupContext parentContext) {
        this.parentContext = parentContext;
    }

    /**
     * Sets an action wrapped in {@code Callable} that represent
     * invocation of a testcase.
     * This action does not necessary mean direct invocation of testcase method.
     */
    public void setCallableTestCase(Callable<?> callableTestCase) {
        this.callableTestCase = callableTestCase;
    }

    /**
     * Sets a testcase "action" wrapped in {@code Callable}.
     */
    public Callable<?> getCallableTestCase() {
        return callableTestCase;
    }

    /**
     * Returns the testcase result.
     */
    public TestCaseResult getTestCaseResult() {
        return testCaseResult;
    }

    /**
     * Clears testcase result.
     */
    public void clearTestCaseResult() {
        testCaseResult = null;
    }

    /**
     * Records the value that a testcase has returned, if any.
     */
    public void setTestCaseMethodReturnedValue(Object returned) {
        if (testCaseResult == null) {
            testCaseResult = new TestCaseResult();
        }
        testCaseResult.setResult(returned);
    }

}

