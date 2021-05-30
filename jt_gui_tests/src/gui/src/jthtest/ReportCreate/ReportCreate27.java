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
 * These test doesn't check any point of Html report as it doesn't affect xml report
 */
package jthtest.ReportCreate;

import java.io.File;
import static jthtest.ReportCreate.ReportCreate.*;
import jthtest.Test;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author linfar
 */
public class ReportCreate27 extends Test {

	public void testImpl() throws Exception {
		deleteUserData();
		startJavaTestWithDefaultWorkDirectory();

		JFrameOperator mainFrame = findMainFrame();

		JDialogOperator rep = openReportCreation(mainFrame);

		setXmlChecked(rep, true);
		setPlainChecked(rep, false);
		setHtmlChecked(rep, false);

		final String path = TEMP_PATH + REPORT_NAME + REPORT_POSTFIX_PLAIN + File.separator;
		deleteDirectory(path);
		setPath(rep, path);
		pressCreate(rep);
		addUsedFile(path);
		pressYes(findShowReportDialog());

		ReportBrowser browser = new ReportBrowser(path);

		String[] urls = browser.getUrls();
		int i = findInStringArray(urls, "xml/report.xml");
		if (i == -1) {
			throw new JemmyException("Expected 'xml/report.xml' url in report browser");
		}
		browser.clickUrl(urlFile(path + urls[i]));
		browser.waitForPageLoading("</Report>\n", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		String text = browser.getText();
		if (!text.contains("<Interview>")) {
			throw new JemmyException("Summary wasn't found ('<Interview>' expected)");
		}
		if (!text.contains("<Environment name=\"")) {
			throw new JemmyException("Test enviroment wasn't found ('<Environment name=\"' expected)");
		}
		if (!text.contains("<StandardValues>")) {
			throw new JemmyException("Standart values wasn't found ('<StandardValues>' expected)");
		}
		if (!text.contains("<TestResults>")) {
			throw new JemmyException("Result summary wasn't found ('<TestResults>' expected");
		}
//	if(!text.contains("<Keywords>")) {
//	    throw new JemmyException("Keywords wasn't found (predefined warning: there isn't any similar block)");
//	}
	}
}
