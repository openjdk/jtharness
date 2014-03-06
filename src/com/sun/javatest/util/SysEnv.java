/*
 * $Id$
 *
 * Copyright (c) 2004, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to provide access to OS environment variables, by means of
 * an external command which is executed.
 *
 * The command will be executed when the first access is made to the
 * environment variables. The command name is determined as follows:
 * <ul>
 * <li> The value passed to setCommand, if that has been set
 * <li> The value of the system property "javatest.sysEnv.command",
 *      if that has been set
 * <li> An OS-specific default, if one is known. The current set of
 *      defaults is as follows:
 *      <table>
 *      <tr><th>OS              <th>Default
 *      <tr><td>Mac OS X        <td>/usr/bin/env
 *      <tr><td>Solaris         <td>/usr/bin/env
 *      <tr><td>Windows XP      <td>cmd /c set
 *      </table>
 * <li> Finally, a simple default of "env" is used. While this may not
 *      always work by default, a user could provide a suitable script
 *      or batch file on the current execution path that will yield the
 *      required results.
 * </ul>
 *
 * <p> Note that the specified command will be invoked by Runtime.exec and
 * must be capable or working as such. This may preclude direct use of
 * builtin commands on some systems. For example, you cannot directly
 * exec the Windows "set" command built in to the standard Windows
 * command shell.
 *
 * <p> The command must print out a series of lines of the form <i>name</i>=<i>value</i>,
 * one for each environment variable.
 */
public class SysEnv
{
    /**
     * Set the command to be executed to access the OS environment variables.
     * To be effective, this method must be set before any of the get methods,
     * @param cmd the command to be executed
     */
    public static void setCommand(String cmd) {
        command = cmd;
    }

    /**
     * Get the value of a specified environment value.
     * @param name the name of the environment variable
     * @return the value of the environment variable if set, or null if not
     */
    public static String get(String name) {
        if (values == null)
            initValues();

        return (String) (values.get(name));
    }

    /**
     * Get a map containing all of the environment variables in the current
     * execution context.
     * @return a map containing all the known environment variables.
     */
    public static Map getAll() {
        return getAll(new HashMap());
    }

    /**
     * Get a map containing all of the environment variables in the current
     * execution context.
     * @param m the map in which to put the names and values of all the
     * environment variables in the current execution context.
     * @return the argument map.
     */
    public static Map getAll(Map m) {
        if (values == null)
            initValues();

        m.putAll(values);
        return m;
    }

    private static void initValues() {
        values = new HashMap();

        if (command == null)
            command = getDefaultCommand();

        if (command == null)
            return;

        try {
            Process p = Runtime.getRuntime().exec(command);

            InputStream pin = p.getInputStream(); // sysout from the process
            DataInputStream in = new DataInputStream(new BufferedInputStream(pin));

            String line;
            while ((line = in.readLine()) != null) {
                int eq = line.indexOf('=');
                if (eq == -1)
                    continue;
                String name = line.substring(0, eq);
                String value = line.substring(eq + 1);
                values.put(name, value);
            }

            in.close();

            p.getErrorStream().close();
            p.getOutputStream().close();
        }
        catch (IOException e) {
            System.err.println(i18n.getString("sysEnv.err", e));
        }
    }

    private static String getDefaultCommand() {
        String prop = System.getProperty("javatest.sysEnv.command");
        if (prop != null)
            return prop;

        String osName = System.getProperty("os.name");

        if (osName.equalsIgnoreCase("SunOS")
            || osName.equalsIgnoreCase("Linux")
            || osName.equalsIgnoreCase("Mac OS X"))
            return "/usr/bin/env";

        if (osName.equalsIgnoreCase("Windows XP")
            || osName.equalsIgnoreCase("Windows 2000"))  // tested
            return "cmd /c set";

        if (osName.toLowerCase().startsWith("windows")) // not yet tested
            return "cmd /c set";

        return "env"; // best guess
    }

    private static String command;
    private static Map values;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(SysEnv.class);
}
