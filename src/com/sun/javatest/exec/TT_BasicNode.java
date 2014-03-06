/*
 * $Id$
 *
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.TRT_TreeNode;
import com.sun.javatest.util.Debug;

import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeNode;

/**
 *
 * Representation of a node in the GUI tree representing the visible test structure.
 */
public class TT_BasicNode extends TT_TreeNode {

    TT_BasicNode(TT_BasicNode parent, TRT_TreeNode tn, Comparator comp) {
        this.comp = comp;
        this.parent = parent;
        this.tn = tn;
    }
    // ------- interface methods --------
    public Enumeration children() {
        updateNode();

        ArrayList<TreeNode> copy = null;
        synchronized (children) {
            // shallow copy child list?
            copy = (ArrayList<TreeNode>) children.clone();
        }

        final Iterator it = copy.iterator();
        return new Enumeration() {

            public boolean hasMoreElements() {
                return it.hasNext();
            }

            public Object nextElement() {
                return it.next();
            }
        };
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public TreeNode getChildAt(int arg0) {
        if (children == null) {
            return null;
        }
        updateNode();
        synchronized (children) {
            return children.get(arg0);
        }
    }

    public int getChildCount() {
        updateNode();

        synchronized (children) {
            return children.size();
        }
    }

    public int getIndex(TreeNode arg0) {
        if (children == null) {
            return -1;
        }
        updateNode();
        synchronized (children) {
            return children.indexOf(arg0);
        }
    }

    public int getIndex(TT_TestNode arg) {
        updateNode();
        synchronized (children) {
            return children.indexOf(arg);
        }
    }

    public TreeNode getParent() {
        return parent;
    }

    public boolean isLeaf() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // --------- basic interface -------------
    boolean isRoot() {
        return (parent == null);
    }

    /**
     * What should the name of this node be when shown in a user interface?
     * May or may not be the same as the short name.
     * @see #getShortName()
     * @return Shortest possible name of this node when displayed.
     */
    String getDisplayName() {
        // could allow override of this
        // maybe translate underscores to spaces?
        return getShortName();
    }

    String getLongDescription() {
        // should be the long path to the folder, or custom
        return null;
    }

    /**
     * String for use whenever you need a basic name for this node.  You can
     * assume that this name is unique within any node.
     * @return Short name for this node, containing no forward slashes or
     *    spaces.
     */
    String getShortName() {
        return tn.getName();
    }

    /**
     * Get the long internal representation of this location.
     * @return Null if the node is the root, else a forward slash separated
     *      path.
     */
    String getLongPath() {
        if (parent == null) // root
        {
            return null;
        }
        StringBuffer sb = new StringBuffer(getShortName());
        TT_BasicNode spot = parent;
        while (spot.parent != null) {
            sb.insert(0, "/");
            sb.insert(0, spot.getShortName());
            spot = spot.parent;
        }
        return sb.toString();
    }

    TT_TreeNode findByName(String name) {
        updateNode();

        synchronized (children) {
            if (children.size() == 0) {
                return null;
            }
            for (TT_TreeNode node : children) {
                if (name.equals(node.getShortName())) {
                    return node;
                }
            }   // for
        }

        return null;
    }

    TT_TestNode findByName(TestResult tr) {
        updateNode();

        synchronized (children) {
            if (children.size() == 0) {
                return null;
            }
        }

        String name = tr.getTestName();
        synchronized (children) {
            for (TT_TreeNode node : children) {
                if (name.equals(node.getLongPath())) {
                    return (TT_TestNode) node;
                }
            }   // for
        }

        return null;
    }

    TRT_TreeNode getTableNode() {
        return tn;
    }

    TreeModelEvent removeTest(TestResult tr) {
        if (updateNode()) {
            return null;
        }

        TT_TestNode tn = findByName(tr);
        if (tn == null)
            return null;

        synchronized (children) {
            int index = children.indexOf(tn);
            children.remove(tn);
            return new TreeModelEvent(
                this, getNodePath(), new int[]{index}, new Object[]{tn});
            //return index;
        }
    }

    int removeNode(TT_TreeNode node) {
        updateNode();

        synchronized (children) {
            int index = children.indexOf(node);
            if (index >= 0) {
                children.remove(index);
                return index;
            }
        }
        return -1;
    }

    TreeModelEvent replaceTest(TestResult tr, boolean insert) {
       if (updateNode()) {
           return null;
       }

       synchronized (children) {
           TT_TestNode tn = findByName(tr);
           if (tn != null) {
               int pos = getIndex(tn);
               children.remove(pos);
               children.add(pos, new TT_TestNode(this, tr));
               return new TreeModelEvent(this, getNodePath());
           }
           else {
                if (insert) {
                    return addTest(new TT_TestNode(this, tr), comp);
                }
                else
                    return null;
           }
       }
    }

    TreeModelEvent addTest(final TT_TestNode tn, Comparator sortComparator) {
        if (updateNode()) {
            //Debug.println("Ignoring add of " + tn.getDisplayName());
            return null;
        }

//        if (sortComparator == null)
//            synchronized (children) {
//                children.add(0, tn);
//                return 0;
//            }
//
        synchronized (children) {
            int result = -1;
            if (children.size() == 0) {
                children.add(tn);
                result = 0;
            } else if (sortComparator == null) {
                // consider inserting it at the end instead?
                children.add(0, tn);
                result = 0;
            } else {
                result = recursiveIns(0, children.size()-1, tn, tn.getDisplayName(),
                        sortComparator);
            }

            return new TreeModelEvent(
                    this, getNodePath(), new int[]{result}, new Object[]{tn});
        }
    }

    // --------- internal methods ------------
    private int recursiveIns(final int lPos, final int rPos,
                final TT_TreeNode tn,
                final String dispName,
                final Comparator sortComparator) {
        synchronized (children) {   // should already be locked!
            int diff = rPos-lPos;
            int pos = (diff/2) + lPos;
            String posStr = children.get(pos).getDisplayName();
            int res = sortComparator.compare(dispName, posStr);
            if (res == 0) {
                children.set(pos, tn);
                if (debug) {
                    Debug.println("Duplicate test, replaced - " + tn.getDisplayName());
                    //Exception e = new Exception(); e.printStackTrace(System.err);
                }
                return pos;
            }

            if (diff <= 0)
                if (res < 0) {
                    children.add(lPos, tn);
                    return lPos;
                } else if (res > 0) {
                    children.add(lPos+1, tn);
                    return lPos+1;
                }

            if (res < 0) {
                return recursiveIns(lPos, pos-1, tn, dispName, sortComparator);
            }
            else {
                return recursiveIns(pos+1, rPos, tn, dispName, sortComparator);
            }
        }
    }

    private boolean updateNode() {
        // is update needed?
        synchronized (children) {
            if (isUpdated.compareAndSet(false, true)) {
                updateNode0();
                return true;
            }
            else
                return false;
        }  // sync
    }

    /**
     * If update needed, this method does it by getting info from TRT.
     */
    private void updateNode0() {
        TestResultTable.TreeNode[] nodes = tn.getTreeNodes();
        addNodes(nodes);

        TestResult[] tests = tn.getTestResults();
        addTests(tests);
    }

    int[] addNodes(TestResultTable.TreeNode[] nodes) {
        if (nodes == null || nodes.length == 0) {
            return null;
        }

        updateNode();

        int[] newPositions = new int[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            TRT_TreeNode tnode = (TRT_TreeNode)nodes[i];

            synchronized (children) {
                newPositions[i] = findDuplicateNode(tnode);
                if (newPositions[i] >= 0) {
                    continue;                // add it
                }
                TT_BasicNode newNode = new TT_BasicNode(this, tnode, comp);
                if (children.size() == 0) {
                    children.add(newNode);
                    newPositions[i] = 0;
                }
                else if (comp == null) {
                    children.add(newNode);
                    newPositions[i] = children.size()-1;
                }
                else {
                    newPositions[i] = recursiveIns(0, children.size()-1, newNode,
                        newNode.getDisplayName(), comp);
                }
            }
        }   // for

        return newPositions;
    }

    private void addTests(TestResult[] tests) {
        if (tests == null || tests.length == 0) {
            return;
        }
        for (int i = 0; i < tests.length; i++) {
            TT_TestNode tn = new TT_TestNode(this, tests[i]);

            synchronized (children) {
                if (children.size() == 0 || comp == null) {
                    children.add(tn);
                } else {
                    int result = recursiveIns(0, children.size()-1, tn,
                        tn.getDisplayName(), comp);
                }
            }   // sync
        }
    }

    /**
     * Assumes that children is already locked (synchronized on).
     * @param node The node to look for.
     * @return Position of duplicate, -1 if not found.
     */
    private int findDuplicateNode(TRT_TreeNode node) {
        Iterator<TT_TreeNode> it = children.iterator();
        while (it.hasNext()) {
            TreeNode tn = it.next();
            if (tn instanceof TT_BasicNode) {
                if (((TT_BasicNode) tn).tn == node) {
                    return children.indexOf(tn);
                }
            }
        // skipping TT_TestNodes
        }

        return -1;  // no dup
    }

    private int insertNewBranch(TT_BasicNode tn) {
        // need to sort
        synchronized (children) {
            if (children.size() == 0) {
                children.add(tn);
                return 0;
            }
            else if (comp == null) {
                children.add(tn);
                return children.size()-1;
            } else {
                int result = recursiveIns(0, children.size()-1, tn,
                        tn.getDisplayName(), comp);
    //                Iterator<TT_TreeNode> it = children.iterator();
    //                while (it.hasNext())
    //                    System.out.println("  " + it.next().getDisplayName());

                return result;
            }
        }
    }

    private final ArrayList<TT_TreeNode> children = new ArrayList<TT_TreeNode>();
    private TRT_TreeNode tn;
    private Comparator comp;
    private final AtomicBoolean isUpdated = new AtomicBoolean(false);
    private boolean debug = Debug.getBoolean(TT_BasicNode.class);
}
