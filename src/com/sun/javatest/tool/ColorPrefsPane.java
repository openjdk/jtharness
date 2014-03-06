/*
 * $Id$
 *
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.util.I18NResourceBundle;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ColorPrefsPane extends PreferencesPane {
        private JPanel inputColors;
        private UIFactory uif;
        private I18NResourceBundle i18n;

        public ColorPrefsPane(UIFactory uifactory) {
                this.uif = uifactory;
                i18n = I18NResourceBundle.getBundleForClass(ColorPrefsPane.class);
                setLayout(new GridBagLayout());
                Insets in = new Insets(2, 3, 2, 3);
                GridBagConstraints c = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, in, 3, 5);

                inputColors = new JPanel();
                inputColors.setBorder(uif.createTitledBorder("colorprefs.inputcolors")); // TODO i18n
                inputColors.setLayout(new GridBagLayout());

                GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, in, 3, 5);

                UIFactory.Colors[] colors = UIFactory.Colors.values();
                for(int i = 0; i < colors.length; i++) {
                        if (!colors[i].isConfigurable())
                                continue;
                        String colorName = colors[i].getPreferencesName();
                        JLabel label = uif.createLabel("colorprefs." + colorName);
                        JButton button = uif.createColorChooseButton(colorName, label, null);
                        button.addActionListener(new ActionListener() {
                                private String title;
                                private JButton button;

                                public void actionPerformed(ActionEvent e) {
                                        Color newColor = JColorChooser.showDialog(ColorPrefsPane.this, title, button.getBackground());
                                        if(newColor != null)
                                                button.setBackground(newColor);
                                }

                                public ActionListener init(String title, JButton button) {
                                        this.title = title;
                                        this.button = button;
                                        return this;
                                }
                        }.init(i18n.getString("colorprefs." + colorName + ".cctitle"), button));
                        gbc.gridx = 0;
                        gbc.weightx = 0.3;
                        gbc.gridy = i;
                        inputColors.add(label, gbc);
                        gbc.gridx = 1;
                        gbc.weightx = 1;
                        gbc.gridy = i;
                        inputColors.add(button, gbc);
                }

                this.add(inputColors, c);

                JButton defaults = uif.createButton("colorprefs.setdefaults", new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                Component[] components = inputColors.getComponents();
                                for(Component c: components) {
                                        if(c instanceof JButton) {
                                                JButton b = (JButton) c;
                                                String name = b.getName();
                                                b.setBackground(Color.decode(i18n.getString("colorprefs." + name + ".defvalue")));
                                        }
                                }
                        }
                });

                c.anchor = GridBagConstraints.EAST;
                c.fill = GridBagConstraints.NONE;
                this.add(defaults, c);

        }

        @Override
        public String getText() {
                return i18n.getString("colorprefs.name");
        }

        public void save(Map m) {
                super.save(m);
                Component[] components = inputColors.getComponents();
                for(Component c: components) {
                        if(c instanceof JButton) {
                                JButton b = (JButton) c;
                                UIFactory.Colors.getColorByPreferencesName(c.getName());
                                String colorCode = String.valueOf(b.getBackground().getRGB());
                                UIFactory.setColorByName(b.getName(), b.getBackground());
                                m.put(b.getName(),colorCode);
                        }
                }
        }
}
