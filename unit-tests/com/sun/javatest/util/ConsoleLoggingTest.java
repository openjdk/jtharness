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
package com.sun.javatest.util;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConsoleLoggingTest extends ConsoleLoggingTestBase {


    @Test
    public void levelALL() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.ALL);
        Log.fine("fine message");
        Log.finer("finer message");
        Log.finest("finest message");
        Log.info("info message 345");
        Log.warning("warning message 345");
        Log.error("error message 890");
        Assert.assertEquals(6, savedSystemErr.size());
        checkSystemErrLineEndsWith(0, "[FINE] (ConsoleLoggingTest.levelALL) fine message");
        checkSystemErrLineEndsWith(1, "[FINER] (ConsoleLoggingTest.levelALL) finer message");
        checkSystemErrLineEndsWith(2, "[FINEST] (ConsoleLoggingTest.levelALL) finest message");
        checkSystemErrLineEndsWith(3, "[INFO] (ConsoleLoggingTest.levelALL) info message 345");
        checkSystemErrLineEndsWith(4, "[WARNING] (ConsoleLoggingTest.levelALL) warning message 345");
        checkSystemErrLineEndsWith(5, "[SEVERE] (ConsoleLoggingTest.levelALL) error message 890");
    }

    @Test
    public void levelOFF() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.OFF);
        Log.fine("fine message");
        Log.finer("finer message");
        Log.finest("finest message");
        Log.info("info message 345");
        Log.warning("warning message 345");
        Log.warning("warning message 345697");
        Log.error("error message 890");
        Log.error("error message 893456");
        Assert.assertTrue(savedSystemErr.isEmpty());
    }

    @Test
    public void levelFINE() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.FINE);
        Log.fine("fine message 23");
        Log.finer("finer message 233");
        Log.finest("finest message 662");
        Log.info("info message 345");
        Log.warning("warning message 345");
        Log.error("error message 890");
        Assert.assertEquals(4, savedSystemErr.size());
        checkSystemErrLineEndsWith(0, "[FINE] (ConsoleLoggingTest.levelFINE) fine message 23");
        checkSystemErrLineEndsWith(1, "[INFO] (ConsoleLoggingTest.levelFINE) info message 345");
        checkSystemErrLineEndsWith(2, "[WARNING] (ConsoleLoggingTest.levelFINE) warning message 345");
        checkSystemErrLineEndsWith(3, "[SEVERE] (ConsoleLoggingTest.levelFINE) error message 890");
    }

    @Test
    public void levelFINER() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.FINER);
        Log.fine("fine message 23");
        Log.finer("finer message 233");
        Log.finest("finest message 662");
        Log.info("info message 345");
        Log.warning("warning message 345");
        Log.error("error message 890");
        Assert.assertEquals(5, savedSystemErr.size());
        checkSystemErrLineEndsWith(0, "[FINE] (ConsoleLoggingTest.levelFINER) fine message 23");
        checkSystemErrLineEndsWith(1, "[FINER] (ConsoleLoggingTest.levelFINER) finer message 233");
        checkSystemErrLineEndsWith(2, "[INFO] (ConsoleLoggingTest.levelFINER) info message 345");
        checkSystemErrLineEndsWith(3, "[WARNING] (ConsoleLoggingTest.levelFINER) warning message 345");
        checkSystemErrLineEndsWith(4, "[SEVERE] (ConsoleLoggingTest.levelFINER) error message 890");
    }

    @Test
    public void levelFINEST() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.FINEST);
        Log.fine("fine message");
        Log.finer("finer message");
        Log.finest("finest message");
        Log.info("info message 345");
        Log.warning("warning message 345");
        Log.error("error message 890");
        Assert.assertEquals(6, savedSystemErr.size());
        checkSystemErrLineEndsWith(0, "[FINE] (ConsoleLoggingTest.levelFINEST) fine message");
        checkSystemErrLineEndsWith(1, "[FINER] (ConsoleLoggingTest.levelFINEST) finer message");
        checkSystemErrLineEndsWith(2, "[FINEST] (ConsoleLoggingTest.levelFINEST) finest message");
        checkSystemErrLineEndsWith(3, "[INFO] (ConsoleLoggingTest.levelFINEST) info message 345");
        checkSystemErrLineEndsWith(4, "[WARNING] (ConsoleLoggingTest.levelFINEST) warning message 345");
        checkSystemErrLineEndsWith(5, "[SEVERE] (ConsoleLoggingTest.levelFINEST) error message 890");
    }

    @Test
    public void levelINFO() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.INFO);
        Log.info("info message 345");
        Log.warning("warning message 345");
        Log.fine("fine message");
        Log.finer("finer message");
        Log.finest("finest message");
        Log.error("error message 890");
        Assert.assertEquals(3, savedSystemErr.size());
        checkSystemErrLineEndsWith(0, "[INFO] (ConsoleLoggingTest.levelINFO) info message 345");
        checkSystemErrLineEndsWith(1, "[WARNING] (ConsoleLoggingTest.levelINFO) warning message 345");
        checkSystemErrLineEndsWith(2, "[SEVERE] (ConsoleLoggingTest.levelINFO) error message 890");
    }

    @Test
    public void levelWARNING() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.WARNING);
        Log.info("info message 345");
        Log.warning("warning message 3455");
        Log.fine("fine message");
        Log.finer("finer message");
        Log.finest("finest message");
        Log.finest("finest message 987");
        Log.error("error message 8902");
        Assert.assertEquals(2, savedSystemErr.size());
        checkSystemErrLineEndsWith(0, "[WARNING] (ConsoleLoggingTest.levelWARNING) warning message 3455");
        checkSystemErrLineEndsWith(1, "[SEVERE] (ConsoleLoggingTest.levelWARNING) error message 8902");
    }


    @Test
    public void levelSEVERE() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.SEVERE);
        Log.info("info message 345");
        Log.warning("warning message 3455");
        Log.fine("fine message");
        Log.finer("finer message");
        Log.finest("finest message");
        Log.error("error message 356346");
        Assert.assertEquals(1, savedSystemErr.size());
        checkSystemErrLineEndsWith(0, "[SEVERE] (ConsoleLoggingTest.levelSEVERE) error message 356346");
    }

    @Test
    public void levelSEVERE_2() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.SEVERE);
        Log.info("info message 345");
        Log.warning("warning message 3455");
        Log.fine("fine message");
        Log.finer("finer message");
        Log.finest("finest message");
        Log.error("error message 356346");
        Log.error("error 2364 message 0398457");
        Assert.assertEquals(2, savedSystemErr.size());
        checkSystemErrLineEndsWith(0, "[SEVERE] (ConsoleLoggingTest.levelSEVERE_2) error message 356346");
        checkSystemErrLineEndsWith(1, "[SEVERE] (ConsoleLoggingTest.levelSEVERE_2) error 2364 message 0398457");
    }


    @Test
    public void levelInfo_threeFineMessages() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.INFO);
        Log.fine("fine message");
        Log.finer("finer message");
        Log.finest("finest message");
        Assert.assertTrue(savedSystemErr.isEmpty());
    }

    @Test
    public void levelInfo_threeFineAndThreeWarnings() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.INFO);
        Log.fine("fine message");
        Log.finer("finer message");
        Log.finest("finest message");
        Log.warning("first warning!");
        Log.warning("second warning!");
        Log.warning("final warning!");
        Assert.assertEquals(3, savedSystemErr.size());
        checkSystemErrLineEndsWith(0, "[WARNING] (ConsoleLoggingTest.levelInfo_threeFineAndThreeWarnings) first warning!");
        checkSystemErrLineEndsWith(1, "[WARNING] (ConsoleLoggingTest.levelInfo_threeFineAndThreeWarnings) second warning!");
        checkSystemErrLineEndsWith(2, "[WARNING] (ConsoleLoggingTest.levelInfo_threeFineAndThreeWarnings) final warning!");
    }

    @Test
    public void levelInfo_threeFineMessages_andAnError() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.INFO);
        Log.fine("fine message");
        Log.finer("finer message");
        Log.finest("finest message");
        Log.error("error message");
        Assert.assertEquals(1, savedSystemErr.size());
        checkSystemErrLineEndsWith(0, "[SEVERE] (ConsoleLoggingTest.levelInfo_threeFineMessages_andAnError) error message");
    }

    @Test
    public void fineON() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.FINE);
        Log.fine("fine message");
        checkSystemErrLineEndsWith(0, "fine message");
    }

    @Test
    public void info() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.INFO);
        String message = "info message 1234";
        Log.info(message);
        checkSystemErrLineEndsWith(0, message);
    }

    @Test
    public void error() {
        savedSystemErr.clear();
        Log.resetLoggingSettings(Level.INFO);
        Log.error("error message 1234");
        checkSystemErrLineContains(0, "(ConsoleLoggingTest.error) error message 1234");
    }

}

