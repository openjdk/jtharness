/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.agent;

import java.io.IOException;
import java.io.PrintWriter;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;

import com.sun.javatest.Command;
import com.sun.javatest.ProductInfo;
import com.sun.javatest.Status;

/**
 * A command that delegates a subcommand to a JT Harness agent that is accessible
 * via a serial port.
 */
public class SerialPortAgentCommand extends Command
{
    /**
     * Delegate a subcommand to an agent that is accessible via a serial port.
     * @param args An array of strings, identifying the subcommand and where
     *          to run it. The array should be of the form:<br>
     *          <em>options</em>... <em>port</em> <em>subcommand-class</em> <em>subcommand-args</em>...
     * <table><tr><th colspan=2>Options</th></tr>
     * <tr><td>-cp <em>path</em><br>-classpath <em>path</em>
     *          <td>Specify a path from which the subcommand should be loaded,
     *          via the connection to the JT Harness harness.
     *          If not specified, any necessary classes will be loaded
     *          from the agent's classpath.
     * <tr><td>-m<br>-mapArgs
     *          <td>Use the map facility on the JT Harness Agent to localize
     *          any configuration values.
     * <tr><td>-t <em>tag</em><br>-tag <em>tag</em>
     *          <td>Specify a tag with with to identify this command in
     *          any tracing output or GUI display.
     * </table>
     * @param err A stream to which to write any diagnostic error messages.
     * @param out An additional stream to which to write any additional output.
     * @return a Status object indicating the outcome of the command that was executed
     */
    public Status run(String[] args, PrintWriter err, PrintWriter out) {
        String classPath = null;
        String tag = null;
        boolean localizeArgs = false;

        // analyze options
        int i = 0;
        for (; i < args.length && args[i].startsWith("-"); i++) {
            if ((args[i].equals("-cp") || args[i].equals("-classpath")) && i+1 < args.length) {
                classPath = args[++i];
            }
            else if (args[i].equals("-m") || args[i].equals("-mapArgs")) {
                localizeArgs = true;
            }
            else if ((args[i].equals("-t") || args[i].equals("-tag")) && i+1 < args.length) {
                tag = args[++i];
            }
            else
                return Status.error("Unrecognized option: " + args[i]);
        }

        if (i == args.length)
            return Status.error("No serial port specified");

        String serialPortName = args[i++];

        if (i == args.length)
            return Status.error("No command specified");

        String cmdClass = args[i++];

        String[] cmdArgs = new String[args.length - i];
        System.arraycopy(args, i, cmdArgs, 0, cmdArgs.length);

        if (tag == null)
            tag = cmdClass;

        try {
            AgentManager mgr = AgentManager.access();
            AgentManager.Task t = mgr.connect(new SerialPortConnection(serialPortName, ProductInfo.getName(), 1000));

            try {
                if (classPath != null)
                    t.setClassPath(classPath);

                out.println("Executing command via " + t.getConnection().getName());

                return t.executeCommand(tag, cmdClass, cmdArgs, localizeArgs, err, out);
            }
            finally {
                // ensure the port is released whatever the outcome
                t.getConnection().close();
            }
        }
        catch (InterruptedException e) {
            return Status.error("Interrupted while waiting for port: " + serialPortName);
        }
        catch (NoSuchPortException e) {
            return Status.error("No such port: " + serialPortName);
        }
        catch (PortInUseException e) {
            return Status.error("port in use: " + serialPortName);
        }
        catch (IOException e) {
            return Status.error("Error accessing agent: " + e);
        }
    }
}

