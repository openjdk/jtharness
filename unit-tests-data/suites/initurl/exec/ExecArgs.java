/*
 * $Id$
 *
 * Copyright (c) 1996, 2019, Oracle and/or its affiliates. All rights reserved.
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

// This class should check the arguments it is given

import java.io.PrintWriter;
import java.io.File;
import java.util.Hashtable;

import javasoft.sqe.javatest.Status;
import javasoft.sqe.javatest.Test;

public class ExecArgs implements Test {
    public static void main(String[] args) {
        Test t = new ExecArgs();
        PrintWriter err = new PrintWriter(System.err);
        PrintWriter out = new PrintWriter(System.out);
        Status s = t.run(args, err, out);
        err.flush();
        out.flush();
        s.exit();
    }

    public Status run(String[] args, PrintWriter log, PrintWriter out) {
        // args should be
        // 0: testSuiteRootURL
        // 1: testURL
        // 2: testWorkDir
        // 3: testClassDir
        // 4: harnessClassDir

        if (args.length != 5)
            return Status.failed("wrong number of args");

        out.println("testSuiteRootURL: " + args[0]);
        if (!args[0].endsWith("/basic/testsuite.html"))
            return Status.failed("bad value for testSuiteRootURL: " + args[0]);

        out.println("testURL: " + args[1]);
        if (!args[1].endsWith("/basic/exec/index.html#ExecArgs"))
            return Status.failed("bad value for testURL: " + args[1]);

        out.println("testWorkDir: " + args[2]);
        String workTail = File.separatorChar + "exec";
        if (!args[2].endsWith(workTail))
            return Status.failed("bad value for testWorkDir: " + args[2]);

        out.println("testClassDir: " + args[3]);
        out.println("harnessClassDir: " + args[4]);
        //hmm .. can't say much about testClassDir and harnessClassDir
        // security could get in the way

        return Status.passed("");
    }
}
