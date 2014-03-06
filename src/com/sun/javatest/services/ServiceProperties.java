/*
 * $Id$
 *
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.services;

import com.sun.javatest.TestEnvironment;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


public class ServiceProperties {

    private ServiceProperties common;

    private Properties externalProps;

    private HashMap<String, String> lastResolution;

    private Map<String, Set<String>> varMap;

    private Map<String, ParametrizeValue> varProps;

    private Map<String, String> constProps;

    /**
     * Create new ServiceProperties object.
     * @param common Common properties, shared between all services. Null means
     * this is common properties, or there is no common properties at all.
     */
    public ServiceProperties(ServiceProperties common) {
        this.common = common;

        externalProps = new Properties();
        varMap = new HashMap();
        varProps = new HashMap();
        constProps = new HashMap();
    }

    /**
     * Method to check, that there exist some properties. Only individual, not
     * common properties are checked.
     * @return true, if there exist any individual properties.
     */
    public boolean isEmpty() {
        return (constProps == null || constProps.size() == 0) &&
                (varProps == null || varProps.size() == 0) &&
                (varMap == null || varMap.size() == 0);
    }

    /**
     * Adds new individual (or common, in case it is common properties)property.
     * Value is parsed to find all references to other
     * properties, then internal data structures are filled.
     * @param key key of the property.
     * @param value value of the property. May be null.
     */
    public void addProperty(String key, String value) {
        if (value == null) {
            commitProperty(key, null);
            return;
        }

        ParametrizeValue val = new ParametrizeValue();

        while (value.length() > 0) {
            Term t = new Term();
            int len = nextTerm(value, t);
            value = value.substring(len);
            val.addTerm(t);
        }

        commitProperty(key, val);
    }

    /**
     * Sets external properties. They are interpreted as key-value pairs
     * without variable values. Current resolution of variable properties removes,
     * another will be built at the next
     * {@link com.sun.javatest.services.ServiceProperties#resolveProperties()}
     * request.
     * @param props external properties to use to resolve own variable values.
     */
    public void setExternalProperties(Properties props) {
        externalProps = props;
        lastResolution = null;
        if (common != null) {
            common.setExternalProperties(props);
        }
    }

    /**
     * Does the same as {@code setExternalProperties(Properties props)},
     * except that properties are firstly extracted from TestEnvironment object.
     * @param env
     */
    public void setExternalProperties(TestEnvironment env) {
        Properties props = new Properties();
        for (Object o : env.keys()) {
            String key = (String)o;
            String value = new String();
            try {
                for (String s : env.lookup(key)) {
                    value += s;
                }
            } catch (TestEnvironment.Fault ex) {
            }
            props.setProperty(key, value);
        }

        props.putAll(env.getExtraValues());

        setExternalProperties(props);
    }

    /**
     * @return returns currently used set of external properties.
     */
    public Properties getExternalProperties() {
        return externalProps;
    }

    /**
     * Resolves variable properties using current set of external properties.
     * Returned map contains only resolved individual properties, not common.
     * @return map with resolved individual properties.
     */
    public Map<String, String> resolveProperties() {
        if (lastResolution != null) {
            return (Map)lastResolution.clone();
        }

        HashMap<String, String> result = new HashMap();
        result.putAll(constProps);

        Map<String, String> justResolved = new HashMap();
        justResolved.putAll(constProps);

        if (common != null) {
            justResolved.putAll(common.resolveProperties());
        }
        for (Object key : externalProps.keySet()) {
            String skey = (String)key;
            justResolved.put(skey, externalProps.getProperty(skey));
        }

        Map<String, ParametrizeValue> copyVars = copyVarProps();
        while (justResolved.size() > 0) {
            justResolved = resolveVars(copyVars, justResolved);
            result.putAll(justResolved);
        }

        lastResolution = new HashMap(result);

        return result;
    }

    private Map<String, String> resolveVars(Map<String, ParametrizeValue> varProps,
            Map<String, String> resolved) {

        Map<String, String> result = new HashMap();

        for (String key : resolved.keySet()) {
            if (varMap.containsKey(key)) {
                for (String pkey : varMap.get(key)) {
                    if (varProps.containsKey(pkey)) {
                        ParametrizeValue val = varProps.get(pkey);
                        val.replace(key, resolved.get(key));
                        if (val.resolved()) {
                            varProps.remove(pkey);
                            result.put(pkey, val.stringValue());
                        }
                    }
                }
            }
        }

        return result;
    }

    private Map<String, ParametrizeValue> copyVarProps() {
        Map<String, ParametrizeValue> copy = new HashMap();
        for (String key : varProps.keySet()) {
            copy.put(key, varProps.get(key).copy());
        }

        return copy;
    }





    private void commitProperty(String key, ParametrizeValue value) {
        if (value == null) {
            value = new ParametrizeValue();
            Term t = new Term();
            t.setValue(key, true);
            List<Term> l = new LinkedList();
            l.add(t);
            value.setValue(l);
        }

        if (value.resolved()) {
            constProps.put(key, value.stringValue());
        }
        else {
            constructMapping(key, value);
            varProps.put(key, value);
        }

        lastResolution = null;
    }

    private void constructMapping(String key, ParametrizeValue value) {
        if (value.resolved()) {
            return;
        }

        for (Term t : value.getTerms()) {
            if (t.isVariable()) {
                String paramName = t.getValue();

                if (varMap.containsKey(paramName)) {
                    varMap.get(paramName).add(key);
                }
                else {
                    HashSet<String> usages = new HashSet();
                    usages.add(key);
                    varMap.put(paramName, usages);
                }
            }
        }

        if (varMap.containsKey(key)) {
            varMap.get(key).add(key);
        }
        else {
            HashSet<String> usages = new HashSet();
            usages.add(key);
            varMap.put(key, usages);
        }

    }


    private int nextTerm(String s, Term dest) {
        int i = 0;
        boolean var = s.charAt(0) == '$' && s.charAt(1) == '{';
        if (var) {
            while (i < s.length() && s.charAt(i) != '}') {
                i++;
            }
            i++; // to count '}'
            s = s.substring(2, i-1);
        }
        else {
            while (i < s.length()) {
                if (s.charAt(i) == '$' && s.charAt(i -1) != '\\') {
                    break;
                }
                else {
                    i++;
                }
            }
            s = s.substring(0, i);
        }
        dest.setValue(s, var);
        return i;
    }

}


