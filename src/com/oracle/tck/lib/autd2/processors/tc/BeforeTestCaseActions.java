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

import com.oracle.tck.lib.autd2.TestResult;
import com.oracle.tck.lib.autd2.TestCaseContext;
import com.oracle.tck.lib.autd2.processors.Processor;
import com.sun.tck.lib.tgf.Before;
import com.sun.tck.lib.tgf.SomethingIsWrong;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;

import static com.sun.tck.lib.tgf.TGFUtils.searchAndInvoke;

/**
 *  Processor is responsible for actions that should be taken before testcase execution.
 */
public class BeforeTestCaseActions extends Processor.TestCaseProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    public PhaseOwnership getPhaseOwnership(TestCaseContext.TestCaseLifePhase interestedLifePhase) {
        return PhaseOwnership.MANY_PER_PHASE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestCaseContext.TestCaseLifePhase[] getLifePhasesInterestedIn() {
        return new TestCaseContext.TestCaseLifePhase[]{TestCaseContext.TestCaseLifePhase.BEFORE_TESTCASE};
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void process(TestCaseContext.TestCaseLifePhase lifePhase, final TestCaseContext context) {
        String methodName = getMethodNameToCall(context);
        try {
            searchAndInvoke(methodName,
                    context.getTestGroupInstance().getClass(),
                    context.getTestGroupInstance(),
                    context.getLog());
        } catch (SomethingIsWrong e) {
            final String message = e.getMessage();
            context.printlnToLog(message);
            final String failedTryingToInvoke = MessageFormat.format(
                    "Failed trying to invoke @Before method \"{0}\"", methodName);
            context.setOverridingResult(TestResult.failure(failedTryingToInvoke));
        }

    }

    /**
     * Returns name of the method that should be run before the testcase method.
     */
    protected String getMethodNameToCall(TestCaseContext context) {
        Annotation tcAnn = getTheOnlyAnnotationInterestedIn(context);
        Annotation tgAnn = getTheOnlyTestGroupAnnotationInterestedIn(context);
        if (tcAnn != null && tgAnn != null) {
            throw new IllegalArgumentException("Annotation @Before is attached " +
                    "to both testgroup class and a testcase \"" + context.getTestCaseName() +
                    "\". This is not allowed" );
        }
        Before before = tcAnn != null ? (Before)tcAnn : (Before)tgAnn;
        return before.value();
    }

}
