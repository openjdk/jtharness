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
package com.sun.javatest;

import com.sun.javatest.finder.HTMLTestFinder;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.PrintStream;

public class UrlFilterTest {

    @Test
    public void test() throws Exception {

        UrlFilterTest st = new UrlFilterTest();
        boolean ok = st.run(new String[] {TestUtil.getPathToTestTestSuite("initurl")}, System.out);
        Assert.assertTrue(ok);
    }

    public boolean run(String[] args, PrintStream out) throws /*IOException,*/ TestFinder.Fault {
        this.out = out;

        out.println("Setting finder to dirwalk mode.");
        String[] tfArgs = {"-dirWalk"};

        File tsRoot = new File(args[0]);
        out.println("Setting TS root to " + tsRoot.getAbsolutePath());
        hf.init(tfArgs, new File(tsRoot.getAbsolutePath()), null);

        boolean ok = true;
        out.println("************ begin set 1 ***************");
        ok &= testSet(urls1, expected1);
        out.println("************ begin set 2 ***************");
        ok &= testSet(urls2, expected2);
        out.println("************ begin set 3 ***************");
        ok &= testSet(urls3, expected3);
        out.println("************ begin set 4 ***************");
        ok &= testSet(urls4, expected4);
        out.println("************ begin set 5 ***************");
        ok &= testSet(urls5, expected5);
        out.println("************ begin set 6 ***************");
        ok &= testSet(urls6, expected6);
        out.println("************ begin set 7 ***************");
        ok &= testSet(urls7, expected7);
        out.println("************ begin set 8 ***************");
        ok &= testSet(urls8, expected8);
        out.println("************ end of tests **************");

        return ok;
    }

    private boolean testSet(String[] urls, int expected) {
        TestFinderQueue tfq = new TestFinderQueue();
        tfq.setTestFinder(hf);
        // select all the tests
        //tfq.setTests(new File[] {new File("comp"), new File("exec")});
        tfq.setTests((String[])null);

        InitialUrlFilter iuf = new InitialUrlFilter(urls);
        TestDescription td = null;
        int before = 0;  // num. tests before filtering
        int after = 0;  // num. tests after filtering

        out.println("Initial URLs are:");
        if (urls == null)
            out.println("   -> NULL SET");
        else if (urls.length == 0)
            out.println("   -> EMPTY SET");
        else
            for (String url : urls) out.println("   -> " + url);


        while ((td = tfq.next()) != null) {
            before++;

            if (iuf.accepts(td)) {
                after++;
                out.println("Filter accepted " + td.getRootRelativeURL());
            } else
                out.println("Filter rejected " + td.getRootRelativeURL());
        }   // while

        if (before == EXPECTED_TESTS_BEFORE && after == expected) {
            out.println("found " + before + "/" + EXPECTED_TESTS_BEFORE + " tests before filtering");
            out.println("found " + after + "/" + expected + " tests after filtering");
            out.println("PASS");
            return true;
        } else {
            out.println("found " + before + "/" + EXPECTED_TESTS_BEFORE + " tests before filtering");
            out.println("found " + after + "/" + expected + " tests after filtering");
            out.println("FAILED");
            return false;
        }
    }

    // 5 + 3
    private String[] urls1 = {"exec", "comp/foo"};
    private int expected1 = 8;

    // 1 + 3
    private String[] urls2 = {"comp/foo/set1.html", "comp/index.html"};
    private int expected2 = 4;

    // 3 + 5
    private String[] urls3 = {"comp/index.html", "exec/index.html"};
    private int expected3 = 8;

    // 5 + 3
    // test trailing slash on dirs; unlike set1
    private String[] urls4 = {"exec/", "comp/foo/"};
    private int expected4 = 8;

    // exact matches
    private String[] urls5 = {"comp/foo/set1.html#CompSucc"};
    private int expected5 = 1;

    // select all
    private String[] urls6 = new String[0];
    private int expected6 = 11;

    private String[] urls7 = null;
    private int expected7 = 11;

    // no matches, partial matches are not valid
    private String[] urls8 = {"com"};
    private int expected8 = 0;

    private HTMLTestFinder hf = new HTMLTestFinder();
    private PrintStream out;

    /**
     * Number of tests in the testsuite.
     */
    private int EXPECTED_TESTS_BEFORE = 11;


}
