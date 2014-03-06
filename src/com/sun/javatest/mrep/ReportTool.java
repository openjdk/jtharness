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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import com.sun.javatest.report.CustomReport;
import com.sun.javatest.report.Report;
import com.sun.javatest.report.ReportSettings;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.ToolManager;
import com.sun.javatest.report.HTMLWriterEx;
import com.sun.javatest.util.I18NResourceBundle;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.Timer;
import javax.swing.SwingUtilities;


class ReportTool extends Tool {

    // desktop is used to get custom reports
    // for results processing
    private Desktop desktop;

    protected ReportTool(ToolManager m, Desktop d) {
        super(m, "report", "mergeReports.window.csh");
        setI18NTitle("tool.title");
        setShortTitle(uif.getI18NString("tool.shortTitle"));
        this.desktop = d;
        initGUI();
        I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ReportTool.class);
        textShowing = i18n.getString("tool.helptext.showing");
        textHidden = i18n.getString("tool.helptext.hidden");
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    @Override
    protected void restore(Map map) {
    }

    protected void save(Map m) {
    }

    private void initGUI(){
        int dpi = uif.getDotsPerInch();
        setPreferredSize(new Dimension(6 * dpi, 4 * dpi));
        setLayout(new BorderLayout());
        htmlKit = new HTMLEditorKit();

        addHierarchyListener(listener);

        menuBar = uif.createMenuBar("tool");
        String[] reportMenuEntries = {
            NEW,
            OPEN
        };

        JMenu reportMenu = uif.createMenu("tool.report", reportMenuEntries, new Listener());
        menuBar.add(reportMenu);
        menuBar.add(uif.createHorizontalGlue("tool.pad"));

        JPanel head = uif.createPanel("head", false);
        head.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        head.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        browserPane = new BrowserPane(uif);
        head.add(browserPane, c);
        add(head);
        updateGUI();

    }

