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

package com.sun.javatest.functional;

import com.sun.javatest.Harness;
import com.sun.javatest.JavaTestSecurityManager;
import com.sun.javatest.TU;
import com.sun.javatest.report.Report;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.Main;
import com.sun.javatest.util.ExitCount;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public abstract class TestSuiteRunningTestBase extends TestBase {

    private String java_home;
    protected String workDirAbsPath;
    protected String reportDirAbsPath;
    protected Path summaryTXT;
    private List<String> summaryTxt;

    protected void runJavaTest() {

        List<String> args = new LinkedList<>();
        args.add("-Eworkdir=" + workDirAbsPath);
        args.add("-batch");
        args.add("-EJAVAC=" + pathToJavac);
        args.add("-EJAVA=" + pathToJava);
        args.add("-params");
        args.add( "-testsuite");
        args.add( TU.getPathToTestTestSuite(getTestsuiteName()));
        args.add("-envfile");
        args.add(getEnvfileName());
        args.add("-env");
        args.add(getEnvName());
        args.add("-workDir");
        args.add(workDirAbsPath);
        args.add("-report");
        args.add(reportDirAbsPath);
        args.addAll(getTailArgs());

        com.sun.javatest.tool.Main.main(args.toArray(new String[args.size()]));

    }

    protected abstract List<String> getTailArgs();

    protected abstract String getEnvName();

    protected abstract String getEnvfileName();

    protected abstract String getTestsuiteName();

    @Before
    public void setup() throws IOException {
//        System.setProperty("javatest.preferences.file", "NONE");
//        System.setProperty("debug.com.sun.javatest.TRT_TreeNode", "2");


        System.setProperty(Report.REPORT_FORMATS_TO_LOAD,
                "com.sun.javatest.report.HTMLReport" +
                        ",com.sun.javatest.report.PlainTextReport" +
                        ",com.sun.javatest.report.XMLReport");

        System.setProperty(Main.COMMAND_MANAGERS_TO_LOAD,
                "com.sun.javatest.agent.AgentMonitorCommandManager" +
                        ",com.sun.javatest.batch.BatchManager" +
                        ",com.sun.javatest.report.ReportManager" +
                        ",com.sun.javatest.tool.ConfigManager" +
                        ",com.sun.javatest.tool.EnvironmentManager" +
                        ",com.sun.javatest.tool.HttpManager" +
                        ",com.sun.javatest.tool.LogManager");

        System.setProperty(Desktop.TOOL_MANAGERS_TO_LOAD,
                "com.sun.javatest.agent.AgentMonitorToolManager," +
                        "com.sun.javatest.exec.ExecToolManager," +
                        "com.sun.javatest.mrep.ReportToolManager");

        System.setProperty(Harness.DEBUG_OBSERVER_CLASSNAME_SYS_PROP,
                "com.sun.javatest.functional.TestObserver");

        Path reportDir = createTempDirectory("jt-report-");
        reportDirAbsPath = reportDir.toAbsolutePath().toString();

        // not allowing to exit
        ExitCount.inc();

        workDirAbsPath = createTempDirAndReturnAbsPathString("jt-work-");
        summaryTXT = reportDir.resolve("text").resolve("summary.txt");
    }

    @After
    public void tearDown() {
        // now letting the unit test to complete
        JavaTestSecurityManager.setAllowExit(true);
        System.setSecurityManager(null);
    }

    private List<String> getSummaryTxt() {
        if (summaryTxt == null) {
            try {
                summaryTxt = Files.readAllLines(summaryTXT, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return summaryTxt;
    }


    protected void checkLineInSummary(int i, String s) {
        Assert.assertEquals(s, getSummaryTxt().get(i));
    }

    protected void checkLinesInSummary(String... lines) {
        Assert.assertEquals(lines.length, getSummaryTxt().size());
        for (int i = 0; i < lines.length; i++) {
            checkLineInSummary(i, lines[i]);
        }
    }

}
