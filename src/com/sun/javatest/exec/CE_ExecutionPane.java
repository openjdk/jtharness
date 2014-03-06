/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.NumberFormat;
import java.text.ParsePosition;
import javax.help.CSH;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters.ConcurrencyParameters;
import com.sun.javatest.Parameters.MutableConcurrencyParameters;
import com.sun.javatest.Parameters.MutableTimeoutFactorParameters;
import com.sun.javatest.Parameters.TimeoutFactorParameters;
import com.sun.javatest.tool.UIFactory;

/**
 * Standard values, core values pane (concurrency, timeout, ...).
 */
class CE_ExecutionPane extends CE_StdPane
{
    CE_ExecutionPane(UIFactory uif, InterviewParameters config) {
        super(uif, config, "exec");

        updateConfig();
        initGUI();
    }

    boolean isOKToClose() {
        if (mutableConcurrencyParameters == null && mutableTimeoutFactorParameters == null)
            return true;

        // check concurrency is OK...

        String cs = concurrencyField.getText();
        if (cs == null || cs.length() == 0) {
            uif.showError("ce.exec.noConcurrency");
            return false;
        }

        /*
        try {
            int c = Integer.parseInt(cs);
            if (c < ConcurrencyParameters.MIN_CONCURRENCY || c > ConcurrencyParameters.MAX_CONCURRENCY) {
                uif.showError("ce.exec.badRangeConcurrency",
                              new Object[] { new Integer(ConcurrencyParameters.MIN_CONCURRENCY),
                                             new Integer(ConcurrencyParameters.MAX_CONCURRENCY) });
                return false;
            }
        }
        catch (NumberFormatException e) {
            uif.showError("ce.exec.badConcurrency");
            return false;
        }
        */

        {
            NumberFormat fmt = NumberFormat.getIntegerInstance(); // will be locale-specific
            ParsePosition pos = new ParsePosition(0);
            Number num = fmt.parse(cs, pos);

            if (num != null && (pos.getIndex() == cs.length())) {
                int c = num.intValue();
                if (c < ConcurrencyParameters.MIN_CONCURRENCY || c > ConcurrencyParameters.MAX_CONCURRENCY) {
                    uif.showError("ce.exec.badRangeConcurrency",
                                  new Object[] { new Integer(ConcurrencyParameters.MIN_CONCURRENCY),
                                                 new Integer(ConcurrencyParameters.MAX_CONCURRENCY) });
                    return false;
                }
            }
            else {
                uif.showError("ce.exec.badConcurrency");
                return false;
            }
        }

        // check timeout factor is OK...

        String ts = timeoutFactorField.getText();
        if (ts == null || ts.length() == 0) {
            uif.showError("ce.exec.noTimeoutFactor");
            return false;
        }

        {
            NumberFormat fmt = NumberFormat.getNumberInstance(); // will be locale-specific
            ParsePosition pos = new ParsePosition(0);
            Number num = fmt.parse(ts, pos);

            if (num != null && (pos.getIndex() == ts.length())) {
                float t = num.floatValue();
                if (t < TimeoutFactorParameters.MIN_TIMEOUT_FACTOR || t > TimeoutFactorParameters.MAX_TIMEOUT_FACTOR) {
                    uif.showError("ce.exec.badRangeTimeoutFactor",
                                  new Object[] { new Float(TimeoutFactorParameters.MIN_TIMEOUT_FACTOR),
                                                 new Float(TimeoutFactorParameters.MAX_TIMEOUT_FACTOR) });
                    return false;
                }
            }
            else {
                uif.showError("ce.exec.badTimeoutFactor");
                return false;
            }
        }

        // all checked; must be OK ...

        return true;
    }


    void load() {
        updateConfig();

        // update the values displayed and update mutability
        concurrencyField.setText(String.valueOf(config.getConcurrency()));
        concurrencyField.setEnabled(mutableConcurrencyParameters != null);

        NumberFormat fmt = NumberFormat.getNumberInstance();  // is locale-specific
        timeoutFactorField.setText(fmt.format(new Double(config.getTimeoutFactor())));
        timeoutFactorField.setEnabled(mutableTimeoutFactorParameters != null);
    }

