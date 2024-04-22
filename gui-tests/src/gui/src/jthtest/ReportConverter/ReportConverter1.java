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
package jthtest.ReportConverter;

import jthtest.Test;
import jthtest.tools.JTFrame;
import jthtest.tools.Task.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

public class ReportConverter1 extends Test {
     String find1 = "Greetings!Thistoolisusedtomergemanyxmlreportsintoone.Selectoutputdirectoryand2ormorexmlreports.";
     String find2 = "Greetings!Thistoolisusedtomergemanyxmlreportsintoone.PushSettings->New...menutoopenconverterdialog.orSettings->Opentoopenafileforreading";

     @Override
     public void testImpl() throws Exception {
          mainFrame = new JTFrame(true);
          mainFrame.getTools_ReportConverterMenu().push();

          JEditorPaneOperator op = new JEditorPaneOperator(mainFrame.getJFrameOperator());
          Waiter waiter = new WaiterImpl(op, find1);
          waiter.waitForDone();

          JDialogOperator d = new JDialogOperator("Create a Report");
          new JButtonOperator(d, "Cancel").push();

          waiter = new WaiterImpl(op, find2);
          waiter.waitForDone();
     }

     private static class WaiterImpl extends Waiter {
          private final JEditorPaneOperator editpane;
          private final String toFind;

          public WaiterImpl(JEditorPaneOperator op, String toFind) {
               super(false);
               this.editpane = op;
               if (toFind == null || toFind.equals("")) {
                    throw new IllegalArgumentException();
               }
               this.toFind = toFind.replaceAll("[\t\n\r\f ]", "");
               start();
          }

          @Override
          protected boolean check() {
               String txt = editpane.getDisplayedText();
               if (txt == null)
                    return false;
               String text = txt.replaceAll("[\t\n\r\f ]", "");
               return text != null && text.equals(toFind);
          }
     }

}
