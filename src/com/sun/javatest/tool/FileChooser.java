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

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * A variant of JFileChooser, with support for file filters based on
 * filename extensions. The chooser also always starts in the user's
 * current directory (instead of home directory).
 */
public class FileChooser extends JFileChooser
{
    /**
     * Create a default file chooser.
     */
    public FileChooser() {
        this(true);
    }

    /**
     * Create a file chooser, specifying whether or not it should have
     * a generic file filter for "all files".
     * @param showAllFilesFilter true if the "All Files" filter should
     *        be available, and false if not.
     */
    public FileChooser(boolean showAllFilesFilter) {
        this.showAllFilesFilter = showAllFilesFilter;
        setCurrentDirectory(userDir);

        if (showAllFilesFilter)
            setAcceptAllFileFilterUsed(true);
        else
            setAcceptAllFileFilterUsed(false);
    }

    /**
     * Add a choosable file filter based on a filename extension.
     * This filter becomes the default filter.
     * @param extn the filename extension used to filter the files to be shown
     * @param desc the description for files selected by the filter
     */
    public void addChoosableExtension(String extn, String desc) {
        FileFilter f = new ExtensionFileFilter(extn, desc);
        addChoosableFileFilter(f);
        setFileFilter(f);

        // now that we have a filter, disable the "all files" filter
        if (!showAllFilesFilter && isAcceptAllFileFilterUsed())
            setAcceptAllFileFilterUsed(false);
    }

    /**
     * Get a chosen file filter extension.
     * @return an extension or null if not specified
     */
    public String getChosenExtension() {
        FileFilter f = getFileFilter();
        if (f instanceof ExtensionFileFilter) {
            return ((ExtensionFileFilter) f).getExtension();
        }
        return null;
    }


    /**
     * Allows to disable directories browsing
     * @param enableDirs <code>true</code> if directories browsing is allowed,
     * <code>false</code> otherwise
     */
    public void enableDirectories(boolean enableDirs) {
        this.enableDirs = enableDirs;
    }

    private boolean showAllFilesFilter;
    private boolean enableDirs = true;
    private static File userDir = new File(System.getProperty("user.dir"));

    private class ExtensionFileFilter extends FileFilter
    {
        ExtensionFileFilter(String extn, String desc) {
            this.extn = extn;
            this.desc = desc;
        }

        public boolean accept(File f) {
            return ((enableDirs && f.isDirectory())
                    || f.getName().endsWith(extn));
        }

        public String getDescription() {
            return desc;
        }

        public String getExtension() {
            return extn;
        }

        private String extn;
        private String desc;
    }
}

