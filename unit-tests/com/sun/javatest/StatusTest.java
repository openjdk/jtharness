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
package com.sun.javatest;

import org.junit.Assert;
import org.junit.Test;

import java.io.PrintStream;

public class StatusTest {

    @Test
    public void test() {
        StatusTest st = new StatusTest();
        boolean ok = st.run(System.out);
        Assert.assertTrue(ok);
    }

    public boolean run(PrintStream out) {
        boolean ok = true;

        String[] data = {
                "",
                " ",
                "  ",
                "   ",
                "hello",
                " hello",
                "  hello",
                "   hello",
                "hello ",
                " hello ",
                "  hello ",
                "   hello ",
                "hello  ",
                " hello  ",
                "  hello  ",
                "   hello  ",
                "hello there",
                " hello there",
                "  hello there",
                "   hello there",
                "hello there ",
                " hello there ",
                "  hello there ",
                "   hello there ",
                "hello there  ",
                " hello there  ",
                "  hello there  ",
                "   hello there  "
        };

        for (String aData : data) {
            ok &= check(Status.passed(aData), Status.PASSED, aData);
            ok &= check(Status.failed(aData), Status.FAILED, aData);
            ok &= check(Status.error(aData), Status.ERROR, aData);
            ok &= check(new Status(Status.NOT_RUN, aData), Status.NOT_RUN, aData);
            ok &= check("Passed. " + aData, Status.PASSED, aData.trim());
            ok &= check("Failed. " + aData, Status.FAILED, aData.trim());
            ok &= check("Error. " + aData, Status.ERROR, aData.trim());
            ok &= check("Not run. " + aData, Status.NOT_RUN, aData.trim());
        }

        return ok;
    }

    private boolean check(Status s, int t, String r) {
        Status s2 = Status.parse(s.toString());
        if (s2.getType() != t) {
            System.err.println("type mismatch for " + s);
            System.err.println("expected " + t);
            System.err.println("found    " + s.getType());
            return false;
        }

        if (!s2.getReason().equals(r.trim())) {
            System.err.println("reason mismatch for " + s);
            System.err.println("expected `" + r.trim() + "'");
            System.err.println("found    `" + s.getReason() + "'");
            return false;
        }

        //System.err.println("trim OK: `" + s + "'");
        return true;
    }

    private boolean check(String stringToParse, int t, String expected) {
        Status s2 = Status.parse(stringToParse);
        if (s2.getType() != t) {
            System.err.println("type mismatch for " + stringToParse);
            System.err.println("expected " + t);
            return false;
        }

        if (!s2.getReason().equals(expected)) {
            System.err.println("reason mismatch for " + stringToParse);
            System.err.println("expected \"" + expected + "\"");
            System.err.println("actual \"" + s2.getReason() + "\"");
            return false;
        }

        return true;
    }
}
