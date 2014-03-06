/*
 * $Id$
 *
 * Copyright (c) 2006, 2012, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.swing.JPanel;
import org.xml.sax.SAXException;

//import com.sun.javatest.util.I18NResourceBundle;

/**
 * API to specify a customized report type for the Test Manager.
 */
public abstract class CustomReport {
    public CustomReport() {
    }

    /**
     * Will this report need the standard XML data report if it is run.  The
     * default implementation returns <code>false</code>.  Returning false does
     * not mean that the report cannot ask for the XML, but by correctly
     * answering, the harness may be able to apply some optimization based on
     * which other reports are being generated.
     * @return True if the XML report will be requested, false otherwise.
     * @see #getXmlReport
     */
    public boolean needsXmlReport() {
        return false;
    }

    /**
     * Request the content of the standard XML report from the harness.
     * This method will always succeed, unless insufficient information
     * exists to generate a report.  Should only be called subsequent to
     * a call from the harness to one of the <code>createReport</code>
     * methods.
     * @see #needsXmlReport
     */
    public final InputStream getXmlReport() throws IOException, SAXException {
        if (this.needsXmlReport()) {
            File xmlSrc = null;
            if (env.xmlReportFile != null) {
                xmlSrc = env.xmlReportFile;
            } else if (env.tmpXmlReportFile != null) {
                xmlSrc = env.tmpXmlReportFile;
            } else {
                // make it!
                env.tmpXmlReportFile = File.createTempFile("jtrep", "tmp");
                FileOutputStream fos = new FileOutputStream(env.tmpXmlReportFile);
                try {
                    Writer fw = new BufferedWriter(new OutputStreamWriter(fos, XMLReportMaker.XML_CHARSET));
                    XMLReport xr = new XMLReport();
                    xr.write(fw, env);
                    fw.close();
                }
                finally {
                    if (fos != null){
                        try { fos.close(); } catch (IOException e) { }
                    }
                }
                xmlSrc = env.tmpXmlReportFile;
            }
            return new FileInputStream(xmlSrc);
        }
        return null;
    }

    /**
     * Create a report within the context of a GUI.
     * The Interview parameter gives access to the WorkDirectory and other
     * data structures required to generate a useful report.  This is the
     * primary method which needs to be overridden to create your report.  Be
     * sure to call the superclass method when overriding.
     * @param rootDir The directory to write the report to.
     * @return The filename of a file that should be displayed if possible.
     *         Currently, only HTML (with simple CSS), plain text and RTF
     *         can be rendered.
     * @throws com.sun.javatest.report.CustomReport.ReportException - if the report creation was unsuccessful.
     *         Harness can show Exception's text
     */
    public abstract File createReport(File rootDir) throws ReportException;

    /**
     * Write a report without the context of a GUI.  All information needed
     * to generate the report should come from the given parameters and
     * the Preferences subsystem if needed.  Code in this method should be
     * careful not to provoke any GUI initialization because the AWT and Swing
     * toolkits may be unable to run in the current environment.  Be
     * sure to call the superclass method when overriding.
     * @param args Arguments given to this report format.  It is encoded into
     *             a single string, the format of which is defined by the
     *             report implementation.  In general though, the arg string
     *             must be absent of spaces, and should not require any
     *             characters which require escaping on common platforms
     *             (semicolon, ampersand, exclamation, any quotation marks).
     * @param rootDir The directory to write the report to.
     * @param ip The interview which should be report on.
     * @param filter The test filter that should be applied when iterating
     *               over the results to place in the report.  If the report
     *               relies on the XML report, this parameter can usually be
     *               ignored since the XML report itself will have been filtered
     *               already.
     * @return The filename of a file that should be displayed if possible.
     *         Currently, only HTML (with simple CSS), plain text and RTF
     *         can be rendered.
     * @see com.sun.javatest.tool.Preferences
     * @throws com.sun.javatest.report.CustomReport.ReportException - if the report creation was unsuccessful.
     *         Harness can show Exception's text
     */
    public abstract File createReport(String args, File rootDir,
                             InterviewParameters ip, TestFilter filter)
        throws ReportException;


