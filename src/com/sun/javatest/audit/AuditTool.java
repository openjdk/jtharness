/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.audit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.util.Map;
import javax.help.CSH;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.util.I18NResourceBundle;
import javax.swing.JDialog;
import javax.swing.Timer;

class AuditTool extends Tool
{
    AuditTool(AuditToolManager m) {
        super(m, "audit", "audit.window.csh");
        setI18NTitle("tool.title");
        setShortTitle(uif.getI18NString("tool.shortTitle"));
        initGUI();
    }

    public void dispose() {
        super.dispose();

        if (optionsDialog != null) {
            optionsDialog.setVisible(false);
            optionsDialog.dispose();
        }
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public TestSuite[] getLoadedTestSuites() {
        TestSuite ts = (interviewParams == null ? null : interviewParams.getTestSuite());
        return (ts == null ? null : new TestSuite[] { ts });
    }

    public WorkDirectory[] getLoadedWorkDirectories() {
        WorkDirectory wd = (interviewParams == null ? null : interviewParams.getWorkDirectory());
        return (wd == null ? null : new WorkDirectory[] { wd });
    }

    public void save(Map m) {
        if (interviewParams == null)
            return;

        // save test suite
        TestSuite ts = interviewParams.getTestSuite();
        m.put("testSuite", ts.getRoot().getPath());

        // save work directory
        WorkDirectory wd = interviewParams.getWorkDirectory();
        if (wd != null)
            m.put("workDir", wd.getPath());

        // save name of interview file
        File cf = interviewParams.getFile();
        if (cf != null)
            m.put("config", cf.getPath());
    }

    @Override
    protected void restore(Map m) {
        String tsp = (String) (m.get("testSuite"));
        String wdp = (String) (m.get("workDir"));
        String cfp = (String) (m.get("config"));

        if (tsp == null && wdp == null && cfp == null)
            return;

        try {
            if (interviewParams != null) {
                interviewParams.dispose();
            }
            interviewParams = InterviewParameters.open(tsp, wdp, cfp);
            updateGUI(null, interviewParams, uif.getI18NString("tool.restore.txt"));
            autoShowOptions = false;
        }
        catch (InterviewParameters.Fault e) {
            uif.showError("tool.cantRestore", e.getMessage());
        }
    }

    private void initGUI() {
        int dpi = uif.getDotsPerInch();
        setPreferredSize(new Dimension(6 * dpi, 4 * dpi));
        setLayout(new BorderLayout());

        addHierarchyListener(listener);

        menuBar = uif.createMenuBar("tool");
        String[] auditMenuEntries = {
            OPTIONS
        };
        JMenu auditMenu = uif.createMenu("tool.audit", auditMenuEntries, listener);
        menuBar.add(auditMenu);

        menuBar.add(uif.createHorizontalGlue("tool.pad"));

        JMenu helpMenu = uif.createMenu("tool.help");
        helpMenu.add(uif.createHelpMenuItem("tool.help.window", "audit.window.csh"));
        menuBar.add(helpMenu);

        JPanel head = uif.createPanel("head", false);
        head.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        head.setLayout(new GridBagLayout());

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.gridwidth = 1;
        lc.insets.right = 5;
        lc.weightx = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;
        fc.gridwidth = GridBagConstraints.REMAINDER;

        testSuiteField = initField("tool.testSuite", head, lc, fc);
        workDirField = initField("tool.workDir", head, lc, fc);
        configFileField = initField("tool.configFile", head, lc, fc);

        add(head, BorderLayout.NORTH);

        panes = new AuditPane[] {
            new SummaryPane(uif),
            new BadTestsPane(uif),
            new BadChecksumPane(uif),
            new BadTestDescriptionPane(uif),
            new BadTestCaseTestsPane(uif)
        };

        tabs = uif.createTabbedPane("tool.tabs", panes);
        tabs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Component c = tabs.getSelectedComponent();
                CSH.setHelpIDString(tabs, CSH.getHelpIDString(c));
            }
        });
        CSH.setHelpIDString(tabs, CSH.getHelpIDString(panes[0]));

        add(tabs, BorderLayout.CENTER);

        updateGUI(null, null, uif.getI18NString("tool.initial.txt"));
    }

    private JTextField initField(String key, JPanel p,
                                 GridBagConstraints lc, GridBagConstraints fc) {

        JLabel lbl = uif.createLabel(key, true);
        p.add(lbl, lc);
        JTextField tf = uif.createOutputField(key, lbl);
        tf.setBorder(null);
        p.add(tf, fc);
        return tf;
    }


    private synchronized void showOptions() {
        if (worker != null) {
            uif.showError("tool.auditInProgress");
            return;
        }

        if (optionsDialog == null) {
            // should arguably worry about standard ownership problem
            optionsDialog = new OptionsDialog(this, listener, uif);
        }

        if (interviewParams != null)
            optionsDialog.setParameters(interviewParams);

        optionsDialog.setVisible(true);
    }

    private void setOptions() {
        String tsp = optionsDialog.getTestSuitePath();
        String wdp = optionsDialog.getWorkDirPath();
        String cfp = optionsDialog.getConfigFilePath();

        if ( isEmpty(tsp) && isEmpty(wdp) && isEmpty(cfp) ) {
            uif.showError("tool.noOptions");
            return;
        }

        try {
            if (interviewParams != null) {
                interviewParams.dispose();
            }
            interviewParams = InterviewParameters.open(tsp, wdp, cfp);
        }
        catch (InterviewParameters.Fault e) {
            uif.showError("tool.badOptions", e.getMessage());
            return;
        }

        optionsDialog.setVisible(false);

        startAudit();
    }

    private synchronized void startAudit() {
        if (worker != null) {
            uif.showError("tool.auditInProgress");
            return;
        }

        // can't do this operation without WD
        if (interviewParams.getWorkDirectory() == null) {
            uif.showError("tool.noWd");
            return;
        }

        final JDialog d = uif.createWaitDialog("tool.wait", this);
        d.setLocationRelativeTo(this);

        worker = new Thread() {
                public void run() {
                    Audit a = audit; // default to previous value
                    try {
                        a = new Audit(interviewParams);
                    }
                    finally {
                        synchronized (AuditTool.this) {
                            worker = null;
                        }
                        d.setVisible(false);
                        // the following may not be right if the audit
                        // has been interrupted and we are reverting to
                        // previous state. Ideally, should pull parameters
                        // from the audit object
                        updateGUI(a, interviewParams, null);
                    }
            }
        };

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent evt){
                // show dialog if still processing
                if (worker != null && worker.isAlive()) {
                    d.setVisible(true);
                }
            }
        };

        // show wait dialog if operation is still running after
        // WAIT_DIALOG_DELAY
        Timer timer = new Timer(WAIT_DIALOG_DELAY, al);
        timer.setRepeats(false);
        timer.start();

        worker.start();


        updateGUI(null, interviewParams, "");
    }

    private static int WAIT_DIALOG_DELAY = 2000;

    private void updateGUI(final Audit a, final InterviewParameters p, final String msg) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        updateGUI(a, p, msg);
                    }
                });
            return;
        }

        audit = a;

        TestSuite ts = (p == null ? null : p.getTestSuite());
        String tsp = (ts == null ? "" : ts.getPath());
        testSuiteField.setText(tsp);

        WorkDirectory wd = (p == null ? null : p.getWorkDirectory());
        String wdp = (wd == null? "" : wd.getPath());
        workDirField.setText(wdp);

        File configFile = (p == null ? null : p.getFile());
        String cfp = (configFile == null ? "" : configFile.getPath());
        configFileField.setText(cfp);

        for (int i = 0; i < panes.length; i++) {
            AuditPane pane = panes[i];
            if (a != null)
                pane.show(a);
            else if (msg != null)
                pane.show(msg);
        }
    }

    private static boolean isEmpty(String s) {
        return (s == null || s.length() == 0);
    }


    private JMenuBar menuBar;
    private JTextField testSuiteField;
    private JTextField workDirField;
    private JTextField configFileField;
    private AuditPane[] panes;
    private JTabbedPane tabs;
    private OptionsDialog optionsDialog;
    private boolean autoShowOptions = true;;
    private Listener listener = new Listener();

    private InterviewParameters interviewParams;

    // access to these fields must be synchronized
    private Thread worker;
    private Audit audit;

    private static final String OPTIONS = "options";
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(AuditTool.class);

    private class Listener implements ActionListener, HierarchyListener {
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(OPTIONS)) {
                showOptions();
            }
            else if (cmd.equals(OptionsDialog.OK)) {
                setOptions();
            }
        }

        public void hierarchyChanged(HierarchyEvent e) {
            if (isShowing() && autoShowOptions) {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            showOptions();
                        }
                    });
                autoShowOptions = false;
            }
        }
    };
}
