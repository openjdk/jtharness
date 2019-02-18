/*
 * $Id$
 *
 * Copyright (c) 2001, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview.wizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.border.LineBorder;

import com.sun.interview.ExtensionFileFilter;
import com.sun.interview.FileFilter;
import com.sun.interview.PropertiesQuestion;
import com.sun.interview.PropertiesQuestion.BooleanConstraints;
import com.sun.interview.PropertiesQuestion.FilenameConstraints;
import com.sun.interview.PropertiesQuestion.FloatConstraints;
import com.sun.interview.PropertiesQuestion.IntConstraints;
import com.sun.interview.PropertiesQuestion.StringConstraints;
import com.sun.interview.PropertiesQuestion.ValueConstraints;
import com.sun.javatest.tool.UIFactory;

/**
 * Utilities for rendering questions.
 */
public class RenderingUtilities {
    public static class PCE implements TableCellEditor {
        private DefaultCellEditor cbCE;
        private DefaultCellEditor tfCE;
        private DefaultCellEditor delegate;
        private PropertiesQuestion q;

        public PCE(PropertiesQuestion q) {
            cbCE =  new PropCellEditor(new JComboBox<>(), q);
            tfCE =  new RestrainedCellEditor(new JTextField(), q);
            this.q = q;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            assignDelegate(table, row, column);
            return delegate.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        public static boolean gotChoice(ValueConstraints vc) {
            if (vc == null) {
                return false;
            }

            if (vc.isUnsetAllowed()) {
               return true;
            }

            if (vc instanceof IntConstraints) {
                IntConstraints ic = (IntConstraints) vc;
                if (ic.getSuggestions() == null ||
                    ic.getSuggestions().length == 0 ||
                    ic.getUpperBound() == ic.getLowerBound() ||
                    ic.getSuggestions().length == 1 && ! ic.isCustomValuesAllowed()) {
                    return false;
                }
            }

            if (vc instanceof FloatConstraints) {
                FloatConstraints fc = (FloatConstraints) vc;
                if (fc.getSuggestions() == null ||
                    fc.getSuggestions().length == 0 ||
                    fc.getUpperBound() == fc.getLowerBound() ||
                    fc.getSuggestions().length == 1 && ! fc.isCustomValuesAllowed()) {
                    return false;
                }
            }

            if (vc instanceof StringConstraints) {
                StringConstraints sc = (StringConstraints) vc;
                if (sc.getSuggestions() == null ||
                    sc.getSuggestions().length == 0 ||
                    sc.getSuggestions().length == 1 && ! sc.isCustomValuesAllowed()) {
                    return false;
                }
            }

            return true;
        }

        private void assignDelegate(JTable table, int row, int column) {
            int columns = table.getColumnCount();
            Object [] values = new Object[columns];
            for (int i = 0; i < columns; i++) {
                values[i] = table.getValueAt(row, i);
            }
            String key = q.getConstraintKeyFromRow(values);
            delegate = gotChoice(q.getConstraints(key))?
                    cbCE :
                    tfCE;
        }

        @Override
        public Object getCellEditorValue() {
            return delegate.getCellEditorValue();
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return delegate == null || delegate.isCellEditable(anEvent);
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return delegate.shouldSelectCell(anEvent);
        }

        @Override
        public boolean stopCellEditing() {
            return delegate.stopCellEditing();
        }

        @Override
        public void cancelCellEditing() {
            delegate.cancelCellEditing();
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
            delegate.addCellEditorListener(l);
        }

        @Override
        public void removeCellEditorListener(CellEditorListener l) {
            delegate.removeCellEditorListener(l);
        }
    }

