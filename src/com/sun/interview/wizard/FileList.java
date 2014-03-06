/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview.wizard;


import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * A component that displays an editable list of files.
 */
public class FileList extends EditableList
{
    /**
     * Create a FileList.
     * @param uiKey A string used as the base of a key to look up resource values
     * for this item.
     * @param files An array of files to display as initial values in the list.
     */
    public FileList(String uiKey, File[] files) {
        super(uiKey, files);

        chooser = new JFileChooser();
        chooser.setName(uiKey + "chsr");
    }

    /**
     * Set the base directory for the file list.
     * Files will be returned relative to this directory if possible.
     * @param dir The directory to set as the base directory. If null,
     *          the user's current directory will be used.
     */
    public void setBaseDirectory(File dir) {
        baseDir = dir;
        if (dir == null)
            dir = new File(System.getProperty("user.dir"));
        chooser.setCurrentDirectory(dir);
    }

    /**
     * Add a filter to the set of file filters used when adding a new file to the list.
     * @param filter A file filter to add to the list of choosable file filters.
     */
    public void addFilter(FileFilter filter) {
        /*
        if (chooser.isAcceptAllFileFilterUsed())
            chooser.setAcceptAllFileFilterUsed(false);
        */
        chooser.addChoosableFileFilter(filter);
    }

    /**
     * Set the file selection mode to be used when adding files.
     * @param mode The mode to be used.
     * @see JFileChooser#FILES_ONLY
     * @see JFileChooser#DIRECTORIES_ONLY
     * @see JFileChooser#FILES_AND_DIRECTORIES
     */
    public void setFileSelectionMode(int mode) {
        chooser.setFileSelectionMode(mode);
    }

    /**
     * Get the set of files in the list.
     * @return the set of files currently in the list
     */
    public File[] getFiles() {
        return (File[]) (getItems(File.class));
    }

    protected Object getNewItem() {
        chooser.setDialogTitle(i18n.getString("flst.addFile.title"));
        int opt = chooser.showDialog(FileList.this, i18n.getString("flst.addFile.ok"));
        if (opt == JFileChooser.APPROVE_OPTION)
            return getBaseRelativeFile(chooser.getSelectedFile());
        else
            return null;
    }

    protected Object getNewItem(Object oldItem) {
        File f = (File) oldItem;
        if (!f.isAbsolute() && baseDir != null)
            f = new File(baseDir, f.getPath());
        chooser.setSelectedFile(f);
        chooser.setDialogTitle(i18n.getString("flst.changeFile.title"));
        int opt = chooser.showDialog(FileList.this, i18n.getString("flst.changeFile.ok"));
        if (opt == JFileChooser.APPROVE_OPTION)
            return getBaseRelativeFile(chooser.getSelectedFile());
        else
            return null;
    }

    private File getBaseRelativeFile(File f) {
        if (baseDir == null)
            return f;

        String bp = baseDir.getPath();
        if (f.getPath().startsWith(bp + File.separatorChar))
            return new File(f.getPath().substring(bp.length() + 1));

        return f;
    }


    private JFileChooser chooser;
    private File baseDir;

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
}
