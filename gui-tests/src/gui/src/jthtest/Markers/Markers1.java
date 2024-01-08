/*
 * $Id$
 *
 * Copyright (c) 2009, 2023, Oracle and/or its affiliates. All rights reserved.
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

package jthtest.Markers;

import java.lang.reflect.InvocationTargetException;
import jthtest.Config_Edit.Config_Edit;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuOperator;

public class Markers1 extends Markers {
     /**
      * Start JavaTest with the -newDesktop option. Create a workdirectory. Load an
      * existing JTI file. Bring up configuration editor by doing Ctrl-E. Click on
      * Bookmarks menu. Verify that Enable bookmarks is checked off and all the other
      * entries are disabled.
      */
     public static void main(String args[]) {
          JUnitCore.main("jthtest.gui.Markers.Markers1");
     }

     @Test
     public void testMarkers1() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
          startJavatestNewDesktop();

          JFrameOperator mainFrame = findMainFrame();

          closeQS(mainFrame);
          openTestSuite(mainFrame);
          createWorkDirInTemp(mainFrame);
          openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
          Config_Edit.waitForConfigurationLoading(mainFrame, CONFIG_NAME);

          openConfigDialogByKey(mainFrame);
          JDialogOperator config = findConfigEditor(mainFrame);

          JMenuOperator bmMenu = new JMenuOperator(config, "Bookmarks");
          for (int i = 1; i < bmMenu.getComponentCount(); i++)
               if (bmMenu.getMenuComponent(i).isEnabled())
                    throw new JemmyException("Menu subelement " + i + " is enabled while unexpected");
     }
}
