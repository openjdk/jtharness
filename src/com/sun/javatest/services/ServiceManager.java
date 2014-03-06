/*
 * $Id$
 *
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.services;

import com.sun.javatest.Harness;
import com.sun.javatest.Parameters;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResult.Fault;
import com.sun.javatest.TestSuite;
import com.sun.javatest.TestSuite.DuplicateLogNameFault;
import com.sun.javatest.TestSuite.NoSuchLogFault;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.services.Service.NotConnectedException;
import com.sun.javatest.services.Service.ServiceError;
import com.sun.javatest.tool.Command;
import com.sun.javatest.tool.CommandContext;
import com.sun.javatest.tool.CommandManager;
import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.HelpTree.Node;
import com.sun.javatest.util.I18NResourceBundle;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that manages all services. Has 1:1 relation with
 * {@code com.sun.javatest.Harness} objects. Implements
 * {@code Harness.Observer} interface to be notified of all {@code Harness}
 * events, such as start and stop execution of all the test suite and each particular
 * test.
 */
public class ServiceManager implements Harness.Observer {

    private Harness harness;

    private Map<String, Service> services;
    private Set<String> activeServices;

    private Set<TestPath> testServiceMap;

    private StartMode mode = ServiceCommandManager.getMode();

    private WritingThread writer;

    private WatchDog watchDog;

    private Logger commonLog;


    /**
     * Enum of all supported service's starting modes.
     * <p>
     * {@code UP_FRONT} - all required services are started at the beginning of
     * test run.
     * <p>
     * {@code LAZY} - each service is started, when first test, which requires
     * this service, is going to be run.
     * <p>
     * {@code MANUALLY} - no one of services starts.
     */
    public static enum StartMode {
        LAZY("lazy"), UP_FRONT("up_front"), MANUALLY("manually");

        private String key;

        StartMode(String s) {
            key = s;
        }

        public String getKey() {return key;}
    }

    /**
     * Constructor to create ServiceManager for services of given test suite.
     * During initialization, reads from test suite information about specified
     * services.
     * @param ts test suite, which services will be managed.
     */
    public ServiceManager(TestSuite ts) {
        ServiceReader reader = ts.getServiceReader();
        services = reader.readServices();
        testServiceMap = reader.readTestServiceMap();
        for (TestPath p : testServiceMap) {
            p.setServiceManager(this);
        }

        activeServices = new TreeSet<String>();

        writer = new WritingThread();
        writer.start();
        watchDog = new WatchDog(1000);
        watchDog.start();
    }

    /**
     * Links this ServiceManager with {@link com.sun.javatest.Harness}.
     * Set itself as Harness observer, creates logger for service-related
     * messages.
     * @param h {@link com.sun.javatest.Harness} to link with.
     */
    public void setHarness(Harness h) {
        this.harness = h;
        harness.addObserver(this);

        setParameters(harness.getParameters());
    }

    public Harness getHarness() {
        return harness;
    }

    public void setParameters(Parameters p) {

        TestEnvironment env = p.getEnv();
        for (Service s : services.values()) {
            s.createLog(p);
            s.getProperties().setExternalProperties(env);
        }

        TestSuite ts = p.getTestSuite();
        WorkDirectory wd = p.getWorkDirectory();

        String name = "Services Common";
        try {
            commonLog = ts.createLog(wd, null, name);
        } catch (DuplicateLogNameFault ex) {
            try {
                commonLog = ts.getLog(wd, name);
            } catch (NoSuchLogFault ex1) {
            }
        }

    }

    /**
     * Allows to specify non-default output Writer for particular service.
     * Default writer logs services output to JavaTest logging subsystem.
     * @param sID string with service's ID.
     * @param out Writer to use.
     */
    public void setOutputWriter(String sID, Writer out) {
        writer.setOutputWriter(sID, out);
    }

    /**
     * Allows to specify non-default error Writer for particular service.
     * Default writer logs services error output to JavaTest logging subsystem.
     * @param sID string with service's ID.
     * @param err Writer to use.
     */
    public void setErrorWriter(String sID, Writer err) {
        writer.setErrorWriter(sID, err);
    }



