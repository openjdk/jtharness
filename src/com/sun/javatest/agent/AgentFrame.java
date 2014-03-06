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

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;

import com.sun.javatest.JavaTestSecurityManager;
import com.sun.javatest.util.ExitCount;
import com.sun.javatest.util.MainFrame;

/**
 * A free-standing window that displays and controls a Agent.
 *
 * @see Agent
 * @see AgentMain
 * @see AgentApplet
 *
 **/

public class AgentFrame extends Frame
{
    /**
     * Create and start a AgentFrame, based on the supplied command line arguments.
     *
     * @param args      The command line arguments
     * <table>
     * <tr><td> -help                           <td> print a short summary of the command usage
     * <tr><td> -usage                          <td> print a short summary of the command usage
     * <tr><td> -active                         <td> set mode to be active
     * <tr><td> -activeHost  <em>hostname</em>  <td> set the host for active connections (implies -active)
     * <tr><td> -activePort  <em>port</em>      <td> set the port for active connections (implies -active)
     * <tr><td> -passive                        <td> set mode to be passive
     * <tr><td> -passivePort <em>port</em>      <td> set the port for passive connections (implies -passive)
     * <tr><td> -map         <em>file</em>      <td> map file for translating arguments of incoming requests
     * <tr><td> -concurrency <em>number</em>    <td> set the maximum number of simultaneous connections
     * <tr><td> -history     <em>number</em>    <td> set the size of the execution history
     * <tr><td> -trace                          <td> trace the execution of the agent
     * <tr><td> -observer    <em>classname</em> <td> add an observer to the agent that is used
     * </table>
     */
    public static void main(String[] args) {
        ModeOptions mode = null;
        String activeHost = null;
        int activePort = -1;
        int passivePort = -1;
        String serialPort = null;
        int concurrency = -1;
        String mapFile = null;
        int history = -1;
        boolean start = false;
        boolean useSharedFrame = true;
        String observerClassName = null;
        boolean tracing = false;

        ActiveModeOptions amo = new ActiveModeOptions();
        PassiveModeOptions pmo = new PassiveModeOptions();
        SerialPortModeOptions smo = new SerialPortModeOptions();

        for (int i = 0; i < args.length; i++) {
            try {
                if (args[i].equalsIgnoreCase("-active")) {
                    mode = amo;
                }
                else if (args[i].equalsIgnoreCase("-passive")) {
                    mode = pmo;
                }
                else if (args[i].equalsIgnoreCase("-activeHost")) {
                    mode = amo;
                    activeHost = args[++i];
                }
                else if (args[i].equalsIgnoreCase("-activePort")) {
                    mode = amo;
                    activePort = Integer.parseInt(args[++i]);
                }
                else if (args[i].equalsIgnoreCase("-passivePort")) {
                    mode = pmo;
                    passivePort = Integer.parseInt(args[++i]);
                }
                else if (args[i].equalsIgnoreCase("-serialPort")) {
                    mode = smo;
                    serialPort = args[++i];
                }
                else if (args[i].equalsIgnoreCase("-concurrency")) {
                    concurrency = Integer.parseInt(args[++i]);
                }
                else if (args[i].equalsIgnoreCase("-map")) {
                    mapFile = args[++i];
                }
                else if (args[i].equalsIgnoreCase("-history")) {
                    history = Integer.parseInt(args[++i]);
                }
                else if (args[i].equalsIgnoreCase("-start")) {
                    start = true;
                }
                else if (args[i].equalsIgnoreCase("-trace")) {
                    tracing = true;
                }
                else if ("-observer".equalsIgnoreCase(args[i]) && i < args.length - 1) {
                    if (observerClassName != null) {
                        System.err.println("duplicate use of -observer");
                        usage(System.err, 1);
                    }
                    observerClassName = args[++i];
                }
                else if (args[i].equalsIgnoreCase("-useSharedFrame")) {
                    System.err.println("Note: -useSharedFrame is now the default");
                    System.err.println("Use -noSharedFrame to disable this feature.");
                    useSharedFrame = true;
                }
                else if (args[i].equalsIgnoreCase("-noSharedFrame")) {
                    useSharedFrame = false;
                }
                else if (args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("-usage") ) {
                    usage(System.err, 0);
                }
                else {
                    System.err.println("Unrecognised option: " + args[i]);
                    usage(System.err, 1);
                }
            }
            catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Missing argument for " + args[args.length - 1]);
                usage(System.err, 1);
            }
            catch (NumberFormatException e) {
                System.err.println("Number expected: " + args[i]);
                usage(System.err, 1);
            }
        }


        if (activeHost != null)
            amo.setHost(activeHost);

        if (activePort != -1)
            amo.setPort(activePort);

