/*
 * $Id$
 *
 * Copyright (c) 1996, 2019, Oracle and/or its affiliates. All rights reserved.
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

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TestPropertyUtils {

    @Test(expected = NullPointerException.class)
    public void testStoreNulls() throws IOException {
        PropertyUtils.store(null, null, null);
    }


    @Test()
    public void testStroreLoadEmpty() throws IOException {
        Map<String, String> props = new HashMap<>();
        saveLoadCheck(props);
    }

    @Test()
    public void testStoreLoadOneEntry() throws IOException {

        Map<String, String> props = new HashMap<>();
        props.put("key1", "val1");

        saveLoadCheck(props);
    }

    @Test()
    public void testStoreLoadSeveralEntries() throws IOException {
        Map<String, String> props = new HashMap<>();
        props.put("key1", "val1");
        props.put("key2", "val2");
        props.put("key3", "val3");
        saveLoadCheck(props);
    }

    @Test()
    public void testStoreLoadSeveralEntries_01() throws IOException {
        Map<String, String> props = new HashMap<>();
        props.put("key1", "va l1      sef  ::SDF        ");
        props.put("key2", "val2                 ");
        props.put("key3", "val3           ; asdfp !@$%^&*(__=+-~)");
        saveLoadCheck(props);
    }


    @Test()
    public void testStoreLoadSeveralEntriesEmptyProps() throws IOException {
        Map<String, String> props = new HashMap<>();
        props.put("key1", "");
        props.put("key2", "");
        props.put("key3", "");
        saveLoadCheck(props);
    }

    @Test()
    public void testStoreLoadSeveralEntriesSpaceValues() throws IOException {
        Map<String, String> props = new HashMap<>();
        props.put("key1", "          ");
        props.put("key2", "                      ");
        props.put("key3", "a                                       ");
        saveLoadCheck(props);
    }

    @Test()
    public void testStoreLoadSeveralEntriesUnicode() throws IOException {
        Map<String, String> props = new HashMap<>();
        props.put("ke\u3424y1", "\u3443");
        props.put("key2", " \uefff");
        props.put("\ueabc\ueabc\ueabc", "\ueabc");
        saveLoadCheck(props);
    }

    @Test()
    public void emptyKeu() throws IOException {
        Map<String, String> props = new HashMap<>();
        props.put("", "amnbsdfm k gkajsdg f");
        props.put("key2", " \uefff");
        props.put("\ueabc\ueabc\ueabc", "\ueabc");
        saveLoadCheck(props);
    }

    private void saveLoadCheck(Map<String, String> props) throws IOException {

        Path tempFile = Files.createTempFile("", "");
        File f = tempFile.toFile();
        f.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(f);
             OutputStream out = new BufferedOutputStream(fos)) {
            PropertyUtils.store(props, out, "Test props file");
        }

        Map<String, String> loaded;
        try (InputStream in = new BufferedInputStream(new FileInputStream(f))) {
            loaded = PropertyUtils.load(in);
        }
        Assert.assertEquals(props, loaded);

        loaded.clear();

        try (Reader in = new FileReader(f)) {
            loaded = PropertyUtils.load(in);
        }
        Assert.assertEquals(props, loaded);

    }

}
