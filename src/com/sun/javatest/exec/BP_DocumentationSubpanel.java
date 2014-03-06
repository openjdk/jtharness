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

import com.sun.javatest.TestSuite;
import com.sun.javatest.tool.UIFactory;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BP_DocumentationSubpanel extends BP_BranchSubpanel {

    /** Creates a new instance of BP_DocumentationSubpanel */
    public BP_DocumentationSubpanel(UIFactory uif, BP_Model bpm, TestTreeModel ttm, ExecModel em) {
        super("ds", uif, bpm, ttm, "br.ds");
        this.em = em;
        this.bpm = bpm;
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());

        filesPane = new FilesPane(uif);

        emptyPane = uif.createPanel("br.ds.ep", new GridBagLayout(), false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = gbc.CENTER;
        JLabel emptyL = uif.createLabel("br.ds.el");
        emptyPane.add(emptyL, gbc);
    }

    protected void updateSubpanel(TT_BasicNode currNode) {
        super.updateSubpanel(currNode);
        TestSuite ts = em.getTestSuite();
        try{
            String path = currNode.getLongPath();
            filelist = em.getTestSuite().getDocsForFolder(path);
            if(filelist != null) {
                filesPane.setFiles(filelist);
                setPanel(filesPane);
            }
            else {
                setPanel(emptyPane);
            }
        }catch(Exception e){

        }

        validateEnabledState();
    }

    protected void invalidateFilters() {
        super.invalidateFilters();
        validateEnabledState();
    }

    private void validateEnabledState() {
        if(filelist != null)
            bpm.setEnabled(this, true);
        else
            bpm.setEnabled(this, false);
    }

    private void setPanel(JPanel p) {
        if(p != null) {
            this.removeAll();
            this.add(p, BorderLayout.CENTER);
        }
    }


    private FilesPane filesPane;
    private JPanel emptyPane;
    private ExecModel em;
    private BP_Model bpm;
    private URL[] filelist;

}
