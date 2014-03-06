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
package com.sun.javatest.tool;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dialog.ModalityType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.help.CSH;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.sun.javatest.util.PrefixMap;

/**
 * A container that presents the current desktop tools in a tabbed pane.
 * The main complexity is that when a tool is made current, its menus
 * are merged onto the main menu bar, and removed when the tool is no
 * longer selected.
 */
class TabDeskView extends DeskView {

    TabDeskView(Desktop desktop) {
        this(desktop, getDefaultBounds());
    }

    TabDeskView(DeskView other) {
        this(other.getDesktop(), other.getBounds());
        //System.err.println("Tab: create from " + other);
        //System.err.println("Tab: create " + other.getTools().length + " tools");

        Tool[] tools = other.getTools();

        // perhaps would be nice to have getTools(Comparator) and have it return a sorted
        // array of tools
        Arrays.sort(tools, new Comparator() {
            public int compare(Object o1, Object o2) {
                Long l1 = new Long(((Tool)o1).getCreationTime());
                Long l2 = new Long(((Tool)o2).getCreationTime());
                return (l1.compareTo(l2));
            }
        });

        for (int i = 0; i < tools.length; i++)
            addTool(tools[i]);

        setVisible(other.isVisible());
    }

    private TabDeskView(Desktop desktop, Rectangle bounds) {
        super(desktop);
        initMainFrame(bounds);
        uif.setDialogParent(mainFrame);
        JDialog.setDefaultLookAndFeelDecorated(false);
    }

    public void dispose() {
        mainFrame.setVisible(false);
        mainFrame.dispose();
        super.dispose();
    }

    public boolean isVisible() {
        return mainFrame.isVisible();
    }

    public void setVisible(boolean v) {
        //System.err.println("Tab: setVisible: " + v);
        if (v == mainFrame.isVisible())
            return;

        mainFrame.setVisible(v);

        if (v) {
            Window[] ww = mainFrame.getOwnedWindows();
            if (ww != null) {
                for (int i = 0; i < ww.length; i++)
                    ww[i].toFront();
            }
        }
    }

    public void addTool(Tool t) {
        DeskView view = t.getDeskView();
        if (view == this)
            return;

        // save info about dialogs before we remove tool from other view
        ToolDialog[] tds = t.getToolDialogs();
        boolean[] vis = new boolean[tds.length];
        for (int i = 0; i < tds.length; i++)
            vis[i] = tds[i].isVisible();

        // remove tool from other view (if any)
        if (view != null)
            view.removeTool(t);

        //System.err.println("Tab: add " + t);
        String tabTitle = getUniqueTabTitle(t.getShortTitle(), null);
        String tabToolTip = t.getTitle();
        contents.addTab(tabTitle, null, t, tabToolTip);
        t.addObserver(listener);
        closeAction.setEnabled(true);

        t.setDeskView(this);

        // update tool dialogs
        for (int i = 0; i < tds.length; i++)
            tds[i].initDialog(this, vis[i]);
    }

    public boolean isEmpty() {
        return (contents.getComponentCount() == 0);
    }

    public Tool[] getTools() {
        Tool[] tools = new Tool[contents.getComponentCount()];
        for (int i = 0; i < tools.length; i++)
            tools[i] = (Tool) (contents.getComponentAt(i));
        return tools;
    }

    public void removeTool(Tool t) {
        t.removeObserver(listener);

        // remove the change listener temporarily because of the
        // broken semantics
        contents.removeChangeListener(listener);

        // remove the tool
        contents.remove(t);
        t.setDeskView(null);

        // update the selection as appropriate
        if (t == selectedTool)
            // set a different tool to be selected
            setSelectedTool((Tool) contents.getSelectedComponent());
        else
            // set the selection again, in case the index has changed
            contents.setSelectedComponent(selectedTool);

        // ensure there is a valid keyboard focus
        KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Component fo = fm.getPermanentFocusOwner();
        if (fo == null || !fo.isShowing()) {
            Container target = (contents.getTabCount() > 0 ? (Container) contents : (Container) mainFrame);
            Container fcr = (target.isFocusCycleRoot() ? target : target.getFocusCycleRootAncestor());
            FocusTraversalPolicy ftp = fcr.getFocusTraversalPolicy();
            Component c = (target.isFocusable() ? target : ftp.getComponentAfter(fcr, target));
            c.requestFocusInWindow();
        }

        // restore the change listener now that the removal has been done
        contents.addChangeListener(listener);

        // update actions
        closeAction.setEnabled(contents.getTabCount() > 0);
    }

