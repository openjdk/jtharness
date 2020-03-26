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
package com.sun.javatest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;

import com.sun.javatest.Harness;
import com.sun.javatest.FileParameters;
import com.sun.javatest.Parameters;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestSuite;
import com.sun.javatest.TestUtil;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;

public class TraceTest implements Harness.Observer {


    @Test
    public void main() throws IOException {
        TraceTest t = new TraceTest();
        boolean ok = t.run(
                new String[]{
                        System.getProperty("build.classes"),
                        TestUtil.getPathToTestTestSuite("empty") + File.separator + "testsuite.html",
                        "empty.jte",
                        TestUtil.createTempDirAndReturnAbsPathString("TraceTest-work")
                }
                , System.out);
        Assert.assertTrue(ok);
    }

    public boolean run(String[] args, PrintStream log) {
        this.log = log;

        if (args.length != 4) {
            log.println("Wrong # args (expected 5, got " + args.length + ")");
            return false;
        }

        try {
            File harnessClassDir = new File(args[0]);
            TestSuite emptyTestSuite = TestSuite.open(new File(args[1]));
            File envFile = new File(args[2]);

            File wdp = new File(args[3]);
            if (wdp.exists()) {
                clearDir(wdp);
            } else {
                wdp.mkdirs();
            }

            WorkDirectory testWorkDir = WorkDirectory.create(wdp, emptyTestSuite);

            System.err.println("create");
            FileParameters params = new FileParameters();
            System.err.println("set work dir");
            params.setWorkDirectory(testWorkDir);
            System.err.println("set init files");
            params.setTests(new String[]{});
            System.err.println("set excl files");
            params.setExcludeMode(FileParameters.NO_EXCLUDE_LIST);
            System.err.println("set keywords");
            params.setKeywordsMode(FileParameters.NO_KEYWORDS);
            System.err.println("set prior status");
            params.setPriorStatusMode(FileParameters.NO_PRIOR_STATUS);
            System.err.println("set report");
            params.setReportDir(testWorkDir.getRoot());
            System.err.println("set env files");
            params.setEnvFiles(new File[]{envFile});
            System.err.println("set env name");
            params.setEnvName("empty");
            if (!params.isValid()) {
                log.println("problem setting parameters: " + params.getErrorMessage());
                return false;
            }

            log.println("test suite: " + emptyTestSuite.getPath());
            log.println("work dir: " + testWorkDir.getPath());

            Date beforeDate = now();
            Harness h = new Harness(harnessClassDir);
            h.addObserver(this);
            System.err.println("start batch");
            h.batch(params);
            System.err.println("done");

            Date afterDate = now();

            File trace = testWorkDir.getSystemFile(TRACE_FILE);
            BufferedReader in = new BufferedReader(new FileReader(trace));
            String line = in.readLine();

            if (!line.startsWith(TRACE_HEADER_PREFIX)) {
                log.println("Trace file does not begin with expected line");
                return false;
            }

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
            df.setTimeZone(TimeZone.getDefault());
            // DateFormat copied from java.lang.Date.toString()
            Date traceDate = df.parse(line.substring(TRACE_HEADER_PREFIX.length()));

            log.println("before: " + beforeDate);
            log.println("trace: " + traceDate);
            log.println("after: " + afterDate);

            if (beforeDate.after(traceDate) || traceDate.after(afterDate)) {
                log.println("before: " + beforeDate.getTime());
                log.println("trace: " + traceDate.getTime());
                log.println("after: " + afterDate.getTime());
                log.println("Trace file does not have valid date");
                return false;
            }

            return true;
        } catch (Harness.Fault e) {
            log.println("problem running harness: " + e.getMessage());
            return false;
        } catch (TestSuite.Fault e) {
            log.println("problem opening test suite: " + e.getMessage());
            return false;
        } catch (WorkDirectory.Fault e) {
            log.println("problem creating work directory: " + e.getMessage());
            return false;
        } catch (ParseException e) {
            log.println("Cannot parse date: " + e.getMessage());
            return false;
        } catch (FileNotFoundException e) {
            log.println("Can't find file: " + e.getMessage());
            return false;
        } catch (IOException e) {
            log.println("Problem with trace file: " + e);
            return false;
        } finally {
            log.flush();
        }
    }

    private Date now() {
        // now() returns the current time, rounded down to the nearest second.
        // This is because we are going to compare it against times read from
        // a file, which do not have milliseconds in either.
        long t = System.currentTimeMillis();
        t = t / 1000 * 1000;    // remove millis
        return new Date(t);
    }

    public synchronized void error(String s) {
        log.println("Error: " + s);
    }

    private static void clearDir(File dir) {
        String[] list = dir.list();
        if (list != null) {
            for (String aList : list) {
                File f = new File(dir, aList);
                if (f.isDirectory()) {
                    clearDir(f);
                }
                f.delete();
            }
        }
    }

    public void startingTestRun(Parameters params) {
    }

    public void startingTest(TestResult tr) {
    }

    public void finishedTest(TestResult tr) {
    }

    public void stoppingTestRun() {
    }

    public void finishedTesting() {
    }

    public void finishedTestRun(boolean allOK) {
    }

    private static final String TRACE_FILE = "harness.trace";
    private static final String TRACE_HEADER_PREFIX = "# Trace file started at ";

    private PrintStream log;
}
