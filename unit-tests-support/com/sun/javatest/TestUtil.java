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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Test utilities, helper methods.
 */
public class TestUtil {

    public static String getPathToTestTestSuite(String... subComponents) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("suites");
        strings.addAll(Arrays.asList(subComponents));
        return getPathToData(strings.toArray(new String[strings.size()]));
    }

    public static String getPathToData(String... subComponents) {
        String property = System.getProperty("unit-tests.data.dir");
        for (String subdir : subComponents) {
            property += File.separator + subdir;
        }
        return property;
    }

    public static Path createTempDirectory(String prefix) throws IOException {
        return Files.createTempDirectory(getTmpPath(), prefix);
    }

    public static String createTempDirAndReturnAbsPathString(String prefix) throws IOException {
        return createTempDirectory(prefix).toAbsolutePath().toString();
    }

    protected static Path getTmpPath() {
        return Paths.get(System.getProperty("build.tmp")).toAbsolutePath().normalize();
    }
}
