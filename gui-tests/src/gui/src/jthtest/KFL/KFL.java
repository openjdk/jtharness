/*
 * $Id$
 *
 * Copyright (c) 2001, 2024, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.KFL;

import com.sun.javatest.Status;
import com.sun.javatest.lib.MultiTest;
import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;
import jthtest.tools.ReportChecker;
import jthtest.tools.ReportChecker.KFLValues;
import jthtest.tools.ReportDialog;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

public abstract class KFL extends Test {
    /**
      * This is KFL base class, used by all the test scripts under KFL directory.
      * and it has the methods that verifies enabling/disabling interview questions,
      * filters and reports etc...
      */

    public KFL(String kfllist, KFLValues golden, String testsuite, String excludelist) {
        this(kfllist, golden);
        addToCopyFile(testsuite);
        this.testsuite = testsuite;
        this.excludelist = excludelist;
        if (excludelist != null) {
            addToCopyFile(excludelist);
        }
    }

    public KFL(String kfllist, KFLValues golden, String testsuite) {
        this(kfllist, golden);
        addToCopyFile(testsuite);
        this.testsuite = testsuite;
    }

    public KFL(String kfllist, KFLValues golden) {
        super();
        this.kfl = kfllist;
        this.golden = golden;
        if (this.kfl != null) {
            addToCopyFile(this.kfl);
        }
    }

    private KFL() {
        super();
    }
    private KFLValues golden;
    protected String kfl;
    protected ReportDialog rd;
    private ReportChecker checker;
    private String testsuite = null;
    protected String excludelist = null;

    protected void init() throws Exception {
    }

    public void testImpl() throws Exception {
        init();

        mainFrame = new JTFrame(true);

        if (testsuite == null) {
            mainFrame.openDefaultTestSuite();
        } else {
            mainFrame.getTestSuite().openTestSuite(testsuite);
        }

        addUsedFile(mainFrame.createWorkDirectoryInTemp());
        Configuration c = mainFrame.getConfiguration();
        c.load(CONFIG_NAME, true);

        {
            ConfigDialog cd = c.openByMenu(true);
            int kflindex = 9;
            if (excludelist != null) {
                kflindex = 10;
                cd.selectQuestion(8);
                new JRadioButtonOperator(cd.getConfigDialog()).push();
                cd.pushNextConfigEditor();
                new JButtonOperator(cd.getConfigDialog(), "Add").push();

                JDialogOperator op = new JDialogOperator("Add File");
                JTextFieldOperator tf = new JTextFieldOperator(op);
                tf.clearText();
                tf.typeText(DEFAULT_PATH + excludelist);

                new JButtonOperator(op, "Ok").push();
                cd.pushNextConfigEditor();
            }

            cd.selectQuestion(kflindex);
            new JRadioButtonOperator(cd.getConfigDialog()).push();
            cd.pushNextConfigEditor();
            new JButtonOperator(cd.getConfigDialog(), "Add").push();

            JDialogOperator op = new JDialogOperator("Add File");
            JTextFieldOperator tf = new JTextFieldOperator(op);
            tf.clearText();
            tf.typeText(DEFAULT_PATH + kfl);

            new JButtonOperator(op, "Ok").push();

            cd.pushDoneConfigEditor();
            cd = null;
        }

        runTests();

        initReportDialog0();

        KFLValues k = checker.getKFLList();

        if (!golden.equals(k)) {
            errors.add("Not proper KFL statistics. Expected: \n" + golden + "\nfound: \n" + k + "\n");
        }

        int summ = 0;
        if (k.f2e > 0) {
            summ += k.f2e;
        }
        if (k.f2m > 0) {
            summ += k.f2m;
        }
        if (k.f2n > 0) {
            summ += k.f2n;
        }
        if (k.f2p > 0) {
            summ += k.f2p;
        }
        if (k.nf > 0) {
            summ += k.nf;
        }
        if (k.tests != summ) {
            errors.add("Not proper tests summ in KFL statistics. Found: \n" + k.tests + " while expected " + summ + " (" + k + ")\n");
        }

        if (k.testcases > 0) {
            summ = 0;
            if (k.tc_f2e > 0) {
                summ += k.tc_f2e;
            }
            if (k.tc_f2m > 0) {
                summ += k.tc_f2m;
            }
            if (k.tc_f2n > 0) {
                summ += k.tc_f2n;
            }
            if (k.tc_f2p > 0) {
                summ += k.tc_f2p;
            }
            if (k.tc_nf > 0) {
                summ += k.tc_nf;
            }
            if (k.testcases != summ) {
                errors.add("Not proper testscases summ in KFL statistics. Found: \n" + k.tests + " while expected " + summ + " (" + k + ")\n");
            }
        }
    }

    private void initReportDialog0() {
        String path = TEMP_PATH + REPORT_NAME;
        rd = mainFrame.openReportDialog(false);
        initReportDialog();
        rd.setPath(path);
        rd.pushCreate();
        addUsedFile(path);

        JDialogOperator showReport = ReportDialog.findShowReportDialog();
        new JButtonOperator(showReport, "Yes").push();

        checker = new ReportChecker(path, rd);
    }

    public void initReportDialog() {
    }

    public void runTests() {
        mainFrame.runTests().waitForDone();
    }
}
