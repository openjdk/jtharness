/*
 * $Id$
 *
 * Copyright (c) 1996, 2021, Oracle and/or its affiliates. All rights reserved.
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
import com.sun.tck.lib.tgf.TGFUtils;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Random;

import static org.mockito.Mockito.inOrder;


/**
 * Test utilities
 */
public class TU {

    public static final String[] EMPTY_ARGV = new String[]{};

    public static final String EXCLUDE_WORD = TGFUtils.EXCLUDE;

    public static PrintWriter createMockedPrintWriter() {
        return Mockito.mock(PrintWriter.class);
    }
    public static String generateRandomString() {
        return "random" + new Random().nextInt();
    }

    /**
     * Processors which are bound to @TestGroup annotation
     */
    public static final Set<Class<? extends Processor.TestGroupProcessor>> TG_PROC = Set.of(
            TestCaseSelecting.class,
            TestCaseExcluding.class,
            RunningTestCases.class,
            PublicNonTestCaseMethodsReporter.class,
            NonPublicTestCasesReporter.class);

    /**
     * Processors which are bound to @TestCase annotation
     */
    public static final Set<Class<? extends Processor.TestCaseProcessor>> TC_PROC_STATUS_RECOGNIZED = Set.of(
            DefaultThreadRunning.class,
            DefaultExecutionResult.class,
            DefaultNoArgTestCaseMethodSetting.class,
            TestCaseResultCanBeStatus.class
    );

    /**
     * Processors which are bound to @TestCase annotation
     */
    public static final Set<Class<? extends Processor.TestCaseProcessor>> TC_PROC_STATUS_NOT_EXPECTED = Set.of(
            DefaultThreadRunning.class,
            DefaultExecutionResult.class,
            DefaultNoArgTestCaseMethodSetting.class
    );


    public static TestResult runTestGroup(Object tg, String... args) {
        PrintWriter log = new PrintWriter(System.err, true, StandardCharsets.UTF_8);
        PrintWriter ref = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
        return runTestGroup(tg, log, ref, args);
    }

    public static TestResult runTestGroup(Object tg) {
        PrintWriter log = new PrintWriter(System.err, true, StandardCharsets.UTF_8);
        PrintWriter ref = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
        return runTestGroup(tg, log, ref, EMPTY_ARGV);
    }

    public static TestResult runTestGroup(Object tg,
                                          PrintWriter log, PrintWriter ref,
                                          Set<Class<? extends Processor.TestGroupProcessor>> additionalUserTGProcessors,
                                          Set<Class<? extends Processor.TestCaseProcessor>>  additionalUserTCProcessors,
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
        return runTestGroup(test, log, ref, Set.of(), Set.of(), args);
    }

    public static void methodThatCallsAssertFail_1() {
        methodThatCallsAssertFail_2();
    }
    public static void methodThatCallsAssertFail_2() {
        methodThatCallsAssertFail_3();
    }
    public static void methodThatCallsAssertFail_3() {
        Assert.fail("Assertion failed");
    }
    public static void throwsAssertionFailedException() {
        throw new AssertionFailedException("Failed!");
    }


    @FunctionalInterface
    interface PrintVerifier {
        void verify(PrintWriter log);
    }

    static void verify_(PrintWriter log, PrintVerifier pw) {
        InOrder inOrder = inOrder(log);
        pw.verify(inOrder.verify(log));
        inOrder.verifyNoMoreInteractions();
    }

}
