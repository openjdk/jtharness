/*
 * $Id$
 *
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.TestSuite;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class that reads service descriptor from a property file.
 * The property names are expected to be as show shown below
 * <pre>
 * service.<ID>.class=....
 * service.<ID>.description=...
 * service.<ID>.arg.<ARGNAME>=...
 *
 * property.<GENERAL-PROPERTY-NAME>=...
 *
 * testpath.<TP-ID>.path=...
 * testpath.<TP-ID>.ids=[space separated list of service IDs]
 * </pre>
 *
 *
 * Example of such property files:
 * <pre>
 * service.rmi.class=com.my.service.RMIService
 * service.rmi.description=The service starts rmid
 * service.rmi.arg.port=1234
 * service.rmi.arg.host=localhost
 *
 * service.orb.class=com.my.services.ORBService
 * service.orb.description=The service starts tnamesev
 *
 * property.general.propName=This is the value of a general property"
 * property.some.prop=One more general property
 *
 * testpath.1.path=rmid
 * testpath.1.ids=rmi
 *
 * testpath.2.path=tnameserv
 * testpath.2.ids=orb
 *
 * </pre>
 */
public class PropertyServiceReader implements ServiceReader {

    private File propFile;
    private Properties props;
    private TestSuite ts;

    public static final String PROPERTY_PREFIX = "property";
    public static final String SERVICE_PREFIX  = "service";
    public static final String TESTPATH_PREFIX = "testpath";

    public static final String SERVICES_PROPERTIES =
            File.separator + "lib" + File.separator + "services.properties";

    public void init(TestSuite ts, String[] args) {
        this.ts = ts;
        File tsRoot = ts.getRootDir();
        if (args == null || args.length == 0) {
            propFile = new File(tsRoot, SERVICES_PROPERTIES);
        } else {
            String path = args[0].replace("/", File.separator);
            propFile = new File(tsRoot, path);
        }
        props = new Properties();

        try {
            props.load(new FileReader(propFile));
        } catch (IOException ex) {
        }
    }

    /**
     * Finds all unique IDs of keys started with a given prefix.
     * Keys are expected to be in format: <prefix>.<ID>.<prop.name>
     * For example:
     * findIDs("a", {a.1.x, a.1.y, a.hey.x, b.3.z, a.hey.z}) will return ("1", "hey").
     *
     * @param keys - set of property names
     * @param prefix - prefix to look for
     * @return HashSet<String>
     */
    Set<String> findIDs(Set<String> keys, String prefix) {
        int len = prefix.length() + 1;
        Set<String> set = new HashSet<String>();
        for (String key: keys) {
            if (key.startsWith(prefix)) {
                int i = key.indexOf(".", len);
                if (i > 0) {
                    set.add(key.substring(len, i));
                }
            }
        }
        return set;
    }

    /**
     * Finds keys started with a given prefix and return list of names without
     * prefix. Keys are expected to be in format: <prefix>.<prop.name>
     * For example:
     * findProps("a", {a.1.x, a.1.y, a.hey.x, b.3.z}) will return (1.x, 1.y, hey.x).
     *
     * @param keys - set of property names
     * @param prefix - prefix to look for
     * @return HashSet<String>
     */
    Set<String> findProps(Set<String> keys, String prefix) {
        int len = prefix.length() + 1;
        Set<String> set = new HashSet<String>();
        for (String key: keys) {
            if (key.startsWith(prefix)) {
                set.add(key.substring(len));
            }
        }
        return set;
    }

    public Map<String, Service> readServices() {
        Map<String, Service> result = new TreeMap<String, Service>();
        if (props == null) {
            return result;
        }

        // read properties first
        ServiceProperties common = new ServiceProperties(null);
        Set<String> allKeys = props.stringPropertyNames();

        Set<String> propertyNames = findProps(allKeys, PROPERTY_PREFIX);
        for (String propName: propertyNames) {
            String propValue = props.getProperty(PROPERTY_PREFIX + "." + propName);
            common.addProperty(propName, propValue);
        }

        Set<String> serviceIDs = findIDs(allKeys, SERVICE_PREFIX);
        for (String serviceID: serviceIDs) {
            String serviceClass = props.getProperty(SERVICE_PREFIX + "." + serviceID + ".class");
            String serviceDescr = props.getProperty(SERVICE_PREFIX + "." + serviceID + ".description");
            if (serviceClass == null) {
                throw new Error("No service classname is provided for "
                        + serviceID + " in " + propFile);
            }
            try {

                Service service = (Service) ts.loadClass(serviceClass).newInstance();
                service.setId(serviceID);
                service.setDescription(serviceDescr);
                Connector conn = new LocalConnector(service.getDefaultServiceExecutor());
                service.setConnector(conn);

                ServiceProperties servProps = new ServiceProperties(common);
                String argPrefix = SERVICE_PREFIX + "." + serviceID + ".arg";
                Set<String> serviceArgsNames = findProps(allKeys, argPrefix);
                for (String argName: serviceArgsNames) {
                    String argValue = props.getProperty(argPrefix + "." + argName);
                    servProps.addProperty(argName, argValue);
                }


                service.setProperties(servProps);
                result.put(service.getId(), service);

            } catch (Exception ex) {
                throw new Error("Failed to start services", ex);
            }
        }
        return result;
    }

    public Set<TestPath> readTestServiceMap() {
        Set<TestPath> result = new HashSet();

        if (props == null) {
            return result;
        }

        Set<String> allKeys = props.stringPropertyNames();
        Set<String> testpathIDs = findIDs(allKeys, TESTPATH_PREFIX);
        for (String tpID: testpathIDs) {
            String path = props.getProperty(TESTPATH_PREFIX + "." + tpID + ".path");
            String matcher = props.getProperty(TESTPATH_PREFIX + "." + tpID + ".matcher");
            String ids = props.getProperty(TESTPATH_PREFIX + "." + tpID + ".ids");
            TestPath tPath = new TestPath(ts, path, matcher);
            String[] refIDs = ids.split(" ");
            for (String refID: refIDs) {
                tPath.addService(refID);
            }
            result.add(tPath);
        }
        return result;
    }

    public String getServiceDescriptorFileName() {
        return SERVICES_PROPERTIES;
    }

}
