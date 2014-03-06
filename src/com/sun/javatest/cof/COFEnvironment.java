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

import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.sun.javatest.util.I18NResourceBundle;

/* temp */public/* temp */class COFEnvironment extends COFItem {
        static String[] propOrder = { "machine", "os", "jdk", "systemLocale",
                        "userLocale", "encoding", "timezone", "bits", "displaydepth",
                        "description", "sw" };

        static String[] propTags = { "machine", "os", "jdk", "system-locale",
                        "user-locale", "encoding", "timezone", "bits", "displaydepth",
                        "description", "sw" };

        static LinkedHashMap xmlAttributes;

        static LinkedHashMap xmlElements;

        static String xmlTagName;
        private static I18NResourceBundle i18n = I18NResourceBundle
        .getBundleForClass(Main.class);

        static {
                xmlElements = new LinkedHashMap();
                for (int i = 0; i < propOrder.length; i++) {
                        xmlElements.put(propOrder[i], propTags[i]);
                }
                xmlAttributes = new LinkedHashMap();
                xmlAttributes.put("id", "id");
                xmlTagName = "environment";
        }

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
        protected Integer bits;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
        protected String description;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
        protected Integer displaydepth;

        private String domainName;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
        protected String encoding;

        private String hostName;

        //    @XmlAttribute(required = true)
        protected String id = "env:0";

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
        protected String jdk;

        //  @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
        protected String machine;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
        protected COFOS os;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema", required = true)
        protected List/*<SWEntity>*/sw;

        //    @XmlElement(name = "system-locale", namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
        protected String systemLocale;

        //    @XmlElement(namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
        protected String timezone;

        //    @XmlElement(name = "system-locale", namespace = "http://qare.sfbay.sun.com/projects/COF/2003/2_0_2/Schema")
        protected String userLocale;
    protected COFData data;

        COFEnvironment(COFData data) {
        this.data = data;

        initDefaultHostInfo();
                initDefaultOSInfo();

                hostName = data.get("environment.host", hostName);
                domainName = data.get("environment.domain", domainName);
                setMachine(data.get("environment.machine", hostName + "." + domainName));
                os.setName(data.get("environment.os.name", os.getName()));
                os.setVersion(data.get("environment.os.version", os.getVersion()));
                os.setArch(data.get("environment.os.arch", os.getArch()));
                setJdk(data.get("environment.jdk", jdk));
                setUserLocale(data.get("environment.user-locale", java.util.Locale.getDefault().toString()));
                setSystemLocale(data.get("environment.system-locale", data
                                .get("LOCALE")));
                setEncoding(data.get("environment.encoding", (new InputStreamReader(
                                System.in)).getEncoding()));
                setTimezone(data.get("environment.timezone", TimeZone.getDefault()
                                .getID()));
                setBits(data.get("environment.bits", null) == null ? null
                                : new Integer(data.get("environment.bits")));
                setDisplaydepth(data.get("environment.displaydepth", null) == null ? null
                                : new Integer(data.get("environment.displaydepth")));
                setDescription(data.get("environment.description"));
        }

    public COFEnvironment(COFData data, String id) {
        this(data);
        this.id = id;
    }

        /**
         * Gets the value of the bits property.
         *
         * @return
         *     possible object is
         *     {@link Integer }
         *
         */
        public Integer getBits() {
                return bits;
        }

        private String getCalderaLinuxVersionInfo() {
                String res = "N/A";

                RandomAccessFile raf = null;
                try {
                        raf = new RandomAccessFile("/etc/issue", "r");
                        String line;
                        while ((line = raf.readLine()) != null) {
                                StringTokenizer st = new StringTokenizer(line, " ");
                                if (st.countTokens() >= 2) {
                                        if (st.nextToken().toUpperCase().equals("VERSION")) {
                                                res = st.nextToken();
                                                break;
                                        }
                                }
                        }
                } catch (Exception e) {
                        // System.err.println("Something might be wrong with Caldera");
                        // cat.warn("The version number of Caldera cannot be retrieved");
                } finally {
                        if (raf != null) {
                                try {
                                        raf.close();
                                } catch (Exception e) {
                                }
                        }
                }

                return res;
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
         * Gets the value of the displaydepth property.
         *
         * @return
         *     possible object is
         *     {@link Integer }
         *
         */
        public Integer getDisplaydepth() {
                return displaydepth;
        }

        /**
         * Gets the value of the encoding property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getEncoding() {
                return encoding;
        }

        private String getGenericLinuxVersionInfo(String fileName) {
                String res = "N/A";

                RandomAccessFile raf = null;
                try {
                        raf = new RandomAccessFile(fileName, "r");
                        StringTokenizer st = new StringTokenizer(raf.readLine(), " ");
                        while (st.hasMoreElements()) {
                                String ele = st.nextToken();
                                if (Character.isDigit(ele.charAt(0))) {
                                        res = ele;
                                        break;
                                }
                        }
                } catch (Exception e) {
                        // System.err.println("Something might be wrong with Linux");
                } finally {
                        if (raf != null) {
                                try {
                                        raf.close();
                                } catch (Exception e) {
                                }
                        }
                }

                return res;
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
         * Gets the value of the jdk property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getJdk() {
                return jdk;
        }

        /**
         * Gets the value of the machine property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getMachine() {
                return machine;
        }

        /**
         * Gets the value of the os property.
         *
         * @return
         *     possible object is
         *     {@link OS }
         *
         */
        public COFOS getOs() {
                return os;
        }

        String[] getPropOrder() {
                return propOrder;
        }

        /**
         * Gets the value of the sw property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the sw property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSw().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SWEntity }
         *
         *
         */
        public List/*<SWEntity>*/getSw() {
                if (sw == null) {
                        sw = new ArrayList/*<SWEntity>*/();
                }
                return this.sw;
        }

        /**
         * Gets the value of the systemLocale property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getSystemLocale() {
                return systemLocale;
        }

        String getTagName() {
                return itemTagName;
        }

        String[] getTags() {
                return propTags;
        }

        /**
         * Gets the value of the timezone property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getTimezone() {
                return timezone;
        }

        /**
         * Gets the value of the userLocale property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getUserLocale() {
                return userLocale;
        }

        private void initDefaultHostInfo() {
                domainName = "unknown";

                try {
                        hostName = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                        hostName = "unknown";
                }
        }

        private void initDefaultOSInfo() {
                os = new COFOS();
                os.setName(System.getProperty("os.name"));
                os.setVersion(System.getProperty("os.version"));
                os.setArch(System.getProperty("os.arch"));

                // standardize results
                /*        if (osName.equals("sunos") || osName.equals("solaris")) {
                 osName = "solaris";
                 if (osVersion.equals("5.6"))
                 osVersion = "2.6";
                 else if (osVersion.equals("5.7"))
                 osVersion = "2.7";
                 else if (osVersion.equals("5.8"))
                 osVersion = "8";
                 else if (osVersion.equals("5.9"))
                 osVersion = "9";
                 } else if (osName.startsWith("windows")) {
                 osName = "windows";
                 if (osName.indexOf("95") != -1)
                 osVersion = "95";
                 else if (osName.indexOf("98") != -1)
                 osVersion = "98";
                 else if (osName.indexOf("2000") != -1)
                 osVersion = "2000";
                 else if (osName.indexOf("me") != -1)
                 osVersion = "me";
                 else if (osName.indexOf("nt") != -1)
                 osVersion = "nt_4.0";
                 else if (osName.indexOf("xp") != -1)
                 osVersion = "xp";
                 } else if (osName.equals("linux")) {
                 if ((new File("/etc/turbolinux-release")).exists()) {
                 osName = "turbo_linux";
                 osVersion = getGenericLinuxVersionInfo("/etc/turbolinux-release");
                 } else if ((new File("/etc/SuSE-release")).exists()) {
                 osName = "suse_linux";
                 osVersion = getGenericLinuxVersionInfo("/etc/SuSE-release");
                 } else if ((new File("/etc/mandrake-release")).exists()) {
                 osName = "mandrake_linux";
                 osVersion = getGenericLinuxVersionInfo("/etc/mandrake-release");
                 } else if ((new File("/etc/redhat-release")).exists()) {
                 osName = "redhat_linux";
                 osVersion = getGenericLinuxVersionInfo("/etc/redhat-release");
                 } else if ((new File("/etc/lst.cnf")).exists()) {
                 osName = "caldera_linux";
                 osVersion = getCalderaLinuxVersionInfo();
                 } else {
                 osName = "unknown_linux";
                 osVersion = "N/A";
                 }
                 }
                 */}

        /**
         * Sets the value of the bits property.
         *
         * @param value
         *     allowed object is
         *     {@link Integer }
         *
         */
        public void setBits(Integer value) {
                this.bits = value;
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
         * Sets the value of the displaydepth property.
         *
         * @param value
         *     allowed object is
         *     {@link Integer }
         *
         */
        public void setDisplaydepth(Integer value) {
                this.displaydepth = value;
        }

        /**
         * Sets the value of the encoding property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setEncoding(String value) {
                this.encoding = value;
        }

        /**
         * Sets the value of the id property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setId(String value) {
                this.id = value;
        }

        /**
         * Sets the value of the jdk property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setJdk(String value) {
                this.jdk = value;
        }

        /**
         * Sets the value of the machine property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setMachine(String value) {
                if (value.endsWith(".unknown"))
                try {
                                machine = InetAddress.getLocalHost().getCanonicalHostName();
                                if (machine.indexOf(".") == -1) {
                    if ("false".equals(data.get("showWarnings"))) {
                        System.err.println(i18n.getString("environment.badMachineName", machine));
                    } else {
                        String warningMessage = data.get("warning", "");
                        data.put("warning", warningMessage + i18n.getString("environment.badMachineName") + "\n");
                    }
                }
                                return;
                } catch (UnknownHostException e) {
            if ("false".equals(data.get("showWarnings"))) {
                System.err.println(i18n.getString("environment.cantGetLocalhostName", e.getMessage()));
                value = value.substring(0,  value.indexOf(".unknown")-1);
                System.err.println(i18n.getString("environment.badMachineName", value));
            } else {
                String warningMessage = data.get("warning", "");
                value = value.substring(0,  value.indexOf(".unknown")-1);
                data.put("warning", warningMessage + i18n.getString("environment.cantGetLocalhostName", e.getMessage()) + "\n" + i18n.getString("environment.badMachineName", value) + "\n");
            }
                }
                this.machine = value;
        }

        /**
         * Sets the value of the os property.
         *
         * @param value
         *     allowed object is
         *     {@link OS }
         *
         */
        public void setOs(COFOS value) {
                this.os = value;
        }

        /**
         * Sets the value of the systemLocale property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setSystemLocale(String value) {
                this.systemLocale = value;
        }

        /**
         * Sets the value of the timezone property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setTimezone(String value) {
                this.timezone = value;
        }

        /**
         * Sets the value of the userLocale property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setUserLocale(String value) {
                this.userLocale = value;
        }

}
