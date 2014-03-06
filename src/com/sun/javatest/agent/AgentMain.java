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

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.sun.javatest.JavaTestSecurityManager;
import com.sun.javatest.Status;

/**
 * Start an agent, based on command line arguments.
 * No GUI is used.  To create an agent with a GUI, see
 * AgentApplet or AgentFrame.
 *
 * @see Agent
 *
 **/
public class AgentMain {

    /**
     * This exception is used to report bad command line arguments.
     */
    public static class BadArgs extends Exception
    {
        /**
         * Create a BadArgs exception.
         * @param msg A detail message about an error that has been found.
         */
        public BadArgs(String msg) {
            this(new String[] { msg });
        }

        /**
         * Create a BadArgs object.
         * @param msgs Detailed message about an error that has been found.
         */
        public BadArgs(String[] msgs) {
            super(msgs[0]);
            this.msgs = msgs;
        }

        /**
         * Get the detail messages.
         * @return the messages given when this exception was created.
         */
        public String[] getMessages() {
            return msgs;
        }

        private String[] msgs;
    }

    /**
     * This exception is used to report problems that occur while running.
     */
    public static class Fault extends Exception
    {
        /**
         * Create a Fault exception.
         * @param msg A detail message about a fault that has occurred.
         */
        public Fault(String msg) {
            this(new String[] {  msg });
        }

        /**
         * Create a Fault object.
         * @param msgs A detail message about a fault that has been found.
         */
        public Fault(String[] msgs) {
            super(msgs[0]);
            this.msgs = msgs;
        }

        /**
         * Get the detail messages.
         * @return the messages given when this exception was created.
         */
        public String[] getMessages() {
            return msgs;
        }

        private String[] msgs;
    }

    /**
     * Create and start an Agent, based on the supplied command line arguments.
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
     * <tr><td> -concurrency <em>number</em>    <td> set the maximum number of simultaneous connections
     * <tr><td> -map         <em>file</em>      <td> map file for translating arguments of incoming requests
     * <tr><td> -trace                          <td> trace the execution of the agent
     * <tr><td> -observer    <em>classname</em> <td> add an observer to the agent that is used
     * </table>
     */
    public static void main(String[] args) {
        AgentMain m = new AgentMain();
        m.runAndExit(args);
    }


    /**
     * Create and start an Agent, based on the supplied command line arguments.
     * This is for use by subtypes with their own main(String[]) entry point.
     * @param args the command line arguments for the run
     */
    protected void runAndExit(String[] args) {
        // install our own permissive security manager, to prevent anyone else
        // installing a less permissive one; moan if it can't be installed.
        JavaTestSecurityManager.install();

        int rc;
        try {
            run(args);
            rc = 0;
        }
        catch (BadArgs e) {
            System.err.println("Error: Bad arguments");
            String[] msgs = e.getMessages();
            for (int i = 0; i < msgs.length; i++)
                System.err.println(msgs[i]);
            System.err.println();
            usage(System.err);
            rc = 1;
        }
        catch (Fault e) {
            String[] msgs = e.getMessages();
            for (int i = 0; i < msgs.length; i++)
                System.err.println(msgs[i]);
            rc = 2;
        }
        catch (Throwable t) {
            t.printStackTrace();
            rc = 3;
        }

        // If the JT security manager is installed, it won't allow a call of
        // System.exit unless we ask it nicely..
        SecurityManager sc = System.getSecurityManager();
        if (sc instanceof JavaTestSecurityManager)
            ((JavaTestSecurityManager) sc).setAllowExit(true);

        System.exit(rc);
    }

    /**
     * Run with the specified command-lines options.
     * This consists of the following steps:
     * <ul>
     * <li>The command line options are decoded with decodeAllArgs
     * <li>Validity checks (such as option inter-dependencies) are done by validateArgs
     * <li>An agent is created with createAgent
     * <li>The agent is run.
     * </ul>
     * @param args      An array of strings, typically provided via the command line
     * @throws AgentMain.BadArgs  if a problem is found in the arguments provided
     * @throws AgentMain.Fault  if a fault is found while running
     * @see #main
     * @see #decodeAllArgs
     * @see #validateArgs
     * @see #createAgent
     */
    public void run(String[] args) throws BadArgs, Fault {
        decodeAllArgs(args);

        if (helpRequested) {
            usage(System.err);
            return;
        }

        validateArgs();

        Agent agent = createAgent();
        agent.addObserver(new ErrorObserver());
        agent.run();
    }

