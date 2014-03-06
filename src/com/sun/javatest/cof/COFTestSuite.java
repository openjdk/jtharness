/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.cof;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.sun.javatest.Status;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.XMLWriter;
import java.util.Collection;
import java.util.HashMap;

class COFTestSuite extends COFItem {

        private static final String[] cofStatus = new String[Status.NUM_STATES];

        private static I18NResourceBundle i18n = I18NResourceBundle
                        .getBundleForClass(COFTestSuite.class);

        static {
                cofStatus[Status.PASSED] = "pass";
                cofStatus[Status.FAILED] = "fail";
                cofStatus[Status.ERROR] = "error";
                cofStatus[Status.NOT_RUN] = "did_not_run";
        }

        private COFData cofData;

        private boolean legacyMode = false; // modern workdir or not

        private String name;

        protected Pattern testCasePattern = Pattern
                        .compile("^(\\S+): (Passed\\.|Failed\\.|Error\\.|Not\\ run\\.)(.*)");


        private TestResultTable trt;

        COFTestSuite(File dir) {
                trt = new TestResultTable();
                scan(dir);
                legacyMode = true;
        }

        COFTestSuite(File dir, COFData cd) {
                cofData = cd;
                trt = new TestResultTable();
                name = cofData.get("testsuites.testsuite.name");
                scan(dir);
                legacyMode = true;
        }

        COFTestSuite(TestResultTable trt) {
                this.trt = trt;
        }

        COFTestSuite(WorkDirectory wd) {
                this(wd, null);
        }

        COFTestSuite(WorkDirectory wd, COFData cd) {
                cofData = cd;
                TestSuite ts = wd.getTestSuite();
                name = ts.getID();
                trt = wd.getTestResultTable();
        }

        COFTestSuite() {
        }

        void scan(File dir) {
                String[] entries = dir.list();
                if (entries != null) {
                        for (int i = 0; i < entries.length; i++) {
                                File f = new File(dir, entries[i]);
                                if (f.isDirectory())
                                        scan(f);
                                else if (TestResult.isResultFile(f)) {
                                        try {
                                                TestResult tr = new TestResult(f);
                                                trt.update(tr);
                                        } catch (TestResult.Fault e) {
                                                // ignore errors for now
                                                // an error handler might report errors to stderr
                                                System.err.println(i18n.getString("ts.badTest",
                                                                new Object[] { f, e.getMessage() }));
                                        }
                                }
                        }
                }
        }

        void write(XMLWriter out) throws IOException {

                out.startTag("testsuite");
                out.writeAttr("id", "unknownTestSuite:0");

                // name
                out.startTag("name");
                out.write(name == null ? "unknown" : name);
                out.endTag("name");
                // version (optional)
                // tests
                out.startTag("tests");

                // might need to wait for workdir to fully load
                if (!legacyMode)
                        trt.waitUntilReady();
                for (Iterator iter = trt.getIterator(); iter.hasNext();) {
                        TestResult tr = (TestResult) (iter.next());
                        out.newLine();
                        new COFTest(tr, cofData).write(out);
                }
                out.endTag("tests");
                out.endTag("testsuite");
        }

    public COFEnvironment[] getCOFEnvironments(COFData data) {
        if (!legacyMode) {
            trt.waitUntilReady();
        }
        HashMap map = new HashMap();
        String id = cofData.get("environment.id", "env:0").split(":")[0] + ":";
        int id_n = 0;
        for (Iterator iter = trt.getIterator(); iter.hasNext();) {
            TestResult tr = (TestResult) (iter.next());
            try {
                String os = tr.getProperty(TestResult.JAVATEST_OS);
                if (!map.containsKey(os) && os != null) {
                    try {
                        String[] split;
                        split = os.split("[()]");
                        String arch = split[split.length - 1];
                        split = split[0].split(" ", 2);
                        String name = split[0];
                        String version = split[1].trim();
                        data.put("environment.os.arch", arch);
                        data.put("environment.os.name", name);
                        data.put("environment.os.version", version);

                        COFEnvironment e = new COFEnvironment(data, id + id_n++);
                        map.put(os, e);
                    } catch (Throwable t) {
                    }
                }
            } catch (TestResult.Fault ex) {
            }
        }
        Collection values = map.values();
        COFEnvironment[] evs;
        if (values.isEmpty()) {
            evs = new COFEnvironment[] {new COFEnvironment(data, "env:0")};
        } else {
            evs = new COFEnvironment[values.size()];
            Iterator e = values.iterator();
            int i = 0;
            while (e.hasNext()) {
                evs[i++] = (COFEnvironment) e.next();
            }
        }
        return evs;
    }
}
