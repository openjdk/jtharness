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
package com.sun.jct.utils.i18ncheck.interview.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import com.sun.interview.FileListQuestion;
import com.sun.interview.FileQuestion;
import com.sun.interview.FinalQuestion;
import com.sun.interview.FloatQuestion;
import com.sun.interview.InetAddressQuestion;
import com.sun.interview.IntQuestion;
import com.sun.interview.Interview;
import com.sun.interview.NullQuestion;
import com.sun.interview.Question;
import com.sun.interview.StringListQuestion;
import com.sun.interview.StringQuestion;
import com.sun.interview.wizard.Exporter;
import com.sun.interview.wizard.Wizard;
import com.sun.interview.wizard.WizPane;
import com.sun.javatest.tool.jthelp.HelpBroker;
import com.sun.javatest.tool.jthelp.HelpSet;
import com.sun.javatest.tool.jthelp.JTHelpBroker;

public class I18NWizTest extends Interview
{
    public static void main(String[] args) {
        try {
            I18NWizTest t = new I18NWizTest();
            t.run();
            System.err.println("I18NWizTest completed successfully");
            System.exit(0);
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private I18NWizTest() {
        super("i18nWizTest");
        setFirstQuestion(qHello);

        HelpSet info_hs = new HelpSet();
        setHelpSet(info_hs);
    }

    private HelpSet help_hs;
    private HelpBroker help_hb;
    private JFrame f;
    private JMenu fileMenu;


    public void run()
        throws InterruptedException, InvocationTargetException
    {
        EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    help_hb = new JTHelpBroker();
                }
            });

        EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    JFrame df = new JFrame();
                    Wizard dw = new Wizard(I18NWizTest.this);
                    dw.showInDialog(df, new ActionListener() {
                            public void actionPerformed(ActionEvent ev) {
                            }
                        });
                }
            });

        EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    Wizard fw = new Wizard(I18NWizTest.this, new Exporter[] { exporter });
                    fw.setHelpBroker(help_hb);

                    fw.showInFrame(false);
                    f = getFrameForComponent(fw);
                }
            });

        EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    System.err.println("check wizard.file open");
                    fileMenu = (JMenu) (getComponent(f.getJMenuBar(), "wizard.file"));
                    cancelOpenWhenShown(f);
                    JMenuItem open = getMenuItem(fileMenu, "open");
                    invokeMenuItem(open);
                }
            });

        EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    System.err.println("check wizard.file export log");
                    cancelExportWhenShown(f);
                    JMenuItem export = getMenuItem(fileMenu, "export/log");
                    invokeMenuItem(export);
                }
            });

        EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    System.err.println("check wizard.search find");
                    JMenu searchMenu = (JMenu) (getComponent(f.getJMenuBar(), "wizard.search"));
                    cancelSearchWhenShown(f);
                    JMenuItem find = getMenuItem(searchMenu, "find");
                    invokeMenuItem(find);
                }
            });

        EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        setCurrentQuestion(qFile);
                        setCurrentQuestion(qFileSugg);
                        setCurrentQuestion(qFileList);
                        setCurrentQuestion(qIPv4);
                        setCurrentQuestion(qIPv6);
                        setCurrentQuestion(qIPv6Sugg);
                        setCurrentQuestion(qString);
                        setCurrentQuestion(qStringSugg);
                        setCurrentQuestion(qStringList);
                        setCurrentQuestion(qInt);
                        setCurrentQuestion(qIntSugg);
                        setCurrentQuestion(qFloat);
                        setCurrentQuestion(qFloatSugg);
                    }
                    catch (Interview.Fault e) {
                        e.printStackTrace();
                    }
                }
            });

        EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    WizPane wp = new WizPane(I18NWizTest.this);
                    wp.getMarkerMenu();
                }
            });
    }

    Exporter exporter = new Exporter() {
            public String getName() {
                return "demo";
            }

            public String[] getFileExtensions() {
                return new String[0];
            }

            public String getFileDescription() {
                return "demo description";
            }

            public boolean isExportable() {
                return true;
            }

            public void export(File f) {
            }
        };

    Question qHello = new NullQuestion(this, "hello") {
            public Question getNext() {
                return qFile;
            }
        };

    Question qFile = new FileQuestion(this, "file") {
            public Question getNext() {
                return qFileSugg;
            }
        };

    Question qFileSugg = new FileQuestion(this, "fileSugg") {
            {
                setSuggestions(new File[] { new File("dummy") });
            }

            public Question getNext() {
                return qFileList;
            }
        };

    Question qFileList = new FileListQuestion(this, "fileList") {
            public Question getNext() {
                return qIPv4;
            }
        };

    Question qIPv4 = new InetAddressQuestion(this, "ipv4") {
            public boolean isValueValid() {
                return true;
            }

            public Question getNext() {
                return qIPv6;
            }
        };

    Question qIPv6 = new InetAddressQuestion(this, "ipv6", InetAddressQuestion.IPv6) {
            public boolean isValueValid() {
                return true;
            }

            public Question getNext() {
                return qIPv6Sugg;
            }
        };

    Question qIPv6Sugg = new InetAddressQuestion(this, "ipv6Sugg", InetAddressQuestion.IPv6) {
            {
                try {
                    setSuggestions(new InetAddress[] { InetAddress.getByName("129.0.0.1") });
                }
                catch (Exception e) {
                    throw new Error(e);
                }
            }

            public boolean isValueValid() {
                return true;
            }

            public Question getNext() {
                return qString;
            }
        };

    Question qString = new StringQuestion(this, "string") {
            public Question getNext() {
                return qStringSugg;
            }
        };

    Question qStringSugg = new StringQuestion(this, "stringSugg") {
            {
                setSuggestions(new String[] { "a", "b", "c" });
            }

            public Question getNext() {
                return qStringList;
            }
        };

    Question qStringList = new StringListQuestion(this, "stringList") {
            public Question getNext() {
                return qInt;
            }
        };

    Question qInt = new IntQuestion(this, "int") {
            public boolean isValueValid() {
                return true;
            }

            public Question getNext() {
                return qIntSugg;
            }
        };

    Question qIntSugg = new IntQuestion(this, "intSugg") {
            {
                setSuggestions(new int[] { 1, 2, 3} );
            }

            public boolean isValueValid() {
                return true;
            }

            public Question getNext() {
                return qFloat;
            }
        };

    Question qFloat = new FloatQuestion(this, "float") {
            public boolean isValueValid() {
                return true;
            }

            public Question getNext() {
                return qFloatSugg;
            }
        };

    Question qFloatSugg = new FloatQuestion(this, "floatSugg") {
            {
                setSuggestions(new float[] { 1f, 2f, 3f} );
            }

            public boolean isValueValid() {
                return true;
            }

            public Question getNext() {
                return qEnd;
            }
        };

    Question qEnd = new FinalQuestion(this, "end");

    private void cancelExportWhenShown(final JFrame f) {
        cancelExportTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.err.println("tick cancelExportWhenShown");
                JDialog d = getDialog(f, "title:Export log");
                if (d != null) {
                    Container cp = d.getContentPane();
                    for (int i = 0; i < cp.getComponentCount(); i++) {
                        Component c = cp.getComponent(i);
                        if (c instanceof JFileChooser) {
                            JFileChooser jfc = (JFileChooser) c;
                            FileFilter ff = jfc.getFileFilter();
                            ff.getDescription();
                            d.dispose();
                            cancelExportTimer.stop();
                        }
                    }
                }
                else {
                    if (--ticks == 0)
                        timerExit();
                }
            }

            int ticks = 10;
        });
        cancelExportTimer.start();
    }

    private Timer cancelExportTimer;

    private void cancelOpenWhenShown(final JFrame f) {
        cancelOpenTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.err.println("tick cancelOpenWhenShown");
                JDialog d = getDialog(f, "title:Open");
                if (d != null) {
                    Container cp = d.getContentPane();
                    for (int i = 0; i < cp.getComponentCount(); i++) {
                        Component c = cp.getComponent(i);
                        if (c instanceof JFileChooser) {
                            JFileChooser jfc = (JFileChooser) c;
                            FileFilter ff = jfc.getFileFilter();
                            ff.getDescription();
                            d.dispose();
                            cancelOpenTimer.stop();
                        }
                    }
                }
                else {
                    if (--ticks == 0)
                        timerExit();
                }
            }

            int ticks = 10;
        });
        cancelOpenTimer.start();
    }

    private Timer cancelOpenTimer;

    private void cancelSearchWhenShown(final JFrame f) {
        cancelSearchTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.err.println("tick cancelSearchWhenShown");
                JDialog d = getDialog(f, "title:Find Question");
                if (d != null) {
                    d.dispose();
                    cancelSearchTimer.stop();
                }
                else {
                    if (--ticks == 0)
                        timerExit();
                }
            }

            int ticks = 10;
        });

        cancelSearchTimer.start();
    }

    private void timerExit() {
        System.err.println("TIMER EXHAUSTED");
        System.exit(1);
    }

    private Timer cancelSearchTimer;

    private static JFrame getFrameForComponent(Component c) {
        return (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, c);
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

    private static JDialog getDialog(Window p, String name) {
        Window[] ownedWindows = p.getOwnedWindows();
        //System.err.println(p.getName() + "--" + ownedWindows.length + " owned windows");
        for (int i = 0; i < ownedWindows.length; i++) {
            Window w = ownedWindows[i];
            if (w instanceof JDialog) {
                JDialog d = (JDialog) w;
                if (name.startsWith("title:")) {
                    //System.err.println(p.getName() + "--" + i + ": " + d.getTitle());
                    if (d.getTitle().equals(name.substring(6))) {
                        System.err.println("found dialog: " + d);
                        return d;
                    }
                }
                else {
                    //System.err.println(p.getName() + "--" + i + ": " + w.getName());
                    if (d.getName().equals(name)) {
                        System.err.println("found dialog: " + d);
                        return d;
                    }
                }
            }
        }
        //System.err.println("Could not find " + name + " within " + p.getName());
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
}
