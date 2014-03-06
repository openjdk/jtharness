/*
 * $Id$
 *
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

import com.sun.javatest.Harness;
import com.sun.javatest.Parameters;
import com.sun.javatest.Status;
import com.sun.javatest.TestFinder;
import com.sun.javatest.TestResult;
import com.sun.javatest.tool.Command;
import com.sun.javatest.tool.CommandContext;
import com.sun.javatest.tool.VerboseCommand;
import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.I18NResourceBundle;

class RunTestsCommand extends Command
{
    static String getName() {
        return "runTests";
    }

    private static final String DATE_OPTION = "date";
    private static final String NON_PASS_OPTION = "non-pass";
    private static final String START_OPTION = "start";
    private static final String FINISH_OPTION = "stop";
    private static final String PROGRESS_OPTION = "progress";

    private Harness harness;

    static void initVerboseOptions() {
        VerboseCommand.addOption(DATE_OPTION, new HelpTree.Node(i18n, "runTests.verbose.date"));
        VerboseCommand.addOption(NON_PASS_OPTION, new HelpTree.Node(i18n, "runTests.verbose.nonPass"));
        VerboseCommand.addOption(START_OPTION, new HelpTree.Node(i18n, "runTests.verbose.start"));
        VerboseCommand.addOption(FINISH_OPTION, new HelpTree.Node(i18n, "runTests.verbose.stop"));
        VerboseCommand.addOption(PROGRESS_OPTION, new HelpTree.Node(i18n, "runTests.verbose.progress"));
    }

    RunTestsCommand() {
        super(getName());
    }

    RunTestsCommand(Iterator argIter) {
        super(getName());
    }

    public boolean isActionCommand() {
        return true;
    }

    public void run(CommandContext ctx) throws Fault {
        this.ctx = ctx;

        try {
            Parameters p = getConfig(ctx); // throws fault if not set

            // might want to move harness down into CommandContext
            // to share with GUI
            Harness h = new Harness();
            harness = h;

            Harness.Observer[] observers = ctx.getHarnessObservers();
            for (int i = 0; i < observers.length; i++)
                h.addObserver(observers[i]);

            // should really merge VerboseObserver and BatchObserver
            VerboseObserver vo = new VerboseObserver(ctx);
            h.addObserver(vo);

            BatchObserver bo = new BatchObserver();
            h.addObserver(bo);
            p.getTestSuite().getTestFinder().setErrorHandler(bo);

            boolean ok = h.batch(p);

            if (bo.getFinderErrorCount() > 0) {
                // other problems during run
                ctx.printErrorMessage(i18n, "runTests.warnError");
            }

            if (!ctx.isVerboseQuiet()) {
                long tt = h.getElapsedTime();
                long setupT = h.getTotalSetupTime();
                long cleanupT = h.getTotalCleanupTime();
                ctx.printMessage(i18n, "runTests.totalTime", tt/1000L);
                ctx.printMessage(i18n, "runTests.setupTime", setupT/1000L);
                ctx.printMessage(i18n, "runTests.cleanupTime", cleanupT/1000L);

                showResultStats(bo.getStats());
            }


            int testsFound = h.getTestsFoundCount();

            if (testsFound > 0 && !ctx.isVerboseQuiet())
                ctx.printMessage(i18n, "runTests.resultsDone", p.getWorkDirectory().getPath());
            int[] stats = bo.getStats();

            if (!ok) {
                if (testsFound > 0 &&
                    testsFound != stats[Status.PASSED]) {
                    // some tests are actually not passed, print
                    // appropriate message
                    ctx.printErrorMessage(i18n, "runTests.testsFailed");
                }
            }

            ctx.addTestStats(stats);
        }
        catch (Harness.Fault e) {
            throw new Fault(i18n, "runTests.harnessError", e.getMessage());
        }
        catch (InterruptedException e) {
            throw new Fault(i18n, "runTests.interrupted");
        }
    }

    private void showResultStats(int[] stats) {
        int p = stats[Status.PASSED];
        int f = stats[Status.FAILED];
        int e = stats[Status.ERROR];
        int nr = stats[Status.NOT_RUN] =
            harness.getTestsFoundCount() - p - f - e;


        if (p + f + e + nr == 0)
            ctx.printMessage(i18n, "runTests.noTests");
        else {
            ctx.printMessage(i18n, "runTests.tests",
                      new Object[] {
                          new Integer(p),
                          new Integer((p > 0) && (f + e + nr > 0) ? 1 : 0),
                          new Integer(f),
                          new Integer((f > 0) && (e + nr > 0) ? 1 : 0),
                          new Integer(e),
                          new Integer((e > 0) && (nr > 0) ? 1 : 0),
                          new Integer(nr)
                              });
        }
    }

    //-------------------------------------------------------------------------

    private CommandContext ctx;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(RunTestsCommand.class);

    //-------------------------------------------------------------------------

    private class BatchObserver
        implements Harness.Observer, TestFinder.ErrorHandler {

        int[] getStats() {
            return stats;
        }

        int getFinderErrorCount() {
            return finderErrors;
        }

        public void startingTestRun(Parameters params) {
            stats = new int[Status.NUM_STATES];
        }

        public void startingTest(TestResult tr) { }

        public void finishedTest(TestResult tr) {
            stats[tr.getStatus().getType()]++;
        }

        public void stoppingTestRun() { }

        public void finishedTesting() { }
        public void finishedTestRun(boolean allOK) { }

        public void error(String msg) {
            ctx.printMessage(i18n, "runTests.error", msg);
            finderErrors++;
        }

        private int[] stats;
        private int finderErrors;
    }

    private class VerboseObserver implements Harness.Observer
    {
        VerboseObserver(CommandContext ctx) {
            this.ctx = ctx;
            this.out = ctx.getLogWriter();

            quiet_flag = ctx.isVerboseQuiet();
            df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                                DateFormat.MEDIUM);
            ctx.addHarnessObserver(this);

            boolean defaultFlag = ctx.getVerboseOptionValue("default", false);
            options = new boolean[OPTION_COUNT];
            options[NO_DATE] = !ctx.isVerboseTimestampEnabled();
            options[NON_PASS] = ctx.getVerboseOptionValue(NON_PASS_OPTION, false);
            options[START] = ctx.getVerboseOptionValue(START_OPTION, false);
            options[FINISH] = ctx.getVerboseOptionValue(FINISH_OPTION, false);
            options[PROGRESS] = ctx.getVerboseOptionValue(PROGRESS_OPTION, defaultFlag);
        }

        public boolean isVerbose(int kind) {
            if (quiet_flag)
                return false;

            if (max_flag)
                return true;

            if (kind < OPTION_COUNT)
                return options[kind];
            else
                return false;
        }

        private void printTimestamp() {
            if (quiet_flag || options[NO_DATE])
                return;

            out.print(df.format(new Date()));
            out.print(" ");
        }

        // ---- Harness.Observer ----
        public void startingTestRun(Parameters params) {
            stats = new int[Status.NUM_STATES];

            if (!quiet_flag) {
                if (progressOnline)
                    out.println();
                printTimestamp();
                ctx.printMessage(i18n, "cmgr.verb.strt",
                    params.getEnv().getName());
                out.flush();
                progressOnline = false;
            }
        }

        public void startingTest(TestResult tr) {
            if (!isVerbose(START))
                return;

            if (progressOnline)
                out.println();

            printTimestamp();
            ctx.printMessage(i18n, "cmgr.verb.tsts", tr.getTestName());
            out.flush();

            progressOnline = false;
        }

        public void finishedTest(TestResult tr) {
            Status s = tr.getStatus();
            stats[s.getType()]++;

            switch(s.getType()) {
                case Status.FAILED:
                case Status.ERROR:
                    if (isVerbose(NON_PASS) || isVerbose(FINISH)) {
                        printFinish(s, tr);
                        progressOnline = false;
                    }
                    break;
                default:
                    if (isVerbose(FINISH)) {
                        printFinish(s, tr);
                        progressOnline = false;
                    }
            }   // switch

            printStats();
            out.flush();
        }

        public void stoppingTestRun() {
            if (progressOnline)
                out.println();

            printTimestamp();
            ctx.printMessage(i18n, "cmgr.verb.stpng");
            out.flush();

            progressOnline = false;
        }

        public void finishedTesting() {
            if (!quiet_flag) {
                if (progressOnline)
                    out.println();

                printTimestamp();
                ctx.printMessage(i18n, "cmgr.verb.donerun");
                out.flush();

                progressOnline = false;
            }
        }

        public void finishedTestRun(boolean allOK) {
            if (!quiet_flag) {
                if (progressOnline)
                    out.println();

                printTimestamp();
                ctx.printMessage(i18n, "cmgr.verb.finish");
                out.flush();

                progressOnline = false;
            }
        }

        public void error(String msg) {
            if (progressOnline)
                out.println();

            printTimestamp();
            ctx.printErrorMessage(i18n, "cmgr.verb.err", msg);
            out.flush();

            progressOnline = false;
        }

        // utility methods
        private void printStats() {
            if (!isVerbose(PROGRESS))
                return;

            if (progressOnline)
                out.print("\r");

            int p = stats[Status.PASSED];
            int f = stats[Status.FAILED];
            int e = stats[Status.ERROR];
            int nr = stats[Status.NOT_RUN] =
                harness.getTestsFoundCount() - p - f - e;

            out.print(i18n.getString("cmgr.verb.prog",
                      new Object[] {
                          new Integer(p),
                          new Integer(f),
                          new Integer(e),
                          new Integer(nr)
                      }));
            out.print("    ");

            progressOnline = true;
        }

        private void printFinish(Status s, TestResult tr) {
            if (!quiet_flag) {
                // need to create newline if we are doing single-line
                // updates
                if (progressOnline)
                    out.println();

                printTimestamp();
                String[] args = {tr.getTestName(),
                                 s.toString()};
                ctx.printMessage(i18n, "cmgr.verb.tstd", args);
                out.flush();
                progressOnline = false;
            }
        }

        /**
         * Is the text being displayed using println during the run?
         * This affects our ability to update a progress counter.
         */
        private boolean isScolling() {
            if (!isVerbose(START) && !isVerbose(FINISH))
                return false;
            else
                return true;
        }

        private boolean[] options;
        private boolean quiet_flag = false;
        private boolean max_flag = false;
        private DateFormat df;
        private CommandContext ctx;
        private PrintWriter out;
        private int[] stats;
        private boolean progressOnline = false;

        public static final int NO_DATE = 0;
        public static final int NON_PASS = 1;
        public static final int START = 2;
        public static final int FINISH = 3;
        public static final int PROGRESS = 4;

        public static final int DEFAULT = PROGRESS;

        private static final int OPTION_COUNT = 5;
    }
}
