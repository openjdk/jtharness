/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
import javax.help.CSH;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters.MutablePriorStatusParameters;
import com.sun.javatest.Parameters.PriorStatusParameters;
import com.sun.javatest.Status;
import com.sun.javatest.tool.UIFactory;

class CE_PriorStatusPane extends CE_StdPane
{
    CE_PriorStatusPane(UIFactory uif, InterviewParameters config) {
        super(uif, config, "status");

        updateConfig();
        initGUI();
    }

    boolean isOKToClose() {
        if (mutablePriorStatusParameters == null)
            return true;

        if (selectCheck.isSelected() && !isAnyStatusCheckSelected()) {
            uif.showError("ce.status.noneSelected");
            return false;
        }

        return true;
    }

    void updateConfig() {
        if (config == null)
            return;

        priorStatusParameters = config.getPriorStatusParameters();
        if (priorStatusParameters instanceof MutablePriorStatusParameters)
            mutablePriorStatusParameters = (MutablePriorStatusParameters) priorStatusParameters;
        else
            mutablePriorStatusParameters = null;
    }


    void load() {
        updateConfig();

        if (mutablePriorStatusParameters != null) {
            int sm = mutablePriorStatusParameters.getPriorStatusMode();
            selectCheck.setSelected(sm == MutablePriorStatusParameters.MATCH_PRIOR_STATUS);
            selectCheck.setEnabled(true);

            boolean[] statusValues = mutablePriorStatusParameters.getMatchPriorStatusValues();
            for (int i = 0; i < statusChecks.length; i++)
                statusChecks[i].setSelected(statusValues[i]);
        }
        else {
            boolean[] statusValues = priorStatusParameters.getPriorStatusValues();
            if (statusValues == null) {
                selectCheck.setSelected(false);
                for (int i = 0; i < statusChecks.length; i++)
                    statusChecks[i].setSelected(false);
            }
            else {
                selectCheck.setSelected(true);
                for (int i = 0; i < statusChecks.length; i++)
                    statusChecks[i].setSelected(statusValues[i]);
            }

            mutablePriorStatusParameters = null;
            selectCheck.setEnabled(false);
        }

        enableStatusFields();
    }

    void save() {
        if (mutablePriorStatusParameters != null) {
            int sm = (selectCheck.isSelected()
                      ? MutablePriorStatusParameters.MATCH_PRIOR_STATUS
                      : MutablePriorStatusParameters.NO_PRIOR_STATUS);
            mutablePriorStatusParameters.setPriorStatusMode(sm);

            boolean[] b = new boolean[Status.NUM_STATES];
            for (int i = 0; i < b.length; i++)
                b[i] = statusChecks[i].isSelected();
            mutablePriorStatusParameters.setMatchPriorStatusValues(b);
        }
    }

    private void initGUI() {
        CSH.setHelpIDString(this, "confEdit.statusTab.csh");

        JPanel p = uif.createPanel("ce.status", new GridBagLayout(),
                                    false);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;

        selectCheck = uif.createCheckBox("ce.status.select");
        selectCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                enableStatusFields();
            }
        });
        p.add(selectCheck, c);

        JLabel anyOfLabel = uif.createLabel("ce.status.anyOf", true);
        c.gridwidth = 1;
        c.insets.left = 17;
        c.insets.right = 5;
        c.weightx = 0;
        p.add(anyOfLabel, c);

        JPanel row = uif.createPanel("ce.status.body",
                                    new GridBagLayout(),
                                    false);
        row.setBorder(BorderFactory.createEtchedBorder());
        GridBagConstraints rc = new GridBagConstraints();
        rc.insets.left = 10;

        statusChecks[Status.PASSED] =
            uif.createCheckBox("ce.status.prev.passed", false);
        row.add(statusChecks[Status.PASSED], rc);

        statusChecks[Status.FAILED] =
            uif.createCheckBox("ce.status.prev.failed", true);
        row.add(statusChecks[Status.FAILED], rc);

        statusChecks[Status.ERROR] =
            uif.createCheckBox("ce.status.prev.error", true);
        row.add(statusChecks[Status.ERROR], rc);

        rc.insets.right = 10;
        statusChecks[Status.NOT_RUN] =
            uif.createCheckBox("ce.status.prev.notRun", true);
        row.add(statusChecks[Status.NOT_RUN], rc);
        uif.setToolTip(row, "ce.status.prev");

        c.fill = GridBagConstraints.NONE;
        c.insets.left = 0;
        anyOfLabel.setLabelFor(row);
        p.add(row, c);

        addBody(p);
    }

    private boolean isAnyStatusCheckSelected() {
        for (int i = 0; i < statusChecks.length; i++) {
            if (statusChecks[i].isSelected())
                return true;
        }
        return false;
    }

    private void enableStatusFields() {
        boolean enable = selectCheck.isEnabled() && selectCheck.isSelected();
        for (int i = 0; i < statusChecks.length; i++) {
            statusChecks[i].setEnabled(enable);
        }
    }

    private PriorStatusParameters priorStatusParameters;
    private MutablePriorStatusParameters mutablePriorStatusParameters;
    private JCheckBox selectCheck;
    private JCheckBox[] statusChecks = new JCheckBox[Status.NUM_STATES];

    private static final String ANY_OF = "anyOf";
    private static final String ALL = "all";
}
