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

import com.oracle.tck.lib.autd2.AUTD2Utils;
import com.oracle.tck.lib.autd2.TestCaseContext;
import com.oracle.tck.lib.autd2.TestResult;
import com.oracle.tck.lib.autd2.processors.InterestedInAnnotations;
import com.oracle.tck.lib.autd2.processors.Processor;
import com.oracle.tck.lib.autd2.processors.tc.DefaultExecutionResult;
import com.oracle.tck.lib.autd2.processors.tc.ExceptionsExpected;
import com.sun.tck.test.TestCase;
import com.sun.javatest.Status;

import static java.text.MessageFormat.format;

/**
 * This processor enhances DefaultExecutionResult and is able to recognize situation
 * when particular testcase method returns javasoft.sqe.javatest.Status.
 * <p>
 * Testgroup classes containing such testcase methods are usually utilizing legacy frameworks together
 * with modern features.
 * <p>
 * If a particular testcase from the testgroup class doesn't return a Status
 * processing is delegated to the parent (default) processor.
 */
@InterestedInAnnotations(TestCase.class)
public class TestCaseResultCanBeStatus extends DefaultExecutionResult {

    /**
     * If no exceptions recorded and testcase returned Status we are treating it in a special way.
     * Otherwise delegating result processing to the parent processor.
     * {@inheritDoc}
     */
    @Override
    public void process(TestCaseContext.TestCaseLifePhase phase, TestCaseContext c) {
        if (AUTD2Utils.unwrapCoreExceptionFromITEs(c) == null && c.getTestCaseResult().getResult() instanceof Status) {
            Status s = (Status) c.getTestCaseResult().getResult();
            if (!s.isPassed() && c.getTestCaseInvocationArgValues() != null) {
                c.getParentContext().printlnToLog(
                        format("Testcase \"{0}\" failed with message: {1}", c.getTestCaseNameWithIndex(), s.getReason()));
            }
            c.addExecutionResult(c.getTestCaseName(),
                    s.isPassed() ? TestResult.ok(s.getReason()) : TestResult.failure(s.getReason()));
            c.clearTestCaseResult();
        } else {
            super.process(phase, c);
        }
    }

    /**
     * Overriding any processor except expected exceptions handler.
     *
     * @param another processor to compare to
     * @return true except when the given one is about processing expected exceptions throwing
     */
    @Override
    public boolean hasHigherPriorityThan(Processor another) {
        return !(another instanceof ExceptionsExpected);
    }
}