    /**
     * Get a short internal name for this report type.  Should be
     * unique within a test suite's set of report types.  The value
     * should be a valid Java identifier.  This value could be used
     * by the user to identify the report type on the command line, but
     * otherwise will not generally be visible to the user.
     *
     * This value is also used to generate a unique directory name
     * within the report directory when writing the report.  So, it will
     * be used when constructing the <code>rootDir</code> parameter
     * of the <code>createReport</code> methods.
     * @see #getName
     * @see java.lang.Character#isJavaIdentifierStart(char)
     * @see java.lang.Character#isJavaIdentifierPart(char)
     */
    public abstract String getReportId();

    /**
     * Get a short name for this report type.
     * <i>The implementation should internationalize this value.</i>
     * This is the name that the user will generally see.  There are not
     * restrictions on the characters in the name, it may contain spaces,
     * etc.  It should be kept short, since it is likely to be presented
     * in GUI lists and tabs.  Example value: <i>JT Harness Extended XML</i>
     * @return Internationalized name of this report.  Should never be null
     *         or zero-length string.
     */
    public abstract String getName();

    /**
     * Get a longer description about the purpose of this report type.
     * <i>The implementation should internationalize this value.</i>
     * This may be presented to the user as inline help in the GUI, a
     * "Info" popup, etc...  The text should be descriptive about who
     * would use the report, why, how it is different than other reports,
     * the format of the report data (XML, binary, etc...).  The length
     * of the description could be up to a full paragraph in length.
     * Do not include any special formatting in the string (newlines,
     * HTML, etc...).
     * @return Internationalized description of this report.  Should
     *         never be null or zero-length string.
     */
    public abstract String getDescription();

    public ReportSettings getReportEnviroment() {
        return env;
    }


    // ------- GUI Related -------

    /**
     * Get report options panes.
     * These panes can be used by the user to configure the report.
     * These panes should be unique to this instance of the custom report,
     * and should not be shared across many instances.
     */
    public ReportConfigPanel[] getOptionPanes() {
        return null;
    }

    /**
     * Validate the options currently in the option panes.
     * This method would only be called after <code>getOptionPanes</code>.
     * It can be assumed that this method is called just before
     * <code>createReport</code> to ensure that the user's settings are valid (in
     * GUI mode).  It is not called in non-GUI mode.
     * @return Null if the current options are valid.  If something is invalid,
     *         the return value is a short message indicating what setting the
     *         user should correct.  The message should not be longer than a
     *         sentence or two.
     * @see #getOptionPanes
     * @see CustomReport.ReportConfigPanel
     */
    public abstract String validateOptions();

    /**
     * Dispose of any data or components which this class may be
     * holding references to.  Implementations should set references to
     * data structures to null.  The assumption is that this object is no
     * longer usable after this method is called.  <b>Be sure to call the
     * superclass implementation if you override this method.</b>
     */
    public void dispose() {
        env = null;
    }

    /**
     * The class ReportException indicates an error during report creation.
     * The harness can print or show Exception's text
     */
    public class ReportException extends IOException {

        /**
         * Constructs a new exception with the specified detail message.
         */
        public ReportException(String message) {
            super(message);
        }
    }

    /**
     * Configuration panel for a report.  It is recommended that you set
     * the name of this component to something unique so that it can be
     * identified when browsing the component tree (during development).
     *
     * Do not forget to call the superclass constructor if you override
     * the constructor.
     */
    public static abstract class ReportConfigPanel extends JPanel {
        public ReportConfigPanel() {
            setFocusable(false);
        }

        /**
         * Get a short name of this option pane.
         * For display on a tab or list in the GUI.
         * <i>The implementation should internationalize this value.</i>
         * @return A short localized name to be shown to the user.
         */
        public abstract String getPanelName();
    }


    public void setEnviroment(ReportSettings envir) {
        env = envir;
    }

    private ReportSettings env;

    //private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ContextManager.class);
}
