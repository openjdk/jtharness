/*
 * $Id$
 *
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool.jthelp;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class HelpSet {

    private HashMap<String, URL> localMap;
    private HashMap<String, URL> combinedMap;
    private String helpTitle = "HelpSet Title";
    private ArrayList<HelpSet> helpSets = new ArrayList<>();

    public HelpSet() {

    }

    public String getTitle() {
        return helpTitle;
    }

    public HelpSet(ClassLoader loader, URL url) {
        this(loader, url, "");
    }

    public HelpSet(ClassLoader loader, URL url, String prefix) {

        XMLStreamReader reader = getXMLReader(url);
        try {
            while (reader != null && reader.hasNext()) {
                int Event = reader.next();
                switch (Event) {
                    case XMLStreamConstants.START_ELEMENT: {
                        if ("title".equals(reader.getLocalName())) {
                            helpTitle = reader.getElementText();
                        }
                        if ("mapref".equals(reader.getLocalName())) {
                            localMap = new HashMap<>();
                            String mapLocation = reader.getAttributeValue(0);
                            URL u = findHelpSet(loader, prefix + mapLocation);
                            if (u == null) {
                                prefix = prefix + "moreInfo/";
                                u = findHelpSet(loader, prefix + mapLocation);
                            }
                            prefix = prefix + mapLocation.substring(0, mapLocation.lastIndexOf("/") + 1);
                            localMap = parseMap(loader, u, prefix);
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

    }

    public void add(HelpSet hs) {
        helpSets.add(hs);
    }

    public boolean remove(HelpSet hs) {
        return helpSets.remove(hs);
    }

    public ArrayList<HelpSet> getHelpSets() {
        return helpSets;
    }

    public HashMap<String, URL> getCombinedMap() {
        if (combinedMap == null) {
            combinedMap = new HashMap<>();
            if (localMap != null) {
                combinedMap.putAll(localMap);
            }
            for (HelpSet hs : helpSets) {
                combinedMap.putAll(hs.getCombinedMap());
            }
        }
        return combinedMap;
    }

    private HashMap<String, URL> parseMap(ClassLoader loader, URL url, String prefix) {

        HashMap<String, URL> result = new HashMap<>();

        HashMap<String, String> helpMap = readHelpMap(url);
        for (String key : helpMap.keySet()) {
            URL u = findHelpSet(loader, prefix + helpMap.get(key));
            if (u == null) {
                u = findHelpSet(loader, helpMap.get(key));
            }
            result.put(key, u);
        }

        return result;
    }

    public static HashMap<String, String> readHelpMap(URL url) {

        HashMap<String, String> result = new HashMap<>();
        XMLStreamReader reader = getXMLReader(url);

        try {
            while (reader != null && reader.hasNext()) {
                int Event = reader.next();
                switch (Event) {
                    case XMLStreamConstants.START_ELEMENT: {
                        if ("mapID".equals(reader.getLocalName())) {
                            String target = reader.getAttributeValue(0);
                            String htmlurl = reader.getAttributeValue(1);
                            if (htmlurl.contains("#")) {
                                htmlurl = htmlurl.substring(0, htmlurl.indexOf("#"));
                            }
                            result.put(target, htmlurl);
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return result;
    }

    public HashMap<String, URL> getLocalMap() {
        return localMap;
    }

    private static XMLStreamReader getXMLReader(URL url) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        URLConnection urlc = null;
        try {
            urlc = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        XMLStreamReader reader = null;
        try {
            if (urlc != null) {
                reader = factory.createXMLStreamReader(urlc.getInputStream());
            }
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }

        return reader;
    }

    public static URL findHelpSet(ClassLoader loader, String name) {

        if (loader == null) {
            return ClassLoader.getSystemClassLoader().getResource(name);
        }

        URL result = loader.getResource(name);

        if (result == null) {
            result = loader.getResource("com/sun/javatest/help/" + name);
        }

        if (result == null && !name.endsWith(".hs")) {
            result = findHelpSet(loader, name + ".hs");
        }

        return result;

    }
}
