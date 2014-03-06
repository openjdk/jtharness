/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;
import com.sun.javatest.tool.UIFactory;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TP_DocumentationSubpanel extends TP_Subpanel{

    /** Creates a new instance of TP_DocumentationSubpanel */
    public TP_DocumentationSubpanel(UIFactory uif) {
        super(uif, "docs");
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());

        filesPane = new FilesPane(uif);

        emptyPane = uif.createPanel("test.docs.ep", new GridBagLayout(), false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = gbc.CENTER;
        JLabel emptyL = uif.createLabel("test.docs.el");
        emptyPane.add(emptyL, gbc);
    }

    protected void updateSubpanel(TestResult currTest) {
        super.updateSubpanel(currTest);
        try {
            TestDescription td = currTest.getDescription();
            filelist = testSuite.getDocsForTest(td);
            if(filelist != null) {
                filesPane.setFiles(filelist);
                setPanel(filesPane);
            }
            else {
                setPanel(emptyPane);
            }
        } catch (TestResult.Fault e) {
            return;
        }
    }

    public URL[] getDocuments() {
        return filelist;
    }

    private void setPanel(JPanel p) {
        if(p != null) {
            this.removeAll();
            this.add(p, BorderLayout.CENTER);
        }
    }

    private FilesPane filesPane;
    private JPanel emptyPane;
    private URL[] filelist;
}
