/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.WeakHashMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * A class to maintain a history of recently used files. The history is
 * maintained in a specified file in a WorkDirectory, and can be
 * dynamically added to a menu by means of a Listener class.
 * The format of the file is one file per line, with most recently
 * added entries appearing first.  Lines beginning with <code>#</code> are ignored.
 */
public class FileHistory
{
    /**
     * Get a shared FileHistory object for a specified file and work directory.
     * @param wd The work directory in which the history file is maintained.
     * @param name The name of the file within the work direectory's jtData/
     * subdirectory.
     * @return the specified FileHistory object
     */
    public static FileHistory getFileHistory(WorkDirectory wd, String name) {
        if (cache == null)
            cache = new WeakHashMap(8);

        // first, get a map for all files in this wd
        HashMap map = (HashMap) (cache.get(wd));
        if (map == null) {
            map = new HashMap(8);
            cache.put(wd, map);
        }

        // then, get the FileHistory for the specified file
        FileHistory h = (FileHistory) (map.get(name));
        if (h == null) {
            h = new FileHistory(wd, name);
            map.put(name, h);
        }

        return h;
    }


    /**
     * Get a shared FileHistory object for a specified file and path to work directory.
     * @param wdFile The path th work directory in which the history file is maintained.
     * @param name The name of the file within the work direectory's jtData/
     * subdirectory.
     * @return the specified FileHistory object
     */
    public static FileHistory getFileHistory(File wdFile, String name) {
        if (cache == null)
            cache = new WeakHashMap(8);

        if (!WorkDirectory.isWorkDirectory(wdFile))
            return null;

        // let's find in the cache work dir corresponding to the path
        Iterator it = cache.keySet().iterator();
        WorkDirectory wd = null;
        while (it.hasNext()) {
            WorkDirectory tempWD = ((WorkDirectory)it.next());
            if (tempWD.getRoot().equals(wdFile)) {
                wd = tempWD;
                break;
            }
        }
        if (wd != null)
            return FileHistory.getFileHistory(wd, name);
        else
            return null;
    }

    /**
     * Add a new file to the history.
     * The file in the work directory for this history will be updated.
     * @param file the file to be added to the history
     */
    public void add(File file) {
        ensureEntriesUpToDate();

        file = file.getAbsoluteFile();
        entries.remove(file);
        entries.add(0, file);

        writeEntries();
    }

    /**
     * Get the most recent entries from the history. Only entries for
     * files that exist on this system are returned. Thus the history
     * can accommodate files for different systems, which will likely not
     * exist on all systems on which the history is used.
     * @param count the number of most recent, existing files
     * to be returned.
     * @return an array of the most recent, existing entries
     */
    public File[] getRecentEntries(int count) {
        ensureEntriesUpToDate();

        // scan the entries, skipping those which do not exist,
        // collecting up to count entries. Non-existent entries are
        // skipped but not deleted because they might be for other
        // platforms.
        Vector v = new Vector();
        for (int i = 0; i < entries.size() && v.size() < count; i++) {
            File f = (File) (entries.elementAt(i));
            if (f.exists())
                v.add(f);
        }
        File[] e = new File[v.size()];
        v.copyInto(e);

        return e;
    }

    /**
     * Get the latest valid entry from a file history object. An entry
     * is valid if it identifies a file that exists on the current system.
     * @return the latest valid entry from afile history object, or null
     * if none found.
     */
    public File getLatestEntry() {
        ensureEntriesUpToDate();

        // scan the entries, skipping those which do not exist,
        // looking for the first entry. Non-existent entries are
        // skipped but not deleted because they might be for other
        // platforms.
        for (int i = 0; i < entries.size(); i++) {
            File f = (File) (entries.elementAt(i));
            if (f.exists())
                return f;
        }

        return null;
    }

    public File getRelativeLatestEntry(String newRoot, String oldRoot) {
        ensureEntriesUpToDate();

        for (int i = 0; i < entries.size(); i++) {
            File f = (File) (entries.elementAt(i));
            if (f.exists()) {
                return f;
            } else {
                String sf = f.getPath();
                String[] diff = WorkDirectory.getDiffInPaths(newRoot, oldRoot);
                if (diff != null) {
                    File toCheck = new File(diff[0] + sf.substring(diff[1].length()));
                    if (toCheck.exists()) {
                        return toCheck;
                    }
                }

            }
        }
        return null;
    }


    private FileHistory(WorkDirectory workDir, String name) {
        workDirRef = new WeakReference(workDir); // just used for logging errors
        this.name = name;
        historyFile = workDir.getSystemFile(name);
    }

    private void ensureEntriesUpToDate() {
        if (entries == null || historyFile.lastModified() > historyFileLastModified)
            readEntries();
    }

    private void readEntries() {
        if (entries == null)
            entries = new Vector();
        else
            entries.clear();

        if (historyFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(historyFile));
                String line;
                while ((line = br.readLine()) != null) {
                    String p = line.trim();
                    if (p.length() == 0 || p.startsWith("#"))
                        continue;
                    entries.add(new File(p));
                }
                br.close();
            }
            catch (IOException e) {
                WorkDirectory workDir = (WorkDirectory) (workDirRef.get());
                workDir.log(i18n, "fh.cantRead", new Object[] { name, e } );
            }