    static class RestrainedCellEditor extends DefaultCellEditor {
        protected RestrainedCellEditor(JTextField tf, PropertiesQuestion q) {
            super(tf);
            this.q = q;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {

            int columns = table.getColumnCount();
            Object [] values = new Object[columns];
            for (int i = 0; i < columns; i++) {
                values[i] = table.getValueAt(row, i);
            }
            String key = q.getConstraintKeyFromRow(values);

            ValueConstraints vc = q.getConstraints(key);

            JTextField tf = (JTextField)getComponent();
            tf.setText(value.toString());
            tf.setFocusable(false);

            if (vc instanceof IntConstraints) {
                IntConstraints ic = (IntConstraints)vc;
                tf.setEditable(ic.isCustomValuesAllowed());
            }

            if (vc instanceof StringConstraints){
                StringConstraints sc = (StringConstraints)vc;
                tf.setEditable(sc.isCustomValuesAllowed());
            }

            if (vc instanceof FloatConstraints) {
                FloatConstraints fc = (FloatConstraints)vc;
                tf.setEditable(fc.isCustomValuesAllowed());
            }

            return tf;
        }

        private PropertiesQuestion q;
    }



    /**
     * Table cell renderer for enforcing constraints on the combo box used
     * for editing.
     */
    public static class PropCellEditor extends DefaultCellEditor {
        protected PropCellEditor(JComboBox<Object> box) {
            super(box);
        }

        PropCellEditor(JComboBox<Object> box, PropertiesQuestion q) {
            this(box);
            question = q;
        }

        /**
         * For use when this renderer is being used outside the context of
         * an interview and question.
         */
        PropCellEditor(JComboBox<Object> box, ValueConstraints rules) {
            this(box);
            this.rules = rules;
        }

        @Override
        public Object getCellEditorValue() {
            if (rules != null){
                if (rules instanceof BooleanConstraints){
                    if (((BooleanConstraints)rules).isYesNo()) {
                        return yesNoBox.getValue();
                    }
                    else {
                        return jCheckBox.isSelected() ? BooleanConstraints.TRUE : BooleanConstraints.FALSE;
                    }
                }
            }
            return super.getCellEditorValue();
        }

    @Override
        public Component getTableCellEditorComponent(final JTable table, Object value,
                 boolean isSelected, final int row, final int column) {

            int columns = table.getColumnCount();
            Object [] values = new Object[columns];
            for (int i = 0; i < columns; i++) {
                values[i] = table.getValueAt(row, i);
            }
            String key = question.getConstraintKeyFromRow(values);

            rules = question.getConstraints(key);

            if (rules instanceof BooleanConstraints){

                if (((BooleanConstraints) rules).isYesNo()) {
                    if (yesNoBox == null){
                        yesNoBox = new YesNoBox();
                    }
                    yesNoBox.selectYes(BooleanConstraints.YES.equals(value));
                    return yesNoBox;
                } else {
                    if (jCheckBox == null) {
                        jCheckBox = new JCheckBox();
                    }
                    jCheckBox.setSelected(BooleanConstraints.TRUE.equals(value));
                    return jCheckBox;
                }

            }

            final JComboBox<Object> cb = (JComboBox<Object>)getComponent();
            cb.setEditable(true);
            cb.removeAllItems();
            cb.addItem(value);
            cb.setSelectedIndex(0);
            if (rules != null)
                setConstraints(cb, rules);

            String valid = question.isValueValid(key);
            if (valid != null) {
                cb.setToolTipText(valid);
            }
            else
                cb.setBackground(Color.WHITE);

            if (!(rules instanceof FilenameConstraints)) {
                return cb;
            }
            else {      // file chooser
                final FilenameConstraints fc = (FilenameConstraints)rules;
                JPanel p = new JPanel();
                p.setName("filename field");
                p.setFocusable(false);
                p.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.LINE_START;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                gbc.gridy = 0;
                p.add(cb, gbc);

                gbc.fill = GridBagConstraints.NONE;

                // configure the button
                final JButton browseBtn = new JButton("...");
                final JFileChooser chooser = FileQuestionRenderer.createChooser(
                                                key, fc.getFilters());
                // setup chooser
                File f = new File((String) cb.getSelectedItem());
                if (!f.exists()) {
                    File dir = fc.getBaseDirectory();
                    if (dir == null)
                        dir = new File(System.getProperty("user.dir"));
                    chooser.setCurrentDirectory(dir);
                }
                else {
                    chooser.setSelectedFile(f);
                }

                browseBtn.setName("file.browse.btn");
                browseBtn.setMnemonic(i18n.getString("file.browse.mne").charAt(0));
                browseBtn.setToolTipText(i18n.getString("file.browse.tip"));
                browseBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // default chooser to point at specified entry
                        String s = (String) cb.getSelectedItem();
                        if (s != null && !s.isEmpty()) {
                            File f = new File(s);
                            File baseDir = fc.getBaseDirectory();
                            if (!f.isAbsolute() && baseDir != null)
                                f = new File(baseDir, s);
                            chooser.setSelectedFile(f);
                        }

                        int opt = chooser.showDialog(browseBtn, "Select");
                        if (opt == JFileChooser.APPROVE_OPTION) {

                            String path = chooser.getSelectedFile().getPath();
                            FileFilter ff = SwingFileFilter.unwrap(chooser.getFileFilter());

                            if (ff != null && ff instanceof ExtensionFileFilter) {
                                ExtensionFileFilter eff = (ExtensionFileFilter) ff;
                                path = eff.ensureExtension(path);
                            }
                            File baseDir = fc.getBaseDirectory();
                            if (baseDir != null) {
                                String bp = baseDir.getPath();
                                if (path.startsWith(bp + File.separatorChar))
                                    path = path.substring(bp.length() + 1);
                            }
                            if (cb.getSelectedIndex() != -1) {
                                cb.removeItemAt(cb.getSelectedIndex());
                            }
                            cb.addItem(path);
                            cb.setSelectedItem(path);
                            table.getModel().setValueAt(path, row, column);
                        }
                    }
                });
                p.add(browseBtn, gbc);
                return p;
            }
        }

