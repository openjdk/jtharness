/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.exec;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.net.URL;

import javax.help.CSH;
import javax.swing.SwingUtilities;

import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;

/**
 * Show the source and other files for the test.
 */

class TP_FilesSubpanel extends TP_Subpanel {
    TP_FilesSubpanel(UIFactory uif) {
        super(uif, "files");
        setLayout(new BorderLayout());
        CSH.setHelpIDString(this, "browse.filesTab.csh");

        filesPane = new FilesPane(uif);

        add(filesPane, BorderLayout.CENTER);
    }

    protected void updateSubpanel(TestResult currTest) {
        if (testSuite == null)
            throw new IllegalStateException();

        TestDescription oldDesc = subpanelDesc;

        super.updateSubpanel(currTest);

        if (subpanelDesc != oldDesc) {
            // The test has changed; need to update the files panel
            if (EventQueue.isDispatchThread())
                updateFiles();
            else
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            updateFiles();
                        }
                    });
        }
    }

    private void updateFiles() {
        URL[] urls = testSuite.getFilesForTest(subpanelDesc);

        filesPane.setBaseDirectory(subpanelDesc.getDir());
        filesPane.setFiles(urls);
    }

    private FilesPane filesPane;
}