            historyFileLastModified = historyFile.lastModified();
        }
    }

    private void writeEntries() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(historyFile));
            bw.write("# Configuration File History");
            bw.newLine();
            bw.write("# written at " + (new Date()));
            bw.newLine();
            for (int i = 0; i < entries.size(); i++) {
                bw.write(entries.elementAt(i).toString());
                bw.newLine();
            }
            bw.close();
        }
        catch (IOException e) {
            WorkDirectory workDir = (WorkDirectory) (workDirRef.get());
            workDir.log(i18n, "fh.cantWrite", new Object[] { name, e } );
        }

        historyFileLastModified = historyFile.lastModified();
    }


    /**
     * A class that will dynamically add the latest entries for a
     * FileHistory onto a menu. To do this, an instance of this class
     * should be added to the menu with
     * {@link javax.swing.JMenu#addMenuListener addMenuListener}.
     */
    public static class Listener implements MenuListener {
        /**
         * Create a Listener that can be used to dynamically add the
         * latest entries from a FileHistory onto a menu.  The dynamic
         * entries will be added to the end of the menu when it is
         * selected. Any previous values added by this listener
         * will automatically be removed.
         * @param l An ActionListener that will be notified when
         * any of the dynamic menu entries are invoked. When this
         * action listener is notified, the action command will be
         * the path of the file. The corresponding File object will
         * be registered on the source as a client property named
         * FILE.
         */
        public Listener(ActionListener l) {
            this(null, -1, l);
        }

        /**
         * Create a Listener that can be used to dynamically add the
         * latest entries from a FileHistory onto a menu.
         * Any previous values added by this listener will automatically
         * be removed.
         * @param o The position in the menu at which to insert the
         * dynamic entries.
         * @param l An ActionListener that will be notified when
         * any of the dynamic menu entries are invoked. When this
         * action listener is notified, the action command will be
         * the path of the file. The corresponding File object will
         * be registered on the source as a client property named
         * FILE.
         */
        public Listener(int o, ActionListener l) {
            this(null, o, l);
        }

        /**
         * Create a Listener that can be used to dynamically add the
         * latest entries from a FileHistory onto a menu.
         * Any previous values added by this listener will automatically
         * be removed.
         * @param h The FileHistory from which to determine the
         * entries to be added.
         * @param o The position in the menu at which to insert the
         * dynamic entries.
         * @param l An ActionListener that will be notified when
         * any of the dynamic menu entries are invoked. When this
         * action listener is notified, the action command will be
         * the path of the file. The corresponding File object will
         * be registered on the source as a client property named
         * FILE.
         */
        public Listener(FileHistory h, int o, ActionListener l) {
            history = h;
            offset = o;
            clientListener = l;
        }

        /**
         * Get the FileHistory object from which to obtain the dynamic menu
         * entries.
         * @return the FileHistory object from which to obtain the dynamic menu
         * entries
         * @see #setFileHistory
         */
        public FileHistory getFileHistory() {
            return history;
        }

        /**
         * Specify the FileHistory object from which to obtain the dynamic menu
         * entries.
         * @param h the FileHistory object from which to obtain the dynamic menu
         * entries
         * @see #getFileHistory
         */
        public void setFileHistory(FileHistory h) {
            history = h;
        }

        public void menuSelected(MenuEvent e) {
            // Add the recent entries, or a disabled marker if none
            JMenu menu = (JMenu) (e.getSource());
            File[] entries = (history == null ? null : history.getRecentEntries(5));
            if (entries == null || entries.length == 0) {
                JMenuItem noEntries = new JMenuItem(i18n.getString("fh.empty"));
                noEntries.putClientProperty(FILE_HISTORY, this);
                noEntries.setEnabled(false);
                if (offset < 0)
                    menu.add(noEntries);
                else
                    menu.insert(noEntries, offset);
            }
            else {
                for (int i = 0; i < entries.length; i++) {
                    JMenuItem mi = new JMenuItem(i + " " + entries[i].getPath());
                    mi.setActionCommand(entries[i].getPath());
                    mi.addActionListener(clientListener);
                    mi.putClientProperty(FILE, entries[i]);
                    mi.putClientProperty(FILE_HISTORY, this);
                    mi.setMnemonic('0' + i);
                    if (offset < 0)
                        menu.add(mi);
                    else
                        menu.insert(mi, offset + i);
                }
            }
        }

        public void menuDeselected(MenuEvent e) {
            removeDynamicEntries((JMenu) (e.getSource()));
        }

        public void menuCanceled(MenuEvent e) {
            removeDynamicEntries((JMenu) (e.getSource()));
        }

        private void removeDynamicEntries(JMenu menu) {
            // Clear out any old menu items previously added by this
            // menu listener; remove them from bottom up because
            // removing an item affects index of subsequent items
            for (int i = menu.getItemCount() -1; i >= 0; i--) {
                JMenuItem mi = menu.getItem(i);
                if (mi != null && mi.getClientProperty(FILE_HISTORY) == this)
                    menu.remove(mi);
            }
        }

        private FileHistory history;
        private int offset;
        private ActionListener clientListener;
    }

    private WeakReference workDirRef;
    private String name;
    private File historyFile;
    private long historyFileLastModified;
    private Vector entries;

    /**
     * The name of the client property used to access the File that identifies
     * which dynamically added menu entry has been selected.
     * @see Listener
     */
    public static final String FILE = "file";

    private static WeakHashMap cache;
    private static final String FILE_HISTORY = "fileHistory";
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(FileHistory.class);

}
