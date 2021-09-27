/*
 * $Id$
 *
 * Copyright (c) 2001, 2021, Oracle and/or its affiliates. All rights reserved.
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


package jthtest.nongui;

import jthtest.NonguiTest;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class PropertiesTest1 extends NonguiTest {
    // should always give a : 1; b : 2; c : 3
    String tests1[] = {"a=1\nb:2\nc 3\n", "a=1\r\nb:2\r\nc 3\r\n", "a=1\r\nb:2\nc 3\r\n", "a =1\nb :2\nc  3\n", "a= 1\nb: 2\nc  3\n", "a = 1\nb : 2\nc   3\n", "a \t =\f1\nb\t\t :\f2\nc\t\t3\n"};
    // should give a\b : 1; a=b : 2; c : =3; ac : 4;
    // a\c should be null
    String test2 = "a\\\\b=1\na\\=b=2\nc==3\na\\c=4";

    public void testImpl() throws Exception {
        test1();
        test2();
    }

    private void test2() throws Exception {
        Properties p = new Properties();
        p.load(new StringReader(test2));
        boolean b = false;
        if (!"1".equals(p.getProperty("a\\b"))) {
            if (!b) {
                b = true;
                errors.add("Error in test string '" + test2 + "' :\n");
            }
            errors.add("a\\b == " + p.getProperty("a\\b") + " (should be 1)");
        }
        if (!"2".equals(p.getProperty("a=b"))) {
            if (!b) {
                b = true;
                errors.add("Error in test string '" + test2 + "' :\n");
            }
            errors.add("a=b == " + p.getProperty("a=b") + " (should be 2)");
        }
        if (!"=3".equals(p.getProperty("c"))) {
            if (!b) {
                b = true;
                errors.add("Error in test string '" + test2 + "' :\n");
            }
            errors.add("c == " + p.getProperty("c") + " (should be =3)");
        }
        if (!"4".equals(p.getProperty("ac"))) {
            if (!b) {
                b = true;
                errors.add("Error in test string '" + test2 + "' :\n");
            }
            errors.add("a\\b == " + p.getProperty("a\\b") + " (should be 1)");
        }
        if (p.getProperty("a\\c") != null) {
            if (!b) {
                b = true;
                errors.add("Error in test string '" + test2 + "' :\n");
            }
            errors.add("a\\c == " + p.getProperty("a\\c") + " (should be null)");
        }
    }

    private void test1() throws IOException {
        for (String s : tests1) {
            Properties p = new Properties();
            p.load(new StringReader(s));
            boolean b = false;
            if (!"1".equals(p.getProperty("a"))) {
                if (!b) {
                    errors.add("Error in test string '" + s + "' :\n");
                    b = true;
                }
                errors.add("a == '" + p.getProperty("a") + "' ('1' expected)\n");
            }
            if (!"2".equals(p.getProperty("b"))) {
                if (!b) {
                    errors.add("Error in test string '" + s + "' :\n");
                    b = true;
                }
                errors.add("b == '" + p.getProperty("b") + "' ('2' expected)\n");
            }
            if (!"3".equals(p.getProperty("c"))) {
                if (!b) {
                    errors.add("Error in test string '" + s + "' :\n");
                    b = true;
                }
                errors.add("c == '" + p.getProperty("c") + "' ('3' expected)\n");
            }
        }
    }

}
