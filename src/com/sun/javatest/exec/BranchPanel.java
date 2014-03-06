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
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import javax.help.CSH;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;

import com.sun.javatest.Harness;
import com.sun.javatest.JavaTestError;
import com.sun.javatest.Parameters;
import com.sun.javatest.Status;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;

import com.sun.javatest.tool.IconFactory;
import com.sun.javatest.tool.UIFactory;

import com.sun.javatest.util.Debug;

/**
 * This is the panel which shows information about a particular branch node in the
 * testsuite tree.  It contains a series of tabs, the first of which is a summary
 * of the number of passed, failed, etc... tests under the node.  The rest of the
 * tabs are lists of test names in each state (including filtered-out); these lists
 * a clickable.
 */

class BranchPanel
    extends JPanel
    implements FilterSelectionHandler.Observer
{
    BranchPanel(UIFactory uif, TreePanelModel model, Harness h, ExecModel em, JComponent parent,
                FilterSelectionHandler filterHandler, TestTreeModel ttm) {
        this.uif = uif;
        this.tpm = model;
        this.harness = h;
        this.execModel = em;
        this.parent = parent;
        this.filterHandler = filterHandler;
        this.ttm = ttm;
        initGUI();

        filterHandler.addObserver(this);
    }

    void setNode(TT_BasicNode tn) {
        if (tn == currNode)
            return;

        updatePanel(tn, currPanel);
    }

    /**
     * This method should only be called to indicate that a change has occurred
     * which replaces the active TRT.  Changes to the parameters (filters,
     * initial URLs, etc... should propagate thru the FilterConfig system.
     *
     * @param p A validated set of parameters.
     * @see com.sun.javatest.exec.FilterConfig
     */
    void setParameters(Parameters p) {
        this.params = p;

        TestResultTable newTrt = null;
        if (p.getWorkDirectory() != null) {
            newTrt = p.getWorkDirectory().getTestResultTable();
        }
    }

    void dispose() {
        // stop counter thread
        summPanel.dispose();
    }

    protected boolean isUpdateRequired() {
        return needToUpdateGUIWhenShown;
    }

    protected void initGUI() {
        setName("branch");
        allPanels = new BP_BranchSubpanel[NUM_TABS];
        allPanels[0] = summPanel = new BP_SummarySubpanel(uif, bModel, ttm);

        allPanels[1] = docPanel = new BP_DocumentationSubpanel(uif, bModel, ttm, execModel);

        // the summary tab is not a list
        lists = new BP_TestListSubpanel[Status.NUM_STATES];
        for (int i = 0; i < lists.length; i++) {
            allPanels[i+2] = lists[i] =
                new BP_TestListSubpanel(uif, harness, execModel, bModel, ttm, i);
        }

        allPanels[allPanels.length-1] = foPanel =
                new BP_FilteredOutSubpanel(uif, bModel, ttm);

        listDisplayStatus = new boolean[allPanels.length];

        // insert the summary panel at the beginning of the folder
        // filtered out at the end
        JComponent[] panes = new JComponent[3 + lists.length];
        panes[0] = summPanel;
        panes[1] = docPanel;
        panes[panes.length-1] = foPanel;
        System.arraycopy(lists, 0, panes, 2, lists.length);

        bPane = uif.createTabbedPane("br.tabs", panes);
        bPane.setTabPlacement(SwingConstants.TOP);
        bPane.setBorder(BorderFactory.createEmptyBorder());

        bPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Component c = bPane.getSelectedComponent();
                if (c instanceof BP_BranchSubpanel) {
                    BP_BranchSubpanel bsp = (BP_BranchSubpanel)c;
                    bsp.updateSubpanel(currNode);

                    if (bPane.isEnabledAt(bPane.indexOfComponent(c))) {
                        // set the bottom text message to reflect this tab's info
                        updatePanel(currNode, bsp);
                        statusTf.setText(bsp.getLastMessage());
                    }
                }
            }
        });

        // set the icons for enabled tabs
        for (int i = 2; i < NUM_TABS; i++) {
            // bPane.setIconAt(i, tabIcons[i]);
            // no icon on first (summary) tab
            bPane.setIconAt(i, IconFactory.getTestIcon(i - 2, false, true));
        }

        // set the icons for disabled tabs
        //tabIcons = loadDTabIcons();
        /*
        for (int i = 0; i < NUM_TABS; i++)
            bPane.setDisabledIconAt(i, tabIcons[i]);
        */

        currPanel = summPanel;
        CSH.setHelpIDString(bPane, CSH.getHelpIDString(currPanel));

        setLayout(new BorderLayout());
        add(bPane, BorderLayout.CENTER);

        // --- anonymous class ---
        ComponentListener cl = new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                // MAY NOT NEED THIS
                currPanel.invalidate();
                bPane.invalidate();
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
                if (needToUpdateGUIWhenShown) {
                    updateGUI();
                    needToUpdateGUIWhenShown = false;
                }
            }
            public void componentHidden(ComponentEvent e) {
            }
        };
        addComponentListener(cl);

        statusTf = uif.createOutputField("br.status");
        statusTf.setEnabled(true);
        statusTf.setEditable(false);
        add(statusTf, BorderLayout.SOUTH);
    }

    void setTreeModel(TestTreeModel ttm) {
        this.ttm = ttm;
        for (BP_BranchSubpanel bps : allPanels) {
            bps.setTreeModel(ttm);
        }
    }


    protected void updatePanel(TT_BasicNode newNode,
                               BP_BranchSubpanel newPanel) {

        if (newPanel != currPanel) {
            // update help for tabbed pane to reflect help for selected panel
            CSH.setHelpIDString(bPane, CSH.getHelpIDString(newPanel));
            currPanel = newPanel;
        }

        if (newNode != currNode) {
            if (debug) {
                Debug.println("BP - setting node to " + newNode.getShortName() + "  " + newNode);
                Debug.println("   -> old node " + (currNode == null ? "[null]" : currNode.getShortName()) + "  " + currNode);
            }

            for (int i = 0; i < lists.length; i++)
                lists[i].setUpdateRequired(true);

            currNode = newNode;
            //currNode.addObserver(this);

            if (isVisible())
                updateGUI();
            else
                needToUpdateGUIWhenShown = true;
        }
    }

    TT_TreeNode getNode() {
        return currNode;
    }

    /**
     * Call when the target node or tree data have changed.  This is called
     * internally to force updates when filters have changed.
     */
    protected void updateGUI() {
        if (currNode == null) {
            // start at 1 since we don't disable the summary tab
            for (int i = 1; i < bPane.getComponentCount(); i++)
                bPane.setEnabledAt(i, false);
        }
        else {
            // preemptively start these threads as soon as we change nodes
            // this allows us to get disabled tabs early - and have data if the user
            // does ask for it
            // we are changing nodes
            if (cache != null) {
                cache.removeObserver(cacheWatcher);
                // XXX tell cache to dump data?
            }

            cache = ttm.getNodeInfo(currNode.getTableNode(), false);
            cache.addObserver(cacheWatcher, false);

            // update all the panels
            summPanel.updateSubpanel(currNode);
            docPanel.updateSubpanel(currNode);
            foPanel.reset(cache);

            for (int i = 0; i < lists.length; i++) {
                lists[i].reset(cache);
            }   // for

            if (currPanel != summPanel)
                currPanel.updateSubpanel(currNode);
        }

    }

    /**
     * Is the given node along the given path?
     */
    private static boolean isAlongPath(TestResultTable.TreeNode[] path,
                                       TestResultTable.TreeNode node) {
        for (int i = 0; i < path.length; i++)
            if (path[i] == node)
                return true;

        return false;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    // --- FilterSelectionHandler.Observer ---

    public void filterUpdated(TestFilter f) {
        for (int i = 0; i < allPanels.length; i++)
            allPanels[i].invalidateFilters();

        updateGUI();
    }

    public void filterSelected(TestFilter f) {
        for (int i = 0; i < allPanels.length; i++)
            allPanels[i].invalidateFilters();

        updateGUI();
    }

    public void filterAdded(TestFilter f) {
        // we don't care here
    }

    public void filterRemoved(TestFilter f) {
        // we don't care here
    }

    private TestTreeModel ttm;
    private JTabbedPane bPane;
    private JTextField statusTf;
    private TT_BasicNode currNode;
    private TreePanelModel tpm;
    private Harness harness;
    private ExecModel execModel;
    private JComponent parent;
    private FilterSelectionHandler filterHandler;
    private TT_NodeCache cache;
    private CacheObserver cacheWatcher = new CacheObserver();
    //private TestResultTable lastTrt;

    private UIFactory uif;
    private BP_BranchSubpanel currPanel;
    private BranchModel bModel = new BranchModel();

    private volatile boolean needToUpdateGUIWhenShown;
    private volatile boolean needToUpdateData;

    protected Parameters params;

    // data for tabs beyond Status.NUM_STATES
    // pass, fail, error, notrun + filtered out
    protected static final int NUM_TABS = Status.NUM_STATES + 3;//2
    static final int STATUS_FILTERED = Status.NUM_STATES;
    // additional tab constants here

    // panels in the folder
    private BP_SummarySubpanel summPanel;
    private BP_DocumentationSubpanel docPanel;
    private BP_FilteredOutSubpanel foPanel;
    private BP_TestListSubpanel[] lists;
    private boolean[] listDisplayStatus;            // which tabs are enabled
    private BP_BranchSubpanel[] allPanels;

    private static boolean debug = Debug.getBoolean(BranchPanel.class);

    /**
     * Implementation of the model.
     * This inner class will be able to access the message field and
     * TestPanelModel to dispatch the messages it receives.
     */
    class BranchModel implements BP_Model {
        public boolean isRunning() {
            return harness.isRunning();
        }

        /**
         * Message are automatically placed onto the event thread.
         * @param msg Localized message to be displayed.
         */
        public void showMessage(String msg) {
            EventQueue.invokeLater(new TextUpdater(statusTf, msg, uif));
        }

        public void showTest(TestResult tr, Object[] path) {
            tpm.showTest(tr, new TreePath(path));
        }

        public void setEnabled(final Component c, final boolean state) {
            if (!EventQueue.isDispatchThread()) {
                Runnable cmd = new Runnable() {
                    public void run() {
                        BranchPanel.BranchModel.this.setEnabled(c, state);
                    }   // run()
                };      // end anon. class

                EventQueue.invokeLater(cmd);
            }
            else {      // now on event thread
                // only likely at startup
                if (bPane == null)
                    return;

                setEnabled(bPane.indexOfComponent(c), state);
            }
        }

        public boolean isEnabled(Component c) {
            int index = bPane.indexOfComponent(c);
            if (index == -1)
                return false;
            else
                return listDisplayStatus[index];
        }

        public TestFilter getFilter() {
            return filterHandler.getActiveFilter();
        }

        /**
         * Must be on the event thread!
         */
        void setEnabled(final int index, final boolean newState) {
            if (listDisplayStatus[index] == newState)
                return;

            if (!EventQueue.isDispatchThread()) {
                Runnable cmd = new Runnable() {
                    public void run() {
                        BranchPanel.BranchModel.this.setEnabled(index, newState);
                    }   // run()
                };      // end anon. class

                EventQueue.invokeLater(cmd);
            }
            else {          // on dispatch thread
                if (index == -1) {
                    return;
                }
                else {
                    listDisplayStatus[index] = newState;
                    bPane.setEnabledAt(index, newState);
                }
            }
        }
    }

    /**
     * Utility class to update text fields on the GUI thread.
     */
    static class TextUpdater implements Runnable {
        /**
         * Update, clear or wait messages into the text fields.
         * Update and wait require all three params to be valid, clear only requires that type
         * and the text fields.
         */
        TextUpdater(int eType, JTextComponent[] tfs, int[] values, UIFactory uif) {
            type = eType;
            this.values = values;
            this.tfs = tfs;
            this.uif = uif;
        }

        /**
         * Update a single text field.
         */
        TextUpdater(JTextComponent tf, String val, UIFactory uif) {
            type = MSG;
            stf = tf;
            msg = val;
            this.uif = uif;
        }

        public void run() {
            //if (!needUpdate)
            //    return;

            //needUpdate = false;

            switch (type) {
                case UPDATE:
                    for (int i = 0; i < tfs.length; i++) {
                        // -1 results in "wait..." being printed
                        if (values[i] >= 0)
                            tfs[i].setText(Integer.toString(values[i]));
                        else
                            tfs[i].setText("");
                    }
                    break;
                case CLEAR:
                    for (int i = 0; i < tfs.length; i++)
                        tfs[i].setText("");
                    break;
                case WAIT:
                    for (int i = 0; i < tfs.length; i++)
                        tfs[i].setText("wait...");
                    break;
                case MSG:
                    stf.setText(msg);
                    break;
                default:
                    throw new JavaTestError(uif.getI18NString("br.noEType2"));
            }   // switch
        }

        static final int UPDATE = 1;    // change the values to the given settings, -1 means no change
        static final int CLEAR = 2;     // set the labels to blank values
        static final int WAIT = 3;      // set the labels to show that we are waiting
        static final int MSG = 4;       // status text field update

        private int type;
        private int[] values;
        private JTextComponent[] tfs;
        private JTextComponent stf;
        private String msg;
        private UIFactory uif;
    }   // counter notifier

    private class CacheObserver extends TT_NodeCache.TT_NodeCacheObserver {
        CacheObserver() {
            super();

            // configure our interest list
            interestList[MSGS_ALL] = false;
            interestList[MSGS_STATS] = true;
            interestList[MSGS_PASSED] = false;
            interestList[MSGS_FAILED] = false;
            interestList[MSGS_ERRORS] = false;
            interestList[MSGS_NOT_RUNS] = false;
            interestList[MSGS_FILTERED] = false;
        }

        public void testAdded(int msgType, TestResultTable.TreeNode[] path,
                              TestResult what, int index) {
            // ignore
        }

        public void testRemoved(int msgType, TestResultTable.TreeNode[] path,
                                TestResult what, int index) {
            // ignore
        }

        public void statsUpdated(final int[] stats) {
            // enabled any disabled tabs which have contents
            // disable any enabled tabs which are empty
            if (!EventQueue.isDispatchThread()) {
                Runnable cmd = new Runnable() {
                    public void run() {
                        statsUpdated(stats);
                    }
                };      // end anon. class

                EventQueue.invokeLater(cmd);
            }
            else {
                for (int i = 0; i < stats.length; i++) {
                    if (stats[i] > 0 && !listDisplayStatus[i+2])
                        bModel.setEnabled(i+2, true);
                    if (stats[i] == 0 && listDisplayStatus[i+2])
                        bModel.setEnabled(i+2, false);
                }       // for

                int rej = cache.getRejectCount();
                if (rej == 0) {
                    if (listDisplayStatus[listDisplayStatus.length-1])
                        bModel.setEnabled(listDisplayStatus.length-1, false);
                    else {}
                }
                else if (rej > 0 && !listDisplayStatus[listDisplayStatus.length-1]) {
                    bModel.setEnabled(listDisplayStatus.length-1, true);
                }
            }   // else
        }
    }

}

