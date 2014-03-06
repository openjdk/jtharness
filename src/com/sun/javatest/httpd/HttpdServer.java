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

import com.sun.javatest.agent.SocketConnection;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.BindException;

import java.io.PrintWriter;

import com.sun.javatest.util.I18NResourceBundle;

/**
 * HTTP services for JT Harness.
 * This is designed to be a Singleton in the system, although this is not
 * enforced.
 */

// this code is based upon that found in the sun.net package
public class HttpdServer implements Runnable {
    public HttpdServer() {
        try {
            init();
            HttpdServer.setActive();
        }
        catch (IOException e) {
            //e.printStackTrace();
            throw new IllegalStateException(i18n.getString("server.cantInit"));
        }
    }

    public void run() {
        while (true) {
            try {
                Socket ns = socket.accept();
                if (debug) System.out.println("httpd-New connection " + ns);

                RequestHandler handler = new RequestHandler(ns);
                Thread thr = new Thread(handler);

                if (debug) System.out.println("httpd-Starting thread for connection ");
                thr.start();
            }
            catch (IOException e) {
                System.out.println(i18n.getString("server.errorInAccept"));
                e.printStackTrace(System.out);
                // abort server?
            }
        }

    }

    /**
     * Get the local port on which the server is listening
     */
    public static int getLocalPort() {
        if (socket == null)
            throw new IllegalStateException();

        return socket.getLocalPort();
    }

    /**
     * Is the webserver active?
     */
    public static boolean isActive() {
        return active;
    }

    private static void setActive() {
        active = true;
    }


    /**
     * Find out the fully qualified base address of the webserver.
     * Typical output would be http://hostname.domain:port/
     */
    private static String getBaseUrl() {
        return baseURL;
    }

    /**
     * @exception IOException May be thrown if an error occurs when attempting
     *                            to create the Socket.
     */
    private void init() throws IOException {
        if (debug) System.out.println("Initializing JT Harness HTTP Server");

        int soc_num = (Integer.getInteger("jt.httpd.port", 1903)).intValue();

        // this loop searches for an available port
        for (int i = soc_num; i < soc_num + MAX_PORT_SEARCH; i++) {

            try {
                socket = SocketConnection.createServerSocket(i, 25);

                // success!
                System.out.println(i18n.getString("server.port",
                                                  String.valueOf(socket.getLocalPort())));

                StringBuffer buf = new StringBuffer("http://");
                buf.append(InetAddress.getLocalHost().getHostAddress());
                buf.append(":");
                buf.append(socket.getLocalPort());
                buf.append("/");
                baseURL = buf.toString();
                buf = null;

                System.out.println(i18n.getString("server.url", baseURL));
                break;
            }   // try
            catch (BindException e) {
                if (i + 1 >= soc_num + MAX_PORT_SEARCH)
                    throw e;
                else {
                    System.out.println(i18n.getString("server.portBusy", new Integer(i)));
                }
            }
            catch (IOException e) {
                System.out.println(i18n.getString("server.errorInInit"));
                throw e;
            }   // catch

        }   // for
    }   // init()

    // ---- FOR DEBUGGING ----
    public static void main(String[] args) {
        System.out.println("Starting JT Harness httpd in debug mode.");

        JThttpProvider prov = new JThttpProvider() {
            public void serviceRequest(httpURL url, PrintWriter out) {
                PageGenerator.generateDocType(out, PageGenerator.HTML32);
                out.println("<html>");
                out.println("<Body>");
                out.println("<h2>Hello, this is the JT Harness web server.</h2>");
                out.println("Running in test mode, no harness.");
                out.println("</Body>");
                out.println("</html>");

                out.close();
            }
        };

        RootRegistry.getInstance().addHandler("/", "Root JT Harness URL", prov);

        HttpdServer server = new HttpdServer();
        server.debug = true;

        Thread thr = new Thread(server);
        thr.start();
    }

    private static ServerSocket socket;
    private static String baseURL;

    /**
     * Maximum number of ports above the given port number to try to attach to.
     */
    private static final int MAX_PORT_SEARCH = 10;

    /**
     * Is the web server running in this instance of JT Harness?
     */
    private static boolean active;

    static protected boolean debug = Boolean.getBoolean("debug." + HttpdServer.class.getName());


    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(HttpdServer.class);
}

