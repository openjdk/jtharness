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

package jthtest.CreateWorkdir;

import java.io.File;
import javax.swing.JTextField;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

import jthtest.Test;

import static jthtest.Tools.*;
import static jthtest.workdir.Workdir.*;

/**
 *
 * @author linfar
 */
public class CreateWorkdir08 extends Test {

    public CreateWorkdir08() {
	depricated = true;
    }

    public void testImpl() throws Exception {
	startJavaTestWithDefaultTestSuite();

	String path;

	JFrameOperator mainFrame = findMainFrame();

	deleteDirectory(DEFAULT_PATH+TEMP_WD_NAME);
	JDialogOperator wrkDir = openCreateWorkDirectoryDialog(mainFrame);
	chooseWorkDirectoryInDialog(wrkDir, TEMP_WD_NAME);

	new JRadioButtonOperator(wrkDir, getExecResource("wdc.template.rb")).push();

	JTextField templatePath = (JTextField)(new JLabelOperator(wrkDir, getExecResource("wdc.template.path.lbl")).getLabelFor());
	if(templatePath.getText().equals(""))
	    throw new JemmyException("Default template path is empty!");
	if(!((templatePath.getText() + File.separator).equals(DEFAULT_PATH)))
	    throw new JemmyException("Default template path doesn't match default path: " + templatePath.getText() + File.separator + " != " + DEFAULT_PATH);
    }

    @Override
    public String getDescription() {
	return "This test is depricated";
    }
}
