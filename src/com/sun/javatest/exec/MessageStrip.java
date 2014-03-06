/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ResourceBundle;

import javax.help.CSH;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.JTextComponent;

import com.sun.javatest.Harness;
import com.sun.javatest.Parameters;
import com.sun.javatest.TestResult;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.tool.UIFactory;

/**
 * The panel at the bottom of exec tool.
 * This class manages the insertion, deletion, and rearrangement of any small
 * monitors.  It also forwards informational text messages to the user.
 */

class MessageStrip extends JSplitPane
    implements Harness.Observer, ComponentListener
{
    MessageStrip(UIFactory uif, Monitor[] monitors, MonitorState state,
                 ActionListener zoomListener) {
        this.uif = uif;
        this.monitors = monitors;
        this.state = state;
        this.zoomListener = zoomListener;

        setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        // left side is a simple text field
        leftField = uif.createOutputField("strip.msg");
        leftField.setOpaque(false);

        setLeftComponent(leftField);
        setRightComponent(createRightPanel());

        setDividerSize(5);
        setEnabled(true);
        addComponentListener(this);
        uif.setAccessibleInfo(this, "strip");
        setDividerLocation(0.60d);
    }

    // which to display while running tests
    void setRunningMonitor(Monitor m) {
        for (int i = 0; i < monitors.length; i++)
            if (monitors[i] == m) {
                runningMonitor = i;
                break;
            }
    }

    // which to display while not running tests
    // these are not currently intended to be called after initialization
    void setIdleMonitor(Monitor m) {
        for (int i = 0; i < monitors.length; i++)
            if (monitors[i] == m) {
                idleMonitor = i;
                break;
            }

        setMonitor(idleMonitor);
    }

    void setMonitor(final int index) {
        if (index < 0)
            return;

        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    setMonitor(index);
                }
            });
            return;
        }

        if (index < monitors.length) {
            currMonitor = index;
            monitorCards.show(rightPanel, monitors[index].getSmallMonitorName());
            selector.setSelectedItem(monitors[index]);
            validate();     // required for correct repaint

            // write to preferences
            Preferences p = Preferences.access();
            p.setPreference(MINI_PREF, monitors[index].getClass().getName());
        }
    }

    // Harness.Observer ...
    // anything that happens here must be switched on to the event thread

    public void startingTestRun(Parameters params) {
        setText(leftField, uif.getI18NString("strip.start"));
        setMonitor(runningMonitor);

        if (clearTimer == null)
            clearTimer = new Timer(CLEAR_TIMEOUT, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    setText(leftField, "");
                }
            });
        clearTimer.start();
    }

    public void startingTest(TestResult tr) {
        setText(leftField, uif.getI18NString("strip.running", tr.getTestName()));
    }

    public void finishedTest(TestResult tr) {
    }

    public void stoppingTestRun() {
        setText(leftField, uif.getI18NString("strip.stop"));
    }

    public void finishedTesting() {
        setText(leftField, uif.getI18NString("strip.cleanup"));
        setMonitor(idleMonitor);
    }

    public void finishedTestRun(boolean allOk) {
        setText(leftField, uif.getI18NString("strip.finish"));
        if (clearTimer != null)
            clearTimer.stop();
    }

    public void error(String msg) {
    }

    void showMessage(ResourceBundle msgs, String key) {
        setText(leftField, msgs.getString(key));
    }

    private void setText(JTextComponent comp, String text) {
        if (EventQueue.isDispatchThread())
            comp.setText(text);
        else
            EventQueue.invokeLater(new BranchPanel.TextUpdater(comp, text, uif));
    }

    private JPopupMenu createMenu() {
        JPopupMenu menu = uif.createPopupMenu("strip.menu");
        for (int i = 0; i < monitors.length; i++) {
            JMenuItem mi = uif.createLiteralMenuItem(
                monitors[i].getSmallMonitorName(), actionListener);
            mi.setActionCommand(Integer.toString(i));
            menu.add(mi);
        }   // for


        return menu;
    }

    private JComponent createRightPanel() {
        JPanel right = uif.createPanel("strip.right", new GridBagLayout(), false);

        selector = uif.createLiteralChoice("strip.sel", monitors);
        uif.setAccessibleName(selector, "strip.sel");
        selector.addActionListener(actionListener);
        selector.setRenderer(new BasicComboBoxRenderer() {
                public Component getListCellRendererComponent(JList list, Object value,
                            int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value,
                                                index, isSelected, cellHasFocus);
                    try {
                        JLabel lab = (JLabel)c;
                        if (value instanceof Monitor)
                            lab.setText(((Monitor)value).getSmallMonitorName());
                    }
                    catch (ClassCastException e) {
                    }

                    return c;
                }
            });
        CSH.setHelpIDString(selector, "run.testProgress");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2.0d;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        monitorCards = new CardLayout();
        rightPanel = uif.createPanel("strip.", monitorCards, false);
        for (int i = 0; i < monitors.length; i++)
            rightPanel.add(monitors[i].getSmallMonitorName(), monitors[i].getSmallMonitor());
        currMonitor = getDefaultSmallMonitor();
        monitorCards.show(rightPanel, monitors[currMonitor].getSmallMonitorName());

        JPanel tmpPanel = uif.createPanel("monitor container", new BorderLayout(), false);
        tmpPanel.add(selector, BorderLayout.WEST);
        tmpPanel.add(rightPanel, BorderLayout.CENTER);

        right.add(tmpPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0d;
        /*
        gbc.anchor = GridBagConstraints.EAST;
        selectButt = uif.createIconButton("strip.drop");
        selectButt.addMouseListener(popupListener);
        selectButt.addActionListener(popupListener);
        uif.setAccessibleName(selectButt, "strip.drop");

        gbc.gridx = 2;
        */
        magButt = uif.createIconButton("strip.magnify");
        magButt.addActionListener(actionListener);
        uif.setAccessibleName(magButt, "strip.magnify");

        JToolBar tb = uif.createToolBar("strip.tb",
                            new JButton[] {magButt});
        tb.setFloatable(false);
        tb.setBorderPainted(true);

        right.add(tb, gbc);

        right.setEnabled(true);
        right.setVisible(true);
        rightPanel.setEnabled(true);
        rightPanel.setVisible(true);

        return right;
    }

    // --------------- private --------------------

    /**
     * Determine by whatever means, which monitor should be shown.
     */
    private int getDefaultSmallMonitor() {
        if (idleMonitor != -1)
            return idleMonitor;
        else {
            Preferences p = Preferences.access();
            String prefSetting = p.getPreference(MINI_PREF, null);

            int index = 0;

            // don't do anything if there's no monitors or no pref.
            if (prefSetting != null && monitors != null) {
                try {
                    for (int i = 0; i < monitors.length; i++) {
                        if (monitors[i].getClass().getName().equals(prefSetting)) {
                            index = i;
                            break;
                        }
                    }   // for
                }   // try
                catch (NumberFormatException excep) {
                    // ok, whatever...
                    index = 0;
                }
            }   // outer if
            // whatever happens, zero is the default

            return index;
        }
    }

    private int getMonitorIndex(Monitor m) {
        if (monitors == null || monitors.length == 0)
            return -1;

        for (int i = 0; i < monitors.length; i++)
            if (monitors[i] == m)
                return i;

        return -1;
    }

    // ---------- ComponentListener ----------
    public void componentHidden(ComponentEvent e) { }
    public void componentMoved(ComponentEvent e) { }
    public void componentResized(ComponentEvent e) {
        setDividerLocation(0.70d);
    }
    public void componentShown(ComponentEvent e) { }

    private JTextField leftField;
    private JPanel rightPanel;
    private JButton selectButt;
    private JButton magButt;

    private JComboBox selector;
    private JPopupMenu popMenu;
    private ActionListener actionListener = new SelectionListener();
    private ActionListener zoomListener;
    private Monitor[] monitors;
    private MonitorState state;
    private int currMonitor;
    private CardLayout monitorCards;
    private int runningMonitor = -1;
    private int idleMonitor = -1;
    private Timer clearTimer;
    private static final int CLEAR_TIMEOUT = 5000;  // time before msg is cleared

    private UIFactory uif;

    private static final String MINI_PREF = "exec.monitorstrip.mini";

    class SelectionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();

            if (source == magButt) {
                zoomListener.actionPerformed(e);
            }
            else if (source == selector) {
                Object item = selector.getSelectedItem();
                setMonitor(getMonitorIndex((Monitor)item));
            }
            else if (source instanceof JMenuItem) {
                try {
                    int index = Integer.parseInt(e.getActionCommand());

                    // no change, or index is out of valid range for some reason
                    if (currMonitor == index || index >= monitors.length)
                        return;

                    setMonitor(index);
                }   // try
                catch (NumberFormatException excep) {
                    // ignore event
                }       // catch
            }
        }   // actionPerformed()
    }

}