    public Tool getSelectedTool() {
        return selectedTool;
    }

    public void setSelectedTool(Tool t) {
        if (t == selectedTool)
            // already selected
            return;

        // hands off the old selected tool (if any)
        if (selectedTool != null) {
            //OLD removeToolMenuItemsFromBasicMenuBar(selectedTool);
            removeToolMenuItemsFromFrameMenuBar(mainFrame, selectedTool);
            selectedTool.removeObserver(listener);
        }

        selectedTool = t;

        // hands on the new selected tool (if any)
        if (selectedTool == null) {
            mainFrame.setTitle(uif.getI18NString("dt.title.txt"));
            CSH.setHelpIDString(contents, null);
        }
        else {
            //OLD addToolMenuItemsToBasicMenuBar(selectedTool);
            addToolMenuItemsToFrameMenuBar(mainFrame, selectedTool);
            selectedTool.addObserver(listener);
            mainFrame.setTitle(uif.getI18NString("dt.title.tool.txt", selectedTool.getTitle()));
            CSH.setHelpIDString(contents, CSH.getHelpIDString(selectedTool));
            contents.setSelectedComponent(selectedTool);
        }

    }

    public int getStyle() {
        return Desktop.TAB_STYLE;
    }

    public JFrame[] getFrames() {
        return new JFrame[] { mainFrame };
    }

    public Rectangle getBounds() {
        return mainFrame.getBounds();
    }

    public boolean isToolOwnerForDialog(Tool tool, Container dialog) {
        for (ToolDialog td: tool.getToolDialogs()) {
            if (td.getDialogParent() == dialog)
                return true;
        }
        return (dialog != null
                && (dialog.getParent() == mainFrame));
    }

    public Window createDialog(Tool tool, String uiKey, String title,
                                  JMenuBar menuBar, Container body,
                                  Rectangle bounds, int type) {
        UIFactory uif = tool.uif;
        if ((type & ToolDialog.FRAME) != 0) {
            JFrame d = uif.createFrame(uiKey, title, body);
            if (menuBar != null)
                d.setJMenuBar(menuBar);

            setBounds(d, bounds);

            return d;
        } else /*if ((type & ToolDialog.DIALOG) != 0)*/ {
            JFrame owner = mainFrame;
            if ((type & ToolDialog.FREE) != 0) {
                owner = null;
            }

            JDialog d = uif.createDialog(uiKey, owner, title, body);

            if ((type & ToolDialog.MODAL) != 0) {
                if ((type & ToolDialog.MODAL_TOOLKIT) == ToolDialog.MODAL_TOOLKIT) {
                    d.setModalityType(ModalityType.TOOLKIT_MODAL);
                } else if ((type & ToolDialog.MODAL_DOCUMENT) == ToolDialog.MODAL_DOCUMENT) {
                    d.setModalityType(ModalityType.DOCUMENT_MODAL);
                } else if ((type & ToolDialog.MODAL_APPLICATION) == ToolDialog.MODAL_APPLICATION) {
                    d.setModalityType(ModalityType.APPLICATION_MODAL);
                } else {
                    d.setModal(true);
                }
            }
            if (menuBar != null)
                d.setJMenuBar(menuBar);

            setBounds(d, bounds);

            return d;
        }
    }

    private void setBounds(Window d, Rectangle bounds) {
        if (bounds == null) {
            d.pack();
            // for some reason the first call of pack seems to yield small results
            // so we need to pack it again to get the real results.  Additional calls
            // seem to have no effect, so after 2 calls we seem to have stable results.
            d.pack();
            d.setLocationRelativeTo(mainFrame);
        } else {
            d.setBounds(bounds);
        }
    }

