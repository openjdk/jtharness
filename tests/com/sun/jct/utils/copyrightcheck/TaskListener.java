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

/**
 * Generic listener interface to monitor Task execution
 * process.
 */
public interface TaskListener {
    /**
     * Invoked when the task execution has just started.
     */
    void taskStarted();

    /**
     * Invoked when the task is done.
     */
    void taskFinished();

    /**
     * Notifies the listener that the specified file is now being checked by the
     * task.
     *
     * @param file
     *            The file being checked by the task.
     */
    void processingFile(File file);

    /**
     * Invoked to notify about error in the specified file.
     *
     * @param file
     *            The file that contains an error.
     * @param errorMessage
     *            The error message.
     */
    void errorInFile(File file, String errorMessage);
}
