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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ConsoleLoggingTestBase {

    protected static final List<String> savedSystemErr = new ArrayList<>();

    @BeforeClass
    public static void init() {
        System.setErr(new PrintStream(System.err) {
            @Override
            public void write(byte[] buf, int off, int len) {
                super.write(buf, off, len);
                String printed = new String(buf, off, len);
                for (String s : printed.split("\n")) {
                    savedSystemErr.add(s);
                }
            }
        });

    }

    protected void checkSystemErrLineIs(int lineZeroBasedIndex, String expectedContent) {
        Assert.assertEquals(expectedContent, savedSystemErr.get(lineZeroBasedIndex));
    }

    protected void checkSystemErrLineStartsWith(int lineZeroBasedIndex, String expectedPrefix) {
        Assert.assertTrue(
                "\"" + savedSystemErr.get(lineZeroBasedIndex) + "\" is expected to start with \"" + expectedPrefix + "\"",
                savedSystemErr.get(lineZeroBasedIndex).startsWith(expectedPrefix));
    }

    protected void checkSystemErrLineContains(int lineZeroBasedIndex, String expectedInclusion) {
        Assert.assertTrue(
                "\"" + savedSystemErr.get(lineZeroBasedIndex) + "\" is expected to contain \"" + expectedInclusion + "\"",
                savedSystemErr.get(lineZeroBasedIndex).contains(expectedInclusion));
    }

    protected void checkSystemErrLineEndsWith(int lineZeroBasedIndex, String expectedEnding) {
        Assert.assertTrue(
                "\"" + savedSystemErr.get(lineZeroBasedIndex) + "\" is expected to ends with \"" + expectedEnding + "\"",
                savedSystemErr.get(lineZeroBasedIndex).endsWith(expectedEnding));
    }
}
