/*
 * $Id$
 *
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * Lightweight wrapper class to provide standard support for tool dialogs.
 * Dialogs have a title, optional menu bar, a body, and an optional set of
 * buttons, including a default button.
 * The actual dialog displayed may be a JDialog, or a JInternalFrame,
 * depending on the current desktop.
 */
abstract public class ToolDialog
{
    /**
     * Create standard ToolDialog.
     * @param parent The parent component of this dialog.
     * @param uif Factory instance associated with this dialog
     * @param uiKey Key to use to get strings and other properties for this
     *        dialog.
     */
    protected ToolDialog(Component parent, UIFactory uif, String uiKey) {
                this(parent, uif, uiKey, DIALOG);
    }

    /**
     * Create a ToolDialog with specified type.
     * @param parent The parent component of this dialog.
     * @param uif Factory instance associated with this dialog
     * @param uiKey Key to use to get strings and other properties for this
     *        dialog.
     * @param type dialog type mask.
     * @see #DIALOG
     * @see #FRAME
     * @see #FREE
     * @see #MODAL
     */
        protected ToolDialog(Component parent, UIFactory uif, String uiKey, int type) {
        if (parent == null || uif == null || uiKey == null)
            throw new NullPointerException();

        this.parent = parent;
        this.uif = uif;
        this.uiKey = uiKey;
        this.type = type;

        setTool(parent);
    }

    public void setTool(Component parent) {
        Tool t = (Tool) (parent instanceof Tool ? parent
               : (SwingUtilities.getAncestorOfClass(Tool.class, parent)));

        if (t != null) {
            setTool(t);
        }
    }


   protected void setTool(Tool t) throws IllegalStateException {

       if (t == null || tool != null) {
            throw new IllegalStateException();
       }
       tool = t;
       tool.addToolDialog(this);

       if (defaultDisposeHandler != null) {
           tool.removeObserver(defaultDisposeHandler);
       }

        defaultDisposeHandler = new Tool.Observer() {

           public void titleChanged(Tool source, String newValue) {
           }

           public void shortTitleChanged(Tool source, String newValue) {
           }

            public void toolDisposed(Tool source) {
                closing();
            }
        };
        tool.addObserver(defaultDisposeHandler);

    }


    /**
     * Get the tool for which this is a dialog.
     * @return the tool for which this is a dialog
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * Check if the dialog is currently showing on the screen.
     * @return True if this dialog is currently showing, false otherwise.
     * @see #isVisible
     * @see #setVisible
     */
    public boolean isShowing() {
        return (dialog != null && dialog.isShowing());
    }

    /**
     * Check if the dialog is currently showing on the screen.
     * @return True if this dialog is currently visible, false otherwise.
     * @see #setVisible
     */
    public boolean isVisible() {
        return (dialog != null && dialog.isVisible());
    }

    /**
     * packs the dialog. The method is useful when it's necessary to call pack
     * separately from <code>setVisible(boolean)</code>
     * @see #setVisible
     */
    public void pack() {
        if (dialog == null)
            ensureDialogInitialized();

        if (dialog instanceof Window) // JDialog and JFrame
            ((Window) dialog).pack();
        else
                ((JInternalFrame) dialog).pack();
    }

    /**
     * Specify whether the dialog should be made visible on the screen or not.
     * @param b True if the dialog should be made visible, false if is should
     *        be made invisible.
     * @see #isVisible
     */
    public void setVisible(boolean b) {
        if (b) {
            // setting dialog visible
            ensureDialogInitialized();
            restorer.restore();
            if (dialog instanceof Window) {
                ((Window) dialog).toFront();
                ((Window) dialog).pack();
            } else {
                ((JInternalFrame) dialog).toFront();
                ((JInternalFrame) dialog).pack();
            }

            dialog.setVisible(true);
        }
        else {
            // setting dialog invisible
            if (dialog != null) {
                restorer.save();
                dialog.setVisible(false);
            }
        }
    }

    /**
     * Dispose of any window system resources used by the dialog.
     * The client-supplied components (menu bar, body, buttons) are not
     * disposed, meaning the dialog can be made visible again, if desired.
     * To dispose the client components, subtype this method.
     */
    public void dispose() {
        if (dialog != null) {
            restorer.save();
            if (dialog instanceof Window)
                ((Window) dialog).dispose();
            else
                ((JInternalFrame) dialog).dispose();

            RootPaneContainer rpc = (RootPaneContainer) dialog;
            rpc.setContentPane(new Container());
            JMenuBar mb = rpc.getRootPane().getJMenuBar();
            if (mb != null)
                mb.removeAll();

            if (componentListener != null && dialog != null) {
                dialog.removeComponentListener(componentListener);
            }

            dialog = null;
        }
    }

