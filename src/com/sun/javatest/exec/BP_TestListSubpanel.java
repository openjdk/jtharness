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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import javax.help.CSH;

import com.sun.javatest.Harness;
import com.sun.javatest.JavaTestError;
import com.sun.javatest.Status;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.tool.I18NUtils;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.Debug;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;
import java.awt.datatransfer.Clipboard;

/**
 * This panel renders information about the tests which are "filtered out" in
 * the current node.
 * <p>
 * The background thread has a two-stage commit process so that the iterator can
 * run at full speed, ignoring the MT-unsafe swing list model. The changes are
 * reflected in the real list when the AWT event queue schedules the
 * notification thread which is created during the iteration. This class also
 * processes the list click events and usually dispatches changes to the branch
 * panel model.
 *
 * <p>
 * If you need to synchronize against both the vLock (for live data) and this
 * class' lock, then blocks should be synchronized against this outer class,
 * then the vLock.  The ordering is vital to avoiding deadlocks.
 */
class BP_TestListSubpanel extends BP_BranchSubpanel {
    BP_TestListSubpanel(UIFactory uif, Harness h, ExecModel em,
            BP_Model bpm, TestTreeModel ttm, int state) {
        super("tl" + state, uif, bpm, ttm, "br.list");
        this.state = state;
        this.harness = h;
        this.execModel = em;

        init();

        // the enableHelp lines below are checked during the build
        switch (state) {
            case Status.PASSED:
                CSH.setHelpIDString(this, "browse.passedTab.csh");
                break;
            case Status.FAILED:
                CSH.setHelpIDString(this, "browse.failedTab.csh");
                break;
            case Status.ERROR:
                CSH.setHelpIDString(this, "browse.errorTab.csh");
                break;
            case Status.NOT_RUN:
                CSH.setHelpIDString(this, "browse.notRunTab.csh");
                break;
            case BranchPanel.STATUS_FILTERED:
                CSH.setHelpIDString(this, "browse.filteredOutTab.csh");
                break;
        } // switch

        // must do this after state variable is set
        cacheWatcher = new CacheObserver(state);
    }

    /**
     * This is to provide BranchPanel the ability to do batch updates.
     */
    /*
     * void removeTest(TestResult tr) { mod.removeTest(tr); }
     *
     * void addTest(TestResult tr) { mod.addTest(tr, false); }
     *
     * void addTest(TestResult[] trs) { if (trs == null || trs.length == 0)
     * return;
     *
     * for (int i = 0; i < trs.length - 1; i++) mod.addTest(trs[i], true);
     *  // do a final add with notification mod.addTest(trs[trs.length-1],
     * false); }
     */

    /**
     * Clear the table contents and prepare to receive new data.
     */
    void reset(TT_NodeCache cache) {
        // grab cache lock first because many other threads may alter that
        // object, causing a deadlock here.
        // sync. to hold observer traffic until re-sync is done
        // also see TableSynchronizer thread
        if (this.cache != null) {
            final TT_NodeCache cacheCopy = this.cache;
            synchronized (cacheCopy) {
                synchronized (BP_TestListSubpanel.this) {
                        cacheCopy.removeObserver(cacheWatcher);
                }   // sync this panel
            }   // sync cache
        }

        synchronized (BP_TestListSubpanel.this) {
            this.cache = cache;

            if (resyncThread != null) {
                resyncThread.halt();
            }

            if (mod != null)
                mod.reset();
        } // sync

        validateEnableState();
        // table.clearSelection();
    }

    protected void invalidateFilters() {
        super.invalidateFilters();

        // if we didn't have one, we certainly don't need to disconnect,
        // and probably don't need to get a new one...
        // XXX see reset() and TableSynchronizer.run(), should do this in a
        //     synchronized block?
        if (cache != null) {
            cache.removeObserver(cacheWatcher);

            if (subpanelNode != null) {
                cache = ttm.getNodeInfo(subpanelNode.getTableNode(), false);
                validateEnableState();
            }
        }
    }

    public void setUpdateRequired(boolean updateRequired) {
         this.updateRequired = updateRequired;
    }

    /**
     * Only called when this panel is onscreen and needs to be kept up to date.
     */
    protected synchronized void updateSubpanel(TT_BasicNode currNode) {
        super.updateSubpanel(currNode);

        if (debug) {
            Debug.println("updating test list " + state);
            Debug.println("  -> size " + (mod == null? 0 : mod.getRowCount()));
        }

        // only run if we change nodes
        if (updateRequired || filtersInvalidated) {
            if (resyncThread != null) {
                resyncThread.halt();
            }
            resyncThread = new TableSynchronizer(state);
            resyncThread.start();

            filtersInvalidated = false;
            validateEnableState();
            updateRequired = false;
        }
    }

