/*
 * $Id$
 *
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import com.sun.javatest.ResourceLoader;
import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;

class ManagerLoader
{
    ManagerLoader(Class managerClass, PrintStream log) {
        setManagerClass(managerClass);
        setLog(log);
    }

    void setManagerClass(Class managerClass) {
        this.managerClass = managerClass;
    }

    void setManagerConstructorArgs(Class[] argTypes, Object[] args) {
        constrArgTypes = argTypes;
        constrArgs = args;
    }

    void setLog(PrintStream log) {
        this.log = log;
    }

    Set loadManagers(String resourceName)
        throws IOException
    {

        Enumeration e = ResourceLoader.getResources(resourceName, getClass());
        Set mgrs = new HashSet();
        URLClassLoader altLoader = null;

        while (e.hasMoreElements()) {
            URL entry = (URL)(e.nextElement());
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(entry.openStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0) {
                        continue;
                    } else if (line.startsWith("#")) {
                        line = line.substring(1).trim();
                        if (line.startsWith("classpath=")) {
                            line = line.substring(10).trim();
                            altLoader = getSecondaryLoader(line);
                            // note that only the last classpath line would be used
                        }

                        continue;
                    }   // elseif

                    try {
                        Object mgr = newInstance(this.getClass().getClassLoader(),
                                line, entry);
                        if (mgr == null && altLoader != null) {
                            // attempt to load from the alternate class loader
                            mgr = newInstance(altLoader, line, entry);
                        }

                        if (mgr != null) {
                            mgrs.add(mgr);
                        } else {
                            if (log != null) {
                                writeI18N("ml.cantFindClass", new Object[]{line, entry});
                            }
                        }
                    }
                    catch (IllegalAccessException ex) {
                        if (log != null) {
                            writeI18N("ml.cantAccessClass", new Object[] { line, entry } );
                        }
                    }
                    catch (InstantiationException ex) {
                        if (log != null) {
                            writeI18N("ml.cantCreateClass", new Object[] { line, entry } );
                        }
                    }
                    catch (NoSuchMethodException ex) {
                        if (log != null) {
                            writeI18N("ml.cantFindConstr", new Object[] { line, entry } );
                        }
                    }
                }   // while
                in.close();
            }
            catch (IOException ex) {
                if (log != null)
                    writeI18N("ml.cantRead", new Object[] { entry, ex });
            }
        }
        return mgrs;
    }

    /**
     * Throws the declared exceptions because it seems that the class was found
     * but couldn't be used.  Not being able to find the class is not a problem,
     * this is handled in the code in other ways.
     * @param cl Class loader to use.
     * @param className Class to load.
     * @param specfile File which specified the classname to be loaded - used
     *     for error message purposes.
     * @return An instance of the requested class, null if not found.  Or if the
     *     object created is not a manager subclass.
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     */
    private Object newInstance(ClassLoader cl, String className, URL specfile)
        throws InstantiationException, NoSuchMethodException, IllegalAccessException {
        if (cl == null) {
            return null;
        }

        try {
            Class c = Class.forName(className, true, cl);
            Object mgr = newInstance(c);
            if (managerClass.isInstance(mgr)) {
                return mgr;
            } else {
                if (log != null) {
                    writeI18N("ml.notSubtype", new Object[]{className,
                            managerClass.getName(), specfile});
                }
            }
        } catch (ClassNotFoundException ex) {
        }

        return null;
    }

    private Object newInstance(Class c)
        throws IllegalAccessException, InstantiationException, NoSuchMethodException
    {
        if (constrArgTypes == null || constrArgTypes.length == 0) {
            return c.newInstance();
        }

        try {
            Constructor constr = c.getConstructor(constrArgTypes);
            return constr.newInstance(constrArgs);
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof RuntimeException) {
                throw ((RuntimeException) t);
            }
            else if (t instanceof Error) {
                throw ((Error) t);
            }
            else {
                throw new Error(e);
            }
        }
    }

    private URLClassLoader getSecondaryLoader(String paths) {
        String[] cps = StringArray.splitList(paths, ":");

        if (cps == null || cps.length == 0) {
            return null;
        }

        URL[] urls = new URL[0];
        boolean someCPok = false;
        for (String s : cps) {
            URL u = ResourceLoader.getExtUrl(new File(s));

            if (u != null) {
                urls = (URL[]) (DynamicArray.append(urls, u));
                someCPok = true;
            }
        }   // for

        // none of the input classpath can be used
        if (!someCPok || urls == null || urls.length == 0) {
            if (log != null) {
                log.println("Unable to create valid paths from any of the custom tool classpath items.");
            }
            return null;
        }

        // create CL
        try {
            URLClassLoader cl = new URLClassLoader(urls);
            return cl;
        } catch (SecurityException e) {
            if (log != null) {
                log.println("Unable to create custom class loader to load tool managers:");
                e.printStackTrace(log);
            }
            return null;
        }   // catch
    }

    private void writeI18N(String key, Object[] args) {
        log.println(i18n.getString(key, args));
    }

    private Class managerClass;
    private Constructor constr;
    private Class[] constrArgTypes;
    private Object[] constrArgs;
    private PrintStream log;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ManagerLoader.class);
}
