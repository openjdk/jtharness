/*
 * $Id$
 *
 * Copyright (c) 2010, 2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.exec;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.Debug;
import com.sun.javatest.util.I18NResourceBundle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The very classic implementation of Session that encapsulates the WorkDirectory
 * instance and InterviewParameters instance.
 *
 * @author Dmitry Fazunenko
 */
public class BasicSession implements SessionExt {

    /**
     * Instance of configuration
     */
    private final InterviewParameters config;

    /**
     * Instance of workdir
     */
    private WorkDirectory wd;

    /**
     * List of registered observers
     */
    protected final List<Observer> observers = new ArrayList<Observer>();

    /**
     * List of available filters
     */
    protected final List<String> filterNames = new ArrayList<String>();

    /**
     * List of observable properties
     */
    protected final List<String> props = new ArrayList<String>();

    static final String EL_FILTER = "ExcludeList";
    static final String PRIOR_FILTER = "PriorStatus";
    static final String KWD_FILTER = "Keywords";
    static final String RELEVANT_FILTER = "Relevant";

    public static final String CONFIG_NAME_PROP = "Configuration";
    public static final String WD_PROP = "WorkDir";

    private boolean isSorted = false;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(BasicSession.class);

    /**
     * Class of update to configuration
     */
    public static class U_NewConfig implements Update {
        public final InterviewParameters ip;
        public U_NewConfig(InterviewParameters ip) {
            this.ip = ip;
        }
    }

    /**
     * Class of update to WorkDirectory
     */
    static class U_NewWD implements Update {
        public final WorkDirectory wd;
        public U_NewWD(WorkDirectory wd) {
            this.wd = wd;
        }
    }


    /**
     * Event which is delivered when WorkDircotry has been set.
     */
    public static class E_NewWD implements Event {
        public final WorkDirectory wd;
        public final boolean doRestoreConfig; // for optimization
        E_NewWD(WorkDirectory wd, boolean doRestoreConfig) {
            this.wd = wd;
            this.doRestoreConfig = doRestoreConfig;
        }
    }

    /**
     * Event which is delivered when current configuration has been modified.
     */
    public static class E_NewConfig implements Event {
        public final InterviewParameters ip;
        public E_NewConfig(InterviewParameters ip) {
            this.ip = ip;
        }
    }

    /**
     * Extension to the Observer interface for those classes which
     * are sensitive to the order of notifying. If an observer wants
     * to be notified in the very first turn, it should implements OrderedObserver
     * interface, not just Observer and implement the order() method to return
     * Integer.MIN_VALUE. To be notified last, the order() method should return
     * Integer.MAX_VALUE. The order of regular observers is zero.
     */
    public static interface OrderedObserver extends Observer {
        /**
         * Returns number from Integer.MIN_VALUE to Integer.MAX_VALUE
         * to be sorted by when notifying.
         */
        public int order();
    }


