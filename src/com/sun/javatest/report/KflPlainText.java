/*
 * $Id$
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.report;

import com.sun.javatest.KnownFailuresList;
import com.sun.javatest.TestResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.TreeSet;


/**
 * Class to emit KFL check results in plain text format.
 * @since 4.5
 */
public class KflPlainText {

    KflPlainText(ReportSettings s) {
        sorter = s.getKflSorter();
        kfl = sorter.getKfl();
    }

    public void write(File dir, boolean writeTcs) {
        this.dir = dir;

        writeTestSet("fail2missing.txt", "kfl.f2m", sorter.getSet(KflSorter.Transitions.FAIL2MISSING));
        writeTestSet("fail2pass.txt", "kfl.f2p", sorter.getSet(KflSorter.Transitions.FAIL2PASS));
        writeTestSet("faiL2error.txt", "kfl.f2e", sorter.getSet(KflSorter.Transitions.FAIL2ERROR));
        writeTestSet("fail2notrun.txt", "kfl.f2nr", sorter.getSet(KflSorter.Transitions.FAIL2NOTRUN));
        writeTestSet("fail2fail.txt", "kfl.f2f", sorter.getSet(KflSorter.Transitions.FAIL2FAIL));
        writeTestSet("newfailures.txt", "kfl.new", sorter.getSet(KflSorter.Transitions.NEWFAILURES));
        writeTestSet("other_errors.txt", "kfl.errors", sorter.getSet(KflSorter.Transitions.OTHER_ERRORS));

        if (writeTcs) {
            writeTestCaseSet("tc_fail2missing.txt", "kfl.tc_f2m", sorter.getSet(KflSorter.Transitions.TC_FAIL2MISSING));
            writeTestCaseSet("tc_fail2pass.txt", "kfl.tc_f2p", sorter.getSet(KflSorter.Transitions.TC_FAIL2PASS));
            writeTestCaseSet("tc_fail2error.txt", "kfl.tc_f2e", sorter.getSet(KflSorter.Transitions.TC_FAIL2ERROR));
            writeTestCaseSet("tc_fail2notrun.txt", "kfl.tc_f2nr", sorter.getSet(KflSorter.Transitions.TC_FAIL2NOTRUN));
            writeTestCaseSet("tc_newfailures.txt", "kfl.tc_new", sorter.getSet(KflSorter.Transitions.TC_NEWFAILURES));
        }
    }

    private void writeTestSet(String f, String id, TreeSet<KflSorter.TestDiff> tests) {
        // add file validation
        try {
            FileWriter out = new FileWriter(new File(dir, f));
            out.write("# ");
            out.write(f);
            out.write("\n");

            for (KflSorter.TestDiff diff : tests) {
                TestResult tr = diff.getTestResult();

                out.write(diff.getName());
                out.write(" ");
                writeBugs(diff, out);
                out.write("\n");
            }

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write a list of bug ids, hyperlinked if possible.  Comma separated.
     */
    void writeBugs(final KflSorter.TestDiff diff, final Writer writer)
            throws IOException {
        KnownFailuresList.Entry[] e = null;

        // find associated KFL entries
        if (diff.getTestCase() == null) {
            e = kfl.find(diff.getTestName());
        }
        else {
            KnownFailuresList.Entry ee = kfl.find(diff.getTestName(),
                    diff.getTestCase());
            if (ee != null)
                e = new KnownFailuresList.Entry[] {ee};
        }

        // no entry, nothing to print
        if (e == null || e.length == 0) {
            // force the associated entry if possible
            if (diff.getKflEntry() != null)
                e = new KnownFailuresList.Entry[] {diff.getKflEntry()};
            else
                return;
        }

        HashSet<String> hs = new HashSet();

        for (int i = 0; i < e.length; i++) {
            String[] bugs = e[i].getBugIdStrings();
            if (bugs == null || bugs.length == 0) {
                continue;
            }

            for (int j = 0; j < bugs.length; j++) {
                // old style kfl and jtx have zero as a placeholder, we don't
                // want to print it
                if (bugs[j].equals("0000000") || bugs[j].equals("0"))
                    continue;

                // already been printed once
                if (hs.contains(bugs[j])) {
                    continue;
                }
                else {
                    hs.add(bugs[j]);
                }

                writer.write(bugs[j]);

                if (bugs.length != j+1)
                    writer.write(",");
            }
        }   // for

        writer.write("\n");
    }

    private void writeTestCaseSet(String f, String id, TreeSet<KflSorter.TestDiff> tests) {
        writeTestSet(f, id, tests);
    }

    private KflSorter sorter;
    private File dir;
    private KnownFailuresList kfl;
}
