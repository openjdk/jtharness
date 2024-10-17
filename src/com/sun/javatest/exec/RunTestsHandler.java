/*
 * $Id$
 *
 * Copyright (c) 2002, 2024, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.Harness;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.exec.Session.Event;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.BackupPolicy;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class RunTestsHandler implements ET_RunTestControl, Session.Observer {
    private final List<Observer> observers = new ArrayList<>();
    private JComponent parent;
    private ExecModel model;
    private Session config;
    //private BasicSessionControl sessionControl;
    private UIFactory uif;
    private TreePanelModel tpm;
    private TestSuite testSuite;
    private WorkDirectory workDir;
    // to track copy of interviewParams in executeImmediate()
    private Parameters localParams;
    private Action showProgressAction;
    private Action startAction;
    private Action stopAction;
    private Harness harness;
    private HarnessObserver observer;
    private MonitorState mState;
    private MessageStrip messageStrip;
    private ProgressMonitor progMonitor;
    // flag indicating that user pressed "run"
    // button while config wasn't ready. And as soon as the config is
    // ready tests will be run
    private boolean waitForConfig;
    // contains paths of tests to run. used together with waitForConfig.
    // to remember paths passed via executeImmediate(paths).
    private String[] whatToRun;

    /*
    private boolean wdReady() {
        if (workDir == null) {
            model.showWorkDirDialog(true);
            if (workDir == null) {
                return false;
            }
        }

        return true;
    }
     */

    RunTestsHandler(JComponent parent, ExecModel model, UIFactory uif) {
        this.parent = parent;
        this.model = model;
        this.uif = uif;

        initActions();
        initHarness();
    }

    /**
     * Merge the session tests to run against the tests selected to run in the
     * configuration.
     *
     * @param requested paths requested by the user to run from the tree. May not be null.
     * @param iTests    paths of tests to run specified in the interview. May not be null.
     * @return Resulting set of test paths that should be run.
     * @see com.sun.javatest.exec.ExecTool#TESTS2RUN_PREF
     * @since 4.2.1
     */
    static String[] reprocessTests2Run(String[] requested, String... iTests) {
        ArrayList<String> result = new ArrayList<>();
        outer:
        for (String curr : requested) {
            for (String iTest : iTests) {
                int slash = curr.lastIndexOf('/');
                int pound = slash == -1 ? curr.lastIndexOf('#') : curr.lastIndexOf('#', slash);

                if (curr.startsWith(iTest) &&
                        (curr.length() == iTest.length() || curr.charAt(iTest.length()) == '#' ||
                                curr.charAt(iTest.length()) == '/')) {
                    result.add(curr);
                    continue outer;
                }
            }   // for j
        }   // for i

        outer2:
        for (String curr : iTests) {
            for (String aRequested : requested) {
                int slash = curr.lastIndexOf('/');
                int pound = slash == -1 ? curr.lastIndexOf('#') : curr.lastIndexOf('#', slash);

                if (curr.startsWith(aRequested) &&
                        (curr.length() == aRequested.length() || curr.charAt(aRequested.length()) == '#' ||
                                curr.charAt(aRequested.length()) == '/')) {
                    result.add(curr);
                    // don't terminate search as in above case
                }
            }   // for j
        }   // for i

        return result.toArray(new String[result.size()]);
    }

    /**
     * This method is normally invoked after QSW...
     */
    @Override
    public void runTests() {
        start();
    }

    @Override
    public void setConfig(Session config) {
        this.config = config;
    }

    @Override
    public void setTreePanelModel(TreePanelModel tpm) {
        this.tpm = tpm;

        if (progMonitor != null) {
            progMonitor.setTreePanelModel(tpm);
        }
    }

    @Override
    public JMenu getMenu() {
        JMenu menu = uif.createMenu("rh");
        menu.add(uif.createMenuItem(startAction));

        /* not ready yet, save for 3.1
        pauseCheckBox = uif.createCheckBoxMenuItem("rh", "pause", false);
        pauseCheckBox.setEnabled(false);
        runMenu.add(pauseCheckBox);
        */

        menu.add(uif.createMenuItem(stopAction));

        // custom menu items
        ContextManager cm = model.getContextManager();
        JavaTestMenuManager mm = null;
        if (cm != null) {
            mm = cm.getMenuManager();
            if (mm != null) {
                JMenuItem[] items =
                        mm.getMenuItems(JavaTestMenuManager.RUN_PRIMARY);
                if (items != null) {
                    for (JMenuItem item : items) {
                        menu.add(item);
                    }
                }
            }
        }

        menu.addSeparator();
        menu.add(uif.createMenuItem(showProgressAction));

        // custom menu items, at the end
        if (mm != null) {
            JMenuItem[] items =
                    mm.getMenuItems(JavaTestMenuManager.RUN_OTHER);
            if (items != null) {
                menu.addSeparator();
                for (JMenuItem item : items) {
                    menu.add(item);
                }
            }
        }

        return menu;
    }

    Action[] getToolBarActions() {
        return new Action[]{
                startAction,
                stopAction
        };
    }

    @Override
    public Harness getHarness() {
        return harness;
    }

    MessageStrip getMessageStrip() {
        if (messageStrip == null) {

            ActionListener zoom = e -> setProgressMonitorVisible(!isProgressMonitorVisible());

            ElapsedTimeMonitor elapsedTime = new ElapsedTimeMonitor(mState, uif);

            boolean showTestRunProgressMonitor =
                    !Boolean.valueOf(System.getProperty(Desktop.TESTRUN_PROGRESSMONITOR_HIDDEN));

            if (showTestRunProgressMonitor) {
                RunProgressMonitor runProgress = new RunProgressMonitor(mState, uif);
                messageStrip = new MessageStrip(uif, new Monitor[] { elapsedTime, runProgress }, mState, zoom);
                messageStrip.setRunningMonitor(runProgress);
                messageStrip.setIdleMonitor(elapsedTime);
            } else {
                messageStrip = new MessageStrip(uif, new Monitor[] { elapsedTime }, mState, zoom);
                messageStrip.setRunningMonitor(elapsedTime);
            }

            harness.addObserver(messageStrip);
        }

        return messageStrip;
    }

    @Override
    public JComponent getViewComponent() {
        return getMessageStrip();
    }

    @Override
    public List<Action> getToolBarActionList() {
        return Arrays.asList(getToolBarActions());
    }

    @Override
    public void save(Map<String, String> m) {
    }

    @Override
    public void restore(Map<String, String> m) {
    }
    //private InterviewParameters interviewParams;

    @Override
    public synchronized void dispose() {
        if (harness != null) {
            harness.stop();
            harness = null;
        }

        parent = null;
        model = null;
        config = null;
        tpm = null;

        if (progMonitor != null) {
            progMonitor.dispose();
        }
    }

    // should really be observing ExecModel
    @Override
    public void updateGUI() {
        testSuite = model.getTestSuite();
        workDir = model.getWorkDirectory();
        //interviewParams = model.getInterviewParameters();
        // initialize the start button once the test suite is set
        if (testSuite != null && !model.isConfiguring()) {
            if (!startAction.isEnabled() && !stopAction.isEnabled()) {
                startAction.setEnabled(true);
            }
        } else {
            startAction.setEnabled(false);
        }

        // if is user press cancel at some moment while configuring
        waitForConfig = waitForConfig && model.isConfiguring();
    }

    /**
     * Starts test run. If configuration is not ready suggests configuring
     * first.
     */
    void start() {
        // UPDATE executeImmediate() if you change code here

        if (!interviewReady()) {
            model.configure();
            waitForConfig = true;
            whatToRun = null;
        }
        startIfReady();

    }

    /**
     * Starts test run if configuration is ready.
     */
    private void startIfReady() {
        startAction.setEnabled(false);
        if (interviewReady()) {
            waitForConfig = false;
            startHarness(config.getParameters());
        } else {
            startAction.setEnabled(!model.isConfiguring());
        }

    }

    /**
     * Handles special reconfiguration required for quick-pick test execution.
     * If not ready, suggests configuring first.
     *
     * @param paths Null or zero length indicates all tests.  Otherwise,
     *              the strings must be root relative locations in the testsuite.
     */
    @Override
    public void executeImmediate(String... paths) {

        if (!interviewReady()) {
            model.configure();
            waitForConfig = true;
            whatToRun = paths;
        }
        executeImmediateIfReady(paths);
    }

    /**
     * Starts test run if configuration is ready.
     */
    private void executeImmediateIfReady(String... paths) {
        if (!interviewReady()) {
            startAction.setEnabled(!model.isConfiguring());
            return;
        }
        waitForConfig = false;
        startAction.setEnabled(false);
        Parameters params = config.getParameters();
        // if we reach this point, we have a usable interview which
        // we can now alter it
        Object[] items = {params.getEnv().getName(),
                TestTreePanel.createNodeListString(TestTreePanel.createNodeList((Object[]) paths))};
        int option = 0;
        if (paths[0].isEmpty()) {
            option = uif.showYesNoDialog("rh.confirmQuickAll",
                    params.getEnv().getName());
        } else {
            JPanel p = uif.createPanel("rh.confirmPanel", false);
            JTextArea msg = uif.createMessageArea("rh.confirmQuick",
                    params.getEnv().getName());
            p.setLayout(new BorderLayout());
            p.add(msg, BorderLayout.NORTH);
            DefaultListModel<String> model = new DefaultListModel<>();
            for (int i = paths.length; i > 0; i--) {
                model.add(model.getSize(), paths[model.getSize()]);
            }

            JList<?> list = uif.createList("rh.confirmList", model);
            p.add(uif.createScrollPane(list,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

            option = uif.showCustomYesNoDialog("rh.confirmQuick", p);
        }

        if (option != JOptionPane.YES_OPTION) {
            startAction.setEnabled(true);
            return;
        }

        // copy interview
        if (localParams != null && (localParams instanceof InterviewParameters)) {
            ((InterviewParameters) localParams).dispose();
        }

        try {
            localParams = BasicSessionControl.clone(params);
        } catch (Session.Fault e) {
            throw new RuntimeException("TBD: i18n cannot clone parameters");
        }

        Preferences p = Preferences.access();
        boolean useTests2Run = p.getPreference(ExecTool.TESTS2RUN_PREF, "true").equals("true");

        if (!useTests2Run) {
            // alter tests in interview
            // (should verify that TestsParameters is mutable)
            Parameters.TestsParameters tps = localParams.getTestsParameters();
            Parameters.MutableTestsParameters mtps = (Parameters.MutableTestsParameters) tps;

            if (paths == null || paths.length == 0 || paths[0].isEmpty()) {
                mtps.setTestsMode(Parameters.MutableTestsParameters.ALL_TESTS);
            } else {
                mtps.setTestsMode(Parameters.MutableTestsParameters.SPECIFIED_TESTS);
                // validate them?
                mtps.setTests(paths);
            }
        } else {
            if (paths == null || paths.length == 0 || paths[0].isEmpty()) {
                // execute whatever is selected in configuration
            } else {
                Parameters.TestsParameters tps = localParams.getTestsParameters();
                Parameters.MutableTestsParameters mtps = (Parameters.MutableTestsParameters) tps;
                if (mtps.getTestsMode() == Parameters.MutableTestsParameters.ALL_TESTS) {
                    mtps.setTests(paths);
                } else {
                    // NOTE: the combined set of paths isn't optimized after being
                    // combined, code elsewhere takes care of this for us
                    mtps.setTestsMode(Parameters.MutableTestsParameters.SPECIFIED_TESTS);
                    String[] origTests = mtps.getSpecifiedTests();

                    if (origTests == null || origTests.length == 0) {
                        mtps.setTests(paths);
                    } else {
                        String[] combined = reprocessTests2Run(paths, origTests);
                        if (combined == null || combined.length == 0) {
                            uif.showInformationDialog("rh.nointersection", (Object[]) paths);
                            return;
                        }
                        mtps.setTests(combined);
                    }
                }
            }
        }

        // start harness
        startHarness(localParams);
    }

    private boolean interviewReady() {
//      sessionControl.ensureInterviewUpToDate();

        return workDir != null && config.isReady();

/*
        interviewParams = model.getInterviewParameters();

        if (!interviewParams.isTemplate() && interviewParams.isFinishable())  {
            return true;
        } else {
            return false;
        }
  */
    }

    private void startHarness(Parameters ips) {
//        sessionControl.beforeExecution();
        notifyStarted(ips); // things like update EL, propagation, etc.
        try {
            if (!config.getParameters().getWorkDirectory().getTestResultTable().isReady()) {
                // get bundle not done inline to avoid i18n check problems
                I18NResourceBundle i18n = uif.getI18NResourceBundle();
                messageStrip.showMessage(i18n, "rh.waitToStart.txt");
            }

            harness.start(ips);
//            sessionControl.afterExecution();
            notifyFinished(ips);
        } catch (Harness.Fault e) {
            uif.showError("rh", e.toString());
        }
    }

    /**
     * Initialize the harness object used by the tool to run tests.
     */
    private void initHarness() {
        harness = new Harness();

        // FIX
        // Set backup parameters; in time this might become more versatile:
        // for example, using preferences
        // Should also probably be static or at least shared between instances
        // Should be moved down into harness land? or at least reused by
        // batch mode and regtest; note: Preferences are currently a GUI feature.
        BackupPolicy backupPolicy = new BackupPolicy() {
            private int numBackupsToKeep = Integer.getInteger("javatest.backup.count", 5).intValue();
            private String[] ignoreExtns = StringArray.split(System.getProperty("javatest.backup.ignore", ".jtr"));

            @Override
            public int getNumBackupsToKeep(File file) {
                return numBackupsToKeep;
            }

            @Override
            public boolean isBackupRequired(File file) {
                if (ignoreExtns != null) {
                    for (String ignoreExtn : ignoreExtns) {
                        if (file.getPath().endsWith(ignoreExtn)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        };

        harness.setBackupPolicy(backupPolicy);
        observer = new HarnessObserver();
        harness.addObserver(observer);

        mState = new MonitorState(harness);
    }

    private boolean isProgressMonitorVisible() {
        if (progMonitor == null || !progMonitor.isVisible()) {
            return false;
        } else {
            return true;
        }
    }

    private void setProgressMonitorVisible(boolean state) {
        if (progMonitor == null) {
            progMonitor = new ProgressMonitor(parent, uif, mState);
            progMonitor.setTreePanelModel(tpm);
        }

        progMonitor.setVisible(state);
    }

    private void initActions() {
        showProgressAction = new ToolAction(uif, "rh.progress") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setProgressMonitorVisible(true);
            }
        };

        startAction = new ToolAction(uif, "rh.start", true) {
            @Override
            public void actionPerformed(ActionEvent e) {
                start();
            }
        };

        stopAction = new ToolAction(uif, "rh.stop", true) {
            {
                // this action is initially disabled, and only enabled when
                // tests are actually being run
                setEnabled(false);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                harness.stop();
            }
        };

    }

    @Override
    public void addObserver(Observer obs) {
        if (obs != null && !observers.contains(obs)) {
            observers.add(obs);
        }
    }

    @Override
    public void removeObserver(Observer obs) {
        if (obs != null && observers.contains(obs)) {
            observers.remove(obs);
        }
    }

    public void notifyStarted(Parameters p) {
        for (Observer obs : observers) {
            obs.startTests(p);
        }
    }

    public void notifyFinished(Parameters p) {
        for (Observer obs : observers) {
            obs.finishTests(p);
        }
    }

    @Override
    public void updated(Event ev) {
        if (ev instanceof BasicSession.E_NewConfig) {
            if (waitForConfig) {
                waitForConfig = false;
                if (whatToRun == null) {
                    startIfReady();
                } else {
                    executeImmediateIfReady(whatToRun);
                }
            }
        } else if (ev instanceof BasicSessionControl.E_EditorVisibility) {
            updateGUI();
        }
    }


    private class HarnessObserver implements Harness.Observer {
        @Override
        public void startingTestRun(Parameters params) {
            EventQueue.invokeLater(() -> {
                startAction.setEnabled(false);
                //pauseCheckBox.setEnabled(true);
                stopAction.setEnabled(true);

                // get bundle not done inline to avoid i18n check problems
                I18NResourceBundle i18n = uif.getI18NResourceBundle();
                messageStrip.showMessage(i18n, "rh.starting.txt");
            });
        }

        @Override
        public void finishedTestRun(boolean allOK) {
            EventQueue.invokeLater(() -> {
                startAction.setEnabled(true);
                //pauseCheckBox.setEnabled(false);
                stopAction.setEnabled(false);
            });

            if (localParams != null && (localParams instanceof InterviewParameters)) {
                ((InterviewParameters) localParams).dispose();
                localParams = null;
            }
        }

        @Override
        public void error(final String msg) {
            EventQueue.invokeLater(() -> uif.showError("rh.error", msg));
        }
    }
}
