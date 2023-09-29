/*
 * $Id$
 *
 * Copyright (c) 2001, 2023, Oracle and/or its affiliates. All rights reserved.
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

/**
 * This test case verifies that selecting the Test Environment box will display a link to the current configuration for the test in the report.html.
 */

import jthtest.ViewFilter.ViewFilter;
import java.io.File;
import jthtest.Test;
import static jthtest.ReportCreate.ReportCreate.*;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

public class ReportCreate11 extends Test {

     JFrameOperator mainFrame;

     public void testImpl() throws Exception {
          deleteUserData();
          startJavaTestWithDefaultWorkDirectory();

          mainFrame = findMainFrame();

          JDialogOperator rep = openReportCreation(mainFrame);

          final String path = TEMP_PATH + REPORT_NAME + REPORT_POSTFIX_HTML + File.separator;
          File f = new File(path);
          deleteDirectory(f);
          setPath(rep, path);

          setPlainChecked(rep, false);
          setXmlChecked(rep, false);
          HtmlReport html = new HtmlReport(rep);
          html.setOptionsAll(false);
          html.setOptionsConfiguration(true, false, true, false);
          html.setFilesAll(false);
          html.setFilesPutInReport(true);

          pressCreate(rep);
          addUsedFile(f);

          pressYes(findShowReportDialog());

          new HtmlReportChecker(path, html).commitMainCheck();
     }

     public void selectEnableTSFilter() {
          JDialogOperator filterEditor = ViewFilter.openFilterEditor(mainFrame);

          ViewFilter.selectFilter(filterEditor, 4);

          ViewFilter.chooseTab(filterEditor, "Special");

          new JCheckBoxOperator(filterEditor, "Enable test suite filter.").setSelected(true);

          ViewFilter.ok(filterEditor);
     }
}