    /**
     * Enable or disable this panel as necessary.
     */
    private void validateEnableState() {
        if (state < Status.NUM_STATES) { // handles all but filtered out
            if (cache.getStats()[state] > 0) {
                model.setEnabled(BP_TestListSubpanel.this, true);
                // setEmpty(false);
            } else if (cache.getStats()[state] == 0) {
                model.setEnabled(BP_TestListSubpanel.this, false);
                // setEmpty(true);
            } else {
            }
        } else
            throw new IllegalStateException();
    }

    /**
     * A special thread to repopulate the test lists.
     */
    private class TableSynchronizer extends Thread {
        TableSynchronizer(int whichList) {
            super("Test List Synchronizer" + whichList);
            setPriority(Thread.MIN_PRIORITY + 2);
        }

        public void run() {
            // grab cache lock first because many other threads may alter that
            // object, causing a deadlock here.
            // sync. to hold observer traffic until re-sync is done.
            // also see reset() for this panel
            final TT_NodeCache cacheCopy = cache;
            synchronized (cacheCopy) {
                synchronized (BP_TestListSubpanel.this) {
                    // resync with this node cache
                    newData = cacheCopy.addObserver(cacheWatcher, true);

                    // add tests into the list model - this doesn't make the
                    // data
                    // live though
                    for (int j = 0; j < newData[state].size() - 1; j++) {
                        if (stopping)
                            break;

                        if (sortingRequested) {
                           mod.sortTests(mod.liveData, mod.SORTING_COLUMN,
                                    mod.SORTING_MODE);
                        } else {
                            mod.addTest(newData[state].elementAt(j), true);
                        }

                    } // for

                    if (newData[state].size() > 0 && !stopping) {
                        // final item with a notify
                        mod.addTest(newData[state].lastElement(), false);
                    }

                    // to indicate completion
                    resyncThread = null;
                } // this sync
            } // cache sync
            validateEnableState();
        } // run()

        public void halt() {
            stopping = true;
        }

        private volatile boolean stopping;
    }

