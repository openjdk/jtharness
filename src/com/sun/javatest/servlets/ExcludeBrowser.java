/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sun.javatest.ExcludeList;
import com.sun.javatest.util.StringArray;

public class ExcludeBrowser extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res)
                throws ServletException, IOException {
        String uri = req.getRequestURI();
        File file = new File(req.getRealPath(uri));

        if (!file.exists()) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        ExcludeList excludeList;
        try {
            excludeList = new ExcludeList(file);
        }
        catch (ExcludeList.Fault e) {
            String msg =
                "The file does not appear to be a valid exclude-list file. " +
                "The following exception was received while trying to open it: " +
                e.toString();
            res.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
            return;
        }
        catch (IOException e) {
            String msg =
                "The file does not appear to be a valid exclude-list file. " +
                "The following exception was received while trying to open it: " +
                e.toString();
            res.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
            return;
        }

        String bugLink = getInitParameter("bugLink");

        res.setContentType("text/html");
        PrintWriter out = new PrintWriter(res.getOutputStream());
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + file + "</title>");
        out.println("</head>");
        out.println("<body>");

        if (excludeList.size() == 0)
            out.println("Exclude list is empty.");
        else {
            out.println("<table border=1>");
            for (Iterator iter = excludeList.getIterator(false); iter.hasNext(); ) {
                ExcludeList.Entry entry = (ExcludeList.Entry) (iter.next());
                String[] bugIds = entry.getBugIdStrings();
                StringBuffer bugIdText = new StringBuffer();
                for (int i = 0; i < bugIds.length; i++) {
                    if (i > 0)
                        bugIdText.append(" ");
                    String b = bugIds[i];
                    if (bugLink == null)
                        bugIdText.append(b);
                    else
                        bugIdText.append("<a href=\"" + bugLink + b + "\">" + b + "</a>");
                }
                out.print("<tr>");
                out.print("<td>" + entry.getRelativeURL());
                if (entry.getTestCases() != null) {
                    out.print("[" + entry.getTestCases() + "]");
                }
                out.print("<td>" + bugIdText);
                out.print("<td>" + StringArray.join(entry.getPlatforms()));
                out.print("<td>" + entry.getSynopsis());
                out.println();
            }
            out.println("</table>");
        }

        out.println("<p><hr>");

        // trailer
        out.println("File: <em>" + file + "</em>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
}
