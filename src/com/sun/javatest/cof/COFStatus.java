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

import java.util.LinkedHashMap;

/*import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;*/


/**
 *
 *         Actual elements contain text that refers to an actual value.
 *         This can be anything. A header, value, body, ..... If the tag
 *         log appears, it may be understood that a URL of a log file will
 *         appear as the PCDATA for this tag. If present, that URL uses
 *         either http or file protocol. If http protocol, the server name
 *         will be fully qualified. If file protocol, the full path
 *         beginning with /net will be expected. Microsoft file mapping or
 *         Unix automounts should not be used.
 * <p>Java class for Status complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Status">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="expected" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="actual" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="value" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="pass"/>
 *             &lt;enumeration value="fail"/>
 *             &lt;enumeration value="ambiguous"/>
 *             &lt;enumeration value="error"/>
 *             &lt;enumeration value="vm_fail"/>
 *             &lt;enumeration value="did_not_run"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
/*@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Status", propOrder = {
    "expected",
    "actual"
})*/
public class COFStatus extends COFItem{

static LinkedHashMap xmlAttributes;
static LinkedHashMap xmlElements;
static String xmlTagName;

    static {
                xmlElements = new LinkedHashMap();
                xmlAttributes = new LinkedHashMap();
                xmlElements.put("expected","expected");
                xmlElements.put("actual","actual");
                xmlAttributes.put("value","value");
                xmlTagName = "status";
        }

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
    protected String actual;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
    protected String expected;

        //    @XmlAttribute(required = true)
    protected String value;

        /**
     * Gets the value of the actual property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getActual() {
        return actual;
    }

        /**
     * Gets the value of the expected property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getExpected() {
        return expected;
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
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the actual property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setActual(String value) {
        this.actual = value;
    }

    /**
     * Sets the value of the expected property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setExpected(String value) {
        this.expected = value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setValue(String value) {
        this.value = value;
    }

}
