/*
 * $Id$
 *
 * Copyright (c) 2009, 2024, Oracle and/or its affiliates. All rights reserved.
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
import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

public class Markers22 extends Test {

     /**
      * This test case verifies that selecting the remove all marked questions button
      * will remove the marked question.
      */
     public void testImpl() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
          mainFrame = new JTFrame(true);

          mainFrame.openDefaultTestSuite();
          addUsedFile(mainFrame.createWorkDirectoryInTemp());
          Configuration configuration = mainFrame.getConfiguration();
          configuration.load(CONFIG_NAME, true);

          ConfigDialog config = configuration.openByKey();
          String[] names = config.getElementsNames();
          int[] indexes = new int[] { -1, -1 };
          int i = 0;
          for (String s : names) {
               if ("Concurrency".equalsIgnoreCase(s.trim())) {
                    indexes[0] = i;
               }
               if ("Time Factor".equalsIgnoreCase(s.trim())) {
                    indexes[1] = i;
               }
               if (indexes[1] > 0) {
                    break;
               }
               i++;
          }
          config.getBookmarks_EnableBookmarks().push();
          setSomeValues(config.getConfigDialog(), indexes);
          config.setBookmarkedByMenu(indexes);
          config.getBookmarks_ClearAnswerToBookmarkedQuestionsMenu().push();
          checkValues(config.getConfigDialog(), indexes);

     }

     private void checkValues(JDialogOperator config, int[] indexes) {
          JListOperator list = new JListOperator(config);

          list.selectItem(indexes[0]);
          if (!(new JTextFieldOperator(config, new NameComponentChooser("int.field.txt")).getText().equals("1"))) {
               throw new JemmyException("Value in Concurrency was not reset");
          }
          list.selectItem(indexes[1]);
          if (!(new JTextFieldOperator(config, new NameComponentChooser("flt.field.txt")).getText().equals("1"))) {
               throw new JemmyException("Value in Time Factor was not reset");
          }
     }

     private void setSomeValues(JDialogOperator config, int[] indexes) {
          JListOperator list = new JListOperator(config);

          list.selectItem(indexes[0]);
          new JTextFieldOperator(config, new NameComponentChooser("int.field.txt")).setText("");
          new JTextFieldOperator(config, new NameComponentChooser("int.field.txt")).typeText("22");
          list.selectItem(indexes[1]);
          new JTextFieldOperator(config, new NameComponentChooser("flt.field.txt")).setText("");
          new JTextFieldOperator(config, new NameComponentChooser("flt.field.txt")).typeText("22");
     }
}
