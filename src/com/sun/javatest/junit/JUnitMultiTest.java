/*
 * $Id$
 *
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.junit;

import com.sun.javatest.lib.MultiTest;
import com.sun.javatest.Status;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 */
public abstract class JUnitMultiTest extends MultiTest {

    /** Creates a new instance of JUnitMultiTest setting specified classloader */
    public JUnitMultiTest(ClassLoader cl) {
        this.cl = cl;
    }

    /**
     * Programmatic entry point.
     */
    public Status run(String[] argv, PrintWriter stdout, PrintWriter stderr) {
        this.log = stderr;
        this.ref = stdout;
        setup(argv[0]);
        return run0(argv);
    }

    /**
     * Common method for running the test, used by all entry points.
     * <code>setTestCaseClass()</code> should have been invoked before calling this.
     */
    public abstract Status run0(String[] argv);

    protected static String exceptionToString(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        return stringWriter.toString();
    }

    /**
     * Entry point for standalone mode.
     */
    protected abstract void setup(String executeClass);

    protected void printStackTrace(Throwable t) {
        t.printStackTrace(log);
    }

    /**
     * Get current ClassLoader used to run tests
     * @return current classloader
     */
    public ClassLoader getClassLoader() {
        return cl;
    }

    /**
     * Set the ClassLoader used to run tests
     * @param cl ClassLoader to be set
     */
    protected void setClassLoader(ClassLoader cl) {
        this.cl = cl;
    }

    private ClassLoader cl;
}
