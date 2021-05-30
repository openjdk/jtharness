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
/*
 * System.getProperty("user.dir");
 */
package jthtest.CreateWorkdir;

import jthtest.Tools;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.swing.JTextField;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class CreateWorkdir extends Tools {

    public static String NEW_WD_NAME = "some_temp_wd_that_will_be_deleted";

    public static class WorkdirOperator extends JDialogOperator {
	private JDialogOperator dialog;
	private String commitButtonName;

	private static final String CREATE_DIALOG_NAME = getToolResource("wdc.new.title");
	private static final String OPEN_DIALOG_NAME = getToolResource("wdc.open.title");
	private static final String MENU_OPEN_PATH = getExecResource("qlb.file.menu") + "|Open|Work Directory ...";
	private static final String MENU_CREATE_PATH = getExecResource("qlb.file.menu") + "|" + getExecResource("ch.newWorkDir.act");

	private WorkdirOperator() {}

	private WorkdirOperator(JFrameOperator mainFrame, String name) {
	    super(mainFrame, name);
	}

	private WorkdirOperator(String name) {
	    super(name);
	}

	public String getCommitButtonName() {
	    return commitButtonName;
	}

	public void setPath(String path) {
	}

	public void dismiss() {
	}

	public void accept() {
	}

	public static WorkdirOperator openWorkdirByMenu() {
	    WorkdirOperator workdirOperator = new WorkdirOperator();
	    workdirOperator.dialog = callOpenByMenu();
	    
	    return workdirOperator;
	}

	public static WorkdirOperator createWorkdirByMenu() {
	    WorkdirOperator workdirOperator = new WorkdirOperator();
	    workdirOperator.dialog = callCreateByMenu();

	    return workdirOperator;
	}

	private static JDialogOperator callOpenByMenu() {
	    JFrameOperator mainFrame = findMainFrame();
	    new JMenuOperator(mainFrame).pushMenu(MENU_OPEN_PATH, "|");
	    return new JDialogOperator(mainFrame, OPEN_DIALOG_NAME);
	}

	private static JDialogOperator callCreateByMenu() {
	    JFrameOperator mainFrame = findMainFrame();
	    new JMenuOperator(mainFrame).pushMenu(MENU_CREATE_PATH, "|");
	    return new JDialogOperator(mainFrame, CREATE_DIALOG_NAME);
	}
    }

    public static final String OPEN_WD_MENU_PATH = getExecResource("tmgr.openMenu.menu") + "|" + getExecResource("ch.setWorkDir.act");
    public static final String CREATE_WD_MENU_PATH = getExecResource("ch.newWorkDir.act");
    public static final String OPEN_DIALOG_NAME = getToolResource("wdc.open.title");
    public static final String CREATE_DIALOG_NAME = getToolResource("wdc.new.title");
    public static final String COMMIT_BUTTON_NAME = getExecResource("wdc.create.btn");
    public static final String[] TEXT_FIELD_NAMES = {"Folder name:", "File name:", "Folder Name:", "File Name:"};

    public static void startJavaTestWithDefaultTestSuite() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
	new ClassReference("com.sun.javatest.tool.Main").startApplication(new String[]{"-NewDesktop", "-open", TEST_SUITE_NAME});
    }

    public static WorkdirOperator openWorkDirectoryOpening(JFrameOperator mainFrame) {
	new JMenuOperator(mainFrame, getExecResource("qlb.file.menu")).pushMenuNoBlock(getExecResource("qlb.file.menu") + "|" + OPEN_WD_MENU_PATH, "|", new SimpleStringComparator());
	WorkdirOperator workdirOperator = new WorkdirOperator(OPEN_DIALOG_NAME);
	workdirOperator.commitButtonName = "Open";
	return workdirOperator;
    }

    public static WorkdirOperator openWorkDirectoryCreation(JFrameOperator mainFrame) {
	new JMenuOperator(mainFrame, getExecResource("qlb.file.menu")).pushMenuNoBlock(getExecResource("qlb.file.menu") + "|" + CREATE_WD_MENU_PATH, "|");
	return findWorkDirectoryCreation(mainFrame);
    }

    public static WorkdirOperator findWorkDirectoryCreation(JFrameOperator mainFrame) {
	WorkdirOperator workdirOperator = new WorkdirOperator(mainFrame, CREATE_DIALOG_NAME);
	workdirOperator.commitButtonName = COMMIT_BUTTON_NAME;
	return workdirOperator;
    }

    private static void setPath(WorkdirOperator dialog, String path) {
	JTextFieldOperator tf = new JTextFieldOperator((JTextField)Tools.getComponent(dialog, TEXT_FIELD_NAMES));
	tf.setText(path);
    }

    private static void commit(WorkdirOperator dialog) {
	new JButtonOperator(dialog, dialog.commitButtonName).push();
    }

    // creates standard work directory using menu, deleting previous optionary
    public static void createWorkDirInTemp(WorkdirOperator mainFrame, boolean delete) {
	String path = TEMP_PATH + TEMP_WD_NAME;
	createWorkDir(mainFrame, TEMP_PATH, TEMP_WD_NAME, delete);
    }

    // creates standard work directory using menu
    public static String createWorkDirInCurrent(WorkdirOperator mainFrame) {
	String path = new File("").getAbsolutePath();
	createWorkDir(mainFrame, path, TEMP_WD_NAME, true);
	return path;
    }

    public static void createWorkDir_(JFrameOperator mainFrame, String path, String workDir, boolean delete) {
	createWorkDir(openWorkDirectoryCreation(mainFrame), path, workDir, delete);
    }

    public static void createWorkDir(WorkdirOperator wrkDir, String path, String workDir, boolean delete) {
	String fullPath;
	if(path != null)
	    fullPath = path + File.separator + workDir;
	else
	    fullPath = workDir;
	
	if (delete) {
	    deleteDirectory(new File(fullPath));
	}

	setPath(wrkDir,fullPath);

	commit(wrkDir);
    }
    
    public static boolean verifyWorkdirCreation() {
	return new File(TEMP_PATH + TEMP_WD_NAME).exists();
    }

    public static boolean verifyWorkdirCreation(String path) {
	return new File(path).exists();
    }

    public static JDialogOperator getOpenErrorDialog(JFrameOperator mainFrame) {
	return new JDialogOperator(mainFrame, getToolResource("wdc.exists_openIt.title"));
    }

    public static String createWorkDirDefWithConfig_() {
	String path;
	JFrameOperator mainFrame = findMainFrame();
	new JMenuOperator(mainFrame).pushMenuNoBlock(OPEN_WD_MENU_PATH, "|");

	JDialogOperator wrkDir = new JDialogOperator(mainFrame, CREATE_DIALOG_NAME);

	new JButtonOperator(wrkDir, getExecResource("wdc.browse.btn")).push();
	JDialogOperator filer = new JDialogOperator(mainFrame, getExecResource("wdc.filechoosertitle"));
	JTextFieldOperator tf;
	tf = new JTextFieldOperator((JTextField)Tools.getComponent(filer, new String[] {"Folder name:", "File name:"}));
	path = tf.getText() + "/";
	tf.typeText(path);
	deleteDirectory(new File(path + TEMP_WD_NAME));
	new JButtonOperator(filer, "Open").push();

	getTextField(wrkDir, getExecResource("wdc.dir.name.lbl")).typeText(TEMP_WD_NAME);

	new JRadioButtonOperator(wrkDir, getExecResource("wdc.template.rb")).push();

	new JButtonOperator(wrkDir, new NameComponentChooser("wdc.template.browse")).push();

	filer = new JDialogOperator(mainFrame, getExecResource("wdc.templchoosertitle"));

	tf = new JTextFieldOperator((JTextField)Tools.getComponent(filer, new String[] {"Folder name:", "File name:"}));
	tf.enterText(TEMPLATE_NAME);

	new JButtonOperator(wrkDir, getExecResource("wdc.create.btn")).push();

	return path;
    }

    public static void createWorkDirWithTemplate_(String basePath) {
	JFrameOperator mainFrame = findMainFrame();
	new JMenuOperator(mainFrame).pushMenuNoBlock(OPEN_WD_MENU_PATH, "|");

	JDialogOperator wrkDir = new JDialogOperator(mainFrame, CREATE_DIALOG_NAME);

	deleteDirectory(new File(basePath + TEMP_WD_NAME));
	getTextField(wrkDir, getExecResource("wdc.dir.name.lbl")).typeText(TEMP_WD_NAME);

	new JButtonOperator(wrkDir, getExecResource("wdc.browse.btn")).push();

	JDialogOperator filer = new JDialogOperator(mainFrame, getExecResource("wdc.filechoosertitle"));

	JTextFieldOperator tf;

	tf = new JTextFieldOperator((JTextField)Tools.getComponent(filer, new String[] {"Folder name:", "File name:"}));
	tf.enterText(basePath);

	new JRadioButtonOperator(wrkDir, getExecResource("wdc.template.rb")).push();

	new JButtonOperator(wrkDir, new NameComponentChooser("wdc.template.browse")).push();

	filer = new JDialogOperator(mainFrame, getExecResource("wdc.templchoosertitle"));

	tf = new JTextFieldOperator((JTextField)Tools.getComponent(filer, new String[] {"Folder name:", "File name:"}));
	tf.enterText(TEMPLATE_NAME);

	new JButtonOperator(wrkDir, getExecResource("wdc.create.btn")).push();
    }
}
