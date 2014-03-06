/*
 * $Id$
 *
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.mrep;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.report.HTMLWriterEx;
import com.sun.javatest.util.I18NResourceBundle;

class BrowserPane extends JPanel {
    /**
     * This exception is used to report problems that arise when using
     * the FilesPane.
     */
    static class Fault extends Exception {
        Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }
    }

    BrowserPane(UIFactory uif) {
        this.uif = uif;

        history = new History();

        initGUI();
    }

    void setBaseDirectory(File base) {
        baseDir = base;
    }

    File getBaseDirectory() {
        return baseDir;
    }

    void setFile(URL file) {
        if (file != null)
            setFiles(new URL[] { file });
    }

    boolean isEmpty() {
        return currURL == null;
    }

    void setPlainText(String text) {
        textArea.setContentType("text/html");
        textArea.setText(text);
        textHomePage = text;
    }

    void setPlainTextHomePage(String text) {
        textHomePage = text;
    }

    void setFiles(URL[] files) {
        clear();

        for (int i = 0; i < files.length; i++) {
            // set the first file as home and show it
            if (i == 0) {
//              homeURL = files[i];
                homeAction.setEnabled(true);
                loadPage(files[i]);
            }
            updateCombo(files[i]);
        }

        // updateCombo leaves the last inserted selected, which is
        // useful elsewhere, but not here, we reset the selection
        if (model.getSize() > 0)
            selectBox.setSelectedIndex(0);
    }

    URL getPage() {
        return textArea.getPage();
    }

    //------------------------------------------------------------------------------------

    private void initGUI() {
        setName("fp");
        setFocusable(false);

        htmlKit = new HTMLEditorKit();
        initActions();

        setLayout(new BorderLayout());
        initHead();
        initBody();

        add(head, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        noteField = uif.createOutputField("fp.note");
        add(noteField, BorderLayout.SOUTH);
    }

    private void initActions() {
        homeAction = new ToolAction(uif, "fp.home", true) {
            public void actionPerformed(ActionEvent e) {
                if (homeURL == null)
                    if (textHomePage != null) {
                        currURL = null;
                        updateCombo(null);
                        textArea.setContentType("text/html");
                        textArea.setText(textHomePage);
                    } else
                        textArea.setDocument(new HTMLDocument());
                else
                    loadPage(homeURL);
            }
        };

        backAction = new ToolAction(uif, "fp.back", true) {
            public void actionPerformed(ActionEvent e) {
                URL url = history.prev();
                if (url != null)
                    loadPage(url);
            }
        };

        forwardAction = new ToolAction(uif, "fp.forward", true) {
            public void actionPerformed(ActionEvent e) {
                URL url = history.next();
                if (url != null)
                    loadPage(url);
            }
        };
    }

    private void initHead() {
        head = uif.createPanel("fp.head", false);
        head.setLayout(new GridBagLayout());
        head.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0,0,0,5);

        JLabel fileLbl = uif.createLabel("fp.file", true);
        head.add(fileLbl, c);

        selectBox = uif.createChoice("fp.choice", fileLbl);
        selectBox.setRenderer(new Renderer());
        selectBox.setModel(createModel());
        selectBox.addItemListener(listener);
        selectBox.setMaximumRowCount(MAX_ROWS_DISPLAY);
        selectBox.setUI(new BasicComboBoxUI() {
            // wrap the content with a scrolling interface
            // would be nice if Swing did this for us
            protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(selectBox) {
                    protected JScrollPane createScroller() {
                        return new JScrollPane(list,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    }
                };
                return popup;
            }
            }   // class
            );
        uif.setAccessibleName(selectBox, "fp.choice");  // override default a11y name

        c.gridx = 1;
        c.weightx = 2.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        head.add(selectBox, c);

        Action[] actions = { backAction, forwardAction, null, homeAction };

        toolBar = uif.createToolBar("fp.toolbar", actions );
        toolBar.setFloatable(false);

        c.weightx = 0;
        c.gridx = 2;
        c.insets.left = 5;

        head.add(toolBar, c);
        backAction.setEnabled(history.hasPrev());
        forwardAction.setEnabled(history.hasNext());

    }

    private void initBody() {
        body = uif.createPanel("fp.body", false);
        body.setLayout(new BorderLayout());

        textArea = new JEditorPane();
        textArea.setName("text");
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textArea.addHyperlinkListener(listener);
        uif.setAccessibleInfo(textArea, "fp");
        body.add(uif.createScrollPane(textArea,
                                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                 BorderLayout.CENTER);
    }

    //------------------------------------------------------------------------------------

    private void clear() {
        if (model != null)
            model.removeAllElements();

        history.clear();
        backAction.setEnabled(false);
        forwardAction.setEnabled(false);
        homeAction.setEnabled(false);

        homeURL = null;
        currURL = null;
        textArea.setDocument(new HTMLDocument());
    }

    private void loadPage(URL url) {
        // avoid recursive callbacks from updating combo
        // URL.equals can result in a big performance hit
        if (currURL!= null && url.toString().equals(currURL.toString()))
            return;

        currURL = url;

        String protocol = url.getProtocol();
        File file = new File(url.getFile());
        if (protocol.equals("file") && file.isDirectory()) {
            String list = listLocalDirectory(file);
            HTMLDocument htmlDoc = (HTMLDocument) (htmlKit.createDefaultDocument());
            textArea.setDocument(htmlDoc);
            htmlDoc.setBase(url);
            textArea.setContentType("text/html");
            textArea.setText(list);
        }
        else if (protocol.equals("file")
                 && !url.getFile().endsWith(".htm")
                 && !url.getFile().endsWith(".html")) {
            textArea.setContentType("text/plain");
            FileReader r = null;
            try {
                r = new FileReader(file);
                BufferedReader br = new BufferedReader(r);
                textArea.read(br, url);
                br.close();
            }
            catch (IOException e) {
                uif.showError("fp.load.error", new Object[] { url, e });
            }
            finally {
                try {
                    if (r != null) r.close();
                }
                catch(IOException e2) {
                    // ignore since this is just from closing the file
                }
            }

        }
        else {
            try {
                URL loaded = textArea.getPage();
                // this next stuff is just to avoid some screen flash if a new doc
                // is being read
                if (loaded == null || !loaded.sameFile(url)) {
                    HTMLDocument htmlDoc = (HTMLDocument) (htmlKit.createDefaultDocument());
                    textArea.setDocument(htmlDoc);
                }
                textArea.setPage(url);
            }
            catch (IOException e) {
                uif.showError("fp.load.error", new Object[] { url, e });
            }
        }

        history.add(url);
        backAction.setEnabled(history.hasPrev());
        forwardAction.setEnabled(history.hasNext());
        updateCombo(url);
    }

    private String listLocalDirectory(File dir) {
        if (!dir.isAbsolute())
            dir = dir.getAbsoluteFile();

        String displayPath = dir.getPath();
        // if contains base dir, only show path relative to baseDir
        if (baseDir != null) {
            String p = baseDir.getParent();
            if (p != null
                && displayPath.startsWith(p) &&
                (displayPath.length() > p.length())) {
                displayPath = displayPath.substring(p.length());
                // in case of Unix
                if (displayPath.startsWith(File.separator)) {
                    displayPath = displayPath.substring(1);
                }
            }
        }

        String[] filelist = dir.list();
        StringWriter sw = new StringWriter();
        try {
            HTMLWriterEx out = new HTMLWriterEx(sw, uif.getI18NResourceBundle());

            out.startTag(HTMLWriterEx.HTML);
            out.startTag(HTMLWriterEx.HEAD);
            out.writeContentMeta();
            out.startTag(HTMLWriterEx.TITLE);
            out.write(displayPath);
            out.endTag(HTMLWriterEx.TITLE);
            out.endTag(HTMLWriterEx.HEAD);
            out.startTag(HTMLWriterEx.BODY);
            out.writeStyleAttr("font-family: SansSerif; font-size: 12pt");
            out.startTag(HTMLWriterEx.H3);
            out.writeI18N("fp.head", displayPath);
            out.endTag(HTMLWriterEx.H3);
            out.startTag(HTMLWriterEx.UL);
            out.writeStyleAttr("margin-left:0");

            File parent = dir.getParentFile();
            if (parent != null) {
                out.startTag(HTMLWriterEx.LI);
                out.startTag(HTMLWriterEx.OBJECT);
                out.writeAttr(HTMLWriterEx.CLASSID, "com.sun.javatest.tool.IconLabel");
                out.writeParam("type", "up");
                out.endTag(HTMLWriterEx.OBJECT);
                out.writeEntity("&nbsp;");
                try {
                    out.startTag(HTMLWriterEx.A);
                    out.writeAttr(HTMLWriterEx.HREF, parent.toURL().toString());
                    out.writeI18N("fp.parent");
                    out.endTag(HTMLWriterEx.A);
                }
                catch (MalformedURLException e) {
                    out.writeI18N("fp.parent");
                }
            }

            for (int i = 0; i < filelist.length; i++) {
                File file = new File(dir, filelist[i]);
                out.startTag(HTMLWriterEx.LI);
                out.startTag(HTMLWriterEx.OBJECT);
                out.writeAttr(HTMLWriterEx.CLASSID, "com.sun.javatest.tool.IconLabel");
                out.writeParam("type", (file.isDirectory() ? "folder" : "file"));
                out.endTag(HTMLWriterEx.OBJECT);
                out.writeEntity("&nbsp;");
                try {
                    out.writeLink(file.toURL(), file.getName());
                }
                catch (MalformedURLException e) {
                    out.write(file.getName());
                }
            }

            out.endTag(HTMLWriterEx.UL);
            out.endTag(HTMLWriterEx.BODY);
            out.endTag(HTMLWriterEx.HTML);
            out.close();
        }
        catch (IOException e) {
            // should not happen, writing to StringWriter
        }

        return sw.toString();
    }

    private void updateCombo(URL s) {
        // check if the new element exists in the combo box...
        if (model.getIndexOf(s) < 0)
            model.addElement(s);

        URL item = (URL) selectBox.getSelectedItem();
        // check if the new element is already selected.
        // URL.equals can result in a big performance hit
        if (s != null && !item.toString().equals(s.toString()))
            selectBox.setSelectedItem(s);
    }

    private DefaultComboBoxModel createModel() {
        if (model == null)
            model = new DefaultComboBoxModel();
        return model;
    }

    private File baseDir;
    private URL homeURL;
    private File[] files;
    private String textHomePage;

    private JButton homeBtn;
    private JButton backBtn;
    private JButton forwardBtn;

    private JComboBox selectBox;
    private JPanel head;
    private JPanel body;
    private JTextField noteField;

    private HTMLEditorKit htmlKit;
    private JEditorPane textArea;
    private URL currURL;

    private History history;

    private DefaultComboBoxModel model;
    private Listener listener = new Listener();
    private JToolBar toolBar;
    private UIFactory uif;

    private Action homeAction;
    private Action backAction;
    private Action forwardAction;

    private static final int MAX_ROWS_DISPLAY = 20;

    static protected boolean debug = Boolean.getBoolean("debug." + BrowserPane.class.getName());

    //------------------------------------------------------------------------------------

    private class History {
        boolean hasPrev() {
            return (index > 0);
        }

        URL prev() {
            if (index == 0)
                return null;

            return (URL) (entries.elementAt(--index));
        }

        boolean hasNext() {
            return (index < entries.size() - 1);
        }

        URL next() {
            if (index == entries.size() - 1)
                return null;

            return (URL) (entries.elementAt(++index));
        }

        void add(URL u) {
            if (u == null)
                throw new NullPointerException();

            // if there is a current entry, and it matches the one to be added, we're done
            if (index >= 0 && index < entries.size() && entries.elementAt(index).equals(u))
                return;

            // if current entry not the last one, truncate to the current entry
            if (index < entries.size() - 1)
                entries.setSize(index + 1);

            // finally, add new entry
            entries.addElement(u);
            index = entries.size() - 1;
        }

        void clear() {
            entries.setSize(0);
            index = -1;
        }

        private Vector entries = new Vector();
        private int index;
    }

    private class Listener implements HyperlinkListener, ItemListener {
        public void hyperlinkUpdate(HyperlinkEvent e) {
            HyperlinkEvent.EventType et = e.getEventType();
            if (et == HyperlinkEvent.EventType.ACTIVATED) {
                if (e instanceof HTMLFrameHyperlinkEvent) {
                    HTMLDocument doc = (HTMLDocument)
                        ((JEditorPane) e.getSource()).getDocument();
                    doc.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) e);
                }
                else
                    loadPage(e.getURL());
            }
            else if (et == HyperlinkEvent.EventType.ENTERED) {
                URL u = e.getURL();
                if (u != null) {
                    textArea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    noteField.setText(u.toString());
                }
            }
            else if (et == HyperlinkEvent.EventType.EXITED) {
                textArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                noteField.setText("");
            }
        }

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                URL url = (URL) e.getItem();
                loadPage(url);
            }
        }
    }

    //------------------------------------------------------------------------------------

    private class Renderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object o, int index, boolean isSelected, boolean cellHasFocus) {
            String name = null;
            if (o instanceof URL) {
                URL url = (URL) o;

                // if not file URL
                if (!url.getProtocol().equals("file")) {
                    name = url.toString();
                }
                else {
                    // if file URL, remove the "file:" prefix
                    name = extractPrefix(url.toString(), "file:");
                    String baseName = null;
                    name = new File(name).getAbsolutePath();
                    if (baseDir != null && baseDir.getParentFile() != null) {
                        baseName = baseDir.getParentFile().getAbsolutePath();
                    }
                    // if contains base dir, only show file name
                    if (baseName != null &&
                        name.startsWith(baseName) &&
                        (name.length() > baseName.length())) {
                        name = name.substring(baseName.length() );
                        // in case of Unix
                        if (name.startsWith(File.separator)) {
                            name = name.substring(1);
                        }
                    }
                }
            } else if (o != null) {
                name = String.valueOf(o);
                return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
            }

            return super.getListCellRendererComponent(list, o, index, isSelected, cellHasFocus);
        }

        private String extractPrefix(String origStr, String target) {
            return (!origStr.startsWith(target)) ? origStr : origStr.substring(target.length());
        }
    }
}
