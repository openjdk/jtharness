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

import java.awt.Component;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.Border;

import com.sun.javatest.Status;
import com.sun.javatest.TestResult;
import com.sun.javatest.tool.I18NUtils;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;

class ProgressMonitor extends ToolDialog {

    ProgressMonitor(Component parent, UIFactory uif, MonitorState state) {
        super(parent, uif, "pm", FRAME);

        /*ToolDialog
        super(parent, uif.getI18NString("pm.title"), false);
        */

        // ToolDialog this.uif = uif;
        this.state = state;
    }

    protected void initGUI() {
        setI18NTitle("pm.title");
        setHelp("browse.testMonitor.csh");

        subpanels = new StatusSubpanel[4];

        /*ToolDialog
        Container content = getContentPane();
        content.setName("progressD");
        content.setLayout(new BorderLayout());
        */

        JPanel body = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        // TOP
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.insets.left = 8;
        c.insets.right = 8;
        c.insets.top = 5;
        c.weightx = 1;
        c.weighty = 0;
        progressSubpanel = new ProgressSubpanel();
        subpanels[0] = progressSubpanel;
        body.add(progressSubpanel, c);

        // LEFT SIDE
        c.gridheight = 1;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.insets.right = 0;
        c.weightx = 1;
        c.weighty = 2;

        timeSubpanel = new TimeSubpanel();
        subpanels[1] = timeSubpanel;
        body.add(timeSubpanel, c);

        c.gridy = 2;
        memorySubpanel = new MemorySubpanel();
        subpanels[2] = memorySubpanel;
        body.add(memorySubpanel, c);

        // RIGHT SIDE
        // list of tests running
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 2;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 1;
        c.insets.right = 8;
        c.weightx = 3;
        c.weighty = 1;
        activitySubpanel = new ActivitySubpanel();
        subpanels[3] = activitySubpanel;
        body.add(activitySubpanel, c);

        setBody(body);
        /*ToolDialog
        content.add(body, BorderLayout.CENTER);
        */

        /*ToolDialog
        JPanel btnPanel = new JPanel(new GridBagLayout());
        GridBagConstraints bc = new GridBagConstraints();
        bc.anchor = GridBagConstraints.EAST;
        bc.insets.top = 5;
        bc.insets.bottom = 11;  // value from JL&F Guidelines
        bc.insets.right = 5;   // value from JL&F Guidelines
        bc.weightx = 1;

        JButton helpBtn = uif.createHelpButton("pm.help", "browse.testMonitor.csh");
        btnPanel.add(helpBtn, bc);

        JButton closeBtn = uif.createCloseButton("pm.close");
        bc.insets.right = 11;   // value from JL&F Guidelines
        bc.weightx = 0;
        btnPanel.add(closeBtn, bc);

        content.add(btnPanel, BorderLayout.SOUTH);
        */
        JButton helpBtn = uif.createHelpButton("pm.help", "browse.testMonitor.csh");
        JButton closeBtn = uif.createCloseButton("pm.close");
        setButtons(new JButton[] { helpBtn, closeBtn }, closeBtn);

        // other stuff
        state.addObserver(listener);

        if (state.isRunning())
            listener.starting();

        /*ToolDialog
        HelpBroker b = uif.getHelpBroker();
        //Desktop.addHelpDebugListener(progDialog);
        b.enableHelpKey(getRootPane(), "browse.testMonitor.csh", null);
        */

        //ToolDialog pack();

        for (int i = 0; i < subpanels.length; i++) {
            // make sure the panels are in any initial state
            // important for getting the "last" run info, less important if
            // there's a run in progress or a future runs
            subpanels[i].update();
        }

        /*ToolDialog
        getAccessibleContext().setAccessibleDescription(
                            uif.getI18NString("pm.desc"));
        */
    }

    void setTreePanelModel(TreePanelModel tpm) {
        this.tpm = tpm;
    }

    //----------member variables-----------------------------------------------------

    private MonitorState state;
    private TreePanelModel tpm;

    private ProgressSubpanel progressSubpanel;
    private ActivitySubpanel activitySubpanel;
    private TimeSubpanel timeSubpanel;
    private MemorySubpanel memorySubpanel;

    private StatusSubpanel[] subpanels;

    private volatile boolean running;
    private Listener listener = new Listener();

    private static final int UPDATE_FREQUENCY = 1000;
    private static int componentCount; // for generating names


    //----------nested classes-------------------------------------------------------

