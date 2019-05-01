/*
 * $Id$
 *
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Map;

/**
 * Preferences for the desktop itself.
 */
class DesktopPrefsPane extends PreferencesPane {
    // cache localized strings because renderers tend to run a lot
    private static String TIP_SHOW_FOREVER;
    private Desktop desktop;
    private UIFactory uif;
    private ButtonGroup styleGrp;
    private JCheckBox ttipsCheck;
   /* MDI/SID views are removed
    private Component createDesktopStylePanel() {
        Box p = Box.createHorizontalBox();
        p.setBorder(uif.createTitledBorder("dt.prefs.style"));
        styleGrp = new ButtonGroup();
        for (int i = 0; i < Desktop.styleNames.length; i++) {
            String styleName = Desktop.styleNames[i];
            String uiKey = "dt.prefs." + styleName;
            JRadioButton b = uif.createRadioButton(uiKey, styleGrp);
            b.setActionCommand(styleName);
            p.add(b);
        }
        p.add(Box.createHorizontalGlue());

        return p;
    }
    */
    private JComboBox<Integer> ttDelay;
    private JComboBox<Integer> ttDuration;
    private JCheckBox saveCheck;
    private JCheckBox restoreCheck;
    private Integer[] tooltipDurations;
    private Integer[] tooltipDelays;
    DesktopPrefsPane(Desktop desktop, UIFactory uif) {
        this.desktop = desktop;
        this.uif = uif;
        setHelp("ui.prefs.appearance.csh");
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.insets.bottom = 10; // set off subsequent entries

        //add(createDesktopStylePanel(), c);
        styleGrp = new ButtonGroup();  // to avoid NPE

        add(createToolTipsPanel(), c);
        add(createShutdownPanel(), c);

        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        add(Box.createVerticalGlue(), c);
    }

    @Override
    public String getText() {
        return uif.getI18NString("dt.prefs.name");
    }

    @Override
    public void load(Map<String, String> m) {
        String styleName = m.get(Desktop.STYLE_PREF);
        if (styleName == null) {
            styleName = Desktop.styleNames[desktop.getStyle()];
        }

        for (Enumeration<AbstractButton> e = styleGrp.getElements(); e.hasMoreElements(); ) {
            JRadioButton rb = (JRadioButton) e.nextElement();
            if (rb.getActionCommand().equals(styleName)) {
                rb.setSelected(true);
                break;
            }
        }
        // tooltips
        String tipState = m.get(Desktop.TTIP_PREF);
        ttipsCheck.setSelected(tipState == null || tipState.equalsIgnoreCase("true"));

        boolean complete = false;       // used for delay and duration code

        String tipDelay = m.get(Desktop.TTIP_DELAY);
        try {
            int delay = Integer.parseInt(tipDelay);

            if (delay == Desktop.TTIP_DELAY_NONE) {
                // no delay
                ttDelay.setSelectedItem(tooltipDelays[0]);
                complete = true;
            } else {
                for (int i = 1; i < ttDelay.getItemCount(); i++) {
                    if (ttDelay.getItemAt(i).intValue() == delay) {
                        ttDelay.setSelectedIndex(i);
                        i = ttDelay.getItemCount(); // stop loop
                        complete = true;
                    }
                }   // for

            }   // if/else
        } catch (NumberFormatException e) {
            complete = false;
        }

        ttDelay.setEnabled(ttipsCheck.isSelected());
        ttDuration.setEnabled(ttipsCheck.isSelected());

        // default
        if (!complete) {
            ttDelay.setSelectedIndex(1);
        }

        complete = false;
        String tipDuration = m.get(Desktop.TTIP_DURATION);
        try {
            int duration = Integer.parseInt(tipDuration);
            if (duration == Desktop.TTIP_DURATION_FOREVER) {
                // forever
                ttDuration.setSelectedItem(tooltipDurations[tooltipDurations.length - 1]);
                complete = true;
            } else {
                for (int i = 0; i < tooltipDurations.length - 1; i++) {
                    if (duration == tooltipDurations[i].intValue()) {
                        ttDuration.setSelectedItem(tooltipDurations[i]);
                        complete = true;
                        i = tooltipDurations.length;    // stop loop
                    }
                }   // for
            }   // if/else
        }   // try
        catch (NumberFormatException e) {
            complete = false;
        }   // catch

        // default
        if (!complete) {
            ttDuration.setSelectedItem(tooltipDurations[1]);
        }

        // make it happen
        syncTooltipPrefs();

        // save on exit
        String saveState = m.get(Desktop.SAVE_ON_EXIT_PREF);
        saveCheck.setSelected(saveState == null || "true".equalsIgnoreCase(saveState)); // true (null) by default
        String restoreState = m.get(Desktop.RESTORE_ON_START_PREF); // false by default
        restoreCheck.setSelected(restoreState == null || "true".equalsIgnoreCase(restoreState));
    }

    @Override
    public void save(Map<String, String> m) {
        ButtonModel bm = styleGrp.getSelection();
        if (bm != null) {
            String styleName = bm.getActionCommand();
            for (int i = 0; i < Desktop.styleNames.length; i++) {
                if (styleName.equals(Desktop.styleNames[i])) {
                    desktop.setStyle(i);
                    m.put(Desktop.STYLE_PREF, styleName);
                    break;
                }
            }
        }

        boolean tips = ttipsCheck.isSelected();
        m.put(Desktop.TTIP_PREF, String.valueOf(tips));
        desktop.setTooltipsEnabled(tips);

        int delay = getTooltipDelay();
        m.put(Desktop.TTIP_DELAY, Integer.toString(delay));
        desktop.setTooltipDelay(delay);

        int duration = getTooltipDuration();
        m.put(Desktop.TTIP_DURATION, Integer.toString(duration));
        desktop.setTooltipDuration(duration);

        m.put(Desktop.SAVE_ON_EXIT_PREF, String.valueOf(saveCheck.isSelected()));
        desktop.setSaveOnExit(saveCheck.isSelected());

        m.put(Desktop.RESTORE_ON_START_PREF, String.valueOf(restoreCheck.isSelected()));
        desktop.setRestoreOnStart(restoreCheck.isSelected());

        syncTooltipPrefs();
    }

