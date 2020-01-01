/*
 * $Id$
 *
 * Copyright (c) 2001, 2019, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.interview;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import com.sun.interview.Interview;
import com.sun.interview.InterviewSetTest;
import com.sun.interview.Question;
import com.sun.javatest.TU;
import com.sun.javatest.TestSuite;
import org.junit.Assert;
import org.junit.Test;

public class InterviewSaveTest {

    @Test
    public void test() throws Exception {
        InterviewSaveTest t = new InterviewSaveTest();
        boolean ok = t.run(new String[] {TU.getPathToTestTestSuite("demotck")});
        Assert.assertTrue(ok);
    }

    boolean run(String[] args)
            throws FileNotFoundException, Interview.Fault, TestSuite.Fault {
        TestSuite ts = TestSuite.open(new File(args[0]));
        Interview interview = ts.createInterview();

        Question[] path = interview.getPath();

        boolean ok = true;

        for (int i = 0; i < path.length; i++) {
            interview.setCurrentQuestion(path[i]);

            Map<String, String> m = new HashMap<>();
            interview.save(m);

            Interview int2 = ts.createInterview();
            int2.load(m);

            if (!equal(interview, int2)) {
                System.err.println("test case " + i + ": mismatch");
                ok = false;
            }
        }

        System.err.println(path.length + " test cases ok");
        return ok;
    }

    private boolean equal(Interview i1, Interview i2) {
        return equal(i1.getCurrentQuestion(), i2.getCurrentQuestion());
    }

    private boolean equal(Question q1, Question q2) {
        if (q1 == null || q2 == null)
            return q1 == q2;

        return equal(q1.getKey(), q2.getKey())
                && equal(q1.getStringValue(), q2.getStringValue());
    }

    private boolean equal(String s1, String s2) {
        return s1 == null || s2 == null ? s1 == s2 : s1.equals(s2);
    }
}
