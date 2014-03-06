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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.sun.javatest.util.XMLWriter;
/*import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
*/

/**
 * <p>Java class for TestCases complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TestCases">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="testcase" type="{http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema}TestCase" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
/*@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestCases", propOrder = {
    "testcase"
})*/
public class COFTestCases extends COFItem{

        static LinkedHashMap xmlAttributes;

        static LinkedHashMap xmlElements;

        static String xmlTagName;

        static {
                xmlTagName = "testcases";
                xmlElements = new LinkedHashMap();
                xmlElements.put("testcase", "testcase");
        }

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
    protected List/*<COFTestCase>*/ testcase;

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
     * Gets the value of the testcase property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the testcase property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTestcase().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link COFTestCase }
     *
     *
     */
    public List/*<COFTestCase>*/ getTestcase() {
        if (testcase == null) {
            testcase = new ArrayList/*<COFTestCase>*/();
        }
        return this.testcase;
    }

        /* (non-Javadoc)
         * @see com.sun.javatest.cof.COFItem#write(com.sun.javatest.util.XMLWriter)
         */
        void write(XMLWriter out) throws IOException {
                if (getTestcase().size() != 0) {
                        out.newLine();
                        super.write(out);
                }
        }

}
