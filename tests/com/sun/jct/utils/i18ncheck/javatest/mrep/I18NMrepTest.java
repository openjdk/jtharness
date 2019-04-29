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
package com.sun.jct.utils.i18ncheck.javatest.mrep;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import com.sun.javatest.mrep.ConflictResolutionDialog;
import com.sun.javatest.mrep.ReportToolManager;
import com.sun.javatest.report.ReportDirChooser;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.ExitCount;

public class I18NMrepTest
{
    public static void main(final String[] args) {
    //new ReportToolManager().getHelp();

    System.setProperty("javatest.preferences.file", "NONE");

    EventQueue.invokeLater(new Runnable() {
        public void run() {
            try {
                ExitCount.inc();
                new I18NMrepTest().run(args);
                System.err.println("I18NMrepTest completed successfully");
                System.exit(0);
            }
            catch (Throwable t) {
                t.printStackTrace();
                System.exit(2);
            }
        }
    });
    }
    void run(String[] args) throws FileNotFoundException, InterviewParameters.Fault,
            TestSuite.Fault, WorkDirectory.Fault {
       // TestSuite ts = TestSuite.open(new File(args[0]));
       // WorkDirectory wd = WorkDirectory.create(new File(args[1]), ts);

        Desktop d = new Desktop();

        ReportToolManager rm = (ReportToolManager) (d.getToolManager(ReportToolManager.class));
        rm.getFileMenuActions();
        Action[] actions = rm.getWindowOpenMenuActions();
        // start tool via the tool manager's action
        actions[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));;

        Tool t = d.getTools()[0];
        JFrame f = (JFrame) (SwingUtilities.getAncestorOfClass(JFrame.class, t));

        JMenu reportMenu = (JMenu) (getComponent(f.getJMenuBar(), "tool.report"));
        JMenuItem newReport = getMenuItem(reportMenu, "new");
        invokeMenuItem(newReport);
        Tool tool = rm.startTool();
        tool.setVisible(true);
        /*JDialog init = getDialog(d, "opts");
        JTabbedPane tabs = (JTabbedPane) (getComponent(init.getContentPane(), "tool.tabs"));
        JPanel files = (JPanel) (getComponent(tabs, "files"));
        JPanel opts = (JPanel) (getComponent(tabs, "opts"));
        JPanel but = (JPanel) (getComponent(opts, "opts.but"));

        Component c = getComponent(files, "files");
        JComboBox result = (JComboBox) (getComponent(files, "files.result"));
        JScrollPane in = (JScrollPane) (getComponent(files, "files.in"));
        JViewport inview = (JViewport) (getComponent(in, "files.inview"));
        JPanel merged = (JPanel) (getComponent(inview, "tool.merged"));
        JTextField in0 = (JTextField) (getComponent(merged, "files.input0"));
        JTextField in1 = (JTextField) (getComponent(merged, "files.input1"));
        JButton ok= (JButton) (getComponent(but, "opts.ok"));

        JPanel bottom = (JPanel) (getComponent(opts, "opts.bottom"));
        JList list = (JList) (getComponent(bottom, "opts.typel"));
        JCheckBox xml = (JCheckBox)list.getSelectedValue();//(JCheckBox) (getComponent(list, "opts.bottom"));

        result.addItem("ressssss");
        result.setSelectedIndex(1);

        in0.setText("1");
        in1.setText("2");


        xml.setSelected(true);
        ok.doClick();*/
        //JDialog conflict = getDialog(d, "opts");
        UIFactory uif = new UIFactory(ReportToolManager.class, null);
        ConflictResolutionDialog conflictResolutionDialog =
            new ConflictResolutionDialog(
                   null, "test", new String[] {"1", "2"}, false, uif);

        JFileChooser jf = new ReportDirChooser();
        ((ReportDirChooser)jf).setMode(ReportDirChooser.NEW);

       // jf.setVisible(true);
        ((ReportDirChooser)jf).setMode(ReportDirChooser.OPEN);
        //jf.showDialog(tool, "");
        //jf.setVisible(true);
        jf.cancelSelection();


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
    System.err.println("getComponent: " + name);
    int sep = name.indexOf('/');
    String n = (sep == -1 ? name : name.substring(0, sep));
    for (int i = 0; i < p.getComponentCount(); i++) {
        Component c = p.getComponent(i);
        System.err.println(p.getName() + "--" + i + ": " + c);
        System.err.println(p.getName() + "--" + i + ": " + c.getName());
        if (n.equals(c.getName()) || (n.length() == 0 && c.getName() == null)) {
        if (sep == -1)
            return c;
        else
            return getComponent((Container)c, name.substring(sep+1));
        }
    }
    System.err.println("Could not find " + n + " within " + p.getName());
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
    ActionListener[] ll = mi.getListeners(ActionListener.class);
    ActionEvent e = new ActionEvent(mi, ActionEvent.ACTION_PERFORMED, mi.getActionCommand());
    for (int i = 0; i < ll.length; i++)
        ll[i].actionPerformed(e);
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
}
