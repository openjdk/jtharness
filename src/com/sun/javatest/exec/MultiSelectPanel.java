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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import javax.help.CSH;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.sun.javatest.tool.UIFactory;

import com.sun.javatest.util.Debug;

/**
 * This panel shows information when multiple nodes are selected in the tree.
 */
class MultiSelectPanel
    extends JPanel
    //implements FilterSelectionHandler.Observer
{
    MultiSelectPanel(UIFactory uif, TreePanelModel model, TestTreeModel ttm) {
        this.uif = uif;
        this.tpm = model;
        this.ttm = ttm;

        initGUI();
    }

    // XXX use TreePath[] as parameter?
    void setNodes(Object[] nodes) {
        this.nodes =nodes;
        updatePanel(nodes);
    }

    /**
     * This method should only be called to indicate that a change has occurred
     * which replaces the active TRT.  Changes to the parameters (filters,
     * initial URLs, etc... should propagate thru the FilterConfig system.
     *
     * @param p A validated set of parameters.
     * @see com.sun.javatest.exec.FilterConfig
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
     */

    protected void initGUI() {
        setName("multiselect");
        setLayout(new GridBagLayout());
        setMinimumSize(new Dimension(150, 100));

        listModel = new DefaultListModel();
        nodeList = uif.createList("ms.nlist", listModel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx= 1.0;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // add inline help
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(uif.createMessageArea("ms.help"), gbc);

        gbc.weighty = 9.0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        nodeList.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 5, 5, 5),
            uif.createTitledBorder("ms.nlist"))
        );

        nodeList.setCellRenderer(RenderingUtilities.createTRTNodeRenderer());

        add(new JScrollPane(nodeList), gbc);

        // --- anonymous class ---
        ComponentListener cl = new ComponentListener() {
            public void componentResized(ComponentEvent e) {
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
        CSH.setHelpIDString(this, "browse.multiselectionTab.csh");
    }

    protected void updatePanel(Object[] nodes) {
        if (isVisible())
            updateGUI();
        else
            needToUpdateGUIWhenShown = true;
    }

    Object[] getNodes() {
        return nodes;
    }

    /**
     * Call when the target node or tree data have changed.  This is called
     * internally to force updates when filters have changed.
     */
    protected void updateGUI() {
        listModel.removeAllElements();

        if (nodes == null)
            return;

        for (int i = 0; i < nodes.length; i++)
            listModel.addElement(nodes[i]);
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    private TestTreeModel ttm;
    private TreePanelModel tpm;
    private JList nodeList;
    private DefaultListModel listModel;
    private Object[] nodes;

    private UIFactory uif;

    private volatile boolean needToUpdateGUIWhenShown;

    private static boolean debug = Debug.getBoolean(MultiSelectPanel.class);
}
