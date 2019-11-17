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

import com.sun.javatest.util.PropertyUtils;
import com.sun.javatest.util.StringArray;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TestEnvTest {

    @Test
    public void test() {
        TestEnvTest t = new TestEnvTest();
        boolean ok = t.run(System.out);
         Assert.assertTrue(ok);
    }

    public boolean run(PrintStream log) {
        this.log = log;
        test("E1", E1_env, E1_ref);
        test("E2", E2_env, E2_ref);
        test("E3", E3_env_sol, E3_ref_sol);
        test("E3", E3_env_WinNT, E3_ref_WinNT);
        test("E3", E3_env_Win95, E3_ref_Win95);
        test("EQ", EQ_env, EQ_ref);
        test("EM", EM_env, EM_ref);
        testLookupFault("Ebad", Ebad_env, "simpleLoop");
        testLookupFault("Ebad", Ebad_env, "multiLoop");
        testLookupFault("Ebad", Ebad_env, "indirectLoop");
        testLookupFault("Ebad", Ebad_env, "mapLoop");
        System.setProperty("srcDir", "unit-tests/com/sun/javatest");
        testFile("Ef1", "TestEnvTest1.jte", Ef1_ref);
        log.flush();
        return ok;
    }


    private void test(String name, String[][][] env, String[][] ref) {
        log.println("*****TEST***** " + name);
        Map<String, String> ep = new HashMap<>();
        for (String[][] anEnv : env) {
            load(ep, anEnv);
        }
        Map<String, String> rp = new HashMap<>();
        load(rp, ref);

        compare(name, ep, rp);
    }

    private void testFile(String name, String file, String[][] ref) {
        log.println("*****TEST file***** " + name + ": " + file);
        Map<String, String> ep = new HashMap<>();
        try {
            String dir = System.getProperty("srcDir", System.getProperty("user.dir"));
            Reader in = new BufferedReader(new FileReader(new File(dir, file)));
            ep = PropertyUtils.load(in);
        } catch (IOException e) {
            log.println("exception occurred: " + e);
            ok = false;
            return;
        }

        Map<String, String> rp = new HashMap<>();
        load(rp, ref);

        compare(name, ep, rp);
    }

    private void testLookupFault(String name, String[][][] env, String entryName) {
        log.println("*****TEST lookup fault***** " + name + ": " + entryName);
        Map<String, String> ep = new HashMap<>();
        for (String[][] anEnv : env) {
            load(ep, anEnv);
        }

        TestEnvironment te;
        try {
            te = new TestEnvironment(name, ep, "test properties");
        } catch (TestEnvironment.Fault e) {
            log.println("exception occurred: " + e.getClass().getName());
            log.println(e.getMessage());
            return;
        }

        try {
            String[] v = te.lookup(entryName);
            log.println("lookup unexpectedly succeeded");
            ok = false;
        } catch (TestEnvironment.Fault e) {
            // expected
        }
    }

    private void compare(String name, Map<String, String> ep, Map<String, String> rp) {
        try {
            TestEnvironment te = new TestEnvironment(name, ep, "test properties");
            for (String s : te.keys()) {
                try {
                    String k = s;
                    log.println("Checking key `" + k + "' ...");
                    String[] v = te.lookup(k);
                    String vj = join(v);
                    log.println("...Value is `" + vj + "'");
                    String r = rp.get(k);
                    if (vj == null && r == null ||
                            vj != null && r != null && r.equals(vj)) {
                        rp.remove(k);
                    } else {
                        log.println("...MISMATCH; expected `" + r + "'");
                        ok = false;
                    }
                } catch (TestEnvironment.Fault ex) {
                    log.println("...BAD ARGS: " + ex);
                    ok = false;
                }
            }
            if (rp.size() != 0) {
                log.println("ERROR: unmatched entries in reference list");
                for (Iterator<?> i = rp.keySet().iterator(); i.hasNext(); ) {
                    String k = (String) i.next();
                    String r = rp.get(k);
                    log.println("UNMATCHED: `" + k + "' = `" + r + "'");
                }
                ok = false;
            }
        } catch (TestEnvironment.Fault e) {
            log.println("exception occurred: " + e);
            ok = false;
        }
    }

    private void load(Map<String, String> p, String[][] data) {
        for (String[] aData : data) p.put(aData[0], aData[1]);
    }

    private String join(String[] ss) {
        if (ss == null || ss.length == 0)
            return "";

        String s = ss[0];
        for (int i = 1; i < ss.length; i++)
            s += SEP + ss[i];

        return s;
    }

    private boolean ok = true;
    private PrintStream log;

    private static final String SEP = "\u0000";

    // First define some collections of name value pairs to compose
    // into environments

    private static final String[][] e_os_sol = {
            {"os", "Solaris"}
    };

    private static final String[][] e_os_WinNT = {
            {"os", "Windows NT"}
    };

    private static final String[][] e_os_Win95 = {
            {"os", "Windows 95"}
    };

    private static final String[][] e_base = {
            {"base.1", "BASE.1"},
            {"base.2", "BASE.2"},
            {"home", "${home.$os}"},
            {"home.Solaris", "solaris home"},
            {"home.Windows_NT", "WinNT home"},
            {"home.Windows_95", "Win95 home"},
    };

    private static final String[][] e_E1 = {
            {"env.E1.e11", "E1"},
            {"env.E1.e12", "${base.0}"},
            {"env.E1.e13", "${base.1}"},
            {"env.E1.e14", "abc ${base.1} def"},
            {"env.E1.e15", "abc${base.1}def"}
    };

    private static final String[][] e_E2 = {
            {"env.E2.inherits", "E1"},
            {"env.E2.e13", "${base.1}${base.2}"},
            {"env.E2.e21", "${base.2}${base.1}"}
    };

    private static final String[][] e_E3 = {
            {"env.E3.inherits", "E2"},
            {"env.E3.e31", "${home}"}
    };

    private static final String[][] e_EX = {
            {"env.E0.e1", "E1"},
            {"env.E11.e2", "${base.0}"},
            {"env.E111.e3", "${base.1}"},
            {"env.E9.e4", "abc ${base.1} def"},
            {"env.E9.e5", "abc${base.1}def"}
    };

    // Define some collections of collections of name value pairs to use
    // as the data for environments, and define reference results for lookup calls

    private static final String[][][] E1_env = {
            e_base, e_E1, e_EX
    };


    private static final String[][] E1_ref = {
            {"base.1", "BASE.1"},
            {"base.2", "BASE.2"},
            {"home", ""},
            {"home.Solaris", "solaris" + SEP + "home"},
            {"home.Windows_NT", "WinNT" + SEP + "home"},
            {"home.Windows_95", "Win95" + SEP + "home"},
            {"e11", "E1"},
            {"e12", ""},
            {"e13", "BASE.1"},
            {"e14", "abc" + SEP + "BASE.1" + SEP + "def"},
            {"e15", "abcBASE.1def"}
    };

    private static final String[][][] E2_env = {
            e_base, e_E1, e_EX, e_E2
    };

    private static final String[][] E2_ref = {
            {"base.1", "BASE.1"},
            {"base.2", "BASE.2"},
            {"home", ""},
            {"home.Solaris", "solaris" + SEP + "home"},
            {"home.Windows_NT", "WinNT" + SEP + "home"},
            {"home.Windows_95", "Win95" + SEP + "home"},
            {"inherits", "E1"},
            {"e11", "E1"},
            {"e12", ""},
            {"e13", "BASE.1BASE.2"},
            {"e14", "abc" + SEP + "BASE.1" + SEP + "def"},
            {"e15", "abcBASE.1def"},
            {"e21", "BASE.2BASE.1"}
    };

    private static final String[][][] E3_env_sol = {
            e_os_sol, e_base, e_E1, e_EX, e_E2, e_E3
    };

    private static final String[][] E3_ref_sol = {
            {"os", "Solaris"},
            {"base.1", "BASE.1"},
            {"base.2", "BASE.2"},
            {"home", "solaris" + SEP + "home"},
            {"home.Solaris", "solaris" + SEP + "home"},
            {"home.Windows_NT", "WinNT" + SEP + "home"},
            {"home.Windows_95", "Win95" + SEP + "home"},
            {"inherits", "E2"},
            {"e11", "E1"},
            {"e12", ""},
            {"e13", "BASE.1BASE.2"},
            {"e14", "abc" + SEP + "BASE.1" + SEP + "def"},
            {"e15", "abcBASE.1def"},
            {"e21", "BASE.2BASE.1"},
            {"e31", "solaris" + SEP + "home"}
    };

    private static final String[][][] E3_env_WinNT = {
            e_os_WinNT, e_base, e_E1, e_EX, e_E2, e_E3
    };

    private static final String[][] E3_ref_WinNT = {
            {"os", "Windows" + SEP + "NT"},
            {"base.1", "BASE.1"},
            {"base.2", "BASE.2"},
            {"home", "WinNT" + SEP + "home"},
            {"home.Solaris", "solaris" + SEP + "home"},
            {"home.Windows_NT", "WinNT" + SEP + "home"},
            {"home.Windows_95", "Win95" + SEP + "home"},
            {"inherits", "E2"},
            {"e11", "E1"},
            {"e12", ""},
            {"e13", "BASE.1BASE.2"},
            {"e14", "abc" + SEP + "BASE.1" + SEP + "def"},
            {"e15", "abcBASE.1def"},
            {"e21", "BASE.2BASE.1"},
            {"e31", "WinNT" + SEP + "home"}
    };

    private static final String[][][] E3_env_Win95 = {
            e_os_Win95, e_base, e_E1, e_EX, e_E2, e_E3
    };

    private static final String[][] E3_ref_Win95 = {
            {"os", "Windows" + SEP + "95"},
            {"base.1", "BASE.1"},
            {"base.2", "BASE.2"},
            {"home", "Win95" + SEP + "home"},
            {"home.Solaris", "solaris" + SEP + "home"},
            {"home.Windows_NT", "WinNT" + SEP + "home"},
            {"home.Windows_95", "Win95" + SEP + "home"},
            {"inherits", "E2"},
            {"e11", "E1"},
            {"e12", ""},
            {"e13", "BASE.1BASE.2"},
            {"e14", "abc" + SEP + "BASE.1" + SEP + "def"},
            {"e15", "abcBASE.1def"},
            {"e21", "BASE.2BASE.1"},
            {"e31", "Win95" + SEP + "home"}
    };

    private static final String[][][] EQ_env = {
            {
                    {"env.EQ.a", "a b \"c d\" e f"},
                    {"env.EQ.b", "a b \'c d\' e f"},
                    {"env.EQ.c", "a b \"c ' d\" e f"},
                    {"env.EQ.d", "a b \'c \" d\' e f"},
                    {"env.EQ.e", "$a $b $c $d"},
                    {"env.EQ.f", "a a\"b b\"'c c'\"d d\"e e"},
                    {"env.EQ.g", "aaa\"bbb $a ccc\"ddd"},
                    {"env.EQ.h", "aaa'bbb $a ccc'ddd"},
            }
    };

    private static final String[][] EQ_ref = {
            {"a", "a" + SEP + "b" + SEP + "c d" + SEP + "e" + SEP + "f"},
            {"b", "a" + SEP + "b" + SEP + "c d" + SEP + "e" + SEP + "f"},
            {"c", "a" + SEP + "b" + SEP + "c ' d" + SEP + "e" + SEP + "f"},
            {"d", "a" + SEP + "b" + SEP + "c \" d" + SEP + "e" + SEP + "f"},
            {"e", "a" + SEP + "b" + SEP + "c d" + SEP + "e" + SEP + "f" + SEP + "a" + SEP + "b" + SEP + "c d" + SEP + "e" + SEP + "f" + SEP + "a" + SEP + "b" + SEP + "c ' d" + SEP + "e" + SEP + "f" + SEP + "a" + SEP + "b" + SEP + "c \" d" + SEP + "e" + SEP + "f"},
            {"f", "a" + SEP + "ab bc cd de" + SEP + "e"},
            {"g", "aaabbb a b c d e f cccddd"},
            {"h", "aaabbb $a cccddd"},
    };

    private static final String[][][] EM_env = {
            {
                    {"env.EM.m1", "${mx}"},
                    {"env.EM.m2", "${mx:FS=%}"},
                    {"env.EM.m3", "${mx:PS=@}"},
                    {"env.EM.m4", "${mx:MAP}"},
                    {"env.EM.m5", "${mx:MAP=abc}"},
                    {"env.EM.m6", "${mx: PS=@ MAP=abc FS=%}"},
                    {"env.EM.m7", "${mx: PS=; MAP=abc FS=\\}"},
                    {"env.EM.m8", "${mx} ${mx:FS=%} ${mx:PS=@} ${mx:MAP=abc} ${mx:PS=@ MAP=abc FS=%}"},
                    {"env.EM.mx", "/home/gzilla/sqe-tools-jt21dev/classes:/home/gzilla/sqe-tools-jt21dev/javatest.jar"},
                    {"env.EM.map", "/home/gzilla/ G:\\"},
                    {"env.EM.map.abc", "/home/ G:\\ gzilla ribbit"}
            }
    };

    private static final String[][] EM_ref = {
            {"m1", "/home/gzilla/sqe-tools-jt21dev/classes:/home/gzilla/sqe-tools-jt21dev/javatest.jar"},
            {"m2", "%home%gzilla%sqe-tools-jt21dev%classes:%home%gzilla%sqe-tools-jt21dev%javatest.jar"},
            {"m3", "/home/gzilla/sqe-tools-jt21dev/classes@/home/gzilla/sqe-tools-jt21dev/javatest.jar"},
            {"m4", "G:\\sqe-tools-jt21dev/classes:G:\\sqe-tools-jt21dev/javatest.jar"},
            {"m5", "G:\\ribbit/sqe-tools-jt21dev/classes:G:\\ribbit/sqe-tools-jt21dev/javatest.jar"},
            {"m6", "G:\\ribbit%sqe-tools-jt21dev%classes@G:\\ribbit%sqe-tools-jt21dev%javatest.jar"},
            {"m7", "G:\\ribbit\\sqe-tools-jt21dev\\classes;G:\\ribbit\\sqe-tools-jt21dev\\javatest.jar"},
            {"m8", "/home/gzilla/sqe-tools-jt21dev/classes:/home/gzilla/sqe-tools-jt21dev/javatest.jar" + SEP + "%home%gzilla%sqe-tools-jt21dev%classes:%home%gzilla%sqe-tools-jt21dev%javatest.jar" + SEP + "/home/gzilla/sqe-tools-jt21dev/classes@/home/gzilla/sqe-tools-jt21dev/javatest.jar" + SEP + "G:\\ribbit/sqe-tools-jt21dev/classes:G:\\ribbit/sqe-tools-jt21dev/javatest.jar" + SEP + "G:\\ribbit%sqe-tools-jt21dev%classes@G:\\ribbit%sqe-tools-jt21dev%javatest.jar"},
            {"mx", "/home/gzilla/sqe-tools-jt21dev/classes:/home/gzilla/sqe-tools-jt21dev/javatest.jar"},
            {"map", "/home/gzilla/" + SEP + "G:\\"},
            {"map.abc", "/home/" + SEP + "G:\\" + SEP + "gzilla" + SEP + "ribbit"}
    };

    private static final String[][][] Ebad_env = {
            {
                    {"env.Ebad.simpleLoop", "$simpleLoop"},
                    {"env.Ebad.multiLoop", " a b c ${multiLoop1} d e f"},
                    {"env.Ebad.multiLoop1", " a b c ${multiLoop2} d e f"},
                    {"env.Ebad.multiLoop2", " a b c ${multiLoop3} d e f"},
                    {"env.Ebad.multiLoop3", " a b c ${multiLoop} d e f"},
                    {"env.Ebad.indirectLoop", "1 2 3 ${simpleLoop} 4 5 6"},
                    {"env.Ebad.mapLoop", " 1 2 3 ${x:MAP} 4 5 6"},
                    {"env.Ebad.map", "simple ${simpleLoop}"}
            }
    };

    private static final String[][] Ef1_ref = {
            {"alphabet", "a" + SEP + "b" + SEP + "c" + SEP + "d" + SEP + "e" + SEP + "f" + SEP + "gg" + SEP + "h" + SEP + "i" + SEP + "j" + SEP + "k" + SEP + "ll" + SEP + "mm" + SEP + "n" + SEP + "o"}
    };


}
