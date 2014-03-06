/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest;

import java.io.PrintWriter;
import java.util.Iterator;

import com.sun.javatest.httpd.httpURL;
import com.sun.javatest.httpd.JThttpProvider;
import com.sun.javatest.httpd.PageGenerator;
import com.sun.javatest.util.Debug;
import com.sun.javatest.util.I18NResourceBundle;

/*
 * HTTP service provider for TestResultTable.
 */
class TRT_HttpHandler extends JThttpProvider {
    TRT_HttpHandler(TestResultTable trt, String url, int instanceNum) {
        this.instanceNum = instanceNum;
        this.trt = trt;
    }

    public void serviceRequest(httpURL requestURL, PrintWriter out) {
        String nf = requestURL.getNextFile();

        // start the document
        if (nf == null) {
            beginGood(out);
            PageGenerator.writeBeginDoc(out);

            printIndex(out);
        }
        else if (nf.equals("tests")) {
            beginGood(out);
            PageGenerator.writeBeginDoc(out);
            printTests(requestURL, out);
        }
        else {
            if (debug)
                Debug.println("TRT.HH-remainder of URL unknown (" + nf + ")");
            beginBad(out);
            printIndex(out);
        }

        out.println("<br><hr>");
        PageGenerator.writeFooter(out);
        PageGenerator.endBody(out);
        PageGenerator.writeEndDoc(out);

        out.close();
    }

    public String getRegistredURL() {
        return file;
    }

    private void beginGood(PrintWriter out) {
        PageGenerator.generateOkHttp(out);
        PageGenerator.generateDocType(out, PageGenerator.HTML32);
    }

    private void beginBad(PrintWriter out) {
        PageGenerator.generateBadHttp(out);
        PageGenerator.generateDocType(out, PageGenerator.HTML32);
    }

    private void printIndex(PrintWriter out) {
        PageGenerator.writeHeader(out, i18n.getString("trtHttp.index.title"));
        PageGenerator.startBody(out);
        // heading
        out.print("<h2>");
        print(out, i18n.getString("trtHttp.index.hdr"));
        out.println("</h2>");
        out.println("<hr Width=\"40%\" Align=left>");

        out.println("<p>");
        printStats(out);

    }

    private void printTests(httpURL url, PrintWriter out) {
        PageGenerator.writeHeader(out, i18n.getString("trtHttp.tests.title"));
        PageGenerator.startBody(out);
        out.print("<h2>");
        print(out, i18n.getString("trtHttp.tests.hdr"));
        out.println("</h2>");
        out.println("<hr Width=\"40%\" Align=left>");

        out.println("<p>");
        writeTests(out, i18n.getString("trtHttp.tests.name"),
                   i18n.getString("trtHttp.tests.status"));
    }

    public void writeTests(PrintWriter out, String keyHeader, String valHeader) {
        // XXX should include HTML filtering of strings
        // this is a custom version of the code found in PageGenerator

        out.println("<Table Border>");

        StringBuffer buf = new StringBuffer(50);

        // write the table header
        buf.append("<tr><th>");
        buf.append(keyHeader);
        buf.append("<th>");
        buf.append(valHeader);
        buf.append("</tr>");
        out.println(buf.toString());

        /*
        if (dict == null || dict.size() == 0) {
            // no values to write, fill the space
            buf.setLength(0);
            buf.append("<tr><td colspan=2>");
            buf.append("-EMPTY-");
            buf.append("</tr>");
        }
        else {
            */
            Iterator it = trt.getIterator();
            while (it.hasNext()) {
                TestResult tr = (TestResult)(it.next());
                String url;
                try {
                    url = tr.getDescription().getRootRelativeURL();
                }
                catch(TestResult.Fault f) {
                    out.println("<tr><td>Unable to get TestResult info, aborting.</tr>");
                    if (debug) f.printStackTrace();
                    // exit the while loop
                    break;
                }

                out.println("<tr>");
                buf.setLength(0);
                buf.append("<td>");
                buf.append(url);
                buf.append("<td>");
                buf.append(tr.getStatus().toString());
                out.println(buf.toString());
                out.println("</tr>");
            }   // while
        //}

        out.println("</Table>");
    }

    private void printStats(PrintWriter out) {
        out.print("<b>");
        print(out, i18n.getString("trtHttp.stats.hdr"));
        out.println("</b><br>");
        PageGenerator.startTable(out, false);

        // workdir
        out.println("<tr>");
        out.print("   <td>");
        print(out, i18n.getString("trtHttp.wd.hdr"));
        out.println("</td>");
        out.print("   <td>");
        print(out, (trt.getWorkDir() != null ? trt.getWorkDir().getPath() :
                i18n.getString("trtHttp.wd.unset")));
        out.println("</td>");

        // size
        out.println("<tr>");
        out.print("   <td>");
        print(out, i18n.getString("trtHttp.size.hdr"));
        out.println("</td>");

        out.print("   <td><a href=\"/trt/");
        out.print(Integer.toString(instanceNum));
        out.print("/tests\">");
        out.print(Integer.toString(trt.size()));
        out.println("</a></td>");
        out.println("</tr>");

        PageGenerator.endTable(out);
    }

    private TestResultTable trt;
    private int instanceNum;
    private String file;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TRT_HttpHandler.class);
    private static boolean debug = Debug.getBoolean(TRT_HttpHandler.class);
}
