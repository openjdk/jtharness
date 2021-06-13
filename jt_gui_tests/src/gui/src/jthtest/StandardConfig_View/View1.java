/*
 * $Id$
 *
 * Copyright (c) 2009, 2010, Oracle and/or its affiliates. All rights reserved.
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

package jthtest.StandardConfig_View;

import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JMenuOperator;

/**
 *
 * @author linfar
 */
public class View1 extends View {

    public static void main(String[] args) {
            JUnitCore.main("jthtest.gui.StandardConfig_View.View1");
    }

        @Test
        public void testView1() {
            openTestSuite(mainFrame);
            createWorkDirInTemp(mainFrame);
            JDialogOperator editor = callNewConfigurationEditor();
        JMenuItem findJMenu = null;
            if(!((JRadioButtonMenuItem)new JMenuOperator(editor, "View").getMenuComponent(0)).isSelected()) { // TODO findJComponent
        throw new JemmyException("Radio button is not selected");
        }
//            new JRadioButtonOperator(editor).isSelected();
        }
}
