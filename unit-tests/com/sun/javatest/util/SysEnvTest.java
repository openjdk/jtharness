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

package com.sun.javatest.util;


import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class SysEnvTest {

    @Test
    public void all() {
        SysEnv.setCommand(null);
        Map<String, String> all = SysEnv.getAll();
        showMap(all);
    }

    @Test
    public void setCommand() {
        SysEnv.setCommand(SysEnv.getDefaultCommand());
        Map<String, String> all = SysEnv.getAll();
        showMap(all);
    }

    @Test
    public void allToGivenMap2() {
        Map<String, String> all = SysEnv.getAll();
        HashMap<String, String> putTo = new HashMap<>();
        SysEnv.getAll(putTo);
        Assert.assertEquals(all.size(), putTo.size());
        for (Map.Entry<String, String> entry : all.entrySet()) {
            Assert.assertTrue(putTo.containsKey(entry.getKey()));
            Assert.assertTrue(putTo.containsValue(entry.getValue()));
        }
    }

    @Test
    public void get() {
        Map<String, String> all = SysEnv.getAll();
        for (Map.Entry<String, String> entry : all.entrySet()) {
            Assert.assertEquals(entry.getValue(), SysEnv.get(entry.getKey()));
        }
    }


    private static void showMap(Map<String, String> m) {
        for (Map.Entry<String, String> o : m.entrySet()) {
            Map.Entry<String, String> e = o;
            System.err.println(e.getKey() + " = " + e.getValue());
        }
    }
}


