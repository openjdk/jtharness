/*
 * $Id$
 *
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.MissingResourceException;

import javax.help.HelpBroker;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
//import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
//import javax.swing.SpinnerModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;

import com.sun.javatest.ProductInfo;
import com.sun.javatest.util.I18NResourceBundle;
import java.awt.Dialog;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * A factory for GUI components, providing support for
 * internationalization, tool tips, context sensitive help, and on.
 * UIFactory objects use a resource bundle specific to the client
 * class to provide the internationalization support.
 */
public class UIFactory {

    public static enum Colors {
        /**
         * Color used for highlighting incorrect input fields
         */
        INPUT_INVALID(local_i18n.getString("colorprefs.colors.input.invalid.defvalue")),
        /**
         * Color used for highlighting correct input fields
         */
        INPUT_VALID(local_i18n.getString("colorprefs.colors.input.valid.defvalue")),
        /**
         * Default background color for input fields
         */
        INPUT_DEFAULT(local_i18n.getString("colorprefs.colors.input.default.defvalue")),

        MENU_BACKGROUND(UIManager.getColor("Menu.background"), 255, false),
        SEPARATOR_FOREGROUND(UIManager.getColor("Separator.foreground"), 255, false),
        CONTROL_INFO(Color.RED, 255, false),
        CONTROL_SHADOW(Color.RED, 255, false),
        TEXT_HIGHLIGHT_COLOR(new JTextField().getSelectionColor(), 255, false),
        TEXT_COLOR(new JLabel().getForeground(), 255, false),
        TEXT_SELECTED_COLOR(new JTextField().getSelectedTextColor(), 255, false),
        WINDOW_BACKGROUND(UIManager.getColor("Panel.background"), 255, false),
        PRIMARY_CONTROL_HIGHLIGHT(Color.WHITE, 255, false),
        PRIMARY_CONTROL_INFO(Color.BLACK, 255, false),
        BUTTON_DISABLED_FOREGROUND(Color.WHITE, 255, false),//UIManager.getDefaults().getColor("Button.disabledForeground")

        // these three are used for icon drawing only
        PRIMARY_CONTROL_SHADOW(MetalLookAndFeel.getPrimaryControlShadow(), 255, false),
        PRIMARY_CONTROL(MetalLookAndFeel.getPrimaryControl(), 255, false),
        PRIMARY_CONTROL_DARK_SHADOW(MetalLookAndFeel.getPrimaryControlDarkShadow(), 255, false),

        BLACK(Color.BLACK, 255, false),
        TRANSPARENT(new Color(255, 255, 255, 0), false);

        private final String defaultColor;
        private Color color = null;
        private boolean configurable;

        Colors(Color defaultColor) {
            this(defaultColor, true);
        }

        Colors(Color defaultColor, int alpha) {
            this(new Color(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue(), alpha), true);
        }

        Colors(Color defaultColor, int alpha, boolean configurable) {
            this(new Color(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue(), alpha), configurable);
        }

        Colors(Color defaultColor, boolean configurable) {
            this.defaultColor = encodeARGB(defaultColor);
            this.configurable = configurable;
        }

        Colors(String defaultColor) {
            this.defaultColor = defaultColor;
            this.configurable = true;
        }

        public boolean isConfigurable() {
            return configurable;
        }

        /**
         * Getter for default String-encoded color value. Is used for Color.decode() and
         * should be formatted similarly
         *
         * @return Default String-encoded color value
         */
        public String getDefaultValue() {
            return defaultColor;
        }

        /**
         * Getter for current color value. It is loaded from preferences if no color is set
         * previously.
         *
         * @return Current color value
         */
        public Color getValue() {
            if(color == null) {
                color = readColorFromPreferences();
            }
            return color;
        }

        /**
         * Setter for current color value.
         *
         * @return Old color value
         */
        public Color setValue(Color c) {
            Color t = color;
            if (configurable)
                color = c;
            return t;
        }

        /**
         * Get color name used in preferences file. It is formed from enum name.
         * E.g. colors.input.invalid for INPUT_INVALID
         *
         * @return Color name used in preferences file
         */
        public String getPreferencesName() {
            return "colors." + this.name().toLowerCase().replaceAll("_", ".");
        }

        /**
         * Read color value from preferences ignoring current color value that is
         * returned by getValue();
         *
         * @return Color value from preferences file
         */
        public Color readColorFromPreferences() {
            return decodeRGBA(Preferences.access().getPreference(this.getPreferencesName(), this.getDefaultValue()));
        }

        /**
         * Find Colors by color preferences name.
         *
         * @param prefsName Color preferences name (e.g. "colors.input.default")
         * @throws IllegalArgumentException in case there is no Colors with such
         * name
         * @return Colors associated with such preferences name
         */
        public static Colors valueOfByPreferencesName(String prefsName) {
            return Colors.valueOf(prefsName.replaceFirst("colors.", "").toUpperCase().replaceAll("\\.", "_"));
        }

        /**
         * Get Color by colors preferences name.
         *
         * @param prefsName Color preferences name (e.g. "colors.input.default")
         * @return Color if Preferences contain this color. Returns default value if exists<br>
         * null otherwise
         */
        public static Color getColorByPreferencesName(String prefsName) {
            Preferences prefs = Preferences.access();
            try {
                Colors c = valueOfByPreferencesName(prefsName); // IllegalArgumentException if such Colors doesn't exist

                return c.getValue();
            } catch(IllegalArgumentException e) {
                String color = prefs.getPreference(prefsName); // try to find color in preferences anyway
                return color == null ? null : decodeRGBA(color);
            }
        }

        /**
         * Get array with all colors names used in preferences
         *
         * @return Names array
         */
        public static String[] getColorsNames() {
            Colors[] values = Colors.values();
            String temp[] = new String[values.length];
            for(int i = 0; i < values.length; i++) {
                temp[i] = values[i].getPreferencesName();
            }
            return temp;
        }

        public static Color decodeRGBA(String color) throws NumberFormatException {
            try {
                if (color.startsWith("0x") && color.length() == 10) {
                    Long colorCode = Long.decode(color);
                    int A = (int) (colorCode & 0xFF);
                    colorCode >>= 8;
                    int B = (int) (colorCode & 0xFF);
                    colorCode >>= 8;
                    int G = (int) (colorCode & 0xFF);
                    colorCode >>= 8;
                    int R = (int) (colorCode & 0xFF);
                    colorCode >>= 8;
                    return new Color(R, G, B, A);
                } else {
                    return Color.decode(color);
                }
            } catch(Exception e) {
                return Color.red;
            }
        }

        public static String encodeARGB(Color color) {
            if(color == null)
                return "";
            return String.format("0x%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static Font getBaseFont() {
        return baseFont;
    }

    /**
     * Get invalid input color (red by default)
     * @return Color of invalid input
     */
    public static Color getInvalidInputColor() {
        return Colors.INPUT_INVALID.getValue();
    }

    /**
     * Set invalid input color
     * @param newColor new invalid input color
     */
    public static void setInvalidInputColor(Color newColor) {
        Colors.INPUT_INVALID.setValue(newColor);
    }

    /**
     * Get valid input color (green by default)
     * @return Color of valid input
     */
    public static Color getValidInputColor() {
        return Colors.INPUT_VALID.getValue();
    }

    /**
     * Set valid input color
     * @param newColor new valid input color
     */
    public static void setValidInputColor(Color newColor) {
        Colors.INPUT_VALID.setValue(newColor);
    }

    /**
     * Get default input color (while by default)
     * @return Color of default input
     */
    public static Color getDefaultInputColor() {
        return Colors.INPUT_DEFAULT.getValue();
    }

    /**
     * Set default input color
     * @param newColor new default input color
     */
    public static void setDefaultInputColor(Color newColor) {
        Colors.INPUT_DEFAULT.setValue(newColor);
    }

    /**
     * Set Color by preferences name
     * @param name Color's preferences name
     * @param c new Color to set
     */
    public static void setColorByName(String name, Color c) {
        Colors.valueOfByPreferencesName(name).setValue(c);
    }

    /**
     * Set all colors to default values
     */
    public static void setDefaultColors() {
        Preferences pref = Preferences.access();
        Colors colors[] = Colors.values();
        for(Colors c: colors) {
            pref.setPreference(c.getPreferencesName(), c.getDefaultValue());
        }
    }

    /**
     * Add Preferences observer to color changes
     * @param observer
     */
    public static void addColorChangeObserver(Preferences.Observer observer) {
        Preferences.access().addObserver(Colors.getColorsNames(), observer);
    }

    /**
     * Creates a color-choosing button with background color set by preferences color name
     * @param cs preferences color name. Used to set background color and is set as JButton.name
     * @param label JLabel for button
     * @param l ActionListener for button
     * @return color-choosing button
     */
    public JButton createColorChooseButton(String cs, JLabel label, ActionListener l) {
        JButton b = new JButton(" ");
        Color c = Colors.getColorByPreferencesName(cs);
        b.setBackground(c);
        b.setSize(14, 14);
        b.setText(" ");
        b.setName(cs);
        if(l != null)
            b.addActionListener(l);
        if(label != null)
            label.setLabelFor(b);
        return b;
    }

    /**
     * Create a UIFactory object for a specific class.
     * The class is used to determine the resource bundle
     * for i18n strings; the bundle is named i18n.properties
     * in the same package as the specified class.
     * @param c the class used to determine the i18n properties
     * @param helpBroker the help broker to be used when creating help buttons
     */
    public UIFactory(Class c, HelpBroker helpBroker) {
        this(c, null, helpBroker);
    }

    /**
     * Create a UIFactory object for a specific component.
     * The component's class is used to determine the resource bundle
     * for i18n strings; the bundle is named i18n.properties
     * in the same package as the specified class.
     * @param c the component used to determine the i18n properties
     * @param helpBroker the help broker to be used when creating help buttons
     */
    public UIFactory(Component c, HelpBroker helpBroker) {
        this(c.getClass(), c, helpBroker);
    }

    /**
     * Create a UIFactory object for a specific class.
     * The class is used to determine the resource bundle
     * for i18n strings; the bundle is named i18n.properties
     * in the same package as the specified class.
     * @param c the class used to determine the i18n properties
     * @param p the parent component to be used for any dialogs that are created
     * @param helpBroker the help broker to be used when creating help buttons
     */
    public UIFactory(Class c, Component p, HelpBroker helpBroker) {
        this.helpBroker = helpBroker;
        clientClass = c;
        parent = p;
        i18n = I18NResourceBundle.getBundleForClass(c);
    }

    /**
     * Set the parent component to be used for dialogs created by this factory.
     * This setting cannot be changed after it is set.
     *
     * @param p The parent component, should not be null.
     */
    public void setDialogParent(Component p) {
        if (parent != null && parent != p)
            throw new IllegalStateException();
        parent = p;
    }

    /**
     * Get the screen resolution, in dots per inch, as provided
     * by the default AWT toolkit.
     * @return the screen resolution, in dots per inch
     */
    public int getDotsPerInch() {
        return DOTS_PER_INCH;
    }

    /**
     * Get the help broker associated with this factory.
     * @return the help broker associated with this factory
     */
    public HelpBroker getHelpBroker() {
        return helpBroker;
    }

    /**
     * Get the resource bundle used to obtain the resources for the
     * components create by this factory.
     * @return the resource bundle used to obtain the resources for the
     * components create by this factory
     */
    public I18NResourceBundle getI18NResourceBundle() {
        return i18n;
    }

    /**
     * Get a keycode from the resource bundle.
     * @param key the name of the resource to be returned
     * @return the first character of the string that was found
     */
    public int getI18NMnemonic(String key) {
        String keyString = getI18NString(key);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyString);
        if (keyStroke != null)
            return keyStroke.getKeyCode();
        else
            //System.err.println("WARNING: bad mnemonic keystroke for " + key + ": " + keyString);
            return 0;
    }

    /**
     * Get a color from the resource bundle.
     * @param key the base name of the resource to be returned
     * @return the color identified in the resource
     */
    public Color getI18NColor(String key) {
        String value = i18n.getString(key + ".clr");
        try {
            if (value != null)
                return Color.decode(value);
        }
        catch (Exception e) {
            // ignore
        }
        return Color.BLACK;
    }

    /**
     * Get a string from the resource bundle.
     * @param key the name of the resource to be returned
     * @return the string that was found
     */
    public String getI18NString(String key) {
        return i18n.getString(key);
    }

    /**
     * Get a string from the resource bundle.
     * @param key the name of the resource to be returned
     * @param arg an argument to be formatted into the result using
     * {@link java.text.MessageFormat#format}
     * @return the formatted string
     */
    public String getI18NString(String key, Object arg) {
        return i18n.getString(key, arg);
    }

    /**
     * Get a string from the resource bundle.
     * @param key the name of the resource to be returned
     * @param args an array of arguments to be formatted into the result using
     * {@link java.text.MessageFormat#format}
     * @return the formatted string
     */
    public String getI18NString(String key, Object[] args) {
        return i18n.getString(key, args);
    }

