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
package com.sun.javatest.lib;

import java.io.File;
import java.io.PrintWriter;

import com.sun.javatest.Script;
import com.sun.javatest.Status;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.TestDescription;

/**
 * A Script designed to compile/execute a test.
 */
public class APIScript extends Script
{
    /// XXX this code really needs to be re-visited!

    /**
     * The method that interprets the tags provided in the test description and
     * performs actions accordingly.
     *
     * @param args Any arguments that the APIScript may use.  Currently
     *             there are none (value ignored).
     * @param td   The current TestDescription.
     * @param env  The test environment giving the details of how to run the
     *             test.
     * @return     The result of running the script on the given test
     *             description.
     */
    public Status run(String [] args, TestDescription td, TestEnvironment env) {

        PrintWriter trOut = getTestResult().getTestCommentWriter();

        Status status = decodeArgs(args);
        if (status != null)
            return status;

        // XXX This isn't everything.  We need to make sure that this is a
        // XXX reasonable subset of JCKScript.  Do we want to handle all options
        // XXX available there?  How about the keywords?

        // compile
        File [] srcs = td.getSourceFiles();
        Status compileStatus;
        if (precompileClassDir == null) {
            trOut.println("Unconditionally compiling all sources");
            compileStatus = compileTogether(TEST_COMPILE, srcs);
        } else {
            trOut.println("Compiling sources only if necessary");
            compileStatus = compileIfNecessary(TEST_COMPILE, srcs, precompileClassDir);
        }

        if (!compileStatus.isPassed())
            return compileStatus;

        // execute
        String executeClass  = td.getParameter("executeClass");
        String executeArgs   = td.getParameter("executeArgs");
        Status executeStatus = execute(TEST_EXECUTE, executeClass, executeArgs);

        return executeStatus;
    } // run()

    //----------private methods-------------------------------------------------

    private Status decodeArgs(String [] args) {
        // decode args
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-precompileClassDir") && (i+1 < args.length))
                precompileClassDir = args[++i];
            else
                return Status.failed(UNRECOGNIZED_ARG + args[i]);
        }

        return null;
    } // init()

    //----------member variables------------------------------------------------

    private static final String TEST_COMPILE = "testCompile";
    private static final String TEST_EXECUTE = "testExecute";

    // special option to use compileIfNecessary
    private String precompileClassDir;

    private static final String
        UNRECOGNIZED_ARG      = "Unrecognized argument for script: ";
}
