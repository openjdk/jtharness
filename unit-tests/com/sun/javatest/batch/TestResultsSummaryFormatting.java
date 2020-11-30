/*
 * $Id$
 *
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.batch;


import com.sun.javatest.util.I18NResourceBundle;
import org.junit.Assert;
import org.junit.Test;

/**
 * Checking "Test results:" line formatting for differen data sets.
 */
public class TestResultsSummaryFormatting {

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(RunTestsCommand.class);

    @Test
    public void statTemplateArgs() {
        Assert.assertArrayEquals(
                new Integer[] {4, 1, 2, 1, 1, 0, 0, 0, 0},
                RunTestsCommand.getTestSummaryStatsArgs(4, 2, 1, 0, 0)
        );

        Assert.assertArrayEquals(
                new Integer[] {7, 1, 2, 0, 0, 0, 0, 0, 0},
                RunTestsCommand.getTestSummaryStatsArgs(7, 2, 0, 0, 0)
        );

        Assert.assertArrayEquals(
                new Integer[] {10, 0, 0, 0, 0, 0, 0, 0, 0},
                RunTestsCommand.getTestSummaryStatsArgs(10, 0, 0, 0, 0)
        );
        Assert.assertArrayEquals(
                new Integer[] {14, 1, 0, 0, 0, 0, 0, 0, 245},
                RunTestsCommand.getTestSummaryStatsArgs(14, 0, 0, 0, 245)
        );
        Assert.assertArrayEquals(
                new Integer[] {1400, 1, 2, 0, 0, 0, 0, 0, 0},
                RunTestsCommand.getTestSummaryStatsArgs(1400, 2, 0, 0, 0)
        );

        Assert.assertArrayEquals(
                new Integer[] {0, 0, 2, 0, 0, 0, 0, 0, 0},
                RunTestsCommand.getTestSummaryStatsArgs(0, 2, 0, 0, 0)
        );
    }


    @Test
    public void templateArgsToResult() {
        Assert.assertEquals("Test results: passed: 4; failed: 2; error: 1",
                i18n.getString("runTests.tests", 4, 1, 2, 1, 1, 0, 0, 0, 0));

        Assert.assertEquals("Test results: passed: 7; failed: 2",
                i18n.getString("runTests.tests", 7, 1, 2, 0, 0, 0, 0, 0, 0));
    }

    @Test
    public void initialDataToResult() {
        Assert.assertEquals("Test results: passed: 13",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(13, 0, 0, 0, 0)
                        ));

        Assert.assertEquals("Test results: failed: 3",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(0, 3, 0, 0, 0)
                ));

        Assert.assertEquals("Test results: failed: 3; error: 8",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(0, 3, 8, 0, 0)
                ));

        Assert.assertEquals("Test results: failed: 3; not run: 1",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(0, 3, 0, 1, 0)
                ));

        Assert.assertEquals("Test results: failed: 3; error: 6; not run: 1",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(0, 3, 6, 1, 0)
                ));

        Assert.assertEquals("Test results: failed: 3; skipped: 4",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(0, 3, 0, 0, 4)
                ));


        Assert.assertEquals("Test results: error: 7",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(0, 0, 7, 0, 0)
                ));

        Assert.assertEquals("Test results: error: 7; not run: 2",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(0, 0, 7, 2, 0)
                ));

        Assert.assertEquals("Test results: error: 7; skipped: 4",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(0, 0, 7, 0, 4)
                ));

        Assert.assertEquals("Test results: not run: 5",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(0, 0, 0, 5, 0)
                ));

        Assert.assertEquals("Test results: skipped: 2",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(0, 0, 0, 0, 2)
                ));




        Assert.assertEquals("Test results: passed: 10; failed: 1",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(10, 1, 0, 0, 0)
                ));

        Assert.assertEquals("Test results: passed: 10; failed: 1; error: 56",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(10, 1, 56, 0, 0)
                ));

        Assert.assertEquals("Test results: passed: 10; failed: 1; error: 56; not run: 7",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(10, 1, 56, 7, 0)
                ));

        Assert.assertEquals("Test results: passed: 10; failed: 1; error: 56; skipped: 231",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(10, 1, 56, 0, 231)
                ));


        Assert.assertEquals("Test results: passed: 10; failed: 1; not run: 89",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(10, 1, 0, 89, 0)
                ));

        Assert.assertEquals("Test results: passed: 10; failed: 1; skipped: 52",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(10, 1, 0, 0, 52)
                ));


        Assert.assertEquals("Test results: passed: 10; error: 133",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(10, 0, 133, 0, 0)
                ));

        Assert.assertEquals("Test results: passed: 10; error: 133; not run: 7",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(10, 0, 133, 7, 0)
                ));

        Assert.assertEquals("Test results: passed: 10; error: 133; skipped: 880",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(10, 0, 133, 0, 880)
                ));

        Assert.assertEquals("Test results: passed: 67; not run: 17",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(67, 0, 0, 17, 0)
                        ));

        Assert.assertEquals("Test results: passed: 67; not run: 17; skipped: 94",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(67, 0, 0, 17, 94)
                        ));

        Assert.assertEquals("Test results: passed: 10; skipped: 501",
                i18n.getString("runTests.tests",
                        RunTestsCommand.getTestSummaryStatsArgs(10, 0, 0, 0, 501)
                ));




    }


}