    /**
     * Returns all services it manages.
     * @return Map of all managed services, where keys are service's IDs and
     * values are {@link com.sun.javatest.services.Service} objects.
     */
    public Map<String, Service> getAllServices() {
        return new HashMap(services);
    }

    /**
     * Returns set of string with IDs for those services, which are required
     * for current test run.
     * @return IDs for currently used services.
     */
    public Set<String> getActiveServices() {
        return new TreeSet(activeServices);
    }

    public void startService(String sID) {
        Service s = services.get(sID);
        try {
            if (s.isAlive()) {
                return;
            }

            if (!s.start()) {
                String msg = "Service " + sID + " was not started";
                commonLog.log(Level.WARNING, msg);
                for (Observer o : watchDog.getObservers()) {
                    o.handleError(sID, new ServiceError(msg));
                }
            }
            else {
                String msg = "Service " + sID + " was started";
                commonLog.log(Level.INFO, msg);
                activeServices.add(sID);
                writer.add(s);
            }

        } catch (NotConnectedException ex) {
            String msg = "Service " + sID + " was not started because of" +
                    " connection failure\n";
            msg += ex.getMessage();
            commonLog.log(Level.WARNING, msg);
            for (Observer o : watchDog.getObservers()) {
                o.handleNotConnected(sID, ex);
            }
        } catch (ServiceError ex) {
            String msg = "Service " + sID + " was not started because of" +
                    " service error\n";
            msg += ex.getMessage();
            commonLog.log(Level.WARNING, msg);
            for (Observer o : watchDog.getObservers()) {
                o.handleError(sID, ex);
            }
        }

        watchDog.refreshServices();
    }

    public void stopService(String sID) {
        if (stopService0(sID)) {
            activeServices.remove(sID);
        }
    }

    private boolean stopService0(String sID) {
        try {
            Service serv = services.get(sID);
            if (!serv.stop()) {
                String msg = "Service " + sID + " was not stopped";
                commonLog.log(Level.WARNING, msg);
                return false;
            } else {
                String msg = "Service " + sID + " was stopped";
                commonLog.log(Level.INFO, msg);
                writer.remove(sID);
                watchDog.refreshServices();
                return true;
            }
        } catch (NotConnectedException ex) {
            String msg = "Service " + sID + " was not stopped because of" +
                    " connection failure\n";
            msg += ex.getMessage();
            commonLog.log(Level.WARNING, msg);
        } catch (ServiceError ex) {
            String msg = "Service " + sID + " was not stopped because of" +
                    " service error\n";
            msg += ex.getMessage();
            commonLog.log(Level.WARNING, msg);
        }

        return false;
    }

    public void startingTestRun(Parameters params) {
        if (mode == StartMode.UP_FRONT) {
            Set<String> active = new TreeSet();
            try {
                Iterator iter = harness.getTestsIterator(null);
                active = selectActiveServices(iter);
            } catch (Harness.Fault f) {
                commonLog.log(Level.SEVERE, f.getMessage());
                return;
            }

            for (String sID : active) {
                startService(sID);
            }
        }

    }

    public synchronized void startingTest(TestResult tr) {
        if (mode == StartMode.LAZY) {
            try {
                TestDescription td = tr.getDescription();

                for (TestPath p : testServiceMap) {
                    if (p.matches(td)) {
                        for (String sID : p.getServices()) {

                            /* If service was started previously and failed,
                             * it will be restarted.
                             */
                            boolean isAlive = true;
                            try {
                                isAlive = services.get(sID).isAlive();
                            } catch (NotConnectedException ex) {
                                isAlive = false;
                            } catch (ServiceError ex) {
                                isAlive = false;
                            }

                            if (!activeServices.contains(sID) || !isAlive) {
                                startService(sID);
                            }
                        }
                    }
                }
            } catch (Fault ex) {
            }
        }
    }


    public void finishedTest(TestResult tr) {
        // Needs do nothing here for now;
    }

    public void stoppingTestRun() {
        stopServices();
    }

    public void finishedTesting() {
        // Decided it's better to stop services right after test execution
        stopServices();
    }

