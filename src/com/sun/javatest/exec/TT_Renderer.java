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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.sun.javatest.JavaTestError;
import com.sun.javatest.Parameters;
import com.sun.javatest.Status;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.tool.IconFactory;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.Debug;

/**
 * "Basic" renderer for the test manager (exec tool) tree.
 */
class TT_Renderer extends DefaultTreeCellRenderer {
    /**
     * @param fh The filter configuration object.
     *        This information is needed to make it easy to retrieve the
     *        current filter for rendering purposes.
     */
    TT_Renderer(UIFactory uif, FilterSelectionHandler fh, TreePanelModel model) {
        this.uif = uif;
        this.filterHandler = fh;
        this.tpm = model;
        //loadIcons();

        // this is supposed to be caught during development
        if (stateOrdering.length != Status.NUM_STATES)
            throw new JavaTestError(uif.getI18NString("tree.unmatched"));
    }

    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded,
                                           leaf, row, hasFocus);

        setIcon(getIcon(value, (TestTreeModel)(tree.getModel())));
        setText(getLabelText(value, (TestTreeModel)tree.getModel()));
        setToolTipText(getTipText(value));

        return this;
    }

    void setParameters(Parameters p) {
        params = p;
    }

    /**
     * Load icon resources.  This may become a complex operation based on user prefs and
     * other factors.
     */
    /*
    private void loadIcons() {
        // if the runIcon is there, we assume that icons have already be loaded
        if (unknownIcon == null) {
            unknownIcon = uif.createIcon("exec.tree.unknown");
            leafIcons = new Icon[numLeafIcons];
            leafIcons[Status.PASSED] = uif.createIcon("exec.tree.pass");
            leafIcons[Status.FAILED] = uif.createIcon("exec.tree.fail");
            leafIcons[Status.ERROR] = uif.createIcon("exec.tree.error");
            leafIcons[Status.NOT_RUN] = uif.createIcon("exec.tree.notRun");
            leafIcons[numLeafIcons-1] = uif.createIcon("exec.tree.disable");


            leafRunIcons = new Icon[numLeafIcons];
            leafRunIcons[Status.PASSED] = uif.createIcon("exec.tree.runPass");
            leafRunIcons[Status.FAILED] = uif.createIcon("exec.tree.runFail");
            leafRunIcons[Status.ERROR] = uif.createIcon("exec.tree.runError");
            leafRunIcons[Status.NOT_RUN] = uif.createIcon("exec.tree.runNotRun");
            leafRunIcons[numLeafIcons-1] = uif.createIcon("exec.tree.runDisable");

            brIcons = new Icon[numBrIcons];
            brIcons[Status.PASSED] = uif.createIcon("exec.tree.passf");
            brIcons[Status.FAILED] = uif.createIcon("exec.tree.failf");
            brIcons[Status.ERROR] = uif.createIcon("exec.tree.errorf");
            brIcons[Status.NOT_RUN] = uif.createIcon("exec.tree.notRunf");
            brIcons[numBrIcons-1] = uif.createIcon("exec.tree.disabledf");

            brComputeIcons = new Icon[numBrIcons];
            brComputeIcons[Status.PASSED] = uif.createIcon("exec.tree.computePassf");
            brComputeIcons[Status.FAILED] = uif.createIcon("exec.tree.computeFailf");
            brComputeIcons[Status.ERROR] = uif.createIcon("exec.tree.computeErrorf");
            brComputeIcons[Status.NOT_RUN] = uif.createIcon("exec.tree.computeNotRunf");
            brComputeIcons[numBrIcons-1] = uif.createIcon("exec.tree.computeDisabledf");
        }
    }
    */

    private Icon getIcon(Object value, TestTreeModel model) {
        // XXX may want to cache the filters - performance
        TestFilter filter = filterHandler.getActiveFilter();

        if (params == null || params.getTestSuite() == null ||
                model instanceof EmptyTestTreeModel)
            return IconFactory.getTestFolderIcon(Status.NOT_RUN, false, true);

        if (value instanceof TT_TestNode) {
            TestResult tr = ((TT_TestNode)value).getTestResult();
            /*
            if (isFilteredOut(tr, filter))          // not selected for execution
                return leafIcons[numLeafIcons-1];
            else if (tr.isMutable())        // test is running, may want to change criteria
                return leafRunIcons[tr.getStatus().getType()];
            else                                    // one of the normal status
                return leafIcons[tr.getStatus().getType()];
            */

            if (isFilteredOut(tr, filter))          // not selected for execution
                return IconFactory.getTestIcon(IconFactory.FILTERED_OUT, false, true);
            else if (isRunning(tr))
                return IconFactory.getTestIcon(tr.getStatus().getType(), true, true);
            else
                return IconFactory.getTestIcon(tr.getStatus().getType(), false, true);
        }
        else if (value instanceof TT_BasicNode) {
            TT_BasicNode tn = (TT_BasicNode)value;
            TT_NodeCache info = model.getNodeInfo(tn.getTableNode(), false);
            int[] stats = info.getStats();

            // XXX should probably investigate why this can happen
            if (stats == null)
                return unknownIcon;

            /*
            if (!info.isValid() || info.isActive() ||   // being processed
                (info.isValid() && !info.isComplete())) {   // to be processed
                return brComputeIcons[selectBranchIconIndex(stats, true)];
            }
            else {
                return brIcons[selectBranchIconIndex(stats, false)];
            }*/
            boolean active = ( !info.isValid()
                               || info.isActive()       // being processed
                               || (info.isValid() && !info.isComplete()) );   // to be processed

            active = active || tpm.isActive(tn);
            return IconFactory.getTestFolderIcon(
                    selectBranchIconIndex(stats, active),
                    active, true);
        }
        else
            return IconFactory.getFolderIcon();
    }

    private String getLabelText(Object value, TestTreeModel model) {
        if (value instanceof TT_TestNode) {
            return ((TT_TestNode)value).getDisplayName();
        } else if (value instanceof TT_BasicNode) {
            TT_BasicNode tn = (TT_BasicNode)value;
            if (tn.getParent() == null)
                return uif.getI18NString("tree.rootName");
            else
                return tn.getDisplayName();
        }
        else if (model instanceof EmptyTestTreeModel)
            if (params == null || params.getTestSuite() == null)
                return uif.getI18NString("tree.noTs");
            else
                return uif.getI18NString("tree.rootName");
        else
            return null;
    }

    private String getTipText(Object val) {
        if (val instanceof TestResult) {
            //return ((TestResult)val).getWorkRelativePath();
            return ((TestResult)val).getTestName();
        }
        else if (val instanceof TestResultTable.TreeNode) {
            if (params == null || params.getTestSuite() == null)
                return uif.getI18NString("tree.noRootName.tip");

            TestResultTable.TreeNode node = (TestResultTable.TreeNode)val;
            if (node.isRoot())
                return uif.getI18NString("tree.rootName.tip", params.getTestSuite().getName());
            else
                return TestResultTable.getRootRelativePath(node);
        }
        else
            return null;
    }

    private boolean isFilteredOut(TestResult tr, TestFilter filter) {
        if (filter == null)
            return false;

        if (debug > 1)
            Debug.println("TT - Checking filter for: " + tr.getTestName() + " (TR status=" +
                                tr.getStatus().getType() + ")");

        TestDescription td;

        try {
            td = tr.getDescription();
        }
        catch (TestResult.Fault f) {
            if (debug > 0)
                f.printStackTrace(Debug.getWriter());

            return false;
        }

        try {
            if (!filter.accepts(td)) {
                if (debug > 1) {
                    Debug.println("TT - Filter " + filter + " rejected: " +
                                  tr.getWorkRelativePath());
                }

                // rejected
                return true;
            }
        }
        catch (TestFilter.Fault f) {
            if (debug > 0)
                f.printStackTrace(Debug.getWriter());

            // just fall through to accept
        }       // catch

        // accepted
        return false;
    }

    private boolean isRunning(TestResult tr) {
        if (tpm.isActive(tr))
            return true;
        else
            return false;
    }

    /**
     * @param loading If true, the result will be appropriate for indicating that
     *        a folder is still in flux.  False, indicates that normal evaluation
     *        rules should apply.
     */
    private static int selectBranchIconIndex(int[] stats, boolean loading) {
        /*
        System.out.println(tn.getName() + " done " +
        info.getStats()[0] + "  " + info.getStats()[1] + "  " + info.getStats()[2] + "  " +
        info.getStats()[3]);
         */

        // in effect this selects in this order:
        // 1) error (blue)
        // 2) fail (red)
        // 3) not run (white)
        // 4) pass (green)
        // 5) not runnable (grey)

        for (int i = 0; i != stateOrdering.length; i++) {
            if (stats[stateOrdering[i]] > 0) {
                return stateOrdering[i];
            }
        }   // for


        // must be a filtered-out node
        if (!loading)
            //return numBrIcons-1;
            return IconFactory.FILTERED_OUT;
        else
            return Status.NOT_RUN;
    }

    private Parameters params;
    private FilterSelectionHandler filterHandler;
    private TreePanelModel tpm;
    private UIFactory uif;
    private static Icon unknownIcon;
    /*
    protected static Icon[] leafIcons;
    protected static Icon[] leafRunIcons;
    protected static Icon[] brIcons;
    protected static Icon[] brRunIcons;
    protected static Icon[] brComputeIcons;
    protected static final int numLeafIcons = Status.NUM_STATES + 1;
    protected static final int numBrIcons = Status.NUM_STATES + 1;
    */

    // state ordering should be of length Status.NUM_STATES
    private static final int[] stateOrdering = {Status.ERROR, Status.FAILED, Status.NOT_RUN, Status.PASSED};

    private static final int debug = Debug.getInt(TT_Renderer.class);
}