        private void setConstraints(JComboBox<Object> cb, ValueConstraints rules) {
            if (rules instanceof IntConstraints) {
                // attach input filter
                // add suggestions
                IntConstraints intRules = (IntConstraints)rules;
                cb.setEditable(intRules.isCustomValuesAllowed());

                int[] sugs = intRules.getSuggestions();
                if (sugs != null)
                    // add all suggestions
                    for (int sug : sugs) {
                        if (!Integer.toString(sug).equals(cb.getItemAt(0)))
                            cb.addItem(Integer.toString(sug));
                    }
            }
            else if (rules instanceof FloatConstraints) {
                // attach input filter
                // add suggestions
                FloatConstraints fltRules = (FloatConstraints)rules;
                float[] sugs = fltRules.getSuggestions();
                cb.setEditable(fltRules.isCustomValuesAllowed());

                if (sugs != null)
                    // add all suggestions
                    for (float sug : sugs) {
                        if (!Float.toString(sug).equals(cb.getItemAt(0)))
                            cb.addItem(Float.toString(sug));
                    }
            }
            else if (rules instanceof StringConstraints) {
                StringConstraints strRules = (StringConstraints)rules;
                cb.setEditable(strRules.isCustomValuesAllowed());

                String[] sugs = strRules.getSuggestions();
                if (sugs != null) {
                    if (strRules.isCustomValuesAllowed()) {
                        // add all suggestions
                        for (String sug : sugs) {
                            if (!sug.equals(cb.getItemAt(0)))
                                cb.addItem(sug);
                        }   // for
                    }
                    else
                        configureSet(cb, sugs, true, strRules.isUnsetAllowed());
                }
                else {}
            }
            else if (rules instanceof FilenameConstraints) {
                FilenameConstraints strRules = (FilenameConstraints)rules;
                cb.setEditable(true);

                File[] sugs = strRules.getSuggestions();
                if (sugs != null) {
                    // add all suggestions
                    for (File sug : sugs) {
                        if (!sug.getPath().equalsIgnoreCase((String) cb.getItemAt(0)))
                            cb.addItem(sug.getPath());
                    }   // for
                }
                else {}
            }
            else {      // generic constraints
                ValueConstraints vRules = rules;
            }
        }

