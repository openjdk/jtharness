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

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ExcludeTest {

    private File data = new File(TestUtil.getPathToData() + File.separator + "exclude");
    private boolean verbose = Boolean.getBoolean("verbose");
    private String currTest;

    @Test
    public void empty() throws IOException, ExcludeList.Fault {
        ExcludeList el = new ExcludeList();
        Assert.assertTrue(el.isEmpty());
        Assert.assertFalse(el.isStrictModeEnabled());
        Assert.assertEquals(0, el.size());
        el.setStrictModeEnabled(true);
        Assert.assertTrue(el.isStrictModeEnabled());
        el.setStrictModeEnabled(false);
        Assert.assertFalse(el.isStrictModeEnabled());
        el.setTitle("A title 1234");
        Assert.assertEquals("A title 1234", el.getTitle());
    }

    @Test
    public void addRemove() throws IOException, ExcludeList.Fault {
        ExcludeList el = new ExcludeList();
        ExcludeList.Entry added = new ExcludeList.Entry("q/w/e", "tc", new String[]{"23"}, new String[]{"platform1"}, "synopsys");
        el.addEntry(added);
        Assert.assertEquals(1, el.size());
        Assert.assertFalse(el.isEmpty());

        Assert.assertTrue(el.excludesAnyOf("q/w/e"));
        ExcludeList.Entry entry = el.getEntry("q/w/e");
        checkEntry(entry, "23", "platform1", "synopsys");
        el.removeEntry(added);
        Assert.assertNull(el.getEntry("q/w/e"));
        Assert.assertEquals(0, el.size());
        Assert.assertTrue(el.isEmpty());
    }

    @Test
    public void addRemove2() throws IOException, ExcludeList.Fault {
        ExcludeList el = new ExcludeList();
        ExcludeList.Entry added = new ExcludeList.Entry("q/w/e#x", null, new String[]{"45"}, new String[]{"platform"}, "synopsys");
        el.addEntry(added);
        Assert.assertEquals(1, el.size());
        Assert.assertFalse(el.isEmpty());

        Assert.assertTrue(el.excludesAnyOf("q/w/e#x"));
        ExcludeList.Entry entry = el.getEntry("q/w/e#x");
        checkEntry(entry, "45", "platform", "synopsys");
        el.removeEntry(added);
        Assert.assertNull(el.getEntry("q/w/e#x"));
        Assert.assertEquals(0, el.size());
        Assert.assertTrue(el.isEmpty());
    }

        @Test
    public void reading_empty() throws IOException, ExcludeList.Fault {
        ExcludeList el = new ExcludeList(new File(data, "empty.jtx"));
        Assert.assertEquals(0, el.size());
        Assert.assertTrue(el.isEmpty());
        Assert.assertFalse(el.excludesAllOf("d/e/f"));
        Assert.assertFalse(el.excludesAllOf("a/b/c"));
        Assert.assertFalse(el.excludesAllOf("j/h/k"));
        Assert.assertFalse(el.excludesAllOf("a/b/c#e[g]"));
        Assert.assertFalse(el.isStrictModeEnabled());

    }

    @Test
    public void reading_1() throws IOException, ExcludeList.Fault {
        ExcludeList el = new ExcludeList(new File(data, "test1.jtx"));
        Assert.assertEquals(6, el.size());
        Assert.assertFalse(el.isEmpty());
        Assert.assertTrue(el.excludesAllOf("d/e/f"));
        Assert.assertTrue(el.excludesAllOf("a/b/c"));
        Assert.assertFalse(el.excludesAllOf("j/h/k"));
        Assert.assertFalse(el.excludesAllOf("a/b/c#e[g]"));

        Assert.assertFalse(el.isStrictModeEnabled());
        el.setStrictModeEnabled(true);
        Assert.assertTrue(el.isStrictModeEnabled());

        Assert.assertTrue(el.excludesAnyOf("d/e/f"));
//      FIXME impl is wrong - this fails: Assert.assertTrue(el.excludesAnyOf("j/h/k"));
        Assert.assertTrue(el.excludesAnyOf("a/b/c"));

        ExcludeList.Entry abc = el.getEntry("a/b/c");
        Assert.assertEquals("", abc.getSynopsis());
        Assert.assertArrayEquals(new String[]{""}, abc.getPlatforms());
        Assert.assertArrayEquals(new String[]{"0"}, abc.getBugIdStrings());
        ExcludeList.Entry def = el.getEntry("d/e/f");
        Assert.assertArrayEquals(new String[]{"BUG-1234"}, def.getBugIdStrings());
        Assert.assertArrayEquals(new String[]{""}, def.getPlatforms());
        Assert.assertEquals("", def.getSynopsis());

        ExcludeList.Entry abcd = el.getEntry("a/b/c#d");
        Assert.assertArrayEquals(new String[]{"123"}, abcd.getBugIdStrings());
        Assert.assertArrayEquals(new String[]{"win32"}, abcd.getPlatforms());
        Assert.assertEquals("this is a synopsis", abcd.getSynopsis());

        ExcludeList.Entry abceg = el.getEntry("a/b/c#e[g]");
        Assert.assertArrayEquals(new String[]{"457"}, abceg.getBugIdStrings());
        Assert.assertArrayEquals(new String[]{"solaris"}, abceg.getPlatforms());
        Assert.assertEquals("descriptive text", abceg.getSynopsis());

        Assert.assertNull(el.getEntry("a/b/c#e"));


        Iterator<?> iterator = el.getIterator(false);
        List<String> urls = new ArrayList<>();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            urls.add(((ExcludeList.Entry) next).getRelativeURL());
        }
        Collections.sort(urls);
        Assert.assertEquals(6, urls.size());
        Assert.assertEquals("a/b/c"  ,urls.get(0));
        Assert.assertEquals("a/b/c#d",urls.get(1));
        Assert.assertEquals("a/b/c#e",urls.get(2));
        Assert.assertEquals("a/b/c#e",urls.get(3));
        Assert.assertEquals("d/e/f"  ,urls.get(4));
        Assert.assertEquals("j/h/k#t"  ,urls.get(5));
    }

    @Test
    public void reading_complexTCNames() throws IOException, ExcludeList.Fault {
        ExcludeList el = new ExcludeList(new File(data, "complex_testcase_names.jtx"));
        Assert.assertEquals(2, el.size());
        Assert.assertFalse(el.isEmpty());
        Assert.assertTrue(el.excludesAnyOf("a/b/c#group"));
        checkEntry(el.getEntry("a/b/c#group[case(0)]"), "BUG-123", "platform1", "description 1");
        checkEntry(el.getEntry("a/b/c#group", "case(0)"), "BUG-123", "platform1", "description 1");
        checkEntry(el.getEntry("a/b/c#group[case(0-4;6;800)]"), "BUG-456", "platform2", "description 2");
        checkEntry(el.getEntry("a/b/c#group", "case(0-4;6;800)"), "BUG-456", "platform2", "description 2");
    }

    private void checkEntry(ExcludeList.Entry entry, String bugid, String platform, String synopsys) {
        Assert.assertArrayEquals(new String[] {bugid}, entry.getBugIdStrings());
        Assert.assertArrayEquals(new String[] {platform}, entry.getPlatforms());
        Assert.assertEquals(synopsys, entry.getSynopsis());
    }

    @Test
    public void test1_1() throws ExcludeList.Fault {
        test1(true);
    }

    @Test
    public void test1_2() throws ExcludeList.Fault {
        test1(false);
    }

    @Test
    public void test2_1() throws ExcludeList.Fault {
        test2(true);
    }

    @Test
    public void test2_2() throws ExcludeList.Fault {
        test2(false);
    }

    void test1(boolean strict) throws ExcludeList.Fault {
        ExcludeList t = new ExcludeList();
        t.setStrictModeEnabled(strict);
        testAdd(t, "a/b/c", true);
        testAdd(t, "d/e/f", true);
        testAdd(t, "a/b/c#d 123 win32 this is a synopsis", true);
        testAdd(t, "a/b/c#e[f] 456 solaris descriptive text", true);
        testAdd(t, "a/b/c#e[g] 457 solaris descriptive text", true);
        testAdd(t, "a/b/c#e 457 solaris can't exclude test if test cases are excluded", !strict);
        testAdd(t, "a/b/c#d[x] 457 can't exclude test case if test is excluded", !strict);
    }

    @Test
    public void testRemoveUnexisting() throws ExcludeList.Fault {
        ExcludeList t = new ExcludeList();
        t.setStrictModeEnabled(true);
        ExcludeList.Entry entry = ExcludeList.Entry.read("a/b/c");
        t.addEntry(entry);
        testRemove(t, entry, true);
    }


    void test2(boolean strict) {
        testCreate("test1.jtx", true, strict);
        testCreate("test2.jtx", !strict, strict);
        testCreate("test3.jtx", !strict, strict);
        testCreate("test3.jtx", !strict, strict);
    }

    @Test
    public void test3()
            throws IOException, ExcludeList.Fault {
        perform("basic access methods");
        ExcludeList t = new ExcludeList(new File(data, "test1.jtx"));
        t.setTitle("Exclude Test Title");
        if (!t.getTitle().equals("Exclude Test Title")) {
            error("Error in set/get Title");
        }

        if (!t.excludesAllOf("a/b/c#d") || !t.excludesAnyOf("a/b/c#e")) {
            error("Problems with excludesAny/AllOf methods");
        }

        if (t.excludesAllOf("a/b/c#e") || t.excludesAnyOf("a/f/r")) {
            error("Problems with excludesAny/AllOf methods");
        }

        ExcludeList.Entry e = t.getEntry("a/b/c#d");
        if (e == null) {
            error("getEntry() failed");
        }

    }

    void testAdd(ExcludeList t, String s, boolean expectOK)
            throws ExcludeList.Fault {
        testAdd(t, ExcludeList.Entry.read(s), expectOK);
    }

    void testAdd(ExcludeList t, ExcludeList.Entry e, boolean expectOK) {
        perform("adding " + e.getRelativeURL());
        try {
            t.addEntry(e);
            if (!expectOK) {
                errorNoFault();
            }
        } catch (ExcludeList.Fault f) {
            if (expectOK) {
                errorFault(f);
            }
        }
    }

    void testCreate(String name, boolean expectOK, boolean strict) {
        perform("creating new table from " + name);
        try {
            ExcludeList e = new ExcludeList(new File(data, name), strict);
            if (!expectOK) {
                errorNoFault();
            }
        } catch (ExcludeList.Fault e) {
            if (expectOK) {
                errorFault(e);
            }
        } catch (IOException e) {
            if (expectOK) {
                errorFault(e);
            }
        }
    }

    void testRemove(ExcludeList t, ExcludeList.Entry e, boolean expectOK) {
        perform("adding " + e.getRelativeURL());
        ExcludeList.Entry e2 = t.getEntry(e.getRelativeURL(), e.getTestCases());
        t.removeEntry(e2);
        if (!expectOK) {
            errorNoFault();
        }
    }

    private void perform(String s) {
        currTest = s;
        if (verbose) {
            System.err.println(s);
        }
    }

    private void errorFault(Throwable t) {
        error("error: " + currTest + ": unexpected exception: " + t);
    }

    private void errorNoFault() {
        error("error: " + currTest + ": exception not thrown");
    }

    private void error(String s) {
        Assert.fail(s);
    }

}