    /**
     * Decode an array of command line options, by calling decodeArg for
     * successive options in the array.
     * @param args the array of command line options
     * @throws AgentMain.BadArgs if a problem is found decoding the args
     * @throws AgentMain.Fault if the args can be decoded successfully but
     * if there is a problem in their interpretation (e.g invalid port number)
     */
    protected void decodeAllArgs(String[] args) throws BadArgs, Fault {
        int i = 0;
        while (i < args.length) {
            int used = decodeArg(args, i);
            if (used == 0 )
                throw new BadArgs("Unrecognised option: " + args[i]);
            i += used;
        }
    }

    /**
     * Decode the next command line option in an array of options.
     * @param args  the array of command line options
     * @param index the position of the next option to be decoded
     * @return the number of elements consumed from the array
     * @throws AgentMain.BadArgs if a problem is found decoding the args
     * @throws AgentMain.Fault if the args can be decoded successfully but
     * if there is a problem in their interpretation (e.g invalid port number)
     */
    protected int decodeArg(String[] args, int index) throws BadArgs, Fault {
        int i = index;
        try {
            if (args[i].equalsIgnoreCase("-active")) {
                mode = ACTIVE;
                modeCheck |= (1 << mode);
                return 1;
            }
            else if (args[i].equalsIgnoreCase("-passive")) {
                mode = PASSIVE;
                modeCheck |= (1 << mode);
                return 1;
            }
            else if (args[i].equalsIgnoreCase("-activeHost")) {
                mode = ACTIVE;
                modeCheck |= (1 << mode);
                activeHost = args[++i];
                return 2;
            }
            else if (args[i].equalsIgnoreCase("-activePort")) {
                mode = ACTIVE;
                modeCheck |= (1 << mode);
                activePort = Integer.parseInt(args[++i]);
                return 2;
            }
            else if (args[i].equalsIgnoreCase("-passivePort")) {
                mode = PASSIVE;
                modeCheck |= (1 << mode);
                passivePort = Integer.parseInt(args[++i]);
                return 2;
            }
            else if (args[i].equalsIgnoreCase("-serialPort")) {
                mode = SERIAL;
                modeCheck |= (1 << mode);
                serialPort = args[++i];
                return 2;
            }
            else if (args[i].equalsIgnoreCase("-concurrency")) {
                concurrency = Integer.parseInt(args[++i]);
                return 2;
            }
            else if (args[i].equalsIgnoreCase("-map")) {
                mapFile = args[++i];
                return 2;
            }
            else if (args[i].equalsIgnoreCase("-trace")) {
                tracing = true;
                return 1;
            }
            else if ("-observer".equalsIgnoreCase(args[i]) && i < args.length - 1) {
                if (observerClassName != null)
                    throw new BadArgs("duplicate use of -observer");
                observerClassName = args[++i];
                return 2;
            }
            else if (args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("-usage") ) {
                helpRequested = true;
                return (args.length - index); // consume remaining args
            }
            else
                return 0;   // unrecognized
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new BadArgs("Missing argument for " + args[args.length - 1]);
        }
        catch (NumberFormatException e) {
            throw new BadArgs("Number expected: " + args[i]);
        }
    }

    /**
     * Validate the values decoded by decodeAllArgs.
     * @throws AgentMain.BadArgs if a problem is found validating the args that
     * is likely caused by a misunderstanding of the command line options or syntax
     * @throws AgentMain.Fault if there is some other problem with the args, such
     * as a bad host name or a port not being available for use
     */
    protected void validateArgs() throws BadArgs, Fault {
        if (modeCheck == 0)
            throw new BadArgs("No connection options given");

        if (modeCheck != (1 << mode))
            throw new BadArgs("Conflicting options for connection to JT Harness harness");

        switch (mode) {
        case ACTIVE:
            if (activeHost == null || activeHost.length() == 0)
                throw new BadArgs("No active host specified");
            if (activePort <= 0)
                throw new BadArgs("No active port specified");
            break;

        case SERIAL:
            if (serialPort == null)
                throw new BadArgs("No serial port specified");
        }

        if (!Agent.isValidConcurrency(concurrency)) {
            throw new BadArgs("Bad value for concurrency: " + concurrency);
        }
    }