    public Container createDialog(Tool tool, String uiKey, String title,
                                  JMenuBar menuBar, Container body,
                                  Rectangle bounds) {
        UIFactory uif = tool.uif;
        JDialog d = uif.createDialog(uiKey, mainFrame, title, body);
        if (menuBar != null)
            d.setJMenuBar(menuBar);

        setBounds(d, bounds);

        return d;
    }

    protected void saveDesktop(Map m) {
        saveBounds(mainFrame, new PrefixMap(m, "dt"));
        saveTools(m);
        int sel = contents.getSelectedIndex();
        if (sel >= 0)
            m.put("dt.selected", String.valueOf(sel));
    }

    protected void restoreDesktop(Map m) {
        restoreBounds(mainFrame, new PrefixMap(m, "dt"));
        if (getDesktop().getRestoreOnStart()) {
            restoreTools(m);

            try {
                String s = (String) (m.get("dt.selected"));
                if (s != null) {
                    int sel = Integer.parseInt(s);
                    if (0 <= sel && sel < contents.getTabCount()) {
                        contents.setSelectedIndex(sel);
                    }
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
    }

    // internal

    private void initMainFrame(Rectangle bounds) {
        //System.err.println("Tab: create");
        mainFrame = createFrame(listener, closeAction, "tdi.main");
        //OLD basicMenuBar = mainFrame.getJMenuBar();

        contents = uif.createTabbedPane("tdi.desk");
        contents.setOpaque(true);
        contents.setPreferredSize(new Dimension(bounds.width, bounds.height));

        // this could be a user preference (deck tab placement)
        contents.setTabPlacement(SwingConstants.BOTTOM);
        contents.addChangeListener(listener);
        contents.addAncestorListener(listener);
        mainFrame.setContentPane(contents);
        mainFrame.setBounds(bounds);

        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                getDesktop().checkToolsAndExitIfOK(mainFrame);
            }
        });
    }

    private String getUniqueTabTitle(String base, Component ignoreable) {
        Set s = new HashSet();
        for (int i = 0; i < contents.getTabCount(); i++) {
            if (contents.getComponentAt(i) != ignoreable)
                s.add(contents.getTitleAt(i));
        }

        if (s.contains(base)) {
            for (int i = 0; i <= s.size(); i++) {
                // checking s.size() + 1 cases: at least one must be free
                String v = base + " [" + (i + 2) + "]";
                if (!s.contains(v))
                    return v;
            }
        }

        return base;
    }

    private JFrame mainFrame;
    // OLD private JMenuBar basicMenuBar;
    private JTabbedPane contents;
    private Tool selectedTool;

    private Listener listener = new Listener();

    private Action closeAction = new CloseAction();

    private class CloseAction extends ToolAction {
        CloseAction() {
            super(uif, "tdi.file.close");
            setEnabled(false);  // enabled if/when there are tools
        }

        public void actionPerformed(ActionEvent e) {
            Tool t = (Tool) (contents.getSelectedComponent());
            if (t != null)
                // should never be null because action should be disabled if there
                // are no tabs
                if (getDesktop().isOKToClose(t, mainFrame)) {
                    removeTool(t);
                    t.dispose();
                }
        }
    };

