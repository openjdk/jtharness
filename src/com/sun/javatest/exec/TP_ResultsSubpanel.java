/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.EventQueue;
import java.util.Enumeration;
import javax.help.CSH;
import com.sun.javatest.TestResult;
import com.sun.javatest.tool.UIFactory;

/**
 * A subpanel of TestPanel that displays the test result properties.
 */

class TP_ResultsSubpanel
    extends TP_PropertySubpanel
{

    TP_ResultsSubpanel(UIFactory uif) {
        super(uif, "rslt");
        CSH.setHelpIDString(this, "browse.resultPropertiesTab.csh");
    }

    protected synchronized void updateSubpanel(TestResult currTest) {
        if (subpanelTest != null)
            subpanelTest.removeObserver(observer);

        super.updateSubpanel(currTest);
        updateEntries();

        // if it is mutable, track updates
        if (subpanelTest.isMutable())  {
            subpanelTest.addObserver(observer);
        }
    }

    private void updateEntries() {
        for (Enumeration e = subpanelTest.getPropertyNames(); e.hasMoreElements(); ) {
            try {
                String key = (String)(e.nextElement());
                String val = subpanelTest.getProperty(key);
                updateEntry(key, val);
            }
            catch (TestResult.Fault f) {
            }
        }
    }

    private void updateEntriesLater(final TestResult tr) {
        if (tr == subpanelTest) {
            if (EventQueue.isDispatchThread())
                updateEntries();
            else {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            if (tr == subpanelTest)
                                updateEntries();
                        }
                    });
            }
        }
    }

    private void updateEntryLater(final TestResult tr, final String name, final String value) {
        if (tr == subpanelTest) {
            if (EventQueue.isDispatchThread())
                updateEntry(name, value);
            else {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            if (tr == subpanelTest)
                                updateEntry(name, value);
                        }
                    });
            }
        }
    }

    private TRObserver observer = new TRObserver();

    //------------------------------------------------------------------------------------

    private class TRObserver
        implements TestResult.Observer
    {
        public void completed(TestResult tr) {
            updateEntriesLater(tr);
            tr.removeObserver(this);
        }

        public void createdSection(TestResult tr, TestResult.Section section) {
        }

        public void completedSection(TestResult tr, TestResult.Section section) {
        }

        public void createdOutput(TestResult tr, TestResult.Section section,
                                  String outputName) {
        }

        public void completedOutput(TestResult tr, TestResult.Section section,
                                    String outputName) {
        }

        public void updatedOutput(TestResult tr, TestResult.Section section,
                                  String outputName,
                                  int start, int end, String text) {
        }

        public void updatedProperty(TestResult tr, String name, String value) {
            updateEntryLater(tr, name, value);
        }
    }

}

