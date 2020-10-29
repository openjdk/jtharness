/*
 * $Id$
 *
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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


import org.junit.Assert;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;

/**
 * Checking function that produces strings with human readable duration, i.e. "2 hours 31 minutes 1 second"
 */
public class RunTestsCommand_formattedDuration {

    @Test
    public void zero() {
        Assert.assertEquals("0 seconds", RunTestsCommand.formattedDuration(0));
        Assert.assertEquals("0 seconds", RunTestsCommand.formattedDuration(-1));
        Assert.assertEquals("0 seconds", RunTestsCommand.formattedDuration(-400));
        Assert.assertEquals("0 seconds", RunTestsCommand.formattedDuration(Integer.MIN_VALUE));
        Assert.assertEquals("0 seconds", RunTestsCommand.formattedDuration(Long.MIN_VALUE));
    }

    @Test
    public void maxValues() {
        Assert.assertEquals("24855 days 3 hours 14 minutes 7 seconds",
                RunTestsCommand.formattedDuration(Integer.MAX_VALUE));
        Assert.assertEquals(
                24855 * 60 * 60 * 24 + 60 * 60 * 3 + 60 * 14 + 7,
                Integer.MAX_VALUE
        );

        Assert.assertEquals("106751991167300 days 15 hours 30 minutes 7 seconds",
                RunTestsCommand.formattedDuration(Long.MAX_VALUE));
        Assert.assertEquals(
                106751991167300L * 60L * 60L * 24L + 60L * 60L * 15L + 60L * 30L + 7L,
                Long.MAX_VALUE
        );
    }

    @Test
    public void seconds() {
        Assert.assertEquals("1 second", RunTestsCommand.formattedDuration(1));
        Assert.assertEquals("2 seconds", RunTestsCommand.formattedDuration(2));
        Assert.assertEquals("3 seconds", RunTestsCommand.formattedDuration(3));
        Assert.assertEquals("30 seconds", RunTestsCommand.formattedDuration(30));
        Assert.assertEquals("40 seconds", RunTestsCommand.formattedDuration(40));
        Assert.assertEquals("59 seconds", RunTestsCommand.formattedDuration(59));
    }

    @Test
    public void minutes() {
        Assert.assertEquals("1 minute", RunTestsCommand.formattedDuration(60));
        Assert.assertEquals("2 minutes", RunTestsCommand.formattedDuration(120));
        Assert.assertEquals("3 minutes", RunTestsCommand.formattedDuration(180));
        Assert.assertEquals("10 minutes", RunTestsCommand.formattedDuration(600));
        Assert.assertEquals("59 minutes", RunTestsCommand.formattedDuration(60 * 59));
        Assert.assertEquals("11 minutes", RunTestsCommand.formattedDuration(660));
    }

    @Test
    public void hours() {
        Assert.assertEquals("1 hour", RunTestsCommand.formattedDuration(60 * 60));
        Assert.assertEquals("7 hours", RunTestsCommand.formattedDuration(60 * 60 * 7));
        Assert.assertEquals("23 hours", RunTestsCommand.formattedDuration(60 * 60 * 23));
    }

    @Test
    public void days() {
        Assert.assertEquals("1 day", RunTestsCommand.formattedDuration(60 * 60 * 24));
        Assert.assertEquals("2 days", RunTestsCommand.formattedDuration(60 * 60 * 48));
        Assert.assertEquals("7 days", RunTestsCommand.formattedDuration(60 * 60 * 24 * 7));
        Assert.assertEquals("100 days", RunTestsCommand.formattedDuration(60 * 60 * 24 * 100));
    }

    @Test
    public void days_hours() {
        Assert.assertEquals("1 day 1 hour", RunTestsCommand.formattedDuration(60 * 60 * 25));
        Assert.assertEquals("2 days 3 hours", RunTestsCommand.formattedDuration(60 * 60 * 51));
        Assert.assertEquals("2 days 23 hours", RunTestsCommand.formattedDuration(60 * 60 * 71));
    }

    @Test
    public void days_hours_minutes() {
        Assert.assertEquals("1 day 1 hour 1 minute", RunTestsCommand.formattedDuration(60 * 60 * 25 + 60));
        Assert.assertEquals("1 day 5 hours 3 minutes", RunTestsCommand.formattedDuration(60 * 60 * 29 + 180));
    }

