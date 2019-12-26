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

import java.io.PrintStream;

public class PathConformTests {

    @Test
    public void test() {
        PathConformTests st = new PathConformTests();
        boolean ok = st.run(System.out);
        Assert.assertTrue(ok);
    }


    public boolean run(PrintStream out) {
        boolean ok = true;

        if (!relativePathCheck()) {
            failed();
            return false;
        }

        return true;
    }

    private void failed() {
        System.out.println("Path conformance tests failed...");
    }

    private boolean relativePathCheck() {
        // test the single URL version
        String url = "api/java_applet/Applet/index.html#NewAudioClip";
        String result = TestResult.getWorkRelativePath(url);

        if (!result.equals("api/java_applet/Applet/index_NewAudioClip.jtr")) {
            System.out.println("Direct URL+ID conversion failed.");
            System.out.println(url + " -> " + result);
            return false;
        }

        url = "api/java_applet/Applet/index.html";
        result = TestResult.getWorkRelativePath(url);
        if (!result.equals("api/java_applet/Applet/index.jtr")) {
            System.out.println("Direct URL conversion failed.");
            System.out.println(url + " -> " + result);
            return false;
        }

        url = "api/java_applet/Applet/somefile";
        result = TestResult.getWorkRelativePath(url);
        if (!result.equals("api/java_applet/Applet/somefile.jtr")) {
            System.out.println("Direct URL conversion failed.");
            System.out.println(url + " -> " + result);
            return false;
        }

        // check the two argument version
        String id = "id4";
        result = TestResult.getWorkRelativePath(url, id);
        if (!result.equals("api/java_applet/Applet/somefile_id4.jtr")) {
            System.out.println("Two param. URL+ID conversion failed.");
            System.out.println(url + "+" + id + " -> " + result);
            return false;
        }

        url = "api/java_applet/Applet/index.html";
        id = null;
        result = TestResult.getWorkRelativePath(url, id);
        if (!result.equals("api/java_applet/Applet/index.jtr")) {
            System.out.println("Two param. URL+null-ID conversion failed.");
            System.out.println(url + "+" + id + " -> " + result);
            return false;
        }

        // check processing of URLs that contain periods in a directory name
        url = "api/javax_naming/spi/DirStateFactory.Result/index.html#Constructor";
        id = null;
        result = TestResult.getWorkRelativePath(url);
        if (!result.equals("api/javax_naming/spi/DirStateFactory.Result/index_Constructor.jtr")) {
            System.out.println("Single param. URL with dotted dir - ID conversion failed.");
            System.out.println(url + " -> " + result);
            return false;
        }

        url = "api/javax_naming/spi/DirStateFactory.Result/index.html";
        id = "Constructor";
        result = TestResult.getWorkRelativePath(url, id);
        if (!result.equals("api/javax_naming/spi/DirStateFactory.Result/index_Constructor.jtr")) {
            System.out.println("Two param. URL with dotted dir - ID conversion failed.");
            System.out.println(url + "+" + id + " -> " + result);
            return false;
        }

        // should test the TD version...

        return true;
    }
}
