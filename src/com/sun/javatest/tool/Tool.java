/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.help.HelpBroker;

import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.DynamicArray;

/**
 * A base class for tools to appear on the JT Harness desktop.
 */
public abstract class Tool extends JPanel
{
    /**
     * An observer interface for use by those that wishing to monitor changes
     * to a tool.
     */
    public interface Observer {
        /**
         * The title of a tool has been changed.
         * @param source The tool whose title has been changed.
         * @param newValue The new title for the tool.
         */
        void titleChanged(Tool source, String newValue);

        /**
         * The short title of a tool has been changed.
         * @param source The tool whose title has been changed.
         * @param newValue The new title for the tool.
         */
        void shortTitleChanged(Tool source, String newValue);

        /**
         * A tool has been disposed.
         * @param source the tool that has been disposed
         */
        void toolDisposed(Tool source);
    }

    //--------------------------------------------------------------------------

    /**
     * Add an observer to be notified of changes to a tool.
     * @param o the observer to be added
     * @see #removeObserver
     */
    synchronized public void addObserver(Observer o) {
        observers = (Observer[])DynamicArray.append(observers, o);
    }

    /**
     * Remove a previously registered observer so that it will no longer
     *  be notified of changes to a tool.
     * @param o the observer to be removed
     * @see #addObserver
     */
    synchronized public void removeObserver(Observer o) {
        observers = (Observer[])DynamicArray.remove(observers, o);
    }

    //--------------------------------------------------------------------------

    /**
     * Get the menu bar for a tool.
     * This should just contain the tool-specific menus. The desktop will
     * automatically add and display the standard menus on the menu bar.
     * @return a menu bar containing tool-specific menus
     */
    public abstract JMenuBar getMenuBar();

    /**
     * Set the title string for the tool. This will normally the displayed
     * by the desktop in the title bar of the window containing the tool.
     * @param key key to be used to get title text from resource bundle
     * @see #getTitle
     */
    protected void setI18NTitle(String key) {
        setLocalizedTitle(uif.getI18NString(key));
    }

    /**
     * Set the title string for the tool. This will normally the displayed
     * by the desktop in the title bar of the window containing the tool.
     * @param key key to be used to get title text from resource bundle
     * @param arg item for substitution into string from resource bundle
     * @see com.sun.javatest.util.I18NResourceBundle
     * @see #getTitle
     */
    protected void setI18NTitle(String key, Object arg) {
        setLocalizedTitle(uif.getI18NString(key, arg));
    }

    /**
     * Set the title string for the tool. This will normally the displayed
     * by the desktop in the title bar of the window containing the tool.
     * @param key key to be used to get title text from resource bundle
     * @param args items for substitution into string from resource bundle
     * @see com.sun.javatest.util.I18NResourceBundle
     * @see #getTitle
     */
    protected void setI18NTitle(String key, Object[] args) {
        setLocalizedTitle(uif.getI18NString(key, args));
    }

    /**
     * Set the title string for the tool. This will normally the displayed
     * by the desktop in the title bar of the window containing the tool.
     * @param newTitle the title string for the tool
     * @see #getTitle
     */
    private synchronized void setLocalizedTitle(String newTitle) {
        if (title == null ? newTitle == null : title.equals(newTitle))
            return;

        title = newTitle;
        for (int i = 0; i < observers.length; i++) {
            observers[i].titleChanged(this, title);
        }
    }

    /**
     * Get the title string for the tool.
     * @return the title string for the tool
     * @see #setI18NTitle(String)
     * @see #setI18NTitle(String,Object)
     * @see #setI18NTitle(String,Object[])
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set a short title for the tool. This will normally be displayed
     * by the desktop in situations where there is not room for the
     * full title.
     * @param newShortTitle the short title string for the tool
     * @see #getShortTitle
     */
    public void setShortTitle(String newShortTitle) {
        if (shortTitle == null ? newShortTitle == null : shortTitle.equals(newShortTitle))
            return;

        shortTitle = newShortTitle;
        for (int i = 0; i < observers.length; i++) {
            observers[i].shortTitleChanged(this, shortTitle);
        }
    }

    /**
     * Get a short title for the tool.
     * @return the short title string for the tool
     * @see #setShortTitle
     */
    public String getShortTitle() {
        if (shortTitle != null)
            return shortTitle;
        else if (title != null)
            return title;
        else
            return getName();
    }

    /**
     * Get a list of any test suites that are being used by this tool.
     * @return a list of test suites being used by the tool, or null if none
     */
    public TestSuite[] getLoadedTestSuites() {
        return null;
    }

    /**
     * Get a list of any work directories that are being used by this tool.
     * @return a list of work directories being used by the tool, or null if none
     */
    public WorkDirectory[] getLoadedWorkDirectories() {
        return null;
    }

    /**
     * Get the creation time for this tool. This is used by the desktop
     * to help preserve a consistent order when changing between desktop
     * styles.
     * @return a standard long cvalue, as returned by
     * System.currentTimeMillis, indicating the time the tool was created
     */
    public long getCreationTime() {
        return creationTime;
    }



    //--------------------------------------------------------------------------

    /**
     * Release any resources this tool may be referencing.
     * This is for "destroying" this tool instance.
     */
    public void dispose() {
        removeAll();
        uif.dispose();

        for (int i = observers.length - 1; i >= 0; i--)
            observers[i].toolDisposed(this);

        observers = new Observer[0];
    }

