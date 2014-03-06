/*
 * $Id$
 *
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.mrep;


import com.sun.javatest.ResourceLoader;
import com.sun.javatest.report.Report;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


class XMLReportReader {

    private final static String JAXP_SCHEMA_VALIDATION = "http://xml.org/sax/features/validation";

    // this copyes constants from non-API com.sun.org.apache.xerces.internal.jaxp.JAXPConstants
    private static final String JAXP_SCHEMA_LANGUAGE =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String W3C_XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";

    private static final String JAXP_SCHEMA_SOURCE =
        "http://java.sun.com/xml/jaxp/properties/schemaSource";

    Map readIDs(File file) throws SAXException, IOException, ParserConfigurationException {
        XMLReader reader = XMLReportReader.createXMLReader(true);
        IDHandler handler = new IDHandler();
        reader.setContentHandler(handler);
        reader.parse(new InputSource(file.getAbsolutePath()));

        return handler.getMap();
    }

    static XMLReader createXMLReader(boolean validating) throws SAXException, ParserConfigurationException {
        SAXParserFactory fact = SAXParserFactory.newInstance();
        fact.setValidating(validating);
        fact.setNamespaceAware(true);

        fact.setFeature(JAXP_SCHEMA_VALIDATION, true);
        SAXParser parser = fact.newSAXParser();
        parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        //File sc = ResourceLoader.getResourceFile("test_run_report.xsd", Report.class);
        InputStream sc = ResourceLoader.getResourceAsStream("test_run_report.xsd", Report.class);
        parser.setProperty(JAXP_SCHEMA_SOURCE, sc);

        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new ErrorHandler() {

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
        return reader;
    }


    private static class IDHandler extends DefaultHandler {
        private Map map = new HashMap();

        private long time = 0;

        public void startElement(String namespaceUri, String localName,
                String qualifiedName, Attributes attributes)
                throws SAXException {
            if (qualifiedName.equals(Scheme.TR)) {
                String url = attributes.getValue(Scheme.TR_URL);
                int id = (Integer.valueOf(attributes.getValue(Scheme.TR_WDID)))
                .intValue();
                String status = attributes.getValue(Scheme.TR_STATUS);
                map.put(url, new TestResultDescr(status, id, time));
            }
            if (qualifiedName.equals(Scheme.WD)) {
                Integer id = Integer.valueOf(attributes.getValue(Scheme.WD_ID));
                map.put(id, id);
            }
            if (qualifiedName.equals(Scheme.REPORT)) {
                String dateStr = (String) attributes
                        .getValue(Scheme.REPORT_GENTIME);
                try {
                    time = XMLReportWriter.ISO8601toDate(dateStr).getTime();
                } catch (ParseException e) {
                    throw new SAXException(e);
                }
            }
        }

        public Map getMap() {
            return map;
        }
    }

}

