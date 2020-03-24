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
package com.sun.javatest.finder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.sun.javatest.FileParameters;
import com.sun.javatest.TestUtil;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.TestFinder;
import com.sun.javatest.TestFinderQueue;
import com.sun.javatest.functional.TestBase;
import org.junit.Assert;
import org.junit.Test;

public class ExpandTestFinderTest extends TestBase {

    @Test
    public void test() throws IOException {
        int exitCode;
        ExpandTestFinderTest expandTestFinderTest = new ExpandTestFinderTest();
        exitCode = expandTestFinderTest.run(new String[]{
                new File(TestUtil.getPathToData() + File.separator + "finder" + File.separator + "data" + File.separator + "expand").toPath().toAbsolutePath().toString(),
                TestUtil.createTempDirAndReturnAbsPathString("ExpandTestFinderTest-report"),
                TestUtil.createTempDirAndReturnAbsPathString("ExpandTestFinderTest-work")
        }, System.out);
        Assert.assertEquals(0, exitCode);
    }

    public final int run(String[] args, PrintStream out) {
        this.out = out;

        if (args.length != 3) {
            out.println("wrong number of args, expected 3, got " + args.length);
            return 1;
        }

        int argc = 0;
        testSuite = args[argc++];
        reportDir = args[argc++];
        workDir = args[argc++];

        File rd = new File(reportDir);
        if (!rd.exists()) rd.mkdirs();

        File wd = new File(workDir);
        if (!wd.exists()) wd.mkdirs();

        // Create a map of the execute arguments that we expect
        // from our single test
        Map<String, String> expArgs = new HashMap<>();

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 3; j++)
                for (int k = 0; k < 3; k++) {
                    expArgs.put("a" + i + " b" + j + " c" + j + " d" + k, TRUE);
                    out.println("adding... " + "a" + i + " b" + j + " c" + j + " d" + k);
                }

        // Call our ExpandTestFinder on our single test, then verify
        // that we have exactly one test for the cross-product of
        // execArgs.
        try {
            setProperties();
            params = setParameters();
            if (!params.isValid()) {
                warn("Error: Bad value in parameters");
                warn(params.getErrorMessage());
                return 1;
            }

            TestFinderQueue tfq = createTestFinderQueue();
            TestDescription td;
            while ((td = tfq.next()) != null) {
                String execArgs = td.getParameter("executeArgs");
                out.println("removing... " + execArgs);
                if (expArgs.remove(execArgs) == null)
                    return 1;
            }

            if (!expArgs.isEmpty()) {
                out.println("Expected to have found all values");
                return 1;
            } else {
                out.println("Found all expanded test descriptions");
                return 0;
            }
        } catch (Fault e) {
            warn("Error: Fault!");
            warn(e.getMessage());
            return 1;
        }
    }

    public static class Fault extends Exception {
        public Fault(String msg) {
            super(msg);
        }
    }

    //------- internal methods -------------------------------------------------

    private TestFinderQueue createTestFinderQueue() throws Fault {
        String[] finderArgs = new String[0];
        File testSuiteRoot = params.getTestSuite().getRoot();
        String[] tests = params.getTests();
        TestEnvironment env = params.getEnv();

        try {
            TestFinder tf = new ExpandTestFinder();
            tf.init(finderArgs, testSuiteRoot, env);
            TestFinderQueue tfq = new TestFinderQueue();
            tfq.setTestFinder(tf);
            tfq.setTests(tests);
            return tfq;
        } catch (TestFinder.Fault e) {
            String msg = "error initializing ExpandTestFinde: " + e.getMessage();
            throw new Fault(msg);
        }
    }

    private FileParameters setParameters() {
        FileParameters p = new FileParameters();

        p.setTestSuite(new File(testSuite));
        p.setWorkDirectory(new File(workDir));
        p.setReportDir(new File(reportDir));
        p.setTests("SimpleTest.java");

        p.setEnvName("expand-test");
        p.setEnvFiles((File[])null);

        return p;
    }

    private void setProperties() {
        Properties sysProps = System.getProperties();

        sysProps.put("env.expand-test.script", "dummyScriptName");
        sysProps.put("env.expand-test.finder", ExpandTestFinder.class.getName());

        // force on our expansion stuff
        sysProps.put("env.expand-test.expand.field1", "a0 a1 a2 a3");
        sysProps.put("env.expand-test.expand.field2.foo", "b0 b1 b2");
        sysProps.put("env.expand-test.expand.field2.bar", "c0 c1 c2");
        sysProps.put("env.expand-test.expand.field3", "d0 d1 d2");

        TestEnvironment.addDefaultPropTable("(system properties)", com.sun.javatest.util.PropertyUtils.convertToStringProps(sysProps));
    }

    private void warn(String s) {
        out.println("!!! " + s);
    }

    //------- member variables -------------------------------------------------

    private FileParameters params;

    private String testSuite;
    private String reportDir;
    private String workDir;
    private PrintStream out;

    private static final String TRUE = "true";
}