    private void init() {
        mod = new TestTableModel(uif);
        renderer = new TestCellRenderer(uif);
        listener = new InputListener();

        table = uif.createTable("br.fo.tbl", mod);
        table.setOpaque(true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getSelectionModel().addListSelectionListener(listener);

        // setup col 0
        TableColumn tc = table.getColumnModel().getColumn(0);
        tc.setCellRenderer(renderer);
        tc.setResizable(true);

        // setup col 1
        tc = table.getColumnModel().getColumn(1);
        tc.setCellRenderer(renderer);
        tc.setResizable(true);

        uif.setAccessibleInfo(this, "br.fo");

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.gridy = 0;
        gbc.ipady = 12;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        infoTL = uif.createMessageArea("br.tl.info");
        infoTL.setOpaque(false);
        add(infoTL, gbc);

        gbc.gridy = 1;
        gbc.weightx = 2.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(uif.createScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), gbc);

        InputListener tableListener = new InputListener();
        table.addMouseListener(tableListener);
        table.getTableHeader().addMouseListener(tableListener);
        table.getSelectionModel().addListSelectionListener(tableListener);

        // to trigger test selection when enter is pressed
        table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, 0, false), "gotoTest");
        table.getActionMap().put("gotoTest", new KbTableAction(uif.getI18NResourceBundle(),
                "br.list.enter"));

        String[] showDescr = { "show.title", "show.keywords" };
        showDescrMenu = uif.createMenu("br.description", showDescr,
                tableListener);

        String[] showRun = { "show.status", "show.time.start", "show.time.end" };
        showRunMenu = uif.createMenu("br.runtime", showRun, tableListener);

        popupHeader = uif.createPopupMenu("br");
        popupHeader.add(showDescrMenu);
        popupHeader.add(showRunMenu);

        String[] actions = { "action.run", "action.clear" };
        popupTable = uif.createPopupMenu("br", actions, tableListener);

        if (state == Status.NOT_RUN) { // ignore clear result in "not run"
            // panel
            showRunMenu.setEnabled(false);
            // XXX should avoid using absolute indexes
            popupTable.getComponent(1).setEnabled(false);
        }

        actions = new String[] { "action.cpnamelist", "action.cpnamestr" };
        popupTable.add(uif.createMenu("br.cp", actions, tableListener));

        // this is necessary to make sure that the split pane can resize
        // this panel. without setting the min., the panel seems to take
        // all it is given, and never gives it back.
        setMinimumSize(new Dimension(150, 100));
    }

    /**
     * It is assumed that this will run on the event thread. private void
     * setEmpty(boolean state) { if (state && list.getModel() !=
     * EmptyListModel.getInstance()) {
     * list.setModel(EmptyListModel.getInstance());
     * model.setEnabled(BP_TestListSubpanel.this, false); lastMsg = ""; } else
     * if (!state && list.getModel() == EmptyListModel.getInstance()) {
     * list.setModel(mod); model.setEnabled(BP_TestListSubpanel.this, true); } }
     */

    // ------------- inner class -------------
    /**
     * Enumerates tree in background to populate the list. If this thread is
     * running, consider list data incomplete. Swing cannot handle an updating
     * model, so there is a two-stage absorbtion of data. This thread runs with
     * no delay reading the TRT and placing that data into an "offline" queue.
     * It periodically schedules an event on the GUI event thread; when that
     * thread run, it copies the data from the offline area to the online area,
     * which is what the ListModel presents. This workaround assumes that Swing
     * will never dispatch more than one event at a time.
     */
    private class TestTableModel extends AbstractTableModel {
        TestTableModel(UIFactory uif) {
            super();

            colNames = new String[] { uif.getI18NString("br.list.col0.txt"),
                    uif.getI18NString("br.list.col1.txt") };

                    if (debug) {
                        Debug.println("TableModel constructed: ");
                        Debug.println("   -> " + this);
                    }

                    init();
        }

        // ---------- TableModel interface ----------
        public int getRowCount() {
            synchronized (liveData) {
                return liveData.size();
            }
        }

        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex >= colNames.length)
                throw new IndexOutOfBoundsException();
            else
                return columnIndex == 0 ? colNames[0] : uif.getI18NString("br.runtime.show.status.mit");
        }

        public Object getValueAt(int row, int column) {
            if (column == 0) {
                synchronized (liveData) {
                    return liveData.get(row);
                }
            } else if (column == 1) {
                synchronized (liveData) {
                    if ((liveData.get(row)) instanceof TestResult)
                        return (getSelectedProperty((TestResult) (liveData.get(row))));
                    else
                        // should not happen
                        return uif.getI18NString("br.list.notAvailable.txt");
                }
            } else
                throw new IndexOutOfBoundsException(
                        "Index into filtered out table is out of range: " + row
                        + ", " + column);
        }

        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false;
        }

        // ---------- Custom methods for this model ----------
        /**
         * @param suppressNotify
         *        Actively request that no update be scheduled.
         */
        void addTest(Object tr, boolean suppressNotify) {
            synchronized (vLock) {
                // make sure this item is not already in the list
                if (!inQueue.contains(tr) && !liveData.contains(tr))
                    inQueue.addElement(tr);
            } // sync

            // try not to saturate the GUI event thread
            if (!suppressNotify && !isUpdateScheduled) {
                TableNotifier tn = new TableNotifier(subpanelNode, this);
                pendingEvents.addElement(tn);
                EventQueue.invokeLater(tn);
            }
        }

        /**
         * Sorts data in the table
         *
         * @param v
         *        Current tests list
         * @param column
         *        Number of column sorting to be applied to
         * @param mode
         *        Indicates ascending (0) or descending (1) sorting
         */
        void sortTests(LinkedList v, int column, boolean mode) {
            synchronized (vLock) {
                inQueue = sort(v, mode);

                TableNotifier tn = new TableNotifier(subpanelNode, this);
                pendingEvents.addElement(tn);
                EventQueue.invokeLater(tn);
            }
        }

        /**
         * Remove the given test from the list. Ignored if the test is not in
         * the list.
         */
        void removeTest(Object tr) {
            synchronized (vLock) {
                rmQueue.addElement(tr);
                // try not to saturate the GUI event thread
                if (!isUpdateScheduled) {
                    TableNotifier tn = new TableNotifier(subpanelNode, this);

                    pendingEvents.addElement(tn);
                    EventQueue.invokeLater(tn);
                }
            } // sync

        }

        void reset() {
            synchronized (vLock) {
                init();
            }

            // force GUI to update the now empty list
            notifyDone();
        }

        // ------------ private --------------

        private String getSelectedProperty(TestResult tst) {
            try {
                if (show.equals("title"))
                    return ((TestResult) tst).getDescription().getTitle();
                else if (show.equals("keywords")) {
                    String[] s = ((TestResult) tst).getDescription().getKeywords();
                    if (s.length == 0)
                        return (uif.getI18NString("br.list.noKeywords.txt"));
                    else {
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < s.length; i++) {
                            sb.append(s[i]);
                            sb.append(" ");
                        }
                        return (sb.toString());
                    }
                } else if (show.equals(TestResult.EXEC_STATUS)) {
                    String tmpStr = ((TestResult) tst).getStatus().getReason();
                    return tmpStr == null || tmpStr.equals("") ?
                        uif.getI18NString("br.list.notAvailable.txt") : tmpStr;
                }
                return ((TestResult) tst).getProperty(show) == null ? uif.getI18NString("br.list.notAvailable.txt")
                        : ((TestResult) tst).getProperty(show);
            } catch (TestResult.Fault f) {
            }
            return (""); // should not be here
        }

        private void init() {
            // discard all pending events
            // this is necessary to ensure that update events which haven't
            // been processed are not processed after the model has changed
            // arguably, this should be solved by putting this init() onto
            // the event thread
            synchronized (pendingEvents) {
                for (int i = 0; i < pendingEvents.size(); i++) {
                    TableNotifier tn = (TableNotifier) (pendingEvents.get(i));
                    tn.cancel();
                } // for
            }

            inQueue = new Vector();
            rmQueue = new Vector();
            liveData = new LinkedList();

            isUpdateScheduled = false;
        }

        private Vector sort(LinkedList v, boolean mode) {

            o = new Object[v.size()];

            if (SORTING_COLUMN == 0)
                o = v.toArray();
            else if (SORTING_COLUMN == 1) {
                for (int i = 0; i < v.size(); i++) {
                    if (v.get(i) instanceof TestResult)
                        o[i] = getSelectedProperty((TestResult) v.get(i));
                    else
                        o[i] = ""; // should not happen
                }
            } else {
                throw new JavaTestError("Internal error: invalid "
                        + "column number specified: " + SORTING_COLUMN);
            }

            rows = new Sorter[o.length];

            for (int i = 0; i < rows.length; i++) {
                rows[i] = new Sorter();
                rows[i].index = i;
            }

            Arrays.sort(rows);

            int[] aaa = new int[v.size()];
            for (int i = 0; i < rows.length; i++) {
                if (mode) // ascending
                    aaa[i] = rows[i].index;
                else
                    // descending
                    aaa[i] = rows[rows.length - i - 1].index;
            }

            Vector temp = new Vector(v.size());

            for (int i = 0; i < v.size(); i++)
                temp.addElement(v.get(aaa[i]));

            return temp;
        }

        private class Sorter implements Comparable {
            public int index;

            public int compareTo(Object other) {
                Sorter otherRow = (Sorter) other;
                if (o[index] instanceof TestResult) {
                    return ((Comparable) ((TestResult) o[index]).getTestName()).
                             compareTo(((TestResult) o[otherRow.index]).getTestName());
                } else if (o[index] instanceof String) {
                    return ((Comparable) o[index]).compareTo(o[otherRow.index]);
                } else {
                    return index - otherRow.index; // should not happen
                }
            }
        }

        /**
         * Transfer data from the internal queue to the live data queue. This is
         * part of the Swing threading workaround. This method immediately exits
         * if there is no work to do. It also dispatches model update events if
         * necessary. This method always runs on the event dispatch thread.
         */
        private void goLive() {
            int firstNew, lastNew = 0;
            if (debug)
                Debug.println("BP_TL.TLM - goLive() starting.");
            // this is sync. against the outer class because we may change the
            // list model object during execution of this block
            synchronized (BP_TestListSubpanel.this) {
                synchronized (vLock) {
                    if (inQueue.size() == 0 && rmQueue.size() == 0) {
                        if (debug)
                            Debug.println("BP_TT.TTM - goLive() nothing to do, returning");
                        return;
                    }

                    processRemoveQueue();
                    //preprocessAddQueue();

                    // now add the new items
                    if (inQueue.size() != 0) {
                        synchronized (liveData) {
                            firstNew = liveData.size();
                            if (inQueue.size() < BATCH_SIZE) {
                                if (sortingRequested) {
                                    liveData.clear();
                                    liveData.addAll(inQueue);
                                    sortingRequested = false;
                                } else {
                                    liveData.addAll(inQueue);
                                    inQueue.setSize(0);
                                }
                                table.repaint();
                                lastNew = liveData.size() - 1;
                            } else { // only add some of the new items
                                if (sortingRequested) {
                                    liveData.clear();
                                    sortingRequested = false;
                                }

                                for (int i = 0; i < BATCH_SIZE; i++) {
                                    Object o = inQueue.remove(0);

                                    liveData.add(o);
                                }

                                // schedule a future update
                                if (!isUpdateScheduled) {
                                    TableNotifier tn = new TableNotifier(
                                            subpanelNode, this);
                                    pendingEvents.addElement(tn);
                                    EventQueue.invokeLater(tn);
                                }
                                table.repaint();
                                lastNew = liveData.size() - 1;
                            }
                        } // sync
                        // dispatch update range event to Swing
                        if (listenerList.getListenerCount() > 0) {
                            TableModelEvent e = new TableModelEvent(this,
                                    firstNew, lastNew,
                                    TableModelEvent.ALL_COLUMNS,
                                    TableModelEvent.INSERT);

                            TableNotifier tn = new TableNotifier(e, this);
                            pendingEvents.addElement(tn);
                            EventQueue.invokeLater(tn);
                        }
                    }

                    // enable this tab now that it has data
            /*
             * if (liveData.size() > 0) { // switch back from an empty
             * list setEmpty(false); } else { setEmpty(true); }
             */

                    // this clears the "please wait" message if needed
                    if (table.getSelectedRow() == -1 && inQueue.size() == 0)
                        showMessage("");
                } // sync
            }

            if (debug)
                Debug.println("BP_TL.LT - goLive() finished");
        }

        /**
         * Remove tests in the removal queue from the live data or the incoming
         * data. vLock should be locked when you call this method
         */
        private void processRemoveQueue() {
            if (rmQueue.size() == 0)
                return;

            while (rmQueue.size() > 0) {
                TestResult target = (TestResult) (rmQueue.remove(0));
                int targetIndex = liveData.indexOf(target);
                if (targetIndex != -1) {
                    synchronized (liveData) {
                        // necessary for proper synchronization
                        // should not be a problem really, based on how other
                        // locking is done, all work on liveData occurs in
                        // goLive()
                        targetIndex = liveData.indexOf(target);

                        // only should happen if the item disappears
                        if (targetIndex == -1)
                            continue;

                        liveData.remove(targetIndex);

                        // WARNING: since we are continually changing the
                        // contents of
                        // the data, you must notify the observers synchronously
                        // to get
                        // proper results
                        notifyRemoved(target, targetIndex);
                    } // sync
                }
            } // while
        }

        /**
         * Remove duplicates in the add queue. vLock should be locked when you
         * call this method
         */
        private void preprocessAddQueue() {
            // make sure this list does not contain dups
            for (int i = 0; i < inQueue.size(); i++) {
                if (liveData.contains(inQueue.elementAt(i))) {
                    inQueue.remove(i);
                    i--;
                } else {
                }
            } // for
        }

        // --------- event utility methods -----------
        /**
         * Notify observers that the given index was added
         */
        private void notifyAdded(TestResult what, int index) {
            if (listenerList.getListenerCount() > 0) {
                // may want to buffer these messages for performance
                TableModelEvent e = new TableModelEvent(this, index, index,
                        TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);

                if (EventQueue.isDispatchThread()) {
                    // XXX try without this to see perf. impact
                    // dispatch synchronously
                    fireTableChanged(e);
                } else {
                    // switch event onto AWT event thread
                    TableNotifier tn = new TableNotifier(e, mod);
                    pendingEvents.addElement(tn);
                    EventQueue.invokeLater(tn);
                }
            }
        }

        private void notifyRemoved(TestResult what, int index) {
            if (listenerList.getListenerCount() > 0) {
                // may want to buffer these messages
                TableModelEvent e = new TableModelEvent(this, index, index,
                        TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);

                if (EventQueue.isDispatchThread()) {
                    // XXX try without this to see perf. impact
                    // dispatch synchronously
                    fireTableChanged(e);
                } else {
                    // switch event onto AWT event thread
                    TableNotifier tn = new TableNotifier(e, mod);
                    pendingEvents.addElement(tn);
                    EventQueue.invokeLater(tn);
                }
            }
        }

        private void notifyDone() {
            if (listenerList.getListenerCount() > 0) {
                // may want to buffer these messages
                TableModelEvent e = new TableModelEvent(this);
                // switch event onto AWT event thread
                TableNotifier tn = new TableNotifier(e, mod);
                pendingEvents.addElement(tn);
                EventQueue.invokeLater(tn);
            }
        }

        private String[] colNames;

        // must sync. on vLock anytime you access inQueue or liveData
        private final Object vLock = new Object(); // lock for inQueue &
        // rmQueue

        private Vector inQueue; // queue of items to be added to live data

        private Vector rmQueue; // queue of items to be removed from live data

        private LinkedList liveData; // to allow manual synchronization

        Vector pendingEvents = new Vector();

        volatile boolean isUpdateScheduled; // are updates waiting in inQueue or
        // rmQueue

        private static final int BATCH_SIZE = 100;

        private static final int COLUMN_COUNT = 2;

        private Sorter[] rows;

        private Object[] o;

        private int SORTING_COLUMN = -1;

        private boolean SORTING_MODE = false;
    }

    private class CacheObserver extends TT_NodeCache.TT_NodeCacheObserver {
        CacheObserver(int state) {
            super();
            // configure our interest list
            interestList[MSGS_ALL] = false;
            interestList[MSGS_STATS] = false;

            switch (state) {
                case Status.PASSED:
                    interestList[MSGS_PASSED] = true;
                    break;
                case Status.FAILED:
                    interestList[MSGS_FAILED] = true;
                    break;
                case Status.ERROR:
                    interestList[MSGS_ERRORS] = true;
                    break;
                case Status.NOT_RUN:
                    interestList[MSGS_NOT_RUNS] = true;
                    break;
                case Status.NUM_STATES:
                    interestList[MSGS_FILTERED] = true;
                    break;
                default:
                    throw new IllegalStateException();
            } // switch
        }

        public void testAdded(int msgType, TestResultTable.TreeNode[] path,
                TestResult what, int index) {
            synchronized (BP_TestListSubpanel.this) {
                mod.addTest(what, false);
            }
        }

        public void testRemoved(int msgType, TestResultTable.TreeNode[] path,
                TestResult what, int index) {
            synchronized (BP_TestListSubpanel.this) {
                mod.removeTest(what);
            }
        }

        public void statsUpdated(int[] stats) {
            // ignore
        }
    }

    /**
     * This is a double duty class; it commits changes the model and also
     * dispatches given events. If instance var. lt is null, it dispatches
     * events, otherwise it triggers a commit on the list thread data using (<tt>goLive()</tt>).
     * This class is critical because it is the task which gets scheduled on the
     * event thread.
     */
    class TableNotifier implements Runnable {
        TableNotifier(TT_BasicNode n, TestTableModel m) {
            node = n;
            tm = m;
            tm.isUpdateScheduled = true;
        }

        TableNotifier(TableModelEvent e, TestTableModel m) {
            tm = m;
            tme = e;
        }

        public void run() {
            tm.pendingEvents.remove(this);

            // this message has been cancelled
            if (!isValid)
                return;

            if (tme == null) {
                // consume the update event
                tm.isUpdateScheduled = false;
                tm.goLive();
            } else {
                tm.fireTableChanged(tme);
            }
        }

        public void cancel() {
            isValid = false;
        }

        // used to validate the event at dispatch time
        TT_BasicNode node;

        // go live data, no event dispatch
        TestTableModel tm;

        // event dispatch items
        private TableModelEvent tme;

        private boolean isValid = true;
    } // list notifier

    /**
     * One of these listeners is associated with each of the test lists.
     */
    class InputListener extends MouseAdapter implements ListSelectionListener,
            ActionListener {

        // ActionListener
        public void actionPerformed(ActionEvent e) {
            final int[] rows = table.getSelectedRows();

            if (e.getActionCommand().equals("action.clear")) {
                if (rows.length > 0) {
                    final WorkDirectory wd = execModel.getWorkDirectory();

                    if (harness.isRunning()) {
                        uif.showInformationDialog("treep.cantPurgeRunning", null);
                        return;
                    }

                    Object[] toPurge = new Object[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        Object item = table.getValueAt(rows[i], 0);
                        if (item instanceof TestResult)
                            toPurge[i] = ((TestResult) item).getWorkRelativePath();
                        else {
                        } // should not happen
                    } // for

                    int confirm = uif.showYesNoDialog("treep.purgeItemsSure",
                            TestTreePanel.createNodeListString(TestTreePanel.createNodeList(toPurge)));
                    // user backs out
                    if (confirm == JOptionPane.NO_OPTION)
                        return;
                    else {
                        // this block is intended to do the following:
                        // - disable menu items
                        // - start purge on background thread
                        // - show a wait dialog if the operation exceeds a min.
                        // time
                        // - hide dialog and re-enable menu item when thread
                        // finishes

                        final JDialog d = uif.createWaitDialog(
                                "treep.waitPurge", table);
                        final String[] finalList = TestTreePanel.createNodeList(toPurge);

                        // disable all menu items
                        // setPopupItemsEnabled(false);

                        final Thread t = new Thread() {
                            public void run() {
                                for (int i = 0; i < rows.length; i++) {
                                    try {
                                        // this may take a long while...
                                        for (int j = 0; j < finalList.length; j++)
                                            wd.purge(finalList[j]);
                                    } // try
                                    catch (WorkDirectory.PurgeFault f) {
                                        // print something in log...
                                        I18NResourceBundle i18n = uif.getI18NResourceBundle();
                                        wd.log(i18n, "treep.purgeFail.err", f);
                                    } // catch
                                    finally {
                                        // fixup GUI on GUI thread
                                        try {
                                            EventQueue.invokeAndWait(new Runnable() {
                                                public void run() {
                                                    if (d.isShowing())
                                                        d.hide();
                                                    // enable all menu
                                                    // items
                                                    // setPopupItemsEnabled(true);
                                                }
                                            });
                                        } catch (InterruptedException e) {
                                        } catch (java.lang.reflect.InvocationTargetException e) {
                                        }
                                    } // outer try
                                } // for
                            } // run()
                        }; // thread

                        ActionListener al = new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                // show dialog if still processing
                                if (t == null)
                                    return;
                                else if (t.isAlive() && !d.isVisible()) {
                                    d.show();
                                } else if (!t.isAlive() && d.isVisible()) {
                                    // just in case...a watchdog type check
                                    d.hide();
                                }
                            }
                        };

                        // bgThread = t;

                        // show wait dialog if operation is still running after
                        // WAIT_DIALOG_DELAY
                        Timer timer = new Timer(WAIT_DIALOG_DELAY, al);
                        timer.setRepeats(false);

                        // do it!
                        // in this order to reduce race condition
                        timer.start();
                        t.start();
                    }
                } else { // no rows selected
                    // XXX show error dialog
                }
            } // "clear"
            else if (e.getActionCommand().equals("action.run")) {
                if (rows.length > 0) {
                    // special case check, remove all items if root
                    // is selected
                    if (harness.isRunning()) {
                        uif.showInformationDialog("treep.cantRunRunning", null);
                        return;
                    }
                    String[] result = new String[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        if (table.getValueAt(rows[i], 0) instanceof TestResult) {
                            TestResult r = (TestResult) table.getValueAt(
                                    rows[i], 0);
                            result[i] = r.getTestName();
                        } else
                            // should not happen
                            result[i] = table.getValueAt(rows[i], 0).toString();
                    } // for

                    execModel.runTests(result);
                } else { // now rows selected
                    // XXX show error dialog
                }
            } // run
            else if (e.getActionCommand().equals("action.cpnamelist") ||
                    e.getActionCommand().equals("action.cpnamestr")) {
                if (rows.length > 0) {
                    // special case check, remove all items if root
                    // is selected
                    if (harness.isRunning()) {
                        uif.showInformationDialog("bp.cp.isRunning", null);
                        return;
                    }
                    String[] result = new String[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        if (table.getValueAt(rows[i], 0) instanceof TestResult) {
                            TestResult r = (TestResult) table.getValueAt(
                                    rows[i], 0);
                            result[i] = r.getTestName();
                        } else
                            // should not happen
                            result[i] = table.getValueAt(rows[i], 0).toString();
                    } // for

                    StringSelection payload = null;
                    if (e.getActionCommand().equals("action.cpnamestr")) {
                        payload = new StringSelection(StringArray.join(result));
                    } else {
                        payload = new StringSelection(StringArray.join(result, "\n"));
                    }

                    // send to clipboard
                    if (payload != null) {
                        Toolkit.getDefaultToolkit().getSystemClipboard().
                            setContents(payload, null);
                        Clipboard selection = Toolkit.getDefaultToolkit().getSystemSelection();
                        if (selection != null)
                                selection.setContents(payload, null);
                    }

                } else { // now rows selected
                    // XXX show error dialog?
                }
            } // run
            else if (e.getActionCommand().equals("show.title")) {
                table.getColumnModel().getColumn(1).setHeaderValue(
                        uif.getI18NString("br.description.show.title.mit"));
                table.getTableHeader().resizeAndRepaint();
                show = "title";
            } else if (e.getActionCommand().equals("show.keywords")) {
                table.getColumnModel().getColumn(1).setHeaderValue(
                        uif.getI18NString("br.description.show.keywords.mit"));
                table.getTableHeader().resizeAndRepaint();
                show = "keywords";
            } else if (e.getActionCommand().equals("show.status")) {
                table.getColumnModel().getColumn(1).setHeaderValue(
                        uif.getI18NString("br.runtime.show.status.mit"));
                table.getTableHeader().resizeAndRepaint();
                show = TestResult.EXEC_STATUS;
            } // status
            else if (e.getActionCommand().equals("show.time.start")) {
                table.getColumnModel().getColumn(1).setHeaderValue(
                        uif.getI18NString("br.runtime.show.time.start.mit"));
                table.getTableHeader().resizeAndRepaint();
                show = TestResult.START;
            } else if (e.getActionCommand().equals("show.time.end")) {
                table.getColumnModel().getColumn(1).setHeaderValue(
                        uif.getI18NString("br.runtime.show.time.end.mit"));
                table.getTableHeader().resizeAndRepaint();
                show = TestResult.END;
            }
            table.repaint();

        } // action performed

        // Mouse Adapter
        public void mouseClicked(MouseEvent e) {
            if (e.getComponent() == table) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    popupTable.show(e.getComponent(), e.getX(), e.getY());
                } else {
                    JTable tbl = (JTable) (e.getComponent());
                    int col = table.columnAtPoint(e.getPoint());
                    int row = table.rowAtPoint(e.getPoint());
                    TableModel tm = table.getModel();
                    // an empty table can't do anything
                    if (tm.getRowCount() < 1) {
                        // clear the message field
                        showMessage("");
                        return;
                    }

                    // always use col 1, which is where the TestResult is
                    // we only really care which row was clicked on
                    TestResult tr = (TestResult) (tm.getValueAt(row, 0));

                    if (e.getClickCount() == 1) {
                        // show vital stats only
                        showMessage(I18NUtils.getStatusMessage(tr.getStatus()));
                    } else if (e.getClickCount() == 2) {
                        showTest(tr);
                    }
                }
            } else if (e.getComponent() == table.getTableHeader()) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    popupHeader.show(e.getComponent(), e.getX(), e.getY());
                } else if (e.getButton() == MouseEvent.BUTTON1) {
                    int tableColumn = table.columnAtPoint(e.getPoint());
                    int modelColumn = table.convertColumnIndexToModel(tableColumn);

                    mod.SORTING_COLUMN = modelColumn;
                    mod.SORTING_MODE = !mod.SORTING_MODE;

                    sortingRequested = true;

                    mod.sortTests(mod.liveData, mod.SORTING_COLUMN, mod.SORTING_MODE);
                }
            }
        }

        // ListSelectionListener
        public void valueChanged(ListSelectionEvent e) {
            int index = e.getLastIndex();

            if (mod.getRowCount() == 0 || index >= mod.getRowCount()) {
                // swing seems to generate re-selection events when the
                // list model is 'replaced' with a new one (completely
                // invalidated). for some reason it changes the selection
                // index without doing bounds checking
                return; // ignore invalid event
            }

            if (index != lastIndex) {
                TestResult tr = (TestResult) (mod.getValueAt(index, 0));

                // show vital stats only
                showMessage(I18NUtils.getStatusMessage(tr.getStatus()));
                lastIndex = index;
            }
        }

        private int lastIndex = -2;
    }

    /**
     * Action to handle user pressing enter to select a test.
     */
    private class KbTableAction extends AbstractAction {
        KbTableAction(I18NResourceBundle bund, String key) {
            desc = bund.getString(key + ".desc");
            name = bund.getString(key + ".act");
        }

        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();

            // nothing selected, ignore event
            if (row < 0)
                return;

            Object target = table.getModel().getValueAt(row, 0);

            // this shouldn't be the case...
            if (!(target instanceof TestResult))
                return;

            TestResult tr = (TestResult) target;
            showTest(tr);
        }

        public Object getValue(String key) {
            if (key == null)
                throw new NullPointerException();

            if (key.equals(NAME))
                return name;
            else if (key.equals(SHORT_DESCRIPTION))
                return desc;
            else
                return null;
        }

        private String name;

        private String desc;
    }

    class TestCellRenderer extends DefaultTableCellRenderer {
        public TestCellRenderer(UIFactory uif) {
            setOpaque(false);
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            if (value == null) // very strange...
                return this;

            if (value instanceof TestResult) {
                TestResult tr = (TestResult) value;
                setText(tr.getTestName());
                setToolTipText(I18NUtils.getStatusMessage(tr.getStatus()));
            } else if (value instanceof TT_TestNode) {
                TestResult tr = ((TT_TestNode)value).getTestResult();
                setText(tr.getTestName());
                setToolTipText(I18NUtils.getStatusMessage(tr.getStatus()));
            } else { // this will run for the property column (1)
                setText(value.toString());
            }

            setBorder(spacerBorder);
            setFont(getFont().deriveFont(Font.PLAIN));

            if (isSelected) {
                setOpaque(true);
                // setBackground(MetalLookAndFeel.getTextHighlightColor());
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                // setForeground(MetalLookAndFeel.getPrimaryControlDarkShadow());
                setForeground(table.getForeground());
                setOpaque(false);
            }

            // would like to find a better way to do this
            // seems like we can't do this properly until the first item
            // is rendered though
            if (!rowHeightSet) {
                table.setRowHeight(getFontMetrics(getFont()).getHeight()
                + ROW_HEIGHT_PADDING);
                rowHeightSet = true;
            }

            return this;
        }

        // border to pad left and right
        private Border spacerBorder = BorderFactory.createEmptyBorder(3, 3, 3,
                3);
    }

    private int state;
    private JTable table;
    private TestTableModel mod; // JTable model
    private TT_NodeCache cache;
    private CacheObserver cacheWatcher;
    private volatile TableSynchronizer resyncThread;
    private TableCellRenderer renderer;
    private InputListener listener;
    private JTextArea infoTL;
    private boolean rowHeightSet;

    private static final int ROW_HEIGHT_PADDING = 3;
    private static final int WAIT_DIALOG_DELAY = 3000; // 3 second delay

    private JPopupMenu popupTable, popupHeader;
    private JMenu showDescrMenu, showRunMenu, sortMenu;

    private Harness harness;
    private ExecModel execModel;

    private boolean debug = Debug.getBoolean(BP_TestListSubpanel.class);

    private boolean sortingRequested = false;

    private String show = TestResult.EXEC_STATUS;

    private Vector[] newData;

    private boolean updateRequired;
}
