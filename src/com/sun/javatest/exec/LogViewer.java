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
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.logging.FilteredLogModel;
import com.sun.javatest.logging.LogModel;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.logging.LoggerFactory;
import com.sun.javatest.tool.FileChooser;
import com.sun.javatest.tool.Preferences;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Random;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

class LogViewer extends ToolDialog {

    public LogViewer(WorkDirectory workDir, UIFactory uif, Component parent) {
        super(parent, uif, "logviewer");
        String fileName = workDir.getLogFileName();
        this.uif = uif;
        this.workDir = workDir;
        makeLogger(workDir);
        if (debug > 1 && log != null) {
            log.info("New LogViewer started");
        }
        model = new FilteredLogModel(workDir.getTestSuite().getObservedFile(fileName), fileName);
        model.setLogger(log);

        initGUI();

        model.addNewLoggerListener(new LogModel.LoggerListener() {
            public void onNewLogger(String logName) {
                if (debug > 1) {
                    String text = "Loggers : " + model.getLoggers().size();
                    loggerCounter.setText(text);
                }
            }


            public void onRemoveAllLoggers() {
                if (debug > 1) {
                    String text = "Loggers : " + model.getLoggers().size();
                    loggerCounter.setText(text);
                }
            }

        });

        model.addNewPageListener(new LogModel.NewPageListener() {
            public void onNewPage(final int from, final int to, final int page) {
                synchronized (thePane) {

                    if (debugPages > 1) {
                        System.out.println("isStable=" + model.isStableState() + " onNewPage from=" + from + " to=" + to + " page=" + page + " thePane.page=" + thePane.page);
                        String text = "Records : " + model.recordsRead();
                        counter.setText(text);
                        text = "Pages : " + model.pagesRead();
                        pageCounter.setText(text);
                    }

                    if (thePane.page == 0 && model.isStableState()) {
                        setPage(page);
                    } else if (thePane.page == page && model.isStableState()){
                        updatePage(from, to);
                    } else if (model.isStableState()) {
                        updateNavBtns();
                    }
                }
            }
        });

        model.addFilterChangedListener(new FilteredLogModel.FilterChangedListener() {
            public void onFilterChanged() {
                synchronized (thePane) {
                    thePane.page = 0;
                    thePane.fromRec = 0;
                }
            }
        });

        model.init();
        updateNavBtns();
        setVisible(true);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        LV_Scroller autoScroller = new LV_Scroller();
        autoScroller.start();
    }

    protected void windowClosingAction(AWTEvent e) {
        onClose(null);
    }

    private void makeLogger(final WorkDirectory workDir) {
        log = Logger.getLogger("LogViewer");
        try {
            log = workDir.getTestSuite().createLog(workDir, null, "LogViewer");
        } catch (TestSuite.DuplicateLogNameFault ex) {
            try {
                log = workDir.getTestSuite().getLog(workDir, "LogViewer");
            } catch (TestSuite.NoSuchLogFault exe) {
                exe.printStackTrace();
            }
        }
    }

    protected void initGUI() {

        working1 = uif.getI18NString("logviewer.working1");
        working2 = uif.getI18NString("logviewer.working2");

        JPanel body = new JPanel();
        addWindowToList();
        setI18NTitle("logviewer.title", windowCounter);

        GridBagConstraints gridBagConstraints;

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());

        Object[] items = {
            uif.getI18NString("logviewer.combobox.actions"),
            new JSeparator(),
            new FilterComboboxItem(uif.getI18NString("logviewer.combobox.selectall"), true),
            new FilterComboboxItem(uif.getI18NString("logviewer.combobox.clearall"), false),
            new JSeparator(),
            new FilterComboboxItem(uif.getI18NString("logviewer.combobox.select") + levelNames[0], levels[0]),
            new FilterComboboxItem(uif.getI18NString("logviewer.combobox.select") + levelNames[1], levels[1]),
            new FilterComboboxItem(uif.getI18NString("logviewer.combobox.select") + levelNames[2], levels[2]),
            new FilterComboboxItem(uif.getI18NString("logviewer.combobox.select") + levelNames[3], levels[3])
        };

