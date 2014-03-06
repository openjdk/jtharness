/*
 * $Id$
 *
 * Copyright (c) 2011, 2012, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.cof.COFData;
import com.sun.javatest.cof.Main;
import com.sun.javatest.cof.Main.Fault;
import com.sun.javatest.util.I18NResourceBundle;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class COFReport implements ReportFormat {

    @Override
    public ReportLink write(ReportSettings s, File dir) throws IOException {
        COFData data = new COFData();
        data.setInterviewParameters(s.getInterview());

        File outputFile = new File(dir.getAbsolutePath() + File.separator + FNAME);

        if (!s.isCOFTestCasesEnabled()) {
            Main.setGenerateTestCases(s.isCOFTestCasesEnabled());
        }

        Main cofMain = new Main();
        cofMain.setInterviewParameters(s.getInterview());
        try {
            com.sun.javatest.cof.Report report = cofMain.fillReport(data);
            cofMain.writeReport(report, outputFile);
        } catch (Fault ex) {
            Logger.getLogger(COFReport.class.getName()).log(Level.SEVERE, null, ex);
        }

        String warning = data.get("warning");
        if (warning != null) {
//            System.out.println(warning);
        }

        return new ReportLink(i18n.getString("index.coftype.txt"),
                getBaseDirName(), i18n.getString("index.desc.cof"), outputFile);
    }

    @Override
    public String getReportID() {
        return ID;
    }

    @Override
    public String getBaseDirName() {
        return ID;
    }

    @Override
    public boolean acceptSettings(ReportSettings s) {
        return s.isCOFEnabled();
    }

    @Override
    public List<ReportFormat> getSubReports() {
        return Collections.<ReportFormat>emptyList();
    }

    @Override
    public String getTypeName() {
        return ID;
    }

    private final String FNAME = "cof.xml";

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(COFReport.class);

    private static final String ID = "cof";

}
