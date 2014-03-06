/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class Report extends COFItem {
        static final String COF_SCHEMA = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema";

        static final String COF_SCHEMA_LOCATION = "http://hestia.sfbay.sun.com/projects/COF/2003/2_0_2/Schema/COF2_0_2.xsd";

        static final String VERSION = "2.0.2";

        static final LinkedHashMap xmlAttributes;

        static final LinkedHashMap xmlElements;

        static String xmlTagName;

        private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

        static {
                xmlElements = new LinkedHashMap();
                xmlAttributes = new LinkedHashMap();
                xmlElements.put("date", "date");
                xmlElements.put("version", "version");
                xmlElements.put("environments", "environments");
                xmlElements.put("swentities", "swentities");
                xmlElements.put("applications", "applications");
                xmlElements.put("operator", "operator");
                xmlElements.put("harness", "harness");
                xmlElements.put("testsuites", "testsuites");
                xmlElements.put("annotations", "annotations");
                xmlAttributes.put("xsiNS", "xmlns:xsi");
                xmlAttributes.put("cofNS", "xmlns:cof");
                xmlAttributes.put("targetNS", "xmlns");
                xmlAttributes.put("schemaLocation", "xsi:schemaLocation");
                xmlTagName = "report";

        }

        //    @XmlAttribute
        protected String analysis;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
        protected COFReportAnnotations annotations;

        //    @XmlAnyElement
        protected List/*<Element>*/any;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
        protected COFApplications applications;

        //  @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true, type = String.class)
        protected Date date;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
        protected COFEnvironments environments;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
        protected String harness;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
        protected String operator;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
        protected COFSWEntities swentities;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
        protected COFTestSuites testsuites;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
        protected String version;

        Report() {
                setDate(new Date());
                setVersion(VERSION);
                setAnnotations(new COFReportAnnotations());
        }

        Report(COFEnvironment env, COFTestSuite ts) {
                this();
                getEnvironments().getEnvironment().add(env);
                getTestsuites().getTestsuite().add(ts);
        }

        Report(COFEnvironment[] envs, COFTestSuite ts) {
                this();
        for (int i = 0; i < envs.length; i++) {
            getEnvironments().getEnvironment().add(envs[i]);
        }
                getTestsuites().getTestsuite().add(ts);
        }

        void addEnvironment(COFEnvironment env) {
                getEnvironments().getEnvironment().add(env);
        }

        void addTestSuite(COFTestSuite ts) {
                getTestsuites().getTestsuite().add(ts);
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
         * Gets the value of the annotations property.
         *
         * @return
         *     possible object is
         *     {@link COFReportAnnotations }
         *
         */
        public COFReportAnnotations getAnnotations() {
                return annotations;
        }

        /**
         * Gets the value of the any property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the any property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAny().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Element }
         *
         *
         */
        public List/*<Element>*/getAny() {
                if (any == null) {
                        any = new ArrayList/*<Element>*/();
                }
                return this.any;
        }

        /**
         * Gets the value of the applications property.
         */
        public COFApplications getApplications() {
                return applications;
        }

        /**
         * @return Returns the cofNS.
         */
        public String getCofNS() {
                return COF_SCHEMA;
        }

        /**
         * Gets the value of the date property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public Date getDate() {
                return date;
        }

        /**
         * Gets the value of the environments property.
         *
         * @return
         *     possible object is
         *     {@link Environments }
         *
         */
        public COFEnvironments getEnvironments() {
                if (environments == null)
                        environments = new COFEnvironments();
                return environments;
        }

        /**
         * Gets the value of the harness property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getHarness() {
                return harness;
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
         * Gets the value of the operator property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getOperator() {
                return operator;
        }

        /**
         * @return Returns the schemaLocation.
         */
        public String getSchemaLocation() {
                return COF_SCHEMA + " " + COF_SCHEMA_LOCATION;
        }

        /**
         * Gets the value of the swentities property.
         *
         * @return
         *     possible object is
         *     {@link COFSWEntities }
         *
         */
        public COFSWEntities getSwentities() {
                return swentities;
        }

        /**
         * @return Returns the targetNS.
         */
        public String getTargetNS() {
                return COF_SCHEMA;
        }

        /**
         * Gets the value of the testsuites property.
         *
         * @return
         *     possible object is
         *     {@link TestSuites }
         *
         */
        public COFTestSuites getTestsuites() {
                if (testsuites == null)
                        testsuites = new COFTestSuites();
                return testsuites;
        }

        /**
         * Gets the value of the version property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getVersion() {
                return version;
        }

        /**
         * @return Returns the xsiNS.
         */
        public String getXsiNS() {
                return XSI_NS;
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
         * Sets the value of the annotations property.
         *
         * @param value
         *     allowed object is
         *     {@link COFReportAnnotations }
         *
         */
        public void setAnnotations(COFReportAnnotations value) {
                this.annotations = value;
        }

        /**
         * Sets the value of the date property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setDate(Date value) {
                this.date = value;
        }

        /**
         * Sets the value of the environments property.
         *
         * @param value
         *     allowed object is
         *     {@link Environments }
         *
         */
        public void setEnvironments(COFEnvironments value) {
                this.environments = value;
        }

        /**
         * Sets the value of the harness property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setHarness(String value) {
                this.harness = value;
        }

        /**
         * Sets the value of the operator property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setOperator(String value) {
                this.operator = value;
        }

        /**
         * Sets the value of the swentities property.
         *
         * @param value
         *     allowed object is
         *     {@link COFSWEntities }
         *
         */
        public void setSwentities(COFSWEntities value) {
                this.swentities = value;
        }

        public void setApplications(COFApplications value) {
                this.applications = value;
        }
        /**
         * Sets the value of the testsuites property.
         *
         * @param value
         *     allowed object is
         *     {@link TestSuites }
         *
         */
        public void setTestsuites(COFTestSuites value) {
                this.testsuites = value;
        }

        /**
         * Sets the value of the version property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setVersion(String value) {
                this.version = value;
        }
}
