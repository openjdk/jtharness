/*
 * $Id$
 *
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.demots.tests.TestCasesTests;

import java.io.PrintWriter;

import com.sun.javatest.Status;
import com.sun.javatest.lib.MultiTest;

import com.sun.demoapi.BigNum;

/**
 * A test for com.sun.demoapi.BigNum.add.
 *
 * @test
 * @sources ManyTest.java
 * @executeClass com.sun.demots.tests.TestCasesTests.ManyTest
 */
public class ManyTest extends MultiTest 
{
    /**
     * Standard command-line entry point.
     * @param args command line args (ignored)
     */
    public static void main(String[] args) {
	PrintWriter err = new PrintWriter(System.err, true);
	ManyTest t = new ManyTest();
	Status s = t.run(args, null, err);
	s.exit();
    }

    public Status PassingTest01() {
        System.err.println("PassingTest01: Passed.");
	return Status.passed("OK");
    }

    public Status PassingTest02() {
        System.err.println("PassingTest02: Passed.");
	return Status.passed("OK");
    }

    public Status PassingTest03() {
        System.err.println("PassingTest03: Passed.");
	return Status.passed("OK");
    }

    public Status PassingTest04() {
        System.err.println("PassingTest04: Passed.");
	return Status.passed("OK");
    }

    public Status FailingTest01() {
        System.err.println("FailingTest01: Failed.");
       return Status.failed("this testcase is always failing");
    }

    public Status FailingTest02() {
        System.err.println("FailingTest02: Failed.");
       return Status.failed("this testcase is always failing");
    }

    public Status ErrorTest01() {
        System.err.println("ErrorTest01: Error.");
       return Status.error("this testcase is always error");
    }

    public Status ErrorTest02() {
        System.err.println("ErrorTest02: Error.");
       return Status.error("this testcase is always error");
    }

    boolean test(String s1, String s2, String expectedResult) {
	BigNum bn1 = new BigNum(s1);
	BigNum bn2 = new BigNum(s2);
	BigNum sum = bn1.add(bn2);
	if (sum.toString().equals(expectedResult))
	    return true;
	else {
	    err.println("s1: " + s1 
			+ " s2: " + s2 
			+ " expected result: " + expectedResult
			+ " actual result: " + sum);
	    return false;
	}
    }

    /**
     * A stream to which to write info about test failures.
     */
    private PrintWriter err;
}


