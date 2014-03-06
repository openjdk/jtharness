/*
 * $Id$
 *
 * Copyright (c) 2004, 2011, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
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

import com.sun.javatest.TestFilter;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.tool.I18NUtils;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.Debug;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.JPopupMenu;

/**
 * This panel renders information about the tests which are "filtered out" in
 * the current node.  It shows the test name, as well as the reason that the
 * test was filtered out.  The tooltip are also upgraded beyond the standard
 * list to provide more filter information.
 * <p>
 * The background thread has a
 * two-stage commit process so that the iterator can run at full speed,
 * ignoring the MT-unsafe swing list model.  The changes are reflected in the
 * real list when the AWT event queue schedules the notification thread which
 * is created during the iteration.  This class also processes the list
 * click events and usually dispatches changes to the branch panel model.
 *
 * <p>
 * If you need to synchronize against both the vLock (for live data) and this
 * class' lock, then blocks should be synchronized against this outer class,
 * then the vLock.  The ordering is vital to avoiding deadlocks.
 */
class BP_FilteredOutSubpanel extends BP_BranchSubpanel {
    BP_FilteredOutSubpanel(UIFactory uif, BP_Model bpm, TestTreeModel ttm) {
        super("fo", uif, bpm, ttm, "br.fo");

        init();
        CSH.setHelpIDString(this, "browse.filteredOutTab.csh");

        cacheWatcher = new CacheObserver();
    }

    /**
     * Clear the table contents and prepare to receive new data.
     */
    void reset(TT_NodeCache cache) {
        synchronized (BP_FilteredOutSubpanel.this) {
            if (this.cache != null)
                this.cache.removeObserver(cacheWatcher);

            this.cache = cache;

            if (resyncThread != null) {
                resyncThread.halt();
            }

            if (mod != null)
                mod.reset();
        }   // sync

        validateEnableState();
        //table.clearSelection();
    }

    protected void invalidateFilters() {
        super.invalidateFilters();

        // if we didn't have one, we certainly don't need to disconnect,
        // and probably don't need to get a new one...
        if (cache != null) {
            cache.removeObserver(cacheWatcher);
        }

        if (subpanelNode != null) {
            cache = ttm.getNodeInfo(subpanelNode.getTableNode(), false);
            validateEnableState();
        }

        updateInfoText();
    }

    /**
     * Only called when this panel is onscreen and needs to be kept up to date.
     */
    protected synchronized void updateSubpanel(TT_BasicNode currNode) {
        super.updateSubpanel(currNode);

        // only run if we change nodes
        if (lastNode != currNode || filtersInvalidated) {
            if (debug)
                Debug.println("updating FO table");

            if (resyncThread != null) {
                resyncThread.halt();
            }

            resyncThread = new TableSynchronizer();
            resyncThread.start();
            lastNode = currNode;

            filtersInvalidated = false;
            validateEnableState();
        }
    }

    private void updateInfoText() {
        if (infoTa == null)
            return;

        TestFilter f = model.getFilter();
        if (f != null)
            infoTa.setText(uif.getI18NString("br.fo.info.txt", f.getName()));
        else
            infoTa.setText(uif.getI18NString("br.fo.noFn.txt"));
    }

    /**
     * Enable or disable this panel as necessary.
     */
    private void validateEnableState() {
        if (cache.getRejectCount() > 0) {
            model.setEnabled(BP_FilteredOutSubpanel.this, true);
        }
        else if (cache.getRejectCount() == 0) {
            model.setEnabled(BP_FilteredOutSubpanel.this, false);
        }
        else { }
    }

    /**
     * A special thread to repopulate the test lists.
     */
    private class TableSynchronizer extends Thread {
        TableSynchronizer() {
            super("filtered-out list synchronizer");
            setPriority(Thread.MIN_PRIORITY + 2);
        }

