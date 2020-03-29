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

import java.io.PrintStream;
import java.util.*;

import org.junit.Assert;
import org.junit.Test;

public class KeyTest {

    @Test
    public void test() {
        KeyTest t = new KeyTest();
        boolean ok = t.run(System.out);
        Assert.assertTrue(ok);
    }

    public boolean run(PrintStream out) {
        this.out = out;

        try {
            int nTables = 256;
            List<HashSet<String>> tables = new ArrayList<>();
            for (int i = 0; i < nTables; i++) {
                HashSet<String> t = new HashSet<>();
                String k = "type" + (char) ('0' + (i & 7));
                t.add(k);
                for (int j = 0; j < 10; j++)
                    if ((i & 1 << j + 3) != 0) {
                        k = "" + (char) ('a' + j);
                        t.add(k);
                    }
                tables.add(t);
            }

            Keywords k;

            k = Keywords.create("all of", "type0 a");
            out.println("KEYS:" + k);
            for (HashSet<String> t : tables) {
                check(k, t, t.contains("type0") && t.contains("a"));
            }

            k = Keywords.create("any of", "b type1 c");
            out.println("KEYS:" + k);
            for (HashSet<String> t : tables) {
                check(k, t, t.contains("b") || t.contains("type1") || t.contains("c"));
            }

            k = Keywords.create("expr", "d");
            out.println("KEYS:" + k);
            for (HashSet<String> t : tables) {
                check(k, t, t.contains("d"));
            }

            k = Keywords.create("expr", "a  &b");
            out.println("KEYS:" + k);
            for (HashSet<String> t : tables) {
                check(k, t, t.contains("a") && t.contains("b"));
            }

            k = Keywords.create("expr", "a & !b");
            out.println("KEYS:" + k);
            for (HashSet<String> t : tables) {
                check(k, t, t.contains("a") && !t.contains("b"));
            }

            k = Keywords.create("expr", "a & b | c");
            out.println("KEYS:" + k);
            for (HashSet<String> t : tables) {
                check(k, t, t.contains("a") && t.contains("b") || t.contains("c"));
            }

            k = Keywords.create("expr", "a | b & c");
            out.println("KEYS:" + k);
            for (HashSet<String> t : tables) {
                check(k, t, t.contains("a") || t.contains("b") && t.contains("c"));
            }

            k = Keywords.create("expr", "(a | b) & c");
            out.println("KEYS:" + k);
            for (HashSet<String> t : tables) {
                check(k, t, (t.contains("a") || t.contains("b")) && t.contains("c"));
            }

            k = Keywords.create("expr", "  a  & !(type0 | b) & c   ");
            out.println("KEYS:" + k);
            for (HashSet<String> t : tables) {
                check(k, t, t.contains("a") &&
                        !(t.contains("type0") || t.contains("b")) &&
                        t.contains("c"));
            }

            return ok;
        } catch (Keywords.Fault e) {
            out.println(e.toString());
            return false;
        }
    }

    private void check(Keywords k, Set<String> s, boolean expected) {
        boolean actual = k.accepts(s);
        if (actual != expected) {
            out.println("mismatch");
            out.println("Keywords: " + k);
            out.print("Table: ");
            for (String value : s) out.print(" " + value);
            out.print("\n\n");
            ok = false;
        }
    }

    private boolean ok = true;
    private PrintStream out;
}

