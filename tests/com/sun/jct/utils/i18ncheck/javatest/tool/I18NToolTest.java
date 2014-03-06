/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jct.utils.i18ncheck.javatest.tool;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.Timer;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.sun.javatest.JavaTestSecurityManager;
import com.sun.javatest.exec.ExecToolManager;
import com.sun.javatest.exec.ExecTool;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.EditableList;
import com.sun.javatest.tool.I18NUtils;
import com.sun.javatest.tool.Main;
import com.sun.javatest.tool.Startup;
import com.sun.javatest.tool.TestSuiteChooser;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.ToolManager;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.tool.WorkDirChooser;
import com.sun.javatest.util.ExitCount;



public class I18NToolTest
{
    public static void main(String[] args) {
        try {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    I18NToolTest t = new I18NToolTest();
                    t.run();
                    System.err.println("I18NToolTest completed successfully");
                    exit(0);
                }
            });
        }
        catch (Throwable t) {
            t.printStackTrace();
            exit(1);
        }
    }

    public void run() {
        ExitCount.inc();

        new Startup();

        sysProps.put("javatest.preferences.file", "NONE");
        sysProps.put("javatest.desktop.file", "NONE");

        try {
            PrintWriter out = new PrintWriter(System.out);
            Main m = new Main();
            m.run(new String[] {"-help"}, out);
            m.run(new String[] {"-help", "all"}, out);
            m.run(new String[] {"-help", "xyz"}, out);

            String[] jtArgs = { };
            m.run(jtArgs, out);

            out.flush();
        }
        catch (Throwable t) {
//          throw new Error("unexpected exception from JavaTest: " + t);
            throw new Error("unexpected exception from JT Harness: " + t);
        }

        for (int i = 0; i < I18NUtils.NUM_STATES; i++)
            I18NUtils.getStatusColor(i);

        I18NUtils.getStatusString(0);

        new EditableList();

        new WorkDirChooser(false);
        new TestSuiteChooser();

        UIFactory uif = new UIFactory(Desktop.class, null);
        // create buttons for dialogs
        uif.createButton("uif.ok");
        uif.createButton("uif.cancel");
        uif.createButton("uif.yes");
        uif.createButton("uif.no");

        Desktop d = new Desktop();
        d.setStyle(Desktop.TAB_STYLE);
        d.getFrames();

        d.setStyle(Desktop.MDI_STYLE);
        JMenuBar mb = d.getFrames()[0].getJMenuBar();
        JMenu fm = mb.getMenu(0);
        select(fm);
        for (int i = 0; i < fm.getItemCount(); i++) {
            JMenuItem mi = fm.getItem(i);
            if (mi != null && mi.getText().equals("Preferences")) {
                hidePrefsWhenShown();
                invoke(mi);
            }
        }

        // trigger About JavaTest dialog
        JMenu hm = mb.getMenu(mb.getComponentCount() - 1);
        select(hm);
        for (int i = 0; i < hm.getItemCount(); i++) {
            JMenuItem mi = hm.getItem(i);
            if (mi != null && mi.getText().equals("About the JT Harness")) {
                cancelDialogWhenShown(d, "About the JT Harness");
                invoke(mi);
            }
        }

        // trigger About JVM dialog
        select(hm);
        for (int i = 0; i < hm.getItemCount(); i++) {
            JMenuItem mi = hm.getItem(i);
            if (mi != null && mi.getText().equals("About the Java Virtual Machine")) {
                cancelDialogWhenShown(d, "About the Java Virtual Machine");
                invoke(mi);
            }
        }

        for (int i = 0; i < mb.getMenuCount(); i++) {
            JMenu menu = mb.getMenu(i);
            // may be a non-menu item (e.g. glue)
            if (menu != null)
                menu.doClick();
        }

        d.setStyle(Desktop.SDI_STYLE);
        ExecToolManager em = new ExecToolManager(d);
        ExecTool et = new ExecTool(em);

        d.addTool(et); // a tool to tickle sdi.file.close

        ToyToolManager ttm = new ToyToolManager(d);
        ToyTool tt = new ToyTool(ttm);
        d.addTool(tt); // a tool with alerts

        cancelDialogWhenShown(d, "OK to Exit?");
        d.isOKToExit(d.getFrames()[0]);

        d.dispose();

        ExitCount.dec();
    }

    private void cancelDialogWhenShown(final Desktop d, final String title) {
        cancelDialogTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.err.println("tick: cancelDialogTimer " + ticks);
                JDialog dialog = getDialog(d, "title:" + title);
                if (dialog != null) {
                    dialog.dispose();
                    cancelDialogTimer.stop();
                }

                if (--ticks == 0)
                    timerExpired("cancelDialogTimer");
            }

            int ticks = 10;
        });
        cancelDialogTimer.start();
    }

    private Timer cancelDialogTimer;

    private void hidePrefsWhenShown() {
        hidePrefsTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.err.println("tick: hidePrefsTimer " + ticks);
                Frame[] allFrames = Frame.getFrames();
                for (int i = 0; i < allFrames.length; i++) {
                    Frame f = allFrames[i];
                    //System.err.println("frame: " + f.getClass().getName());
                    Window[] ow = f.getOwnedWindows();
                    for (int j = 0; j < ow.length; j++) {
                        Window w = ow[j];
                        //System.err.println("window: " + w.getClass().getName());
                        if (w instanceof JDialog
                            && w.getClass().getName().startsWith("com.sun.javatest.tool.Preferences")) {
                            w.setVisible(false);
                            hidePrefsTimer.stop();
                        }
                    }
                }

                if (--ticks == 0)
                    timerExpired("hidePrefsTimer");
            }

            int ticks = 10;
        });
        hidePrefsTimer.start();
    }

    private Timer hidePrefsTimer;

    private static void timerExpired(String name) {
        System.err.println("timer " + name + " expired; exiting");
        exit(99);
    }

    private static void showContents(Container p) {
        for (int i = 0; i < p.getComponentCount(); i++) {
            Component c = p.getComponent(i);
            System.err.println(i + ": " + c.getClass().getName() + " -- " + c.getName());
        }
    }

    private static Component getComponent(Container p, String name) {
        int sep = name.indexOf('/');
        String n = (sep == -1 ? name : name.substring(0, sep));
        for (int i = 0; i < p.getComponentCount(); i++) {
            Component c = p.getComponent(i);
            String cn = c.getName();
            if (n.equals(cn)  ||  (n.length() == 0 && (cn == null))) {
                if (sep == -1)
                    return c;
                else
                    return getComponent((Container)c, name.substring(sep+1));
            }
        }
        //System.err.println("Could not find " + n + " within " + p.getName());
        return null;
    }

    private static JDialog getDialog(Desktop d, String name) {
        //System.err.println("looking on desktop for " + name);
        JFrame[] frames = d.getFrames();
        for (int i = 0; i < frames.length; i++) {
            JDialog di = getDialog(frames[i], name);
            if (di != null )
                return di;
        }
        return null;
    }

    private static JDialog getDialog(Window p, String name) {
        //System.err.println("looking on " + p.getName() + " for " + name);
        Window[] ownedWindows = p.getOwnedWindows();
        for (int i = 0; i < ownedWindows.length; i++) {
            Window w = ownedWindows[i];
            if (w instanceof JDialog) {
                JDialog d = (JDialog) w;
                if (name.startsWith("title:")) {
                    System.err.println(p.getName() + "--" + i + ": " + d.getTitle());
                    String dTitle = d.getTitle();
                    if (dTitle != null && dTitle.equals(name.substring(6)))
                        return d;
                }
                else {
                    System.err.println(p.getName() + "--" + i + ": " + w.getName());
                    String dName = d.getName();
                    if (dName != null && dName.equals(name))
                        return d;
                }
            }
        }
        //System.err.println("Could not find " + name + " within " + p.getName());
        return null;
    }

    private static void invoke(JMenuItem mi) {
        ActionEvent e = new ActionEvent(mi, ActionEvent.ACTION_PERFORMED, mi.getActionCommand());
        ActionListener[] ll = (ActionListener[])mi.getListeners(ActionListener.class);
        for (int i = 0; i < ll.length; i++)
            ll[i].actionPerformed(e);
    }

    private static void select(JMenu m) {
        MenuEvent e = new MenuEvent(m);
        MenuListener[] ll = (MenuListener[]) m.getListeners(MenuListener.class);
        for (int i = 0; i < ll.length; i++)
            ll[i].menuSelected(e);
    }

    private static void deselect(JMenu m) {
        MenuEvent e = new MenuEvent(m);
        MenuListener[] ll = (MenuListener[]) m.getListeners(MenuListener.class);
        for (int i = 0; i < ll.length; i++)
            ll[i].menuDeselected(e);
    }

    private static void exit(int n) {
        // If the JT security manager is installed, it won't allow a call of
        // System.exit unless we ask it nicely, pretty please, thank you.
        SecurityManager sc = System.getSecurityManager();
        if (sc instanceof JavaTestSecurityManager)
            ((JavaTestSecurityManager) sc).setAllowExit(true);
        System.exit(n);
    }

    private Properties sysProps = System.getProperties();

    private static class ToyToolManager extends ToolManager {
        ToyToolManager(Desktop d) {
            super(d);
        }

        public Tool startTool() {
            return new ToyTool(this);
        }

        public Tool restoreTool(Map m) {
            return new ToyTool(this);
        }
    };

    private static class ToyTool extends Tool {
        ToyTool(ToolManager m) {
            super(m, "toy");
        }

        public JMenuBar getMenuBar() {
            return null;
        }

        protected String[] getCloseAlerts() {
            return new String[] { "alert" };
        }

        protected void save(Map m) {
        }
    }
}
