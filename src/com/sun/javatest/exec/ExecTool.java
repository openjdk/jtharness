/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.interview.Interview.Fault;
import com.sun.javatest.Harness;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.ToolManager;
import com.sun.javatest.tool.UIFactory;
import java.awt.BorderLayout;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.print.Printable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;


public class ExecTool extends Tool implements ExecModel,
        BasicSession.OrderedObserver {

    SessionExt session;
    ContextManager context;
    final ExecToolManager etm;
    final TestSuite testSuite;
    final ET_TestTreeControl testTreePanel;
    final ET_SessionControl sessionControl;
    final ET_RunTestControl runTestsHandler;
    final ET_ReportControl reportHandler;
    final ET_FilterHandler filterHandler;
    final List<ET_Control> controls = new ArrayList<ET_Control>();

    JMenuBar menuBar = null;
    HashMap map = new HashMap();
    private boolean shouldPauseTree;
    private PageFormat pageFormat;

    public ExecTool(ExecToolManager mgr, TestSuite ts) throws Session.Fault {
        super((ToolManager)mgr, "exec", "browse.window.csh");
        this.testSuite = ts;
        String testSuiteName = testSuite.getName();
        if (testSuiteName != null)
            setShortTitle(testSuiteName);
        this.etm = mgr;
        context = createContextManager();
        ET_ControlFactory controlFactory = context.getExecToolControlFactory(this, uif);
        ET_PrivateControlFactory prviateFactory = new ET_PrivateControlFactory(this, uif, this);


        sessionControl = controlFactory.createSessionControl();
        Session s = sessionControl.getSession();
        if (s instanceof SessionExt) {
            session = (SessionExt)s;
        } else {
            throw new Error(uif.getI18NString("bcc.notSessionExtInstance.err", s.getClass()));
        }
        context.setCurrentConfig(session);
        controls.add(sessionControl);


        runTestsHandler =  prviateFactory.createRunTestControl();
        runTestsHandler.setConfig(session);
        controls.add(runTestsHandler);

        reportHandler = controlFactory.createReportControl();
        controls.add(reportHandler);

        ET_FilterControl fControl = controlFactory.createFilterControl();
        if (fControl instanceof ET_FilterHandler){
            filterHandler = (ET_FilterHandler)fControl;
        } else {
            throw new Error(uif.getI18NString("et.badFilterControl.err"));
        }
        controls.add(filterHandler);

        testTreePanel = prviateFactory.createTestTreeControl();
        testTreePanel.setFilterSelectionHandler(filterHandler.getFilterSelectionHandler());
        testTreePanel.setParameters(session.getParameters());
        runTestsHandler.setTreePanelModel(testTreePanel.getTreePanelModel());
        controls.add(testTreePanel);

        ET_ViewControl viewControl = controlFactory.createViewControl();
        viewControl.setConfig(session);
        controls.add(viewControl);

        ET_HelpControl helpControl = controlFactory.createHelpControl();
        controls.add(helpControl);

        List<ET_Control> customControls = controlFactory.createCustomControls();
        if (customControls != null) {
            controls.addAll(customControls);
        }

        Harness harness = runTestsHandler.getHarness();
        for (ET_Control c:controls) {
            if (c instanceof Session.Observer) {
                session.addObserver((Session.Observer)c);
            }
            if (c instanceof ET_RunTestControl.Observer) {
                runTestsHandler.addObserver((ET_RunTestControl.Observer)c);
            }
            if (c instanceof HarnessAware) {
                ((HarnessAware)c).setHarness(harness);
            }
        }

        session.addObserver(context);
        session.addObserver(this);


//        initGUI();
    }

    ContextManager createContextManager() {
        try {
           ContextManager cm = createContextManager(testSuite);
           if (cm != null) {
               cm.setTestSuite(testSuite);
               if (session != null) {
                   cm.setCurrentConfig(session);
               }
               cm.setTool(this);
           }
           return cm;
        } catch (Exception e) {
            e.printStackTrace();        // XXX rm
            // should print log entry
        }
        return null;
    }
    public static ContextManager createContextManager(TestSuite ts) {
        ContextManager cm = null;
        String cls = null;
        if (ts != null)
            cls = ts.getTestSuiteInfo(TestSuite.TM_CONTEXT_NAME);
        try {
            if (cls == null) {
                // use default implementation
                cm = (ContextManager) ((Class.forName(
                        "com.sun.javatest.exec.ContextManager")).newInstance());
            } else {
                cm = (ContextManager) ((Class.forName(cls, true,
                        ts.getClassLoader())).newInstance());
            }
            cm.setTestSuite(ts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cm;
    }

    public UIFactory getUIF() {
        return uif;
    }


    @Override
    public JMenuBar getMenuBar() {
        if (!this.initialized) {
            return null;
        }
        if (menuBar != null) {
            return menuBar;
        }

        menuBar = new JMenuBar();
        for (ET_Control c:controls) {
            JMenu m = c.getMenu();
            if (m != null) {
                menuBar.add(m);
            }
        }
        return menuBar;
    }

    protected JToolBar getToolBar() {
        ArrayList<Action> v = new ArrayList<Action>();
        for (ET_Control c: controls) {
            List<Action> acts = c.getToolBarActionList();
            if (acts != null) {
                v.addAll(acts);
                v.add(null);
            }
        }
        Action[] toolBarActions = (Action[])v.toArray(new Action[v.size()]);

        Preferences p = Preferences.access();

        JToolBar toolBar = uif.createToolBar("exec.toolbar");
        toolBar.setFloatable(false);
        boolean tbVisible = p.getPreference(TOOLBAR_PREF, "true").equals("true");
        toolBar.setVisible(tbVisible);
        toolBar.getMargin().left = 10;
        toolBar.getMargin().right = 10;

        JLabel lab = uif.createLabel("exec.filter", false);
        JComponent selector = filterHandler.getFilterSelectionHandler().getFilterSelector();
        //lab.setLabelFor(selector);
        lab.setMaximumSize(lab.getPreferredSize());

        toolBar.add(lab);
        toolBar.addSeparator();
        toolBar.add(selector);
        toolBar.addSeparator();

        // now add all the other buttons
        uif.addToolBarActions(toolBar, toolBarActions);
        return toolBar;
    }

    boolean initialized = false;
    @Override
    public void setVisible(boolean f) {
        initGUI(); // does real work just once
        super.setVisible(f);
    }
    protected void initGUI() {
        if (initialized) {
            return;
        }
        initialized = true;

        if (context != null &&
            "com.sun.javatest.exec.ContextManager".equals(context.getClass().getCanonicalName())) {
            context = null;
        }
        setLayout(new BorderLayout());
        if (shouldPauseTree)
            testTreePanel.getTreePanelModel().pauseWork();

        ToolBarPanel toolBarPanel = new ToolBarPanel();
        toolBarPanel.add(getToolBar());


        //add(toolBar, BorderLayout.NORTH);
        add(toolBarPanel, BorderLayout.NORTH);


        add(testTreePanel.getViewComponent(), BorderLayout.CENTER);

        // this panel contains two full-width panels
        JPanel statusStrips = uif.createPanel("exec.strips", false);
        statusStrips.setLayout(new BorderLayout());
        statusStrips.add(sessionControl.getViewComponent(), BorderLayout.NORTH);
        statusStrips.add(runTestsHandler.getViewComponent(), BorderLayout.SOUTH);
        add(statusStrips, BorderLayout.SOUTH);

        testTreePanel.initialize();


        //asavdd(sessionControl.getStateViewPanel(), BorderLayout.SOUTH);
        //filterHandler.updateParameters();
        updateGUI();

    }

    @Override
    protected void save(Map m) {
        for (ET_Control c:controls) {
            c.save(m);
        }
    }

    @Override
    protected void restore(Map m) {
        for (ET_Control c:controls) {
            c.restore(m);
        }
    }

    @Override
    public void dispose() {
        // standard cleanup (remove refs to child components)
        super.dispose();
        if (context != null)
            context.dispose();

        for (ET_Control c:controls) {
            c.dispose();
        }

        // no need to track changes in preferences any more
        //Preferences p = Preferences.access();
        //p.removeObserver(TOOLBAR_PREF, prefsObserver);
        //p.save();

    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    /**
     * @return Array of 1 element - the current testSuite
     */
    @Override
    public TestSuite[] getLoadedTestSuites() {
        return (testSuite == null ? null : new TestSuite[]{testSuite});
    }

    public WorkDirectory getWorkDirectory() {
        return session.getWorkDirectory();
    }

    public InterviewParameters getInterviewParameters() {
        return session.getInterviewParameters();
    }

    public FilterConfig getFilterConfig() {
         return filterHandler.getFilterConfig();
    }

    public ContextManager getContextManager() {
        if (context == null) {
            try {
                context = createContextManager();
                //context = (ContextManager) ((Class.forName(
                //        "com.sun.javatest.exec.ContextManager")).newInstance());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return context;
    }

    public TestResultTable getActiveTestResultTable() {
        WorkDirectory workDir = getWorkDirectory();
        if (workDir != null)
            return workDir.getTestResultTable();
        else if (testTreePanel != null)
            return testTreePanel.getTestResultTable();
        else
            return null;

    }


    /**
     * Returns action creating work directory.
     * This ugly method violates the beautiful picture, but present here to
     * preserve functionality of creating the WD from the File menu.
     * Hopefully some day it will be eliminated.
     *
     * @return real action or null
     */
    Action getCreateWDAction() {
        if (sessionControl instanceof BasicSessionControl) {
            return ((BasicSessionControl)sessionControl).newWorkDirAction;
        }
        return null; // no idea how to create WD
    }

    /**
     * Returns action opening work directory for the TestSuite.
     * This ugly method violates the beautiful picture, but present here to
     * preserve functionality of creating the WD from the File menu.
     * Hopefully some day it will be eliminated.
     *
     * @return real action or null
     */
    Action getOpenWDAction() {
        if (sessionControl instanceof BasicSessionControl) {
            return ((BasicSessionControl)sessionControl).openWorkDirAction;
        }
        return null; // no idea how to create WD
    }

    void updateGUI() {
        if (!initialized) {
            return;
        }
        for (ET_Control c: controls) {
            c.updateGUI();
        }
        updateTitle();
    }

    /**
     * Invoked when manager orders to use new wd.
     * @param wd
     * @throws com.sun.javatest.exec.Session.Fault
     * @see ExecTool#update(WorkDirectory, boolean)
     */
    public void update(WorkDirectory wd) throws Session.Fault {
        // reloading the config is traditional
        session.update(new BasicSession.U_NewWD(wd), true);
    }

    /**
     * Invoked when manager orders to use new wd.
     * @param wd Work dir to update.
     * @param updateConfig - hint whether to reload the configuration from disk
     * @throws com.sun.javatest.exec.Session.Fault
     */
    public void update(WorkDirectory wd, boolean updateConfig) throws Session.Fault {
        session.update(new BasicSession.U_NewWD(wd), updateConfig);
    }

    /**
     * Invoked when manager orders to use new ip
     * @param ip
     * @throws com.sun.javatest.exec.Session.Fault
     */
    public void update(InterviewParameters ip) throws Session.Fault {
        if (session.getInterviewParameters().getFile() != null) {
            // someone already initialized IP
            return;
        }
        session.update(new BasicSession.U_NewConfig(ip));
    }

    /**
     * Session.Observer interface method
     * @param e
     */
    public void updated(Session.Event e) {
        updateGUI();
    }

    /**
     * BasicSession.OrderedObserver interface method.
     */
    public int order() {
        return Integer.MAX_VALUE;
    }

    public void showWorkDirDialog(boolean allowTemplates) {
        throw new UnsupportedOperationException("Not supported already.");
    }

    public void showConfigEditor(boolean runTests) {
        throw new UnsupportedOperationException("Not supported already.");
    }
    public void showTemplateEditor() {
        throw new UnsupportedOperationException("Not supported already.");
    }

    /**
     * Invoked after QSW
     */
    public void showConfigEditor() {
        sessionControl.edit();
    }

    /**
     * Invoked after QSW
     */
    public void runTests() {
        runTestsHandler.runTests();
    }


    public void runTests(String[] urls) {
        if (urls == null || urls.length == 0)
            // error dialog?
            return;
        runTestsHandler.executeImmediate(urls);
    }

    public void showMessage(ResourceBundle msgs, String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void printSetup() {
        PageFormat pf = PrinterJob.getPrinterJob().pageDialog(
            (pageFormat == null ? PrinterJob.getPrinterJob().defaultPage() : pageFormat));
        pageFormat = (pf == null ? pageFormat : pf);
    }

    public void print(Printable p) {
        //throw new UnsupportedOperationException("Not supported yet.");
        PrinterJob pj = PrinterJob.getPrinterJob();

        pj.setPrintable(p, (pageFormat == null ? pj.defaultPage() : pageFormat));
        boolean result = pj.printDialog();

        if (result) {
            try {
                pj.print();
            } catch(PrinterException e) {
                // Should send to logging system
                //e.printStackTrace();
            }
        }
        else {
            //System.err.println("print job REJECTED!");
            //Send to logging system
        }
    }

    public void setWorkDir(WorkDirectory wd, boolean addToFileHistory) throws Fault, TestSuite.Fault {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Causes a series of actions to be performed to complete configuration.
     */
    public void configure() {
        sessionControl.configure();
    }

    /**
     * @return true if configuring is in progress at the moment.
     */
    public boolean isConfiguring() {
        return sessionControl.isConfiguring();
    }


    public ExecToolManager getExecToolManager() {
        return etm;
    }

    public void showQuickStartWizard() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public boolean isQuickStartWizardShowing() {
        return false;
    }

    void updateTitle() {
        // set the title for the tool, based on the following info:
        // - test suite name
        // - test suite path
        // - work dir path
        // all of which may be null.

        String testSuiteName = (testSuite == null ? null : testSuite.getName());
        WorkDirectory workDir = session.getParameters().getWorkDirectory();
        String workDirPath = (workDir == null ? null : workDir.getRoot().getPath());
        if (testSuite == null)
            setI18NTitle("exec.title.noTS.txt");
        else if (workDirPath == null) {
            if (testSuiteName == null)
                setI18NTitle("exec.title.noWD.txt");
            else {
                setShortTitle(testSuiteName);
                setI18NTitle("exec.title.tsName.txt", testSuiteName);
            }
        } else {
            if (testSuiteName == null)
                if (workDirPath != null) {
                    setI18NTitle("exec.title.wd.txt", workDirPath);
                } else
                    setI18NTitle("exec.title.noTsName.txt");
            else {
                setShortTitle(testSuiteName);
                setI18NTitle("exec.title.tsName_wd.txt", new Object[]{testSuiteName, workDirPath});
            }
        }

    }

    void pauseTreeCacheWork() {
        if (testTreePanel != null &&
                testTreePanel.getTreePanelModel() != null)
            testTreePanel.getTreePanelModel().pauseWork();
        else
            shouldPauseTree = true;
    }

    void unpauseTreeCacheWork() {
        if (testTreePanel != null &&
                testTreePanel.getTreePanelModel() != null) {
            testTreePanel.getTreePanelModel().refreshTree();
            testTreePanel.getTreePanelModel().unpauseWork();

        }
        else
            shouldPauseTree = false;
    }
    // special method to allow saving when the interview was externally modified
    // for JDTS project, may change in the future
    // as of JT 4.3 does nothing...
    void syncInterview() {
        // nothing to do :)
    }

    // legacy
    void loadInterview(File file) {
        if (sessionControl instanceof BasicSessionControl) {
            BasicSessionControl bcc = (BasicSessionControl)sessionControl;
            if (bcc.session.getWorkDirectory() == null) {
                throw new IllegalStateException();
            }
            bcc.loadInterviewFromFile(bcc.session.getWorkDirectory(), file);
        }
    }

    void saveTreeState(Map m) {
        testTreePanel.saveTreeState(m);
    }

    void restoreTreeState(Map m) {
        testTreePanel.restoreTreeState(m);
    }

    static final String TOOLBAR_PREF = "exec.toolbar";
    static final String FILTER_WARN_PREF = "exec.filterWarn";
    static final String TESTS2RUN_PREF = "exec.tests2runPop";
    static final String ACTIVE_FILTER = "filter";

}
