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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Context for testcase or testgroup.
 * Contains collections of TestGroup/TestCase processors mapped to corresponding life phases.
 * Contains result of corresponding testcase or testgroup execution
 * to be accumulated or returned to JavaTest.
 * @see TestCaseContext
 * @see TestGroupContext
 */
public abstract class Context<P extends Processor<?, L>, L extends LifePhase<?>> {

    private TestResult overridingResult;
    private Map<L, List<P>> lifePhase2Processors;
    private TestResultAccumulator testResultAccumulator;

    /**
     * Returns lists of processors mapped to particular life phases
     */
    public Map<L, List<P>> getLifePhase2ProcessorsMap() {
        return lifePhase2Processors;
    }

    /**
     * Used internally.
     */
    public void setLifePhase2ProcessorsMap(Map<L, List<P>> proc2LifePhase) {
        this.lifePhase2Processors = proc2LifePhase;
    }

    /**
     * Maps the given processor instance to the life phases that it's interested in.
     */
    public void addProcessor(P processor) {
        if (lifePhase2Processors == null) {
            lifePhase2Processors = new HashMap<>();
        }
        for (L phase : processor.getLifePhasesInterestedIn()) {
            List<P> ps = lifePhase2Processors.get(phase);
            if (ps == null) {
                ps = new LinkedList<>();
                lifePhase2Processors.put(phase, ps);
            }
            ps.add(processor);
        }

    }

    /**
     * A workaround for counting passed testcases which are actually not applicable.
     */
    public void recordNotApplicable() {
        getTestResultAccumulator().recordNotApplicable();
    }

    private TestResultAccumulator getTestResultAccumulator() {
        if (testResultAccumulator == null) {
            testResultAccumulator = createTestResultAccumulator();
        }
        return testResultAccumulator;
    }

    /**
     * This is used if something happened at "Before" or "After" stage of testcase or testgroup execution,
     * so it does not matter what exec result was accumulated, the final result is predefined.
     */
    public void setOverridingResult(TestResult overridingResult) {
        this.overridingResult = overridingResult;
    }

    /**
     * Returns the predefined result.
     */
    public TestResult getOverridingResult() {
        return overridingResult;
    }

    /**
     * Aggregates execution result of a particular testcase.
     */
    public void addExecutionResult(String tcName, TestResult result) {
        getTestResultAccumulator().add(tcName, result);
    }

    /**
     * Returns the resulting result of a testcase.
     * If a predefined result was set then it overrides accumulated result.
     */
    public TestResult getFinalTestResult() {
        TestResult predefinedResult = getOverridingResult();
        return predefinedResult != null ? predefinedResult : getTestResultAccumulator().getFinalResult();
    }

    /**
     * Returns used 'log' output stream instance.
     */
    public abstract PrintWriter getLog();

    /**
     * Returns used 'ref' output stream instance.
     */
    public abstract PrintWriter getRef();

    /**
     * A convenience method which prints a message to 'log' output stream.
     */
    public abstract void printlnToLog(String message);

    /**
     * A convenience method which prints a message to 'ref' output stream.
     */
    public abstract void printlnToRef(String message);

    /**
     * A convenience method which prints the stacktrace
     * of the provided exception to 'log' output stream.
     */
    public abstract void printStackTraceToLog(Throwable thrownException);

    /**
     * A convenience method which prints the stacktrace
     * of the provided exception to 'ref' output stream.
     */
    public abstract void printStackTraceToRef(Throwable thrownException);

    /**
     * Returns the arguments that were passed.
     */
    public abstract String[] getExecutionArgs();

    /**
     * Returns a <code>TestResultAccumulator</code> instance suitable for result accumulation.
     */
    protected abstract TestResultAccumulator createTestResultAccumulator();

    /**
     * Method is used only internally. Not for external usage.
     */
    public void resetTestResultAccumulator() {
        /**
         * todo this method is used only by framework so the good thing to do
         * todo is to move methods for customers to a separate interface leaving this one
         * todo and other utility methods visible only to framework
         */
        testResultAccumulator = null;
    }

    /**
     * Returns instantiated test group.
     */
    public abstract Object getTestGroupInstance();

}
