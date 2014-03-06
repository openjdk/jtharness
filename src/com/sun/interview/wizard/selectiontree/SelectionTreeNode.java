/*
 * $Id$
 *
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview.wizard.selectiontree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.sun.interview.wizard.selectiontree.selection.SelectionElement;
import com.sun.interview.wizard.selectiontree.selection.SelectionType;

/**
 * <code>SelectionTreeNode</code> class represents a tree node displayed in {@link SelectionTree}.
 */
public class SelectionTreeNode implements TreeNode {

    private List<SelectionTreeNode> children;
    private SelectionTreeNode parent;
    private SelectionElement element;
    private SelectionTree tree = null;
    private SelectionTreeNode nextNode;
    private SelectionTreeNode previousNode;

    /**
     * Answers new selection-tree node.<P>
     * This constructor must be always overridden in subclasses.
     *
     * @param tree owner {@link SelectionTree}
     * @param filter {@link SelectionTreeFilter} to filter children
     * @param element selection element represented by the node
     */
    public SelectionTreeNode(SelectionTree tree, SelectionTreeFilter filter, SelectionElement element, SelectionTreeNode previousNode) {
        this(tree, element, filter, null, previousNode);
    }

    /**
     * Answers new selection-tree node.<P>
     * This constructor must be always overridden in subclasses.
     *
     * @param tree owner {@link SelectionTree}
     * @param element selection element represented by the node
     * @param filter {@link SelectionTreeFilter} to filter children
     * @param parent parent node
     */
    public SelectionTreeNode(SelectionTree tree, SelectionElement element, SelectionTreeFilter filter, SelectionTreeNode parent, SelectionTreeNode previousNode) {
        super();

        this.parent = parent;
        this.element = element;
        this.tree = tree;
        this.previousNode = previousNode;
        if (previousNode != null) {
            previousNode.nextNode = this;
        }

        createChildren(filter);
    }

    private void createChildren(SelectionTreeFilter filter) {
        List<SelectionTreeNode> children = new ArrayList<SelectionTreeNode>();

        SelectionTreeNode prevNode = this;

        if (element.getChildren() != null) {
            for (ListIterator<SelectionElement> iter = element.getChildren().listIterator(); iter.hasNext();) {
                SelectionElement child = iter.next();
                if (filter == null || filter.accept(child)) {
                    SelectionTreeNode node = new SelectionTreeNode(tree, child, filter, this, prevNode);
                    prevNode = node;
                    int childCount;
                    while ((childCount = prevNode.getChildCount()) > 0) {
                        prevNode = (SelectionTreeNode) prevNode.getChildAt(childCount - 1);
                    }
                    children.add(node);
                }
            }
        }

        this.children = children;
    }

    public int getChildCount() {
        return children.size();
    }

    public boolean getAllowsChildren() {
        return !children.isEmpty();
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public Enumeration<SelectionTreeNode> children() {
        return Collections.enumeration(children);
    }

    public SelectionTreeNode getParent() {
        return parent;
    }

    public SelectionTreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    /**
     * Changes this node selection to the corresponding alternate selection type.
     */
    protected void changeSelection() {
        SelectionType newType;
        if (getSelectionType().equals(SelectionType.PARTIALLY_SELECTED) || getSelectionType().equals(SelectionType.SELECTED)) {
            newType = SelectionType.UNSELECTED;
        } else if (getSelectionType().equals(SelectionType.UNSELECTED)) {
            newType = SelectionType.SELECTED;
        } else {
            return;
        }

        changeSelection(newType);
    }

    /**
     * Answers the next node in node sequence ordered by tree row.
     */
    public SelectionTreeNode getNextNode() {
        return nextNode;
    }

    /**
     * Answers the previous node in node sequence ordered by tree row.
     */
    public SelectionTreeNode getPreviousNode() {
        return previousNode;
    }

    /**
     * Changes this node selection to the given selection type.
     */
    protected void changeSelection(SelectionType newType) {
        tree.fireSelectionChangeStarted(this);
        if (setSelectionType(newType)) {
            if (tree.isParentDependsOnChildren()) {
                updateChildren();

                if (parent != null) {
                    parent.updateSelection();
                }
            }
        }
        tree.fireSelectionChangeFinished(this);
    }

    /**
     * Update selection of the node.
     */
    protected void updateSelection() {
        updateSelection(false);
    }

    /**
     * Update selection of the node and update selections of the parent nodes if selection is changed or parameter is true.
     */
    protected void updateSelection(boolean forceParentUpdate) {
        boolean selected = false;
        boolean unselected = false;

        for (Iterator<SelectionTreeNode> it = children.iterator(); it.hasNext();) {
            SelectionType selectionType = it.next().getSelectionType();
            switch (selectionType) {
                case UNSELECTED:
                    unselected = true;
                    break;
                case SELECTED:
                    selected = true;
                    break;
                case PARTIALLY_SELECTED:
                    selected = true;
                    unselected = true;
                    break;
            }
        }

        boolean changed = true;
        if (selected && !unselected) {
            changed = setSelectionType(SelectionType.SELECTED);
        } else if (unselected && !selected) {
            changed = setSelectionType(SelectionType.UNSELECTED);
        } else if (selected && unselected) {
            changed = setSelectionType(SelectionType.PARTIALLY_SELECTED);
        }

        if (parent != null && (changed || forceParentUpdate)) {
            parent.updateSelection(forceParentUpdate);
        }
    }

    private void updateChildren() {
        SelectionType selectionType = element.getSelectionType();
        if (SelectionType.SELECTED.equals(selectionType) || SelectionType.UNSELECTED.equals(selectionType)) {
            for (Iterator<SelectionTreeNode> it = children.iterator(); it.hasNext();) {
                SelectionTreeNode child = it.next();
                if (child.setSelectionType(selectionType)) {
                    child.updateChildren();
                }
            }
        }
    }

    /**
     * Answers the node selection type.
     */
    protected SelectionType getSelectionType() {
        return element.getSelectionType();
    }

    /**
     * Sets the node selection type and answers whether the selection type has been really changed.
     */
    private boolean setSelectionType(SelectionType selectionType) {
        SelectionType oldType = getSelectionType();
        element.setSelectionType(selectionType);

        if (!selectionType.equals(oldType)) {
            updateNode();
            tree.fireSelectionTypeChanged(this, oldType, selectionType);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Update view of node.
     */
    public void updateNode() {
        ((DefaultTreeModel) tree.getModel()).nodeChanged(this);
    }

    public String toString() {
        return element.getDisplayableName();
    }

    /**
     * Answers the stored selection element.
     */
    public SelectionElement getSelectionElement() {
        return element;
    }

    private SelectionTreeNode[] getPathToRoot(SelectionTreeNode aNode, int depth) {
        SelectionTreeNode[] result;

        if (aNode == null) {
            if (depth == 0) {
                return null;
            } else {
                result = new SelectionTreeNode[depth];
            }
        } else {
            depth++;
            result = getPathToRoot((SelectionTreeNode) aNode.getParent(), depth);
            result[result.length - depth] = aNode;
        }

        return result;
    }

    /**
     * Answers tree-path of the node.
     */
    public TreePath getPath() {
        return new TreePath(getPathToRoot(this, 0));
    }

    /**
     * Answers the owner selection-tree
     */
    public SelectionTree getTree() {
        return tree;
    }
}
