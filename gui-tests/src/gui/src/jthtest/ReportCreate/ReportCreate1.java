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


package jthtest.ReportCreate;

import jthtest.Test;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import java.io.File;

import static jthtest.ReportCreate.ReportCreate.*;

public class ReportCreate1 extends Test {

    public void testImpl() throws Exception {
        deleteUserData();
        startJavaTestWithDefaultWorkDirectory();

        JFrameOperator mainFrame = findMainFrame();

        JDialogOperator rep = openReportCreation(mainFrame);
        String path = TEMP_PATH + REPORT_NAME + "_default" + File.separator;
        deleteDirectory(path);
        setPath(rep, path);

        pressCreate(rep);
        addUsedFile(path);

        findShowReportDialog();

        if (!new File(path + "html" + File.separator + "report.html").exists()) {
            throw new JemmyException("report.html was not found in");
        }
        if (!new File(path + "html" + File.separator + "error.html").exists()) {
            throw new JemmyException("error.html was not found");
        }
        if (!new File(path + "html" + File.separator + "notRun.html").exists()) {
            throw new JemmyException("notRun.html was not found");
        }
        if (!new File(path + "html" + File.separator + "failed.html").exists()) {
            throw new JemmyException("failed.html was not found");
        }
        if (!new File(path + "html" + File.separator + "passed.html").exists()) {
            throw new JemmyException("passed.html was not found");
        }
        if (!new File(path + "html" + File.separator + "report.css").exists()) {
            throw new JemmyException("report.css was not found");
        }
        if (!new File(path + "text" + File.separator + "summary.txt").exists()) {
            throw new JemmyException("summary.txt was not found");
        }
        if (!new File(path + "html" + File.separator + "excluded.html").exists()) {
            throw new JemmyException("excluded.html was not found");
        }
    }
}
