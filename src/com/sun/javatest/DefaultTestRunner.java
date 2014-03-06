/*
 * $Id$
 *
 * Copyright (c) 2004, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.javatest.util.BackupPolicy;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * Traditional implementation of the test execution engine which has been
 * used throughout the JT Harness 2.x harness.  It supplies all the basic
 * for creating threads for each test, running the <code>Script</code>,
 * and handling timeouts.
 */
public class DefaultTestRunner extends TestRunner
{
    public synchronized boolean runTests(Iterator testIter)
        throws InterruptedException
    {
        this.testIter = testIter;

        Thread[] threads = new Thread[getConcurrency()];
        activeThreads = new HashSet();
        allPassed = true;

        try {
            int n = 0;
            while (!stopping) {
                for (int i = 0; i < threads.length; i++) {
                    Thread t = threads[i];
                    if (t == null || !activeThreads.contains(t)) {
                        int prio = Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 1);
                        t = new Thread() {
                                public void run() {
                                    try {
                                        TestDescription td;
                                        while ((td = nextTest()) != null) {
                                            if (!runTest(td))
                                                allPassed = false;
                                        }
                                    }
                                    finally {
                                        // Inform runner this thread is dying, so it can start another thread
                                        // to replace it, if necessary.
                                        threadExiting(this);
                                    }
                                }
                            };
                        t.setName("DefaultTestRunner:Worker-" + i + ":" + n++);
                        t.start();
                        t.setPriority(prio);
                        activeThreads.add(t);
                        threads[i] = t;
                    }
                }
                wait();
            }
            // Wait for all the threads to finish so they don't get nuked by the
            // finally code. Order is not important so just wait for them one at a time.
            // Note we can't simply join with the thread because that gives a deadlock
            // on our lock.
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] != null) {
                    while (activeThreads.contains(threads[i]))
                        wait();
                    threads[i] = null;
                }
            }
        }
        catch (InterruptedException ex) {
            // The thread has been interrupted

            stopping = true;    // stop workers from starting any new tests

            // interrupt the worker threads
            for (Iterator iter = activeThreads.iterator() ; iter.hasNext(); ) {
                Thread t = (Thread) (iter.next());
                t.interrupt();
            }

            // while a short while (a couple of seconds) for tests to clean up
            // before we nuke them
            long now = System.currentTimeMillis();
            try {
                while (activeThreads.size() > 0 && (System.currentTimeMillis() - now < 2000)) {
                    wait(100);
                }
            }
            catch (InterruptedException e) {
            }

            // rethrow the original exception so the caller knows what's happened
            throw ex;
        }
        finally {
            // ensure all child threads killed
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] != null)
                    Deprecated.invokeThreadStop(threads[i]);
            }
        }

        return allPassed;
    }

    private synchronized void threadExiting(Thread t) {
        activeThreads.remove(t);
        notifyAll();
    }

    private synchronized TestDescription nextTest() {
        if (stopping)
            return null;

        if (testIter.hasNext())
            return (TestDescription) (testIter.next());
        else {
            stopping = true;
            return null;
        }
    }

    private boolean runTest(TestDescription td) {
        WorkDirectory workDir = getWorkDirectory();
        TestResult result = null;

        boolean scriptUsesNotifier = false;

        try {
            TestSuite testSuite = getTestSuite();
            TestEnvironment env = getEnvironment();
            BackupPolicy backupPolicy = getBackupPolicy();

            String[] exclTestCases = getExcludedTestCases(td);
            Script s = testSuite.createScript(td, exclTestCases, env.copy(), workDir, backupPolicy);

            scriptUsesNotifier = s.useNotifier();
            if (!scriptUsesNotifier) {
                notifyStartingTest(s.getTestResult());
            } else {
                delegateNotifier(s);
            }

            result = s.getTestResult();

            s.run();
        }
        catch (ThreadDeath e) {
            String url = td.getRootRelativeURL();
            workDir.log(i18n, "dtr.threadKilled", url);
            result = createErrorResult(td, i18n.getString("dtr.threadKilled", url), e);
            throw e;
        }
        catch (Throwable e) {
            String url = td.getRootRelativeURL();
            workDir.log(i18n, "dtr.unexpectedThrowable",
                        new Object[] { url, e, classifyThrowable(e) });
            result = createErrorResult(td,
                                       i18n.getString("dtr.unexpectedThrowable",
                                                      new Object[] { url, e, classifyThrowable(e) }),
                                       e);
        }
        finally {
            if (result == null) {
                String url = td.getRootRelativeURL();
                result = createErrorResult(td, i18n.getString("dtr.noResult", url), null);
            }

            if (!scriptUsesNotifier) {
                try {
                    notifyFinishedTest(result);
                }
                catch (ThreadDeath e) {
                    String url = td.getRootRelativeURL();
                    workDir.log(i18n, "dtr.threadKilled", url);
                    throw e;
                }
                catch (Throwable e) {
                    String url = td.getRootRelativeURL();
                    workDir.log(i18n, "dtr.unexpectedThrowable", new Object[] { url, e, classifyThrowable(e) });
                }
            }
        }

        return (result.getStatus().getType() == Status.PASSED);
    }

    private TestResult createErrorResult(TestDescription td, String reason, Throwable t) { // make more i18n
        Status s = Status.error(reason);
        TestResult tr;
        if (t == null)
            tr = new TestResult(td, s);
        else {
            tr = new TestResult(td);
            TestResult.Section trs = tr.createSection(i18n.getString("dtr.details"));
            PrintWriter pw = trs.createOutput(i18n.getString("dtr.stackTrace"));
            t.printStackTrace(pw);
            pw.close();
            tr.setStatus(s);
        }

        WorkDirectory workDir = getWorkDirectory();
        BackupPolicy backupPolicy = getBackupPolicy();
        try {
            tr.writeResults(workDir, backupPolicy);
        }
        catch (Exception e) {
            workDir.log(i18n, "dtr.unexpectedThrowable",
                new Object[] {td.getRootRelativeURL(), e, EXCEPTION });
        }
        return tr;
    }

    private Integer classifyThrowable(Throwable t) {
        if (t instanceof Exception)
            return EXCEPTION;
        else if (t instanceof Error)
            return ERROR;
        else
            return THROWABLE;
    }

    // constants used by classifyThrowable and i18n key unexpectedThrowable
    private static final Integer EXCEPTION = new Integer(0);
    private static final Integer ERROR = new Integer(1);
    private static final Integer THROWABLE = new Integer(2);


    private Iterator testIter;
    private Set activeThreads;
    private boolean allPassed;
    private boolean stopping;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(DefaultTestRunner.class);
}
