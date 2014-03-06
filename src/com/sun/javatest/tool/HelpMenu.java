/*
 * $Id$
 *
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.sun.interview.Help;
import com.sun.javatest.Harness;
import com.sun.javatest.ProductInfo;
import com.sun.javatest.TestSuite;


class HelpMenu extends JMenu
{
    HelpMenu(Component parent, Desktop desktop, UIFactory uif) {
        this.parent = parent;
        this.desktop = desktop;
        this.uif = uif;

        listener = new Listener();

        String[] items = {
            HELP,
            null,
            // test suite items will be dynamically added here
            // (after the last separator)
            ABOUT_JAVATEST,
            ABOUT_JAVA
        };
        uif.initMenu(this, "hm", items, listener);
        addMenuListener(listener);
    }

    private void addTestSuiteItems() {
        Tool[] tools = desktop.getTools();
        if (tools == null || tools.length == 0) {
            return;
        }

        // first, collect the set of active test suites
        Set loadedTestSuites = new TreeSet(new Comparator() {
            @Override
                public int compare(Object o1, Object o2) {
                    TestSuite ts1 = (TestSuite) o1;
                    TestSuite ts2 = (TestSuite) o2;
                    return ts1.getName().compareTo(ts2.getName());
                }
            });
        Tool selTool = desktop.getSelectedTool();
        if (selTool != null) {
            TestSuite[] tss = selTool.getLoadedTestSuites();
            if (tss != null){
                loadedTestSuites.addAll(Arrays.asList(tss));
            }
        }
        else {
            for (int i = 0; i < tools.length; i++) {
                TestSuite[] tss = tools[i].getLoadedTestSuites();
                if (tss != null) {
                    loadedTestSuites.addAll(Arrays.asList(tss));
                }
            }
        }


        // locate insert point after last separator
        int insertPoint = getItemCount() - 1;
        while (insertPoint > 0 && (getItem(insertPoint - 1) != null)) {
            insertPoint--;
        }

        // for the active test suites, add any available help sets to the menu
        // e.g. those specified in the testsuite.jtt
        int count = 0;
        for (Iterator iter = loadedTestSuites.iterator(); iter.hasNext(); ) {
            TestSuite ts = (TestSuite) (iter.next());
            JMenuItem[] menuItems = getMenuItems(ts, count);
            if (menuItems != null && menuItems.length > 0) {
                for (int i = 0; i < menuItems.length; i++) {
                    JMenuItem mi = menuItems[i];
                    // mark the entry as a dynamic entry
                    mi.putClientProperty(getClass(), this);
                    insert(mi, insertPoint++);
                }

                Separator sep = new Separator();
                sep.putClientProperty(getClass(), this);
                JPopupMenu p = getPopupMenu();
                p.insert(sep, insertPoint++);
                count += menuItems.length + 1;
            }
        }

        // add custom menus (as GUI components)
        // e.g. those that come from exec tool
        // this is suboptimal/wrong if the test suite uses both
        // custom GUI menus and help from the testsuite.jtt
        ToolManager[] mgrs = desktop.getToolManagers();
        for (int i = 0; i < mgrs.length; i++) {
            JMenuItem[] jmi = mgrs[i].getHelpPrimaryMenus();
            if (jmi != null) {
                for (int j = 0; j < jmi.length; j++)  {
                    jmi[j].putClientProperty(getClass(), this);
                    insert(jmi[j], insertPoint++);
                }   // inner for
            }
        }   // for

        // add secondary test suite help items
        for (int i = 0; i < mgrs.length; i++) {
            JMenuItem[] jmi = mgrs[i].getHelpTestSuiteMenus();
            if (jmi != null) {
                for (int j = 0; j < jmi.length; j++)  {
                    jmi[j].putClientProperty(getClass(), this);
                    insert(jmi[j], insertPoint++);
                }   // inner for (j)
            }
        }   // for

        // add test suite specified About items
        for (int i = 0; i < mgrs.length; i++) {
            JMenuItem[] jmi = mgrs[i].getHelpAboutMenus();
            if (jmi != null) {
                for (int j = 0; j < jmi.length; j++)  {
                    jmi[j].putClientProperty(getClass(), this);
                    add(jmi[j]);
                }   // inner for (j)
            }
        }   // for
    }

    private void removeTestSuiteItems() {
        for (Component c : getMenuComponents()) {
            if (c instanceof JComponent) {
                JComponent comp = (JComponent) c;
                if (comp.getClientProperty(getClass()) == this) {
                    remove(comp);
                }
            }
        }
    }

    /**
     * Display a multi-line string in a dialog window, in response
     * to a Help>About.... menu item.
     * @param contKey i18n bundle key for getting accessibility name and desc
     *        for the container
     * @param filedKey i18n bundle key for getting accessibility name and desc
     *        for each of the fields holding the "about" text
     */
    private void showAbout(String title, String s, String contKey, String fieldKey) {
        ArrayList v = new ArrayList();
        int start = 0;
        int end   = 0;

        while ((end = s.indexOf('\n', start)) != -1) {
            v.add(s.substring(start, end));
            start = end+1;
        }
        v.add(s.substring(start));

        //JTextField[] tfs = new JTextField[v.size()];
        ArrayList<JComponent> tfs = new ArrayList();
        for (int i = 0; i < v.size(); i++) {
            JTextField tf = new JTextField((String)v.get(i));
            tf.setBorder(null);
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setOpaque(false);
            tf.setBackground(UIFactory.Colors.TRANSPARENT.getValue());
            tf.setEditable(false);
            uif.setAccessibleInfo(tf, fieldKey);
            tfs.add(tf);
        }

        JTextArea ta = uif.createMessageArea("hm.aboutJavaTest.copy");
        ta.setRows(15);
        tfs.add(uif.createScrollPane(ta,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        Object[] fields = tfs.toArray();

        /*
        JOptionPane.showMessageDialog(parent,
                                      tfs,
                                      title,
                                      JOptionPane.INFORMATION_MESSAGE,
                                      desktop.getLogo());
        */
        JOptionPane pane = new JOptionPane(fields, JOptionPane.INFORMATION_MESSAGE);
        pane.setIcon(desktop.getLogo());
        pane.setOptionType(JOptionPane.OK_OPTION);
        uif.setAccessibleInfo(pane, contKey);
        JButton okBtn = uif.createCloseButton("hm.about.ok", false);
        pane.setOptions(new Object[] { okBtn });
        JDialog d = pane.createDialog(parent, title);
        d.getRootPane().setDefaultButton(okBtn);
        d.setVisible(true);
    }

    private void showHelpSet(HelpSet hs) {
        HelpBroker hb = getHelpBroker(hs);
        if (hb != null) {
            hb.setDisplayed(true);
            if (hb instanceof DefaultHelpBroker)
                ((DefaultHelpBroker) hb).getWindowPresentation().getHelpWindow().toFront();
        }
        else {
            // could internationalize this, but the error isn't that helpful because a
            // end-user probably can't fix the problem
            System.err.println("Unable to display help, the help set isn't available.");
        }
    }

    private HelpBroker getHelpBroker(HelpSet hs) {
        HelpBroker hb = (HelpBroker) (helpBrokerTable.get(hs));
        if (hb == null) {
            int dpi = uif.getDotsPerInch();
            //hb = hs.createHelpBroker();   // pres. attributes work with this on JH 2.0_02
            hb = new DefaultHelpBroker(hs);  // needed for pres. attributes in JH 2.0
            hb.setSize(new Dimension(7*dpi, 9*dpi));
            helpBrokerTable.put(hs, hb);
        }
        return hb;
    }

    private JMenuItem[] getMenuItems(TestSuite ts, int count) {
        HelpSet[] docs = (HelpSet[]) (docTable.get(ts));
        if (docs == null) {
            try {
                docs = Help.getAdditionalDocs(ts);
            }
            catch (Help.Fault e) {
                String msg = uif.getI18NString("hm.cantLoadDocs",
                                               new Object[] { ts.getName(), e.getMessage() });
                System.err.println(msg);
            }
            if (docs == null)
                docs = new HelpSet[0];
            docTable.put(ts, docs);
        }

        if (docs.length == 0) {
            return null;
        }

        ArrayList<JMenuItem> v = new ArrayList();
        for (int i = 0; i < docs.length; i++) {
            final HelpSet doc = docs[i];

            JMenuItem mi;
            if (count + i < 10) {
                mi = new JMenuItem((count + i) + " " + doc.getTitle());
                mi.setMnemonic('0' + count + i);
            }
            else {
                mi = new JMenuItem("  " + doc.getTitle());
            }

            mi.addActionListener(new ActionListener() {
                @Override
                    public void actionPerformed(ActionEvent e) {
                        showHelpSet(doc);
                    }
                });

            v.add(mi);
        }

        JMenuItem[]items = new JMenuItem[v.size()];
        v.toArray(items);
        return items;
    }

    private Component parent;
    private Desktop desktop;
    private UIFactory uif;

    private Listener listener;

    private static WeakHashMap docTable = new WeakHashMap();  // gives HelpSet[] for TestSuite
    private static WeakHashMap helpBrokerTable = new WeakHashMap(); // gives HelpBroker for HelpSet

    private static final String HELP = "help";
    private static final String ABOUT_JAVA = "aboutJava";
    private static final String ABOUT_JAVATEST = "aboutJavaTest";

    private class Listener
        implements MenuListener, ActionListener
    {
        @Override
        public void menuSelected(MenuEvent e) {
            // remove any prior items
            removeTestSuiteItems();
            // add current ones
            addTestSuiteItems();
        }

        @Override
        public void menuDeselected(MenuEvent e) {
        }

        @Override
        public void menuCanceled(MenuEvent e) {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(ABOUT_JAVATEST)) {
                JMenuItem src = (JMenuItem)(e.getSource());

                // read en_US date, and prepare to emit it using the
                // current locale
                DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
                String date = null;
                Date dt = ProductInfo.getBuildDate();
                if (dt != null) {
                    date = df.format(dt);
                }
                else {
                    date = uif.getI18NString("hm.aboutBadDate");
                }

                String aboutJavaTest =
                    uif.getI18NString("hm.aboutJavaTest", new Object[] {
                        ProductInfo.getName(),
                        ProductInfo.getVersion(),
                        ProductInfo.getMilestone(),
                        ProductInfo.getBuildNumber(),
                        Harness.getClassDir().getPath(),
                        ProductInfo.getBuildJavaVersion(),
                        date
                });
                showAbout(src.getText(), aboutJavaTest, "hm.aboutJavaTest",
                            "hm.aboutJavaTest.text");
            }
            else if (cmd.equals(ABOUT_JAVA)) {
                JMenuItem src = (JMenuItem)(e.getSource());
                String aboutJava =
                    uif.getI18NString("hm.aboutJava", new Object[] {
                        System.getProperty("java.version"),
                        System.getProperty("java.vendor"),
                        System.getProperty("java.home"),
                        System.getProperty("java.vendor.url")
                });
                showAbout(src.getText(), aboutJava, "hm.aboutJava", "hm.aboutJava.text");
            }
            else if (cmd.equals(HELP)) {
                HelpBroker helpBroker = desktop.getHelpBroker();
                if (helpBroker != null) {
                    helpBroker.setCurrentID("jthelp.csh");
                    helpBroker.setDisplayed(true);
                    if (helpBroker instanceof DefaultHelpBroker)
                        ((DefaultHelpBroker) helpBroker).getWindowPresentation().getHelpWindow().toFront();
                }
                else {
                    // could internationalize this, but the error isn't that helpful because a
                    // end-user probably can't fix the problem
                    System.err.println("Unable to display help, the help system isn't available.");
                }
            }
        }

    }
}
