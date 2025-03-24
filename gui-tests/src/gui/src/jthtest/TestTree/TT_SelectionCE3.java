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
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

public class TT_SelectionCE3 extends TT_SelectionCE {

     /**
      * Check that selection on TestTree on main page doesn't dissapear when opening
      * Configuration Editor and closing it without any changes. Includes root path.
      */

     public TT_SelectionCE3() {
          super("loading and editing (without changes) ConfigEditor", new int[] { 0, 1, 2, 3, 5, 8, 10 });
          knownFail = true;
     }

     public void make() {
          Configuration c = mainFrame.getConfiguration();
          c.load(CONFIG_NAME, true);
          ConfigDialog cd = c.openByMenu(true);
          if (cd.getSelectedQuestionNumber() > 1)
               cd.pushBackConfigEditor();
          cd.pushLastConfigEditor();
          cd.pushDoneConfigEditor();
     }
}
