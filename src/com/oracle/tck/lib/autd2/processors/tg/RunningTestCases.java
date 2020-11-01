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

package com.oracle.tck.lib.autd2.processors.tg;

import com.oracle.tck.lib.autd2.*;
import com.oracle.tck.lib.autd2.processors.InterestedInAnnotations;
import com.oracle.tck.lib.autd2.processors.Processor;
import com.sun.tck.lib.NotApplicableException;
import com.sun.tck.lib.tgf.SomethingIsWrong;
import com.sun.tck.lib.tgf.TestDataCollector;
import com.oracle.tck.lib.autd2.TestResult;
import com.sun.tck.test.TestGroup;

import java.util.Collection;

/**
 * Processor responsible for running all the testcases.
 */
@InterestedInAnnotations(TestGroup.class)
public class RunningTestCases extends Processor.TestGroupProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(TestGroupContext.TestGroupLifePhase lifePhase, TestGroupContext testGroupContext) {
        Collection<TestCaseContext> testCases = testGroupContext.getTestCaseContexts();

        // todo with streams here we may go parallel if needed in future
        testCases.forEach(context ->  {
            TestResult result;
            try {
                TestDataCollector.checkIfTestCaseShouldBeExecuted(context.getTestCaseMethod(),
                        context.getTestGroupInstance(),
                        context.getTestGroupInstance().getClass(),
                        context.getLog());

                result = runTestCaseAsNeeded(context);
            } catch (SomethingIsWrong e) {
                // something might be wrong with runtime test skipping for example - @ExecuteIf/Not
                context.printlnToLog(e.getMessage());
                result = TestResult.failure(e.getMessage());
            } catch (NotApplicableException e) {
                String message = e.getMessage();
                result = new InapplicableTestResult(
                            "Not applicable." + (message != null ? " Reason: " + message : ""));
            }
            // todo improve this
            if (result instanceof InapplicableTestResult) {
                testGroupContext.recordNotApplicable();
            }
            testGroupContext.addExecutionResult(context.getTestCaseName(), result);
        });
    }

    /**
     * Performs run of a particular testcase
     */
    protected TestResult runTestCaseAsNeeded(TestCaseContext context) {
        return AUTD2Utils.iterateTestCaseLifePhase(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestGroupContext.TestGroupLifePhase[] getLifePhasesInterestedIn() {
        return new TestGroupContext.TestGroupLifePhase[]{TestGroupContext.TestGroupLifePhase.RUNNING_TESTCASES};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasHigherPriorityThan(Processor<TestGroupContext, TestGroupContext.TestGroupLifePhase> anotherProc) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PhaseOwnership getPhaseOwnership(TestGroupContext.TestGroupLifePhase interestedLifePhase) {
        return PhaseOwnership.ONE_PER_PHASE;
    }

}

