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
import com.sun.javatest.Script;
import com.sun.javatest.Status;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.util.StringArray;

/**
 * A Script to compile/execute a standard test.
 */
public class StdTestScript extends Script
{
    public Status run(String[] args, TestDescription td, TestEnvironment env) {
        try {
            String[] m = env.lookup("script.mode");
            if (m != null && m.length == 1)
                setMode(m[0]);
        }
        catch (TestEnvironment.Fault e) {
            return Status.failed("error determining script mode: " + e.getMessage());
        }

        boolean compile = false;
        boolean execute = false;
        boolean expectFail = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-certify")) {
                compile = false;
                execute = true;
            }
            else if (arg.equals("-precompile")) {
                compile = true;
                execute = false;
            }
            else if (arg.equals("-developer")) {
                compile = true;
                execute = true;
            }
            else if (arg.equals("-compile")) {
                compile = true;
            }
            else if (arg.equals("-execute")) {
                execute = true;
            }
            else if (arg.equals("-expectFail")) {
                expectFail = true;
            }
            else
                return Status.failed("bad arg for script: `" + arg + "'");
        }

        if (compile == false && execute == false) {
            // not set in args, so set from mode
            compile = (mode == DEVELOPER || mode == PRECOMPILE);
            execute = (mode == DEVELOPER || mode == CERTIFY);
        }

        if (compile) {
            String srcsParameter = td.getParameter("sources");
            if (srcsParameter == null)
                // check "source" for backwards compatibility
                srcsParameter = td.getParameter("source");
            String[] srcs = StringArray.split(srcsParameter);
            File[] files = new File[srcs.length];
            File tdDir  = td.getDir();
            for (int i = 0; i < files.length; i++)
                files[i] = new File(tdDir, srcs[i].replace('/', File.separatorChar));

            Status compileStatus = compileTogether(files);

            // if we're not going to execute the test, this is the end of the task
            if (!execute)  {
                if (expectFail) {
                    // backwards compatibility for negative compiler tests,
                    // for which we expect the compilation to fail,
                    // so verify that it did, and return accordingly
                    if (compileStatus.getType() == Status.FAILED)
                        return pass_compFailExp.augment(compileStatus);
                    else
                        return fail_compSuccUnexp.augment(compileStatus);
                } else
                    // normal exit for compile-only tests
                    return compileStatus;
            }

            // if we want to execute the test, but the compilation failed, we can't go on
            if (compileStatus.isFailed())
                return fail_compFailUnexp.augment(compileStatus);
        }

        if (execute) {
            String executeClass = td.getParameter("executeClass");
            if (executeClass == null)
                return error_noExecuteClass;

            Status executeStatus = execute(executeClass, td.getParameter("executeArgs"));

            if (expectFail) {
                // backwards compatibility for negative execution tests,
                // for which we expect the execution to fail,
                // so verify that it did, and return accordingly
                if (executeStatus.getType() == Status.FAILED)
                    return pass_execFailExp.augment(executeStatus);
                else
                    return fail_execSuccUnexp.augment(executeStatus);
            } else
                // normal exit for (compile and) execute tests
                return executeStatus;
        }

        return error_noActionSpecified;
    }

    /**
     * Get the execution mode for this script. The default mode is CERTIFY.
     * @return an integer signifying the execution mode for this script
     * @see #setMode
     * @see #UNKNOWN
     * @see #CERTIFY
     * @see #PRECOMPILE
     * @see #DEVELOPER
     */
    public int getMode() {
        return mode;
    }

    /**
     * Set the execution mode for this script.
     * @param mode an integer signifying the execution mode for this script
     * @see #getMode
     * @see #UNKNOWN
     * @see #CERTIFY
     * @see #PRECOMPILE
     * @see #DEVELOPER
     */
    public void setMode(int mode) {
        switch (mode) {
        case CERTIFY:
        case PRECOMPILE:
        case DEVELOPER:
            this.mode = mode;
            break;

        default:
            throw new IllegalArgumentException();
        }
    }

    private void setMode(String mode) {
        setMode(parseMode(mode));
    }

    private static int parseMode(String m) {
        if (m == null || m.equals("certify"))
            return CERTIFY;
        else if (m.equals("precompile"))
            return PRECOMPILE;
        else if (m.equals("developer"))
            return DEVELOPER;
        else
            return UNKNOWN;
    }

    private static int getDefaultMode() {
        return parseMode(System.getProperty("javatest.stdTestScript.defaultMode"));
    }

    /**
     * An integer signifying that the execution mode is unknown.
     */
    public static final int UNKNOWN = 0;

    /**
     * An integer signifying that the execution mode is to perform
     * a certification run, executing precompiled classes.
     */
    public static final int CERTIFY = 1;

    /**
     * An integer signifying that the execution mode is to precompile
     * but not otherwise execute the tests.
     */
    public static final int PRECOMPILE = 2;

    /**
     * An integer signifying that the execution mode is to compile
     * and execute the tests.
     */
    public static final int DEVELOPER = 3;

    private int mode = getDefaultMode();
}
