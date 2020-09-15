/*
 * $Id$
 *
 * Copyright (c) 2003, 2009, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.tool.jthelp.JHelpContentViewer;

import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.Vector;

class FocusMonitor {
    private static final Color STD_COLOR = Color.black;
    private static final Color ERR_COLOR = Color.red;
    private static final Color WARN_COLOR = Color.yellow;
    private static final Color HILITE_COLOR = new Color(255, 255, 200);
    private static final String NEWLINE = System.getProperty("line.separator");
    private static FocusMonitor focusMonitor;
    private KeyStroke activateKey;
    private KeyStroke reportKey;
    private String reportFile;
    private JFrame frame;
    private SummaryPanel prevFocusPanel;
    private DetailPanel currFocusPanel;
    private Action reportAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            report();
        }
    };
    private SummaryPanel nextFocusPanel;
    private Component currentComponent;
    private boolean highlighting;
    private boolean savedOpaque;
    private Color savedBackground;
    private Action activateAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(true);
        }
    };

    private FocusMonitor() {
        KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        fm.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("focusOwner")) {
                update();
            }
        });
    }

    public static FocusMonitor access() {
        if (focusMonitor == null) {
            focusMonitor = new FocusMonitor();
        }

        return focusMonitor;
    }

    private static String getKeysString(Component c, int mode) {
        if (c == null) {
            return null;
        }

        Set<AWTKeyStroke> s = c.getFocusTraversalKeys(mode);
        StringBuilder sb = new StringBuilder();
        for (AWTKeyStroke value : s) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(value);
        }
        if (!c.areFocusTraversalKeysSet(mode)) {
            sb.append(" (inherited)");
        }
        return sb.toString();
    }

    private static String getPath(Component c) {
        StringBuffer sb = new StringBuffer();
        appendPath(sb, c);
        return sb.toString();
    }

    private static void appendPath(StringBuffer sb, Component c) {
        Container p = c.getParent();
        if (p != null) {
            appendPath(sb, p);
        }
        sb.append('/');
        String name = c.getName();
        if (name == null || name.isEmpty()) {
            if (p == null)  // special case, root component, no name
            {
                sb.append("(Root component)");
            } else {
                for (int i = 0; i < p.getComponentCount(); i++) {
                    if (p.getComponent(i) == c) {
                        sb.append(i);
                        break;
                    }
                }   // for
            }
        } else {
            sb.append(name);
        }
    }

    public void setOptions(String... opts) {
        for (String opt : opts) {
            if (opt.equals("-open")) {
                setVisible(true);
            } else if (opt.equals("-bg")) {
                setHighlightEnabled(true);
            } else {
                System.err.println("Warning: bad option for FocusMonitor: " + opt);
            }
        }
    }

    public void setActivateKey(String key) {
        activateKey = KeyStroke.getKeyStroke(key);
    }

    public void setReportKey(String key) {
        reportKey = KeyStroke.getKeyStroke(key);
    }

    public void setReportFile(String file) {
        reportFile = file;
    }

    public void monitor(Component c) {
        if (c == null
                || (frame != null && (frame == c || frame.isAncestorOf(c)))) {
            return;
        }

        if (activateKey != null || reportKey != null) {
            Window w = (Window) (c instanceof Window ? c
                    : SwingUtilities.getAncestorOfClass(Window.class, c));
            if (w == null) {
                return;
            }

            JRootPane root;
            if (w instanceof JFrame) {
                root = ((JFrame) w).getRootPane();
            } else if (w instanceof JDialog) {
                root = ((JDialog) w).getRootPane();
            } else {
                return;
            }

            if (root == null) {
                return;
            }

            InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(activateKey, "focusMonitor.activate");
            inputMap.put(reportKey, "focusMonitor.report");

            ActionMap actionMap = root.getActionMap();
            actionMap.put("focusMonitor.activate", activateAction);
            actionMap.put("focusMonitor.report", reportAction);
        }
    }

    public void report() {
        try {
            Writer out;
            if (reportFile == null) {
                out = new OutputStreamWriter(System.out, StandardCharsets.UTF_8) {
                    @Override
                    public void close() throws IOException {
                        flush();  // don't close System.out
                    }
                };
            } else {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile, true), StandardCharsets.UTF_8));
            }

            out.write("---------------------------------------");
            out.write(NEWLINE);
            report(out);
            out.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void setVisible(boolean b) {
        if (b) {
            if (frame == null) {
                initGUI();
            }
            KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            focusMonitor.monitor(fm.getFocusOwner());
            frame.setVisible(true);
            focusMonitor.update();
        } else if (frame != null) {
            frame.setVisible(false);
        }
    }

    private void deactivate() {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }

    private void update() {
        if (frame == null || !frame.isVisible()) {
            return;
        }

        KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Component c = fm.getFocusOwner();

        if (c == null || frame == c || frame.isAncestorOf(c)) {
            prevFocusPanel.setEnabled(false);
            currFocusPanel.setEnabled(false);
            nextFocusPanel.setEnabled(false);
        } else {
            if (highlighting) {
                setHighlight(currentComponent, false);
                setHighlight(c, true);
            }

            currentComponent = c;

            prevFocusPanel.setComponent(getPreviousFocus(c));
            currFocusPanel.setComponent(c);
            nextFocusPanel.setComponent(getNextFocus(c));

            Window w = fm.getFocusedWindow();
            while (w != null && !(w instanceof Frame)) {
                w = w.getOwner();
            }

            String title = "Focus Monitor";
            if (w instanceof Frame) {
                title += " - " + ((Frame) w).getTitle();
            }
            frame.setTitle(title);
        }
    }

    private void setHighlightEnabled(boolean b) {
        if (b != highlighting) {
            highlighting = b;
            KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Component c = fm.getFocusOwner();
            if (frame == null || !frame.isAncestorOf(c)) {
                setHighlight(c, highlighting);
            }
        }
    }

    private void setHighlight(Component c, boolean b) {
        if (c == null) {
            return;
        }
        if (b) {
            savedOpaque = c.isOpaque();
            savedBackground = c.getBackground();
            if (c instanceof JComponent) {
                ((JComponent) c).setOpaque(true);
            }
            c.setBackground(HILITE_COLOR);
        } else {
            if (c instanceof JComponent) {
                ((JComponent) c).setOpaque(savedOpaque);
            }
            c.setBackground(savedBackground);
        }
    }

    private void report(Writer out) throws IOException {
        if (currFocusPanel == null) {
            System.err.println("focus monitor not open: no component selected");
            return;
        }

        currFocusPanel.write(out);
    }

    private void initGUI() {
        JMenuBar menuBar = new JMenuBar();
        JMenu viewMenu = new JMenu("View");
        final JCheckBoxMenuItem showBackgroundMenuItem = new JCheckBoxMenuItem("background");
        viewMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                showBackgroundMenuItem.setSelected(highlighting);
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        showBackgroundMenuItem.addChangeListener(e -> setHighlightEnabled(showBackgroundMenuItem.isSelected()));
        viewMenu.add(showBackgroundMenuItem);
        menuBar.add(viewMenu);

        prevFocusPanel = new SummaryPanel();
        prevFocusPanel.setBorder(BorderFactory.createTitledBorder("previous focus"));

        currFocusPanel = new DetailPanel();
        currFocusPanel.setBorder(BorderFactory.createTitledBorder("current focus"));

        nextFocusPanel = new SummaryPanel();
        nextFocusPanel.setBorder(BorderFactory.createTitledBorder("next focus"));

        JPanel main = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        main.add(prevFocusPanel, c);
        c.insets.top = 10;
        main.add(currFocusPanel, c);
        main.add(nextFocusPanel, c);

        JFrame f = new JFrame("Focus Monitor");
        f.setJMenuBar(menuBar);
        f.setContentPane(main);
        f.pack();
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                deactivate();
            }
        });

        frame = f;
    }

    private Component getNextFocus(Component c) {
        if (c == null) {
            return null;
        }

        Container rootAncestor = c.getFocusCycleRootAncestor();
        Component comp = c;
        while (rootAncestor != null &&
                !(rootAncestor.isShowing() &&
                        rootAncestor.isFocusable() &&
                        rootAncestor.isEnabled())) {
            comp = rootAncestor;
            rootAncestor = comp.getFocusCycleRootAncestor();
        }

        if (rootAncestor == null) {
            return null;
        }

        FocusTraversalPolicy policy = rootAncestor.getFocusTraversalPolicy();
        Component toFocus = policy.getComponentAfter(rootAncestor, comp);
        if (toFocus == null) {
            toFocus = policy.getDefaultComponent(rootAncestor);
        }

        return toFocus;
    }

    private Component getPreviousFocus(Component c) {
        if (c == null) {
            return null;
        }

        Container rootAncestor = c.getFocusCycleRootAncestor();
        Component comp = c;
        while (rootAncestor != null &&
                !(rootAncestor.isShowing() &&
                        rootAncestor.isFocusable() &&
                        rootAncestor.isEnabled())) {
            comp = rootAncestor;
            rootAncestor = comp.getFocusCycleRootAncestor();
        }

        if (rootAncestor == null) {
            return null;
        }

        FocusTraversalPolicy policy = rootAncestor.getFocusTraversalPolicy();
        Component toFocus = policy.getComponentBefore(rootAncestor, comp);
        if (toFocus == null) {
            toFocus = policy.getDefaultComponent(rootAncestor);
        }

        return toFocus;
    }

    private Component getUpFocus(Component c) {
        if (c == null) {
            return null;
        }

        Container rootAncestor;
        for (rootAncestor = c.getFocusCycleRootAncestor();
             rootAncestor != null && !(rootAncestor.isShowing() &&
                     rootAncestor.isFocusable() &&
                     rootAncestor.isEnabled());
             rootAncestor = rootAncestor.getFocusCycleRootAncestor()) {
        }

        if (rootAncestor != null) {
            return rootAncestor;
        }

        Container window =
                (c instanceof Container) ? (Container) c : c.getParent();
        while (window != null && !(window instanceof Window)) {
            window = window.getParent();
        }
        if (window == null) {
            return null;
        }

        return window.getFocusTraversalPolicy().getDefaultComponent(window);
    }

    private static class Entry {
        JLabel label;
        JTextField field;
        boolean enabled;

        Entry(String name) {
            label = new JLabel(name + ": ");
            label.setName(name);
            field = new JTextField(60);
            field.setName(name + ".value");
            field.setEditable(false);
            label.setLabelFor(field);
        }

        void setParentEnabled(boolean parentEnabled) {
            label.setEnabled(parentEnabled && enabled);
            field.setEnabled(parentEnabled && enabled);
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
            label.setEnabled(enabled);
            field.setEnabled(enabled);
            field.setText("");
        }

        void setText(String s) {
            setEnabled(true);
            field.setText(s);
            field.setForeground(STD_COLOR);
        }

        void setText(String s, boolean ok) {
            setText(s, ok, STD_COLOR, ERR_COLOR);
        }

        void setText(String s, boolean ok, Color okColor, Color notOKColor) {
            setEnabled(true);
            field.setText(s);
            field.setForeground(ok ? okColor : notOKColor);
        }

        void setText(String s, String err) {
            if (s == null || s.isEmpty()) {
                setText(err, false);
            } else {
                setText(s, true);
            }
        }

        void setText(String s, String err, Color okColor, Color notOKColor) {
            if (s == null || s.isEmpty()) {
                setText(err, false, okColor, notOKColor);
            } else {
                setText(s, true, okColor, notOKColor);
            }
        }

        void write(Writer out) throws IOException {
            if (field.isEnabled()) {
                out.write(field.getForeground() == ERR_COLOR ? "** " : "   ");
                out.write(label.getText());
                out.write(field.getText());
                out.write(NEWLINE);
            }
        }
    }

    private static class SummaryPanel extends JPanel {
        private Vector<Entry> entries = new Vector<>();
        private Entry name;
        private Entry path;
        private Entry type;

        SummaryPanel() {
            setName("summary");
            setLayout(new GridBagLayout());
            add(type = new Entry("type"));
            add(name = new Entry("name"));
            add(path = new Entry("path"));
        }

        void setComponent(Component c) {
            if (c == null) {
                setEnabled(false);
                type.setEnabled(false);
                name.setEnabled(false);
                path.setEnabled(false);
            } else {
                setEnabled(true);
                type.setText(c.getClass().getName());
                name.setText(c.getName(), "no name", STD_COLOR, WARN_COLOR);
                path.setText(getPath(c));
            }
        }

        void write(Writer out) throws IOException {
            for (Entry e : entries) {
                e.write(out);
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            for (Entry e : entries) {
                e.setParentEnabled(enabled);
            }
        }

        protected void add(Entry entry) {
            GridBagConstraints lc = new GridBagConstraints();
            add(entry.label, lc);

            GridBagConstraints fc = new GridBagConstraints();
            fc.fill = GridBagConstraints.HORIZONTAL;
            fc.gridwidth = GridBagConstraints.REMAINDER;
            fc.weightx = 1;
            add(entry.field, fc);

            entries.add(entry);
        }
    }

    private static class DetailPanel extends SummaryPanel {
        private Entry accName;
        private Entry accDesc;
        private Entry toolTip;
        private Entry text;
        private Entry labelFor;
        private Entry mnemonic;
        private Entry fwdKeys;
        private Entry bwdKeys;
        private Entry upKeys;
        private Entry downKeys;
        DetailPanel() {
            // is accessible
            add(accName = new Entry("acc. name"));
            add(accDesc = new Entry("acc. desc"));
            add(toolTip = new Entry("tooltip"));
            add(text = new Entry("text"));
            // labelled by
            add(labelFor = new Entry("label for"));
            add(mnemonic = new Entry("mnemonic"));
            add(fwdKeys = new Entry("> keys"));
            add(bwdKeys = new Entry("< keys"));
            add(upKeys = new Entry("^ keys"));
            add(downKeys = new Entry("v keys"));
        }

        @Override
        void setComponent(Component c) {
            // summary info (type, name)
            super.setComponent(c);

            // accessible info (name, description)
            AccessibleContext ac = c == null ? null : c.getAccessibleContext();
            if (ac == null) {
                accName.setText(null, "not accessible");
                accDesc.setText(null, "not accessible");
            } else {
                boolean sb = c instanceof JScrollBar;
                String an = ac.getAccessibleName();
                accName.setText(an != null ? an : "no accessible name", an != null || sb);
                String ad = ac.getAccessibleDescription();
                accDesc.setText(ad != null ? ad : "no accessible description", ad != null || sb);
            }

            if (c != null && c instanceof JComponent) {
                String ttText = ((JComponent) c).getToolTipText();
                boolean ttEmpty = ttText == null || ttText.isEmpty();
                boolean toolTipOK = !ttEmpty
                        || c instanceof JTree
                        || c instanceof JEditorPane
                        || c instanceof JHelpContentViewer
                        || c instanceof JList
                        || c instanceof JRootPane
                        || c instanceof JScrollBar
                        || (c instanceof JTextComponent && !((JTextComponent) c).isEditable());
                toolTip.setText(ttEmpty ? "no tooltip" : ttText, toolTipOK);
            } else {
                toolTip.setEnabled(false);
            }

            // what the text content might be
            if (c instanceof JButton) {
                text.setText(((JButton) c).getText());
            } else if (c instanceof JLabel) {
                text.setText(((JLabel) c).getText());
            } else if (c instanceof JTextComponent) {
                JTextComponent tc = (JTextComponent) c;
                Document d = tc.getDocument();
                try {
                    text.setText(d.getText(0, Math.min(80, d.getLength())));
                } catch (Exception e) {
                    text.setText(null, e.toString());
                }
            } else {
                text.setEnabled(false);
            }

            // what it might be a label for
            if (c != null && c instanceof JLabel) {
                labelFor.setText(c.getClass().getName() + " " + c.getName());
            } else {
                labelFor.setEnabled(false);
            }

            // what the mnemonic might be
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                int mne = l.getDisplayedMnemonic();
                boolean mnemonicOK = mne != 0
                        || l.getLabelFor() == null;
                mnemonic.setText(mne == 0 ? "no mnemonic" : String.valueOf((char) mne), mnemonicOK);
            } else if (c instanceof JButton) {
                JButton b = (JButton) c;
                int mne = b.getMnemonic();
                String cmd = b.getActionCommand();
                boolean mnemonicOK = mne != 0
                        || (cmd != null && cmd.equals(UIFactory.CANCEL));
                mnemonic.setText(mne == 0 ? "no mnemonic" : String.valueOf((char) mne), mnemonicOK);
            } else {
                mnemonic.setEnabled(false);
            }

            // what the traversal keys are
            fwdKeys.setText(getKeysString(c, KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
            bwdKeys.setText(getKeysString(c, KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
            upKeys.setText(getKeysString(c, KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS));
            downKeys.setText(getKeysString(c, KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS));
        }

    }
}
