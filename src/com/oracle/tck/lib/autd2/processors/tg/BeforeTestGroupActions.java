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

import com.oracle.tck.lib.autd2.TestGroupContext;
import com.oracle.tck.lib.autd2.processors.Processor;
import com.sun.tck.lib.BeforeTestGroup;
import com.sun.tck.lib.tgf.SomethingIsWrong;

import java.text.MessageFormat;

import static com.sun.tck.lib.tgf.TGFUtils.searchAndInvoke;

/**
 *  Processor responsible for actions to be taken before testgroup execution.
 */
public class BeforeTestGroupActions extends Processor.TestGroupProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    public TestGroupContext.TestGroupLifePhase[] getLifePhasesInterestedIn() {
        return new TestGroupContext.TestGroupLifePhase[] {TestGroupContext.TestGroupLifePhase.BEFORE_TESTGROUP};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PhaseOwnership getPhaseOwnership(TestGroupContext.TestGroupLifePhase interestedLifePhase) {
        return PhaseOwnership.MANY_PER_PHASE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(TestGroupContext.TestGroupLifePhase lifePhase,
                        TestGroupContext context) throws Throwable {

        BeforeTestGroup before = (BeforeTestGroup) getTheOnlyTestGroupAnnotationInterestedIn(context);

        String methodName = before.value();

        try {
            searchAndInvoke(methodName,
                    context.getTestGroupInstance().getClass(),
                    context.getTestGroupInstance(),
                    context.getLog());
        } catch (SomethingIsWrong e) {
            final String message = e.getMessage();
            context.printlnToLog(message);
            final String failedTryingToInvoke = MessageFormat.format(
                    "Failed trying to invoke @BeforeTestGroup method \"{0}\"", methodName);
            throw new RuntimeException(failedTryingToInvoke);
        }

    }

}
