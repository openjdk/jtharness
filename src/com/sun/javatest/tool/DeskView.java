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
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.help.HelpBroker;
import javax.accessibility.AccessibleContext;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import com.sun.javatest.util.ExitCount;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.PrefixMap;
import com.sun.javatest.util.StringArray;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

/**
 * A deskview defines the abstract behavior of a style of desktop,
 * such as MDI, SDI, or tabbed.
 * @see Desktop
 */
abstract class DeskView {
    /**
     * This exception is used to report problems while using a tool manager.
     */
    static class Fault extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        Fault(I18NResourceBundle i18n, String s, Object[] o) {
            super(i18n.getString(s, o));
        }
    }

    protected DeskView(Desktop desktop) {
        this.desktop = desktop;
        uif = new UIFactory(getClass(), desktop.getHelpBroker());
    }

    public Desktop getDesktop() {
        return desktop;
    }

    /**
     * Dispose of this desktop object, and resources it may use.
     * Subsequent call of access() will cause a new desktop object
     * to be created.
     * @see #access
     */
    public void dispose() {
        Tool[] tools = getTools();
        for (int i = 0; i < tools.length; i++)
            tools[i].dispose();

        /*
        if (this == theOne) {
            theOne = null;
            if (helpBroker != null && helpBroker.isDisplayed()) {
                helpBroker.setDisplayed(false);
            }
        }
        */
    }

    /**
     * Check if the top level windows of the desktop are visible or not.
     * @return true if the top level windows are visible; otherwise, return false
     * @see #setVisible
     */
    public abstract boolean isVisible();

    /**
     * Set whether or not the top level windows of the desktop should be visible.
     * @param v If true, the top level windows will be made visible; if false, they
     * will be hidden.
     */
    public abstract void setVisible(boolean v);

    /**
     * Check whether the desktop is empty of any tools.
     * @return true if there are no tools on the desktop, and false otherwise
     */
    public abstract boolean isEmpty();

    /**
     * Get the set of tools currently on the desktop.
     * @return the set of tools currently on the desktop
     */
    public abstract Tool[] getTools();

    /**
     * Add a new tool to the desktop.
     * @param t the tool to be added
     * @see #removeTool
     */
    public abstract void addTool(Tool t);

    /**
     * Remove a tool from the desktop.
     * @param t the tool to be removed
     * @see #addTool
     */
    public abstract void removeTool(Tool t);

    /**
     * Get the currently selected tool on the desktop.
     * @return the currently selected tool on the desktop
     * @see #setSelectedTool
     */
    public abstract Tool getSelectedTool();

    /**
     * Set the currently selected tool on the desktop.
     * @param t the the tool to be selected on the desktop
     * @see #getSelectedTool
     */
    public abstract void setSelectedTool(Tool t);

    //--------------------------------------------------------------------------

    /**
     * Get an integer signifying the style of the current desktop.
     * @return an integer signifying the style of the current desktop
     * @see #TAB_STYLE
     * @see #MDI_STYLE
     * @see #SDI_STYLE
     * @see #setStyle
     */
    public abstract int getStyle();

    /**
     * Get the top level frames that make up this desktop. TAB and MDI style
     * desktops just have a single frame; An SDI style desktop may have more
     * than one frame.
     * @return the top level frames of this desktop
     */
    public abstract JFrame[] getFrames();

    /**
     * Get a parent component for a dialog to use.
     * @return a component which can be used as a parent, or null if none
     *         is available.
     */
    public Component getDialogParent() {
        JFrame[] frames = getFrames();
        if (frames == null || frames.length == 0)
            return null;
        return frames[0].getContentPane();
    }

    /**
     * Get the bounds for this desktop.
     * @return a rectangle describing the size and position of this desktop
     * on the screen.
     */
    public abstract Rectangle getBounds();

    /**
     * Get thedefault bounds for a JT Harness desktop.
     * @return a rectangle describing the size and position on the screen
     * of the default desktop.
     */
    public static Rectangle getDefaultBounds() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle mwb = ge.getMaximumWindowBounds();
        int w = Math.min(mwb.width, Math.max(640, mwb.width * 3 / 4));
        int h = Math.min(mwb.height, Math.max(480, mwb.height * 3 / 4));

        int x = ge.getCenterPoint().x - w/2;
        int y = ge.getCenterPoint().y - h/2;

        return new Rectangle(x, y, w, h);
    }

    /**
     * Create a new top level frame for the desktop. The frame will have the
     * standard JT Harness menu bar.
     * @param winMenuListener a listener to be invoked when the user clicks on
     * the standard Windows menu
     * @param uiKey key to use for retrieving accessibility info for the frame
     * @return a new top level frame for the desktop
     */
    protected JFrame createFrame(MenuListener winMenuListener, String uiKey) {
        return createFrame(winMenuListener, null, uiKey);
    }

    /**
     * Create a new top level frame for the desktop. The frame will have the
     * standard JT Harness menu bar.
     * @param winMenuListener a listener to be invoked when the user clicks on
     * the standard Windows menu
     * @param fileCloseAction an action to use to create the File>Close menu item
     * to close the frame
     * @param uiKey key to use for retrieving accessibility info for the frame
     * @return a new top level frame for the desktop
     */
    protected JFrame createFrame(MenuListener winMenuListener, Action fileCloseAction,
                                 String uiKey) {
        JFrame frame = new JFrame();
        frame.setName(uiKey + ":" + (frameIndex++));
        frame.setTitle(uif.getI18NString("dt.title.txt"));
        frame.setIconImage(uif.createImage("images/jticon.gif"));
        uif.setAccessibleInfo(frame, uiKey);

        JRootPane root = frame.getRootPane();
        root.setName("root");
        uif.setAccessibleInfo(root, uiKey); // same as for frame, deliberate

        //System.err.println("DT: createFrame");

        JMenuBar mb = uif.createMenuBar("dt.menuBar");
        JMenu fileMenu = uif.createMenu("dt.file");
        // fileMenu is populated dynamically by FileMenuListener
        fileMenu.addMenuListener(new FileMenuListener(fileCloseAction));
        mb.add(fileMenu);

        JMenu toolMenu = uif.createMenu("dt.tasks");
        // could dynamically create this list when menu invoked
        ToolManager[] mgrs = desktop.getToolManagers();
        for (int i = 0; i < mgrs.length; i++) {
            //Action[] actions = mgrs[i].getTaskMenuActions();
            Action[] actions = mgrs[i].getWindowOpenMenuActions();  // method name is out of date
            if (actions != null) {
                for (int j = 0; j < actions.length; j++)
                    toolMenu.add(actions[j]);
            }
        }
        mb.add(toolMenu);

        JMenu winMenu = uif.createMenu("dt.windows");
        // winMenu is populated dynamically by WinMenuListener
        winMenu.addMenuListener(winMenuListener);
        mb.add(winMenu);

        mb.add(uif.createHorizontalGlue("dt.pad"));

        JMenu helpMenu = new HelpMenu(frame, desktop, uif);
        mb.add(helpMenu);

        frame.setJMenuBar(mb);

        HelpBroker helpBroker = desktop.getHelpBroker();
        if (helpBroker != null) {
            helpBroker.enableHelpKey(frame.getRootPane(), "jthelp.csh", null);
            Desktop.addHelpDebugListener(frame);
        }

        Desktop.addPreferredSizeDebugListener(frame);

        if (focusMonitor != null)
            focusMonitor.monitor(frame);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                // WARNING: this event may be called more than once
                // so only do post-processing the first time it is called
                // for this window
                final JFrame frame = (JFrame) (e.getSource());
                //System.err.println("DT: closed " + frame.getTitle());
                synchronized (allFrames) {
                    if (allFrames.remove(frame) && allFrames.isEmpty()) {
                        // defer until outstanding events are processed
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                ExitCount.dec();
                            }
                        });
                    }
                }
            }
        });

        synchronized (allFrames) {
            allFrames.add(frame);
            if (allFrames.size() == 1)
                ExitCount.inc();
        }

        //System.err.println("DT: createFrame done");

        return frame;
    }

    /**
     * Add tool menu items from the given tool into the given frame's
     * tool menu.
     * @param frame the frame to modify
     * @param tool the tool to get the menu items from
     */
    protected void addToolMenuItemsToFrameMenuBar(JFrame frame, Tool tool) {
        JMenuBar frameMenuBar = frame.getJMenuBar();
        Tool curr = (Tool)(frameMenuBar.getClientProperty(getClass()));
        if (tool == curr)
            return;
        else if (curr != null)
            removeToolMenuItemsFromFrameMenuBar(frame, curr);

        JMenuBar toolMenuBar = tool.getMenuBar();
        if (toolMenuBar == null)
            return;

        for (int i = 0; i < toolMenuBar.getMenuCount(); i++) {
            JMenu toolMenu = toolMenuBar.getMenu(i);
            if (toolMenu == null)
                // null if i'th item not a menu, e.g. glue
                continue;

            int toolMenuSize = toolMenu.getMenuComponentCount();
            JMenu frameMenu = findMenu(frameMenuBar, toolMenu.getText());
            if (frameMenu == null) {
                frameMenu = new JMenu(toolMenu.getText());
                frameMenu.setName(toolMenu.getName());
                frameMenu.setMnemonic(toolMenu.getMnemonic());
                AccessibleContext bc = frameMenu.getAccessibleContext();
                AccessibleContext tc = toolMenu.getAccessibleContext();
                bc.setAccessibleName(tc.getAccessibleName());
                bc.setAccessibleDescription(tc.getAccessibleDescription());
                copyMenuListeners(toolMenu, frameMenu);
                for (int j = 0; j < toolMenuSize; j++) {
                    frameMenu.add(toolMenu.getMenuComponent(0));
                }
                frameMenuBar.add(frameMenu, frameMenuBar.getMenuCount() - MENU_INSERT_POINT);
            }
            else {
                for (int j = 0; j < toolMenuSize; j++) {
                    frameMenu.add(toolMenu.getMenuComponent(0), j);
                }
                frameMenu.insertSeparator(toolMenuSize);
            }
            frameMenu.putClientProperty(getClass(), new Integer(toolMenuSize));
        }   // for
        frameMenuBar.putClientProperty(getClass(), tool);
    }

    private void removeToolMenuItemsFromFrameMenuBar(JFrame frame) {
        JMenuBar frameMenuBar = frame.getJMenuBar();
        Tool tool = (Tool)(frameMenuBar.getClientProperty(getClass()));
        if (tool == null)
            return;
        removeToolMenuItemsFromFrameMenuBar(frame, tool);
    }

    /**
     * Remove the tool menu items for the given tool from the given frame's
     * tool menu.
     * @param frame the frame with the menu bar to which the tool's menus have
     * been added
     * @param tool the tool to which to return the menu items
     */
    protected void removeToolMenuItemsFromFrameMenuBar(JFrame frame, Tool tool) {
        JMenuBar frameMenuBar = frame.getJMenuBar();
        JMenuBar toolMenuBar = tool.getMenuBar();
        for (int i = 0; i < toolMenuBar.getMenuCount(); i++) {
            JMenu toolMenu = toolMenuBar.getMenu(i);
            if (toolMenu == null)
                // null if i'th item not a menu, e.g. glue
                continue;

            JMenu frameMenu = findMenu(frameMenuBar, toolMenu.getText());
            int toolMenuSize = ((Integer)(frameMenu.getClientProperty(getClass()))).intValue();
            for (int j = 0; j < toolMenuSize; j++) {
                toolMenu.add(frameMenu.getMenuComponent(0));
            }
            if (frameMenu.getItemCount() == 0)
                frameMenuBar.remove(frameMenu);
            else {
                frameMenu.remove(0); // separator
                frameMenu.putClientProperty(getClass(), null);
            }
        }

        frameMenuBar.putClientProperty(getClass(), null);
    }

    protected JMenu getWindowOpenMenu() {
        JMenu menu = uif.createMenu("dt.windows.open");
        // could dynamically create this list when menu invoked
        ToolManager[] mgrs = desktop.getToolManagers();
        for (int i = 0; i < mgrs.length; i++) {
            Action[] actions = mgrs[i].getWindowOpenMenuActions();
            if (actions != null) {
                for (int j = 0; j < actions.length; j++)
                    menu.add(actions[j]);
            }
        }
        return menu;
    }

    private void copyMenuListeners(JMenu src, JMenu dst) {
        MenuListener[] ll = (MenuListener[]) (src.getListeners(MenuListener.class));
        for (int i = 0; i < ll.length; i++)
            dst.addMenuListener(ll[i]);
    }

    private JMenu findMenu(JMenuBar mb, String text) {
        for (int i = 0; i < mb.getMenuCount(); i++) {
            JMenu m = mb.getMenu(i);
            if (m != null && m.getText().equals(text))
                return m;
        }
        return null;
    }

    private static final int MENU_INSERT_POINT = 4; // before Windows, glue and Help

    /**
     * Create a dialog.
     * @param tool the parent tool for the dialog
     * @param uiKey a string which is to be used as the base name for any
     * resources that may be required
     * @param title the title for the dialog
     * @param menuBar the menu bar for the dialog
     * @param body the body component for the dialog
     * @param bounds the size and position for the dialog
     * @return a JDialog or JInternalDialog built from the supplied values.
     */
    public abstract Container createDialog(Tool tool, String uiKey, String title,
                                           JMenuBar menuBar, Container body,
                                           Rectangle bounds, int type);

    /**
     * Check if the tool's parent Window is the owner of a dialog.
     * This may become false if the desktop style is changed after the dialog
     * was created.
     * @param tool the tool from which to determine the parent Window
     * @param dialog the dialog to be checked
     * @return true if the tool's parent Window is the owner of the dialog, and
     * false otherwise.
     */
    public abstract boolean isToolOwnerForDialog(Tool tool, Container dialog);

    /**
     * Handle the File>Preferences menu: show a Preferences window.
     */
    private void doPrefs(JFrame parent) {
        desktop.showPreferences(parent);
    }

    /**
     * Save the state of the desktop to a map object.
     * @param m The map object to which to write the state of the desktop
     * @see #restoreDesktop
     */
    protected abstract void saveDesktop(Map m);

    /**
     * Save the state of the current tools on the desktop to a map object.
     * @param m The map object to which to write the state of the desktop
     * @see #restoreDesktop
     */
    protected void saveTools(Map m) {
        Tool[] tools = getTools();
        m.put("tool.count", String.valueOf(tools.length));
        for (int i = 0; i < tools.length; i++)
            saveTool(new PrefixMap(m, "tool." + String.valueOf(i)), tools[i]);
    }

    /**
     * Save information about a tool on the desktop.
     * @param m A map object in which to save the information
     * @param t the tool to be saved
     * @see #restoreTool
     */
    protected void saveTool(Map m, Tool t) {
        m.put("mgr", t.getManager().getClass().getName());
        m.put("class", t.getClass().getName());
        m.put("selected", String.valueOf(t == getSelectedTool()));
        t.save(m);
    }

    /**
     * Restore the state of the desktop from a map object.
     * @param m The map object from which to restore the state of the desktop
     * @see #saveDesktop
     */
    protected abstract void restoreDesktop(Map m);

    /**
     * Restore the state of the saved tools from a map object.
     * @param m The map object from which to restore the state of the saved tools
     * @see #saveTools
     */
    protected void restoreTools(Map m) {
        try {
            String c = (String) (m.get("tool.count"));
            if (c != null) {
                int count = Integer.parseInt(c);
                for (int i = 0; i < count; i++) {
                    try {
                        String prefix = "tool." + i;
                        Map toolMap = new PrefixMap(m, prefix);
                        restoreTool(toolMap, prefix);
                    }
                    catch (ToolManager.Fault e) {
                        uif.showError("dv.restore.cantRestoreTool",
                                      new Object[] { new Integer(i), e.getMessage() });
                    }
                    catch (Fault e) {
                        uif.showError("dv.restore.cantRestoreTool",
                                      new Object[] { new Integer(i), e.getMessage() });
                    }
                    catch (Throwable e) {
                        uif.showError("dv.restore.cantRestoreTool",
                                      new Object[] { new Integer(i), e.toString() });
                        I18NResourceBundle i18n = uif.getI18NResourceBundle();
                        desktop.log(i18n, "dv.restore.cantRestoreTool",
                                    new Object[] { e, new Integer(i) });
                    }
                }
            }
        }
        catch (NumberFormatException ignore) {
            // ignore, for now
        }

    }

    /**
     * Restore a tool on the desktop from information in a map object.
     * @param m The map containing the information about a tool
     * @return the tool that was restored from the specified information in the map
     * @see #saveTool
     * @throws ClassNotFoundException if the class for the tool cannot be found
     * @throws NoSuchMethodException if the tool does not have the appropriate restore method
     * @throws IllegalAccessException if the tool's restore method does not have public access
     * @throws InvocationTargetException if the tool's restore method threw an exception
     */
    protected Tool restoreTool(Map m, String name) throws Fault, ToolManager.Fault
    {
        String mgrClassName = (String) (m.get("mgr"));
        if (mgrClassName == null) {
            // backwards compatibility with 3.1
            String toolClassName = (String) (m.get("class"));
            if (toolClassName != null && toolClassName.endsWith("Tool")) {
                String n = toolClassName.substring(0, toolClassName.length()) + "Manager";
                try {
                    if (Class.forName(n) != null)
                        mgrClassName = n;
                }
                catch (Throwable e) {
                    // ignore
                }
            }

            if (mgrClassName == null)
                throw new Fault(i18n, "dv.restore.noMgrClass", name);
        }

        ToolManager mgr = desktop.getToolManager(mgrClassName);
        if (mgr == null)
            throw new Fault(i18n, "dv.restore.noMgr",
                            new Object[] { name, mgrClassName } );

        Tool t = mgr.restoreTool(m);

        addTool(t);
        boolean selected = "true".equals(m.get("selected"));
        if (selected)
            setSelectedTool(t);

        return t;
    }

    /**
     * Save the screen size and position for a component in a map object.
     * @param c the component whose size and position are to be recorded in the map.
     * @param m the map in which the information is to be recorded
     * @see #restoreBounds
     */
    protected static void saveBounds(Component c, Map m) {
        Rectangle r = c.getBounds();
        m.put("x", String.valueOf(r.x));
        m.put("y", String.valueOf(r.y));
        m.put("w", String.valueOf(r.width));
        m.put("h", String.valueOf(r.height));
    }


    /**
     * Restore the screen size and position for a component from information
     * in a map object.
     * @param c the component whose size and position are to be restored.
     * @param m the map in which the information is to be recorded
     * @see #saveBounds
     */
    protected static void restoreBounds(Component c, Map m) {
        try {
            String xs = (String) (m.get("x"));
            String ys = (String) (m.get("y"));
            String ws = (String) (m.get("w"));
            String hs = (String) (m.get("h"));
            if (xs != null && ys != null && ws != null && hs != null) {
                Rectangle restored = new Rectangle(Integer.parseInt(xs),
                        Integer.parseInt(ys),
                        Integer.parseInt(ws),
                        Integer.parseInt(hs));

                restored = getScreenBounds().intersection(restored);
                if (!restored.isEmpty()) {
                    c.setBounds(restored);
                }

            }
        }
        catch (NumberFormatException e) {
            // ignore
        }
    }

    public static Rectangle getScreenBounds() {
        // Consider multi-display environment
        Rectangle result = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (int j = 0; j < gs.length; j++) {
            GraphicsDevice gd = gs[j];
            GraphicsConfiguration[] gc = gd.getConfigurations();
            for (int i = 0; i < gc.length; i++) {
                result = result.union(gc[i].getBounds());
            }
        }

        return result;
    }


    // keep a private list of all the outstanding frames, so that
    // when the collection becomes empty, we can lower the ExitCount.
    private static Collection allFrames = new Vector();

    // counter for unique name generation
    private static int frameIndex;

    private Desktop desktop;

    /**
     * The UI factory used to create GUI components.
     */
    protected final UIFactory uif;

    private static FocusMonitor focusMonitor;

    static {
        String opts = System.getProperty("javatest.focus.monitor");
        if (opts != null) {
            focusMonitor = FocusMonitor.access();
            if (!opts.equals("true")) // support old value for back compat
                focusMonitor.setOptions(StringArray.split(opts));
            focusMonitor.setActivateKey("alt 2");
            focusMonitor.setReportKey("shift alt 2");
            focusMonitor.setReportFile(System.getProperty("javatest.focus.monitor.log"));
        }
    };

            static final String CLOSE = "close"; // visible for file close listeners
    private static final String EXIT = "exit";
    private static final String PREFS = "prefs";
    private static final String HISTORY = "history";
    private static final String SEPARATOR = null;

    private static final I18NResourceBundle i18n =
        I18NResourceBundle.getBundleForClass(DeskView.class);


    //-------------------------------------------------------------------------

    private class FileMenuListener implements MenuListener, ActionListener {
        FileMenuListener(Action closeAction) {

            prefs = uif.createMenuItem("dt.file", PREFS, this);
            if (closeAction != null)
                close = uif.createMenuItem(closeAction);
            exit = uif.createMenuItem("dt.file", EXIT, this);
            // additional entries are created dynamically
        }

        public void menuSelected(MenuEvent e) {
            //System.err.println("DT:FileMenu: selected " + e);
            JMenu m = (JMenu) (e.getSource());
            m.removeAll();

            // this code is not ideal, and essentially works (for now) because
            // only one tool manager provides file menu actions: ie. ExecManager.
            // If that gets changed, we'll want to consider sorting/grouping
            // the actions somehow.
            ToolManager[] mgrs = desktop.getToolManagers();
            for (int i = 0; i < mgrs.length; i++) {
                Action[] fma = mgrs[i].getFileMenuActions();
                if (fma != null) {
                    for (int j = 0; j < fma.length; j++)
                        m.add(new JMenuItem(fma[j]));
                }

                JMenuItem[] jmi = mgrs[i].getFileMenuPrimaries();
                if (jmi != null) {
                    for (int j = 0; j < jmi.length; j++)
                        m.add(jmi[j]);
                }

            }   // for

            // add secondary items before prefs, close, exit
            for (int i = 0; i < mgrs.length; i++) {
                JMenuItem[] jmi = mgrs[i].getFileMenuSecondaries();
                if (jmi != null) {
                    for (int j = 0; j < jmi.length; j++)
                        m.add(jmi[j]);
                    m.addSeparator();
                }

            }   // for

            List fileHistory = desktop.getFileHistory();
//          if (!fileHistory.isEmpty()) {
        JMenu hm = uif.createMenu("dt.file.recentwd");
        if (!fileHistory.isEmpty()) {
                int n = 0;

                for (Iterator i = fileHistory.iterator(); i.hasNext(); ) {
                    Desktop.FileHistoryEntry h = (Desktop.FileHistoryEntry) (i.next());
                    if (!h.file.exists())
                        continue;
                    String s = uif.getI18NString("dt.file.historyX.mit",
                                             new Object[] {new Integer(n), h.file.getPath()});
                    JMenuItem mi = new JMenuItem(s);
                    mi.setActionCommand(HISTORY);
                    mi.addActionListener(this);
                    mi.putClientProperty(this, h);
                    if (n < 10)
                        mi.setMnemonic(Character.forDigit(n, 10));
                    n++;
                    hm.add(mi);
                }
        } else {
                JMenuItem noEntries = new JMenuItem(i18n.getString("fh.empty"));
                noEntries.setEnabled(false);
                hm.add(noEntries);

        }
        m.add(hm);
                m.addSeparator();

            m.add(prefs);
            m.addSeparator();
            if (close != null)
                m.add(close);
            m.add(exit);
        }

        public void menuDeselected(MenuEvent e) {
            JMenu m = (JMenu) (e.getSource());
            m.removeAll();
        }

        public void menuCanceled(MenuEvent e) {
            JMenu m = (JMenu) (e.getSource());
            m.removeAll();
        }

        public void actionPerformed(ActionEvent e) {
            //System.err.println("DT:FileMenu: action " + e);
            Component src = (Component) (e.getSource());
            JFrame parent = (JFrame) (SwingUtilities.getAncestorOfClass(JFrame.class, src));
            if (parent == null) // only happens during testing when invoking menuitem directly,
                parent = getFrames()[0];

            String cmd = e.getActionCommand();
            if (cmd.equals(PREFS)) {
                doPrefs(parent);
            }
            else if (cmd.equals(HISTORY)) {
                JMenuItem mi = (JMenuItem) (e.getSource());
                Desktop.FileHistoryEntry h = (Desktop.FileHistoryEntry) (mi.getClientProperty(this));
                try {
                    h.fileOpener.open(h.file);
                }
                catch (FileNotFoundException ex) {
                    uif.showError("dt.file.cannotFind", h.file);
                }
                catch (FileOpener.Fault ex) {
                    uif.showError("dt.file.cannotOpen",
                                  new Object[] { h.file, ex.getMessage() });
                }
            }
            else if (cmd.equals(EXIT)) {
                desktop.checkToolsAndExitIfOK(parent);
            }
        }

        private JMenu fileOpenMenu;
        private JMenuItem prefs;
        private JMenuItem close;
        private JMenuItem exit;
    }


}
