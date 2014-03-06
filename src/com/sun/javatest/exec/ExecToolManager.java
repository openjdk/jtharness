/*
 * $Id$
 *
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.exec;

import com.sun.interview.Interview;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.sun.javatest.InterviewParameters;
//import com.sun.javatest.TemplateUtilities;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.FileOpener;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.tool.PreferencesPane;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.tool.TestSuiteChooser;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.ToolManager;
import com.sun.javatest.tool.WorkDirChooser;
import javax.swing.JMenu;
import javax.swing.KeyStroke;


/**
 * The ToolManager for {@link ExecTool test manager} windows.
 */
public class ExecToolManager extends ToolManager implements QSW_Listener
{
    /**
     * Create an ExecManager to manage the test manager windows on a desktop.
     * @param desktop the desktop for which this manager is responsible
     */
    public ExecToolManager(Desktop desktop) {
        super(desktop);
        emptyTool = new EmptyTool(this, "empty");
    }

    @Override
    public FileOpener[] getFileOpeners() {
        return fileOpeners;
    }

    @Override
    public Action[] getFileMenuActions() {
        if (QuickStartWizard.isQswDisabled()) {
            return new Action[0];
        } else {
            return new Action[] {openQuickStartAction};
        }

        // action is performed.
        // The openWorkDirAction is always enabled, but we stash private data
        // on it in case a default test suite is required.
/*
        boolean done = false;
        Desktop d = getDesktop();

        // if not got a test suite yet, check if the user's directory is a test suite;
        // if so, remember that filename (don't open the test suite because it might not
        // be needed.)
        if (!done) {
            try {
                if (TestSuite.isTestSuite(userDir)) {
                    openWorkDirAction.putValue("testSuitePath", userDir);
                    done = true;
                }
            }
            catch (Exception e) {
            }
        }

        // if not got a test suite yet, check if the user's directory is a work directory;
        // if so, remember that filename (don't open the test suite because it might not
        // be needed.)
        if (!done) {
            try {
                if (WorkDirectory.isWorkDirectory(userDir)) {
                    openWorkDirAction.putValue("workDirPath", userDir);
                    done = true;
                }
            }
            catch (Exception e) {
            }
        }


        // if not got a test suite yet, check if the JT installation directory or its parent
        // is a test suite; if so, remember that filename (don't open the test suite because
        // it might not be needed.)
        if (!done) {
            try {
                File classDir = Harness.getClassDir();
                File installDir = (classDir == null ? null : classDir.getParentFile());
                File installParentDir = (installDir == null ? null : installDir.getParentFile());
                if (installDir != null && TestSuite.isTestSuite(installDir)) {
                    openWorkDirAction.putValue("testSuitePath", installDir);
                    done = true;
                }
                else if (installParentDir != null && TestSuite.isTestSuite(installParentDir)) {
                    openWorkDirAction.putValue("testSuitePath", installParentDir);
                    done = true;
                }
            }
            catch (Exception e) {
            }
        }

        if (QuickStartWizard.isQswDisabled()) {
            fileMenuActions = new Action[1];
//            fileMenuActions[0] = newWorkDirAction;
        }

        return fileMenuActions;
 */
    }

    @Override
    public JMenuItem[] getFileMenuPrimaries() {

        // Tricky code:
        // OpenWD action will behave differently depending on the current tool.
        // If ExecTool is active and doesn't have a WD set - openWD will
        // open WD for that ExecTool (for selected TestSuite)
        // In all other cases it will cause opening new tab.
        // It's not allowed to open or recreate WD if it's already open.
        Desktop d = getDesktop();
        Tool currentTool = d.getSelectedTool();
        Action createWD = null;
        Action openWD = openWorkDirAction;
        if (currentTool != null && currentTool instanceof ExecTool) {
            ExecTool et = (ExecTool)currentTool;
            createWD = et.getCreateWDAction();
            openWD = et.getOpenWDAction();
            // Normally, create/open WD actions should become unavailable
            // when a testsuite is associated with a WD.
            // To make those JT users who are addicted to some strange behavior
            // happy, the previous functionality is preserved.
            // I don't abandon a hope to illuminate it some beautiful day though...
            if (createWD == null || !createWD.isEnabled()) {
                createWD = createWorkDirAction;
            }
            if (openWD == null || !openWD.isEnabled()) {
                openWD = openWorkDirAction;
            }
        }
        //

        JMenu openMenu = new JMenu(i18n.getString("tmgr.openMenu.menu"));
        openMenu.setName("tmgr.openMenu");
        // this craziness usually done by UIFactory
        String keyString = i18n.getString("tmgr.openMenu.mne");
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyString);
        openMenu.setMnemonic(keyStroke.getKeyCode());
        openMenu.getAccessibleContext().setAccessibleDescription(i18n.getString("tmgr.openMenu.desc"));
        openMenu.add(new JMenuItem(openWD));
        openMenu.add(new JMenuItem(openTestSuiteAction));
        //openMenu.add(new JMenuItem(openConfigAction));

