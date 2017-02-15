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
package com.sun.jct.utils.i18ncheck.javatest.audit;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.sun.javatest.audit.AuditCommandManager;
import com.sun.javatest.audit.AuditToolManager;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.ToolManager;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.FileParameters;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.ExitCount;

public class I18NAuditTest
{
    public static void main(final String[] args) {
        new AuditCommandManager().getHelp();

        System.setProperty("javatest.preferences.file", "NONE");

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ExitCount.inc();
                    new I18NAuditTest().run(args);
                    System.err.println("I18NAuditTest completed successfully");
                    System.exit(0);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                    System.exit(2);
                }
            }
        });
    }

    void run(String[] args) throws FileNotFoundException, InterviewParameters.Fault, TestSuite.Fault, WorkDirectory.Fault {
        TestSuite ts = TestSuite.open(new File(args[0]));
        WorkDirectory wd = WorkDirectory.create(new File(args[1]), ts);

        Desktop d = new Desktop();

        AuditToolManager am = (AuditToolManager) (d.getToolManager(AuditToolManager.class));
        am.getFileMenuActions();
        Action[] actions = am.getWindowOpenMenuActions();
        // start tool via the tool manager's action
        actions[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));;

        Tool t = d.getTools()[0];
        JFrame f = (JFrame) (SwingUtilities.getAncestorOfClass(JFrame.class, t));

        JMenu auditMenu = (JMenu) (getComponent(f.getJMenuBar(), "tool.audit"));
        JMenuItem options = getMenuItem(auditMenu, "options");
        invokeMenuItem(options);

    }

    private static void showComponent(Component c) {
        showComponent(c, 0);
    }

    private static void showComponent(Component c, int depth) {
        for (int i = 0; i < depth; i++)
            System.err.print("  ");
        System.err.print(c.getClass().getName());
        if (c.getName() != null)
            System.err.print(" " + c.getName());
        System.err.println();

        if (c instanceof Container) {
            Container p = (Container) c;
            for (int i = 0; i < p.getComponentCount(); i++)
                showComponent(p.getComponent(i), depth + 1);
        }
    }

    private static Component getComponent(Container p, String name) {
        //System.err.println("getComponent: " + name);
        int sep = name.indexOf('/');
        String n = (sep == -1 ? name : name.substring(0, sep));
        for (int i = 0; i < p.getComponentCount(); i++) {
            Component c = p.getComponent(i);
            //System.err.println(p.getName() + "--" + i + ": " + c);
            //System.err.println(p.getName() + "--" + i + ": " + c.getName());
            if (n.equals(c.getName()) || (n.length() == 0 && c.getName() == null)) {
                if (sep == -1)
                    return c;
                else
                    return getComponent((Container)c, name.substring(sep+1));
            }
        }
        //System.err.println("Could not find " + n + " within " + p.getName());
        return null;
    }

    private static JMenuItem getMenuItem(JMenu p, String name) {
        //System.err.println("getMenuItem: " + name + " " + p.getName() + "[" + p.getItemCount() + "]");
        int sep = name.indexOf('/');
        String n = (sep == -1 ? name : name.substring(0, sep));
        for (int i = 0; i < p.getItemCount(); i++) {
            JMenuItem c = p.getItem(i);
            //System.err.println(p.getName() + "--" + i + ": " + (c == null ? "---" : c.getName()));
            if (c != null && n.equals(c.getName())) {
                if (sep == -1)
                    return c;
                else
                    return getMenuItem((JMenu)c, name.substring(sep+1));
            }
        }
        //System.err.println("Could not find " + n + " within " + p.getName());
        return null;
    }

    private static void invokeMenuItem(JMenuItem mi) {
        ActionListener[] ll = (ActionListener[])mi.getListeners(ActionListener.class);
        ActionEvent e = new ActionEvent(mi, ActionEvent.ACTION_PERFORMED, mi.getActionCommand());
        for (int i = 0; i < ll.length; i++)
            ll[i].actionPerformed(e);
    }

    private static JDialog getDialog(Desktop d, String name) {
        JFrame[] frames = d.getFrames();
        for (int i = 0; i < frames.length; i++) {
            JDialog di = getDialog(frames[i], name);
            if (di != null )
                return di;
        }
        return null;
    }

    private static JDialog getDialog(Window p, String name) {
        Window[] ownedWindows = p.getOwnedWindows();
        for (int i = 0; i < ownedWindows.length; i++) {
            Window w = ownedWindows[i];
            if (w instanceof JDialog) {
                JDialog d = (JDialog) w;
                if (name.startsWith("title:")) {
                    //System.err.println(p.getName() + "--" + i + ": " + d.getTitle());
                    if (d.getTitle().equals(name.substring(6)))
                        return d;
                }
                else {
                    //System.err.println(p.getName() + "--" + i + ": " + w.getName());
                    if (d.getName().equals(name))
                        return d;
                }
            }
        }
        //System.err.println("Could not find " + name + " within " + p.getName());
        return null;
    }
}