    /**
     * Get a parent component for a dialog to use.
     * @return a component which can be used as a parent (JDialog or Frame), or null if none
     *         is available.
     */
    public Container getDialogParent() {
        if (dialog instanceof Window) {
            return dialog;
        }
        return (Frame) (SwingUtilities.getAncestorOfClass(Frame.class, dialog));
    }

    /**
     * Initialize the GUI, by calling the various setXXX methods.
     */
    protected abstract void initGUI();

    /**
     * Get the title for the dialog.
     * @return The title string for this dialog (localized)
     * @see #setI18NTitle
     */
    protected String getTitle() {
        return title;
    }

    /**
     * Specify the title for the dialog.
     * @param key key to use to retrieve the dialogs title
     * @see #getTitle
     */
    protected void setI18NTitle(String key) {
        setLocalizedTitle(uif.getI18NString(key));
    }

    /**
     * Specify the title for the dialog.
     * @param key key to use to retrieve the dialogs title
     * @param arg item to substitute into the title from the resource bundle
     * @see #getTitle
     */
    protected void setI18NTitle(String key, Object arg) {
        setLocalizedTitle(uif.getI18NString(key, arg));
    }

    /**
     * Specify the title for the dialog.
     * @param key key to use to retrieve the dialogs title
     * @param args items to substitute into the title from the resource bundle
     * @see #getTitle
     */
    protected void setI18NTitle(String key, Object[] args) {
        setLocalizedTitle(uif.getI18NString(key, args));
    }

    private void setContentPane() {
        if (dialog != null) {
            initMain();
            if (dialog instanceof JDialog)
                ((JDialog) dialog).setContentPane(main);
            else if (dialog instanceof JFrame)
                ((JFrame) dialog).setContentPane(main);
            else
                ((JInternalFrame) dialog).setContentPane(main);
        }
    }

    private void setLocalizedTitle(String title) {
        if (dialog != null)  {
            if (dialog instanceof JDialog)
                ((JDialog) dialog).setTitle(title);
            else if (dialog instanceof JFrame)
                ((JFrame) dialog).setTitle(title);
            else
                ((JInternalFrame) dialog).setTitle(title);
        }

        this.title = title;
    }

    /**
     * Set the help ID for the dialog.
     * This must be called before the dialog is made visible.
     * @param helpID the help identifier for this dialog
     */
    protected void setHelp(String helpID) {
        if (dialog != null)
            uif.setHelp(dialog, helpID);

        this.helpID = helpID;
    }

    /**
     * Get the menu bar for the dialog.
     * @return the menu bar being used for this dialog, may be null
     * @see #setJMenuBar
     */
    protected JMenuBar getJMenuBar() {
        return menuBar;
    }

    /**
     * Set the menu bar for the dialog.
     * @param menuBar The menu bar for this dialog; should not be null.
     * @see #getJMenuBar
     */
    protected void setJMenuBar(JMenuBar menuBar) {
        if (dialog != null)  {
            if (dialog instanceof JDialog)
                ((JDialog) dialog).setJMenuBar(menuBar);
            else if (dialog instanceof JFrame)
                ((JFrame) dialog).setJMenuBar(menuBar);
            else
                ((JInternalFrame) dialog).setJMenuBar(menuBar);
        }

        this.menuBar = menuBar;
    }

    /**
     * Get the component for the main body of the dialog.
     * @return the body container for this dialog
     * @see #setBody
     */
    protected Container getBody() {
        return body;
    }

    /**
     * Set the component for the main body of the dialog.
     * This should not include the button bar, which should be set separately.
     * This method must be called before the dialog is made visible.
     * @param body the body container that should be used by this dialog
     * @see #getBody
     */
    protected void setBody(Container body) {
        this.body = body;

        setContentPane();
    }

    /**
     * Get the buttons from the button bar at the bottom of the dialog.
     * @return array of buttons currently used in this dialog
     * @see #setButtons
     */
    protected JButton[] getButtons() {
        return buttons;
    }

    /**
     * Get the default button from the button bar at the bottom of the dialog.
     * @return the button currently set to be the default
     * @see #setButtons
     */
    protected JButton getDefaultButton() {
        return defaultButton;
    }

