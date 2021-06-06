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
package jthtest.menu;

import jthtest.Test;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;

/**
 *
 * @author andrey
 */
public abstract class MenuItems extends Test {

    public MenuItems() {
        super();
        maxTime = 1200000; // 20 min
    }

    protected void checkGenericMenus(boolean ts, boolean wd) {
        String append = ts ? ", testsuite is opened" : "";
        if (wd) {
            append = append + ", workdirectory is opened";
        }

        mainFrame.getTools_ReportConverterMenu().push();
        JDialogOperator d = null;
        try {
            d = new JDialogOperator("Create a Report");
            new JButtonOperator(d, "Cancel").push();
            mainFrame.closeCurrentTool();
        } catch (Exception e) {
            errors.add("Exception while looking for Create a Report dialog: '" + e.getMessage() + "' (Tools->Report Converter...) pushed" + append);
        }

        mainFrame.getTools_OpenQuickStartWizardMenu().push();
        try {
            d = new JDialogOperator(WINDOWNAME + " Harness Quick Start");
            new JButtonOperator(d, "Cancel").push();
        } catch (Exception e) {
            errors.add("Exception while looking for " + WINDOWNAME + " Harness Quick Start dialog: '" + e.getMessage() + "' (Tools->Open Quick Start Wizard...) pushed" + append);
        }

        mainFrame.getTools_AgentMonitorMenu().push();
        try {
            String tabName = ts ? mainFrame.getTabName(1) : mainFrame.getTabName(0);
            if (!"Agent Monitor".equals(tabName)) {
                errors.add("Exception while looking for Agent Pool tab: 'Agent Monitor tab not found' (found " + tabName + ") (Tools->Agent Monitor...) pushed" + append);
            }
            mainFrame.closeCurrentTool();
        } catch (Exception e) {
            errors.add("Exception while looking for Agent Pool tab: '" + e.getMessage() + "' (Tools->Agent Monitor...) pushed" + append);
        }

        mainFrame.getTools_TestResultsAuditorMenu().push();
        try {
            d = new JDialogOperator("Audit Test Results: Options");
            new JButtonOperator(d, "Cancel").push();
            mainFrame.closeCurrentTool();
        } catch (Exception e) {
            errors.add("Exception while looking for Audit Test Results: Options dialog: '" + e.getMessage() + "' (Tools->Test Results Auditor...) pushed" + append);
        }

        mainFrame.getFile_OpenQuickStartMenu().push();
        try {
            d = new JDialogOperator(WINDOWNAME + " Harness Quick Start");
            new JButtonOperator(d, "Cancel").push();
        } catch (Exception e) {
            errors.add("Exception while looking for " + WINDOWNAME + " Harness Quick Start dialog: '" + e.getMessage() + "' (Files->Open Quick Start Wizard...) pushed" + append);
        }

        mainFrame.getFile_PreferencesMenu().push();
        try {
            d = new JDialogOperator(WINDOWNAME + " Harness Preferences");
            new JButtonOperator(d, "Cancel").push();
        } catch (Exception e) {
            errors.add("Exception while looking for " + WINDOWNAME + " Harness Preferences dialog: '" + e.getMessage() + "' (Files->Preferences...) pushed" + append);
        }

        mainFrame.getFile_Open_TestSuiteMenu().push();
        try {
            d = new JDialogOperator("Open Test Suite");
            new JButtonOperator(d, "Cancel").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Open Test Suite dialog: '" + e.getMessage() + "' (Files->Open->Test Suite ...) pushed" + append);
        }

        mainFrame.getFile_Open_WorkDirectoryMenu().push();
        try {
            d = new JDialogOperator("Open Work Directory");
            new JButtonOperator(d, "Cancel").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Open Work Directory dialog: '" + e.getMessage() + "' (Files->Open->Work Directory ...) pushed" + append);
        }
    }