    public void finishedTestRun(boolean allOK) {
        // Or is it better to stop services after all post-processing done?
    }

    public void error(String msg) {
        // Ignore test errors
    }

    /**
     * Set service's start mode.
     * @param mode one of supported
     * {@link com.sun.javatest.service.ServiceManager#StartMode} values.
     */
    public void setStartMode(StartMode mode) {
        this.mode = mode;
    }

    private Set<String> selectActiveServices(Iterator iter) {
        Set<String> active = new TreeSet();
        Set<TestPath> copy = new HashSet(testServiceMap);
        TestResult tr;
        TestDescription td;
        while (!copy.isEmpty() && active.size() < services.size() &&
                (tr = (TestResult)iter.next()) != null ) {
            try {
                td = tr.getDescription();
                HashSet<TestPath> toRemove = new HashSet();
                for (TestPath p : copy) {
                    if (p.matches(td)) {
                        for (String sId : p.getServices()) {
                            active.add(sId);
                        }
                        toRemove.add(p);
                    }
                }
                copy.removeAll(toRemove);
            } catch (Fault ex) {}
        }
        return active;
    }

    private synchronized void stopServices() {
        Set<String> toRemove = new TreeSet();
        for (String s : activeServices) {
            if (stopService0(s)) {
                toRemove.add(s);
            }
        }
        activeServices.removeAll(toRemove);

        if (harness != null) {
            harness.removeObserver(this);
            harness = null;
        }

        writer.finish();
    }

    private class WritingThread extends Thread {
        private Map<String, Writer> outs = new HashMap();
        private Map<String, Writer> errs = new HashMap();

        private Map<String, InputStreamReader> sOuts = new HashMap();
        private Map<String, InputStreamReader> sErrs = new HashMap();

        public WritingThread() {
            setDaemon(true);
        }

        public synchronized void add(Service s) {
            String sID = "";
            try {
                sID = s.getId();

                Writer w = outs.get(sID);
                if (w == null) {
                    outs.put(sID, new LogWriter(s.getLog(), Level.ALL));
                }
                InputStreamReader or  = new InputStreamReader(s.getInputStream());
                sOuts.put(sID, or);

                w = errs.get(sID);
                if (w == null) {
                    errs.put(sID, new LogWriter(s.getLog(), Level.SEVERE));
                }
                InputStreamReader er  = new InputStreamReader(s.getErrorStream());
                sErrs.put(sID, er);

                notifyAll();
            } catch (NotConnectedException ex) {
                String msg = "Service :" + sID;
                msg += "Error occurred when trying to access service streams.\n";
                msg += ex.getMessage();
                commonLog.log(Level.WARNING, msg);
            }
        }

        public synchronized void remove(String sID) {
            try {
                Writer w = outs.get(sID);
                if (w != null) {
                    w.flush();
                }
                InputStreamReader r = sOuts.get(sID);
                if (r != null) {
                    r.close();
                }
                sOuts.remove(sID);

                w = errs.get(sID);
                if (w != null) {
                    w.flush();
                }
                r = sErrs.get(sID);
                if (r != null) {
                    r.close();
                }
                sErrs.remove(sID);
            } catch (IOException ex) {
            }
        }

        public synchronized void setOutputWriter(String sID, Writer out) {
            if (outs == null) {
                outs = new HashMap();
            }
            outs.put(sID, out);
        }

        public synchronized void setErrorWriter(String sID, Writer err) {
            if (errs == null) {
                errs = new HashMap();
            }
            errs.put(sID, err);
        }


        synchronized void finish() {
            for (String sID : outs.keySet()) {
                remove(sID);
            }
            for (String sID : errs.keySet()) {
                remove(sID);
            }
        }