    //--------------------------------------------------------------------------

    /**
     * Create a tool object.
     * The resources used are from your resource bundle:
     * <table>
     * <tr><td><i>uiKey</i>.name <td>accessible name text
     * </table>
     * @param m the manager for this tool
     * @param uiKey the component name for this tool
     */
    protected Tool(ToolManager m, String uiKey) {
        super(new BorderLayout());
        setName(uiKey + ":" + (toolIndex++));
        setFocusable(false);
        manager = m;
        uif = new UIFactory(this, m.getDesktop().getHelpBroker());
        uif.setAccessibleName(this, uiKey);
        uif.setToolTip(this, uiKey);
        creationTime = System.currentTimeMillis();
    }

    /**
     * Create a tool object.
     * @param m the manager for this tool
     * @param uiKey the component name for this tool
     * @param helpID the help ID for context-sensitive help for this tool
     */
    protected Tool(ToolManager m, String uiKey, String helpID) {
        this(m, uiKey);

        HelpBroker b = getHelpBroker();
        if (b != null)
            b.enableHelp(this, helpID, null);
    }

    /**
     * Get the manager for this tool.
     * @return the manager for this tool
     */
    public ToolManager getManager() {
        return manager;
    }

    /**
     * Get the desktop for this tool.
     * @return the desktop for this tool
     */
    public Desktop getDesktop() {
        return manager.getDesktop();
    }

    /**
     * Get the help broker used to provide help for this tool.
     * @return the help broker used to provide help for this tool
     */
    public HelpBroker getHelpBroker() {
        return manager.getDesktop().getHelpBroker();
    }

    /**
     * Save information about a tool in a map, so that the
     * tool can be restored in a later invocation of JT Harness.
     * The tool must also implement the following method
     * <code>public static Tool restore(Map m)</code>
     * which will be invoked to recreate the tool.
     * @param m the map in which to store the significant state
     * of the tool
     */
    protected abstract void save(Map m);


    /**
     * Restore information about a tool from a map, and configure
     * the tool according this information.
     * @param m the map in which to store the significant state
     * of the tool
     */
    protected abstract void restore(Map m);


    /**
     * Get a list (if any) of the reasons why it might be inadvisable
     * to close the tool at this point. This will normally include
     * information about unsaved data or tasks in progress.
     * @return a list of alerts about why the user might not want
     * to close the tool at this time; the list may be empty or null
     * to indicate that there are no such reasons
     */
    protected String[] getCloseAlerts() {
        return null;
    }


    /**
     * Set the helpID for this component.  The help
     * will be shown if and when the focus is on this component
     * and the standard Help key (F1) is pressed.
     * @param helpID the ID for the help to be displayed
     */
    protected void setHelp(String helpID) {
        setHelp(this, helpID);
    }

    /**
     * Set the helpID for a specific component.  The help
     * will be shown if and when the focus is on the specified component
     * and the standard Help key (F1) is pressed.
     * @param comp the component in question
     * @param helpID the ID for the help to be displayed
     */
    protected void setHelp(Component comp, String helpID) {
        HelpBroker b = getHelpBroker();
        if (b != null) {
            if (comp instanceof JDialog) {
                JDialog d = (JDialog) comp;
                Desktop.addHelpDebugListener(d);
                b.enableHelpKey(d.getRootPane(), helpID, null);
            }
            else
                b.enableHelp(comp, helpID, null);
        }
    }

    /**
     * Set a button to be a Help button and to invoke the help
     * view set to a specific helpID.
     * @param comp the button to be made into a help button
     * @param helpID the ID for the help to be displayed when the
     * button is pressed
     */
    protected void setHelpOnButton(Component comp, String helpID) {
        HelpBroker b = getHelpBroker();
        if (b != null)
            b.enableHelpOnButton(comp, helpID, null);
    }

    /**
     * Get an array containing the set of ToolDialogs owned by this tool.
     * @return an array containing the set of ToolDialogs owned by this tool
     */
    public ToolDialog[] getToolDialogs() {
        if (toolDialogs == null)
            return new ToolDialog[0];

        ArrayList l = new ArrayList();
        for (Iterator iter = toolDialogs.iterator(); iter.hasNext(); ) {
            WeakReference r = (WeakReference) (iter.next());
            ToolDialog td = (ToolDialog) (r.get());
            if (td == null)
                iter.remove();
            else
                l.add(td);
        }

        return (ToolDialog[]) (l.toArray(new ToolDialog[l.size()]));
    }

    /**
     * Record a ToolDialog as belonging to this tool.
     * @param td the tool dialog to be registered as belonging to this tool.
     */
    void addToolDialog(ToolDialog td) {
        if (td == null)
            throw new NullPointerException();

        if (toolDialogs == null)
            toolDialogs = new ArrayList();

        toolDialogs.add(new WeakReference(td));
    }

    void setDeskView(DeskView view) {
        deskView = view;
    }

    DeskView getDeskView() {
        return deskView;
    }

    /**
     * The UI factory used to create GUI components.
     */
    protected final UIFactory uif;
    private ToolManager manager;
    private String title;
    private String shortTitle;
    private String helpID;
    private long creationTime;
    private List toolDialogs;
    private DeskView deskView;
    private Observer[] observers = new Observer[0];
    private static int toolIndex;
}
