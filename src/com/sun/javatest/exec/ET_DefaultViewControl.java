/*
 * $Id$
 *
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.TestSuite;
import com.sun.javatest.services.ServiceManager;
import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.Debug;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author Dmitry Fazunenko
 */
public class ET_DefaultViewControl implements ET_ViewControl {

    SessionExt config = null;
    UIFactory uif = null;
    JComponent parent = null;
    TestSuite testSuite;
    List<Action> actions = null;
    ExecModel execModel = null;
    EnvironmentBrowser environmentBrowser = null;
    ExcludeListBrowser excludeListBrowser = null;
    QuestionLogBrowser questionLogBrowser = null;
    ChecklistBrowser checkListBrowser = null;
    ET_FilterControl filterControl = null;
    private static int debug = Debug.getInt(BasicSessionControl.class);

    public ET_DefaultViewControl(JComponent parent, TestSuite ts,
            ExecModel execModel, UIFactory uif, ET_FilterControl filterControl) {
        this.parent = parent;
        this.uif = uif;
        this.testSuite = ts;
        this.execModel = execModel;
        this.actions = createActions();
        this.filterControl = filterControl;
    }

    public void setConfig(Session cfg) {
        if (cfg instanceof SessionExt) {
            config = (SessionExt)cfg;
        } else {
            throw new Error(uif.getI18NString("bcc.notSessionExtInstance.err", cfg.getClass()));
        }
    }

    public Session getConfig() {
        return config;
    }

    public void updateGUI() {
        boolean isWD  = (config != null && config.getWorkDirectory() != null);

        // we always allow View/Properties action
        // even if config and/or wd is not set
        propertiesAction.setEnabled(true);

        logViewerAction.setEnabled(isWD);
        if (serviceViewerAction != null) {
            serviceViewerAction.setEnabled(isWD);
        }
        if (showChecklistAction != null) {
            showChecklistAction.setEnabled(config.getInterviewParameters() != null
                    && !config.getInterviewParameters().isChecklistEmpty());
        }
    }

    public void save(Map m) {
        // nothing to save
    }

    public void restore(Map m) {
        // nothing to restore
    }

    public JMenu getMenu() {
        JMenu viewMenu = uif.createMenu("exec.view");

        List<Action> cfgActs = createConfigActions();
        Action[] viewConfigActions = new Action[cfgActs.size()];
        cfgActs.toArray(viewConfigActions);
        JMenu viewConfigMenu = uif.createMenu("exec.view.cfg", viewConfigActions);
        viewMenu.add(viewConfigMenu);

        if (filterControl != null) {
            JMenu filters = filterControl.getFilterMenu();
            if (filters != null) {
                 viewMenu.add(filters);
            }
        }
        viewMenu.addSeparator();
        for (Action action: createActions()) {
            viewMenu.add(action);
        }

        ContextManager cm = execModel.getContextManager();
        JavaTestMenuManager mm = null;
        if (cm != null) {
            mm = cm.getMenuManager();
            if (mm != null) {
                JMenuItem[] items = mm.getMenuItems(JavaTestMenuManager.CONFIG_VIEW);
                if (items != null) {
                    for (int i = 0; i < items.length; i++) {
                        viewConfigMenu.add(items[i]);
                    }
                }
            }
        }
        return viewMenu;

    }

    public List<Action> getToolBarActionList() {
        return null;
    }

    protected List<Action> createActions() {
        if (actions != null) {
            return actions;
        }
        actions = new LinkedList<Action>();
        propertiesAction = createPropertyAction();
        actions.add(propertiesAction);
        logViewerAction = createLogViewerAction();
        actions.add(logViewerAction);
        if (testSuite != null && testSuite.needServices()) {
            serviceViewerAction = createServiceViewerAction();
            actions.add(serviceViewerAction);
        }
        testSuiteErrorsAction = createTestSuiteErrorsAction();
        actions.add(testSuiteErrorsAction);
        return actions;
    }

    public void dispose() {
        // nothing to dispose
    }

    private Action propertiesAction;
    private Action createPropertyAction() {
        return new ToolAction(uif, "exec.view.props") {
            public void actionPerformed(ActionEvent e) {
                if (propertiesBrowser == null) {
                    propertiesBrowser = new PropertiesBrowser(parent, uif);
                }
                propertiesBrowser.showDialog(testSuite,
                        config.getWorkDirectory(), config.getInterviewParameters());
            }

            private PropertiesBrowser propertiesBrowser;
        };
    };

