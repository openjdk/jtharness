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
package com.sun.interview.wizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import com.sun.interview.ExtensionFileFilter;
import com.sun.interview.FileFilter;
import com.sun.interview.PropertiesQuestion;
import com.sun.interview.PropertiesQuestion.BooleanConstraints;
import com.sun.interview.PropertiesQuestion.FilenameConstraints;
import com.sun.interview.PropertiesQuestion.FloatConstraints;
import com.sun.interview.PropertiesQuestion.IntConstraints;
import com.sun.interview.PropertiesQuestion.StringConstraints;
import com.sun.interview.PropertiesQuestion.ValueConstraints;

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
            cbCE =  new PropCellEditor(new JComboBox(), q);
            tfCE =  new RestrainedCellEditor(new JTextField(), q);
            this.q = q;
        }


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

        public Object getCellEditorValue() {
            return delegate.getCellEditorValue();
        }

        public boolean isCellEditable(EventObject anEvent) {
            return delegate == null || delegate.isCellEditable(anEvent);
        }

        public boolean shouldSelectCell(EventObject anEvent) {
            return delegate.shouldSelectCell(anEvent);
        }

        public boolean stopCellEditing() {
            return delegate.stopCellEditing();
        }

        public void cancelCellEditing() {
            delegate.cancelCellEditing();
        }

        public void addCellEditorListener(CellEditorListener l) {
            delegate.addCellEditorListener(l);
        }

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
        protected PropCellEditor(JComboBox box) {
            super(box);
        }

        PropCellEditor(JComboBox box, PropertiesQuestion q) {
            this(box);
            question = q;
        }

        /**
         * For use when this renderer is being used outside the context of
         * an interview and question.
         */
        PropCellEditor(JComboBox box, ValueConstraints rules) {
            this(box);
            this.rules = rules;
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

            final JComboBox cb = ((JComboBox)getComponent());
            cb.setEditable(true);
            cb.removeAllItems();
            cb.addItem(value);
            cb.setSelectedIndex(0);
            if (rules != null)
                setConstraints(cb, rules);

            String valid = question.isValueValid(key);
            if (valid != null) {
                cb.setBackground(Color.RED);
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
                File f = new File((String)(cb.getSelectedItem()));
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
                    public void actionPerformed(ActionEvent e) {
                        // default chooser to point at specified entry
                        String s = (String)(cb.getSelectedItem());
                        if (s != null && s.length() > 0) {
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

        private void setConstraints(JComboBox cb, ValueConstraints rules) {
            if (rules instanceof IntConstraints) {
                // attach input filter
                // add suggestions
                IntConstraints intRules = (IntConstraints)rules;
                cb.setEditable(intRules.isCustomValuesAllowed());

                int[] sugs = intRules.getSuggestions();
                if (sugs != null)
                    // add all suggestions
                    for (int i = 0; i < sugs.length; i++) {
                        if (!Integer.toString(sugs[i]).equals(cb.getItemAt(0)))
                            cb.addItem(Integer.toString(sugs[i]));
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
                    for (int i = 0; i < sugs.length; i++) {
                        if (!Float.toString(sugs[i]).equals(cb.getItemAt(0)))
                            cb.addItem(Float.toString(sugs[i]));
                    }
            }
            else if (rules instanceof StringConstraints) {
                StringConstraints strRules = (StringConstraints)rules;
                cb.setEditable(strRules.isCustomValuesAllowed());

                String[] sugs = strRules.getSuggestions();
                if (sugs != null) {
                    if (strRules.isCustomValuesAllowed()) {
                        // add all suggestions
                        for (int i = 0; i < sugs.length; i++) {
                            if (!sugs[i].equals(cb.getItemAt(0)))
                                cb.addItem(sugs[i]);
                        }   // for
                    }
                    else
                        configureSet(cb, sugs, true, strRules.isUnsetAllowed());
                }
                else {}
            }
            else if (rules instanceof BooleanConstraints) {
                BooleanConstraints bolRules = (BooleanConstraints)rules;
                cb.setEditable(false);
                if (bolRules.isYesNo()) {
                    configureSet(cb, new String[] {"Yes", "No"}, true,
                                 bolRules.isUnsetAllowed());
                }
                else {
                    configureSet(cb, new String[] {"True", "False"}, true,
                                 bolRules.isUnsetAllowed());
                }
            }
            else if (rules instanceof FilenameConstraints) {
                FilenameConstraints strRules = (FilenameConstraints)rules;
                cb.setEditable(true);

                File[] sugs = strRules.getSuggestions();
                if (sugs != null) {
                    // add all suggestions
                    for (int i = 0; i < sugs.length; i++) {
                        if (!sugs[i].getPath().equalsIgnoreCase((String)(cb.getItemAt(0))))
                            cb.addItem(sugs[i].getPath());
                    }   // for
                }
                else {}
            }
            else {      // generic constraints
                ValueConstraints vRules = (ValueConstraints)rules;
            }
        }

        /**
         * Add set of choices to combo box.  Handles special cases, such as
         * when the current value matches one of the possible choices.  Also
         * adds a blank choice (unset).  Assume the current value in the
         * combo box is the one at index zero.
         */
        private void configureSet(JComboBox cb, String[] possible,
                                boolean ignoreCase, boolean isUnsetAllowed) {
            // wishlist: i18n
            //           values which are independent of locale
            String curr = (String)(cb.getItemAt(0));

            // add unset choice if allowed and needed
            if (isUnsetAllowed)
                cb.addItem("");

            for (int i = 0; i < possible.length; i++)
                cb.addItem(possible[i]);

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
    }   // editor cell


    /**
     * Table cell renderer for use when a cell is not being edited.
     */
    public static class PropCellRenderer extends DefaultTableCellRenderer {
        PropCellRenderer(PropertiesQuestion q) {
            this.q = q;
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                  boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected,
                                hasFocus, row, column);

            // XXX needs i18n and 508
            if ( q.isValueValid((String)(table.getValueAt(row, 0))) != null )
                c.setBackground(Color.RED);
            else
                c.setBackground(Color.WHITE);

            return c;
        }

        PropertiesQuestion q;
    }   // non-editing cell

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
}
