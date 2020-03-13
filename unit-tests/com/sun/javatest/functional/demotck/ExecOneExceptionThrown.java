/*
 * $Id$
 *
 * Copyright (c) 2001, 2019, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.functional.demotck;

import com.sun.javatest.functional.TestSuiteRunningTestBase;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ExecOneExceptionThrown extends TestSuiteRunningTestBase {

    @Test
    public void test() throws IOException {
        runJavaTest();
        checkJTRLine(8, "keywords=compile execute positive shouldexecute shouldfail", "exec", "index_ExecFailRuntimExc.jtr");
        checkJTRLine(28, "execStatus=Failed. exit code 1", "exec", "index_ExecFailRuntimExc.jtr");
        checkJTRLine(42, "----------messages:(4/228)----------", "exec", "index_ExecFailRuntimExc.jtr");
        checkJTRLine(51, "----------out1:(0/0)----------", "exec", "index_ExecFailRuntimExc.jtr");
        checkJTRLine(52, "----------out2:(0/0)----------", "exec", "index_ExecFailRuntimExc.jtr");
        checkJTRLine(53, "result: Passed. exit code 0", "exec", "index_ExecFailRuntimExc.jtr");
        checkJTRLine(58, "----------out1:(10/393)----------", "exec", "index_ExecFailRuntimExc.jtr");
        checkJTRLine(59, "System err printing 934588596674637", "exec", "index_ExecFailRuntimExc.jtr");
        checkJTRLine(60, "Exception in thread \"main\" java.lang.RuntimeException: example runtime exception", "exec", "index_ExecFailRuntimExc.jtr");
        checkJTRLine(69, "----------out2:(1/36)----------", "exec", "index_ExecFailRuntimExc.jtr");
        checkJTRLine(70, "System out printing 203974850982375", "exec", "index_ExecFailRuntimExc.jtr");
        checkJTRLine(71, "result: Failed. exit code 1", "exec", "index_ExecFailRuntimExc.jtr");
    }

    @Override
    protected int[] getExpectedTestRunFinalStats() {
        return new int[]{0, 1, 0, 0};
    }


    @Override
    protected String[] getExpectedLinesInTestrunSummary() {
        return new String[]{
                "exec/index.html#ExecFailRuntimExc  Failed. exit code 1"
        };
    }

    protected List<String> getTailArgs() {
        return Arrays.asList("exec/index.html#ExecFailRuntimExc");
    }

    protected String getEnvName() {
        return "basic";
    }

    protected String getEnvfileName() {
        return "basic.jte";
    }

    protected String getTestsuiteName() {
        return "demotck";
    }


}