    /**
     * Creates empty session for the passed test suite.
     * @param ts
     * @throws com.sun.javatest.exec.Session.Fault
     */
    public BasicSession(TestSuite ts) throws Fault {
        initFilterList();
        initPropertyList();
        try {
            config = ts.createInterview();
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

   /**
    * Applies the update. Ignores updates of unknown type. Subclasses need
    * override this method to support more update types.
    * @param u
    * @throws com.sun.javatest.exec.Session.Fault
    */
   public void update(Update u) throws Fault {
        // here to preserve 4.4.0 behavior (true)
        update(u, true);
    }


      /**
    * Applies the update. Ignores updates of unknown type. Subclasses need
    * override this method to support more update types.
    * @param u
    * @throws com.sun.javatest.exec.Session.Fault
    * @since 4.4.1
    */
   public void update(Update u, boolean updateConfig) throws Fault {
        if (u instanceof U_NewWD) {
            updateWorkDir(((U_NewWD)u).wd, updateConfig);
        } else if (u instanceof U_NewConfig) {
            updateNewConfig(((U_NewConfig)u).ip);
        }
    }

    public void addObserver(Observer obs) {
        if (obs != null && !observers.contains(obs)) {
            observers.add(obs);
            isSorted = false;
        }
    }

    public void removeObserver(Observer obs) {
        if (obs != null && observers.contains(obs)) {
            observers.remove(obs);
        }
    }

    /**
     * Delivers events to the all registered observers
     * @param evn - event to be sent out.
     */
    public void notifyObservers(Event evn) {
        if (!isSorted) {
            sortObservers();
        }
        for (Observer obs: observers) {
            queue.add(new Pair(obs, evn));
        }
        notifyQueue();
    }

    private final ArrayList<Pair> queue  = new ArrayList<Pair>();
    private boolean isNotifying = false;
    private static class Pair {
        final Observer obs;
        final Event evn;
        Pair(Observer obs, Event evn) {
            this.obs = obs;
            this.evn = evn;
        }
    }
    private void notifyQueue() {
        if (isNotifying) {
            return; // already working...
        }
        isNotifying = true;
        boolean cont = queue.size() > 0;
        while (cont) {
           Pair pair = queue.remove(0);
           pair.obs.updated(pair.evn); // this call may cause a new
                                       // pair to be add to the queue
           cont = queue.size() > 0;
        }
        isNotifying = false;
    }

    /**
     * Sorts observers by their order.
     */
    private void sortObservers() {
        Collections.sort(observers, new Comparator<Observer>() {
            public int compare(Observer o1, Observer o2) {
                long order1 = 0;
                if (o1 instanceof OrderedObserver) {
                    order1 = ((OrderedObserver)o1).order();
                }
                long order2 = 0;
                if (o2 instanceof OrderedObserver) {
                    order2 = ((OrderedObserver)o2).order();
                }
                return (int)(order1 - order2); // long is used to avoid overflow
            }

        });
        isSorted = true;
    }

    public TestFilter getTestFilter(String name) {
        if (config == null) {
            throw new IllegalStateException(i18n.getString("bc.configNotReady.err"));
        }
        TestFilter tf;
        if (filterNames.contains(name)) {
            tf = findFilter(name);
            if (tf != null) {
                return tf;
            }
        }
        throw new IllegalArgumentException(i18n.getString("bc.unknownFilter.err", name));
    }

    /**
     * Supposed to be overridden when extra filters added
     * @param name
     * @return found filter or null, if not found.
     */
    protected TestFilter findFilter(String name) {
        if (EL_FILTER.equals(name)) {
            return config.getExcludeListFilter();
        } else if (KWD_FILTER.equals(name)) {
            return config.getKeywordsFilter();
        } else if (PRIOR_FILTER.equals(name)) {
            return config.getPriorStatusFilter();
        } else if (RELEVANT_FILTER.equals(name)) {
            return config.getRelevantTestFilter();
        }
        return null;
    }

    public List<String> getTestFilterNames() {
        return filterNames;
    }

    public void save(Map map) {
        if (wd != null)
            map.put("workDir", wd.getPath());
        // save name of interview file
        if (config != null && config.getFile() != null)
            map.put("config", config.getFile().getPath());
    }

    public void restore(Map map) throws Fault {
        if (map == null)
            return;

        String wdPath = (String)map.get("workDir");
        if (wdPath == null) {
            return;
        }
        try {
            WorkDirectory workDir = WorkDirectory.open(new File(wdPath), config.getTestSuite());
            updateWorkDir(workDir, false);
            //this.wd = workDir;
            //applyWorkDir(wd);
        } catch (FileNotFoundException e) {
            // It's ok - saved WD could be removed or moved
            return;
        } catch (Exception e) {
            throw new Fault(e);
        }

        String ipPath = (String)map.get("config");
        if (ipPath == null) {
            return;
        }
        try {
            loadInterviewFromFile(wd, new File(ipPath));
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    /**
     * Loads interview from file.
     * @param wd
     * @param cfgFile
     * @throws com.sun.javatest.exec.Session.Fault
     */
    public void loadInterviewFromFile(WorkDirectory wd, File cfgFile) throws Fault {
        try {
            final long start = System.currentTimeMillis();

            config.load(cfgFile);
            logLoadTime("exec.log.iload", System.currentTimeMillis()-start,
                wd, cfgFile.getAbsolutePath());
            config.setWorkDirectory(wd);
            notifyObservers(new E_NewConfig(config));
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    public void dispose() {
        config.dispose();
    }
    public List<String> getPropertyNames() {
        return props;
    }

    public String getValue(String name) {
        if (props.contains(name)) {
            if (WD_PROP.equals(name)) {
                return wd == null ? null : wd.getPath();
            } else if (CONFIG_NAME_PROP.equals(name)) {
                if (config == null) {
                    return null;
                }
                File f = config.getFile();
                return f == null ?  null : f.getPath();
            }
        }
        throw new IllegalArgumentException(i18n.getString("bc.unknownProperty.err", name));
    }

    /**
     * Work directory assigned to the session.
     * @return The current wd set.
     */
    public WorkDirectory getWorkDirectory() {
        return wd;
    }

    public InterviewParameters getInterviewParameters() {
        return config;
    }

    public Parameters getParameters() {
        return getInterviewParameters();
    }

    public boolean isReady() {
        return config != null && config.isFinishable() && config.getFile() != null;
    }

    /**
     * Creates list of supported filters: ExcludeList, PriorStatus, Keyword,
     * Relevant.
     */
    protected void initFilterList() {
        filterNames.add(EL_FILTER);
        filterNames.add(PRIOR_FILTER);
        filterNames.add(KWD_FILTER);
        filterNames.add(RELEVANT_FILTER);
    }

    /**
     * Creates list of two session properties: WorkDirectory and Configuration.
     */
    protected void initPropertyList() {
        props.add(WD_PROP);
        props.add(CONFIG_NAME_PROP);
    }

    /**
     * Sets the work dir to the new specified value, inovkes applyWorkDir()
     * method, notifies observers of the work dir change.
     * <p>
     * It's not recommended to override this method.
     * @param wd - instance of WorkDirectory
     * @param doRestoreConfig - flag to be passed via Event
     *        signaling whether restoring configuration from wd is required
     */
    protected void updateWorkDir(WorkDirectory wd, boolean doRestoreConfig) {
        if (this.wd == wd) {
            return; // nothing to change
        }
        if (this.wd != null) {
            throw new IllegalStateException(i18n.getString("bc.resetWorkDir.err"));
        }
        this.wd = wd;
        applyWorkDir(wd);
        notifyObservers(new E_NewWD(wd, doRestoreConfig));
    }

    /**
     * Associates session with the work dir.
     * To be overridden when wd should be applied not only to session, but template
     * or other properties.
     *
     * @param wd
     */
    protected void applyWorkDir(WorkDirectory wd) {
        if (config != null) {
            config.setWorkDirectory(wd);
        }
    }

    /**
     * Method invoked as a reaction on U_NewConfig update.
     * Checks if there are any changes in the update, if none - does nothing,
     * Otherwise, copies new values into the main configuration instance,
     * notifies observers with E_NewConfig event.
     *
     * @param ip
     * @throws com.sun.javatest.exec.Session.Fault
     */
    protected void updateNewConfig(InterviewParameters ip) throws Fault {
        if (InterviewEditor.equal(ip, this.config) &&
                ip.getFile() != null && ip.getFile().equals(config.getFile())) {
            return; // nothing to update
        }
        try {
            InterviewEditor.copy(ip, this.config);
        } catch (Exception e) {
            throw new Fault(e);
        }
        notifyObservers(new E_NewConfig(this.config));
    }

    /**
     * Reloads interview if out of date.
     */
    public void reloadInterview() throws Fault {
        ensureInterviewUpToDate();
    }

    void ensureInterviewUpToDate() throws Fault {
        try {
            if (config.isFileNewer())  {
                config.load();
            }
        } catch (Exception e) {
            throw new Fault(e);
        }
    }
    /**
     * @param time Time used in loading, in ms.
     * @param wd Work directory associated, may not be null.
     * @param msg The message to include with the time, may be null, but usually
     *     is the path to the session file that was loaded.
     */
    private static void logLoadTime(String res,final long time, WorkDirectory wd, String msg) {
        if (wd == null)
            return;

        Logger log = null;
        try {
            log = wd.getTestSuite().createLog(wd, null, i18n.getString("exec.log.name"));
        }
        catch (TestSuite.DuplicateLogNameFault f) {
            try {
                log = wd.getTestSuite().getLog(wd, i18n.getString("exec.log.name"));
            }
            catch (TestSuite.NoSuchLogFault f2) { return; }
        }

        if (log != null) {
            Integer loadTime = new Integer((int) (time / 1000));
            Object[] params = new Object[]{loadTime, msg};
            String output = i18n.getString(res, params);
            log.info(output);

            if (debug > 0)
                Debug.println(output);
        }

    }
    private static int debug = Debug.getInt(ExecTool.class);

}