    class Listener implements MonitorState.Observer, ActionListener {
        Listener() {
            timer = new Timer(UPDATE_FREQUENCY, this);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == timer) {
                for (int i = 0; i < subpanels.length; i++)
                    subpanels[i].update();
            }
        }

        // these are probably on whichever thread the harness is running on
        // be sure to switch them onto the event thread
        public void starting() {
            running = true;

            for (int i = 0; i < subpanels.length; i++)
                subpanels[i].starting();

            timer.start();
        }

        public void postProcessing() {
            for (int i = 0; i < subpanels.length; i++) {
                    // make sure the panels update one last time
                subpanels[i].update();
                subpanels[i].postProcessing();
            }   // for
        }

        public void stopping() {
            // it is assumed that finished() will be called right away
        }

        public void finished(final boolean allOk) {
            running = false;
            for (int i = 0; i < subpanels.length; i++) {
                // make sure the panels update one last time
                subpanels[i].update();
                subpanels[i].stopping();
            }   // for

            timer.stop();
        }

        private Timer timer;
    }

    private abstract class StatusSubpanel extends JPanel {
        /**
         * Refresh any data in this subpanel.
         * This method is always called on the event dispatch thread.
         */
        abstract void update();

        /**
         * Test run is starting.  This method can be ignored if it does not apply.
         */
        void starting() {
        }

        void postProcessing() {
        }

        /**
         * Test run is finishing.  This method can be ignored if it does not apply.
         */
        void stopping() {
        }
    }

    private class ProgressSubpanel extends StatusSubpanel
    {
        ProgressSubpanel() {
            setBorder(uif.createTitledBorder("pm.prog"));
            setLayout(new GridBagLayout());

            // Line label
            GridBagConstraints lnc = new GridBagConstraints();
            lnc.gridx = 0;
            lnc.anchor = GridBagConstraints.EAST;
            lnc.insets.right = 10;

            // Field label
            GridBagConstraints lc = new GridBagConstraints();
            lc.insets.right = 5;

            // Field
            GridBagConstraints fc = new GridBagConstraints();
            fc.weightx = 1;
            fc.fill = GridBagConstraints.HORIZONTAL;
            fc.insets.right = 15;

            // Remaining field
            GridBagConstraints rc = new GridBagConstraints();
            rc.weightx = 1;
            rc.fill = GridBagConstraints.BOTH;
            rc.gridwidth = GridBagConstraints.REMAINDER;

            JTextField tf = uif.createHeading("pm.tests");
            tf.setBackground(UIFactory.Colors.TRANSPARENT.getValue());
            uif.setAccessibleInfo(tf, "pm.tests");
            add(tf, lnc);
            JLabel lab = uif.createLabel("pm.tests.pass");
            add(lab, lc);
            passTf = uif.createOutputField("pm.tests.pass", 6);
            passTf.setHorizontalAlignment(JTextField.RIGHT);
            lab.setLabelFor(passTf);
            lab.setDisplayedMnemonic(uif.getI18NString("pm.tests.pass.mne").charAt(0));
            add(passTf, fc);

            lab = uif.createLabel("pm.tests.fail");
            lab.setDisplayedMnemonic(uif.getI18NString("pm.tests.fail.mne").charAt(0));
            add(lab, lc);
            failTf = uif.createOutputField("pm.tests.fail", 6);
            failTf.setHorizontalAlignment(JTextField.RIGHT);
            lab.setLabelFor(failTf);
            add(failTf, fc);

            lab = uif.createLabel("pm.tests.err");
            add(lab, lc);
            errorTf = uif.createOutputField("pm.tests.err", 6);
            lab.setDisplayedMnemonic(uif.getI18NString("pm.tests.err.mne").charAt(0));
            errorTf.setHorizontalAlignment(JTextField.RIGHT);
            lab.setLabelFor(errorTf);
            add(errorTf, fc);

            lab = uif.createLabel("pm.tests.nr");
            lab.setDisplayedMnemonic(uif.getI18NString("pm.tests.nr.mne").charAt(0));
            add(lab, lc);
            notRunTf = uif.createOutputField("pm.tests.nr", 6);
            notRunTf.setHorizontalAlignment(JTextField.RIGHT);
            lab.setLabelFor(notRunTf);
            add(notRunTf, rc);
            add(Box.createVerticalStrut(10), rc);

            Color[] colors = new Color[Status.NUM_STATES];
            colors[Status.PASSED] = I18NUtils.getStatusBarColor(Status.PASSED);
            colors[Status.FAILED] = I18NUtils.getStatusBarColor(Status.FAILED);
            colors[Status.ERROR] = I18NUtils.getStatusBarColor(Status.ERROR);
            colors[Status.NOT_RUN] = I18NUtils.getStatusBarColor(Status.NOT_RUN);

            /*
            String[] actions = new String[5 + 1];
            actions[Status.PASSED] = Integer.toString(Status.PASSED);
            actions[Status.FAILED] = Integer.toString(Status.FAILED);
            actions[Status.ERROR] = Integer.toString(Status.ERROR);
            actions[Status.NOT_RUN] = Integer.toString(Status.NOT_RUN);
            actions[actions.length - 1] = "-1";
            */

            tf = uif.createHeading("pm.tests.mtr");
            uif.setAccessibleInfo(tf, "pm.tests.mtr");
            add(tf, lnc);
            add(meter = new ProgressMeter(colors, state), rc);
            uif.setAccessibleInfo(meter, "pm.tests.bar");
            /*
            meter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    model.showSummary(Integer.parseInt(e.getActionCommand()));
                }
            };

            meter.addActionListener(al);
            */
            uif.setToolTip(this, "pm.tests");
        }

        public void update() {
            updateAll();
        }

        //--------------------------------------------------------------------------------

        private void updateAll() {
            meter.update();

            int[] stats = state.getStats();

            setCount(passTf, stats[Status.PASSED]);
            setCount(failTf, stats[Status.FAILED]);
            setCount(errorTf, stats[Status.ERROR]);
            setCount(notRunTf,state.getTestsRemainingCount());
        }

        private final void setCount(JTextField tf, int value) {
            if (EventQueue.isDispatchThread())
                tf.setText(Integer.toString(value));
            else
                try {
                    EventQueue.invokeAndWait(new BranchPanel.TextUpdater(tf,
                                             Integer.toString(value), uif));
                }
                catch (InterruptedException e) {
                }
                catch (InvocationTargetException e) {
                }
        }

        private JTextField passTf;
        private JTextField failTf;
        private JTextField errorTf;
        private JTextField notRunTf;

        private ProgressMeter meter;
        private int[] meterStats;
    }

    private class ActivitySubpanel extends StatusSubpanel {
        ActivitySubpanel() {
            setBorder(uif.createTitledBorder("pm.activity"));
            setLayout(new CardLayout());

            idleCard = createSimpleCard("pm.idle");
            addCard(idleCard);
            initRunningCard();
            showCard(idleCard);
        }

        void starting() {
            showCard(runningCard);
            //StatusDialog.this.validate();
        }

        void postProcessing() {
            showCard(idleCard);
        }

        void stopping() {
        }

        void update() {
            testListData.removeAllElements();
            TestResult[] rt = state.getRunningTests();
            for (int i = 0; i < rt.length; i++)
                testListData.addElement(rt[i]);
        }

        //------------------------------------------------------------------

        private JComponent createSimpleCard(String uiKey) {
            JPanel card = new JPanel();
            card.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.CENTER;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1;
            c.weighty = 1;
            JTextField tf = uif.createHeading(uiKey);
            uif.setAccessibleInfo(tf, uiKey);
            card.add(tf, c);

            JLabel icon = uif.createIconLabel(uiKey);
            c.anchor = GridBagConstraints.WEST;
            c.weighty = 0;
            card.add(icon, c);

            return card;
        }

        private void initRunningCard() {
            testListData = new DefaultListModel();
            final JList list = uif.createList("pm.runlist", testListData);
            list.setBorder(BorderFactory.createEtchedBorder());
            list.setCellRenderer(RenderingUtilities.createTestListRenderer());
            list.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        int index = list.locationToIndex(e.getPoint());
                        if (index < 0)
                            return;
                        Object target = list.getModel().getElementAt(index);
                        if (target instanceof TestResult && tpm != null) {
                            tpm.showTest(((TestResult)target).getTestName());
                        }
                    }
                }
            );
            runningCard =
                uif.createScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            addCard(runningCard);
        }

        private void addCard(JComponent comp) {
            String s = comp.getName();
            if (s == null)
                comp.setName("StatusDialog.component" + componentCount++);
            add(comp, comp.getName());
        }

        private void showCard(JComponent comp) {
            //setVisible(false);
            ((CardLayout)(getLayout())).show(this, comp.getName());
            //setVisible(true);
        }

        private JComponent idleCard;
        private JComponent runningCard;

        private JTextField fileField;
        private DefaultListModel testListData;
        private JList testList;
        private String rootDir;
    }

    private class TimeSubpanel extends StatusSubpanel {
        TimeSubpanel() {
            setLayout(new GridBagLayout());
            Border b1 = uif.createTitledBorder("pm.time");
            Border b2 = BorderFactory.createEmptyBorder(10, 5, 10, 5);
            setBorder(BorderFactory.createCompoundBorder(b1, b2));

            // Line label
            GridBagConstraints lnc = new GridBagConstraints();
            lnc.gridx = 0;
            lnc.anchor = GridBagConstraints.EAST;
            lnc.insets.right = 10;

            // Remaining field
            GridBagConstraints rc = new GridBagConstraints();
            rc.fill = GridBagConstraints.BOTH;
            rc.gridwidth = GridBagConstraints.REMAINDER;
            rc.weightx = 1;

            JLabel lab = uif.createLabel("pm.time.sofar");
            lab.setDisplayedMnemonic(uif.getI18NString("pm.time.sofar.mne").charAt(0));
            add(lab, lnc);
            add(elapsedField = uif.createOutputField("pm.time.sofar", 8), rc);
            elapsedField.setText("00:00:00");
            lab.setLabelFor(elapsedField);

            lab = uif.createLabel("pm.time.remain");
            lab.setDisplayedMnemonic(uif.getI18NString("pm.time.remain.mne").charAt(0));
            add(lab, lnc);
            add(estimatedRemainingField = uif.createOutputField("pm.time.remain", 8), rc);
            estimatedRemainingField.setText("00:00:00");
            lab.setLabelFor(estimatedRemainingField);

            // workaround unknown GridBagLayout problem
            add(Box.createVerticalStrut(10), rc);

            uif.setToolTip(this, "pm.time");
        }

        void update() {
            long elapsed = state.getElapsedTime();
            elapsedField.setText(ElapsedTimeMonitor.millisToString(elapsed));

            long remain = state.getEstimatedTime();
            estimatedRemainingField.setText(ElapsedTimeMonitor.millisToString(remain));
        }

        private JTextField elapsedField;
        private JTextField estimatedRemainingField;
        //private ProgressMeter meter;
        private int[] meterStats = new int[2];
    }

    private class MemorySubpanel extends StatusSubpanel {
        MemorySubpanel() {
            setLayout(new GridBagLayout());
            Border b1 = uif.createTitledBorder("pm.memory");
            Border b2 = BorderFactory.createEmptyBorder(10, 5, 10, 5);
            setBorder(BorderFactory.createCompoundBorder(b1, b2));

            // Line label
            GridBagConstraints lnc = new GridBagConstraints();
            lnc.gridx = 0;
            lnc.anchor = GridBagConstraints.EAST;
            lnc.insets.right = 10;
            lnc.weightx = 0;
            lnc.fill = GridBagConstraints.NONE;

            // Remaining field
            GridBagConstraints rc = new GridBagConstraints();
            rc.fill = GridBagConstraints.BOTH;
            rc.gridwidth = GridBagConstraints.REMAINDER;
            rc.weightx = 1;

            JLabel lab = uif.createLabel("pm.memory.used");
            lab.setDisplayedMnemonic(uif.getI18NString("pm.memory.used.mne").charAt(0));
            add(lab, lnc);
            add(usedField = uif.createOutputField("pm.memory.used", 10), rc);
            lab.setLabelFor(usedField);

            lab = uif.createLabel("pm.memory.ttl");
            lab.setDisplayedMnemonic(uif.getI18NString("pm.memory.ttl.mne").charAt(0));
            add(lab, lnc);
            add(totalField = uif.createOutputField("pm.memory.ttl", 10), rc);
            lab.setLabelFor(totalField);

            // workaround unknown GridBagLayout problem
            add(Box.createVerticalStrut(10), rc);

            /*
            rc.fill = GridBagConstraints.VERTICAL;
            Color[] mc = {
                new Color(128, 0, 0),
                new Color(0, 128, 0)
            };
            add(meter = new ProgressMeter(mc, state), rc);
            */
            uif.setToolTip(this, "pm.memory");
        }

        void update() {
            int freeMem = (int)(runtime.freeMemory() / 1024);
            int totalMem = (int)(runtime.totalMemory() / 1024);
            int usedMem = totalMem - freeMem;
            usedField.setText(usedMem + "K");
            totalField.setText(totalMem + "K");

        }

        private JTextField usedField;
        private JTextField totalField;
        private ProgressMeter meter;
        private int[] meterStats = new int[2];
        private Runtime runtime = Runtime.getRuntime();
    }
}
