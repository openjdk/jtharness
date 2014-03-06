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
package com.sun.jct.utils.copyrightcheck;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

public class BaseTask extends MatchingTask {
    private File srcDir;
    private File reportFile;
    private boolean failOnError = true;
    private TaskListener reporter;
    private ConsoleTaskListener consoleListener = new ConsoleTaskListener();
    private List listeners = new ArrayList();
    {
        // at least one listener is always present
        listeners.add(consoleListener);
    }

    /**
     * Set the source dir to find the source text files.
     */
    public void setDir(File dir) {
        this.srcDir = dir;
    }

    /**
     * @return Returns the source dir.
     */
    public File getDir() {
        return srcDir;
    }

    /**
     * @param failOnError The failOnError to set.
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * @return Returns the failOnError.
     */
    public boolean isFailOnError() {
        return failOnError;
    }

    /**
     * @param reportFile The reportFile to set.
     */
    public void setReportFile(File reportFile) {
        this.reportFile = reportFile;
        reporter = new XmlReporter(this, reportFile);
        listeners.add(reporter);
        // only basic info goes to console if there is a file report
        consoleListener.setVerbose(false);
    }

    /**
     * @return Returns the reportFile.
     */
    public File getReportFile() {
        return reportFile;
    }

    /**
     * Main execution entry.
     * @throws BuildException
     */
    public void execute() throws BuildException {
        // first off, make sure that we've got a valid dir
        if (getDir() == null) {
            throw new BuildException(getTaskName() + ": dir attribute must be set!");
        }
        if (!getDir().exists()) {
            throw new BuildException(getTaskName() + ": Dir does not exist: " + getDir());
        }
        if (!getDir().isDirectory()) {
            throw new BuildException(getTaskName() + ": dir is not a directory!");
        }

        taskStarted(this);
    }

    private void taskStarted(BaseTask task) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            TaskListener listener = (TaskListener) iter.next();
            listener.taskStarted();
        }
    }

    protected final void errorInFile(File file, String errorMessage) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            TaskListener listener = (TaskListener) iter.next();
            listener.errorInFile(file, errorMessage);
        }
    }

    protected final void taskFinished() {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            TaskListener listener = (TaskListener) iter.next();
            listener.taskFinished();
        }
    }

    protected final void processingFile(File file) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            TaskListener listener = (TaskListener) iter.next();
            listener.processingFile(file);
        }
    }

    class ConsoleTaskListener implements TaskListener {
        private File currentFile = null;
        private int badFilesCount = 0;
        private int filesTotal = 0;
        private boolean verbose = true;
        private boolean isDirTask = BaseTask.this instanceof BaseDirTask;

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        public void taskStarted() {
            log("Start checking the dir: " + getDir());
        }
        public void taskFinished() {
            String out;
            if (badFilesCount > 0) {
                if (isDirTask) {
                    out = "FAIL: Errors found in dir: " + getDir();
                } else {
                    out = "FAIL: " + badFilesCount + " file(s) failed.";
                }
            } else {
                out = "Passed!";
            }
            if (filesTotal > 0) {
                out += " Files checked: " + filesTotal;
            }
            log(out);
        }
        public void errorInFile(File file, String errorMessage) {
            if (file != currentFile) {
                log("Failed: " + file, Project.MSG_ERR);
                currentFile = file;
                badFilesCount++;
            }
            log(" -- " + errorMessage,
                    (verbose ? Project.MSG_ERR : Project.MSG_VERBOSE));
        }

        public void processingFile(File file) {
            log("Checking file: " + file, Project.MSG_VERBOSE);
            filesTotal++;
        }
    }
}

class XmlReporter implements TaskListener {
    Writer reportWriter = null;
    File reportFile = null;
    BaseTask task = null;
    String baseDir = null;
    File currentFile = null;

    XmlReporter(BaseTask task, File reportFile) {
        if (task == null || reportFile == null) {
            throw new BuildException(
                    "XmlReporter: task or report file are null");
        }
        this.task = task;
        this.reportFile = reportFile;
    }

    /**
     * Returns a String with XML special characters escaped.
     *
     * @param string The String to escape
     * @return The escaped version string.
     */
    private static String escapeString(String string) {
        StringWriter writer = new StringWriter(string.length());

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch(c) {
                case '&': writer.write("&amp;"); break;
                case '<': writer.write("&lt;"); break;
                case '>': writer.write("&gt;"); break;
                case '\'': writer.write("&apos;"); break;
                case '"': writer.write("&quot;"); break;
                default: writer.write(c);
            }
        }

        return writer.toString();
    }

    public void taskStarted() {
        try {
            baseDir = task.getDir().getAbsolutePath();
            if (!baseDir.endsWith(File.separator)) {
                baseDir += File.separator;
            }
            reportWriter = Utils.getFileWriter(reportFile);
            reportWriter.write(
                    "<?xml version=\"1.0\" encoding=\""
                    + Utils.ENCODING + "\" ?>\n"
                    + "<task name=\"" + task.getTaskName() + "\"\n"
                    + "\t\tbaseDir=\"" + baseDir + "\""
                    + "\t\tbaseDirURL=\"" + task.getDir().toURI().toURL() + "\""
                    + "\t\ttime=\""
                    + DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())
                    + "\">\n");
        } catch (IOException ioe) {
            close();
            throw new BuildException(ioe);
        }
    }

    public void taskFinished() {
        if (reportWriter == null) {
            return;
        }
        try {
            if (currentFile != null) {
                reportWriter.write("\t</file>\n");
            }
            reportWriter.write("</task>\n");
            reportWriter.flush();
        } catch (IOException e) {
            throw new BuildException(e);
        } finally {
            close();
            task.log("Report written to: " + reportFile.getAbsolutePath());
        }
    }

    public void errorInFile(File file, String errorMessage) {
        try {
            if (file != currentFile) {
                if (currentFile != null) { // this is not our first reported entry
                    reportWriter.write("\t</file>\n");
                }
                currentFile = file;
                // add trailing slash to directories
                String path = file.getAbsolutePath();
                if (file.isDirectory() && !path.endsWith(File.separator)) {
                    path += File.separator;
                }
                fileCheckFailed(path);
            }
            reportWriter.write(
                    "\t\t<error>" + escapeString(errorMessage) + "</error>\n");
        } catch (IOException e) {
            close();
            throw new BuildException(e);
        }
    }

    private void fileCheckFailed(String fileName) {
        try {
            if (fileName.startsWith(baseDir)) {
                fileName = fileName.substring(baseDir.length());
                if (fileName.equals("")) {
                    fileName = File.separator;
                }
            }
            reportWriter.write("\t<file name=\"" + fileName + "\">\n");
        } catch (IOException e) {
            close();
            throw new BuildException(e);
        }

    }

    void close() {
        if (reportWriter != null) {
            try {
                reportWriter.close();
                reportWriter = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void processingFile(File file) {
        // do nothing
    }
}
