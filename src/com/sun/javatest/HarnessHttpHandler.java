/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import com.sun.interview.WizPrint;
import com.sun.javatest.httpd.JThttpProvider;
import com.sun.javatest.httpd.PageGenerator;
import com.sun.javatest.httpd.RootRegistry;
import com.sun.javatest.httpd.httpURL;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.PropertyArray;
import com.sun.javatest.util.StringArray;

class HarnessHttpHandler extends JThttpProvider
    implements Harness.Observer
{
    HarnessHttpHandler(Harness harness) {
        this.harness = harness;
        harness.addObserver(this);
    }

    public void serviceRequest(httpURL requestURL, PrintWriter out) {
        String nf = requestURL.getNextFile();
        if (nf == null) {                           // request for root
            beginGood(out);
            printHtml(out);
        }
        else if (nf.equals("env")) {                // environment dump
            beginGood(out);
            printEnv(out);
        }
        else if (nf.equals("interview")) {          // interview dump
            beginGood(out);
            printInterview(out);
        }
        else if (nf.equals("text"))                 // get text only data
            printText(requestURL, out);
        else if (nf.equals("stop")) {       // stop the harness
            if (!harness.isRunning()) {
                println(out, i18n.getString("harnessHttp.noStop"));
            }
            else {
                // generate the token if it hasn't already been
                beginGood(out);
                if (magicToken == null) {
                    long num = Math.round(Math.floor(Math.random() * 30000.0 ));
                    magicToken = Integer.toString((int)num);
                }

                PageGenerator.writeHeader(out, i18n.getString("harnessHttp.stop.hdr"));
                PageGenerator.startBody(out);

                String token = requestURL.getValue("token");
                if (token == null)
                    printStopConfirm(out);
                else if (token.equals(magicToken)) {
                    harness.stop();
                    println(out, i18n.getString("harnessHttp.stopped"));
                }
                else {
                    printStopErr(out);
                }

                out.println("<hr>");
                PageGenerator.writeFooter(out);
                PageGenerator.endBody(out);
                PageGenerator.writeEndDoc(out);
            }
        }
        else {                              // bad url
            if (debug)
                System.out.println("TRT.HH-remainder of URL unknown (" + nf + ")");

            beginBad(out);
            printHtml(out);
        }

        out.close();
    }

    private void beginGood(PrintWriter out) {
        PageGenerator.generateOkHttp(out);
        PageGenerator.generateDocType(out, PageGenerator.HTML32);
    }

    private void beginBad(PrintWriter out) {
        PageGenerator.generateBadHttp(out);
        PageGenerator.generateDocType(out, PageGenerator.HTML32);
    }

    /**
     * Print the basic HTML page for Harness.
     */
    private void printHtml(PrintWriter out) {
        PageGenerator.writeBeginDoc(out);

        printIndex(out);

        out.println("<hr>");
        PageGenerator.writeFooter(out);
        PageGenerator.endBody(out);
        PageGenerator.writeEndDoc(out);
    }

    /**
     * Print the environment settings in formatted HTML.
     */
    private void printEnv(PrintWriter out) {
        PageGenerator.writeBeginDoc(out);
        PageGenerator.writeHeader(out, i18n.getString("harnessHttp.env.title"));

        out.print("<h3>");
        print(out, i18n.getString("harnessHttp.env.hdr"));
        out.println("</h3>");

        Parameters params = harness.getParameters();
        if (params != null)
            printEnv(out, params.getEnv());
        else
            out.println(i18n.getString("harnessHttp.env.none"));

        out.println("<hr>");
        PageGenerator.writeFooter(out);
        PageGenerator.endBody(out);
        PageGenerator.writeEndDoc(out);
    }

    /**
     * Print the interview settings in formatted HTML.
     */
    private void printInterview(PrintWriter out) {
        PageGenerator.writeBeginDoc(out);
        PageGenerator.writeHeader(out, i18n.getString("harnessHttp.interview.title"));

        out.print("<h3>");
        print(out, i18n.getString("harnessHttp.interview.hdr"));
        out.println("</h3>");

        try {
            Parameters params = harness.getParameters();
            if (params == null ||
                !(harness.getParameters() instanceof InterviewParameters)) {
                println(out, i18n.getString("harnessHttp.interview.none"));
            }
            else {
                InterviewParameters interview = (InterviewParameters)(harness.getParameters());
                WizPrint wp = new WizPrint(interview);
                wp.setShowResponses(true);
                wp.setShowResponseTypes(true);
                wp.setShowTags(true);
                wp.write(out);
            }   // else

            out.println("<hr>");
            PageGenerator.writeFooter(out);
            PageGenerator.endBody(out);
            PageGenerator.writeEndDoc(out);
        }   // try
        catch (IOException e) {
            println(out, i18n.getString("harnessHttp.ioProblem"));
        }   // catch
    }

    /**
     * Access to raw text output pages.
     * We choose to not including the standard HTTP response header when
     * returning plain text.  This makes it easier to process on the client side.
     */
    private void printText(httpURL requestURL, PrintWriter out) {
        // write directly to out in this method, we are not
        // transmitting HTML encoded data
        Parameters params = harness.getParameters();
        String nf = requestURL.getNextFile();
        if (nf == null) {
            beginGood(out);
            printIndex(out);
        }
        else if (nf.equals("env")) {
            // special case for incomplete interview?

            TestEnvironment env = params.getEnv();

            if (env == null) {
                print(out, i18n.getString("harnessHttp.env.none"));
                return;
            }

            /* does not fit in the properties style output
            out.print(i18n.getString("harnessHttp.env.name"));
            out.println(env.getName());
            */

            String[] pa = new String[0];

            for (Iterator i = env.elements().iterator(); i.hasNext(); ) {
                TestEnvironment.Element elem = (TestEnvironment.Element) (i.next());
                // this is stunningly inefficient and should be fixed
                pa = PropertyArray.put(pa, elem.getKey(), elem.getValue());
            }

            try {
                PropertyArray.save(pa, out);
            }
            catch (IOException e) {
                // this is probably useless since problems with out
                // probably caused this
                out.println(i18n.getString("harnessHttp.ioProblem"));
            }
        }
        else if (nf.equals("config")) {
            if (params == null) {
                out.print(i18n.getString("harnessHttp.text.unavail"));
                return;
            }

            // test suite info
            TestSuite ts = params.getTestSuite();
            out.print(i18n.getString("harnessHttp.text.ts.path"));
            out.print("=");
            if (ts != null) {
                out.println(ts.getPath());
            }
            else
                out.println(i18n.getString("harnessHttp.text.empty"));

            out.print(i18n.getString("harnessHttp.text.ts.name"));
            out.print("=");
            if (ts != null) {
                out.println(ts.getName());
            }
            else
                out.println(i18n.getString("harnessHttp.text.empty"));

            out.print(i18n.getString("harnessHttp.text.wd.val"));
            out.print("=");
            WorkDirectory wd = params.getWorkDirectory();
            if (wd != null)
                out.println(wd.getPath());
            else
                out.println(i18n.getString("harnessHttp.text.empty"));
        }
        else if (nf.equals("tests")) {
            if (params == null) {
                out.print(i18n.getString("harnessHttp.text.unavail"));
                return;
            }

            String[] tests = params.getTests();
            if (tests != null && tests.length != 0) {
                for (int i = 0; i < tests.length; i++) {
                    out.print("url");
                    out.print(Integer.toString(i));
                    out.print("=");
                    out.println(tests[i]);
                }   // for
            }
        }
        else if (nf.equals("stats")) {
            if (!harness.isRunning()) {
                out.println("");
                return;
            }

            stats[Status.NOT_RUN] = harness.getTestsFoundCount() - stats[Status.PASSED] -
                                stats[Status.FAILED] - stats[Status.ERROR];

            for (int i = 0; i < Status.NUM_STATES; i++) {
                out.print((Status.typeToString(i)).replace(' ', '_'));
                out.print("=");
                out.println(stats[i]);
            }   // for
        }
        else if (nf.equals("state")) {
            // provide information about the Harness' state
            print(out, i18n.getString("harnessHttp.state.run.val"));
            out.println(harness.isRunning());
        }
        else if (nf.equals("results")) {
            // test dump
            TestResultTable trt = harness.getResultTable();
            TestFilter[] filters = params.getFilters();
            String[] tests = params.getTests();

            Iterator it = null;
            if (tests == null || tests.length == 0)
                it = trt.getIterator(filters);
            else
                it = trt.getIterator(params.getTests(), filters);

            while (it.hasNext()) {
                TestResult tr = (TestResult)(it.next());
                out.println(tr.getTestName());
                out.println(tr.getStatus().toString());
            }   // while
        }
        else {
            if (debug)
                System.out.println("TRT.HH-remainder of URL unknown (" + nf + ")");

            println(out, i18n.getString("harnessHttp.badRequest", requestURL.getFullPath()));
        }
    }

    private void printStopConfirm(PrintWriter out) {
        out.print("<h2>");
        print(out, i18n.getString("harnessHttp.stopConfirm.hdr"));
        out.println("</h2>");
        out.print("<h4>");
        print(out, i18n.getString("harnessHttp.stopConfirm.txt"));
        out.println("</h4>");
        out.print("<Form method=get enctype=application/x-www-form-urlencoded>");

        out.print("<p><Input align=center type=submit value=");
        out.print(i18n.getString("harnessHttp.stopConfirm.btn"));
        out.println(">");
        out.print("<Input type=hidden name=token value=");
        out.print(magicToken);
        out.println("></Form>");
    }

    private void printStopErr(PrintWriter out) {
        out.println("<h2>");
        println(out, i18n.getString("harnessHttp.stopErr.hdr"));
        out.println("</h2>");
        out.println("<b>");
        println(out, i18n.getString("harnessHttp.stopErr.text1"));
        println(out, i18n.getString("harnessHttp.stopErr.text2"));
        out.println("</b>");
        out.println("<p>");
        println(out, i18n.getString("harnessHttp.stopErr.text3"));
        out.println("<a href=\"/harness/stop\">");
        println(out, i18n.getString("harnessHttp.stopErr.text4"));
        out.println("</a>");
        println(out, i18n.getString("harnessHttp.stopErr.text5"));
    }

    private void printIndex(PrintWriter out) {
        PageGenerator.writeHeader(out, i18n.getString("harnessHttp.index.title"));
        PageGenerator.startBody(out);
        out.println("<h2>");
        out.print("JT Harness &#8482; ");
        println(out, i18n.getString("harnessHttp.index.hdr"));
        out.println("</h2>");

        Parameters params = harness.getParameters();
        if (params == null) {
            out.println(i18n.getString("harnessHttp.parameters.noParams"));
            return;
        }

        // print test suite
        print(out, i18n.getString("harnessHttp.parameters.tsName"));
        String name = params.getTestSuite().getName();
        println(out, (name == null ?
                      i18n.getString("harnessHttp.parameters.noTs") :
                      name));

        out.println("<br>");
        print(out, i18n.getString("harnessHttp.parameters.tsPath"));
        File tsr = params.getTestSuite().getRoot();
        println(out, (tsr == null ?
                      i18n.getString("harnessHttp.parameters.noTs") :
                      tsr.getPath()));

        out.println("<br>");

        // print workdir
        print(out, i18n.getString("harnessHttp.parameters.wd"));

        WorkDirectory wd = params.getWorkDirectory();
        if (wd != null)
            println(out, wd.getPath());
        else
            print(out, i18n.getString("harnessHttp.parameters.noWd"));

        out.println("<p>");

        // print parameters
        printParameters(out, params);

        // print results link
        TestResultTable currentResults = harness.getResultTable();
        out.println("<h3>");
        println(out, i18n.getString("harnessHttp.results.hdr"));
        out.println("</h3>");
        if (currentResults != null) {
            JThttpProvider trtProv = RootRegistry.getObjectHandler(currentResults);
            if (trtProv != null && trtProv.getRootURL() != null) {
                out.print("<a href=\"");
                out.print(trtProv.getRootURL());
                out.print("\">");
                print(out, i18n.getString("harnessHttp.trt.link"));
                out.println("</a>");
            }
        }
        else {
            out.print(i18n.getString("harnessHttp.trt.none"));
        }
    }

    private void printParameters(PrintWriter out, Parameters params) {
        out.println("<h3>");
        println(out, i18n.getString("harnessHttp.parameters.hdr"));
        out.println("</h3>");

        print(out, i18n.getString("harnessHttp.parameters.env"));
        TestEnvironment tev = params.getEnv();
        if (tev != null) {
            out.print("<a href=\"");
            out.print(getRootURL());
            out.print("/env\">");

            print(out, tev.getName());
            out.println("</a>");
        }
        else {
            println(out, i18n.getString("harnessHttp.parameters.noEnv"));
        }

        out.println("<br>");

        print(out, i18n.getString("harnessHttp.parameters.interview"));
        if (params instanceof InterviewParameters) {
            InterviewParameters ip = (InterviewParameters)params;
            File ipf = ip.getFile();

            out.print("<a href=\"");
            out.print(getRootURL());
            out.print("/interview\">");
            print(out, (ipf == null ?
                       i18n.getString("harnessHttp.parameters.noInterviewFile") :
                       ipf.getPath()));
            out.println("</a>");
        }
        else {
            println(out, i18n.getString("harnessHttp.parameters.noInterview"));
        }

        out.println("<p>");

        // initial files info
        out.println("<ul>");
        out.print("<li>");
        print(out, i18n.getString("harnessHttp.parameters.urls"));
        printTests(out);

        // jtx info
        out.print("<li>");
        print(out, i18n.getString("harnessHttp.parameters.jtx"));
        Parameters.ExcludeListParameters exclParams = params.getExcludeListParameters();

        if (exclParams instanceof Parameters.MutableExcludeListParameters) {
            Parameters.MutableExcludeListParameters e =
                (Parameters.MutableExcludeListParameters) (exclParams);
            File[] jtx = e.getExcludeFiles();
            if (jtx == null || jtx.length == 0)
                println(out, i18n.getString("harnessHttp.parameters.emptyJtx"));
            else {
                out.println("<ul>");
                for (int i = 0; i < jtx.length; i++)
                    out.println("<li>" + jtx[i].getPath());

                out.println("</ul>");
            }
        }
        else {
            println(out, i18n.getString("harnessHttp.parameters.noJtx"));
        }

        // keyword info
        /* removed until output can be improved
        out.print("<li>");
        print(out, i18n.getString("harnessHttp.parameters.keyw"));
        out.println(params.getKeywords());
        */

        out.println("</ul>");

        // status
    }

    private void printTests(PrintWriter out) {
        String[] tests = harness.getParameters().getTests();
        if (tests == null || tests.length == 0) {
            print(out, i18n.getString("harnessHttp.parameters.noTests"));
        }
        else {
            out.println("<ul>");

            for (int i = 0; i < tests.length; i++) {
                out.println("<li>");
                println(out, tests[i]);
            }

            out.println("</ul>");
        }
    }   // printTests()

    private void printEnv(PrintWriter out, TestEnvironment env) {
        out.print(i18n.getString("harnessHttp.env.name"));
        println(out, env.getName());

        String keyHeader = "Key";
        String valHeader = "Value";

        out.println("<Table Border>");

        StringBuffer buf = new StringBuffer(50);

        // write the table header
        buf.append("<tr><th>");
        buf.append(filterTags(keyHeader));
        buf.append("<th>");
        buf.append(filterTags(valHeader));
        buf.append("</tr>");
        out.println(buf.toString());

        for (Iterator keys = env.keys().iterator(); keys.hasNext(); ) {
            String key = (String) (keys.next());
            out.println("<tr>");
            buf.setLength(0);
            buf.append("<td>");
            buf.append(key.toString());
            buf.append("<td>");
            try {
                buf.append(filterTags(StringArray.join((env.lookup(key)))));
            }
            catch (TestEnvironment.Fault f) {
                buf.append(i18n.getString("harnessHttp.env.error"));
            }

            out.println(buf.toString());
            out.println("</tr>");
        }   // while

        out.println("</Table>");
    }

    // ------------ instance vars ------------
    private Harness harness;
    private boolean debug = false;
    private String magicToken;
    private int[] stats = new int[Status.NUM_STATES];
    private TestFinderQueue tfq;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(HarnessHttpHandler.class);

    // ------------ Harness.Observer ------------
    public void startingTestRun(Parameters params) { }

    public void startingTest(TestResult tr) { }

    public void finishedTest(TestResult tr) {
        stats[tr.getStatus().getType()]++;
    }

    public void stoppingTestRun() { }
    public void finishedTesting() { }
    public void finishedTestRun(boolean allOK) { }

    public void error(String msg) { }
}
