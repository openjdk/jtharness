/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.audit;

import java.awt.Component;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;
import com.sun.javatest.tool.UIFactory;

abstract class ListPane extends AuditPane {
    ListPane(String uiKey, UIFactory uif) {
        super(uiKey, uif);

        model = new ListModel();
        list = uif.createList(uiKey + ".lst", model);
        list.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        list.setCellRenderer(new Renderer());

        JScrollPane sp = uif.createScrollPane(list,
                                         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setBody(sp);
    }

    void setData(Object[] data) {
        if (data == null || data.length == 0)
            show(uif.getI18NString("list.noEntries"));
        else {
            model.setData(data);
            showBody();
        }
    }

    private class ListModel extends AbstractListModel {
        public Object getElementAt(int index) {
            return data[index];
        }

        public int getSize() {
            return (data == null ? 0 : data.length);
        }

        void setData(Object[] data) {
            this.data = data;
            fireContentsChanged(this, 0, data.length - 1);
        }

        private Object[] data;
    }

    private class Renderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object o, int index, boolean isSelected, boolean cellHasFocus) {
            String name;
            if (o instanceof TestResult) {
                TestResult tr = (TestResult) o;
                name = tr.getTestName();
            }
            else if (o instanceof TestDescription) {
                TestDescription td = (TestDescription) o;
                name = td.getRootRelativeURL();
            }
            else
                name = String.valueOf(o);
            return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
        }
    }

    protected JList list;
    private ListModel model;
}
