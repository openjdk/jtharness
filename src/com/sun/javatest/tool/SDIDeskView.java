/*
 * $Id$
 *
 * Copyright (c) 2004, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.PrefixMap;

class SDIDeskView extends DeskView {

    SDIDeskView(Desktop desktop) {
        super(desktop);
        initFrames();
        uif.setDialogParent(console);
        JDialog.setDefaultLookAndFeelDecorated(false);
    }


    SDIDeskView(DeskView other) {
        this(other.getDesktop());

        Tool[] tools = other.getTools();
        for (int i = 0; i < tools.length; i++)
            addTool(tools[i]);

        doCascade();
        setVisible(other.isVisible());
    }

    public void dispose() {
        for (int i = 0; i < frames.length; i++)
            frames[i].setVisible(false);

        for (int i = 0; i < frames.length; i++)
            frames[i].dispose();

        super.dispose();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean v) {
        //System.err.println("SDI: setVisible: " + v);
        if (v != visible) {
            for (int i = 0; i < frames.length; i++) {
                JFrame f = frames[i];
                f.setVisible(v);
                if (v) {
                    // make sure we don't make frames visible in front of dialogs
                    // (you'd think JDK would do this...)
                    Window[] ww = f.getOwnedWindows();
                    for (int wwi = 0; wwi < ww.length; wwi++)
                        ww[wwi].toFront();
                }
            }
            visible = v;
        }
    }

    public boolean isEmpty() {
        return (frames.length == 1);
    }

    public Tool[] getTools() {
        // frames[0] is the console
        Tool[] tools = new Tool[frames.length - 1];
        for (int i = 0; i < tools.length; i++)
            tools[i] = (Tool) (frames[i + 1].getContentPane());
        return tools;
    }

    public void addTool(final Tool t) {
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

        //System.err.println("SDI: add " + t);
        Action closeAction = (new ToolAction(uif, "sdi.file.close") {
            public void actionPerformed(ActionEvent e) {
                removeTool(t);
                t.dispose();
            }
        });
        JFrame f = createFrame(listener, closeAction, "sdi.tool");
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addToolMenuItemsToFrameMenuBar(f, t);
        f.setTitle(uif.getI18NString("dt.title.tool.txt", t.getTitle()));
        f.setContentPane(t);
        f.pack();
        t.addObserver(listener);
        f.addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                JFrame f = (JFrame) (e.getSource());
                if (f.getContentPane() instanceof Tool) {
                    Tool t = (Tool) (f.getContentPane());
                    selectedTool = t;
                }
            }

            public void windowDeactivated(WindowEvent e) {
                selectedTool = null;
            }

            public void windowClosing(WindowEvent e) {
                JFrame f = (JFrame) (e.getSource());
                if (f.getContentPane() instanceof Tool) {
                    Tool t = (Tool) (f.getContentPane());
                    if (getDesktop().isOKToClose(t, f))
                        f.dispose();
                }
            }

            public void windowClosed(WindowEvent e) {
                JFrame f = (JFrame) (e.getSource());
                //removeToolMenuItemsFromFrameMenuBar(f);
                frames = (JFrame[]) (DynamicArray.remove(frames, f));

                if (f.getContentPane() instanceof Tool) {
                    Tool t = (Tool) (f.getContentPane());
                    t.removeObserver(listener);
                    t.dispose();

                    //f.dispose();
                    f.getRootPane().removeAll();

                    if (t == selectedTool)
                        selectedTool = null;
                }
            }
        });

        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = f.getSize();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        f.setLocation(ge.getCenterPoint().x - size.width/2,
                      ge.getCenterPoint().y - size.height/2);
        //f.setLocation(screenSize.width/2 - size.width/2, screenSize.height/2 - size.height/2);
        f.setVisible(visible);

        frames = (JFrame[]) (DynamicArray.append(frames, f));

        t.setDeskView(this);

        // update tool dialogs
        for (int i = 0; i < tds.length; i++)
            tds[i].initDialog(this, vis[i]);
    }

    public void removeTool(Tool t) {
        //System.err.println("SDI: remove " + t);
        JFrame f = getFrameForTool(t);
        frames = (JFrame[]) (DynamicArray.remove(frames, f));    // remove it from list of frames
        f.setVisible(false);                          // hide it before we start disassembling it

        removeToolMenuItemsFromFrameMenuBar(f, t);
        f.setContentPane(new Container()); // avoid the tool getting disposed along with the frame
        f.dispose();
        // other housekeeping done in window listener to ensure
        // that it executes however the tool is closed
        t.setDeskView(null);
    }

    public Tool getSelectedTool() {
        //System.err.println("SDI: selected tool " + selectedTool);
        return selectedTool;
    }

    public void setSelectedTool(Tool t) {
        JFrame f = getFrameForTool(t);
        if (f.getState() == JFrame.ICONIFIED)
            f.setState(JFrame.NORMAL);
        //f.toFront();
        f.setVisible(visible);
        // selectedTool set when the window is activated -- there appears to
        // be no way to query directly which window has focus
    }

    public int getStyle() {
        return Desktop.SDI_STYLE;
    }

    public JFrame[] getFrames() {
        return frames;
    }

    public Rectangle getBounds() {
        Rectangle bounds = null;
        // start loop at 0 to include console, start at 1 to ignore it
        for (int i = 1; i < frames.length; i++) {
            Rectangle r = frames[i].getBounds();
            bounds = (bounds == null ? r : bounds.union(r));
        }
        return (bounds == null ? getDefaultBounds() : bounds);
    }

    public boolean isToolOwnerForDialog(Tool tool, Container dialog) {
        if (dialog == null)
            return false;

        JFrame f = (JFrame) (SwingUtilities.getAncestorOfClass(JFrame.class, tool));
        return (dialog.getParent() == f);
    }

    public Container createDialog(Tool tool, String uiKey, String title,
                                  JMenuBar menuBar, Container body,
                                  Rectangle bounds, int type) {
        JFrame f = (JFrame) (SwingUtilities.getAncestorOfClass(JFrame.class, tool));

        UIFactory uif = tool.uif;
        JDialog d = uif.createDialog(uiKey, f, title, body);
        if (menuBar != null)
            d.setJMenuBar(menuBar);

        if (bounds == null) {
            d.pack();
            // for some reason the first call of pack seems to yield small results
            // so we need to pack it again to get the real results.  Additional calls
            // seem to have no effect, so after 2 calls we seem to have stable results.
            d.pack();
            d.setLocationRelativeTo(f);
        }
        else
            d.setBounds(bounds);

        return d;
    }

    protected void saveDesktop(Map m) {
        saveBounds(console, new PrefixMap(m, "dt.console"));
        saveTools(m);
    }

    protected void restoreDesktop(Map m) {
        restoreBounds(console, new PrefixMap(m, "dt.console"));
        restoreTools(m);
    }

    protected void saveTool(Map m, Tool t) {
        super.saveTool(m, t);
        saveBounds(getFrameForTool(t), new PrefixMap(m, "dt"));
    }

    protected Tool restoreTool(Map m, String name) throws Fault, ToolManager.Fault
    {
        Tool t = super.restoreTool(m, name);
        restoreBounds(getFrameForTool(t), new PrefixMap(m, "dt"));
        return t;
    }

    private void initFrames() {
        console = createFrame(listener, "sdi.console");
        console.setTitle(uif.getI18NString("dt.title.console"));
        console.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                getDesktop().checkToolsAndExitIfOK(console);
            }
        });
        JLabel logo = new JLabel(getDesktop().getLogo());
        logo.setName("sdi.console");
        logo.setFocusable(false);
        logo.setOpaque(true);
        console.setContentPane(logo);
        console.pack();
        frames = new JFrame[] { console };
    }

    private void doCascade() {
        final int offset = 30;
        // first find width of cascaded set
        int maxWidth = 0;
        int maxHeight = 0;
        // start at 0 to include console, start at 1 for just tools
        for (int i = 1; i < frames.length; i++) {
            JFrame f = frames[i];
            Dimension fSize = f.getSize();
            maxWidth = Math.max(maxWidth, i * offset + fSize.width);
            maxHeight = Math.max(maxHeight, i * offset + fSize.height);
        }
        //Dimension dSize = Toolkit.getDefaultToolkit().getScreenSize();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        int offsetX = (ge.getCenterPoint().x - maxWidth/2);
        int offsetY = (ge.getCenterPoint().y - maxHeight/2);

        // make sure we start on-screen
        if (offsetX <= 0)
            offsetX = offset;

        if (offsetY <= 0)
            offsetY = offset;

        // start at 0 to include console, start at 1 for just tools
        for (int i = 1; i < frames.length; i++) {
            JFrame f = frames[i];
            f.setLocation(offsetX + i * offset, offsetY + i * offset);
            f.toFront();
            Window[] ww = f.getOwnedWindows();
            for (int wwi = 0; wwi < ww.length; wwi++)
                ww[wwi].toFront();
        }
    }

    private void doTile() {
        // start at 0 to include console, start at 1 for just tools
        int n = frames.length - 1;
        int cols = (int) Math.sqrt(n);
        int rows = (n + cols - 1) / cols;
        Dimension dSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension tSize = new Dimension(dSize.width / cols, dSize.height / rows);
        for (int i = 0; i < n; i++) {
            // start at 0 to include console, start at 1 for just tools
            JFrame f = frames[i + 1];
            int c = (i % cols);
            int r = (i / cols);
            f.setBounds(c * tSize.width, r * tSize.height, tSize.width, tSize.height);
            //f.validate();
        }
    }

    private static JFrame getFrameForTool(Tool t) {
        return (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, t);
    }

    private static final int MENU_INSERT_POINT = 3; // before Windows, glue and Help

    private JFrame console;
    private JFrame[] frames;
    private boolean visible;
    private Tool selectedTool;
    private Listener listener = new Listener();

    private static final String TILE = "tile";
    private static final String CASCADE = "cascade";

    private class Listener
        implements ActionListener, MenuListener, Tool.Observer
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
                else if (o instanceof Tool)
                    setSelectedTool((Tool) o);
            }
        }

        // ---------- WindowAdapter ----------
        /*
        public void windowClosed(WindowEvent e) {
            JFrame src = (JFrame) (e.getSource());
            removeToolMenuItemsFromFrameMenuBar(src);
            }*/

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
            m.addSeparator();

            /*
            JMenu winOpenMenu = getWindowOpenMenu();
            if (winOpenMenu.getItemCount() > 0) {
                m.add(getWindowOpenMenu());
                m.addSeparator();
            }
            */

            int n = 0;

            // add entries for all current tools
            for (int i = 0; i < tools.length; i++) {
                Tool tool = tools[i];
                addMenuItem(m, n++, tool.getTitle(), tool);
            }

            // add entries for any dialogs
            for (int f = 0; f < frames.length; f++) {
                Window[] ownedWindows = frames[f].getOwnedWindows();
                for (int i = 0; i < ownedWindows.length; i++) {
                    Window w = ownedWindows[i];
                    if (w instanceof JDialog && w.isVisible())
                        addMenuItem(m, n++, ((JDialog) w).getTitle(), w);
                }
            }

            // add the main console
            addMenuItem(m, n++, console.getTitle(), console);
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

        // ---------- Tool.Observer ----------

        public void shortTitleChanged(Tool src, String newValue) {
        }

        public void titleChanged(Tool src, String newValue) {
            JFrame f = getFrameForTool(src);
            f.setTitle(uif.getI18NString("dt.title.tool.txt", newValue));
            //System.err.println("Tool title changed: " + newValue);
        }

        public void toolDisposed(Tool src) { }
    };
}
