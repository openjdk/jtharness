/*
 * $Id$
 *
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sun.javatest.Status;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;
import com.sun.javatest.util.StringArray;

public class ResultBrowser extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res)
                throws ServletException, IOException {
        String uri = req.getRequestURI();
        File file = new File(req.getRealPath(uri));

        if (!file.exists()) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        TestResult tr;
        try {
            tr = new TestResult(file);
        }
        catch (TestResult.Fault e) {
            String msg =
                "File does not appear to be a valid test result file. " +
                "The following exception was received while trying to open it: " +
                e.getLocalizedMessage();
            res.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
            return;
        }

        res.setContentType("text/html");
        PrintWriter out = new PrintWriter(res.getOutputStream());
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + file + "</title>");
        out.println("</head>");
        out.println("<body>");

        // header
        try {
            TestDescription td = tr.getDescription();
            out.println("<h1>Test Results: " + td.getRootRelativeURL() + "</h1>");
        }
        catch (TestResult.Fault e) {
            out.println("<h1>(Unknown test)</h1>");
        }

        String[] colors = {"lime", "red", "yellow", "aqua", null};
                // color names are per HTML 3.2 specification; see http://www.w3.org/TR/REC-html32.html
        String[] outcomes = {"Passed", "Failed", "Check output", "Error", "Not run"};
        Status status = tr.getStatus();
        String color = colors[status.getType()];
        String outcome = outcomes[status.getType()];
        out.println("<table cellpadding=5><tr><td" + (color != null ? " bgcolor=" + color : "") +">" +
                    "<b>" + outcome + "</b><td><b>" + status.getReason() + "</b></table>");

        out.println("<ul>");
        out.println("<li><a href=\"#td\">Test Description properties</a>");
        out.println("<li><a href=\"#tr\">Test Result properties</a>");
        out.println("<li><a href=\"#tr\">Test Environment</a>");
        out.println("<li><a href=\"#output\">Test Output</a>");
        out.println("<ul>");
        for (int i = 0; i < tr.getSectionCount(); i++) {
            try {
                TestResult.Section s = tr.getSection(i);
                out.println("<li><a href=\"#output-" + s.getTitle() + "\">" + s.getTitle() + "</a>");
            }
            catch (TestResult.Fault f) {
                out.println("The following exception occurred while trying to determine the test description: " + f.getLocalizedMessage());
            }
        }
        out.println("</ul>");
        out.println("</ul>");
        out.println("<p><hr>");

        // test description properties
        out.println("<h2><a name=td>Test Description properties</a></h2>");
        try {
            TestDescription td = tr.getDescription();
            out.println("<table>");
            for (Iterator iter = td.getParameterKeys(); iter.hasNext(); ) {
                String key = (String) (iter.next());
                String value = td.getParameter(key);
                if (key.equals("$root") || key.equals("$file") || key.equals("testsuite") || key.equals("file"))
                    out.println("<tr><td align=top>" + key + "<td><a href=\"" + value + "\">" + filter(value, false) + "</a>");
                else if (key.equals("source")) {
                    out.println("<tr><td align=top>" + key + "<td>");
                    String[] srcs = StringArray.split(value);
                    if (srcs != null) {
                        File tdFile = td.getFile();
                        String tdFilePath = (tdFile == null ? null : tdFile.getPath());
                        String tdDir = (tdFilePath == null ? null : tdFilePath.substring(0, tdFilePath.lastIndexOf('/') + 1)); // File.separator?
                        for (int i = 0; i < srcs.length; i++) {
                            if (tdDir == null)
                                out.println(srcs[i]);
                            else
                                out.println("<a href=\"" + tdDir + srcs[i] + "\">" + srcs[i] + "</a>");
                        }
                    }
                }
                else
                    out.println("<tr><td align=top>" + key + "<td>" + filter(value, true));
            }
            out.println("</table>");
        }
        catch (TestResult.Fault e) {
            out.println("The following exception occurred while trying to determine the test description: " + e.getLocalizedMessage());
        }
        out.println("<p><hr>");

        // test result properties
        out.println("<h2><a name=tr>Test Result properties</a></h2>");
        try {
            out.println("<table>");
            for (Enumeration e = tr.getPropertyNames(); e.hasMoreElements(); ) {
                String key = (String)(e.nextElement());
                out.println("<tr><td>" + key + "<td>" + filter(tr.getProperty(key), true));
            }
        }
        catch (TestResult.Fault e) {
            out.println("The following exception occurred while trying to determine the test result properties: " + e.getLocalizedMessage());
        }
        finally {
            out.println("</table>");
        }
        out.println("<p><hr>");

        // test environment
        out.println("<h2><a name=env>Test Environment</a></h2>");
        try {
            Map env = tr.getEnvironment();
            if (env.size() == 0) {
                out.println("<tr><td>No environment details found");
            }
            else {
                out.println("<table>");
                for (Iterator i = env.entrySet().iterator(); i.hasNext(); ) {
                    Map.Entry e = (Map.Entry) (i.next());
                    String key = (String) (e.getKey());
                    String value = (String) (e.getValue());
                    out.println("<tr><td>" + key + "<td>" + filter(value, true));
                }
                out.println("</table>");
            }
        }
        catch (TestResult.Fault e) {
            out.println("The following exception occurred while trying to determine the test environment: " + e.getLocalizedMessage());
        }
        out.println("<p><hr>");

        // output
        out.println("<h2><a name=output>Test Output</a></h2>");

        if (tr.getSectionCount() == 0)
            out.println("No output recorded.");
        else {
            try {
                for (int i = 0; i < tr.getSectionCount(); i++) {
                    TestResult.Section s = tr.getSection(i);
                    if (i > 0)
                        out.println("<p><hr align=left width=\"25%\">");
                    out.println("<h3><a name=\"output-" + s.getTitle() + "\">" + s.getTitle() + "</a></h3>");
                    String[] sects = s.getOutputNames();
                    for (int j = 0; j < sects.length; j++) {
                        if (!sects[j].equals("messages"))
                            out.println("<h4>Output: " + sects[j] + "</h4>");
                        String output = s.getOutput(sects[j]);
                        if (output.equals(""))
                        out.println("<em>(No output.)</em>");
                        out.println("<pre>" + output + "</pre>");
                    }
                    if (s.getStatus() != null) {
                        out.println("<h4>Status</h4>");
                        out.println(s.getStatus());
                    }
                }   // for
            }   // try
            catch (TestResult.ReloadFault f) {
                out.println("<b>Internal error while reading test results.</b>");
            }
        }
        out.println("<p><hr>");

        // trailer
        out.println("File: <em>" + file + "</em>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    private String filter(String s, boolean newlines) {
        if (s.indexOf('<') == -1 && s.indexOf('>') == -1 && (!newlines || (s.indexOf('\n') == -1)))
            return s;
        else {
            StringBuffer sb = new StringBuffer(s.length() * 2);
            for (int i = 0; i < s.length(); i++) {
                char c;
                switch (c = s.charAt(i)) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '\n':
                    if (newlines)
                        sb.append("<br>");
                    sb.append(c);
                    break;
                default:
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}
