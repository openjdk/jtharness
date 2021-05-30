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
package jthtest.TestTree;

import jthtest.tools.ConfigDialog;

/**
 *
 * @author andrey
 */
public class TT_SelectionCE02 extends TT_SelectionCE {

    public TT_SelectionCE02() {
        super("creating and cancelling ConfigEditor", new int[]{1, 2, 3, 5, 8, 10});
    }

    public void make() {
        ConfigDialog cd = mainFrame.getConfiguration().create(true);
        cd.closeByMenu();
//        new JButtonOperator(new JDialogOperator("Warning: Unsaved Changes"), "No").push();
    }

    public String getDescription() {
        return "Check that selection on TestTree on main page doesn't dissapear when opening Create New Configuration and closing it without saving. Doesn't include root path.";
    }
}