    /**
     * Update internal values from the configuration.
     */
    void updateConfig() {
        concurrencyParameters = config.getConcurrencyParameters();
        if (concurrencyParameters instanceof MutableConcurrencyParameters)
            mutableConcurrencyParameters =
                (MutableConcurrencyParameters) concurrencyParameters;
        else
            mutableConcurrencyParameters = null;

        timeoutFactorParameters = config.getTimeoutFactorParameters();
        if (timeoutFactorParameters instanceof MutableTimeoutFactorParameters)
            mutableTimeoutFactorParameters =
                (MutableTimeoutFactorParameters) timeoutFactorParameters;
        else
            mutableTimeoutFactorParameters = null;

    }

    void save() {
        if (mutableConcurrencyParameters != null) {
            int c = getInt(concurrencyField.getText(), 1);
            mutableConcurrencyParameters.setConcurrency(c);
        }

        if (mutableTimeoutFactorParameters != null) {
            float t = getFloat(timeoutFactorField.getText(), 1);
            mutableTimeoutFactorParameters.setTimeoutFactor(t);
        }
    }

    private void initGUI() {
        CSH.setHelpIDString(this, "confEdit.execTab.csh");

        JPanel p = uif.createPanel("ce.exec", new GridBagLayout(), false);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.gridwidth = 1;
        lc.insets.right = 5;
        lc.weightx = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.anchor = GridBagConstraints.WEST;
        fc.gridwidth = GridBagConstraints.REMAINDER;

        concurrencyLabel = uif.createLabel("ce.exec.concurrency", true);
        p.add(concurrencyLabel, lc);
        concurrencyField = uif.createInputField("ce.exec.concurrency", 5);
        concurrencyField.setEnabled(mutableConcurrencyParameters != null);
        concurrencyLabel.setLabelFor(concurrencyField);
        p.add(concurrencyField, fc);

        timeoutFactorLabel = uif.createLabel("ce.exec.timeoutFactor", true);
        p.add(timeoutFactorLabel, lc);
        timeoutFactorField = uif.createInputField("ce.exec.timeoutFactor", 5);
        timeoutFactorField.setEnabled(mutableTimeoutFactorParameters != null);
        timeoutFactorLabel.setLabelFor(timeoutFactorField);
        p.add(timeoutFactorField, fc);

        addBody(p);
    }

    private int getInt(String s, int dflt) {
        /* OLD
        try {
            return (s == null || s.trim().length() == 0 ? dflt : Integer.parseInt(s));
        }
        catch (NumberFormatException e) {
            return dflt;
        }
        */

        NumberFormat fmt = NumberFormat.getIntegerInstance(); // will be locale-specific
        ParsePosition pos = new ParsePosition(0);
        Number num = fmt.parse(s, pos);
        return (num != null && (pos.getIndex() == s.length()) ? num.intValue() : dflt);
    }

    private float getFloat(String s, float dflt) {
        /* OLD
        try {
            return (s == null || s.trim().length() == 0 ? dflt : Float.parseFloat(s));
        }
        catch (NumberFormatException e) {
            return dflt;
        }
        */

        NumberFormat fmt = NumberFormat.getNumberInstance(); // will be locale-specific
        ParsePosition pos = new ParsePosition(0);
        Number num = fmt.parse(s, pos);
        return (num != null && (pos.getIndex() == s.length()) ? num.floatValue() : dflt);
    }

    private ConcurrencyParameters concurrencyParameters;
    private MutableConcurrencyParameters mutableConcurrencyParameters;
    private JLabel concurrencyLabel;
    private JTextField concurrencyField;

    private TimeoutFactorParameters timeoutFactorParameters;
    private MutableTimeoutFactorParameters mutableTimeoutFactorParameters;
    private JLabel timeoutFactorLabel;
    private JTextField timeoutFactorField;
}
