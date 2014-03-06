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
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.accessibility.AccessibleContext;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import com.sun.javatest.util.PrefixMap;

//-------------------------------------------------------------------------

    /**
     * A container that presents the current desktop tools in a JDesktopPane.
     */
class MDIDeskView extends DeskView {
    MDIDeskView(Desktop desktop) {
        this(desktop, getDefaultBounds());
    }

    MDIDeskView(DeskView other) {
        this(other.getDesktop(), other.getBounds());
        //System.err.println("MDI: create from " + other);
        //System.err.println("MDI: create " + other.getTools().length + " tools");

        Tool[] tools = other.getTools();
        for (int i = 0; i < tools.length; i++)
            addTool(tools[i]);

        setVisible(other.isVisible());
    }

    private MDIDeskView(Desktop desktop, Rectangle bounds) {
        super(desktop);
        initMainFrame(bounds);
        uif.setDialogParent(mainFrame);
        JDialog.setDefaultLookAndFeelDecorated(true);
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

    public boolean isEmpty() {
        return (internalFrames.size() == 0);
    }

    public Tool[] getTools() {
        Tool[] tools = new Tool[internalFrames.size()];
        return (Tool[]) internalFrames.keySet().toArray(tools);
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

        //System.err.println("MDI.addTool " + t);
        // create resizeable, closable, maximizable, iconifiable frame
        JInternalFrame f = new JInternalFrame(t.getTitle(), true, true, true, true);
        f.setName(t.getShortTitle());

        f.putClientProperty(this, t);
        f.setJMenuBar(t.getMenuBar());
        f.setContentPane(t);
        f.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.addInternalFrameListener(listener);
        f.getAccessibleContext().setAccessibleDescription(t.getToolTipText());

        // copy accessible info to rootpane
        JRootPane root = f.getRootPane();
        root.setName("root");
        AccessibleContext f_ac = f.getAccessibleContext();
        AccessibleContext r_ac = root.getAccessibleContext();
        r_ac.setAccessibleName(f_ac.getAccessibleName());
        r_ac.setAccessibleDescription(f_ac.getAccessibleDescription());

        // center new tool on desktop
        // (doesn't work if desktop not yet shown/sized, sigh)
        // (-perhaps use preferredSize?)
        Dimension dtSize = desktopPane.getSize();
        if (dtSize.width == 0 || dtSize.height == 0)
            dtSize = desktopPane.getPreferredSize();
        Dimension size = f.getSize();
        f.setLocation(Math.max(0, dtSize.width/2 - size.width/2),
                      Math.max(0, dtSize.height/2 - size.height/2) );
        f.setVisible(true);
        internalFrames.put(t, f);

        //System.err.println("MDI.addTool--desktopPane: " + desktopPane);
        //System.err.println("MDI.addTool--internal frame: " + f);

        desktopPane.add(f);
        f.toFront();
        t.addObserver(listener);

        t.setDeskView(this);

        // update tool dialogs
        for (int i = 0; i < tds.length; i++)
            tds[i].initDialog(this, vis[i]);
    }

    public void removeTool(Tool t) {
        //System.err.println("MDI: remove " + t);
        JInternalFrame f = (JInternalFrame) (internalFrames.get(t));
        if (f != null) {
            desktopPane.remove(f);
            internalFrames.remove(t);
        }
        t.removeObserver(listener);
        t.setDeskView(null);

        // ensure there is a valid keyboard focus
        KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Component fo = fm.getPermanentFocusOwner();
        if (fo == null || !fo.isShowing())
            desktopPane.requestFocusInWindow();
    }

    public Tool getSelectedTool() {
        JInternalFrame f = desktopPane.getSelectedFrame(); // NOT VALID FOR DIALOGS!
        if (f == null)
            return null;

        Tool t = (Tool) (f.getClientProperty(this));
        return t;
    }

    public void setSelectedTool(Tool t) {
        JInternalFrame f = (JInternalFrame) (internalFrames.get(t));
        if (f != null) {
            try {
                f.setIcon(false);
                f.setSelected(true);
                desktopPane.moveToFront(f);
            }
            catch (Exception e) { // for java.beans.PropertyVetoException
            }
        }
    }

    public int getStyle() {
        return Desktop.MDI_STYLE;
    }

    public JFrame[] getFrames() {
        return new JFrame[] { mainFrame };
    }

    public Rectangle getBounds() {
        return mainFrame.getBounds();
    }

    public boolean isToolOwnerForDialog(Tool tool, Container dialog) {
        if (dialog == null)
            return false;

        if (useInternalDialogs)
            return (dialog.getParent() == desktopPane);
        else
            return (dialog.getParent() == mainFrame);
    }

    public Container createDialog(Tool tool, String uiKey, String title,
                                  JMenuBar menuBar, Container body,
                                  Rectangle bounds, int type) {
        UIFactory uif = tool.uif;

        if (useInternalDialogs) {
        // create resizeable, closable, non-maximizable, non-iconifiable frame
            JInternalFrame f = new JInternalFrame(title, true, true, false, false);
            f.putClientProperty(this, tool);

            f.setName(uiKey);
            uif.setAccessibleInfo(f, uiKey);
            // copy accessible info to rootpane
            JRootPane root = f.getRootPane();
            root.setName(uiKey + ".root");
            AccessibleContext f_ac = f.getAccessibleContext();
            AccessibleContext r_ac = root.getAccessibleContext();
            r_ac.setAccessibleName(f_ac.getAccessibleName());
            r_ac.setAccessibleDescription(f_ac.getAccessibleDescription());

            if (menuBar != null)
                f.setJMenuBar(menuBar);

            f.setContentPane(body);

            if (bounds == null) {
                f.pack();
                // for some reason the first call of pack seems to yield small results
                // so we need to pack it again to get the real results.  Additional calls
                // seem to have no effect, so after 2 calls we seem to have stable results.
                f.pack();
                Dimension size = f.getSize();
                JInternalFrame tf = (JInternalFrame) (internalFrames.get(tool));
                Rectangle tb = tf.getBounds();
                f.setLocation(Math.max(0, tb.x + (tb.width - size.width) / 2),
                              Math.max(0, tb.y + (tb.height - size.height ) / 2) );
            }
            else
                f.setBounds(bounds);

            // put dialogs above tools on the desktop
            JLayeredPane.putLayer(f, JLayeredPane.DEFAULT_LAYER.intValue() + 1);

            desktopPane.add(f);

            return f;
        }
        else {
            JDialog d = uif.createDialog(uiKey, mainFrame, title, body);
            if (menuBar != null)
                d.setJMenuBar(menuBar);

            if (bounds == null) {
                d.pack();
                d.setLocationRelativeTo(mainFrame);
            }
            else
                d.setBounds(bounds);

            return d;
        }
    }

    protected void saveDesktop(Map m) {
        saveBounds(mainFrame, new PrefixMap(m, "dt"));
        saveTools(m);
    }

    protected void restoreDesktop(Map m) {
        restoreBounds(mainFrame, new PrefixMap(m, "dt"));
        restoreTools(m);
    }

    protected void saveTool(Map m, Tool t) {
        super.saveTool(m, t);
        JInternalFrame f = (JInternalFrame) (internalFrames.get(t));
        saveBounds(f, new PrefixMap(m, "dt"));
    }

    protected Tool restoreTool(Map m, String name) throws Fault, ToolManager.Fault
    {
        Tool t = super.restoreTool(m, name);
        JInternalFrame f = (JInternalFrame) (internalFrames.get(t));
        restoreBounds(f, new PrefixMap(m, "dt"));
        f.setVisible(true);
        return t;
    }

    // internal routines

    private void initMainFrame(Rectangle bounds) {
        //System.err.println("MDI: create");
        mainFrame = createFrame(listener, "mdi.main");
        desktopPane = new JDesktopPane();
        desktopPane.setName("mdi.desk");
        uif.setAccessibleInfo(desktopPane, "mdi.desk");
        desktopPane.setPreferredSize(new Dimension(bounds.width, bounds.height));
        mainFrame.setContentPane(desktopPane);
        mainFrame.setBounds(bounds);

        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                getDesktop().checkToolsAndExitIfOK(mainFrame);
            }
        });
    }

    private void doCascade() {
        Point p = new Point(0, 0);
        JInternalFrame[] frames = getVisibleInternalFrames();
        for (int i = 0; i < frames.length; i++) {
            JInternalFrame f = frames[i];
            f.setLocation(p);
            desktopPane.moveToFront(f); // would be nice to preserve layering if possible
            if (i == frames.length - 1) {
                try {
                    f.setSelected(true);
                    // would be nice to make sure the previously
                    // selected window stays on top
                }
                catch (Exception e) { // for java.beans.PropertyVetoException
                }
            }
            BasicInternalFrameUI ui = (BasicInternalFrameUI) (f.getUI());
            int offset = ui.getNorthPane().getHeight();
            p.x += offset;
            p.y += offset;
        }
    }

    private void doTile() {
        JInternalFrame[] frames = getVisibleInternalFrames();
        int n = frames.length;
        int cols = (int) Math.sqrt(n);
        int rows = (n + cols - 1) / cols;
        Dimension dSize = desktopPane.getSize();
        Dimension tSize = new Dimension(dSize.width / cols, dSize.height / rows);
        for (int i = 0; i < frames.length; i++) {
            JInternalFrame f = frames[i];
            int c = (i % cols);
            int r = (i / cols);
            f.setBounds(c * tSize.width, r * tSize.height, tSize.width, tSize.height);
        }
    }

    private JInternalFrame[] getVisibleInternalFrames() {
        Vector v = new Vector();
        for (int i = 0; i < desktopPane.getComponentCount(); i++) {
            Component c = desktopPane.getComponent(i);
            if (c instanceof JInternalFrame && c.isVisible())
                v.add(c);
        }
        JInternalFrame[] frames = new JInternalFrame[v.size()];
        v.copyInto(frames);
        return frames;
    }

    private JFrame mainFrame;
    private HashMap internalFrames = new HashMap(17);
    private JDesktopPane desktopPane;
    private Listener listener = new Listener();

    private static final String TILE = "tile";
    private static final String CASCADE = "cascade";

    private static final boolean useInternalDialogs = true;

    private class Listener
        implements ActionListener, AncestorListener,
                   InternalFrameListener, MenuListener,
                   Tool.Observer
    {
        // --------- ActionListener  ---------

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(CASCADE))
                doCascade();
            else if (cmd.equals(TILE))
                doTile();
            else {
                JMenuItem mi = (JMenuItem) (e.getSource());
                Object o = mi.getClientProperty(this);
                if (o instanceof Window)
                    ((Window) o).toFront();
                if (o instanceof JInternalFrame)
                    ((JInternalFrame) o).toFront();
                else if (o instanceof Tool)
                    setSelectedTool((Tool) o);
            }
        }

        // --------- AncestorListener ---------

        public void ancestorAdded(AncestorEvent event) {
            Tool[] tools = getTools();
            for (int i = 0; i < tools.length; i++) {
                Tool t = tools[i];
                t.addObserver(listener);
            }
        }

        public void ancestorMoved(AncestorEvent event) { }

        public void ancestorRemoved(AncestorEvent event) {
            Tool[] tools = getTools();
            for (int i = 0; i < tools.length; i++) {
                Tool t = tools[i];
                t.removeObserver(listener);
            }
        }

        // --------- InternalFrameListener ---------

        public void internalFrameClosed(InternalFrameEvent e)  {
            JInternalFrame f = (JInternalFrame) (e.getSource());
            if (f.getContentPane() instanceof Tool) {
                Tool t = (Tool) (f.getContentPane());
                removeTool(t);
                t.removeObserver(listener);
                t.dispose();

                f.getJMenuBar().removeAll();
                f.setContentPane(new Container());
            }
        }

        public void internalFrameClosing(InternalFrameEvent e)  {
            JInternalFrame f = (JInternalFrame) (e.getSource());
            Tool t = (Tool) (f.getContentPane());
            if (getDesktop().isOKToClose(t, mainFrame))
                f.dispose();
        }

        public void internalFrameActivated(InternalFrameEvent e) { }

        public void internalFrameDeactivated(InternalFrameEvent e) { }

        public void internalFrameDeiconified(InternalFrameEvent e)  { }

        public void internalFrameIconified(InternalFrameEvent e)  { }

        public void internalFrameOpened(InternalFrameEvent e)  { }

        // --------- MenuListener ---------

        public void menuSelected(MenuEvent e) {
            Tool[] tools = getTools();

            JMenu m = (JMenu) (e.getSource());
            m.removeAll();
            JMenuItem tmi = uif.createMenuItem("dt.windows", TILE, this);
            tmi.setEnabled(tools.length > 0);
            m.add(tmi);
            JMenuItem cmi = uif.createMenuItem("dt.windows", CASCADE, this);
            cmi.setEnabled(tools.length > 0);
            m.add(cmi);

            /*
            JMenu winOpenMenu = getWindowOpenMenu();
            if (winOpenMenu.getItemCount() > 0) {
                m.addSeparator();
                m.add(getWindowOpenMenu());
            }
            */

            if (tools.length > 0)
                m.addSeparator();

            int n = 0;

            // add entries for all current tools
            for (int i = 0; i < tools.length; i++) {
                Tool tool = tools[i];
                addMenuItem(m, n++, tool.getTitle(), tool);
            }

            // add entries for all internal dialogs,
            // which are internal frames not containing tools
            JInternalFrame[] frames = desktopPane.getAllFrames();
            for (int i = 0; i < frames.length; i++) {
                JInternalFrame f = frames[i];
                if (f.isVisible() && !(f.getContentPane() instanceof Tool) )
                    addMenuItem(m, n++, f.getTitle(), f);
            }

            // add entries for any external dialogs
            Window[] ownedWindows = mainFrame.getOwnedWindows();
            for (int i = 0; i < ownedWindows.length; i++) {
                Window w = ownedWindows[i];
                if (w instanceof JDialog && w.isVisible())
                    addMenuItem(m, n++, ((JDialog) w).getTitle(), w);
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
        }

        public void menuCanceled(MenuEvent e) {
        }

        // --------- Tool.Observer ---------

        public void shortTitleChanged(Tool src, String newValue) {
        }

        public void titleChanged(Tool src, String newValue) {
            JInternalFrame f = (JInternalFrame) (internalFrames.get(src));
            f.setTitle(newValue);
            //System.err.println("Tool title changed: " + newValue);
        }

        public void toolDisposed(Tool src) { }

    }
}