    /**
     * Set the help ID for the context-sensitive help for a component.
     * @param comp the component for which to set the help ID
     * @param helpID the help ID identifying the context sensitive help for
     * the component
     */
    public void setHelp(Component comp, String helpID) {
        if (helpID == null)
            throw new NullPointerException();

        if (helpBroker != null) {
            if (comp instanceof JDialog) {
                JDialog d = (JDialog) comp;
                Desktop.addHelpDebugListener(d);
                helpBroker.enableHelpKey(d.getRootPane(), helpID, null);
            }
            else
                helpBroker.enableHelp(comp, helpID, null);
        }
    }

    /**
     * Set a tool tip for a component from a resource in the factory's resource
     * bundle.  <br>
     * By convention, tool tip resources end in ".tip".  Most
     * components created by this factory will already have a tool tip set, so
     * this method need not be called for them. <br>
     * Also, the component's accessible description text will automatically
     * be set to the supplied tooltip text.<br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the component
     * </table>
     * @param c the component for which to set the tool tip
     * @param uiKey the base name of the resource to be used
     */
    public void setToolTip(JComponent c, String uiKey) {
        String text = getI18NString(uiKey + ".tip");
        c.setToolTipText(text);

        // workaround - tooltip doesn't get copied from this component
        // see JComponent.AccessibleJComponent.getToolTipText()
        c.getAccessibleContext().setAccessibleDescription(text);
    }

    /**
     * Sets only the accessible description for the given context, using the
     * given key.
     * <table>
     * <tr><td><i>uiKey</i>.desc  <td>accessible description
     * </table>
     * @param c the component to modify
     * @param uiKey the base name of the resource to be used
     * @see #setAccessibleDescription(AccessibleContext,String)
     */
    public void setAccessibleDescription(Component c, String uiKey) {
        setAccessibleDescription(c.getAccessibleContext(), uiKey);
    }

    /**
     * Sets only the accessible description for the given context, using the
     * given key.
     * <table>
     * <tr><td><i>uiKey</i>.desc  <td>accessible description
     * </table>
     * @param c the context object to modify
     * @param uiKey the base name of the resource to be used
     */
    public void setAccessibleDescription(AccessibleContext c, String uiKey) {
        String text = getI18NString(uiKey + ".desc");
        c.setAccessibleDescription(text);
    }

    /**
     * Sets only the accessible name for the given context, using the
     * given key.
     * @param c the component object to modify
     * @param uiKey the base name of the resource to be used
     * @see #setAccessibleName(AccessibleContext,String)
     */
    public void setAccessibleName(Component c, String uiKey) {
        setAccessibleName(c.getAccessibleContext(), uiKey);
    }

    /**
     * Sets only the accessible name for the given context, using the
     * given key.
     * <table>
     * <tr><td><i>uiKey</i>.name  <td>accessible name
     * </table>
     * @param c the context object to modify
     * @param uiKey the base name of the resource to be used
     */
    public void setAccessibleName(AccessibleContext c, String uiKey) {
        String text = getI18NString(uiKey + ".name");
        c.setAccessibleName(text);
    }

    /**
     * Sets the accessible name and description for the given
     * component.
     * @param c the component object to modify
     * @param uiKey the base name of the resource to be used
     * @see #setAccessibleInfo(AccessibleContext,String)
     */
    public void setAccessibleInfo(Component c, String uiKey) {
        setAccessibleInfo(c.getAccessibleContext(), uiKey);
    }

    /**
     * Sets the accessibility name and description for the given context
     * using the given key as the base.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.name  <td>accessible name
     * <tr><td><i>uiKey</i>.desc  <td>accessible description text
     * </table>
     * @param c the context object to modify
     * @param uiKey the base name of the resource to be used
     */
    public void setAccessibleInfo(AccessibleContext c, String uiKey) {
        setAccessibleDescription(c, uiKey);
        setAccessibleName(c, uiKey);
    }


    //----------------------------------------------------------------------------
    //
    // borders

    /**
     * Create a titled border, using a resource to specify the title. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.bdr  <td>the text for the title
     * </table>
     * @param uiKey the base name of the resource to be used
     * @return the border that was created
     */
    public Border createTitledBorder(String uiKey) {
        return BorderFactory.createTitledBorder(null, getI18NString(uiKey + ".bdr"), TitledBorder.LEADING, TitledBorder.DEFAULT_JUSTIFICATION, getBaseFont(), Colors.TEXT_COLOR.getValue());
    }

    //----------------------------------------------------------------------------
    //
    // white space

    /**
     * Create a horizontal filler that expands to fill the available space.
     * The name of the glue component will be set to <i>uikey</i>.  No resource
     * strings are required at this time.
     * @param uiKey the base name of the resource to be used
     * @return a filler component that expands to fill the available space
     */
    public Component createHorizontalGlue(String uiKey) {
        Component c = Box.createHorizontalGlue();
        c.setName(uiKey);
        c.setFocusable(false);
        return c;
    }

    /**
     * Create a filler that expands to fill the available space.
     * @param uiKey the base name of the resource to be used
     * @return a filler component that expands to fill the available space
     */
    public Component createGlue(String uiKey) {
        Component c = Box.createGlue();
        c.setName(uiKey);
        c.setFocusable(false);
        return c;
    }


    /**
     * Create a horizontal filler of a given width.
     * @param width the desired width of the filler component
     * @return a filler component of a given width
     */
    public Component createHorizontalStrut(int width) {
        Component c = Box.createHorizontalStrut(width);
        c.setFocusable(false);
        return c;
    }


    //----------------------------------------------------------------------------
    //
    // buttons

    /**
     * Create a button, using resources to specify the name and the tool tip. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.btn  <td>the name for the button
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * </table>
     * In addition, the name of the button and the action command
     * for the button is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @return the button that was created
     * @see #createHelpButton
     * @see #createIconButton
     */
    public JButton createButton(String uiKey) {
        JButton b = new JButton(getI18NString(uiKey + ".btn"));
        b.setActionCommand(uiKey);
        b.setName(uiKey);
        setMnemonic(b, uiKey);
        setToolTip(b, uiKey);
        return b;
    }

    /**
     * Create a button based on the information in an Action.
     * @param a the Action for which to define the button
     * @return the button that was created
     */
    public JButton createButton(Action a) {
        JButton b = new JButton(a);
        b.setName((String) (a.getValue(Action.NAME)));
        return b;
    }

    /**
     * Create a button containing an Icon.
     * @param uiKey the base name of the resource to be used
     * @param icon the icon to appear in the button
     * @return the button that was created
     */
    public JButton createButton(String uiKey, Icon icon) {
        JButton b = new JButton(icon);
        b.setName(uiKey);
        setMnemonic(b, uiKey);
        setToolTip(b, uiKey);
        return b;
    }

    /**
     * Create a button, using resources to specify the name and the tool tip,
     * and with a specified ActionListener. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.btn  <td>the name for the button
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * </table>
     * In addition, the name of the button and the action command
     * for the button is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @param l the ActionListener to be add to the button
     * @return the button that was created
     */
    public JButton createButton(String uiKey, ActionListener l) {
        JButton b = createButton(uiKey);
        b.addActionListener(l);
        return b;
    }

    /**
     * Create a button, using resources to specify the name and the tool tip,
     * and with a specified ActionListener and action command. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.btn  <td>the name for the button
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * </table>
     * In addition, the name of the button is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @param l the ActionListener to be add to the button
     * @param cmd the action command to be set for the button
     * @return the button that was created
     */
    public JButton createButton(String uiKey, ActionListener l, String cmd) {
        JButton b = createButton(uiKey);
        b.setActionCommand(cmd);
        b.addActionListener(l);
        return b;
    }

    /**
     * Constant to identify the cancellation option.
     */
    public static final String CANCEL = "cancel";

    /**
     * Special method to create a cancel button.  Differs from a
     * standard button because it does not require a mnemonic, per
     * the Java Look and Feel standard.
     * @param uiKey key to use to get the tooltip with
     * @return the button that was created
     */
    public JButton createCancelButton(String uiKey) {
        return createCancelButton(uiKey, closeListener);
    }

    /**
     * Special method to create a cancel button.  Differs from a
     * standard button because it does not require a mnemonic, per
     * the Java Look and Feel standard.
     * @param uiKey key to use to get the tooltip with
     * @param l listener to attach to the created button
     * @return the button that was created
     */
    public JButton createCancelButton(String uiKey, ActionListener l) {
        JButton b;

        I18NResourceBundle save_i18n = i18n;
        try {
            i18n = local_i18n;
            b = new JButton(getI18NString("uif.cancel.btn"));
            // no mnemonic for Cancel buttons
        }
        finally {
            i18n = save_i18n;
        }

        b.setActionCommand(CANCEL);
        b.addActionListener(l);
        b.setName(uiKey);
        setToolTip(b, uiKey);
        return b;
    }

    /**
     * Create a Close button, that will close the containing window when pressed,
     * using a resource to specify the information for the button.  <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.btn  <td>the name for the button
     * <tr><td><i>uiKey</i>.mne  <td>the mnemonic for the button
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * </table>
     * In addition, the name of the button is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @return the button that was created
     * @see #createButton
     */
    public JButton createCloseButton(String uiKey) {
        return createCloseButton(uiKey, true);
    }

    /**
     * Create a Close button, that will close the containing window when pressed,
     * using a resource to specify the information for the button.  <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.btn  <td>the name for the button
     * <tr><td><i>uiKey</i>.mne  <td>the mnemonic for the button, if required
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * </table>
     * In addition, the name of the button is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @param needMnemonic a boolean indicating whether or not a mnemonic should be
     * set on the button. If the button is going to be the default button for a
     * dialog, it does not need a mnemonic.
     * @return the button that was created
     * @see #createButton
     */
    public JButton createCloseButton(String uiKey, boolean needMnemonic) {
        JButton b = new JButton(getI18NString(uiKey + ".btn"));
        b.setName(uiKey);
        if (needMnemonic)
            setMnemonic(b, uiKey);
        setToolTip(b, uiKey);
        b.addActionListener(closeListener);
        return b;
    }

    /**
     * Create a Help button, that will display a specific help topic when pressed,
     * using a resource to specify the tool tip for the button.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * </table>
     * In addition, the name of the button is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @param helpID the help ID for the help topic to be displayed when the
     * button is pressed
     * @return the button that was created
     * @see #createButton
     */
    public JButton createHelpButton(String uiKey, String helpID) {
        JButton hb = createButton(uiKey);
        if (helpBroker == null)
            hb.setEnabled(false);
        else
            helpBroker.enableHelpOnButton(hb, helpID, null);
        return hb;
    }

    /**
     * Create a button containing an icon, using resources to specify the
     * icon image and the tool tip. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.icon <td>the name of the resource for the icon image
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * </table>
     * @param uiKey the base name of the resource to be used
     * @return the button that was created
     */
    public JButton createIconButton(String uiKey) {
        JButton b = createButton(uiKey, createIcon(uiKey));
        b.setBorder(BorderFactory.createEmptyBorder());
        return b;
    }

    /**
     * Create a button containing an icon, using resources to specify the
     * icon image and the tool tip. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.icon <td>the name of the resource for the icon image
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * </table>
     * @param uiKey the base name of the resource to be used
     * @param l the action listener to attach to the new button
     * @return the button that was created
     */
    public JButton createIconButton(String uiKey, ActionListener l) {
        JButton b = createButton(uiKey, createIcon(uiKey));
        b.addActionListener(l);
        return b;
    }

