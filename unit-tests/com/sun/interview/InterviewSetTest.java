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

package com.sun.interview;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class InterviewSetTest {


    @Test
    public void test() throws Exception {
        InterviewSetTest t = new InterviewSetTest();
        boolean ok = t.run();
        Assert.assertTrue(ok);
    }


    boolean run() throws Exception {
        boolean ok = true;

        for (String test : tests) {
            if (!test(test))
                ok = false;
        }

        return ok;
    }

    String[] tests = {
            "-c A a -c B b -c C c               : a.a b.b c.c",
            "-c A a -c B b -c C c -a b c        : a.a c.c b.b",
            "-c A a -c B b -c C c -a a b        : b.b a.a c.c",
            "-c A a -c B b -c C c -a a c -a c b : b.b c.c a.a",
            "-c A a -c B b -c C c -a a b -a a c : b.b c.c a.a",
            "-c A a -c B b -c C c -a a c -a a b : b.b c.c a.a",
            "-c A a -c B b -c C c -a a b -a b c : c.c b.b a.a",
            "-c A a -c B b -c C c -a a c        : c.c a.a b.b",
            "-c A a -c B b -c C c -c A d -a a c : c.c a.a b.b d.a",
            "-c A a -c B b -c C c -c A d -a a c -a b d : c.c a.a d.a b.b",
            "-c A a -c B b -c C c -c A d -a b c -a b d : a.a c.c d.a b.b",
    };

    boolean test(String data) throws Exception {
        int sep = data.indexOf(":");
        String[] args = data.substring(0, sep).trim().split("  *");
        String[] expect = data.substring(sep + 1).trim().split("  *");

        TestInterview t = new TestInterview(args);
        Question[] path = t.getPath();
        if (path.length != expect.length + 2) {
            error(data, path, "wrong length for path; expected: " + (expect.length + 2) + "; found: " + path.length);
            return false;
        }

        for (int i = 0; i < path.length; i++) {
            Question q = path[i];
            String tag = q.getTag();
            String e = i == 0 ? "test.first"
                    : i == path.length - 1 ? "test.last"
                    : "test.set." + expect[i - 1];

            if (!tag.equals(e)) {
                error(data, path, "wrong entry for path[" + i + "]; expected: " + e + "; found: " + tag);
                return false;
            }
        }

        return true;
    }

    void error(String data, Question[] path, String message) {
        System.err.println("Error: " + message);
        System.err.println("Data: " + data);
        for (int i = 0; i < path.length; i++)
            System.err.println(" path[" + i + "] " + path[i].getTag());
    }

    private static Class<?> getInterviewClass(String name) throws Exception {
        return Class.forName(InterviewSetTest.class.getName() + "$" + name + "Interview");
    }

    private static class TestInterview extends Interview {
        TestInterview(String[] args) throws Exception {
            super("test");
            iSet = new SetInterview(this, args);
            setFirstQuestion(qFirst);
        }

        private NullQuestion qFirst = new NullQuestion(this, "first") {
            public Question getNext() {
                return callInterview(iSet, qLast);
            }
        };

        private FinalQuestion qLast = new FinalQuestion(this, "last");

        private SetInterview iSet;
    }


    private static class SetInterview extends InterviewSet {
        SetInterview(Interview parent, String[] args) throws Exception {
            super(parent, "set");

            for (int i = 0; i < args.length; i++) {
                //System.err.println(i + ": " + args[i]);
                String arg = args[i];
                if ((arg.equals("-c") || arg.equals("-create"))
                        && i + 2 < args.length) {
                    create(args[i + 1], args[i + 2]);
                    i += 2;
                } else if ((arg.equals("-a") || arg.equals("-add"))
                        && i + 2 < args.length) {
                    add(args[i + 1], args[i + 2]);
                    i += 2;
                } else if ((arg.equals("-r") || arg.equals("-remove"))
                        && i + 2 < args.length) {
                    remove(args[i + 1], args[i + 2]);
                    i += 2;
                } else
                    throw new Exception("bad arg: " + arg);
            }
        }

        private void create(String type, String name) throws Exception {
            //System.err.println("create " + type + " " + name);
            Constructor<?> constr = getInterviewClass(type).getConstructor(Interview.class, String.class);
            Interview i = (Interview) constr.newInstance(new Object[]{this, name});
            children.put(name, i);
        }

        private void add(String entName, String encyName) throws Exception {
            //System.err.println("add " + entName + " " + encyName);
            Interview ent = children.get(entName);
            Interview ency = children.get(encyName);
            addDependency(ent, ency);
        }

        private void remove(String entName, String encyName) {
            //System.err.println("remove " + entName + " " + encyName);
            Interview ent = children.get(entName);
            Interview ency = children.get(encyName);
            removeDependency(ent, ency);
        }

        private Map<String, Interview> children = new HashMap<>();
    }

    static class AInterview extends Interview {
        public AInterview(Interview parent, String tag) {
            super(parent, tag);
            setFirstQuestion(qString);
        }

        Question qString = new StringQuestion(this, "a") {
            public Question getNext() {
                return qEnd;
            }
        };

        Question qEnd = new FinalQuestion(this);
    }

    static class BInterview extends Interview {
        public BInterview(Interview parent, String tag) {
            super(parent, tag);
            setFirstQuestion(qString);
        }

        Question qString = new StringQuestion(this, "b") {
            public Question getNext() {
                return qEnd;
            }
        };

        Question qEnd = new FinalQuestion(this);
    }

    static class CInterview extends Interview {
        public CInterview(Interview parent, String tag) {
            super(parent, tag);
            setFirstQuestion(qString);
        }

        Question qString = new StringQuestion(this, "c") {
            public Question getNext() {
                return qEnd;
            }
        };

        Question qEnd = new FinalQuestion(this);
    }
}
