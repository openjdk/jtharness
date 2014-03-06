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
package com.sun.javatest.cof;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;

import com.sun.javatest.util.XMLWriter;

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
 *         accept the pass/fail status of the test using E-mail.
 *
 *
 * <p>Java class for TestCase complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TestCase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="status" type="{http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema}Status" minOccurs="0"/>
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
 *
 *
 */
/*@XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "TestCase", propOrder = {
 "name",
 "status",
 "starttime",
 "endtime",
 "attributes",
 "description"
 })*/
public class COFTestCase extends COFItem {
        static long count = 0;

        static LinkedHashMap xmlAttributes;

        static LinkedHashMap xmlElements;

        static String xmlTagName;

        static {
                xmlTagName = "testcase";
                xmlElements = new LinkedHashMap();
                xmlElements.put("name", "name");
                xmlElements.put("status", "status");
                xmlElements.put("starttime", "starttime");
                xmlElements.put("endtime", "endtime");
                xmlElements.put("attributes", "attributes");
                xmlElements.put("description", "description");
                xmlAttributes = new LinkedHashMap();
                xmlAttributes.put("id", "id");
//              xmlAttributes.put("analysis", "analysis");
        }

        //    @XmlAttribute
        protected String analysis;

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
                return name + ":" + idNum;
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

        /* (non-Javadoc)
         * @see com.sun.javatest.cof.COFItem#write(com.sun.javatest.util.XMLWriter)
         */
        void write(XMLWriter out) throws IOException {
                out.newLine();
                super.write(out);
        }
}
