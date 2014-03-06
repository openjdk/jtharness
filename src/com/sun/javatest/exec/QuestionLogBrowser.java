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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.EventQueue;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.interview.WizPrint;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.tool.FileChooser;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;

class QuestionLogBrowser extends ToolDialog
{
    QuestionLogBrowser(JComponent parent, ExecModel model, UIFactory uif) {
        super(parent, uif, "qlb");

        if (model == null)
            throw new NullPointerException();

        this.model = model;

        params = model.getInterviewParameters();
        if (params == null)
            throw new NullPointerException();

        listener = new Listener();
    }

    protected void initGUI() {
        setHelp("quLog.window.csh");

        JMenuBar mb = uif.createMenuBar("qlb");
        String[] fileActions = { SAVE_AS, PRINT_SETUP, PRINT };
        JMenu fileMenu = uif.createMenu("qlb.file", fileActions, listener);
        mb.add(fileMenu);
        setJMenuBar(mb);

        body = new MultiFormatPane(uif);
        body.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        body.setName("qlb.body");
        uif.setAccessibleName(body, "qlb.body");
        uif.setToolTip(body, "qlb");

        int dpi = uif.getDotsPerInch();
        body.setPreferredSize(new Dimension(6 * dpi, 9 * dpi));
        setBody(body);

        JButton helpBtn = uif.createHelpButton("qlb.help", "quLog.window.csh");
        JButton closeBtn = uif.createCloseButton("qlb.close");
        setButtons(new JButton[] { helpBtn, closeBtn }, closeBtn);

        setComponentListener(listener);
    }

    private void updateContent() {
        if (body == null)
            initGUI();

        final JDialog d = uif.createWaitDialog("qlb.wait", parent);

        File f = params.getFile();
        if (f == null)
            setI18NTitle("qlb.title");
        else
            setI18NTitle("qlb.titleWithFile", f);

        final Thread t = new Thread() {
            public void run() {
                try {
                    WizPrint wp = new WizPrint(params);
                    wp.setShowResponses(true);
                    wp.setShowResponseTypes(false);
                    wp.setShowTags(true);
                    StringWriter out = new StringWriter();
                    wp.write(out);
                    // wp will automatically flush and close the stream

                    finishContentUpdate(d, out);
                }
                catch (IOException e) {
                    // should not happen while writing to StringWriter
                }
            }
        };  // thread

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // show dialog if still processing
                if (t.isAlive()) {
                    d.show();
                }
            }
        };

        // show wait dialog if operation is still running after
        // WAIT_DIALOG_DELAY
        Timer timer = new Timer(WAIT_DIALOG_DELAY, al);
        timer.setRepeats(false);
        timer.start();

        // do it!
        t.start();
    }

    private void finishContentUpdate(final JDialog waitDialog,
                                     final StringWriter out) {
        // done generating report, switch back to GUI thread
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                waitDialog.hide();

                TextPane textPane = (TextPane)body.getMediaPane(body.TEXT);
                textPane.showText(out.toString(), "text/html");
                textPane.getCaret().setDot(0);
            }
        });
    }

    private void doSaveAs() {
        if (fileChooser == null) {
            fileChooser = new FileChooser(true);
            fileChooser.addChoosableExtension(".html",
                                              uif.getI18NString("qlb.htmlFiles"));
        }

        fileChooser.setDialogTitle(uif.getI18NString("qlb.save.title"));

        File file = null;

        while (file == null) {
            int rc = fileChooser.showDialog(parent, uif.getI18NString("qlb.save.btn"));
            if (rc != JFileChooser.APPROVE_OPTION)
                // user has canceled or closed the chooser
                return;

            file = fileChooser.getSelectedFile();

            // if file exists, leave well enough alone;
            // otherwise, make sure it ends with .html
            if (!file.exists()) {
                String path = file.getPath();
                if (!path.endsWith(".html"))
                    file = new File(path + ".html");
            }

            // if file exists, make sure user wants to overwrite it
            if (file.exists()) {
                rc = uif.showYesNoCancelDialog("qlb.save.warn", file);
                switch (rc) {
                case JOptionPane.YES_OPTION:
                    break;  // use this file

                case JOptionPane.NO_OPTION:
                    fileChooser.setSelectedFile(null);
                    file = null;
                    continue;  // choose another file

                default:
                    return;  // exit without saving
                }
            }
        }

        try {
            Writer out = new BufferedWriter(new FileWriter(file));
            out.write(((JEditorPane)body.getMediaPane(body.TEXT)).getText());
            out.close();
        }
        catch (IOException e) {
            if (!file.canWrite())
                uif.showError("qlb.save.cantWriteFile", file);
            else if (e instanceof FileNotFoundException)
                uif.showError("qlb.save.cantFindFile", file);
            else
                uif.showError("qlb.save.error", new Object[] { file, e } );
        }
    }

    private void doPrintSetup() {
        model.printSetup();
    }

    private void doPrint() {
            model.print(body);
    }

    private class Listener
        extends ComponentAdapter
        implements ActionListener, Interview.Observer
    {
        // ActionListener
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(SAVE_AS))
                doSaveAs();
            else if (cmd.equals(PRINT_SETUP))
                doPrintSetup();
            else if (cmd.equals(PRINT))
                doPrint();
        }

        // ComponentListener
        public void componentShown(ComponentEvent e) {
            params.addObserver(this);
            updateContent();
        }

        public void componentHidden(ComponentEvent e) {
            params.removeObserver(this);
        }

        // Interview.Observer
        public void currentQuestionChanged(Question q) {
        }

        public void pathUpdated() {
            updateContent();
        }

    };

    private ExecModel model;
    private InterviewParameters params;

    private MultiFormatPane body;

    private FileChooser fileChooser;
    private Listener listener;

    private static final String SAVE_AS = "SaveAs";
    private static final String PRINT_SETUP = "PrintSetup";
    private static final String PRINT = "Print";
    private static final int WAIT_DIALOG_DELAY = 3000;      // 3 second delay
}
