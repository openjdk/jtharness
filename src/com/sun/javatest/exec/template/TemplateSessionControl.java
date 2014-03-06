/*
 * $Id$
 *
 * Copyright (c) 2010, 2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.exec.template;

import com.sun.interview.Interview;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters;
import com.sun.javatest.TemplateUtilities;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.exec.BasicSession;
import com.sun.javatest.exec.BasicSession.E_NewConfig;
import com.sun.javatest.exec.BasicSessionControl;
import com.sun.javatest.exec.Session;
import com.sun.javatest.exec.Session.Event;
import com.sun.javatest.exec.Session.Fault;
import com.sun.javatest.exec.ContextManager;
import com.sun.javatest.exec.FeatureManager;
import com.sun.javatest.exec.InterviewEditor;
import com.sun.javatest.exec.SessionExt;
import com.sun.javatest.exec.WorkDirChooseTool;
import com.sun.javatest.exec.WorkDirChooseTool.ExecModelStub;
import com.sun.javatest.tool.FileHistory;
import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.tool.WorkDirChooser;
import com.sun.javatest.util.Debug;
import com.sun.javatest.util.I18NResourceBundle;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Class extending BasicSessionControl with support template related actions.
 *
 * @author Dmitry Fazunenko
 */
public class TemplateSessionControl extends BasicSessionControl {

    final InterviewParameters template; // short cut to the template instance
    protected Action newTemplateAction;
    protected Action showTemplateAction;
    protected Action loadTemplateAction;
    protected Action checkUpdatesAction;
    protected TemplateEditor templateEditor;
    protected TemplateSession templateSession;
    private FileHistory.Listener configTemplateHistoryListener;
    private JMenu menuHistory;
    private static boolean debug = Debug.getBoolean(TemplateSessionControl.class);
    protected UIFactory uifOrig;
    protected TU_dialog tu_dialog;

