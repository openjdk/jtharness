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
package com.sun.javatest;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;


public class ResourceTableTest implements Harness.Observer {

    @Test
    public void test_01() throws IOException {
        ResourceTable resourceTable = new ResourceTable();
        assertTrue(resourceTable.table().isEmpty());
    }

    @Test
    public void test_02() throws IOException {
        ResourceTable resourceTable = new ResourceTable(1000);
        assertTrue(resourceTable.table().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongTimeout_01() throws IOException, InterruptedException {
        new ResourceTable(1000).acquire(new String[]{"213"}, -1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void wrongTimeout_02() throws IOException, InterruptedException {
        new ResourceTable(1000).acquire(new String[]{"213"}, 0);
    }

    @Test
    public void test_01_release_nonexisting() throws IOException, InterruptedException {
        ResourceTable rt = new ResourceTable();
        Map<String, Thread> table = rt.table();
        rt.release("a");
        assertEquals(0, table.size());
    }

    @Test
    public void test_02_release_nonexisting() throws IOException, InterruptedException {
        ResourceTable rt = new ResourceTable();
        Map<String, Thread> table = rt.table();
        rt.release("a", "b", "c");
        assertEquals(0, table.size());
    }

    @Test
    public void test_02_release_empty() throws IOException, InterruptedException {
        ResourceTable rt = new ResourceTable();
        Map<String, Thread> table = rt.table();
        rt.release();
        assertEquals(0, table.size());
    }

    @Test
    public void test_03() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        Map<String, Thread> table = resourceTable.table();
        assertTrue(resourceTable.acquire(new String[]{"x"}, 1));
        assertEquals(1, table.size());
        assertEquals(Thread.currentThread(), table.get("x"));
    }

    @Test
    public void test_03_addrelease_01() throws IOException, InterruptedException {
        ResourceTable rt = new ResourceTable();
        Map<String, Thread> table = rt.table();
        assertTrue(rt.acquire(new String[]{"a"}, 1));
        assertEquals(1, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        rt.release("a");
        assertEquals(0, table.size());
    }

    @Test
    public void test_03_addrelease_02() throws IOException, InterruptedException {
        ResourceTable rt = new ResourceTable();
        Map<String, Thread> table = rt.table();
        assertTrue(rt.acquire(new String[]{"a"}, 1));
        assertEquals(1, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        rt.release("a", "x", "y", "z", "non_existing");
        assertEquals(0, table.size());
    }

    @Test
    public void test_03_addrelease_03() throws IOException, InterruptedException {
        ResourceTable rt = new ResourceTable();
        Map<String, Thread> table = rt.table();
        assertTrue(rt.acquire(new String[]{"a", "b"}, 1));
        assertEquals(2, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        assertEquals(Thread.currentThread(), table.get("b"));
        rt.release("b");
        assertEquals(1, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        rt.release("b");
        assertEquals(1, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        rt.release("a");
        assertEquals(0, table.size());
    }

    @Test
    public void test_03_addrelease_04() throws IOException, InterruptedException {
        ResourceTable rt = new ResourceTable();
        Map<String, Thread> table = rt.table();
        assertTrue(rt.acquire(new String[]{"a", "b", "c"}, 100));
        assertEquals(3, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        assertEquals(Thread.currentThread(), table.get("b"));
        assertEquals(Thread.currentThread(), table.get("c"));
        rt.release("b");
        assertEquals(2, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        assertEquals(Thread.currentThread(), table.get("c"));
        assertTrue(rt.acquire(new String[]{"b"}, 100));
        assertEquals(3, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        assertEquals(Thread.currentThread(), table.get("b"));
        assertEquals(Thread.currentThread(), table.get("c"));

        assertFalse(rt.acquire(new String[]{"b"}, 100));
        assertEquals(2, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        assertEquals(Thread.currentThread(), table.get("c"));
        rt.release("b");
        assertEquals(2, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        assertEquals(Thread.currentThread(), table.get("c"));
        rt.release("a");
        assertEquals(1, table.size());
        assertEquals(Thread.currentThread(), table.get("c"));

        assertTrue(rt.acquire(new String[]{"x", "y"}, 100));
        assertEquals(3, table.size());
        assertEquals(Thread.currentThread(), table.get("x"));
        assertEquals(Thread.currentThread(), table.get("y"));
        assertEquals(Thread.currentThread(), table.get("c"));

        assertFalse(rt.acquire(new String[]{"y"}, 100));
        assertEquals(2, table.size());
        assertEquals(Thread.currentThread(), table.get("x"));
        assertEquals(Thread.currentThread(), table.get("c"));
        rt.release("x", "c");
        assertEquals(0, table.size());

        assertTrue(rt.acquire(new String[]{"a", "b", "c"}, 100));
        assertEquals(3, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        assertEquals(Thread.currentThread(), table.get("b"));
        assertEquals(Thread.currentThread(), table.get("c"));
        assertFalse(rt.acquire(new String[]{"b", "c"}, 100));
        assertEquals(1, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        assertTrue(rt.acquire(new String[]{"b", "c"}, 100));
        assertEquals(3, table.size());
        assertEquals(Thread.currentThread(), table.get("a"));
        assertEquals(Thread.currentThread(), table.get("b"));
        assertEquals(Thread.currentThread(), table.get("c"));
        rt.release("c", "a", "b");
        assertEquals(0, table.size());
        rt.release("c", "a", "b");
        assertEquals(0, table.size());
    }

    @Test
    public void test_03_01() throws IOException, InterruptedException {
        for (int i = 0; i < 20; i++) {
            ResourceTable rt = new ResourceTable();
            assertTrue(rt.acquire(new String[]{"x"}, 100));
            assertFalse(rt.acquire(new String[]{"x"}, 100));
            assertTrue(rt.acquire(new String[]{"x"}, 100));
            assertFalse(rt.acquire(new String[]{"x"}, 100));
        }
    }

    @Test
    public void test_03_01_01() throws IOException, InterruptedException {
        ResourceTable rt = new ResourceTable();
        Map<String, Thread> table = rt.table();
        for (int i = 0; i < 20; i++) {
            assertTrue(rt.acquire(new String[]{"x"}, 100));
            assertEquals(1, table.size());
            assertEquals(Thread.currentThread(), table.get("x"));
            assertFalse(rt.acquire(new String[]{"x"}, 100));
            assertEquals(0, table.size());
        }
    }

    @Test
    public void test_03_01_01_release() throws IOException, InterruptedException {
        ResourceTable rt = new ResourceTable();
        Map<String, Thread> table = rt.table();
        for (int i = 0; i < 20; i++) {
            assertTrue(rt.acquire(new String[]{"x"}, 100));
            assertEquals(1, table.size());
            assertEquals(Thread.currentThread(), table.get("x"));
            rt.release("x");
            assertEquals(0, table.size());
        }
    }

    @Test
    public void test_03_02() throws IOException, InterruptedException {

        List<Integer> timeouts = Arrays.asList(10, 50, 100, 200, 300);
        for (Integer timeout : timeouts) {
            ResourceTable resourceTable = new ResourceTable();
            assertTrue(resourceTable.acquire(new String[]{"x"}, timeout));
            Map<String, Thread> table;
            table = resourceTable.table();
            assertEquals(1, table.size());
            assertEquals(Thread.currentThread(), table.get("x"));
            assertFalse(resourceTable.acquire(new String[]{"x"}, timeout));
            assertEquals(0, table.size());
        }
    }

    @Test
    public void test_04() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        assertTrue(resourceTable.acquire(new String[]{"x", "y"}, 1));
        Map<String, Thread> table = resourceTable.table();
        assertEquals(2, table.size());
        assertEquals(Thread.currentThread(), table.get("x"));
        assertEquals(Thread.currentThread(), table.get("y"));
    }

    @Test
    public void test_04_01() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        assertTrue(resourceTable.acquire(new String[]{"x"}, 1));
        assertTrue(resourceTable.acquire(new String[]{"y"}, 1));
        Map<String, Thread> table = resourceTable.table();
        assertEquals(2, table.size());
        assertEquals(Thread.currentThread(), table.get("x"));
        assertEquals(Thread.currentThread(), table.get("y"));
    }

    @Test
    public void test_05() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        assertTrue(resourceTable.acquire(new String[]{"x", "y", "z"}, 200));
        Map<String, Thread> table = resourceTable.table();
        assertEquals(3, table.size());
        assertEquals(Thread.currentThread(), table.get("x"));
        assertEquals(Thread.currentThread(), table.get("y"));
        assertEquals(Thread.currentThread(), table.get("z"));
    }

    @Test
    public void test_05_pushingOut() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        assertTrue(resourceTable.acquire(new String[]{"x", "y", "z"}, 200));
        assertEquals(3, resourceTable.table().size());
        assertEquals(Thread.currentThread(), resourceTable.table().get("x"));
        assertEquals(Thread.currentThread(), resourceTable.table().get("y"));
        assertEquals(Thread.currentThread(), resourceTable.table().get("z"));

        assertFalse(resourceTable.acquire(new String[]{"x"}, 100));

        assertEquals(2, resourceTable.table().size());
        assertEquals(Thread.currentThread(), resourceTable.table().get("y"));
        assertEquals(Thread.currentThread(), resourceTable.table().get("z"));

        assertFalse(resourceTable.acquire(new String[]{"y"}, 100));

        assertEquals(1, resourceTable.table().size());
        assertEquals(Thread.currentThread(), resourceTable.table().get("z"));


        assertTrue(resourceTable.acquire(new String[]{"x"}, 1000));

        assertEquals(2, resourceTable.table().size());
        assertEquals(Thread.currentThread(), resourceTable.table().get("x"));
        assertEquals(Thread.currentThread(), resourceTable.table().get("z"));

        assertFalse(resourceTable.acquire(new String[]{"x", "z"}, 1000));

        assertEquals(0, resourceTable.table().size());

    }

    @Test
    public void test_06_pushingOut() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        assertTrue(resourceTable.acquire(new String[]{"a", "b", "c"}, 200));
        assertEquals(3, resourceTable.table().size());
        assertFalse(resourceTable.acquire(new String[]{"a", "b", "c"}, 200));
        assertEquals(0, resourceTable.table().size());
    }

    @Test
    public void test_07_pushingOut() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        assertTrue(resourceTable.acquire(new String[]{"a", "b", "c"}, 200));
        Map<String, Thread> table = resourceTable.table();
        assertEquals(3, table.size());
        // evrything is rejected, "d" is not going to be added
        assertFalse(resourceTable.acquire(new String[]{"a", "b", "c", "d"}, 200));
        assertEquals(0, table.size());
    }

    @Test
    public void test_08() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        Map<String, Thread> table = resourceTable.table();
        assertTrue(resourceTable.acquire(new String[]{"c", "d"}, 200));
        assertEquals(2, table.size());
        assertEquals(Thread.currentThread(), resourceTable.table().get("c"));
        assertEquals(Thread.currentThread(), resourceTable.table().get("d"));

        // pushing out "d"
        assertFalse(resourceTable.acquire(new String[]{"d"}, 200));
        assertEquals(1, table.size());
        assertEquals(Thread.currentThread(), resourceTable.table().get("c"));

        assertTrue(resourceTable.acquire(new String[]{"d"}, 200));
        assertEquals(2, table.size());
        assertEquals(Thread.currentThread(), resourceTable.table().get("c"));
        assertEquals(Thread.currentThread(), resourceTable.table().get("d"));

        // pushing out "c"
        assertFalse(resourceTable.acquire(new String[]{"c"}, 200));
        assertEquals(1, table.size());
        assertEquals(Thread.currentThread(), resourceTable.table().get("d"));
        // pushing out "d"
        assertFalse(resourceTable.acquire(new String[]{"d"}, 200));
        assertEquals(0, table.size());

    }


    @Test
    public void test_09_01() throws IOException, InterruptedException {

        CountDownLatch latch = new CountDownLatch(3);
        ResourceTable rt = new ResourceTable();

        new Thread(() -> {
            try {
                assertTrue(rt.acquire(new String[]{"x", "y"}, Integer.MAX_VALUE));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Map<String, Thread> table = rt.table();
            assertEquals(2, table.size());
            assertEquals(Thread.currentThread(), table.get("x"));
            assertEquals(Thread.currentThread(), table.get("y"));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            rt.release("x", "y");
            latch.countDown();
        }, "first").start();

        new Thread(() -> {
            try {
                rt.acquire(new String[] {"y"}, Integer.MAX_VALUE);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            rt.release( "y");
            latch.countDown();
        }, "second").start();


        new Thread(() -> {
            try {
                rt.acquire(new String[] {"y"}, Integer.MAX_VALUE);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            rt.release( "y");
            latch.countDown();
        }, "third").start();

        latch.await();
    }


}
