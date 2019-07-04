/*
 * $Id$
 *
 * Copyright (c) 2002, 2018, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.util.XMLWriter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

abstract class COFItem {
    BeanInfo bi = null;
    LinkedHashMap<String, String> itemAttributes = getItemAttributes();
    LinkedHashMap<String, String> itemElements = getItemElements();
    String itemTagName = getItemTagName();
    PropertyDescriptor[] pds = null;

    private String[] getAttributeProperties() {
        return itemAttributes == null ? null : itemAttributes.keySet().toArray(new String[itemAttributes.size()]);
    }

    LinkedHashMap<String, String> getItemAttributes() {
        return null;
    }

    LinkedHashMap<String, String> getItemElements() {
        return null;
    }

    ;

    String getItemTagName() {
        return null;
    }

    Object getProperty(String name) {
        if (bi == null) {
            try {
                bi = Introspector.getBeanInfo(this.getClass());
                pds = bi.getPropertyDescriptors();
                Arrays.sort(pds, new Comparator<Object>() {
                    public int compare(Object o1, Object o2) {
                        PropertyDescriptor pd1 = (PropertyDescriptor) o1;
                        PropertyDescriptor pd2 = (PropertyDescriptor) o2;
                        return pd1.getName().compareTo(pd2.getName());
                    }
                });
            } catch (IntrospectionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (pds == null) {
            return null;
        }

        Object result = null;
        int propIndex = Arrays.binarySearch(pds, name, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                PropertyDescriptor pd = (PropertyDescriptor) o1;
                String propName = (String) o2;
                return pd.getName().compareTo(propName);
            }

        });

        if (propIndex < 0 || propIndex >= pds.length) { // no such property
//            System.out.println("passing "+ name);
            return null;
        }

        try {

            Method readMethod = pds[propIndex].getReadMethod();
            if (readMethod == null) {
                System.err.println("Error occured for property '" + name + "' - no read method defined. Please contact developers.");
                return null;
            }
            /* null casted to Object[] for suppresing "non-varargs call" warning */
            result = readMethod.invoke(this, (Object[]) null);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    private String[] getPropOrder() {
        return itemElements == null ? null : itemElements.keySet().toArray(new String[itemElements.size()]);
    }

    void write(XMLWriter out) throws IOException {

        if (itemTagName == null) {
            return;
        }
        out.startTag(itemTagName);
        if (itemAttributes != null) {
            String[] attrOrder = getAttributeProperties();
            for (String s : attrOrder) {
                out.writeAttr(itemAttributes.get(s).toString(),
                        (String) getProperty(s));
            }
        }
        if (itemElements != null) {
            String[] propOrder = getPropOrder();
            for (String s : propOrder) {
                Object propValue = getProperty(s);
                if (propValue instanceof COFItem) {
                    COFItem item = (COFItem) propValue;
                    item.write(out);
                    continue;
                } else if (propValue instanceof Collection) {
                    writeCollection(out, s);
                    continue;
                }
                if (propValue == null) {
                    continue;
                }
                out.startTag(itemElements.get(s).toString());
                write(out, propValue);
                out.endTag(itemElements.get(s).toString());
            }
        }
        out.endTag(itemTagName);
    }

    protected void write(XMLWriter out, Object o) throws IOException {
        if (o instanceof String) {
            out.write((String) o);
        } else if (o instanceof Date) {
            out.writeDate((Date) o);
        } else {
            out.write(o.toString());
        }
    }

    void writeCollection(XMLWriter out, String propName) throws IOException {
        Collection<?> col = (Collection<?>) getProperty(propName);
        for (Object value : col) {
            if (value instanceof COFItem) {
                ((COFItem) value).write(out);
            } else {
                out.startTag(itemElements.get(propName).toString());
                write(out, value);
                out.endTag(itemElements.get(propName).toString());
            }
        }

    }

}
