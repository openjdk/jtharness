/*
 * $Id$
 *
 * Copyright (c) 2003, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.util;

import java.applet.AppletContext;
import java.applet.Applet;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * This class provides a means whereby tools can temporarily give access
 * to a shared AppletContext and applet instance(s) which will be
 * used in AppletContext tests.
 *
 * With hindsight, this code should probably be in com.sun.javatest.agent
 * but the tests expect the class to be here in com.sun.javatest.util.
 */
public class MainAppletContext {

    private static AppletContext context = null;

    private static Hashtable applets = new Hashtable();

    private static Applet agentApplet = null;

    private static boolean started = false;

    /**
     * Set the AppletContext in use, so that it might be shared.
     * @param ctx the AppletContext to be shared.
     * @see #getAppletContext
     */
    public static void setAppletContext(AppletContext ctx) {
        context = ctx;
    }

    /**
     * Get the current AppletContext.
     * @return the current AppletContext, or null if it has been set.
     * @see #setAppletContext
     */
    public static AppletContext getAppletContext() {
        return context;
    }

    /**
     * Register an applet in a table of applets.
     * @param name the name with which to register the applet
     * @param applet the applet to be registered
     * @see #getApplet
     * @see #getAppletNames
     */
    public static synchronized void putApplet(String name, Applet applet) {
        applets.put(name, applet);
    }

    /**
     * Get the applet that has been registered with a given name.
     * @param name the name of the applet to return
     * @return the applet that has been registered with the given name
     */
    public static synchronized Applet getApplet(String name) {
        return (Applet) applets.get(name);
    }

    /**
     * Get an enumeration of all the names that have been used to register applets.
     * @return an enumeration of all the names that have been used to register applets
     */
    public static synchronized Enumeration getAppletNames() {
        return applets.keys();
    }

    /**
     * Set the applet to be identified as the JT Harness Agent applet.
     * @param applet the Javatest Agent applet
     * @see #getAgentApplet
     */
    public static void setAgentApplet(Applet applet) {
        agentApplet = applet;
    }

    /**
     * Get the applet that has been identified as the JT Harness Agent applet.
     * @return the Javatest Agent applet
     * @see #setAgentApplet
     */
    public static Applet getAgentApplet() {
        return agentApplet;
    }

    /**
     * Set a flag to indicate that the agent applet has been started.
     * @param value a boolean value indicating whether the agent applet has been
     * started or not.
     */
    public static synchronized void setStarted(boolean value) {
        started = value;
    }

    /**
     * Check the flag that indicates whether the agent applet has been started.
     * @return a boolean value indicating whether the agent applet has been
     * started or not.
     */
    public static synchronized boolean isStarted() {
        return started;
    }
}
