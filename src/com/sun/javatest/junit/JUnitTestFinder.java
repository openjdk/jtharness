/*
 * $Id$
 *
 * Copyright (c) 2008, 2009, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.TestFinder;
import com.sun.javatest.finder.CommentStream;
import com.sun.javatest.finder.JavaCommentStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public abstract class JUnitTestFinder extends TestFinder {

    /** Creates a new instance of JUnitTestFinder */
    public JUnitTestFinder() {
    }

    /**
     * Decode the arg at a specified position in the arg array.  If overridden
     * by a subtype, the subtype should try and decode any arg it recognizes,
     * and then call super.decodeArg to give the superclass(es) a chance to
     * recognize any arguments.
     *
     * @param args The array of arguments
     * @param i    The next argument to be decoded.
     * @return     The number of elements consumed in the array; for example,
     *             for a simple option like "-v" the result should be 1; for an
     *             option with an argument like "-f file" the result should be
     *             2, etc.
     * @throws TestFinder.Fault If there is a problem with the value of the current arg,
     *             such as a bad value to an option, the Fault exception can be
     *             thrown.  The exception should NOT be thrown if the current
     *             arg is unrecognized: in that case, an implementation should
     *             delegate the call to the supertype.
     */
    protected void decodeAllArgs(String[] args) throws Fault {
        // supports selection of two modes -
        // 1 - scan for .java files, use to locate .class file
        // 2 - just scan for .class files
        for (int i = 0; i < args.length; i++) {
            if ("-scanClasses".equalsIgnoreCase(args[i])) {
                scanClasses = true;
                addExtension(".class", null);
            } else if ("-verbose".equalsIgnoreCase(args[i])) {
                verbose = true;
            } else
                super.decodeArg(args, i);
        }   // for

        // this establishes the default if the user did not select
        // class scanning
        if (!scanClasses)
            addExtension(".java", JavaCommentStream.class);
    }

    /**
     * Get the name of the file currently being scanned.
     * @return the name of the file currently being scanned.
     */
    // Ideally, we should be able to get the current line number as well,
    // (for error messages)
    protected File getCurrentFile() {
        return currFile;
    }

    /**
     * Exclude all files with a particular name from being scanned.
     * This will typically be for directories like SCCS, Codemgr_wsdata, etc
     * @param name The name of files to be excluded.
     */
    public void exclude(String name) {
        excludeList.put(name, name);
    }

    /**
     * Exclude all files with particular names from being scanned.
     * This will typically be for directories like SCCS, Codemgr_wsdata, etc
     * @param names The names of files to be excluded.
     */
    public void exclude(String[] names) {
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            excludeList.put(name, name);
        }
    }

    /**
     * Nominate a class to read files that have a particular extension.
     * @param extn      The extension for which this class is to be used
     * @param commentStreamClass
     *                  A class to read files of a particular extension.
     *                  The class must be a subtype of CommentStream
     */
    public void addExtension(String extn, Class commentStreamClass) {
        if (!extn.startsWith("."))
            throw new IllegalArgumentException("extension must begin with `.'");
        if (commentStreamClass != null && !CommentStream.class.isAssignableFrom(commentStreamClass))
            throw new IllegalArgumentException("class must be a subtype of " + CommentStream.class.getName());

        extensionTable.put(extn, commentStreamClass);
    }

    /**
     * Get the class used to handle an extension.
     * @param extn The extension in question
     * @return the class previously registered with addExtension
     */
    public Class getClassForExtension(String extn) {
        return (Class)extensionTable.get(extn);
    }

    /**
     * Call to register the methods which are test methods.
     */
    public void foundTestMethod(String name) {
        testMethods.add(name);
    }

    protected boolean verbose = false;
    protected Map tdValues = new HashMap();
    protected boolean scanClasses = false;
    protected File currFile;
    protected HashMap excludeList   = new HashMap();
    protected HashMap extensionTable = new HashMap();
    protected ArrayList<String> testMethods;
    protected static final String[] excludeNames = {
        "SCCS", "deleted_files", ".svn"
    };
}