    void updateGUI() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    updateGUI();
                }
            });
            return;
        }

        if (browserPane.isEmpty()) {
            if (optionsDialog == null || !optionsDialog.isVisible()) {
                browserPane.setPlainText(textHidden);
            } else {
                browserPane.setPlainText(textShowing);
            }
        } else {
            if (optionsDialog == null || !optionsDialog.isVisible()) {
                browserPane.setPlainTextHomePage(textHidden);
            } else {
                browserPane.setPlainTextHomePage(textShowing);
            }
        }
    }

    private synchronized void showOptions() {
        if (worker != null) {
            uif.showError("tool.reportInProgress");
            return;
        }

        if (optionsDialog == null) {
            // should arguably worry about standard ownership problem
            optionsDialog = new OptionsDialog(this, new OkListener(), uif, desktop);
        }
        optionsDialog.updateCustomReports();
        optionsDialog.setVisible(true);
    }

    private boolean setOptions() {

        if (!optionsDialog.checkInput()) {
            return false;
        }

        out = new File(optionsDialog.getResultPath());

        in = new File[optionsDialog.getXmlFiles().length];
        for (int i = 0; i < optionsDialog.getXmlFiles().length; i++) {
            in[i] = new File(optionsDialog.getXmlFiles()[i]);
        }

        resolveByRecent = optionsDialog.resolveByRecent();
        customReports = optionsDialog.getCustomReports();
        isXmlReport = optionsDialog.isXmlReport();

        if (!isXmlReport && (customReports == null || customReports.length ==0)) {
            uif.showError("tool.no_report_types");
            return false;
        }

        File outXML = new File(out, xmlreportFileName);
        for (int i = 0; i < in.length; i++) {
            String path = outXML.getAbsolutePath();
            if (in[i].getAbsolutePath().equals(path)) {
                uif.showError("tool.outinput", path);
                return false;
            }
        }

        try {
            optionsDialog.setVisible(false);
            startMerge();
            return true;
        } catch (Exception e) {
            uif.showError("tool.execpt", e.getMessage());
        }
        return false;
    }

    private synchronized void startMerge() {
        if (worker != null) {
            uif.showError("tool.reportInProgress");
            return;
        }

        waitDialog = uif.createWaitDialog("tool.wait", this);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets.bottom = 10;
        gbc.insets.top = 10;
        gbc.gridy = 2;
        gbc.gridx = 0;
        JButton cancelBtn = uif.createButton("tool.cancel");
        waitDialog.getContentPane().add(cancelBtn, gbc);
        waitDialog.pack();
        waitDialogController = new WaitDialogController(waitDialog);
        final String cancelling = uif.getI18NString("tool.cancelling");
        cancelBtn.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JButton butt = (JButton) e.getSource();
                butt.setEnabled(false);
                Component[] cmp = waitDialog.getContentPane().getComponents();
                if (worker != null && worker.isAlive()) {
                    worker.interrupt();
                }
                for ( int i = 0; i < cmp.length; i++) {
                    if("tool.wait".equals(cmp[i].getName())) {
                        if (cmp[i] instanceof JTextComponent) {
                            ((JTextComponent)cmp[i]).setText(cancelling);
                        }
                        break;
                    }
                }

            }
        });

        worker = new Thread() {
            public void run() {
                try {
                    Merger merger = new Merger();
                    ConflictResolver resolver;
                    if (!resolveByRecent) {
                        resolver = new ManualConfilctResolver(waitDialogController);
                    } else {
                        resolver = new MostRecentConfilctResolver();
                    }
                    out.mkdir();
                    File xmlOut = new File(out, xmlreportFileName);
                    if (merger.merge(in,xmlOut, resolver)) {
                        for (int i = 0; i < customReports.length; i++) {
                            if (Thread.currentThread().isInterrupted()) {
                                return ;
                            }
                            CustomReport cr = customReports[i];
                            ReportSettings re = cr.getReportEnviroment();
                            re.setMergingFiles(in);
                            re.setXMLReportFile(xmlOut);
                            cr.createReport(new File(out, cr.getReportId()));
                        }

                        waitDialogController.finish();

                        if (!isXmlReport) {
                            xmlOut.delete();
                        }
                        showReportDialog(out);
                    }
                } catch (Exception e) {
                    showError("tool.exceptionInProgress", e.getMessage(), waitDialog, waitDialogController);
                } finally {

                    if (!waitDialogController.wasFinished)
                        waitDialogController.finish();

                    synchronized (ReportTool.this) {
                        worker = null;
                    }
                    updateGUI();
                }
            }

        };

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // show dialog if still processing
                if (worker != null && worker.isAlive()) {
                    waitDialogController.show();
                }
            }
        };

        // show wait dialog if operation is still running after
        // WAIT_DIALOG_DELAY
        Timer timer = new Timer(WAIT_DIALOG_DELAY, al);
        timer.setRepeats(false);
        timer.start();

        worker.start();

        updateGUI();
    }

    private void showError(final String uiKey, final String msg,
                           final JDialog waitDialog, final WaitDialogController waitDialogController) {
        if (!waitDialogController.wasFinished)
            waitDialogController.finish();
        // switch back to GUI thread
        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    uif.showError(uiKey, msg);
                }
            }
        );
    }

    private static class WaitDialogController {
        WaitDialogController(JDialog waitDialog) {
            this.waitDialog = waitDialog;
        }

        synchronized void show() {
            // show only once
            if (!wasFinished) {
                wasShown = true;
                if (!wasHidden) {
                    setVisible(true);
                }
            }
        }

        synchronized void finish() {
            wasFinished = true;
            setVisible(false);
        }


        synchronized void hide() {
            wasHidden = true;
            if (wasShown && ! wasFinished) {
                setVisible(false);
            }
        }

        synchronized void restore() {
            wasHidden = false;
            if (wasShown && ! wasFinished) {
                setVisible(true);
            }
        }

        private synchronized void setVisible(final boolean b) {
            // should we care about EventDispatchThread here? Yes I guess.
            if (EventQueue.isDispatchThread()) {
                waitDialog.setVisible(b);
            }  else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            waitDialog.setVisible(b);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        private JDialog waitDialog;
        private boolean wasFinished = false;
        private boolean wasShown = false;
        private boolean wasHidden = false;

    }

    class MostRecentConfilctResolver implements ConflictResolver {

        public int resolve(String testUrl, TestResultDescr[] descrs) {
            int res = 0;

            for (int i = 1; i < descrs.length; i++) {
                // priority of NOT_RUN status is the lowerest
                boolean newer = descrs[i].getTime() > descrs[res].getTime();
                if (descrs[res].isNotRun()) {
                    if (!descrs[i].isNotRun() || newer) {
                        res = i;
                    }
                } else {
                    if (!descrs[i].isNotRun() && newer) {
                        res = i;
                    }
                }
            }
            return res;

        }

    }

    class ManualConfilctResolver implements ConflictResolver {
        private File preffered = null;
        private ConflictResolutionDialog conflictResolutionDialog;
        private WaitDialogController wdc;

        public ManualConfilctResolver(WaitDialogController wdc) {
            this.wdc = wdc;
        }

        public int resolve(String testUrl, TestResultDescr[] descrs) {
            String[] conflictFiles = new String[descrs.length];
            for (int i = 0; i < descrs.length; i++) {
                conflictFiles[i] = descrs[i].getFile().getAbsolutePath() + " "
                        + descrs[i].getStatus() ;
            }

            if (preffered != null) {
                for (int i = 0; i < descrs.length; i++) {
                    if (descrs[i].getFile().equals(preffered))
                        return i;
                }
            }

            conflictResolutionDialog =
                    new ConflictResolutionDialog(
                    null, testUrl, conflictFiles, false, uif);

            wdc.hide();
            conflictResolutionDialog.setVisible(true);

            if (conflictResolutionDialog.wasCanceled()) {
                return -1;
            }
            wdc.restore();

            if (conflictResolutionDialog.getUseMostRecent()) {
                int res = 0;
                for (int i = 0; i < descrs.length; i++) {
                    if (descrs[i].getTime() > descrs[res].getTime()) {
                        res = i;
                    }
                }
                return res;
            }


            int selected = conflictResolutionDialog.getSelectedIndex();
            if (selected != -1) {
                if (conflictResolutionDialog.getPreferredReport()) {
                    preffered = descrs[selected].getFile();
                }
                return selected;
            }
            return 0;
        }

    }


    private String listLocalDirectory(File dir) {
        if (!dir.isAbsolute())
            dir = dir.getAbsoluteFile();

        String displayPath = dir.getPath();

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
                } catch (MalformedURLException e) {
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
                } catch (MalformedURLException e) {
                    out.write(file.getName());
                }
            }

            out.endTag(HTMLWriterEx.UL);
            out.endTag(HTMLWriterEx.BODY);
            out.endTag(HTMLWriterEx.HTML);
            out.close();
        } catch (IOException e) {
            // should not happen, writing to StringWriter
        }

        return sw.toString();
    }

    private void loadPage(URL url) {
        // avoid recursive callbacks from updating combo
        // URL.equals can result in a big performance hit
        if (currURL != null && url.toString().equals(currURL.toString()))
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
        } else if (protocol.equals("file")
        && !url.getFile().endsWith(".htm")
        && !url.getFile().endsWith(".html")) {
            textArea.setContentType("text/plain");
            try {
                Reader r = new BufferedReader(new FileReader(file));
                textArea.read(r, url);
                r.close();
            } catch (IOException e) {
                uif.showError("fp.load.error", new Object[] { url, e });
            }

        } else {
            try {
                URL loaded = textArea.getPage();
                // this next stuff is just to avoid some screen flash if a new doc
                // is being read
                if (loaded == null || !loaded.sameFile(url)) {
                    HTMLDocument htmlDoc = (HTMLDocument) (htmlKit.createDefaultDocument());
                    textArea.setDocument(htmlDoc);
                }
                textArea.setPage(url);
            } catch (IOException e) {
                uif.showError("fp.load.error", new Object[] { url, e });
            }
        }

    }


    // The UI components
    private JMenuBar menuBar;
    private BrowserPane browserPane;
    private JDialog waitDialog;
    private WaitDialogController waitDialogController;
    private static final int WAIT_DIALOG_DELAY = 3000;      // 3 second delay

    // The merge options
    private File[] in;
    private File out;
    private boolean resolveByRecent;
    private boolean isXmlReport;
    private CustomReport[] customReports;

    private String xmlreportFileName = "report.xml";

    private boolean autoShowOptions = true;

    private Thread worker;
    private URL currURL;

    private HTMLEditorKit htmlKit;
    private JEditorPane textArea;

    private static final String NEW = "new";
    private static final String OPEN = "open";

    private OptionsDialog optionsDialog;
    private Listener listener = new Listener();

    private final String textShowing;
    private final String textHidden;

    class OkListener implements ActionListener {
        public OkListener() {
        }

        public void actionPerformed(ActionEvent e) {
            if(setOptions())
                optionsDialog.cleanUp();
            updateGUI();
        }
    };

    private void showReportBrowser(File reportDir) {
        // if if is a dir, try to find a particular file to show
        // since there may be multiple choices, use the one with the
        // most recent date
        String[] names = Report.getHtmlReportFilenames();
        File target = reportDir;
        long newestTime = 0;

        for (int i = 0; i < names.length; i++) {
            File f = new File(reportDir, names[i]);
            if (f.exists()  && f.lastModified() > newestTime) {
                target = f;
                newestTime = f.lastModified();
            }
        }

        try {
            browserPane.setFile(target.toURL());
        } catch (MalformedURLException e) {
            uif.showError("tool.report.browser", e.getMessage());
        }
    }

    void showReportDialog(File init) {
        JFileChooser rdc = new JFileChooser(init);

        int option = rdc.showDialog(this, uif.getI18NString("tool.report.open"));
        if (option != JFileChooser.APPROVE_OPTION)
            return;

        File f = rdc.getSelectedFile();
        showReportBrowser(f);

    }

    private class Listener implements ActionListener, HierarchyListener {
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(NEW)) {
                showOptions();
            } else if (cmd.equals(OPEN)) {
                showReportDialog(out);
            }
            updateGUI();
        }

        public void hierarchyChanged(HierarchyEvent e) {
            if (isShowing() && autoShowOptions) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        showOptions();
                        updateGUI();
                    }
                });
                autoShowOptions = false;
            }
        }

    };

    private class HTMLListener implements HyperlinkListener, ItemListener {
        public void hyperlinkUpdate(HyperlinkEvent e) {
            HyperlinkEvent.EventType et = e.getEventType();
            if (et == HyperlinkEvent.EventType.ACTIVATED) {
                if (e instanceof HTMLFrameHyperlinkEvent) {
                    HTMLDocument doc = (HTMLDocument)
                    ((JEditorPane) e.getSource()).getDocument();
                    doc.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) e);
                } else
                    loadPage(e.getURL());
            } else if (et == HyperlinkEvent.EventType.ENTERED) {
                URL u = e.getURL();
                if (u != null) {
                    textArea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            } else if (et == HyperlinkEvent.EventType.EXITED) {
                textArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                URL url = (URL) e.getItem();
                loadPage(url);
            }
        }
    }

}