    // note this uses local_i18n, not the client i18n
    private JButton createOptionButton(String uiKey) {
        I18NResourceBundle save_i18n = i18n;
        try {
            i18n = local_i18n;
            JButton b = createButton(uiKey, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Component c = (Component) (e.getSource());
                        JOptionPane op = (JOptionPane) SwingUtilities.getAncestorOfClass(JOptionPane.class, c);
                        op.setValue(c); // JOptionPane expects the value to be set to the selected button
                        op.setVisible(false);
                    }
                });
            return b;
        }
        finally {
            i18n = save_i18n;
        }
    }

    /**
     * Create a radio button, using resources to specify the name and tool tip. <br>
     * The button is initially set to <code>false</code>.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.rb  <td>the label for the button
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * <tr><td><i>uiKey</i>.mne  <td>the mnemonic for the button
     * </table>
     * In addition, the name of the button is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @param group the group to which the check box will be added
     * @return the radio button that was created
     * @see #createButton
     * @see #createCheckBox
     */
    public JRadioButton createRadioButton(String uiKey, ButtonGroup group) {
        String text = getI18NString(uiKey + ".rb");
        JRadioButton btn = new JRadioButton(text, true);
        btn.setName(uiKey);
        btn.setSelected(false); // workaround Merlin bug
        setMnemonic(btn, uiKey);
        setToolTip(btn, uiKey);
        group.add(btn);
        return btn;
    }

    /**
     * Set the mnemonic a button.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.mne <td>The keystroke to use
     * </table>
     * @param b the button to modify
     * @param uiKey the base name of the resources to be used
     * @see javax.swing.KeyStroke
     */
    public void setMnemonic(AbstractButton b, String uiKey) {
        // NOTE: Swing is misleading; it uses an integer value for the mnemonic
        // but according to SwingUtilities.findDisplayedMnemonicIndex it is always
        // the literal character for the mnemonic, and not anything fancy like
        // an integer keycode
        int mne = getI18NMnemonic(uiKey + ".mne");
        if (mne != 0)
            b.setMnemonic(mne);
    }

    //----------------------------------------------------------------------------
    //
    // check boxes

    /**
     * Create a check box, using resources to specify the name and the tool tip. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.ckb  <td>the name for the check box
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * <tr><td><i>uiKey</i>.mne  <td>the mnemonic for the button
     * </table>
     * In addition, the name of the check box is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @return the check box that was created
     * @see #createCheckBoxMenuItem
     */
    public JCheckBox createCheckBox(String uiKey) {
        return createCheckBox(uiKey, false, null);
    }

    /**
     * Create a check box, using resources to specify the name and the tool tip. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.ckb  <td>the name for the check box
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * <tr><td><i>uiKey</i>.mne  <td>the mnemonic for the button
     * </table>
     * In addition, the name of the check box is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @param state the initial state of the check box
     * @return the check box that was created
     * @see #createCheckBoxMenuItem
     */
    public JCheckBox createCheckBox(String uiKey, boolean state) {
        return createCheckBox(uiKey, state, null);
    }

    /**
     * Create a check box, using resources to specify the name and the tool tip,
     * within a specified button group. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.ckb  <td>the name for the check box
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the button
     * <tr><td><i>uiKey</i>.mne  <td>the mnemonic for the button
     * </table>
     * In addition, the name of the check box is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @param state the initial state of the check box
     * @param group the group to which the check box will be added
     * @return the check box that was created
     */
    public JCheckBox createCheckBox(String uiKey, boolean state, ButtonGroup group) {
        String ckbKey = uiKey + ".ckb";
        JCheckBox b = new JCheckBox(getI18NString(ckbKey), state);
        b.setName(uiKey);
        if (group != null)
            group.add(b);
        setMnemonic(b, uiKey);
        setToolTip(b, uiKey);
        return b;
    }


    //----------------------------------------------------------------------------
    //
    // choice lists

    /**
     * Create a choice item, using resources to specify the choices and the
     * tool tip. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.<i>choiceKeys<sub>i</sub></i>.chc  <td>the choice to appear in the item, for 0 &lt;= i &lt; choiceKeys.length
     * <tr><td><i>uiKey</i>.name <td>the accessible name for the selector
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the choice item
     * </table>
     * In addition, the name of the choice is set to <i>uiKey</i>.
     * Note: the choice item is created with the choices set to the names
     * of the resources used -- not the values. This means that the client can
     * examine and manipulate the choices, including the selected choice,
     * as location-independent resource names. A custom renderer is used to
     * ensure that the correctly localized value is displayed to the user.
     * @param uiKey the base name of the resources to be used for the menu
     * @param choiceKeys an array of strings used to construct the resource
     * names for the choices.
     * @return the choice item that was created
     * @see #createLiteralChoice
     */
    public JComboBox createChoice(final String uiKey, final String[] choiceKeys) {
        return createChoice(uiKey, choiceKeys, false);
    }

    /**
     * Same as the two parameter <code>createChoice</code>, except you can
     * make this an mutable choice component (freeform editing of the
     * response).  If the component is to be editable, an additional
     * <i>uiKey</i>.ed resource is needed to set the component name of the
     * editable field which will be onscreen.
     * @param uiKey the base name of the resources to be used for the menu
     * @param choiceKeys an array of strings used to construct the resource
     * names for the choices.
     * @param editable True if the choice component should allow freeform
     * editing of the response.
     * @return a choice box with the attributes indicated by the parameters
     * @see #createChoice(String,String[])
     */
    public JComboBox createChoice(final String uiKey, final String[] choiceKeys, boolean editable) {
        // create a cache of the presentation string, for use when
        // rendering, but otherwise, let the JComboBox work in terms of the
        // choiceKeys
        final String[] choices = new String[choiceKeys.length];
        for (int i = 0; i < choices.length; i++)
            choices[i] = getI18NString(uiKey + "." + choiceKeys[i] + ".chc");

        JComboBox choice = new JComboBox(choiceKeys);
        choice.setName(uiKey);
        setToolTip(choice, uiKey);
        setAccessibleName(choice, uiKey);

        choice.setEditable(editable);
        if (editable) {
            Component editComp = choice.getEditor().getEditorComponent();
            if (editComp instanceof Accessible) {
                if (editComp.getName() == null)
                    editComp.setName(uiKey + ".ed");
                AccessibleContext ac = choice.getAccessibleContext();
                AccessibleContext ed_ac = editComp.getAccessibleContext();
                ed_ac.setAccessibleName(ac.getAccessibleName());
                ed_ac.setAccessibleDescription(ac.getAccessibleDescription());
            }
        }

        choice.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object o, int index,
                                boolean isSelected, boolean cellHasFocus) {
                Object c = o;
                for (int i = 0; i < choiceKeys.length; i++) {
                    if (choiceKeys[i] == o) {
                        c = choices[i];
                        break;
                    }
                }
                return super.getListCellRendererComponent(list, c, index, isSelected, cellHasFocus);
            }
        });

        return choice;
    }

    /**
     * Create an empty choice item, using a resource to specify the tool tip. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the choice item
     * </table>
     * In addition, the name of the choice is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used for the menu
     * @return the choice component that was created
     */
    public JComboBox createChoice(String uiKey) {
        return createChoice(uiKey, false);
    }

    /**
     * Same as single parameter version, except you can select a
     * component that allows freeform editing of the user's response.
     * @param uiKey the base name of the resources to be used for the menu
     * @param editable True if the user should be allowed to edit the
     * response.
     * @return the choice component that was created
     * @see #createChoice(String)
     */
    public JComboBox createChoice(String uiKey, boolean editable) {
        return createChoice(uiKey, editable, (JLabel) null);
    }

    /**
     * Same as the one parameter version, except a label can be
     * associated with this component.  This is to support accessibility.
     * @param uiKey the base name of the resources to be used for the menu
     * @param label Label to associate with this component
     * @return the choice component that was created
     * @see #createChoice(String)
     * @see javax.swing.JLabel#setLabelFor
     */
    public JComboBox createChoice(String uiKey, JLabel label) {
        return createChoice(uiKey, false, label);
    }

    /**
     * Combination of the two parameter methods, allowing you to select
     * a mutable response and associate a label.
     * @param uiKey the base name of the resources to be used for the menu
     * @param editable True if the user should be allowed to edit the
     * response.
     * @param label Label to associate with this component
     * @return a choice box with the attributes indicated by the parameters
     * @see #createChoice(String,JLabel)
     * @see #createChoice(String,boolean)
     * @see #createChoice(String)
     * @see javax.swing.JLabel#setLabelFor
     */
    public JComboBox createChoice(String uiKey, boolean editable, JLabel label) {
        JComboBox choice = new JComboBox();
        choice.setName(uiKey);
        setToolTip(choice, uiKey);

        if (label != null)
            label.setLabelFor(choice);
        else
            setAccessibleName(choice, uiKey);

        choice.setEditable(editable);
        if (editable) {
            Component editComp = choice.getEditor().getEditorComponent();
            if (editComp instanceof Accessible) {
                if (editComp.getName() == null)
                    editComp.setName(uiKey + ".ed");
                AccessibleContext ac = choice.getAccessibleContext();
                AccessibleContext ed_ac = editComp.getAccessibleContext();
                ed_ac.setAccessibleName(ac.getAccessibleName());
                ed_ac.setAccessibleDescription(ac.getAccessibleDescription());
            }
        }

        return choice;
    }

    /**
     * Create an choice item containing literal choices,
     * and using a resource to specify the tool tip.
     * The choices appear as given: for example, this method might be used to
     * create a choice item containing a set of filenames from which to choose. <br>
     * Note that if the choices are strings, they should probably be localized, and
     * if they are otherwise should probably be shown to the user using a renderer
     * which produces localized output.
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the choice item
     * </table>
     * In addition, the name of the choice is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used for the menu
     * @param choices the choices to appear in the choice item
     * @return the choice item that was created
     * @see #createChoice
     */
    public JComboBox createLiteralChoice(String uiKey, Object[] choices) {
        JComboBox choice = new JComboBox(choices);
        choice.setName(uiKey);
        setToolTip(choice, uiKey);
        return choice;
    }

    //----------------------------------------------------------------------------
    //
    // icons, images etc

    /**
     * Create an icon, using a resource to specify the image. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.icon  <td>the name of a resource containing the image
     * </table>
     * @param uiKey the base name of the resource to be used
     * @return the icon that was created
     * @throws  MissingResourceException if the image resource cannot be found
     * @see #createIconButton
     */
    public Icon createIcon(String uiKey) {
        return new ImageIcon(getIconURL(uiKey));
    }

    /**
     * Get the resource URL for an icon specified in a resource bundle. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.icon  <td>the name of a resource containing the image
     * </table>
     * @param uiKey the base name of the resource to be used
     * @return the URL for the resource obtained from the resource bundle
     * @throws  MissingResourceException if the image resource cannot be found
     */
    public URL getIconURL(String uiKey) {
        String r = getI18NString(uiKey + ".icon");
        URL url = clientClass.getResource(r);
        if (url == null)
            throw new MissingResourceException(r, clientClass.getName(), r);
        return url;
    }

    /**
     * Create a label containing an icon, using a resource to specify the
     * icon image. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.icon <td>the name of the resource for the icon image
     * </table>
     * @param uiKey the base name of the resource to be used
     * @return the image that was created
     * @throws  MissingResourceException if the image resource cannot be found
     * @see #createLabel
     */
    public JLabel createIconLabel(String uiKey) {
        return new JLabel(createIcon(uiKey));
    }

    /**
     * Create an image from a named resource.
     * @param r The resource containing the image data.
     * @return the image that was created
     * @throws  MissingResourceException if the image resource cannot be found
     */
    public Image createImage(String r) {
        URL url = getClass().getResource(r);
        if (url == null)
            throw new MissingResourceException(r, clientClass.getName(), r);
        return Toolkit.getDefaultToolkit().getImage(url);
    }

    //----------------------------------------------------------------------------
    //
    // labels

    /**
     * Create a label, using a resource to specify the text. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.lbl  <td>the text for the label
     * </table>
     * @param uiKey the base name of the resource to be used
     * @return the label that was created
     * @see #createIconLabel
     */
    public JLabel createLabel(String uiKey) {
        return createLabel(uiKey, false);
    }

    /**
     * Create a label, using a resource to specify the text and an optional mnemonic.<br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.lbl  <td>the text for the label
     * <tr><td><i>uiKey</i>.tip  <td>the tooltip text for the label
     * <tr><td><i>uiKey</i>.mne  <td>the mnemonic for the label
     * </table>
     * @param uiKey the base name of the resource to be used
     * @param need508 whether or not a mnemonic and tooltip should be set for this label
     * @return the label that was created
     * @see #createIconLabel
     */
    public JLabel createLabel(String uiKey, boolean need508) {
        JLabel l = new JLabel(getI18NString(uiKey + ".lbl"));
        l.setName(uiKey);
        if (need508) {
            setToolTip(l, uiKey);
            l.setDisplayedMnemonic(getI18NMnemonic(uiKey + ".mne"));
        }
        return l;
    }


    //----------------------------------------------------------------------------
    //
    // lists

    /**
     * Create an input text field, using a resource to specify the tool tip.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * </table>


    /**
     * Create an empty list component. <br>
     * Note: list components do not currently support tool tips.
     * When they do, this method will use a resource to specify the tool tip.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.name  <td>the accessible name of the list
     * <tr><td><i>uiKey</i>.desc  <td>the accessible description of the list
     * </table>
     * @param uiKey the base name of the resource to be used (currently ignored)
     * @return the list that was created
     */
    public JList createList(String uiKey) {
        JList list = new JList();
        list.setName(uiKey);
        setAccessibleInfo(list, uiKey);
        return list;
    }

    /**
     * Create a list component with a given data model. <br>
     * Note: list components do not currently support tool tips.
     * When they do, this method will use a resource to specify the tool tip.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.name  <td>the accessible name of the list
     * <tr><td><i>uiKey</i>.desc  <td>the accessible description of the list
     * </table>
     * @param uiKey the base name of the resource to be used (currently ignored)
     * @param model the data model for this list
     * @return the list that was created
     */
    public JList createList(String uiKey, ListModel model) {
        JList list = new JList(model);
        list.setName(uiKey);
        setAccessibleInfo(list, uiKey);
        return list;
    }

    //----------------------------------------------------------------------------
    //
    // menus

    /**
     * Create an empty menu bar, using resources to specify the accessible info.<br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.name <td>the accessible name text
     * <tr><td><i>uiKey</i>.desc <td>accessible description text
     * </table>
     * @param uiKey the base name of the resource to be used
     * @return the menu bar that was created
     */
    public JMenuBar createMenuBar(String uiKey) {
        JMenuBar mb = new JMenuBar();
        mb.setName(uiKey);
        setAccessibleInfo(mb, uiKey);
        return mb;
    }

    /**
     * Create an empty menu, using resources to specify the name and mnemonic. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.menu  <td>the display name of the menu
     * <tr><td><i>uiKey</i>.mne  <td>the single character mnemonic for the menu
     * <tr><td><i>uiKey</i>.desc <td>accessible description text
     * </table>
     * @param uiKey the base name of the resource to be used
     * @return the menu that was created
     * @see #createPopupMenu
     */
    public JMenu createMenu(String uiKey) {
        JMenu m = new JMenu();
        initMenu(m, uiKey);
        return m;
    }

    /**
     * Initialize an empty menu, using resources to specify the name and mnemonic. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.menu  <td>the display name of the menu
     * <tr><td><i>uiKey</i>.mne  <td>the single character mnemonic for the menu
     * <tr><td><i>uiKey</i>.desc <td>accessible description text
     * </table>
     * @param m the menu the be initialized
     * @param uiKey the base name of the resource to be used
     * @see #createPopupMenu
     */
    public void initMenu(JMenu m, String uiKey) {
        m.setName(uiKey);
        m.setText(getI18NString(uiKey + ".menu"));
        setMnemonic(m, uiKey);
        setAccessibleDescription(m, uiKey);
    }

    /**
     * Create a menu, using actions to specify the menu items,
     * and using resources to specify the name and mnemonic. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.menu  <td>the display name of the menu
     * <tr><td><i>uiKey</i>.mne  <td>the single character mnemonic for the menu
     * </table>
     * @param uiKey the base name of the resources to be used
     * @param actions the actions from which to create the menu items;
     *  use null in the array to indicate if and where a separator is required
     * @return the menu that was created
     * @see #createMenuItem(Action)
     */
    public JMenu createMenu(String uiKey, Action[] actions) {
        JMenu m = createMenu(uiKey);
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            if (action == null)
                m.addSeparator();
            else
                m.add(createMenuItem(action));
        }
        return m;
    }

    /**
     * Create a menu using resources and an action listener to specify
     * the menu items, and using resources to specify the name and mnemonic. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.menu  <td>the display name of the menu
     * <tr><td><i>uiKey</i>.mne  <td>the single character mnemonic for the menu
     * <tr><td><i>uiKey</i>.<i>actions<sub>i</sub></i>.mit  <td>the text for the menu item, for 0 &lt;= i &lt; choiceKeys.length
     * <tr><td><i>uiKey</i>.<i>actions<sub>i</sub></i>.mne  <td>the single character mnemonic for the menu item, for 0 &lt;= i &lt; choiceKeys.length
     * </table>
     * @param uiKey the base name of the resources to be used
     * @param actions the qualifying names for the resources for the
     *  individual menu items; use null in the array to indicate if
     *  and where a separator is required
     * @param l the action listener to be used for each menu item
     * @return the menu that was created
     * @see #createMenuItem(String, String, ActionListener)
     */
    public JMenu createMenu(String uiKey, String[] actions, ActionListener l) {
        JMenu m = new JMenu();
        initMenu(m, uiKey, actions, l);
        return m;
    }

    /**
     * Initialize a menu using resources and an action listener to specify
     * the menu items, and using resources to specify the name and mnemonic. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.menu  <td>the display name of the menu
     * <tr><td><i>uiKey</i>.mne  <td>the single character mnemonic for the menu
     * <tr><td><i>uiKey</i>.<i>actions<sub>i</sub></i>.mit  <td>the text for the menu item, for 0 &lt;= i &lt; choiceKeys.length
     * <tr><td><i>uiKey</i>.<i>actions<sub>i</sub></i>.mne  <td>the single character mnemonic for the menu item, for 0 &lt;= i &lt; choiceKeys.length
     * </table>
     * @param m the menu the be initialized
     * @param uiKey the base name of the resources to be used
     * @param actions the qualifying names for the resources for the
     *  individual menu items; use null in the array to indicate if
     *  and where a separator is required
     * @param l the action listener to be used for each menu item
     * @see #createMenuItem(String, String, ActionListener)
     */
    public void initMenu(JMenu m, String uiKey, String[] actions, ActionListener l) {
        initMenu(m, uiKey);
        for (int i = 0; i < actions.length; i++) {
            String action = actions[i];
            if (action == null)
                m.addSeparator();
            else
                m.add(createMenuItem(uiKey, action, l));
        }
    }

    /**
     * Create an empty popup menu.
     * @param uiKey the base name of the resource to be used (currently ignored)
     * @return the popup menu that was created
     * @see #createMenu
     */
    public JPopupMenu createPopupMenu(String uiKey) {
        return new JPopupMenu(/*getI18NString(uiKey + ".pop")*/);
    }

    /**
     * Create an popup menu.
     * @param uiKey the base name of the resource to be used
     * @param actions the qualifying names for the resources for the
     *  individual menu items; use null in the array to indicate if
     *  and where a separator is required
     * @param l the action listener to be used for each menu item
     * @return the popup menu that was created
     * @see #createMenu
     */
    public JPopupMenu createPopupMenu(String uiKey, String[] actions, ActionListener l) {
        JPopupMenu m = createPopupMenu(uiKey);
        for (int i = 0; i < actions.length; i++) {
            String action = actions[i];
            if (action == null)
                m.addSeparator();
            else
                m.add(createMenuItem(uiKey, action, l));
        }
        return m;
    }

    //----------------------------------------------------------------------------
    //
    // menu items

    /**
     * Create a menu item for an action.
     * The name of the item is set to the action name.
     * @param action from which to create the menu item
     * @return the menu item that was created
     * @see #createMenu(String, Action[])
     */
    public JMenuItem createMenuItem(Action action) {
        JMenuItem item = new JMenuItem(action);
        item.setName((String)(action.getValue(Action.NAME)));
        // could (should?) ensure everything is set correctly
        return item;
    }

    /**
     * Create a menu item, using resources to specify the text and mnemonic. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.<i>action</i>.mit  <td>the text for the menu item
     * <tr><td><i>uiKey</i>.<i>action</i>.mne  <td>the single character mnemonic for the menu item
     * </table>
     * @param uiKey the base name of the resources to be used
     * @param action the qualifying name for the resources for the menu item
     * @param l the action listener for the menu item
     * @return the menu item that was created
     * @see #createMenu(String, String[], ActionListener)
     */
    public JMenuItem createMenuItem(String uiKey, String action, ActionListener l) {
        JMenuItem item = new JMenuItem(getI18NString(uiKey + "." + action + ".mit"));
        item.setActionCommand(action);
        item.addActionListener(l);
        item.setName(action);
        setMnemonic(item, uiKey + "." + action);
        return item;
    }

    /**
     * Create a check box menu item, using resources to specify the
     * name and the tool tip. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.<i>name</i>.ckb  <td>the name for the menu item
     * <tr><td><i>uiKey</i>.<i>name</i>.tip  <td>the tool tip for the menu item
     * </table>
     * In addition, the name of the check box is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @param name a qualifying name for the resources used for this menu item
     * @param state the initial state of the check box
     * @return the check box that was created
     */
    public JCheckBoxMenuItem createCheckBoxMenuItem(String uiKey, String name, boolean state) {
        String uiKey_name = uiKey + "." + name;
        String ckbKey = uiKey_name + ".ckb";
        JCheckBoxMenuItem b = new JCheckBoxMenuItem(getI18NString(ckbKey), state);
        b.setName(uiKey_name);
        setMnemonic(b, uiKey_name);
        setToolTip(b, uiKey_name);
        return b;
    }

    /**
     * Create a Help menu item, that will display a specific help topic when pressed,
     * using resources to specify the name and mnemonic for the item.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.mit  <td>the text for the menu item
     * <tr><td><i>uiKey</i>.mne  <td>the mnemonic for the menu item
     * </table>
     * In addition, the name of the choice is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @param helpID the help ID for the help topic to be displayed when the
     * button is pressed
     * @return the button that was created
     * @see #createButton
     */
    public JMenuItem createHelpMenuItem(String uiKey, String helpID) {
        JMenuItem mi = new JMenuItem(getI18NString(uiKey + ".mit"));
        setMnemonic(mi, uiKey);
        if (helpBroker == null)
            mi.setEnabled(false);
        else
            helpBroker.enableHelpOnButton(mi, helpID, null);
        return mi;
    }

    /**
     * Create a menu item for a literal string and a specified listener.
     * No mnemonic key nor descriptive action is added.
     * @param literal the text for the menu item
     * @param l the action listener to add to the menu item
     * @return the menu item that was created
     */
    public JMenuItem createLiteralMenuItem(String literal, ActionListener l) {
        JMenuItem item = new JMenuItem(literal);
        item.addActionListener(l);
        return item;
    }

    /**
     * Create a check box menu item, using resources to specify the
     * name and the tool tip. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.<i>name</i>.ckb  <td>the name for the menu item
     * <tr><td><i>uiKey</i>.<i>name</i>.tip  <td>the tool tip for the menu item
     * </table>
     * In addition, the name of the radio button is set to <i>uiKey</i>.
     * @param uiKey the base name of the resources to be used
     * @param name a qualifying name for the resources used for this menu item
     * @return the check box that was created
     */
    public JRadioButtonMenuItem createRadioButtonMenuItem(String uiKey, String name) {
        String uiKey_name = uiKey + "." + name;
        String radKey = uiKey_name + ".rad";
        JRadioButtonMenuItem b = new JRadioButtonMenuItem(getI18NString(radKey));
        b.setName(uiKey_name);
        setMnemonic(b, uiKey_name);
        setToolTip(b, uiKey_name);
        return b;
    }

    //----------------------------------------------------------------------------
    //
    // scrollpane

    /**
     * Surround a component in a scroll pane.
     * The name of the scroll pane component is set to <code>c.getName()</code>
     * plus the <i>.sp</i> suffix.
     * @param c The component to put into the scroll pane.
     * @return a scroll pane component with the given component inside
     */
    public JScrollPane createScrollPane(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setName(c.getName() == null ? "sp" : c.getName() + ".sp");
        sp.setFocusable(false);
        return sp;
    }

    /**
     * Same as the single argument version, with options for altering
     * the scrollbar appearance policy.
     * @param c The component to put into the scroll pane.
     * @param vsp vertical scrollbar policy setting
     * @param hsp horizontal scrollbar policy setting
     * @return a scroll pane component with the given component inside
     * @see javax.swing.ScrollPaneConstants
     * @see javax.swing.JScrollPane
     */
    public JScrollPane createScrollPane(JComponent c, int vsp, int hsp) {
        JScrollPane sp = new JScrollPane(c, vsp, hsp);
        sp.setName(c.getName() == null ? "sp" : c.getName() + ".sp");
        sp.setFocusable(false);
        return sp;
    }

    //----------------------------------------------------------------------------
    //
    // slider

    /**
     * Create a slider, using resources to specify the the tool tip. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.<i>name</i>.tip  <td>the tool tip for the menu item
     * </table>
     * @param uiKey the base name of the resources to be used
     * @param min the minimum value for the slider
     * @param max the maximum value for the slider
     * @param value the initial value for the slider
     * @return the slider that was created
     */
    public JSlider createSlider(String uiKey, int min, int max, int value) {
        JSlider s = new JSlider(min, max, value);
        setToolTip(s, uiKey);
        return s;
    }

    //----------------------------------------------------------------------------
    //
    // split pane

    /**
     * Create an empty split pane with the given orientation.
     * @param orient The split's orientation.
     * @return The empty split pane component.
     * @see javax.swing.JSplitPane#VERTICAL_SPLIT
     * @see javax.swing.JSplitPane#HORIZONTAL_SPLIT
     */
    public JSplitPane createSplitPane(int orient) {
        JSplitPane sp = new JSplitPane(orient);
        sp.setName("split");
        setSplitPaneInfo(sp);
        return sp;
    }

    /**
     * Create an empty split pane with the given components inside.
     * @param orient The split's orientation.
     * @param c1 first component (left)
     * @param c2 first component (right)
     * @return The populated split pane component.
     * @see javax.swing.JSplitPane
     * @see javax.swing.JSplitPane#VERTICAL_SPLIT
     * @see javax.swing.JSplitPane#HORIZONTAL_SPLIT
     */
    public JSplitPane createSplitPane(int orient, Component c1, Component c2) {
        JSplitPane sp = new JSplitPane(orient, c1, c2);
        sp.setName("split");
        setSplitPaneInfo(sp);
        return sp;
    }

    private void setSplitPaneInfo(JSplitPane sp) {
        // set a11y info manually using local bundle
        AccessibleContext ac = sp.getAccessibleContext();
        if (sp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
            ac.setAccessibleName(local_i18n.getString("uif.sp.hor.name"));
            ac.setAccessibleDescription(local_i18n.getString("uif.sp.hor.desc"));
        }
        else {
            ac.setAccessibleName(local_i18n.getString("uif.sp.vert.name"));
            ac.setAccessibleDescription(local_i18n.getString("uif.sp.vert.desc"));
        }
    }

    //----------------------------------------------------------------------------
    //
    // spinners - not accessible as of JDK 1.5, so it's commented out here!

    /**
     * Create a spinner.
     * @param uiKey the base name of the resources to be used
     * @return a spinner component
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.<code>name</code><td> the accessible name for the tab pane.
     *          Where <code>name</code> is the literal string "name".
     * <tr><td><i>uiKey</i>.<code>tip</code><td> the accessible name for the tab pane.
     *          Where <code>tip</code> is the literal string "tip".
     * </table>
     * The tooltip will automatically be transferred to the pane's accessible
     * description.  Use <code>setAccessibleDescription()</code> to set it
     * independently.
    public JSpinner createSpinner(String uiKey, SpinnerModel model) {
        JSpinner s = new JSpinner(model);
        s.setName(uiKey);
        setAccessibleName(s, uiKey);
        setToolTip(s, uiKey);
        return s;
    }
     */


    //----------------------------------------------------------------------------
    //
    // tabbed paned

    /**
     * Create an empty tabbed pane.
     * @param uiKey the base name of the resources to be used
     * @return an empty (no tabs) tabbed pane
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.<code>name</code><td> the accessible name for the tab pane.
     *          Where <code>name</code> is the literal string "name".
     * <tr><td><i>uiKey</i>.<code>tip</code><td> the accessible name for the tab pane.
     *          Where <code>tip</code> is the literal string "tip".
     * </table>
     * The tooltip will automatically be transferred to the pane's accessible
     * description.  Use <code>setAccessibleDescription()</code> to set it
     * independently.
     */
    public JTabbedPane createTabbedPane(String uiKey) {
        JTabbedPane p = new JTabbedPane();
        p.setName(uiKey);
        setAccessibleName(p, uiKey);
        setToolTip(p, uiKey);
        return p;
    }

    /**
     * Create a tabbed pane with a given set of component panes,
     * using resources to determine the name and tool tip for each tab. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.<i>name<sub>i</sub></i>.tab  <td>the display name for the tab,
     *          where <i>name<sub>i</sub></i> is the component name for children[i]
     * <tr><td><i>uiKey</i>.<i>name<sub>i</sub></i>.tip  <td>the tool tip for the tab,
     *          where <i>name<sub>i</sub></i> is the component name for children[i]
     * <tr><td><i>uiKey</i>.<code>name</code><td> the accessible name for the tab pane.
     *          Where <code>name</code> is the literal string "name".
     * <tr><td><i>uiKey</i>.<code>tip</code><td> the accessible name for the tab pane.
     *          Where <code>tip</code> is the literal string "tip".
     * </table>
     * The tooltip will automatically be transferred to the pane's accessible
     * description.  Use <code>setAccessibleDescription()</code> to set it
     * independently.
     * @param uiKey the base name of the resources to be used
     * @param children an array of components to be added into the tabbed pane
     * @return the tabbed pane that was created
     * @see #setAccessibleDescription(Component,String)
     * @see #setAccessibleName(Component,String)
     * @see #setToolTip(JComponent,String)
     */
    public JTabbedPane createTabbedPane(String uiKey, JComponent[] children) {
        JTabbedPane p = new JTabbedPane();
        p.setName(uiKey);
        setAccessibleName(p, uiKey);
        for (int i = 0; i < children.length; i++) {
            JComponent child = children[i];
            addTab(p, uiKey + "." + child.getName(), child);
        }
        setToolTip(p, uiKey);
        return p;
    }

    /**
     * Add a component to a tabbed pane, using resources to specify
     * the name and the tool tip for the tab. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.tab  <td>the name for the tab
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the tab
     * </table>
     * @param tPane the tabbed pane to which to add the component
     * @param uiKey the base name of the resources to be used
     * @param comp the component to be added
     */
    public void addTab(JTabbedPane tPane, String uiKey, JComponent comp) {
        String name = getI18NString(uiKey + ".tab");
        String tip = getI18NString(uiKey + ".tip");
        tPane.addTab(name, null, comp, tip);

    }

    //----------------------------------------------------------------------------
    //
    // tables

    /**
     * Create a table with a given data model.
     * Resources used:
     * <table>
     * <tr><td><i>uiKey</i>.<code>name</code><td> the accessible name for the tab pane.
     *          Where <code>name</code> is the literal string "name".
     * <tr><td><i>uiKey</i>.<code>tip</code><td> the accessible name for the tab pane.
     *          Where <code>tip</code> is the literal string "tip".
     * </table>
     * The tooltip will automatically be transferred to the pane's accessible
     * description.  Use <code>setAccessibleDescription()</code> to set it
     * independently.
     * @param uiKey the base name of the resources to be used (currently ignored)
     * @param model the data model for the table
     * @return the table that was created
     * @see #setAccessibleDescription(Component,String)
     * @see #setAccessibleName(Component,String)
     * @see #setToolTip(JComponent,String)
     */
    public JTable createTable(String uiKey, TableModel model) {
        JTable tbl = new JTable(model);
        setAccessibleName(tbl, uiKey);
        setToolTip(tbl, uiKey);
        return tbl;
    }

    //----------------------------------------------------------------------------
    //
    // text fields, text areas etc

    /**
     * Create a text field for use as a heading, using a resource to specify
     * the heading. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the text for the heading
     * </table>
     * In addition, the name of the output field is set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @return the text field that was created
     */
    public JTextField createHeading(String uiKey) {
        String value = getI18NString(uiKey + ".txt");
        JTextField tf = new JTextField(value, value.length());
        tf.setName(uiKey);
        tf.setEditable(false);
        tf.setFont(tf.getFont().deriveFont(Font.BOLD));
        tf.setBorder(BorderFactory.createEmptyBorder());
        tf.setBackground(Colors.TRANSPARENT.color);
        tf.setOpaque(false);
        setAccessibleDescription(tf, uiKey);
        setAccessibleName(tf, uiKey);
        return tf;
    }

    /**
     * Create an input text field, using a resource to specify the tool tip.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * </table>
     * In addition, the name of the input field is set to <i>uiKey</i>.
     * By default, the input field is 10 characters wide.
     * @param uiKey the base name of the resource to be used
     * @return the input field that was created
     */
    public JTextField createInputField(String uiKey) {
        return createInputField(uiKey, null);
    }

    /**
     * Create an input text field, using a resource to specify the tool tip.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * </table>
     * In addition, the name of the input field is set to <i>uiKey</i>.
     * By default, the input field is 10 characters wide.
     * @param uiKey the base name of the resource to be used
     * @param label the label to associate with this component
     * @return the input field that was created
     */
    public JTextField createInputField(String uiKey, JLabel label) {
        return createInputField(uiKey, 10, label);
    }

    /**
     * Create an input text field with a specified number of columns,
     * using a resource to specify the tool tip.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * </table>
     * In addition, the name of the input field is set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @param cols the default width of the field, in characters
     * @return the input field that was created
     * @see #createOutputField
     */
    public JTextField createInputField(String uiKey, int cols) {
        return createInputField(uiKey, cols, null);
    }

    /**
     * Create an input text field with a specified number of columns,
     * using a resource to specify the tool tip.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * </table>
     * In addition, the name of the input field is set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @param cols the default width of the field, in characters
     * @param label the label to associate with this component
     * @return the input field that was created
     * @see #createOutputField
     */
    public JTextField createInputField(String uiKey, int cols, JLabel label) {
        JTextField tf = new JTextField("", cols) {
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };

        if (label != null)
            label.setLabelFor(tf);
        else {
            // this should be setAccessibleName(tf, uiKey); but that will break too much code
            tf.setName(uiKey);
            //setAccessibleName(tf, uiKey);
        }

        setToolTip(tf, uiKey);
        return tf;
    }

    /**
     * Create a message area, using a resource to specify the content.
     * The message area will be transparent, uneditable, and word-wrapped. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the text for the message area
     * </table>
     * @param uiKey the name of the resource to be used
     * @return the message area that was created
     */
    public JTextArea createMessageArea(String uiKey) {
        return createLocalizedMessageArea(uiKey, getI18NString(uiKey + ".txt"), true);
    }

    /**
     * Create a message area, using a resource to specify the content.
     * The message area will be transparent, uneditable, and word-wrapped. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the text for the message area
     * <tr><td><i>uiKey</i>.name  <td>accessible name
     * <tr><td><i>uiKey</i>.desc  <td>accessible description text
     * </table>
     * @param uiKey the name of the resource to be used
     * @param arg an argument to be formatted into the content using
     * {@link java.text.MessageFormat#format}
     * @return the message area that was created
     */
    public JTextArea createMessageArea(String uiKey, Object arg) {
        return createLocalizedMessageArea(uiKey, getI18NString(uiKey + ".txt", arg), true);
    }


    /**
     * Create a message area, using a resource to specify the content.
     * The message area will be transparent, uneditable, and word-wrapped. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the text for the message area
     * <tr><td><i>uiKey</i>.name  <td>accessible name
     * <tr><td><i>uiKey</i>.desc  <td>accessible description text
     * </table>
     * @param uiKey the name of the resource to be used
     * @param args an array of arguments to be formatted into the content using
     * {@link java.text.MessageFormat#format}
     * @return the message area that was created
     */
    public JTextArea createMessageArea(String uiKey, Object[] args) {
        return createLocalizedMessageArea(uiKey, getI18NString(uiKey + ".txt", args), true);
    }

    /**
     * Only use this method if the origin of the message text is not coming from
     * a bundle.
     */
    private JTextArea createLiteralMessageArea(String msg) {
        JTextArea txt = new JTextArea(msg);
        txt.setName("literal");
        txt.setOpaque(false);
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        // The height is effectively ignored in the next line (just don't use 0.)
        // The text will be laid out, wrapping lines, for the width, and the
        // preferred height will thereby be determined accordingly.
        txt.setSize(new Dimension(7 * DOTS_PER_INCH, Integer.MAX_VALUE));
        // override JTextArea focus traversal keys, resetting them to
        // the Component default (i.e. the same as for the parent.)
        txt.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        txt.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        AccessibleContext ac = txt.getAccessibleContext();
        ac.setAccessibleName(local_i18n.getString("uif.message.name"));
        ac.setAccessibleDescription(local_i18n.getString("uif.message.desc"));
        return txt;
    }


    /**
     * @param std True if this area should be made accessible.
     */
    private JTextArea createLocalizedMessageArea(String uiKey, String msg, boolean std) {
        JTextArea txt = new JTextArea(msg);
        txt.setName(uiKey);
        txt.setOpaque(false);
        txt.setBackground(Colors.TRANSPARENT.getValue());
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        // The height is effectively ignored in the next line (just don't use 0.)
        // The text will be laid out, wrapping lines, for the width, and the
        // preferred height will thereby be determined accordingly.
        txt.setSize(new Dimension(7 * DOTS_PER_INCH, Integer.MAX_VALUE));
        // override JTextArea focus traversal keys, resetting them to
        // the Component default (i.e. the same as for the parent.)
        txt.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        txt.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        if (std) {
            AccessibleContext ac = txt.getAccessibleContext();
            ac.setAccessibleName(local_i18n.getString("uif.message.name"));
            ac.setAccessibleDescription(local_i18n.getString("uif.message.desc"));
        }
        else
            setAccessibleInfo(txt, uiKey);
        return txt;
    }

    /**
     * Create an output text field, using a resource to specify the tool tip.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * <tr><td><i>uiKey</i>.name <td>accessible name
     * </table>
     * In addition, the name of the output field is set to <i>uiKey</i>.
     * By default, the output field is empty and is 10 characters wide.
     * @param uiKey the base name of the resource to be used
     * @return the empty output field that was created
     * @see #createInputField
     */
    public JTextField createOutputField(String uiKey) {
        return createOutputField(uiKey, "", 10, null, false);
    }

    /**
     * Same as the single parameter version, except a label, which labels
     * this new component, will be set.
     * The label's <code>setLabelFor()</code> will be set.
     * @param uiKey the base name of the resource to be used
     * @param label the label which is labeling this field
     * @return the output field that was created
     * @see #createInputField(String)
     */
    public JTextField createOutputField(String uiKey, JLabel label) {
        return createOutputField(uiKey, "", 10, label, false);
    }

    /**
     * Create an output text field with a specified number of columns,
     * using a resource to specify the tool tip,
     * which can automaticly select contained text.<br>
     * The label's <code>setLabelFor()</code> will be set.
     * @param uiKey the base name of the resource to be used
     * @param label the label which is labeling this field
     * @param autoSelect automaticly select text containing in the field on focus
     * @return the output field that was created
     * @see #createInputField(String)
     */
    public JTextField createOutputField(String uiKey, JLabel label, boolean autoSelect) {
        return createOutputField(uiKey, "", 10, label, autoSelect);
    }

    /**
     * Create an output text field with a specified number of columns,
     * and using a resource to specify the tool tip.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * <tr><td><i>uiKey</i>.name <td>accessible name
     * </table>
     * In addition, the name of the output field is set to <i>uiKey</i>.
     * The output field is initially empty.
     * @param uiKey the base name of the resource to be used
     * @param cols the default width of the field, in characters
     * @return the empty output field that was created
     */
    public JTextField createOutputField(String uiKey, int cols) {
        return createOutputField(uiKey, "", cols, null, false);
    }

    /**
     * Create an output text field with a specified number of columns,
     * using a resource to specify the tool tip, with an attached label.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * <tr><td><i>uiKey</i>.name <td>accessible name
     * </table>
     * In addition, the name of the output field is set to <i>uiKey</i>.
     * The output field is initially empty.
     * @param uiKey the base name of the resource to be used
     * @param cols the default width of the field, in characters
     * @param label the label which is labeling this field
     * @return the empty output field that was created
     */
    public JTextField createOutputField(String uiKey, int cols, JLabel label) {
        return createOutputField(uiKey, "", cols, label, false);
    }

    /**
     * Create an output text field with a specified number of columns,
     * using a resource to specify the tool tip, with an attached label,
     * which can automaticly select contained text.<br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * <tr><td><i>uiKey</i>.name <td>accessible name
     * </table>
     * In addition, the name of the output field is set to <i>uiKey</i>.
     * The output field is initially empty.
     * @param uiKey the base name of the resource to be used
     * @param cols the default width of the field, in characters
     * @param label the label which is labeling this field
     * @param autoSelect automaticly select text containing in the field on focus
     * @return the empty output field that was created
     */
    public JTextField createOutputField(String uiKey, int cols, JLabel label, boolean autoSelect) {
        return createOutputField(uiKey, "", cols, label, autoSelect);
    }

    /**
     * Create an output text field containing a specified value,
     * and using a resource to specify the tool tip.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * <tr><td><i>uiKey</i>.name <td>accessible name
     * </table>
     * In addition, the name of the output field is set to <i>uiKey</i>.
     * By default, the output field is 10 characters wide.
     * @param uiKey the base name of the resource to be used
     * @param value the initial text to appear in the output field
     * @return the output field that was created
     */
    public JTextField createOutputField(String uiKey, String value) {
        return createOutputField(uiKey, value, 10, null, false);
    }

    /**
     * Create an output text field containing a specified value,
     * using a resource to specify the tool tip,
     * with an attached label. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * <tr><td><i>uiKey</i>.name <td>accessible name
     * </table>
     * In addition, the name of the output field is set to <i>uiKey</i>.
     * By default, the output field is 10 characters wide.
     * @param uiKey the base name of the resource to be used
     * @param value the text to appear in the output field
     * @param label the label which is labeling this field
     * @return the output field that was created
     */
    public JTextField createOutputField(String uiKey, String value, JLabel label) {
        return createOutputField(uiKey, value, 10, label, false);
    }

    /**
     * Create an output text field containing a specified value,
     * with a specified number of columns,
     * and using a resource to specify the tool tip.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * <tr><td><i>uiKey</i>.name <td>accessible name
     * </table>
     * In addition, the name of the output field is set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @param value the text to appear in the output field
     * @param cols the default width of the field, in characters
     * @return the output field that was created
     */
    public JTextField createOutputField(String uiKey, String value, int cols) {
        return createOutputField(uiKey, value, cols, null, false);
    }

    /**
     * Create an output text field containing a specified value,
     * with a specified number of columns,
     * using a resource to specify the tool tip,
     * with a label referencing this new field.<br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * <tr><td><i>uiKey</i>.name <td>accessible name
     * </table>
     * In addition, the name of the output field is set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @param value the text to appear in the output field
     * @param cols the default width of the field, in characters
     * @param label the label which is labeling this field
     * @return the output field that was created
     */
    public JTextField createOutputField(String uiKey, String value, int cols, JLabel label) {
        return createOutputField(uiKey, value, cols, label, false);
    }

    /**
     * Create an output text field containing a specified value,
     * with a specified number of columns,
     * using a resource to specify the tool tip,
     * with a label referencing this new field,
     * which can automaticly select contained text.<br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the field
     * <tr><td><i>uiKey</i>.name <td>accessible name
     * </table>
     * In addition, the name of the output field is set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @param value the text to appear in the output field
     * @param cols the default width of the field, in characters
     * @param label the label which is labeling this field
     * @param autoSelect automaticly select text containing in the field on focus
     * @return the output field that was created
     */
    public JTextField createOutputField(String uiKey, String value, int cols, JLabel label, boolean autoSelect) {
        final JTextField tf = new JTextField(value, cols);
        tf.setName(uiKey);
        tf.setEditable(false);
        tf.setBackground(Colors.TRANSPARENT.getValue());
        tf.setOpaque(false);
        if(autoSelect)
            tf.addFocusListener(new java.awt.event.FocusListener() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    tf.setSelectionStart(0);
                    tf.setSelectionEnd(tf.getText().length());
                }

                public void focusLost(java.awt.event.FocusEvent e) {
                    tf.setSelectionStart(0);
                    tf.setSelectionEnd(0);
                }
            });

        if (label != null)
            label.setLabelFor(tf);
        else
            setAccessibleName(tf, uiKey);

        setToolTip(tf, uiKey);
        // override JTextField focus traversal keys, resetting them to
        // the Component default (i.e. the same as for the parent.)
        tf.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        tf.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        return tf;
    }

    /**
     * Create a text area, using a resource to specify the tool tip.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the text area
     * </table>
     * In addition, the name of the text area is set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @return the text area that was created
     */
    public JTextArea createTextArea(String uiKey) {
        return createTextArea(uiKey, null);
    }

    /**
     * Create a text area, using a resource to specify the tool tip.  <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the text area
     * </table>
     * In addition, the name of the text area is set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @param label the label that labels this text area.  May be null.
     * @return the text area that was created
     */
    public JTextArea createTextArea(String uiKey, JLabel label) {
        JTextArea t = new JTextArea() {
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(100, 100);
            }
        };
        t.setName(uiKey);

        if (label != null)
            label.setLabelFor(t);
        else
            setAccessibleName(t, uiKey);

        setToolTip(t, uiKey);
        return t;
    }

    //----------------------------------------------------------------------------
    //
    // progress bars

    /**
     * Create a basic progress bar.
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.name <td>accessible name
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the text area
     * </table>
     *
     * @param uiKey the base name of the resource to be used
     * @param orient Value from <code>JProgressBar</code>
     * @return Returns a progress bar component with the specified attributes.
     * @see javax.swing.JProgressBar#VERTICAL
     * @see javax.swing.JProgressBar#HORIZONTAL
     */
    public JProgressBar createProgressBar(String uiKey, int orient) {
        JProgressBar pb = new JProgressBar(orient);
        setToolTip(pb, uiKey);
        setAccessibleName(pb, uiKey);

        return pb;
    }

    /**
     * Create a basic progress bar.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.name <td>accessible name
     * <tr><td><i>uiKey</i>.tip  <td>the tool tip for the text area
     * </table>
     *
     * @param uiKey the base name of the resource to be used
     * @param orient Value from <code>JProgressBar</code>
     * @param model Model to use for the progress bar.
     * @return Returns a progress bar component with the specified attributes.
     * @see javax.swing.JProgressBar#VERTICAL
     * @see javax.swing.JProgressBar#HORIZONTAL
     */
    public JProgressBar createProgressBar(String uiKey, int orient,
                            BoundedRangeModel model) {
        JProgressBar pb = createProgressBar(uiKey, orient);
        pb.setModel(model);

        return pb;
    }

    //----------------------------------------------------------------------------
    //
    // toolbar

    /**
     * Create an empty toolbar.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.name  <td>accessible name
     * <tr><td><i>uiKey</i>.desc  <td>accessible description text
     * </table>
     * @param uiKey Used to obtain accessibility info and name the component
     * @return the tool bar that was created
     */
    public JToolBar createToolBar(String uiKey) {
        JToolBar tb = new JToolBar();
        tb.setName(uiKey);
        setAccessibleInfo(tb, uiKey);

        return tb;
    }

    /**
     * Create a toolbar, using actions to specify the buttons,
     * and using resources to specify the name and mnemonic. <br>
     * The components on the toolbar which are derived from the actions will
     * have their accessible description set to the short description of the
     * action.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.name  <td>accessible name
     * <tr><td><i>uiKey</i>.desc  <td>accessible description text
     * </table>
     * @param uiKey used to obtain accessibility info and name the component
     * @param actions the actions from which to create the buttons;
     *  use null in the array to indicate if and where a separator is required
     * @return the tool bar that was created
     * @see javax.swing.Action#SHORT_DESCRIPTION
     */
    public JToolBar createToolBar(String uiKey, Action[] actions) {
        JToolBar tb = new JToolBar();
        tb.setName(uiKey);
        setAccessibleInfo(tb, uiKey);
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            if (action == null)
                tb.addSeparator();
            else {
                JButton b = tb.add(action);
                b.setName((String) (action.getValue(Action.NAME)));
                b.getAccessibleContext().setAccessibleName(b.getName());
            }
        }
        return tb;
    }

    /**
     * Create a toolbar, using buttons.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.name  <td>accessible name
     * <tr><td><i>uiKey</i>.desc  <td>accessible description text
     * </table>
     * @param uiKey used to obtain accessibility info and name the component
     * @param buttons the buttons to be included in the bar.<br>
     *  use null in the array to indicate if and where a separator is required
     * @return the tool bar that was created
     */
    public JToolBar createToolBar(String uiKey, JButton[] buttons) {
        JToolBar tb = new JToolBar();
        tb.setName(uiKey);
        setAccessibleInfo(tb, uiKey);
        for (int i = 0; i < buttons.length; i++) {
            JButton button = buttons[i];
            if (button == null)
                tb.addSeparator();
            else {
                tb.add(button);
            }
        }
        return tb;
    }

    /**
     * Add a set of actions to an existing toolbar.
     *
     * @param tb The toolbar to modify, must not be null.
     * @param actions the actions from which to create the buttons;
     *        use null in the array to indicate if and where a separator is required
     * @see javax.swing.Action#SHORT_DESCRIPTION
     */
    public void addToolBarActions(JToolBar tb, Action[] actions) {
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            if (action == null)
                tb.addSeparator();
            else {
                JButton b = tb.add(action);
                b.setName((String) (action.getValue(Action.NAME)));
                b.getAccessibleContext().setAccessibleName(b.getName());
            }
        }
    }

    //----------------------------------------------------------------------------
    //
    // blocking confirmation and error dialogs

    /**
     * Show an information dialog, using a resource to specify the error message. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.err  <td>the information message to be displayed
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     */
    public void showInformation(String uiKey) {
        showLocalizedInfo(uiKey, getI18NString(uiKey + ".inf"));
    }

    /**
     * Show an error dialog, using a resource to specify the error message. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.err  <td>the error message to be displayed
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     */
    public void showError(String uiKey) {
        showLocalizedError(uiKey, getI18NString(uiKey + ".err"));
    }

    /**
     * Show an error dialog, using a resource to specify the error message. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.err  <td>the error message to be displayed
     * </table>
     * @param uiKey the base name of the resource to be used
     * @param arg an argument to be formatted into the content using
     * {@link java.text.MessageFormat#format}
     * The method will block until the dialog is dismissed by the user.
     */
    public void showError(String uiKey, Object arg) {
        showLocalizedError(uiKey, getI18NString(uiKey + ".err", arg));
    }

    /**
     * Show an error dialog, using a resource to specify the error message. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.err  <td>the error message to be displayed
     * </table>
     * @param uiKey the base name of the resource to be used
     * @param args an array of arguments to be formatted into the content using
     * {@link java.text.MessageFormat#format}
     * The method will block until the dialog is dismissed by the user.
     */
    public void showError(String uiKey, Object[] args) {
        String msg = getI18NString(uiKey + ".err", args);
        String title = local_i18n.getString("uif.error", ProductInfo.getName());
        JButton okBtn = createOptionButton("uif.ok");
        JTextArea ta = createLocalizedMessageArea(uiKey, msg.trim(), true);
        Dimension d = ta.getMinimumSize();
        Object content = ta;
        // need scrolling ?
        if (d.width > Math.round(6.f * DOTS_PER_INCH) || d.height > Math.round(2.f * DOTS_PER_INCH)) {
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(Math.round(6.f * DOTS_PER_INCH),
                                    Math.round(2.f * DOTS_PER_INCH)));
            content = sp;
        }

        JOptionPane.showOptionDialog(parent,
                                     content,
                                     title,
                                     JOptionPane.DEFAULT_OPTION,
                                     JOptionPane.ERROR_MESSAGE,
                                     null,
                                     new Object[] { okBtn },
                                     null);
    }

    /**
     * Show an error dialog containing stack trace information, using a
     * resource to specify the error message. <br>
     * The resource used is:
     * <table>
     * <tr><td><i>uiKey</i>.err  <td>the error message to be displayed
     * </table>
     * @param uiKey the base name of the resource to be used
     * @param args an array of arguments to be formatted into the content using
     * @param trace an array of arguments containing stack trace information
     * to be added to scrollable pane
     * The method will block until the dialog is dismissed by the user.
     */
    public void showError(String uiKey, Object[] args, Object[] trace) {
        String title = local_i18n.getString("uif.error", ProductInfo.getName());
        JButton okBtn = createOptionButton("uif.ok");
        StringBuffer traceString = new StringBuffer(getI18NString(uiKey + ".err", args));
        traceString.append(":\n");
        for (int i = 0; i < trace.length; i++) {
            traceString.append(trace[i]);
            if (i != (trace.length -1))
                traceString.append("\n\tat ");
        }
        JTextArea ta = createLocalizedMessageArea(uiKey, traceString.toString(), true);
        ta.setLineWrap(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(Math.round(6.f * DOTS_PER_INCH),
                                Math.round(2.f * DOTS_PER_INCH)));

        JOptionPane.showOptionDialog(parent,
                                     sp,
                                     title,
                                     JOptionPane.DEFAULT_OPTION,
                                     JOptionPane.ERROR_MESSAGE,
                                     null,
                                     new Object[] { okBtn },
                                     null);
    }

    /**
     * Show a error dialog to the user, using previously localized (or
     * unlocalized) strings for the message and title.
     * @param title Title string for the dialog.  If null, a generic title
     *              will be used.
     * @param msg Message to show to the user.
     * @see #showError(String)
     * @see #showError(String,Object[])
     * @see #showError(String,Object[],Object[])
     */
    public void showLiteralError(String title, String msg) {
        JButton okBtn = createOptionButton("uif.ok");
        if (title == null)
            title = local_i18n.getString("uif.error", ProductInfo.getName());

        JOptionPane.showOptionDialog(parent,
                                     createLiteralMessageArea(msg),
                                     title,
                                     JOptionPane.DEFAULT_OPTION,
                                     JOptionPane.ERROR_MESSAGE,
                                     null,
                                     new Object[] { okBtn },
                                     null);
    }

    private void showLocalizedError(String uiKey, String text) {
        String title = local_i18n.getString("uif.error", ProductInfo.getName());
        JButton okBtn = createOptionButton("uif.ok");
        JOptionPane.showOptionDialog(parent,
                                     createLocalizedMessageArea(uiKey, text, true),
                                     title,
                                     JOptionPane.DEFAULT_OPTION,
                                     JOptionPane.ERROR_MESSAGE,
                                     null,
                                     new Object[] { okBtn },
                                     null);
    }


    private void showLocalizedInfo(String uiKey, String text) {
        String title = i18n.getString(uiKey + ".title");
        JButton okBtn = createOptionButton("uif.ok");
        JOptionPane.showOptionDialog(parent,
                                     createLocalizedMessageArea(uiKey, text, true),
                                     title,
                                     JOptionPane.DEFAULT_OPTION,
                                     JOptionPane.INFORMATION_MESSAGE,
                                     null,
                                     new Object[] { okBtn },
                                     null);
    }

    /**
     * Show a confirmation dialog with OK and Cancel buttons,
     * using a resource to specify the message and title. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     * @return an integer signifying how the dialog was dismissed
     * @see JOptionPane#OK_OPTION
     * @see JOptionPane#CANCEL_OPTION
     */
    public int showOKCancelDialog(String uiKey) {
        return showLocalizedOKCancelDialog(uiKey, getI18NString(uiKey + ".txt"));
    }

    /**
     * Show a confirmation dialog with OK and Cancel buttons,
     * using a resource to specify the message and title. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     * @param arg an argument to be formatted into the content using
     * {@link java.text.MessageFormat#format}
     * @return an integer signifying how the dialog was dismissed
     * @see JOptionPane#OK_OPTION
     * @see JOptionPane#CANCEL_OPTION
     */
    public int showOKCancelDialog(String uiKey, Object arg) {
        return showLocalizedOKCancelDialog(uiKey, getI18NString(uiKey + ".txt", arg));
    }

    /**
     * Show a confirmation dialog with OK and Cancel buttons,
     * using a resource to specify the message and title. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     * @return an integer signifying how the dialog was dismissed
     * @param args an array of arguments to be formatted into the content using
     * {@link java.text.MessageFormat#format}
     * @see JOptionPane#OK_OPTION
     * @see JOptionPane#CANCEL_OPTION
     */
    public int showOKCancelDialog(String uiKey, Object[] args) {
        return showLocalizedOKCancelDialog(uiKey, getI18NString(uiKey + ".txt", args));
    }

    private int showLocalizedOKCancelDialog(String uiKey, String text) {
        JTextArea msg = createLocalizedMessageArea(uiKey, text, true);
        String title = getI18NString(uiKey + ".title");
        JButton okBtn = createOptionButton("uif.ok");
        JButton cancelBtn = createOptionButton("uif.cancel");
        int rc = JOptionPane.showOptionDialog(parent,
                                              msg,
                                              title,
                                              JOptionPane.OK_CANCEL_OPTION,
                                              JOptionPane.QUESTION_MESSAGE,
                                              null,
                                              new Object[] { okBtn, cancelBtn },
                                              null);
        return (rc == 0 ? JOptionPane.OK_OPTION /*0*/
                : rc == 1 ? JOptionPane.CANCEL_OPTION /*2*/
                : rc);
    }

    /**
     * Show a confirmation dialog with Yes and No buttons,
     * using a resource to specify the message and title. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     * @return an integer signifying how the dialog was dismissed
     * @see JOptionPane#YES_OPTION
     * @see JOptionPane#NO_OPTION
     */
    public int showYesNoDialog(String uiKey) {
        return showLocalizedYesNoDialog(uiKey, getI18NString(uiKey + ".txt"));
    }

    /**
     * Show a confirmation dialog with Yes and No buttons,
     * using a resource to specify the message and title. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     * @param arg an argument to be formatted into the content using
     * {@link java.text.MessageFormat#format}
     * @return an integer signifying how the dialog was dismissed
     * @see JOptionPane#YES_OPTION
     * @see JOptionPane#NO_OPTION
     */
    public int showYesNoDialog(String uiKey, Object arg) {
        return showLocalizedYesNoDialog(uiKey, getI18NString(uiKey + ".txt", arg));
    }

    /**
     * Show a confirmation dialog with Yes and No buttons,
     * using a resource to specify the title and component for the message.<br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     * @param msg the GUI component to be used as the dialogs message payload
     * @return an integer signifying how the dialog was dismissed
     * @see JOptionPane#YES_OPTION
     * @see JOptionPane#NO_OPTION
     */
    public int showCustomYesNoDialog(String uiKey, Component msg) {
        return showComponentYesNoDialog(uiKey, msg);
    }

    /**
     * Show a confirmation dialog with Yes and No buttons,
     * using a resource to specify the message and title. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     * @return an integer signifying how the dialog was dismissed
     * @param args an array of arguments to be formatted into the content using
     * {@link java.text.MessageFormat#format}
     * @see JOptionPane#YES_OPTION
     * @see JOptionPane#NO_OPTION
     */
    public int showYesNoDialog(String uiKey, Object[] args) {
        return showLocalizedYesNoDialog(uiKey, getI18NString(uiKey + ".txt", args));
    }

    private int showLocalizedYesNoDialog(String uiKey, String text) {
        JTextArea msg = createLocalizedMessageArea(uiKey, text, true);
        return showComponentYesNoDialog(uiKey, msg);
    }

    /**
     * Show a Yes/No dialog with the given text and title.
     * Use this with care and only when really really needed.
     */
    int showLiteralYesNoDialog(String title, String text) {
        // warning, this only works because createLocalizedMessageArea
        // does not use the uikey for anything except the component name
        JTextArea msg = createLocalizedMessageArea("literal", text, true);

        // update showComponentYesNoDialog if you change this!
        JButton yesBtn = createOptionButton("uif.yes");
        JButton noBtn = createOptionButton("uif.no");
        return JOptionPane.showOptionDialog(parent,
                                            msg,
                                            title,
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.QUESTION_MESSAGE,
                                            null,
                                            new Object[] { yesBtn, noBtn },
                                            null);
    }

    private int showComponentYesNoDialog(String uiKey, Component msg) {
        // update showLiteralYesNoDialog if you change this!
        String title = getI18NString(uiKey + ".title");
        JButton yesBtn = createOptionButton("uif.yes");
        JButton noBtn = createOptionButton("uif.no");
        return JOptionPane.showOptionDialog(parent,
                                            msg,
                                            title,
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.QUESTION_MESSAGE,
                                            null,
                                            new Object[] { yesBtn, noBtn },
                                            null);
    }

    /**
     * Show a confirmation dialog with Yes, No and Cancel buttons,
     * using a resource to specify the message and title. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     * @return an integer signifying how the dialog was dismissed
     * @see JOptionPane#YES_OPTION
     * @see JOptionPane#NO_OPTION
     * @see JOptionPane#CANCEL_OPTION
     */
    public int showYesNoCancelDialog(String uiKey) {
        return showLocalizedYesNoCancelDialog(uiKey, getI18NString(uiKey + ".txt"));
    }

    /**
     * Show a confirmation dialog with Yes and No buttons,
     * using a resource to specify the message and title. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     * @param arg an argument to be formatted into the content using
     * {@link java.text.MessageFormat#format}
     * @return an integer signifying how the dialog was dismissed
     * @see JOptionPane#YES_OPTION
     * @see JOptionPane#NO_OPTION
     * @see JOptionPane#CANCEL_OPTION
     */
    public int showYesNoCancelDialog(String uiKey, Object arg) {
        return showLocalizedYesNoCancelDialog(uiKey, getI18NString(uiKey + ".txt", arg));
    }

    /**
     * Show a confirmation dialog with Yes and No buttons,
     * using a resource to specify the message and title. <br>
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * The method will block until the dialog is dismissed by the user.
     * @param uiKey the base name of the resource to be used
     * @return an integer signifying how the dialog was dismissed
     * @param args an array of arguments to be formatted into the content using
     * {@link java.text.MessageFormat#format}
     * @see JOptionPane#YES_OPTION
     * @see JOptionPane#NO_OPTION
     * @see JOptionPane#CANCEL_OPTION
     */
    public int showYesNoCancelDialog(String uiKey, Object[] args) {
        return showLocalizedYesNoCancelDialog(uiKey, getI18NString(uiKey + ".txt", args));
    }

    private int showLocalizedYesNoCancelDialog(String uiKey, String text) {
        JTextArea msg = createLocalizedMessageArea(uiKey, text, true);
        String title = getI18NString(uiKey + ".title");
        JButton yesBtn = createOptionButton("uif.yes");
        JButton noBtn = createOptionButton("uif.no");
        JButton cancelBtn = createOptionButton("uif.cancel");
        return JOptionPane.showOptionDialog(parent,
                                            msg,
                                            title,
                                            JOptionPane.YES_NO_CANCEL_OPTION,
                                            JOptionPane.QUESTION_MESSAGE,
                                            null,
                                            new Object[] { yesBtn, noBtn, cancelBtn },
                                            null);
    }

    /**
     * Show a message only dialog, no user feedback.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * @param uiKey the base name of the resource to be used
     * @param args any arguments to be used to create the message
     */
    public void showInformationDialog(String uiKey, Object[] args) {
        showLocalizedInformationDialog(uiKey,
            getI18NString(uiKey + ".title"),
            getI18NString(uiKey + ".txt", args), parent);
    }

    public void showInformationDialog(String uiKey, Object[] args, Component parent) {
        showLocalizedInformationDialog(uiKey,
            getI18NString(uiKey + ".title"),
            getI18NString(uiKey + ".txt", args), parent);
    }


    private void showLocalizedInformationDialog(String uiKey, String title,
                                                String text, Component localParent) {
        JTextArea msg = createLocalizedMessageArea(uiKey, text, true);
        Dimension d = msg.getMinimumSize();
        Object content = msg;
        // need scrolling ?
        if (d.width > Math.round(6.f * DOTS_PER_INCH) || d.height > Math.round(2.f * DOTS_PER_INCH)) {
            JScrollPane sp = new JScrollPane(msg);
            sp.setPreferredSize(new Dimension(Math.round(6.f * DOTS_PER_INCH),
                                    Math.round(2.f * DOTS_PER_INCH)));
            content = sp;
        }

        JOptionPane.showMessageDialog(localParent,
                                        content,
                                        title,
                                        JOptionPane.INFORMATION_MESSAGE,
                                        null);
    }

    //----------------------------------------------------------------------------
    //
    // don't show this again message box

    /**
     * Show a dialog which provides the user with an informational message.
     *
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * @param uiKey the base name of the resource to be used
     * @param msg the body of the dialog, which should have already been localized
     */
    public void showCustomInfoDialog(String uiKey, Object msg) {
        JOptionPane.showMessageDialog(parent,
                                        msg,
                                        getI18NString(uiKey + ".title"),
                                        JOptionPane.INFORMATION_MESSAGE,
                                        null);
    }

    //----------------------------------------------------------------------------
    //
    // panels

    /**
     * Create a horizontal placeholder "box". <br>
     * The name of this new box component will be set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @return A Box component
     * @see javax.swing.Box
     */
    public Box createHorizontalBox(String uiKey) {
        Box box = Box.createHorizontalBox();
        box.setName(uiKey);
        box.setFocusable(false);
        return box;
    }

    /**
     * Create an empty panel.  <br>
     * In the J2SE 1.4 and greater world, panels are focusable by default,
     * so this panel will be focusable.  Because of this, accessibility
     * information must be set, therefore the following resources are
     * required from the resource bundle:
     * <table>
     * <tr><td><i>uiKey</i>.name  <td>the accessible name of the panel
     * <tr><td><i>uiKey</i>.desc <td>accessible description text
     * </table>
     * The name of this new component will be set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @return An empty panel component
     */
    public JPanel createPanel(String uiKey) {
        return createPanel(uiKey, true);
    }

    /**
     * Create an empty panel.  <br>
     * In the J2SE 1.4 and greater world, panels are focusable by default,
     * so this panel will be focusable.  Use this method to control
     * whether or not the panel remains focusable.  If you choose 'true',
     * the following must be provided in the resource bundle:
     * <table>
     * <tr><td><i>uiKey</i>.name  <td>the accessible name of the panel
     * <tr><td><i>uiKey</i>.desc <td>accessible description text
     * </table>
     * The name of this new component will be set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @param focusable If true, the panel will accept focus in the GUI.
     *        If false it will not.  Note that if it is focusable, you need to
     *        provide accessibility text.
     * @return An empty panel component
     */
    public JPanel createPanel(String uiKey, boolean focusable) {
        JPanel p = new JPanel();
        initPanel(p, uiKey, focusable);
        return p;
    }

    /**
     * Create an empty panel with a specific layout manager.  <br>
     * In the J2SE 1.4 and greater world, panels are focusable by default,
     * so this panel will be focusable.  Because of this, accessibility
     * information must be set, therefore the following resources are
     * required from the resource bundle:
     * <table>
     * <tr><td><i>uiKey</i>.name  <td>the accessible name of the panel
     * <tr><td><i>uiKey</i>.desc <td>accessible description text
     * </table>
     * The name of this new component will be set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @param layout the layout manager instance to use in this panel
     * @return An empty panel component
     */
    public JPanel createPanel(String uiKey, LayoutManager layout) {
        return createPanel(uiKey, layout, true);
    }

    /**
     * Create an empty panel with a specific layout manager.  <br>
     * In the J2SE 1.4 and greater world, panels are focusable by default,
     * so this panel will be focusable.  Use this method to control
     * whether or not the panel remains focusable.  If you choose 'true',
     * the following must be provided in the resource bundle:
     * <table>
     * <tr><td><i>uiKey</i>.name <td>the accessible name of the panel
     * <tr><td><i>uiKey</i>.desc <td>accessible description text
     * </table>
     * The name of this new component will be set to <i>uiKey</i>.
     * @param uiKey the base name of the resource to be used
     * @param layout the layout manager instance to use in this panel
     * @param focusable If true, the panel will accept focus in the GUI.
     *        If false it will not.  Note that if it is focusable, you need to
     *        provide accessibility text.
     * @return An empty panel component
     */
    public JPanel createPanel(String uiKey, LayoutManager layout, boolean focusable) {
        JPanel p = new JPanel();
        initPanel(p, uiKey, layout, focusable);
        return p;
    }

    /**
     * Set properties on an existing panel.
     * @param p the panel to modify
     * @param uiKey the base name of the resource to be used
     * @param focusable If true, the panel will accept focus in the GUI.
     *        If false it will not.  Note that if it is focusable, you need to
     *        provide accessibility text.
     */
    public void initPanel(JPanel p, String uiKey, boolean focusable) {
        p.setName(uiKey);
        if (focusable)
            setAccessibleInfo(p, uiKey);
        else
            p.setFocusable(false);
    }

    /**
     * Set properties on an existing panel, including the layout manager.
     * @param p the panel to modify
     * @param uiKey the base name of the resource to be used
     * @param layout the layout manager instance that this panel should use
     * @param focusable If true, the panel will accept focus in the GUI.
     *        If false it will not.  Note that if it is focusable, you need to
     *        provide accessibility text.
     */
    public void initPanel(JPanel p, String uiKey, LayoutManager layout, boolean focusable) {
        initPanel(p, uiKey, focusable);
        p.setLayout(layout);

    }

    //----------------------------------------------------------------------------
    //
    // dialogs

    /**
     * Create an empty dialog. <br>
     * See <code>initDialog(JDialog,String)</code> for required resources.
     * @param uiKey the base name of the resource to be used
     * @param parent the parent component of this dialog
     * @return an empty dialog component
     * @see #initDialog
     */
    public JDialog createDialog(String uiKey, Component parent) {
        JFrame owner = (JFrame) (SwingUtilities.getAncestorOfClass(JFrame.class, parent));
        return createDialog(uiKey, owner);
    }

    /**
     * Create an empty dialog. <br>
     * See <code>initDialog(JDialog,String)</code> for required resources.
     * @param uiKey the base name of the resource to be used
     * @param owner the parent frame of this dialog
     * @return an empty dialog component
     * @see #initDialog
     */
    public JDialog createDialog(String uiKey, JFrame owner) {
        JDialog d = new JDialog(owner);
        initDialog(d, uiKey);
        return d;
    }

    /**
     * Create an empty dialog. <br>
     * See <code>initDialog(JDialog,String)</code> for required resources.
     * @param uiKey the base name of the resource to be used
     * @param owner the parent frame of this dialog. If owner is null - icon is set to the dialog
     * @param title the localized title of this new dialog
     * @param content the content to go into the dialog
     * @return an dialog component with the given content component and title
     * @see #initDialog
     */
    public JDialog createDialog(String uiKey, JFrame owner, String title, Container content) {
        return createDialog(uiKey, owner, title, content, Dialog.ModalityType.MODELESS);
    }

    /**
     * Create an empty dialog. <br>
     * See <code>initDialog(JDialog,String)</code> for required resources.
     * @param uiKey the base name of the resource to be used
     * @param owner the parent frame of this dialog. If owner is null - icon is set to the dialog
     * @param title the localized title of this new dialog
     * @param content the content to go into the dialog
     * @param type specifies whether dialog blocks input to other windows when shown.
     * null value and unsupported modality types are equivalent to MODELESS
     * @return an dialog component with the given content component and title
     * @see #initDialog
     */
    public JDialog createDialog(String uiKey, JFrame owner, String title, Container content, Dialog.ModalityType type) {
        // can't use constructor JDialog(Window, String, Dialog.ModalityType) -
        // it has different behavior from JDialog(Frame, String, boolean)
        JDialog d = new JDialog(owner, title, false);
        d.setModalityType(type);
        if (owner == null) {
            d.setIconImage(createImage("images/jticon.gif"));
        }
        initDialog(d, uiKey);
        d.setContentPane(content);
        return d;
    }

    /**
     * Create an empty frame. Unlike to dialog <code>createDialog(String uiKey,
     * JFrame owner, String title, Container content)</code> it can't be modal,
     * it's always free-floating and it has minimize and maximize buttons <br/>
     * See <code>initFrame(JFrame,String)</code> for required resources.
     * @param uiKey the base name of the resource to be used
     * @param title the localized title of this new frame
     * @param content the content to go into the frame
     * @return a frame component with the given content component and title
     * @see #initFrame
     */
    public JFrame createFrame(String uiKey, String title, Container content) {
        JFrame frame = new JFrame(title);
        initFrame(frame, uiKey);
        frame.setContentPane(content);
        return frame;
    }

    /**
     * Create a dialog which will ask the user to wait.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * </table>
     * @param uiKey The prefix to retrieve strings to be displayed.
     * @param parent The parent component of this new dialog.
     * @return a dialog appropriate for asking the user to wait
     */
    public JDialog createWaitDialog(String uiKey, Component parent) {
        JFrame owner = (JFrame) (SwingUtilities.getAncestorOfClass(JFrame.class, parent));
        return createWaitDialog(uiKey, owner);
    }

    /**
     * Create a dialog which will ask the user to wait.
     * The resources used are:
     * <table>
     * <tr><td><i>uiKey</i>.txt  <td>the message to be displayed
     * <tr><td><i>uiKey</i>.title  <td>the title for the dialog
     * <tr><td><i>uiKey</i>.desc <td>accessible description of the dialog
     * <tr><td><i>uiKey</i>.name <td>accessible name of the dialog
     * </table>
     * @param uiKey The prefix to retrieve strings to be displayed.
     * @param owner The frame which will own this new dialog.
     * @return a dialog appropriate for asking the user to wait
     */
    public JDialog createWaitDialog(String uiKey, JFrame owner) {
        final int msgWidth = 50;
        JDialog d = new JDialog(owner);
        initDialog(d, uiKey);
        d.setTitle(getI18NString(uiKey + ".title"));

        JProgressBar pb = new JProgressBar(SwingConstants.HORIZONTAL);
        pb.setName(uiKey);
        pb.setIndeterminate(true);
        pb.setBorderPainted(true);
        pb.setPreferredSize(new Dimension(Math.round(2.5f * DOTS_PER_INCH),
                                15));

        JPanel body = createPanel(uiKey, new GridBagLayout(), false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets.left = 12;   // JL&F spacing
        gbc.insets.right = 12;  // JL&F spacing
        gbc.insets.top = 12;    // JL&F spacing

        gbc.gridy = 0;
        gbc.weightx = 0;

        JTextArea msg = createLocalizedMessageArea(uiKey,
                                    getI18NString(uiKey + ".txt"),
                                    false);
        // uif sets the size, but too large for this dialog
        msg.setSize(new Dimension(Math.round(4.0f * DOTS_PER_INCH),
                        Integer.MAX_VALUE));
        body.add(msg, gbc);

        // add progress bar
        gbc.gridy = 1;
        gbc.insets.top = 11;    // JL&F spacing
        gbc.insets.bottom = 12; // JL&F spacing
        body.add(pb, gbc);

        d.setContentPane(body);
        d.pack();

        d.setLocationRelativeTo(owner);
        return d;
    }

    /**
     * Configure a dialog with accessibility information.
     * <table>
     * <tr><td><i>uiKey</i>.desc <td>accessible description of the dialog
     * <tr><td><i>uiKey</i>.name <td>accessible name of the dialog
     * <tr><td><i>uiKey</i>.root <td>component name for the root pane of the
     *          dialog
     * </table>
     * @param d the dialog to upgrade
     * @param uiKey Key to retrieve the new properties with
     */
    public void initDialog(JDialog d, String uiKey) {
        d.setName(uiKey);
        setAccessibleInfo(d, uiKey);
        d.setLocationRelativeTo(d.getParent());

        JRootPane root = d.getRootPane();
        root.setName(uiKey + ".root");
        AccessibleContext ac = d.getAccessibleContext();
        AccessibleContext r_ac = root.getAccessibleContext();
        r_ac.setAccessibleName(ac.getAccessibleName());
        r_ac.setAccessibleDescription(ac.getAccessibleDescription());
    }

    /**
     * Configure a frame with accessibility information and an icon.
     * <table>
     * <tr><td><i>uiKey</i>.desc <td>accessible description of the frame
     * <tr><td><i>uiKey</i>.name <td>accessible name of the frame
     * <tr><td><i>uiKey</i>.root <td>component name for the root pane of the
     *          frame
     * </table>
     * @param d the frame to upgrade
     * @param uiKey Key to retrieve the new properties with
     */
    public void initFrame(JFrame d, String uiKey) {
        d.setName(uiKey);
        setAccessibleInfo(d, uiKey);
        d.setLocationRelativeTo(d.getParent());

        d.setIconImage(createImage("images/jticon.gif"));

        JRootPane root = d.getRootPane();
        root.setName(uiKey + ".root");
        AccessibleContext ac = d.getAccessibleContext();
        AccessibleContext r_ac = root.getAccessibleContext();
        r_ac.setAccessibleName(ac.getAccessibleName());
        r_ac.setAccessibleDescription(ac.getAccessibleDescription());
    }


    //----------------------------------------------------------------------------

    /**
     * Dispose of any owned resources.
     */
    public void dispose() {
        clientClass = null;
        parent = null;
    }

    //----------------------------------------------------------------------------

    private static Font baseFont = new JLabel().getFont();

    private static final ActionListener closeListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Component src = (Component) (e.getSource());
                for (Container p = src.getParent(); p != null; p = p.getParent()) {
                    if (p instanceof JInternalFrame || p instanceof Window) {
                        p.setVisible(false);
                        return;
                    }
                }
            }
        };

    private Class clientClass;
    private Component parent;
    private I18NResourceBundle i18n;
    private HelpBroker helpBroker;

    private static I18NResourceBundle local_i18n = I18NResourceBundle.getBundleForClass(UIFactory.class);
    private static final int DOTS_PER_INCH = Toolkit.getDefaultToolkit().getScreenResolution();

    /**
     * Extension to the UIFactory that allows to use more than one resource
     * bundle. All methods accessing the resource bundle are overridden to
     * search for a resource in the alternative bundle first, and, if not found,
     * look up it in the original one.
     * <b>
     * This class might be helpful, when a component extends another components
     * from a different package.
     */
    public static class UIFactoryExt extends UIFactory {
        private I18NResourceBundle i18n_alt;
        private Class altClass;

        public UIFactoryExt(UIFactory uif, Class altClass) {
            super(uif.clientClass, uif.parent, uif.helpBroker);
            i18n_alt = I18NResourceBundle.getBundleForClass(altClass);
            this.altClass = altClass;
        }

        @Override
        public Color getI18NColor(String key) {
            if (!hasKey(i18n_alt,key)) {
                return super.getI18NColor(key);
            }
            String value = i18n_alt.getString(key + ".clr");
            try {
                if (value != null)
                    return Color.decode(value);
            }
            catch (Exception e) {
                // ignore
            }
            return Color.BLACK;

        }

        @Override
        public String getI18NString(String key) {
            if (hasKey(i18n_alt,key)) {
                return i18n_alt.getString(key);
            } else {
                return super.getI18NString(key);
            }
        }

        @Override
        public String getI18NString(String key, Object arg) {
            if (hasKey(i18n_alt,key)) {
                return i18n_alt.getString(key, arg);
            } else {
                return super.getI18NString(key, arg);
            }
        }

        @Override
        public String getI18NString(String key, Object[] args) {
            if (hasKey(i18n_alt,key)) {
                return i18n_alt.getString(key, args);
            } else {
                return super.getI18NString(key, args);
            }
        }

        @Override
        public URL getIconURL(String uiKey) {
            String r = getI18NString(uiKey + ".icon");

            URL url = altClass.getResource(r);

            if (url == null)
                url = super.getIconURL(uiKey);

            return url;
        }

        /**
         * It would be much better to use containsKey() instead, but
         * it's available since 1.6
         */
        static boolean hasKey(ResourceBundle rb, String key) {
            Enumeration<String> keys = rb.getKeys();
            if (keys == null || key == null) {
                return false;
            }
            while(keys.hasMoreElements()) {
                if (key.equals(keys.nextElement())) {
                    return true;
                }
            }
            return false;
        }
    }
}
