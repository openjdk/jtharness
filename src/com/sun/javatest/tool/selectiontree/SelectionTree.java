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
package com.sun.javatest.tool.selectiontree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.sun.javatest.tool.selectiontree.selection.SelectionElement;
import com.sun.javatest.tool.selectiontree.selection.SelectionType;
import com.sun.javatest.tool.selectiontree.selection.StyledSelectionElement;
import com.sun.javatest.util.I18NResourceBundle;
import javax.swing.Action;

/**
 * <code>SelectionTree</code> class provide tree functionality with check-boxes as nodes for comfort selection.
 * Business-objects stored in the tree must implement {@link SelectionElement} interface.
 */
public class SelectionTree extends JTree {

    private static final int TOOLTIP_UI_TEXT_GAP = 3; // this is hardcoded constant in ToolTipUI subclasses
    private static final I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(SelectionTree.class);
    private static final Icon selectedIcon = getIcon("SelectAll.icon");
    private static final Icon unselectedIcon = getIcon("DeselectAll.icon");
    private static final Icon partiallySelectedIcon = getIcon("partial.icon");
    private static final int checkSize = selectedIcon.getIconWidth();
    private static final int checkBorderSize = 1;
    private final Action selectAllAction = createAction("SelectAll");
    private final Action deselectAllAction = createAction("DeselectAll");
    private final Action expandAllAction = createAction("ExpandAll");
    private final Action collapseAllAction = createAction("CollapseAll");
    private List<SelectionTreeListener> listeners = new ArrayList<SelectionTreeListener>();
    private SelectionTreeFilter filter = null;

    /**
     * Answers selection tree with given {@link SelectionElement} object as its root.
     *
     * @param rootObject {@link SelectionElement} root object
     */
    public SelectionTree(SelectionElement rootObject) {
        this(rootObject, null, true);
    }

    /**
     * Answers selection tree with given {@link SelectionElement} object as its root,
     * which contains objects accepted by specified filter only in case of the filter is not null.
     *
     * @param rootObject {@link SelectionElement} root object
     * @param filter     {@link SelectionTreeFilter} to filter objects in the tree
     */
    public SelectionTree(SelectionElement rootObject, SelectionTreeFilter filter) {
        this(rootObject, filter, true);
    }

    /**
     * Answers selection tree with given {@link SelectionElement} object as its root, which has root node shown or not.
     *
     * @param rootObject {@link SelectionElement} root object
     * @param rootIsVisible true if root node is be visible
     */
    public SelectionTree(SelectionElement rootObject, boolean rootIsVisible) {
        this(rootObject, null, rootIsVisible);
    }

    /**
     * Answers selection tree with given {@link SelectionElement} object as its root, which has root node shown or not.
     *
     * @param rootObject {@link SelectionElement} root object
     * @param filter {@link SelectionTreeFilter} to filter objects in the tree
     * @param rootIsVisible true if root node should be visible
     */
    public SelectionTree(SelectionElement rootObject, SelectionTreeFilter filter, boolean rootIsVisible) {
        this.filter = filter;

        setModel(new DefaultTreeModel(new SelectionTreeNode(this, filter, rootObject, null)));

        setRootVisible(rootIsVisible);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        setCellRenderer(new SelectionTreeCellRenderer());
        addMouseListener(new SelectionTreeMouseListener());
        addKeyListener(new SelectionTreeKeyListener());
        setLargeModel(true);
        ToolTipManager.sharedInstance().registerComponent(this);

        Action popupMenuAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isEnabled()) {
                    return;
                }

                TreePath treePath = SelectionTree.this.getSelectionPath();

