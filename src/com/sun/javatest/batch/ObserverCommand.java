/*
 * $Id$
 *
 * Copyright (c) 2003, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.batch;

import java.io.File;
import java.lang.NoSuchMethodException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ListIterator;
import java.util.Vector;

import com.sun.javatest.Harness;
import com.sun.javatest.tool.Command;
import com.sun.javatest.tool.CommandContext;
import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.I18NResourceBundle;

class ObserverCommand extends Command
{
    static String getName() {
        return "observer";
    }

    static HelpTree.Node getHelp() {
        String[] opts = { "cp" };
        return new HelpTree.Node(i18n, "cmgr.help.observer", opts);
    }

    ObserverCommand(ListIterator argIter) throws Fault {
        super(getName());

        Vector v = null;

        while (argIter.hasNext()) {
            String arg = nextArg(argIter);

            if (arg.equals("-cp") && argIter.hasNext())
                setClassPath(nextArg(argIter));
            else if (arg.startsWith("-"))
                throw new Fault(i18n, "observer.badArg", arg);
            else {
                className = arg;
                while (argIter.hasNext()) {
                    arg = nextArg(argIter);
                    if (arg.equals("-end"))
                        break;
                    else {
                        if (v == null)
                            v = new Vector();
                        v.add(arg);
                    }
                }
                break;
            }
        }

        if (className == null)
            throw new Fault(i18n, "observer.noClassName");

        if (v != null) {
            classArgs = new String[v.size()];
            v.copyInto(classArgs);
        }
    }

    public void run(CommandContext ctx) throws Fault {
        try {
            Class oc = loadClass(className);

            Harness.Observer o = null;
            if (classArgs == null || classArgs.length == 0) {
                o = tryConstructor(oc,
                                   new Class[] { },
                                   new Object[] { });
            }
            else if (classArgs.length == 1) {
                o = tryConstructor(oc,
                                   new Class[] { String.class },
                                   new Object[] { classArgs[0] });
            }

            if (o == null)
                o = tryConstructor(oc,
                                   new Class[] { String[].class },
                                   new Object[] { classArgs });

            if (o == null)
                throw new Fault(i18n, "observer.cantFindConstructor", className);

            ctx.addHarnessObserver(o);
        }
        catch (ClassNotFoundException e) {
            throw new Fault(i18n, "observer.cantFindClass", className);
        }
        catch (IllegalAccessException e) {
            throw new Fault(i18n, "observer.cantAccessClass", className);
        }
        catch (InstantiationException e) {
            throw new Fault(i18n, "observer.cantCreateClass", className);
        }
        catch (InvocationTargetException e) {
            throw new Fault(i18n, "observer.cantCreateClass", className);
        }
    }

    private Harness.Observer tryConstructor(Class obsClass, Class[] argTypes, Object[] args)
        throws IllegalAccessException, InstantiationException, InvocationTargetException
    {
        try {
            Constructor c = obsClass.getConstructor(argTypes);
            return (Harness.Observer) (c.newInstance(args));
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

    private void setClassPath(String s) throws Fault {
        char pathCh = File.pathSeparatorChar;
        Vector v = new Vector();
        int start = 0;
        for (int i = s.indexOf(pathCh); i != -1; i = s.indexOf(pathCh, start)) {
            addClassPathEntry(s.substring(start, i), v);
            start = i + 1;
        }
        addClassPathEntry(s.substring(start), v);
        URL[] path = new URL[v.size()];
        v.copyInto(path);
        setClassPath(path);
    }

    private void setClassPath(URL[] path) {
        classLoader = new URLClassLoader(path);
    }

    private Class loadClass(String name) throws ClassNotFoundException {
        return (classLoader == null ? Class.forName(name) : classLoader.loadClass(name));
    }

    private void addClassPathEntry(String s, Vector v) throws Fault {
        try {
            if (s.length() > 0)
                v.add(new File(s).toURL());
        }
        catch (MalformedURLException e) {
            throw new Fault(i18n, "observer.badClassPath", new Object[] { s, e });
        }
    }

    private ClassLoader classLoader;
    private String className;
    private String[] classArgs;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(BatchManager.class);
}
