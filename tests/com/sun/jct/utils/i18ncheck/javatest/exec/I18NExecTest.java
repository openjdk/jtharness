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
package com.sun.jct.utils.i18ncheck.javatest.exec;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.SwingUtilities;

import com.sun.javatest.FileParameters;
import com.sun.javatest.Harness;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.JavaTestSecurityManager;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.exec.ExecToolManager;
import com.sun.javatest.exec.WorkDirChooseTool;
import com.sun.javatest.exec.ExecTool;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.ToolManager;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.tool.WorkDirChooser;
import com.sun.javatest.tool.WDC_FileFilter;
import com.sun.javatest.util.ExitCount;

// testing API
import com.sun.javatest.exec.AccessWrapper;

public class I18NExecTest
{
    public static void main(final String[] args) {
        System.setProperty("javatest.preferences.file", "NONE");

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ExitCount.inc();
                    new I18NExecTest().run(args);
                    System.err.println("I18NExecTest completed successfully");
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
        TestSuite demo_ts = TestSuite.open(new File(args[0]));
        WorkDirectory demo_wd = WorkDirectory.create(new File(args[1]), demo_ts);
        File jti = new File(args[2]);
        TestSuite service_ts = TestSuite.open(new File(args[3]));
        WorkDirectory service_wd = WorkDirectory.create(new File(args[4]), service_ts);

        Desktop d = new Desktop();
        d.setVisible(true);

        ExecToolManager em = (ExecToolManager) (d.getToolManager(ExecToolManager.class));
        //OLD em.getCommandLineHelp();
        em.getFileMenuActions();
        em.getTaskMenuActions();
        {
            ExecTool execTool = new ExecTool(em);
            execTool.setVisible(true);
            d.addTool(execTool);
            execTool.setVisible(true);  // seems necessary to force early visibility
            //execTool.showInitialDialog();
            //JDialog init = getDialog(d, "exec.init");
            //JButton btn = (JButton) (getComponent(init.getContentPane(), "exec.init.btns/exec.init.cancel"));
            //btn.doClick();
            execTool.showQuickStartWizard();
            JDialog init = getDialog(d, "qsw");
            JButton btn = (JButton) (getComponent(init.getContentPane(), "qsw.btns/qsw.cancel"));
            btn.doClick();
        }

        {
            // need ready-to-run setup
            InterviewParameters p = demo_ts.createInterview();
            try {
                p.load(jti);
            } catch (IOException e) {
                e.printStackTrace();
                exit(55);       // random number...
            }

            p.setWorkDirectory(demo_wd);
            ExecTool execTool = new ExecTool(em, p);
            d.addTool(execTool);
            d.setSelectedTool(execTool);
            //showComponent(execTool);

            JTabbedPane tabs = (JTabbedPane) getComponent(execTool, "treeAndDetails/split/main/deck/test/testTabs");
            JComponent descTab = (JComponent)  getComponent(execTool, "treeAndDetails/split/main/deck/test/testTabs/desc");
            tabs.setSelectedComponent(descTab);

            JTree tree = (JTree) getComponent(execTool, "treeAndDetails/split/tree/tree.sp/0/tree");
            //showTree(tree);
            TreePath tp = getTreePath(tree, "comp/index.html#CompSuccUnexp");
            System.err.println("tp: " + tp);
            tree.setSelectionPath(tp);

            // trigger confirm dialog, valid configuration and wd needed for this
            // needed to get the resources off the JList, which are not checked by
            // static analysis
            cancelConfirmWhenShown(d);
            execTool.runTests(new String[] {"comp/index.html#CompSuccUnexp"});


            // setup tree to check clear and refresh confirm dialogs
            TreePath[] tps = new TreePath[2];
            tps[0] = tp;
            tps[1] = getTreePath(tree, "comp/index.html#CompFailExp");

            tree.setSelectionPaths(tps);

            cancelClearWhenShown(d);
            ActionEvent event = new ActionEvent(tree, 0, "clear");
            MouseListener[] ls = (MouseListener[])(tree.getListeners(MouseListener.class));
            if (ls != null)
                for (int i = 0; i < ls.length; i++)
                    if (ls[i] instanceof ActionListener)
                        ((ActionListener)ls[i]).actionPerformed(event);

            //execTool.showInitialDialog();
            //JDialog init = getDialog(d, "exec.init");
            //JButton btn = (JButton) (getComponent(init.getContentPane(), "exec.init.btns/exec.init.cancel"));
            //btn.doClick();
        }

        {
            /*
            FileParameters p = new FileParameters();
            p.setTestSuite(ts);
            ExecTool execTool = new ExecTool(em, p);
            d.addTool(execTool);
            */
            InterviewParameters p = demo_ts.createInterview();
            ExecTool execTool = new ExecTool(em, p);
            d.addTool(execTool);

            cancelDialogWhenShown(d, "Work Directory Required");
            execTool.showWorkDirDialog(true);
        }

        {
            d.dispose();

            I18NHarness h = new I18NHarness();
            InterviewParameters ip = demo_ts.createInterview();
            try {
                ip.load(jti);
            } catch (IOException e) {
                e.printStackTrace();
                exit(55);
            }
            ip.setWorkDirectory(demo_wd);

            d = new Desktop(Desktop.TAB_STYLE);
            ExecTool execTool = new ExecTool(em, ip);
            d.addTool(execTool);
            d.setSelectedTool(execTool);

            cancelPrefsWhenShown();
            Preferences p = Preferences.access();
            Preferences.Pane[] execPrefs = { em.getPrefsPane() };
            p.showDialog(d.getFrames()[0], execPrefs, d.getHelpBroker());

            JButton fullBtn = (JButton) getComponent(execTool, "toolbarPanel/exec.toolbar/Edit Configuration...");
            changeViewWhenShown(d);
            fullBtn.doClick();

            JButton stdBtn = (JButton) getComponent(execTool, "toolbarPanel/exec.toolbar/Edit in Quick Set Mode");
            changeViewWhenShown(d);
            stdBtn.doClick();

            JMenu configMenu = (JMenu) (getComponent(d.getFrames()[0].getJMenuBar(), "ch"));

            JMenuItem changeConfig = getMenuItem(configMenu, "ch.change");
            invokeMenuItem(changeConfig);

            UIFactory etUIF = new UIFactory(ExecTool.class, d.getHelpBroker());
            ToolDialog rb = AccessWrapper.createReportBrowser(execTool, execTool, etUIF,
                                                                 AccessWrapper.createReportHandler(execTool, execTool, h, etUIF));
            rb.setVisible(true);

            ToolDialog nrd = AccessWrapper.createNewReportDialog(execTool, etUIF, execTool.getFilterConfig(),
                                                rb, execTool);
            nrd.setVisible(true);

            ToolDialog td = AccessWrapper.createTemplateDialog(execTool, execTool.getInterviewParameters(), execTool, etUIF);
            td.setVisible(true);

            JMenu viewMenu = (JMenu) (getComponent(d.getFrames()[0].getJMenuBar(), "exec.view"));

            JMenu configurationMenu = (JMenu) (getMenuItem(viewMenu, "exec.view.cfg"));

            JMenuItem showChecklist = getMenuItem(configurationMenu, "Show Checklist");
            invokeMenuItem(showChecklist);
            JMenuItem showQLog = getMenuItem(configurationMenu, "Show Question Log");
            invokeMenuItem(showQLog);
            JMenuItem showEnv = getMenuItem(configurationMenu, "Show Test Environment");
            invokeMenuItem(showEnv);
            JMenuItem showExcludeList = getMenuItem(configurationMenu, "Show Exclude List");
            invokeMenuItem(showExcludeList);

            cancelPropsWhenShown(d);
            JMenuItem viewProps = getMenuItem(viewMenu, "Properties...");
            invokeMenuItem(viewProps);

            cancelLogsWhenShown(d);
            JMenuItem viewLogVr = getMenuItem(viewMenu, "Logs...");
            invokeMenuItem(viewLogVr);

            cancelTestSuiteErrorsWhenShown(d);
            JMenuItem viewTSErrors = getMenuItem(viewMenu, "Test Suite Errors...");
            invokeMenuItem(viewTSErrors);

            JMenu filterMenu = (JMenu) (getMenuItem(viewMenu, "fconfig.submenu"));

            // select the custom filter to make I18N test work right
            JMenuItem customFilter = getMenuItem(filterMenu, "Custom");
            invokeMenuItem(customFilter);

            JMenuItem viewEditFilters = getMenuItem(filterMenu, "Configure Filters ...");
            invokeMenuItem(viewEditFilters);
            cancelFilterEditorWhenShown(d);

            JMenu runMenu = (JMenu) (getComponent(d.getFrames()[0].getJMenuBar(), "rh"));
            cancelStatusPWhenShown(d);
            JMenuItem viewStatusP = getMenuItem(runMenu, "Monitor Progress");
            invokeMenuItem(viewStatusP);

            AccessWrapper.createFilterHandler(execTool, execTool, etUIF, h);
            cancelFilterWarnWhenShown(d);
            h.notifyStart();
        }
        {
            ExecTool execTool = new ExecTool(em, service_wd);
            d.addTool(execTool);
            d.setSelectedTool(execTool);

            JMenu viewMenu = (JMenu) (getComponent(d.getFrames()[0].getJMenuBar(), "exec.view"));
            cancelDialogWhenShown(d, "Services...");
            JMenuItem viewServices = getMenuItem(viewMenu, "Services...");
            invokeMenuItem(viewServices);


        }
        {
            InterviewParameters p = demo_ts.createInterview();
            p.setWorkDirectory(demo_wd);
            ExecTool execTool = new ExecTool(em, p);
            d.addTool(execTool);


            UIFactory etUIF = new UIFactory(ExecTool.class, d.getHelpBroker());

            WorkDirChooseTool.getTool(execTool, execTool.getUIF(), execTool, WorkDirChooser.NEW,  demo_ts, true).doTool();
            cancelWDCWhenShown();

            WorkDirChooseTool.getTool(execTool, execTool.getUIF(), execTool, WorkDirChooser.OPEN_FOR_GIVEN_TESTSUITE,  demo_ts, true).doTool();
            cancelWDCWhenShown();

            WorkDirChooseTool.getTool(execTool, execTool.getUIF(), execTool, WorkDirChooser.OPEN_FOR_ANY_TESTSUITE,  demo_ts, true).doTool();
            cancelWDCWhenShown();

            WorkDirChooseTool.getTool(execTool, execTool.getUIF(), execTool, WorkDirChooser.NEW,  demo_ts, true).doTool();
            cancelDialogWhenShown(d, "Please select work directory");

        }
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
                    timerExpired("cancelDialogTimer :" + title);

            }

