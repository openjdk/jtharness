/*
 * $Id$
 *
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.agent;

import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.io.IOException;
import java.net.URL;

import com.sun.javatest.Status;
import com.sun.javatest.util.MainAppletContext;
import com.sun.javatest.util.MainFrame;

/**
 * An applet that displays and controls a Agent.
 *
 * @see Agent
 * @see AgentMain
 * @see AgentFrame
 *
 **/
public class AgentApplet extends Applet implements Agent.Observer
{

    /**
     * Initialize the applet, based on the applet parameters.
     *
     * The following applet parameters are recognized
     * <table>
     * <tr><td>mode        <em>mode</em>        <td> set mode to be "active" or "passive"
     * <tr><td>activeHost  <em>hostname</em>    <td> set the host for active connections
     * <tr><td>activePort  <em>port</em>        <td> set the port for active connections
     * <tr><td>passivePort <em>port</em>        <td> set the port for passive connections
     * <tr><td>map         <em>url</em>         <td> map file for translating arguments of incoming requests; the url is relative to the applet's document base
     * <tr><td>concurrency <em>number</em>      <td> set the maximum number of simultaneous connections
     * <tr><td>history     <em>number</em>      <td> set the size of the execution history
     * <tr><td>trace       <em>boolean</em>     <td> trace the execution of the agent
     * <tr><td>observer    <em>classname</em>   <td> add an observer to the agent that is used
     * </table>
     */
    public void init() {

        String mode = getParameter("mode", "active");
        URL docBase = getDocumentBase(); // may be null if run standalone, sigh
        String defaultActiveHost = (docBase == null ? "localhost" : docBase.getHost());
        String activeHost = getParameter("activeHost", defaultActiveHost);
        int activePort = getIntParameter("activePort", Agent.defaultActivePort);
        int passivePort = getIntParameter("passivePort", Agent.defaultPassivePort);
        String serialPort = getParameter("serialPort");
        int concurrency = getIntParameter("concurrency", 1);
        int history = getIntParameter("history", -1);
        int delay = getIntParameter("retryDelay", -1);
        String mapFile = getParameter("map");
        String usac = getParameter("useSharedAppletContext");
        shareAppletContext = (usac != null && usac.equals("true"));
        String usf = getParameter("useSharedFrame");
        boolean shareFrame = (usf == null || usf.equals("true"));
        final boolean tracing = "true".equals(getParameter("trace"));
        boolean autostart = "true".equals(getParameter("start"));
        String observerClassName = getParameter("observer");
        String appletName;

        if (shareAppletContext) {
            appletName = getParameter("appletName");
            // do checks for use with MainAppletContext
            if (appletName == null) {
                // the following normally executes on the test platform, so is not i18n
                String line1 = "Error: Applet parameter \"appletName\" must be defined";
                String line2 = "and match the applet's \"name\" attribute.";
                showStatus("Error starting Applet: Applet parameter \"appletName\" must be defined.");
                Panel p = new Panel(new GridLayout(0,1));
                Label label1 = new Label(line1);
                Label label2 = new Label(line2);
                p.add(label1);
                p.add(label2);
                add(p);
                return;
            }
        }
        else
            appletName = null;

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        ActiveModeOptions amo = new ActiveModeOptions();
        if (activeHost != null)
            amo.setHost(activeHost);
        amo.setPort(activePort);

        PassiveModeOptions pmo = new PassiveModeOptions();
        pmo.setPort(passivePort);

        SerialPortModeOptions smo = new SerialPortModeOptions();
        if (serialPort != null)
            smo.setPort(serialPort);

        ModeOptions[] modeOptions = new ModeOptions[] {amo, pmo, smo};

        AgentPanel.MapReader mapReader = new AgentPanel.MapReader() {
            public Map read(String name) throws IOException {
                if (name == null || name.length() == 0)
                    return null;
                else {
                    Map m = Map.readURL(new URL(getDocumentBase(), name));
                    m.setTracing(tracing, System.out);
                    return m;
                }
            }
        };
        AgentPanel ap = new AgentPanel(modeOptions, mapReader);

        if (observerClassName != null) {
            try {
                Class observerClass = Class.forName(observerClassName);
                Agent.Observer observer = (Agent.Observer)(observerClass.newInstance());
                ap.addObserver(observer);
            }
            catch (ClassCastException e) {
                showStatus("observer is not of type " +
                        Agent.Observer.class.getName() + ": " + observerClassName);
            }
            catch (ClassNotFoundException e) {
                showStatus("cannot find observer class: " + observerClassName);
            }
            catch (IllegalAccessException e) {
                showStatus("problem instantiating observer: " + e);
            }
            catch (InstantiationException e) {
                showStatus("problem instantiating observer: " + e);
            }
        }

        ap.setMode(mode);
        ap.setConcurrency(concurrency);
        ap.setTracing(tracing, System.out);

        if (mapFile != null)
            ap.setMapFile(mapFile);

        if (history != -1)
            ap.setHistoryLimit(history);

        if (delay != -1)
            ap.setRetryDelay(delay);

        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(ap, c);

        if (shareFrame) {
            try {
                Container x = this;
                while (x != null && !(x instanceof Frame))
                    x = x.getParent();
                if (x != null)
                    MainFrame.setFrame((Frame)x);
            }
            catch (SecurityException e) {
                System.err.println("Security Exception occurred while attempting to access shared frame; " + e);
            }
        }

        if (shareAppletContext) {
            AppletContext context = getAppletContext();
            MainAppletContext.setAppletContext(context);
            MainAppletContext.putApplet(appletName, this);
            MainAppletContext.setAgentApplet(this);
        }

        if (autostart)
            ap.start();
    }