    /**
     * Set the buttons to appear in a button bar at the bottom of the dialog.
     * A default button can also be specified.
     * The default button gets the focus when the dialog is initially activated,
     * and will be activated if the user clicks "Enter".
     * The default button should not normally have a mnemonic, per JL&amp;F.
     * @param buttons buttons to use
     * @param defaultButton button from the previous argument which should be
     *        used as the default
     * @see #getButtons
     * @see #getDefaultButton
     */
    protected void setButtons(JButton[] buttons, JButton defaultButton) {
        this.buttons = buttons;
        this.defaultButton = defaultButton;

        cancelButton = null;
        if (buttons != null) {
            for (int i = 0; i < buttons.length && cancelButton == null; i++) {
                if (buttons[i].getActionCommand().equals(UIFactory.CANCEL))
                    cancelButton = buttons[i];
            }
        }

        setContentPane();
    }

    /**
     * Set a ComponentListener to be registered on the dialog.
     * This is useful for listening for events when the dialog is made visible and
     * invisible.
     * @param l listener to attach to this component
     */
    protected void setComponentListener(ComponentListener l) {
        componentListener = l;
    }

    /**
     * Get the size of the dialog. An exception will be thrown if the dialog has not
     * yet been shown, or if it is has been disposed since it was shown on the screen.
     * @return the current size of this dialog
     * @see #setSize
     */
    protected Dimension getSize() {
        if (dialog == null)
            throw new IllegalStateException();
        return dialog.getSize();
    }

    /**
     * Set the size of the dialog. An exception will be thrown if the dialog has not
     * yet been shown, or if it is has been disposed since it was shown on the screen.
     * @param d the new size of this dialog
     * @see #getSize
     */
    protected void setSize(Dimension d) {
        if (dialog == null)
            throw new IllegalStateException();

        dialog.setSize(d);
    }

    /**
     * Set the size of the dialog. An exception will be thrown if the dialog has not
     * yet been shown, or if it is has been disposed since it was shown on the screen.
     * @param width the new width of this dialog
     * @param height the new height of this dialog
     * @see #getSize
     */
    protected void setSize(int width, int height) {
        if (dialog == null)
            ensureDialogInitialized();

        dialog.setSize(width, height);
    }


    /**
     * Get the location of the dialog. An exception will be thrown if the dialog has not
     * yet been shown, or if it is has been disposed since it was shown on the screen.
     * @return the current location of this dialog
     * @see #setLocation
     */
    protected Point getLocation() {
        if (dialog == null)
            throw new IllegalStateException();
        return dialog.getLocation();
    }

    /**
     * Set the location of the dialog. An exception will be thrown if the dialog has not
     * yet been shown, or if it is has been disposed since it was shown on the screen.
     * @param p the new location of this dialog
     * @see #getLocation
     */
    protected void setLocation(Point p) {
        if (dialog == null)
            ensureDialogInitialized();

        dialog.setLocation(p);
    }

    /**
     * Set the location of the dialog. An exception will be thrown if the dialog has not
     * yet been shown, or if it is has been disposed since it was shown on the screen.
     * @param x - the x-coordinate of the new location's top-left corner in the parent's coordinate space
     * @param y - the y-coordinate of the new location's top-left corner in the parent's coordinate space
     * @see #getLocation
     */
    protected void setLocation(int x, int y) {
        if (dialog == null)
            throw new IllegalStateException();

        dialog.setLocation(x, y);
    }

    /**
     * Ensure the dialog has been initialized.  If the desktop style has been changed
     * since the dialog was last made visible, the dialog will be reinitialized.
     */
    private void ensureDialogInitialized() {
        Desktop desktop = tool.getDesktop();

        if (dialog == null || !desktop.isToolOwnerForDialog(tool, dialog)) {
            if (main == null)
                initMain();
            initDialog();
        }
    }

    void initDialog() {
        initDialog(null, isVisible());
    }

