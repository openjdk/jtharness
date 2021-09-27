/*
 * $Id$
 *
 * Copyright (c) 2001, 2021, Oracle and/or its affiliates. All rights reserved.
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

import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;

import static jthtest.Tools.*;

public class Menu {
    public static final String FILE_MENU_TI18N = "dt.file.menu";
    public static final String FILE_MENU_MNEMONIC_TI18N = "dt.file.mne";
    public static final String FILE_OPENQS_MENU_EI18N = "mgr.openQuickStart.act";
    public static final String FILE_OPEN_MENU_TI18N = "???";
    public static final String FILE_OPEN_WD_MENU_EI18N = "ch.setWorkDir.act";
    public static final String FILE_OPEN_WD_MNEMONIC_EI18N = "ch.setWorkDir.mne";
    public static final String FILE_OPEN_TS_MENU_EI18N = "mgr.openTestSuite.act";
    public static final String FILE_OPEN_TS_MNEMONIC_EI18N = "mgr.openTestSuite.mne";
    public static final String FILE_RECENTWD_MENU_TI18N = "dt.file.recentwd.menu";
    public static final String FILE_PREFS_MENU_TI18N = "dt.file.prefs.mit";
    public static final String FILE_CLOSE_MENU_TI18N = "tdi.file.close.act";
    public static final String FILE_EXIT_MENU_TI18N = "dt.file.exit.mit";
    public static final String FILE_CREATEWD_MENU_EI18N = "mgr.createWorkDir.act";
    public static final String CONFIGURE_MENU_EI18N = "ch.menu";
    public static final String CONFIGURE_EDIT_MENU_EI18N = "ch.full.act";
    public static final String CONFIGURE_EDIT_QS_MENU_EI18N = "ch.change.menu";
    public static final String CONFIGURE_NEW_MENU_EI18N = "ch.new.act";
    public static final String CONFIGURE_LOAD_MENU_I18N = "ce.file.load.mit";
    public static final String CONFIGURE_RECENT_MENU_I18N = "ce.history.menu";
    public static final String RUNTESTS_MENU_EI18N = "rh.menu";
    public static final String RUNTESTS_START_MENU_EI18N = "rh.start.act";
    public static final String RUNTESTS_STOP_MENU_EI18N = "rh.stop.act";
    public static final String RUNTESTS_MONITOR_MENU_EI18N = "rh.progress.act";
    public static final String REPORT_MENU_EI18N = "rpth.menu";
    public static final String REPORT_CREATE_MENU_EI18N = "rpth.new.act";
    public static final String REPORT_OPEN_MENU_EI18N = "rpth.open.act";
    public static final String VIEW_MENU_EI18N = "ce.view.menu";
    public static final String VIEW_CONFIGURATION_MENU_EI18N = "exec.view.cfg.menu";
    public static final String VIEW_FILTER_MENU_EI18N = "fconfig.submenu.menu";
    public static final String VIEW_PROPERTIES_MENU_EI18N = "exec.view.props.act";
    public static final String VIEW_LOGS_MENU_EI18N = "exec.view.logviewer.act";
    public static final String VIEW_TSERRORS_MENU_EI18N = "exec.view.testSuiteErrors.act";
    public static final String VIEW_CONFIGURATION_SHOWTESTENV_MENU_EI18N = "ch.env.act";
    public static final String VIEW_CONFIGURATION_SHOWEXCLUDELIST_MENU_EI18N = "ch.excl.act";
    public static final String VIEW_CONFIGURATION_SHOWCHECKLIST_MENU_EI18N = "ch.checkList.act";
    public static final String VIEW_CONFIGURATION_SHOWQUESTIONLOG_MENU_EI18N = "ch.quLog.act";
    public static final String TOOLS_MENU_TI18N = "dt.tasks.menu";
    public static final String TOOLS_MENU_MNEMONIC_TI18N = "dt.tasks.mne";
    public static final String TOOLS_TEST_RESULTS_AUDITOR_I18N = "???";
    public static final String TOOLS_AGENT_MONITOR_I18N = "???";
    public static final String TOOLS_OPENQS_EI18N = "mgr.openQuickStart.act";
    public static final String WINDOWS_MENU_TI18N = "dt.windows.menu";
    public static final String WINDOWS_MENU_MNEMONIC_TI18N = "dt.windows.mne";
    public static final String HELP_MENU_EI18N = "qlb.help.btn";
    public static final String HELP_MENU_MNEMONIC_TI18N = "qlb.help.mne";
    public static final String HELP_ONLINEHELP_MENU_TI18N = "hm.help.mit";
    public static final String HELP_ONLINEHELP_MNEMONIC_TI18N = "hm.help.mne";
    public static final String HELP_ABOUTJT_MENU_I18N = "hm.aboutJavaTest.mit";
    public static final String HELP_ABOUTJT_MNEMONIC_I18N = "hm.aboutJavaTest.mne";
    public static final String HELP_ABOUTJVM_MENU_I18N = "hm.aboutJava.mit";
    public static final String HELP_ABOUTJVM_MNEMONIC_I18N = "hm.aboutJava.mne";
    private static JMenuBarOperator menuBar;

    public static JMenuBarOperator getMenuBar(JFrameOperator mainFrame) {
        if (menuBar == null)
            menuBar = new JMenuBarOperator(mainFrame);
        return menuBar;
    }

    public static JMenuOperator getFileMenu(JFrameOperator mainFrame) {
        return new JMenuOperator(mainFrame, getFileMenuName());
    }

    public static JMenuItemOperator getFile_OpenQuickStartMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getFileMenuName(), getFile_OpenQuickStartMenuName()});
    }

    public static JMenuItemOperator getFile_OpenMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItems(new String[]{getFileMenuName()}, new SimpleStringComparator())[1];
    }

    public static JMenuItemOperator getFile_RecentWorkDirectoryMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getFileMenuName(), getFile_RecentWorkDirectoryMenuName()});
    }

    public static JMenuItemOperator getFile_PreferencesMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getFileMenuName(), getFile_PreferencesMenuName()});
    }

    public static JMenuItemOperator getFile_CloseMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getFileMenuName(), getFile_CloseMenuName()});
    }

    public static JMenuItemOperator getFile_ExitMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getFileMenuName(), getFile_ExitMenuName()});
    }

    public static JMenuItemOperator getFile_Open_WorkDirectoryMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getFileMenuName(), getFile_OpenMenuName(), getFile_Open_WorkDirectoryMenuName()}, new SimpleStringComparator());
    }

    public static JMenuItemOperator getFile_Open_TestSuiteMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getFileMenuName(), getFile_OpenMenuName(), getFile_Open_TestSuiteMenuName()}, new SimpleStringComparator());
    }

    public static JMenuItemOperator getFile_CreateWorkDirectoryMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getFileMenuName(), getFile_CreateWorkDirectoryMenuName()});
    }

    public static String getFileMenuName() {
        return getToolResource(FILE_MENU_TI18N);
    }

    public static String getFile_OpenQuickStartMenuName() {
        return getExecResource(FILE_OPENQS_MENU_EI18N);
    }

    public static String getFile_OpenMenuName() {
        return "Open";
    }

    public static String getFile_RecentWorkDirectoryMenuName() {
        return getToolResource(FILE_RECENTWD_MENU_TI18N);
    }

    public static String getFile_PreferencesMenuName() {
        return getToolResource(FILE_PREFS_MENU_TI18N);
    }

    public static String getFile_CloseMenuName() {
        return getToolResource(FILE_CLOSE_MENU_TI18N);
    }

    public static String getFile_ExitMenuName() {
        return getToolResource(FILE_EXIT_MENU_TI18N);
    }

    public static String getFile_Open_WorkDirectoryMenuName() {
        return getExecResource(FILE_OPEN_WD_MENU_EI18N);
    }

    public static String getFile_Open_TestSuiteMenuName() {
        return getExecResource(FILE_OPEN_TS_MENU_EI18N);
    }

    public static String getFile_CreateWorkDirectoryMenuName() {
        return getExecResource(FILE_CREATEWD_MENU_EI18N);
    }

    public static JMenuOperator getConfigureMenu(JFrameOperator mainFrame) {
        return new JMenuOperator(mainFrame, getConfigureMenuName());
    }

    public static JMenuItemOperator getConfigure_EditConfigurationMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getConfigureMenuName(), getConfigure_EditConfigurationMenuName()});
    }

    public static JMenuItemOperator getConfigure_EditQuickSetMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getConfigureMenuName(), getConfigure_EditQuickSetMenuName()});
    }

    public static JMenuItemOperator getConfigure_NewConfigurationMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getConfigureMenuName(), getConfigure_NewConfigurationMenuName()});
    }

    public static JMenuItemOperator getConfigure_LoadConfigurationMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getConfigureMenuName(), getConfigure_LoadConfigurationMenuName()});
    }

    public static JMenuItemOperator getConfigure_LoadRecentConfigurationMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getConfigureMenuName(), getConfigure_LoadRecentConfigurationMenuName()});
    }

    public static String getConfigureMenuName() {
        return getExecResource(CONFIGURE_MENU_EI18N);
    }

    public static String getConfigure_EditConfigurationMenuName() {
        return getExecResource(CONFIGURE_EDIT_MENU_EI18N);
    }

    public static String getConfigure_EditQuickSetMenuName() {
        return getExecResource(CONFIGURE_EDIT_QS_MENU_EI18N);
    }

    public static String getConfigure_NewConfigurationMenuName() {
        return getExecResource(CONFIGURE_NEW_MENU_EI18N);
    }

    public static String getConfigure_LoadConfigurationMenuName() {
        return getExecResource(CONFIGURE_LOAD_MENU_I18N);
    }

    public static String getConfigure_LoadRecentConfigurationMenuName() {
        return getExecResource(CONFIGURE_RECENT_MENU_I18N);
    }

    public static JMenuOperator getRunTestsMenu(JFrameOperator mainFrame) {
        return new JMenuOperator(mainFrame, getRunTestsMenuName());
    }

    public static JMenuItemOperator getRunTests_StartMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getRunTestsMenuName(), getRunTests_StartMenuName()});
    }

    public static JMenuItemOperator getRunTests_StopMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getRunTestsMenuName(), getRunTests_StopMenuName()});
    }

    public static JMenuItemOperator getRunTests_MonitorProgressMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getRunTestsMenuName(), getRunTests_MonitorProgressMenuName()});
    }

    public static String getRunTestsMenuName() {
        return getExecResource(RUNTESTS_MENU_EI18N);
    }

    public static String getRunTests_StartMenuName() {
        return getExecResource(RUNTESTS_START_MENU_EI18N);
    }

    public static String getRunTests_StopMenuName() {
        return getExecResource(RUNTESTS_STOP_MENU_EI18N);
    }

    public static String getRunTests_MonitorProgressMenuName() {
        return getExecResource(RUNTESTS_MONITOR_MENU_EI18N);
    }

    public static JMenuOperator getReportMenu(JFrameOperator mainFrame) {
        return new JMenuOperator(mainFrame, getReportMenuName());
    }

    public static JMenuItemOperator getReport_CreateReportMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getReportMenuName(), getReport_CreateReportMenuName()});
    }

    public static JMenuItemOperator getReport_OpenReportMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getReportMenuName(), getReport_OpenReportMenuName()});
    }

    public static String getReportMenuName() {
        return getExecResource(REPORT_MENU_EI18N);
    }

    public static String getReport_CreateReportMenuName() {
        return getExecResource(REPORT_CREATE_MENU_EI18N);
    }

    public static String getReport_OpenReportMenuName() {
        return getExecResource(REPORT_OPEN_MENU_EI18N);
    }

    public static JMenuOperator getViewMenu(JFrameOperator mainFrame) {
        return new JMenuOperator(mainFrame, getViewMenuName());
    }

    public static JMenuItemOperator getView_ConfigurationMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getViewMenuName(), getView_ConfigurationMenuName()});
    }

    public static JMenuItemOperator getView_FilterMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getViewMenuName(), getView_FilterMenuName()});
    }

    public static JMenuItemOperator getView_PropertiesMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getViewMenuName(), getView_PropertiesMenuName()});
    }

    public static JMenuItemOperator getView_LogsMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getViewMenuName(), getView_LogsMenuName()});
    }

    public static JMenuItemOperator getView_TestSuiteErrorsMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getViewMenuName(), getView_TestSuiteErrorsMenuName()});
    }

    public static JMenuItemOperator getView_Configuration_ShowChecklistMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getViewMenuName(), getView_ConfigurationMenuName(), getView_Configuration_ShowChecklistMenuName()});
    }

    public static JMenuItemOperator getView_Configuration_ShowExcludeListMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getViewMenuName(), getView_ConfigurationMenuName(), getView_Configuration_ShowExcludeListMenuName()});
    }

    public static JMenuItemOperator getView_Configuration_ShowQuestionLogMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getViewMenuName(), getView_ConfigurationMenuName(), getView_Configuration_ShowQuestionLogMenuName()});
    }

    public static JMenuItemOperator getView_Configuration_ShowTestEnvironmentMenu(JFrameOperator mainFrame) {
        return getMenuBar(mainFrame).showMenuItem(new String[]{getViewMenuName(), getView_ConfigurationMenuName(), getView_Configuration_ShowTestEnvironmentMenuName()});
    }

    public static String getViewMenuName() {
        return getExecResource(VIEW_MENU_EI18N);
    }

    public static String getView_ConfigurationMenuName() {
        return getExecResource(VIEW_CONFIGURATION_MENU_EI18N);
    }

    public static String getView_FilterMenuName() {
        return getExecResource(VIEW_FILTER_MENU_EI18N);
    }

    public static String getView_PropertiesMenuName() {
        return getExecResource(VIEW_PROPERTIES_MENU_EI18N);
    }

    public static String getView_LogsMenuName() {
        return getExecResource(VIEW_LOGS_MENU_EI18N);
    }

    public static String getView_TestSuiteErrorsMenuName() {
        return getExecResource(VIEW_TSERRORS_MENU_EI18N);
    }

    public static String getView_Configuration_ShowTestEnvironmentMenuName() {
        return getExecResource(VIEW_CONFIGURATION_SHOWTESTENV_MENU_EI18N);
    }

    public static String getView_Configuration_ShowChecklistMenuName() {
        return getExecResource(VIEW_CONFIGURATION_SHOWCHECKLIST_MENU_EI18N);
    }

    public static String getView_Configuration_ShowExcludeListMenuName() {
        return getExecResource(VIEW_CONFIGURATION_SHOWEXCLUDELIST_MENU_EI18N);
    }

    public static String getView_Configuration_ShowQuestionLogMenuName() {
        return getExecResource(VIEW_CONFIGURATION_SHOWQUESTIONLOG_MENU_EI18N);
    }

    public static String getToolsMenuName() {
        return getToolResource(TOOLS_MENU_TI18N);
    }

    public static String getTools_TestResultsAuditorMenuName() {
        return "Test Results Auditor...";
    }

    public static String getTools_AgentMonitorMenuName() {
        return "Agent Monitor...";
    }

    public static String getTools_OpenQuickStartWizardMenuName() {
        return getExecResource(TOOLS_OPENQS_EI18N);
    }

    public static String getTools_ReportConverterMenuName() {
        return "Report Converter...";
    }

    public static String getWindowsMenuName() {
        return getToolResource(WINDOWS_MENU_TI18N);
    }

    public static String getHelpMenuName() {
        return getExecResource(HELP_MENU_EI18N);
    }

    public static String getHelp_OnlineHelpMenuName() {
        return getToolResource(HELP_ONLINEHELP_MENU_TI18N);
    }

    public static String getHelp_AboutJTHarnessMenuName() {
        return getToolResource(HELP_ABOUTJT_MENU_I18N);
    }

    public static String getHelp_AboutJVMMenuName() {
        return getToolResource(HELP_ABOUTJVM_MENU_I18N);
    }

}
