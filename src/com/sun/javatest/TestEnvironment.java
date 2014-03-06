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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;

/**
 * This class provides "environments", as embodied by groups of related properties.
 * Environments have a name, and consist of those properties provided whose names
 * do not begin with "<code>env.</code>", and in addition, those
 * properties provided whose names begin "<code>env.</code><i>env-name</i><code>.</code>".
 * In addition, an environment may inherit the properties of another environment
 * by defining a property <code>env.</code><i>env-name</i><code>inherits=</code><i>inherited-env-name</i>
 * The values of the environment's properties are split into words and various
 * substitutions are performed.
 *
 * <p>The preferred way to make an environment is via a configuration interview,
 * avoiding the use of the <code>env.</code><i>env-name</i> prefix, which is
 * retained for backwards compatibility with older test suites that read environments
 * from environment (.jte) files.
 */
public class TestEnvironment
{
    /**
     * This exception is used to report resolving values in an environment.
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
     * Add a default set of properties to be included when environments are
     * created.
     * @param name a name for this collection or properties, so that the
     * source of the properties can be identified when browing an environment
     * @param propTable a table of properties to be included when environments
     * are created
     * @see #clearDefaultPropTables
     * @throws NullPointerException if either name or propTable is null.
     */
    public static synchronized void addDefaultPropTable(String name, Map propTable) {
        if (name == null || propTable == null)
            throw new NullPointerException();

        //System.err.println("TEC: add default propTable " + name);
        defaultPropTableNames = (String[]) (DynamicArray.append(defaultPropTableNames, name));
        defaultPropTables = (Map[]) (DynamicArray.append(defaultPropTables, propTable));
    }

    /**
     * Remove all previously registered default property tables.
     * @see #addDefaultPropTable
     */
    public static synchronized void clearDefaultPropTables() {
        defaultPropTableNames = new String[0];
        defaultPropTables = new Map[0];
    }

    static String[] defaultPropTableNames = { };
    static Map[] defaultPropTables = { };

    /**
     * Construct an environment for a named group of properties.
     * @param name      The name by which to identify the group of properties
     *                  for this environment
     * @param propTable Dictionaries containing (but not limited to) the
     *                  properties for this environment.
     * @param propTableName
     *                  The name of the property table, for use in diagnostics etc
     * @throws TestEnvironment.Fault if there is an error in the table
     *
     */
    public TestEnvironment(String name, Map propTable, String propTableName)
                throws Fault {
        this(name, (new Map[] {propTable}), (new String[] {propTableName}));
    }