    void initDialog(DeskView view, boolean visible) {
        if (view == null) {
            Desktop desktop = tool.getDesktop();
            dialog = desktop.createDialog(tool, uiKey, title, menuBar, main,
                                      (dialog == null ? null : dialog.getBounds()), type);
        }
        else {
            dialog = view.createDialog(tool, uiKey, title, menuBar, main,
                                      (dialog == null ? null : dialog.getBounds()), type);
        }


        if (helpID != null)
            uif.setHelp(dialog, helpID);

        if (dialog instanceof JDialog) {
            final JDialog d = (JDialog) dialog;
            if (defaultButton != null) {
                d.getRootPane().setDefaultButton(defaultButton);
            }

            d.addWindowListener(new WindowAdapter() {
                    public void windowActivated(WindowEvent e) {
                        if(defaultButton != null)
                            defaultButton.requestFocus();
                    }
                    public void windowClosing(WindowEvent e) {
                        windowClosingAction(e);
                    }
            });

            d.setDefaultCloseOperation(defaultCloseOperation);
            d.pack();
        } else if (dialog instanceof JFrame) {
            final JFrame d = (JFrame) dialog;
            if (defaultButton != null) {
                d.getRootPane().setDefaultButton(defaultButton);
            }

            d.addWindowListener(new WindowAdapter() {
                    public void windowActivated(WindowEvent e) {
                        if(defaultButton != null)
                            defaultButton.requestFocus();
                    }
                    public void windowClosing(WindowEvent e) {
                        windowClosingAction(e);
                    }
            });

            d.setDefaultCloseOperation(defaultCloseOperation);
            d.pack();
        }
        else {
            final JInternalFrame f = (JInternalFrame) dialog;
            if (defaultButton != null) {
                f.getRootPane().setDefaultButton(defaultButton);
                /* this does not seem desirable or according to JL&F
                   f.addInternalFrameListener(new InternalFrameAdapter() {
                   public void internalFrameActivated(InternalFrameEvent e) {
                   defaultButton.requestFocus();
                   f.removeInternalFrameListener(this);
                   }
                   });
                */
            }
            f.addInternalFrameListener(new InternalFrameAdapter() {
                public void internalFrameClosing(InternalFrameEvent e) {
                    windowClosingAction(e);
                }
            });

            f.setDefaultCloseOperation(defaultCloseOperation);
            f.pack();
        }

        if (componentListener != null)
            dialog.addComponentListener(componentListener);

        if (cancelButton != null) {
            // if there are buttons, main will be a JPanel that we created
            JPanel p = (JPanel) main;
            InputMap inputMap = p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = p.getActionMap();
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), UIFactory.CANCEL);
            actionMap.put(UIFactory.CANCEL, new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        cancelButton.doClick(250);
                    }
                });
        }

        if (main instanceof JComponent) {
            // debug stuff
            JComponent p = (JComponent) main;
            InputMap inputMap = p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = p.getActionMap();
            inputMap.put(KeyStroke.getKeyStroke("ctrl 0"), "showInfo");
            actionMap.put("showInfo", new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        showInfo();
                    }
                });
        }

        dialog.setVisible(visible);
    }

    /**
     * This method add to allow subclasses of ToolDialog to change default close operation.
     * @param operation one of WindowConstants used by JDialog/JInternalFrame
     * setDefaultCloseOperation method
     */
    protected void setDefaultCloseOperation(int operation) {
        defaultCloseOperation = operation;

        if(dialog != null) {
            if(dialog instanceof JDialog) {
                ((JDialog)dialog).setDefaultCloseOperation(operation);
            } else if (dialog instanceof JFrame) {
                ((JFrame)dialog).setDefaultCloseOperation(operation);
            }
            else if(dialog instanceof JInternalFrame) {
                ((JInternalFrame)dialog).setDefaultCloseOperation(operation);
            }
        }
    }

    /**
     * this method invokes in Window/JInternalFrame listener (depends on dialog variable
     * instance). Should be overwritten in subclass if you want to handle window closing
     * event. You may need to change default close operation first for this event to be fired
     */
    protected void windowClosingAction(AWTEvent e) {
        return;
    }

    /**
     * By default disposing tool leads closing tool's ToolDialogs.
     * This approach is wrong in some certain cases. QuickStartWizard can
     * be example. Unlike most other cases QSW is a �parent� for its tool
     * This method provides way to disable default behavior
     */
    protected void disableDefaultDisposeHandler() {
        if (defaultDisposeHandler != null) {
            tool.removeObserver(defaultDisposeHandler);
        }
    }

    /**
     * Attempt to close
     */
    private void closing() {
        if (dialog instanceof Window) {
            dialog.dispatchEvent(new WindowEvent((Window) dialog, WindowEvent.WINDOW_CLOSING));
        }  else if (dialog instanceof JInternalFrame) {
            dialog.dispatchEvent(new InternalFrameEvent((JInternalFrame) dialog, InternalFrameEvent.INTERNAL_FRAME_CLOSING));
        }

        if (dialog instanceof JDialog) {
            final JDialog d = (JDialog) dialog;
            d.dispose();
        } else if (dialog instanceof JInternalFrame) {
            final JInternalFrame f = (JInternalFrame) dialog;
            f.dispose();
        }
    }


    /**
     * Initialize the content pane of the dialog.
     * If there are no buttons, the content pane is simply the client-supplied body;
     * otherwise, it will be a panel containing the client-supplied body, with
     * the buttons below it.
     */
    private void initMain() {
        if (body == null)
            initGUI();

        if (body == null)
            throw new IllegalStateException();

        if (buttons == null || buttons.length == 0)
            main = body;
        else {
            Container m = uif.createPanel(uiKey + ".main", false);
            m.setLayout(new BorderLayout());
            m.add(body, BorderLayout.CENTER);

            // set all the buttons to the same preferred size, per JL&F
            Dimension maxBtnDims = new Dimension();
            for (int i = 0; i < buttons.length; i++) {
                Dimension d = buttons[i].getPreferredSize();
                maxBtnDims.width = Math.max(maxBtnDims.width, d.width);
                maxBtnDims.height = Math.max(maxBtnDims.height, d.height);
            }

            for (int i = 0; i < buttons.length; i++)
                buttons[i].setPreferredSize(maxBtnDims);

            Container p = uif.createPanel(uiKey + ".btns", false);
            p.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.EAST;
            c.insets.top = 5;
            c.insets.bottom = 11;  // value from JL&F Guidelines
            c.insets.right = 11;   // value from JL&F Guidelines
            c.weightx = 1;         // first button absorbs space to the left

            for (int i = 0; i < buttons.length; i++) {
                p.add(buttons[i], c);
                c.weightx = 0;
            }

            m.add(p, BorderLayout.SOUTH);

            main = m;
        }
    }

    private void showInfo() {
        showComponent(main, 0);
    }

    private void showComponent(Component c, int depth) {
        for (int i = 0; i < depth; i++)
            System.err.print("  ");

        System.err.print(c.getClass().getName() + " " + c.getName() + ":");
        Dimension pref = c.getPreferredSize();
        Dimension size = c.getSize();
        Dimension diff = new Dimension(size.width - pref.width, size.height - pref.height);
        System.err.print(" pref:[w=" + pref.width + "," + pref.height + "]");
        System.err.print(" size:[w=" + size.width + "," + size.height + "]");
        System.err.print(" diff:[w=" + diff.width + "," + diff.height + "]");

        if (c instanceof Container) {
            Container p = (Container) c;
            System.err.println(" " + p.getComponentCount() + " children");
            for (int i = 0; i < p.getComponentCount(); i++)
                showComponent(p.getComponent(i), depth + 1);
        }
        else
            System.err.println();
    }

    /**
     * Parent component of this dialog.
     */
    protected final Component parent;

    /**
     * Factory associated with this dialog instance.
     */
    protected final UIFactory uif;

    /**
     * Parent tool of this dialog.
     */
    protected Tool tool;

    private String uiKey;
    private Tool.Observer defaultDisposeHandler;

    private Container dialog; // JDialog or JInternalFrame, as provided by desktop
    private String title;
    private JMenuBar menuBar;
    private Container main;
    private Container body;
    private JButton[] buttons;
    private JButton cancelButton;
    private JButton defaultButton;
    private String helpID;
    private ComponentListener componentListener;
    private int defaultCloseOperation  = WindowConstants.HIDE_ON_CLOSE;
    private Restorer restorer = new Restorer();
    private int type;

    /**
     * Mask used for ToolDialog initialization. With this mask ToolDialog will create simple dialog without minimize and maximize buttons that can be modal.
     * @see #DIALOG
     * @see #FREE
     * @see #MODAL
     * @see ToolDialog#ToolDialog(java.awt.Component, com.sun.javatest.tool.UIFactory, java.lang.String, int)
     */
    public static final int DIALOG                              = 0x000000;
    /**
     * Mask used for ToolDialog initialization. with this mask ToolDialog will create dialog with minimize and maximize buttons that can be used separately of main window and can't be modal.
     * @see #DIALOG
     * @see ToolDialog#ToolDialog(java.awt.Component, com.sun.javatest.tool.UIFactory, java.lang.String, int)
     */
    public static final int FRAME                               = 0x000001;
    /**
     * Mask that allows free floating to dialog created with DIALOG mask. Doesn't affect dialogs created with <code>FRAME</code> mask.
     * @see #FRAME
     * @see #DIALOG
     * @see ToolDialog#ToolDialog(java.awt.Component, com.sun.javatest.tool.UIFactory, java.lang.String, int)
     */
    public static final int FREE                                = 0x000010;
    /**
     * Mask that sets default modality (as JDialog.setModal(true)) to dialog created with DIALOG mask that blocks all AWT
     * frames. Doesn't affect dialogs created with <code>FRAME</code> mask.
     * @see #FRAME
     * @see #DIALOG
     * @see ToolDialog#ToolDialog(java.awt.Component, com.sun.javatest.tool.UIFactory, java.lang.String, int)
     */
    public static final int MODAL                               = 0x000100;
    /**
     * Mask that enables blocking input to all top-level windows (without any owner) from the same document except those from its own child hierarchy.
     * @see java.awt.Dialog.ModalityType#DOCUMENT_MODAL
     */
    public static final int MODAL_DOCUMENT                      = 0x001100;
    /**
     * Mask that enables blocking input to all top-level windows from the same Java application except those from its own child hierarchy..
     * @see java.awt.Dialog.ModalityType#APPLICATION_MODAL
     */
    public static final int MODAL_APPLICATION                   = 0x010100;
    /**
     * Mask that enables blocking input to all top-level windows run from the same toolkit except those from its own child hierarchy.
     * @see java.awt.Dialog.ModalityType#TOOLKIT_MODAL
     */
    public static final int MODAL_TOOLKIT                       = 0x011100;

    protected Restorer getRestorer() {
        return restorer;
    }

    protected void setRestorer(Restorer restorer) {
        this.restorer = restorer;
    }

    public class Restorer {

        // default value for key is outer's uiKey
        private String windowKey = ToolDialog.this.uiKey;
        private int restorePolicy = RESTORE_NOTHING;
        public final static int RESTORE_NOTHING = 0x0;
        public final static int RESTORE_ALL = 0x1;

        private final static String POS_X = ".pos.x";
        private final static String POS_Y = ".pos.y";
        private final static String SIZE_H = ".size.height";
        private final static String SIZE_W = ".size.weight";

        private Preferences prefs = Preferences.access();

        // ---
        private void save() {
           if (! "".equals(windowKey) && restorePolicy != RESTORE_NOTHING) {
                Container d = ToolDialog.this.dialog;
                Rectangle r = d.getBounds();
                prefs.setPreference(windowKey + POS_X, Integer.toString(r.x));
                prefs.setPreference(windowKey + POS_Y, Integer.toString(r.y));
                prefs.setPreference(windowKey + SIZE_H, Integer.toString(r.height));
                prefs.setPreference(windowKey + SIZE_W, Integer.toString(r.width));
            }
        }

        private void restore() {
            if (! "".equals(windowKey) && restorePolicy != RESTORE_NOTHING) {
                Container c = ToolDialog.this.dialog;
                Rectangle r = c.getBounds();
                int x = Integer.parseInt(prefs.getPreference(windowKey + POS_X, Integer.toString(r.x)));
                int y = Integer.parseInt(prefs.getPreference(windowKey + POS_Y, Integer.toString(r.y)));
                int h = Integer.parseInt(prefs.getPreference(windowKey + SIZE_H, Integer.toString(r.height)));
                int w = Integer.parseInt(prefs.getPreference(windowKey + SIZE_W, Integer.toString(r.width)));

                final Rectangle screenBounds = DeskView.getScreenBounds();
                // slow operation - make it before dialog is shown
                // trick for multiple displays environment when dialog allocated on several displays -
                // in these conditions dialog contents can be partially shown on one of displays
                // (look like JDK bug)
                // To avoid this effects do following:
                // 1) set location outside visible area to avoid dialog flickering
                // 2) set actual dialog location and size when dialog is opened (see below)

                Rectangle n = new Rectangle(x,y,w,h);
                n = screenBounds.intersection(n);
                if (!n.isEmpty()) {
                    c.setBounds(n);
                    c.setPreferredSize(n.getSize());
                }
                if (c instanceof Window)
                    ((Window)c).pack();
            }
        }

        public String getWindowKey() {
            return windowKey;
        }

        public void setWindowKey(String windowKey) {
            this.windowKey = windowKey;
        }

        public int getRestorePolicy() {
            return restorePolicy;
        }

        public void setRestorePolicy(int restorePolicy) {
            this.restorePolicy = restorePolicy;
        }
    }

}

