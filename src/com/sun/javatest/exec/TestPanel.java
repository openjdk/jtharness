/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.help.CSH;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.javatest.Harness;
import com.sun.javatest.JavaTestError;
import com.sun.javatest.Parameters;
import com.sun.javatest.Status;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestSuite;
import com.sun.javatest.tool.I18NUtils;
import com.sun.javatest.tool.UIFactory;
import java.util.HashMap;
import java.util.Vector;

/**
 * Browser for details of a specific test. The browser provides a variety
 * of displays. controlled by its own toolbar (button-bar.)
 */

class TestPanel extends JPanel
{
    TestPanel(UIFactory uif, Harness harness, ContextManager contextManager) {
        this.uif = uif;
        this.harness = harness;
        this.contextManager = contextManager;
        initGUI();

    }

    // most of the tabs have arbitrary, accommodating sizes,
    // so set a default preferred size here for the panel
    public Dimension getPreferredSize() {
        int dpi = uif.getDotsPerInch();
        return new Dimension(5 * dpi, 4 * dpi);
    }

    void setTestSuite(TestSuite ts) {
        for (int i = 0; i < panels.length; i++)
            panels[i].setTestSuite(ts);
    }

    TestResult getTest() {
        return currTest;
    }

    void setTest(TestResult tr) {
        for (int i=stdPanels.length ; i < panels.length; i++) {
              TP_CustomSubpanel sp = (TP_CustomSubpanel) panels[i];
              sp.onCangedTestResult(tr, (sp == currPanel));
        }
        updatePanel(tr, currPanel);
    }

    //------private methods----------------------------------------------

