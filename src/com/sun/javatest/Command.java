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
package com.sun.javatest;

import java.io.PrintWriter;

/**
 * Command provides the ability to invoke arbitrary Java
 * code from within a Script. Standard implementations may exist to compile
 * or execute tests, in the same JVM as the harness or its agent,
 * in separate processes, or even on a separate machine.
 * Custom implementations can also be used.
 */
public abstract class Command
{
    /**
     * The method that that does the work of the command.
     * @param args      Command-specific options and arguments
     * @param out1      A stream to which to report messages and errors.
     *                  This stream was previously called "log".
     * @param out2      An additional stream to which to write output.
     *                  This stream was previously called "ref".
     * @return          The result of the command
     */
    public abstract Status run(String[] args, PrintWriter out1, PrintWriter out2);

    /**
     * Set a class loader that to be used if this command needs to dynamically
     * load additional classes.
     * @param cl the class loader to be used
     * @see #getClassLoader
     */
    public void setClassLoader(ClassLoader cl) {
        loader = cl;
    }

    /**
     * Get the class loader to be used if this command needs to dynamically
     * load additional classes.
     * @return the class loader to be used
     * @see #setClassLoader
     */
    public ClassLoader getClassLoader() {
        return loader;
    }

    private ClassLoader loader;
}