    /**
     * Construct an environment for a named group of properties.
     * @param name      The name by which to identify the group of properties
     *                  for this environment
     * @param propTables        Dictionaries containing (but not limited to) the
     *                  properties for this environment. They should be ordered
     *                  so that values specified in later tables override those
     *                  specified in subsequent tables.
     * @param propTableNames
     *                  The names of the property tables, for use in diagnostics etc
     * @throws TestEnvironment.Fault if there is an error in the given tables
     *
     */
    public TestEnvironment(String name, Map[] propTables, String[] propTableNames)
        throws Fault
    {
        this.name = name;
        if (defaultPropTables != null && defaultPropTables.length > 0) {
            propTables = (Map[]) (DynamicArray.join(defaultPropTables, propTables));
            propTableNames = (String[]) (DynamicArray.join(defaultPropTableNames, propTableNames));
        }

        // First, figure out the inheritance chain
        Vector v = new Vector();
        for (String n = name, inherit = null; n != null && n.length() > 0; n = inherit, inherit = null) {
            if (v.contains(n))
                throw new Fault(i18n, "env.loop", name);

            v.addElement(n);
            String prefix = "env." + n + ".";
            for (int i = propTables.length - 1; i >= 0 && inherit == null; i--) {
                inherit = (String)(propTables[i].get("env." + n + ".inherits"));
            }
        }
        inherits = new String[v.size()];
        v.copyInto(inherits);

        // for this environment, and its inherited environments, scan for
        // properties of the form env.NAME.KEY=value and add KEY=value into the
        // environment's table
        for (int inheritIndex = 0; inheritIndex < inherits.length; inheritIndex++) {
            String prefix = "env." + inherits[inheritIndex] + ".";
            for (int propIndex = propTables.length - 1; propIndex >= 0; propIndex--) {
                Map propTable = propTables[propIndex];
                for (Iterator i = propTable.keySet().iterator(); i.hasNext(); ) {
                    String prop = (String) (i.next());
                    if (prop.startsWith(prefix)) {
                        String key = prop.substring(prefix.length());
                        if (!table.containsKey(key)) {
                            Element elem = new Element(key,
                                                       (String)(propTable.get(prop)),
                                                       inherits[inheritIndex],
                                                       propTableNames[propIndex]);
                            table.put(key, elem);
                        }
                    }
                }
            }
        }

        // finally, add in any top-level names (not beginning with env.)
        for (int propIndex = propTables.length - 1; propIndex >= 0; propIndex--) {
            Map propTable = propTables[propIndex];
            for (Iterator i = propTable.keySet().iterator(); i.hasNext(); ) {
                String key = (String) (i.next());
                if (!key.startsWith("env.")) {
                    if (!table.containsKey(key)) {
                        Element elem = new Element(key,
                                                   (String)(propTable.get(key)),
                                                   null,
                                                   propTableNames[propIndex]);
                        table.put(key, elem);
                    }
                }
            }
        }
    }


    /**
     * Create a copy of the current environment.
     * @return a copy of the current environment
     */
    public TestEnvironment copy() {
        return new TestEnvironment(this);
    }

    /**
     * Get the distinguishing name for the properties of this environment.
     * @return  The name used to distinguish the properties of this environment
     */
    public String getName() {
        return name;
    }

    /**
     * Get the description of this environment, as given by the "description" entry.
     * @return the description of this environment, or null if not given
     */
    public String getDescription() {
            if (table == null || ! table.containsKey("description")) {
            return null;
        }

        return ((Element)table.get("description")).getValue();
    }

    /**
     * Get the list of names of inherited environments, including this environment,
     * in reverse  order or inheritance (ie this one, parent, grandparent etc).
     * @return an array containing the names of inherited environments
     */
    public String[] getInherits() {
        return inherits;
    }

    /**
     * A backdoor method to add global properties to the environment. The value is
     * not subject to any substitutions.
     * @param name      The name of the property to be written
     * @param value     The value of the property to be written
     */
    public void put(String name, String value) {
        // used to save values without subjecting them to any $ or # processing
        // Note further that the main props table is considered IMMUTABLE,
        // because it is shared amongst the clones.
        String[] v = {value};
        extras.put(name, v);
    }

    /**
     * A backdoor method to add global properties to the environment. The value is
     * not subject to any substitutions.
     * @param name      The name of the property to be written
     * @param value     The value of the property to be written
     */
    public void put(String name, String[] value) {
        // used to save values without subjecting them to any $ or # processing
        // Note further that the main props table is considered IMMUTABLE,
        // because it is shared amongst the clones.
        extras.put(name, value);
    }

    /**
     * A backdoor method to add global properties to the environment that have a
     * value that might be desired as both a file and a URL.  The URL form is
     * installed as a property with "URL" appended to the given property name.
     * The values are not subject to any substitutions.
     * URL result constructed using the following expression -
     * f.toURI().toASCIIString();
     * @param name      The name of the property to be written
     * @param f         The file indicating the value to be stored.
     */
    public void putUrlAndFile(String name, File f) {
        String filePath = f.getPath();

        if (filePath.endsWith(File.separator))
            filePath = filePath.substring(0, filePath.length() - File.separator.length());

        String url = f.toURI().toASCIIString();

        put(name, filePath);

        // should upgrade to re-encode using UTF-8 perhaps
        put(name + "URL", url);
    }

