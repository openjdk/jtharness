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
package com.sun.jct.utils.i18ncheck.javatest.batch;

import java.io.PrintWriter;
import java.util.Properties;

import com.sun.javatest.tool.Main;
import com.sun.javatest.util.ExitCount;



public class I18NBatchTest
{
    public static void main(String[] args) {
        try {
            I18NBatchTest t = new I18NBatchTest();
            t.run();
            System.err.println("I18NBatchTest completed successfully");
            System.exit(0);
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public void run() {
        ExitCount.inc();

        sysProps.put("javatest.preferences.file", "NONE");
        sysProps.put("javatest.desktop.file", "NONE");


        try {
            PrintWriter out = new PrintWriter(System.out);
            Main m = new Main();
            m.run(new String[] {"-help"}, out);

            out.flush();
        }
        catch (Throwable t) {
            throw new Error("unexpected exception from JavaTest: " + t);
        }

        ExitCount.dec();
    }

    private Properties sysProps = System.getProperties();
}
