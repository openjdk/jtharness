/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.io.PrintWriter;

/**
 * This interface is implemented by tests to be run by standard scripts.
 * Information about the test is normally contained in a
 * {@link TestDescription test description}.
 *
 * A test should also define `main' as follows:
 * <pre>
 * <code>
 *      public static void main(String[] args) {
 *          Test t = new <em>test-class-name</em>();
 *          Status s = t.run(args, new PrintWriter(System.err), new PrintWriter(System.out));
 *          s.exit();
 *      }
 * </code>
 * </pre>
 * Defining `main' like this means that the test can also be run standalone,
 * independent of the harness.
 */
public interface Test
{
    /**
     * Runs the test embodied by the implementation.
     * @param args      These are supplied by the {@link Script script} running the test,
     *                  typically derived from values in the
     *                  {@link TestDescription test description}.
     *                  and allow a script to provide configuration information to a test,
     *                  or to reuse a test with different test values.
     * @param out1      A stream to which to report errors. This stream was previously called "err".
     * @param out2      An additional stream to which to report messages. This stream was previously called "out".
     * @return          A {@link Status} object representing the outcome of the test.
     */
    public Status run(String[] args, PrintWriter out1, PrintWriter out2);

}
