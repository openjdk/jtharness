/*
 * $Id$
 *
 * Copyright (c) 2011,2024 Oracle and/or its affiliates. All rights reserved.
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

import jthtest.TT_SelectionCE;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

public class TT_SelectionCE9 extends TT_SelectionCE {

     /**
      * Check that selection on TestTree on main page doesn't dissapear when editing
      * TestsToRun list in Configuration Editor and closing it with save. Doesn't
      * include root path.
      */

    public TT_SelectionCE9() {
        super("opening 2 new ExecTools (by TestSuite loading and by new WD creating)", new int[]{0, 1, 5, 7, 9});
        knownFail = true;
    }

    @Override
    protected void make() {
        mainFrame.openDefaultTestSuite();
        mainFrame.closeCurrentTool();
        mainFrame.getFile_CreateWorkDirectoryMenu().push();
        new JButtonOperator(new JDialogOperator("Create Work Directory"), "Cancel").push();
        mainFrame.closeCurrentTool();
    }
}
