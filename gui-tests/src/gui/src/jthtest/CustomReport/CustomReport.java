/*
 * $Id$
 *
 * Copyright (c) 2001, 2022, Oracle and/or its affiliates. All rights reserved.
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
import java.io.File;
import javax.swing.JCheckBox;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

public class CustomReport extends ReportTools {
    public static JCheckBox selectType(JDialogOperator rep, ReportType sel) {
    JListOperator types = new JListOperator(rep, new NameComponentChooser("nrd.typel"));
    int index = 0;
/*    switch(sel) {
        case HTML: index = 0; break;
        case PLAIN_TEXT: index = 1; break;
        case XML: index = 2; break;
        case CUSTOM_TEXT: index = 3; break;
        case CUSTOM_XML: index = 4; break;
    }/**/
    index = sel.ordinal();
    types.setSelectedIndex(index);
    return (JCheckBox) types.getModel().getElementAt(index);
    }

    public static boolean checkReportFile(String path, ReportType type) {
    String name = "";
    switch(type) {
        case HTML: name = File.separator+"html"+File.separator+"report.html"; break;
        case PLAIN_TEXT: name = File.separator+"text"+File.separator+"summary.txt"; break;
        case XML: name = File.separator+"xml"+File.separator+"report.xml"; break;
    }
    return new File(path+name).exists();
    }

    public static void checkReportBrowser(JDialogOperator report, String path,  boolean html, boolean plaintext, boolean xml) throws InterruptedException {
    JEditorPaneOperator text = new JEditorPaneOperator(report, new NameComponentChooser("text"));

    if(html) {
        Thread.sleep(500);
        if(!text.getText().trim().replaceAll("\\s+", " ").contains("HTML Report"))
        throw new JemmyException("'HTML Report' hyperlink was not found in the report");
        if(!checkReportFile(path, ReportType.HTML))
        throw new JemmyException("Html report file was not found");
    }

    if(plaintext) {
        Thread.sleep(500);
        if(!text.getText().trim().replaceAll("\\s+", " ").contains("Plain Text Report"))
        throw new JemmyException("'Plain Text Report' hyperlink was not found in the report");
        if(!checkReportFile(path, ReportType.PLAIN_TEXT))
        throw new JemmyException("Plain text report file was not found");
    }

    if(xml) {
        Thread.sleep(500);
        if(!text.getText().trim().replaceAll("\\s+", " ").contains("XML Report"))
        throw new JemmyException("'XML Report' hyperlink was not found in the report");
        if(!checkReportFile(path, ReportType.XML))
        throw new JemmyException("xml report file was not found");
    }
    }
}
