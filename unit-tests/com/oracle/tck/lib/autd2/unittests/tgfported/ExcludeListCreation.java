/*
 * $Id$
 *
 * Copyright (c) 1996, 2020, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.tck.lib.autd2.unittests.tgfported;

import com.sun.tck.lib.tgf.Values;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static com.sun.tck.lib.tgf.TGFUtils.*;

/**
 *
 */
public class ExcludeListCreation {

    @Test
    public void none() {
        Map<String, Values.ExcludedIndices> excludeList = createExcludeList();
        Assert.assertTrue(excludeList.isEmpty());
    }

    @Test
    public void singleTestCase() {
        Map<String, Values.ExcludedIndices> excludeList = createExcludeList(EXCLUDE, "testCaseOne");
        Assert.assertEquals(1, excludeList.size());
        Assert.assertTrue(excludeList.get("testCaseOne").noIndicesSpecified());
    }

    @Test
    public void twoTestCases() {
        Map<String, Values.ExcludedIndices> excludeList = createExcludeList(EXCLUDE, "testCaseOne" + EXCLUDE_DELIMETER + "testCaseTwo");
        Assert.assertEquals(2, excludeList.size());
        Assert.assertTrue(excludeList.get("testCaseOne").noIndicesSpecified());
        Assert.assertTrue(excludeList.get("testCaseTwo").noIndicesSpecified());
    }


    @Test
    public void singleTestCaseWithMethodCalls_01() {
        Map<String, Values.ExcludedIndices> excludeList = createExcludeList(EXCLUDE, "testCaseOne" + INDICES_START + 1 + INDICES_END);
        Assert.assertEquals(1, excludeList.size());
        Values.ExcludedIndices testCaseOne = excludeList.get("testCaseOne");
        Assert.assertFalse(testCaseOne.noIndicesSpecified());
        Assert.assertTrue(testCaseOne.isExcluded(1));
        Assert.assertFalse(testCaseOne.isExcluded(0));
        Assert.assertFalse(testCaseOne.isExcluded(2));
    }

    @Test
    public void singleTestCaseWithMethodCalls_02() {
        Map<String, Values.ExcludedIndices> excludeList =
                createExcludeList(EXCLUDE, "testCaseOne" + INDICES_START + 1 + INDEX_SEPARATOR + 2 + INDICES_END);
        Assert.assertEquals(1, excludeList.size());
        Values.ExcludedIndices testCaseOne = excludeList.get("testCaseOne");
        Assert.assertFalse(testCaseOne.noIndicesSpecified());
        Assert.assertTrue(testCaseOne.isExcluded(1));
        Assert.assertFalse(testCaseOne.isExcluded(0));
        Assert.assertTrue(testCaseOne.isExcluded(2));
    }

    @Test
    public void singleTestCaseWithMethodCalls_03() {
        Map<String, Values.ExcludedIndices> excludeList =
                createExcludeList(EXCLUDE, "testCaseOne" + INDICES_START + 1 + INDEX_SEPARATOR + 2 + INDEX_SEPARATOR + 508 + INDICES_END);
        Assert.assertEquals(1, excludeList.size());
        Values.ExcludedIndices testCaseOne = excludeList.get("testCaseOne");
        Assert.assertFalse(testCaseOne.noIndicesSpecified());
        Assert.assertTrue(testCaseOne.isExcluded(1));
        Assert.assertFalse(testCaseOne.isExcluded(0));
        Assert.assertTrue(testCaseOne.isExcluded(2));
        Assert.assertTrue(testCaseOne.isExcluded(508));
    }

    @Test
    public void singleTestCaseWithMethodCalls_04() {
        Map<String, Values.ExcludedIndices> excludeList =
                createExcludeList(EXCLUDE, "testCaseOne" + INDICES_START + 0 + RANGE_DELIMITER + 2 + INDEX_SEPARATOR + 508 + INDICES_END);
        Assert.assertEquals(1, excludeList.size());
        Values.ExcludedIndices testCaseOne = excludeList.get("testCaseOne");
        Assert.assertFalse(testCaseOne.noIndicesSpecified());
        Assert.assertTrue(testCaseOne.isExcluded(1));
        Assert.assertTrue(testCaseOne.isExcluded(0));
        Assert.assertTrue(testCaseOne.isExcluded(2));
        Assert.assertTrue(testCaseOne.isExcluded(508));
        Assert.assertFalse(testCaseOne.isExcluded(509));
    }

    @Test
    public void singleTestCaseWithMethodCalls_05() {
        Map<String, Values.ExcludedIndices> excludeList =
                createExcludeList(EXCLUDE, "testCaseOne" + INDICES_START + 0 + RANGE_DELIMITER + 1234567L + INDICES_END);
        Assert.assertEquals(1, excludeList.size());
        Values.ExcludedIndices testCaseOne = excludeList.get("testCaseOne");
        Assert.assertFalse(testCaseOne.noIndicesSpecified());
        for (long i = 0; i <= 1234567L; i++) {
            Assert.assertTrue(testCaseOne.isExcluded(i));
        }
    }

    @Test
    public void singleTestCaseWithMethodCalls_06() {
        Map<String, Values.ExcludedIndices> excludeList =
                createExcludeList(EXCLUDE, "testCaseOne" + INDICES_START + 15 + RANGE_DELIMITER + 567 + INDEX_SEPARATOR + 789 + RANGE_DELIMITER + 8998 + INDICES_END);
        Assert.assertEquals(1, excludeList.size());
        Values.ExcludedIndices testCaseOne = excludeList.get("testCaseOne");
        checkTwoRanges(testCaseOne);

    }

