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
package com.sun.javatest.tool;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * A lightweight component that can display an icon from the {@link IconFactory}.
 * To use this component in a JEditorPane, use the following: <pre>
 *    &lt;object classid="com.sun.javatest.tool.IconLabel"&gt;
 *    &lt;param  name=... value=...&gt;
 *    ...
 *    &lt;/object&gt;
 * </pre>
 * The following parameters are recognized:
 * <dl>
 * <dt>type
 * <dd>one of "test", "testFolder", "testSection"
 * <dt>state
 * <dd>one of "passed", "failed", "error", "notRun"
 * </dl>
 * @see IconLabelBeanInfo
 */
public class IconLabel extends JLabel
{
    public float getAlignmentY() {
        return .7f;
    }

    public Icon getIcon() {
        if (type == null)
            return null;

        if (type.equals("file"))
            return IconFactory.getFileIcon();

        if (type.equals("folder"))
            return IconFactory.getFolderIcon();

        if (type.equals("up"))
            return IconFactory.getUpFolderIcon();

        if (state == null)
            return null;

        int s;
        if (state.equalsIgnoreCase("passed"))
            s = IconFactory.PASSED;
        else if (state.equalsIgnoreCase("failed"))
            s = IconFactory.FAILED;
        else if (state.equalsIgnoreCase("error"))
            s = IconFactory.ERROR;
        else if (state.equalsIgnoreCase("notRun"))
            s = IconFactory.NOT_RUN;
        else
            s = 0; // should have unknown icon?

        if (type.equalsIgnoreCase("test"))
            return IconFactory.getTestIcon(s, false, true);
        else if (type.equalsIgnoreCase("testFolder"))
            return IconFactory.getTestFolderIcon(s, false, true);
        else if (type.equalsIgnoreCase("testSection"))
            return IconFactory.getTestSectionIcon(s);
        else
            return null;
    }

    /**
     * Get the type of icon to be displayed.
     * @return either "test" or "folder"
     * @see #setType
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type of icon to be displayed.
     * @param type one of "test" or "folder"
     * @see #getType
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the state for the icon to be displayed.
     * @return one of "passed", "failed", "error", "notRun"
     * @see #setState
     */
    public String getState() {
        return state;
    }

    /**
     * Set the state for the icon to be displayed.
     * @param state one of "passed", "failed", "error", "notRun"
     * @see #getState
     */
    public void setState(String state) {
        this.state = state;
    }

    private String type;
    private String state;
}
