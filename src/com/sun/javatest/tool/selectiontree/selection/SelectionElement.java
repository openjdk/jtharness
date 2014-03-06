/*
 * $Id$
 *
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool.selectiontree.selection;

import java.util.List;

/**
 * Selection element interface represents an object which can be selected in UI.
 */
public interface SelectionElement {

    /**
     * Answers whether the interface implementor is selected, unselected or have partially selected children.
     */
    SelectionType getSelectionType();

    /**
     * Sets whether the interface implementor is selected, unselected or have partially selected children.
     */
    void setSelectionType(SelectionType selectionType);

    /**
     * Answers the UI displayable element name.
     */
    String getDisplayableName();

    /**
     * Answers tooltip text for this element.
     * If the return ed value is null, tooltip will not be displayed in UI.
     */
    String getToolTip();

    /**
     * Answers whether the tooltip should be always shown regardless the displayable name visibility.
     */
    boolean isToolTipAlwaysShown();

    /**
     * Answer the list with children.
     */
    List<SelectionElement> getChildren();
}
