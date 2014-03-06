/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Properties;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.interview.WizPrint;

/**
 * A wizard to present an {@link Interview interview} consisting of
 * a series of {@link Question questions}.
 *
 * <p>The tool can be started as an application itself,
 * by using the {@link #main main}
 * method. This requires that the class name of the interview
 * be supplied as the first argument; the class itself must be on
 * the tool's class path. This technique allows any interview
 * to be run by this tool.
 * <p>An alternative technique is to provide a small default main method
 * inside each interview, which creates an instance of the interview
 * and starts up a tool such as this one to run the interview.
 *<pre>
 *    import javasoft.sqe.wizard.Interview;
 *    import javasoft.sqe.wizard.swing.Wizard;
 *
 *    public class Demo extends Interview {
 *        public static void main(String[] args) {
 *          Demo d = new Demo();
 *          Wizard w = new Wizard(d);
 *          w.showInFrame(true);
 *        }
 *    }
 *</pre>
 */
public class Wizard extends JComponent {
    /**
     * A minimal main program to invoke the wizard on a specified interview.
     * @param args Only one argument is accepted: the name of a class which is
     * a subtype of {@link Interview}.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            Class ic = (Class.forName(args[0]));
            Interview i = (Interview)(ic.newInstance());
            Wizard w = new Wizard(i);
            w.showInFrame(true);
        }
        catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Create a wizard to present an interview.
     * @param i The interview to be presented.
     */
    public Wizard(Interview i) {
        this(i, null);
    }

    /**
     * Create a wizard to present an interview.
     * @param i The interview to be presented.
     * @param e An array of exporters to which the interview can be exported.
     */
    public Wizard(Interview i, Exporter[] e) {
        interview = i;
        exporters = e;
    }

