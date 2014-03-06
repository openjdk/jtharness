/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.exec;

import java.awt.Component;
import java.awt.Font;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.sun.javatest.TestFilter;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.tool.I18NUtils;
import com.sun.javatest.util.I18NResourceBundle;

class RenderingUtilities {
    static ListCellRenderer createTestListRenderer() {
        return new TestCellRenderer(i18n);
    }

    static ListCellRenderer createTRTNodeRenderer() {
        return new TestCellRenderer(i18n);
    }

    static ListCellRenderer createFilterListRenderer() {
        return new FilterCellRenderer(i18n);
    }

    private static I18NResourceBundle i18n =
        I18NResourceBundle.getBundleForClass(RenderingUtilities.class);
    private static TestCellRenderer tlRend;
    private static FilterCellRenderer flRend;

    // --------- Inner classes -------

    /**
     * Render a list of tests (TestResult objects).
     */
    static class TestCellRenderer extends JLabel implements ListCellRenderer {
         public TestCellRenderer(I18NResourceBundle i18n) {
             setOpaque(false);
             this.i18n = i18n;
         }

        public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null)  // very strange...
                return this;

            if (value instanceof TestResult) {
                TestResult tr = (TestResult)value;
                setText(tr.getTestName());
                setToolTipText(I18NUtils.getStatusMessage(tr.getStatus()));
                setBasicAttribs(isSelected, list);
            }
            else if (value instanceof TestResultTable.TreeNode) {
                TestResultTable.TreeNode tn = (TestResultTable.TreeNode)value;
                if (tn.getName() != null)
                    setText(TestResultTable.getRootRelativePath(tn));
                else
                    setText(i18n.getString("rendUtil.rootName"));
                //setToolTipText(I18NUtils.getStatusMessage(tr.getStatus()));
                setBasicAttribs(isSelected, list);
            }
            else {          // this code really should never run
                setText(value.toString());
                if (isSelected) {
                    setOpaque(true);
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                }
                else {
                    setForeground(list.getForeground());
                    setOpaque(false);
                }
            }

            setFont(getFont().deriveFont(Font.PLAIN));

            return this;
        }

        private void setBasicAttribs(boolean isSelected, JList list) {
            // Hopefully safe to share...will help with saving space
            // This border is to provide space between the text and the
            // side of the widget, helping readability.
            setBorder(spacerBorder);

            if (isSelected) {
                setOpaque(true);
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else {
                setForeground(list.getForeground());
                setOpaque(false);
            }
        }

        private I18NResourceBundle i18n;
        // border to pad left and right
        private Border spacerBorder = BorderFactory.createEmptyBorder(0,3,0,3);
    }

    /**
     * Render a list of test filters with their descriptive name.
     * @see com.sun.javatest.TestFilter#getName()
     */
    static class FilterCellRenderer extends JLabel implements ListCellRenderer {
        public FilterCellRenderer(I18NResourceBundle i18n) {
            setOpaque(false);
             this.i18n = i18n;
        }

        public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {

            String name = null;

            TestFilter filter = (TestFilter)value;
            name = filter.getName();

            //setToolTipText(filter.getDescription());
            if (name != null && name.length() > 0)
                setText(name);
            else
                setText(i18n.getString("rendUtil.noFilterName"));

            setColors(isSelected, list);
            setFont(false);

            return this;
        }

        private void setColors(boolean isSelected, JList list) {
            if (isSelected) {
                setOpaque(true);
                setForeground(list.getSelectionForeground());
                setBackground(list.getSelectionBackground());
            }
            else {
                //setForeground(MetalLookAndFeel.getPrimaryControlDarkShadow());
                setForeground(list.getForeground());
                setOpaque(false);
            }
        }

        private void setFont(boolean isActive) {
            if (isActive)
                setFont(getFont().deriveFont(Font.BOLD));
            else
                setFont(getFont().deriveFont(Font.PLAIN));
        }

        private I18NResourceBundle i18n;
    }
    // end inner classes
}
