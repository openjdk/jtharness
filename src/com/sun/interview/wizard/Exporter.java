/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
import java.io.IOException;

import com.sun.interview.Interview;

/**
 * This interface defines the ability to export the answers
 * contained in a configuration to a custom file format.
 */
public interface Exporter
{
    /**
     * Get the name of this exporter, as might be presented to a user.
     * @return the name of this exporter
     */
    String getName();

    /**
     * Get the set of file extensions supported by this exporter.
     * @return an array of file extensions supported by this exporter
     */
    String[] getFileExtensions();

    /**
     * Get a description of the set of file extensions supported by
     * this exporter.
     * @return a string containing a short description
     */
    String getFileDescription();

    /**
     * Check whether this exporter is currently "usable"-- for example,
     * an incomplete interview may not be exportable.
     * @return true if the exporter is ready for {@link #export} to be called.
     */
    boolean isExportable();

    /**
     * Export the data to the given file.
     * @param f The file to which to write the data
     * @throws IOException if there is a problem while writing the file
     * @throws Interview.Fault if there is a problem exporting the data
     *           from the interview.
     */
    void export(File f) throws IOException, Interview.Fault;
}
