/*
 * $Id$
 *
 * Copyright (c) 2008, 2009, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.tool.StringFitter;
import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.UIFactory;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public class NavigationPane extends JPanel {

    public NavigationPane(UIFactory uif, MultiFormatPane mediaPane)
                                               throws IllegalArgumentException {

        if(mediaPane == null) {
            throw new IllegalArgumentException("Corresponding media pane should exist");
        }
        this.mediaPane = mediaPane;

        this.uif = uif;

        history = new History();

        initActions();

        initGUI();

        mediaPane.setNavigationPane(this);
    }

    public void setHomeURL(URL url) {
        homeURL = url;
        homeAction.setEnabled(true);
    }

    public URL getHomeURL() {
        return homeURL;
    }

    public void setURLs(URL[] urls) {
        if(model != null) {
            model.removeAllElements();

            homeURL = urls[0];
            homeAction.setEnabled(homeURL != null);

            for(URL url : urls) {
                model.addElement(url);
            }
            model.setSelectedItem(homeURL);
        }
    }
    // When click link on corresponding media pane
    public void processURL(URL url) {
        history.add(url);
        backAction.setEnabled(history.hasPrev());
        forwardAction.setEnabled(history.hasNext());
        updateCombo(url);
    }

    public void clear() {
        if (model != null)
            model.removeAllElements();

        history.clear();
        backAction.setEnabled(false);
        forwardAction.setEnabled(false);
        homeAction.setEnabled(false);

        homeURL = null;
    }

    private void initGUI() {
        uif.initPanel(this, "np", false);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 0;
        c.gridy = 0;

        JLabel fileLbl = uif.createLabel("np.file", true);
        add(fileLbl, c);

        selectBox = uif.createChoice("np.choice", fileLbl);
        selectBox.setRenderer(new Renderer(selectBox));
        selectBox.setModel(createModel());
        selectBox.addItemListener(listener);
        selectBox.setMaximumRowCount(MAX_ROWS_DISPLAY);
        selectBox.setUI(new BasicComboBoxUI() {
            // wrap the content with a scrolling interface
            // would be nice if Swing did this for us
            protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(selectBox) {
                    protected JScrollPane createScroller() {
                        return new JScrollPane(list,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    }
                };
                return popup;
            }
            }   // class
            );
        uif.setAccessibleName(selectBox, "np.choice");  // override default a11y name

        c.gridx = 1;
        c.weightx = 2.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        add(selectBox, c);

        Action[] actions = { backAction, forwardAction, null, homeAction };

        toolBar = uif.createToolBar("np.toolbar", actions );
        toolBar.setFloatable(false);

        c.weightx = 0;
        c.gridx = 2;
        c.insets.left = 5;

        add(toolBar, c);

    }

    private void initActions() {
        homeAction = new ToolAction(uif, "np.home", true) {
            public void actionPerformed(ActionEvent e) {
                if (homeURL == null) {
                    mediaPane.setDefaultView();
                }
                else{
                    mediaPane.stopAudio();
                    mediaPane.loadPage(homeURL);
                }
            }
        };

        backAction = new ToolAction(uif, "np.back", true) {
            public void actionPerformed(ActionEvent e) {
                URL url = history.prev();
                if (url != null) {
                    mediaPane.stopAudio();
                    mediaPane.loadPage(url);
                }
            }
        };

        forwardAction = new ToolAction(uif, "np.forward", true) {
            public void actionPerformed(ActionEvent e) {
                URL url = history.next();
                if (url != null) {
                    mediaPane.stopAudio();
                    mediaPane.loadPage(url);
                }
            }
        };
    }


    private DefaultComboBoxModel createModel() {
        if (model == null)
            model = new DefaultComboBoxModel();
        return model;
    }

    private void updateCombo(URL s) {
        // check if the new element exists in the combo box...
        if (model.getIndexOf(s) < 0)
            model.addElement(s);

        URL item = (URL) selectBox.getSelectedItem();
        // check if the new element is already selected.
        // URL.equals can result in a big performance hit
        if (s != null && !item.toString().equals(s.toString()))
            selectBox.setSelectedItem(s);
    }

    private class Listener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                mediaPane.stopAudio();
                URL url = (URL) e.getItem();
                mediaPane.loadPage(url);
            }
        }
    }


// History
    private static class History {
        boolean hasPrev() {
            return (index > 0);
        }

        URL prev() {
            if (index == 0)
                return null;

            return (URL) (entries.elementAt(--index));
        }

        boolean hasNext() {
            return (index < entries.size() - 1);
        }

        URL next() {
            if (index == entries.size() - 1)
                return null;

            return (URL) (entries.elementAt(++index));
        }

        void add(URL u) {
            if (u == null)
                throw new NullPointerException();

            // if there is a current entry, and it matches the one to be added, we're done
            if (index >= 0 && index < entries.size() && entries.elementAt(index).equals(u))
                return;

            // if current entry not the last one, truncate to the current entry
            if (index < entries.size() - 1)
                entries.setSize(index + 1);

            // finally, add new entry
            entries.addElement(u);
            index = entries.size() - 1;
        }

        void clear() {
            entries.setSize(0);
            index = -1;
        }

        private Vector entries = new Vector();
        private int index;
    }




    private class Renderer extends DefaultListCellRenderer {

        public Renderer() {
            setPreferredSize(new JLabel("MinSize").getPreferredSize());
        }

        public Renderer(Container container) {
            setPreferredSize(new JLabel("MinSize").getPreferredSize());
            this.container = container;
            sf = new StringFitter(getFontMetrics(getFont()));
        }

        public Component getListCellRendererComponent(JList list, Object o, int index, boolean isSelected, boolean cellHasFocus) {
            String name = null;
            if (o instanceof URL) {
                URL url = (URL) o;

                // if not file URL
                if (!url.getProtocol().equals("file")) {
                    name = url.toString();
                }
                else {
                    // if file URL, remove the "file:" prefix
                    name = extractPrefix(url.toString(), "file:");
                    String baseName = null;
                    name = new File(name).getAbsolutePath();

                    File baseDir = mediaPane.getBaseDirectory();
                    if (baseDir != null && baseDir.getParentFile() != null) {
                        baseName = baseDir.getParentFile().getAbsolutePath();
                    }
                    // if contains base dir, only show file name
                    if (baseName != null &&
                        name.startsWith(baseName) &&
                        (name.length() > baseName.length())) {
                        name = name.substring(baseName.length() );
                        // in case of Unix
                        if (name.startsWith(File.separator)) {
                            name = name.substring(1);
                        }
                    }
                }
            }
            else
                name = String.valueOf(o);

        JLabel cell = (JLabel) super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
        if(container != null)
            cell.setText(sf.truncateBeginning(name, container.getWidth() - 17));
            cell.setToolTipText(name);
            return cell;
        }

        private String extractPrefix(String origStr, String target) {
            return (!origStr.startsWith(target)) ? origStr : origStr.substring(target.length());
        }

    private Container container;
    private StringFitter sf;
    }


    private UIFactory uif;
    private String uiKey;

    private History history;

    private Action homeAction;
    private Action backAction;
    private Action forwardAction;

    private JButton homeBtn;
    private JButton backBtn;
    private JButton forwardBtn;

    private JComboBox selectBox;

    private DefaultComboBoxModel model;
    private Listener listener = new Listener();
    private JToolBar toolBar;

    private static final int MAX_ROWS_DISPLAY = 20;

    private MultiFormatPane mediaPane;

    private URL homeURL;


}
