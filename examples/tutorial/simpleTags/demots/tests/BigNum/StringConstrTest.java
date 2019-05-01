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
 * A test for com.sun.demoapi.BigNum(String).
 *
 * @test
 * @sources StringConstrTest.java
 * @executeClass com.sun.demots.tests.bignum.StringConstrTest
 */
public class StringConstrTest implements Test
{
    /**
     * Standard command-line entry point.
     * @param args command line args (ignored)
     */
    public static void main(String[] args) {
        PrintWriter err = new PrintWriter(System.err, true);
        Test t = new StringConstrTest();
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

        // check zero
        ok = ok & posTest("0");

        // check positive numbers
        ok = ok & posTest("123");
        ok = ok & posTest("123456789");
        ok = ok & posTest("1234567890123456789");

        // check negative numbers
        ok = ok & posTest("-123");
        ok = ok & posTest("-123456789");
        ok = ok & posTest("-1234567890123456789");

        // check leading zeroes ignored
        ok = ok & posTest("-0", "0");
        ok = ok & posTest("000123456789", "123456789");

        // check invalid numbers detected
        ok = ok & negTest(null,   NullPointerException.class);
        ok = ok & negTest("",     NumberFormatException.class);
        ok = ok & negTest("-",    NumberFormatException.class);
        ok = ok & negTest("a",    NumberFormatException.class);
        ok = ok & negTest("-a",   NumberFormatException.class);
        ok = ok & negTest("123a", NumberFormatException.class);
        ok = ok & negTest("12.3", NumberFormatException.class);

        if (ok)
            return Status.passed("OK");
        else
            return Status.failed("one or more test cases failed");
    }

    /**
     * Test the BigNum(String) constructor. A BigNum is created
     * with the specified argument; the creation is then verified by
     * converting it to a string and comparing it against the
     * original argument. If the test fails, a message is written
     * to the log.
     * @param s the argument for the BigNum constructor
     * @return true if the test succeeded, and false otherwise.
     */
    boolean posTest(String s) {
        return posTest(s, s);
    }


    /**
     * Test the BigNum(String) constructor. A BigNum is created
     * with a specified argument; the creation is then verified by
     * converting it to a string and comparing it a reference value.
     * If the test fails, a message is written to the log.
     * @param s1 the argument for the BigNum constructor
     * @param s2 the reference value, giving the expected string
     * representation of the BigNum that is created
     * @return true if the test succeeded, and false otherwise.
     */
    boolean posTest(String s1, String s2) {
        BigNum bn = new BigNum(s1);
        String bs = bn.toString();
        if (bs != null && bs.equals(s2))
            return true;
        else {
            err.println("arg: " + s1 + ", expected: " + s2 + ", found: " + bs);
            return false;
        }
    }

    /**
     * Test the BigNum(String) constructor. A BigNum is created
     * with a specified argument; the creation is expected to fail
     * with a specified exception. The test passes if the correct
     * exception is thrown.
     * If the test fails, a message is written to the log.
     * @param s the argument for the BigNum constructor
     * @param expectedThrowableClass the class of the exception that
     * is expected to be thrown
     * @return true if the expected exception is thrown when calling
     * the BigNum(String) constructor, and false otherwise.
     */
    boolean negTest(String s, Class<?> expectedThrowableClass) {
        try {
            BigNum bn = new BigNum(s);
            err.println("arg: " + s + ", expected: "
                        + expectedThrowableClass.getName()
                        + ", no exception thrown");
            return false;
        }
        catch (Throwable t) {
            if (expectedThrowableClass.isInstance(t))
                return true;
            else {
                err.println("arg: " + s + ", expected: " +
                            expectedThrowableClass.getName() +
                            ", received " + t.getClass().getName());
                return false;
            }
        }
    }

    /**
     * A stream to which to write info about test failures.
     */
    private PrintWriter err;
}
