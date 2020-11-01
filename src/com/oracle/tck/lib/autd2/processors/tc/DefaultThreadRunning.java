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

import com.oracle.tck.lib.autd2.TestCaseContext;
import com.oracle.tck.lib.autd2.processors.InterestedInAnnotations;
import com.oracle.tck.lib.autd2.processors.Processor;
import com.sun.tck.test.TestCase;

/**
 * Processor is responsible for running a testcase in the default thread.
 */
@InterestedInAnnotations(TestCase.class)
public class DefaultThreadRunning extends Processor.TestCaseProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    public TestCaseContext.TestCaseLifePhase[] getLifePhasesInterestedIn() {
        return new TestCaseContext.TestCaseLifePhase[]{TestCaseContext.TestCaseLifePhase.CALLING_TESTCASE};
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
        if (context.getCallableTestCase() != null) {
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
        try {
            Object result = context.getCallableTestCase().call();
            context.setTestCaseMethodReturnedValue(result);
            // todo if something has not set item to execute then
        } catch (Throwable e) {
            context.setThrownException(e);
        } finally {
            context.setCallableTestCase(null);
        }
    }

}
