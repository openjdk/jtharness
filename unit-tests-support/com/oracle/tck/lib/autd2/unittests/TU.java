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

package com.oracle.tck.lib.autd2.unittests;

import com.oracle.tck.lib.autd2.TestResult;
import com.oracle.tck.lib.autd2.TestRunner;
import com.oracle.tck.lib.autd2.processors.Processor;
import com.oracle.tck.lib.autd2.processors.tc.DefaultExecutionResult;
import com.oracle.tck.lib.autd2.processors.tc.DefaultNoArgTestCaseMethodSetting;
import com.oracle.tck.lib.autd2.processors.tc.DefaultThreadRunning;
import com.oracle.tck.lib.autd2.processors.tc.TestCaseResultCanBeStatus;
import com.oracle.tck.lib.autd2.processors.tg.NonPublicTestCasesReporter;
import com.oracle.tck.lib.autd2.processors.tg.PublicNonTestCaseMethodsReporter;
import com.oracle.tck.lib.autd2.processors.tg.RunningTestCases;
import com.oracle.tck.lib.autd2.processors.tg.TestCaseExcluding;
import com.oracle.tck.lib.autd2.processors.tg.TestCaseSelecting;
import com.sun.tck.lib.Assert;
import com.sun.tck.lib.AssertionFailedException;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Random;


/**
 * Test utilities
 */
public class TU {

    public static final String[] EMPTY_ARGV = new String[]{};

    public static String generateRandomString() {
        return "random" + new Random().nextInt();
    }

    /**
     * Processors which are bound to @TestGroup annotation
     */
    public static final Set<Class<? extends Processor.TestGroupProcessor>> TG_PROC =
            new HashSet<Class<? extends Processor.TestGroupProcessor>>() {{
                addAll(Arrays.asList(
                        TestCaseSelecting.class,
                        TestCaseExcluding.class,
                        RunningTestCases.class,
                        PublicNonTestCaseMethodsReporter.class,
                        NonPublicTestCasesReporter.class));
            }};


    /**
     * Processors which are bound to @TestCase annotation
     */
    public static final Set<Class<? extends Processor.TestCaseProcessor>> TC_PROC_STATUS_RECOGNIZED =
            new HashSet<Class<? extends Processor.TestCaseProcessor>>() {{
                addAll(Arrays.asList(
                        DefaultThreadRunning.class,
                        DefaultExecutionResult.class,
                        DefaultNoArgTestCaseMethodSetting.class,
                        TestCaseResultCanBeStatus.class
                ));
            }};
    /**
     * Processors which are bound to @TestCase annotation
     */
    public static final Set<Class<? extends Processor.TestCaseProcessor>> TC_PROC_STATUS_NOT_EXPECTED =
            new HashSet<Class<? extends Processor.TestCaseProcessor>>() {{
                addAll(Arrays.asList(
                        DefaultThreadRunning.class,
                        DefaultExecutionResult.class,
                        DefaultNoArgTestCaseMethodSetting.class
                ));
            }};


    public static TestResult runTestGroup(Object tg, String... args) {
        PrintWriter log = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.err, StandardCharsets.UTF_8)), true);
        PrintWriter ref = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)), true);
        return runTestGroup(tg, log, ref, args);
    }

    public static TestResult runTestGroup(Object tg) {
        PrintWriter log = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.err, StandardCharsets.UTF_8)), true);
        PrintWriter ref = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)), true);
        return runTestGroup(tg, log, ref, EMPTY_ARGV);
    }

    public static TestResult runTestGroup(Object tg,
                                          PrintWriter log, PrintWriter ref,
                                          Set<Class<? extends Processor.TestGroupProcessor>> additionalUserTGProcessors,
                                          Set<Class<? extends Processor.TestCaseProcessor>> additionalUserTCProcessors,
                                          String... args) {
        Set<Class<? extends Processor.TestGroupProcessor>> finalTGProc = new HashSet<>(TG_PROC);
        Set<Class<? extends Processor.TestCaseProcessor>> finalTCProc = new HashSet<>(TC_PROC_STATUS_RECOGNIZED);
        finalTGProc.addAll(additionalUserTGProcessors);
        finalTCProc.addAll(additionalUserTCProcessors);
        return TestRunner.run(tg, log, ref, finalTGProc, finalTCProc, args);
    }

    public static TestResult runTestGroup(
            Object test,
            PrintWriter log,
            PrintWriter ref,
            String... args) {
        return runTestGroup(test, log, ref, new HashSet<>(), new HashSet<>(), args);
    }

}
