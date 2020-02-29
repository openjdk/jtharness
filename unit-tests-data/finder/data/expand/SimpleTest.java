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

/**
 * @test
 * @title This title should not be translated:  $field1 $field3
 * @expand field1 field2 field3
 * @source SimpleTest.java
 * @executeClass SimpleTest
 * @executeArgs $field1 ${field2.foo} ${field2.bar} ${field3}
 */

import java.io.PrintWriter;

import javasoft.sqe.javatest.Status;
import javasoft.sqe.javatest.Test;

public class SimpleTest implements Test {
    public static void main(String[] args) {
        Test t = new SimpleTest();
        PrintWriter err = new PrintWriter(System.err);
        PrintWriter out = new PrintWriter(System.out);
        Status s = t.run(args, err, out);
        err.flush();
        out.flush();
        s.exit();
    }

    public Status run(String[] args, PrintWriter err, PrintWriter out) {
        for (int i = 0; i < args.length; i++)
            out.println("arg[" + i + "] : " + args[i]);

        return Status.passed("I passed!");
    }
}
