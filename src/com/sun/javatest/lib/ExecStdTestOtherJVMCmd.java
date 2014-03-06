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

import com.sun.javatest.Status;

/**
 * This is a modification of <code>ProcessCommand</code> suitable
 * for executing standard tests in a separate JVM.  When run in a
 * separate process, these tests report their exit status by calling
 * <code>Status.exit()</code>.
 **/
public class ExecStdTestOtherJVMCmd extends ProcessCommand
{

    /**
     *  Generate a status for the command, based upon the command's exit code
     *  and a status that may have been passed from the command by using
     *  <code>status.exit()</code>.
     *
     *  @param exitCode         The exit code from the command that was executed.
     *  @param logStatus        If the command that was executed was a test program
     *                          and exited by calling <code>status.exit()</code>,
     *                          then logStatus will be set to `status'.  Otherwise,
     *                          it will be null.  The value of the status is passed
     *                          from the command by writing it as the last line to
     *                          stdout before exiting the process.   If it is not
     *                          received as the last line, the value will be lost.
     *  @return                 If <code>logStatus</code> is not null, it will
     *                          be used as the result; this will normally correspond
     *                          to the status for which the test called
     *                          <code>status.exit();</code>.
     *                          <p> If <code>logStatus</code> is null, that means
     *                          that for some reason the test did not successfully
     *                          call <code>status.exit()</code> and the test is
     *                          deemed to have failed. If the exit code is zero,
     *                          a likely possibility is that the test raised an
     *                          exception which caused the JVM to dump the stack
     *                          and exit. In this case, the result is
     *                          <code>Status.failed("exit without status, exception assumed")</code>.
     *                          In other cases, the result is simply
     *                          <code>Status.failed("exit code" + exitCode)</code>.
     *
     **/
    protected Status getStatus(int exitCode, Status logStatus) {
        if (logStatus != null)
            return logStatus;
        else if (exitCode == 0)
            return Status.failed("exit without status, exception assumed");
        else
            return Status.failed("exit code " + exitCode);
    }
}
