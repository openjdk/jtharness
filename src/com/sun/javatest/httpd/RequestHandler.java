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
package com.sun.javatest.httpd;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Hashtable;

import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;

/**
 * HTTP service handler for requests sent to JT Harness
 */

// this code is based upon that found in the sun.net package
class RequestHandler implements Runnable {
    /**
     * Construct a handler to take the HTTP request on the given socket.
     * Based on the requested URL, the correct JThttpProvider class will be
     * notified.  For special URLs, this class will delegate responsibility to
     * them.
     */
    public RequestHandler(Socket soc) {
        this.soc = soc;
    }

    public void run() {
        // read the request
        String request = null;

        try {
            if (debug) {
                StringBuffer buf = new StringBuffer();
                buf.append("Handling request from ");
                buf.append(soc.getInetAddress().getHostName());
                buf.append(" (");
                buf.append(soc.getInetAddress().getHostAddress());
                buf.append(")");
                System.out.println(buf.toString());
                buf.setLength(0);
            }

            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())));
            in = new LineNumberReader(new InputStreamReader(soc.getInputStream()));


            request = in.readLine();

            if (debug) {
                System.out.println("-------------");
                System.out.println("RH-Full request:");
                System.out.println(request);
            }

            // if we are debugging, print the rest of the message
            while (debug && in.ready()) {
                String line = in.readLine();
                System.out.println(line);
            }

            if (debug) System.out.println("-------------");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // decode
        String[] args = StringArray.split(request);
        if (debug) {
            System.out.print("Decode: " + request);
            System.out.println(" -> " + args.length + " params.");
        }

        if (args == null || args.length < 3) {
            if (debug) System.err.println("RH-HTTP request too short.");
            // ignore the request
            return;
        }

        if (args[0].equalsIgnoreCase(GET)) {
            if (debug) System.out.println("RH-Processing HTTP GET request.");
            doGet(args);
        }
        else if (args[0].equalsIgnoreCase(POST)) {
            if (debug) System.out.println("RH-Processing HTTP POST request.");
            doPost(args);
        }
        else {
            if (debug) System.err.println("RH-Unsupported request method: " + args[0]);
            error(out, BAD_METHOD, args[0] + " is an unsupported request method. " + BAD_METHOD);
            out.close();
            return;
        }
    }

    protected void doGet(String[] args) {
        if (debug) System.out.println("RH-Get processing URL: \"" + args[1] + "\"");

        httpURL url = new httpURL(args[1]);

        /*
        JThttpProvider handler = ProviderRegistry.getHandler(args[1]);
        */
        JThttpProvider handler = RootRegistry.getInstance().getHandler(url);
        if (handler != null)
            handler.serviceRequest(url, out);
        else {
            // need to produce an error page!
            if (debug) System.out.println("No handler found for: " + args[1]);
        }
    }

    protected void doPost(String[] args) {
    }

    /**
     * Produce and error message.  404?
     */
    protected void notFound(String[] args) {
    }

    private void error(PrintWriter out, String code, String msg) {
        out.print("HTTP/1.1 ");
        out.println(code);
        out.println(HTTP_CONTENT_HTML);
        out.print("<html><head><title>");
        out.print(code);
        out.println("</title></head><body>");
        out.print(i18n.getString("handler.err.txt"));
        out.println("<br>");
        out.println(code);
        out.println("</body></html>");
    }

    private static Hashtable urlMap;

    private Socket soc;
    private PrintWriter out;
    private LineNumberReader in;

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String BAD_METHOD = "405 Method Not Allowed";
    private static final String HTTP_CONTENT_TYPE = "Content-Type: ";
    private static final String HTTP_CONTENT_HTML = HTTP_CONTENT_TYPE + "text/html";
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(RequestHandler.class);
    protected static boolean debug = Boolean.getBoolean("debug." + RequestHandler.class.getName());
}
