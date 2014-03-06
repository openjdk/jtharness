/*
 * $Id$
 *
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import com.sun.javatest.util.I18NResourceBundle;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

/**
 * An abstract class for a GUI panel that can be displayed to
 * allow the user to edit some of the user preferences.
 * A pane may have child panes, containing additional groups
 * of preferences relevant to but not contained on this pane.
 */
public abstract class PreferencesPane extends JPanel {
    /**
     * Show a dialog to allow the user to edit the set of preferences.
     * A collection of editing panes must be provided that each provide
     * the GUI to edit a subset of the preferences.
     * @param f the parent frame for the dialog
     * @param panes the editing panes to be displayed in the dialog
     * @param helpBroker a help broker to be used to provide context sensitive
     *          help for the dialog
     */
    public static void showDialog(JFrame f, Preferences preferences,
            PreferencesPane[] panes, HelpBroker helpBroker) {
        //System.err.println("Preferences.showDialog");
        PrefsDialog d = new PrefsDialog(f, preferences, panes, helpBroker);
        d.setVisible(true);
        //System.err.println("Preferences.showDialog done");
    }

    /**
     * Set the help ID that gives the context sensitive help for
     * this panel. The help will be displayed in the help broker
     * specified when the dialog is displayed.
     * @param helpID the ID for the context sensitive help for this panel
     * @see PreferencesPane#showDialog
     */
    protected void setHelp(String helpID) {
        CSH.setHelpIDString(this, helpID);
    }

    /**
     * Get a text string which identifies the group of user preferences
     * that can be edited on the panel.
     * @return a text string to identify the preferences on this panel
     */
    public abstract String getText();

    /**
     * Load the values of the user preferences relevant to this panel
     * from a given map object.
     * @param m the map from which to load the user preferences into the
     * GUI components
     */
    public void load(Map m) {
        PreferencesPane[] p = getChildPanes();
        if (p != null) {
            for (int i = 0; i < p.length; i++)
                p[i].load(m);
        }
    }

    /**
     * Save the values of the user preferences relevant to this panel
     * into a given map object.
     * @param m the map to which to save the user preferences from the
     * GUI components
     */
    public void save(Map m) {
        PreferencesPane[] p = getChildPanes();
        if (p != null) {
            for (int i = 0; i < p.length; i++)
                p[i].save(m);
        }
    }

    /**
     * Analyze the current values entered by the user and determine if
     * they are valid.  If they are, the return value should be null.
     * By default the return value is null.
     * @return null if all the values are valid.  Otherwise, an
     *    <b>internationalized</b> string.
     * @see com.sun.javatest.util.I18NResourceBundle
     */
    public String validateValues() {
        return null;
    }

    /**
     * Get the set of child panes, if any, containing additional groups
     * of preferences relevant to but not contained on this pane.
     * @return an array of child panes, or null if none
     */
    public PreferencesPane[] getChildPanes() {
        return null;
    }

    private static class PrefsDialog
        extends JDialog // can't be ToolDialog because might change desktop style
        implements ActionListener, TreeModel, TreeSelectionListener, WindowListener
    {
        PrefsDialog(JFrame f, Preferences preferences,
                PreferencesPane[] panes, HelpBroker helpBroker) {
            // Don't use the argument frame 'f' as the parent of the dialog
            // in case that frame is disposed while "apply"ing preferences.
            // Instead, we use null (private hidden frame) as the parent,
            // and merely center the dialog over the argument frame.
            super((JFrame)null, /*modal:*/true);
            owner = f;
            this.preferences = preferences;
            this.props = preferences.getProperties();
            this.panes = panes;
            this.helpBroker = helpBroker;

            uif = new UIFactory(getClass(), helpBroker);
            initGUI();

            //System.err.println("Prefs.dialog addWindowListener " + owner.getName());
            owner.addWindowListener(this);
        }

        public void setVisible(boolean b) {
            if (b) {
                for (int i = 0; i < panes.length; i++)
                    panes[i].load(props);
            }
            super.setVisible(b);

            if (!b) {
                //System.err.println("Prefs.dialog removeWindowListener " + owner.getName());
                owner.removeWindowListener(this);
            }
        }

        private void initGUI() {
            //prefsTreeLeafIcon = uif.createIcon("prefs.leaf");
            setName("prefs");
            setTitle(uif.getI18NString("prefs.title"));
            if (helpBroker != null) {
                helpBroker.enableHelpKey(getRootPane(), "ui.prefs.dialog.csh", null);
            }
            Desktop.addHelpDebugListener(this);
            uif.setAccessibleDescription(this, "prefs");

            main = uif.createPanel("prefs.main", new BorderLayout(), false);

            initTree();    // add into main, WEST
            initPanes();   // add into main, CENTER
            initButtons(); // add into main, SOUTH

            setContentPane(main);
            Object[] path = new Object[2];
            path[0] = tree.getModel().getRoot();
            path[1] = getChildren(this)[0];     // assumes we always have at least one
            tree.setSelectionPath(new TreePath(path));

            pack();
            setLocationRelativeTo(owner);

            getRootPane().setDefaultButton(okBtn);
        }

        private void initTree() {
            tree = new JTree(this);
            tree.setName("prefs.tree");
            tree.addTreeSelectionListener(this);
            tree.setEditable(false);
            tree.setShowsRootHandles(true);
            tree.setRootVisible(false);
            uif.setAccessibleInfo(tree, "prefs.tree");

            int dpi = uif.getDotsPerInch();
            tree.setPreferredSize(new Dimension(2 * dpi, dpi));
            tree.setVisibleRowCount(10);

            tree.setCellRenderer(new DefaultTreeCellRenderer() {
                public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected,
                                                      boolean expanded,
                                                      boolean leaf, int row,
                                                      boolean hasFocus) {
                    if (value instanceof PreferencesPane) {
                        return super.getTreeCellRendererComponent(tree, ((PreferencesPane)value).getText(),
                            selected, expanded, leaf, row, hasFocus);
                    } else {
                        return super.getTreeCellRendererComponent(tree, value,
                            selected, expanded, leaf, row, hasFocus);
                    }
                }
                /*
                public Icon getLeafIcon() {
                    // consider use of blank icon?
                    return prefsTreeLeafIcon;
                }
                */
            });
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            tree.expandRow(2);  // expand two levels

            // construct the tree side
            JComponent treeStuff = uif.createScrollPane(tree);
            treeStuff.setBorder(BorderFactory.createEtchedBorder());

            main.add(treeStuff, BorderLayout.WEST);
        }

