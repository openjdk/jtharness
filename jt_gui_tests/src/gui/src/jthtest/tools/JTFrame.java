/*
 * $Id$
 *
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.tools;

import java.awt.Component;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.swing.JDialog;
import jthtest.ReportCreate.ReportCreate;
import jthtest.Test;
import jthtest.Tools;
import jthtest.menu.QSWizard;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;
import static jthtest.Tools.SimpleStringComparator;
import static jthtest.Tools.getExecResource;
import static jthtest.Tools.getToolResource;

public class JTFrame {

    public static boolean closeQSOnOpen = true;

    static {
        String qs = System.getProperty("jt_gui_test.QSdefault");
        if (qs != null) {
            closeQSOnOpen = Boolean.parseBoolean(qs);
        }
    }
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
    private JFrameOperator mainFrame;
    private Configuration configuration;
    private JMenuBarOperator menuBar;
    private TestSuite testSuite;
    private WorkDirectory workDirectory;

    public void closeQS() {
        new JDialogOperator(mainFrame, getExecResource("qsw.title")).close();
    }

    public void closeQSGently() {
//            new JDialogOperator(mainFrame, getExecResource("qsw.title")).close();
        try {
            new Thread() {

                public void run() {
                    JDialog d = JDialogOperator.findJDialog(mainFrame.getWindow(), new ComponentChooser() {

                        public boolean checkComponent(Component cmpnt) {
                            if (cmpnt instanceof JDialog) {
                                if (((JDialog) cmpnt).getTitle().equals(getExecResource("qsw.title"))) {
                                    return true;
                                }
                            }
                            return false;
                        }

                        public String getDescription() {
                            return "searching QuickStartWizard";
                        }
                    });
                    if (d != null) {
                        new JDialogOperator(d).close();
                    }
                }
            }.start();
        } catch (Exception e) {
        }
    }

    private void init() {
        configuration = new Configuration(this);
        testSuite = new TestSuite(this);
        workDirectory = new WorkDirectory(this);
        mainFrame = Tools.findMainFrame();
        if (closeQSOnOpen) {
            closeQSGently();
        }
    }

    public JTFrame() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        Tools.startJavatest();
        init();
    }

    public JTFrame(boolean newDesktop) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        if (newDesktop) {
            Tools.startJavatestNewDesktop();
        } else {
            Tools.startJavatest();
        }
        init();
    }

    public JTFrame(String params) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        Tools.startJavatest(params);
        init();
    }

    public JTFrame(String params[]) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        Tools.startJavatest(params);
        init();
    }

    public JTFrame(String testsuite, String workdir, String config) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        Tools.startJavatest(testsuite, workdir, config);
        init();
    }

    public static JTFrame startJTWithDefaultWorkDirectory() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        closeQSOnOpen = false;
        return new JTFrame(new String[]{"-NewDesktop", "-open", Test.LOCAL_PATH + File.separator + Test.DEFAULT_WD_NAME});
    }

    public static JTFrame startJTWithDefaultTestSuite() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        closeQSOnOpen = false;
        return new JTFrame(new String[]{"-NewDesktop", "-open", Tools.TEST_SUITE_NAME});
    }

    public static JTFrame startJTWithRunWD() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        closeQSOnOpen = false;
        return new JTFrame(new String[]{"-NewDesktop", "-open", Tools.WD_RUN_NAME});
    }

    public int getTabsCount() {
        JTabbedPaneOperator tabs = new JTabbedPaneOperator(mainFrame);
        return tabs.getTabCount();
    }

    public String getTabName(int i) {
        JTabbedPaneOperator tabs = new JTabbedPaneOperator(mainFrame);
        return tabs.getTitleAt(i);
    }

    public void closeCurrentTool() {
        getFile_CloseMenu().push();
    }

    public void closeAllTools() throws InterruptedException {
        if (mainFrame.isVisible()) {
            Tools.closeAll(mainFrame);
        }
    }

    public void closeFrame() throws InterruptedException {
        Tools.closeJT(mainFrame);
    }

    public JFrameOperator getJFrameOperator() {
        return mainFrame;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public WorkDirectory getWorkDirectory() {
        return workDirectory;
    }

    /////////////////////////////// menu methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public JMenuBarOperator getMenuBar() {
        if (menuBar == null) {
            menuBar = new JMenuBarOperator(mainFrame);
        }
        return menuBar;
    }

    public JMenuOperator getFileMenu() {
        return new JMenuOperator(mainFrame, getFileMenuName());
    }

    public JMenuItemOperator getFile_OpenQuickStartMenu() {
        return getMenuBar().showMenuItem(new String[]{getFileMenuName(), getFile_OpenQuickStartMenuName()});
    }

    public JMenuItemOperator getFile_OpenMenu() {
        return getMenuBar().showMenuItems(new String[]{getFileMenuName()}, new SimpleStringComparator())[1];
    }

    public JMenuItemOperator getFile_RecentWorkDirectoryMenu() {
        return getMenuBar().showMenuItem(new String[]{getFileMenuName(), getFile_RecentWorkDirectoryMenuName()});
    }

    public JMenuItemOperator getFile_PreferencesMenu() {
        return getMenuBar().showMenuItem(new String[]{getFileMenuName(), getFile_PreferencesMenuName()});
    }

    public JMenuItemOperator getFile_CloseMenu() {
        return getMenuBar().showMenuItem(new String[]{getFileMenuName(), getFile_CloseMenuName()});
    }

    public JMenuItemOperator getFile_ExitMenu() {
        return getMenuBar().showMenuItem(new String[]{getFileMenuName(), getFile_ExitMenuName()});
    }

    public JMenuItemOperator getFile_Open_WorkDirectoryMenu() {
        return getMenuBar().showMenuItem(new String[]{getFileMenuName(), getFile_OpenMenuName(), getFile_Open_WorkDirectoryMenuName()}, new SimpleStringComparator());
    }

    public JMenuItemOperator getFile_Open_TestSuiteMenu() {
        return getMenuBar().showMenuItem(new String[]{getFileMenuName(), getFile_OpenMenuName(), getFile_Open_TestSuiteMenuName()}, new SimpleStringComparator());
    }

    public JMenuItemOperator getFile_CreateWorkDirectoryMenu() {
        return getMenuBar().showMenuItem(new String[]{getFileMenuName(), getFile_CreateWorkDirectoryMenuName()});
    }

    public JMenuOperator getConfigureMenu() {
        return new JMenuOperator(mainFrame, getConfigureMenuName());
    }

    public JMenuItemOperator getConfigure_EditConfigurationMenu() {
        return getMenuBar().showMenuItem(new String[]{getConfigureMenuName(), getConfigure_EditConfigurationMenuName()});
    }

    public JMenuItemOperator getConfigure_EditQuickSetMenu() {
        return getMenuBar().showMenuItem(new String[]{getConfigureMenuName(), getConfigure_EditQuickSetMenuName()});
    }

    public JMenuItemOperator getConfigure_EditQuickSet_TestsToRunMenu() {
        return getMenuBar().showMenuItem(new String[]{getConfigureMenuName(), getConfigure_EditQuickSetMenuName(), getConfigure_EditQuickSet_TestsToRunMenuName()});
    }

    public JMenuItemOperator getConfigure_EditQuickSet_ExcludeListMenu() {
        return getMenuBar().showMenuItem(new String[]{getConfigureMenuName(), getConfigure_EditQuickSetMenuName(), getConfigure_EditQuickSet_ExcludeListMenuName()});
    }

    public JMenuItemOperator getConfigure_EditQuickSet_PriorStatusMenu() {
        return getMenuBar().showMenuItem(new String[]{getConfigureMenuName(), getConfigure_EditQuickSetMenuName(), getConfigure_EditQuickSet_PriorStatusMenuName()});
    }

    public JMenuItemOperator getConfigure_EditQuickSet_ConcurrencyMenu() {
        return getMenuBar().showMenuItem(new String[]{getConfigureMenuName(), getConfigure_EditQuickSetMenuName(), getConfigure_EditQuickSet_ConcurrencyMenuName()});
    }

    public JMenuItemOperator getConfigure_EditQuickSet_TimeoutFactorMenu() {
        return getMenuBar().showMenuItem(new String[]{getConfigureMenuName(), getConfigure_EditQuickSetMenuName(), getConfigure_EditQuickSet_TimeoutFactorMenuName()});
    }

    public JMenuItemOperator getConfigure_NewConfigurationMenu() {
        return getMenuBar().showMenuItem(new String[]{getConfigureMenuName(), getConfigure_NewConfigurationMenuName()});
    }

    public JMenuItemOperator getConfigure_LoadConfigurationMenu() {
        return getMenuBar().showMenuItem(new String[]{getConfigureMenuName(), getConfigure_LoadConfigurationMenuName()}, new Tools.SimpleStringComparator());
    }

    public JMenuItemOperator getConfigure_LoadRecentConfigurationMenu() {
        return getMenuBar().showMenuItem(new String[]{getConfigureMenuName(), getConfigure_LoadRecentConfigurationMenuName()});
    }

    public JMenuItemOperator[] getConfigure_LoadRecentConfiguration_subMenu() {
        return getMenuBar().showMenuItems(new String[]{getConfigureMenuName(), getConfigure_LoadRecentConfigurationMenuName()});
    }

    public JMenuOperator getRunTestsMenu() {
        return new JMenuOperator(mainFrame, getRunTestsMenuName());
    }

    public JMenuItemOperator getRunTests_StartMenu() {
        return getMenuBar().showMenuItem(new String[]{getRunTestsMenuName(), getRunTests_StartMenuName()});
    }

    public JMenuItemOperator getRunTests_StopMenu() {
        return getMenuBar().showMenuItem(new String[]{getRunTestsMenuName(), getRunTests_StopMenuName()});
    }

    public JMenuItemOperator getRunTests_MonitorProgressMenu() {
        return getMenuBar().showMenuItem(new String[]{getRunTestsMenuName(), getRunTests_MonitorProgressMenuName()});
    }

    public JMenuOperator getReportMenu() {
        return new JMenuOperator(mainFrame, getReportMenuName());
    }

    public JMenuItemOperator getReport_CreateReportMenu() {
        return getMenuBar().showMenuItem(new String[]{getReportMenuName(), getReport_CreateReportMenuName()});
    }

    public JMenuItemOperator getReport_OpenReportMenu() {
        return getMenuBar().showMenuItem(new String[]{getReportMenuName(), getReport_OpenReportMenuName()});
    }

    public JMenuOperator getViewMenu() {
        return new JMenuOperator(mainFrame, getViewMenuName());
    }

    public JMenuItemOperator getView_ConfigurationMenu() {
        return getMenuBar().showMenuItem(new String[]{getViewMenuName(), getView_ConfigurationMenuName()});
    }

    public JMenuItemOperator getView_FilterMenu() {
        return getMenuBar().showMenuItem(new String[]{getViewMenuName(), getView_FilterMenuName()});
    }

    public JMenuItemOperator getView_Filter_ConfigureFiltersMenu() {
        return getMenuBar().showMenuItem(new String[]{getViewMenuName(), getView_FilterMenuName(), getView_Filter_ConfigureFiltersMenuName()});
    }

    public JMenuItemOperator getView_PropertiesMenu() {
        return getMenuBar().showMenuItem(new String[]{getViewMenuName(), getView_PropertiesMenuName()});
    }

    public JMenuItemOperator getView_LogsMenu() {
        return getMenuBar().showMenuItem(new String[]{getViewMenuName(), getView_LogsMenuName()});
    }

    public JMenuItemOperator getView_TestSuiteErrorsMenu() {
        return getMenuBar().showMenuItem(new String[]{getViewMenuName(), getView_TestSuiteErrorsMenuName()});
    }

    public JMenuItemOperator getView_Configuration_ShowChecklistMenu() {
        return getMenuBar().showMenuItem(new String[]{getViewMenuName(), getView_ConfigurationMenuName(), getView_Configuration_ShowChecklistMenuName()});
    }

    public JMenuItemOperator getView_Configuration_ShowExcludeListMenu() {
        return getMenuBar().showMenuItem(new String[]{getViewMenuName(), getView_ConfigurationMenuName(), getView_Configuration_ShowExcludeListMenuName()});
    }

    public JMenuItemOperator getView_Configuration_ShowQuestionLogMenu() {
        return getMenuBar().showMenuItem(new String[]{getViewMenuName(), getView_ConfigurationMenuName(), getView_Configuration_ShowQuestionLogMenuName()});
    }

    public JMenuItemOperator getView_Configuration_ShowTestEnvironmentMenu() {
        return getMenuBar().showMenuItem(new String[]{getViewMenuName(), getView_ConfigurationMenuName(), getView_Configuration_ShowTestEnvironmentMenuName()});
    }

    public JMenuItemOperator getTools_ReportConverterMenu() {
        return getMenuBar().showMenuItem(new String[]{getToolsMenuName(), getTools_ReportConverterMenuName()});
    }

    public JMenuItemOperator getTools_OpenQuickStartWizardMenu() {
        return getMenuBar().showMenuItem(new String[]{getToolsMenuName(), getTools_OpenQuickStartWizardMenuName()});
    }

    public JMenuItemOperator getTools_AgentMonitorMenu() {
        return getMenuBar().showMenuItem(new String[]{getToolsMenuName(), getTools_AgentMonitorMenuName()});
    }

    public JMenuItemOperator getTools_TestResultsAuditorMenu() {
        return getMenuBar().showMenuItem(new String[]{getToolsMenuName(), getTools_TestResultsAuditorMenuName()});
    }

    /////////////////////////// static name getters ////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
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

    public static String getConfigureMenuName() {
        return getExecResource(CONFIGURE_MENU_EI18N);
    }

    public static String getConfigure_EditConfigurationMenuName() {
        return getExecResource(CONFIGURE_EDIT_MENU_EI18N);
    }

    public static String getConfigure_EditQuickSetMenuName() {
        return getExecResource(CONFIGURE_EDIT_QS_MENU_EI18N);
    }

    public static String getConfigure_EditQuickSet_TestsToRunMenuName() {
        return "Tests To Run ...";
    }

    public static String getConfigure_EditQuickSet_ExcludeListMenuName() {
        return "Exclude List ...";
    }

    public static String getConfigure_EditQuickSet_PriorStatusMenuName() {
        return "Prior Status ...";
    }

    public static String getConfigure_EditQuickSet_ConcurrencyMenuName() {
        return "Concurrency ...";
    }

    public static String getConfigure_EditQuickSet_TimeoutFactorMenuName() {
        return "Timeout Factor ...";
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

    public static String getReportMenuName() {
        return getExecResource(REPORT_MENU_EI18N);
    }

    public static String getReport_CreateReportMenuName() {
        return getExecResource(REPORT_CREATE_MENU_EI18N);
    }

    public static String getReport_OpenReportMenuName() {
        return getExecResource(REPORT_OPEN_MENU_EI18N);
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

    public static String getView_Filter_ConfigureFiltersMenuName() {
        return "Configure Filters ...";
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
        return "Tools";
    }

    public static String getTools_ReportConverterMenuName() {
        return "Report Converter...";
    }

    public static String getTools_OpenQuickStartWizardMenuName() {
        return "Open Quick Start Wizard ...";
    }

    public static String getTools_AgentMonitorMenuName() {
        return "Agent Monitor...";
    }

    /*There is no Test Results Auditor... menu item and it Test Manager...
     * public static String getTools_TestResultsAuditorMenuName() { return
     * "Test Results Auditor..."; }
     */

    public static String getTools_TestResultsAuditorMenuName() {
        return "Test Manager...";
    }

    public void openDefaultTestSuite() {
        testSuite.openTestSuite(Tools.LOCAL_PATH + Tools.TEST_SUITE_NAME);
    }

    public File createWorkDirectoryInTemp() {
        return workDirectory.createWorkDirectoryInTemp();
    }

    public QSWizard openQuickStartWizard() {
        getFile_OpenQuickStartMenu().push();
        return new QSWizard();
    }

    public TestTree getTestTree() {
        return new TestTree(this);
    }

    public Task<Boolean> runTests() {
        return new TestRunner(true);
    }

    public Task<Boolean> runTests(boolean start) {
        return new TestRunner(start);
    }

    public Task<Boolean> runTests(int row) {
        return new TestRunner(row);
    }

    public int getPassedCounter() {
        String actual = new JTextFieldOperator(mainFrame, new NameComponentChooser("br.summ.0")).getText();
        return Integer.parseInt(actual);
    }

    public int getFailedCounter() {
        String actual = new JTextFieldOperator(mainFrame, new NameComponentChooser("br.summ.1")).getText();
        return Integer.parseInt(actual);
    }

    public int getErrorCounter() {
        String actual = new JTextFieldOperator(mainFrame, new NameComponentChooser("br.summ.2")).getText();
        return Integer.parseInt(actual);
    }

    public int getNotRunCounter() {
        String actual = new JTextFieldOperator(mainFrame, new NameComponentChooser("br.summ.3")).getText();
        return Integer.parseInt(actual);
    }

    public int getNotFilteredCounter() {
        String actual = new JTextFieldOperator(mainFrame, new NameComponentChooser("br.summ.4")).getText();
        return Integer.parseInt(actual);
    }

    public int getFilteredCounter() {
        String actual = new JTextFieldOperator(mainFrame, new NameComponentChooser("br.summ.5")).getText();
        return Integer.parseInt(actual);
    }

    public int getAllTestsCounter() {
        String actual = new JTextFieldOperator(mainFrame, new NameComponentChooser("br.summ.6")).getText();
        return Integer.parseInt(actual);
    }

    public void click() {
        mainFrame.clickMouse();
    }

    public ReportDialog openReportDialog() {
        getReport_CreateReportMenu().push();
        return new ReportDialog(new JDialogOperator(getExecResource("nrd.title")), true);
    }

    public ReportDialog openReportDialog(boolean init) {
        getReport_CreateReportMenu().push();
        return new ReportDialog(new JDialogOperator(getExecResource("nrd.title")), init);
    }

    public int checkCounters(int[] counters) {
        for (int i = 0; i < counters.length; i++) {
            int value = counters[i];

            String actual = new JTextFieldOperator(mainFrame, new NameComponentChooser("br.summ." + i)).getText();

            if (!actual.equals(Integer.toString(value))) {
                return i;
            }
        }
        return -1;
    }

    public void waitForCounters(final int[] counters) {
        Task.Waiter countersWaiter = new Task.Waiter(true) {

            @Override
            protected void init() {
                super.maxTimeToWaitMS = 10000;
            }

            @Override
            protected boolean check() {
                for (int i = 0; i < counters.length; i++) {
                    int value = counters[i];

                    String actual = new JTextFieldOperator(mainFrame, new NameComponentChooser("br.summ." + i)).getText();

                    if (!actual.equals(Integer.toString(value))) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            protected String getTimeoutExceptionDescription() {
                return "Timeout occured while waiting for counters '" + Arrays.toString(counters) + "'. Current counters values: '" + Arrays.toString(getCounters()) + "'";
            }
        };

        countersWaiter.waitForDone();
    }

    public int[] getCounters() {
        int[] counters = new int[7];
        for (int i = 0; i < 7; i++) {
            counters[i] = Integer.parseInt(new JTextFieldOperator(mainFrame, new NameComponentChooser("br.summ." + i)).getText());
        }
        return counters;
    }

    /**
     * Creates full (with html, xml and plain) report
     * @param path Path to report directory
     * @param openViewer Open or not report viewer
     * @return
     */
    public ReportChecker createFullReport(String path, boolean openViewer) {
        ReportCreate rc;

        ReportDialog rep = openReportDialog(false);
        rep.setPath(path);
        rep.pushCreate();
        JDialogOperator showReport = ReportDialog.findShowReportDialog();

        if (openViewer) {
            new JButtonOperator(showReport, "Yes").push();
            return new ReportChecker(path, rep);
        } else {
            new JButtonOperator(showReport, "No").push();
            return null;
        }
    }

    private class TestRunner extends Task<Boolean> {

        private int row;

        @Override
        protected void init() {
            setName("RunTestsThread" + (int) (Math.random() * 100));
        }

        public TestRunner(boolean start) {
            super(false);
            this.row = -1;
            start();
        }

        public TestRunner(int row) {
            super(false);
            this.row = row;
            start();
        }

        @Override
        protected void runImpl() {
            try {
                if (row < 0) {
                    getRunTests_StartMenu().push();
                } else {
                    getTestTree().clickPopup(row);

                    new JPopupMenuOperator(mainFrame).pushMenuNoBlock("Execute These Tests");
                    new JButtonOperator(new JDialogOperator("Run Tests"), "Yes").push();
                }
                try {
                    new JButtonOperator(new JDialogOperator("View Filter Info"), "OK").push();
                } catch (Exception e) {
                }
                waitForExecutionDone();
            } catch (TimeoutExpiredException e) {
                result = false;
                exception = e;
            }
            result = true;
        }
    }

    /**
     * Waits for tests execution done.
     * Attention! This method should be used only when tests are runned without JTFrame.TestRunner.
     * Do not use this method along with runTests()! Use Task.waitForDone() instead.
     * @see #runTests()
     * @see #runTests(boolean)
     * @see Task#waitForDone()
     */
    public void waitForExecutionDone() {
        try {
            final JTextFieldOperator filed1 = new JTextFieldOperator(mainFrame, new NameComponentChooser("strip.msg"));
            Task.Waiter waiter = new Task.Waiter(false) {

                @Override
                protected void init() {
                    setName("FinishedRunWaiter" + (int) (Math.random() * 10));
                }

                @Override
                protected boolean check() {
                    return Tools.getExecResource("strip.finish").equals(filed1.getText());
                }
            };
            waiter.maxTimeToWaitMS = 600000;
            waiter.start();
            waiter.waitForDone();
        } catch (TimeoutExpiredException e) {
        }
    }
}
