/*
 * $Id$
 *
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.report;

import com.sun.javatest.CompositeFilter;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestFilter;
import com.sun.javatest.report.ReportFormat.ReportLink;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.util.BackupUtil;
import com.sun.javatest.util.I18NResourceBundle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Vector;



/**
 * A report generator for sets of test results.
 */
public class Report implements ReportModel {
    public Report() {
    }

    /**
     * Creates and initializes an instance of the report generator.
     * @param params Configuration parameters to be included in the report.
     * @param dir The directory to which to write the report.
     * @deprecated It is expected that you call writeReport() if you use this
     *             constructor.
     */
    public Report(InterviewParameters params, File dir) {
        this.params = params;
        //this.resultTable = params.getWorkDirectory().getTestResultTable();

        reportDir = dir.getAbsoluteFile();
    }

    /**
     * Creates and initializes an instance of the report generator.
     * @param params Configuration parameters to be included in the report.
     * @param dir The directory to which to write the report.
     * @param tf The test filter to be used to filter out tests in the report.
     * @deprecated It is expected that you call writeReport() if you use this
     *             constructor.
     */
    public Report(InterviewParameters params, File dir, TestFilter tf) {
        this(params, dir);

        paramFilters = new TestFilter[] { tf };
    }

    /**
     *
     * @deprecated use writeReports(ReportSettings s, File dir)
     */
    @Deprecated
    public void writeReport(Report.Settings s, File dir) throws IOException {
        writeReports(s.getSettings(), dir);
    }

    /**
     * Write report files using the given settings, to the given location.
     * This is the execution entry point for GUI mode.  The settings used
     * are written into the JT Harness preferences automatically at the end
     * of this method, unless this method exits with an exception.
     * @param s Settings to use for this report run, never null.
     * @param dir Output location, never null.
     * @throws IOException May occur at any time during the writing of the reports
     *     or creating the directories to store them in.
     */
    public void writeReports(ReportSettings s, File dir) throws IOException {
        reportDir = dir.getAbsoluteFile();
        // ensure test result table is stable
        s.getInterview().getWorkDirectory().getTestResultTable().waitUntilReady();

        backupReports(reportDir, s);

        // can be optimized - needs for HTML, plain
        s.setupSortedResults();
        s.setupKfl();

        ArrayList<ReportLink> links = new ArrayList<ReportLink>();

        try {
            // this is not a static field because service providers are not stateless
            ServiceLoader<ReportFormat> reportLoader = ServiceLoader.load(ReportFormat.class);
            for (ReportFormat rf : reportLoader) {
                boolean wasInterrapted = doReport(rf, s, links);
                if (wasInterrapted) return;
            }

            writePrefs(s); // write settings to Preferences

            if (Thread.currentThread().isInterrupted()) return;
        } finally {
            s.cleanup();
        }

        updateStaffFiles(reportDir, s, links);
    }

    /**
     * Process single ReportFormat and its sub-reports if any
     * @param rf
     * @param s
     * @param links
     * @return true if the process was interrupted
     * @throws RuntimeException
     * @throws IOException
     */
    private boolean doReport(ReportFormat rf, ReportSettings s, ArrayList<ReportLink> links) throws RuntimeException, IOException {
        if (rf.acceptSettings(s)) {
            try {
                links.add(writeReport(s, rf));
            }
            catch (RuntimeException t) {
                t.printStackTrace();
                throw t;
            }
            if (Thread.currentThread().isInterrupted()) return true;
        }
        for (ReportFormat sub: rf.getSubReports()) {
            boolean wasInterrapted = doReport(sub, s, links);
            if (wasInterrapted) return true;
        }
        return false;
    }


