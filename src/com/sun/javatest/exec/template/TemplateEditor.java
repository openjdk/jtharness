/*
 * $Id$
 *
 * Copyright (c) 2010, 2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.exec.template;

import com.sun.interview.Interview;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.exec.ContextManager;
import com.sun.javatest.exec.InterviewEditor;
import com.sun.javatest.exec.WorkDirChooseTool;
import com.sun.javatest.tool.UIFactory;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Provides template specific features of interview editing...
 */
public class TemplateEditor extends InterviewEditor {
    static final String TEMPLATE_EXTENSION = ".jtm";
    static final String JTM = TEMPLATE_EXTENSION;
    static final String TEMPLATE_HISTORY = "templateHistory.jtl";

    /**
     * Creates an instance of TemplateEditor in the same manner as the
     * parent does.
     */
    public TemplateEditor(JComponent parent, UIFactory uif,
            InterviewParameters ip, ContextManager cm) {
        super(parent, uif, ip, cm);
        super.viewConfig.setTemplate(true);
        super.templateMode = true;
    }


    /**
     * @return template extenstion
     */
    @Override
    public String getExtention() {
        return JTM;
    }

    /**
     * Returns file name to store history of template files.
     */
    @Override
    protected String getHistoryFileName() {
        return TEMPLATE_HISTORY;
    }

    /**
     * Invokes super.notifyObservers(), but before makes sure that viewConfig
     * is template.
     */
    @Override
    protected void notifyObservers() {
        viewConfig.setTemplate(true);
        super.notifyObservers();
    }

    /**
     * Overrides parent behaviour in template specific way.
     */
    @Override
    protected void doSave(File file) throws Interview.Fault, IOException {
        viewConfig.save(file, true);
        viewConfig.setTemplate(true);
    }

    @Override
    protected String getRestorerWindowKey(boolean isFullView) {
        return "confEdit.template" + (isFullView ? ".f" : ".s");
    }

    @Override
    protected void initGUI() {
        super.initGUI();
        JMenuBar bar = super.getJMenuBar();
        if (bar != null) {
            for (int i = bar.getMenuCount() - 1; i >= 0; i--) {
                JMenu jm = bar.getMenu(i);
                if (jm != null) {
                    String mName = jm.getName();
                    if (mName != null && mName.endsWith("help")) {
                        JMenuItem mainItem = uif.createHelpMenuItem("te.help.maint", "confEdit.templateDialog.csh");
                        jm.insert(mainItem, 1);
                        break;
                    }
                }
            }
        }
    }

    /**
     * @return  mode that will be used by WorkDirChooseTool to select file.
     */
    @Override
    public int getFileChooserMode() {
        return WorkDirChooseTool.LOAD_TEMPLATE;
    }

}