    @Test
    public void days_seconds() {
        Assert.assertEquals("1 day 59 seconds", RunTestsCommand.formattedDuration(60 * 60 * 24 + 59));
        Assert.assertEquals("1 day 1 second", RunTestsCommand.formattedDuration(60 * 60 * 24 + 1));
        Assert.assertEquals("1 day 2 seconds", RunTestsCommand.formattedDuration(60 * 60 * 24 + 2));
        Assert.assertEquals("3 days 1 second", RunTestsCommand.formattedDuration(60 * 60 * 72 + 1));
    }

    @Test
    public void days_minutes() {
        Assert.assertEquals("1 day 2 minutes", RunTestsCommand.formattedDuration(60 * 60 * 24 + 120));
        Assert.assertEquals("1 day 10 minutes", RunTestsCommand.formattedDuration(60 * 60 * 24 + 600));
    }

    @Test
    public void days_hours_minutes_seconds() {
        Assert.assertEquals("1 day 1 hour 1 minute 4 seconds", RunTestsCommand.formattedDuration(60 * 60 * 25 + 64));
        Assert.assertEquals("1 day 1 hour 2 minutes 7 seconds", RunTestsCommand.formattedDuration(60 * 60 * 25 + 127));
        Assert.assertEquals("2 days 1 hour 2 minutes 7 seconds", RunTestsCommand.formattedDuration(60 * 60 * 49 + 127));
    }

    @Test
    public void days_minutes_seconds() {
        Assert.assertEquals("1 day 8 minutes 15 seconds", RunTestsCommand.formattedDuration(60 * 60 * 24 + 60*8 + 15));
        Assert.assertEquals("2 days 1 minute 4 seconds", RunTestsCommand.formattedDuration(60 * 60 * 48 + 64));
    }

    @Test
    public void days_hours_seconds() {
        Assert.assertEquals("1 day 1 hour 4 seconds", RunTestsCommand.formattedDuration(60 * 60 * 25 + 4));
        Assert.assertEquals("1 day 23 hours 59 seconds", RunTestsCommand.formattedDuration(60 * 60 * 47 + 59));
    }

    @Test
    public void minutes_seconds() {
        Assert.assertEquals("1 minute 1 second", RunTestsCommand.formattedDuration(61));
        Assert.assertEquals("1 minute 59 seconds", RunTestsCommand.formattedDuration(119));
        Assert.assertEquals("2 minutes 1 second", RunTestsCommand.formattedDuration(121));
        Assert.assertEquals("2 minutes 3 seconds", RunTestsCommand.formattedDuration(123));
        Assert.assertEquals("2 minutes 6 seconds", RunTestsCommand.formattedDuration(126));
        Assert.assertEquals("3 minutes 11 seconds", RunTestsCommand.formattedDuration(191));
        Assert.assertEquals("10 minutes 1 second", RunTestsCommand.formattedDuration(601));
        Assert.assertEquals("10 minutes 5 seconds", RunTestsCommand.formattedDuration(605));
        Assert.assertEquals("10 minutes 59 seconds", RunTestsCommand.formattedDuration(659));
    }

    @Test
    public void hours_minutes() {
        Assert.assertEquals("1 hour 40 minutes", RunTestsCommand.formattedDuration(6000));
        Assert.assertEquals("1 hour 41 minutes", RunTestsCommand.formattedDuration(6060));
        Assert.assertEquals("1 hour 59 minutes", RunTestsCommand.formattedDuration(60 * 60 + 60 * 59));
    }

    @Test
    public void hours_seconds() {
        Assert.assertEquals("19 hours 15 seconds", RunTestsCommand.formattedDuration(60 * 60 * 19 + 15));
    }

    @Test
    public void hours_minutes_seconds() {
        Assert.assertEquals("1 hour 41 minutes 7 seconds", RunTestsCommand.formattedDuration(6067));
        Assert.assertEquals("5 hours 45 minutes 45 seconds", RunTestsCommand.formattedDuration(45 + 60*45 + 60*60*5));
    }


}
