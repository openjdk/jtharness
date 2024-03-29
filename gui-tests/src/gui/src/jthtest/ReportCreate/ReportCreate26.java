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

import java.io.File;
import static jthtest.ReportCreate.ReportCreate.*;
import jthtest.Test;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

public class ReportCreate26 extends Test {
    /**
     * This test case verifies that Cancel button in the Create Report Directory
     * will not save settings in the preferences file.
     */
    public void testImpl() throws Exception {
        deleteUserData();
        final String path = TEMP_PATH + REPORT_NAME + REPORT_POSTFIX_HTML + File.separator;
        deleteDirectory(path);
        createFakeRepDir(path);
        addUsedFile(path);

        startJavaTestWithDefaultWorkDirectory();

        JFrameOperator mainFrame = findMainFrame();

        JDialogOperator rep = openReportCreation(mainFrame);

        setXmlChecked(rep, false);
        setPlainChecked(rep, false);
        HtmlReport htmlReport = new HtmlReport(rep, false);

        setPath(rep, path);
        pressCreate(rep);
        new JButtonOperator(findShowReportDialog(), "No").push();

        rep = openReportCreation(mainFrame);

        setXmlChecked(rep, false);
        setPlainChecked(rep, false);
        htmlReport = new HtmlReport(rep);
        htmlReport.setExtraBackUp(false);

        new JButtonOperator(rep, "Cancel").push();

        rep = openReportCreation(mainFrame);
        htmlReport = new HtmlReport(rep, true);

        if (!htmlReport.isEBackUp()) {
            throw new JemmyException("configuration was saved");
        }
    }
}