            int ticks = 10;
        });
        cancelDialogTimer.start();
    }

    private void cancelDialogWhenShown(final Window w, final String title) {
        cancelDialogTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.err.println("tick: cancelDialogTimer " + ticks);
                JDialog dialog = getDialog(w, "title:" + title);
                if (dialog != null) {
                    dialog.dispose();
                    cancelDialogTimer.stop();
                }

                if (--ticks == 0)
                    timerExpired("cancelDialogTimer :" + title);
            }

            int ticks = 10;
        });
        cancelDialogTimer.start();
    }

    private void cancelDialogWhenShown(final String title) {
        final Window defaultOwner = new JDialog().getOwner();
        cancelDialogWhenShown(defaultOwner, title);
    }

    private void cancelPrefsWhenShown() {
        cancelDialogWhenShown("JT Harness Preferences");
    }

    private void cancelWDCWhenShown() {
        cancelDialogWhenShown("Create Work Directory");
    }

    private void cancelTmplDWhenShown() {
        cancelDialogWhenShown("Create Template");
    }


    private void cancelPropsWhenShown(final Desktop d) {
        cancelDialogWhenShown(d, "Test Manager: Properties");
    }

    private void cancelLogsWhenShown(final Desktop d) {
        cancelDialogWhenShown(d, "Test Manager: Logs");
    }

    private void cancelStatusPWhenShown(final Desktop d) {
        cancelDialogWhenShown(d, "Test Manager: Progress Monitor");
    }

    private void cancelFilterWarnWhenShown(final Desktop d) {
        cancelDialogWhenShown(d, "View Filter Info");
    }

    private void cancelTestSuiteErrorsWhenShown(final Desktop d) {
        cancelDialogWhenShown(d, "Test Manager: Test Suite Errors");
    }

    private void cancelFilterEditorWhenShown(final Desktop d) {
        cancelDialogWhenShown(d, "View Filter Editor");
    }

    /**
     * Run tests confirmation dialog, shown when right click on tree is used.
     */
    private void cancelConfirmWhenShown(final Desktop d) {
        cancelDialogWhenShown(d, "Run Tests");
    }

    /**
     * Clear tests confirmation dialog, shown when right click on tree is used.
     */
    private void cancelClearWhenShown(final Desktop d) {
        cancelDialogWhenShown(d, "Confirm Clear Operation");
    }

    private void changeViewWhenShown(final Desktop d) {
        changeViewTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.err.println("tick: changeViewTimer " + ticks);
                JDialog ce = getDialog(d, "title:Configuration Editor: Question Mode");
                if (ce != null) {
                    JMenu viewMenu = (JMenu) getComponent(ce.getJMenuBar(), "ce.view");
                    JMenuItem stdView = getMenuItem(viewMenu, "Quick Set Mode");
                    invokeMenuItem(stdView);
                    changeViewTimer.stop();
                }

                if (--ticks == 0)
                    timerExpired("changeViewTimer");
            }

            int ticks = 10;
        });
        changeViewTimer.start();
    }

    private Timer cancelDialogTimer;
    private Timer cancelPrefsTimer;
    private Timer cancelPropsTimer;
    private Timer cancelLogsTimer;
    private Timer cancelStatusPTimer;
    private Timer cancelTSErrorsTimer;
    private Timer cancelFETimer;
    private Timer cancelFilterWarnTimer;
    private Timer changeViewTimer;
    private Timer cancelConfirmTimer;
    private Timer cancelWDCTimer;
    private Timer cancelTmplTimer;

    private static void timerExpired(String name) {
        System.err.println("timer " + name + " expired; exiting");
        exit(99);
    }

    private static void showTree(JTree tree) {
        showTree(tree.getModel());
    }

    private static void showTree(TreeModel model) {
        showTree(model, model.getRoot(), 0);
    }

    private static void showTree(TreeModel model, Object node, int depth) {
        for (int i = 0; i < depth; i++)
            System.err.print("  ");
        boolean leaf = model.isLeaf(node);
        int childCount = model.getChildCount(node);
        if (leaf)
            System.err.print("leaf");
        if (!leaf || childCount > 0)
            System.err.print("[" + childCount + "]");
        System.err.print(" ");
        if (node instanceof TestResultTable.TreeNode) {
            TestResultTable.TreeNode tn = (TestResultTable.TreeNode) node;
            System.err.println(tn.getName());
        }
        if (node instanceof TestResult) {
            TestResult tr = (TestResult) node;
            System.err.println(tr.getTestName());
        }
        else
            System.err.println(node);
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++)
                showTree(model, model.getChild(node, i), depth+1);
        }
    }

    private TreePath getTreePath(JTree tree, String name) {
        Vector v = new Vector();
        v.add(tree.getModel().getRoot());
        getTreePath(tree.getModel(), name, v);
        return new TreePath(v.toArray(new Object[v.size()]));
    }

    private void getTreePath(TreeModel model, String name, Vector v) {
        //System.err.println("getComponent: " + name);
        int sep = name.indexOf('/');
        String n = (sep == -1 ? name : name.substring(0, sep));
        int ni;
        try {
            ni = Integer.parseInt(n);
        }
        catch (NumberFormatException e) {
            ni = -1;
        }

        Object node = v.lastElement();
        for (int i = 0; i < model.getChildCount(node); i++) {
            Object o = model.getChild(node, i);
            String on;
            if (o instanceof TestResultTable.TreeNode)
                on = ((TestResultTable.TreeNode) o).getName();
            else if (o instanceof TestResult) {
                String tn = ((TestResult) o).getTestName();
                int x = tn.lastIndexOf('/');
                on = (x == -1 ? tn : tn.substring(x + 1));
            }
            else
                on = null;

            if (n.equals(on)
                || (n.length() == 0 && on == null)
                || (ni == i)) {
                v.add(o);
                if (sep != -1)
                    getTreePath(model, name.substring(sep+1), v);
                return;
            }
        }

        //System.err.println("Could not find " + n + " within " + p.getName());

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
        System.err.print(" vis=" + c.isVisible() + " disp=" + c.isDisplayable() + " show=" + c.isShowing());
        System.err.println();

        if (c instanceof Container) {
            Container p = (Container) c;
            for (int i = 0; i < p.getComponentCount(); i++)
                showComponent(p.getComponent(i), depth + 1);
        }
    }

    private static void showDesktop(Desktop d) {
        JFrame[] frames = d.getFrames();
        for (int i = 0; i < frames.length; i++)
            showComponent(frames[i]);
    }

    private static Component getComponent(Container p, String name) {
        //System.err.println("getComponent: " + name);
        int sep = name.indexOf('/');
        String n = (sep == -1 ? name : name.substring(0, sep));
        int ni;
        try {
            ni = Integer.parseInt(n);
        }
        catch (NumberFormatException e) {
            ni = -1;
        }

        for (int i = 0; i < p.getComponentCount(); i++) {
            Component c = p.getComponent(i);
            //System.err.println(p.getName() + "--" + i + ": " + c);
            //System.err.println(p.getName() + "--" + i + ": " + c.getName());
            if (n.equals(c.getName())
                || (n.length() == 0 && c.getName() == null)
                || (ni == i)) {
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
        if (mi == null) {
            return;
        }

        mi.setEnabled(true);

        ActionListener[] ll = (ActionListener[])mi.getListeners(ActionListener.class);
        ActionEvent e = new ActionEvent(mi, ActionEvent.ACTION_PERFORMED, mi.getActionCommand());
        for (int i = 0; i < ll.length; i++)
            ll[i].actionPerformed(e);

        if (mi instanceof JMenu) {
            JMenu jm = (JMenu) mi;
            for (int i = 0; i < jm.getItemCount(); i++) {
                invokeMenuItem(jm.getItem(i));
            }
        }
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

    private static void exit(int n) {
        // If the JT security manager is installed, it won't allow a call of
        // System.exit unless we ask it nicely, pretty please, thank you.
        SecurityManager sc = System.getSecurityManager();
        if (sc instanceof JavaTestSecurityManager)
            ((JavaTestSecurityManager) sc).setAllowExit(true);
        System.exit(n);
    }

    private class I18NHarness extends Harness {
        public void addObserver(Observer o) {
            obs = o;
        }

        void notifyStart() {
            obs.startingTestRun(null);
        }

        private Harness.Observer obs;
    }
}

