/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Iterator;

/**
 * An iterator that can read ahead of the current position, either for
 * performance reasons or to help find out the number of items returned by an
 * iterator before accessing them.
 */
public class ReadAheadIterator implements Iterator
{
    /**
     * A constant indicating that no read ahead is required.
     * @see #ReadAheadIterator
     */
    public static final int NONE = 0;

    /**
     * A constant indicating that limited read ahead is required.
     * @see #ReadAheadIterator
     */
    public static final int LIMITED = 1;

    /**
     * A constant indicating that full read ahead is required.
     * @see #ReadAheadIterator
     */
    public static final int FULL = 2;

    /**
     * Create a ReadAheadIterator.
     * @param source The iterator from which to read ahead
     * @param mode A value indicating the type of read ahead required.
     * @see #NONE
     * @see #LIMITED
     * @see #FULL
     */
    public ReadAheadIterator(Iterator source, int mode) {
        this(source, mode, DEFAULT_LIMITED_READAHEAD);
    }

    /**
     * Create a ReadAheadIterator.
     * @param source The iterator from which to read ahead.
     * @param mode A value indicating the type of read ahead required.
     * @param amount A value indicating the amount of read ahead required,
     * if the mode is set to LIMITED. If the mode is NON or FULL, this
     * parameter will be ignored.
     * @see #NONE
     * @see #LIMITED
     * @see #FULL
     */
    public ReadAheadIterator(Iterator source, int mode, int amount) {
        this.source = source;
        setMode(mode, amount);
    }

    /**
     * Check if all available items from the underlying source iterator
     * have been read.
     * @return true if all available items from the underlying source iterator
     * have been read.
     */
    public synchronized boolean isReadAheadComplete() {
        return (worker == null ? !source.hasNext() : !sourceHasNext);
    }

    /**
     * Get the number of items read (so far) from the underlying source iterator.
     * If the read ahead has not yet completed, this will be a partial count of
     * the total set of items available to be read.  If the read ahead is complete,
     * the value will be the total number of items returned from the underlying
     * source iterator.
     * @return the number of items (so far) from the underlying source iterator.
     * @see #isReadAheadComplete
     */
    public synchronized int getItemsFoundCount() {
        return (usedCount + queue.size());
    }

    /**
     * Are there items that have not be read-ahead yet?
     * @return True if the source has no more elements, false otherwise.
     * @deprecated Use hasNext().
     */
    public synchronized boolean isSourceExhausted() {
        return (worker == null ? !source.hasNext() : !sourceHasNext);
    }

    /**
     * How many elements have been distributed through <code>getNext()</code>.
     * @return number of used elements, greater-than or equal-to zero
     * @deprecated Will not be supported in the future.
     * @see #getItemsFoundCount
     */
    public synchronized int getUsedElementCount() {
        return usedCount;
    }

    /**
     * Number of items have been read-ahead by not distributed.
     * @return number of items ready to be distributed, greater-than
     *         or equal-to zero
     * @deprecated Will not be supported in the future.
     */
    public synchronized int getOutputQueueSize() {
        return queue.size();
    }

    /**
     * Set the type and/or amount of read ahead required.
     * @param mode A value indicating the type of read ahead required.
     * @param amount A value indicating the amount of read ahead required,
     * if the mode is set to LIMITED. If the mode is NON or FULL, this
     * parameter will be ignored.
     */
    synchronized void setMode(int mode, int amount) {
        switch (mode) {
        case NONE:
            minQueueSize = 0;
            maxQueueSize = 0;
            if (worker != null) {
                worker = null;
                notifyAll();  // wake up worker if necessary
            }
            break;

        case LIMITED:
            if (amount <= 0)
                throw new IllegalArgumentException();
            minQueueSize = Math.min(10, amount);
            maxQueueSize = amount;
            break;

        case FULL:
            minQueueSize = 10;
            maxQueueSize = Integer.MAX_VALUE;
            break;

        default:
            throw new IllegalArgumentException();
        }
    }

    public synchronized boolean hasNext() {
        return (queue.size() > 0
                || (worker == null ? source.hasNext() : sourceHasNext));
    }

