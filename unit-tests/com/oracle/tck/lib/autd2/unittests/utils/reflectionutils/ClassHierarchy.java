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
package com.oracle.tck.lib.autd2.unittests.utils.reflectionutils;

import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.lib.ExpectedExceptions;
import com.sun.tck.lib.tgf.ReflectionUtils;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.lib.tgf.data.Longs;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Assert;
import org.junit.Test;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static com.sun.tck.lib.Assert.assertTrue;
import static com.sun.tck.lib.tgf.DataFactory.createColumn;
import static com.sun.tck.lib.tgf.DataFactory.createRow;
import static java.awt.Color.*;

/**
 *
 */
public class ClassHierarchy {

    @Test
    public void test_01() {
        List<Class<?>> classHierarchy = ReflectionUtils.getClassHierarchy(Object.class);
        Assert.assertEquals(1, classHierarchy.size());
        Assert.assertEquals(Object.class, classHierarchy.get(0));
    }

    @Test
    public void test_02() {
        List<Class<?>> classHierarchy = ReflectionUtils.getClassHierarchy(Serializable.class);
        Assert.assertEquals(1, classHierarchy.size());
        Assert.assertEquals(Serializable.class, classHierarchy.get(0));
    }

    @Test
    public void test_03() {
        List<Class<?>> classHierarchy = ReflectionUtils.getClassHierarchy(Number.class);
        Assert.assertEquals(3, classHierarchy.size());
        Assert.assertEquals(Number.class, classHierarchy.get(0));
        Assert.assertEquals(Serializable.class, classHierarchy.get(1));
        Assert.assertEquals(Object.class, classHierarchy.get(2));
    }

    @Test
    public void test_04() {
        class Clazzz {  }
        List<Class<?>> classHierarchy = ReflectionUtils.getClassHierarchy(Clazzz.class);
        Assert.assertEquals(2, classHierarchy.size());
        Assert.assertEquals(Clazzz.class, classHierarchy.get(0));
        Assert.assertEquals(Object.class, classHierarchy.get(1));
    }

    @Test
    public void test_05() {
        class Clazzz1 {  }
        class Clazzz2 extends Clazzz1 {  }
        List<Class<?>> classHierarchy = ReflectionUtils.getClassHierarchy(Clazzz2.class);
        Assert.assertEquals(3, classHierarchy.size());
        Assert.assertEquals(Clazzz2.class, classHierarchy.get(0));
        Assert.assertEquals(Clazzz1.class, classHierarchy.get(1));
        Assert.assertEquals(Object.class, classHierarchy.get(2));
    }

    interface Inter1 {  }

    @Test
    public void test_06() {

        class Clazzz2 implements Inter1 {  }
        List<Class<?>> classHierarchy = ReflectionUtils.getClassHierarchy(Clazzz2.class);
        Assert.assertEquals(3, classHierarchy.size());
        Assert.assertEquals(Clazzz2.class, classHierarchy.get(0));
        Assert.assertEquals(Inter1.class, classHierarchy.get(1));
        Assert.assertEquals(Object.class, classHierarchy.get(2));
    }

    interface Inter2 extends Inter1 {  }

    @Test
    public void test_07() {

        class Clazzz2 implements Inter2 {  }
        List<Class<?>> classHierarchy = ReflectionUtils.getClassHierarchy(Clazzz2.class);
        Assert.assertEquals(4, classHierarchy.size());
        Assert.assertTrue(classHierarchy.contains(Inter1.class));
        Assert.assertTrue(classHierarchy.contains(Inter2.class));
        Assert.assertTrue(classHierarchy.contains(Clazzz2.class));
        Assert.assertTrue(classHierarchy.contains(Object.class));
    }

}
