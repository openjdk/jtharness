/*
 * $Id$
 *
 * Copyright (c) 2001, 2021, Oracle and/or its affiliates. All rights reserved.
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
 * A test for com.sun.demoapi.BigNum.equals.
 *
 * @test
 * @sources EqualsTest.java
 * @executeClass com.sun.demots.tests.bignum.EqualsTest
 */
public class EqualsTest implements Test {
    /**
     * A stream to which to write info about test failures.
     */
    private PrintWriter err;

    /**
     * Standard command-line entry point.
     *
     * @param args command line args (ignored)
     */
    public static void main(String[] args) {
        PrintWriter err = new PrintWriter(System.err, true);
        Test t = new EqualsTest();
        Status s = t.run(args, null, err);
        s.exit();
    }

    /**
     * Main test method. The test consists of a series of test cases;
     * the test passes only if all the individual test cases pass.
     *
     * @param args ignored
     * @param out  ignored
     * @param err  a stream to which to write details about test failures
     * @return a Status object indicating if the test passed or failed
     */
    public Status run(String[] args, PrintWriter out, PrintWriter err) {
        // save error stream to which to write error messages
        this.err = err;

        boolean ok = true;

        ok = ok & test("-12345678901234567890", "-12345678901234567891");
        ok = ok & test("-12345678901234567890", "-12345678901234567890");
        ok = ok & test("-12345678901234567890", "-12345678901234567889");
        ok = ok & test("-12345678901234567890", "-1234567890");
        ok = ok & test("-12345678901234567890", "-123");
        ok = ok & test("-12345678901234567890", "0");
        ok = ok & test("-12345678901234567890", "123");
        ok = ok & test("-12345678901234567890", "1234567890");
        ok = ok & test("-12345678901234567890", "12345678901234567890");

        ok = ok & test("-1234567890", "-12345678901234567890");
        ok = ok & test("-1234567890", "-1234567891");
        ok = ok & test("-1234567890", "-1234567890");
        ok = ok & test("-1234567890", "-1234567889");
        ok = ok & test("-1234567890", "-123");
        ok = ok & test("-1234567890", "0");
        ok = ok & test("-1234567890", "123");
        ok = ok & test("-1234567890", "1234567890");
        ok = ok & test("-1234567890", "12345678901234567890");

        ok = ok & test("-123", "-12345678901234567890");
        ok = ok & test("-123", "-1234567890");
        ok = ok & test("-123", "-124");
        ok = ok & test("-123", "-123");
        ok = ok & test("-123", "-122");
        ok = ok & test("-123", "0");
        ok = ok & test("-123", "123");
        ok = ok & test("-123", "1234567890");
        ok = ok & test("-123", "12345678901234567890");

        ok = ok & test("0", "-12345678901234567890");
        ok = ok & test("0", "-1234567890");
        ok = ok & test("0", "-123");
        ok = ok & test("0", "-1");
        ok = ok & test("0", "0");
        ok = ok & test("0", "1");
        ok = ok & test("0", "123");
        ok = ok & test("0", "1234567890");
        ok = ok & test("0", "12345678901234567890");

        ok = ok & test("123", "-12345678901234567890");
        ok = ok & test("123", "-1234567890");
        ok = ok & test("123", "-123");
        ok = ok & test("123", "0");
        ok = ok & test("123", "122");
        ok = ok & test("123", "123");
        ok = ok & test("123", "124");
        ok = ok & test("123", "1234567890");
        ok = ok & test("123", "12345678901234567890");

        ok = ok & test("1234567890", "-12345678901234567890");
        ok = ok & test("1234567890", "-1234567890");
        ok = ok & test("1234567890", "-123");
        ok = ok & test("1234567890", "0");
        ok = ok & test("1234567890", "123");
        ok = ok & test("1234567890", "1234567889");
        ok = ok & test("1234567890", "1234567890");
        ok = ok & test("1234567890", "1234567891");
        ok = ok & test("1234567890", "12345678901234567890");

        ok = ok & test("12345678901234567890", "-12345678901234567890");
        ok = ok & test("12345678901234567890", "-1234567890");
        ok = ok & test("12345678901234567890", "-123");
        ok = ok & test("12345678901234567890", "0");
        ok = ok & test("12345678901234567890", "123");
        ok = ok & test("12345678901234567890", "1234567890");
        ok = ok & test("12345678901234567890", "12345678901234567889");
        ok = ok & test("12345678901234567890", "12345678901234567890");
        ok = ok & test("12345678901234567890", "12345678901234567891");

        if (ok)
            return Status.passed("OK");
        else
            return Status.failed("one or more test cases failed");
    }

    boolean test(String s1, String s2) {
        BigNum bn1 = new BigNum(s1);
        BigNum bn2 = new BigNum(s2);
        boolean actualResult = bn1.equals(bn2);
        boolean expectedResult = s1.equals(s2);
        if (actualResult == expectedResult)
            return true;
        else {
            err.println("s1: " + s1
                    + " s2: " + s2
                    + " expected result: " + expectedResult
                    + " actual result: " + actualResult);
            return false;
        }
    }
}


