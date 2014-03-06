/*
 * $Id$
 *
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
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
import com.sun.javatest.Parameters;
import com.sun.javatest.TestResult;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.sun.javatest.WorkDirectory;
//import com.sun.javatest.report.Report;
import com.sun.javatest.report.ReportDirChooser;
import com.sun.javatest.report.Report;
import com.sun.javatest.tool.FileChooser;
import com.sun.javatest.tool.FileHistory;
import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.PrefixMap;

/**
 * Class to handle report create/open operations in exec tool.
 */
class ReportHandler implements ET_ReportControl, HarnessAware {
    ReportHandler(JComponent parent, ExecModel model, UIFactory uif) {
        this.parent = parent;
        this.model = model;
        this.uif = uif;
        initActions();
    }

    ReportHandler(JComponent parent, ExecModel model, Harness harness, UIFactory uif) {
        this(parent, model, uif);
        setHarness(harness);
    }

    public JMenu getMenu() {
        Action[] reportActions = {
            newReportAction,
            openReportAction,
            null
        };

        JMenu menu = uif.createMenu("rpth", reportActions);
        menu.addMenuListener(reportHistoryListener);

        return menu;
    }

    void showReportDialog(int mode) {
        // refactored for 4.0 to use standard chooser for opening a report

        if (mode == ReportDirChooser.NEW) {
            ReportDirChooser rdc = getReportDirChooser();
            rdc.setMode(mode);
            int option = rdc.showDialog(parent);
            if (option != JFileChooser.APPROVE_OPTION)
                return;

            // set parameters?
        }
        else if (mode == ReportDirChooser.OPEN) {
            FileChooser fc = new FileChooser();
            fc.setApproveButtonToolTipText(uif.getI18NString("rh.open.tip"));
            fc.setApproveButtonMnemonic(uif.getI18NMnemonic("rh.open.mne"));
            fc.setDialogTitle(uif.getI18NString("rh.open.title"));

            if (lastOpenPath != null) {
                fc.setCurrentDirectory(lastOpenPath);
            }

            int res = fc.showOpenDialog(parent);
            //File f = rdc.getSelectedReportDirectory();
            File f = fc.getSelectedFile();
            if (res == JFileChooser.APPROVE_OPTION) {
                showReportBrowser(f);
                history.add(f);
                lastOpenPath = f.getParentFile();
            }
        }
    }

    void showNewReportDialog() {
        if (newReportD == null) {
            newReportD = new NewReportDialog(parent, uif, model.getFilterConfig(),
                            getReportBrowser(), model);

        newReportD.addObserver(new NewReportDialog.Observer() {
                public void update(Map l) {
                    lastState = l;
                    String lastReportDir =
                        (String) (lastState.get(NewReportDialog.REPORT_DIR));

                   if (lastReportDir != null)
                       history.add(new File(lastReportDir));
                }

                public void writingReport() {
                    newReportAction.setEnabled(false);
                }

                public void wroteReport() {
                    newReportAction.setEnabled(true);
                }

                public void errorWriting(String problem) {
                    newReportAction.setEnabled(true);
                }
            });
        }

        newReportD.setInterviewParameters(model.getInterviewParameters());
        if (lastState != null)
            newReportD.setLastState(lastState);

        newReportD.setVisible(true);
    }

    void showReportBrowser(File reportDir) {
        // if if is a dir, try to find a particular file to show
        // since there may be multiple choices, use the one with the
        // most recent date
        File target = reportDir;
        if (reportDir.isDirectory()) {
            String[] names = Report.getHtmlReportFilenames();
            long newestTime = 0;

            for (int i = 0; i < names.length; i++) {
                File f = new File(reportDir, names[i]);
                if (f.exists()  && f.lastModified() > newestTime) {
                    target = f;
                    newestTime = f.lastModified();
                }
            }   // for
        }
        else {
            // target is a file
        }

        getReportBrowser().show(target);
    }

    ReportBrowser getReportBrowser() {
        if (reportBrowser == null) {
            reportBrowser = new ReportBrowser(parent, model, uif, this);
        }
        return reportBrowser;
    }

    ReportDirChooser getReportDirChooser() {
        if (reportDirChooser == null)
            reportDirChooser = new ReportDirChooser();
        return reportDirChooser;
    }

    Action getNewReportAction() {
        return newReportAction;
    }

    Action getOpenReportAction() {
        return openReportAction;
    }

    // should really be observing ExecModel
    public void updateGUI() {

        workDir = model.getWorkDirectory();

        boolean workDirSet = (workDir != null);
        newReportAction.setEnabled(workDirSet);
        openReportAction.setEnabled(workDirSet);

        if (!workDirSet) return;

        if (history == null) {
            history = FileHistory.getFileHistory(workDir, "reportDirHistory.jtl");
            reportHistoryListener.setFileHistory(history);
        }
    }

    public void save(Map parentMap) {
        if (lastState != null && lastState.size() > 0)  {
           PrefixMap pm = new PrefixMap(parentMap, REPORT_PREFIX);
           pm.putAll(lastState);
        }
    }

    public void restore(Map parentMap) {
        if (parentMap == null)
            return;

        try {
            PrefixMap pm = new PrefixMap(parentMap, REPORT_PREFIX);
            if (pm == null) return;

            Object[] keys = pm.keySet().toArray();
            if (lastState == null)
                lastState = new HashMap();

            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                String value = (String) pm.get(keys[i]);

                if (value != null)
                    lastState.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        if (newReportD != null) {
            newReportD.dispose();
        }

        if (reportBrowser != null) {
            reportBrowser.dispose();
        }

        if (uif != null) {
            uif.dispose();
        }

        workDir = null;
        model = null;
        lastState = null;
    }

    public void setHarness(Harness h) {
        h.addObserver(new Harness.Observer() {

            public void startingTestRun(Parameters params) {
                newReportAction.setEnabled(false);
            }

            public void startingTest(TestResult tr) {}
            public void finishedTest(TestResult tr) {}
            public void stoppingTestRun() {}
            public void finishedTesting() {}

            public void finishedTestRun(boolean allOK) {
                newReportAction.setEnabled(true);
            }

            public void error(String msg) {}
        });
    }

    /**
     * ET_Control interface method
     * @return null
     */
    public List<Action> getToolBarActionList() {
        return null;
    }

    //----------------------------------------------------------------------------
    // internal methods

    // cannot use instance initialization for the actions because they depend
    // on uif being initialized in the constructor
    private void initActions() {
        newReportAction = new ToolAction(uif, "rpth.new") {
            public void actionPerformed(ActionEvent e) {
                showNewReportDialog();
            }
        };

        openReportAction = new ToolAction(uif, "rpth.open") {
            public void actionPerformed(ActionEvent e) {
                showReportDialog(ReportDirChooser.OPEN);
            }
        };

        reportHistoryListener = new FileHistory.Listener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem mi = (JMenuItem) (e.getSource());
                File f = (File) (mi.getClientProperty(FileHistory.FILE));
                if (f != null)
                    showReportBrowser(f);
            }
        });

    }

    private Action newReportAction;
    private Action openReportAction;

    private static final String REPORT_PREFIX = "report";

    private JComponent parent;
    private UIFactory uif;

    private FileHistory.Listener reportHistoryListener;
    private FileHistory history;
    private File lastOpenPath;
    private Map lastState;
    private ExecModel model;
    private NewReportDialog newReportD;
    private ReportBrowser reportBrowser;
    private ReportDirChooser reportDirChooser;
    private WorkDirectory workDir;
}
