/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.demots.tests.bignum;

import java.io.PrintWriter;

import com.sun.javatest.Status;
import com.sun.javatest.Test;

import com.sun.demoapi.BigNum;

/**
 * A test for com.sun.demoapi.BigNum(long).
 *
 * @test
 * @sources LongConstrTest.java
 * @executeClass com.sun.demots.tests.bignum.LongConstrTest
 */
public class LongConstrTest implements Test
{
    /**
     * Standard command-line entry point.
     * @param args command line args (ignored)
     */
    public static void main(String[] args) {
        PrintWriter err = new PrintWriter(System.err, true);
        Test t = new LongConstrTest();
        Status s = t.run(args, null, err);
        s.exit();
    }

    /**
     * Main test method. The test consists of a series of test cases;
     * the test passes only if all the individual test cases pass.
     * @param args ignored
     * @param out  ignored
     * @param err  a stream to which to write details about test failures
     * @return a Status object indicating if the test passed or failed
     */
    public Status run(String[] args, PrintWriter out, PrintWriter err) {
        // save error stream to which to write error messages
        this.err = err;

        boolean ok = true;

        // create some BigNums, convert them to strings and verify the results
        ok = ok & test(0);
        ok = ok & test(123);
        ok = ok & test(123456);
        ok = ok & test(123456789);
        ok = ok & test(123456789123456789L);

        ok = ok & test(-123);
        ok = ok & test(-123456);
        ok = ok & test(-123456789);
        ok = ok & test(-123456789123456789L);

        return (ok ? Status.passed("OK") : Status.failed("some test cases failed"));
    }

    /**
     * Test the BigNum(long) constructor. A BigNum is created with
     * the specified argument; the creation is then verified by
     * converting it to a string and comparing it against the string
     * representation of the original argument. If the test fails,
     * a message is written to the log.
     * @param n the argument for the BigNum constructor
     * @return true if the test succeeded, and false otherwise.
     */
    private boolean test(long n) {
        BigNum bn = new BigNum(n);
        String bs = bn.toString();
        if (bs.equals(String.valueOf(n)))
            return true;
        else {
            err.println("test failed: expected: " + n + "; found: " + bs);
            return false;
        }
    }

    /**
     * A stream to which to write info about test failures.
     */
    private PrintWriter err;
}
