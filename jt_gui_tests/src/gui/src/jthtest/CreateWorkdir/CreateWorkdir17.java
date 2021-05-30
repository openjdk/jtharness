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

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import jthtest.Test;

import static jthtest.Tools.*;

/**
 *
 * @author linfar
 */
public class CreateWorkdir17 extends Test {

    public CreateWorkdir17() {
	depricated = true;
    }
    JFrameOperator mainFrame;

    public void testImpl() throws Exception {
	startJavatest(new String[]{"-workdir", "demowd_template"});

	mainFrame = findMainFrame();

	if (!(new JTextFieldOperator(mainFrame, new NameComponentChooser("bcc.WorkDir")).getText().equals("demowd_template"))) {
	    throw new JemmyException("Work Directory is not shown in status bar");
	}
	if (!(new JTextFieldOperator(mainFrame, new NameComponentChooser("bcc.Configuration")).getText().equals("demotemplate.jtm"))) {
	    throw new JemmyException("Template is not shown in status bar");
	}
    }

    @Override
    public String getDescription() {
	return "This test is depricated";
    }
}