    private static KeyStroke tEditorAccelerator =
        KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK);

    public TemplateSessionControl(JComponent parent, UIFactory uif, TestSuite ts,
            ContextManager cm, UIFactory uifOrig) throws Fault {
        super(parent, uif, ts, cm);
        this.uifOrig = uifOrig;
        templateSession = (TemplateSession)session;
        template = templateSession.templ;
        tu_dialog = createTU_Dialog();
        TU_ViewManager.register(templateSession.getInterviewParameters(), tu_dialog);
        initExtraActions();
    }


    /**
     * Creates an empty session for the test suite.
     * @return created session instance of TemplateSession
     * @throws com.sun.javatest.exec.Session.Fault
     */
    @Override
    protected SessionExt createEmptySession() throws Fault {
        return new TemplateSession(testSuite);
    }

    /**
     * Invokes propagator if needed
     * @param ev
     */
    @Override
    public void updated(Event ev) {
        super.updated(ev);
        if (needCheckForUpdates(ev)) {
            templateSession.getInterviewParameters().checkForUpdates();
        }
    }

    /**
     * Checks, if propagator needs to be invoked.
     * @param ev
     * @return true, if propagator is needed to be invoked
     */
    protected boolean needCheckForUpdates(Event ev) {
        if (ev instanceof E_EditorVisibility) {
            E_EditorVisibility event = (E_EditorVisibility)ev;
            if ( event.source != null && event.source instanceof TemplateEditor &&
                    !event.isVisible &&
                    templateSession.getValue(BasicSession.CONFIG_NAME_PROP) != null &&
                    templateSession.getValue(TemplateSession.TEMPLATE_PROP_NAME) != null) {
                return true;
            } else {
                return false;
            }
        } else if (ev instanceof E_NewConfig) {
            return true;
        }
        return false;
    }

    @Override
    public void startTests(Parameters p) {
        super.startTests(p);
        ((InterviewParameters)p).checkForUpdates();
    }

    @Override
    public void updateGUI() {
        super.updateGUI();
        WorkDirectory wd = template.getWorkDirectory();
        boolean isWorkDirSet = wd != null;
        boolean configCreated = templateSession.getValue(
                BasicSession.CONFIG_NAME_PROP) != null;
        boolean templateCreated = templateSession.getValue(
                TemplateSession.TEMPLATE_PROP_NAME) != null;

        boolean editorNotVisible = !isEditorVisible();
        newTemplateAction.setEnabled(isWorkDirSet && editorNotVisible);
        loadTemplateAction.setEnabled(isWorkDirSet && editorNotVisible);
        showTemplateAction.setEnabled(isWorkDirSet && templateCreated && editorNotVisible);
        checkUpdatesAction.setEnabled(configCreated && templateCreated && editorNotVisible);
        menuHistory.setEnabled(editorNotVisible);
        if (isWorkDirSet && configTemplateHistoryListener.getFileHistory() == null) {
            FileHistory h = FileHistory.getFileHistory(wd, TemplateEditor.TEMPLATE_HISTORY);
            configTemplateHistoryListener.setFileHistory(h);
        }
    }
    /**
     * @return true if user has an interview editor open.
     */
    @Override
    protected boolean isEditorVisible() {
        return super.isEditorVisible() || templateEditor != null && templateEditor.isVisible();
    }

    @Override
    public JMenu getMenu() {
        JMenu menu = super.getMenu();

        FeatureManager fm = null;
        if (cm != null) {
            fm = cm.getFeatureManager();
        }

        if (cm == null || fm.isEnabled(FeatureManager.TEMPLATE_CREATION)) {
            menu.addSeparator();
            menu.add(uif.createMenuItem(newTemplateAction));
            JMenuItem editTemplateItem = uif.createMenuItem(showTemplateAction);
            editTemplateItem.setAccelerator(tEditorAccelerator);
            menu.add(editTemplateItem);
            menu.add(uif.createMenuItem(loadTemplateAction));
            menu.add(menuHistory);
        }

        if (cm == null || fm.isEnabled(FeatureManager.SHOW_TEMPLATE_UPDATE)) {
            menu.addSeparator();
            menu.add(uif.createMenuItem(checkUpdatesAction));
        }

        return menu;

    }

    @Override
    protected JPanel createSessionView() {
        return new TemplateConfigView(session);
    }

    @Override
    public void dispose() {
        template.dispose();
        TU_ViewManager.unregister(templateSession.getInterviewParameters());
        if (tu_dialog != null) {
            tu_dialog.dispose();
                        tu_dialog = null;
        }
        if (templateEditor != null) {
            templateEditor.dispose();
            templateEditor = null;
        }

        super.dispose();
    }

    protected void initExtraActions() {
        I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TemplateSessionControl.class);
        // uif doesn't work here, because uif.getI18NResourceBundle() will
        // return not overriden bundle
        newTemplateAction = new ToolAction(i18n, "tcc.file.new") {
            public void actionPerformed(ActionEvent e) {
                newTemplate();
            }
        };
        showTemplateAction = new ToolAction(i18n, "tcc.file.edit") {
            public void actionPerformed(ActionEvent e) {
                editTemplate();
            }
        };
        loadTemplateAction = new ToolAction(i18n, "tcc.file.load") {
            public void actionPerformed(ActionEvent e) {
                loadTemplate();
            }
        };
        checkUpdatesAction = new ToolAction(i18n, "tcc.file.chUpdate") {
            public void actionPerformed(ActionEvent e) {
                checkUpdate();
            }
        };
    }

    @Override
    protected void initHistoryListeners() {
        super.initHistoryListeners();
        configTemplateHistoryListener = new FileHistory.Listener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JMenuItem mi = (JMenuItem) (e.getSource());
                    File f = (File) (mi.getClientProperty(FileHistory.FILE));
                    if (f != null) {
                        if (initTemplateEditor()) {
                            // ensureConfigEditorInitialized();
                            templateEditor.loadAndEdit(f);
                        }
                    }
                }
            });
        menuHistory = uif.createMenu("tcc.templatehistory");
        menuHistory.addMenuListener(configTemplateHistoryListener);
    }

    /**
     * Reaction on the "New Template" action
     */
    void newTemplate() {
        if (!initTemplateEditor()) {
            return;
        }
        templateEditor.newConfig();
    }

    /**
     * Reaction on the "Load Template" action
     */
    void loadTemplate() {
        if (!initTemplateEditor()) {
            return;
        }
        templateEditor.loadConfig();
    }

    /**
     * Reaction on the "Edit Template" action
     */
    void editTemplate() {
        if (!initTemplateEditor()) {
            return;
        }
        templateEditor.edit(InterviewEditor.FULL_MODE);
    }

    /**
     * Reaction on the "Check for Updates" action
     */
    protected void checkUpdate() {
        try {
            boolean wasUpdate = false;
            String whichUpdate = null;
            InterviewParameters cfg = templateSession.getInterviewParameters();
            WorkDirectory wd = cfg.getWorkDirectory();
            final long start = System.currentTimeMillis();
            if (cfg.isFileNewer())  {
                wasUpdate = cfg.load();
                whichUpdate = "exec.log.iload";

            } else {
                wasUpdate = cfg.checkForUpdates();
                whichUpdate = "exec.log.iupdate";
            }

            final long time = System.currentTimeMillis();
            if (!wasUpdate) {
                uif.showInformation("exec.noUpdate");
            } else {
                if (wd == null) {
                    return;
                }

                Logger log = null;
                try {
                    log = wd.getTestSuite().createLog(wd, null, uif.getI18NString("exec.log.name"));
                } catch (TestSuite.DuplicateLogNameFault f) {
                    try {
                        log = wd.getTestSuite().getLog(wd, uif.getI18NString("exec.log.name"));
                    } catch (TestSuite.NoSuchLogFault f2) {
                        return;
                    }
                }

                if (log != null) {
                    Integer loadTime = new Integer((int) (time / 1000));
                    Object[] params = new Object[]{loadTime,
                        cfg.getFile().getAbsolutePath()};
                    String output = uif.getI18NString(whichUpdate, params);
                    log.info(output);

                    if (debug)
                        Debug.println(output);
                }
            }
        } catch (IOException ex) {
            uif.showError("tcc.loadTemplate", ex.toString());
        } catch (InterviewParameters.Fault ex) {
            uif.showError("tcc.loadTemplate", ex.getMessage());
        }
    }

    /**
     * Initializes interviewEditor.
     * @return true if initialized successfully, false if failed.
     */
    boolean initTemplateEditor() {
        if (templateEditor != null) {
            // already initialized
            return true;
        }

        InterviewParameters ip = session.getInterviewParameters();
        if (ip == null || ip.getWorkDirectory() == null) {
            return false;
        }
        templateEditor = createTemplateEditor();
        if (cm != null) {
            templateEditor.setCustomRenderers(cm.getCustomRenderersMap());
        }
        templateEditor.setCheckExcludeListListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    JComponent p = (src instanceof JComponent ? (JComponent) src : parent);
                    checkExcludeListUpdate(p, false, template);
                }
            });
        templateEditor.addObserver(this);
        return true;
    }

    TemplateEditor createTemplateEditor() {
        return new TemplateEditor(parent, uif, template, cm);
    }

    /**
     * Method returning UIFactory to be used to create InterviewEditor instance.
     */
    @Override
    protected UIFactory getUIFactory() {
        // this is workaround for template editor
        return uifOrig;
    }

    /**
     * Causes the dialog for work directory selecting to appear.
     * Overrides parent behaviour to apply template value.
     *
     * @see chooseWD(int)
     */
    @Override
    protected boolean createWD() {
        return chooseWD(WorkDirChooser.NEW);
    }

    /**
     * Causes the dialog for work directory selecting to appear.
     * Overrides parent behaviour to apply template value.
     *
     * @see chooseWD(int)
     */
    @Override
    protected boolean setWD() {
        return chooseWD(WorkDirChooser.OPEN_FOR_GIVEN_TESTSUITE);
    }

    /**
     * Causes the dialog for work directory selecting to appear.
     * Applies selected work dir and template values
     * @param mode - either WorkDirChooser.OPEN_FOR_GIVEN_TESTSUITE or
     *   WorkDirChooser.NEW
     *
     * @return true if user kindly agreed to edit configuration
     */
    protected boolean chooseWD(int mode) {
        ExecModelStub em = new ExecModelStub(testSuite, cm);
        WorkDirChooseTool tool =  WorkDirChooseTool.getTool(parent, uif, em, mode, testSuite, true);
        tool.doTool();

        if (em.getWorkDirectory() != null) {
            applyWorkDir(em.getWorkDirectory());

            // apply template value
            File tempPath = TemplateUtilities.getTemplateFile(em.getWorkDirectory());
            if (tempPath != null && initTemplateEditor()) {
                templateEditor.loadConfigFromFile(tempPath);
            }
            fixTemplatePath(tempPath);

            // user kindly agreed to edit configuration
            if (em.isShowConfigEditor()) {
                return true;
            }
        }
        return false;
    }

    protected void fixTemplatePath(File tempPath) {
        InterviewParameters ip = templateSession.getInterviewParameters();
        if (tempPath != null && ip.getTemplatePath() != null) {
            if (!ip.getTemplatePath().equals(tempPath.getPath())) {
                ip.setTemplatePath(tempPath.getPath());
                try {
                    ip.save();
                } catch (IOException ex) {
                    //
                } catch (Interview.Fault ex) {
                    //
                }
            }
        }
    }

    @Override
    public void restoreConfigFromWD(WorkDirectory wd) throws Fault {
        super.restoreConfigFromWD(wd);
        templateSession.loadTemplateFromWD(wd);
    }

    /**
     * Creates an instance of dialog to be used for propagation
     */
    protected TU_dialog createTU_Dialog() {
        Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
        return new TU_dialog(frame, uif, templateSession, cm);
    }

    /**
     * InterviewEditor.Observer method.
     * Invoked when current session has changed from InterviewEditor
     * @param p
     */
    @Override
    public void changed(InterviewParameters p) {
        if (p == null) {
            return; // should never occur
        } else if (p.isTemplate()) {
            try {
                templateSession.update(new TemplateSession.U_NewTemplate(p));
            } catch (Session.Fault e) {
                e.printStackTrace();
                System.err.println(uif.getI18NString("tcc.internalError.err"));
            }
        } else {
            super.changed(p);
        }
    }

    protected class TemplateConfigView extends SessionView {
        protected JLabel templ_n;
        protected JTextField templ_f;

        TemplateConfigView(Session config) {
            super(config);
        }



        @Override
        protected void init() {
            super.init();


            templ_n = uif.createLabel("tcc.templatename");
            templ_f = uif.createOutputField("tcc.templatename", templ_n, true);
            //templ_f.setFont(BOLD_FONT);
            templ_f.setBorder(BorderFactory.createEmptyBorder());
            templ_n.setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        protected void layoutComponents() {
            super.layoutComponents();
            GridBagConstraints gridBagConstraints;
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 4;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new Insets(0, 3, 0, 3);
            add(templ_n, gridBagConstraints);

            updateTempl(templ_f);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 5;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new Insets(0, 3, 0, 3);
            add(templ_f, gridBagConstraints);

        }

        protected void updateTempl(JTextField fld) {
            String text = session.getValue(TemplateSession.TEMPLATE_PROP_NAME);
            updateField(fld, text);
        }

        @Override
        public void updated(Event ev) {
            super.updated(ev);
            updateTempl(this.templ_f);
        }

    }


}