        if (passivePort != -1)
            pmo.setPort(passivePort);

        if (serialPort != null) {
            if (smo == null) {
                System.err.println("Could not initialize serial ports");
                System.exit(1);
            }
            else
                smo.setPort(serialPort);
        }


        ModeOptions[] modeOptions = new ModeOptions[] {amo, pmo, smo};

        final AgentFrame sf = new AgentFrame(modeOptions);

        if (observerClassName != null) {
            try {
                Class observerClass = Class.forName(observerClassName);
                Agent.Observer observer = (Agent.Observer)(observerClass.newInstance());
                sf.panel.addObserver(observer);
            }
            catch (ClassCastException e) {
                System.err.println("observer is not of type " +
                        Agent.Observer.class.getName() + ": " + observerClassName);
                System.exit(1);
            }
            catch (ClassNotFoundException e) {
                System.err.println("cannot find observer class: " + observerClassName);
                System.exit(1);
            }
            catch (IllegalAccessException e) {
                System.err.println("problem instantiating observer: " + e);
                System.exit(1);
            }
            catch (InstantiationException e) {
                System.err.println("problem instantiating observer: " + e);
                System.exit(1);
            }
        }

        if (useSharedFrame)
            MainFrame.setFrame(sf);

        AgentPanel sp = sf.panel;
        sp.setTracing(tracing, System.out);

        if (mode != null)
            sp.setMode(mode.getModeName());

        if (concurrency != -1)
            sp.setConcurrency(concurrency);

        if (mapFile != null)
            sp.setMapFile(mapFile);

        if (history != -1)
            sp.setHistoryLimit(history);

        Integer delay = Integer.getInteger("agent.retry.delay");
        if (delay != null)
            sp.setRetryDelay(delay.intValue());

        // install our own permissive security manager, to prevent anyone else
        // installing a less permissive one; moan if it can't be installed.
        JavaTestSecurityManager.install();

        if (start)
            sp.start();

        try {
            Method invokeLater = EventQueue.class.getMethod("invokeLater", new Class[] { Runnable.class });
            invokeLater.invoke(null, new Object[] { new Runnable() {
                    public void run() {
                        sf.showCentered();
                    }
                } });
        }
        catch (NoSuchMethodException e) {
            // must be JDK 1.1
            sf.showCentered();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }

    }

    /**
     * Display the set of options recognized by main(), and exit.
     *
     * @param out       The output stream to which to write the
     *                  command line help.
     * @param exitCode  The exit code to be passed to System.exit.
     */
    public static void usage(PrintStream out, int exitCode) {
        String className = AgentFrame.class.getName();
        out.println("Usage:");
        out.println("    java " + className + " [options]");
        out.println("        -help             print this message");
        out.println("        -usage            print this message");
        out.println("        -active           set mode to be active");
        out.println("        -activeHost host  set the host for active connections (implies -active)");
        out.println("        -activePort port  set the port for active connections (implies -active)");
        out.println("        -passive          set mode to be passive");
        out.println("        -passivePort port set the port for passive connections (implies -passive)");
        out.println("        -serialPort port  set the port for serial port connections");
        out.println("        -concurrency num  set the maximum number of simultaneous connections");
        out.println("        -map file         map file for translating arguments of incoming requests");
        out.println("        -history num      set the maximum number of requests remembered in the history list");
        out.println("        -start            automatically start a agent");
        out.println("        -trace            trace the execution of the agent");
        out.println("        -observer class   add an observer to the agent");
        out.println("        -useSharedFrame   share the application frame with any tests that require it");
        System.exit(exitCode);
    }

    /**
     * Create a AgentFrame.
     * @param modeOptions An array of option panels for different connection modes.
     */
    public AgentFrame(ModeOptions[] modeOptions) {
        super(Agent.productName);

        ExitCount.inc();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                AgentFrame.this.dispose();
            }

            public void windowClosed(WindowEvent e) {
                ExitCount.dec();
            }
        });

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        panel = new AgentPanel(modeOptions, new AgentPanel.MapReader() {
            public Map read(String name) throws IOException {
                // Experiments indicate that the following code works OK
                // on versions of PersonalJava that do not support local file systems.
                // Just specify the map file as an http: URL.
                if (name == null || name.length() == 0)
                    return null;
                else
                    return Map.readFileOrURL(name);
            }
        });

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        add(panel, c);
    }

    private void showCentered() {
        pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = getSize();
        setLocation(screenSize.width/2 - size.width/2, screenSize.height/2 - size.height/2);
        show();
    }

    private class Listener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(EXIT)) {
                AgentFrame.this.dispose();
            }
        }
    }

    private Listener listener = new Listener();
    private AgentPanel panel;
    private boolean tracing;

    // action commands
    private static final String EXIT = "Exit";
}
