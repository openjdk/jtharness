/*
 * $Id$
 *
 * Copyright (c) 2010, 2012 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.Parameters;
import java.util.List;
import java.util.Map;

/**
 * An interface that encapsulates all properties required to run tests.
 * It's introduced to make it possible to get rid of such monsters as
 * InterviewParameters, Template and WorkDir.
 *
 * It's assumed that components will communicate Session via Update and Event
 * classes: those components which are aware how to modify the config
 * will apply some Update object to the config, the config in its turn will
 * send to all registered observers the corresponding Event object.
 *
 * <b>Important note:</b> JavaTest is not ready yet to operate with Session
 * instances, therefore one should implement the SessionExt interface or extends
 * BasicSession class to provide its own behavior.
 *
 * @see SessionExt
 * @see BasicSession
 */
public interface Session {
    // Currently in
    // the com.sun.javatest.exec package, but in the future it's better to
    // find more appropriate place for it.

    /**
     * Exception signaling of the problem happened while dealing with Session.
     */
    public class Fault extends Exception {
        public Fault(String reason) {
            super(reason);
        }
        public Fault(Throwable thr) {
            super(thr);
        }
    }

    /**
     * Root interface for all updates to Session.
     */
    public interface Update { }

    /**
     * Root interface for all events happened when state of Session changed.
     */
    public interface Event { }


    /**
     * Interface for observers of the Session state.
     */
    public interface Observer {
        /**
         * Invoked when state of config has changed
         * @param ev - Event describing the change
         */
        void updated(Event ev);
    }

    /**
     * Method to be invoked from outside to change the state of the Session.
     * @param u - object encapsulating data describing the change.
     * @throws com.sun.javatest.exec.Session.Fault in case of any problem
     */
    public void update(Update u) throws Fault;

    /**
     * Method to be invoked from outside to change the state of the Session.
     * @param u - object encapsulating data describing the change.
     * @param updateConfig - hint whether to reload the configuration from disk
     * @throws com.sun.javatest.exec.Session.Fault in case of any problem
     * @since 4.4.1
     */
    public void update(Update u, boolean updateConfig) throws Fault;

    /**
     * Registers the observer. Does nothing if the observer is null or already
     * registered.
     * @param obs - observer
     */
    public void addObserver(Observer obs);

    /**
     * Unregisters the observer. Does nothing if the observer is null or not
     * registered.
     * @param obs - observer
     */
    public void removeObserver(Observer obs);

    /**
     * Delivers events to the all registered observers
     * @param evn - event to be sent out.
     */
    public void notifyObservers(Event evn);

    /**
     * Gets test filter by its name.
     * @param name - should be from the list of supported names.
     * @return desired filter, if found
     * @throw new IllegalArgumentException if name is null or unknown.
     * @see getTestFilterNames
     */
    //public TestFilter getTestFilter(String name);

    /**
     * @return list of names of supported test filters.
     */
    //public List<String> getTestFilterNames();

    /**
     * Saves the config state to the file
     * @param file destination file
     * @throws com.sun.javatest.exec.Session.Fault
     */
    //public void save(File file) throws Fault;

    /**
     * Saves the config state to the map
     * @param map
     * @throws com.sun.javatest.exec.Session.Fault
     */
    public void save(Map map);


    /**
     * Restores the config state from the file
     * @param file - source file
     * @throws com.sun.javatest.exec.Session.Fault
     */
    //public void restore(File file) throws Fault;

    /**
     * Restores the config state from the map
     * @param map
     * @throws com.sun.javatest.exec.Session.Fault
     */
    public void restore(Map map) throws Fault;

    /**
     * Disposes configuration. Critical when heavy objects were used.
     */
    public void dispose();

    /**
     * Returns the config property names
     * @return Configuration property name List
     */
    public List<String> getPropertyNames();

    /**
     * @return the value of property or null if unset
     * @throws IllegalArgumentException if case of unknown name
     * @see #getPropertyNames
     */
    public String getValue(String name);

    /**
     * @return true if configuration is ready for test execution
     */
    public boolean isReady();

    /**
     * Data required to execute tests.
     * In future - should be replaced.
     * @return The current parameters in use.
     */
    public Parameters getParameters();

}