    /**
     * Create a connection factory based on the values decoded by decodeAllArgs
     * Normally called from createAgent.
     * @return a connection factory based on the values decoded by decodeAllArgs
     * @throws AgentMain.Fault if there is a problem createing the factory
     */
    protected ConnectionFactory createConnectionFactory() throws Fault {
        String s = AgentMain.class.getName();
        String pkg = s.substring(0, s.lastIndexOf('.'));

        switch (mode) {
        case ACTIVE:
            try {
                Class c = Class.forName(pkg + ".ActiveConnectionFactory");
                Constructor m = c.getConstructor(new Class[] {String.class, int.class});
                Object[] args = { activeHost, new Integer(activePort) };
                return (ConnectionFactory)(m.newInstance(args));
            }
            catch (Throwable e) {
                Throwable t = unwrapInvocationTargetException(e);
                String[] msgs = {
                    "Error occurred while trying to start an active agent",
                    t.toString(),
                    "Are the java.net classes available?"
                };
                throw new Fault(msgs);
            }

        case PASSIVE:
            try {
                Class c = Class.forName(pkg + ".PassiveConnectionFactory");
                Constructor m = c.getConstructor(new Class[] {int.class, int.class});
                Object[] args = { new Integer(passivePort), new Integer(concurrency + 1) };
                return (ConnectionFactory)(m.newInstance(args));
            }
            catch (Throwable e) {
                Throwable t = unwrapInvocationTargetException(e);
                if (t instanceof IOException)
                    throw new Fault("Cannot create socket on port " + passivePort);
                else {
                    String[] msgs = {
                        "Error occurred while trying to start a passive agent",
                        t.toString(),
                        "Are the java.net classes available?"
                    };
                    throw new Fault(msgs);
                }
            }

        case SERIAL:
            try {
                Class c = Class.forName(pkg + ".SerialPortConnectionFactory");
                Constructor m = c.getConstructor(new Class[] {String.class, String.class, int.class});
                Object[] args = {serialPort, Agent.productName, new Integer(10*1000)};
                return (ConnectionFactory)(m.newInstance(args));
            }
            catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof IllegalArgumentException ||
                    t.getClass().getName().equals("gnu.io.NoSuchPortException")) {
                    throw new Fault(serialPort + " is not a valid port");
                }
                else {
                    String[] msgs = {
                        "Error occurred while trying to access the communication ports",
                        t.toString(),
                        "Is the gnu.io extension installed?"
                    };
                    throw new Fault(msgs);
                }
            }
            catch (Throwable e) {
                String[] msgs = {
                    "Error occurred while trying to access the communication ports",
                    e.toString(),
                    "Is the gnu.io extension installed?"
                };
                throw new Fault(msgs);
            }