    /**
     * Force the GUI and the actual settings to be synchronized.
     */
    private void syncTooltipPrefs() {
        boolean tips = ttipsCheck.isSelected();
        desktop.setTooltipsEnabled(tips);

        int delay = getTooltipDelay();
        desktop.setTooltipDelay(delay);

        int duration = getTooltipDuration();
        desktop.setTooltipDuration(duration);
    }

    private Component createShutdownPanel() {
        Box p = Box.createVerticalBox();
        p.setBorder(uif.createTitledBorder("dt.prefs.shutdown"));

        saveCheck = uif.createCheckBox("dt.prefs.saveOnExit");
        p.add(saveCheck);

        restoreCheck = uif.createCheckBox("dt.prefs.restoreOnStart");
        p.add(restoreCheck);

        p.add(Box.createVerticalGlue());
        return p;
    }

    private Component createToolTipsPanel() {
        JPanel p = uif.createPanel("dt.prefs.tt", false);

        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.ipadx = 3;
        c.ipady = 5;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets.left = 3;
        c.insets.right = 3;
        c.insets.top = 2;
        c.insets.bottom = 2;

        p.setBorder(uif.createTitledBorder("dt.prefs.ttips"));
        ttipsCheck = uif.createCheckBox("dt.prefs.ttips");
        // override default name
        uif.setAccessibleName(ttipsCheck, "dt.prefs.ttips");

        ttipsCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean state = ttipsCheck.isSelected();
                ttDelay.setEnabled(state);
                ttDuration.setEnabled(state);
            }
        });
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        p.add(ttipsCheck, c);

        tooltipDurations = new Integer[]{1000, 2000, 3000, 5000, 10000, 15000, -1};
        tooltipDelays = new Integer[]{0, 1000, 2000, 3000, 5000, 10000};

        JLabel l = uif.createLabel("dt.prefs.ttDelay", true);

        ttDelay = uif.createChoice("dt.prefs.ttDelay", l);
        for (Integer tooltipDelay : tooltipDelays) {
            ttDelay.addItem(tooltipDelay);
        }

        ttDelay.setSelectedItem(tooltipDelays[0]);
        ttDelay.setRenderer(new TipDelayRenderer());

        c.gridwidth = 1;
        c.weightx = 0;
        p.add(l, c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        p.add(ttDelay, c);

        l = uif.createLabel("dt.prefs.ttDuration", true);

        ttDuration = uif.createChoice("dt.prefs.ttDuration", l);
        for (Integer tooltipDuration : tooltipDurations) {
            ttDuration.addItem(tooltipDuration);
        }

        ttDuration.setRenderer(new TipDurationRenderer());
        // nominate a reasonable choice
        ttDuration.setSelectedItem(tooltipDurations[
                Math.max(tooltipDurations.length - 2, 0)]);

        c.gridwidth = 1;
        c.weightx = 0;
        p.add(l, c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        p.add(ttDuration, c);

        return p;
    }

    /**
     * How long before a tooltip should be shown, as known by the GUI.
     *
     * @return Zero for no delay, otherwise a delay in milliseconds.
     */
    private int getTooltipDelay() {
        Integer value = (Integer) ttDelay.getSelectedItem();
        return value.intValue();
    }

    /**
     * How long tooltips should be shown, as known by the GUI.
     *
     * @return <code>Desktop.TTIP_DURATION_FOREVER</code>, otherwise a duration in
     * milliseconds.
     */
    private int getTooltipDuration() {
        int value = ((Integer) ttDuration.getSelectedItem()).intValue();
        return value < 0 ? Desktop.TTIP_DURATION_FOREVER : value;
    }

    private class TipDelayRenderer extends BasicComboBoxRenderer {
        @Override
        @SuppressWarnings("rawtypes")
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            Object theVal;
            if (value instanceof Integer) {
                int val = ((Integer) value).intValue();
                // convert to seconds and create localized text
                theVal = uif.getI18NString("dt.prefs.ttDelay", Integer.valueOf(val / 1000));
            } else {
                theVal = value;
            }

            return super.getListCellRendererComponent(
                    list, theVal, index, isSelected, cellHasFocus);
        }
    }

    private class TipDurationRenderer extends BasicComboBoxRenderer {
        TipDurationRenderer() {
            synchronized (DesktopPrefsPane.this) {
                if (TIP_SHOW_FOREVER == null) {
                    TIP_SHOW_FOREVER =
                            uif.getI18NString("dt.prefs.ttDuration.forev");
                }
            }
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            Object theVal;
            if (value instanceof Integer) {
                int val = ((Integer) value).intValue();
                if (val > 0) {
                    // convert to seconds and create localized text
                    theVal = uif.getI18NString("dt.prefs.ttDuration.sec",
                            Integer.valueOf(val / 1000));
                } else {
                    theVal = TIP_SHOW_FOREVER;
                }
            } else {
                theVal = value;
            }

            return super.getListCellRendererComponent(
                    list, theVal, index, isSelected, cellHasFocus);
        }
    }
}