        public void run() {
            while (true) {
                synchronized (this) {
                    if (sOuts.size() == 0 && sErrs.size() == 0) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {}
                    }
                }
                write(outs, sOuts);
                write(errs, sErrs);
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException ex) {}
            }
        }

        private void write(
                Map<String, Writer> wm,
                Map<String, InputStreamReader> rm) {

            char[] buf = new char[1000];

            List<String> toRemove = new LinkedList();

            String[] idCopy;
            synchronized (this) {
                idCopy = rm.keySet().toArray(new String[rm.keySet().size()]);
            }

            for (String sID : idCopy) {
                InputStreamReader r = rm.get(sID);
                Writer w = wm.get(sID);
                if (r != null && w != null) {
                    int n = 0;
                    try {
                        n = r.read(buf);
                        if (n > 0) {
                            w.write(buf, 0, n);
                        }
                    } catch (IOException ex) {}
                    if (n == -1) {
                        toRemove.add(sID);
                    }
                }
            }

            for (String sID : toRemove) {
                synchronized (this) {
                    InputStreamReader r = rm.remove(sID);
                    if (r != null) { // reader could be removed by other thread
                        try {
                            r.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        }

    }

    private class WatchDog extends Thread {
        private boolean stop;
        private Set<Observer> observers = new HashSet();
        private int pause;
        private Set<String> active;
        private Object sync;


        public WatchDog(int timeout) {
            this.pause = timeout;
            setDaemon(true);
            active = getActiveServices();
            sync = new Object();
        }

        public void addObserver(Observer o) {
            observers.add(o);
        }
        public void removeObserver(Observer o) {
            observers.remove(o);
        }
        public Set<Observer> getObservers() {
            return observers;
        }

        public void refreshServices() {
            active = getActiveServices();
            synchronized (sync) {
                sync.notifyAll();
            }
        }

        public void run() {
            stop = false;
            boolean isAlive = true;
            while (true) {

                synchronized (sync) {
                    if (active.size() == 0) {
                        try {
                            sync.wait();
                        } catch (InterruptedException ex) {}
                    }
                }

                for (String sID : active) {
                    try {
                        isAlive = services.get(sID).isAlive();
//                        long time = System.currentTimeMillis();
//                        System.out.println("Service: " + sID);
//                        System.out.println("TIME: " + time);
//                        System.out.println("IS_ALIVE: " + isAlive);
                        for (Observer o : observers) {
                            o.handleAlive(sID, isAlive);
                        }
                    } catch (NotConnectedException ex) {
                        for (Observer o : observers) {
                            o.handleNotConnected(sID, ex);
                        }
                    } catch (ServiceError ex) {
                        for (Observer o : observers) {
                            o.handleError(sID, ex);
                        }
                    }
                }
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException ex) {}
            }
        }

    }

    public static interface Observer {
        public void handleAlive(String sID, boolean alive);
        public void handleNotConnected(String sID, NotConnectedException ex);
        public void handleError(String sID, ServiceError ex);
    }

    public void addObserver(Observer o) {
        if (watchDog != null) {
            watchDog.addObserver(o);
        }
    }

    public void removeObserver(Observer o) {
        if (watchDog != null) {
            watchDog.removeObserver(o);
        }
    }

    public static class ServiceCommandManager extends CommandManager {
        private static StartMode mode = StartMode.UP_FRONT;

        public static StartMode getMode() {
            return mode;
        }

        @Override
        public Node getHelp() {
            String[] cmds = {
                ServiceStartCommand.getName()
            };
            return new HelpTree.Node(i18n, "cmds", cmds);
        }

        @Override
        public boolean parseCommand(String cmd, ListIterator argIter, CommandContext ctx) throws Command.Fault {
            if (isMatch(cmd, ServiceStartCommand.getName())) {
                if (!argIter.hasNext()) {
                    return false;
                }
                String val = (String)argIter.next();
                for (StartMode m : StartMode.values()) {
                    if (m.getKey().equalsIgnoreCase(val)) {
                        mode = m;
                        return true;
                    }
                }
                return false;
            }

            return false;
        }

        private static class ServiceStartCommand extends Command {

            static String getName() {
                return "startServices";
            }

            ServiceStartCommand() {
                super(getName());
            }

            @Override
            public void run(CommandContext ctx) throws Command.Fault {}
        }


        private I18NResourceBundle i18n =
                I18NResourceBundle.getBundleForClass(this.getClass());
    }

}
