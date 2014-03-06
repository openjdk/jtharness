/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

class Merger {

    /**
     * @param in array of files with XML reports
     * @param out file to put results
     * @param confilctResolver ConflictResolver to resolve conflicts during merge
     */
    public boolean merge(File[] in, File out, ConflictResolver confilctResolver) throws SAXException,
            ParserConfigurationException, IOException{
        // Maps with statistics
        Map[] inputs = new Map[in.length];
        // read statistics
        for (int i = 0; i < in.length; i++) {
            XMLReportReader reader = new XMLReportReader();
            Map map = reader.readIDs(in[i]);
            inputs[i] = map;
        }

        // shift word workgroups id renaming
        int allCnt = 0;
        // rename id's and find conflicts
        Set all = new HashSet();
        List confilcts = new ArrayList();
        for (int i = 0; i < in.length; i++) {
            int workdirsInFile = 0;
            Iterator it = inputs[i].keySet().iterator();
            Map newMap = new HashMap();
            while (it.hasNext()) {
                Object o =  it.next();
                // this is workdir ID
                if (o instanceof Integer) {
                    Integer id = (Integer) o;
                    Integer nid = new Integer(id.intValue() + allCnt);
                    newMap.put(id, nid);
                    workdirsInFile++;
                }
                // this is test result url
                if (o instanceof String) {
                    String url = (String) o;
                    if (all.contains(url) && !confilcts.contains(url)) {
                        confilcts.add(url);
                    }
                    all.add(url);
                    TestResultDescr td = (TestResultDescr)inputs[i].get(o);
                    td.setID(td.getID() + allCnt);
                    newMap.put(url, td);
                }

            }
            inputs[i] = newMap;
            allCnt += workdirsInFile;
        }

        // resolve each conflict
        for (int c = 0; c < confilcts.size(); c++) {
            String url = (String)confilcts.get(c);
            ArrayList tds = new ArrayList();
            for(int i = 0; i < in.length; i++) {
                TestResultDescr td = (TestResultDescr) inputs[i].get(url);
                if (td != null) {
                    td.setFile(in[i]);
                    tds.add(td);
                }
            }
            TestResultDescr[] tda = (TestResultDescr[])tds.toArray(new TestResultDescr[0]);
            int res = confilctResolver.resolve(url, tda);
            if (res < 0) {
                // cancel
                return false;
            }
            for(int i = 0; i < in.length; i++) {
                if (!in[i].equals(tda[res].getFile())) {
                    TestResultDescr td = (TestResultDescr)inputs[i].get(url);
                    if (td != null) {
                        td.setID(-1);
                    }
                }
            }
        }

        // make merge with using statistics
        new XMLReportWriter(out).write(in, inputs);
        return true;
    }
}
