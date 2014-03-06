/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview;

import java.io.File;

/**
 * A filter which accepts files based on their extension.
 */
public class ExtensionFileFilter implements FileFilter
{

    /**
     * Create a filter which accepts files based on their extension.
     * @param extn The required extension for files that are to be
     *          accepted by this filter.
     * @param description A short string describing the filter.
     */
    public ExtensionFileFilter(String extn, String description) {
        this.extns = new String[] {extn};
        this.description = description;
    }

    /**
     * Create a filter which accepts files based on their extension.
     * @param extns Any array of permissible extensions for
     *          files that are to be accepted by this filter.
     * @param description A short string describing the filter.
     */
    public ExtensionFileFilter(String[] extns, String description) {
        this.extns = extns;
        this.description = description;
    }

    /**
     * Specify whether or not this filter should perform case-sensitive matching for
     * extensions.
     * @param b true if this filter should perform case-sensitive matching for
     * extensions, and false otherwise
     * @see #isCaseSensitive
     */
    public void setCaseSensitive(boolean b) {
        caseSensitive = b;
    }

    /**
     * Check whether or not this filter should perform case-sensitive matching for
     * extensions.
     * @return true if this filter should perform case-sensitive matching for
     * extensions, and false otherwise
     * @see #setCaseSensitive
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public boolean accept(File f) {
        for (int i = 0; i < extns.length; i++)
            if (endsWith(f.getName(), extns[i]))
                return true;
        return false;
    }

    public boolean acceptsDirectories() {
        return false;
    }

    /**
     * Get a short description for this filter.
     * @return a short description of this filter
     */
    public String getDescription() {
        return description;
    }

    /**
     * Ensure that a pathname ends with one of the extensions accepted
     * by this filter.  If it does not, one is appended
     * @param path The path to be checked.
     * @return the original path if it already ended with a valid extension,
     *          or if it was null or empty;
     *          otherwise, a copy of the path is returned, with a valid
     *          extension added on.
     */
    public String ensureExtension(String path) {
        if (path == null || path.length() == 0)
            return path;

        for (int i = 0; i < extns.length; i++)
            if (endsWith(path, extns[i]))
                return path;

        return (path + extns[0]);
    }


    /**
     * Ensure that a filename ends with one of the extensions accepted
     * by this filter.  If it does not, one is appended
     * @param file The file to be checked.
     * @return the original file if it already ended with a valid extension,
     *          or if it was null or empty;
     *          otherwise, a new file is returned, whose path is a copy
     *          of the original, with a valid extension added on.
     */
    public File ensureExtension(File file) {
        if (file == null)
            return null;

        String path = file.getPath();
        String newPath = ensureExtension(path);
        return (newPath.equals(path) ? file : new File(newPath));
    }

    private boolean endsWith(String s, String sfx) {
        if (caseSensitive)
            return s.endsWith(sfx);

        int slen = s.length();
        int sfxlen = sfx.length();
        if (slen < sfxlen)
            return false;

        int offset = slen - sfxlen;
        for (int i = 0; i < sfxlen; i++) {
            char c1 = s.charAt(offset + i);
            char c2 = sfx.charAt(i);
            if (Character.toLowerCase(c1) != Character.toLowerCase(c2))
                return false;
        }

        return true;
    }

    private String[] extns;
    private String description;
    private boolean caseSensitive;
}