    /**
     * Writes a report about a set of test results.
     * This is the execution entry point for batch mode.  The default settings
     * from the preferences will be used.
     * @throws IllegalArgumentException if the type parameter does not
     *         identify a proper report type.
     * @param type The report type identifier, may be a custom type.
     *              null for default generated reports
     * @throws java.io.IOException If an error occurs writing any of the files.
     */
    public void writeReport(String type) throws IOException {
        // make a settings object - default settings
        //Settings settings = new Settings(params);
        ReportSettings settings = getSettingsPrefs();
        settings.setInterview(params);

        String[] typesToGen;
        if (type == null) {
            // if type is not specified generate html and plain text
            typesToGen = new String[] {"html", "txt"};
        } else {
            typesToGen = new String[] {type};
        }

        // this setting can originate in a legacy constructor
        if (paramFilters != null) {
            if (paramFilters.length == 1) {
                settings.filter = paramFilters[0];
            } else {
                settings.filter = new CompositeFilter(paramFilters);
            }
        }

        // ensure test result table is stable
        params.getWorkDirectory().getTestResultTable().waitUntilReady();

        backupReports(reportDir, settings);

        settings.setupSortedResults();
        settings.setupKfl();

        // I didn't make it as static calss field because
        // not sure that all service providers are stateless
        ServiceLoader<ReportFormat> reportLoader = ServiceLoader.load(ReportFormat.class);
        ArrayList<ReportLink> links = new ArrayList<ReportLink>();
        for (ReportFormat rf : reportLoader) {
            doCLReport(rf, settings, typesToGen, links);
        }
        if (links.isEmpty()) {
            throw new IllegalArgumentException("Unknown report type: " + type);
        }

        updateStaffFiles(reportDir, settings, links);
    }

    private void doCLReport(ReportFormat rf, ReportSettings settings,
        String[] types, ArrayList<ReportLink> links) throws IOException {
        String id = rf.getTypeName();
        for (String t : types) {
            if (t.toLowerCase().equals(id)) {
                links.add(writeReport(settings, rf));
            }
        }
        for (ReportFormat sub : rf.getSubReports()) {
            doCLReport(sub, settings, types, links);
        }
    }


    /**
     * Checks if the input directory contains JT Harness reports.
     * @param d The directory to be checked.
     * @return true if the directory contains JT Harness reports.
     */
    public static boolean isReportDirectory(File d) {

        String[] list = d.list();
        if (list == null)
            return false;

        for(int i = 0; i < list.length; i++) {
            if(list[i].equals(MARKER_FILE_NAME)) {
                return true;
            }
        }

        // no matches
        return false;
    }


// Report Backup methods-------------------------------------------------------------------

    /**
     * This is entry point to report backup mechanism.
     * Invokes methods, which rename existing report subdirs, index.html file;
     * updates index.html backupped versions to have correct links
     * Checks, if we able to perform backup
     * @param dir root report directory, where we perform backup
     * @param s Settings, collected for report creation
     */
    private void backupReports(File dir, ReportSettings s/*, Collection customReports*/) {
        if(s.isBackupsEnabled()) {
            BackupUtil.backupAllSubdirs(dir, s.backups);

            backupIndexFile(dir, s.backups);
        }
    }

    /**
     * Backups index.html file using BackupUtil.backupFile() method. Then update
     * links in backupped versions of index.html
     * @param dir Root reportDir, where index.html file situates
     * @param maxBackups maximum allowed number of backupped versions to exist
     */
    private void backupIndexFile(File dir, int maxBackups) {
        int nbackups = BackupUtil.backupFile(new File(dir, INDEX_FILE_NAME), maxBackups);

        for(int i = 1; i <= nbackups; i++) {
            updateIndexLinks(new File(dir, INDEX_FILE_NAME + "~" + Integer.toString(i) + "~"), i);
        }
    }

    /**
     * This is adapter only for backward compatibility purpose.
     * @deprecated Use com.sun.javatest.report.ReportSettings instead
     */
    @Deprecated
    public static class Settings {

        ReportSettings sett;

        @Deprecated
        public Settings(InterviewParameters params) {
            sett = new ReportSettings(params);
        }

        @Deprecated
        public void setEnableHtmlReport(boolean state) {
            sett.setEnableHtmlReport(state);
        }

        @Deprecated
        public void setHtmlMainReport(boolean reportHtml, boolean indexHtml) {
            sett.setHtmlMainReport(reportHtml, indexHtml);
        }

        @Deprecated
        public void setEnablePlainReport(boolean state) {
            sett.setEnablePlainReport(state);
        }

        @Deprecated
        public void setEnableXmlReport(boolean state) {
            sett.setEnableXmlReport(state);
        }

        @Deprecated
        public void setFilter(CompositeFilter compositeFilter) {
            sett.setFilter(compositeFilter);
        }

        @Deprecated
        public void setEnableBackups(boolean state) {
            sett.setEnableBackups(state);
        }

        @Deprecated
        public void setBackupLevels(int levels) {
            sett.setBackupLevels(levels);
        }

