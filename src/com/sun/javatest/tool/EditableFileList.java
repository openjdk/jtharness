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
package com.sun.javatest.tool;

import java.io.File;
import javax.swing.JFileChooser;

/**
 * A component that displays an editable list of filenames.
 */
public class EditableFileList extends EditableList
{
    /**
     * Create an empty component.
     */
    public EditableFileList() {
    }

    /**
     * Create an empty component, that uses a given file chooser
     * to allow the user to specify new filenames to be added.
     * @param c the file chooser to be used
     * @see #setChooser
     */
    public EditableFileList(JFileChooser c) {
        setChooser(c);
    }

    /**
     * Set the base directory to be displayed by the file chooser.
     * "null" may be used to mean "the user's current directory".
     * @param dir the directory to be displayed by the file chooser
     */
    public void setBaseDirectory(File dir) {
        baseDir = dir;
        if (chooser != null) {
            if (dir == null)
                dir = new File(System.getProperty("user.dir"));
            chooser.setCurrentDirectory(dir);
        }
    }

    /**
     * Set the file chooser used to display filenames when the user
     * wants to add another file to the list.
     * @param c the chooser to be used
     */
    public void setChooser(JFileChooser c) {
        if (c == null)
            throw new NullPointerException();
        chooser = c;
    }

    /**
     * Set the items in the list. Any previous items are removed first.
     * The items should be files, or strings (which will be turned into
     * files.)
     * @param items the array of strings or files to be put in the list.
     * @see #getItems
     */
    public void setItems(Object[] items) {
        listModel.clear();
        if (items == null)
            return;

        for (int i = 0; i < items.length; i++) {
            Object o = items[i];
            if (o instanceof File)
                listModel.addElement(o);
            else if (o instanceof String)
                listModel.addElement(new File((String) o));
            else
                throw new IllegalArgumentException(o.toString());
        }
    }

    /**
     * Get the files currently in the list.
     * @return the files currently in the list
     * @see #setFiles
     */
    public File[] getFiles() {
        File[] files = new File[listModel.size()];
        listModel.copyInto(files);
        return files;
    }

    /**
     * Set the files in the list, replacing any files currently there.
     * @param files the files to be put in the list
     * @see #getFiles
     */
    public void setFiles(File[] files) {
        setItems(files);
    }

    /**
     * Invoked to get a new item to put in the list, when the user clicks
     * the "Add" button". The current file chooser will be shown. If the
     * user selects a file, it will be returned as the result of this method;
     * otherwise, the result will be null, to indicate that no file was selected.
     * @return a file to be added to the list, or null if none
     */
    protected Object getNewItem() {
        if (chooser == null)
            chooser = new JFileChooser();
        int opt = chooser.showDialog(this, uif.getI18NString("list.add.select.txt"));
        if (opt != JFileChooser.APPROVE_OPTION)
            return null;

        File f = chooser.getSelectedFile();
        if (baseDir != null) {
            String bp = baseDir.getPath();
            if (f.getPath().startsWith(bp + File.separatorChar))
                f = new File(f.getPath().substring(bp.length() + 1));
        }
        return f;
    }

    private JFileChooser chooser;
    private File baseDir;
}
