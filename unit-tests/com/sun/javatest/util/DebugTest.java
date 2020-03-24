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


import java.io.PrintStream;
import java.util.Properties;

import com.sun.javatest.util.Debug;
import org.junit.Assert;
import org.junit.Test;

/**
 * TODO This test needs some negative cases.
 */
public class DebugTest {


    @Test
    public void testNormBool() {
        loadProps(system1);
        Debug.init(true);

        boolean result = Debug.getBoolean(this.getClass());
        if (!result) {
            error("simple getBoolean(Class) returned false", BOOLEAN_FAIL);
        }

        result = Debug.getBoolean(this.getClass(), "customext");
        if (!result) {
            error("simple getBoolean(Class, String) returned false", BOOLEAN_FAIL);
        }

        result = Debug.getBoolean("myFlag");
        if (!result) {
            error("simple getBoolean(String) returned false", BOOLEAN_FAIL);
        }

        MyInner mi = new MyInner();
        result = Debug.getBoolean(mi.getClass());
        if (!result) {
            error("simple getBoolean(InnerClass) with integer conversion returned false", BOOLEAN_FAIL);
        }

        result = Debug.getBoolean(this.getClass(), "withint");
        if (!result) {
            error("simple getBoolean(Class, String) with integer conversion returned false", BOOLEAN_FAIL);
        }

        result = Debug.getBoolean("myInt");
        if (!result) {
            error("simple getBoolean(String) with integer conversion failed ", BOOLEAN_FAIL);
        }
    }

    @Test
    public void testNormInt() {
        loadProps(system2);
        Debug.init(true);

        int result = Debug.getInt(this.getClass());
        if (result != 1) {
            error("simple getInt(Class) returned non-one number", BOOLEAN_FAIL);
        }

        result = Debug.getInt(this.getClass(), "customext");
        if (result != 2) {
            error("simple getInt(Class,String) returned non-two number", BOOLEAN_FAIL);
        }

        MyInner mi = new MyInner();
        result = Debug.getInt(mi.getClass());
        if (result != 1) {
            error("simple getInt(InnerClass) did not return 1", BOOLEAN_FAIL);
        }

        result = Debug.getInt("myNum");
        if (result != 10) {
            error("simple getInt(String) did not return 10", BOOLEAN_FAIL);
        }

        result = Debug.getInt("myBool");
        if (result != 1) {
            error("simple getInt(String) with boolean conversion did not return 1", BOOLEAN_FAIL);
        }
    }

    @Test
    public void testWildBool() {
        loadProps(system3);
        Debug.init(true);

        boolean result = Debug.getBoolean(this.getClass());
        if (!result) {
            error("wild getBoolean(Class) returned false", BOOLEAN_FAIL);
        }

        MyInner mi = new MyInner();
        result = Debug.getBoolean(mi.getClass());
        if (!result) {
            error("wild getBoolean(InnerClass) with boolean conversion returned false", BOOLEAN_FAIL);
        }
    }

    @Test
    public void testWildInt() {
        loadProps(system4);
        Debug.init(true);

        int result = Debug.getInt(this.getClass());
        if (result != 5) {
            error("simple getInt(Class) returned non-five number", BOOLEAN_FAIL);
        }

        result = Debug.getInt("my.custom.bar");
        if (result != 9) {
            error("wild getInt(String) returned non-nine number", BOOLEAN_FAIL);
        }

        MyInner mi = new MyInner();
        result = Debug.getInt("my.custom.strings.foo");
        if (result != 1) {
            error("wild getInt(String) with boolean conversion returned non-one number", BOOLEAN_FAIL);
        }

    }

    // ----------------------------

    private void loadProps(String[] settings) {
        Properties props = new Properties();
        for (int i = 0; i < settings.length; i += 2) {
            props.put(settings[i], settings[i + 1]);
        }

        System.setProperties(props);
    }

    // simple boolean tests
    String[] system1 = {"debug.com.sun.javatest.util.DebugTest", "true",
            "debug.com.sun.javatest.util.DebugTest.customext", "true",
            "debug.com.sun.javatest.util.DebugTest.MyInner", "2",
            "debug.com.sun.javatest.util.DebugTest.withint", "9",
            "debug.myInt", "1",
            "debug.myFlag", "true"};

    // simple integer tests
    String[] system2 = {"debug.com.sun.javatest.util.DebugTest", "1",
            "debug.com.sun.javatest.util.DebugTest.customext", "2",
            "debug.com.sun.javatest.util.DebugTest.MyInner", "true",
            "debug.myNum", "10",
            "debug.myBool", "true"};

    // wild boolean tests
    String[] system3 = {"debug.com.sun.javatest.util.*", "true",
            "debug.com.sun.javatest.util.DebugTest.*", "1"};

    // wild integer tests
    String[] system4 = {"debug.my.custom.*", "9",
            "debug.com.sun.javatest.util.*", "5",
            "debug.my.custom.strings.*", "true"};

    static class MyInner {
        public MyInner() {
        }
    }

    private static final int INIT_FAIL = 1;
    private static final int BOOLEAN_FAIL = 2;
    private static final int INT_FAIL = 3;
    private PrintStream out = System.out;

    void error(String msg, int status) {
        Assert.fail(msg);
    }

}