    public synchronized Object next() {
        // see if there are items in the read ahead queue
        Object result = queue.remove();

        if (result == null) {
            // queue is empty: check whether to read source directly, or rely on the worker thread
            if (maxQueueSize == 0)
                // no read ahead, so don't start worker; use source directly
                result = source.next();
            else {
                if (worker == null) {
                    // only start a worker if there are items for it to read
                    sourceHasNext = source.hasNext();
                    if (sourceHasNext) {
                        // there is more to be read, so start a worker to read it
                        worker = new Thread("ReadAheadIterator" + (workerNum++)) {
                                public void run() {
                                    readAhead();
                                }
                            };
                        worker.start();
                    }
                }
                else {
                    // ensure worker is awake
                    notifyAll();
                }

                // wait for the worker to deliver some results
                while (sourceHasNext && queue.isEmpty()) {
                    try {
                        wait();
                    }
                    catch (InterruptedException e) {
                        // should not happen, but if it does, propogate the interrupt
                        Thread.currentThread().interrupt();
                    }
                }

                result = queue.remove();
            }
        }
        else if (sourceHasNext && (queue.size() < minQueueSize)) {
            // we've got something from the queue, but the queue is getting empty,
            // so ensure worker is awake
            notifyAll();
        }

        if (result != null)
            usedCount++;

        return result;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * The body of the worker thread which is used to perform the read ahead.
     * While the worker is running, it notionally "owns" the source iterator.
     * As such, read ahead from the source is not synchronized; instead,
     * just the updates to the queue and other monitored data with the results
     * of the read ahead are synchronized. This ensure minimum latency on the
     * main monitor lock.
     */
    private void readAhead() {
        final Thread thisThread = Thread.currentThread();
        boolean keepReading;

        // check whether the thread is really required
        synchronized (this) {
            keepReading = (sourceHasNext && (thisThread == worker));
        }

        try {
            while (keepReading) {
                // sourceHasNext is true, which means there is another item
                // to be read, so read it, and also check whether there is
                // another item after that
                Object srcNext  = source.next();
                boolean srcHasNext = source.hasNext();

                // get the lock to update the queue and sourceHasNext;
                // check that the worker is still required; and
                // wait (if necessary) for the queue to empty a bit
                synchronized (this) {
                    queue.insert(srcNext);
                    sourceHasNext = srcHasNext;
                    notifyAll();

                    keepReading = (sourceHasNext && (thisThread == worker));

                    while (queue.size() >= maxQueueSize && keepReading) {
                        wait();
                        keepReading = (sourceHasNext && (thisThread == worker));
                    }
                }
            }
        }
        catch (InterruptedException e) {
            // ignore
        }
        finally {
            // if this is still the main worker thread, zap the
            // reference to the thread, to help GC.
            synchronized (this) {
                if (thisThread == worker)
                    worker = null;
            }
        }
    }

    //------------------------------------------------------------------------------------------
    //
    // Instance variables: access to all of these (except source) must be synchronized.

    /**
     * The queue to hold the items that have been read from the underlying source iterator.
     */
    private final Fifo queue = new Fifo();

    /**
     * The underlying source iterator.  If the worker thread is running, it alone
     * should access this iterator; otherwise, access to this should be synchronized,
     * along with everything else.
     * @see #worker
     */
    private final Iterator source;

    /**
     * A value indicating whether the underlying source iterator has more values to be read.
     * Use this instead of source.hasNext() when the worker thread is running.
     */
    private boolean sourceHasNext;

    /**
     * A minimum size for the queue. If the queue falls below this size, and if there
     * are more items to be read, the worker thread will be woken up to replenish the queue.
     * This may happen if the mode is set to PARTIAL and the worker thread fills the queue.
     */
    private int minQueueSize;

    /**
     * Set a maximum size for the queue, which is derived from the type and amount of
     * read ahead, given to setMode.
     * If the worker thread determines the queue size is bigger than this value, it will
     * wait until the size goes below minQueueSize.
     */
    private int maxQueueSize;

    /**
     * The number of items (i.e. not including null) returned from next().
     */
    private int usedCount;

    /**
     * The worker thread that does the read ahead. While it is set, the worker thread
     * should be the only one to access the underlying source iterator, which it will
     * do unsynchronized.
     */
    private Thread worker;

    /**
     * A counter for generating names for worker threads.
     */
    private static int workerNum;

    /**
     * The default amount for LIMITED read ahead.
     */
    private static final int DEFAULT_LIMITED_READAHEAD = 100;
}
