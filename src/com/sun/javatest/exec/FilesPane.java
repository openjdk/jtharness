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
package com.sun.javatest.exec;

import java.awt.BorderLayout;
import java.io.File;
import java.net.URL;
import javax.swing.JPanel;
import javax.swing.JTextField;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.I18NResourceBundle;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import javax.swing.JEditorPane;

class FilesPane extends JPanel implements Printable {

    static class Fault extends Exception {
        Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }
    }

    FilesPane(UIFactory uif) {
        this.uif = uif;

        initGUI();
    }

    public MultiFormatPane getMediaPane() {
        return mediaPane;
    }

    public NavigationPane getNavigationPane() {
        return navPane;
    }

    void setBaseDirectory(File base) {
        mediaPane.setBaseDirectory(base);
    }

    File getBaseDirectory() {
        return mediaPane.getBaseDirectory();
    }

    void setText(String text, String MIME) {
        mediaPane.showText(text, MIME);
    }

    String getText() {
        return ((JEditorPane)mediaPane.getMediaPane(mediaPane.TEXT)).getText();
    }

    void setFile(URL file) {
        if (file != null)
            setFiles(new URL[] { file });
    }

    void setFiles(URL[] files) {
        mediaPane.clear();
        navPane.setURLs(files);
        mediaPane.loadPage(files[0]);
    }

    URL getPage() {
        return mediaPane.getPage();
    }

    //------------------------------------------------------------------------------------

    private void initGUI() {
        setName("fp");
        setFocusable(false);

        setLayout(new BorderLayout());
        mediaPane = new MultiFormatPane(uif);
        navPane = new NavigationPane(uif, mediaPane);

        add(navPane, BorderLayout.NORTH);
        add(mediaPane, BorderLayout.CENTER);

        noteField = uif.createOutputField("fp.note");
        mediaPane.setNoteField(noteField);

        add(noteField, BorderLayout.SOUTH);
    }

    void clear() {
        mediaPane.clear();
    }

    String getMIMEType(URL url) {
        return TextPane.getMIMEType(url);
    }

    private JTextField noteField;
    private UIFactory uif;

    public int print (Graphics g, PageFormat pf, int pageIndex) {
        return mediaPane.print(g, pf, pageIndex);
    }

    private MultiFormatPane mediaPane;
    private NavigationPane navPane;
}