    public void start() {
        if (shareAppletContext)
            MainAppletContext.setStarted(true);
        super.start();
    }

    public void destroy() {
        Component c = getComponent(0);
        if (c instanceof AgentPanel) {
            AgentPanel ap = (AgentPanel) c;
            ap.stop();
        }
    }

    /**
     * Returns a string containing information about
     * the author, version and copyright of the applet.
     */
    public String getAppletInfo() {
        return (Agent.productName + " " +
                Agent.productVersion + " " +
                Agent.productCopyright);
    }

    /**
     * Returns an array of strings describing the
     * parameters that are understood by this
     * applet.
     */
    public String[][] getParameterInfo() {
        String[][] pinfo = {
            {"mode",       "\"active\" or \"passive\"",
                                        "the mode for the agent"},
            {"activeHost",  "hostname", "the host for active connections"},
            {"activePort",  "port",     "the port for active connections"},
            {"passivePort", "port",     "the port for passive connections"},
            {"map",         "url",      "map file for translating arguments of incoming requests"},
            {"concurrency", "number",   "the maximum number of simultaneous connections"},
            {"history",     "int",      "the size of the execution history"},
            {"trace",       "boolean",  "trace the execution of the agent"}
        };
        return pinfo;
    }

    private int getIntParameter(String name, int dflt) {
        try {
            String s = getParameter(name);
            if (s != null)
                return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            // ignore
        }
        return dflt;
    }

    private String getParameter(String name, String dflt) {
        String s = getParameter(name);
        return (s == null ? dflt : s);
    }

    public void started(Agent sl) {
        showStatus("agent started");
    }

    public void errorOpeningConnection(Agent sl, Exception e) {
        showStatus("error opening connection: " + e.getMessage());
    }

    public void finished(Agent sl) {
        showStatus("agent stopped");
    }

    public synchronized void openedConnection(Agent sl, Connection c) {
        showStatus("OPENED SOCKET");
    }

    public synchronized void execTest(Agent sl, Connection c, String tag, String className, String[] args) {
        showStatus("EXEC");
    }

    public synchronized void execCommand(Agent sl, Connection c, String tag, String className, String[] args) {
        showStatus("EXEC");
    }

    public synchronized void execMain(Agent sl, Connection c, String tag, String className, String[] args) {
        showStatus("EXEC");
    }

    public synchronized void result(Agent sl, Connection c, Status r) {
        showStatus("RESULT");
    }

    public synchronized void exception(Agent sl, Connection c, Throwable t) {
        showStatus("EXCEPTION (NYI)");
    }

    public synchronized void completed(Agent sl, Connection c) {
        showStatus("COMPLETED (NYI)");
    }

    private boolean shareAppletContext;

}
