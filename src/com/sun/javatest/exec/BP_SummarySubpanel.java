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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.help.CSH;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
//import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.sun.javatest.JavaTestError;
import com.sun.javatest.Parameters;
import com.sun.javatest.Status;
import com.sun.javatest.TRT_TreeNode;
import com.sun.javatest.TestSuite;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.tool.I18NUtils;
import com.sun.javatest.tool.PieChart;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.Debug;

/**
 * Subpanel of BranchPanel.  This panel displays a summary of tests below the given
 * node which have passed, failed, errored, etc...
 */
class BP_SummarySubpanel extends BP_BranchSubpanel {

    BP_SummarySubpanel(UIFactory uif, BP_Model bpm, TestTreeModel ttm) {
        super("stats", uif, bpm, ttm, "br.summ");
        init();
    }

    private synchronized void init() {
        if (pieColors == null) {
            pieColors = new Color[I18NUtils.NUM_STATES];
            pieColors[Status.PASSED] = I18NUtils.getStatusColor(Status.PASSED);
            pieColors[Status.FAILED] = I18NUtils.getStatusColor(Status.FAILED);
            pieColors[Status.ERROR] = I18NUtils.getStatusColor(Status.ERROR);
            pieColors[Status.NOT_RUN] = I18NUtils.getStatusColor(Status.NOT_RUN);
            pieColors[pieColors.length - 1] = I18NUtils.getStatusColor(I18NUtils.FILTERED_OUT);
        }

        CSH.setHelpIDString(this, "browse.summaryTab.csh");

        JPanel bodyPanel = uif.createPanel("br.summ.body", false);
        bodyPanel.setLayout(new GridBagLayout());

        JPanel statsPanel = uif.createPanel("br.summStats", false);
        statsPanel.setLayout(new GridBagLayout());
        statsPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();

        // number of components in y-axis of layout before the text fields begin
        int pbarOffset = 3;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipady = 3;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;

        // --- could be static from here... ---
        sTypes = new JLabel[NUM_FIELDS];

        for (int i = 0; i < Status.NUM_STATES; i++) {
            // NOTE: intentional extra space in the line below
            //       to defeat static i18n checking in build
            sTypes[i] = uif.createLabel("br.summ.status" + i);
            sTypes[i].setHorizontalAlignment(SwingConstants.LEFT);
            sTypes[i].setDisplayedMnemonic(uif.getI18NString(
                    "br.summ.status" + i + ".mne").charAt(0));
            sTypes[i].setIcon(new LegendIcon(I18NUtils.getStatusBarColor(i), true));
            uif.setAccessibleDescription(sTypes[i], "br.summ.status" + i);
        // other label config here
        }   // for

        sTypes[SUBTOTAL_INDEX] = uif.createLabel("br.summ.subtotal");
        sTypes[FILTERED_INDEX] = uif.createLabel("br.summ.filtered");
        sTypes[TOTAL_INDEX] = uif.createLabel("br.summ.total");

        sTypes[FILTERED_INDEX].setIcon(
                new LegendIcon(I18NUtils.getStatusColor(I18NUtils.FILTERED_OUT), true));

        // a11y
        sTypes[SUBTOTAL_INDEX].setDisplayedMnemonic(uif.getI18NString(
                "br.summ.subtotal.mne").charAt(0));
        sTypes[FILTERED_INDEX].setDisplayedMnemonic(uif.getI18NString(
                "br.summ.filtered.mne").charAt(0));
        sTypes[TOTAL_INDEX].setDisplayedMnemonic(uif.getI18NString(
                "br.summ.total.mne").charAt(0));
        uif.setAccessibleDescription(sTypes[SUBTOTAL_INDEX], "br.summ.subtotal");
        uif.setAccessibleDescription(sTypes[FILTERED_INDEX], "br.summ.filtered");
        uif.setAccessibleDescription(sTypes[TOTAL_INDEX], "br.summ.total");

        sTypes[SUBTOTAL_INDEX].setBackground(UIFactory.Colors.PRIMARY_CONTROL_DARK_SHADOW.getValue());
        sTypes[SUBTOTAL_INDEX].setForeground(UIFactory.Colors.WINDOW_BACKGROUND.getValue());
        sTypes[SUBTOTAL_INDEX].setOpaque(true);
        sTypes[TOTAL_INDEX].setBackground(UIFactory.Colors.PRIMARY_CONTROL_DARK_SHADOW.getValue());
        sTypes[TOTAL_INDEX].setForeground(UIFactory.Colors.WINDOW_BACKGROUND.getValue());
        sTypes[TOTAL_INDEX].setOpaque(true);
        // --- to here ---

        sValues = new JTextField[NUM_FIELDS];

        for (int i = 0; i < Status.NUM_STATES; i++) {
            sValues[i] = createPlainField(i);
        }   // for
        sValues[FILTERED_INDEX] = createPlainField(FILTERED_INDEX);
        sValues[SUBTOTAL_INDEX] = createTotalField(SUBTOTAL_INDEX);
        sValues[TOTAL_INDEX] = createTotalField(TOTAL_INDEX);

        // use blank icons to get indentation effect
        sTypes[SUBTOTAL_INDEX].setIcon(
                new LegendIcon(sTypes[SUBTOTAL_INDEX].getBackground(), false));
        sTypes[TOTAL_INDEX].setIcon(
                new LegendIcon(sTypes[TOTAL_INDEX].getBackground(), false));

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        gbc.ipadx = 10;
        gbc.weightx = 0.0;

        // put it all into the layout
        for (int i = 0; i < NUM_FIELDS; i++) {
            gbc.gridy = i + pbarOffset;
            gbc.gridx = 1;
            gbc.insets.right = NONTOTAL_FIELD_RMARGIN;

            if (i == SUBTOTAL_INDEX || i == TOTAL_INDEX) {
                gbc.fill = GridBagConstraints.NONE;
            } else {
                gbc.fill = GridBagConstraints.HORIZONTAL;
            }

            // add label
            statsPanel.add(sTypes[i], gbc);

            gbc.gridx = 2;

            if (sValues[i] != subtotalTf &&
                    sValues[i] != totalTf) {
                gbc.insets.right = NONTOTAL_FIELD_RMARGIN;
            } else {
                gbc.insets.right = TOTAL_FIELD_RMARGIN;
            }

            // add value
            statsPanel.add(sValues[i], gbc);

            // a11y
            sTypes[i].setLabelFor(sValues[i]);
        }   // for

        // ----- now configure outer panel ----
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weighty = 10.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipadx = 10;

        // component to fill extra vertical space
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 10.0;
        gbc.gridwidth = 3;
        Component strut = Box.createVerticalStrut(1);
        bodyPanel.add(strut, gbc);

        // components to fill extra horizontal space
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 1;
        gbc.weightx = 3.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        strut = Box.createHorizontalStrut(1);
        bodyPanel.add(strut, gbc);  // left
        gbc.gridx = 4;
        strut = Box.createHorizontalStrut(1);
        bodyPanel.add(strut, gbc);       // right

        gbc.weighty = 0.0;

        // add folder and filter fields
        // folder name display
        JLabel lab = uif.createLabel("br.summ.fldlbl", true);
        lab.setOpaque(false);
        folderNameTf = uif.createOutputField("br.summ.fldtf", 35, lab);
        folderNameTf.setOpaque(false);
        folderNameTf.setBorder(BorderFactory.createEmptyBorder());
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.weightx = 1.0;
        gbc.gridy = 1;
        gbc.gridx = 1;
        bodyPanel.add(lab, gbc);

        // output field
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 5.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        bodyPanel.add(folderNameTf, gbc);

        // view filter display
        lab = uif.createLabel("br.summ.ftrlbl", true);
        lab.setOpaque(false);
        filterNameTf = uif.createOutputField("br.summ.ftrtf", 35, lab);
        filterNameTf.setOpaque(false);
        filterNameTf.setBorder(BorderFactory.createEmptyBorder());
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        bodyPanel.add(lab, gbc);

        // output field
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 5.0;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bodyPanel.add(filterNameTf, gbc);

        filterDescTf = uif.createOutputField("br.summ.fdesc", 35);
        filterDescTf.setOpaque(false);
        filterDescTf.setBorder(BorderFactory.createEmptyBorder());
        gbc.gridy = 3;
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        bodyPanel.add(filterDescTf, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        JComponent div = new Divider(1, 5);
        div.getInsets().top = 5;
        div.getInsets().bottom = 5;
        bodyPanel.add(div, gbc);

        // spacer
        /*
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        strut = Box.createVerticalStrut(12);
        bodyPanel.add(strut, gbc);
         */

        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 1;
        gbc.weightx = 3.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        strut = Box.createHorizontalStrut(1);
        bodyPanel.add(strut, gbc);  // left

        JPanel pan = uif.createPanel("br.statpie", new GridBagLayout(), false);
        pan.setOpaque(false);
        GridBagConstraints gl = new GridBagConstraints();
        gl.fill = GridBagConstraints.NONE;
        gl.anchor = GridBagConstraints.CENTER;
        gl.gridy = 0;
        gl.gridx = 0;
        gl.weightx = 1.0;
        statsPanel.setMinimumSize(new Dimension(80, 80));
        pan.add(statsPanel, gl);
        statsPanel.setMinimumSize(new Dimension(150, 100));
        pie = new PieChart(new int[]{100},
                new Color[]{I18NUtils.getStatusColor(I18NUtils.FILTERED_OUT)});
        //pie.setOpaque(false);
        gl.fill = GridBagConstraints.BOTH;
        gl.weightx = 2.0;
        gl.gridx = 1;
        pan.add(pie, gl);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridheight = 1;
        gbc.gridwidth = 3;
        bodyPanel.add(pan, gbc);

        // stats
        /*
        gbc.weighty = 0.0;
        gbc.weightx = 2.0;
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bodyPanel.add(statsPanel, gbc);

        // pie
        pie = new PieChart(new int[] {100},
        new Color[] {I18NUtils.getStatusColor(I18NUtils.FILTERED_OUT)});
        pie.setOpaque(false);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 2.0;
        gbc.weighty = 5.0;
        gbc.gridx = 3;
        bodyPanel.add(pie, gbc);
         */

        // spacer to take up space
        gbc.gridwidth = 5;
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.weightx = 10.0;
        gbc.weighty = 10.0;
        strut = Box.createVerticalStrut(5);
        bodyPanel.add(strut, gbc);

        /*
        // create the message area
        JTextArea ta = uif.createMessageArea("br.statInfo");
        ta.setColumns(30);
        ta.setRows(2);

        // add message area to bottom of panel
        gbc.weighty = 2.0;
        gbc.weightx = 10.0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;

        bodyPanel.add(ta, gbc);

        setBackground(Color.green);
        setOpaque(true);
         */

        // this is necessary to make sure that the split pane can resize
        // this panel.  without setting the min., the panel seems to take
        // all it is given, and never gives it back.
        bodyPanel.setMinimumSize(new Dimension(150, 100));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setOpaque(true);

        JScrollPane sp = uif.createScrollPane(bodyPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setMinimumSize(new Dimension(150, 100));
        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);
        setMinimumSize(new Dimension(150, 100));
    }

    void dispose() {
        if (ct != null) {
            ct.interrupt();
            ct = null;
        }
    }

    protected JTextField createPlainField(int id) {
        JTextField f = uif.createOutputField("br.summ." + id, NTFIELD_WIDTH);
        f.setHorizontalAlignment(SwingConstants.RIGHT);
        f.setBorder(BorderFactory.createEmptyBorder());
        f.setOpaque(false);
        // other field config here

        return f;
    }

    protected JTextField createTotalField(int id) {
        JTextField f = uif.createOutputField("br.summ." + id, TFIELD_WIDTH);
        f.setHorizontalAlignment(JTextField.RIGHT);
        f.setBorder(null);
        f.setBackground(UIFactory.Colors.PRIMARY_CONTROL_DARK_SHADOW.getValue());
        f.setForeground(UIFactory.Colors.WINDOW_BACKGROUND.getValue());
        f.setOpaque(true);
        f.setEnabled(true);
        // other field config here

        return f;
    }

    protected void updateSubpanel(TT_BasicNode currNode) {
        super.updateSubpanel(currNode);

        // XXX consider doing it in the background anyway
        //     and altering the thread priority based on visibility
        //if (!isVisible())
        //return;

        //if (ct != null && subpanelNode == ct.getNode())
        //   return;

        if (debug > 1) {
            Debug.println("BP_SP - updateSubpanel() called");
        }

        if (filterNameTf != null && filterDescTf != null &&
                !(ttm.getTestFilter().getName().equals(filterNameTf.getText()))) {
            filterNameTf.setText(ttm.getTestFilter().getName());
            filterDescTf.setText(ttm.getTestFilter().getDescription());
        }

        // for efficiency, we won't restart the thread on an update request.
        // this may lead to synchronization issues where the numbers will
        // be slightly off.
        // as a possibly solution, we might want to set a flag so once the
        //    current thread finishes, it restarts a new counter thread.
        // alternatively, we can assume that another update will occur
        //    in a short period of time without intervention



        // it was "ct != null && ct.getNode() == subpanelNode" , result was always false.
        // after fixing this behavuiour can change!!!
        if (ct != null && ct.getNode() == subpanelNode.getTableNode()) {
            // same node and we are counting
            return;
        } else {
            if (debug > 0) {
                Debug.println("BP_SP - updating panel");
            }

            // we are watching a new node, or need to update the current stats
            if (ct == null) {
                ct = new CounterThread(subpanelNode.getTableNode(), sValues);
                ct.setPriority(Thread.MIN_PRIORITY + 3);
                ct.start();
            }

            // in all cases, tell the thread what to look at
            ct.setNode(subpanelNode.getTableNode());
            String spn = subpanelNode.getDisplayName();
            if (spn != null) {
                folderNameTf.setText(spn);
            } else {
                Parameters p = ttm.getParameters();
                if (p != null) {
                    TestSuite ts = p.getTestSuite();
                    folderNameTf.setText((ts == null ? "" : uif.getI18NString("br.summ.fldtf.root", ts.getName())));
                }
            }

        }
    }

    /**
     * Iterates to get the necessary data and updates the GUI.
     * The behavior depends on whether the harness is running or not.  If it is not
     * running tests, the GUI is incrementally updated.  If it is running, the
     * GUI is only updated after the new data is available.  The result should be
     * better behavior from the user's standpoint.
     */
    private class CounterThread extends Thread {

        /**
         * @param tn The node whose stats are to be scanned and placed onscreen.
         * @param fs Filters used to correctly generate stats.
         * @param fields Components to be updated with stats.
         */
        CounterThread(TestResultTable.TreeNode tn, JTextField[] fields) {
            super("BP_SP.CounterThread");
            node = tn;
            tfs = fields;
        }

        /**
         * Discover the node this thread is scanning.
         */
        TestResultTable.TreeNode getNode() {
            return node;
        }

        public void run() {
            if (debug) {
                Debug.println("BP_SP.CT - thread running");
                Debug.println("   -> " + this);
            }

            if (stopping) {
                halt();
                return;
            }

            // high priority cache request
            synchronized (this) {
                cache = ttm.getNodeInfo(node, true);
            }

            int[] stats;
            int[] combinedStats;

            // do not overwrite the info onscreen if we are in the middle of a run
            // otherwise reset numbers to zero
            if (!model.isRunning()) {
                EventQueue.invokeLater(
                        new BranchPanel.TextUpdater(BranchPanel.TextUpdater.CLEAR,
                        tfs, null, uif));
                showMessage(uif.getI18NString("br.summ.working"));
            }

            while (true) {
                if (stopping) {
                    if (debug) {
                        Debug.println("BP_SP.CT - thread terminating in run() due to halt()");
                        Debug.println("   -> " + this);
                    }
                    // terminates the thread
                    break;
                }

                // refresh reference from model
                // cache == null usually indicates we changed nodes


                boolean isComplete = false;
                synchronized (this) {
                    //refresh everytime
                    //if (cache == null || !cache.isValid())
                    cache = ttm.getNodeInfo(node, true);

                    if (cache == null) {
                        continue;
                    }

                    // update the GUI stats
                    stats = cache.getStats();
                    combinedStats = fillStats(stats, cache.getRejectCount(), true, true);
                    isComplete = cache.isComplete() || cache.isAborted();
                }   // sync

                if (!Arrays.equals(lastStats, combinedStats)) {
                    notifyUpdate(stats, combinedStats);
                    lastStats = combinedStats;
                    updateMessage(stats, !isComplete);
                } else if(ttm.isWorkPaused()) {
                    //Debug.println("Pause update, but have " + ((TRT_TreeNode)node).getEstimatedSize());
                    int[] tmpStats = new int[Status.NUM_STATES];
                    tmpStats[Status.NOT_RUN] = ((TRT_TreeNode)node).getEstimatedSize();
                    updateMessage(tmpStats, true);
                }
                else {
                    //Debug.println("Insig update - ignored");
                }

                synchronized (this) {
                    try {
                        if (cache == null || cache.isComplete() || cache.isAborted()) {
                            wait(INACTIVE_FREQUENCY);
                        } else {
                            wait(ACTIVE_FREQUENCY);
                        }
                    } catch (InterruptedException e) {
                        // someone wants this thread dead
                        return;
                    }   // catch
                }
            }   // while

            // checkpoint
            if (stopping) {
                // warning, possible incomplete data in GUI
                halt();
                return;
            }
            // update the GUI stats
            // synchronized to protect again NPE when setNode() nulls the cache ref.
            synchronized (this) {
                if (cache == null) {
                    cache = ttm.getNodeInfo(node, true);
                }

                stats = cache.getStats();
                notifyUpdate(stats,
                        fillStats(stats, cache.getRejectCount(), true, true));
            }   // sync

            updateMessage(stats, false);

            if (debug) {
                Debug.println("counter thread done...");
            }
        }       // run()

        /**
         * Request that this list scanning thread terminate.
         * The run method should exit promptly.
         */
        public void halt() {
            if (debug) {
                Debug.println("BP_SP.CT - thread stopping");
                Debug.println("   -> " + this);
            }

            stopping = true;
        }

        synchronized void setNode(TestResultTable.TreeNode tn) {
            if (tn != node) {
                cache = null;
                node = tn;
            }
        }

        /**
         * Create an array of length tfs.length.  Containing the basic + filter stats,
         * the subtotal and total fields empty.
         *
         * @param basic Array of length Status.NUM_STATES, probably output from
         *        TreeIterator.getResultStats().
         * @param filtered Number of tests filtered out.  Should be zero or greater.
         * @param validSub Is the subtotal okay to calculate and display?
         * @param validTot Is the total okay to calculate and display?
         */
        private int[] fillStats(int[] basic, int filtered, boolean validSub,
                boolean validTot) {
            int[] result = new int[tfs.length];

            System.arraycopy(basic, 0, result, 0, basic.length);

            // subtotal position
            if (validSub) {
                int sub = 0;
                for (int i = 0; i < basic.length; i++) {
                    sub += basic[i];
                }

                result[SUBTOTAL_INDEX] = sub;
            } else // disable this field
            {
                result[SUBTOTAL_INDEX] = -1;
            }

            result[FILTERED_INDEX] = filtered;

            // total position
            if (validTot) {
                result[TOTAL_INDEX] = result[FILTERED_INDEX] + result[SUBTOTAL_INDEX];
            } else // disable this field
            {
                result[TOTAL_INDEX] = -1;
            }

            return result;
        }

        private void updateMessage(int[] stats, boolean stillRunning) {
            // calculate the message at the bottom
            // XXX this needs to be moved to the outer class, but stats are
            // convenient here.  also this panel may not always be updated if
            // not visible, so this code will not run on time
            int worstIndex = -1;
            for (int i = 0; i < stateOrdering.length; i++) {
                if (stats[stateOrdering[i]] > 0) {
                    worstIndex = stateOrdering[i];
                    break;
                }
            }   // for

            // checkpoint
            if (stopping) {
                halt();
                return;
            }

            if (worstIndex != -1) {
                // NOTE: this is the switch is used instead of this line to allow static
                // i18n string checking to work
                //statusTf.setText(uif.getI18NString("br.worst." + worstIndex));

                if (Status.NUM_STATES > 4) {
                    throw new JavaTestError(uif.getI18NString("br.worstNum"));
                }

                String msg = null;

                switch (worstIndex) {
                    case 0:
                        msg = uif.getI18NString("br.worst.0");
                        break;
                    case 1:
                        msg = uif.getI18NString("br.worst.1");
                        break;
                    case 2:
                        msg = uif.getI18NString("br.worst.2");
                        break;
                    case 3:
                        msg = uif.getI18NString("br.worst.3");
                        break;
                    default:
                        throw new JavaTestError(uif.getI18NString("br.worstNum"));
                }

                if (stillRunning) {
                    msg = uif.getI18NString("br.stillWork") + msg;
                }

                showMessage(msg);

            } else {
                showMessage(uif.getI18NString("br.none"));
            }

        }

        private void notifyUpdate(final int[] basic, final int[] values) {
            EventQueue.invokeLater(
                    new BranchPanel.TextUpdater(BranchPanel.TextUpdater.UPDATE,
                    tfs,
                    values,
                    uif));
            final int[] pieStats = new int[I18NUtils.NUM_STATES];
            System.arraycopy(basic, 0, pieStats, 0, basic.length);
            pieStats[I18NUtils.FILTERED_OUT] = values[FILTERED_INDEX];
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    pie.setValue(pieStats, pieColors);
                /*
                int sum = 0;
                for (int i = 0; i < pieStats[i]; i++)
                sum += pieStats[i];

                // hide pie if all filtered out or not run
                if (pieStats[FILTERED_INDEX] == sum ||
                pieStats[I18NUtils.NOT_RUN] == sum)
                pie.setVisible(false);
                else
                pie.setVisible(true);
                 */
                }
            });
        }
        private volatile boolean stopping;
        private TestResultTable.TreeNode node;
        private TT_NodeCache cache;
        private JTextField[] tfs;
        private int[] lastStats;        // to optimize out extra GUI refreshes
        private boolean debug = Debug.getBoolean(BP_SummarySubpanel.class, "CounterThread");
    }

    class Divider extends JComponent {

        /**
         * Creates a default 2 wide pair of lines.
         */
        public Divider() {
        }

        public Divider(int thickness, int spacing) {
            thick = thickness;
            space = spacing;
        }

        protected void paintComponent(Graphics g) {
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            Insets inset = getInsets();
            int width = getWidth() - inset.left - inset.right;
            int height = getHeight() - inset.left - inset.right;

            g.setColor(Color.BLACK);

            /*
            if (height < 2)
            return;
            else if (height < ((thick * 2) + space)) {
             */
            // not enough space for two lines, draw one
            int mid = height / 2;
            g.fillRect(inset.left, mid - (thick / 2), width, thick);
        /*
        g.fillRect(inset.left, mid-(thick/2), width, thick);
        }
        //else if (height < ((thick * 2) + space) ) {
        else {
        // draws two lines vertically centered, spanning the entire
        // usable width
        int mid = height / 2;
        g.fillRect(inset.left, mid-(space/2)-thick, width, thick);
        g.fillRect(inset.left, mid+(space/2), width, thick);
        }
         */
        }

        public Dimension getPreferredSize() {
            return getMinimumSize();
        }

        public Dimension getMinimumSize() {
            Insets inset = getInsets();
            return new Dimension(10 + inset.left + inset.right,
                    thick * 2 + space + inset.top + inset.bottom);
        }
        int thick = 2;
        int space = 4;
    }

    // incomplete
    class Divider2 extends JComponent {

        public Divider2() {
        }

        public Divider2(int thickness) {
            thick = thickness;
        }

        protected void paintComponent(Graphics g) {
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            Insets inset = getInsets();
            int width = getWidth() - inset.left - inset.right;
            int height = getHeight() - inset.left - inset.right;

            g.setColor(UIFactory.Colors.SEPARATOR_FOREGROUND.getValue());

            if (height < 2) {
                return;
            } else if (height < (thick * 2)) {
                // not enough space for two lines, draw one
                int mid = height / 2;
                g.fillRect(inset.left, mid - (thick / 2), width, thick);
            } else {
                // usable width
                int mid = height / 2;
                g.fillRect(inset.left + arcWidth, mid - thick * 2,
                        width - arcWidth, thick);
                g.fillArc(inset.left, mid - (thick * 2), width, thick * 2, -90, 0);
            }
        }

        public Dimension getPreferredSize() {
            return getMinimumSize();
        }

        public Dimension getMinimumSize() {
            Insets inset = getInsets();
            return new Dimension(arcWidth * 2 + inset.left + inset.right,
                    thick * 2 + inset.top + inset.bottom);
        }
        int thick = 2;
        int arcWidth = 15;
    }

    private class LegendIcon implements Icon {

        LegendIcon(Color c, boolean shadow) {
            color = c;
            shadowColor = new Color(0x555555);
            paintShadow = shadow;
        }

        // creates a transparent icon
        LegendIcon() {
            color = shadowColor = null;
        }

        public int getIconWidth() {
            return 16;
        }

        public int getIconHeight() {
            return 16;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (image == null) {
                image = new BufferedImage(getIconWidth(), getIconHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics imageG = image.getGraphics();
                paintIt(imageG, paintShadow, color);
                imageG.dispose();
            }

            g.drawImage(image, x, y, null);
        }

        private void paintIt(Graphics g, boolean shadow, Color c) {
            if (c == null) {
                return;
            }

            int x = 2, y = 2, width, height;
            if (shadow) {
                width = height = 10;
            } else {
                width = height = 12;
            }

            if (shadow) {
                g.setColor(shadowColor);
                g.fillRect(x + 2, y + 2, width, height);
            }

            /*
            if (c.equals(Color.WHITE)) {
            // outline for a white icon
            g.setColor(Color.BLACK);
            g.fillRect(x,y,width,height);
            g.setColor(c);
            g.fillRect(x,y,width-1,height-1);
            }
            else {
             */
            g.setColor(c);
            g.fillRect(x, y, width, height);
        //}
        }
        private Color color,  shadowColor;
        private Image image;
        private boolean paintShadow;
    }
    private CounterThread ct;
    private int[] stats;
    private int filtered;
    private JLabel progLabel;
    private ProgressMeter pMeter;
    private /* static */ JLabel[] sTypes;
    private JTextField[] sValues;
    private JTextField subtotalTf;
    private JTextField totalTf;
    private JTextField folderNameTf;
    private JTextField filterNameTf;
    private JTextField filterDescTf;
    private PieChart pie;
    private static Color[] pieColors;
    // seven fields:
    // pass, fail, error, not run, subtotal, filtered out, total
    private static final int NUM_FIELDS = 7;
    private static final int SUBTOTAL_INDEX = 4;
    private static final int FILTERED_INDEX = 5;
    private static final int TOTAL_INDEX = 6;
    private static final int NONTOTAL_FIELD_RMARGIN = 10;
    private static final int TOTAL_FIELD_RMARGIN = 2;
    private static final int ACTIVE_FREQUENCY = 1000;       // while iterator running
    private static final int INACTIVE_FREQUENCY = 2500;     // while just waiting for updates
    private final int NTFIELD_WIDTH = 5;
    private final int TFIELD_WIDTH = 7;
    protected final int[] stateOrdering = {Status.ERROR, Status.FAILED, Status.NOT_RUN, Status.PASSED};
    private static int debug = Debug.getInt(BP_SummarySubpanel.class);
}