class ParametrizeValue {
    private List<Term> terms;
    private int varNumb;

    public ParametrizeValue() {
        terms = new LinkedList();
        varNumb = 0;
    }

    public ParametrizeValue(String constValue) {
        this();
        Term t = new Term();
        t.setValue(constValue, false);
        terms.add(t);
    }

    public void setValue(List<Term> newValue) {
        terms = newValue;
        varNumb = 0;
        for (Term t : terms) {
            if (t.isVariable())
                varNumb++;
        }
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void addTerm(Term t) {
        terms.add(t);
        varNumb += t.isVariable() ? 1 : 0;
    }

    public ParametrizeValue copy() {
        ParametrizeValue copy = new ParametrizeValue();
        copy.varNumb = varNumb;

        for (Term t : terms) {
            Term tcopy = new Term();
            tcopy.setValue(t.getValue(), t.isVariable());
            copy.terms.add(tcopy);
        }

        return copy;
    }

    public void replace (String key, String value) {
        for (Term t : terms) {
            if (t.isVariable() && t.getValue().equals(key)) {
                t.setValue(value, false);
                varNumb -= 1;
            }
        }
    }

    public boolean resolved() {
        return varNumb == 0;
    }

    public String stringValue() {
        if (varNumb != 0) {
            return null;
        }

        String res = "";
        for (Term t : terms) {
            res += t.getValue();
        }
        return res;
    }
}

class Term {
    private boolean var;

    private String value;

    public String getValue() {
        return value;
    }

    public boolean isVariable() {
        return var;
    }

    public void setValue(String val, boolean var) {
        value = val;
        this.var = var;
    }
}
