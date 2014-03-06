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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import com.sun.javatest.report.XMLReportMaker;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;



class XMLReportWriter {
    public static String XML_CHARSET = "UTF-8";
    private final AttributesImpl emptyAttr = new AttributesImpl();
    private TransformerHandler ser;

    private Writer fw;

    public XMLReportWriter(File file) throws IOException{
        Properties outputProps = new Properties();
        outputProps.put("indent", "yes");

        outputProps.put("encoding", XML_CHARSET);

        SAXTransformerFactory stf = (SAXTransformerFactory )TransformerFactory.newInstance();
        stf.setAttribute("indent-number", 4);
        try {
            ser = stf.newTransformerHandler();
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        }

        ser.getTransformer().setOutputProperties(outputProps);

        fw = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(file), XMLReportMaker.XML_CHARSET));
        ser.setResult(new StreamResult(fw));
    }

    /**
     * Convert date to string in ISO-8601 or xs:dateTime format
     *
     * @param date
     *            Date
     * @return ISO-8601 String
     */
    static String dateToISO8601(Date date) {
        DateFormat dfISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        // format in (almost) ISO8601 format
        String dateStr = dfISO8601.format(date);
        // remap the timezone from 0000 to 00:00 (starts at char 22)
        return dateStr.substring(0, 22) + ":" + dateStr.substring(22);
    }

    /**
     * Convert string in ISO-8601 or xs:dateTime format to date
     *
     * @param dateStr
     *            ISO-8601 String
     * @return corresponding date
     */
    static Date ISO8601toDate(String dateStr) throws ParseException {
        DateFormat dfISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        // format in (almost) ISO8601 format
        Date date = dfISO8601.parse(dateStr.substring(0, 22)
                + dateStr.substring(23));
        // remap the timezone from 0000 to 00:00 (starts at char 22)
        return date;
    }

    private void sReport() throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", Scheme.XSI, "String", Scheme.XSI_VAL);
        atts.addAttribute("", "", Scheme.SCH_LOC, "String", Scheme.SCH_LOC_VAL);
        atts.addAttribute("", "", Scheme.REPORT_FORMST, "String", "v1");
        atts.addAttribute("", "", Scheme.REPORT_GENTIME, "String",
                dateToISO8601(new Date()));
        ser.startElement("", "", Scheme.REPORT, atts);
    }

    public void write(File[] file, Map[] map) throws SAXException,
            ParserConfigurationException, IOException {
        try {
            ser.startDocument();
            sReport();
            ser.startElement("", "", Scheme.WDS, emptyAttr);
            for (int i = 0; i < file.length; i++) {
                XMLReader reader = XMLReportReader.createXMLReader(false);
                reader.setContentHandler(new CopyHandler(ser, true, map[i]));
                reader.parse(new InputSource(file[i].getAbsolutePath()));
            }
            ser.endElement("", "", Scheme.WDS);
            ser.startElement("", "", Scheme.TRS, emptyAttr);
            for (int i = 0; i < file.length; i++) {
                XMLReader reader = XMLReportReader.createXMLReader(false);
                reader.setContentHandler(new CopyHandler(ser, false, map[i]));
                reader.parse(new InputSource(file[i].getAbsolutePath()));
            }
            ser.endElement("", "", Scheme.TRS);
            ser.endElement("", "", Scheme.REPORT);
            ser.endDocument();
        } finally {
            fw.close();
        }

    }
}

// This handler just copy one xml to other,
// modifying elements according mapping

class CopyHandler extends DefaultHandler {

    private ContentHandler ser;
    private LexicalHandler lh;

    // are we collect workdirs or not
    private final boolean isWorkDir;

    // is it need to write
    private boolean needWrite;

    // skipping by conflict resolving
    private boolean skipByConflict;

    // id -> new_id mapping
    // url -> new_TestDescr mapping
    private Map map;

    public CopyHandler(ContentHandler ser, boolean isWorkDir, Map map) {
        this.ser = ser;
        if (ser instanceof LexicalHandler) {
            lh = (LexicalHandler) ser;
        }
        this.isWorkDir = isWorkDir;
        this.needWrite = false;
        this.map = map;
        this.skipByConflict = false;
    }

    public void startElement(String namespaceUri, String localName,
            String qName, Attributes attrs) throws SAXException {
        if (needWrite == true && skipByConflict == false) {

            if (qName.equals(Scheme.WD)) {
                // for WD WD_ID may be updated
                AttributesImpl newAttrs = new AttributesImpl();
                for (int i = 0; i < attrs.getLength(); i++) {
                    if (attrs.getQName(i).equals(Scheme.WD_ID)) {
                        String id = String.valueOf((Integer) map.get(Integer
                                .valueOf(attrs.getValue(i))));
                        newAttrs.addAttribute(attrs.getURI(i), attrs
                                .getLocalName(i), attrs.getQName(i), attrs
                                .getType(i), id);
                    } else {
                        newAttrs.addAttribute(attrs.getURI(i), attrs
                                .getLocalName(i), attrs.getQName(i), attrs
                                .getType(i), attrs.getValue(i));
                    }
                }
                ser.startElement("", "", qName, newAttrs);
            } else if (qName.equals(Scheme.TR)) {
                // for TR TR_WDID may be updated
                // or TR may be skipped during conflic resolving
                AttributesImpl newAttrs = new AttributesImpl();
                for (int i = 0; i < attrs.getLength(); i++) {
                    if (attrs.getQName(i).equals(Scheme.TR_WDID)) {
                        int id = ((TestResultDescr) map.get(attrs
                                .getValue(Scheme.TR_URL))).getID();
                        if (id < 0) {
                            skipByConflict = true;
                            return;
                        }
                        String idS = String.valueOf(id);
                        newAttrs.addAttribute(attrs.getURI(i), attrs
                                .getLocalName(i), attrs.getQName(i), attrs
                                .getType(i), idS);
                    } else {
                        newAttrs.addAttribute(attrs.getURI(i), attrs
                                .getLocalName(i), attrs.getQName(i), attrs
                                .getType(i), attrs.getValue(i));
                    }
                }
                ser.startElement("", "", qName, newAttrs);
            } else {
                // by default source attributes are used
                ser.startElement("", "", qName, attrs);
            }

        }

        // don't write TRS, WDS
        // they are written by XMLReportWriter
        // also mode needWrite may be changed
        if (isWorkDir == true && qName.equals(Scheme.WDS)) {
            needWrite = true;
        }
        if (isWorkDir == false && qName.equals(Scheme.TRS)) {
            needWrite = true;
        }
    }

    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        if (this.needWrite == false || this.skipByConflict == true)
            return;
        // copy only text is really present
        if (String.copyValueOf(arg0, arg1, arg2).trim().equals(""))
            return;
        lh.startCDATA();
        ser.characters(arg0, arg1, arg2);
        lh.endCDATA();
    }

    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        // don't write TRS, WDS
        // they are written by XMLReportWriter
        // also mode needWrite may be changed
        if (isWorkDir == true && arg2.equals(Scheme.WDS)) {
            needWrite = false;
        }
        if (isWorkDir == false && arg2.equals(Scheme.TRS)) {
            needWrite = false;
        }
        if (needWrite == true) {
            if (skipByConflict == false)
                ser.endElement(arg0, arg1, arg2);
            else if (arg2.equals(Scheme.TR)) {
                skipByConflict = false;
            }
        }
    }
}