    protected void checkTSMenus(boolean wd, boolean cfg) {
        String append = wd ? ", testsuite is opened, workdirectory is opened" : ", testsuite is opened";

        JDialogOperator d;

        mainFrame.getFile_CreateWorkDirectoryMenu().push();
        try {
            d = new JDialogOperator("Create Work Directory");
            new JButtonOperator(d, "Cancel").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Create Work Directory dialog: '" + e.getMessage() + "' (Files->Create Work Directory ...) pushed" + append);
        }
        if (wd) {
            if (mainFrame.getTabsCount() < 2) {
                errors.add("New Tool was not opened when pushing File->Create Work Directory" + append);
            } else if (mainFrame.getTabsCount() != 2) {
                throw new JemmyException("Error: tab count is not 2 (File->Create Work Directory)");
            } else {
                mainFrame.closeCurrentTool();
            }
        }

        mainFrame.getConfigure_LoadConfigurationMenu().push();
        if (wd) {
            try {
                d = new JDialogOperator("Load Configuration");
                new JButtonOperator(d, "Cancel").push();
            } catch (Exception e) {
                errors.add("Exception while looking for Load Configuration dialog: '" + e.getMessage() + "' (Configure->Load Configuration...) pushed" + append);
            }
        } else {
            try {
                d = new JDialogOperator("Work Directory Required");
                new JButtonOperator(d, "Cancel").push();
            } catch (Exception e) {
                errors.add("Exception while looking for Work Directory Required dialog: '" + e.getMessage() + "' (Configure->Load Configuration...) pushed" + append);
            }
            mainFrame.getConfigure_LoadConfigurationMenu().push();
            try {
                d = new JDialogOperator("Work Directory Required");
                new JButtonOperator(d, "Create Work Directory").push();
                d = new JDialogOperator("Create Work Directory");
                new JButtonOperator(d, "Cancel").push();
            } catch (Exception e) {
                errors.add("Exception while looking for Create Work Directory dialog: '" + e.getMessage() + "' (Configure->Load Configuration... : Create Work Directory) pushed" + append);
            }
            mainFrame.getConfigure_LoadConfigurationMenu().push();
            try {
                d = new JDialogOperator("Work Directory Required");
                new JButtonOperator(d, "Open Work Directory").push();
                d = new JDialogOperator("Open Work Directory");
                new JButtonOperator(d, "Cancel").push();
            } catch (Exception e) {
                errors.add("Exception while looking for Open Work Directory dialog: '" + e.getMessage() + "' (Configure->Load Configuration... : Open Work Directory) pushed" + append);
            }
        }

        mainFrame.getConfigure_NewConfigurationMenu().push();
        if (wd) {
            try {
                d = new JDialogOperator("Configuration Editor");
                d.close();
                if (cfg) {
                    d = new JDialogOperator("Warning: Unsaved Changes");
                    new JButtonOperator(d, "No").push();
                }
            } catch (Exception e) {
                errors.add("Exception while looking for Configuration Editor dialog: '" + e.getMessage() + "' (Configure->New Configuration...) pushed" + append);
            }
        } else {
            try {
                d = new JDialogOperator("Work Directory Required");
                new JButtonOperator(d, "Cancel").push();
            } catch (Exception e) {
                errors.add("Exception while looking for Work Directory Required dialog: '" + e.getMessage() + "' (Configure->New Configuration...) pushed" + append);
            }
        }

        if (!cfg) {
            mainFrame.getRunTests_StartMenu().push();
            if (wd) {
                try {
                    d = new JDialogOperator("Configuration Required");
                    new JButtonOperator(d, "Cancel").push();
                } catch (Exception e) {
                    errors.add("Exception while looking for Configuration Required dialog: '" + e.getMessage() + "' (Run Tests->Start) pushed" + append);
                }
                mainFrame.getRunTests_StartMenu().push();
                try {
                    d = new JDialogOperator("Configuration Required");
                    new JButtonOperator(d, "OK").push();
                    d = new JDialogOperator("Configuration Editor");
                    d.close();
                } catch (Exception e) {
                    errors.add("Exception while looking for Configuration Editor dialog: '" + e.getMessage() + "' (Run Tests->Start : OK) pushed" + append);
                }
            } else {
                try {
                    d = new JDialogOperator("Work Directory Required");
                    new JButtonOperator(d, "Cancel").push();
                } catch (Exception e) {
                    errors.add("Exception while looking for Work Directory Required dialog: '" + e.getMessage() + "' (Run Tests->Start) pushed" + append);
                }
            }
        }

        mainFrame.getRunTests_MonitorProgressMenu().push();
        try {
            JFrameOperator f = new JFrameOperator("Test Manager: Progress Monitor");
            new JButtonOperator(f, "Close").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Test Manager: Progress Monitor dialog: '" + e.getMessage() + "' (Run Tests->Monitor Progress...) pushed" + append);
        }

        if (wd) {
            mainFrame.getReport_CreateReportMenu().push();
            try {
                d = new JDialogOperator("Create a New Report");
                new JButtonOperator(d, "Cancel").push();
            } catch (Exception e) {
                errors.add("Exception while looking for Create a New Report dialog: '" + e.getMessage() + "' (Report->Create Report...) pushed" + append);
            }

            mainFrame.getReport_OpenReportMenu().push();
            try {
                d = new JDialogOperator("Open Report");
                new JButtonOperator(d, "Cancel").push();
            } catch (Exception e) {
                errors.add("Exception while looking for Open Report dialog: '" + e.getMessage() + "' (Report->Open Report...) pushed" + append);
            }
        }

        mainFrame.getView_Configuration_ShowTestEnvironmentMenu().push();
        try {
            d = new JDialogOperator("Test Environment: ");
            new JButtonOperator(d, "Close").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Test Environment: <name> dialog: '" + e.getMessage() + "' (View->Configuration->Show Test Environment...) pushed" + append);
        }

        mainFrame.getView_Configuration_ShowExcludeListMenu().push();
        try {
            d = new JDialogOperator("No Exclude List Specified");
            new JButtonOperator(d, "Close").push();
        } catch (Exception e) {
            errors.add("Exception while looking for No Exclude List Specified dialog: '" + e.getMessage() + "' (View->Configuration->Show Exclude List...) pushed" + append);
        }

        if (!wd) {
            mainFrame.getView_Configuration_ShowChecklistMenu().push();
            try {
                d = new JDialogOperator("Configuration Checklist");
                new JButtonOperator(d, "Close").push();
            } catch (Exception e) {
                errors.add("Exception while looking for Configuration Checklist dialog: '" + e.getMessage() + "' (View->Configuration->Show Checklist...) pushed" + append);
            }
        }

        mainFrame.getView_Configuration_ShowQuestionLogMenu().push();
        try {
            d = new JDialogOperator("Configuration Question Log");
            new JButtonOperator(d, "Close").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Configuration Question Log dialog: '" + e.getMessage() + "' (View->Configuration->Show Question Log...) pushed" + append);
        }

        mainFrame.getView_Filter_ConfigureFiltersMenu().push();
        try {
            d = new JDialogOperator("Filter Editor");
            new JButtonOperator(d, "Cancel").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Filter Editor <TS name> dialog: '" + e.getMessage() + "' (View->Filter->Configure Filters...) pushed" + append);
        }

        mainFrame.getView_PropertiesMenu().push();
        try {
            d = new JDialogOperator("Test Manager Properties");
            new JButtonOperator(d, "Close").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Test Manager Properties dialog: '" + e.getMessage() + "' (View->Properties...) pushed" + append);
        }

        if (wd) {
            mainFrame.getView_LogsMenu().push();
            try {
                d = new JDialogOperator("Log Viewer 1");
                new JButtonOperator(d, "Close").push();
            } catch (Exception e) {
                errors.add("Exception while looking for Log Viewer 1 dialog: '" + e.getMessage() + "' (View->Logs...) pushed" + append);
            }
        }

        mainFrame.getView_TestSuiteErrorsMenu().push();
        try {
            d = new JDialogOperator("Test Manager: Test Suite Errors");
            new JButtonOperator(d, "Close").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Test Manager: Test Suite Errors dialog: '" + e.getMessage() + "' (View->Test Suite Errors...) pushed" + append);
        }
    }