    TestSuiteErrorsDialog testSuiteErrorsDialog = null;
    private Action testSuiteErrorsAction;
    private Action createTestSuiteErrorsAction() {
        return new ToolAction(uif, "exec.view.testSuiteErrors") {
            public void actionPerformed(ActionEvent e) {
                if (testSuiteErrorsDialog == null)
                    testSuiteErrorsDialog = new TestSuiteErrorsDialog(parent, uif);
                testSuiteErrorsDialog.show(testSuite);
            }
        };
    }

    private Action logViewerAction;
    private Action createLogViewerAction() {
        return new ToolAction(uif, "exec.view.logviewer") {
            public void actionPerformed(ActionEvent e) {

                if (config != null && config.getWorkDirectory() != null)
                    openLogViewer();
                else
                    // should not happen: menu item is disabled in this case
                    testSuite.getNotificationLog(null).info(uif.getI18NString("exec.view.logviewer.noworkdir"));
            }
        };
    }

    private void openLogViewer() {
        new LogViewer(config.getWorkDirectory(), uif, parent);
    }

    private ServiceViewer serviceViewer;

    private Action serviceViewerAction;
    private Action createServiceViewerAction() {
        return new ToolAction(uif, "exec.view.serviceviewer") {
            public void actionPerformed(ActionEvent e) {

                if (config != null && config.getWorkDirectory() != null)
                    openServiceViewer();
                else
                    // should not happen: menu item is disabled in this case
                    testSuite.getNotificationLog(null).info(uif.getI18NString("exec.view.serviceviewer.noworkdir"));
            }
        };
    }

    private void openServiceViewer() {
        if (serviceViewer == null) {
            ServiceManager mgr = testSuite.getServiceManager();
            serviceViewer = new ServiceViewer(mgr, uif, parent);
            updateServiceViewer();
        }

        serviceViewer.setVisible(true);
    }

    private void updateServiceViewer() {
        if (serviceViewer != null) {
            ServiceManager mgr = serviceViewer.getServiceManager();
            if (config != null && config.getInterviewParameters() != null) {
                mgr.setParameters(config.getInterviewParameters());
                if (!config.getInterviewParameters().containsObserver(serviceViewer)) {
                    config.getInterviewParameters().addObserver(serviceViewer);
                }
            }
        }
    }

    Action showEnvironmentAction;
    Action showExcludeListAction;
    Action showChecklistAction;
    Action showQuestionLogAction;
    List<Action> createConfigActions() {
        List<Action> acts = new LinkedList<Action>();

        showEnvironmentAction = new ToolAction(uif, "ch.env") {
            public void actionPerformed(ActionEvent e) {
                showEnvironment();
            }
        };

        showExcludeListAction = new ToolAction(uif, "ch.excl") {
            public void actionPerformed(ActionEvent e) {
                showExcludeList();
            }
        };

        showChecklistAction = new ToolAction(uif, "ch.checkList") {
            public void actionPerformed(ActionEvent e) {
                showChecklist();
            }
        };

        showQuestionLogAction = new ToolAction(uif, "ch.quLog") {
            public void actionPerformed(ActionEvent e) {
                showQuestionLog();
            }
        };
        acts.add(showEnvironmentAction);
        acts.add(showExcludeListAction);
        acts.add(showChecklistAction);
        acts.add(showQuestionLogAction);
        return acts;
    }

    void ensureInterviewUpToDate() {
        try {
            config.reloadInterview();
        } catch (Exception ex) {
            if (debug > 0) {
                ex.printStackTrace(Debug.getWriter());
            }
            uif.showError("exec.loadInterview", ex.toString());
        }
    }

    boolean isConfigEdited() {
        return false;
    }
    boolean isOKToContinue() {
        return true;
    }

    void showEnvironment() {
        ensureInterviewUpToDate();

        if (isConfigEdited() && !isOKToContinue())
            return;

        if (environmentBrowser == null)
            environmentBrowser = new EnvironmentBrowser(parent, uif);

        environmentBrowser.show(config.getInterviewParameters());
    }

    void showExcludeList() {
        ensureInterviewUpToDate();

        if (isConfigEdited() && !isOKToContinue())
            return;

        if (excludeListBrowser == null)
            excludeListBrowser = new ExcludeListBrowser(parent, uif);

        excludeListBrowser.show(config.getInterviewParameters());
    }

    void showChecklist() {
        ensureInterviewUpToDate();

        if (isConfigEdited() && !isOKToContinue())
            return;

        if (checkListBrowser == null)
            checkListBrowser = new ChecklistBrowser(parent, execModel, uif);

        checkListBrowser.setVisible(true);
    }

    void showQuestionLog() {
        ensureInterviewUpToDate();

        if (isConfigEdited() && !isOKToContinue())
            return;

        if (questionLogBrowser == null)
            questionLogBrowser = new QuestionLogBrowser(parent, execModel, uif);

        questionLogBrowser.setVisible(true);
    }

}
