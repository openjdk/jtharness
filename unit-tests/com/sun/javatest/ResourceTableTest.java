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


public class ResourceTableTest implements Harness.Observer {

    @Test
    public void test_01() throws IOException {
        ResourceTable resourceTable = new ResourceTable();
        Assert.assertTrue(resourceTable.table().isEmpty());
    }

    @Test
    public void test_02() throws IOException {
        ResourceTable resourceTable = new ResourceTable(1000);
        Assert.assertTrue(resourceTable.table().isEmpty());
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
    public void test_03() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        Assert.assertTrue( resourceTable.acquire(new String[] {"x"}, 1) );
        Map<String, Thread> table = resourceTable.table();
        Assert.assertEquals(1, table.size());
        Assert.assertEquals(Thread.currentThread(), table.get("x"));
    }

    @Test
    public void test_03_01() throws IOException, InterruptedException {
        for (int i=0; i < 20; i ++) {
            ResourceTable rt = new ResourceTable();
            Assert.assertTrue(rt.acquire(new String[]{"x"}, 100));
            Assert.assertFalse(rt.acquire(new String[]{"x"}, 100));
            Assert.assertTrue(rt.acquire(new String[]{"x"}, 100));
            Assert.assertFalse(rt.acquire(new String[]{"x"}, 100));
        }
    }

   @Test
   public void test_03_02() throws IOException, InterruptedException {

        List<Integer> timeouts = Arrays.asList(10, 50, 100, 200, 300);
        for (Integer timeout : timeouts) {
            ResourceTable resourceTable = new ResourceTable();
            Assert.assertTrue( resourceTable.acquire(new String[] {"x"}, timeout) );
            Map<String, Thread> table;
            table = resourceTable.table();
            Assert.assertEquals(1, table.size());
            Assert.assertEquals(Thread.currentThread(), table.get("x"));
            Assert.assertFalse( resourceTable.acquire(new String[] {"x"}, timeout) );
            Assert.assertEquals(0, table.size());
        }
    }

    @Test
    public void test_04() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        Assert.assertTrue( resourceTable.acquire(new String[] {"x", "y"}, 1) );
        Map<String, Thread> table = resourceTable.table();
        Assert.assertEquals(2, table.size());
        Assert.assertEquals(Thread.currentThread(), table.get("x"));
        Assert.assertEquals(Thread.currentThread(), table.get("y"));
    }

    @Test
    public void test_05() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        Assert.assertTrue(resourceTable.acquire(new String[] {"x", "y", "z"}, 200));
        Map<String, Thread> table = resourceTable.table();
        Assert.assertEquals(3, table.size());
        Assert.assertEquals(Thread.currentThread(), table.get("x"));
        Assert.assertEquals(Thread.currentThread(), table.get("y"));
        Assert.assertEquals(Thread.currentThread(), table.get("z"));
    }

    @Test
    public void test_05_pushingOut() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        Assert.assertTrue(resourceTable.acquire(new String[] {"x", "y", "z"}, 200));
        Assert.assertEquals(3, resourceTable.table().size());
        Assert.assertEquals(Thread.currentThread(), resourceTable.table().get("x"));
        Assert.assertEquals(Thread.currentThread(), resourceTable.table().get("y"));
        Assert.assertEquals(Thread.currentThread(), resourceTable.table().get("z"));
        
        Assert.assertFalse(resourceTable.acquire(new String[] {"x"}, 100));

        Assert.assertEquals(2, resourceTable.table().size());
        Assert.assertEquals(Thread.currentThread(), resourceTable.table().get("y"));
        Assert.assertEquals(Thread.currentThread(), resourceTable.table().get("z"));

        Assert.assertFalse(resourceTable.acquire(new String[] {"y"}, 100));

        Assert.assertEquals(1, resourceTable.table().size());
        Assert.assertEquals(Thread.currentThread(), resourceTable.table().get("z"));


        Assert.assertTrue(resourceTable.acquire(new String[] {"x"}, 1000));

        Assert.assertEquals(2, resourceTable.table().size());
        Assert.assertEquals(Thread.currentThread(), resourceTable.table().get("x"));
        Assert.assertEquals(Thread.currentThread(), resourceTable.table().get("z"));

        Assert.assertFalse(resourceTable.acquire(new String[] {"x", "z"}, 1000));

        Assert.assertEquals(0, resourceTable.table().size());

    }

    @Test
    public void test_06_pushingOut() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        Assert.assertTrue(resourceTable.acquire(new String[] {"a", "b", "c"}, 200));
        Assert.assertEquals(3, resourceTable.table().size());
        Assert.assertFalse(resourceTable.acquire(new String[] {"a", "b", "c"}, 200));
        Assert.assertEquals(0, resourceTable.table().size());
    }

    @Test
    public void test_07_pushingOut() throws IOException, InterruptedException {
        ResourceTable resourceTable = new ResourceTable();
        Assert.assertTrue(resourceTable.acquire(new String[] {"a", "b", "c"}, 200));
        Assert.assertEquals(3, resourceTable.table().size());
        // evrything is rejected, "d" is not going to be added
        Assert.assertFalse(resourceTable.acquire(new String[] {"a", "b", "c", "d"}, 200));
        Assert.assertEquals(0, resourceTable.table().size());
    }


}
