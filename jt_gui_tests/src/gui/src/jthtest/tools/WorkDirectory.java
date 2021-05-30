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
package jthtest.tools;

import java.io.File;
import javax.swing.JTextField;
import jthtest.Tools;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author at231876
 */
public class WorkDirectory {

	private String fullPath;
	private final JTFrame mainFrame;
	public static String TO_DELETE_TEMP_WD_NAME = "some_temp_wd_that_will_be_deleted";

	public enum WDStatus {

		ALL_PASSED, SOME_FAILED, SOME_ERRORS, SOME_NOTRUN
	}

	WorkDirectory(JTFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public WorkDirectoryBrowser openWorkDirectoryCreation() {
		mainFrame.getFile_CreateWorkDirectoryMenu().pushNoBlock();
		return WorkDirectoryBrowser.create();
	}

	public WorkDirectoryBrowser openWorkDirectoryOpening() {
		mainFrame.getFile_Open_WorkDirectoryMenu().pushNoBlock();
		return WorkDirectoryBrowser.open();
	}

	public File createWorkDirectory(String path, String name, boolean delete) {
		fullPath = path + File.separator + name;
		File file = new File(fullPath);
		if (delete) {
			Tools.deleteDirectory(file);
		}

		WorkDirectoryBrowser browser = openWorkDirectoryCreation();
		browser.setPath(path);
		browser.setName(name);
		browser.commit();

		return file;
	}

	public File createWorkDirectory(String name, boolean delete) {
		fullPath = Tools.LOCAL_PATH + File.separator + name;
		File file = new File(fullPath);
		if (delete) {
			Tools.deleteDirectory(file);
		}

		WorkDirectoryBrowser browser = openWorkDirectoryCreation();
		browser.setFullPath(name);
		browser.commit();

		return file;
	}

	public File createWorkDirectoryInTemp() {
		int attempts = 0;
		File temp = new File(Tools.TEMP_PATH);
		if (!temp.exists()) {
			temp.mkdirs();
		}

		WorkDirectoryBrowser browser = openWorkDirectoryCreation();

		String wdName = Tools.TEMP_WD_NAME;
		File file = null;
		while (attempts < 10) {
			String path = Tools.TEMP_PATH + wdName;
			file = new File(path);
			if (!file.exists()) {
				break;
			}
			Tools.deleteDirectory(file);
			file = new File(path);
			if (!file.exists()) {
				break;
			}
			wdName = Tools.TEMP_WD_NAME + (int) (Math.random() * 10000);
			attempts++;
		}
		if (attempts >= 10) {
			throw new JemmyException("error");
		}

		browser.setPath(Tools.TEMP_PATH, wdName);
//		browser.setName(wdName);
		browser.commit();

		return file;
	}

	public File openWorkDirectory(String path, String name) {
		fullPath = path + File.separator + name;
		File file = new File(fullPath);

		WorkDirectoryBrowser browser = openWorkDirectoryOpening();
		browser.setPath(path);
		browser.setName(name);
		browser.commit();

		return file;
	}

	public File openWorkDirectory(File wd) {
		fullPath = wd.getAbsolutePath();
		File file = new File(fullPath);

		WorkDirectoryBrowser browser = openWorkDirectoryOpening();
		browser.setFullPath(fullPath);
		browser.commit();

		return file;
	}

	public void waitForStatus(WDStatus status) {
		new JTextFieldOperator(mainFrame.getJFrameOperator(), Tools.getExecResource("br.worst." + status.ordinal()));
	}
}
