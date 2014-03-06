/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.sun.interview.ChoiceArrayQuestion;
import com.sun.interview.ChoiceQuestion;
import com.sun.interview.ErrorQuestion;
import com.sun.interview.FileListQuestion;
import com.sun.interview.FileQuestion;
import com.sun.interview.FloatQuestion;
import com.sun.interview.InetAddressQuestion;
import com.sun.interview.IntQuestion;
import com.sun.interview.Interview;
import com.sun.interview.ListQuestion;
import com.sun.interview.NullQuestion;
import com.sun.interview.PropertiesQuestion;
import com.sun.interview.Question;
import com.sun.interview.StringQuestion;
import com.sun.interview.StringListQuestion;
import com.sun.interview.TreeQuestion;
import com.sun.interview.YesNoQuestion;
import java.util.EventListener;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

/**
 * A panel which implements a {@link com.sun.interview.wizard.Wizard wizard}
 * that asks a series of {@link com.sun.interview.Question questions}
 * embodied in an {@link Interview interview}.
 */
class QuestionPanel extends JPanel
    implements Scrollable
{

    /**
     * Create a panel in which to display the questions of the interview.
     * The interview to be run.
     */
    QuestionPanel(Interview i) {
        interview = i;

        initRenderers();
        initGUI();

        addAncestorListener(listener);

    }


    // ---------- Component stuff ---------------------------------------

    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, PREFERRED_HEIGHT * DOTS_PER_INCH);
        d.width = Math.max(d.width, PREFERRED_WIDTH * DOTS_PER_INCH);
        return d;
    }

    // ---------- Scrollable stuff ---------------------------------------

    public Dimension getPreferredScrollableViewportSize() {
        Dimension maxD = new Dimension(PREFERRED_WIDTH * DOTS_PER_INCH, PREFERRED_HEIGHT * DOTS_PER_INCH);
        Dimension ps = getPreferredSize();
        ps.width = Math.min(ps.width, maxD.width);
        ps.height = Math.min(ps.height, maxD.height);
        return ps;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch(orientation) {
        case SwingConstants.VERTICAL:
            return visibleRect.height / 10;
        case SwingConstants.HORIZONTAL:
            return visibleRect.width / 10;
        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch(orientation) {
        case SwingConstants.VERTICAL:
            return visibleRect.height;
        case SwingConstants.HORIZONTAL:
            return visibleRect.width;
        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    public boolean getScrollableTracksViewportHeight() {
        // We can't use getPreferred size here, because of situation, when
        // getPreferredSize() gives default values. In this case, and if horizontal scroll exists,
        // viewport height becomes less than getPreferredSize().height, but may be enough for components
        // without any scrolling
        if(getParent() instanceof JViewport)
                return (((JViewport)getParent()).getHeight() > getPreferredSize().height);
        else
            return false;
    }

    public final boolean getScrollableTracksViewportWidth() {

//        if (currentRenderer instanceof SizeSensitiveQuestionRenderer) {
//            Dimension d;
//            int visWidth = ((JViewport)getParent()).getExtentSize().width;
//            int requiredWidth = valuePanel.getPreferredSize().width + TEXT_AREA_INSETS_LEFT_RIGHT*2;
//            if(requiredWidth < visWidth) {
////                d = this.getPreferredSize();
////                this.setPreferredSize(new Dimension(visWidth,
////                        super.getPreferredSize().height));
////                d = this.getPreferredSize();
//                setHorScrollStatus(false);
//                return true;
//            }
//            else {
////                d = this.getPreferredSize();
////                this.setPreferredSize(new Dimension(requiredWidth,
////                        super.getPreferredSize().height));
////                d = this.getPreferredSize();
//                setHorScrollStatus(true);
//                return false;
//            }
////            setHorScrollStatus(true);
////            return false;
//        }
//        else {
////            this.setPreferredSize(this.getSize());
//            setHorScrollStatus(false);
            return true;
//        }
    }

    // ---------- end of Scrollable stuff -----------------------------------

    void setNextAction(Action nextAction) {
        this.nextAction = nextAction;
    }

    void saveCurrentResponse() /*throws Interview.Fault*/ {
        if (valueSaver != null)
            valueSaver.run();
    }

    boolean isTagVisible() {
        return propsPanel.isVisible();
    }

    void setTagVisible(boolean v) {
        propsPanel.setVisible(v);
    }

    private void initRenderers() {
        renderers = new HashMap();
        renderers.put(ChoiceQuestion.class, new ChoiceQuestionRenderer());
        renderers.put(ChoiceArrayQuestion.class, new ChoiceArrayQuestionRenderer());
        renderers.put(FileQuestion.class, new FileQuestionRenderer());
        renderers.put(FileListQuestion.class, new FileListQuestionRenderer());
        renderers.put(FloatQuestion.class, new FloatQuestionRenderer());
        renderers.put(IntQuestion.class, new IntQuestionRenderer());
        renderers.put(InetAddressQuestion.class, new InetAddressQuestionRenderer());
        renderers.put(ListQuestion.class, new ListQuestionRenderer());
        renderers.put(NullQuestion.class, new NullQuestionRenderer());
        renderers.put(PropertiesQuestion.class, new PropertiesQuestionRenderer());
        renderers.put(StringQuestion.class, new StringQuestionRenderer());
        renderers.put(StringListQuestion.class, new StringListQuestionRenderer());
        renderers.put(TreeQuestion.class, new TreeQuestionRenderer());
        renderers.put(YesNoQuestion.class, new YesNoQuestionRenderer());
        setCustomRenderers(new HashMap());
    }

    /**
     * Create the basic GUI infrastructure. The content of the
     * various areas are filled in from the questions that are
     * asked.
     */
    private void initGUI() {
        /*
                +---------------+-------------------------------+
                |               |       title                   |
                |               +-------------------------------+
                |               |       text area               |
                |   graphic     |                               |
                |               +-------------------------------+
                |               |                               |
                |               |       value                   |
                |               |                               |
                |               +-------------------------------+
                |               |       msg                     |
                |               +-------------------------------+
                |               |       tag info (optional)     |
                +---------------+-------------------------------+
        */
        setInfo(this, "qu", false);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        graphicLabel = new JLabel();
        setInfo(graphicLabel, "qu.icon", false);
        graphicLabel.setFocusable(false);
        if (interview != null) {
            URL u = interview.getDefaultImage();
            if (u != null)
                graphicLabel.setIcon(new ImageIcon(u));
        }
        c.anchor = GridBagConstraints.CENTER;
        c.gridheight = GridBagConstraints.REMAINDER;
        add(graphicLabel, c);

        titleField = new JTextField();
        setInfo(titleField, "qu.title", true);
        titleField.setEditable(false);
        titleField.setBackground(new Color(102, 102, 153));//titleField.setBackground(MetalLookAndFeel.getPrimaryControlDarkShadow());
        titleField.setForeground(Color.WHITE);//titleField.setForeground(MetalLookAndFeel.getWindowBackground());
        Font f = titleField.getFont();//Font f = MetalLookAndFeel.getSystemTextFont();
        titleField.setFont(f.deriveFont(f.getSize() * 1.5f));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.weightx = 1;
        add(titleField, c);

        textArea = new JTextArea(3, 30);
        setInfo(textArea, "qu.text", true);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setBackground(new Color(255, 255, 255, 0));
        textArea.setBorder(null);
        textArea.setWrapStyleWord(true);
        // override JTextArea focus traversal keys, resetting them to
        // the Component default (i.e. the same as for the parent.)
        textArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        textArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        // set enter to be same as Next
        {
            InputMap im = textArea.getInputMap();
            im.put(enterKey, "next");
            ActionMap am = textArea.getActionMap();
            am.put("next", valueAction);
        }
        c.insets.top = TEXT_AREA_INSETS_TOP;
        c.insets.left = c.insets.right = TEXT_AREA_INSETS_LEFT_RIGHT;
        c.insets.bottom = TEXT_AREA_INSETS_BOTTOM;
        c.fill = GridBagConstraints.BOTH;
        add(textArea, c);


        valuePanel = new JPanel(new BorderLayout());
        setInfo(valuePanel, "qu.vp", false);
        valuePanel.setOpaque(true);
        // set default action for enter to be same as Next
        {
            InputMap im = valuePanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            im.put(enterKey, "next");
            ActionMap am = valuePanel.getActionMap();
            am.put("next", valueAction);
        }

        c.insets.top = VALUE_PANEL_INSETS_TOP;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets.bottom = VALUE_PANEL_INSETS_BOTTOM;
        add(valuePanel, c);

        c.fill = GridBagConstraints.BOTH;
        valueMessageField = new JTextField();
        setInfo(valueMessageField, "qu.vmsg", false);
        valueMessageField.setEditable(false);
        valueMessageField.setOpaque(false);
        valueMessageField.setFont(valueMessageField.getFont().deriveFont(Font.BOLD));
        valueMessageField.setBorder(null);
        c.insets.top = VALUE_MESSAGE_FIELD_INSETS_TOP;
        c.insets.bottom = VALUE_MESSAGE_FIELD_INSETS_BOTTOM;
        c.weighty = 0;
        add(valueMessageField, c);

        propsPanel = new JPanel(new BorderLayout());
        propsPanel.setBorder(BorderFactory.createEtchedBorder());
                // replace by titled border "properties" if we get more than one...
        propsPanel.setName("qu.props.pnl");
        propsPanel.setFocusable(false);
        JLabel tagLabel = new JLabel(i18n.getString("qu.tag.lbl"));
        setInfo(tagLabel, "qu.tag.lbl", true);
        tagLabel.setDisplayedMnemonic(i18n.getString("qu.tag.mne").charAt(0));
        tagLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 7));
        propsPanel.add(tagLabel, BorderLayout.WEST);
        tagField = new JTextField();
        tagField.setName("qu.tag.fld");
        tagField.setEditable(false);
        tagField.setBorder(null);
        tagLabel.setLabelFor(tagField);
        propsPanel.add(tagField, BorderLayout.CENTER);
        propsPanel.setVisible(false);
        c.insets.top = PROPS_PANEL_INSETS_TOP;
        c.insets.bottom = PROPS_PANEL_INSETS_BOTTOM;
        add(propsPanel, c);

        ActionMap actionMap = getActionMap();

        actionMap.put("hideProps", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    propsPanel.setVisible(false);
                }
            });

        actionMap.put("showProps", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    propsPanel.setVisible(true);
                }
            });

        actionMap.put("toggleProps", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    propsPanel.setVisible(!propsPanel.isVisible());
                }
            });

        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("ctrl T"), "toggleProps");

    }

    private void setInfo(JComponent jc, String uiKey, boolean addToolTip) {
        jc.setName(uiKey);
        AccessibleContext ac = jc.getAccessibleContext();
        ac.setAccessibleName(i18n.getString(uiKey + ".name"));
        if (addToolTip) {
            String tip = i18n.getString(uiKey + ".tip");
            jc.setToolTipText(tip);
            ac.setAccessibleDescription(tip);
        }
        else
            ac.setAccessibleDescription(i18n.getString(uiKey + ".desc"));
    }

    /**
     * Show a question. The appropriate showXXX method
     * is called, depending on the question, and then a response
     * is awaited.
     */
    public void showQuestion(Question q) {
        //System.err.println("QP.showQuestion " + q.getTag() + " " + q);
        if (q instanceof ErrorQuestion) {
            showErrorQuestion((ErrorQuestion) q);
            try {
                // no current response to save :-)
                interview.prev();
            }
            catch (Interview.Fault ignore) {
            }
            return;
        }

        URL u = q.getImage();
        final Icon icon = (u == null ? null : new ImageIcon(u));

        if (icon != null)
            graphicLabel.setIcon(icon);

        titleField.setText(q.getSummary());
        textArea.setText(q.getText());
        tagField.setText(q.getTag());

        boolean focus = anyChildHasFocus(valuePanel);
        valuePanel.removeAll();

        QuestionRenderer r = getRenderer(q);

        //------------------------------------

        if (r == null) {
            //System.err.println("no renderer for " + q.getTag() + " [" + q.getClass().getName() + "]");
            valueSaver = null;
        }
        else {
            JComponent rc = r.getQuestionRendererComponent(q, valueAction);
            if (rc == null) {
                valueSaver = null;
                if (focus) {
                    // no response area, so put focus back on question text
                    textArea.requestFocus();
                }
            }
            else {
                if (rc.getName() == null)
                    rc.setName(r.getClass().getName());

                valueSaver = (Runnable) (rc.getClientProperty(QuestionRenderer.VALUE_SAVER));
                //System.err.println("QP.showQuestion valueSaver=" + valueSaver);
                valuePanel.add(rc);
                if (focus) {
                    //System.err.println("QP.showQuestion: setFocus");
                    FocusManager fm = FocusManager.getCurrentManager();
                    //fm.focusNextComponent(valuePanel);
                    fm.focusNextComponent(textArea);
                }
            }
        }

        if (q.isValueAlwaysValid())
            valueMessageField.setVisible(false);
        else {
            showValueMessage(null);
            valueMessageField.setVisible(true);
        }

        // relayout the GUI
        revalidate();
        repaint();

        currentRenderer = r;
        currentQuestion = q;
    }

    private void showErrorQuestion(ErrorQuestion q) throws HeadlessException {

        JEditorPane ePane = new JEditorPane(q.getTextMimeType(), q.getText());
        ePane.setEditable(false);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension minS = new Dimension(350, 100);
        final Dimension maxS = new Dimension(Math.min(2*(screen.width/3), 800), Math.min(2*(screen.height/3), 600));
        Dimension p =  ePane.getPreferredSize();

        p.setSize(Math.max(p.width, minS.width), Math.max(p.height, minS.height));
        p.setSize(Math.min(p.width, maxS.width), Math.min(p.height, maxS.height));
        ePane.setPreferredSize(p);

        ePane.setCaretPosition(0);
        JScrollPane sPane = new JScrollPane(ePane);
        sPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JOptionPane.showMessageDialog(this, sPane, q.getSummary(), JOptionPane.ERROR_MESSAGE);

    }

    /**
     * This method invokes when config editor is going to be closed.
     * This made to allow components handle feature closing
     */
    public void prepareClosing() {
        if(currentRenderer instanceof PropertiesQuestionRenderer) {
            AncestorEvent e = new AncestorEvent(this, AncestorEvent.ANCESTOR_REMOVED,
                                    valuePanel, valuePanel.getParent());
            Component[] childs = valuePanel.getComponents();
            for(int i = 0; i < childs.length; i++) {
                EventListener[] l = childs[i].getListeners(AncestorListener.class);
                for(int j = 0; j < l.length; j++) {
                    if(l[i] instanceof AncestorListener) {
                        ((AncestorListener)l[i]).ancestorRemoved(e);
                    }
                }
            }
        }
    }

    public void showValueInvalidMessage() {
        String msg = (currentRenderer == null
                      ? null
                      : currentRenderer.getInvalidValueMessage(currentQuestion));
        showValueMessage((msg == null ? INVALID_VALUE : msg), INVALID_VALUE_COLOR);
    }

    private void showValueMessage(String msg) {
        showValueMessage(msg, Color.BLACK);//showValueMessage(msg, MetalLookAndFeel.getBlack());
    }

    private void showValueMessage(String msg, Color c) {
        if (msg == null || msg.length() == 0) {
            valueMessageField.setText("");
            valueMessageField.setEnabled(false);
        }
        else {
            valueMessageField.setForeground(c);
            valueMessageField.setText(msg);
            valueMessageField.setEnabled(true);
        }
    }

    private QuestionRenderer getRenderer(Question q) {
        QuestionRenderer result = null;
        if (customRenderers != null) {
            result = getRenderer(q, customRenderers);
        }
        if (result == null) {
            result = getRenderer(q, renderers);
        }
        return result;
    }

    private QuestionRenderer getRenderer(Question q, Map rendMap) {
        for (Class c = q.getClass(); c != null; c = c.getSuperclass()) {
            QuestionRenderer r = (QuestionRenderer) (rendMap.get(c));
            if (r != null)
                return r;
        }
        return null;
    }


    private boolean anyChildHasFocus(JPanel p) {
        if (p.hasFocus())
            return true;

        for (int i = 0; i < p.getComponentCount(); i++) {
            Component c = (p.getComponent(i));
            if ((c instanceof JComponent) && ((JComponent)c).hasFocus()
                || (c instanceof JPanel && anyChildHasFocus((JPanel)c)))
                return true;
        }
        return false;
    }

    private Interview interview;
    private Question currentQuestion;
    private QuestionRenderer currentRenderer;
    private JLabel graphicLabel;
    private JTextField titleField;
    private JTextArea textArea;
    private JPanel valuePanel;
    private Runnable valueSaver;
    private JTextField valueMessageField;
    private JPanel propsPanel;
    private JTextField tagField;
    private Map renderers;
    private Map customRenderers;
    private Listener listener = new Listener();

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
    private static String INVALID_VALUE = i18n.getString("qu.invalidValue.txt");
    private static Color INVALID_VALUE_COLOR = i18n.getErrorColor();

    private KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    private Action valueAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                if (cmd.equals(QuestionRenderer.EDITED))
                    showValueMessage(null);
                else {
                    if (nextAction != null) {
                        nextAction.actionPerformed(e);
                    }
                    else {
                        // default old behavior
                        try {
                            saveCurrentResponse();
                            interview.next();
                        }
                        catch (Interview.Fault ex) {
                            // exception normally means no more questions,
                            // which should only be  because the value of the current
                            // question is invalid
                            // e.printStackTrace();
                            // QuestionPanel.this.getToolkit().beep();
                            showValueInvalidMessage();
                        }
                    }
                }
            }

    };

    private Action nextAction; // optionally settable

    private static final int PREFERRED_HEIGHT = 3; // inches
    private static final int PREFERRED_WIDTH = 4; // inches
    private static final int DOTS_PER_INCH = Toolkit.getDefaultToolkit().getScreenResolution();
    private static final int TEXT_AREA_INSETS_TOP = 20;

    private static final int TEXT_AREA_INSETS_LEFT_RIGHT = 10;
    private static final int TEXT_AREA_INSETS_BOTTOM = 10;

    private static final int VALUE_PANEL_INSETS_TOP = 0;
    private static final int VALUE_PANEL_INSETS_BOTTOM = 10;

    private static final int VALUE_MESSAGE_FIELD_INSETS_TOP = 0;
    private static final int VALUE_MESSAGE_FIELD_INSETS_BOTTOM = 0;

    private static final int PROPS_PANEL_INSETS_TOP = 0;
    private static final int PROPS_PANEL_INSETS_BOTTOM = 10;

    public void setCustomRenderers(Map customRenderers) {
        this.customRenderers = customRenderers;
    }


    private class Listener
        implements AncestorListener, Interview.Observer
    {

        // ---------- AncestorListener

        public void ancestorAdded(AncestorEvent e) {
            interview.addObserver(this);
            showQuestion(interview.getCurrentQuestion());
        }

        public void ancestorMoved(AncestorEvent e) { }

        public void ancestorRemoved(AncestorEvent e) {
            interview.removeObserver(this);
        }

        // ---------- Interview.Observer ----------

        public void pathUpdated() {
            // if path is updated as a result of refresh, clear the error message
            showValueMessage(null);
        }

        public void currentQuestionChanged(Question q) {
            showQuestion(q);
        }

        public void finished() { }

    }

}
