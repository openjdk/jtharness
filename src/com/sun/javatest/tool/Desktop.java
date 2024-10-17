/*
 * $Id$
 *
 * Copyright (c) 2001, 2024, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.JavaTestError;
import com.sun.javatest.TestSuite;
import com.sun.javatest.report.HTMLWriterEx;
import com.sun.javatest.tool.jthelp.ContextHelpManager;
import com.sun.javatest.tool.jthelp.HelpBroker;
import com.sun.javatest.tool.jthelp.HelpSet;
import com.sun.javatest.tool.jthelp.JTHelpBroker;
import com.sun.javatest.util.BackupPolicy;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.LogFile;
import com.sun.javatest.util.PropertyUtils;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Desktop is the host for a series of Tools,
 * which may be displayed as in a number of styles,
 * provided by a DeskView.
 * <p>Much of the functionality of a desktop is provided by the current view,
 * and because of that, many of the methods here simply call through to the
 * underlying current view object.
 *
 * @see DeskView
 */
public class Desktop {
    /**
     * A constant to indicate the tabbed-style desktop:
     * a single window for the desktop, using a tabbed pane for the tools.
     */
    public static final int TAB_STYLE = 0;
    /**
     * A constant to indicate the MDI-style desktop:
     * a single window for the desktop, containing multiple internal windows, one per tool.
     */
    public static final int MDI_STYLE = 1;
    /**
     * A constant to indicate the SDI-style desktop:
     * multiple top-level windows, one per tool.
     */
    public static final int SDI_STYLE = 2;

    /**
     * JT Harness allows you to hide the auto-updated progress bars that reflect
     * the progress of a test run in the GUI. To do this, start JT Harness with
     * this system property set to 'true'.
     */
    public static final String TESTRUN_PROGRESSMONITOR_HIDDEN = "javatest.desktop.testrunprogressmonitor.hidden";

    static final int NUM_STYLES = 3;
    static final String[] styleNames = {"tab", "mdi", "sdi"};
    static final String STYLE_PREF = "tool.appearance.style";
    static final String TTIP_PREF = "tool.appearance.ttipToggle";
    static final String TTIP_DELAY = "tool.appearance.ttipDelay";
    static final String TTIP_DURATION = "tool.appearance.ttipDuration";
    static final int TTIP_DURATION_FOREVER = -1;
    static final int TTIP_DELAY_NONE = 0;
    static final int TTIP_DELAY_DEFAULT = 0;
    static final int TTIP_DURATION_DEFAULT = 5000;
    static final String SAVE_ON_EXIT_PREF = "tool.appearance.saveOnExit";
    static final String RESTORE_ON_START_PREF = "tool.appearance.restoreOnStart";
    private static final int CLOSE = 0;
    private static final int EXIT = 1;
    private static final String[] jt31StyleClassNames = {
            "com.sun.javatest.tool.TabDesktop",
            "com.sun.javatest.tool.MDIDesktop",
            "com.sun.javatest.tool.SDIDesktop"
    };
    private static final int FILE_HISTORY_MAX_SIZE = 10;
    private static final String TOOLMGRLIST = "META-INF/services/com.sun.javatest.tool.ToolManager.lst";
    /**
     * If this system property is defined then its value is expected to contain comma-separated list
     * of ToolManager subclasses to load, which would be put in use instead of those contained in the dedicated file.
     */
    public static final String TOOL_MANAGERS_TO_LOAD = "services.com.sun.javatest.tool.ToolManager";
    private static final String defaultToolManager =
            System.getProperty("javatest.desktop.defaultToolManager", "com.sun.javatest.exec.ExecToolManager");
    private static Preferences preferences = Preferences.access();
    private final UIFactory uif;
    private JDialog confirmDialog;
    private CommandContext commandContext;
    private DeskView currView;
    private int style; // used until currView is set, then style comes from that
    private PreferencesPane prefsPane;
    private PreferencesPane colorPane;
    private ToolManager[] toolManagers;
    private HelpBroker helpBroker;
    private LogFile logFile;
    private boolean firstTime;
    private boolean saveOnExit;
    private PrintRequestAttributeSet printAttrs;
    private boolean restoreOnStart;
    private LinkedList<FileHistoryEntry> fileHistory = new LinkedList<>();

    /**
     * Create a desktop using a style determined according to the
     * user's preferences.
     */
    public Desktop() {
        this(getPreferredStyle());
    }

    /**
     * New desktop, using preferred style and given context.
     */
    public Desktop(CommandContext ctx) {
        this(getPreferredStyle(), ctx);
    }


    public Desktop(int style, CommandContext ctx) {
        commandContext = ctx;

        String val = preferences.getPreference(TTIP_PREF);
        boolean t = val == null || val.equalsIgnoreCase("true");
        setTooltipsEnabled(t);

        int delay = getTooltipDelay(preferences);
        setTooltipDelay(delay);

        int duration = getTooltipDuration(preferences);
        setTooltipDuration(duration);

        String soe = preferences.getPreference(SAVE_ON_EXIT_PREF);
        setSaveOnExit(soe == null || "true".equalsIgnoreCase(soe));
        String rtos = preferences.getPreference(RESTORE_ON_START_PREF);
        setRestoreOnStart(rtos == null || "true".equalsIgnoreCase(rtos)); // false by default (null)

        File f = getDesktopFile();
        firstTime = !(f != null && f.exists());

        initHelpBroker();
        initLAF(ctx);


        uif = new UIFactory(getClass(), helpBroker);

        initToolManagers();

        /* defer view initialization in case we do a restore
        initView(style);
        */
        this.style = style;
    }

    /**
     * Create a desktop using a specified style.
     *
     * @param style a value indicating the desired desktop style.
     * @see #MDI_STYLE
     * @see #SDI_STYLE
     * @see #TAB_STYLE
     */
    public Desktop(int style) {
        this(style, null);
    }