    /**
     * Open a file and load it into the interview for this wizard.
     * This does not affect the name of the current file.
     * @param f The file to be loaded.
     * @throws IOException if any problems occur while reading the file.
     * @throws Interview.Fault if the checksum is missing or incorrect in the file
     * @see Interview#load
     * @see #setFile
     */
    public void open(File f) throws Interview.Fault, IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        try {
            Properties p = new Properties();
            p.load(in);
            interview.load(p);
            interview.setEdited(false);
            initialInfoVisible = p.getProperty("INFO", "true").equals("true");
        }
        finally {
            in.close();
        }
    }

    /**
     * Save the current responses to the interview's questions in a file..
     * @param f The file in which to save the responses.
     * @throws IOException if any problems occur while reading the file.
     * @see Interview#save
     */
    public void save(File f) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        try {
            Properties p = new Properties();
            if (infoPanel != null)
                p.put("INFO", String.valueOf(infoPanel.isShowing()));
            interview.save(p);
            interview.setEdited(false);
            p.save(out, "Wizard data file: " + interview.getTitle());
        }
        finally {
            out.close();
        }
    }

    /**
     * Get the name of the current file associated with this interview.
     * @return the file for the interview
     * @see #setFile
     */
    public File getFile() {
        return currFile;
    }

    /**
     * Set the name of the current file associated with this interview.
     * The file may be used as a default in open/save operations.
     * @param f The file to be associated with this interview.
     * @see #getFile
     * @see #setDefaultFile
     */
    public void setFile(File f) {
        currFile = new File(f.getAbsolutePath());
        if (window != null)
            updateTitle(window);
    }

    /**
     * Set the name of a default file associated with this interview.
     * The default file is used for the name of the current value
     * if the user performs a File>New operation. In addition, if the
     * default file is set, and the current file matches the default file,
     * it will not be shown in the title bar.
     * @param f The default file to be associated with this interview.
     */
    public void setDefaultFile(File f) {
        defaultFile = f;
        if (window != null)
            updateTitle(window);
    }

    /**
     * Set the help broker in which context sensitive help and default menu help
     * is displayed. If not set, a default help broker will be created.
     * @param helpBroker The help broker to use for context sensitive and menu help.
     */
    public void setHelpBroker(HelpBroker helpBroker) {
        helpHelpBroker = helpBroker;
    }

    /**
     * Set the help set to be used for context sensitive help and the default menu help.
     * If not set, the interview's help set will be used.
     * @param helpSet The help set to use for context sensitive and menu help.
     *
     */
    public void setHelpSet(HelpSet helpSet) {
        helpHelpSet = helpSet;
    }

    /**
     * Set the prefix string for the help IDs for context sensitive help and default menu help.
     * If not set, the default is "wizard.".
     * @param helpPrefix A prefix to be used for all context sentive help and menu entries.
     */
    public void setHelpSetPrefix(String helpPrefix) {
        helpHelpPrefix = helpPrefix;
    }

    /**
     * Set the help menu to be used on the wizard. If not set, the default is a menu
     * containing a single "Help" entry.
     * @param helpMenu The help menu to be used.
     */
    public void setHelpMenu(JMenu helpMenu) {
        this.helpMenu = helpMenu;
    }


    /**
     * Show the wizard in a frame centered on the screen.
     * @param exitOnClose Set to true if the JVM should be exited when the frame is closed.
     */
    public void showInFrame(final boolean exitOnClose) {
        if (window != null && !(window instanceof JFrame))
            throw new IllegalStateException();

        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    showInFrame(exitOnClose);
                }
            });
            return;
        }

        initGUI();
        okBtn.setVisible(false);
        cancelBtn.setVisible(false);

        final JFrame f = new JFrame();
        initMenuBar(f);
        updateTitle(f);
        f.setName("interview.wizard");
        f.setJMenuBar(menuBar);
        f.setContentPane(main);
        f.pack();

        f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (interview.isEdited() && !okToContinue())
                    return;
                e.getWindow().dispose();
            }

            public void windowClosed(WindowEvent e) {
                if (exitOnClose)
                    System.exit(0);
            }
        });

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = f.getSize();
        f.setLocation(screenSize.width/2 - size.width/2, screenSize.height/2 - size.height/2);
        f.show();

        window = f;
    }

    /**
     * Action command for the okListener for {@link #showInDialog}.
     */
    public static final String OK = "OK";

    /**
     * Show the wizard in a dialog.
     * @param parent The parent frame for this dialog.
     * @param okListener A listener to e notified when the dialog is dismissed.
     */
    public void showInDialog(final Frame parent, final ActionListener okListener) {
        if (window != null && !(window instanceof JDialog))
            throw new IllegalStateException();

        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    showInDialog(parent, okListener);
                }
            });
            return;
        }

        this.okListener = okListener;

        initGUI();
        okBtn.setVisible(true);
        okBtn.setEnabled(interview.isFinishable());
        cancelBtn.setVisible(true);

        final JDialog d = new JDialog(parent);
        initMenuBar(d);
        updateTitle(d);
        d.setJMenuBar(menuBar);
        d.setContentPane(main);
        d.pack();

        d.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        d.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (!interview.isEdited() || okToContinue())
                    e.getWindow().dispose();
            }

            public void windowClosed(WindowEvent e) {
            }
        });

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = d.getSize();
        d.setLocation(screenSize.width/2 - size.width/2, screenSize.height/2 - size.height/2);
        d.show();

        window = d;
    }

    /**
     * Check if this object is being displayed on the screen.
     * @return true if the wizard is currently being displayed,
     * and false otherwise.
     */
    public boolean isShowing() {
        return (window != null && window.isShowing());
    }

    /**
     * Ensure that this object is showing in front of all other windows
     * on the screen. If the object is not currently visible, the call
     * has no effect.
     */
    public void toFront() {
        if (window != null)
            window.toFront();
    }

    /**
     * Initialize the frame's GUI components
     */
    private void initGUI() {

        title = interview.getTitle();
        if (title == null || title.equals(""))
            title = i18n.getString("wizard.defaultTitle");

        //main = new JPanel(new BorderLayout());
        setLayout(new BorderLayout());
        main = this;

        questionPanel = new QuestionPanel(interview);
        questionPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        pathPanel = new PathPanel(questionPanel, interview);

        if (interview.getHelpSet() != null)
            infoPanel = new InfoPanel(interview);

        buttonPanel = new JToolBar();
        buttonPanel.setFloatable(false);
        //buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        buttonPanel.add(Box.createHorizontalGlue());
        backBtn = createButton("back", "performBack", performer);
        buttonPanel.add(backBtn);
        nextBtn = createButton("next", "performNext", performer);
        buttonPanel.add(nextBtn);
        buttonPanel.addSeparator();
        okBtn = createButton("ok", "performOk", performer);
        buttonPanel.add(okBtn);
        cancelBtn = createButton("cancel", "performCancel", performer);
        buttonPanel.add(cancelBtn);
        if (infoPanel != null) {
            buttonPanel.addSeparator();
            infoBtn = createToggle("info", "performInfo", performer);
            infoBtn.setSelected(initialInfoVisible);
            buttonPanel.add(infoBtn);
        }
        buttonPanel.addAncestorListener(new Listener());

        body = new JPanel(new BorderLayout());
        body.add(pathPanel, BorderLayout.WEST);
        body.add(questionPanel, BorderLayout.CENTER);
        body.add(buttonPanel, BorderLayout.SOUTH);

        body.registerKeyboardAction(performer, "performFindNext", KeyStroke.getKeyStroke("F3"),
                                           JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        if (helpHelpPrefix == null)
            helpHelpPrefix = "wizard.";

        if (helpHelpSet == null && infoPanel != null)
            helpHelpSet = infoPanel.getHelpSet();

        if (helpHelpBroker == null && helpHelpSet != null)
            helpHelpBroker = helpHelpSet.createHelpBroker();

        if (helpHelpBroker != null && helpHelpSet != null)
            helpHelpBroker.enableHelpKey(main, helpHelpPrefix + "window.csh", helpHelpSet);
        if (infoPanel == null)
            main.add(body);
        else
            update(infoBtn.isSelected());
    }

    private void initMenuBar(Window w) {
        menuBar = new JMenuBar();

        fileMenu = createMenu("file", fileMenuData, performer);
        if (w instanceof JFrame) {
            fileMenu.addSeparator();
            fileMenu.add(createMenuItem("file", "exit", "performExit", performer));
        }
        else {
            fileMenu.addSeparator();
            fileMenu.add(createMenuItem("file", "close", "performCancel", performer));
        }


        if (exporters != null) {
            // replace the default "export log" item with a full export submenu
            for (int i = 0; i < fileMenu.getItemCount(); i++) {
                JMenuItem mi = (JMenuItem)(fileMenu.getItem(i));
                if (mi != null && mi.getActionCommand().equals("performExportLog")) {
                    fileMenu.remove(i);
                    JMenu exportMenu = new ExportMenu(exporters);
                    exportMenu.add(createMenuItem("export", "log", "performExportLog", performer));
                    fileMenu.insert(exportMenu, i);
                    break;
                }
            }
        }
        menuBar.add(fileMenu);

        JMenu searchMenu = createMenu("search", searchMenuData, performer);
        menuBar.add(searchMenu);

        if  (helpHelpBroker != null) {
            if (helpMenu == null)
                helpMenu = createMenu("help", helpMenuData, performer);
            menuBar.add(helpMenu);
        }
    }

    private void update(boolean showInfoPanel) {
        Dimension bodySize = body.getSize();
        if (bodySize.width == 0)
            bodySize = body.getPreferredSize();

        Dimension infoSize = infoPanel.getSize();
        if (infoSize.width == 0)
            infoSize = infoPanel.getPreferredSize();
        // need to capture the next value before we remove everything from main
        boolean infoPanelIsShowing = infoPanel.isShowing();

        main.removeAll();

        if (showInfoPanel) {
            // body-help
            JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, body, infoPanel);
            sp.setDividerLocation(bodySize.width + 2);
            main.add(sp);
            infoPanel.setCurrentID(interview.getCurrentQuestion());
        }
        else {
            // body
            main.add(body);
        }

        if (window != null) {
            int divWidth = new JSplitPane().getDividerSize();
            Dimension winSize = window.getSize();
            int newWidth = winSize.width;
            if (showInfoPanel != infoPanelIsShowing)
                newWidth += (showInfoPanel ? +1 : -1) * (infoSize.width + divWidth + 4);
            window.setSize(newWidth, winSize.height);
        }

        if (infoBtn.isSelected() != showInfoPanel)
            infoBtn.setSelected(showInfoPanel);
    }

    private void updateTitle(Window w) {
        String t;
        if (currFile == null
            || (defaultFile != null && currFile.equals(defaultFile)))
            t = title;
        else
            t = i18n.getString("wizard.titleAndFile", new Object[] {title, currFile.getPath()});
        if (w instanceof JFrame)
            ((JFrame) w).setTitle(t);
        else
            ((JDialog) w).setTitle(t);
    }

    /**
     * Invoke a performXXX method via reflection
     * @param s The name of the method to be invoked.
     */
    private void perform(String s) {
        try {
            Method m = Wizard.class.getDeclaredMethod(s, new Class[] { });
            m.invoke(Wizard.this, new Object[] { });
        }
        catch (IllegalAccessException ex) {
            System.err.println(s);
            ex.printStackTrace();
        }
        catch (InvocationTargetException ex) {
            System.err.println(s);
            ex.getTargetException().printStackTrace();
        }
        catch (NoSuchMethodException ex) {
            System.err.println(s);
        }
    }

    /**
     * Handle the "back" action
     */
    private void performBack() {
        try {
            questionPanel.saveCurrentResponse();
            interview.prev();
        }
        catch (Interview.Fault e) {
            // exception normally means no more questions
            // e.printStackTrace();
        }
        catch (RuntimeException e) {
            // typically NumberFormatError
            // SEE ALSO QuestionPanel.showInetAddressQuestion
            // which wants to throw Interview.Fault from
            // the value saver, but can't
            questionPanel.getToolkit().beep();
        }
    }

    /**
     * Handle the "cancel" action
     */
    private void performCancel() {
        questionPanel.saveCurrentResponse();
        if (interview.isEdited() && !okToContinue())
            return;
        window.dispose();
    }

    /**
     * Handle the "exit" action
     */
    private void performExit() {
        questionPanel.saveCurrentResponse();
        if (interview.isEdited() && !okToContinue())
            return;
        // setVisible(false);
        System.exit(0); // uugh
    }

    /**
     * Handle the "exportLog" action
     */
    private void performExportLog() {
        questionPanel.saveCurrentResponse();
        JFileChooser chooser = new JFileChooser();
        if (currFile != null) {
            //  setCurrentDirectory required
            chooser.setCurrentDirectory(new File(currFile.getParent()));
            int dot = currFile.getName().lastIndexOf(".");
            if (dot != -1) {
                File f = new File(currFile.getName().substring(0, dot) + ".html");
                chooser.setSelectedFile(f);
            }
        }
        else {
            chooser.setCurrentDirectory(getUserDir());
        }
        chooser.setFileFilter(htmlFilter);
        //chooser.addChoosableFileFilter(txtFilter);
        int action = chooser.showDialog(main, i18n.getString("wizard.exportLog"));
        if (action != JFileChooser.APPROVE_OPTION)
            return;

        File f = ensureExtn(chooser.getSelectedFile(), ".html");
        if (f.exists() && !okToOverwrite(f))
            return;
        try {
            Writer out = new FileWriter(f);
            WizPrint w = new WizPrint(interview, interview.getPath());
            w.setShowResponses(true);
            w.write(out);
        }
        catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(main,
                                          i18n.getString("wizard.fileNotFound.txt", e.getMessage()),
                                          i18n.getString("wizard.fileNotFound.title"),
                                          JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(main,
                                          i18n.getString("wizard.badFile.txt", e.getMessage()),
                                          i18n.getString("wizard.badFile.title"),
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handle the "find" action
     */
    private void performFind() {
        if (searchDialog == null)
            searchDialog = SearchDialog.create(window, interview, helpHelpBroker, helpHelpPrefix);
        searchDialog.setVisible(true);
    }

    /**
     * Handle the "find" action
     */
    private void performFindNext() {
        if (searchDialog == null)
            searchDialog = SearchDialog.create(window, interview, helpHelpBroker, helpHelpPrefix);
        searchDialog.find();
    }

    /**
     * Handle the "help" action
     */
    private void performHelp() {
        helpHelpBroker.setCurrentID(helpHelpPrefix + "intro.csh");
        helpHelpBroker.setDisplayed(true);
    }

    /**
     * Handle the "info" action
     */
    private void performInfo() {
        boolean infoOn = infoBtn.isSelected();
        if (infoPanel.isShowing() != infoOn) {
            update(infoOn);
            window.validate();
        }
    }

    /**
     * Handle the "new" action
     */
    private void performNew() {
        questionPanel.saveCurrentResponse();
        if (interview.isEdited() && !okToContinue())
            return;
        interview.clear();
        interview.setEdited(false);
        setFile(defaultFile);
    }

    /**
     * Handle the "next" action
     */
    private void performNext() {
        try {
            questionPanel.saveCurrentResponse();
            interview.next();
        }
        catch (Interview.Fault e) {
            // exception normally means no more questions
            // e.printStackTrace();
            questionPanel.getToolkit().beep();
        }
        catch (RuntimeException e) {
            // typically NumberFormatError
            questionPanel.getToolkit().beep();
        }
    }

    /**
     * Handle the "ok" action
     */
    private void performOk() {
        try {
            questionPanel.saveCurrentResponse();
            window.dispose();
            okListener.actionPerformed(new ActionEvent(this,
                                                       ActionEvent.ACTION_PERFORMED,
                                                       OK));
        }
        catch (RuntimeException e) {
            // typically NumberFormatError
            questionPanel.getToolkit().beep();
        }
    }

    /**
     * Handle the "open" action
     */
    private void performOpen() {
        questionPanel.saveCurrentResponse();
        if (interview.isEdited() && !okToContinue())
            return;

        JFileChooser chooser = new JFileChooser();
        // set current directory from file or user.dir
        if (currFile != null) {
            // setCurrentDirectory required
            chooser.setCurrentDirectory(new File(currFile.getParent()));
            chooser.setSelectedFile(new File(currFile.getName()));
        }
        else {
            chooser.setCurrentDirectory(getUserDir());
        }
        chooser.setFileFilter(jtiFilter);
        int action = chooser.showOpenDialog(main);
        if (action != JFileChooser.APPROVE_OPTION)
            return;
        File f = ensureExtn(chooser.getSelectedFile(), ".jti");
        try {
            open(f);
            setFile(f);
        }
        catch (Interview.Fault e) {
            JOptionPane.showMessageDialog(main,
                                          i18n.getString("wizard.badInterview.txt", e.getMessage()),
                                          i18n.getString("wizard.badInterview.title"),
                                          JOptionPane.ERROR_MESSAGE);
        }
        catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(main,
                                          i18n.getString("wizard.fileNotFound.txt", e.getMessage()),
                                          i18n.getString("wizard.fileNotFound.title"),
                                          JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(main,
                                          i18n.getString("wizard.badFile.txt", e.getMessage()),
                                          i18n.getString("wizard.badFile.title"),
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handle the "save" action
     */
    private void performSave() {
        questionPanel.saveCurrentResponse();
        // save with current file
        if (currFile == null)
            performSaveAs();
        else
            performSaveInternal(currFile);
    }

    /**
     * Handle the "save as" action
     */
    private void performSaveAs() {
        questionPanel.saveCurrentResponse();
        JFileChooser chooser = new JFileChooser();
        if (currFile != null) {
            // setCurrentDirectory required
            chooser.setCurrentDirectory(new File(currFile.getParent()));
            chooser.setSelectedFile(new File(currFile.getName()));
        }
        else {
            chooser.setCurrentDirectory(getUserDir());
        }
        chooser.setFileFilter(jtiFilter);
        int action = chooser.showSaveDialog(main);
        if (action != JFileChooser.APPROVE_OPTION)
            return;
        File f = ensureExtn(chooser.getSelectedFile(), ".jti");
        if (f.exists() && !okToOverwrite(f))
            return;
        performSaveInternal(f);
    }

    /**
     * Internal common routine for the save/saveAs actions
     */
    private void performSaveInternal(File f) {
        try {
            save(f);
            setFile(f);
        }
        catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(main,
                                          i18n.getString("wizard.fileNotFound.txt", e.getMessage()),
                                          i18n.getString("wizard.fileNotFound.title"),
                                          JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(main,
                                          i18n.getString("wizard.badFile.txt", e.getMessage()),
                                          i18n.getString("wizard.badFile.title"),
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Get the user's current directory
     */
    private File getUserDir() {
        return new File(System.getProperty("user.dir"));
    }

    private JButton createButton(String uiKey, String actionCommand, ActionListener l) {
        JButton b = new JButton(createIcon(uiKey));
        b.setToolTipText(i18n.getString("wizard." + uiKey + ".tip"));
        b.setActionCommand(actionCommand);
        b.addActionListener(l);
        b.registerKeyboardAction(l, actionCommand, enterKey, JComponent.WHEN_FOCUSED);
        return b;
    }

    private Icon createIcon(String uiKey) {
        String iconResource = i18n.getString("wizard." + uiKey + ".icon");
        URL url = getClass().getResource(iconResource);
        return (url == null ? null : new ImageIcon(url));
    }

    /**
     * Create a menu according to an array of data
     * @title the title for the menu
     * @menuData the data for the menu; one element per menu item; an element can be
     * one of
     * <dl>
     * <dt> null
     *   <dd> a separator
     * <dt> an array of two strings
     *   <dd> a menu item, whose name is the first string, and whose action is the second
     * </dl>
     */
    private JMenu createMenu(String uiKey, String[][] menuData, ActionListener l) {
        JMenu m = new JMenu(i18n.getString("wizard." + uiKey + ".menu"));
        m.setName("wizard." + uiKey);
        m.setMnemonic(i18n.getString("wizard." + uiKey + ".mne").charAt(0));
        for (int i = 0; i < menuData.length; i++) {
            String[] data = menuData[i];
            if (data == null)
                m.addSeparator();
            else {
                JMenuItem mi = createMenuItem(uiKey, data[0], data[1], l);
                if (data.length > 2) {
                    KeyStroke accel = KeyStroke.getKeyStroke(data[2]);
                    mi.setAccelerator(accel);
                }
                m.add(mi);
            }
        }
        return m;
    }

    private JMenuItem createMenuItem(String uiKey, String name, String actionCommand, ActionListener l) {
        JMenuItem item = new JMenuItem(i18n.getString("wizard." + uiKey + "." + name + ".mit"));
        item.setName(name);
        item.setMnemonic(i18n.getString("wizard." + uiKey + "." + name + ".mne").charAt(0));
        item.setActionCommand(actionCommand);
        item.addActionListener(l);
        return item;
    }

    private JToggleButton createToggle(String uiKey, String actionCommand, ActionListener l) {
        JToggleButton b = new JToggleButton(createIcon(uiKey)) {
            public Insets getInsets() {
                return (nextBtn == null ? super.getInsets() : nextBtn.getInsets()); // !!
            }
        };
        b.setToolTipText(i18n.getString("wizard." + uiKey + ".tip"));
        b.setActionCommand(actionCommand);
        b.addActionListener(l);
        b.registerKeyboardAction(l, actionCommand, enterKey, JComponent.WHEN_FOCUSED);
        return b;
    }

    private File ensureExtn(File f, String extn) {
        if (f.getName().endsWith(extn))
            return f;
        else
            return new File(f.getPath() + extn);
    }

    private boolean okToContinue() {
        int response =
            JOptionPane.showConfirmDialog(main,
                                         i18n.getString("wizard.unsavedAnswers.txt"),
                                         i18n.getString("wizard.unsavedAnswers.title"),
                                         JOptionPane.YES_NO_OPTION);
        return (response == JOptionPane.YES_OPTION);
    }

    private boolean okToOverwrite(File f) {
        int response =
            JOptionPane.showConfirmDialog(main,
                                         i18n.getString("wizard.overwrite.txt", f),
                                         i18n.getString("wizard.overwrite.title"),
                                         JOptionPane.YES_NO_OPTION);
        return (response == JOptionPane.YES_OPTION);
    }

    private ActionListener performer = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            perform(e.getActionCommand());
        }
    };

    private Interview interview;
    private Exporter[] exporters;
    private String title;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    //private JPanel main;
    private JComponent main;
    private JPanel body;
    private PathPanel pathPanel;
    private QuestionPanel questionPanel;
    private InfoPanel infoPanel;
    private JToolBar buttonPanel;
    private JButton cancelBtn;
    private JButton backBtn;
    private JButton nextBtn;
    private JButton okBtn;
    private JToggleButton infoBtn;
    private Window window;
    private ActionListener okListener;
    private SearchDialog searchDialog;
    private boolean initialInfoVisible = true;
    private Listener listener = new Listener();

    // help for Help menu and context sensitive help (F1)
    private HelpSet helpHelpSet;
    private HelpBroker helpHelpBroker;
    private String helpHelpPrefix;
    private JMenu helpMenu;

    private File currFile;
    private File defaultFile;
    private boolean exitOnClose;

    private final FileFilter jtiFilter = new ExtensionFileFilter(".jti");
    private final FileFilter htmlFilter =
        new ExtensionFileFilter(new String[] {".htm", ".html"});

    private static final KeyStroke enterKey = KeyStroke.getKeyStroke("ENTER");

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();

    private static final String[][] fileMenuData = {
        {"new", "performNew"},
        {"open", "performOpen"},
        {"save", "performSave"},
        {"saveAs", "performSaveAs"},
        null,
        {"exportLog", "performExportLog"}
    };

    private static final String[][] helpMenuData = {
        {"help", "performHelp", "F1"}
    };

    private static final String[][] searchMenuData = {
        {"find", "performFind", "control F"},
        {"findNext", "performFindNext", "F3"},
    };

    private class ExtensionFileFilter extends FileFilter {
        ExtensionFileFilter(String extn) {
            this.extns = new String[] {extn};
        }

        ExtensionFileFilter(String[] extns) {
            this.extns = extns;
        }


        ExtensionFileFilter(String[] extns, String description) {
            this.extns = extns;
            this.description = description;
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            for (int i = 0; i < extns.length; i++)
                if (f.getName().endsWith(extns[i]))
                    return true;
            return false;
        }

        public String getDescription() {
            if (description == null) {
                StringBuffer sb = new StringBuffer("wizard.extn");
                if (extns.length == 0)
                    sb.append(".allFiles");
                else {
                    for (int i = 0; i < extns.length; i++)
                        sb.append(extns[i]);
                }
                description = i18n.getString(sb.toString());
            }
            return description;
        }

        private String[] extns;
        private String description;
    }

    private class ExportMenu extends JMenu implements ActionListener, PopupMenuListener {
        ExportMenu(Exporter[] exporters) {
            super(i18n.getString("wizard.export.menu"));
            setName("export");
            setMnemonic(i18n.getString("wizard.export.mne").charAt(0));
            for (int i = 0; i < exporters.length; i++) {
                JMenuItem mi = new JMenuItem(exporters[i].getName());
                mi.putClientProperty("exporter", exporters[i]);
                mi.setActionCommand("performGenericExport");
                mi.addActionListener(this);
                add(mi);
            }
            getPopupMenu().addPopupMenuListener(this);
        }

        public void actionPerformed(ActionEvent ev) {
            questionPanel.saveCurrentResponse();
            JMenuItem mi = (JMenuItem)(ev.getSource());
            Exporter e = (Exporter)(mi.getClientProperty("exporter"));
            export(e);
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent ev) {
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
            JPopupMenu m = (JPopupMenu)(ev.getSource());
            for (int i = 0; i < m.getComponentCount(); i++) {
                JMenuItem mi = (JMenuItem)(m.getComponent(i));
                if (mi != null) {
                    Exporter e = (Exporter)(mi.getClientProperty("exporter"));
                    if (e != null)
                        mi.setEnabled(e.isExportable());
                }
            }
        }

        private void export(Exporter e) {
            JFileChooser exportChooser = new JFileChooser();
            if (currFile != null) {
                // setCurrentDirectory required
                exportChooser.setCurrentDirectory(new File(currFile.getParent()));
                String[] extns = e.getFileExtensions();
                int dot = currFile.getName().lastIndexOf(".");
                if (dot != -1 && extns != null && extns.length > 0) {
                    File f = new File(currFile.getName().substring(0, dot) + extns[0]);
                    exportChooser.setSelectedFile(f);
                }
            }
            else {
                exportChooser.setCurrentDirectory(getUserDir());
            }
            exportChooser.setApproveButtonText(i18n.getString("wizard.exportChooser.export"));
            String[] extns = e.getFileExtensions();
            String desc = e.getFileDescription();
            exportChooser.setFileFilter(new ExtensionFileFilter(extns, desc));
            int action = exportChooser.showSaveDialog(main);
            if (action != JFileChooser.APPROVE_OPTION)
                return;
            try {
                File f = ensureExtn(exportChooser.getSelectedFile(), extns[0]);
                if (f.exists() && !okToOverwrite(f))
                    return;
                e.export(f);
            }
            catch (IOException ex) {
                JOptionPane.showMessageDialog(main,
                                              i18n.getString("wizard.exportError.txt", ex.getMessage()),
                                              i18n.getString("wizard.exportError.title"),
                                              JOptionPane.ERROR_MESSAGE);
            }
            catch (Interview.Fault ex) {
                JOptionPane.showMessageDialog(main,
                                              i18n.getString("wizard.exportError.txt", ex.getMessage()),
                                              i18n.getString("wizard.exportError.title"),
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class Listener implements AncestorListener, Interview.Observer
    {
        // ---------- from AncestorListener -----------

        public void ancestorAdded(AncestorEvent e) {
            interview.addObserver(this);
            pathUpdated();
            currentQuestionChanged(interview.getCurrentQuestion());
        }

        public void ancestorMoved(AncestorEvent e) { }

        public void ancestorRemoved(AncestorEvent e) {
            interview.removeObserver(this);
        }

        //----- from Interview.Observer -----------

        public void pathUpdated() {
            okBtn.setEnabled(interview.isFinishable());
        }

        public void currentQuestionChanged(Question q) {
            backBtn.setEnabled(!interview.isFirst(q));
            nextBtn.setEnabled(!interview.isLast(q));
        }
    }
}