    protected void checkConfigMenus() {
        JDialogOperator d;

        mainFrame.getConfigure_EditConfigurationMenu().push();
        try {
            d = new JDialogOperator("Configuration Editor");
            new JListOperator(d);
            try {
                new JTabbedPaneOperator(d);
                errors.add("Tabbed Pane found on Full Configuration Editor. Bad. ");
            } catch (TimeoutExpiredException e) {
            }
            new JButtonOperator(d, "Done").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Configuration Editor dialog: '" + e.getMessage() + "' (Configuration->Edit Configuration...) pushed, testsuite, workdirectory and configuration opened");
        }

        mainFrame.getConfigure_EditQuickSet_TestsToRunMenu().push();
        try {
            d = new JDialogOperator("Configuration Editor");
            JTabbedPaneOperator tp = new JTabbedPaneOperator(d);
            if (!"Tests".equals(tp.getTitleAt(tp.getSelectedIndex()))) {
                errors.add("'Tests' tab was expected to be opened by Configuration->Edit Quick Set->Tests To Run ... and " + tp.getTitleAt(tp.getSelectedIndex()) + " was opened");
            }
            try {
                new JListOperator(d);
                errors.add("List found on Quick Configuration Editor. Bad. ");
            } catch (TimeoutExpiredException e) {
            }
            new JButtonOperator(d, "Done").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Configuration Editor dialog: '" + e.getMessage() + "' (Configuration->Edit Quick Set->Tests To Run ...) pushed, testsuite, workdirectory and configuration opened");
        }

        mainFrame.getConfigure_EditQuickSet_ConcurrencyMenu().push();
        try {
            d = new JDialogOperator("Configuration Editor");
            JTabbedPaneOperator tp = new JTabbedPaneOperator(d);
            if (!"Execution".equals(tp.getTitleAt(tp.getSelectedIndex()))) {
                errors.add("'Execution' tab was expected to be opened by Configuration->Edit Quick Set->Concurrency ... and " + tp.getTitleAt(tp.getSelectedIndex()) + " was opened");
            }
            new JButtonOperator(d, "Done").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Configuration Editor dialog: '" + e.getMessage() + "' (Configuration->Edit Quick Set->Concurrency ...) pushed, testsuite, workdirectory and configuration opened");
        }

        mainFrame.getConfigure_EditQuickSet_ExcludeListMenu().push();
        try {
            d = new JDialogOperator("Configuration Editor");
            JTabbedPaneOperator tp = new JTabbedPaneOperator(d);
            if (!"Exclude List".equals(tp.getTitleAt(tp.getSelectedIndex()))) {
                errors.add("'Exclude List' tab was expected to be opened by Configuration->Edit Quick Set->Exclude List ... and " + tp.getTitleAt(tp.getSelectedIndex()) + " was opened");
            }
            new JButtonOperator(d, "Done").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Configuration Editor dialog: '" + e.getMessage() + "' (Configuration->Edit Quick Set->Exclude List ...) pushed, testsuite, workdirectory and configuration opened");
        }

        mainFrame.getConfigure_EditQuickSet_PriorStatusMenu().push();
        try {
            d = new JDialogOperator("Configuration Editor");
            JTabbedPaneOperator tp = new JTabbedPaneOperator(d);
            if (!"Prior Status".equals(tp.getTitleAt(tp.getSelectedIndex()))) {
                errors.add("'Prior Status' tab was expected to be opened by Configuration->Edit Quick Set->Prior Status ... and " + tp.getTitleAt(tp.getSelectedIndex()) + " was opened");
            }
            new JButtonOperator(d, "Done").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Configuration Editor dialog: '" + e.getMessage() + "' (Configuration->Edit Quick Set->Prior Status ...) pushed, testsuite, workdirectory and configuration opened");
        }

        mainFrame.getConfigure_EditQuickSet_TimeoutFactorMenu().push();
        try {
            d = new JDialogOperator("Configuration Editor");
            JTabbedPaneOperator tp = new JTabbedPaneOperator(d);
            if (!"Execution".equals(tp.getTitleAt(tp.getSelectedIndex()))) {
                errors.add("'Execution' tab was expected to be opened by Configuration->Edit Quick Set->Timeout Factor ... and " + tp.getTitleAt(tp.getSelectedIndex()) + " was opened");
            }
            new JButtonOperator(d, "Done").push();
        } catch (Exception e) {
            errors.add("Exception while looking for Configuration Editor dialog: '" + e.getMessage() + "' (Configuration->Edit Quick Set->Timeout Factor ...) pushed, testsuite, workdirectory and configuration opened");
        }
    }
}