        /**
         * Add set of choices to combo box.  Handles special cases, such as
         * when the current value matches one of the possible choices.  Also
         * adds a blank choice (unset).  Assume the current value in the
         * combo box is the one at index zero.
         */
        private void configureSet(JComboBox<Object> cb, String[] possible,
                                boolean ignoreCase, boolean isUnsetAllowed) {
            // wishlist: i18n
            //           values which are independent of locale
            String curr = (String) cb.getItemAt(0);

            // add unset choice if allowed and needed
            if (isUnsetAllowed)
                cb.addItem("");

            for (String aPossible : possible) cb.addItem(aPossible);

            for (int i = 0; i < possible.length; i++) {
                if (compareStr(curr, possible[i], ignoreCase)) {
                    cb.removeItemAt(0);
                    cb.setSelectedIndex(i + (isUnsetAllowed ? 1 : 0));
                    return;
                }
            }   // for

            // no matches, delete current value, set empty
            // should select a better default?  try from defaultValue?
            cb.removeItemAt(0);
            cb.setSelectedIndex(0);
        }

        private boolean compareStr(String s1, String s2, boolean ignoreCase) {
            if (ignoreCase)
                return s1.equalsIgnoreCase(s2);
            else
                return s1.equals(s2);
        }

        private PropertiesQuestion question;
        private ValueConstraints rules;
        private JCheckBox jCheckBox;
        private YesNoBox yesNoBox;
    }   // editor cell


    /**
     * Table cell renderer for use when a cell is not being edited.
     */
    public static class PropCellRenderer extends DefaultTableCellRenderer {
        PropCellRenderer(PropertiesQuestion q) {
            this.q = q;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            if (column == 1){
                String keyName = q.getKeyPropertyName((String)table.getValueAt(row, 0));
                PropertiesQuestion.ValueConstraints constraints = q.getConstraints(keyName);
                if (constraints instanceof BooleanConstraints){
                    if (((BooleanConstraints) constraints).isYesNo()) {
                        yesNoBox = new YesNoBox();
                        yesNoBox.selectYes(BooleanConstraints.YES.equals(value));
                        return yesNoBox;

                    }
                    else{
                        jCheckBox = new JCheckBox();
                        jCheckBox.setSelected(BooleanConstraints.TRUE.equals(value));
                        return jCheckBox;
                    }
                }
                else{
                    jComboBox = new JComboBox<>();
                    jComboBox.addItem(value);
                    jComboBox.setEditable(true);
                    if ( q.isValueValid(q.getKeyPropertyName((String)table.getValueAt(row, 0))) != null ) {
                        jComboBox.setBorder(new LineBorder(Color.RED, 2));
                    }
                    return jComboBox;
                }
            }
            else {
                Component c = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
                c.setBackground(UIFactory.Colors.WINDOW_BACKGROUND.getValue());
                ((DefaultTableCellRenderer)c).setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
                return c;
            }
            // XXX needs i18n and 508
        }

        public PropertiesQuestion getQuestion() {
            return q;
        }

        PropertiesQuestion q;
        JCheckBox jCheckBox;
        YesNoBox yesNoBox;
        JComboBox<Object> jComboBox;
    }   // non-editing cell

    static class YesNoBox extends JPanel{

        private JRadioButton yesButton;
        private JRadioButton noButton;
        private ButtonGroup bgroup;

        public YesNoBox(){
            super();
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            bgroup = new ButtonGroup();
            yesButton = new JRadioButton(BooleanConstraints.YES);
            noButton = new JRadioButton(BooleanConstraints.NO);
            bgroup.add(yesButton);
            bgroup.add(noButton);

            add(yesButton);
            add(Box.createRigidArea(new Dimension(5,0)));
            add(noButton);
        }

        public String getValue(){
            if (yesButton.isSelected()){
                return BooleanConstraints.YES;
            }
            return BooleanConstraints.NO;
        }

        public void selectYes(boolean value){
            yesButton.setSelected(value);
            noButton.setSelected(!value);
        }

    }

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
}
