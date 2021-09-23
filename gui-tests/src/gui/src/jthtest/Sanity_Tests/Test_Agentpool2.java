/*
 * $Id$
 *
 * Copyright (c) 2001, 2021, Oracle and/or its affiliates. All rights reserved.
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


package jthtest.Sanity_Tests;

import jthtest.menu.Menu;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.fail;

public class Test_Agentpool2 extends Config_New {
    //This is the 30th Sanity Test. It tests that when a negative value is given to the port value in agent monitor an error message should appear.

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_Agentpool2");
    }

    @Test
    public void test30() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, FileNotFoundException, InterruptedException, IOException {
        //Selecting Tools->Agent Monitor.
        JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
        jmbo.pushMenu(Menu.getToolsMenuName(), "/");

        Thread.sleep(200);
        jmbo.pushMenu(Menu.getToolsMenuName() + "/" + Menu.getTools_AgentMonitorMenuName(), "/");

        //Getting Port text field and listening check box.
        JTextFieldOperator port_value = new JTextFieldOperator(mainFrame, 0);
        JCheckBoxOperator listening = new JCheckBoxOperator(mainFrame, 0);

        //Inputting -1 as port value and pressing listening checkbox.
        port_value.clearText();
        port_value.typeText("-1");
        listening.push();

        //getting expected error dialog box.
        JDialogOperator error = new JDialogOperator("JT Harness: Error");

        //If no dialog box appear failing the test.
        if (!error.isVisible()) {
            fail("Failure because no error message was prompted even when a value -1 was given as input to port value.");
        }
    }
}