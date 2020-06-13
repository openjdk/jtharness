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

import com.sun.javatest.util.I18NResourceBundle;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * A segment of the main top level HTML report.
 */
abstract class HTMLSection {
    protected String name;
    protected File reportDir;
    protected ReportSettings settings;
    protected HTMLReport parent;
    protected File workDirRoot;

    HTMLSection(String name, ReportSettings settings, File reportDir, HTMLReport parent) {
        this.name = name;
        this.settings = settings;
        this.reportDir = reportDir;
        this.parent = parent;

        workDirRoot = this.settings.getInterview().getWorkDirectory().getRoot();

        String workPath;
        String reportDirPath;

        try {
            workPath = workDirRoot.getCanonicalPath();
            reportDirPath = this.reportDir.getCanonicalPath();
        } catch (IOException e) {
            workPath = workDirRoot.getPath();
            reportDirPath = this.reportDir.getPath();
        }

        if (!workPath.endsWith(File.separator)) {
            workPath += File.separator;
        }

        if (reportDirPath.startsWith(workPath)) {
            // since reportFile is in reportDir, reset path to be relative
            File d = this.reportDir;
            StringBuilder sb = new StringBuilder();
            try {
                while (d != null && !d.getCanonicalPath().equals(workDirRoot.getCanonicalPath())) {
                    sb.append("../");
                    d = d.getParentFile();
                }
            } catch (IOException e) {
                d = null;
            }
            if (d != null) {
                workDirRoot = new File(sb.toString());
            }
        }
    }

    Writer openWriter(int reportCode) throws IOException {
        return parent.openWriter(reportDir, reportCode);
    }

    String getName() {
        return name;
    }

    void writeContents(ReportWriter repWriter) throws IOException {
        repWriter.writeLink('#' + name, name);
    }

    void writeSummary(ReportWriter repWriter) throws IOException {
        repWriter.startTag(HTMLWriterEx.H2);
        repWriter.writeLinkDestination(name, name);
        repWriter.endTag(HTMLWriterEx.H2);
    }

    void writeExtraFiles() throws IOException {
    }

    protected ReportWriter openAuxFile(int reportCode, String title,
                                       I18NResourceBundle i18n) throws IOException {
        return new ReportWriter(openWriter(reportCode), title, i18n);
    }
}