    /**
     *
     * @return all external global properties.
     */
    public HashMap getExtraValues() {
        return extras;
    }

    /**
     * Lookup a named property in the environment.
     * @param key       The name of the property to look up
     * @return          The resolved value of the property
     * @throws  TestEnvironment.Fault is thrown if there is a problem resolving the value
     *                  of the property
     * @see #resolve
     */
    public String[] lookup(String key) throws Fault {
        return lookup(key, null);
    }

    private String[] lookup(String key, Vector activeKeys) throws Fault {
        String[] v = (String[])extras.get(key);
        if (v != null)
            return v;


        Element elem = (Element)table.get(key);
        if (elem != null) {
            cache.put(key, elem);
            if (activeKeys == null)
                activeKeys = new Vector();
            else if (activeKeys.contains(key))
                throw new Fault(i18n, "env.recursive",
                                new Object[] {key, elem.getDefinedInFile()});

            activeKeys.addElement(key);
            try {
                return resolve(elem.getValue(), activeKeys);
            }
            catch (Fault e) {
                throw new Fault(i18n, "env.badName",
                                new Object[] {key, elem.getDefinedInFile(), e.getMessage()});
            }
            finally {
                activeKeys.removeElement(key);
            }
        }

        return EMPTY_STRING_ARRAY;
    }

    /**
     * Resolve a value in the environment by splitting it into words and performing
     * various substitutions on it. White-space separates words except inside
     * quoted strings.
     * `<code>$<em>name</em></code>' and `<code>${<em>name</em>}</code>' are
     * replaced by the result of calling `lookup(<em>name</em>)'.
     * `<code>$/</code>' is replaced by the platform-specific file separator;
     * `<code>$:</code>' is replaced by the platform-specific path separator; and
     * `<code>$$</code>' is replaced by a single `$'.
     * No substitutions are performed inside single-quoted strings; $ substitutions
     * are performed in double-quoted strings.
     *
     * @param s The string to be resolved
     * @return  An array of strings containing the words of the argument, after
     *          substitutions have been performed.
     * @throws TestEnvironment.Fault
     *          This is thrown if there is a problem resolving the value
     *          of the argument.
     */
    public String[] resolve(String s) throws Fault {
        return resolve(s, null);
    }

