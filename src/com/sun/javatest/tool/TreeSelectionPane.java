/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool;

import com.sun.javatest.tool.selectiontree.SelectionTree;
import com.sun.javatest.tool.selectiontree.selection.SelectionElement;
import com.sun.javatest.tool.selectiontree.selection.SelectionType;
import java.awt.BorderLayout;
import java.util.*;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import com.sun.javatest.util.I18NResourceBundle;

/**
 * A component to allow selection of nodes in a tree.
 */
public class TreeSelectionPane extends JComponent implements Accessible
{
    /**
     * The model for the tree whose nodes can be selected in a TreeSelectionPane.
     */
    public static interface Model
    {
        /**
         * Get the root node of the tree.
         * @return the root node of the tree
         */
        Object getRoot();

        /**
         * Get the number of children for a node.
         * @param node the node for which the number of children is required
         * @return the number of children of the specified node
         */
        int getChildCount(Object node);

        /**
         * Get a specific child of a node.
         * @param node the node for which the child is required
         * @param index the index of the desired child; this should be
         * in the range [0..getChildCount())
         * @return the specified child node
         */
        Object getChild(Object node, int index);

        /**
         * Get the name of a node.
         * The name should identify the child within the set of its parent's children.
         * @param node the node for which the name is required
         * @return the name of the node
         */
        String getName(Object node);

        /**
         * Get the path of a node.
         * The path is a series of names, separated by '/', which identify
         * successive children, initially relative to the root node.
         * @param node the node for which the path is required
         * @return the path of the node
         */
        String getPath(Object node);

        /**
         * Check whether or not a node is a leaf node.
         * This is simply a semantic distinction for a node, that will be
         * used to determine how the node should be displayed; it is independent
         * of whether a node has any children or not.
         * @param node the node to be checked
         * @return true if the node is a leaf node, and false otherwise
         */
        boolean isLeaf(Object node);
    };

    /**
     * Create a TreeSelectionPane, using a specified tree model.
     * @param model the model for the tree from which nodes may be selected
     */
    TreeSelectionPane(Model model) {
        uif = new UIFactory(getClass(), null);

        setLayout(new BorderLayout());
        SelectNode rootNode = new SelectNode(model, model.getRoot());
        tree = new SelectionTree(rootNode, null, true);
        tree.setName("tsp.tree");
        AccessibleContext ac = tree.getAccessibleContext();
        ac.setAccessibleName(i18n.getString("tsp.tree.name"));
        ac.setAccessibleDescription(i18n.getString("tsp.tree.desc"));

        JScrollPane sp= uif.createScrollPane(tree);
        add(sp);
    }
    protected class SelectNode implements SelectionElement {
        private SelectionType type = SelectionType.UNSELECTED;
        private Object object;
        private LinkedList<SelectionElement> children;
        private Model model;
        private String name;

        public SelectNode(Model model, Object object) {
            this.model = model;
            this.object = object;
        }

        public SelectionType getSelectionType() {
            return type;
        }

        public void setSelectionType(SelectionType selectionType) {
            if (!type.equals(selectionType)) {
                    type = selectionType;
            }
        }

        public String getDisplayableName() {
            return getName();
        }

        public String getToolTip() {
            return null;
        }

        public boolean isToolTipAlwaysShown() {
            return false;
        }

        public List<SelectionElement> getChildren() {
            if (children == null) {
                initChildren();
            }
            return children;
        }

        private void initChildren() {
            int childCount = model.getChildCount(object);
            children = new LinkedList<SelectionElement>();
            for (int i = 0; i < childCount; i++) {
                SelectNode newChild = new SelectNode(model, model.getChild(object, i));
                children.add(newChild);
                if (type.equals(SelectionType.SELECTED)) {
                    newChild.setSelectionType(SelectionType.SELECTED);
                }
            }
            model.getChild(object, 0);
        }

        private String getName() {
            if (name == null)
                name = model.getName(object);
            return name;
        }
    }

    /**
     * Get the accessible context for this pane.
     * @return the accessible context for this pane
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null)
            accessibleContext = new AccessibleJComponent() { };
        return accessibleContext;
    }

    /**
     * Get the current selection, represented as a set of paths to the
     * selected nodes.
     * @return the current selection, represented as a set of paths to the
     * selected nodes
     * @see #setSelection
     */
    public String[] getSelection() {
        return tree.getSelection();
    }

    /**
     * Set the current selection, by means of a set of paths to the
     * nodes to be selected.
     * @param paths a set of paths to the nodes to be selected
     * @see #getSelection
     * @see #clear
     */
    public void setSelection(String[] paths) {
        tree.setSelection(paths);
    }

    /**
     * Check if the selection is empty.
     * @return true if the selection is empty
     */
    public boolean isSelectionEmpty() {
        return tree.isSelectionEmpty();
    }

    /**
     * Clear the current selection.
     * @see #getSelection
     * @see #setSelection
     */
    public void clear() {
        tree.setSelection(null);
    }

    public void setEnabled(boolean b) {
        super.setEnabled(b);
        // propogate enabled-ness onto tree
        tree.setEnabled(b);
    }

    private AccessibleContext accessibleContext;
    private SelectionTree tree;
    private static I18NResourceBundle i18n =
        I18NResourceBundle.getBundleForClass(TreeSelectionPane.class);

    private UIFactory uif;
}