        filterCombo = uif.createLiteralChoice("logviewer.combobox", items);   //new JComboBox(items);
        uif.setAccessibleName(filterCombo, filterCombo.getName());
        filterCombo.setRenderer(new CustomRenderer());
        filterTreeScroll = new JScrollPane();

        createFilterTree(filterTreeScroll);

        JLabel filterSubstringLbl = uif.createLabel("logviewer.label.find");
        final JTextField filterSubstring = uif.createInputField("logviewer.fitertext");
        uif.setAccessibleInfo(filterSubstring, filterSubstring.getName());
        filterSubstring.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.getFilter().setSubstring(filterSubstring.getText());
            }
        });

        autoScrollCheckBox = uif.createCheckBox("logviewer.autoscroll");
        autoScroll = Boolean.parseBoolean(prefs.getPreference(AUTOSCROLL_PREF, Boolean.toString(true)));
        autoScrollCheckBox.setSelected(autoScroll);
        autoScrollCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                autoScroll = (e.getStateChange() == ItemEvent.SELECTED);
                prefs.setPreference(AUTOSCROLL_PREF, Boolean.toString(autoScroll));
            }
        });

        JCheckBox wordWrapCheckBox = uif.createCheckBox("logviewer.wordwarp");
        wordWrap = Boolean.parseBoolean(prefs.getPreference(WORDWRAP_PREF, Boolean.toString(false)));
        wordWrapCheckBox.setSelected(wordWrap);
        wordWrapCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                wordWrap = (e.getStateChange() == ItemEvent.SELECTED);
                synchronized (thePane) {

                    // try to restore position (not exactly)  -
                    Point vp = scrollPane.getViewport().getViewPosition();
                    Dimension vs = scrollPane.getViewport().getViewSize();
                    final double magic = vp.getY()/vs.getHeight();

                    int p = thePane.page;
                    clearPane(0);
                    setPage(p);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            double newMagic = thePane.getDocument().getLength()*magic;
                            thePane.setCaretPosition((int)newMagic);
                        }
                    });
                }
                prefs.setPreference(WORDWRAP_PREF, Boolean.toString(wordWrap));
            }
        });

        scrollPane = new JScrollPane();
        thePane = new LogPane();


        thePane.setEditorKit(new LogEditorKit());
        JButton btnClose = uif.createButton("logviewer.button.close");
        JButton btnNew = uif.createButton("logviewer.button.open");
        btnSave = uif.createButton("logviewer.button.save");
        btnClear = uif.createButton("logviewer.button.clear");
        if (debug != 0 || debugPages != 0) {
            counter = new JLabel();
            pageCounter = new JLabel();
            loggerCounter = new JLabel();
            currPage = new JLabel();
        }
        naviPanel = new JPanel();
        processLabel = new JLabel();
        Font old = processLabel.getFont();
        processLabel.setFont(old.deriveFont(old.getSize()-1));
        JPanel naviBtnPanel = new JPanel();
        lblPageCounter = new JLabel();
        lblPageCounter.setVerticalAlignment(SwingConstants.TOP);
        btnFirst = uif.createButton("logviewer.firstpage");
        btnNext = uif.createButton("logviewer.nextpage");
        btnPrev = uif.createButton("logviewer.previouspage");
        btnLast = uif.createButton("logviewer.lastspage");

        body.setLayout(new GridBagLayout());

        thePane.setEditable(false);
        thePane.setPreferredSize(new Dimension(600, 400));
        scrollPane.setViewportView(thePane);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        filterPanel.add(filterCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        filterPanel.add(filterTreeScroll, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(5, 0, 0, 0);
        filterPanel.add(filterSubstringLbl, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        filterPanel.add(filterSubstring, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(11, 11, 5, 5);

        body.add(filterPanel, gridBagConstraints);

        initPane();

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(11, 5, 5, 11);
        body.add(scrollPane, gridBagConstraints);

        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onClose(evt);
            }
        });

        btnNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onNew(evt);
            }
        });

        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onSave(evt);
            }
        });

        btnClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onClear(evt);
            }
        });


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 11, 0, 0);
        body.add(autoScrollCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 11, 0, 0);
        body.add(wordWrapCheckBox, gridBagConstraints);

        if (debug != 0) {

            counter.setText("Records : ");
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(0, 5, 0, 5);
            body.add(counter, gridBagConstraints);

            pageCounter.setText("Pages : ");
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(0, 5, 0, 5);
            body.add(pageCounter, gridBagConstraints);

            loggerCounter.setText("Loggers : ");
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 5;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(0, 5, 0, 5);
            body.add(loggerCounter, gridBagConstraints);

            currPage.setText("Current page ");
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 6;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            body.add(currPage, gridBagConstraints);

            JButton btnGen = new JButton(new AbstractAction("Generate !") {
                public void actionPerformed(ActionEvent e) {
                    log.config("Config");
                }
            });
            JButton btnGen2 = new JButton(new AbstractAction("Generate 2") {
                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < 10000; i++) {
                        Random r = new Random();
                        Logger myLog = getLogger();
                        myLog.log(Level.FINE, "To be, or not to be: that is the question:\n"+
                                "Whether 'tis nobler in the mind to suffer\n"+
                                "The slings and arrows of outrageous fortune,\n"+
                                "Or to take arms against a sea of troubles,\n"+
                                "And by opposing end them? To die: to sleep;");
                        myLog.log(Level.INFO, "Random long " + r.nextLong());
                        myLog.log(Level.WARNING, "The World Health Organization says a cluster of bird flu cases in Indonesia may have been caused by human-to-human transmission. \nAn outbreak of bird flu that infected at least seven Indonesian family members earlier this month in north Sumatra was not a mutated version of the often deadly H5N1 form of the virus, World Health Organization spokesman Peter Cordingly told CNN.");
                        myLog.config("Config");
                    }
                }
            });

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 3;
            body.add(btnGen, gridBagConstraints);
            gridBagConstraints.gridy = 4;
            body.add(btnGen2, gridBagConstraints);
        }

        naviBtnPanel.setLayout(new GridLayout(1, 4, 10, 10));
        btnFirst.setEnabled(false);
        btnFirst.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                goFirst(evt);
            }
        });

        naviBtnPanel.add(btnFirst);

        btnPrev.setEnabled(false);
        btnPrev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                goPrev(evt);
            }
        });

        naviBtnPanel.add(btnPrev);

        btnNext.setEnabled(false);
        btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                goNext(evt);
            }
        });

        naviBtnPanel.add(btnNext);

        btnLast.setEnabled(false);
        btnLast.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                goLast(evt);
            }
        });

        naviBtnPanel.add(btnLast);

        naviPanel.setLayout(new BorderLayout());
        naviPanel.add(lblPageCounter, BorderLayout.WEST);
        naviPanel.add(naviBtnPanel, BorderLayout.EAST);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);
        body.add(processLabel, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new Insets(0, 5, 5, 11);
        body.add(naviPanel, gridBagConstraints);

        setButtons(new JButton[] {btnClear, btnSave, btnNew, btnClose}, btnClose);

        setBody(body);
    }

    private void createFilterTree(final JScrollPane filterTreeScroll) {
        treeRoot = new DefaultMutableTreeNode(new JCheckBox("Root"));
        filterTree = new JTree();
        LoggersTreeModel ltm = new LoggersTreeModel();
        filterTree.setModel(ltm);
        filterTree.setCellRenderer(new CheckBoxRenderer());
        filterTree.setCellEditor(new CheckBoxEditor(filterTree));
        filterTree.setRootVisible(false);
        filterTree.setEditable(true);
        filterTreeScroll.setViewportView(filterTree);
        if (filterComboBoxListener != null) {
            filterCombo.removeActionListener(filterComboBoxListener);
        }
        filterComboBoxListener = new FilterComboBoxListener(ltm);
        filterCombo.addActionListener(filterComboBoxListener);
        filterTree.setName("logviewer.logtree");
        uif.setAccessibleInfo(filterTree, filterTree.getName());
    }

    private Logger getLogger() {
        Random r = new Random();
        String lName = "log_" + r.nextInt(10);
        Logger ret = null;

        try {
            ret = workDir.getTestSuite().createLog(workDir, null, lName);
        } catch (TestSuite.DuplicateLogNameFault ex) {
            try {
                ret = workDir.getTestSuite().getLog(workDir, lName);
            } catch (TestSuite.NoSuchLogFault exe) {
                exe.printStackTrace();
            }
        }
        return ret;
    }

    private class EditorFiller extends Thread {
        EditorFiller(int from, int to, int pagenum) {
            super("editorFiller");
            this.from=from;
            this.to=to;
            this.pagenum=pagenum;
        }

        public void run() {
            if (noWindow) return;
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            ArrayList<LogModel.LiteLogRecord> records = model.getRecords();
            for (int i = from; i <= to && i < records.size() && i >= 0; i++) {
                if (noWindow) return;
                LogModel.LiteLogRecord rec = records.get(i);
                if (rec == null) {
                    continue;
                }
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                final String msg = model.getRecordMessage(rec);
                final int level = rec.severety;
                String hd = rec.getHeader(model.getLogname(rec.loggerID));
                final String header = debug > 1 ? "#" + (i+1) + " " + hd : hd;
                final String substr = model.getFilter().getSubstring();
                final int iter = i;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (noWindow) return;
                        synchronized (thePane) {
                            if (iter <= thePane.fromRec) return;
                            thePane.page = pagenum;
                            thePane.fromRec = iter;
                        }
                        try {
                            int pos = doc.getEndPosition().getOffset()-1;
                            if (thePane.getCaret() == null) {
                                thePane.setCaretPosition(0);
                            }
                            int oldPos = thePane.getCaretPosition();
                            doc.insertString(pos, header, getStyle(level));
                            doc.insertString(doc.getEndPosition().getOffset()-1, "\n", getStyle(level));

                            if (!"".equals(substr)) {
                                String up = header.toUpperCase();
                                int s = 0; int ss;
                                while ((ss = up.indexOf(substr, s)) >= 0) {
                                    doc.setCharacterAttributes(pos + ss, substr.length(), selected, false);
                                    s += substr.length();
                                }
                            }
                            pos = doc.getEndPosition().getOffset()-1;
                            doc.insertString(pos, msg, styleMsg);
                            doc.insertString(doc.getEndPosition().getOffset()-1, "\n", getStyle(level));

                            if (!"".equals(substr)) {
                                String up = msg.toUpperCase();
                                int s = 0; int ss;
                                while ((ss = up.indexOf(substr, s)) >= 0) {
                                    doc.setCharacterAttributes(pos + ss, substr.length(), selected, false);
                                    s += substr.length();
                                }
                            }
                            thePane.setCaretPosition(oldPos);
                            if (noWindow) return;
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    }
                } );
            }
        }


        private int pagenum;
        private int to;
        private int from;
    }


    private void setRecords(final int from, final int to, final int pagenum) {
        if (editorThread != null && editorThread.isAlive()) {
            editorThread.interrupt();
            try {
                editorThread.join();
            } catch (InterruptedException ex) {
                // it's ok
            }
        }
        editorThread = new EditorFiller(from, to, pagenum);
        editorThread.setPriority(Thread.MIN_PRIORITY);
        editorThread.start();
    }


    private void clearPane(final int from) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (doc != null) {
                    try {
                        doc.remove(0, doc.getEndPosition().getOffset()-1);
                        synchronized (thePane) {
                            thePane.fromRec = from-1;
                        }
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } );
    }


    private Style getStyle(int level) {
        if (level < Level.INFO.intValue()) {
            return styleOther;
        } else if (level < Level.WARNING.intValue()) {
            return styleInfo;
        } else if (level < Level.SEVERE.intValue()) {
            return styleWarning;
        } else {
            return styleSevere;
        }
    }


    private void goLast(ActionEvent evt) {
        // it's important to put it to the end of the queue !
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (thePane) {
                    setPage(model.pagesRead());
                }
            }
        });
    }

    private void goPrev(ActionEvent evt) {
        // it's important to put it to the end of the queue !
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (thePane) {
                    autoScrollCheckBox.setSelected(false);
                    autoScroll = false;
                    setPage(thePane.page-1);
                }
            }
        });
    }

    private void goNext(ActionEvent evt) {
        // it's important to put it to the end of the queue !
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (thePane) {
                    setPage(thePane.page+1);
                }
            }
        });
    }

    private void goFirst(ActionEvent evt) {
        // it's important to put it to the end of the queue !
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (thePane) {
                    autoScrollCheckBox.setSelected(false);
                    autoScroll = false;
                    setPage(1);
                }
            }
        });
    }

    private void onClose(ActionEvent evt) {
        dispose();
    }

    private void onNew(ActionEvent evt) {
        LogViewer lv = new LogViewer(workDir, uif, parent);
        Point newL = getLocation();
        newL.translate(20, 20);
        lv.setLocation(newL);
    }

    private void onSave(ActionEvent evt) {
        FileChooser fileChooser = new FileChooser(true);
        fileChooser.addChoosableExtension(".xml",
                uif.getI18NString("logviewer.save.ext.xml"));
        fileChooser.setDialogTitle(uif.getI18NString("logviewer.save.title"));
        if (fileChooser.showDialog(parent, uif.getI18NString("ce.save.btn")) != JFileChooser.APPROVE_OPTION) {
            // user has canceled or closed the chooser
            return;
        }
        File f = fileChooser.getSelectedFile();
        if (f != null) {

            if (!f.getName().toLowerCase().endsWith(".xml")) {
                f = new File(f.getPath() + ".xml");
            }

            LogViewerTools rm = new LogViewerTools(model, f, log, parent, uif);
            rm.go();
        }
    }

    private void onClear(ActionEvent evt) {
        if (uif.showYesNoDialog("logviewer.clearconfirm") == JOptionPane.YES_OPTION) {
            try {
                workDir.getTestSuite().eraseLog(workDir);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updatePage(int from, int to) {
        setRecords(from, to, thePane.page);
        updateNavBtns();
    }

    private void setPage(int pageNum) {
        int from, to;
        from = (pageNum - 1) * model.getPageSize() ;
        to = pageNum * model.getPageSize() - 1;

        synchronized (thePane) {
            thePane.page = pageNum;
        }

        clearPane(from);
        setRecords(from, to, pageNum);

        updateNavBtns();

        if (debugPages > 1) {
            System.out.println("setPage " + pageNum + " thePane.page=" + thePane.page );
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (thePane != null && currPage != null) {
                        currPage.setText("Current page " + thePane.page );
                    }
                }
            });
        }
    }

    private void updateNavBtns() {
        // can be called not from swing thread
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                if (noWindow) return;
                synchronized (thePane) {
                    if (model != null && btnFirst != null && btnLast != null &&
                            btnPrev != null && btnNext != null && lblPageCounter != null &&
                            naviPanel != null && btnSave != null) {
                        btnFirst.setEnabled(thePane.page > 1);
                        btnLast.setEnabled(thePane.page < model.pagesRead() && model.pagesRead() > 1);
                        btnPrev.setEnabled(thePane.page > 1);
                        btnNext.setEnabled(thePane.page < model.pagesRead() && model.pagesRead() > 1);
                        String pop = uif.getI18NString("logviewer.pageofpage",
                                new Object[] {thePane.page, model.pagesRead()});
                        lblPageCounter.setText(pop);
                        naviPanel.setVisible(btnFirst.isEnabled() ||
                                btnFirst.isEnabled() ||
                                btnLast.isEnabled() ||
                                btnNext.isEnabled());
                        btnSave.setEnabled(model.isStableState() && model.recordsRead() > 0);
                        btnClear.setEnabled(model.pagesRead() > 0);
                    }
                }
            }
        });
    }

    private void initPane() {
        doc = thePane.getStyledDocument();
        Style def = StyleContext.getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);

        styleMsg = doc.addStyle("msg_text", def);
        StyleConstants.setFontFamily(styleMsg, "Monospaced");

        styleWait  = doc.addStyle("blink", styleMsg);
        StyleConstants.setBold(styleWait, true);

        styleInfo = doc.addStyle("info_text", styleMsg);
        Color darkGreen = new Color(0, 180, 0);
        StyleConstants.setForeground(styleInfo, darkGreen);

        styleWarning = doc.addStyle("warning_text", styleMsg);
        StyleConstants.setForeground(styleWarning, new Color(200, 150, 0));

        styleSevere = doc.addStyle("severe_text", styleMsg);
        StyleConstants.setForeground(styleSevere, Color.RED);

        styleOther = doc.addStyle("other_text", styleMsg);
        StyleConstants.setForeground(styleOther, Color.BLUE);

        selected = doc.addStyle("selected", null);
        StyleConstants.setBackground(selected, Color.YELLOW);

    }


    private class LogEditorKit extends StyledEditorKit {


        public ViewFactory getViewFactory() {
            if (fact == null) {
                fact = new LogViewFactory();
            }
            return fact;
        }
        ViewFactory fact;

        class NoWrapLabelView extends LabelView {
            public NoWrapLabelView(Element elem) {
                super(elem);
            }

            public int getBreakWeight(int axis, float pos, float len) {
                return BadBreakWeight;
            }
        }

        private class LogViewFactory implements ViewFactory {
            public View create(Element elem) {
                String kind = elem.getName();
                if (kind != null) {
                    if (kind.equals(AbstractDocument.ContentElementName)) {
                        if (!wordWrap) {
                            return new NoWrapLabelView(elem)/* LabelView(elem)*/;
                        } else {
                            return new LabelView(elem);
                        }
                    } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                        return new ParagraphView(elem);
                    } else if (kind.equals(AbstractDocument.SectionElementName)) {
                        return new BoxView(elem, View.Y_AXIS);
                    } else if (kind.equals(StyleConstants.ComponentElementName)) {
                        return new ComponentView(elem);
                    } else if (kind.equals(StyleConstants.IconElementName)) {
                        return new IconView(elem);
                    }
                }

                // default to text display
                return new LabelView(elem);
            }
        }

    }

    private class LogPane extends JTextPane {
        int page;
        int fromRec;
        public LogPane() {
            super();
            setName("logviewer.viewerpane");
            uif.setAccessibleName(this, getName());

        }
    }

    private class CheckBoxEditor extends DefaultTreeCellEditor {
        CheckBoxEditor(JTree tree) {
            super(tree, new DefaultTreeCellRenderer());
        }

        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object o = node.getUserObject();
                if (o instanceof JCheckBox) {
                    JCheckBox cb = (JCheckBox) o;
                    cb.setBackground(tree.getBackground());
                    return cb;
                }
            }

            return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
        }

        // no expand/collaps !!
        // edit this to change
        public boolean isCellEditable(EventObject event) {
            return true;
        }

    }

    private class CheckBoxRenderer extends JCheckBox
            implements TreeCellRenderer {

        public CheckBoxRenderer() {
            super();
            setBackground(filterTree.getBackground());
        }

        public Component getTreeCellRendererComponent(JTree tree,
                Object value, boolean isSelected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {

            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object o = node.getUserObject();
                if (o instanceof JCheckBox) {
                    JCheckBox cb = (JCheckBox) o;
                    setText(cb.getText());
                    setSelected(cb.isSelected());
                    return this;
                }
            }

            return defRend.getTreeCellRendererComponent(tree,
                    value, isSelected, expanded, leaf, row, hasFocus);
        }
        DefaultTreeCellRenderer defRend = new DefaultTreeCellRenderer();
    }


    private class CustomRenderer extends JComponent
            implements ListCellRenderer {
        public Component getListCellRendererComponent(
                JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            if (!(value instanceof JSeparator) && !(value instanceof JCheckBox)) {
                DefaultListCellRenderer defRend = new DefaultListCellRenderer();
                return defRend.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
            } else if (value instanceof JSeparator) {
                return (JSeparator)value;
            } else if (value instanceof JCheckBox) {
                if (isSelected) {
                    ((JCheckBox)value).setBackground(list.getSelectionBackground());
                    ((JCheckBox)value).setForeground(list.getSelectionForeground());
                } else {
                    ((JCheckBox)value).setBackground(list.getBackground());
                    ((JCheckBox)value).setForeground(list.getForeground());
                }

                return (JCheckBox)value;
            }
            return this;
        }
    }

    private class LoggersTreeModel extends DefaultTreeModel {
        LoggersTreeModel() {
            super(treeRoot);
            model.removeNewLoggerListeners();
            model.addNewLoggerListener(new LogModel.LoggerListener() {
                public void onNewLogger(final String name) {
                    // it calls from Worker thread
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // check for the same

                            final PropagatedCheckBox ch = new PropagatedCheckBox(name);
                            ch.setSelected(true);
                            DefaultMutableTreeNode newLogger = new DefaultMutableTreeNode(ch, true);

                            for (int i = 0 ; i < levels.length; i++) {
                                final JCheckBox chh = new FilterTreeItem(name, levels[i].intValue(), levelNames[i]);
                                chh.setSelected(true);
                                final int level = levels[i].intValue();
                                chh.addItemListener( new ItemListener() {
                                    {
                                        box = chh;
                                        l = level;
                                        logName = name;
                                    }
                                    public void itemStateChanged(ItemEvent e) {
                                        model.getFilter().enableLogger(logName, l, box.isSelected());
                                    }
                                    JCheckBox box;
                                    int l;
                                    String logName;
                                });
                                ch.addChild(chh);
                                DefaultMutableTreeNode node = new DefaultMutableTreeNode(chh);
                                newLogger.add(node);
                            }

                            treeRoot.add(newLogger);
                            nodesWereInserted(treeRoot, new int [] {treeRoot.getIndex(newLogger)});
                            filterTree.expandPath(new TreePath(newLogger.getPath()));

                        }
                    });

                }

                public void onRemoveAllLoggers() {
                    createFilterTree(filterTreeScroll);
                }
            });
        }
    }

    private class PropagatedCheckBox extends JCheckBox {
        PropagatedCheckBox(final String name) {
            super(name);
            this.addItemListener( new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    for (JCheckBox aChildren : children) {
                        boolean s = isSelected();
                        if (aChildren.isSelected() != s) {
                            aChildren.setSelected(s);
                            if (debug > 1) {
                                System.out.println(aChildren.getText() + " selected " + s);
                            }
                        }
                    }
                    filterTree.repaint();
                }
            });
        }

        public void fireEvent() {
            fireItemStateChanged(
                    new ItemEvent(this,
                    ItemEvent.ITEM_STATE_CHANGED,
                    this,
                    this.isSelected() ?  ItemEvent.SELECTED : ItemEvent.DESELECTED));
        }

        public void addChild(JCheckBox ch) {
            children.add(ch);
        }

        private ArrayList<JCheckBox> children = new ArrayList<JCheckBox>();
    }


    private class FilterTreeItem extends JCheckBox {
        public FilterTreeItem(String logName, int level, String levelName) {
            super(levelName);
            this.logName = logName;
            this.level = level;
        }
        String logName;
        int level;
    }

    private class FilterComboboxItem {

        public FilterComboboxItem(String txt, Level l) {
            label = txt;
            level = l.intValue();
            kind = false;
        }

        public FilterComboboxItem(String txt, boolean s) {
            label = txt;
            select = s;
            kind = true;
        }


        public String toString() {
            return label;
        }

        // true - select all / unselect all
        // false - select particular level
        boolean kind;

        private String label;
        int level;
        boolean select;

    }


    private class FilterComboBoxListener implements ActionListener {
        public FilterComboBoxListener(LoggersTreeModel m) {
            model = m;
        }

        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox) e.getSource();
            Object o = cb.getSelectedItem();
            if (o instanceof FilterComboboxItem) {
                FilterComboboxItem fc = (FilterComboboxItem) o;
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
                if (debug > 1 && fc.kind) {
                    System.err.println(fc.select ? "Select all" : "Unselect all");
                }
                for (int i=0; i < root.getChildCount(); i++) {
                    DefaultMutableTreeNode fstLevelCh = (DefaultMutableTreeNode) root.getChildAt(i);
                    PropagatedCheckBox chLog = (PropagatedCheckBox) fstLevelCh.getUserObject();
                    if (fc.kind) {
                        boolean needToFire = chLog.isSelected() == fc.select;
                        chLog.setSelected(fc.select);
                        if (needToFire) {
                            chLog.fireEvent();
                        }
                    } else {
                        boolean allSelected = true;
                        for (int j=0; j < fstLevelCh.getChildCount(); j++) {
                            DefaultMutableTreeNode secondLevelCh = (DefaultMutableTreeNode) fstLevelCh.getChildAt(j);
                            FilterTreeItem chLev = (FilterTreeItem) secondLevelCh.getUserObject();
                            if (chLev.level == fc.level && !chLev.isSelected()) {
                                chLev.setSelected(true);
                                filterTree.repaint();
                            } else {
                                allSelected = allSelected && chLev.isSelected();
                            }
                        }
                        if (allSelected && !chLog.isSelected()) {
                            chLog.setSelected(true);
                            filterTree.repaint();
                        }
                    }
                }
                cb.setSelectedIndex(0);
            }
        }

        private LoggersTreeModel model;

    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            dispose();
        }
    }

    public void dispose() {
        removeWindowFromList();
        if (noWindow) {
            return;
        }
        noWindow = true;
        model.dispose();
        super.dispose();
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        thePane = null;
        doc = null;
        model = null;
        workDir = null;
        scrollPane = null;
        if (debug > 1 && log != null) {
            log.info("LogViewer closed");
        }
        log = null;
    }

    private void removeWindowFromList() {
        windowList.remove(windowCounter);
    }

    private void addWindowToList() {
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            if (!windowList.contains(i)) {
                windowCounter = i;
                windowList.add(i);
                return;
            }
        }
        windowCounter = 0;
    }

    private class LV_Scroller extends Thread {

        public LV_Scroller() {
            super("LV_scroller");
        }

        public void run() {
            try {
                while (true) {
                    if (noWindow) return;
                    boolean reset = false;
                    if (model.isStableState()) {
                        if (autoScroll) {
                            reset = thePane.page != model.pagesRead();
                        } else {
                            reset = thePane.page > model.pagesRead() || thePane.page == 0;
                        }
                    }
                    if (reset) {
                        setPage(model.pagesRead());
                        setBusy(!model.isStableState());
                        sleep(500);
                        continue;
                    }
                    if (model.isStableState()) {
                        setBusy(false);
                        if (autoScroll) {
                            synchronized (thePane) {
                                final JScrollBar sb = scrollPane.getVerticalScrollBar();
                                if (sb != null) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            sb.setValue(sb.getMaximum());
                                        }

                                    });
                                }
                            }
                        } else {
                            if (thePane.getCaret() == null)
                                thePane.setCaretPosition(0);
                        }
                    } else {
                        setBusy(true);
                    }
                    sleep(500);
                }
            } catch (InterruptedException ex) {
                // ok
            }
        }

        private void setBusy(boolean b) {
            if (!b) {
                processLabel.setText("");
            } else {
                String oldText = processLabel.getText();
                if ("".equals(oldText) || working1.equals(oldText)) {
                    processLabel.setText(working2);
                } else {
                    processLabel.setText(working1);
                }
            }
        }
    }

    private UIFactory uif;

    private final Level  [] levels = { Level.SEVERE, Level.WARNING, Level.INFO, Level.FINE } ;
    private final String [] levelNames =
    {LoggerFactory.getLocalizedLevelName(Level.SEVERE),
     LoggerFactory.getLocalizedLevelName(Level.WARNING),
     LoggerFactory.getLocalizedLevelName(Level.INFO),
     LoggerFactory.getLocalizedLevelName(Level.FINE) } ;

    private DefaultMutableTreeNode treeRoot;
    private JComboBox filterCombo;
    private JTree filterTree ;

    private boolean noWindow = false;
    private JLabel lblPageCounter;
    private JButton btnFirst;
    private JButton btnLast;
    private JButton btnNext;
    private JButton btnPrev;
    private JCheckBox autoScrollCheckBox;
    private JLabel counter;
    private JLabel currPage;
    private JButton btnSave;
    private JButton btnClear;
    private JPanel naviPanel;
    private JScrollPane scrollPane;
    private LogPane thePane;
    private JLabel loggerCounter;
    private JLabel pageCounter;
    private JLabel processLabel;
    private WorkDirectory workDir;
    private Thread editorThread;

    private StyledDocument doc;
    private Style styleMsg, styleInfo, styleWarning, styleSevere, styleOther, styleWait, selected;
    private FilteredLogModel model;
    private Logger log;

    private final int debug = 0;
    private final int debugPages = 0;
    private boolean autoScroll = true;

    private FilterComboBoxListener filterComboBoxListener;

    private JScrollPane filterTreeScroll;

    private boolean wordWrap = false;

    private Preferences prefs = Preferences.access();
    private static final String AUTOSCROLL_PREF = "logviewer.autoScroll";
    private static final String WORDWRAP_PREF = "logviewer.wordWrap";
    private int windowCounter = 0;
    private static HashSet<Integer> windowList = new HashSet<Integer>();
    private String working1;
    private String working2;

}