        private void initButtons() {
            okBtn = uif.createButton("prefs.ok", this);
            cancelBtn = uif.createCancelButton("prefs.cancel", this);
            helpBtn = uif.createHelpButton("prefs.help", "ui.prefs.dialog.csh");

            JButton[] btns = { okBtn, cancelBtn, helpBtn };

            // set all the buttons to the same preferred size, per JL&F
            Dimension maxBtnDims = new Dimension();
            for (int i = 0; i < btns.length; i++) {
                Dimension d = btns[i].getPreferredSize();
                maxBtnDims.width = Math.max(maxBtnDims.width, d.width);
                maxBtnDims.height = Math.max(maxBtnDims.height, d.height);
            }

            for (int i = 0; i < btns.length; i++)
                btns[i].setPreferredSize(maxBtnDims);

            JPanel p = uif.createPanel("prefs.btns", false);
            p.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.EAST;
            c.insets.top = 5;
            c.insets.bottom = 11;  // value from JL&F Guidelines
            c.insets.right = 11;   // value from JL&F Guidelines
            c.weightx = 1;         // first button absorbs space to the left

            for (int i = 0; i < btns.length; i++) {
                p.add(btns[i], c);
                c.weightx = 0;
            }

            main.add(p, BorderLayout.SOUTH);

            InputMap inputMap = p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = p.getActionMap();
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), UIFactory.CANCEL);
            actionMap.put(UIFactory.CANCEL, new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        cancelBtn.doClick(250);
                    }
                });
        }

        private void initPanes() {
            deck = uif.createPanel("prefs.deck", new CardLayout(), false);
            addAllPanes(deck, panes);

            int dpi = uif.getDotsPerInch();
            Dimension maxPrefSize = new Dimension(3 * dpi, 2 * dpi);
            for (int i  = 0; i < deck.getComponentCount(); i++) {
                Dimension d = deck.getComponent(i).getPreferredSize();
                maxPrefSize.width = Math.max(maxPrefSize.width, d.width);
                maxPrefSize.height = Math.max(maxPrefSize.height, d.height);
            }
            deck.setPreferredSize(maxPrefSize);

            main.add(deck, BorderLayout.CENTER);
        }

        private void addAllPanes(JPanel deck, PreferencesPane[] panes) {
            for (int i = 0; i < panes.length; i++) {
                PreferencesPane pane = panes[i];

                JPanel p = uif.createPanel("prefs.card" + cardNum++, false);
                p.setLayout(new BorderLayout());
                JLabel head = new JLabel(pane.getText());
                head.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                p.add(head, BorderLayout.NORTH);

                pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                p.add(uif.createScrollPane(pane), BorderLayout.CENTER);
                deck.add(p, pane.getText());

                if (pane.getChildPanes() != null)
                    addAllPanes(deck, pane.getChildPanes());
            }
        }

        private int cardNum;

        // ---------- ActionListener -------------------------------

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == okBtn) {
                boolean b = okToSave();

                if (!b)
                    return;

                setPreferences(panes);
                preferences.save();
                setVisible(false);
            }
                else if (src == cancelBtn) {
                        for (int i = 0; i < panes.length; i++)
                                panes[i].load(props);
                        setVisible(false);
                }
        }

        private boolean okToSave() {
            String reason = null;
            for (int i = 0; i < panes.length; i++) {
                reason = panes[i].validateValues();
                if (reason != null) {
                    tree.setSelectionPath(new TreePath(new Object[] {this, panes[i]}));
                    break;
                }

                PreferencesPane[] p = panes[i].getChildPanes();
                if (p != null) {
                    for (int j = 0; j < p.length; j++) {
                        reason = p[j].validateValues();
                        if (reason != null) {
                            tree.setSelectionPath(new TreePath(
                                new Object[] {this, panes[i], p[j]}));
                            break;
                        }
                    }   // for j

                    if (reason != null)
                        break;
                }
            }   // for i

            if (reason != null) {
                // show error dialog
                uif.showLiteralError(uif.getI18NString("prefs.badPref.title"),
                                     reason);
                return false;
            } else {
                return true;
            }
        }

        // ---------- TreeSelectionListener ---------------------------------

        public void valueChanged(TreeSelectionEvent e) {
            TreePath path = e.getNewLeadSelectionPath();
            if (path != null) {
                PreferencesPane pane = (PreferencesPane) (path.getLastPathComponent());
                ((CardLayout)(deck.getLayout())).show(deck, pane.getText());
            }
        }

        // --------- TreeModel --------------------------------------

        public Object getChild(Object parent, int index) {
            return getChildren(parent)[index];
        }

        public int getChildCount(Object parent) {
            return getChildren(parent).length;
        }

        public int getIndexOfChild(Object parent, Object child) {
            PreferencesPane[] children = getChildren(parent);
            for (int i = 0; i < children.length; i++) {
                if (children[i] == child)
                    return i;
            }
            return -1;
        }

        public Object getRoot() {
            return this;
        }

        public boolean isLeaf(Object node) {
            PreferencesPane[] children = getChildren(node);
            return (children == null || children.length == 0);
        }

        private void setPreferences(PreferencesPane[] panes) {
            Map m = new Map() {

                public void clear() {
                    throw new UnsupportedOperationException();
                }

                public boolean containsKey(Object key) {
                    return props.containsKey(key);
                }

                public boolean containsValue(Object value) {
                    return props.containsValue(value);
                }

                public Set entrySet() {
                    return props.entrySet();
                }

                public boolean equals(Object o) {
                    return props.equals(o);
                }

                public Object get(Object key) {
                    return props.get(key);
                }

                public int hashCode() {
                    return props.hashCode();
                }

                public boolean isEmpty() {
                    return props.isEmpty();
                }

                public Set keySet() {
                    return props.keySet();
                }

                public Object put(Object key, Object value) {
                    Object oldValue = props.get(key);
                    if (oldValue == null || !oldValue.equals(value)) {
                        preferences.setPreference((String) key, (String) value);
                    }
                    return oldValue;
                }

                public void putAll(Map m) {
                    throw new UnsupportedOperationException();
                }

                public Object remove(Object key) {
                    throw new UnsupportedOperationException();
                }

                public int size() {
                    return props.size();
                }

                public Collection values() {
                    return props.values();
                }
            };

            for (int i = 0; i < panes.length; i++) {
                panes[i].save(m);
            }
        }

        // --------- TreeModelListener ---------------------------------------

        public void addTreeModelListener(TreeModelListener l) {
        }

        public void removeTreeModelListener(TreeModelListener l) {
        }

        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        // --------- WindowListener -----------------------------------------

        public void windowOpened(WindowEvent e) {
            //System.err.println("Prefs.dialog " + e);
        }

        public void windowClosing(WindowEvent e) {
            //System.err.println("Prefs.dialog " + e);
        }

        public void windowClosed(WindowEvent e) {
            //System.err.println("Prefs.dialog " + e);
        }

        public void windowIconified(WindowEvent e) {
            //System.err.println("Prefs.dialog " + e);
        }

        public void windowDeiconified(WindowEvent e) {
            //System.err.println("Prefs.dialog " + e + " (" + e.getSource() + ")");
            if (e.getSource() == owner)
                toFront();
        }

        public void windowActivated(WindowEvent e) {
            //System.err.println("Prefs.dialog " + e + " (" + e.getSource() + ")");
            if (e.getSource() == owner)
                toFront();
        }

        public void windowDeactivated(WindowEvent e) {
            //System.err.println("Prefs.dialog " + e);
        }

        // ------------------------------------------------------------------

        private PreferencesPane[] getChildren(Object parent) {
            return (parent == this ? panes : ((PreferencesPane)parent).getChildPanes());
        }

        private JFrame owner;
        private Preferences preferences;
        private Properties props;

        private HelpBroker helpBroker;
        private PreferencesPane[] panes;
        private UIFactory uif;
        private JPanel main;
        private JPanel deck;
        private JButton okBtn;
        private JButton cancelBtn;
        private JButton helpBtn;
        private JTree tree;
    }
}