        public void run() {
            // grab cache lock first because many other threads may alter that
            // object, causing a deadlock here.
            // sync. to hold observer traffic until re-sync is done
            synchronized (cache) {
                synchronized(BP_FilteredOutSubpanel.this) {
                    // resync with this node cache
                    Vector[] newData = cache.addObserver(cacheWatcher, true);

                    // add tests into the list model - this doesn't make the data
                    // live though
                    for (int j = 0; j < newData[newData.length-1].size() - 1; j++) {
                        if (stopping)
                            break;

                        mod.addTest(newData[newData.length-1].elementAt(j), true);
                    }   // for

                    if (newData[newData.length-1].size() > 0 && !stopping) {
                        // final item with a notify
                        mod.addTest(newData[newData.length-1].lastElement(), false);
                    }

                    // to indicate completion
                    resyncThread = null;
                }   // this sync
            }   // cache sync

            validateEnableState();
        }   // run()

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

        infoTa = uif.createMessageArea("br.fo.info");
        infoTa.setOpaque(false);
        add(infoTa, gbc);

        gbc.gridy = 1;
        gbc.weightx = 2.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(uif.createScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), gbc);

        updateInfoText();

        InputListener tableListener = new InputListener();
        table.addMouseListener(tableListener);
        table.getSelectionModel().addListSelectionListener(tableListener);

        // to trigger test selection when enter is pressed
        table.getInputMap(JComponent.WHEN_FOCUSED).put(
                        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
                        "gotoTest");
        table.getActionMap().put("gotoTest",
                    new KbTableAction(uif.getI18NResourceBundle(),
                                    "br.list.enter"));

    String[] actions = { };
    popupTable = uif.createPopupMenu("br", actions, (ActionListener)tableListener);

    actions = new String[] { "action.cpnamelist", "action.cpnamestr" };
    popupTable.add(uif.createMenu("br.cp", actions, (ActionListener)tableListener));

