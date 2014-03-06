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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
/*import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
*/

/**
 *
 *         In general, a Java command line flag argument that it is agreed
 *         will get tracked as an attribute, say -foo,will get tracked by a
 *         test attribute
 *
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;name xmlns:cof="http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema" xmlns:jxb="http://java.sun.com/xml/ns/jaxb" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;foo&lt;/name&gt;
 * </pre>
 *
 *         without a corresponding value element. A Java command line
 *         argument pair "-foo bar" will get tracked by a test attribute
 *
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;name xmlns:cof="http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema" xmlns:jxb="http://java.sun.com/xml/ns/jaxb" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;foo&lt;/name&gt;
 * </pre>
 *
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;value xmlns:cof="http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema" xmlns:jxb="http://java.sun.com/xml/ns/jaxb" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;bar&lt;/value&gt;
 * </pre>
 *
 *         .
 *
 *
 * <p>Java class for TestAttribute complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TestAttribute">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
/*@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestAttribute", propOrder = {
    "name",
    "value"
})*/
public class COFTestAttribute extends COFItem{

static LinkedHashMap xmlAttributes;
static LinkedHashMap xmlElements;

    static String xmlTagName;

    static {
                xmlElements = new LinkedHashMap();
                xmlElements.put("name","name");
                xmlElements.put("value","value");
                xmlTagName = "attribute";
        }

    //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
    protected List/*<String>*/ name;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
    protected List/*<String>*/ value;

        public COFTestAttribute(String name, String value) {
                this.getName().add(name);
                this.getValue().add(value);
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
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the name property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getName().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List/*<String>*/ getName() {
        if (name == null) {
            name = new ArrayList/*<String>*/();
        }
        return this.name;
    }

        /**
     * Gets the value of the value property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the value property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValue().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List/*<String>*/ getValue() {
        if (value == null) {
            value = new ArrayList/*<String>*/();
        }
        return this.value;
    }

}
