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

import java.awt.AWTEvent;
import java.io.File;

import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;

class ReportBrowser extends ToolDialog
{
    public ReportBrowser(JComponent parent, ExecModel model, UIFactory uif, ReportHandler rh) {
        super(parent, uif, "rb");
        this.model = model;
        reportHandler = rh;
    }

    void show(File f) {
        try {
            setSelectedFile(f);
            setVisible(true);
        }
        catch (IOException e) {
            uif.showError("rb.load.error", new Object[] { f, e });
        }
    }

    void setSelectedFile(File f) throws IOException
    {
        if (f == null)
            throw new NullPointerException();

        reportFile = f;

        setI18NTitle("rb.title", f);

        if (fp == null)
            initGUI();

        fp.setBaseDirectory(f);
        fp.setFile(f.toURL());
    }

    File getSelectedFile() {
        return reportFile;
    }

    //------------------------------------------------------------------------------


    protected void initGUI() {
        setHelp("report.reportBrowser.csh");

        fp = new FilesPane(uif);

        int dpi = uif.getDotsPerInch();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int prefWidth = Math.min(7 * dpi, screenSize.width * 9 / 10);
        int prefHeight = Math.min(9 * dpi, screenSize.height * 9 / 10);
        fp.setPreferredSize(new Dimension(prefWidth, prefHeight));
        setBody(fp);

        JMenuBar menuBar = uif.createMenuBar("rb.menubar");
        menuBar.add(createFileMenu(reportHandler));
        menuBar.add(uif.createHorizontalGlue("rb.pad"));
        menuBar.add(createHelpMenu());
        setJMenuBar(menuBar);

        if (reportFile == null)
            setI18NTitle("rb.title");
        else {
            setI18NTitle("rb.title_file", reportFile);
            fp.setBaseDirectory(reportFile);
            try {
                fp.setFile(reportFile.toURL());
            }
            catch (MalformedURLException e) {
                // ignore
                //e.printStackTrace();
            }
        }
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }


    private JMenu createFileMenu(ReportHandler reportHandler) {
        Action newReport = reportHandler.getNewReportAction();
        Action openReport = reportHandler.getOpenReportAction();

        Action printSetup = new ToolAction(uif, "rb.file.printSetup") {
                public void actionPerformed(ActionEvent e) {
                    model.printSetup();
                }
            };

        Action print = new ToolAction(uif, "rb.file.print") {
                public void actionPerformed(ActionEvent e) {
                    model.print(fp);
                }
            };

        Action close = new ToolAction(uif, "rb.file.close") {
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    cleanup();
                }
            };

        Action[] actions = { newReport, openReport, null, printSetup, print, null, close };
        JMenu fileMenu = uif.createMenu("rb.file", actions );
        return fileMenu;
    }

    private JMenu createHelpMenu() {
        JMenu helpMenu = uif.createMenu("rb.help");
        JMenuItem rbItem = uif.createHelpMenuItem("rb.help.rb", "report.reportBrowser.csh");
        helpMenu.add(rbItem);
        return helpMenu;
    }

    protected void windowClosingAction(AWTEvent e) {
        setVisible(false);
        cleanup();
    }

    private void cleanup() {
        fp.clear();
    }

    private ExecModel model;
    private ReportHandler reportHandler;
    private FilesPane fp;
    private File reportFile;

}
