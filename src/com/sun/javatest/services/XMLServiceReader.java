/*
 * $Id$
 *
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.ResourceLoader;
import com.sun.javatest.TestSuite;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Implementation of {@link com.sun.javatest.services.ServiceReader} interface.
 * Reads info from one XML file. Path to this file may be specified as argument
 * to it's init method. Otherwise, {@code test_suite_root/lib/services.xml} is
 * searched.
 */
public class XMLServiceReader implements ServiceReader {

    private File xml;
    private Document doc;
    private TestSuite ts;

    public static final String SERVICES_XML =
            File.separator + "lib" + File.separator + "services.xml";

    /**
     * @return value of SERVICE_XML
     */
    public String getServiceDescriptorFileName() {
        return SERVICES_XML;
    }

    public void init(TestSuite ts, String[] args) {
        this.ts = ts;
        File tsRoot = ts.getRootDir();
        if (args == null || args.length == 0) {
            xml = new File(tsRoot, SERVICES_XML);
        }
        else {
            String path = args[0].replace("/", File.separator);
            xml = new File(tsRoot, path);
        }

        try {
            SchemaFactory xsdFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            File sch = ResourceLoader.getResourceFile("services.xsd", this.getClass());
            Schema schema = xsdFactory.newSchema(sch.toURI().toURL());

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

            domFactory.setNamespaceAware(true);

            // setValidating(false) is not an error.
            // we have to do it because we set schema object.
            // JAXP is wonderful!
            domFactory.setValidating(false);
            domFactory.setSchema(schema);

            DocumentBuilder parser = domFactory.newDocumentBuilder();
            parser.setErrorHandler(new ErrorHandler() {

                @Override
                public void warning(SAXParseException exception) throws SAXException {

                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });


            doc = parser.parse(xml);
        } catch (SAXParseException ex) {
            // we don't have an access to UI here (((
            // so just print it
            ts.getNotificationLog(null).severe(ex.getMessage());
        } catch (SAXException ex) {
// TODO            logExc();
        } catch (ParserConfigurationException ex) {
// TODO            logParseExc();
        } catch (IOException ex) {
// TODO            logIOExc();
        }
    }

    public Map<String, Service> readServices() {
        ServiceProperties common = readProperties(doc);

        return readServices(doc, common);
    }

    public Set<TestPath> readTestServiceMap() {
        Set<TestPath> result = new HashSet();

        if (doc == null) {
            return result;
        }

        NodeList paths = doc.getElementsByTagName("testpath");
        for (int i = 0; i < paths.getLength(); i++) {
            Element path = (Element)paths.item(i);

            String regexp = null;
            String matcher = null;
            if (path.hasAttribute("path")) {
                regexp = path.getAttribute("path");
            }
            if (path.hasAttribute("matcher")) {
                matcher = path.getAttribute("matcher");
            }
            TestPath tPath = new TestPath(ts, regexp, matcher);
            NodeList refs = path.getElementsByTagName("service");

            for (int j = 0; j < refs.getLength(); j++) {
                Element ref = (Element)refs.item(j);
                tPath.addService(ref.getAttribute("refid"));
            }
            result.add(tPath);
        }
        return result;
    }

    private ServiceProperties readProperties(Document doc) {
        if (doc == null) {
            return null;
        }

        ServiceProperties res = new ServiceProperties(null);

        NodeList imports = doc.getElementsByTagName("propertyfile");
        FileInputStream fis = null;
        for (int i = 0; i < imports.getLength(); i++) {
            try {
                Element elem = (Element) imports.item(i);
                String path = elem.getAttribute("file");
                File propFile = new File(xml.getAbsolutePath() + path);
                Properties props = new Properties();
                fis = new FileInputStream(propFile);
                props.load(fis);
                for (Object key : props.keySet()) {
                    String skey = (String)key;
                    res.addProperty(skey, props.getProperty(skey));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            finally {
                try { if (fis != null) fis.close(); } catch (IOException e) {}
// TODO                logWrongPropFile();
            }
        }

        NodeList defs = doc.getElementsByTagName("property");
        for (int i = 0; i < defs.getLength(); i++) {
            Element elem = (Element)defs.item(i);
            String key = elem.getAttribute("name");
            String value = null;
            if (elem.hasAttribute("value")) {
                value = elem.getAttribute("value");
            }
            res.addProperty(key, value);
        }

        if (res.isEmpty()) {
            return null;
        }
        return res;
    }

    private Map<String, Service> readServices(Document doc, ServiceProperties common) {
        Map<String, Service> result = new TreeMap();

        if (doc == null) {
            return result;
        }

        Node root = doc.getDocumentElement();
        Node child = root.getFirstChild();
        while (child != null) {
            if(child instanceof Element &&
                    ((Element)child).getNodeName().equals("service")) {
                try {
                    Element elem = (Element) child;

                    String id = elem.getAttribute("id");
                    String svClassName = elem.getAttribute("class");
                    String descr = elem.getAttribute("description");

                    Service service = (Service) ts.loadClass(svClassName).newInstance();
                    service.setId(id);
                    service.setDescription(descr);
                    Connector conn = new LocalConnector(service.getDefaultServiceExecutor());
                    service.setConnector(conn);

                    ServiceProperties servProps = new ServiceProperties(common);
                    NodeList args = elem.getElementsByTagName("arg");
                    for (int j = 0; j < args.getLength(); j++) {
                        Element arg = (Element) args.item(j);
                        String key = arg.getAttribute("name");
                        String value = null;
                        if (arg.hasAttribute("value")) {
                            value = arg.getAttribute("value");
                        }
                        servProps.addProperty(key, value);
                    }
                    service.setProperties(servProps);

                    result.put(service.getId(), service);
                } catch (TestSuite.Fault ex) {
// TODO                    logClassExc();
                } catch (InstantiationException ex) {
// TODO                    logClassExc();
                } catch (IllegalAccessException ex) {
// TODO                    logClassExc();
                }
            }
            child = child.getNextSibling();
        }

        return result;
    }

}
