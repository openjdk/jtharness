/*
 * $Id$
 *
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.CustomReport;

import jthtest.ReportTools;
import jthtest.Test;
import jthtest.tools.JTFrame;
import jthtest.tools.ReportDialog;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author andrey
 */
public abstract class CReport extends Test {
    private String path;
    private boolean html;
    private boolean plain;
    private boolean xml;

    public CReport(boolean html, boolean plain, boolean xml) {
        this.html = html;
        this.plain = plain;
        this.xml = xml;
        path = TEMP_PATH + REPORT_NAME + (html ? ReportTools.REPORT_POSTFIX_HTML : "") + (plain ? ReportTools.REPORT_POSTFIX_PLAIN : "") + (xml ? ReportTools.REPORT_POSTFIX_XML : "");
    }

    @Override
    public void testImpl() throws Exception {
        mainFrame = JTFrame.startJTWithDefaultWorkDirectory();

        ReportDialog rd = mainFrame.openReportDialog();
        rd.setHtmlChecked(html);
        rd.setPlainChecked(plain);
        rd.setXmlChecked(xml);
        rd.setPath(path);
        rd.pushCreate();
        new JButtonOperator(ReportDialog.findShowReportDialog(), "Yes").push();

        JDialogOperator report = new JDialogOperator(ReportTools.getExecResource("rb.title"));
        CustomReport.checkReportBrowser(report, path, html, plain, xml);
    }
}
