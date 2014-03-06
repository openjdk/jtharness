/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.util;

import com.sun.javatest.JavaTestSecurityManager;

/**
 * ExitCount can be used by a family of cooperating objects (windows?) to cause
 * the process to exit when the last object is destroyed.
 */

public class ExitCount
{
    /**
     * Increment the count to prevent the process from exiting.
     */
    public static void inc() {
        count++;
    }

    /**
     * Decrement the count; if it reaches zero, the process will be forcibly
     * exited.
     */
    public static void dec() {
        dec(true, 0);
    }

    /**
     * Decrement the count; if it reaches zero, the process will
     * optionally be exited.
     * @param autoExitIfZero if true, and if the count gets decremented to zero,
     * then the process will be exited
     * @param exitCode if the process is to be exited, this will be used as the exit code
     * @return true if the process has not been exited, and if the count is now zero
     */
    public static boolean dec(boolean autoExitIfZero, int exitCode) {
        if (--count == 0) {
            SecurityManager sc = System.getSecurityManager();
            if (sc instanceof JavaTestSecurityManager)
                ((JavaTestSecurityManager) sc).setAllowExit(true);

            if (autoExitIfZero)
                System.exit(exitCode);
        }

        return (count == 0);
    }

    //-----member variables-------------------------------------------------------

    private static int count = 0;
}
