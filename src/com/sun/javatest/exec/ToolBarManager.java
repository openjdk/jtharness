/*
 * $Id$
 *
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import com.sun.javatest.tool.UIFactory;

/**
 * The "Tool Bar Manager" tool, which allows a user to add, remove,
 * and get custom toolbars.
 */
public class ToolBarManager {
    public ToolBarManager() {
    }

    /**
     * Adds specified toolbar.
     * @param theBar the tool bar which should be added to the TestManager,
     * can't be null
     * @param toolBarID unique string ID of theBar, can't be null or empty
     * @return <tt>true</tt> if the bar was successfully added
     **/
    public boolean addToolBar(final JavaTestToolBar theBar, String toolBarID) {
        if (!store.containsKey(toolBarID)) {
            store.put(toolBarID, theBar);

            if (panel != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        panel.add(theBar);
                        mmanager.addToolbar(theBar);
                    }
                });
            }
            else {
                // will occur in setPanel()
            }

            return true;
        }
        return false;
    }

    /**
     * Finds toolbar by ID and returns it.
     * @param toolBarID unique string ID of theBar, can't be null or empty
     * @return the toolbar object, or <tt>null</tt> if the manager contains
     * no tool bar for this key.
     **/
    public JavaTestToolBar getToolBar(String toolBarID) {
        return (JavaTestToolBar) store.get(toolBarID);
    }

    /**
     * Returns an array of currently registered toolbars.
     * @return array of currently registered toolbars
     **/
    public JavaTestToolBar[] getToolBars() {
        JavaTestToolBar[] ret = new JavaTestToolBar[0];
        ret = (JavaTestToolBar[]) store.values().toArray(ret);
        return ret;
    }

    /**
     * Removes the toolbar by ID.
     * @param toolBarID unique string ID of theBar, can't be null or empty
     * @return <tt>true</tt> if the bar was successfully removed and <tt>false</tt>
     * if the specified ID was not be found.
     **/
    boolean removeToolBar(String toolBarID) {
        if (store.containsKey(toolBarID)) {
            final JavaTestToolBar tb = (JavaTestToolBar) store.get(toolBarID);

            if (panel != null)
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        panel.remove(tb);
                        mmanager.removeToolbar(tb);
                    }
                });
            else {
                mmanager.removeToolbar(tb);
            }

            store.remove(toolBarID);
            return true;
        }
        return false;
    }

    /**
     * Accessor for the toolbar submenu in View menu
     **/
    JMenu getToolbarMenu() {
        if (toolBarMenu == null) {
            toolBarMenu = uif.createMenu("tbmanager.viewmenu");
            toolBarMenu.setVisible(false);
        }
        return toolBarMenu;
    }

    /**
     * Saves visible state.
     * Invoked from ExecTool's save
     **/
    void save(Map m) {
        if (m != null) {
            for (JavaTestToolBar tb : store.values()) {
                tb.save(m);
            }
        }
    }

    void load(Map m) {
        if (m != null) {
            for (JavaTestToolBar tb : store.values()) {
                tb.load(m);
            }
        }
    }

    void setUIFactory(UIFactory u) {
        uif = u;
    }

    void setPanel(ToolBarPanel p) {
        if (p == null)
            throw new NullPointerException();

        panel = p;

        if (store.size() > 0) {
            JavaTestToolBar[] tbs = getToolBars();
            for (int i = 0; i < tbs.length; i++) {
                panel.add(tbs[i]);
                mmanager.addToolbar(tbs[i]);
            }   // for
        }
    }

    void setVisibleFromPrefs(boolean visible) {
        int count = toolBarMenu.getItemCount();
        for (int i = 0; i < count; i++) {
            JCheckBoxMenuItem mi = (JCheckBoxMenuItem)toolBarMenu.getItem(i);
            if (mi != null) {
                ToolbarMenuAction a = (ToolbarMenuAction)mi.getAction();
                JavaTestToolBar bar = a.getBar();
                if (!visible) {
                    bar.setVisibleNoStateAffect(visible);
                }
                else {
                    bar.setVisibleNoStateAffect(bar.readVisibleState());
                }
                mi.setState(bar.isVisible());
            }
        }
    }

    /**
     * TBMenuManager manages toolbar menu and processes adding/removing toolbars
     **/
    class TBMenuManager {
        /**
         * Process adding toolbar to the panel.
         **/
        public void addToolbar(JavaTestToolBar t) {
            if (t.isMenuControlled()) {
                JMenuItem m = getToolbarMenu();
                ToolbarMenuAction ac = new ToolbarMenuAction(t);
                JCheckBoxMenuItem mi = new JCheckBoxMenuItem(ac);
                mi.setState(t.isVisible());
                m.add(mi);
                toolBarMenu.setVisible(true);
            }
        }
        /**
         * Process removing toolbar from the panel.
         **/
        public void removeToolbar(JavaTestToolBar t) {
            if (t.isMenuControlled()) {
                JMenu m = getToolbarMenu();
                for (int i = 0; i < m.getItemCount() ; i++) {
                    JMenuItem it = m.getItem(i);
                    Action act = it.getAction();
                    if (act instanceof ToolbarMenuAction) {
                        if (((ToolbarMenuAction) act).getBar() == t) {
                            m.remove(i);
                            break;
                        }
                    }
                }
                if (m.getItemCount() == 0) {
                    m.setVisible(false);
                }
            }
        }

    }

    /**
     *ToolbarMenuAction reflects changing of visible state to the menu
     **/
    class ToolbarMenuAction extends AbstractAction {
        public JavaTestToolBar getBar() {
            return theBar;
        }
        public ToolbarMenuAction(JavaTestToolBar bar) {
            super(bar.getName());
            theBar = bar;
        }
        public void actionPerformed(ActionEvent evt) {
            theBar.setVisible(!theBar.isVisible());
        }
        private JavaTestToolBar theBar;
    }

    private UIFactory uif;
    private JMenu toolBarMenu;
    private TBMenuManager mmanager = new TBMenuManager();
    private Map<String, JavaTestToolBar> store =
        Collections.synchronizedMap(new LinkedHashMap<String, JavaTestToolBar>());
    private ToolBarPanel panel;

}
