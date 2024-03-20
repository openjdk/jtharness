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
package jthtest.ReportCreate;

import java.io.File;
import jthtest.Test;
import static jthtest.ReportCreate.ReportCreate.*;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * This test case verifies that the Question Log, Results Summary and Keyword
 * Summa ry box will display a link to the configuration, summary of the test
 * run and the keywords used by the testsuite in the index.html and report.html.
 */

public class ReportCreate18 extends Test {

    public void testImpl() throws Exception {
        deleteUserData();
        startJavaTestWithDefaultWorkDirectory();

        JFrameOperator mainFrame = findMainFrame();

        JDialogOperator rep = openReportCreation(mainFrame);

        setXmlChecked(rep, false);
        setPlainChecked(rep, false);
        HtmlReport report = new HtmlReport(rep);
        report.setFilesAll(false);
        report.setFilesPutInIndex(true);
        report.setFilesPutInReport(true);
        report.setOptionsResults(true);
        report.setOptionsKeyword(true);
        report.setOptionsConfiguration(true, true, false, false);

        final String path = TEMP_PATH + REPORT_NAME + REPORT_POSTFIX_HTML + File.separator;
        deleteDirectory(path);
        setPath(rep, path);
        pressCreate(rep);
        addUsedFile(path);
        pressYes(findShowReportDialog());

        HtmlReportChecker check = new HtmlReportChecker(path, report);
        check.commitMainCheck();
    }
}