        @Deprecated
        public boolean isPlainEnabled() {
            return sett.isPlainEnabled();
        }

        ReportSettings getSettings() {
            return sett;
        }
    }

    private class LinkFinder {
        private File index;

        public LinkFinder(File index) {
            this.index = index;
        }

        public Vector getLinks() {
            Vector links = new Vector();
            StringBuilder content = new StringBuilder();
            BufferedReader r = null;
            try {
                r = new BufferedReader(new InputStreamReader(new FileInputStream(index)));
                String line;
                while( (line = r.readLine()) != null ) {
                    content.append(line);
                    content.append("\n");
                }
            } catch (IOException e) {}
            finally {
                try { if (r != null) r.close(); } catch (IOException e) {}
            }

            int i = 0;
            while (i < (content.length() - 1) ) {
                if(content.charAt(i) == '<' && content.charAt(i + 1) == 'a') {
                    StringBuilder link = new StringBuilder();
                    link.append(content.charAt(i));
                    i++;
                    link.append(content.charAt(i));
                    i++;

                    boolean end = false;
                    while (!end && i < (content.length() - 1) ) {
                        if (content.charAt(i) == '/' && content.charAt(i+1) == 'a') {
                            link.append(content.charAt(i));
                            i++;
                            link.append(content.charAt(i));
                            i++;
                            link.append(content.charAt(i));
                            i++;
                            end = true;
                        }
                        else {
                            link.append(content.charAt(i));
                            i++;
                        }
                    }
                    links.add(link.toString());
                }
                else {
                    i++;
                }
            }
            return links;
        }
    };


    /**
     * Parses backupped version of index.html file to update links, which points
     * to files in backupped subdirs. Searchs for subdirs with the same backup
     * suffix, as selected index.html has. Then checks all links in file to find
     * those pointing to backupped subdirs and updates them.
     * @param index backupped version of index.html file we work with
     * @param backupNumb index in backupped version of index.html file we work with
     */
    private void updateIndexLinks(File index, int backupNumb) {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(new FileInputStream(index)));
            String line;
            while( (line = r.readLine()) != null ) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {}
        finally {
            try { if (r != null) r.close(); } catch (IOException e) {}
        }

        String oldId = backupNumb == 1 ? "" : "~" + (backupNumb - 1) + "~";
        String newId = "~" + backupNumb + "~";

        File reportDir = index.getParentFile();
        File[] files = reportDir.listFiles();
        ArrayList subdirs = new ArrayList();
        for(int i = 0; i < files.length; i ++) {
            if(files[i].isDirectory() &&
                    files[i].getName().lastIndexOf(newId) != -1) {
                subdirs.add(files[i]);
            }
        }

