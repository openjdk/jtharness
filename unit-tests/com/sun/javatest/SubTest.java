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
package com.sun.javatest;

import com.sun.javatest.util.StringArray;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class SubTest {

    @Test
    public void test() {
        SubTest t = new SubTest();
            String[] args = new String[]{
                    "1", "$SubTest_file1", "/alpha/beta/gamma",
                    "2", "$SubTest_file2", "/delta/epsilon",
                    "3", "$SubTest_path ", "/alpha/beta/gamma:/delta/epsilon",
                    "4", "${SubTest_file1:FS=!}", "!alpha!beta!gamma",
                    "5", "${SubTest_file1:PS=+}", "/alpha/beta/gamma",
                    "6", "${SubTest_file1:FS=! PS=+}", "!alpha!beta!gamma",
                    "7", "${SubTest_path}", "/alpha/beta/gamma:/delta/epsilon",
                    "8", "${SubTest_path:FS=^}", "^alpha^beta^gamma:^delta^epsilon",
                    "9", "${SubTest_path:PS=@}", "/alpha/beta/gamma@/delta/epsilon",
                    "10", "${SubTest_path:FS=^ PS=@}", "^alpha^beta^gamma@^delta^epsilon"};
        boolean ok = t.run(args, System.out);
        Assert.assertTrue(ok);
    }

    public boolean run(String[] args, PrintStream out) {
        if (args.length % 3 != 0) {
            out.println("bad number of args: " + args.length);
            return false;
        }

        Map<String, String> envData = new HashMap<>();
        envData.put("SubTest_file1", "$/alpha$/beta$/gamma");
        envData.put("SubTest_file2", "$/delta$/epsilon");
        envData.put("SubTest_path", "$SubTest_file1$:$SubTest_file2");

        TestEnvironment env;
        try {
            env = new TestEnvironment("subtest", envData, "subtestData");
        } catch (TestEnvironment.Fault e) {
            e.printStackTrace(out);
            return false;
        }

        boolean ok = true;

        for (int i = 0; i < args.length; i += 3) {
            String count = args[i];
            String test = args[i + 1];
            String ref = args[i + 2].replace('/', File.separatorChar).replace(':', File.pathSeparatorChar);

            if (!count.equals(String.valueOf(i / 3 + 1))) {
                out.println("count mismatch at " + i);
                return false; // cannot reasonably continue
            }

            String[] result;
            try {
                result = env.resolve(test);
            } catch (TestEnvironment.Fault e) {
                out.println("problem resolving entry " + i + ": `" + test + "'");
                ok = false;
                continue;
            }

            if (result == null || result.length != 1) {
                out.println("bad result for entry " + i + ": `" + StringArray.join(result) + "'");
                ok = false;
                continue;
            }

            if (!result[0].equals(ref)) {
                out.println("args mismatch, " + count + ":`" + result[0] + "' vs. `" + ref + "'");
                ok = false;
                continue;
            }
        }

        return ok;
    }


}
