/*
 * $Id$
 *
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.util.BackupPolicy;
import com.sun.javatest.util.I18NResourceBundle;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Concurrent implementation of test execution engine
 * based on java.util.concurrent APIs to run tests effectively using a fixed thread pool.
 * Inheriting default test runner's implementaiton of running a single test.
 *
 * THIS IS YET AN EXPERIMENTAL IMPLEMENTATION - USE AT YOUR OWN RISK
 */
@java.lang.Deprecated
public class DefaultTestRunner2 extends DefaultTestRunner {

    private final Object testIterLock = new Object();

    public synchronized boolean runTests(final Iterator<TestDescription> testIter) throws InterruptedException {

        int concurrency = getConcurrency();
        final AtomicBoolean allPassed = new AtomicBoolean(true);
        ExecutorService executorService = Executors.newFixedThreadPool(concurrency);
        final CountDownLatch doneSignal = new CountDownLatch(concurrency);

        for (int i = 0; i < concurrency; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    TestDescription td;
                    while ((td = nextTest()) != null) {
                        if (!runTest(td)) {
                            allPassed.set(false);
                        }
                    }
                    doneSignal.countDown();
                }
                TestDescription nextTest() {
                    synchronized (testIterLock) {
                        return testIter.hasNext() ? testIter.next() : null;
                    }
                }
            });
        }
        doneSignal.await();
        executorService.shutdownNow();
        return allPassed.get();
    }

}