                if (treePath != null) {
                    Rectangle nodeRect = getPathBounds(treePath);

                    if (nodeRect != null) {
                        showPopupMenu(nodeRect.x, nodeRect.y + nodeRect.height);
                    }
                }
            }
        };

        addActionAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), popupMenuAction);
        addActionAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK), popupMenuAction);
    }

    private static Icon getIcon(String iconResource) {
        try {
            return new ImageIcon(
                    ImageIO.read(
                    com.sun.javatest.tool.Main.class.getResourceAsStream(i18n.getString(iconResource))));
        } catch (IOException e) {
            return null;
        }
    }

    private Action createAction(String actionID) {
        return createAction(actionID, i18n, null);
    }

    protected Action createAction(String actionID, I18NResourceBundle i18n, ActionListener actionListener) {
        SelectionTreeAction action = new SelectionTreeAction(actionListener);

        action.putValue(Action.NAME, i18n.getString(actionID + ".name"));

        try {
            String mnemonic = i18n.getString(actionID + ".mnemonic");
            Field field = KeyEvent.class.getField(mnemonic);
            action.putValue(Action.MNEMONIC_KEY, new Integer(field.getInt(null)));
        } catch (Exception ex) {
        }

        action.putValue(Action.SHORT_DESCRIPTION, i18n.getString(actionID + ".tooltip"));

        String acceleratorKey = i18n.getOptionalString(actionID + ".accelerator");

        if (acceleratorKey != null) {
            addActionAccelerator(KeyStroke.getKeyStroke(acceleratorKey), action);
        }

        return action;
    }

    private void addActionAccelerator(KeyStroke keyStroke, Action action) {
        action.putValue(Action.ACCELERATOR_KEY, keyStroke);

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke, keyStroke);
        getActionMap().put(keyStroke, action);
    }

    /**
     * Reset selection tree and fill with new root object.
     *
     * @param newRootObject new root node object
     */
    public void resetData(SelectionElement newRootObject) {
        setModel(new DefaultTreeModel(new SelectionTreeNode(this, filter, newRootObject, null)));
    }

    /**
     * Answers whether parent nodes selection depend on children selections.<P>
     * In case of this method returns true, the parent node is:<UL>
     * <li>selected if all children are selected
     * <li>unselected if all children are unselected
     * <li>partially selected if children are partially selected or there is partially selected child.</UL>
     */
    public boolean isParentDependsOnChildren() {
        return true;
    }

    private void fireAction(SelectionTreeAction action) {
        if (action == selectAllAction) {
            selectAll(true);
        } else if (action == deselectAllAction) {
            deselectAll(true);
        } else if (action == expandAllAction) {
            expandAll(true);
        } else if (action == collapseAllAction) {
            collapseAll(true);
        }
    }

    private boolean isRowDescendsFromSelected(int row) {
        TreePath path = getPathForRow(row);
        while (path != null) {
            if (isPathSelected(path)) {
                return true;
            }
            path = path.getParentPath();
        }

        return false;
    }

    /**
     * Expand all nodes.
     */
    public void expandAll() {
        expandAll(false);
    }

    /**
     * Expand all nodes depending on current selection.<P>
     * <p/>
     * In case of parameter is true and there is at least one node selected, expand all nodes starting from selected nodes.<BR>
     * In case of parameter is false or there is no selected node, expand all nodes.
     */
    public void expandAll(boolean considerSelection) {
        boolean hasSelection = getSelectionCount() > 0;
        for (int i = 0; i < getRowCount(); i++) {
            if (!considerSelection || !hasSelection || isRowDescendsFromSelected(i)) {
                expandRow(i);
            }
        }
    }

    /**
     * Collapse all nodes, except visible first-level nodes.
     */
    public void collapseAll() {
        collapseAll(false);
    }

    /**
     * Collapse all nodes depending on current selection.<P>
     * <p/>
     * In case of parameter is true and there is at least one node selected, collapse all nodes starting from selected nodes.<BR>
     * In case of parameter is false or there is no selected node, collapse all nodes.
     */
    public void collapseAll(boolean considerSelection) {
        boolean hasSelection = getSelectionCount() > 0;
        for (int i = getRowCount() - 1; i >= 0; i--) {
            TreePath path = getPathForRow(i);
            if ((path.getPath().length > 1) && (!considerSelection || !hasSelection || isRowDescendsFromSelected(i))) {
                collapseRow(i);
            }
        }
    }

    private void deselectNodeWithChildren(SelectionTreeNode node) {
        node.changeSelection(SelectionType.UNSELECTED);
        Enumeration<SelectionTreeNode> childrenEnum = node.children();
        while (childrenEnum.hasMoreElements()) {
            deselectNodeWithChildren(childrenEnum.nextElement());
        }
    }

    private void selectNodeWithChildren(SelectionTreeNode node) {
        node.changeSelection(SelectionType.SELECTED);
        Enumeration<SelectionTreeNode> childrenEnum = node.children();
        while (childrenEnum.hasMoreElements()) {
            selectNodeWithChildren(childrenEnum.nextElement());
        }
    }

    /**
     * Set all nodes selected.
     */
    public void selectAll() {
        selectAll(false);
    }

    /**
     * Set all nodes selected depending on current selection.<P>
     * <p/>
     * In case of parameter is true and there is at least one selected node, set all nodes selected starting from selected nodes.<BR>
     * In case of parameter is false or there is no selected node, set all nodes selected.
     */
    public void selectAll(boolean considerSelection) {
        List<SelectionTreeNode> selNodes = new ArrayList<SelectionTreeNode>();

        if (considerSelection) {
            selNodes.addAll(getSelectedNodes());
        }

        if (selNodes.isEmpty()) {
            selNodes.add((SelectionTreeNode) getModel().getRoot());
        }

        fireSelectionChangeStarted(null);
        for (SelectionTreeNode selNode : selNodes) {
            selectNodeWithChildren(selNode);
        }
        fireSelectionChangeFinished(null);
    }

    /**
     * Set all nodes unselected.
     */
    public void deselectAll() {
        deselectAll(false);
    }

    /**
     * Set all nodes unselected depending on current selection.<P>
     * <p/>
     * In case of parameter is true and there is at least one selected node, set all nodes unselected starting from selected nodes.<BR>
     * In case of parameter is false or there is no selected node, set all nodes unselected.
     */
    public void deselectAll(boolean considerSelection) {
        List<SelectionTreeNode> selNodes = new ArrayList<SelectionTreeNode>();

        if (considerSelection) {
            selNodes.addAll(getSelectedNodes());
        }

        if (selNodes.isEmpty()) {
            selNodes.add((SelectionTreeNode) getModel().getRoot());
        }

        fireSelectionChangeStarted(null);
        for (SelectionTreeNode selNode : selNodes) {
            deselectNodeWithChildren(selNode);
        }
        fireSelectionChangeFinished(null);
    }

    private int getDeep(SelectionTreeNode node) {
        int childrenDeep = 0;

        Enumeration<SelectionTreeNode> childrenEnum = node.children();

        while (childrenEnum.hasMoreElements()) {
            childrenDeep = Math.max(childrenDeep, getDeep(childrenEnum.nextElement()));
        }

        return childrenDeep + 1;
    }

    private int getDeep() {
        return getDeep((SelectionTreeNode) getModel().getRoot());
    }

    /**
     * Adds selection tree actions to popup-menu actions.
     */
    protected void addActionsToPopupMenu(JPopupMenu popupMenu) {
        popupMenu.add(selectAllAction);
        popupMenu.add(deselectAllAction);
        if (getDeep() > 2) {
            popupMenu.addSeparator();
            popupMenu.add(expandAllAction);
            popupMenu.add(collapseAllAction);
        }
    }

    /**
     * Answers selected {@link SelectionTreeNode}.
     */
    public SelectionTreeNode getSelectedNode() {
        TreePath treePath = this.getSelectionPath();
        if (treePath != null) {
            Object obj = treePath.getLastPathComponent();
            if (obj instanceof SelectionTreeNode) {
                return (SelectionTreeNode) obj;
            }
        }

        return null;
    }

    /**
     * Answers collection of selected {@link SelectionTreeNode SelectionTreeNodes}.
     */
    public Collection<SelectionTreeNode> getSelectedNodes() {
        TreePath[] paths = getSelectionPaths();
        List<SelectionTreeNode> result = new ArrayList<SelectionTreeNode>();
        if (paths != null) {
            for (int i = 0; i < paths.length; i++) {
                TreePath treePath = paths[i];
                if (treePath != null) {
                    Object obj = treePath.getLastPathComponent();
                    if (obj instanceof SelectionTreeNode) {
                        result.add((SelectionTreeNode) obj);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Refreshes contents of the tree. Restores expand states and selections after refresh (when possible).<P>
     * This method can be used to update tree structure, when hierarchy of displayed objects has been changed.
     */
    public void refreshContents() {
        Map<SelectionElement, Object> expanded = getExpandedElementsHierarchy();

        SelectionTreeNode sel = getSelectedNode();
        SelectionElement[] selPath = null;
        if (sel != null) {
            Object[] path = sel.getPath().getPath();
            selPath = new SelectionElement[path.length];
            for (int i = 0; i < path.length; i++) {
                SelectionTreeNode node = (SelectionTreeNode) path[i];
                selPath[i] = node.getSelectionElement();
            }
        }

        setModel(new DefaultTreeModel(new SelectionTreeNode(this, filter, ((SelectionTreeNode) getModel().getRoot()).getSelectionElement(), null)));

        if (isParentDependsOnChildren()) {
            updateSelection((SelectionTreeNode) getModel().getRoot());
        }

        List<SelectionTreeNode> firstLevel = Collections.singletonList((SelectionTreeNode) getModel().getRoot());
        expandHierarchyMap(firstLevel, expanded);
        Enumeration<SelectionTreeNode> curLevel = Collections.enumeration(firstLevel);
        SelectionTreeNode newSel = null;
        if (selPath != null) {
            for (int i = 0; i < selPath.length && curLevel != null; i++) {
                SelectionElement selected = selPath[i];
                SelectionTreeNode levelSel = null;
                while (curLevel.hasMoreElements() && levelSel == null) {
                    SelectionTreeNode node = curLevel.nextElement();
                    SelectionElement nodeElement = node.getSelectionElement();
                    if (nodeElement == selected || nodeElement.equals(selected)) {
                        levelSel = node;
                    }
                }
                if (levelSel == null) {
                    curLevel = null;
                } else {
                    if (i == selPath.length - 1) {
                        newSel = levelSel;
                    } else {
                        curLevel = levelSel.children();
                    }
                }
            }
        }
        if (newSel != null) {
            TreePath toSelect = newSel.getPath();
            scrollPathToVisible(toSelect);
            setSelectionPath(toSelect);
        }
    }

    /**
     * Expands the tree according given selection elements hierarchy.
     */
    public void expandElementsHierarchy(Map<SelectionElement, Object> expanded) {
        if (expanded == null) {
            return;
        }
        List<SelectionTreeNode> firstLevel = Collections.singletonList((SelectionTreeNode) getModel().getRoot());
        expandHierarchyMap(firstLevel, expanded);
    }

    /**
     * Answers hierarchy of expanded selection elements.
     */
    @SuppressWarnings("unchecked")
    public Map<SelectionElement, Object> getExpandedElementsHierarchy() {
        Map<SelectionElement, Object> expanded = new HashMap<SelectionElement, Object>();
        for (int i = 0; i < getRowCount(); i++) {
            TreePath path = getPathForRow(i);
            Object[] toExpand = path.getPath();
            Map<SelectionElement, Object> curExMap = expanded;
            for (int j = 0; j < toExpand.length - 1; j++) {
                SelectionTreeNode node = (SelectionTreeNode) toExpand[j];
                SelectionElement nodeObject = node.getSelectionElement();
                if (!curExMap.containsKey(nodeObject)) {
                    curExMap.put(nodeObject, new HashMap<SelectionElement, Object>());
                }
                curExMap = (Map<SelectionElement, Object>) curExMap.get(nodeObject);
            }
        }
        return expanded;
    }

    private void updateSelection(SelectionTreeNode node) {
        Enumeration<SelectionTreeNode> children = node.children();
        if (children.hasMoreElements()) {
            while (children.hasMoreElements()) {
                updateSelection(children.nextElement());
            }
        } else {
            node.updateSelection(true);
        }
    }

    /**
     * Updates view of tree.<P>
     * This method can be used to update tree, when displayed objects has been changed.
     */
    public void updateView() {
        for (int i = 0; i < getRowCount(); i++) {
            TreePath path = getPathForRow(i);
            ((SelectionTreeNode) path.getLastPathComponent()).updateNode();
        }
    }

    /**
     * Answers {@link SelectionTreeNode} corresponding to {@link SelectionElement} business-object in the selection tree
     * starting from the given {@link SelectionTreeNode}. To obtain first node corresponding to the given criterion,
     * null value can be specified as starting node.
     */
    public SelectionTreeNode findNodeByObject(final SelectionElement nodeObject, SelectionTreeNode startingFrom, boolean forward) {
        if (nodeObject == null) {
            return null;
        }

        return findNodeByObjectFilter(new SelectionTreeFilter() {

            public boolean accept(SelectionElement candidateNodeObject) {
                return candidateNodeObject == nodeObject;
            }
        }, startingFrom, forward);
    }

    /**
     * Answers {@link SelectionTreeNode} accepted by given {@link SelectionTreeFilter} starting from the given
     * {@link SelectionTreeNode}. To obtain first node corresponding to the given criterion, null value can be
     * specified as starting node.
     */
    public SelectionTreeNode findNodeByObjectFilter(SelectionTreeFilter objectFilter, SelectionTreeNode startingFrom, boolean forward) {
        SelectionTreeNode firstNode = (SelectionTreeNode) getModel().getRoot();
        SelectionTreeNode lastNode = firstNode;

        while (lastNode.getChildCount() > 0) {
            lastNode = (SelectionTreeNode) lastNode.getChildAt(lastNode.getChildCount() - 1);
        }

        if (!isRootVisible()) {
            firstNode = firstNode.getNextNode();
            if (lastNode == getModel().getRoot()) { //no root, no children - nothing to find
                return null;
            }
        }

        if (startingFrom == null) {
            startingFrom = forward ? firstNode : lastNode;
        }

        SelectionTreeNode node = startingFrom;

        while (node != (forward ? lastNode.getNextNode() : firstNode.getPreviousNode())) {
            if (objectFilter.accept(node.getSelectionElement())) {
                return node;
            }

            node = forward ? node.getNextNode() : node.getPreviousNode();
        }

        node = forward ? firstNode : lastNode;

        while (node != startingFrom) {
            if (objectFilter.accept(node.getSelectionElement())) {
                return node;
            }

            node = forward ? node.getNextNode() : node.getPreviousNode();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private void expandHierarchyMap(List<SelectionTreeNode> curLevelNodes, Map<SelectionElement, Object> expanded) {
        for (Iterator<Map.Entry<SelectionElement, Object>> iter = expanded.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<SelectionElement, Object> entry = iter.next();
            SelectionElement nodeObject = entry.getKey();
            SelectionTreeNode nodeToExpand = null;
            for (Iterator<SelectionTreeNode> levelIter = curLevelNodes.iterator(); levelIter.hasNext();) {
                SelectionTreeNode node = levelIter.next();
                SelectionElement nodeElement = node.getSelectionElement();
                if (nodeElement == nodeObject || nodeElement.equals(nodeObject)) {
                    nodeToExpand = node;
                }
            }

            if (nodeToExpand != null) {
                expandPath(nodeToExpand.getPath());
                List<SelectionTreeNode> subLevel = new ArrayList<SelectionTreeNode>();
                Enumeration<SelectionTreeNode> children = nodeToExpand.children();
                while (children.hasMoreElements()) {
                    subLevel.add(children.nextElement());
                }

                expandHierarchyMap(subLevel, (Map<SelectionElement, Object>) entry.getValue());
            }
        }
    }

    /**
     * Adds selection tree listener to the selection tree.
     *
     * @param selectionTreeListener {@link SelectionTreeListener} to add
     */
    public synchronized void addSelectionTreeListener(SelectionTreeListener selectionTreeListener) {
        listeners.add(selectionTreeListener);
    }

    /**
     * Removes selection tree listener from the selection tree.
     *
     * @param selectionTreeListener {@link SelectionTreeListener} to remove
     */
    public synchronized void removeSelectionTreeListener(SelectionTreeListener selectionTreeListener) {
        listeners.remove(selectionTreeListener);
    }

    /**
     * Notifies listeners than selection type of a node has been changed.
     *
     * @param node     {@link SelectionTreeNode} with selection type changed
     * @param oldType old selection type
     * @param newType new selection type
     */
    protected void fireSelectionTypeChanged(SelectionTreeNode node, SelectionType oldType, SelectionType newType) {
        if (listeners != null) {
            for (Iterator<SelectionTreeListener> iter = listeners.iterator(); iter.hasNext();) {
                iter.next().selectionTypeChanged(node, oldType, newType);
            }
        }
    }

    /**
     * Notifies listeners than selection change is started.
     *
     * @param node node which selection is going to be changed or null if this operation is the part of mass selection change (Select All/Deselect All)
     */
    protected void fireSelectionChangeStarted(SelectionTreeNode node) {
        if (listeners != null) {
            for (Iterator<SelectionTreeListener> iter = listeners.iterator(); iter.hasNext();) {
                iter.next().selectionChangeStarted(node);
            }
        }
    }

    /**
     * Notifies listeners than selection change is finished.
     *
     * @param node node which selection is changed or null if this operation is the part of mass selection change (Select All/Deselect All)
     */
    protected void fireSelectionChangeFinished(SelectionTreeNode node) {
        if (listeners != null) {
            for (Iterator<SelectionTreeListener> iter = listeners.iterator(); iter.hasNext();) {
                iter.next().selectionChangeFinished(node);
            }
        }
    }

    /**
     * Answers whether the root is selectable.
     */
    protected boolean isRootSelectable() {
        return true;
    }

    public String[] getSelection() {
        Vector v = new Vector();
        getSelection((SelectionTreeNode) getModel().getRoot(), v);

        String[] paths = new String[v.size()];
        v.copyInto(paths);
        return paths;
    }

    private void getSelection(SelectionTreeNode node, Vector v) {
        switch (node.getSelectionType()) {
            case UNSELECTED:
                break;

            case PARTIALLY_SELECTED:
                for (int i = 0; i < node.getChildCount(); i++) {
                    getSelection(node.getChildAt(i), v);
                }
                break;

            case SELECTED:
                v.add(getPath(node));
                break;
        }
    }

    protected String getPath(SelectionTreeNode node) {
        return getPath(node, 0).toString();
    }

    private StringBuffer getPath(SelectionTreeNode node, int length) {
        if (node.getParent() == null) {
            return new StringBuffer(length);
        } else {
            String nodeName = node.getSelectionElement().getDisplayableName();
            StringBuffer sb = getPath(node.getParent(), 1 + nodeName.length() + length);
            if (sb.length() > 0) {
                sb.append("/");
            }
            sb.append(nodeName);
            return sb;
        }
    }

    public boolean isSelectionEmpty() {
        return ((SelectionTreeNode) getModel().getRoot()).getSelectionType() == SelectionType.UNSELECTED;
    }

    public void setSelection(String[] paths) {
        if (paths == null || paths.length == 0) {
            ((SelectionTreeNode) getModel().getRoot()).changeSelection(SelectionType.SELECTED);
        } else {
            ((SelectionTreeNode) getModel().getRoot()).changeSelection(SelectionType.UNSELECTED);
            if (paths != null) {
                for (int i = 0; i < paths.length; i++) {
                    SelectionTreeNode node = getNode(((SelectionTreeNode) getModel().getRoot()), paths[i]);
                    if (node != null) {
                        node.changeSelection(SelectionType.SELECTED);
                    }
                }
            }
        }
    }

    private SelectionTreeNode getNode(SelectionTreeNode node, String path) {
        if (node == null) {
            throw new NullPointerException();
        }

        if (path.length() == 0) {
            return node;
        }

        String head;
        String tail;

        int sep = path.indexOf("/");
        if (sep == -1) {
            head = path;
            tail = null;
        } else {
            head = path.substring(0, sep);
            tail = path.substring(sep + 1);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            SelectionTreeNode c = node.getChildAt(i);
            if (c.getSelectionElement().getDisplayableName().equals(head)) {
                return (tail == null ? c : getNode(c, tail));
            }
        }

        return null;
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        return ((SelectionTreeCellRenderer) getCellRenderer()).getToolTipLocation(event);
    }

    private void showPopupMenu(int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();
        addActionsToPopupMenu(popupMenu);
        popupMenu.show(this, x, y);
        return;
    }

    private class SelectionTreeCellRenderer extends DefaultTreeCellRenderer {

        private boolean ignoreToolTip = false;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            SelectionTreeNode node = (SelectionTreeNode) value;

            Icon icon;
            SelectionElement selectionElement = node.getSelectionElement();
            Object rootNode = getModel().getRoot();
            boolean isRootNode = node == rootNode || (!isRootVisible() && node.getParent() == rootNode);
            if (isRootNode && !isRootSelectable()) {
                icon = null;
            } else {
                switch (selectionElement.getSelectionType()) {
                    case SELECTED:
                        icon = selectedIcon;
                        break;
                    case PARTIALLY_SELECTED:
                        icon = partiallySelectedIcon;
                        break;
                    default:
                        icon = unselectedIcon;
                        break;
                }
            }

            setIcon(icon);
            if (!ignoreToolTip) {
                ignoreToolTip = true;
                setToolTipText(null);
                Rectangle rect = getPathBounds(node.getPath());
                if (rect != null && (!tree.getVisibleRect().contains(rect) || selectionElement.isToolTipAlwaysShown())) {
                    setToolTipText(selectionElement.getToolTip());
                }
                ignoreToolTip = false;
            }
            setText(selectionElement.getDisplayableName());

            Font oldFont = getFont();
            if (oldFont != null) {
                setFont(oldFont.deriveFont(selectionElement instanceof StyledSelectionElement
                        ? ((StyledSelectionElement) selectionElement).getFontStyle() : Font.PLAIN));
            }

            this.hasFocus = hasFocus;
            this.selected = sel;

            this.setVerticalTextPosition(SwingConstants.CENTER);

            Color foregroundColor = null;

            if (selectionElement instanceof StyledSelectionElement) {
                foregroundColor = ((StyledSelectionElement) selectionElement).getColor();
            }

            if (foregroundColor == null) {
                if (sel) {
                    foregroundColor = getTextSelectionColor();
                } else {
                    foregroundColor = getTextNonSelectionColor();
                }
            }

            setForeground(foregroundColor);

            setEnabled(tree.isEnabled());
            setComponentOrientation(tree.getComponentOrientation());

            return this;
        }

        @Override
        public Point getToolTipLocation(MouseEvent e) {
            TreePath treePath = getPathForLocation(e.getX(), e.getY());

            if (treePath != null) {
                Rectangle r = getPathBounds(treePath);
                if (r != null) {
                    Insets tooltipInsets = SelectionTree.this.createToolTip().getInsets();
                    Object rootNode = getModel().getRoot();
                    TreeNode node = (TreeNode) treePath.getLastPathComponent();
                    boolean isRootNode = node == rootNode || (!isRootVisible() && node.getParent() == rootNode);
                    int iconSpace = 0;
                    if (!isRootNode || isRootSelectable()) {
                        iconSpace = checkSize + getIconTextGap();
                    }
                    return new Point(r.x + iconSpace
                            - tooltipInsets.left - TOOLTIP_UI_TEXT_GAP, r.y);
                }
            }

            return super.getToolTipLocation(e);
        }
    }

    private class SelectionTreeMouseListener extends MouseAdapter {

        private TreePath treePath = null;

        @Override
        public void mousePressed(MouseEvent e) {
            treePath = SelectionTree.this.getPathForLocation(e.getX(), e.getY());
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (isEnabled() && e.getClickCount() == 1) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (treePath != null) {
                        Object obj = treePath.getLastPathComponent();
                        if (obj instanceof SelectionTreeNode) {
                            SelectionTreeNode node = (SelectionTreeNode) obj;
                            int checkAreaSize = checkSize - 2 * checkBorderSize;
                            Rectangle r = SelectionTree.this.getPathBounds(treePath);
                            if (r != null) {
                                int yPos = (r.height - checkAreaSize) / 2 + (int) r.getY();
                                Rectangle btn = new Rectangle((int) r.getX() + checkBorderSize, yPos, checkAreaSize,
                                        checkAreaSize);
                                if (btn.contains(e.getX(), e.getY())) {
                                    node.changeSelection();
                                    return;
                                }
                            }
                        }
                    }
                } else {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        treePath = SelectionTree.this.getPathForLocation(e.getX(), e.getY());
                        if (e.isControlDown()) {
                            if (isPathSelected(treePath)) {
                                removeSelectionPath(treePath);
                            } else {
                                addSelectionPath(treePath);
                                setAnchorSelectionPath(treePath);
                                setLeadSelectionPath(treePath);
                            }
                        } else if (e.isShiftDown()) {
                            TreePath anchor = getAnchorSelectionPath();
                            int anchorRow = (anchor == null) ? -1 : getRowForPath(anchor);
                            if (anchorRow == -1 || getSelectionModel().getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION) {
                                setSelectionPath(treePath);
                            } else {
                                int row = getRowForPath(treePath);
                                TreePath lastAnchorPath = anchor;
                                if (row < anchorRow) {
                                    setSelectionInterval(row, anchorRow);
                                } else {
                                    setSelectionInterval(anchorRow, row);
                                }
                                setAnchorSelectionPath(lastAnchorPath);
                                setLeadSelectionPath(treePath);
                            }
                        } else {
                            setSelectionPath(treePath);
                        }
                        showPopupMenu(e.getX(), e.getY());
                        return;
                    }
                }
            }
        }
    }

    private class SelectionTreeKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (isEnabled() && e.getKeyCode() == KeyEvent.VK_SPACE) {
                final TreePath treePath = SelectionTree.this.getSelectionPath();
                if (treePath != null) {
                    Object obj = treePath.getLastPathComponent();
                    if (obj instanceof SelectionTreeNode) {
                        SelectionTreeNode node = (SelectionTreeNode) obj;
                        node.changeSelection();
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                SelectionTree.this.setSelectionPath(treePath);
                            }
                        });
                        return;
                    }
                }
            }
        }
    }

    private class SelectionTreeAction extends AbstractAction {

        private ActionListener actionListener;

        public SelectionTreeAction(ActionListener actionListener) {
            this.actionListener = actionListener;
        }

        public void actionPerformed(final ActionEvent e) {
            new Thread(new Runnable() {

                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            if (actionListener != null) {
                                actionListener.actionPerformed(e);
                            } else {
                                fireAction(SelectionTreeAction.this);
                            }
                        }
                    });
                }
            }, "Selection Tree Action Thread").start();
        }
    }
}
