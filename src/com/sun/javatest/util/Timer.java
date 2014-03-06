/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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

//import java.util.Date;
import java.util.Vector;

/**
 * Timer objects accept requests to call back on Timeable objects after a
 * specifiable delay.
 *
 * @see Timeable
 */

public class Timer
{
    /**
     * Implementations of this interface are passed to Timer, to be
     * called back after a specified interval.
     *
     * @see com.sun.javatest.util.Timer#requestDelayedCallback
     */
    public interface Timeable
    {
        /**
         * This method will be called if an implementation of this interface
         * is passed to a Timer.
         */
        void timeout();
    }

    /**
     * Entry objects are returned as the result calling
     * requestDelayedCallback on a timer; they may be used to cancel the request.
     */
    public class Entry
    {
        Entry(Timeable obj, long expiration) {
            this.obj = obj;
            this.expiration = expiration;
        }

        Timeable obj;
        long expiration;
    }

    /**
     * Create and start a timer object.
     */
    public Timer() {
        Thread t = new Thread() {
            public void run() {
                try {
                    Entry e;
                    while ((e = getNextEntry()) != null) {
                        e.obj.timeout();
                        e = null; // for GC
                    }
                }
                catch (InterruptedException e) {
                }
            }
        };

        t.setName("Timer" + nextThreadNum());
        t.setDaemon(true);
        t.start();
    }

    /* For autonumbering anonymous threads. */
    private static int threadInitNumber;
    private static synchronized int nextThreadNum() {
        return threadInitNumber++;
    }

    /**
     * Request that the Timeable object given will have its timeout() method
     * called after not less than delay milliseconds.
     *
     * @param obj       The object to be called back
     * @param delay     The number of milliseconds to delay before invoking the
     *                  timemout method on the callback object.
     * @return          An object which can be passed to cancel() to cancel this request
     */
    public synchronized Entry requestDelayedCallback(Timeable obj, long delay) {
        try {
            // add a new entry into a vector of entries sorted by increasing
            // absolute callback time
            long absCallbackTime = System.currentTimeMillis() + delay;
            //System.out.println("timeout set for " + obj + " at " + (new Date(absCallbackTime)));
            Entry e = new Entry(obj, absCallbackTime);
            for (int i = 0; i < entries.size(); i++) {
                Entry ee = (Entry)(entries.elementAt(i));
                if (e.expiration < ee.expiration) {
                    entries.insertElementAt(e, i);
                    return e;
                }
            }
            entries.addElement(e);
            return e;
        }
        finally {
            // kick timer thread awake to check this entry if necessary
            notify();
        }
    }

    /**
     * Cancel a prior request to requestDelayedEntry().
     *
     * @param e         The result of the prior call to requestDelayedEntry
     */
    public synchronized void cancel(Entry e) {
        entries.removeElement(e);
        // kick timer thread awake so it can exit if necessary
        notify();
    }

    /**
     * Stop accepting requests.
     */
    public synchronized void finished() {
        acceptingRequests = false;
        // kick timer thread awake so it can exit if necessary
        notify();
    }

    /**
     * Main body of timer thread .... get next entry to have its timeout called
     * @return The next entry to have timed out, or null if the timer is closing down.
     */
    private synchronized Entry getNextEntry() throws InterruptedException {
        while (acceptingRequests) {
            if (entries.size() == 0) {
                // nothing on list; wait until new requests come in
                wait();
            } else {
                long now = System.currentTimeMillis();
                Entry e = (Entry)(entries.elementAt(0));
                if (e.expiration <= now) {
                    // time to call back e.obj; do so and remove it from list
                    entries.removeElementAt(0);
                    return e;
                }
                else {
                    // not ready to invoke e yet; wait until nearer the time
                    wait(e.expiration - now);
                    // update current time
                    now = System.currentTimeMillis();
                    // list might have been updated during wait, so go round and
                    // process list again
                }
            }
        }
        return null;
    }

    /**
     * Original code ... problem is timeout is called while synchronized
     *
    public synchronized void run() {
        try {
            while (acceptingRequests) {
                if (entries.size() == 0) {
                    // nothing on list; wait until new requests come in
                    wait();
                } else {
                    long now = System.currentTimeMillis();
                    Entry e = (Entry)(entries.elementAt(0));
                    if (e.expiration > now) {
                        // not ready to invoke e yet; wait until nearer the time
                        wait(e.expiration - now);
                        // update current time
                        now = System.currentTimeMillis();
                        // list might have been updated during wait, so go round and
                        // process list again
                    } else {
                        // time to call back e.obj; do so and remove it from list
                        entries.removeElementAt(0);
                        e.obj.timeout();
                    }
                    e = null; // for GC
                }
            }
        }
        catch (InterruptedException e) {
        }
    }
    */

    //-----member variables-------------------------------------------------------

    private Vector entries = new Vector();
    private boolean acceptingRequests = true;
}
