/*
 * $Id$
 *
 * Copyright (c) 2001, 2024, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.KFL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import jthtest.tools.ReportChecker.KFLValues;

public class KFL11 extends KFL {

    public KFL11() throws FileNotFoundException, IOException {
         super(null, new KFLValues(5, 0, 0, 0, 2, 0, 3, 8, 0, 0, 3, 0, 5), TESTCASES_TEST_SUITE_NAME);
    }

    protected void init() throws Exception {
        FileWriter out = new FileWriter(DEFAULT_PATH + File.separator + "kfl.kfl");
        out.write("TestCasesTests/ErrorTest.java[ErrorTest01]\nTestCasesTests/ManyTest.java[ErrorTest01]\nTestCasesTests/ManyTest.java[ErrorTest02]");
        out.flush();
        out.close();
        this.kfl = "kfl.kfl";
        addUsedFile("kfl.kfl");
    }
}