    private String[] resolve(String s, Vector activeKeys) throws Fault {
        Vector v = new Vector();
        StringBuffer current = new StringBuffer(64);
        char term = 0;

  loop:
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                        case '#':
                // # at top level introduces comment to end of line and terminates
                //command (if found); otherwise, it goes into the current word
                        if ((!isInlineCommentsDisabled() || (i == 0 || s.charAt(i - 1) == ' ' || s.charAt(i - 1) == '\t')) && (term == 0 || term == ' '))
                                        break loop;
                                else
                                        current.append(c);
                                break;

              case '\'':
              case '\"':
                // string quotes at top level begin/end a matched pair; otherwise they
                // are part of it
                if (term == 0 || term == ' ') {
                    term = c;           // start matched pair
                } else if (term == c)
                    term = ' ';         // end matched pair
                else
                    current.append(c);  // put character in string
                break;

              case '$':
                // dollar introduces a name to be substituted, provided it does not
                // appear in single quotes. Special values: $/ is File.separatorChar,
                // $: is File.pathSeparatorChar, and $$ is $
                if (term != '\'') {
                    StringBuffer buf = new StringBuffer();
                    String name = null;
                    String[] nameArgs = null;
                    try {
                        c = s.charAt(++i);
                        switch (c) {
                        case '/':
                            current.append(File.separatorChar);
                            continue loop;

                        case ':':
                            current.append(File.pathSeparatorChar);
                            continue loop;

                        case '$':
                            current.append('$');
                            continue loop;

                        case '{':
                            c = s.charAt(++i);
                            while (c != ':' && c != '}') {
                                buf.append(c);
                                c = s.charAt(++i);
                            }
                            name=convertToName(resolve(buf.toString()));

                            // pick up optional nameArgs after embedded ':'
                            if (c == ':') {
                                buf = new StringBuffer();
                                c = s.charAt(++i);
                                while (c != '}') {
                                    buf.append(c);
                                    c = s.charAt(++i);
                                }
                                nameArgs = StringArray.split(buf.toString());
                            }

                            break;

                        default:
                            if (isNameChar(c)) {
                                while (i < s.length() && isNameChar(s.charAt(i))) {
                                    buf.append(s.charAt(i++));
                                }
                                i--;
                            } else
                                throw new Fault(i18n, "env.badExprChar", new Character(c));
                            name = buf.toString();
                        }

                        String[] val = lookup(name, activeKeys);

                        // apply nameArgs, if any
                        if (nameArgs != null) {
                            for (int argi = 0; argi < nameArgs.length; argi++) {
                                String arg = nameArgs[argi];
                                if (arg.startsWith("FS=") && arg.length() == 4)
                                    substituteChar(val, File.separatorChar, arg.charAt(3));
                                else if (arg.startsWith("PS=") && arg.length() == 4)
                                    substituteChar(val, File.pathSeparatorChar, arg.charAt(3));
                                else if (arg.startsWith("MAP="))
                                    substituteMap(val, lookup("map."+arg.substring(4), activeKeys));
                                else if (arg.equals("MAP"))
                                    substituteMap(val, lookup("map", activeKeys));
                                else
                                    throw new Fault(i18n, "env.badOption", arg);
                            }
                        }

                        if (val != null && val.length > 0) {
                            // only start a new word if there is something to substitute
                            if (term == 0)
                                term = ' ';
                            for (int vi = 0; vi < val.length; vi++) {
                                if (vi == 0)
                                    current.append(val[vi]);
                                else if (term == '"') {
                                    current.append(' ');
                                    current.append(val[vi]);
                                }
                                else {
                                    v.addElement(current.toString());
                                    current.setLength(0);
                                    current.append(val[vi]);
                                }
                            }
                        }
                    }
                    catch (IndexOutOfBoundsException e) {
                        throw new Fault(i18n, "env.badExpr");
                    }
                } else
                    current.append(c);
                break;

              case ' ':
              case '\t':
                // space or tab are skipped if not in a word; if in a word and
                // term is space, they terminate it; otherwise they go into the
                // current word
                if (term != 0) {
                    if (term == ' ') {
                        v.addElement(current.toString());
                        current.setLength(0);
                        term = 0;
                    } else
                        current.append(c);
                }
                break;


              default:
                // other characters start a word if needed, then go into the word
                if (term == 0)
                    term = ' ';
                current.append(c);
                break;
            }
        }

        // we've reached the end; if a word has been started, finish it
        if (term != 0)
           v.addElement(current.toString());

        String[] result = new String[v.size()];
        v.copyInto(result);
        return result;
    }

    /**
     * This is the name of system property to turn off the bugfix for inline
     * comments. You should specify "true" value for this property to enable
     * the bugfix, disabling the inline comments.
     */
    static String DISABLE_INLINE_COMMENTS_PROPERTY = "com.sun.javatest.InlineEnvComments";

    static boolean isInlineCommentsDisabled() {
        return Boolean.parseBoolean(System.getProperty(DISABLE_INLINE_COMMENTS_PROPERTY, "false"));
    }

    /**
     * Check if the environment has any undefined values. These are entries containing
     * the text VALUE_NOT_DEFINED.
     * @return true if and only if there are any entries containing the text
     * VALUE_NOT_DEFINED.
     */
    public boolean hasUndefinedValues() {
        for (Iterator i = elements().iterator(); i.hasNext(); ) {
            TestEnvironment.Element entry = (TestEnvironment.Element) (i.next());
            if (entry.value.indexOf("VALUE_NOT_DEFINED") >= 0)
                return true;
        }
        return false;
    }

    private void substituteChar(String[] v, char from, char to) {
        for (int i = 0; i < v.length; i++)
            v[i] = v[i].replace(from, to);
    }

    private void substituteMap(String[] v, String[] map) {
        if (map == null)
            return;

        // this algorithm is directly based on the "map" algorithm in
        // Slave.Map, which it supercedes
        for (int i = 0; i < v.length; i++) {
            String word = v[i];
            for (int j = 0; j+1 < map.length; j+=2) {
                String f = map[j];
                String t = map[j+1];
                for (int index = word.indexOf(f);
                     index != -1;
                     index = word.indexOf(f, index + t.length())) {
                    word = word.substring(0, index) + t + word.substring(index + f.length());
                }
            }
            v[i] = word;
        }
    }

    private String convertToName(String[] v) {
        String s = "";
        for (int i = 0; i < v.length; i++) {
            if (i > 0)
                s += '_';
            for (int j = 0; j < v[i].length(); j++) {
                char c = v[i].charAt(j);
                s += (isNameChar(c) ? c : '_');
            }
        }
        return s;
    }

    /**
     * Identifies the characters recognized for $ names
     */
    private static boolean isNameChar(char c) {
        return (Character.isUpperCase(c)
                || Character.isLowerCase(c)
                || Character.isDigit(c)
                || (c == '_')
                || (c == '.'));
    }

    /**
     * Enumerate the keys for this environment, including any inherited keys.
     * Use `lookup' to find the values of the individual keys.
     *
     * @return  An enumeration that yields the various keys, explicit or inherited,
     *          that are available in this environment. The keys do <em>not</em>
     *          include the `env.<em>environment-name</em>.' prefix of the corresponding
     *          property names.
     */
    public Set keys() {
        return table.keySet();
    }

    /**
     * Get a collection containing those entries in this environment that have been
     * referenced, either directly via lookup, or indirectly via the $ syntax in
     * other entries.
     * @return a collection of those entries in this environment that have been
     * referenced.
     * @see #resetElementsUsed
     */
    public Collection elementsUsed() {
        return cache.values();
    }

    /**
     * Reset the record of entries in this environment that have been referenced.
     * @see #elementsUsed
     */
    public void resetElementsUsed() {
        cache.clear();
    }

    /**
     * Enumerate the elements for this environment, including any inherited elements.
     *
     * @return  An enumeration that yields the various elements, explicit or inherited,
     *          that are available in this environment.
     */
    public Collection elements() {
        return table.values();
    }


    protected TestEnvironment(TestEnvironment o) {
        name = o.name;
        inherits = o.inherits;
        table = o.table;
        extras = (HashMap)(o.extras.clone());
    }

    /**
     * A class representing an entry in a test environment.
     */
    public class Element {
        /**
         * Create an entry for a test environment.
         * @param key The name of the entry
         * @param value The unresolved value of the entry
         * @param definedInEnv The name of the environment that defines this entry
         * @param definedInFile The name of the file (or table) that defines this entry
         */
        Element(String key, String value, String definedInEnv, String definedInFile) {
            this.key = key;
            this.value = value;
            this.definedInEnv = definedInEnv;
            this.definedInFile = definedInFile;
        }

        /**
         * Get the name of this entry.
         * @return the name of this entry
         */
        public String getKey() { return key; }

        /**
         * Get the (unresolved) value of this entry.
         * @return the (unresolved) value of this entry
         */
        public String getValue() { return value; }

        /**
         * Get the name of the environment that defines this entry.
         * @return the name of the environment that defines this entry
         */
        public String getDefinedInEnv() { return definedInEnv; }

        /**
         * Get the name of the file (or table) that defines this entry.
         * @return the name of the file (or table) that defines this entry
         */
        public String getDefinedInFile() { return definedInFile; }

        String key;
        String value;
        String definedInEnv;
        String definedInFile;
    }

    private String name;
    private String[] inherits;
    private HashMap table = new HashMap();
    private HashMap extras = new HashMap();
    private HashMap cache = new HashMap();

    private static final String[] EMPTY_STRING_ARRAY = {};
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TestEnvironment.class);
}
