/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview.wizard;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.sun.interview.FileFilter;
import com.sun.interview.FileListQuestion;
import com.sun.interview.Question;
import java.io.File;

public class FileListQuestionRenderer
    implements QuestionRenderer
{
    public JComponent getQuestionRendererComponent(Question qq, ActionListener listener) {
        final FileListQuestion q = (FileListQuestion) qq;

        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("flst");
        panel.setFocusable(false);

        JLabel label = new JLabel(i18n.getString("flst.lbl"));
        label.setName("flst.lbl");
        label.setDisplayedMnemonic(i18n.getString("flst.mne").charAt(0));
        label.setToolTipText(i18n.getString("flst.tip"));
        panel.add(label, BorderLayout.NORTH);

        final FileList list = createFileList("flst", q.getValue());
        list.setDuplicatesAllowed(q.isDuplicatesAllowed());
        list.addListDataListener(new ActionListDataListener(panel,
                                                            listener,
                                                            QuestionRenderer.EDITED));
        list.setBaseDirectory(q.getBaseDirectory());
        label.setLabelFor(list);

        FileFilter[] filters = q.getFilters();
        if (filters == null || filters.length == 0) {
            list.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        else {
            int mode = -1;
            for (int i = 0; i < filters.length; i++) {
                FileFilter filter = filters[i];
                list.addFilter(SwingFileFilter.wrap(filter));
                if (filter.acceptsDirectories()) {
                    if (mode == -1){
                        //
                        // setting mode to DIRECTORIES_ONLY ignores the possibility
                        // that the filter might accept (some) files, so set it to
                        // FILES_AND_DIRECTORIES and leave to filter to hide any
                        // unacceptable files.
                        // Same issue in FileQuestionRenderer
                        mode = JFileChooser.FILES_AND_DIRECTORIES;
                    }
                    else if (mode == JFileChooser.FILES_ONLY)
                        mode = JFileChooser.FILES_AND_DIRECTORIES;
                }
                else {
                    if (mode == -1)
                        mode = JFileChooser.FILES_ONLY;
                    else if (mode == JFileChooser.DIRECTORIES_ONLY)
                        mode = JFileChooser.FILES_AND_DIRECTORIES;
                }
            }
            list.setFileSelectionMode(mode);
        }

        panel.add(list, BorderLayout.CENTER);

        Runnable valueSaver = new Runnable() {
            public void run() {
                q.setValue(list.getFiles());
            }
        };

        panel.putClientProperty(VALUE_SAVER, valueSaver);

        return panel;
    }

    protected FileList createFileList(String uiKey, File[] files) {
        return new FileList(uiKey, files);
    }

    public String getInvalidValueMessage(Question q) {
        return null;
    }

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
}
