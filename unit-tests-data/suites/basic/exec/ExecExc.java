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

// This class should throw an exception

import java.io.PrintWriter;

import javasoft.sqe.javatest.Status;
import javasoft.sqe.javatest.Test;

public class ExecExc implements Test {
    public static void main(String[] args) {
        Test t = new ExecExc();
        PrintWriter err = new PrintWriter(System.err);
        PrintWriter out = new PrintWriter(System.out);
        Status s = t.run(args, err, out);
        err.flush();
        out.flush();
        s.exit();
    }

    public Status run(String[] args, PrintWriter log, PrintWriter out) {
        if (args.length == 1) {
            String arg = args[0];
            if ("-runtime".equals(arg))
                throw new RuntimeException("example runtime exception");
            else if ("-error".equals(arg))
                throw new Error("example error");
        }
        // we don't really mean "passed" in the next line since the test is
        // supposed to demonstrate a test that fails with an unexpected exception.
        // So, if we are here, we want to signal an unexpected return and
        // passed would be unexpected for this test!
        return Status.passed("This test has unexpectedly not thrown an exception");
    }
}