    @Test
    public void singleTestCaseWithMethodCalls_7() {
        Map<String, Values.ExcludedIndices> excludeList =
                createExcludeList(EXCLUDE,
                        "testCaseOne" + INDICES_START + 15 + INDEX_SEPARATOR + 16 + RANGE_DELIMITER + 567 + INDEX_SEPARATOR + 789 + RANGE_DELIMITER + 8997 + INDEX_SEPARATOR + 8998 + INDICES_END);
        Assert.assertEquals(1, excludeList.size());
        Values.ExcludedIndices testCaseOne = excludeList.get("testCaseOne");
        checkTwoRanges(testCaseOne);

    }

    @Test
    public void twoTestCasesWithMethodCalls_7() {
        Map<String, Values.ExcludedIndices> excludeList =
                createExcludeList(EXCLUDE,
                        "testCaseOne" + INDICES_START + 15 + INDEX_SEPARATOR + 16 + RANGE_DELIMITER + 567 + INDEX_SEPARATOR + 789 + RANGE_DELIMITER + 8997 + INDEX_SEPARATOR + 8998 + INDICES_END
                                + EXCLUDE_DELIMETER +
                                "testCase02" + INDICES_START + 15 + INDEX_SEPARATOR + 16 + RANGE_DELIMITER + 567 + INDEX_SEPARATOR + 789 + RANGE_DELIMITER + 8997 + INDEX_SEPARATOR + 8998 + INDICES_END
                );
        Assert.assertEquals(2, excludeList.size());
        checkTwoRanges(excludeList.get("testCaseOne"));
        checkTwoRanges(excludeList.get("testCase02"));
    }

    @Test
    public void threeTestCasesWithMethodCalls_7() {
        Map<String, Values.ExcludedIndices> excludeList =
                createExcludeList(EXCLUDE,
                        "testCaseOne" + INDICES_START + 15 + INDEX_SEPARATOR + 16 + RANGE_DELIMITER + 567 + INDEX_SEPARATOR + 789 + RANGE_DELIMITER + 8997 + INDEX_SEPARATOR + 8998 + INDICES_END
                                + EXCLUDE_DELIMETER +
                                "testCase02" + INDICES_START + 15 + INDEX_SEPARATOR + 16 + RANGE_DELIMITER + 567 + INDEX_SEPARATOR + 789 + RANGE_DELIMITER + 8997 + INDEX_SEPARATOR + 8998 + INDICES_END
                                + EXCLUDE_DELIMETER + "third_03"
                );
        Assert.assertEquals(3, excludeList.size());
        checkTwoRanges(excludeList.get("testCaseOne"));
        checkTwoRanges(excludeList.get("testCase02"));
        Values.ExcludedIndices third_03 = excludeList.get("third_03");
        Assert.assertTrue(third_03.noIndicesSpecified());
    }

    @Test
    public void threeTestCasesWithMethodCalls_8() {
        Map<String, Values.ExcludedIndices> excludeList =
                createExcludeList(EXCLUDE,
                        "testCaseOne" + INDICES_START + 15 + INDEX_SEPARATOR + 16 + RANGE_DELIMITER + 567 + INDEX_SEPARATOR + 789 + RANGE_DELIMITER + 8997 + INDEX_SEPARATOR + 8998 + INDICES_END
                                + EXCLUDE_DELIMETER +
                                "testCase02" + INDICES_START + 15 + INDEX_SEPARATOR + 16 + RANGE_DELIMITER + 567 + INDEX_SEPARATOR + 789 + RANGE_DELIMITER + 8997 + INDEX_SEPARATOR + 8998 + INDICES_END
                                + EXCLUDE_DELIMETER + "third_03" + INDICES_START + 783877474 + INDICES_END
                );
        Assert.assertEquals(3, excludeList.size());
        checkTwoRanges(excludeList.get("testCaseOne"));
        checkTwoRanges(excludeList.get("testCase02"));
        Values.ExcludedIndices third_03 = excludeList.get("third_03");
        Assert.assertFalse(third_03.noIndicesSpecified());
        Assert.assertTrue(third_03.isExcluded(783877474));
    }

    @Test
    public void threeTestCasesOneWithCalls_01() {
        Map<String, Values.ExcludedIndices> excludeList = createExcludeList(EXCLUDE, "testCaseOne" + EXCLUDE_DELIMETER + "testCaseTwo" + EXCLUDE_DELIMETER + "three" + INDICES_START + 500 + INDICES_END);
        Assert.assertEquals(3, excludeList.size());
        Assert.assertTrue(excludeList.get("testCaseOne").noIndicesSpecified());
        Assert.assertTrue(excludeList.get("testCaseTwo").noIndicesSpecified());
        Assert.assertFalse(excludeList.get("three").noIndicesSpecified());
        Assert.assertTrue(excludeList.get("three").isExcluded(500));
    }


    private void checkTwoRanges(Values.ExcludedIndices testCaseOne) {
        Assert.assertFalse(testCaseOne.noIndicesSpecified());
        for (long i = 0; i <= 14L; i++) {
            Assert.assertFalse(testCaseOne.isExcluded(i));
        }
        for (long i = 15; i <= 567L; i++) {
            Assert.assertTrue(testCaseOne.isExcluded(i));
        }
        for (long i = 568; i <= 788L; i++) {
            Assert.assertFalse(testCaseOne.isExcluded(i));
        }
        for (long i = 789; i <= 8998L; i++) {
            Assert.assertTrue(testCaseOne.isExcluded(i));
        }
    }


}
