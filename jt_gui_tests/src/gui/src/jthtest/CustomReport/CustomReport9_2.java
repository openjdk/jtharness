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

package jthtest.CustomReport;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JCheckBox;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author linfar
 */
public class CustomReport9_2 extends CustomReport {
    // this test was excluded because current testsuite doesn't provide custom report formats
    public static void main(String[] args) {
	JUnitCore.main("jthtest.gui.CustomReport.CustomReport9_2");
    }
    
    @Test
    public void testCustomReport9_2() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
//	startJavaTestWithDefaultWorkDirectory();
//
//	JFrameOperator mainFrame = findMainFrame();
//
//	JDialogOperator rep = openReportCreation(mainFrame);
//
//	setPath(rep, TEMP_PATH + REPORT_NAME + File.separator);
//
//	JCheckBox t = selectType(rep, ReportType.CUSTOM_TEXT);
//	t.setSelected(true);
//   	selectType(rep, ReportType.CUSTOM_XML);
//	selectType(rep, ReportType.CUSTOM_TEXT);
//
//	String repPath = "customText";
//	new JTextFieldOperator(rep, "", 1).typeText(repPath);
//
//	pressCreate(rep);
//
//	if(!(new File(TEMP_PATH + REPORT_NAME + File.separator + "customReport1" + File.separator + repPath).exists()))
//	    throw new JemmyException("Report was not created");
    }
}
