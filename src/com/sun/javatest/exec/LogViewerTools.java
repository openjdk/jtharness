/*
 * $Id$
 *
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.logging.LogModel;
import com.sun.javatest.report.Report;
import com.sun.javatest.report.XMLReportMaker;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.logging.LoggerFactory;
import com.sun.javatest.report.ReportSettings;
import com.sun.javatest.util.BackupPolicy;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.Timer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

class LogViewerTools extends Thread {

    LogViewerTools(LogModel model, File out, Logger log, Component parent, UIFactory uif) {
        this.model = model;
        this.out = out;
        this.log = log;
        this.parent = parent;
        this.uif = uif;
    }

    void go() {

        waitDialog = uif.createWaitDialog("lvt.wait",parent);
        waitDialogEnabled = true;

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // show dialog if still processing
                if (isAlive() && waitDialogEnabled) {
                    waitDialog.show();
                }
            }
        };

        // show wait dialog if operation is still running after
        // WAIT_DIALOG_DELAY
        Timer timer = new Timer(WAIT_DIALOG_DELAY, al);
        timer.setRepeats(false);
        timer.start();

        start();
    }

    public void run() {
        Properties outputProps = new Properties();
        outputProps.put("indent", "yes");
        outputProps.put("encoding", XML_CHARSET);
        SAXTransformerFactory stf = (SAXTransformerFactory )TransformerFactory.newInstance();
        stf.setAttribute("indent-number", 4);
        try {
            ser = stf.newTransformerHandler();
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
            return;
        }

        ser.getTransformer().setOutputProperties(outputProps);
        AttributesImpl emptyAttr = new AttributesImpl();
        try {

            ReportSettings settings = Report.getSettingsPrefs();
            BackupPolicy backupPolicy;

            if (settings.isBackupsEnabled()) {
                backupPolicy = BackupPolicy.simpleBackups(settings.getBackupLevel());
            }  else {
                backupPolicy = BackupPolicy.noBackups();
            }

            Writer w;
            try {
                w = backupPolicy.backupAndOpenWriter(out, XML_CHARSET);
            } catch (FileNotFoundException ex) {
                waitDialogEnabled = false;
                uif.showError("logviewer.cantwritereport", out.getAbsolutePath());
                return;
            }

            ser.setResult(new StreamResult(w));

            ser.startDocument();
            ser.startElement("","","report", emptyAttr);
            for (int i = 0; i < model.getRecords().size(); i++) {
                LogModel.LiteLogRecord llr =  model.getRecords().get(i);
                outRecord(llr);
            }
            ser.endElement("","","report");
            ser.endDocument();
            w.close();

        } catch (IOException ex) {
            log.log(Level.SEVERE, "LogViewer report", ex);
        } catch (SAXException ex) {
            log.log(Level.SEVERE, "LogViewer report", ex);
        } finally {
            waitDialog.setVisible(false);
        }
    }

    private void outRecord(LogModel.LiteLogRecord llr) throws SAXException, IOException {
        String msg = model.getRecordMessage(llr);
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", "Logger", "String", model.getLogname(llr.loggerID));
        atts.addAttribute("", "", "Level", "String", "" +llr.severety);
        atts.addAttribute("", "", "LevelName", "String",
                LoggerFactory.getLocalizedLevelName(Level.parse("" + llr.severety)));
        atts.addAttribute("", "", "Time", "String", dateToISO8601(llr.time));
        ser.startElement("","","logrecord", atts);
        XMLReportMaker.writeCDATA(ser, ser, msg);
        ser.endElement("","","logrecord");
    }

    private String dateToISO8601(long time) {
        DateFormat dfISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String dateStr = dfISO8601.format(new Date(time));
        return dateStr.substring(0, 22) + ":" + dateStr.substring(22);
    }

    private JDialog waitDialog;
    private boolean waitDialogEnabled;
    private TransformerHandler ser;
    private LogModel model;
    private File out;
    private Logger log;
    private Component parent;
    private  UIFactory uif;
    private String XML_CHARSET = "UTF-8";
    private static final int WAIT_DIALOG_DELAY = 3000;      // 3 second delay
}


    /*
     * There is no correct way obtain on close notification for ToolDialog by
     * clicking dialog's close icon.
     * So, we need to be on the watch for it...
     */
class StopWatcher extends Thread {
    public StopWatcher(LogViewer lv) {
        super("LogViewerStopWatcher");
        this.lv = lv;
    }
    private LogViewer lv;
    public void run() {
        while (lv.isVisible()) {
            try {
                sleep(1000);
            } catch (InterruptedException ex) {
                // ok
            }
        }
        lv.dispose();
        lv = null;
    }
}



