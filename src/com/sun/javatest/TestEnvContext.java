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
package com.sun.javatest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
//import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.Properties;
import com.sun.javatest.util.DynamicArray;
//import com.sun.interview.Interview;

/**
 * A table representing the collection of environments found in a set of environment files.
 */
public class TestEnvContext
{
    /**
     * This exception is to report problems using {@link TestEnvContext} objects.
     */
    public static class Fault extends Exception
    {
        Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }

        Fault(I18NResourceBundle i18n, String s, Object[] o) {
            super(i18n.getString(s, o));
        }
    }

    /**
     * Create a context from a set of environment (.jte) files.
     * @param files The environment files to read
     * @throws TestEnvContext.Fault if an error is found while reading the files
     */
    public TestEnvContext(File[] files) throws Fault {
        Vector n = new Vector();
        Vector p = new Vector();
        try {
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    add(p, n, load(f), f.getPath());
                }
            }
        }
        finally {
            propTables = new Map[p.size()];
            p.copyInto(propTables);
            propTableNames = new String[n.size()];
            n.copyInto(propTableNames);
            updateEnvTable();
        }
    }

    /**
     * Create a context from a specified set of named tables.
     * @param tables An array of tables giving the properties to be read
     * @param tableNames An array of names, one for each table in the tables array,
     *  that will be used to identify the source of the properties in any
     *  environments that are created
     */
    public TestEnvContext(Map[] tables, String[] tableNames) {
        Vector n = new Vector();
        Vector p = new Vector();
        for (int i = 0; i < tables.length; i++) {
            add(p, n, tables[i], tableNames[i]);
        }
        propTables = new Map[p.size()];
        p.copyInto(propTables);
        propTableNames = new String[n.size()];
        n.copyInto(propTableNames);
        updateEnvTable();
    }

    /**
     * Create a context from a named table.
     * @param table A table giving the properties to be read
     * @param tableName The name that will be used to identify the source
     *   of the properties in any environments that are created.
     */
    public TestEnvContext(Map table, String tableName) {
        Vector n = new Vector();
        Vector p = new Vector();
        add(p, n, table, tableName);
        propTables = new Map[p.size()];
        p.copyInto(propTables);
        propTableNames = new String[n.size()];
        n.copyInto(propTableNames);
        updateEnvTable();
    }

    /**
     * Get a environment from this set of environments.
     * @param name the name of the desired environment
     * @return the specified environment, or null if not found
     * @throws TestEnvironment.Fault if there is a problem creating
     *          the specified environment
     */
    public TestEnvironment getEnv(String name) throws TestEnvironment.Fault {
        if (isValidEnv(name))
            return new TestEnvironment(name, propTables, propTableNames);
        else
            return null;
    }

    /**
     * Check if a name matches the name of one of the environments in this
     * set of environments.
     * @param name the name to check
     * @return true if and only if the name matches the name of one of the
     * environments in trhis set of environments
     */
    public boolean isValidEnv(String name) {
        // empty name is always valid (null is not)
        if (name.length() == 0)
            return true;

        for (int i = 0; i < envNames.length; i++)
            if (envNames[i].equals(name))
                return true;

        return false;
    }

    /**
     * Get an array containing all the names of environments in this set of
     * environments.
     * @return an array containing the names of all the environments in this set
     */
    public String[] getEnvNames() {
        // does not include null string (for now)
        return envNames;
    }

    /**
     * Get an array containing all the names of environments that should
     * appear in a menu of valid environment names. This is all environment
     * names, excluding those environments that define an entry "menu=false".
     * @return an array containing the names of all the environments in this set
     * that should appear in a menu of valid environment names
     */
    public String[] getEnvMenuNames() {
        return envMenuNames;
    }

    private Map load(File f) throws Fault {
        if (f == null)
            return null;

        try {
            Properties p = new Properties();
            Reader in = new BufferedReader(new FileReader(f));
            p.load(in);
            in.close();

            /*
            if (f.getName().endsWith(".jti" )) {  //BETTER TO GET THIS FROM SOMEWHERE
                String interviewClassName = (String)p.get("INTERVIEW");
                if (interviewClassName == null)
                    throw new Fault(i18n, "tec.noInterview", f);
                Class c = Class.forName(interviewClassName);
                Interview i = (Interview)(c.newInstance());
                Map d = new Hashtable();
                i.export(d);
                return d;
            }
            else*/
                return p;
        }
        catch (FileNotFoundException e) {
            throw new Fault(i18n, "tec.cantFindFile", f);
        }
        catch (IOException e) {
            throw new Fault(i18n, "tec.ioError", new Object[] {f, e});
        }
        /*
        catch (ClassNotFoundException e) {
            throw new Fault(i18n, "tec.interviewClassNotFound",
                            new Object[] {f, e.getMessage()});
        }
        */
        /*
        catch (IllegalAccessException e) {
            throw new Fault(i18n, "tec.interviewClassAccess",
                            new Object[] {f, e.getMessage()});
        }
        */
        /*
        catch (InstantiationException e) {
            throw new Fault(i18n, "tec.interviewClassInstantiation",
                            new Object[] {f, e.getMessage()});
        }
        */
    }

    private void add(Vector pv, Vector nv, Map p, String n) {
        if (p != null) {
            pv.addElement(p);
            nv.addElement(n);
        }
    }

    private void updateEnvTable() {
        // the tables given to the constructor ...
        Map[] tables = propTables;
        String[] tableNames = propTableNames;

        // defaults given to TestEnvironment
        Map[] defaultTables = TestEnvironment.defaultPropTables;
        String[] defaultTableNames = TestEnvironment.defaultPropTableNames;

        // if there are defaults, merge them with the TestEnvContext tables
        // for the purposes of determining the EnvTable
        if (defaultTables != null && defaultTables.length > 0) {
            tables = (Map[]) (DynamicArray.join(defaultTables, tables));
            tableNames = (String[]) (DynamicArray.join(defaultTableNames, tableNames));
        }

        Vector allVec = new Vector();
        Vector menuExcludeVec = new Vector();

        // scan all the property tables, looking for entries
        final String ENV_DOT = "env.";
        final String DOT_DESCRIPTION = ".description";
        final String DOT_FINDER = ".finder";
        final String DOT_SCRIPT = ".script";
        final String DOT_SCRIPT_DOT = ".script.";
        final String DOT_INHERITS = ".inherits";
        final String DOT_MENU = ".menu";
        final String DOT_TESTSUITE = ".testsuite";

        if (debug)
            System.err.println(getClass().getName() + ": trace");

        for (int i = 0; i < tables.length; i++) {
            if (debug)
                System.err.println("Checking " + tableNames[i] + " for environments...");

            Map table = tables[i];
            for (Iterator ii = table.keySet().iterator(); ii.hasNext(); ) {
                String prop = (String) (ii.next());
                String name = null;

                if (debug)
                    System.err.println("Checking property " + prop);

                if (!prop.startsWith(ENV_DOT))
                    continue;

                if (prop.endsWith(DOT_INHERITS)) {
                    name = prop.substring(ENV_DOT.length(), prop.length() - DOT_INHERITS.length());
                }
                else if (prop.endsWith(DOT_MENU)) {
                    name = prop.substring(ENV_DOT.length(), prop.length() - DOT_MENU.length());
                    String value = (String) (table.get(prop));
                    if ("false".equals(value))
                        sortedInsert(menuExcludeVec, name);
                }
                else if (prop.endsWith(DOT_DESCRIPTION)) {
                    name = prop.substring(ENV_DOT.length(), prop.length() - DOT_DESCRIPTION.length());
                }
                else if (prop.endsWith(DOT_FINDER)) {
                    name = prop.substring(ENV_DOT.length(), prop.length() - DOT_FINDER.length());
                }
                else if (prop.endsWith(DOT_SCRIPT)) {
                    name = prop.substring(ENV_DOT.length(), prop.length() - DOT_SCRIPT.length());
                }
                else if (prop.endsWith(DOT_TESTSUITE)) {
                    name = prop.substring(ENV_DOT.length(), prop.length() - DOT_TESTSUITE.length());
                }
                else {
                    int lastDot = prop.lastIndexOf('.');
                    int scriptStartIndex = lastDot - DOT_SCRIPT_DOT.length() + 1;
                    if (scriptStartIndex > 0 &&
                        prop.regionMatches(scriptStartIndex, DOT_SCRIPT_DOT, 0, DOT_SCRIPT_DOT.length())) {
                        name = prop.substring(ENV_DOT.length(), scriptStartIndex);
                    }
                    else
                        continue;
                }

                if (debug)
                    System.err.println("found environment name: " + name);

                sortedInsert(allVec, name);
            }
        }

        envNames = new String[allVec.size()];
        allVec.copyInto(envNames);
        Vector menuVec = (Vector)allVec.clone();
        for (int i = 0; i < menuExcludeVec.size(); i++)
            menuVec.removeElement(menuExcludeVec.elementAt(i));
        envMenuNames = new String[menuVec.size()];
        menuVec.copyInto(envMenuNames);
    }

    private void sortedInsert(Vector v, String s) {
        for (int i = 0; i < v.size(); i++) {
            int c = s.compareTo((String)(v.elementAt(i)));
            if (c > 0) {
                v.insertElementAt(s, i);
                return;
            }
            else if (c == 0)
                return;
        }
        v.addElement(s);
    }

    private Map[] propTables;
    private String[] propTableNames;
    private String[] envNames;
    private String[] envMenuNames;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TestEnvContext.class);
    private static boolean debug = Boolean.getBoolean("debug." + TestEnvContext.class.getName());
}
