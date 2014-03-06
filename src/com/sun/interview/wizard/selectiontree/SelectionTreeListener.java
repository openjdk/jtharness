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
package com.sun.interview.wizard.selectiontree;

import java.util.EventListener;

import com.sun.interview.wizard.selectiontree.selection.SelectionType;

/**
 * <code>SelectionTreeListener</code> interface used to listen changing of selection type in the {@link SelectionTree}.
 */
public interface SelectionTreeListener extends EventListener {

    /**
     * Invokes when selection type is changed.
     *
     * @param node {@link SelectionTreeNode} with selection type changed
     * @param oldType old selection type
     * @param newType new selection type
     */
    void selectionTypeChanged(SelectionTreeNode node, SelectionType oldType, SelectionType newType);

    /**
     * Invokes when selection change is started.
     *
     * @param node node which selection is going to be changed or null if this operation is the part of mass selection change (Select All/Deselect All)
     */
    void selectionChangeStarted(SelectionTreeNode node);

    /**
     * Invokes when selection change is finished.
     *
     * @param node node which selection is changed or null if this operation is the part of mass selection change (Select All/Deselect All)
     */
    void selectionChangeFinished(SelectionTreeNode node);
}
