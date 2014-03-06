/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.report;

import com.sun.javatest.Status;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ext.LexicalHandler;

public class XMLReportMaker {

    private TransformerHandler ser;

    XMLReportMaker(Writer w) throws IOException {

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
        ser.setResult(new StreamResult(w));
    }

    void sDocument() throws SAXException {
        ser.startDocument();
    }

    void eDocument() throws SAXException, IOException {
        ser.endDocument();
    }

    void sReport() throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", Scheme.XSI, "String", Scheme.XSI_VAL);
        atts.addAttribute("", "", Scheme.SCH_LOC, "String", Scheme.SCH_LOC_VAL);
        atts.addAttribute("", "", Scheme.REPORT_FORMST, "String", "v1");
        atts.addAttribute("", "", Scheme.REPORT_GENTIME, "String", XMLReport.Utils.dateToISO8601(new Date()));
        sE(Scheme.REPORT, atts);
    }

    void eReport() throws SAXException {
        eE(Scheme.REPORT);
    }

    void sSummary() throws SAXException {
        sE(Scheme.SUMMARY);
    }

    void eSummary() throws SAXException {
        eE(Scheme.SUMMARY);
    }


    void sWorkdirectories() throws SAXException {
        sE(Scheme.WDS);
    }

    void eWorkdirectories() throws SAXException {
        eE(Scheme.WDS);
    }

    void sEnvironment(String name, String descr) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (name != null && ! "".equals(name.trim())) {
            atts.addAttribute("", "", Scheme.ENV_NAME, "String", name);
        }
        if (descr != null && ! "".equals(descr.trim())) {
            atts.addAttribute("", "", Scheme.ENV_DESCR, "String", descr);
        }
        sE(Scheme.ENV, atts);
    }

    void eEnvironment() throws SAXException {
        eE(Scheme.ENV);
    }

    void conCur(int val) throws SAXException  {
        sE(Scheme.CONC);
        String sVal = Integer.toString(val);
        ser.characters(sVal.toCharArray(), 0, sVal.length());
        eE(Scheme.CONC);
    }

    void timeOut(float val) throws SAXException  {
        sE(Scheme.TIMO);
        String sVal = Float.toString(val);
        ser.characters(sVal.toCharArray(), 0, sVal.length());
        eE(Scheme.TIMO);
    }

    void sWorkdirectory(String jti) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", Scheme.WD_ID, "String", "1");
        if (jti != null) {
            atts.addAttribute("", "", Scheme.WD_JTI, "String", jti);
        }
        sE(Scheme.WD, atts);
    }

    void eWorkdirectory() throws SAXException {
        eE(Scheme.WD);
    }

    void sTestResults() throws SAXException {
        sE(Scheme.TRS);
    }

    void eTestResults() throws SAXException {
        eE(Scheme.TRS);
    }

    void sTestResult(String url, Status st, int id) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", Scheme.TR_URL, "String", url);
        atts.addAttribute("", "", Scheme.TR_STATUS, "String", XMLReport.Utils.statusToString(st));
        atts.addAttribute("", "", Scheme.TR_WDID, "Integer", Integer.toString(id));
        sE(Scheme.TR, atts);
    }

    void eTestResult() throws SAXException {
        eE(Scheme.TR);
    }

    void sDescriptionData() throws SAXException {
        sE(Scheme.DESCR_DATA);
    }

    void eDescriptionData() throws SAXException {
        eE(Scheme.DESCR_DATA);
    }

    void sTAnnotationData() throws SAXException {
        sE(Scheme.TANNOT_DATA);
    }

    void eTAnnotationData() throws SAXException {
        eE(Scheme.TANNOT_DATA);
    }

    void sKeyWords() throws SAXException {
        sE(Scheme.KEY_WORDS);
    }

    void sKeyWords(String expr) throws SAXException {

        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", Scheme.KEYWORDS_EXPR, "String", expr);
        sE(Scheme.KEY_WORDS, atts);
    }

    void eKeyWords() throws SAXException {
        eE(Scheme.KEY_WORDS);
    }

    void sTestEnvironment() throws SAXException {
        sE(Scheme.TEST_ENV);
    }

    void eTestEnvironment() throws SAXException {
        eE(Scheme.TEST_ENV);
    }

    void sResultProps(String time) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (time != null) {
            try {
                time = XMLReport.Utils.dateToISO8601(XMLReport.Utils.jtrToDate(time));
            } catch (ParseException ex) {
                throw new SAXException(ex);
            }
            atts.addAttribute("", "", Scheme.RES_PROP_TIM, "String", time);
        }
        sE(Scheme.RES_PROP, atts);
    }

    void eResultProps() throws SAXException {
        eE(Scheme.RES_PROP);
    }

    void sSections() throws SAXException {
        sE(Scheme.SES);
    }

    void eSections() throws SAXException {
        eE(Scheme.SES);
    }

    void sStdValues() throws SAXException {
        sE(Scheme.STD_VALS);
    }

    void eStdValues() throws SAXException {
        eE(Scheme.STD_VALS);
    }

    void sPriorStatusList() throws SAXException {
        sE(Scheme.PRIOS);
    }

    void ePriorStatusList() throws SAXException {
        eE(Scheme.PRIOS);
    }

    void sExclList() throws SAXException {
        sE(Scheme.EXCL_LIST);
    }

    void eExclList() throws SAXException {
        eE(Scheme.EXCL_LIST);
    }

    void sTests() throws SAXException {
        sE(Scheme.TESTS);
    }

    void eTests() throws SAXException {
        eE(Scheme.TESTS);
    }

    void sInterview() throws SAXException {
        sE(Scheme.INT);
    }

    void eInterview() throws SAXException {
        eE(Scheme.INT);
    }

    void sQuestion(String value, String text, String summary) throws SAXException {

        AttributesImpl atts = new AttributesImpl();
        if (summary != null) {
            atts.addAttribute("", "", Scheme.QUEST_SUMM, "String", summary);
        }
        if (value != null) {
            atts.addAttribute("", "", Scheme.QUEST_VALUE, "String", value);
        }
        if (value != null) {
            atts.addAttribute("", "", Scheme.QUEST_TEXT, "String", text);
        }

        sE(Scheme.QUEST, atts);
    }

    void eQuestion() throws SAXException {
        eE(Scheme.QUEST);
    }


    void sListQuestion() throws SAXException {
        sE(Scheme.LIST_QUEST);
    }

    void eListQuestion() throws SAXException {
        eE(Scheme.LIST_QUEST);
    }

    void sChoiceQuestion() throws SAXException {
        sE(Scheme.CHOICE_QUEST);
    }

    void eChoiceQuestion() throws SAXException {
        eE(Scheme.CHOICE_QUEST);
    }

    void sPropertiesQuestion()  throws SAXException {
        sE(Scheme.PROP_QUEST);
    }

    void ePropertiesQuestion()  throws SAXException {
        eE(Scheme.PROP_QUEST);
    }

    void sGroup(String name, String hd1, String hd2) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (name != null) {
            atts.addAttribute("", "", Scheme.GROUP_NAME, "String", name);
        }
        if (hd1 != null) {
            atts.addAttribute("", "", Scheme.GROUP_HD1, "String", hd1);
        }
        if (hd2 != null) {
            atts.addAttribute("", "", Scheme.GROUP_HD2, "String", hd2);
        }
        sE(Scheme.GROUP, atts);
    }

    void eGroup(String name, String h1, String h2) throws SAXException {
        eE(Scheme.GROUP);
    }

    void makeRow(String key, String val) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (key != null) {
            atts.addAttribute("", "", Scheme.ROW_KEY, "String", key);
        }
        if (val != null) {
            atts.addAttribute("", "", Scheme.ROW_VAL, "String", val);
        }
        sE(Scheme.ROW, atts);
        eE(Scheme.ROW);
    }

    void makeChoices(String[] ch, String[] dispCh) throws SAXException {
        if (ch != null) {
            for (int i = 0; i < ch.length; i++) {
                makeChoice(ch[i], i < dispCh.length ? dispCh[i] : null);
            }
        }
    }

    void makeChoices(String[] ch, String[] dispCh, boolean[] values) throws SAXException {
        if (ch != null) {
            for (int i = 0; i < ch.length; i++) {
                makeChoice(ch[i], i < dispCh.length ? dispCh[i] : null, i < values.length ? values[i] : false);
            }
        }
    }


    private void makeChoice(String ch, String di) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (ch != null) {
            atts.addAttribute("", "", Scheme.CHOICE_CH, "String", ch);
        }
        if (di != null) {
            atts.addAttribute("", "", Scheme.CHOICE_DCH, "String", di);
        }
        sE(Scheme.CHOICE, atts);
        eE(Scheme.CHOICE);
    }

    private void makeChoice(String ch, String di, boolean val) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", Scheme.CHOICE_CH, "String", ch);
        if (di != null) {
            atts.addAttribute("", "", Scheme.CHOICE_DCH, "String", di);
        }
        atts.addAttribute("", "", Scheme.CHOICE_VAL, "String", Boolean.toString(val));
        sE(Scheme.CHOICE, atts);
        eE(Scheme.CHOICE);
    }


    void makeEntireTestTree() throws SAXException {
        sE(Scheme.ENTTREE);
        eE(Scheme.ENTTREE);
    }

    void sSection(String title, Status st) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", Scheme.SE_TIT, "String", title);
        if (st != null) {
            atts.addAttribute("", "", Scheme.SE_ST, "String", XMLReport.Utils.statusToString(st));
        }
        sE(Scheme.SE, atts);
    }

    void eSection() throws SAXException {
        eE(Scheme.SE);
    }

    void sOutput(String title, String content) throws SAXException, IOException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", Scheme.OU_TIT, "String", title);
        sE(Scheme.OU, atts);
        if (content != null && content.length() > 0) {
            writeCDATA(ser, ser, content);
        }
    }

    void eOutput() throws SAXException {
        eE(Scheme.OU);
    }

    void makeTemplateInfo(String tPath, String name, String descr) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (tPath != null) {
            atts.addAttribute("", "", Scheme.TEM_FILE, "String", tPath);
        }
        if (name != null) {
            atts.addAttribute("", "", Scheme.TEM_NAME, "String", name);
        }
        if (descr != null) {
            atts.addAttribute("", "", Scheme.TEM_DESCRIPTION, "String", descr);
        }
        sE(Scheme.TEMPLATE, atts);
        eE(Scheme.TEMPLATE);
    }


    void makeProperty(String key, String val) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", Scheme.PR_NAME, "String", key);
        atts.addAttribute("", "", Scheme.PR_VAL, "CDATA", val);
        sE(Scheme.PR, atts);
        eE(Scheme.PR);
    }

    void makeItem(String val) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", Scheme.IT_VAL, "CDATA", val);
        sE(Scheme.IT, atts);
        eE(Scheme.IT);
    }

    void makeItems(String[] vals) throws SAXException {
        if (vals != null) {
            for (int i = 0 ; i < vals.length; i++) {
                if (vals[i] != null) {
                    makeItem(vals[i]);
                }
            }
        }
    }

    void makeItems(File[] files) throws SAXException {
        if (files != null) {
            for (int i = 0 ; i < files.length; i++) {
                makeItem(files[i].getPath());
            }
        }
    }


    private void sE(String name) throws SAXException {
        ser.startElement("","",name, emptyAttr);
    }

    private void sE(String name, Attributes atts) throws SAXException {
        ser.startElement("","",name, atts);
    }

    private void eE(String name) throws SAXException {
        ser.endElement("","",name);
    }

    public static void writeCDATA(LexicalHandler lh, ContentHandler ser, String cdata) throws IOException, SAXException {

        cdata = convertProhibitedChars(cdata);

        if (lh != null) {
            int start = 0;
            int end;
            while ( start < cdata.length()) {
                end = cdata.length(); int xpos = cdata.indexOf("]]>", start);
                if (xpos != -1) {
                    end = xpos+1;
                }
                lh.startCDATA();
                String fragment= cdata.substring(start, end);
                ser.characters(fragment.toCharArray(), 0, fragment.length());
                lh.endCDATA();
                start = end;
            }
        }
    }

    public static String convertProhibitedChars(String cdata) {
        StringBuffer sb = new StringBuffer();
        char [] data = cdata.toCharArray();
        for (int i = 0; i < data.length; i++) {
            if (prohibited(data[i])) {
                sb.append("\\u");
                String rX = Integer.toHexString((int)data[i]);
                for (int ii = rX.length();  ii < 4; ii++) {
                    sb.append("0");  //
                }
                sb.append(rX);
            } else {
                sb.append(data[i]);
            }
        }
        return sb.toString();
    }

    // XML 1.0 specification ( http://www.w3.org/TR/2004/REC-xml-20040204/ ) defines legal chars:
    // Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
    public static boolean prohibited(int c) {
        if (c == 0x0009 || c == 0x000A || c == 0x000D) return false;
        if (c >= 0x0020 && c <= 0xD7FF) return false;
        if (c >= 0xE000 && c <= 0xFFFD) return false;
        if (c >= 0x10000 && c <= 0x10FFFF) return false; // can't be java char, but any way...
        return true;
    }


    private final AttributesImpl emptyAttr = new AttributesImpl();

    /**
     * Elements and attributes names are defined here
     */
    private static class Scheme {

        // ELEMENTS
        private static final String REPORT = "Report";
        private static final String SUMMARY = "Summary";
        private static final String WDS = "WorkDirectories";
        private static final String WD = "WorkDirectory";
        private static final String TRS = "TestResults";
        private static final String TR = "TestResult";
        private static final String INT = "Interview";
        private static final String Q = "Question";
        private static final String DESCR_DATA ="DescriptionData";
        private static final String KEY_WORDS = "Keywords";
        private static final String TEST_ENV = "TestEnvironment";
        private static final String RES_PROP = "ResultProperties";
        private static final String TANNOT_DATA = "TestAnnotation";
        private static final String SES = "Sections";
        private static final String SE = "Section";
        private static final String OU = "Output";
        private static final String PR = "Property";
        private static final String IT = "Item";
        private static final String ENV = "Environment";
        private static final String CONC = "Concurrency";
        private static final String TIMO = "TimeOut";
        private static final String PRIOS = "PriorStatusList";
        private static final String EXCL_LIST = "ExcludeList";
        private static final String STD_VALS = "StandardValues";
        private static final String TESTS = "Tests";
        private static final String ENTTREE = "EntireTestTree";
        private static final String QUEST = "Question";
        private static final String LIST_QUEST = "ListQuestion";
        private static final String CHOICE_QUEST = "ChoiceQuestion";
        private static final String CHOICE = "Choice";
        private static final String PROP_QUEST = "PropertiesQuestion";
        private static final String GROUP = "Group";
        private static final String ROW = "Row";
        private static final String TEMPLATE = "Template";

        // ATTRS
        private static final String TR_URL = "url";
        private static final String TR_STATUS = "status";
        private static final String TR_WDID = "workDirID";
        private static final String PR_NAME = "name";
        private static final String PR_VAL = "value";
        private static final String IT_VAL = "value";
        private static final String SE_TIT = "title";
        private static final String SE_ST = "status";
        private static final String OU_TIT = "title";
        private static final String RES_PROP_TIM = "endTime";
        private static final String WD_ID = "id";
        private static final String WD_JTI = "jti";
        private static final String KEYWORDS_EXPR = "expression";
        private static final String REPORT_FORMST = "formatVersion";
        private static final String REPORT_GENTIME = "generatedTime";
        private static final String XSI = "xmlns:xsi";
        private static final String SCH_LOC = "xsi:noNamespaceSchemaLocation";
        private static final String QUEST_VALUE = "value";
        private static final String QUEST_TEXT = "text";
        private static final String QUEST_SUMM = "summary";
        private static final String CHOICE_CH = "choice";
        private static final String CHOICE_DCH = "displayChoice";
        private static final String CHOICE_VAL = "value";
        private static final String GROUP_NAME = "name";
        private static final String GROUP_HD1 = "header1";
        private static final String GROUP_HD2 = "header2";
        private static final String ROW_KEY = "key";
        private static final String ROW_VAL = "value";
        private static final String ENV_NAME = "name";
        private static final String ENV_DESCR = "description";

        private static final String TEM_NAME = "name";
        private static final String TEM_DESCRIPTION = "description";
        private static final String TEM_FILE = "fileName";

        // VALUES
        private static final String XSI_VAL = "http://www.w3.org/2001/XMLSchema-instance";
        private static final String SCH_LOC_VAL = "Report.xsd";

    }

    public static final String XML_CHARSET = "UTF-8";

}