    /**
     * Get a value indicating the user's preferred desktop style,
     * as recorded in the user's preferences.
     *
     * @return a value indicating the user's preferred desktop style
     * @see #MDI_STYLE
     * @see #SDI_STYLE
     * @see #TAB_STYLE
     */
    public static int getPreferredStyle() {
        // would be better(?) to use classname, perhaps
        // NO! It must be better to use the tabbed view only.

        return TAB_STYLE;
        /* SDI/MDI are not supported any more...
        String prefStyleName = preferences.getPreference(STYLE_PREF);
        int i = indexOf(prefStyleName, styleNames);
        return (i != -1 ? i : TAB_STYLE);
         */
    }

    static Map<String, String> getPreviousDesktop(File file) {
        if (file == null) {
            file = getDesktopFile();
        }

        Map<String, String> stringPropsMap = new HashMap<>();

        if (file != null && file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 InputStream in = new BufferedInputStream(fis)) {
                stringPropsMap = PropertyUtils.load(in);
            } catch (IOException e) {
                // I18N
                System.err.println("Error reading desktop file: " + e);
            }
        }

        return stringPropsMap;
    }

    /**
     * Get Tooltip delay from prefs in ms.
     *
     * @return Range is 0-Integer.MAX_VALUE
     */
    static int getTooltipDelay(Preferences p) {
        String val = p.getPreference(TTIP_DELAY);
        int result = TTIP_DELAY_DEFAULT;

        try {
            // expected range from prefs in 0-Integer.MAX_VALUE
            result = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            // default to no delay
            result = TTIP_DELAY_DEFAULT;
        }

        if (result < 0) {
            result = TTIP_DELAY_DEFAULT;
        }

        return result;
    }

    /**
     * Get tooltip duration from prefs in ms.
     * This is the translated value, so the "forever" value has been
     * transformed into something useful.
     *
     * @return Range is 0-Integer.MAX_VALUE
     */
    static int getTooltipDuration(Preferences p) {
        String val = p.getPreference(TTIP_DURATION);
        int result = TTIP_DURATION_DEFAULT;

        try {
            // expected range from prefs in -1-Integer.MAX_VALUE
            result = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            // default to no delay
            result = TTIP_DURATION_DEFAULT;
        }

        if (result < 0) {
            if (result == TTIP_DURATION_FOREVER)        // indicates forever duration
            {
                result = Integer.MAX_VALUE;
            } else                        // -2 or less, unknown value
            {
                result = TTIP_DURATION_DEFAULT;
            }
        } else {
        }

        return result;
    }

    private static void appendStrings(StringBuffer sb, String... msgs) {
        if (msgs != null) {
            for (String msg : msgs) {
                sb.append(msg);
                if (!msg.endsWith("\n")) {
                    sb.append('\n');
                }
            }
        }
    }

    /**
     * Get the file in which the desktop is (to be) stored.
     * The standard location is the platform equivalent of
     * $HOME/.javatest/desktop
     * It can be overridden by setting the system property
     * "javatest.desktop.file", which can be set to "NONE"
     * to disable the feature.
     */
    private static File getDesktopFile() {
        String s = System.getProperty("javatest.desktop.file");
        if (s == null) {
            File jtDir = Preferences.getPrefsDir();
            return new File(jtDir, "desktop");
        } else if (!s.equals("NONE")) {
            return new File(s);
        } else {
            return null;
        }
    }

    static void addHelpDebugListener(Component c) {
        JComponent root;
        if (c instanceof JFrame) {
            root = ((JFrame) c).getRootPane();
        } else if (c instanceof JDialog) {
            root = ((JDialog) c).getRootPane();
        } else {
            throw new IllegalArgumentException();
        }

        ActionListener showFocusListener = e -> {
            Component src = (Component) e.getSource();
            Component comp = javax.swing.SwingUtilities.findFocusOwner(src);
            System.err.println("ALT-F2: source=" + src);
            System.err.println("ALT-F2:  focus=" + comp);
            String helpId = comp == null ? "(none)" : ContextHelpManager.getHelpIDString(comp);
            helpId = helpId == null ? ContextHelpManager.getHelpIDString(src) : helpId;
            System.err.println("ALT-F2: helpId=" + helpId);
        };

        root.registerKeyboardAction(showFocusListener,
                KeyStroke.getKeyStroke("alt F2"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    static void addPreferredSizeDebugListener(Component c) {
        JComponent root;
        if (c instanceof JFrame) {
            root = ((JFrame) c).getRootPane();
        } else if (c instanceof JDialog) {
            root = ((JDialog) c).getRootPane();
        } else {
            throw new IllegalArgumentException();
        }

        ActionListener showPrefSizeListener = e -> {
            Component src = (Component) e.getSource();
            Component c1 = javax.swing.SwingUtilities.findFocusOwner(src);
            while (c1 != null) {
                Dimension d = c1.getPreferredSize();
                System.err.println("ALT-1: comp=" + c1.getName() + "(" + c1.getClass().getName() + ") "
                        + "[w:" + d.width + ",h:" + d.height + "]");
                c1 = c1.getParent();
            }
        };

        root.registerKeyboardAction(showPrefSizeListener,
                KeyStroke.getKeyStroke("alt 1"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private static void invokeOnEventThread(Runnable r) {
        try {
            EventQueue.invokeAndWait(r);
        } catch (InterruptedException e) {
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw (Error) t;
            }
        }
    }

    private static int indexOf(String s, String... a) {
        for (int i = 0; i < a.length; i++) {
            if (s == null ? a[i] == null : s.equals(a[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Walks down the exception causes until the original exception is found.
     *
     * @param t The problem object to decend into.
     * @return The original problem object.
     */
    private static Throwable unwrap(Throwable t) {
        if (t == null) {
            return t;
        }

        Throwable t1;
        while (true) {
            t1 = t.getCause();
            if (t1 == null) {
                return t;
            } else {
                t = t1;
            }
        }
    }

    private void initLAF(CommandContext ctx) {
        int preferredLAF;
        if (ctx != null) {
            preferredLAF = ctx.getPreferredLookAndFeel();
        } else {
            // can occur from the test
            preferredLAF = CommandContext.DEFAULT_LAF;
        }

        switch (preferredLAF) {
            case CommandContext.SYSTEM_LAF:
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                }
                break;
            case CommandContext.METAL_LAF:
                try {
                    Class<? extends LookAndFeel> nimbusClass =
                            Class.forName("javax.swing.plaf.metal.MetalLookAndFeel").asSubclass(LookAndFeel.class);
                    UIManager.setLookAndFeel(nimbusClass.getDeclaredConstructor().newInstance());
                } catch (Throwable e) {
                }
                break;
            case CommandContext.NIMBUS_LAF:
                try {
                    Class<? extends LookAndFeel> nimbusClass =
                            Class.forName("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel").asSubclass(LookAndFeel.class);
                    UIManager.setLookAndFeel(nimbusClass.getDeclaredConstructor().newInstance());
                } catch (Throwable e) {
                }
                break;
            // no need to process CommandContext.DEFAULT_LAF - it should equal one of the others constants
        }

    }

    /**
     * Get a value indicating the current style of the desktop.
     *
     * @return a value indicating the current style of the desktop
     * @see #setStyle
     * @see #MDI_STYLE
     * @see #SDI_STYLE
     * @see #TAB_STYLE
     */
    public int getStyle() {
        return currView == null ? style : currView.getStyle();
    }

    /**
     * Set the current style of the desktop.
     *
     * @param style a value indicating the current style of the desktop
     * @see #getStyle
     * @see #MDI_STYLE
     * @see #SDI_STYLE
     * @see #TAB_STYLE
     */
    public void setStyle(int style) {
        //System.err.println("Desktop.setStyle: " + style);
        if (style == getStyle()) {
            return;
        }

        if (currView == null) {
            this.style = style;
            return;
        }

        DeskView oldView = currView;
        //System.err.println("DT: creating new desktop (" + style + ")");
        switch (style) {
            case MDI_STYLE:
                currView = new MDIDeskView(oldView);
                break;

            case SDI_STYLE:
                currView = new SDIDeskView(oldView);
                break;

            case TAB_STYLE:
                currView = new TabDeskView(oldView);
                break;

            default:
                throw new IllegalArgumentException();
        }

        //System.err.println("DT: disposing old deskview");
        oldView.dispose();

        //System.err.println("DT: setStyle done ");
    }

    // the order of the styles is the presentation order in the preferences panel

    /**
     * Get the Help Broker used by the Help menu amd context sensitive help.
     *
     * @return the help broker
     */
    public HelpBroker getHelpBroker() {
        return helpBroker;
    }

    /**
     * Determine if this is the first time that JT Harness has been run.
     * This is determined by checking if a saved desktop exists from
     * a prior run of JT Harness.
     *
     * @return true if this appears to be the first time the user has
     * run JT Harness, and false otherwise
     */
    public boolean isFirstTime() {
        return firstTime;
    }

    /**
     * Set the flag indicating whether or not this is the first time
     * that JT Harness has been run.
     *
     * @param b true if JT Harness should behave as though this is th
     *          first time JT Harness has been run
     * @see #isFirstTime
     */
    public void setFirstTime(boolean b) {
        firstTime = b;
    }

    /**
     * Check whether the desktop is empty of any tools.
     *
     * @return true if there are no tools on the desktop, and false otherwise
     */
    public boolean isEmpty() {
        return currView == null ? true : currView.isEmpty();
    }

    /**
     * Get the set of tools currently on the desktop.
     *
     * @return the set of tools currently on the desktop
     */
    public Tool[] getTools() {
        return currView == null ? new Tool[0] : currView.getTools();
    }

    /**
     * Add a new tool to the desktop.
     *
     * @param t the tool to be added
     * @see #removeTool
     */
    public void addTool(Tool t) {
        ensureViewInitialized();
        currView.addTool(t);
    }

    /**
     * Remove a tool from the desktop.
     *
     * @param t the tool to be removed
     * @see #addTool
     */
    public void removeTool(Tool t) {
        if (currView != null) {
            currView.removeTool(t);
        }
    }

    /**
     * Get the currently selected tool on the desktop.
     *
     * @return the currently selected tool on the desktop
     * @see #setSelectedTool
     */
    public Tool getSelectedTool() {
        return currView == null ? null : currView.getSelectedTool();
    }

    /**
     * Set the currently selected tool on the desktop.
     *
     * @param t the the tool to be selected on the desktop
     * @see #getSelectedTool
     */
    public void setSelectedTool(Tool t) {
        ensureViewInitialized();
        currView.setSelectedTool(t);
    }

    /**
     * Add a new default tool to the desktop. The default can be set via the
     * system property "javatest.desktop.defaultTool", which should identify
     * the class name of an appropriate tool manager; if not set, the default
     * is com.sun.javatest.exec.ExecManager.
     *
     * @see #removeTool
     */
    public void addDefaultTool() {
        if (!EventQueue.isDispatchThread()) {
            invokeOnEventThread(this::addDefaultTool);
            return;
        }

        for (ToolManager m : toolManagers) {
            if (m.getClass().getName().equals(defaultToolManager)) {
                m.startTool();
                return;
            }
        }
    }

    /**
     * Add a new default tool to the desktop. The default can be set via the
     * system property "javatest.desktop.defaultTool", which should identify
     * the class name of an appropriate tool manager; if not set, the default
     * is com.sun.javatest.exec.ExecManager.
     *
     * @param ip a configuration to be passed to the default tool manager's startTool
     *           method
     * @see #removeTool
     */
    public Tool addDefaultTool(InterviewParameters ip) {
        for (ToolManager mgr : toolManagers) {
            if (mgr.getClass().getName().equals(defaultToolManager)) {
                try {
                    // this is to avoid a class dependency to exec package, which is
                    // normally not allowed in this package
                    Method m = mgr.getClass().getMethod("startTool", InterviewParameters.class);

                    return (Tool) m.invoke(mgr, ip);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    // ignore??
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // ignore??
                    unwrap(e).printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Check if a tool is present on the desktop.
     *
     * @param t the tool for which to check
     * @return true if the specified tool exists on the desktop, and false otherwise
     */
    public boolean containsTool(Tool t) {
        Tool[] tools = getTools();
        for (Tool tool : tools) {
            if (t == tool) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the set of tool managers associated with this desktop.
     * The managers are determined from resource files named
     * "com.sun.javatest.tool.ToolManager.lst" on the main JT Harness classpath.
     *
     * @return the set of tool managers associated with this desktop
     */
    public ToolManager[] getToolManagers() {
        return toolManagers;
    }

    /**
     * Get the instance of a tool manager for this desktop of a specific class.
     *
     * @param c the class of the desired tool manager.
     * @return a tool manager of the desired type, or null if none found
     */
    public ToolManager getToolManager(Class<? extends ToolManager> c) {
        for (ToolManager m : toolManagers) {
            if (c.isInstance(m)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Get the instance of a tool manager for this desktop of a specific class.
     *
     * @param className the name of the class of the desired tool manager.
     * @return a tool manager of the desired type, or null if none found
     */
    public ToolManager getToolManager(String className) {
        for (ToolManager m : toolManagers) {
            if (m.getClass().getName().equals(className)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Get the top level frames that make up this desktop. TAB and MDI style
     * desktops just have a single frame; An SDI style desktop may have more
     * than one frame.
     *
     * @return the top level frames of this desktop
     */
    public JFrame[] getFrames() {
        ensureViewInitialized();
        return currView.getFrames();
    }

    /**
     * Get a parent component for a dialog to use.
     *
     * @return Component which can be used as a parent, or null if none
     * is available.
     */
    public Component getDialogParent() {
        ensureViewInitialized();
        return currView.getDialogParent();
    }

    /**
     * Add a file and a corresponding file opener to the file history
     * that appears on the File menu.
     *
     * @param f  The file to be added
     * @param fo A FileOpener object to be used to open the file if necessary
     */
    public void addToFileHistory(File f, FileOpener fo) {
        // if it is already in the history, remove it
        for (Iterator<FileHistoryEntry> i = fileHistory.iterator(); i.hasNext(); ) {
            FileHistoryEntry h = i.next();
            if (h.fileOpener == fo && h.file.equals(f)) {
                i.remove();
                break;
            }
        }

        // add it to the front of the list
        fileHistory.addFirst(new FileHistoryEntry(fo, f));

        // throw away old entries in the list
        while (fileHistory.size() > FILE_HISTORY_MAX_SIZE) {
            fileHistory.removeLast();
        }
    }

    /**
     * Get a list of the current entries on the file history associated with this desktop.
     *
     * @return a list of the current entries on the file history associated with this desktop
     * @see #addToFileHistory
     */
    List<FileHistoryEntry> getFileHistory() {
        return fileHistory;
    }

    /**
     * Check if the top level windows of the desktop are visible or not.
     *
     * @return true if the top level windows are visible; otherwise, return false
     * @see #setVisible
     */
    public boolean isVisible() {
        return currView == null ? false : currView.isVisible();
    }

    /**
     * Set whether or not the top level windows of the desktop should be visible.
     *
     * @param b If true, the top level windows will be made visible; if false, they
     *          will be hidden.
     */
    public void setVisible(final boolean b) {
        if (!EventQueue.isDispatchThread()) {
            invokeOnEventThread(() -> setVisible(b));
            return;
        }

        ensureViewInitialized();
        currView.setVisible(b);
    }

    /**
     * Create a dialog.
     *
     * @param tool    the parent tool for the dialog
     * @param uiKey   a string which is to be used as the base name for any
     *                resources that may be required
     * @param title   the title for the dialog
     * @param menuBar the menu bar for the dialog
     * @param body    the body component for the dialog
     * @param bounds  the size and position for the dialog
     * @return a JDialog or JInternalDialog built from the supplied values.
     */
    public Container createDialog(Tool tool, String uiKey, String title,
                                  JMenuBar menuBar, Container body,
                                  Rectangle bounds, int type) {
        ensureViewInitialized();
        return currView.createDialog(tool, uiKey, title, menuBar, body, bounds, type);
    }

    /**
     * Check if the tool's parent Window is the owner of a dialog.
     * This may become false if the desktop style is changed after the dialog
     * was created.
     *
     * @param tool   the tool from which to determine the parent Window
     * @param dialog the dialog to be checked
     * @return true if the tool's parent Window is the owner of the dialog, and
     * false otherwise.
     */
    public boolean isToolOwnerForDialog(Tool tool, Container dialog) {
        ensureViewInitialized();
        return currView.isToolOwnerForDialog(tool, dialog);
    }

    /**
     * Check all the tools on the desktop to see if they have open state
     * that should be saved or processes running. If there is open state
     * or active processes, a confirmation dialog will be displayed.
     * If the user confirms OK, or if there was no need to show the
     * confirmation dialog, the desktop will be saved and disposed.
     *
     * @param parent A parent frame to be used if a confirmation dialog
     *               is necessary
     * @see #isOKToExit
     */
    public void checkToolsAndExitIfOK(JFrame parent) {
        if (isOKToExit(parent)) {
            if (getSaveOnExit()) {
                save();
            }

            dispose();
        }
    }

    /**
     * Check if it is OK to close a tool. If the tool has important
     * state that needs to be saved, or any processes running, a confirmation
     * dialog will be shown, to allow the user to cancel the operation if
     * necessary.
     *
     * @param t      The tool to be checked
     * @param parent A parent frame to be used if a confirmation dialog
     *               is necessary
     * @return true if it is OK to close the tool
     */
    public boolean isOKToClose(Tool t, JFrame parent) {
        if (confirmDialog != null) {
            Toolkit.getDefaultToolkit().beep();
            confirmDialog.toFront();
            return false;
        }

        String[] alerts = t.getCloseAlerts();
        if (alerts == null || alerts.length == 0) {
            return true;
        } else {
            return isOKToExitOrClose(parent, alerts, CLOSE);
        }
    }

    /**
     * Check if it is OK to close all tools and exit the desktop.
     * If any tools have important state that needs to be saved, or active tasks
     * running, a confirmation dialog will be shown to allow the user to
     * cancel the operation in progress.
     *
     * @param parent A parent frame to be used if a confirmation dialog
     *               is necessary
     * @return true if it is OK to exit the desktop, and false otherwise.
     */
    public boolean isOKToExit(JFrame parent) {
        if (confirmDialog != null) {
            Toolkit.getDefaultToolkit().beep();
            confirmDialog.toFront();
            return false;
        }

        Vector<String> v = new Vector<>();

        Tool[] tools = getTools();
        for (Tool tool : tools) {
            String[] alerts = tool.getCloseAlerts();
            if (alerts != null) {
                v.addAll(Arrays.asList(alerts));
            }
        }

        if (v.isEmpty()) {
            return true;
        } else {
            return isOKToExitOrClose(parent, v.toArray(new String[v.size()]), EXIT);
        }
    }

    private boolean isOKToExitOrClose(JFrame parent, String[] alerts, int mode) {
        if (confirmDialog != null) {
            Toolkit.getDefaultToolkit().beep();
            confirmDialog.toFront();
            return false;
        }

        Integer m = Integer.valueOf(mode);

        if (alerts.length > 0) {
            // protect against reentrant calls by setting confirmDialog
            // while showing it

            StringWriter sw = new StringWriter();
            try {
                HTMLWriterEx out = new HTMLWriterEx(sw, uif.getI18NResourceBundle());
                out.startTag(HTMLWriterEx.HTML);
                out.writeLangAttr();
                out.startTag(HTMLWriterEx.HEAD);
                out.writeContentMeta();
                out.endTag(HTMLWriterEx.HEAD);
                out.startTag(HTMLWriterEx.BODY);
                out.writeStyleAttr("font-family: SansSerif");
                out.startTag(HTMLWriterEx.P);
                out.writeStyleAttr("margin-top:0");
                out.startTag(HTMLWriterEx.B);
                out.writeI18N("dt.confirm.head", m);
                out.endTag(HTMLWriterEx.B);
                out.endTag(HTMLWriterEx.P);
                out.startTag(HTMLWriterEx.P);
                out.startTag(HTMLWriterEx.I);
                out.writeI18N("dt.confirm.warn", m);
                out.endTag(HTMLWriterEx.I);
                out.endTag(HTMLWriterEx.P);
                out.startTag(HTMLWriterEx.UL);
                out.writeStyleAttr("margin-top:0; margin-bottom:0; margin-left:30");
                for (String alert : alerts) {
                    out.startTag(HTMLWriterEx.LI);
                    out.write(alert);
                }
                out.endTag(HTMLWriterEx.UL);
                out.startTag(HTMLWriterEx.P);
                out.writeStyleAttr("margin-top:5");
                out.writeI18N("dt.confirm.warn2", m);
                out.startTag(HTMLWriterEx.BR);
                out.writeI18N("dt.confirm.warn3", m);
                out.endTag(HTMLWriterEx.P);
                out.startTag(HTMLWriterEx.P);
                out.writeStyleAttr("margin-bottom:0");
                out.writeI18N("dt.confirm.tail", m);
                out.endTag(HTMLWriterEx.P);
                out.endTag(HTMLWriterEx.BODY);
                out.endTag(HTMLWriterEx.HTML);
                out.close();
            } catch (IOException e) {
                JavaTestError.unexpectedException(e);
            }

            JEditorPane body = new JEditorPane();
            body.setOpaque(false);
            body.setContentType("text/html");
            body.setText(sw.toString());
            body.setEditable(false);
            body.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            //body.setSize(new Dimension(3 * uif.getDotsPerInch(), Integer.MAX_VALUE));
            //System.err.println("DT.isOK size=" + body.getSize());
            //System.err.println("DT.isOK psize=" + body.getPreferredSize());
            String title = uif.getI18NString("dt.confirm.title", m);
            // can't use JOptionPane convenience methods because we want to set
            // default option to "No"

            final JOptionPane pane = new JOptionPane(body, JOptionPane.WARNING_MESSAGE);
            ActionListener l = e -> {
                pane.setValue(e.getSource());
                pane.setVisible(false);
            };
            JButton yesBtn = uif.createButton("dt.confirm.yes", l);
            JButton noBtn = uif.createButton("dt.confirm.no", l);
            pane.setOptions(new JComponent[]{yesBtn, noBtn});
            pane.setInitialValue(noBtn);

            confirmDialog = pane.createDialog(parent, title);
            confirmDialog.setVisible(true);
            confirmDialog.dispose();
            confirmDialog = null;

            return pane.getValue() == yesBtn;
        }

        return true;
    }

    /**
     * Check if it is OK to automatically exit JT Harness.
     * A warning dialog is posted to the user for a reasonable but short while
     * allowing the user to cancel the exit.
     *
     * @return true if the user does not respond within the available time,
     * or if the user allows the request; and false otherwise
     */
    public boolean isOKToAutoExit() {
        final int delay = 30/*seconds*/;
        final JTextArea body = new JTextArea();
        body.setOpaque(false);
        body.setText(uif.getI18NString("dt.autoExit.txt", Integer.valueOf(delay)));
        body.setEditable(false);
        body.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        body.setSize(new Dimension(4 * uif.getDotsPerInch(), Integer.MAX_VALUE));

        final JOptionPane pane = new JOptionPane(body, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        String title = uif.getI18NString("dt.confirm.title", Integer.valueOf(EXIT));
        final JDialog dialog = pane.createDialog(null, title);

        Timer timer = new Timer(1000, new ActionListener() {
            private int timeRemaining = delay;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (--timeRemaining == 0) {
                    pane.setValue(Integer.valueOf(JOptionPane.OK_OPTION));
                    dialog.setVisible(false);
                } else {
                    body.setText(uif.getI18NString("dt.autoExit.txt", Integer.valueOf(timeRemaining)));
                }
            }
        });

        timer.start();
        dialog.setVisible(true);
        timer.stop();

        Object value = pane.getValue();
        return value != null && value.equals(Integer.valueOf(JOptionPane.OK_OPTION));
    }

    /**
     * Save the current state of the desktop in the user's standard desktop file.
     */
    public void save() {
        save(getDesktopFile());
    }

    /**
     * Save the current state of the desktop in a specified desktop file.
     *
     * @param f the file in which to save the desktop
     */
    public void save(File f) {
        //System.err.println("DT: save to " + f);
        if (f == null) {
            return;
        }

        SortedMap<String, String> p = new TreeMap<>();

        int s = getStyle();
        if (s < NUM_STYLES) {
            p.put("dt.style", styleNames[s]);
            // backwards compatibility for JT3.1.x
            p.put("dt.class", jt31StyleClassNames[s]);
        }

        ensureViewInitialized();
        currView.saveDesktop(p);
        Preferences.access().save();

        p.put("file.count", String.valueOf(fileHistory.size()));
        int n = 0;
        for (FileHistoryEntry h : fileHistory) {
            p.put("fileHistory." + n + ".type", h.fileOpener.getFileType());
            p.put("fileHistory." + n + ".path", h.file.getPath());
            n++;
        }

        try {
            File dir = f.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(f);
                 OutputStream out = new BufferedOutputStream(fos)) {
                PropertyUtils.store(p, out, "JT Harness Desktop");
            }
        } catch (IOException e) {
            System.err.println(uif.getI18NString("dt.cantWriteDt.txt", e.getMessage()));
            //System.err.println("Error writing desktop file: " + e);
        }
    }

    /**
     * Restore the desktop from information in a saved desktop file.
     * If no such file exists, or if no tools are successfully started
     * from the info in the file, add a default tool.
     * The work will automatically performed on the main AWT EventQueue
     * thread.
     */
    public void restore() {
        restore(getDesktopFile());
    }

    /**
     * Restore the desktop from information in a specified file.
     * If no such file exists, or if no tools are successfully started
     * from the info in the file, add a default tool.
     * The work will automatically performed on the main AWT EventQueue
     * thread.
     *
     * @param file the file from which to load the data
     */
    public void restore(File file) {
        Map<String, String> p = getPreviousDesktop(file);
        restore0(p);
    }

    public void restoreHistory() {
        restoreHistory(getPreviousDesktop(getDesktopFile()));
    }

    private void restore0(final Map<String, String> p) {
        //System.err.println("DT: restore " + file);
        if (!EventQueue.isDispatchThread()) {
            invokeOnEventThread(() -> restore0(p));
            return;
        }

        restoreHistory(p);

        // ALERT!! NEEDS FIXING!
        // should use saved view info
        /*
        String dtClassName = (String) p.getProperty("dt.class");
        if (dtClassName != null) {
            try {
                if (theOne != null)
                    theOne.dispose();
                theOne = (Desktop) (Class.forName(dtClassName).newInstance());
            }
            catch (Throwable e) {
                // I18N
                System.err.println("Error loading saved desktop class: " + e);
            }
        }
        */
/*
        if (currView != null) {
            style = currView.getStyle(); // set default in case no valid style in desktop file
            currView.dispose();
        }

        int savedStyle;
        String s = (String) (p.get("dt.style"));
        if (s != null)
            savedStyle = indexOf(s, styleNames);
        else {
            // javatest 3.1 compatibility
            String c = (String) (p.get("dt.class"));
            savedStyle = (c == null ? -1 : indexOf(c, jt31StyleClassNames));
        }

        if (savedStyle != -1)
            style = savedStyle;
*/

        ensureViewInitialized();
        currView.restoreDesktop(p);

        if (getTools().length == 0) {
            addDefaultTool();
        }

        // select the previously selected tool, if given
        // else a default, if available
        Tool t = getSelectedTool();
        if (t == null) {
            Tool[] tools = getTools();
            if (tools.length > 0) {
                t = tools[0];
            }
        }
        if (t != null) {
            setSelectedTool(t);
        }

        //System.err.println("DT.restore: set visible");
        setVisible(true);
        //System.err.println("DT: restore done");

    }

    private void restoreHistory(Map<String, String> p) {
        HashMap<String, FileOpener> allOpeners = new HashMap<>();
        for (ToolManager m : toolManagers) {
            FileOpener[] mgrOpeners = m.getFileOpeners();
            if (mgrOpeners != null) {
                for (FileOpener fo : mgrOpeners) {
                    allOpeners.put(fo.getFileType(), fo);
                }
            }
        }

        try {
            fileHistory.clear();
            String c = p.get("file.count");
            if (c != null) {
                int count = Integer.parseInt(c);
                for (int i = 0; i < count; i++) {
                    try {
                        String ft = p.get("fileHistory." + i + ".type");
                        FileOpener fo = allOpeners.get(ft);
                        if (fo != null) {
                            String path = p.get("fileHistory." + i + ".path");
                            if (path != null && !path.isEmpty()) {
                                fileHistory.add(new FileHistoryEntry(fo, new File(path)));
                            }
                        }
                    } catch (Throwable e) {
                        // I18N
                        //System.err.println("Error loading saved file: " + e);
                        System.err.println(uif.getI18NString("dt.cantLoadHist.txt"));
                        e.printStackTrace();
                    }
                }
            }
        } catch (NumberFormatException ignore) {
            // ignore, for now
        }
    }

    /**
     * Show a Preferences window.
     *
     * @param parent the parent frame to be used for the preferences dialog
     */
    public void showPreferences(JFrame parent) {
        if (prefsPane == null) {
            prefsPane = new DesktopPrefsPane(this, uif);
        }
        if (colorPane == null) {
            colorPane = new ColorPrefsPane(uif);
        }

        Vector<PreferencesPane> v = new Vector<>();
        v.add(prefsPane);
        v.add(colorPane);
        for (ToolManager m : toolManagers) {
            PreferencesPane p = m.getPrefsPane();
            if (p != null) {
                v.add(p);
            }
        }

        PreferencesPane[] custom = getCustomPreferences();
        if (custom != null) {
            for (PreferencesPane aCustom : custom) {
                v.add(aCustom);
            }
        }

        PreferencesPane.showDialog(parent, preferences, v.toArray(new PreferencesPane[v.size()]), helpBroker);
    }

    /**
     * Allow for other custom prefs panes.  Current implementation scans
     * test suite properties for one called "prefsPane", which should be a
     * class name referring to a class which subclasses {@code Preferences.Pane}.
     * That class will be loaded and instantiated using the test suite's class loader.
     *
     * @return A set of prefs panes, beyond that of the currently active Tools.
     * Null if none.
     */
    private PreferencesPane[] getCustomPreferences() {
        List<PreferencesPane> al = new ArrayList<>();

        Set<String> customPrefsClasses = new HashSet<>();

        Tool[] tools = getTools();
        for (Tool tool : tools) {
            TestSuite[] tss = tool.getLoadedTestSuites();
            if (tss != null && tss.length > 0) {
                for (TestSuite ts : tss) {
                    // only process each test suite once
                    if (customPrefsClasses.contains(ts.getID())) {
                        continue;
                    } else {
                        customPrefsClasses.add(ts.getID());
                    }

                    String cls = ts.getTestSuiteInfo("prefsPane");
                    try {
                        if (cls != null) {
                            PreferencesPane pane =
                                    Class.forName(cls, true, ts.getClassLoader())
                                            .asSubclass(PreferencesPane.class).getDeclaredConstructor().newInstance();
                            al.add(pane);
                        }
                    } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                        // todo should print log entry
                    }
                }   // inner for j
            }
        }   // for i

        if (!al.isEmpty()) {
            PreferencesPane[] panes = new PreferencesPane[al.size()];
            al.toArray(panes);
            return panes;
        } else {
            return null;
        }
    }

    /**
     * Get an icon containing the JT Harness logo.
     *
     * @return an icon containing the JT Harness logo
     */
    public Icon getLogo() {
        return uif.createIcon("dt.logo");
    }

    /**
     * Dispose of any resources used by this object.
     */
    public void dispose() {
        if (currView != null) {
            currView.dispose();
        }
    }

    /**
     * Print a text message to the desktop logfile.
     * A single line of text which is as short as possible is highly
     * recommended for readability purposes.
     *
     * @param i18n a resource bundle containing the localized messages
     * @param key  a key into the resource bundle for the required message
     * @since 3.0.1
     */
    public void log(I18NResourceBundle i18n, String key) {
        ensureLogFileInitialized();
        logFile.log(i18n, key);
    }

    /**
     * Print a text message to the desktop logfile.
     * A single line of text which is as short as possible is highly
     * recommended for readability purposes.
     *
     * @param i18n a resource bundle containing the localized messages
     * @param key  a key into the resource bundle for the required message
     * @param arg  An argument to be formatted into the specified message.
     *             If this is a {@code Throwable}, its stack trace
     *             will be included in the log.
     * @since 3.0.1
     */
    public void log(I18NResourceBundle i18n, String key, Object arg) {
        ensureLogFileInitialized();
        logFile.log(i18n, key, arg);
    }

    /**
     * Print a text message to the desktop logfile.
     * A single line of text which is as short as possible is highly
     * recommended for readability purposes.
     *
     * @param i18n a resource bundle containing the localized messages
     * @param key  a key into the resource bundle for the required message
     * @param args An array of arguments to be formatted into the specified message.
     *             If the first arg is a {@code Throwable}, its stack
     *             trace will be included in the log.
     * @since 3.0.1
     */
    public void log(I18NResourceBundle i18n, String key, Object... args) {
        ensureLogFileInitialized();
        logFile.log(i18n, key, args);
    }

    private void ensureLogFileInitialized() {
        if (logFile == null) {
            File f;
            String s = System.getProperty("javatest.desktop.log");
            if (s == null) {
                File jtDir = Preferences.getPrefsDir();
                f = new File(jtDir, "log.txt");
            } else if (s.equals("NONE")) {
                f = null;
            } else {
                f = new File(s);
            }

            try {
                BackupPolicy p = BackupPolicy.simpleBackups(5);
                p.backup(f);
            } catch (IOException e) {
                // ignore? or save exception to write to logFile
            }

            logFile = f == null ? new LogFile() : new LogFile(f);
        }
    }

    /**
     * Check whether or not the desktop will save its state when the VM exits.
     *
     * @return true if the desktop will save its state when the VM exits, and false otherwise
     * @see #setSaveOnExit
     */
    public boolean getSaveOnExit() {
        return saveOnExit;
    }

    /**
     * Specify whether or not the desktop will save its state when the VM exits.
     *
     * @param b true if the desktop should save its state when the VM exits, and false otherwise
     * @see #getSaveOnExit
     */
    public void setSaveOnExit(boolean b) {
        saveOnExit = b;
    }

    /**
     * Check whether or not the desk view should restore saved tools state when the Harness is starting.
     *
     * @return true if the desk will restore its tools when the Harness is starting, and false otherwise
     * @see #setRestoreOnStart(boolean)
     */
    public boolean getRestoreOnStart() {
        return restoreOnStart;
    }

    /**
     * Specify whether or not the desk view should restore saved tools state when the Harness is starting.
     *
     * @param restoreOnStart true if the desk will restore its tools when the Harness is starting, and false otherwise
     * @see #getRestoreOnStart()
     */
    public void setRestoreOnStart(boolean restoreOnStart) {
        this.restoreOnStart = restoreOnStart;
    }

    // these are here to be shared with DesktopPrefsPane.
    void setTooltipsEnabled(boolean state) {
        ToolTipManager.sharedInstance().setEnabled(state);
    }

    /**
     * Unconditionally set the tooltip delay to the given setting.
     *
     * @param delay Delay time in ms or TTIP_DELAY_NONE.
     */
    void setTooltipDelay(int delay) {
        ToolTipManager.sharedInstance().setInitialDelay(delay);
    }

    /**
     * Unconditionally set the tooltip duration to the given setting.
     *
     * @param duration Duration time in ms or TTTIP_DURATION_FOREVER.
     */
    void setTooltipDuration(int duration) {
        if (duration == TTIP_DURATION_FOREVER) {
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        } else {
            ToolTipManager.sharedInstance().setDismissDelay(duration);
        }
    }

    public void printSetup() {
        ensurePrintAttrsInitialized();
        PrinterJob job = PrinterJob.getPrinterJob();
        job.pageDialog(printAttrs);
    }

    public void print(Printable printable) {

        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);

        if (services.length > 0) {
            ensurePrintAttrsInitialized();

            Component parent = getDialogParent();
            int x = (int) parent.getLocationOnScreen().getX() + parent.getWidth() / 2 - 250;
            int y = (int) parent.getLocationOnScreen().getY() + parent.getHeight() / 2 - 250;

            PrintService service = ServiceUI.printDialog(null, x, y, services,
                    services[0], flavor, printAttrs);
            if (service != null) {
                DocPrintJob job = service.createPrintJob();
                try {
                    Doc doc = new SimpleDoc(printable, flavor, null);

                    job.print(doc, printAttrs);
                } catch (PrintException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void ensurePrintAttrsInitialized() {
        if (printAttrs == null) {
            printAttrs = new HashPrintRequestAttributeSet();
        }
    }

    private void initHelpBroker() {
        URL u = null;      // URL for help set
        ClassLoader theLoader = null;

        if (commandContext != null) {
            Command[] cmds = commandContext.getCommands();
            // uses the first custom help loader found.
            // use a customized HelpBroker that will exit the VM when closed.
            for (Command cmd : cmds) {
                theLoader = cmd.getCustomHelpLoader();
                // could also upgrade this to accept a different help set name
                u = HelpSet.findHelpSet(theLoader, "jthelp.hs");
                if (u != null) {
                    break;
                }
            }   // for
        }

        if (u == null) {
            // could also upgrade this to accept a different help set name
            theLoader = this.getClass().getClassLoader();
            u = HelpSet.findHelpSet(theLoader, "com/sun/javatest/help/jthelp.hs");
        }

        if (u != null) {
            //HelpSet helpSet = new HelpSet(theLoader, u);
            //helpBroker = helpSet.createHelpBroker();
            helpBroker = new JTHelpBroker();

            // Not safe to run initPresentation in a background
            // thread, despite the JavaHelp specification.
            // so run it synchronously instead.
            // don't even run it synchronously
            //helpBroker.initPresentation();
            /*
            // launch background thread to init help in the
            // background, per JavaHelp suggestions
            Runnable r = new Runnable() {
                public void run() {
                    helpBroker.initPresentation();
                }
            };
            Thread t = new Thread(r);
            t.setPriority(Thread.MIN_PRIORITY + 1);  // not lowest, but pretty low
            t.start();
             *
             */
        }
    }

    private void initToolManagers() {
        // locate init file and load up the managers
        //System.err.println("Desktop.initToolManagers");

        try {
            Set<Object> s;
            String listOfManagersToLoad = System.getProperty(TOOL_MANAGERS_TO_LOAD);
            if (listOfManagersToLoad == null) {
                ManagerLoader ml = new ManagerLoader(ToolManager.class, System.err);
                ml.setManagerConstructorArgs(new Class<?>[]{Desktop.class}, this);
                s = ml.loadManagers(TOOLMGRLIST);
            } else {
                s = new HashSet<>();
                for (String name : listOfManagersToLoad.split(",")) {
                    s.add(Class.forName(name).getDeclaredConstructor(Desktop.class).newInstance(this));
                }
            }
            toolManagers = s.toArray(new ToolManager[s.size()]);
        } catch (IOException e) {
            throw new JavaTestError(uif.getI18NResourceBundle(),
                    "dt.cantAccessResource", new Object[]{TOOLMGRLIST, e});
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureViewInitialized() {
        if (currView != null) {
            return;
        }

        switch (style) {
            case MDI_STYLE:
                currView = new MDIDeskView(this);
                break;

            case SDI_STYLE:
                currView = new SDIDeskView(this);
                break;

            default:
                currView = new TabDeskView(this);
                break;
        }
    }


    //-------------------------------------------------------------------------

    /**
     * A class for an entry on the file history list.
     * It defines a file, and an object to open that file if required.
     */
    static class FileHistoryEntry {
        FileOpener fileOpener;
        File file;
        FileHistoryEntry(FileOpener fo, File f) {
            fileOpener = fo;
            file = f;
        }
    }
}
