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
package com.sun.javatest.cof;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.javatest.Status;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResult.ReloadFault;
import com.sun.javatest.TestResult.ResultFileNotFoundFault;
import java.util.Iterator;

/*import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 */
/**
 *
 *         The analysis setting is meant to allow remote groups to waive or
 *         accept the pass/fail status of the test using E-mail. For
 *         testcases, the analysis setting provides a default value to
 *         apply to the analysis the setting for all testcase results for
 *         this test.
 *
 *
 * <p>Java class for Test complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Test">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="appuse" type="{http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema}IDWithColon" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="status" type="{http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema}Status" minOccurs="0"/>
 *         &lt;element name="testcases" type="{http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema}TestCases" minOccurs="0"/>
 *         &lt;element name="starttime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="endtime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="attributes" type="{http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema}TestAttributes" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="analysis" default="accept">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="accept"/>
 *             &lt;enumeration value="waive"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="id" use="required" type="{http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema}IDWithColon" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
/*@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Test", propOrder = {
"name",
"appuse",
"status",
"testcases",
"starttime",
"endtime",
"attributes",
"description"
})*/
public class COFTest extends COFItem {

    private static final Date badDate = new Date(0);
    private static final String[] cofStatus = new String[Status.NUM_STATES];
    static long count = 0;
    private static DateFormat[] dateFormats;
    protected final static Pattern idPattern = Pattern.compile(".*[^\\w\\.\\[\\]\\(\\)\\{\\},_\\-]([\\w\\.\\[\\]\\(\\)\\{\\},_\\-]+)");
    static boolean noTestCases = false;
    protected final static Pattern testCasePattern = Pattern //.compile("^(\\S+): (Passed\\.|Failed\\.|Error\\.|Not\\ run\\.)(.*)");
            .compile("^(.*): (Passed\\.|Failed\\.|Error\\.|Not\\ run\\.)(.*)");
    static LinkedHashMap xmlAttributes;
    static LinkedHashMap xmlElements;
    static String xmlTagName;

    static {
        xmlElements = new LinkedHashMap();
        xmlAttributes = new LinkedHashMap();
        xmlElements.put("name", "name");
        xmlElements.put("appuse", "appuse");
        xmlElements.put("status", "status");
        xmlElements.put("testcases", "testcases");
        xmlElements.put("starttime", "starttime");
        xmlElements.put("endtime", "endtime");
        xmlElements.put("attributes", "attributes");
        xmlElements.put("description", "description");
        //              xmlAttributes.put("analysis", "analysis");
        xmlAttributes.put("id", "id");
        xmlTagName = "test";

        initDateFormats();
    }

    static {
        cofStatus[Status.PASSED] = "pass";
        cofStatus[Status.FAILED] = "fail";
        cofStatus[Status.ERROR] = "error";
        cofStatus[Status.NOT_RUN] = "did_not_run";
    }
    //    @XmlAttribute
    protected String analysis;
    //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
    protected List/*<String>*/ appuse;
    //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
    protected COFTestAttributes attributes;
    //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
    protected String description;
    //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", type = String.class)
    //    @XmlJavaTypeAdapter(Adapter1 .class)
    protected Date endtime;
    //    @XmlAttribute(required = true)
    protected String id;
    final long idNum = count++;
    //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
    protected String name;
    //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", type = String.class)
    //    @XmlJavaTypeAdapter(Adapter1 .class)
    protected Date starttime;
    //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
    protected COFStatus status;
    //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
    protected COFTestCases testcases;
    private COFData cofData;