    private synchronized void updatePanel(TestResult newTest, TP_Subpanel newPanel) {
        // this method is specifically designed to be fast to execute when
        // the panel is hidden; it also tries to avoid unnecessarily getting
        // rid of useful information which is still valid

        if (newTest != currTest) {
            currTest = newTest;
            currDesc = null;  // don't evaluate till later
        }

        if (newPanel != currPanel) {
            // update help for tabbed pane to reflect help for selected panel
            CSH.setHelpIDString(tabs, CSH.getHelpIDString(newPanel));
            currPanel = newPanel;
        }

        if (EventQueue.isDispatchThread())
            updateGUIWhenVisible();
        else {
            if (!updatePending && !needToUpdateGUIWhenShown) {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            synchronized (TestPanel.this) {
                                updateGUIWhenVisible();
                                updatePending = false;
                            }
                        }
                    });
                updatePending = true;
            }
        }
    }


    // must be called on the AWT event thread
    private void updateGUIWhenVisible() {
        if (isVisible())
            updateGUI();
        else
            needToUpdateGUIWhenShown = true;
    }

    // updateGUI is the second half of updatePanel(...)
    // It is called directly from updatePanel via updateGUIWhenVisible,
    // if the update is made when the panel is visible; otherwise the
    // call is delayed until the panel gets ComponentEvent.shown.
    private synchronized void updateGUI() {
        //System.err.println("TP.updateGUI");
        if (currTest == null) {
            for (int i = 0; i < tabs.getComponentCount(); i++)
                tabs.setEnabledAt(i, false);

            statusField.setEnabled(false);
        }
        else {
            try {
                if (currDesc == null)
                    currDesc = currTest.getDescription();
            }
            catch (TestResult.Fault e) {
                JavaTestError.unexpectedException(e);
                // ignore exception if can't find description ??
            }

            // always got a test description
            tabs.setEnabledAt(tabs.indexOfComponent(descPanel), true);

            // always got source files
            tabs.setEnabledAt(tabs.indexOfComponent(filesPanel), true);

            // check if there are any environment entries recorded
            boolean hasEnv;
            try {
                Map map = currTest.getEnvironment();
                hasEnv = (map != null && map.size() > 0);
            }
            catch (TestResult.Fault f) {
                hasEnv = false;
            }
            tabs.setEnabledAt(tabs.indexOfComponent(envPanel), hasEnv);

            // check if there are any result properties recorded
            boolean hasResults = currTest.getPropertyNames().hasMoreElements();
            tabs.setEnabledAt(tabs.indexOfComponent(resultPanel), hasResults);

            // check if there is any output recorded
            boolean hasOutput = (currTest.getSectionCount() > 0);
            tabs.setEnabledAt(tabs.indexOfComponent(outputPanel), hasOutput);

            for (int i = stdPanels.length; i < tabs.getTabCount(); i++) {
                tabs.setEnabledAt(i, true);
            }

            updateStatus();

            // should consider tracking test, if test is mutable
            // and enable tabs/status as required
            if (currPanel.isUpdateRequired(currTest)) {
                currPanel.updateSubpanel(currTest);
            }

            FeatureManager fm = contextManager.getFeatureManager();
            if (fm.isEnabled(fm.SHOW_DOCS_FOR_TEST)) {
                if (docPanel.isUpdateRequired(currTest)) {
                    docPanel.updateSubpanel(currTest);
                }
                tabs.setEnabledAt(tabs.indexOfComponent(docPanel),
                    docPanel.getDocuments() != null);

            }

        }
    }

    private void updateStatus() {
        if (isShowing()) {
            Status s = currTest.getStatus();
            statusField.setText(I18NUtils.getStatusMessage(s));
            Color c = I18NUtils.getStatusBarColor(s.getType());
            statusField.setBackground(c);
            statusField.setEnabled(true);
        }
    }

    /**
     * Updates custom panels except currPanel. Invoked when invoked when the
     * status of the current test result has changed.
     * @param tr  - test result
     * @param currPanel - the panel to not update
     */
    private void updateCustomPanels(TestResult tr, TP_Subpanel currPanel) {
        for (int i = stdPanels.length ; i < panels.length; i++) {
              TP_CustomSubpanel sp = (TP_CustomSubpanel) panels[i];
              if (sp != currPanel) {
                  sp.updateSubpanel(tr);
              }
        }
    }

    private void initGUI() {
        setName("test");
        descPanel = new TP_DescSubpanel(uif);
        filesPanel = new TP_FilesSubpanel(uif);
        resultPanel = new TP_ResultsSubpanel(uif);
        envPanel = new TP_EnvSubpanel(uif);
        outputPanel = new TP_OutputSubpanel(uif);

        Vector<TP_Subpanel> vpanels = new Vector();
        vpanels.add(descPanel);

        FeatureManager fm = contextManager.getFeatureManager();
        if (fm.isEnabled(fm.SHOW_DOCS_FOR_TEST)) {
            docPanel = new TP_DocumentationSubpanel(uif);
            vpanels.add(docPanel);
        }

        vpanels.add(filesPanel);
        vpanels.add(resultPanel);
        vpanels.add(envPanel);
        vpanels.add(outputPanel);

        stdPanels = new TP_Subpanel[vpanels.size()];
        stdPanels = vpanels.toArray(stdPanels);

        tabs = uif.createTabbedPane("test", stdPanels);

        panels = stdPanels;
        if (contextManager != null ) {
            CustomTestResultViewer[] cv = contextManager.getCustomResultViewers();
            if (cv != null) {
                customViewTable = new HashMap();
                panels = new TP_Subpanel[stdPanels.length + cv.length];
                System.arraycopy(stdPanels, 0, panels, 0, stdPanels.length);
                for (int i=0; i < cv.length; i++) {
                    panels[stdPanels.length + i] = new TP_CustomSubpanel(uif, cv[i]);
                    customViewTable.put(cv[i], panels[stdPanels.length + i]);
                    if (cv[i].isViewerVisible()) {
                        tabs.addTab(cv[i].getDescription(), null, cv[i], cv[i].getDescription());
                    }
                }
                for (int i=0; i < cv.length; i++) {
                    cv[i].addPropertyChangeListener(CustomTestResultViewer.visibleProperetyName,
                            new ViewerStateListener(cv, i, stdPanels.length));
                }
            }
        }

        tabs.setTabPlacement(SwingConstants.TOP);
        tabs.setName("testTabs");
        tabs.setSelectedComponent(outputPanel);
        tabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Component c = tabs.getSelectedComponent();
                if (c instanceof TP_Subpanel) {
                    updatePanel(currTest, (TP_Subpanel) c);
                }  if (c instanceof CustomTestResultViewer) {
                    updatePanel(currTest, (TP_CustomSubpanel) customViewTable.get(c));
                }
            }
        });

        currPanel = outputPanel;
        CSH.setHelpIDString(tabs, CSH.getHelpIDString(currPanel));

        statusField = uif.createOutputField("test.status");
        statusField.setEnabled(false);

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        add(statusField, BorderLayout.SOUTH);

        // --- anonymous class ---
        ComponentListener cl = new ComponentListener() {
            public void componentResized(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }
            public void componentShown(ComponentEvent e) {
                if (needToUpdateGUIWhenShown) {
                    updateGUI();
                    needToUpdateGUIWhenShown = false;
                }
                //System.err.println("TP: showing");
                harness.addObserver(observer);
            }
            public void componentHidden(ComponentEvent e) {
                //System.err.println("TP: hidden");
                harness.removeObserver(observer);
            }
        };
        addComponentListener(cl);
    }

    class ViewerStateListener implements  PropertyChangeListener{
        ViewerStateListener(CustomTestResultViewer[] cv, int pos, int offset) {
            this.cv = cv;
            this.pos = pos;
            this.offset = offset;
        }
        private CustomTestResultViewer[] cv;
        private int pos, offset;

        public void propertyChange(PropertyChangeEvent evt) {
            Boolean state = (Boolean) evt.getNewValue();
            if (state.booleanValue()) {
                int j = offset;
                for (int i = 0; i < pos; i++) {
                    if (tabs.indexOfComponent(cv[i]) >=0){
                        j++;
                    }
                }
                tabs.insertTab(cv[pos].getDescription(), null, cv[pos], cv[pos].getDescription(), j);
            } else {
                tabs.remove(cv[pos]);
            }
        }
    }

    static final String lineSeparator = System.getProperty("line.separator");

    // basic GUI objects
    private UIFactory uif;
    private TP_Subpanel[] panels;
    private TP_Subpanel[] stdPanels;
    private JTabbedPane tabs;
    private TP_DescSubpanel descPanel;
    private TP_DocumentationSubpanel docPanel;
    private TP_FilesSubpanel filesPanel;
    private TP_ResultsSubpanel resultPanel;
    private TP_EnvSubpanel envPanel;
    private TP_OutputSubpanel outputPanel;
    private JTextField statusField;
    private HashMap customViewTable;

    //
    private Harness harness;

    //
    private ContextManager contextManager;

    // set these values via update
    private TestResult currTest;
    private TP_Subpanel currPanel;

    // these values are derived from values given to update
    private TestDescription currDesc;

    // used to minimize update load
    private boolean needToUpdateGUIWhenShown = true;
    private boolean updatePending = false;

    // will be needed for dynamic update
    private final Observer observer = new Observer();

    private class Observer implements Harness.Observer, TestResult.Observer {
            // ---------- Harness.Observer ----------
        public void startingTestRun(Parameters params) {
        }

        public void startingTest(TestResult tr) {
        //System.err.println("TP$Observer.starting: " + tr);
            try {
                if (tr.getDescription() == currDesc) {
                    //System.out.println("RunnerObserver.UPDATING CURRENT TEST");
                    // this will update currTest to tr if needed
                    updatePanel(tr, currPanel);
                    updateCustomPanels(tr, currPanel);
                }
            }
                catch (TestResult.Fault e) {
            }
        }

        public void finishedTest(TestResult tr) {
            //System.err.println("TP$Observer.finished: " + tr);
            if (tr == currTest) {
                updatePanel(tr, currPanel);
                updateCustomPanels(tr, currPanel);
            }
        }

        public void stoppingTestRun() {
        }

        public void finishedTesting() {
        }

        public void finishedTestRun(boolean allOK) {
        }

        public void error(String msg) {
        }

        // ----- TestResult.Observer interface -----
        public void completed(TestResult tr) {
            tr.removeObserver(this);
            updateStatus();
        }

        public void createdSection(TestResult tr, TestResult.Section section) { }

        public void completedSection(TestResult tr, TestResult.Section section) { }

        public void createdOutput(TestResult tr, TestResult.Section section,
                      String outputName) { }

        public void completedOutput(TestResult tr, TestResult.Section section,
                        String outputName) { }

        public void updatedOutput(TestResult tr, TestResult.Section section,
                      String outputName,
                      int start, int end, String text) { }

        public void updatedProperty(TestResult tr, String name, String value) { }
    }

}