        LinkFinder finder = new LinkFinder(index);
        Vector links = finder.getLinks();
        for(int i = 0; i < links.size(); i++) {
            String link = (String)links.get(i);
            for(int j = 0; j < subdirs.size(); j++) {
                String newName = ((File)subdirs.get(j)).getName();
                String oldName = newName.replaceAll(newId, oldId);
                if(link.lastIndexOf(oldName) != -1) {
                    StringBuilder newLink = new StringBuilder(link);
                    int link_start = newLink.indexOf(oldName);
                    newLink.replace(link_start, link_start + oldName.length(), newName);
                    int start = sb.indexOf(link);
                    sb.replace(start, start + link.length(), newLink.toString());
                    break;
                }
            }
        }

        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(index));
            writer.write(sb.toString());
            writer.flush();
            writer.close();
        }
        catch (IOException ex) {}
        finally {
            try { if (writer != null) writer.close(); } catch (IOException e) {}
        }
    }


    private void updateStaffFiles(File dir, ReportSettings s, List<ReportLink> links) {
        updateMarkerFile(dir);
        updateIndexFile(dir, s, links);
    }

    private void updateMarkerFile(File dir) {
        File f = new File(dir, MARKER_FILE_NAME);
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                return;
            }
        }
    }

    private void updateIndexFile(File dir, ReportSettings s, List<ReportLink> links) {
        File f = new File(dir, INDEX_FILE_NAME);
        if(f.exists()) {
            f.delete();
        }
        try {
            f.createNewFile();
            fillIndexFile(f, s, links);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillIndexFile(File index, ReportSettings s,  List<ReportLink> links) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(index));
            HTMLWriterEx out = new HTMLWriterEx(writer);
            out.setI18NResourceBundle(i18n);

            out.startTag(HTMLWriterEx.HTML);
            out.startTag(HTMLWriterEx.HEAD);
            out.writeContentMeta();
            out.startTag(HTMLWriterEx.TITLE);
            out.writeI18N("index.title");

            out.endTag(HTMLWriterEx.TITLE);
            out.endTag(HTMLWriterEx.HEAD);

            out.startTag(HTMLWriterEx.BODY);

            out.startTag(HTMLWriterEx.H1);
            out.write(s.getInterview().getTestSuite().getName());
            out.endTag(HTMLWriterEx.H1);

            out.newLine();
            Date date = new Date();
            SimpleDateFormat format= new SimpleDateFormat(i18n.getString("index.dateFormat"));
            out.write(i18n.getString("index.date.txt", format.format(date)));
            out.newLine();

            String[] predefinedFormats = {"html", "text", "xml", "cof"};
            for (String f : predefinedFormats) {
                putLink(f, links, out);
            }

            putCustomLink(predefinedFormats, links, out);

            out.endTag(HTMLWriterEx.BODY);
            out.endTag(HTMLWriterEx.HTML);
            out.flush();
            out.close();

        } catch (IOException e) {
            //return;
        }
        finally {
            try { if (writer != null) writer.close(); } catch (IOException e) {}
        }
    }

    private void putCustomLink(String[] predefinedFormats, List<ReportLink> links, HTMLWriterEx out) throws IOException {
        for (ReportLink ln : links) {
            if (!Arrays.asList(predefinedFormats).contains(ln.linkID)) {
                writeReportLink(out, ln);
            }
        }
    }

    private void putLink(String type, List<ReportLink> links, HTMLWriterEx out) throws IOException {
        for (ReportLink ln : links) {
            if (type.equalsIgnoreCase(ln.linkID)) {
                writeReportLink(out, ln);
            }
        }
    }

    private void writeReportLink(HTMLWriterEx out, ReportLink ln) throws IOException {
        out.startTag(HTMLWriterEx.P);
        out.writeLink(ln.linkFile, ln.linkText);
        out.startTag(HTMLWriterEx.BR);
        out.write(ln.linkDesk);
    }


// END Report Backup methods-------------------------------------------------------

    /**
     * Gets the report directory that is currently defined.
     * @return The report directory.
     */
    public File getReportDir() {
        return reportDir;
    }

    public static String[] getHtmlReportFilenames() {
        return HTMLReport.getReportFilenames();
    }


    private ReportLink writeReport(ReportSettings settings, ReportFormat rf) throws IOException {
        File out = new File(reportDir, rf.getBaseDirName());
        out.mkdir();
        notifyStartGenListeners(settings, rf.getReportID());
        return rf.write(settings, out);
    }

    private void notifyStartGenListeners(ReportSettings s, String reportID) {
        if(startGenListeners != null) {
            for(int i = 0; i < startGenListeners.size(); i ++) {
                StartGenListener sgl = (StartGenListener)startGenListeners.get(i);
                sgl.startReportGeneration(s, reportID);
            }
        }
    }


    public static void writePrefs(ReportSettings s) {
        Preferences prefs = Preferences.access();
        s.write(prefs);
    }

    public static ReportSettings getSettingsPrefs() {
        Preferences prefs = Preferences.access();
        return ReportSettings.create(prefs);
    }

    public interface CustomReportManager {
        CustomReport[] getCustomReports();
    }

    public interface StartGenListener {
        void startReportGeneration(ReportSettings s, String reportID);
    }

    public void addStartGenListener(StartGenListener l) {
        if(startGenListeners == null) {
            startGenListeners = new ArrayList();
        }

        startGenListeners.add(l);
    }

    public void removeStartGeneratingListener(StartGenListener l)  {
        if(startGenListeners != null) {
            startGenListeners.remove(l);
        }
    }

    //---------- data members -----------------------------------------------

    private InterviewParameters params;     // legacy
    private TestFilter[] paramFilters;      // legacy

    private File reportDir;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(Report.class);

    public static final String MARKER_FILE_NAME = "reportdir.dat";
    public static final String INDEX_FILE_NAME = "index.html";

    private ArrayList startGenListeners;
}
