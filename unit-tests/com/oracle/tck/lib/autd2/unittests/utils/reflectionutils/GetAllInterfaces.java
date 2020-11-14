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

import com.sun.tck.lib.tgf.ReflectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class GetAllInterfaces {

    @Test
//    @DisplayName("zero interfaces")
    public void test_01() {
        Set<Class<?>> allInterfaces = ReflectionUtils.getAllInterfaces(Object.class);
        assertEquals(0, allInterfaces.size());
    }

    @Test
    public void test_02() {
        Set<Class<?>> allInterfaces = ReflectionUtils.getAllInterfaces(Serializable.class);
        assertEquals(0, allInterfaces.size());
    }

    @Test
    public void test_03() {
        Set<Class<?>> allInterfaces = ReflectionUtils.getAllInterfaces(Number.class);
        assertEquals(1, allInterfaces.size());
        assertTrue(allInterfaces.contains(Serializable.class));
    }


    @Test
    public void test_04() {
        class Clazzz {  }
        Set<Class<?>> allInterfaces = ReflectionUtils.getAllInterfaces(Clazzz.class);
        assertEquals(0, allInterfaces.size());
    }

    @Test
    public void test_05() {
        class Clazzz1 {  }
        class Clazzz2 extends Clazzz1 {  }
        Set<Class<?>> allInterfaces = ReflectionUtils.getAllInterfaces(Clazzz2.class);
        assertEquals(0, allInterfaces.size());
    }

    @Test
    public void test_06() {
        class Clazzz1 {  }
        class Clazzz2 extends Clazzz1 implements Runnable {
            @Override
            public void run() {
            }
        }
        Set<Class<?>> allInterfaces = ReflectionUtils.getAllInterfaces(Clazzz2.class);
        assertEquals(1, allInterfaces.size());
        assertTrue(allInterfaces.contains(Runnable.class));
    }

    @Test
    public void test_07() {
        class Clazzz1 implements Runnable {
            @Override
            public void run() {

            }
        }
        class Clazzz2 extends Clazzz1 {
        }
        assertEquals(0, ReflectionUtils.getAllInterfaces(Clazzz2.class).size());
        Set<Class<?>> interfaces = ReflectionUtils.getAllInterfaces(Clazzz1.class);
        assertEquals(1, interfaces.size());
        assertTrue(interfaces.contains(Runnable.class));
    }


    @Test
    public void test_08() {
        class Clazzz implements Runnable, Serializable {
            private static final long serialVersionUID = 0L;
            @Override
            public void run() {

            }
        }
        Set<Class<?>> interfaces = ReflectionUtils.getAllInterfaces(Clazzz.class);
        assertEquals(2, interfaces.size());
        assertTrue(interfaces.contains(Runnable.class));
        assertTrue(interfaces.contains(Serializable.class));
    }

    interface I1 {

    }

    interface I2 extends I1 {

    }

    interface I3 extends I1, I2 {

    }


    @Test
    public void test_09() {
        class Clazzz implements Runnable, Serializable, I1 {
            private static final long serialVersionUID = 0L;
            @Override
            public void run() {

            }
        }
        Set<Class<?>> interfaces = ReflectionUtils.getAllInterfaces(Clazzz.class);
        assertEquals(3, interfaces.size());
        assertTrue(interfaces.contains(Runnable.class));
        assertTrue(interfaces.contains(Serializable.class));
        assertTrue(interfaces.contains(I1.class));
    }

    @Test
    public void test_10() {
        class Clazzz implements Runnable, I2 {
            @Override
            public void run() {

            }
        }
        Set<Class<?>> interfaces = ReflectionUtils.getAllInterfaces(Clazzz.class);
        assertEquals(3, interfaces.size());
        assertTrue(interfaces.contains(Runnable.class));
        assertTrue(interfaces.contains(I2.class));
        assertTrue(interfaces.contains(I1.class));
    }

    @Test
    public void test_11() {
        class Clazzz implements Runnable, I3 {
            @Override
            public void run() {

            }
        }
        Set<Class<?>> interfaces = ReflectionUtils.getAllInterfaces(Clazzz.class);
        assertEquals(4, interfaces.size());
        assertTrue(interfaces.contains(Runnable.class));
        assertTrue(interfaces.contains(I3.class));
        assertTrue(interfaces.contains(I2.class));
        assertTrue(interfaces.contains(I1.class));
    }

    @Test
    public void test_12() {
        class Clazzz implements I3 {
            public void run() {

            }
        }
        Set<Class<?>> interfaces = ReflectionUtils.getAllInterfaces(Clazzz.class);
        assertEquals(3, interfaces.size());
        assertTrue(interfaces.contains(I3.class));
        assertTrue(interfaces.contains(I2.class));
        assertTrue(interfaces.contains(I1.class));
    }


//
//    interface Inter1 {  }
//
//    @Test
//    public void test_06() {
//
//        class Clazzz2 implements Inter1 {  }
//        List<Class> classHierarchy = ReflectionUtils.getAllInterfaces(Clazzz2.class);
//        Assert.assertEquals(3, classHierarchy.size());
//        Assert.assertEquals(Clazzz2.class, classHierarchy.get(0));
//        Assert.assertEquals(Inter1.class, classHierarchy.get(1));
//        Assert.assertEquals(Object.class, classHierarchy.get(2));
//    }


}
