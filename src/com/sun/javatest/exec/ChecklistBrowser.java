/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import com.sun.interview.Checklist;
import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.tool.FileChooser;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;
import java.awt.Dimension;
import javax.swing.BorderFactory;

class ChecklistBrowser extends ToolDialog
{
    ChecklistBrowser(JComponent parent, ExecModel model, UIFactory uif) {
        super(parent, uif, "cb");

        if (model == null)
            throw new NullPointerException();

        this.model = model;

        params = model.getInterviewParameters();
        if (params == null)
            throw new NullPointerException();

        listener = new Listener();
    }

    protected void initGUI() {
        setHelp("checklist.window.csh");

        JMenuBar mb = uif.createMenuBar("cb");
        String[] fileActions = { SAVE_AS, PRINT_SETUP, PRINT };
        JMenu fileMenu = uif.createMenu("cb.file", fileActions, listener);
        mb.add(fileMenu);
        setJMenuBar(mb);

        body = new MultiFormatPane(uif);
        body.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        uif.setAccessibleName(body, "cb");
        uif.setToolTip(body, "cb");
        int dpi = uif.getDotsPerInch();
        body.setPreferredSize(new Dimension(6 * dpi, 8 * dpi));
        setBody(body);

        JButton helpBtn = uif.createHelpButton("cb.help", "checklist.window.csh");
        JButton closeBtn = uif.createCloseButton("cb.close");
        setButtons(new JButton[] { helpBtn, closeBtn }, closeBtn);

        setComponentListener(listener);
    }

    private void updateContent() {
        if (body == null)
            initGUI();

        File f = params.getFile();
        if (f == null)
            setI18NTitle("cb.title");
        else
            setI18NTitle("cb.titleWithFile", f);

        try {
            Checklist c = params.createChecklist();
            StringWriter out = new StringWriter();
            writeChecklist(c, out);
        }
        catch (IOException e) {
            // should not happen while writing to StringWriter
        }

    }

    private void writeChecklist(Checklist c, Writer out) throws IOException {
        out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n");
        out.write("<html><body>\n");

        TestEnvironment env = params.getEnv();
        if (env != null) {
            // very improbable to have a checklist with no env!
            out.write("<h1>");
            out.write(escape(uif.getI18NString("cb.html.title", env.getName())));
            out.write("</h1>\n");
        }

        if (c.isEmpty())
            out.write(escape(uif.getI18NString("cb.html.noEntries")));
        else {
            out.write(escape(uif.getI18NString("cb.html.intro")));
            String[] names = c.getSectionNames();
            for (int i = 0; i < names.length; i++) {
                writeSection(c, names[i], out);
            }
        }

        out.write("</body></html>");
        out.close();

        TextPane pane = (TextPane)body.getMediaPane(body.TEXT);
        pane.showText(out.toString(), "text/html");
        pane.getCaret().setDot(0);
    }

    private void writeSection(Checklist c, String name, Writer out) throws IOException {
        out.write("<h2>");
        out.write(escape(name));
        out.write("</h2>\n");

        out.write("<ul>");
        String[] msgs = c.getSectionMessages(name);
        for (int i = 0; i < msgs.length; i++) {
            out.write("<li>");
            out.write(escape(msgs[i]));
            out.write("\n");
        }
        out.write("</ul>\n");
    }

    private String escape(String s) {
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
            case '<': case '>': case '&':
                StringBuffer sb = new StringBuffer(s.length()*2);
                for (int j = 0; j < s.length(); j++) {
                    char c = s.charAt(j);
                    switch (c) {
                    case '<': sb.append("&lt;"); break;
                    case '>': sb.append("&gt;"); break;
                    case '&': sb.append("&amp;"); break;
                    default: sb.append(c);
                    }
                }
                return sb.toString();
            }
        }
        return s;
    }

    private void doSaveAs() {
        if (fileChooser == null) {
            fileChooser = new FileChooser(true);
            fileChooser.addChoosableExtension(".html",
                                              uif.getI18NString("cb.htmlFiles"));
        }

        fileChooser.setDialogTitle(uif.getI18NString("cb.save.title"));

        File file = null;

        while (file == null) {
            int rc = fileChooser.showDialog(parent, uif.getI18NString("cb.save.btn"));
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
                rc = uif.showYesNoCancelDialog("cb.save.warn");
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

        FileWriter fw = null;
        Writer out = null;
        try {
            fw = new FileWriter(file);
            out = new BufferedWriter(fw);
            TextPane pane = (TextPane)body.getMediaPane(body.TEXT);
            out.write(pane.getText());
            out.close();
        }
        catch (IOException e) {
            if (!file.canWrite())
                uif.showError("cb.save.cantWriteFile", file);
            else if (e instanceof FileNotFoundException)
                uif.showError("cb.save.cantFindFile", file);
            else
                uif.showError("cb.save.error", new Object[] { file, e } );
        }
        finally {
            // attempt to close buffered writer first
            // followed by the underlying writer for leak prevention
            if (out != null){
                try { out.close(); } catch (IOException e) { }
            }

            if (fw != null){
                try { fw.close(); } catch (IOException e) { }
            }
        }   // finally
    }

    private void doPrintSetup() {
        model.printSetup();
    }

    private void doPrint() {
        model.print(body);
    }

    private static final String SAVE_AS = "SaveAs";
    private static final String PRINT_SETUP = "PrintSetup";
    private static final String PRINT = "Print";

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
}