    // this is necessary to make sure that the split pane can resize
    // this panel.  without setting the min., the panel seems to take
        // all it is given, and never gives it back.
        setMinimumSize(new Dimension(150,100));
    }

    /**
     * It is assumed that this will run on the event thread.
    private void setEmpty(boolean state) {
        if (state && list.getModel() != EmptyListModel.getInstance()) {
            list.setModel(EmptyListModel.getInstance());
            model.setEnabled(BP_TestListSubpanel.this, false);
            lastMsg = "";
        }
        else if (!state && list.getModel() == EmptyListModel.getInstance()) {
            list.setModel(mod);
            model.setEnabled(BP_TestListSubpanel.this, true);
        }
    }
     */

    // ------------- inner class -------------
    /**
     * Enumerates tree in background to populate the list.
     * If this thread is running, consider list data incomplete.
     * Swing cannot handle an updating model, so there is a two-stage absorbtion
     * of data.  This thread runs with no delay reading the TRT and placing that data
     * into an "offline" queue.  It periodically schedules an event on the GUI event
     * thread; when that thread run, it copies the data from the offline area to the
     * online area, which is what the ListModel presents.  This workaround assumes that
     * Swing will never dispatch more than one event at a time.
     */
    private class TestTableModel extends AbstractTableModel {
        TestTableModel(UIFactory uif) {
            super();

            colNames = new String[] {
                uif.getI18NString("br.fo.col0.txt"),
                uif.getI18NString("br.fo.col1.txt")
            };

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
                return colNames[columnIndex];
        }

        public Object getValueAt(int row, int column) {
            if (column == 0) {
                synchronized (liveData) {
                    return liveData.get(row);
                }
            }
            else if (column == 1) {
                synchronized (liveData) {
                    Object tst = (liveData.get(row));
                    Object r = null;

                    if (cache != null && r == null) {
                        r = cache.getRejectReason((TestResult)tst);
                    }

                    if (r == null)
                        r = uif.getI18NString("br.fo.noFi.txt");

                    return r;
                }
            }
            else
                throw new IndexOutOfBoundsException(
                    "Index into filtered out table is out of range: " +
                    row + ", " + column);
        }

        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false;
        }

        // ---------- Custom methods for this model ----------
        /**
         * @param suppressNotify Actively request that no update be scheduled.
         */
        void addTest(Object tr, boolean suppressNotify) {
            synchronized (vLock) {
                // make sure this item is not already in the list
                if (!inQueue.contains(tr)) {
                    inQueue.addElement(tr);
                }
            }   // sync

            // try not to saturate the GUI event thread
            if (!suppressNotify && !isUpdateScheduled) {
                TableNotifier tn = new TableNotifier(subpanelNode, this);
                pendingEvents.addElement(tn);
                EventQueue.invokeLater(tn);
            }
        }

        /**
         * Remove the given test from the list.
         * Ignored if the test is not in the list.
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
            }   // sync

        }

        void reset() {
            synchronized (vLock) {
                init();
            }

            // force GUI to update the now empty list
            notifyDone();
        }

        // ------------ private --------------

        private void init() {
            // discard all pending events
            // this is necessary to ensure that update events which haven't
            // been processed are not processed after the model has changed
            // arguably, this should be solved by putting this init() onto
            // the event thread
            synchronized (pendingEvents) {
                for (int i = 0; i < pendingEvents.size(); i++) {
                    TableNotifier tn = (TableNotifier)(pendingEvents.get(i));
                    tn.cancel();
                }   // for
            }

            inQueue = new Vector();
            rmQueue = new Vector();
            liveData = new LinkedList();

            isUpdateScheduled = false;
        }

        /**
         * Transfer data from the internal queue to the live data queue.
         * This is part of the Swing threading workaround.  This method immediately
         * exits if there is no work to do.  It also dispatches model update events
         * if necessary.
         * This method always runs on the event dispatch thread.
         */
        private void goLive() {
            int firstNew, lastNew = 0;
            if (debug)
                Debug.println("BP_TL.TLM - goLive() starting.");

            // this is sync. against the outer class because we may change the
            // list model object during execution of this block
            synchronized (BP_FilteredOutSubpanel.this) {
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
                                liveData.addAll(inQueue);
                                lastNew = liveData.size()-1;
                                inQueue.setSize(0);
                            }
                            else {          // only add some of the new items
                                    for (int i = 0; i < BATCH_SIZE; i++) {
                                        liveData.add(inQueue.remove(0));
                                    }   // for

                                // schedule a future update
                                if (!isUpdateScheduled) {
                                    TableNotifier tn = new TableNotifier(
                                                        subpanelNode, this);
                                    pendingEvents.addElement(tn);
                                    EventQueue.invokeLater(tn);
                                }

                                lastNew = liveData.size()-1;
                            }
                        }       // sync

                        // dispatch update range event to Swing
                        if (listenerList.getListenerCount() > 0) {
                            TableModelEvent e =
                                new TableModelEvent(this, firstNew, lastNew,
                                            TableModelEvent.ALL_COLUMNS,
                                            TableModelEvent.INSERT);
                            TableNotifier tn = new TableNotifier(e, mod);
                            pendingEvents.addElement(tn);
                            EventQueue.invokeLater(tn);
                        }
                    }

                    // enable this tab now that it has data
                    /*
                    if (liveData.size() > 0) {
                        // switch back from an empty list
                        setEmpty(false);
                    }
                    else {
                        setEmpty(true);
                    }
                    */

                    // this clears the "please wait" message if needed
                    if (table.getSelectedRow() == -1 && inQueue.size() == 0)
                        showMessage("");
                }   // sync
            }

            if (debug)
                Debug.println("BP_TL.LT - goLive() finished");
        }

        /**
         * Remove tests in the removal queue from the live data or the incoming data.
         * vLock should be locked when you call this method
         */
        private void processRemoveQueue() {
            if (rmQueue.size() == 0)
                return;

            while (rmQueue.size() > 0) {
                TestResult target = (TestResult)(rmQueue.remove(0));
                int targetIndex = liveData.indexOf(target);
                if (targetIndex != -1) {
                    synchronized (liveData) {
                        // necessary for proper synchronization
                        // should not be a problem really, based on how other
                        // locking is done, all work on liveData occurs in goLive()
                        targetIndex = liveData.indexOf(target);

                        // only should happen if the item disappears
                        if (targetIndex == -1)
                            continue;

                        liveData.remove(targetIndex);

                        // WARNING: since we are continually changing the contents of
                        // the data, you must notify the observers synchronously to get
                        // proper results
                        notifyRemoved(target, targetIndex);
                    }   // sync
                }
            }   // while
        }

        /**
         * Remove duplicates in the add queue.
         * vLock should be locked when you call this method
         */
        private void preprocessAddQueue() {
            // make sure this list does not contain dups
            for (int i = 0; i < inQueue.size(); i++) {
                if (liveData.contains(inQueue.elementAt(i))) {
                    inQueue.remove(i);
                    i--;
                }
                else {
                }
            }   // for
        }

        // --------- event utility methods -----------
        /**
         * Notify observers that the given index was added
         */
        private void notifyAdded(TestResult what, int index) {
            if (listenerList.getListenerCount() > 0) {
                // may want to buffer these messages for performance
                TableModelEvent e = new TableModelEvent (this, index, index,
                                            TableModelEvent.ALL_COLUMNS,
                                            TableModelEvent.INSERT);

                if (EventQueue.isDispatchThread()) {
                    // XXX try without this to see perf. impact
                    // dispatch synchronously
                    mod.fireTableChanged(e);
                }
                else {
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
                                        TableModelEvent.ALL_COLUMNS,
                                        TableModelEvent.DELETE);

                if (EventQueue.isDispatchThread()) {
                    // XXX try without this to see perf. impact
                    // dispatch synchronously
                    mod.fireTableChanged(e);
                }
                else {
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
        private final Object vLock = new Object();      // lock for inQueue & rmQueue
        private Vector inQueue;     // queue of items to be added to live data
        private Vector rmQueue;     // queue of items to be removed from live data
        private LinkedList liveData;    // to allow manual synchronization
        Vector pendingEvents = new Vector();

        volatile boolean isUpdateScheduled;  // are updates waiting in inQueue or rmQueue

        private static final int BATCH_SIZE = 100;
        private static final int COLUMN_COUNT = 2;
    }

    private class CacheObserver extends TT_NodeCache.TT_NodeCacheObserver {
        CacheObserver() {
            super();
            // configure our interest list
            interestList[MSGS_FILTERED] = true;
        }

        public void testAdded(int msgType, TestResultTable.TreeNode[] path,
                              TestResult what, int index) {
            synchronized(BP_FilteredOutSubpanel.this) {
                mod.addTest(what, false);
            }
        }

        public void testRemoved(int msgType, TestResultTable.TreeNode[] path,
                                TestResult what, int index) {
            synchronized(BP_FilteredOutSubpanel.this) {
                mod.removeTest(what);
            }
        }

        public void statsUpdated(int[] stats) {
            // ignore
        }
    }

    /**
     * This is a double duty class; it commits changes the model and also dispatches
     * given events.  If instance var. lt is null, it dispatches events, otherwise it
     * triggers a commit on the list thread data using (<tt>goLive()</tt>).
     * This class is critical because it is the task which gets scheduled on
     * the event thread.
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
            }
            else {
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
    }   // list notifier

    /**
     * One of these listeners is associated with each of the test lists.
     */
    class InputListener extends MouseAdapter implements ListSelectionListener, ActionListener {
        // ActionListener
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("action.cpnamelist") ||
                    e.getActionCommand().equals("action.cpnamestr")) {
                final int[] rows = table.getSelectedRows();

                if (rows.length > 0) {
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
            } // Copy
            table.repaint();

        } // action performed

        // Mouse Adapter
        public void mouseClicked(MouseEvent e) {
            if (e.getComponent() == table) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    popupTable.show(e.getComponent(), e.getX(), e.getY());
                } else {
            JTable tbl = (JTable)(e.getComponent());
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
            TestResult tr = (TestResult)(tm.getValueAt(row, 0));

            if (e.getClickCount() == 1) {
                // show vital stats only
                showMessage(I18NUtils.getStatusMessage(tr.getStatus()));
            }
            else if (e.getClickCount() == 2) {
                // construct the path required by the model
                TestResultTable.TreeNode[] path = TestResultTable.getObjectPath(tr);

                // sanity check, could happen in exceptional cases (out of memory)
                if (path == null || path.length == 0)
                    return;

                Object[] fp = new Object[path.length + 1];
                System.arraycopy(path, 0, fp, 0, path.length);
                fp[fp.length-1] = tr;

                model.showTest(tr, fp);
            }
                }
            }
        }

        // ListSelectionListener
        public void valueChanged(ListSelectionEvent e) {
            int index = e.getLastIndex();

            if (mod.getRowCount() == 0 || index >= mod.getRowCount()) {
                // swing seems to generate re-selection events when the
                // list model is 'replaced' with a new one (completely
                // invalidated).  for some reason it changes the selection
                // index without doing bounds checking
                return;     // ignore invalid event
            }

            if (index != lastIndex) {
                TestResult tr =
                    (TestResult)(mod.getValueAt(index, 0));

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

            TestResult tr = (TestResult)target;
            TestResultTable.TreeNode[] path = TestResultTable.getObjectPath(tr);

            // sanity check, could happen in exceptional cases (out of memory)
            if (path == null || path.length == 0)
                return;

            Object[] fp = new Object[path.length + 1];
            System.arraycopy(path, 0, fp, 0, path.length);
            fp[fp.length-1] = tr;

            model.showTest(tr, fp);
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

        public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row,
                        int column) {
            if (value == null)  // very strange...
                return this;

            if (value instanceof TestResult) {
                TestResult tr = (TestResult)value;
                setText(tr.getTestName());
                setToolTipText(I18NUtils.getStatusMessage(tr.getStatus()));

            } else if (value instanceof TestFilter) {
                TestFilter tf = (TestFilter)value;
                setText(tf.getReason());
                setToolTipText(tf.getDescription());
            }
            else {      // this will run for the reason column (1)
                setText(value.toString());
            }

            setBorder(spacerBorder);
            setFont(getFont().deriveFont(Font.PLAIN));

            if (isSelected) {
                setOpaque(true);
                //setBackground(MetalLookAndFeel.getTextHighlightColor());
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            else {
                //setForeground(MetalLookAndFeel.getPrimaryControlDarkShadow());
                setForeground(table.getForeground());
                setOpaque(false);
            }

            // would like to find a better way to do this
            // seems like we can't do this properly until the first item
            // is rendered though
            if (!rowHeightSet) {
                table.setRowHeight(getFontMetrics(getFont()).getHeight() +
                                   ROW_HEIGHT_PADDING);
                rowHeightSet = true;
            }

            return this;
        }

        // border to pad left and right
        private Border spacerBorder = BorderFactory.createEmptyBorder(3,3,3,3);
    }


    private JTable table;
    private TestTableModel mod;         // JTable model
    private TT_NodeCache cache;
    private TT_BasicNode lastNode;          // last node updated against
    private CacheObserver cacheWatcher;
    private volatile TableSynchronizer resyncThread;
    private TableCellRenderer renderer;
    private InputListener listener;
    private JTextArea infoTa;
    private boolean rowHeightSet;
    private static final int ROW_HEIGHT_PADDING = 3;

    private JPopupMenu popupTable;

    private boolean debug = Debug.getBoolean(BP_FilteredOutSubpanel.class);
}