    private class Listener
        implements ActionListener, AncestorListener,
                   ChangeListener, MenuListener,
                   Tool.Observer
    {
        // --------- ActionListener  ---------

        public void actionPerformed(ActionEvent e) {
            JMenuItem mi = (JMenuItem) (e.getSource());
            Object o = mi.getClientProperty(this);
            if (o instanceof Window)
                ((Window) o).toFront();
            else if (o instanceof Tool)
                setSelectedTool((Tool) o);
        }

        // ---------- AncestorListener ----------

        public void ancestorAdded(AncestorEvent event) {
            Tool t = (Tool)(contents.getSelectedComponent());
            if (t != null) {
                // OLD addToolMenuItemsToBasicMenuBar(t);
                addToolMenuItemsToFrameMenuBar(mainFrame, t);
                t.addObserver(this);
                mainFrame.setTitle(uif.getI18NString("dt.title.tool.txt", t.getTitle()));
            }
        }

        public void ancestorMoved(AncestorEvent event) { }

        public void ancestorRemoved(AncestorEvent event) {
            // // update menubar
            // removeToolMenuItemsFromBasicMenuBar();
            // stop observing current tool
            Tool t = (Tool) (contents.getSelectedComponent());
            if (t != null)
                t.removeObserver(this);
            mainFrame.setTitle(uif.getI18NString("dt.title.txt"));
        }

        // ---------- ChangeListener ----------

        public void stateChanged(ChangeEvent e) {
            setSelectedTool((Tool) (contents.getSelectedComponent()));
        }

        // --------- MenuListener ---------

        // note this is for Windows menu only
        public void menuSelected(MenuEvent e) {
            Tool[] tools = getTools();

            JMenu m = (JMenu) (e.getSource());
            m.removeAll();

            /*
            JMenu winOpenMenu = getWindowOpenMenu();
            if (winOpenMenu.getItemCount() > 0) {
                m.add(getWindowOpenMenu());
                m.addSeparator();
            }
            */

            // add entries for all current tools
            if (tools.length == 0) {
                JMenuItem mi = new JMenuItem(uif.getI18NString("dt.windows.noWindows.mit"));
                mi.setEnabled(false);
                m.add(mi);
            }
            else {
                int n = 0;

                // add entries for all current tools
                for (int i = 0; i < tools.length; i++) {
                    Tool tool = tools[i];
                    addMenuItem(m, n++, tool.getTitle(), tool);
                }

                // add entries for any dialogs
                Window[] ownedWindows = mainFrame.getOwnedWindows();
                for (int i = 0; i < ownedWindows.length; i++) {
                    Window w = ownedWindows[i];
                    if (w.isVisible()) {
                        if (w instanceof JDialog)
                            addMenuItem(m, n++, ((JDialog) w).getTitle(), w);
                        if (w instanceof JFrame)
                            addMenuItem(m, n++, ((JFrame) w).getTitle(), w);
                    }
                }
            }
        }

        private void addMenuItem(JMenu m, int n, String s, Object o) {
            JMenuItem mi = new JMenuItem(uif.getI18NString("dt.windows.toolX.mit",
                                                 new Object[] { new Integer(n), s }));
            if (n < 10)
                mi.setMnemonic(Character.forDigit(n, 10));
            mi.addActionListener(this);
            mi.putClientProperty(this, o);
            m.add(mi);
        }

        public void menuDeselected(MenuEvent e) {
            // it's not so much the menu items we want to get rid of, as the
            // client properties on those items
            JMenu m = (JMenu) (e.getSource());
            m.removeAll();
        }

        public void menuCanceled(MenuEvent e) {
            // it's not so much the menu items we want to get rid of, as the
            // client properties on those items
            JMenu m = (JMenu) (e.getSource());
            m.removeAll();
        }

        // ---------- Tool.Observer ----------

        public void shortTitleChanged(Tool src, String newValue) {
            for (int i = 0; i < contents.getTabCount(); i++) {
                if (contents.getComponentAt(i) == src) {
                    String tabTitle = getUniqueTabTitle(newValue, src);
                    contents.setTitleAt(i, tabTitle);
                    break;
                }
            }
        }

        public void titleChanged(Tool src, String newValue) {
            if (src == contents.getSelectedComponent())
                mainFrame.setTitle(uif.getI18NString("dt.title.tool.txt", newValue));

            for (int i = 0; i < contents.getTabCount(); i++) {
                if (contents.getComponentAt(i) == src) {
                    contents.setToolTipTextAt(i, newValue);
                    break;
                }
            }
            //System.err.println("Tool title changed: " + newValue);
        }

        public void toolDisposed(Tool src) { }
    };
}
