/*
 * $Id$
 *
 * Copyright (c) 2001, 2020, Oracle and/or its affiliates. All rights reserved.
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
import java.io.PrintWriter;

import javasoft.sqe.javatest.Status;
import javasoft.sqe.javatest.Test;


public class RerunTest implements Test {
    public static void main(String[] args) {
        RerunTest t = new RerunTest();
        Status s = t.run(args, null, null);
        s.exit();
    }

    public Status run(String[] args, PrintWriter log, PrintWriter ref) {
        int serial = 0;
        int pass = 0;
        int fail = 0;
        int error = 0;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-pass") && i + 1 < args.length)
                pass = Integer.parseInt(args[++i]);
            else if (args[i].equals("-fail") && i + 1 < args.length)
                fail = Integer.parseInt(args[++i]);
            else if (args[i].equals("-error") && i + 1 < args.length)
                error = Integer.parseInt(args[++i]);
            else
                serial = Integer.parseInt(args[i]);
        }

        if (pass != 0 && serial % pass == 0)
            return Status.passed("serial: " + serial + ", pass: " + pass);

        if (fail != 0 && serial % fail == 0)
            return Status.failed("serial: " + serial + ", fail: " + fail);

        if (error != 0 && serial % error == 0)
            return Status.error("serial: " + serial + ", error: " + error);

        return Status.passed("pass by default: serial: " + serial);

    }
}
