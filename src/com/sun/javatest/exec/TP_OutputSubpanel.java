/*
 * $Id$
 *
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.StringWriter;
import javax.help.CSH;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ListSelectionModel;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.sun.javatest.JavaTestError;
import com.sun.javatest.Status;
import com.sun.javatest.TestResult;
import com.sun.javatest.tool.IconFactory;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.report.HTMLWriterEx;
import com.sun.javatest.util.StringArray;

/**
 * Show the output sections for a particular test result.
 * This panel will dynamically update data if the test is running.
 */

class TP_OutputSubpanel extends TP_Subpanel {

    String currentTOCEntry = null;

    TP_OutputSubpanel(UIFactory uif) {
        super(uif, "out");
        initGUI();
    }

    protected synchronized void updateSubpanel(TestResult newTest) {
        if (subpanelTest != null)
            subpanelTest.removeObserver(observer);

        super.updateSubpanel(newTest);
        updateTOC();

        // if it is mutable, track updates
        if (subpanelTest.isMutable())  {
            subpanelTest.addObserver(observer);
        }
    }

    private void initGUI() {
        setLayout(new BorderLayout());

        tocEntries = new DefaultListModel();
        toc = uif.createList("test.out.toc", tocEntries);
        toc.setCellRenderer(new TOCRenderer());
        toc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toc.setPrototypeCellValue("12345678901234567890");
        toc.setVisibleRowCount(10);
        toc.addListSelectionListener(listener);

        JScrollPane scrollableTOC =
            uif.createScrollPane(toc,
                            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        main = new JPanel(new BorderLayout());
        titleField = uif.createOutputField("test.out.title");
        titleField.setBackground(UIFactory.Colors.PRIMARY_CONTROL_DARK_SHADOW.getValue());
        titleField.setForeground(UIFactory.Colors.WINDOW_BACKGROUND.getValue());
        main.add(titleField, BorderLayout.NORTH);

        body = new JPanel(new CardLayout()) {
            public Dimension getPreferredSize() {
                int dpi = uif.getDotsPerInch();
                return new Dimension(3 * dpi, 3 * dpi);
            }
        };


        textArea = uif.createTextArea("test.out.textbody");
        textArea.setEditable(false);
        textArea.setLineWrap(wrap);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        body.add(uif.createScrollPane(textArea,
                                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                 "text");

        htmlArea = new JEditorPane();
        htmlArea.setName("out_summ");
        htmlArea.getAccessibleContext().setAccessibleName(uif.getI18NString("test.out.summ.name"));
        htmlArea.getAccessibleContext().setAccessibleDescription(uif.getI18NString("test.out.summ.name"));
        //htmlArea.setContentType("text/html");

        // create and set a vacuous subtype of HTMLDocument, simply in order
        // to have the right classloader associated with it, that can load
        // any related OBJECT tags
        htmlArea.setDocument(new HTMLDocument(getStyleSheet()) { });
        htmlArea.setEditable(false);
        htmlArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        htmlArea.addHyperlinkListener(listener);
        body.add(uif.createScrollPane(htmlArea,
                                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
                 "html");

        main.add(body, BorderLayout.CENTER);

        JSplitPane sp =
            new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollableTOC, main);
        sp.setBorder(BorderFactory.createLoweredBevelBorder());
        sp.setResizeWeight(0); // all excess space to right hand side
        add(sp);

        CSH.setHelpIDString(this, "browse.outputTab.csh");
        initIcons();
    }

    private void initIcons() {
        streamIcon = uif.createIcon("test.out.sect.stream");
    }

    private String getStatusKey(int i) {
        String s;
        switch (i) {
        case Status.PASSED:  return "passed";
        case Status.FAILED:  return "failed";
        case Status.ERROR:   return "error";
        case Status.NOT_RUN: return "notRun";
        default:             return "unknown";
        }
    }

    private String createNotRunSummary() {
        StringWriter sw = new StringWriter();
        try {
            HTMLWriterEx out = new HTMLWriterEx(sw, uif.getI18NResourceBundle());
            out.startTag(HTMLWriterEx.HTML);
            out.startTag(HTMLWriterEx.HEAD);
            out.writeContentMeta();
            out.endTag(HTMLWriterEx.HEAD);
            out.startTag(HTMLWriterEx.BODY);
            //out.writeStyleAttr(bodyStyle);
            out.writeI18N("test.out.smry.testNotRun");
            out.endTag(HTMLWriterEx.BODY);
            out.endTag(HTMLWriterEx.HTML);
            out.close();
        }
        catch (IOException e) {
            // should not happen, with StringWriter
        }
        return sw.toString();
    }

    private String createSummary() {
        StringWriter sw = new StringWriter();
        try {
            HTMLWriterEx out = new HTMLWriterEx(sw, uif.getI18NResourceBundle());
            out.startTag(HTMLWriterEx.HTML);
            out.startTag(HTMLWriterEx.HEAD);
            out.writeContentMeta();
            out.endTag(HTMLWriterEx.HEAD);
            out.startTag(HTMLWriterEx.BODY);
            //out.writeStyleAttr(bodyStyle);

            String[] scriptAndArgs;
            try {
                scriptAndArgs = StringArray.split(subpanelTest.getProperty(TestResult.SCRIPT));
            }
            catch (TestResult.Fault e) {
                scriptAndArgs = null;
            }

            String script = (scriptAndArgs == null || scriptAndArgs.length == 0
                             ? uif.getI18NString("test.out.smry.unknownScript")
                             : scriptAndArgs[0]);
            out.writeI18N("test.out.script");
            out.startTag(HTMLWriterEx.TABLE);
            out.writeAttr(HTMLWriterEx.BORDER, "0");
            //out.writeStyleAttr(tableStyle);
            out.startTag(HTMLWriterEx.TR);
            out.startTag(HTMLWriterEx.TD);
            out.startTag(HTMLWriterEx.CODE);
            out.write(script);
            out.endTag(HTMLWriterEx.CODE);
            out.endTag(HTMLWriterEx.TD);
            out.endTag(HTMLWriterEx.TR);
            out.endTag(HTMLWriterEx.TABLE);
            if (scriptAndArgs != null && scriptAndArgs.length > 1) {
                out.writeI18N("test.out.scriptArgs");
                out.startTag(HTMLWriterEx.TABLE);
                out.writeAttr(HTMLWriterEx.BORDER, "0");
                //out.writeStyleAttr(tableStyle);
                for (int i = 1; i < scriptAndArgs.length; i++) {
                    out.startTag(HTMLWriterEx.TR);
                    out.startTag(HTMLWriterEx.TD);
                    out.startTag(HTMLWriterEx.CODE);
                    out.write(scriptAndArgs[i]);
                    out.endTag(HTMLWriterEx.CODE);
                    out.endTag(HTMLWriterEx.TD);
                    out.endTag(HTMLWriterEx.TR);
                }
                out.endTag(HTMLWriterEx.TABLE);
            }

            if (subpanelTest.getSectionCount() > 0) {
                TestResult.Section s = subpanelTest.getSection(0);
                if (s.getTitle().equals(TestResult.MSG_SECTION_NAME)) {
                    out.writeI18N("test.out.smry.scriptLog.txt");
                    out.startTag(HTMLWriterEx.TABLE);
                    out.writeAttr(HTMLWriterEx.BORDER, "0");
                    //out.writeStyleAttr(tableStyle);
                    String[] names = s.getOutputNames();
                    for (int i = 0; i < names.length; i++) {
                        String name = names[i];
                        String text = s.getOutput(name);
                        out.startTag(HTMLWriterEx.TR);
                        out.startTag(HTMLWriterEx.TD);
                        out.writeLink("#" + name, name/*, linkStyle*/);
                        out.endTag(HTMLWriterEx.TD);
                        out.endTag(HTMLWriterEx.TR);
                    }
                    out.endTag(HTMLWriterEx.TABLE);
                }
            }

            // generate a table showing the various sections
            if (subpanelTest.getSectionCount() > 0) {
                out.startTag(HTMLWriterEx.H3);
                //out.writeStyleAttr(h3Style);
                out.writeI18N("test.out.smry.sections.head");
                out.endTag(HTMLWriterEx.H3);
                out.writeI18N("test.out.smry.sections.txt");
                out.startTag(HTMLWriterEx.TABLE);
                out.writeAttr(HTMLWriterEx.BORDER, "0");
                //out.writeStyleAttr(tableStyle);
                for (int i = 0; i < subpanelTest.getSectionCount(); i++) {
                    TestResult.Section s = subpanelTest.getSection(i);
                    if (s.getTitle().equals(TestResult.MSG_SECTION_NAME))
                        continue; // already done, above
                    out.startTag(HTMLWriterEx.TR);
                    out.startTag(HTMLWriterEx.TD);
                    out.startTag(HTMLWriterEx.OBJECT);
                    out.writeAttr(HTMLWriterEx.CLASSID, "com.sun.javatest.tool.IconLabel");
                    out.writeParam("type", "testSection");
                    out.writeParam("state", getStatusKey(s.getStatus().getType()));
                    out.endTag(HTMLWriterEx.OBJECT);
                    out.writeLink(String.valueOf(i), s.getTitle()/*, linkStyle*/);
                    out.endTag(HTMLWriterEx.TD);
                    out.endTag(HTMLWriterEx.TR);
                }
                out.endTag(HTMLWriterEx.TABLE);

                out.startTag(HTMLWriterEx.H3);
                //out.writeStyleAttr(h3Style);
                out.writeI18N("test.out.outcome.head");
                out.endTag(HTMLWriterEx.H3);
                out.writeI18N("test.out.testResultForOutput.txt");
            }
            else {
                out.startTag(HTMLWriterEx.H3);
                //out.writeStyleAttr(h3Style);
                out.writeI18N("test.out.outcome.head");
                out.endTag(HTMLWriterEx.H3);
                out.writeI18N("test.out.testResultNoOutput.txt");
            }

            Status s = subpanelTest.getStatus();
            out.startTag(HTMLWriterEx.TABLE);
            out.writeAttr(HTMLWriterEx.BORDER, "0");
            //out.writeStyleAttr(tableStyle);
            out.startTag(HTMLWriterEx.TR);
            out.startTag(HTMLWriterEx.TD);
            out.startTag(HTMLWriterEx.OBJECT);
            out.writeAttr(HTMLWriterEx.CLASSID, "com.sun.javatest.tool.IconLabel");
            out.writeParam("type", "test");
            out.writeParam("state", getStatusKey(s.getType()));
            out.endTag(HTMLWriterEx.OBJECT);
            out.endTag(HTMLWriterEx.TD);
            out.startTag(HTMLWriterEx.TD);
            out.write(s.toString());
            out.endTag(HTMLWriterEx.TD);
            out.endTag(HTMLWriterEx.TR);
            out.endTag(HTMLWriterEx.TABLE);

            out.endTag(HTMLWriterEx.BODY);
            out.endTag(HTMLWriterEx.HTML);
            out.close();
        }
        catch (IOException e) {
            // should not happen, writing to StringWriter
        }
        catch (TestResult.ReloadFault e) {
            throw new JavaTestError("Error loading result file for " +
                                    subpanelTest.getTestName());
        }

        return sw.toString();
    }

    private String createSectionSummary(TestResult.Section section) {
        StringWriter sw = new StringWriter();
        try {
            HTMLWriterEx out = new HTMLWriterEx(sw, uif.getI18NResourceBundle());
            out.startTag(HTMLWriterEx.HTML);
            out.startTag(HTMLWriterEx.HEAD);
            out.writeContentMeta();
            out.endTag(HTMLWriterEx.HEAD);
            out.startTag(HTMLWriterEx.BODY);
            //out.writeStyleAttr(bodyStyle);

            // generate a table showing the size of the various output streams
            out.startTag(HTMLWriterEx.H3);
            out.writeStyleAttr("margin-top: 0");
            out.writeI18N("test.out.outputSummary.head");
            out.endTag(HTMLWriterEx.H3);
            out.writeI18N("test.out.outputSummary.txt");
            out.startTag(HTMLWriterEx.TABLE);
            out.writeAttr(HTMLWriterEx.BORDER, "0");
            //out.writeStyleAttr(tableStyle);
            out.startTag(HTMLWriterEx.TR);
            out.startTag(HTMLWriterEx.TH);
            out.writeAttr(HTMLWriterEx.ALIGN, HTMLWriterEx.LEFT);
            out.writeI18N("test.out.outputName.txt");
            out.endTag(HTMLWriterEx.TH);
            out.startTag(HTMLWriterEx.TH);
            out.writeAttr(HTMLWriterEx.ALIGN, HTMLWriterEx.LEFT);
            out.writeStyleAttr("margin-left:10");
            out.writeI18N("test.out.outputSize.txt");
            out.endTag(HTMLWriterEx.TH);
            out.endTag(HTMLWriterEx.TR);
            String[] names = section.getOutputNames();
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                String text = section.getOutput(name);
                out.startTag(HTMLWriterEx.TR);
                out.startTag(HTMLWriterEx.TD);
                if (text.length() == 0)
                    out.write(name);
                else
                    out.writeLink("#" + name, name/*, linkStyle*/);
                out.endTag(HTMLWriterEx.TD);
                out.startTag(HTMLWriterEx.TD);
                out.writeStyleAttr("margin-left:10");
                if (text.length() == 0)
                    out.writeI18N("test.out.empty.txt");
                else
                    out.write(String.valueOf(text.length()));
                out.endTag(HTMLWriterEx.TD);
                out.endTag(HTMLWriterEx.TR);
            }
            out.endTag(HTMLWriterEx.TABLE);

            // if there is a status, show it
            Status s = section.getStatus();
            if (s != null) {
                out.startTag(HTMLWriterEx.H3);
                //out.writeStyleAttr(h3Style);
                out.writeI18N("test.out.outcome.head");
                out.endTag(HTMLWriterEx.H3);
                out.writeI18N("test.out.sectionResult.txt");
                out.startTag(HTMLWriterEx.P);
                out.writeStyleAttr("margin-left:30; margin-top:0; font-size: 12pt");
                out.startTag(HTMLWriterEx.OBJECT);
                out.writeAttr(HTMLWriterEx.CLASSID, "com.sun.javatest.tool.IconLabel");
                out.writeParam("type", "testSection");
                out.writeParam("state", getStatusKey(s.getType()));
                out.endTag(HTMLWriterEx.OBJECT);
                out.write(s.toString());
                out.endTag(HTMLWriterEx.P);
            }

            out.endTag(HTMLWriterEx.BODY);
            out.endTag(HTMLWriterEx.HTML);
            out.close();
        }
        catch (IOException e) {
            // should not happen with StringWriter
        }
        return sw.toString();
    }

    private String createStatusSummary() {
        StringWriter sw = new StringWriter();
        try {
            HTMLWriterEx out = new HTMLWriterEx(sw, uif.getI18NResourceBundle());
            out.startTag(HTMLWriterEx.HTML);
            out.startTag(HTMLWriterEx.HEAD);
            out.writeContentMeta();
            out.endTag(HTMLWriterEx.HEAD);
            out.startTag(HTMLWriterEx.BODY);
            //out.writeStyleAttr(bodyStyle);

            if (subpanelTest.getSectionCount() > 0)
                out.writeI18N("test.out.testResultForOutput.txt");
            else
                out.writeI18N("test.out.testResultNoOutput.txt");

            Status s = subpanelTest.getStatus();
            out.startTag(HTMLWriterEx.TABLE);
            out.writeAttr(HTMLWriterEx.BORDER, "0");
            //out.writeStyleAttr(tableStyle);
            out.startTag(HTMLWriterEx.TR);
            out.startTag(HTMLWriterEx.TD);
            out.startTag(HTMLWriterEx.OBJECT);
            out.writeAttr(HTMLWriterEx.CLASSID, "com.sun.javatest.tool.IconLabel");
            out.writeParam("type", "test");
            out.writeParam("state", getStatusKey(s.getType()));
            out.endTag(HTMLWriterEx.OBJECT);
            out.endTag(HTMLWriterEx.TD);
            out.startTag(HTMLWriterEx.TD);
            out.write(s.toString());
            out.endTag(HTMLWriterEx.TD);
            out.endTag(HTMLWriterEx.TR);
            out.endTag(HTMLWriterEx.TABLE);
            out.endTag(HTMLWriterEx.BODY);
            out.endTag(HTMLWriterEx.HTML);
            out.close();
        }
        catch (IOException e) {
            // should not happen, for StringWriter
        }
        return sw.toString();
    }

    void updateTOC() {
        try {
            TOCEntry newSelectedEntry = null;
            tocEntries.setSize(0);
            for (int i = 0; i < subpanelTest.getSectionCount(); i++) {
                TestResult.Section s = subpanelTest.getSection(i);
                TOCEntry e = new TOCEntry(s);
                if (e.isScriptMessagesSection() && (currentTOCEntry == null) ||
                    (e.getID().equals(currentTOCEntry)))
                    newSelectedEntry = e;
                tocEntries.addElement(e);
                String[] names = s.getOutputNames();
                for (int j = 0; j < names.length; j++) {
                    e = new TOCEntry(s, names[j]);
                    if (e.getID().equals(currentTOCEntry)) {
                        newSelectedEntry = e;
                    }
                    tocEntries.addElement(e);
                }
            }

            TOCEntry e = new TOCEntry(); // for final status
            if (newSelectedEntry == null)
                newSelectedEntry = e;
            tocEntries.addElement(e);

            currentTOCEntry = newSelectedEntry.getID();
            toc.setSelectedValue(newSelectedEntry, true);
        }
        catch (TestResult.ReloadFault e) {
            throw new JavaTestError("Error loading result file for " +
                                    subpanelTest.getTestName());
        }
    }

    private void updateTOCLater() {
        if (EventQueue.isDispatchThread())
            updateTOC();
        else {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        updateTOC(); // will also update current entry
                    }
                });
        }
    }

    /**
     * @param tr Result being updated.
     * @param section Section being updated.
     * @param outputName Output in section being updated, may be null.
     * @param start Start index from TestResult API
     * @param end End index from TestResult API
     * @param text New text.
     * @see com.sun.javatest.TestResult
     */
    private void updateOutput(final TestResult tr, final TestResult.Section section,
                              final String outputName,
                              final int start, final int end, final String text) {
        //System.err.println("TPOS_TRO: written output[" + section.getTitle() + "/" + outputName + "]: " + tr.getWorkRelativePath());
        TOCEntry entry = findTOCEntry(section);
        // should happen zero or one times per section
        // primarily to catch observer messages that were missed before this
        // panel attached to the TR.
        if (entry == null) {
            updateTOC();
            return;  // return because it can't possibly be selected by user yet
        }

        // are we actually looking at the section being updated?
        entry = (TOCEntry) (toc.getSelectedValue());
        if (entry.getSection() == section && outputName == entry.getOutputName()) {
            addText(section, outputName, text);
        }
    }


    private TOCEntry findTOCEntry(TestResult.Section section) {
        if (tocEntries == null)
            return null;

        for (int i = 0; i < tocEntries.size(); i++) {
            TOCEntry entry = (TOCEntry) (tocEntries.get(i));
            if (entry.getSection() == section) {
                // found match, select this entry
                return entry;
            }
        }
        return null;
    }

    private void showHTML(String s) {
        // high cost method call
        htmlArea.setContentType("text/html");

        // try resetting doc to an empty doc before setting new text

        // create and set a vacuous subtype of HTMLDocument, simply in order
        // to have the right classloader associated with it, that can load
        // any related OBJECT tags
        HTMLDocument doc = new HTMLDocument(getStyleSheet()) { };
        htmlArea.setDocument(doc);
        htmlArea.setText(s);

        /*
        StyleSheet styles = doc.getStyleSheet();
        Enumeration rules = styles.getStyleNames();
        while (rules.hasMoreElements()) {
            String name = (String) rules.nextElement();
            System.out.println(styles.getStyle(name));
        }
        */

        ((CardLayout)(body.getLayout())).show(body, "html");
    }

    private void showText(String s) {
        if (s.length() == 0) {
            textArea.setText(uif.getI18NString("test.out.empty.txt"));
            textArea.setEnabled(false);
        }
        else {
            wrap = Boolean.parseBoolean(prefs.getPreference(LINE_WRAP_PREF, Boolean.toString(true)));
            if (wrap) {
                textArea.setLineWrap(true);
            } else {
                textArea.setLineWrap(false);
            }
            textArea.setText(s);
            textArea.setCaretPosition(0);
            textArea.setEnabled(true);
        }

        ((CardLayout)(body.getLayout())).show(body, "text");
    }

    private void addText(TestResult.Section section,
            String outputName, String s) {
        if (s == null || s.length() == 0)
            return;

        if (!textArea.isEnabled()) {
            textArea.setText("");
            textArea.append(s);
            textArea.setEnabled(true);
        }
        else if (outputName != null) {
            textArea.append(s);
        }
    }

    private StyleSheet getStyleSheet() {
        if (htmlEditorKit == null)
            htmlEditorKit = new HTMLEditorKit();

        if (styleSheet == null) {
            styleSheet = new StyleSheet();
            styleSheet.addStyleSheet(htmlEditorKit.getStyleSheet());
            styleSheet.addRule("body  { font-family: SansSerif; font-size: 12pt }");
            styleSheet.addRule("h3    { margin-top:15 }");
            styleSheet.addRule("table { margin-left:30; margin-top:0 }");
        }
        return styleSheet;
    }

    private boolean wrap = true;
    private Preferences prefs = Preferences.access();
    public static final String LINE_WRAP_PREF = "testOutput.lineWrap";
    private Icon streamIcon;
    private JList toc;
    private JTextField titleField;
    private JPanel body;
    private JPanel main;
    private JTextArea textArea;

    private JEditorPane htmlArea;

    private DefaultListModel tocEntries;
    private Listener listener = new Listener();
    private TRObserver observer = new TRObserver();

    private StyleSheet styleSheet;
    private HTMLEditorKit htmlEditorKit;

    //------------------------------------------------------------------------------------

    private class Listener implements HyperlinkListener, ListSelectionListener
    {
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                String desc = e.getDescription();
                if (desc.startsWith("#")) {
                    String outputName = desc.substring(1);
                    // search for the output entry for the currently selected section
                    int index = toc.getSelectedIndex();
                    if (index != -1) {
                        for (int i = index + 1; i < tocEntries.size(); i++) {
                            TOCEntry entry = (TOCEntry) (tocEntries.get(i));
                            String entryOutputName = entry.getOutputName();
                            if (entryOutputName == null)
                                // name not found, reached next section entry
                                break;
                            else if (entryOutputName.equals(outputName)) {
                                // found match, select this entry
                                toc.setSelectedIndex(i);
                                return;
                            }
                        }
                    }
                }
                else {
                    try {
                        int sectIndex = Integer.parseInt(desc);
                        TestResult.Section s = subpanelTest.getSection(sectIndex);
                        for (int i = 0; i < tocEntries.size(); i++) {
                            TOCEntry entry = (TOCEntry) (tocEntries.get(i));
                            if (entry.getSection() == s) {
                                // found match, select this entry
                                toc.setSelectedIndex(i);
                                return;
                            }
                        }

                    }
                    catch (TestResult.ReloadFault f) {
                        throw new JavaTestError("Error loading result file for " +
                                                subpanelTest.getTestName());
                    }
                }
            }
        }

        public void valueChanged(ListSelectionEvent e) {
            JList l = (JList) (e.getSource());
            TOCEntry entry = (TOCEntry) (l.getSelectedValue());
            if (entry == null)
                return;
            titleField.setText(entry.getTitle());
            currentTOCEntry = entry.getID();
            String outputName = entry.getOutputName();

            if (entry.section == null) {
                if (subpanelTest.getStatus().getType() == Status.NOT_RUN)
                    showHTML(createNotRunSummary());
                else
                    showHTML(createStatusSummary());
            }
            else if (outputName != null)
                showText(entry.getSection().getOutput(outputName));
            else if (entry.isScriptMessagesSection())
                showHTML(createSummary());
            else
                showHTML(createSectionSummary(entry.getSection()));
        }
    }

    //------------------------------------------------------------------------------------

    private class TRObserver
        implements TestResult.Observer
    {
        public void completed(TestResult tr) {
            //System.err.println("TPOS_TRO: completed: " + tr.getWorkRelativePath());
            updateTOCLater();
            tr.removeObserver(this);
        }

        public void createdSection(TestResult tr, TestResult.Section section) {
            //System.err.println("TPOS_TRO: created section[" + section.getTitle() + "]: " + tr.getWorkRelativePath());
            updateTOCLater();
        }

        public void completedSection(TestResult tr, TestResult.Section section) {
            //System.err.println("TPOS_TRO: completed section[" + section.getTitle() + "]: " + tr.getWorkRelativePath());
            updateTOCLater();
        }

        public void createdOutput(TestResult tr, TestResult.Section section,
                                  String outputName) {
            //System.err.println("TPOS_TRO: created output[" + section.getTitle() + "/" + outputName + "]: " + tr.getWorkRelativePath());
            updateTOCLater();
        }

        public void completedOutput(TestResult tr, TestResult.Section section,
                                    String outputName) {
            //System.err.println("TPOS_TRO: completed output[" + section.getTitle() + "/" + outputName + "]: " + tr.getWorkRelativePath());
        }

        public void updatedOutput(final TestResult tr, final TestResult.Section section,
                                  final String outputName,
                                  final int start, final int end, final String text) {
            //System.err.println("TPOS_TRO: written output[" + section.getTitle() + "/" + outputName + "]: " + tr.getWorkRelativePath());
            // this msg almost always on different thread - send it to the
            // event thread
            Runnable t = new Runnable() {
                public void run() {
                    updateOutput(tr, section, outputName, start, end, text);
                }
            };  // Runnable

            EventQueue.invokeLater(t);
        }

        public void updatedProperty(TestResult tr, String name, String value) {
            // ignore
        }
    }

    //------------------------------------------------------------------------------------

    private class TOCEntry {
        // create an entry that will show the test result status
        TOCEntry() {
            section = null;
            outputName = null;
        }

        // create an entry for a section summary
        TOCEntry(TestResult.Section s) {
            section = s;
            outputName = null;
        }

        // create an entry for a block of section output
        TOCEntry(TestResult.Section s, String n) {
            section = s;
            outputName = n;
        }

        boolean isScriptMessagesSection() {
            return (section != null && section.getTitle().equals(TestResult.MSG_SECTION_NAME));
        }

        TestResult.Section getSection() {
            return section;
        }

        String getOutputName() {
            return outputName;
        }

        String getTitle() {
            if (section == null) {
                if (subpanelTest.getStatus().getType() == Status.NOT_RUN)
                    return uif.getI18NString("test.out.notRunTitle");
                else
                    return uif.getI18NString("test.out.statusTitle");
            }
            else if (isScriptMessagesSection()) {
                if (outputName == null)
                    return uif.getI18NString("test.out.summary");
                else
                    return uif.getI18NString("test.out.scriptMessages");
            }
            else {
                if (outputName == null)
                    return uif.getI18NString("test.out.sectionTitle", section.getTitle());
                else
                    return uif.getI18NString("test.out.streamTitle",
                                             new Object[] { section.getTitle(), outputName });
            }
        }

        String getText() {
            if (section == null){
                if (subpanelTest.getStatus().getType() == Status.NOT_RUN)
                    return uif.getI18NString("test.out.notRunTitle");
                else
                    return uif.getI18NString("test.out.statusTitle");
            }
            else if (isScriptMessagesSection()) {
                if (outputName == null)
                    return uif.getI18NString("test.out.summary");
                else
                    return uif.getI18NString("test.out.scriptMessages");
            }
            else {
                if (outputName == null)
                    return section.getTitle();
                else
                    return outputName;
            }
        }

        Icon getIcon() {
            if (section == null)
                return IconFactory.getTestIcon(subpanelTest.getStatus().getType(), false, true);
            else if (outputName != null)
                return streamIcon;
            else {
                Status s = section.getStatus();
                //return (s == null ? null : sectIcons[s.getType()]);
                return (s == null ? null : IconFactory.getTestSectionIcon(s.getType()));
            }
        }

        String getID() {
            String s = "";
            if (section != null ) {
                s = section.getTitle() + ":" +
                        (outputName == null ? "" : outputName);
            }
            return s;
        }

        private TestResult.Section section;
        private String outputName;  // null for section entry
    }

    private class TOCRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, null, index,
                                                                   isSelected, cellHasFocus);
            if (value instanceof TOCEntry) {
                TOCEntry e = (TOCEntry) value;
                l.setText(e.getText());
                l.setIcon(e.getIcon());
            }
            else
                l.setText(value.toString());
            return l;
        }
    }


}