        return createWD == null ? new JMenuItem[] {openMenu} :
            new JMenuItem[] {new JMenuItem(createWD), openMenu} ;
        /*
        return new JMenuItem[] {
            new JMenuItem(openTestSuiteAction),
            new JMenuItem(openWorkDirAction),
            new JMenuItem(openConfigAction)

        };
         */

    }

    @Override
    public JMenuItem[] getHelpPrimaryMenus() {
        Desktop d = getDesktop();
        Tool t = d.getSelectedTool();
        if (t != null && (t instanceof ExecTool)) {
            ExecTool et = (ExecTool)t;
            ContextManager context = et.getContextManager();
            if (context == null)
                return null;

            JavaTestMenuManager mm = context.getMenuManager();
            if (mm != null)
                return mm.getMenuItems(JavaTestMenuManager.HELP_PRIMARY);
            else
                return null;
        }
        return null;
    }

    @Override
    public JMenuItem[] getHelpTestSuiteMenus() {
        Desktop d = getDesktop();
        Tool t = d.getSelectedTool();
        if (t != null && (t instanceof ExecTool)) {
            ExecTool et = (ExecTool)t;
            ContextManager context = et.getContextManager();
            if (context == null)
                return null;

            JavaTestMenuManager mm = context.getMenuManager();
            if (mm != null)
                return mm.getMenuItems(JavaTestMenuManager.HELP_TESTSUITE);
            else
                return null;

        }
        return null;
    }

    @Override
    public JMenuItem[] getHelpAboutMenus() {
        Desktop d = getDesktop();
        Tool t = d.getSelectedTool();
        if (t != null && (t instanceof ExecTool)) {
            ExecTool et = (ExecTool)t;
            ContextManager context = et.getContextManager();
            if (context == null)
                return null;

            JavaTestMenuManager mm = context.getMenuManager();
            if (mm != null)
                return mm.getMenuItems(JavaTestMenuManager.HELP_ABOUT);
            else
                return null;
        }
        return null;
    }

    @Override
    public Action[] getTaskMenuActions() {
        return null;
    }

    @Override
    public Action[] getWindowOpenMenuActions() {
        if (QuickStartWizard.isQswDisabled()) {
            return new Action[0];
        } else {
            return new Action[] {openQuickStartAction};
        }
    }
    @Override
    public PreferencesPane getPrefsPane() {
        if (prefsPane == null)
            prefsPane = new PrefsPane(getDesktop().getHelpBroker());
        return prefsPane;
    }


    /**
     * If ExecTool have SINGLE_TEST_MANAGER enabled then
     * this method check SINGLE_TEST_MANAGER in all
     * loaded tools and return false if such found.
     *
     * @param newTool new tool which is added to Desktop
     * @param d Desktop to add
     * @return true if there is no conflict with SINGLE_TEST_MANAGER
     *             false otherwise
     */
    boolean checkOpenNewTool(ExecTool newTool, Desktop d) {
        return checkOpenNewTool(d, newTool.getContextManager());
    }

    public boolean checkOpenNewTool(Desktop d, ContextManager conManager) {
        if (conManager != null  && conManager.getFeatureManager().isEnabled(
                        FeatureManager.SINGLE_TEST_MANAGER)) {
            Tool[] tools = d.getTools();
            ArrayList list = new ArrayList();
            for (int i = 0; i < tools.length; i++) {
                if (tools[i] instanceof ExecTool) {
                    ExecTool tool = (ExecTool) tools[i];
                    ContextManager cm = tool.getContextManager();
                    if (cm != null) {
                        FeatureManager fm = cm.getFeatureManager();
                        if (fm.isEnabled(FeatureManager.SINGLE_TEST_MANAGER)) {
                            // only single test manager
                            list.add(tools[i]);
                        }
                    }
                }
            }
            if (list.isEmpty()) {
                return true;
            }
            if (list.size() == 1) {
                if (showCloseQuestion() == JOptionPane.YES_OPTION) {
                    ExecTool old = (ExecTool) list.get(0);
                    old.getDesktop().removeTool(old);
                    old.dispose();
                    return true;
                } else {
                    return false;
                }
            }
            showError("tse.single");
            return false;
        }
        return true;
    }


    class EmptyTool extends Tool {
        EmptyTool(ToolManager m, String uiKey) {
            super(m, uiKey);
        }

        @Override
        public JMenuBar getMenuBar() {
            return new JMenuBar();
        }

        @Override
        protected void save(Map m) {
        }

        @Override
        protected void restore(Map m) {
        }

    }

    /**
     * Shows Quick Configuration Editor if not disabled
     *
     * @return null
     */
    public Tool startTool() {
        showQSW();
        return null;
/*
        Desktop d = getDesktop();
        Tool[] tools = d.getTools();
        if (tools != null) {
            // check to see if there is an empty tool; if so, note it
            for (Tool tool : tools) {
                if (tool instanceof EmptyTool) {
                    return tool;
                }
            }
        }
        Tool t = new EmptyTool(this, "empty");
        d.addTool(t);
        return t;
 */
    }

    /**
     * Start an ExecTool for a particular configuration.
     * @param p the configuration defining the tests and test results to be
     * displayed
     * @return the tool created to show the tests and test results specified
     * by the configuration
     */
    public Tool startTool(InterviewParameters p) {
        if (p != null) {
            return addNewExecTool(p.getTestSuite(), p.getWorkDirectory(), p, "tmgr.errorOpenConfig");
        } else {
            return null;
        }
    }

    public Tool restoreTool(Map m) throws Fault {
        try {
            //
            String tsp = (String) (m.get("testSuite"));
            TestSuite ts = TestSuite.open(new File(tsp));
            ExecTool et = new ExecTool(this, ts);
            et.restore(m);
            return et;

/*
            InterviewParameters ip = getInterview(m);
            if (ip == null) {
                return null;
            }


            TestSuite ts = ip.getTestSuite();
            if (ts == null) {
                return null;
            }

            ExecTool et = new ExecTool(this, ts);
            // it's better to rely on config.restore(m) although...
            // may in be in the next life...
            et.update(ip.getWorkDirectory());
            et.update(ip);
            et.restore(m);
            return et;
 */
        } catch (Exception e) {
            throw new Fault(i18n, "mgr.restoreFaultWD", e.getMessage());
        }
    }

    private static InterviewParameters getInterview(Map m) throws Interview.Fault {
        String tsp = (String) (m.get("testSuite"));
        String wdp = (String) (m.get("workDir"));
        String cfp = (String) (m.get("config"));
        if (isEmpty(tsp) && isEmpty(wdp) && isEmpty(cfp))
            return null;

        return InterviewParameters.open(tsp, wdp, cfp);
    }
    private static boolean isEmpty(String s) {
        return (s == null || s.length() == 0);
    }

    //-------------------------------------------------------------------------

    /**
     * Create an ExecTool instance using the given test suite.
     * @param ts the test suite to seed the new tool with
     * @return tool instance now associated with the given test suite
     * @throws Interview.Fault if there is a problem initializing
     *         the test suite interview parameters
     * @throws TestSuite.Fault if there is a problem while accessing the test
     *         suite object
     * @deprecated
     */
    @Deprecated
    public ExecTool showTestSuite(TestSuite ts)
        throws InterviewParameters.Fault, TestSuite.Fault
    {
        return  addNewExecTool(ts, null, null, "tmgr.errorOpenTestSuite");

/*
        // check to see if there is an empty tool; if so select it
        Desktop d = getDesktop();

        Tool[] tools = d.getTools();
        if (tools != null) {
            // check to see if there is an empty tool; if so, note it
            for (Tool tool : tools) {
                if (tool instanceof EmptyTool) {
                    d.removeTool(tool);
                }
            }
        }

        try {
            ExecTool t = new ExecTool(this, ts);
            if (!checkOpenNewTool(t, d)) {
                t.dispose();
                return null;
            }
            d.addTool(t);
            d.setSelectedTool(t);
            //d.addToFileHistory(ts.getRoot(), testSuiteOpener);
            return t;
        } catch (Session.Fault f) {
            f.printStackTrace();
            return null;
        }
 */
    }

    private TestSuiteChooser getTestSuiteChooser() {
        if (testSuiteChooser == null)
            testSuiteChooser = new TestSuiteChooser();

        return testSuiteChooser;
    }

    void addToFileHistory(TestSuite ts) {
        // for 4.0, we think adding test suites is not useful
        //getDesktop().addToFileHistory(ts.getRoot(), testSuiteOpener);
    }

    //-------------------------------------------------------------------------

    /**
     * Create an ExecTool instance using the given work directory.
     * @param wd the work directory to open
     * @return tool instance now associated with the given work directory
     * @throws Interview.Fault if there is a problem initializing
     *         the test suite interview parameters
     * @throws TestSuite.Fault if there is a problem while accessing the test
     *         suite object
     * @deprecated
     */
    @Deprecated
    public ExecTool showWorkDirectory(WorkDirectory wd)
        throws InterviewParameters.Fault, TestSuite.Fault
    {
        return addNewExecTool(wd.getTestSuite(), wd, null, "tmgr.errorOpenWorkDir");
    }

    void addToFileHistory(WorkDirectory wd) {
        getDesktop().addToFileHistory(wd.getRoot(), workDirOpener);
    }

    void showError(String key) {
        showError(key, (String[]) null);
    }

    void showError(String key, Object arg) {
        showError(key, new Object[] { arg });
    }

    void showError(String key, Object[] args) {
        getUIF().showError(key, args);
    }

    int showCloseQuestion() {
        return getUIF().showYesNoDialog("tse.closeCurrent");
    }


    UIFactory getUIF() {
        if (uif == null)
            uif = new UIFactory(getClass(), getDesktop().getDialogParent(), getDesktop().getHelpBroker());
        return uif;
    }

    protected ExecTool addNewExecTool(TestSuite ts, WorkDirectory wd,
            InterviewParameters ip, String errorKey) {
        Desktop d = getDesktop();
        ExecTool et;
        try {
            et = new ExecTool(ExecToolManager.this, ts);
            if (!checkOpenNewTool(et, d)) {
                return null;
            }
            this.addToFileHistory(ts);
            if (wd != null) {
                et.update(wd, (ip == null));
                addToFileHistory(wd);
            }
            if (wd != null && ip != null) {
                et.update(ip);
            }
        } catch (Session.Fault ex) {
            showError(errorKey, ex.getMessage());
            return null;
        }
        d.addTool(et);
        d.setSelectedTool(et);
        return et;
    }

    //-------------------------------------------------------------------------
    /**
     * QSW_Listener interface method
     *
     * @param ts
     * @param wd
     * @param ip
     * @param showConfigEditorFlag
     * @param runTestsFlag
     */
    public void finishQSW(TestSuite ts, WorkDirectory wd, InterviewParameters ip,
            boolean showConfigEditorFlag, boolean runTestsFlag) {

        qsw = null;
        ExecTool et = addNewExecTool(ts, wd, ip, "tmgr.errorOpenWorkDir");
        if (et == null) {
            return;
        }
        if (showConfigEditorFlag) {
            et.showConfigEditor();
        } else if (runTestsFlag) {
            et.runTests();
        }

    }
    /**
     * QSW_Listener interface method
     */
    public void cancelQSW() {
        qsw = null;
    }

    /**
     * To be invoked when user wants to open Quick Start Wizard
     */
    public void showQSW() {
        if (qsw != null || QuickStartWizard.isQswDisabled()) {
            // QSW already running or disabled
            return;
        }
        qsw = new QuickStartWizard(emptyTool, getDesktop().getLogo(), ExecToolManager.this, getUIF());
        qsw.setVisible(true);
    }

    QuickStartWizard qsw = null;
    private Action openQuickStartAction = new ToolAction(i18n, "mgr.openQuickStart") {
        public void actionPerformed(ActionEvent e) {
            showQSW();
        }
    };

    //-------------------------------------------------------------------------

    private Action openTestSuiteAction = new ToolAction(i18n, "mgr.openTestSuite") {
        public void actionPerformed(ActionEvent e) {
            //System.err.println("EM:openTestSuiteAction " + e);
            TestSuiteChooser tsc = getTestSuiteChooser();
            int action = tsc.showDialog(getDesktop().getDialogParent());
            if (action != JFileChooser.APPROVE_OPTION) {
                return;
            }
            addNewExecTool(tsc.getSelectedTestSuite(), null, null, "tmgr.errorOpenTestSuite");
            tsc.setSelectedTestSuite(null);
        }
    };

    //-------------------------------------------------------------------------

    private Action openWorkDirAction = new ToolAction(i18n, "mgr.openWorkDir") {
        public void actionPerformed(ActionEvent e) {
            WorkDirectory wd = WorkDirChooseTool.chooseWD(emptyTool, null, null, WorkDirChooser.OPEN_FOR_ANY_TESTSUITE);
            if (wd == null) {
                return;
            }
            addNewExecTool(wd.getTestSuite(), wd, null, "tmgr.errorOpenWorkDir");
        }
    };
    private Action createWorkDirAction = new ToolAction(i18n, "mgr.createWorkDir") {
        public void actionPerformed(ActionEvent e) {

            Desktop d = getDesktop();
            Tool currentTool = d.getSelectedTool();
            if (currentTool == null || !(currentTool instanceof ExecTool)) {
                return;
            }
            TestSuite ts = ((ExecTool)currentTool).getTestSuite();

            ExecTool et = addNewExecTool(ts, null, null, "tmgr.errorOpenTestSuite");
            if (et != null) {
                Action act = et.getCreateWDAction();
                if (act != null && act.isEnabled()) {
                    act.actionPerformed(e);
                }
            }
        }
    };
    //-------------------------------------------------------------------------


    //-------------------------------------------------------------------------

    private FileOpener testSuiteOpener = new FileOpener() {
        public String getFileType() {
            return "testSuite";
        }

        public void open(File f) throws FileNotFoundException, Fault {
            try {
                TestSuite ts = TestSuite.open(f);
                addNewExecTool(ts, null, null, "tmgr.errorOpenTestSuite");
            } catch (TestSuite.Fault e) {
                throw new Fault(i18n, "mgr.errorOpeningTestSuite", new Object[] { f, e });
            }
        }
    };

    //-------------------------------------------------------------------------

    private FileOpener workDirOpener = new FileOpener() {
        public String getFileType() {
            return "workDirectory";
        }

        public void open(File f) throws FileNotFoundException, Fault {
            try {
                WorkDirectory wd = WorkDirectory.open(f);
                addNewExecTool(wd.getTestSuite(), wd, null, "tmgr.errorOpenWorkDir");
            } catch (WorkDirectory.Fault e) {
                throw new Fault(i18n, "mgr.errorOpeningWorkDirectory", new Object[] { f, e.getMessage() });
            }

            Preferences prefs = Preferences.access();
            try {
                prefs.setPreference(WorkDirChooseTool.DEFAULT_WD_PREF_NAME,
                                        f.getParentFile().getCanonicalPath());
            }
            catch (IOException e) {}
        }
    };

    public boolean isQuickStartWizardActive() {
        return qsw != null;
    }

    //-------------------------------------------------------------------------

    private TestSuiteChooser testSuiteChooser;
    private PrefsPane prefsPane;
    private boolean doneQuickStart, doneWDChoser;
    private UIFactory uif;

    private static final String EXEC = "exec";
    private static final File userDir = new File(System.getProperty("user.dir"));

    private final Tool emptyTool;

    private FileOpener[] fileOpeners = {
        testSuiteOpener,
        workDirOpener
    };
/*
    private Action[] fileMenuActions = {
        openQuickStartAction,
        //openWorkDirAction,
        //openTestSuiteAction,
    };
 */
}
