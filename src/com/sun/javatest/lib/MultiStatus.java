/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.lib;

import java.io.PrintWriter;
import com.sun.javatest.Status;

/**
 * When executing multiple test cases in the same test class, it is usually
 * easier for each test case to return a Status object representing whether that
 * individual test case passed or failed.  However, combining those individual
 * Status objects into one Status object to represent the overall Status of the
 * tests executed can be difficult.  This test library is designed to solve the
 * problem of generating an aggregate or overall Status from multiple Status
 * objects.  The static method overallStatus is designed to take an array of
 * Status objects from individual test cases and generate a Status object that
 * correctly reflects the overall status of all the individual the test
 * cases executed.
 *
 * <P>The rule for how MultiStatus calculates the overall Status of an array of
 * Status objects is based on the following precedence:
 * <BLOCKQUOTE>
 * If any of the test cases return a Status.FAILED, then the overall status is
 * Status.FAILED.<BR>
 * If all test cases return Status.PASSED, then the overall status is
 * Status.PASSED.<BR>
 * If at least one test case returns either a null Status or some other Status,
 * the overall status is Status.FAILED.
 * </BLOCKQUOTE>
 *
 * <P>For an example of how to use this library see the UmbrellaTest library or the
 * JCK test case: <TT>tests/api/java_lang/Double/SerializeTests.html</TT>.
 */
public class MultiStatus {

    /**
     * Create a MultiStatus object to accumulate individual Status objects.
     */
    public MultiStatus() {
    }

    /**
     * Create a MultiStatus object to accumulate individual Status objects.
     * @param out    A stream to which the report the outcome of the tests.
     *                   If the stream is null, no reporting is done.
     */
    public MultiStatus(PrintWriter out) {
        this.out = out;
    }

    /**
     * Get the number of individual test results that have been added.
     * @return the number of individual results that have been added.
     */
    public int getTestCount() {
        return iTestCases;
    }

    /**
     * Add another test result into the set for consideration.
     *
     * @param testID A name for this test case.  Should not be null.
     * @param status The outcome of this test case
     */
    public void add(String testID, Status status) {
        if (out != null) {
            out.println(testID + ": " + status);
        }

        ++iTestCases;

        if (status != null) {
            int t = status.getType();

            switch (t) {
            case Status.PASSED:
                ++iPassed;
                break;

            case Status.FAILED:
                ++iFail;
                break;

            case Status.ERROR:
                ++iError;
                break;

            default:
                ++iBad;
                break;
            }

            if (t != Status.PASSED && firstTestCase.length() == 0)
                firstTestCase = testID;
        }
    }

    /**
     * Get the aggregate outcome of all the outcomes passed to "add".
     * @return the aggregate outcome
     */
    public Status getStatus() {

        // If stream not null, flush it
        if( out != null ) {
            out.flush();
        }

        String summary;
        if (iTestCases == 0)
            summary = "No tests cases found (or all test cases excluded.)";
        else {
            summary = "test cases: " + iTestCases;
            if (iPassed > 0) {
                if (iPassed == iTestCases)
                    summary += "; all passed";
                else
                    summary += "; passed: " + iPassed;
            }

            if (iFail > 0) {
                if (iFail == iTestCases)
                    summary += "; all failed";
                else
                    summary += "; failed: " + iFail;
            }

            if (iError > 0) {
                if (iError == iTestCases)
                    summary += "; all had an error";
                else
                    summary += "; error: " + iError;
            }

            if (iBad > 0) {
                if (iBad == iTestCases)
                    summary += "; all bad";
                else
                    summary += "; bad status: " + iBad;
            }
        }

        /* Return a status object that reflects the aggregate of the various test cases. */

        /* At least one test case was bad */
        if (iBad > 0) {
            return Status.error(summary + "; first bad test case result found: " + firstTestCase);
        }
        /* At least one test case was bad */
        else if (iError > 0) {
            return Status.error(summary + "; first test case with error: " + firstTestCase);
        }
        /* At least one test case failed */
        else if (iFail > 0) {
            return Status.failed(summary + "; first test case failure: " + firstTestCase);
        }
        /* All test cases passed */
        else {
            return Status.passed(summary);
        }
    }


    /**
     * Generates a Status object that reflects an array of Status objects.
     * Uses the algorithm above to generate an overall status from an array of
     * Status objects.  This method prints out the individual Status values from
     * each test case to the PrintWriter supplied.  If the PrintWriter is null,
     * no output is generated.
     *
     * @param testIDs an array of names used to identify the individual test cases.
     * @param status an array of Status objects giving the outcomes of the individual test cases.
     * @param out a PrintWriter that can be used to output the individual test case
     *            status values. If null, no output is generated.
     * @return the aggregate status of the array of Status objects.
     */
    public static Status overallStatus(String testIDs[], Status status[], PrintWriter out) {

        /* Check the number of tests against the number of statuses */
        if( testIDs.length != status.length ) {
            return Status.failed( "mismatched array sizes; test cases: " + testIDs.length +
                                  " statuses: " + status.length );
        }

        /* Loop through status objects, check types,
         * increment appropriate counters, and identify the
         * first test that should be listed in the aggregate status.
         */

        MultiStatus ms = new MultiStatus(out);
        for( int i = 0; i < status.length; ++i ) {
            ms.add(testIDs[i], status[i]);
        }

        return ms.getStatus();
    }


    /**
     * Generates a Status object that reflects an array of Status objects.
     * Uses the algorithm above to generate an overall status from an array of
     * Status objects.  This method does not output any information
     *
     * @param testIDs an array of names used to identify the individual test cases.
     * @param status an array of Status objects giving the outcomes of the individual test cases.
     * @return overall status of the specified array of Status objects.
     */
    public static Status overallStatus(String testIDs[], Status status[]) {
        return MultiStatus.overallStatus(testIDs, status, null);
    }

    // These values accumulate the aggregate outcome, without requiring
    // the individual outcomes be stored
    private int iTestCases = 0;
    private int iPassed = 0;
    private int iFail = 0;
    private int iError = 0;
    private int iBad = 0;
    private String firstTestCase = "";

    private PrintWriter out = null;
}
