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

import java.util.LinkedList;

/**
 * Accumulates results from various testcases or "micro-testcases"
 * (testcase method invocations with particular set of arg values).
 *
 * Subclasses give a properly summarized TestResult.
 */
public abstract class TestResultAccumulator {

    private static final String NO_TEST_CASES_RUN = "No test cases found (or all test cases excluded.)";
    private static final String ALL_OK = "; all passed";
    private static final String ALL_FAILED = "; all failed";
    private static final String ALL_NOT_APPLICABLE = "; all not applicable";
    private static final String TEST_CASES = "test cases: ";
    private static final String OK = "; passed: ";
    protected static final String FAILED = "; failed: ";
    private static final String NOT_APPLICABLE = "; not applicable: ";
    protected final LinkedList<String> failedTestNames = new LinkedList<>();
    private int numberOfPassed;
    private int numberOfNotApplicable;
    private TestResult lastAddedResult;

    /**
     * Get the number of tests that have been added.
     *
     * @return total number of testcases
     */
    protected int getTotalTestCount() {
        return numberOfPassed + failedTestNames.size() + numberOfNotApplicable;
    }

    public int getNumberOfNotApplicable() {
        return numberOfNotApplicable;
    }

    /**
     * Add another test into the set for consideration.
     *
     * @param testID A name for this test case
     * @param result The outcome of this test case
     */
    public void add(String testID, TestResult result) {
        lastAddedResult = result;
        if (result.isOK()) {
            numberOfPassed++;
        } else {
            failedTestNames.add(testID);
        }
    }

    /**
     * Returns the accumulated result. Differs for testgroup and testcase contexts.
     */
    abstract TestResult getFinalResult();

    /**
     * Creates a summary message prefix - which is common for micro and macro testcases.
     */
    String getSummaryPrefix() {
        int numberOfFailed = failedTestNames.size();
        String summary = getTotalTestCount() == 0 ? NO_TEST_CASES_RUN : TEST_CASES + getTotalTestCount();
        if (numberOfPassed > 0) {
            summary += numberOfPassed == getTotalTestCount() ? ALL_OK : (OK + numberOfPassed);
        }
        if (numberOfFailed > 0) {
            summary += numberOfFailed == getTotalTestCount() ? ALL_FAILED : (FAILED + numberOfFailed);
        }
        if (numberOfNotApplicable > 0) {
            summary += numberOfNotApplicable == getTotalTestCount() ? ALL_NOT_APPLICABLE : (NOT_APPLICABLE + numberOfNotApplicable);
        }
        return summary;
    }

    boolean allTestsAreNotApplicable() {
        return numberOfPassed == 0 && failedTestNames.isEmpty() && numberOfNotApplicable > 0;
    }

    /**
     * By calling this method it is recorded that a testcase is not applicable.
     */
    void recordNotApplicable() {
        numberOfNotApplicable++;
        numberOfPassed--;
    }

    public TestResult getLastAddedResult() {
        return lastAddedResult;
    }
}