        default:
            throw new Error("unexpected mode");
        }
    }

    /**
     * Create an agent based on the command line options previously decoded.
     * createConnectionFactory() is used to create the connection factory required
     * by the agent.
     * @return an agent based on the values decoded by decodeAllArgs
     * @throws AgentMain.Fault if there is a problem createing the agent
     */
    protected Agent createAgent() throws Fault {
        ConnectionFactory cf = createConnectionFactory();
        Agent agent = new Agent(cf, concurrency);
        agent.setTracing(tracing);

        if (observerClassName != null) {
            try {
                Class observerClass = Class.forName(observerClassName);
                Agent.Observer observer = (Agent.Observer)(observerClass.newInstance());
                agent.addObserver(observer);
            }
            catch (ClassCastException e) {
                throw new Fault("observer is not of type " +
                        Agent.Observer.class.getName() + ": " + observerClassName);
            }
            catch (ClassNotFoundException e) {
                throw new Fault("cannot find observer class: " + observerClassName);
            }
            catch (IllegalAccessException e) {
                throw new Fault("problem instantiating observer: " + e);
            }
            catch (InstantiationException e) {
                throw new Fault("problem instantiating observer: " + e);
            }
        }

        // for now, we only read a map file if one is explicitly specified;
        // in JDK 1.1, it might be nice to check if <<HOSTNAME>>.jtm exists
        // or something like that. In JDK 1.0.2, getting the HOSTNAME is
        // problematic, because INetAddress fails the verifier.
        if (mapFile != null) {
            try {
                agent.setMap(Map.readFileOrURL(mapFile));
            }
            catch (IOException e) {
                String[] msgs = {"Problem reading map file", e.toString()};
                throw new Fault(msgs);
            }
        }

        Integer delay = Integer.getInteger("agent.retry.delay");
        if (delay != null)
            agent.setRetryDelay(delay.intValue());

        return agent;
    }

    /**
     * Display the set of options recognized by main(String[] args).
     * @param out a stream to which to write the information
     */
    public void usage(PrintStream out) {
        String className = getClass().getName();
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
        out.println("        -map file         map file for translating arguments of incoming requests");
        out.println("        -concurrency num  set the maximum number of simultaneous connections");
        out.println("        -trace            trace the execution of the agent");
        out.println("        -observer class   add an observer to the agent");
    }

    /**
     * Unwrap an InvocationTargetException.
     * @param t the exception to be unwrapped
     * @return the argument's target exception if the argument is an
     * InvocationTargetException; otherwise the argument itself is returned.
     */
    protected static Throwable unwrapInvocationTargetException(Throwable t) {
        if (t instanceof InvocationTargetException)
            return ((InvocationTargetException) t).getTargetException();
        else
            return t;
    }

    private boolean helpRequested = false;
    private int mode = 0;
    private int modeCheck = 0;
    private String activeHost = null;
    private int activePort = Agent.defaultActivePort;
    private int passivePort = Agent.defaultPassivePort;
    private String serialPort = null;
    private int concurrency = 1;
    private String mapFile = null;
    private String observerClassName;
    private boolean tracing;

    private static final int ACTIVE = 1;
    private static final int PASSIVE = 2;
    private static final int SERIAL = 3;

    private static final int max(int a, int b) {
        return (a > b ? a : b);
    }

    private static class ErrorObserver implements Agent.Observer {
        ErrorObserver() {
            try {
                connectExceptionClass = Class.forName("java.net.ConnectException");
            }
            catch (Throwable t) {
                // ignore
            }

            try {
                unknownHostExceptionClass = Class.forName("java.net.UnknownHostException");
            }
            catch (Throwable t) {
                // ignore
            }
        }

        public void started(Agent agent) {
        }

        public void errorOpeningConnection(Agent agent, Exception e) {
            if (connectExceptionClass != null && connectExceptionClass.isInstance(e)) {
                long now = System.currentTimeMillis();
                if (lastNotRespondMsgTime + lastNotRespondMsgInterval < now) {
                    System.err.println("host not responding: " + e.getMessage());
                    lastNotRespondMsgTime = now;
                }
            }
            else if (unknownHostExceptionClass != null && unknownHostExceptionClass.isInstance(e))
                System.err.println("unknown host: " + e.getMessage());
            else
                System.err.println("error connecting to host: " + e);
        }

        private long lastNotRespondMsgTime = 0;
        private int lastNotRespondMsgInterval =
            max(Integer.getInteger("notResponding.message.interval", 60).intValue(), 10)*1000;

        public void finished(Agent agent) {
        }

        public void openedConnection(Agent agent, Connection c) {
        }

        public void execTest(Agent agent, Connection c, String tag, String className, String[] args) {
        }

        public void execCommand(Agent agent, Connection c, String tag, String className, String[] args) {
        }

        public void execMain(Agent agent, Connection c, String tag, String className, String[] args) {
        }

        public void result(Agent agent, Connection c, Status result) {
        }

        public void exception(Agent agent, Connection c, Throwable e) {
        }

        public void completed(Agent agent, Connection c) {
        }

        private Class connectExceptionClass;
        private Class unknownHostExceptionClass;
    }
}
