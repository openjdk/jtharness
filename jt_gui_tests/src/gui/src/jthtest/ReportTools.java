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

package jthtest;

import java.lang.reflect.InvocationTargetException;
import javax.swing.JCheckBox;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class ReportTools extends Tools {
    public enum ReportType {HTML, PLAIN_TEXT, XML, CUSTOM_TEXT, CUSTOM_XML}; 

    public static final String REPORT_POSTFIX_HTML = "_html";
    public static final String REPORT_POSTFIX_XML = "_xml";
    public static final String REPORT_POSTFIX_PLAIN = "_text";
    public static final String REPORT_WD_PATH = "demowd_run";
    
    public static void startJavaTestWithDefaultWorkDirectory() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
	new ClassReference("com.sun.javatest.tool.Main").startApplication(new String[]{"-NewDesktop", "-open", LOCAL_PATH + java.io.File.separator + REPORT_WD_PATH});
    }

    public static void setPath(JDialogOperator rep, String path) {
	new JButtonOperator(rep, getExecResource("nrd.browse.btn")).push();
	JDialogOperator browser = new JDialogOperator("Report Directory");
	getTextField(browser, "File Name:").typeText(path);
        try { Thread.sleep(1000); } catch(Exception e) {}
	new JButtonOperator(browser, "Open").push();
    }
    
    public static void pressCreate(JDialogOperator rep) {
	new JButtonOperator(rep, getExecResource("nrd.ok.btn")).push();
    }

    public static JDialogOperator openReportCreation(JFrameOperator mainFrame) {
	new JMenuOperator(mainFrame, getExecResource("rpth.menu")).pushMenuNoBlock(("rpth.menu") + "|" + getExecResource("rpth.new.act"), "|");
	return new JDialogOperator(getExecResource("nrd.title"));
    }

    public static JCheckBox getListElement(JDialogOperator rep, int index) {
	JListOperator types = getList(rep);
	return (JCheckBox) types.getModel().getElementAt(index);
    }

    public static JListOperator getList(JDialogOperator rep) {
	JListOperator types = new JListOperator(rep, new NameComponentChooser("nrd.typel"));
	return types;
    }
    
    public static JDialogOperator createReports(JFrameOperator mainFrame, String path, boolean createHtml, boolean createPlain, boolean createXml) {
	JDialogOperator rep = openReportCreation(mainFrame);
	
	setPath(rep, path);
	
	setHtmlChecked(rep, createHtml);
	setPlainChecked(rep, createPlain);
	setXmlChecked(rep, createXml);

	rep.invalidate();
	
	pressCreate(rep);
	
	return findShowReportDialog();
    }
    
    public static JDialogOperator findShowReportDialog() {
	return new JDialogOperator(getExecResource("nrd.showReport.title"));
    }
    
    // returns previous state
    public static boolean setHtmlChecked(JDialogOperator rep, boolean set) {
	JCheckBox element = getListElement(rep, 0);
	boolean temp = element.isSelected();
	element.setSelected(set);
	return temp; 
    }
    
    // returns previous state
    public static boolean setPlainChecked(JDialogOperator rep, boolean set) {
//	JListOperator types = new JListOperator(rep, new NameComponentChooser("nrd.typel"));
//	JCheckBoxOperator element = new JCheckBoxOperator(types, getExecResource("nrd.type.pt.ckb"));
//	JListOperator types = new JListOperator(rep, new NameComponentChooser("nrd.typel"));
	JCheckBox element = getListElement(rep, 1);
	boolean temp = element.isSelected();
	element.setSelected(set);
	return temp; 
    }
    
    // returns previous state
    public static boolean setXmlChecked(JDialogOperator rep, boolean set) {
//	JListOperator types = new JListOperator(rep, new NameComponentChooser("nrd.typel"));
//	JCheckBox element = (JCheckBox)types.getModel().getElementAt(2);
	JCheckBox element = getListElement(rep, 2);
	boolean temp = element.isSelected();
	element.setSelected(set);
	return temp; 
    }
}
