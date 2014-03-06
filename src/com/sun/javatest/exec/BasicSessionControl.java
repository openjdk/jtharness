/*
 * $Id$
 *
 * Copyright (c) 2010, 2012, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.ExcludeListUpdateHandler;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters;
import com.sun.javatest.Parameters.LegacyEnvParameters;
import com.sun.javatest.Parameters.MutableConcurrencyParameters;
import com.sun.javatest.Parameters.MutableExcludeListParameters;
import com.sun.javatest.Parameters.MutableKeywordsParameters;
import com.sun.javatest.Parameters.MutablePriorStatusParameters;
import com.sun.javatest.Parameters.MutableTestsParameters;
import com.sun.javatest.Parameters.MutableTimeoutFactorParameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.exec.BasicSession.E_NewWD;
import com.sun.javatest.exec.Session.Event;
import com.sun.javatest.exec.Session.Fault;
import com.sun.javatest.tool.FileHistory;
import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.tool.WorkDirChooser;
import com.sun.javatest.util.Debug;
import com.sun.javatest.util.I18NResourceBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Class that encapsulate logic of user's actions on update session:
 * operations on configuration and work directory.
 * Some methods are added to preserve old functionality...
 *
 * @author Dmitry Fazunenko
 */
public class BasicSessionControl implements InterviewEditor.Observer,
        RunTestsHandler.Observer, ET_SessionControl, Session.Observer {


    protected final SessionExt session;
    protected final TestSuite testSuite;
    protected UIFactory uif;
    protected JComponent parent;
    protected JPanel sessionView;
    protected InterviewEditor interviewEditor;
    protected ContextManager cm;

    ChangeConfigMenu changeMenu;
    Action loadConfigAction;
    Action newConfigAction;
    Action showFullConfigAction;
    Action showStdConfigAction;
    Action newWorkDirAction;
    Action openWorkDirAction;

    FileHistory.Listener configHistoryListener;
    JMenu menuHistory;

    private static KeyStroke configEditorAccelerator =
        KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK);

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(BasicSessionControl.class);
    private static int debug = Debug.getInt(BasicSessionControl.class);
    private boolean doConfig = false; // flag indicating that configure() method is running

    /**
     * Creates a control over new created session for the passed test suite.
     * @param parent
     * @param uif
     * @param ts
     * @param cm
     * @throws com.sun.javatest.exec.Session.Fault
     */
    public BasicSessionControl(JComponent parent, UIFactory uif, TestSuite ts,
            ContextManager cm) throws Fault {
        this.testSuite = ts;
        this.cm = cm;
        this.uif = uif;
        this.parent = parent;
        session = createEmptySession();
        initActions();
        initHistoryListeners();
    }

    /**
     * Returns the session object under control
     */
    public Session getSession() {
        return session;
    }

    /**
     * Returns the panel reflecting the current state of the session
     */
    public JComponent getViewComponent() {
        if (sessionView == null) {
            sessionView = createSessionView();
        }
        return sessionView;
    }

    /**
     * Returns array of actions for the tool bar.
     */
    Action[] getToolBarActions() {
        return new Action[] {
            showFullConfigAction,
            showStdConfigAction,
        };
    }

    public List<Action> getToolBarActionList() {
        return Arrays.asList(getToolBarActions());
    }

    public void save(Map m) {
        if (testSuite != null && testSuite.getRoot() != null)
            m.put("testSuite", testSuite.getRoot().getPath());

        // save work dir and ip
        session.save(m);
    }

    public void restore(Map m) {
        try {
            session.restore(m);
        } catch (Fault e) {
            if (debug > 0) {
                e.printStackTrace(Debug.getWriter());
            }
            uif.showError("exec.cantRestoreSession", e.getMessage());
        }
    }

    public void dispose() {
        session.dispose();
        if (interviewEditor != null) {
            interviewEditor.dispose();
            interviewEditor = null;
        }
    }

    /**
     * Creates an empty configuration for the test suite. By default BasicSession
     * is used. It's supposed for subclasses to override this method.
     * @return created session.
     * @throws com.sun.javatest.exec.Session.Fault
     */
    protected SessionExt createEmptySession() throws Fault {
        return new BasicSession(testSuite);
    }

    /**
     * Clones passed parameters. Works only for InterviewParameters instances.
     * @param p instance to clone, might be null.
     * @return cloned object obtained for the passed one by performing save/load
     * operations.
     * @throws com.sun.javatest.exec.Session.Fault
     */
    public static Parameters clone(Parameters p) throws Session.Fault {
        if (p == null) {
            return null;
        }
        if (p instanceof InterviewParameters) {
            try {
                InterviewParameters ip = (InterviewParameters)p;
                InterviewParameters clone = ip.getWorkDirectory().getTestSuite().createInterview();
                clone.setWorkDirectory(ip.getWorkDirectory());
                HashMap data = new HashMap();
                ip.save(data);
                clone.load(data, false);
                return clone;
            } catch (Exception e) {
                throw new Session.Fault(e);
            }

        }
        throw new IllegalStateException(i18n.getString("bcc.cantClonParameters.err", p));
    }

    public void ensureInterviewUpToDate() {
        try {
            session.reloadInterview();
        } catch (Exception ex) {
            if (debug > 0) {
                ex.printStackTrace(Debug.getWriter());
            }
            uif.showError("exec.loadInterview", ex.toString());
        }
    }

    /**
     * Invoked when runTestHandler is going to start test execution
     */
    public void startTests(Parameters p) {
        if (getNeedToAutoCheckExcludeList(p)) {
            checkExcludeListUpdate(parent, false, p);
        }
    }

    /**
     * Invoked when runTestHandler completed test execution
     */
    public void finishTests(Parameters p) {
    }

    public void whatToDoWhenConfigNotReadyButUserPressedStartButton(
            final Action startAction) {
        final InterviewParameters ip = session.getInterviewParameters();


        // configuration is incomplete, possibly not even started:
        // post a message explaining that the wizard is required.
        int option;

        if (!ip.isTemplate() && ip.isStarted()) {
            String errorMessage = ip.getErrorMessage();
            if (errorMessage != null) {
                option = uif.showOKCancelDialog("rh.configError", errorMessage);
            } else {
                option = uif.showOKCancelDialog("rh.completeConfigure");
            }
        } else {
            option = uif.showOKCancelDialog("rh.startConfigure");
        }
        if (option != JOptionPane.OK_OPTION) {
            startAction.setEnabled(true);
            return;
        }

        if (ip.isTemplate()) {
            newConfig();
        }

        showConfigEditor(new ActionListener() {
            public void actionPerformed(ActionEvent e2) {
                // configuration has been completed: post a message to remind
                // user that tests will now be run
                if (ip.isTemplate() ||
                        !ip.isFinishable()) {
                    startAction.setEnabled(true);
                }
                else {
                    int option = uif.showOKCancelDialog("rh.configDone");
                    if (option == JOptionPane.OK_OPTION) {
                        startAction.actionPerformed(null);
                    }
                }
            }
        });
    }

    void editConfigWithErrorMessage() {
        final InterviewParameters ip = session.getInterviewParameters();
        String errorMessage = ip.getErrorMessage();
        int option = (errorMessage == null) ?
            uif.showOKCancelDialog("rh.mustConfigure") :
            uif.showOKCancelDialog("rh.configError", errorMessage);

        // show the session editor if user clicks ok
        if (option == JOptionPane.OK_OPTION) {
            if (ip.isTemplate()) {
                newConfig();
            } else {
                showConfigEditor(null);
            }
        }
    }

    protected void checkExcludeListUpdate(JComponent parent, boolean quietIfNoUpdate, Parameters params) {
        if (params instanceof InterviewParameters) {
            InterviewParameters ip = (InterviewParameters)params;
            checkExcludeListUpdate(parent, quietIfNoUpdate, ip,
                    ip.getTestSuite(), ip.getWorkDirectory(), uif);

        }
    }

    static void checkExcludeListUpdate(JComponent parent, boolean quietIfNoUpdate,
            Parameters interviewParams, TestSuite testSuite,
            WorkDirectory workDir, UIFactory uif) {
        try {
            InterviewParameters.ExcludeListParameters elp = interviewParams.getExcludeListParameters();
            if ( !(elp instanceof InterviewParameters.MutableExcludeListParameters))
                return;

            URL remote = testSuite.getLatestExcludeList();
            File local = workDir.getSystemFile("latest.jtx");
            ExcludeListUpdateHandler eluh = new ExcludeListUpdateHandler(remote, local);

            if (quietIfNoUpdate && !eluh.isUpdateAvailable())
                return;

            JPanel info = new JPanel(new GridBagLayout());
            info.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
            GridBagConstraints lc = new GridBagConstraints();
            lc.anchor = GridBagConstraints.EAST;
            GridBagConstraints fc = new GridBagConstraints();
            fc.gridwidth = GridBagConstraints.REMAINDER;
            fc.fill = GridBagConstraints.HORIZONTAL;
            fc.weightx = 1;

            JLabel remoteLbl = uif.createLabel("ch.elu.remote");
            info.add(remoteLbl, lc);

            JTextField remoteText = uif.createOutputField("ch.elu.remote", remoteLbl);
            remoteText.setBorder(null);
            // should consider better date formatting; is this i18n-ok?
            long remoteDate =  eluh.getRemoteURLLastModified();
            String remoteDateText = (remoteDate <= 0 ?
                                     uif.getI18NString("ch.elu.notAvailable")
                                     : new Date(remoteDate).toString());
            remoteText.setText(remoteDateText);
            remoteText.setColumns(remoteDateText.length());
            info.add(remoteText, fc);

            JLabel localLbl = uif.createLabel("ch.elu.local");
            info.add(localLbl, lc);

            JTextField localText = uif.createOutputField("ch.elu.local", localLbl);
            localText.setBorder(null);
            // should consider better date formatting; is this i18n-ok?
            long localDate =  eluh.getLocalFileLastModified();
            String localDateText = (localDate <= 0 ?
                                    uif.getI18NString("ch.elu.notAvailable")
                                    : new Date(localDate).toString());
            localText.setText(localDateText);
            localText.setColumns(localDateText.length());
            info.add(localText, fc);

            if (eluh.isUpdateAvailable()) {
                String title = uif.getI18NString("ch.elu.update.title");
                String head = uif.getI18NString("ch.elu.update.head");
                String foot = uif.getI18NString("ch.elu.update.foot");
                int rc = JOptionPane.showConfirmDialog(parent,
                                                       new Object[] { head, info, foot },
                                                       title,
                                                       JOptionPane.YES_NO_OPTION );
                if (rc == JOptionPane.YES_OPTION)
                    eluh.update(); // should we show message if successful?
            }
            else {
                String title = uif.getI18NString("ch.elu.noUpdate.title");
                String head = uif.getI18NString("ch.elu.noUpdate.head");
                JOptionPane.showMessageDialog(parent,
                                              new Object[] { head, info },
                                              title,
                                              JOptionPane.INFORMATION_MESSAGE );
            }
        }
        catch (IOException e) {
            workDir.log(uif.getI18NResourceBundle(), "ch.elu.logError", e);
            uif.showError("ch.elu.error", e);
        }

        // The following lines are to keep the i18N checks happy, because it is difficult
        // to invoke the various code paths that create the JOptionPanes
        //      getI18NString("ch.elu.local.lbl")
        //      getI18NString("ch.elu.local.tip")
        //      getI18NString("ch.elu.remote.lbl")
        //      getI18NString("ch.elu.remote.tip")

    }


    // arguably, this ought to be in InterviewParameters/BasicParameters
    protected boolean getNeedToAutoCheckExcludeList(Parameters params) {
        WorkDirectory workDir = params.getWorkDirectory();
        InterviewParameters.ExcludeListParameters elp = session.getParameters().getExcludeListParameters();
        if ( !(elp instanceof InterviewParameters.MutableExcludeListParameters))
            return false;

        InterviewParameters.MutableExcludeListParameters melp = (InterviewParameters.MutableExcludeListParameters) elp;
        if (melp.getExcludeMode() != InterviewParameters.MutableExcludeListParameters.LATEST_EXCLUDE_LIST)
            return false;

        // double check there is a URL to download from
        if (params.getTestSuite().getLatestExcludeList() == null)
            return false;

        if (!melp.isLatestExcludeAutoCheckEnabled())
            return false;

        if (melp.getLatestExcludeAutoCheckMode() == InterviewParameters.MutableExcludeListParameters.CHECK_EVERY_RUN)
            return true;

        File local = workDir.getSystemFile("latest.jtx");
        if (!local.exists())
            return true;

        long localLastModified = local.lastModified();
        long now = System.currentTimeMillis();
        int ageInDays = melp.getLatestExcludeAutoCheckInterval();
        long ageInMillis = 24L * 60 * 60 * 1000 * ageInDays;

        return (ageInDays > 0) && (now > (localLastModified + ageInMillis));
    }


    public JMenu getMenu() {
        JMenu menu = uif.createMenu("ch");
        // I hope some day workdir action will become a part
        // of "Configure" menu, not "File"
        //menu.add(uif.createMenuItem(newWorkDirAction));
        //menu.add(uif.createMenuItem(openWorkDirAction));
        //menu.addSeparator();


        // add Edit Configuration
        JMenuItem menuEdit = uif.createMenuItem(showFullConfigAction);
        menuEdit.setIcon(null);
        menuEdit.setAccelerator(configEditorAccelerator);
        menu.add(menuEdit);


        // add Edit Quick Set
        menu.add(changeMenu);

        menu.addSeparator();
        menu.add(uif.createMenuItem(newConfigAction));
        menu.add(uif.createMenuItem(loadConfigAction));

        // custom menu items
        // note: JavaTestMenuManager is deprecated mechanism
        // one should provide an alternative ET_ControlFactory implementation
        // instead.
        JavaTestMenuManager menuManager = null;
        if (cm != null) {
            menuManager = cm.getMenuManager();
            if (menuManager != null) {
                JMenuItem[] items =
                    menuManager.getMenuItems(JavaTestMenuManager.CONFIG_PRIMARY);
                if (items != null)
                    for (int i = 0; i < items.length; i++)
                        menu.add(items[i]);
            }
        }



        //menu.addMenuListener(configHistoryListener);
        menu.add(menuHistory);

        if (cm != null) {
            menuManager = cm.getMenuManager();
            if (menuManager != null) {
                JMenuItem[] items =
                    menuManager.getMenuItems(JavaTestMenuManager.CONFIG_OTHER);
                if (items != null) {
                    menu.addSeparator();
                    for (int i = 0; i < items.length; i++)
                        menu.add(items[i]);
                }   // innerest if
            }
        }
        return menu;
    }

    /**
     * Initializes interviewEditor.
     * @return true if initialized successfully, false if failed.
     */
    protected boolean initEditor() {
        if (interviewEditor != null) {
            // already initialized
            return true;
        }

        InterviewParameters ip = session.getInterviewParameters();
        if (ip == null || ip.getWorkDirectory() == null) {
            return false;
        }
        interviewEditor = createInterviewEditor(ip);
        if (cm != null) {
            interviewEditor.setCustomRenderers(cm.getCustomRenderersMap());
            interviewEditor.setContextManager(cm);
        }
        interviewEditor.setCheckExcludeListListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    JComponent p = (src instanceof JComponent ? (JComponent) src : parent);
                    checkExcludeListUpdate(p, false, session.getParameters());
                }
            });

        interviewEditor.addObserver(this);
        return true;

    }

    /**
     * Creates an InterviewEditor instance. Subclasses might override this
     * method to create an alternative editor.
     *
     * @param ip parameters to be edited
     */
    protected InterviewEditor createInterviewEditor(InterviewParameters ip) {
        return new InterviewEditor(parent, getUIFactory(), ip);
    }

    /**
     * Method returning UIFactory to be used to create InterviewEditor instance.
     */
    protected UIFactory getUIFactory() {
        // this is workaround for template editor
        return uif;
    }

    /**
     * Action to create new configuration.
     */
    void newConfig() {
        if (!initEditor()) {
            return;
        }
        interviewEditor.newConfig();
    }

    /**
     * Action to load configuration.
     */
    void loadConfig() {
        if (!initEditor()) {
            return;
        }
        interviewEditor.loadConfig();
    }

    /**
     * Causes configuration editor to appear. If workdir is not set,
     * suggests to create one first.
     */
    public void edit() {
        if (session.getWorkDirectory() == null) {
            createWD();
            if (session.getWorkDirectory() == null) {
                return;
            }
        }
        showConfig(InterviewEditor.FULL_MODE);
    }

    /**
     * Causes a series of actions to be performed to complete configuration:<br>
     * If session is already ready - does nothing.<br>
     * If work directory is not set - suggests creating or opening one<br>
     * Opens configuration editor.
     */
    public void configure() {
        doConfig = true;
        try {
            if (session.isReady()) {
                return;
            }
            if (session.getWorkDirectory() == null) {
                showWorkDirDialog();
            }

            if (session.getWorkDirectory() == null) {
                return;
            }
            if (!session.isReady()) {
                showConfigureDialog();
            }
        } finally {
            doConfig = false;
        }
    }

    /**
     * @return true if configure() method is running or configuration is editing.
     */
    public boolean isConfiguring() {
        return doConfig || isEditorVisible();
    }

    protected void showWorkDirDialog() {
        ActionListener optionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Component c = (Component) (e.getSource());
                JOptionPane op = (JOptionPane) SwingUtilities.getAncestorOfClass(JOptionPane.class, c);
                op.setValue(c); // JOptionPane expects the value to be set to the selected button
                op.setVisible(false);
            }
        };

        JTextArea msg = uif.createMessageArea("exec.wd.need");
        String title = uif.getI18NString("exec.wd.need.title");
        JButton[] options = {
                uif.createButton("exec.wd.open", optionListener),
                uif.createButton("exec.wd.new", optionListener),
                uif.createCancelButton("exec.wd.cancel", optionListener)
        };
        int option = JOptionPane.showOptionDialog(parent,
                msg,
                title,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);

        switch (option) {
            case JOptionPane.YES_OPTION:
                setWD();
                break;
            case JOptionPane.NO_OPTION:
                createWD();
                break;
            default:
                return;
        }
    }

    protected void showConfigureDialog() {
        int option;
        if (session.getInterviewParameters().isStarted()) {
            String errorMessage = session.getInterviewParameters().getErrorMessage();
            if (errorMessage != null) {
                option = uif.showOKCancelDialog("rh.configError", errorMessage);
            } else {
                option = uif.showOKCancelDialog("rh.completeConfigure");
            }
        } else {
            option = uif.showOKCancelDialog("rh.startConfigure");
        }
        if (option == JOptionPane.OK_OPTION) {
            showConfig();
        } else {
            doConfig = false;
            session.notifyObservers(new E_EditorVisibility(isEditorVisible(), null));
        }
    }

    /**
     * Method to be invoked right after WD has been created/opened.
     */
    private void showConfig() {
        if (session.getInterviewParameters().getFile() == null) {
            newConfig();
        } else {
            showConfig(InterviewEditor.FULL_MODE);
        }
    }

    /**
     * Action to edit current configuration
     * @param mode
     */
    void showConfig(int mode) {
        if (!initEditor()) {
            return;
        }
        interviewEditor.edit(mode);
    }

    void showConfigEditor(ActionListener actionListener) {
        if (!initEditor()) {
            return;
        }
        interviewEditor.edit(InterviewEditor.FULL_MODE);
    }
    void checkUpdate() {
        System.err.println("Temporary not implemented...");
    }
    void ensureConfigEditorInitialized() {
        /*
        if (workDir == null)
            throw new IllegalStateException();

        if (configEditor == null) {
            configEditor = new ConfigEditor(parent, interviewParams, model, uif);
            configEditor.setCustomRenderers(customRenderersMap);
            configEditor.setCheckExcludeListListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Object src = e.getSource();
                        JComponent p = (src instanceof JComponent ? (JComponent) src : parent);
                        checkExcludeListUpdate(p, false);
                    }
                });

            configEditor.setObserver(new ConfigEditor.Observer() {
                    public void saved(InterviewParameters p) {
                        syncCurrentConfig(p);
                        updateGUI();
                    }
                    public void loaded(InterviewParameters p) {
                        syncCurrentConfig(p);
                        updateGUI();
                    }
            });

        }
         */
    }

    protected void initActions() {
        loadConfigAction = new ToolAction(uif, "ch.load") {
            public void actionPerformed(ActionEvent e) {
                if (session.getWorkDirectory() == null) {
                    showWorkDirDialog();
                }
                loadConfig();
            }
        };

        newConfigAction = new ToolAction(uif, "ch.new") {
            public void actionPerformed(ActionEvent e) {
                if (session.getWorkDirectory() == null) {
                    showWorkDirDialog();
                }
                newConfig();
            }
            @Override
            public Object getValue(String key) {
                if (SessionView.ACTION_NAME.equals(key)){
                    return uif.getI18NString("ch.new2.act");
                }
                return super.getValue(key);
            }
        };

       showFullConfigAction = new ConfigAction(uif, "ch.full",
               InterviewEditor.FULL_MODE, true) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (session.getWorkDirectory() == null) {
                    showWorkDirDialog();
                }
                showConfig(InterviewEditor.FULL_MODE);
            }
            @Override
            public Object getValue(String key) {
                if (SessionView.ACTION_NAME.equals(key)){
                    return uif.getI18NString("ch.full2.act");
                }
                return super.getValue(key);
            }
       };

       showStdConfigAction = new ConfigAction(uif, "ch.std",
               InterviewEditor.STD_MODE, true) {

            @Override
            public void setEnabled(boolean newValue) {
                super.setEnabled(newValue);
                if (changeMenu != null) {
                    changeMenu.checkEnabled();
                }
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isEnabled())
                    return;

                Object control = e.getSource();
                if (control instanceof JMenuItem) {
                    JMenuItem mi = (JMenuItem)control;
                    Integer miMode = (Integer)getValue(mi.getName());
                    performAction(miMode);
                }
                else {
                    performAction(mode);
                }
            }
       };

       changeMenu = new ChangeConfigMenu();

       newWorkDirAction = createNewWorkDirAction();
       openWorkDirAction = createSetWorkDirAction();

    }

    protected ToolAction createNewWorkDirAction() {
        return new ToolAction(uif, "ch.newWorkDir") {
            public void actionPerformed(ActionEvent e) {
                boolean editConfig = createWD();
                if (editConfig) {
                    showConfig();
                }
            };
            @Override
            public Object getValue(String key) {
                if (SessionView.ACTION_NAME.equals(key)){
                    return uif.getI18NString("ch.newWorkDir.act");
                }
                return super.getValue(key);
            }

        };
    }

    protected ToolAction createSetWorkDirAction() {
        return new ToolAction(uif, "ch.setWorkDir") {
            public void actionPerformed(ActionEvent e) {
                boolean editConfig = setWD();
                if (editConfig) {
                    showConfig(InterviewEditor.FULL_MODE);
                }
            }
            @Override
            public Object getValue(String key) {
                if (SessionView.ACTION_NAME.equals(key)){
                    return uif.getI18NString("ch.setWorkDir.act");
                }
                return super.getValue(key);
            }
        };
    }

    protected void initHistoryListeners() {
        configHistoryListener = new FileHistory.Listener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JMenuItem mi = (JMenuItem) (e.getSource());
                    File f = (File) (mi.getClientProperty(FileHistory.FILE));
                    if (f != null) {
                        if (initEditor()) {
                            // ensureConfigEditorInitialized();
                            interviewEditor.loadAndEdit(f);
                        }
                    }
                }
            });
        menuHistory = uif.createMenu("ch.history");
        menuHistory.addMenuListener(configHistoryListener);

    }

    protected JPanel createSessionView() {
        return new SessionView(session);
    }

    public void updateGUI() {

        Parameters params = session.getParameters();

        if (params != null)
            ensureInterviewUpToDate();

        boolean isWorkDirSet = session.getWorkDirectory() != null;
        boolean configCreated = session.getValue(BasicSession.CONFIG_NAME_PROP) != null;
        boolean editorNotVisible = !isEditorVisible();
        showFullConfigAction.setEnabled(params != null && configCreated && editorNotVisible);
        showStdConfigAction.setEnabled(params != null && configCreated && editorNotVisible);
        newWorkDirAction.setEnabled(!isWorkDirSet);
        openWorkDirAction.setEnabled(!isWorkDirSet);

        // you can only configure tests if the test suite is set
        newConfigAction.setEnabled(editorNotVisible);
        loadConfigAction.setEnabled(editorNotVisible);
        changeMenu.checkEnabled();

        menuHistory.setEnabled(editorNotVisible);
        if (isWorkDirSet) {
            FileHistory h;
            if (configHistoryListener.getFileHistory() == null) {
                h = FileHistory.getFileHistory(session.getWorkDirectory(), InterviewEditor.CONFIG_HISTORY);
                configHistoryListener.setFileHistory(h);
            } else {
                h = configHistoryListener.getFileHistory();
            }
            if (h.getLatestEntry() == null) {
                menuHistory.setEnabled(false);
            }
        } else {
            menuHistory.setEnabled(false);
        }

        /*
        showExcludeListAction.setEnabled(testSuiteSet);
        showEnvironmentAction.setEnabled(testSuiteSet);
        showQuestionLogAction.setEnabled(testSuiteSet);
        showChecklistAction.setEnabled(testSuiteSet
                                       && params != null
                                       && !params.isChecklistEmpty());
        checkUpdatesAction.setEnabled(false);

        menuHistory.setEnabled(testSuiteSet);


        if (workDir != null && configHistoryListener.getFileHistory() == null) {
            FileHistory h = FileHistory.getFileHistory(workDir, "configHistory.jtl");
            configHistoryListener.setFileHistory(h);
        }

        if (workDir != null && configTemplateHistoryListener.getFileHistory() == null) {
            FileHistory h = FileHistory.getFileHistory(workDir, "templateHistory.jtl");
            configTemplateHistoryListener.setFileHistory(h);
        }

        if (params != null && observer == null) {
            observer = new Interview.Observer() {
                    public void currentQuestionChanged(Question q) {
                    }
                    public void pathUpdated() {
                        showChecklistAction.setEnabled(!params.isChecklistEmpty());
                    }
                };
            params.addObserver(observer);
        }
*/

    }

    /**
     * @return true if user has an interview editor open.
     */
    protected boolean isEditorVisible() {
        return interviewEditor != null && interviewEditor.isVisible();
    }

    /**
     * InterviewEditor.Observer method.
     * Invoked when current session has changed from InterviewEditor
     * @param p
     */
    public void changed(InterviewParameters p) {
        try {
            session.update(new BasicSession.U_NewConfig(p));
        } catch (Session.Fault e) {
            e.printStackTrace();
            System.err.println(i18n.getString("bcc.internalError.err"));
        }
    }

    /**
     * InterviewEditor.Observer method.
     * Invoked when InterviewEditor is made either visible or invisible.
     * Implementations call updateGUI() to enable/disable actions.
     * @param isVisible - true or false
     */
    public void changedVisibility(boolean isVisible, InterviewEditor editor) {
        updateGUI();
        session.notifyObservers(new E_EditorVisibility(isVisible, editor));
    }

    /**
     * Causes the dialog for new directory creating to appear.
     * Invoked from createNewWorkDirAction.
     * Subclasses might override this method to perform extra actions
     * associated with creating new work directory, like setting template.
     *
     * @return true if configuration editing is required after WorkDir created
     */
    protected boolean createWD() {
         applyWorkDir(WorkDirChooseTool.chooseWD(parent, null, testSuite, WorkDirChooser.NEW));
         return false;
    }

    /**
     * Causes the dialog for work directory selecting to appear.
     * Invoked from createSetWorkDirAction.
     * Subclasses might override this method to perform extra actions
     * associated with setting work directory, like setting template.
     *
     * @return true if configuration editing is required after WorkDir set
     */
    protected boolean setWD() {
        applyWorkDir(WorkDirChooseTool.chooseWD(parent, null, testSuite, WorkDirChooser.OPEN_FOR_GIVEN_TESTSUITE));
        return false;
    }

    /**
     * Applies value of the selected work directory.
     * @param wd
     */
    protected void applyWorkDir(WorkDirectory wd) {
        if (wd != null) {
            try {
                session.update(new BasicSession.U_NewWD(wd));
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    /**
     * Tries to restore latest available configuration for the session.
     * @param wd
     */
    public void restoreConfigFromWD(WorkDirectory wd) throws Fault {
        // This code would better belong to BasicSession
        // but it uses some gui features :(

        FileHistory h = FileHistory.getFileHistory(wd, "configHistory.jtl");
        File latestConfigFile = h.getRelativeLatestEntry(wd.getRoot().getPath(), wd.getPrevWDPath());

        // validate location of session file if needed
        if (latestConfigFile != null) {
            if (!cm.getAllowConfigLoadOutsideDefault()) {
                File defaultConfigLoadPath = InterviewEditor.checkLoadConfigFileDefaults(cm);

                File dir = new File(latestConfigFile.getAbsolutePath().substring(
                        0, latestConfigFile.getAbsolutePath().
                        lastIndexOf(File.separator)));

                boolean isMatch = true;

                if (dir != null && defaultConfigLoadPath != null)
                    try {
                        isMatch = (dir.getCanonicalPath().indexOf
                                ((defaultConfigLoadPath.getCanonicalPath())) == 0);
                    } catch (IOException ioe) {
                        // use logging subsystem instead when available
                        // Internal error in ExecToolManager: exception thrown:
                        uif.showError("exec.internalError", ioe);
                        return;
                    }

                if (!isMatch) {
                    // resetWorkDirectory();
                    uif.showError("ce.load.notAllowedDir", defaultConfigLoadPath);
                    return;
                }
            }
            loadInterviewFromFile(wd, latestConfigFile);
            h.add(latestConfigFile);

            // final long time = System.currentTimeMillis();
            // interviewParams.load(latestConfigFile);
            //logLoadTime("exec.log.iload",System.currentTimeMillis()-time,
            //        workDir, latestConfigFile.getAbsolutePath());

        }
    }

    void loadInterviewFromFile(WorkDirectory wd, File latestConfigFile) {
        try {
            session.loadInterviewFromFile(wd, latestConfigFile);
        } catch (Fault e) {
            uif.showError("exec.internalError", e);
        }
    }

    public void updated(Event ev) {
        if (ev instanceof BasicSession.E_NewWD) {
            E_NewWD e_WD = (BasicSession.E_NewWD)ev;
            if (e_WD.doRestoreConfig) {
                try {
                    restoreConfigFromWD(e_WD.wd);
                } catch (Fault f) {
                    f.printStackTrace();
                }
            }
        }
    }

    /**
     * Event to be sent out when Editor become visible/invisible
     */
    protected class E_EditorVisibility implements Event {
        public final boolean isVisible;
        public final InterviewEditor source;
        E_EditorVisibility(boolean isVisible, InterviewEditor source) {
            this.isVisible = isVisible;
            this.source = source;
        }
    }

    protected class SessionView extends JPanel implements Session.Observer {

        protected Session session;
        protected JLabel wd_n;
        protected JLabel conf_n;
        protected JTextField wd_f;
        protected JTextField conf_f;
        public static final String ACTION_NAME = "actionName";
        protected final Color COLOR_NOT_READY = uif.getI18NColor("bcc.notready");
        protected final Color COLOR_READY = uif.getI18NColor("bcc.ready");

        protected SessionView(Session session) {
            super();
            this.session = session;
            this.session.addObserver(this);
            init();
            layoutComponents();
        }

        protected void init() {

            wd_n = uif.createLabel("bcc.WorkDir");
            wd_f = uif.createOutputField("bcc.WorkDir", wd_n, true);
            //wd_f.setFont(BOLD_FONT);
            wd_f.setBorder(BorderFactory.createEmptyBorder());

            conf_n = uif.createLabel("bcc.Configuration");
            conf_f = uif.createOutputField("bcc.Configuration", conf_n, true);
            //conf_f.setFont(BOLD_FONT);
            conf_f.setBorder(BorderFactory.createEmptyBorder());
        }

        protected void layoutComponents() {
            GridBagConstraints gridBagConstraints;

            setLayout(new GridBagLayout());

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new Insets(0, 3, 0, 3);
            add(wd_n, gridBagConstraints);

            boolean wdReady = session.getParameters().getWorkDirectory() != null;

            updateWD(wd_f, wdReady);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.insets = new Insets(0, 3, 0, 3);
            add(wd_f, gridBagConstraints);


            conf_n.setHorizontalAlignment(SwingConstants.RIGHT);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new Insets(0, 3, 0, 3);
            add(conf_n, gridBagConstraints);

            updateCfg(conf_f, wdReady);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.insets = new Insets(0, 3, 0, 3);
            add(conf_f, gridBagConstraints);

        }

        protected void updateField(JTextField fld, String text) {
            String tipText = null;
            if (text == null) {
                text = "NONE";
                tipText = "Not set yet";
            } else {
                tipText = text;
                int index = text.lastIndexOf(File.separator);
                if (index >=0) {
                    text = text.substring(index + 1);
                }
            }
            fld.setText(text);
            fld.setToolTipText(tipText);
            //fld.setFont(BOLD_FONT);
        }
        protected void updateWD(JTextField fld, boolean isReady) {
            String text = session.getValue(BasicSession.WD_PROP);
            updateField(fld, text);
            /*
            if (isReady) {
                fld.setForeground(COLOR_READY);
                fld.setFont(BOLD_FONT);
            } else {
                fld.setForeground(COLOR_NOT_READY);
            }*/
        }

        protected void updateCfg(JTextField fld, boolean isWDReady) {
            String text = session.getValue(BasicSession.CONFIG_NAME_PROP);
            updateField(fld, text);
            /*
            if (!isWDReady || session.isReady()) {
                fld.setForeground(COLOR_READY);
                fld.setFont(BOLD_FONT);
            } else {
                fld.setForeground(COLOR_NOT_READY);
            }*/

        }

        public void updated(Event ev) {
            boolean wdReady = session.getParameters().getWorkDirectory() != null;
            updateWD(wd_f, wdReady);
            updateCfg(conf_f, wdReady);
        }
    }

    private class ChangeConfigMenu extends JMenu implements MenuListener {

        ChangeConfigMenu() {
            uif.initMenu(this, "ch.change");
            tests       = addMenuItem(CHANGE_TESTS,             InterviewEditor.STD_TESTS_MODE);
            excludeList = addMenuItem(CHANGE_EXCLUDE_LIST,      InterviewEditor.STD_EXCLUDE_LIST_MODE);
            keywords    = addMenuItem(CHANGE_KEYWORDS,          InterviewEditor.STD_KEYWORDS_MODE);
            kfl         = addMenuItem(CHANGE_KFL,               InterviewEditor.STD_KFL_MODE);
            priorStatus = addMenuItem(CHANGE_PRIOR_STATUS,      InterviewEditor.STD_PRIOR_STATUS_MODE);
            environment = addMenuItem(CHANGE_ENVIRONMENT,       InterviewEditor.STD_ENVIRONMENT_MODE);
            concurrency = addMenuItem(CHANGE_CONCURRENCY,       InterviewEditor.STD_CONCURRENCY_MODE);
            timeoutFactor = addMenuItem(CHANGE_TIMEOUT_FACTOR,  InterviewEditor.STD_TIMEOUT_FACTOR_MODE);
            addMenuListener(this);
        }

        public void checkEnabled() {
            if (!(tests.isEnabled() || excludeList.isEnabled() ||
                    keywords.isEnabled() || priorStatus.isEnabled() ||
                    environment.isEnabled() || concurrency.isEnabled() ||
                    timeoutFactor.isEnabled() || kfl.isEnabled())) {
                setEnabled(false);
            }
            else {
                setEnabled(!isEditorVisible());
            }
        }

        private JMenuItem addMenuItem(String action, int configEditorMode) {
            JMenuItem mi = uif.createMenuItem(showStdConfigAction);
            mi.setIcon(null);
            mi.setName(action);
            mi.setText(uif.getI18NString("ch.change" + "." + action + ".mit"));
            mi.setMnemonic(uif.getI18NMnemonic("ch.change" + "." + action + ".mne"));
            showStdConfigAction.putValue(action, configEditorMode);
            add(mi);
            return mi;
        }

        // ---------- from MenuListener -----------
        public void menuSelected(MenuEvent e) {
            Parameters c = session.getParameters(); // alias, to save typing
            if (c == null) // if null, should not even be enabled
                return;

            // Update the various menu items depending whether the corresponding parameters
            // can be handled by the Standard Values view -- ie whether the corresponding
            // parameters are mutable
            // Note: can't ask configEditor and hence stdView directly, since they
            // may not have been initialized yet

            update(tests,       c.getTestsParameters(),         MutableTestsParameters.class);
            update(excludeList, c.getExcludeListParameters(),   MutableExcludeListParameters.class);
            update(keywords,    c.getKeywordsParameters(),      MutableKeywordsParameters.class);
            update(priorStatus, c.getPriorStatusParameters(),   MutablePriorStatusParameters.class);
            update(environment, c.getEnvParameters(),           LegacyEnvParameters.class);
            update(concurrency, c.getConcurrencyParameters(),   MutableConcurrencyParameters.class);
            update(timeoutFactor, c.getTimeoutFactorParameters(), MutableTimeoutFactorParameters.class);
        }

        private void update(JMenuItem mi, Object o, Class c) {
            mi.setVisible(o != null && c.isAssignableFrom(o.getClass()));
        }

        public void menuDeselected(MenuEvent e) {
        }

        public void menuCanceled(MenuEvent e) {
        }
/*
        void loadInterview(File file) {
            if (initEditor()) {
                interviewEditor.load(file);
            }
        }
*/

        // ----------

        private JMenuItem tests;
        private JMenuItem excludeList;
        private JMenuItem keywords;
        private JMenuItem kfl;
        private JMenuItem priorStatus;
        private JMenuItem environment;
        private JMenuItem concurrency;
        private JMenuItem timeoutFactor;

        private static final String CHANGE_TESTS = "test";
        private static final String CHANGE_EXCLUDE_LIST = "excl";
        private static final String CHANGE_KEYWORDS = "keyw";
        private static final String CHANGE_KFL = "kfl";
        private static final String CHANGE_PRIOR_STATUS = "stat";
        private static final String CHANGE_ENVIRONMENT = "envt";
        private static final String CHANGE_CONCURRENCY = "conc";
        private static final String CHANGE_TIMEOUT_FACTOR = "time";
    };


    private class ConfigAction extends ToolAction {
        public int mode;

        public ConfigAction(UIFactory uif, String key, int mode, boolean needIcon) {
            super(uif, key, needIcon);
            this.mode = mode;
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;

            performAction(mode);
        }

        protected void performAction(Integer mode) {
            showConfig(mode);
        }

    }

}