    COFTest(TestResult tr, COFData data) {
        cofData = data;
        status = new COFStatus();
        status.setValue(cofStatus[tr.getStatus().getType()]);
        status.setActual(tr.getStatus().getReason());
        fillTestCases(tr);
        //if (tr.getStatus().isError() || tr.getStatus().isFailed()) {
            checkLostTestCases(tr);
        //}
        setName(tr.getTestName());
        if (!tr.getStatus().isNotRun()) {
            setStarttime(parseDate(tr, TestResult.START));
            setEndtime(parseDate(tr, TestResult.END));
        }
        attributes = new COFTestAttributes();
        attributes.getAttribute().add(
                new COFTestAttribute("logfile", tr.getWorkRelativePath()));
        if (data.isInterviewParametersAvailable()) {
            try {
                if (!data.getInterviewParameters().getExcludeListFilter().accepts(tr.getDescription())) {
                    attributes.getAttribute().add(new COFTestAttribute("isOnExcludeList", "true"));
                }
            } catch(Exception e) {
            }
        }
        String jo = cofData.get("jvmopt", null);
        if (jo != null) {
            attributes.getAttribute().add(new COFTestAttribute("javaopt", jo));
        }

    }

    protected void fillTestCases(TestResult tr) {
        if (noTestCases) {
            return;
        }
        testcases = new COFTestCases();
        int sCount = tr.getSectionCount();
        if (sCount == 0 && tr.getStatus().getType() != Status.NOT_RUN) {
            try {
                tr = new TestResult(new File(cofData.get("workdir") + File.separator + tr.getWorkRelativePath()));
                sCount = tr.getSectionCount();
            } catch (ResultFileNotFoundFault e) {
                // warning is out from somewhere else, but again
                System.err.println(e.getMessage());
            } catch (ReloadFault e) {
                System.err.println(tr.getFile());
            }
        }
        for (int i = 0; i < sCount; i++) {
            try {
                String sectionOut = tr.getSection(i).getOutput("out1");
                if (sectionOut == null) {
                    continue;
                }
                BufferedReader reader = new BufferedReader(new StringReader(
                        sectionOut));
                String s = reader.readLine();
                while (s != null) {
                    Matcher m = testCasePattern.matcher(s);
                    if (m.matches()) {
                        COFTestCase tc = new COFTestCase();
                        COFStatus cStatus = new COFStatus();
                        String tcName = fixIncorrectTestCaseName(m.group(1));
                        if (tcName == null) {
                            s = reader.readLine();
                            continue;
                        }
                        tc.setName(tcName);
                        cStatus.setValue(cofStatus[Status.parse(m.group(2)).getType()]);
                        cStatus.setActual(m.group(3));
                        tc.setStatus(cStatus);
                        testcases.getTestcase().add(tc);
                    }
                    s = reader.readLine();
                }
            } catch (ReloadFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void checkLostTestCases(TestResult tr) {
        MTL mtl = cofData.getMtl();

        if (mtl == null) {
            // mtl is not specified
            return;
        }

        List  /*<COFTestCase>*/ jtrCases = testcases.getTestcase();
        List  /*<String>*/ mtlTestCases = mtl.getTestCases(tr.getTestName());
        if (mtlTestCases != null && mtlTestCases.size() > 0) {
            // for (COFTestCase jtrCase : jtrCases) {
            Iterator it = jtrCases.iterator();
            while (it.hasNext()) {
                COFTestCase jtrCase = (COFTestCase) it.next();
                mtlTestCases.remove(jtrCase.name);
            }
            if (mtlTestCases.size() > 0) {
                //for (String testCaseName : mtlTestCases) {
                it = mtlTestCases.iterator();
                while(it.hasNext()) {
                    String testCaseName = (String) it.next();
                    COFTestCase nTc = new COFTestCase();
                    COFStatus nStatus = new COFStatus();
                    nTc.setName(testCaseName);
                    String strS = cofStatus[tr.getStatus().getType()];
                    nStatus.setValue(strS);
                    //System.out.println("+ " + tr.getTestName() + " " + tcName + " " + strS );
                    testcases.getTestcase().add(testCaseName);
                }
            }
        }
    }

    /**
     * Gets the value of the analysis property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAnalysis() {
        if (analysis == null) {
            return "accept";
        } else {
            return analysis;
        }
    }

    /**
     * Gets the value of the appuse property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the appuse property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAppuse().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List/*<String>*/ getAppuse() {
        if (appuse == null) {
            appuse = new ArrayList/*<String>*/();
        }
        return this.appuse;
    }

    /**
     * Gets the value of the attributes property.
     *
     * @return
     *     possible object is
     *     {@link COFTestAttributes }
     *
     */
    public COFTestAttributes getAttributes() {
        return attributes;
    }

    /**
     * Gets the value of the description property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the value of the endtime property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public Date getEndtime() {
        return endtime;
    }

    /**
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getId() {
        if (id != null) {
            return id;
        }
        if (name == null) {
            throw new IllegalStateException("Name was not set.");
        }
        Matcher m = idPattern.matcher(name);
        if (m.matches()) {
            id = m.group(1) + ":" + idNum;
        } else {
            id = "test:" + idNum;
        }
        return id;
    }

    LinkedHashMap getItemAttributes() {
        return xmlAttributes;
    }

    LinkedHashMap getItemElements() {
        return xmlElements;
    }

    String getItemTagName() {
        return xmlTagName;
    }

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the starttime property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public Date getStarttime() {
        return starttime;
    }

    /**
     * Gets the value of the status property.
     *
     * @return
     *     possible object is
     *     {@link COFStatus }
     *
     */
    public COFStatus getStatus() {
        return status;
    }

    /**
     * Gets the value of the testcases property.
     *
     * @return
     *     possible object is
     *     {@link COFTestCases }
     *
     */
    public COFTestCases getTestcases() {
        return testcases;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    //  public void setId(String value) {
    //          this.id = value;
    //  }
    private static void initDateFormats() {
        Vector v = new Vector();

        // generic Java default
        // 10-Sep-99 3:25:11 PM
        v.addElement(DateFormat.getDateTimeInstance());

        // standard IETF date syntax
        // Fri, 10 September 1999 03:25:12 PDT
        v.addElement(new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss zzz"));

        // Unix C time
        // Fri Sep 10 14:41:37 PDT 1999
        v.addElement(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"));

        // XML time
        // 1999-09-10T03:25:12.123 (ISO 8601, sect 5.4)
        v.addElement(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS"));

        // allow user-specified format
        String s = System.getProperty("javatest.date.format");
        if (s != null) {
            v.addElement(new SimpleDateFormat(s));
        }

        dateFormats = new DateFormat[v.size()];
        v.copyInto(dateFormats);
    }

    private static Date parseDate(String s) {
        for (int i = 0; i < dateFormats.length; i++) {
            try {
                Date d = dateFormats[i].parse(s);
                // successfully parsed the date; shuffle the format to the front
                // to speed up future parses, assuming dates will likely be similar
                if (i > 0) {
                    DateFormat tmp = dateFormats[i];
                    System.arraycopy(dateFormats, 0, dateFormats, 1, i);
                    dateFormats[0] = tmp;
                }
                return d;
            } catch (ParseException e) {
                //System.err.println("pattern: " + ((SimpleDateFormat)dateFormats[i]).toPattern());
                //System.err.println("  value: " + s);
                //System.err.println("example: " + dateFormats[i].format(new Date()));
            }
        }

        return badDate;
    }

    private static Date parseDate(TestResult tr, String key) {
        try {
            String s = tr.getProperty(key);
            if (s != null && s.length() > 0) {
                return parseDate(s);
            }
        } catch (TestResult.Fault e) {
            System.err.println(e);
        }
        // default, if no entry in test result, or if error reloading test result
        return badDate;
    }

    /**
     * Sets the value of the analysis property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAnalysis(String value) {
        this.analysis = value;
    }

    /**
     * Sets the value of the attributes property.
     *
     * @param value
     *     allowed object is
     *     {@link COFTestAttributes }
     *
     */
    public void setAttributes(COFTestAttributes value) {
        this.attributes = value;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Sets the value of the endtime property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEndtime(Date value) {
        this.endtime = value;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Sets the value of the starttime property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStarttime(Date value) {
        this.starttime = value;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     *     {@link COFStatus }
     *
     */
    public void setStatus(COFStatus value) {
        this.status = value;
    }

    /**
     * Sets the value of the testcases property.
     *
     * @param value
     *     allowed object is
     *     {@link COFTestCases }
     *
     */
    public void setTestcases(COFTestCases value) {
        this.testcases = value;
    }

    private String fixIncorrectTestCaseName(String tcn) {
        return cofData.getCustomFilter().transformTestCaseName(tcn);
    }
}
