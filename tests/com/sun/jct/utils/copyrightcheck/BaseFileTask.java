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
package com.sun.jct.utils.copyrightcheck;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;

/**
 * Base class for most of the tasks. Implements looping through
 * all the files configured for this task in the user's build.
 */
public abstract class BaseFileTask extends BaseTask {

    /**
     * Main execution entry. Can be overridden by subclasses, but subclasses
     * must invoke this implementation to preserve standard behaviour.
     * <p>
     * This implementation loops through all the files and invokes
     * {@link #processFile(File)}.
     *
     * @throws BuildException
     */
    public void execute() throws BuildException {
        super.execute();
        boolean taskFailed = false;

        DirectoryScanner ds = super.getDirectoryScanner(getDir());
        String[] files = ds.getIncludedFiles();

        for (int i = 0; i < files.length; i++) {
            File file = new File(getDir(), files[i]);
            if (!processFile(file)) {
                taskFailed = true;
            }
        }

        taskFinished();

        if (taskFailed && isFailOnError()) {
            throw new BuildException(getTaskName() + " task found errors.");
        }

    }

    /**
     * Returns <code>true</code> in case of positive results,
     * <code>false</code> in case of error.
     *
     * @param file
     *            The file to process.
     * @return true or false depending on results.
     * @throws BuildException
     *             in case of build problems.
     */
    protected abstract boolean processFile(File file);
}
