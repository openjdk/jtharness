/*
 * $Id$
 *
 * Copyright (c) 2009, 2010, Oracle and/or its affiliates. All rights reserved.
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

package jthtest.Audit;

import jthtest.Tools;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class Audit extends Tools {
    //throw new UnsupportedOperationException("Not yet implemented");
    public static class AuditTool {
    public enum AuditTabs {Summary, BadResultFile, BadChecksum, BadDescription, BadTestClasses};
    private AuditTabs openedTab;
    private JFrameOperator mainFrame;
    private boolean isOptionsOpened;

    public static class AuditOptions {
        JDialogOperator dialog;

        public AuditOptions(JDialogOperator dialog) {
        this.dialog = dialog;
        }
        public AuditOptions(AuditTool audit) {
        new JMenuOperator(audit.getFrame(), "Audit").pushMenu(new String[] {"Audit", "Options"});
        dialog = findDialog();
        }
        public AuditOptions() {
        dialog = findDialog();
        }

        public void setTestsuite(String testsuite) {
//      getComboBox(dialog, "Test Suite:").getTextField().typeText(testsuite);
        getComboBox(dialog, "Test Suite:").typeText(testsuite);
        }
        public void setWorkdir(String workdir) {
        getComboBox(dialog, "Work Directory:").typeText(workdir);//getTextField().typeText(workdir);
        }
        public void setConfigFile(String config) {
        getComboBox(dialog, "Configuration File:").typeText(config);//getTextField().typeText(config);
        }

        public void pushStartAudit() {

        new JButtonOperator(dialog, "Start Audit").getFocus();
        new JButtonOperator(dialog, "Start Audit").push();
        }
        public void pushCancel() {
        new JButtonOperator(dialog, "Cancel").push();
        }
        public void pushHelp() {
        new JButtonOperator(dialog, "Help").push();
        }

        public JDialogOperator getDialog() {
        return dialog;
        }
        public static JDialogOperator findDialog() {
        return new JDialogOperator("Audit Test Results: Options");
        }
    }

    public static void openAuditTool(JFrameOperator mainFrame) {
        new JMenuOperator(mainFrame, "Tools").pushMenu(new String[] {"Tools", "Test Results Auditor"});
    }

    public static void waitForLoading(AuditTool tool) throws InterruptedException {
        int t = 0;
        while(tool.getSummary().contains("<p style=\"margin-top: 0\">")) {
        Thread.sleep(100);
        t+=100;
        if(t > 60000)
            throw new InterruptedException();
        }
    }

    public AuditTool(JFrameOperator mainFrame) {
        this.mainFrame = mainFrame;
        openAuditTool(mainFrame);
        isOptionsOpened = true;
    }

    // testsuite and config can be null
    public AuditTool(JFrameOperator mainFrame, String testsuite, String workdir, String config) {
        this.mainFrame = mainFrame;
        openAuditTool(mainFrame);
        AuditOptions options = new AuditOptions();
        options.setConfigFile(config);
        options.setTestsuite(testsuite);
        options.setWorkdir(workdir);
        options.pushStartAudit();
        openedTab = AuditTabs.Summary;
        isOptionsOpened = false;
    }

    public JFrameOperator getFrame() {
        return mainFrame;
    }

    public AuditOptions openOptions() {
        return new AuditOptions(this);
    }

    public void setOptions(String testsuite, String workdir, String config) {
        AuditOptions newOptinos;
        if(!isOptionsOpened) {
        newOptinos = openOptions();
        } else {
        newOptinos = new AuditOptions();
        }

        newOptinos.setConfigFile(config);
        newOptinos.setTestsuite(testsuite);
        newOptinos.setWorkdir(workdir);
    }

    public String getSummary() {
        if(openedTab != AuditTabs.Summary) {
        openTab(AuditTabs.Summary);
        }
        return new JEditorPaneOperator(mainFrame, new NameComponentChooser("smry.html")).getText();
    }

    public String getBadResultFile() {
        return null;
    }

    public String getBadChecksum() {
        return null;
    }

    public String getBadDescription() {
        return null;
    }

    public String getBadTestClasses() {
        return null;
    }

    public String getUsedTestSuite() {
        return null;
    }

    public String getUsedWorkDirectory() {
        return null;
    }

    public String getUsedConfigFile() {
        return null;
    }

    private void openTab(AuditTabs tab) {
    }

    public AuditOptions findAuditOptions() {
        return new AuditOptions();
    }
    }
}
