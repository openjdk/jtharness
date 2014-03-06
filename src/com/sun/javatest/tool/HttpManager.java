/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool;

import java.text.DateFormat;
import java.util.Date;

import java.util.ListIterator;

import com.sun.javatest.ProductInfo;
import com.sun.javatest.httpd.HttpdServer;
import com.sun.javatest.httpd.PageGenerator;
import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * A tool manager to handle the command line options for starting
 * an HTTP server to monitor JT Harness's progress while executing tests.
 * The supported options are:
 * <ul>
 * <li><code>-startHttp</code>: start an HTTP server
 * </ul>
 */
public class HttpManager extends CommandManager
{
    public HelpTree.Node getHelp() {
        String[] cmds = {
            HttpCommand.getName()
        };
        return new HelpTree.Node(i18n, "http", cmds);
    }

    //----------------------------------------------------------------------------

    public boolean parseCommand(String cmd, ListIterator argIter, CommandContext ctx)
        throws Command.Fault
    {
        if (isMatch(cmd, HttpCommand.getName())) {
            if (!httpFlag) {
                ctx.addCommand(new HttpCommand());
                httpFlag = true;
            }
            return true;
        }
        return false;
    }

    private boolean httpFlag;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ConfigManager.class);

    private static class HttpCommand extends Command {
        static String getName() {
            return "startHttp";
        }

        HttpCommand() {
            super(getName());
        }

        public void run(CommandContext ctx) {
            HttpdServer server = new HttpdServer();
            Thread thr = new Thread(server);

            Date date = ProductInfo.getBuildDate();
            DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

            PageGenerator.setSWName(ProductInfo.getName());
            PageGenerator.setSWBuildDate(df.format(date));
            PageGenerator.setSWVersion(ProductInfo.getVersion());

            thr.start();
        }
    }

}
