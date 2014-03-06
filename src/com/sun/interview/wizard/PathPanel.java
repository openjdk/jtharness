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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractListModel;
import javax.swing.Icon;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.TransferHandler;

import com.sun.interview.ErrorQuestion;
import com.sun.interview.FinalQuestion;
import com.sun.interview.Interview;
import com.sun.interview.NullQuestion;
import com.sun.interview.Question;

class PathPanel extends JPanel
    implements Scrollable
{
    public PathPanel(QuestionPanel questionPanel, Interview interview) {
        this.questionPanel = questionPanel;  //uugh; but needed for autosaving answers before changing questions
        this.interview = interview;
        moreText = i18n.getString("path.more");
        initGUI();
    }

    // ---------- Component stuff ---------------------------------------

    public Dimension getPreferredSize() {
        return list.getPreferredSize(); // should not be necessary
    }

    // ---------- Scrollable stuff ---------------------------------------

    public Dimension getPreferredScrollableViewportSize() {
        return list.getPreferredScrollableViewportSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return list.getScrollableBlockIncrement(visibleRect, orientation, direction);
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return list.getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            return (((JViewport)getParent()).getHeight() > getPreferredSize().height);
        }
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    // ---------- end of Scrollable stuff -----------------------------------

    boolean getMarkersEnabled() {
        return markersEnabled;
    }

    void setMarkersEnabled(boolean on) {
        if (on != markersEnabled) {
            markersEnabled = on;
            pathList.update();
        }
    }

    boolean getMarkersFilterEnabled() {
        return markersFilterEnabled;
    }

    void setMarkersFilterEnabled(boolean on) {
        if (on != markersFilterEnabled) {
            markersFilterEnabled = on;
            pathList.update();
        }
    }

    Question getNextVisible() {
        return pathList.getNextVisible();
    }

    Question getPrevVisible() {
        return pathList.getPrevVisible();
    }

    Question getLastVisible() {
        return pathList.getLastVisible();
    }

    JMenu getMarkerMenu() {
        return createMenu();
    }

    private void initGUI() {
        setName("path");
        setFocusable(false);
        setLayout(new BorderLayout());
        pathList = new PathList();
        list = new JList(pathList);
        setInfo(list, "path.list", true);
        list.setCellRenderer(pathList);
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        list.registerKeyboardAction(pathList, enterKey, JComponent.WHEN_FOCUSED);
        list.addListSelectionListener(pathList);
        list.addMouseListener(pathList);
        //list.setPrototypeCellValue("What is a good default to use?");

        // would be better if this were configurable
        list.setFixedCellWidth(2 * DOTS_PER_INCH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(new Color(255, 255, 255, 0));
        list.setOpaque(false);
        list.setVisibleRowCount(5);
        list.setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }
            @Override
            public Transferable createTransferable(JComponent c) {
                Object selected = list.getSelectedValue();
                if(selected != null) {
                    if(selected instanceof Question)
                        return new StringSelection(((Question)selected).getSummary());
                    else if(selected instanceof List) {
                        StringBuffer temp = new StringBuffer();
                        for(Question q: (List<Question>)selected) {
                            temp.append(q.getSummary());
                            temp.append("\n");
                        }
                        return new StringSelection(temp.toString());
                    } else if(selected instanceof String) {
                        return new StringSelection(selected.toString());
                    }
                }
                return null;
            }
        });

        pathList.currentQuestionChanged(interview.getCurrentQuestion());
        addAncestorListener(pathList);
        add(list);
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

    private QuestionPanel questionPanel;
    private Interview interview;
    private PathList pathList;
    private JList list;
    private String moreText;

    // client parameters
    private boolean markersEnabled;
    private boolean markersFilterEnabled;
    private String markerName = null; // theoretically settable

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
    private static Color INVALID_VALUE_COLOR = i18n.getErrorColor();
    private static final int DOTS_PER_INCH = Toolkit.getDefaultToolkit().getScreenResolution();

    private class PathList
                extends AbstractListModel
                implements ActionListener, AncestorListener,
                           ListCellRenderer, ListSelectionListener,
                           MouseListener,
                           Interview.Observer
    {
        //----- navigation support for WizPane -----------------------

        Question getNextVisible() {
            for (int i = currIndex + 1; i < currEntries.length; i++) {
                Object e = currEntries[i];
                if (e instanceof Question)
                    return ((Question) e);
            }
            return null;
        }

        Question getPrevVisible() {
            for (int i = currIndex - 1; i >= 0; i--) {
                Object e = currEntries[i];
                if (e instanceof Question)
                    return ((Question) e);
            }
            return null;
        }

        Question getLastVisible() {
            for (int i = currEntries.length - 1; i >= 0; i--) {
                Object e = currEntries[i];
                if (e instanceof Question)
                    return ((Question) e);
            }
            return null;
        }

        //----- state support for menus -----------------------------

        boolean isQuestionVisible(Question q) {
            for (int i = 0; i < currEntries.length; i++) {
                Object e = currEntries[i];
                if (e instanceof Question && ((Question) e) == q)
                    return true;
                else if (e instanceof List && ((List) e).contains(q))
                    return false;
            }
            return false;
        }

        boolean isQuestionAutoOpened(Question q) {
            // only return true if the preceding marked question is in the autoOpen set
            boolean autoOpened = autoOpenSet.contains(null);
            for (int i = 0; i < currEntries.length; i++) {
                Object e = currEntries[i];
                if (e instanceof Question) {
                    Question qe = (Question) e;
                    if (qe.hasMarker(markerName)) {
                        if (qe == q)
                            return false;
                        autoOpened = autoOpenSet.contains(qe);
                    }
                    else if (qe == q)
                        return autoOpened;
                }
                else if (e instanceof List && ((List) e).contains(q))
                    return false;
            }
            return false;
        }

        //----- actions ------------------------

        void markCurrentQuestion() {
            setQuestionMarked(interview.getCurrentQuestion(), true);
        }

        void unmarkCurrentQuestion() {
            setQuestionMarked(interview.getCurrentQuestion(), false);
        }

        private void setQuestionMarked(Question q, boolean on) {
            questionPanel.saveCurrentResponse();
            if (on)
                q.addMarker(markerName);
            else
                q.removeMarker(markerName);

            pathList.update(q);
        }

        void openCurrentEntry() {
            openEntry(currIndex);
        }

        void openEntry(int index) {
            Object o = currEntries[index];

            // only a list can be opened
            if (!(o instanceof List))
                return;

            // the marker question for a List is the question in the preceding entry
            // find that marker question and add it to the autoOpenSet
            for (int i = 1; i < currEntries.length; i++) {
                if (currEntries[i] == o) {
                    Object m = currEntries[i - 1];
                    if (m instanceof Question)
                        autoOpenSet.add(m);
                    update();
                }
            }
        }

        void closeCurrentEntry() {
            closeEntry(currIndex);
        }

        void closeEntry(int index) {
            Object o = currEntries[index];

            // only a question can be closed
            if (!(o instanceof Question))
                return;

            // need to figure out the autoOpenSet entry
            // scan back through entries looking for a marked question
            // or the first question
            Question marker = null;
            for (int i = index; i >= 0; i--) {
                Object ei = currEntries[i];
                if (ei instanceof Question) {
                    Question qi = (Question) ei;
                    if (i == 0 || qi.hasMarker(markerName)) {
                        marker = qi;
                        break;
                    }
                }
            }

            // can't close the marker question itself:
            // must close a question after the marker
            if (marker == o)
                return;

            autoOpenSet.remove(marker);
            update();
        }

        //----- from AbstractListModel -----------

        public int getSize() {
            return (currEntries == null ? 0 : currEntries.length);
        }

        public Object getElementAt(int index) {
            return (index < currEntries.length ? currEntries[index] : null);
        }

        //----- from ListCellRenderer -----------

        private JLabel sample = new JLabel() {
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        };

        public Component getListCellRendererComponent(JList list, Object o, int index, boolean isSelected, boolean cellHasFocus) {
            if (o instanceof Question) {
                Question q = (Question)o;
                Font f;
                String s;
                Color c;
                Color bg = null;        // null is default
                if (q instanceof ErrorQuestion) {
                    f = list.getFont().deriveFont(Font.BOLD);
                    s = "   " + q.getSummary();
                    c = INVALID_VALUE_COLOR;
                }
                else if (q instanceof NullQuestion) {
                    int level = ((NullQuestion)q).getLevel();

                    switch (level) {
                    case NullQuestion.LEVEL_NONE:
                        f = list.getFont();
                        s = " " + q.getSummary();
                        c = list.getForeground();
                        bg = null;
                        break;
                    case NullQuestion.LEVEL_1:
                        f = list.getFont().deriveFont(Font.BOLD,
                                        list.getFont().getSize() +3);
                        s = q.getSummary();
                        c = new Color(0x63,0x82,0xBF);
                        bg = new Color(0xDD,0xDD,0xDD);
                        break;
                    case NullQuestion.LEVEL_2:
                        f = list.getFont().deriveFont(Font.BOLD);
                        s = q.getSummary();
                        c = new Color(0x63,0x82,0xBF);
                        break;
                    case NullQuestion.LEVEL_3:
                        f = list.getFont().deriveFont(Font.PLAIN);
                        s = "  " + q.getSummary();
                        c = list.getForeground();
                        break;
                    default:        // LEVEL_LEGACY handled here
                        f = list.getFont().deriveFont(Font.BOLD);
                        s = " " + q.getSummary();
                        c = list.getForeground();
                    }   // switch
                }
                else {
                    f = list.getFont().deriveFont(Font.PLAIN);
                    s = "   " + q.getSummary();
                    c = list.getForeground();
                }
                sample.setText(s);
                sample.setFont(f);
                sample.setForeground(c);
                if (bg != null)
                    sample.setBackground(bg);
                else
                    sample.setBackground(list.getBackground());

                if (markersEnabled)
                    sample.setIcon(q.hasMarker(markerName) ? markerIcon : noMarkerIcon);
                else
                    sample.setIcon(null);
            }
            else if (o instanceof List) {
                sample.setText(null);
                sample.setFont(list.getFont());
                sample.setForeground(list.getForeground());
                sample.setIcon(ellipsisIcon);
            }
            else if (o instanceof String) {
                // prototype value or more...
                sample.setText(" " + (String)o);
                sample.setFont(list.getFont().deriveFont(Font.ITALIC));
                sample.setForeground(list.getForeground());
                sample.setIcon(markersEnabled ? noMarkerIcon : null);
            }
            else
                throw new IllegalArgumentException();

            // rest of method based on javax.swing.DefaultListCellRenderer
            if (isSelected) {
                sample.setBackground(list.getSelectionBackground());
                //sample.setForeground(list.getSelectionForeground());
            }
            else {
                //sample.setBackground(list.getBackground());
                //sample.setForeground(list.getForeground());
            }
            sample.setOpaque(true);
            sample.setEnabled(list.isEnabled());
            sample.setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : null);

            return sample;
        }

        //----- from ActionListener -----------

        // invoked by keyboard "enter"
        public void actionPerformed(ActionEvent e) {
            //System.err.println("PP.actionPerformed");
            JList list = (JList)(e.getSource());
            Object o = list.getSelectedValue();
            if (o != null && o instanceof Question) {
                Question q = (Question)o;
                if (q == interview.getCurrentQuestion())
                    return;

                //System.err.println("PP.actionPerformed saveCurrentResponse");
                questionPanel.saveCurrentResponse();

                try {
                    //System.err.println("PP.actionPerformed setCurrentQuestion");
                    interview.setCurrentQuestion(q);
                }
                catch (Interview.Fault ex) {
                    // ignore, should never happen; FLW
                }
            }
        }

        //----- from ListSelectionListener -----------

        // invoked by mouse selection (or by list.setSelectedXXX ??)
        public void valueChanged(ListSelectionEvent e) {
            JList list = (JList) (e.getSource());
            Object o = list.getSelectedValue();
            if (o == null)
                return;

            // make sure the interview's current question is synchronized with
            // the list selection
            if (o instanceof Question) {
                Question q = (Question) o;
                if (q == interview.getCurrentQuestion())
                    return;

                questionPanel.saveCurrentResponse();

                try {
                    interview.setCurrentQuestion(q);
                }
                catch (Interview.Fault ex) {
                    // ignore, should never happen; FLW
                }
            }
            else if (o instanceof List) {
                List l = (List) o;
                if (l.contains(interview.getCurrentQuestion()))
                    return;

                questionPanel.saveCurrentResponse();

                try {
                    Question q = (Question) (l.get(0));
                    interview.setCurrentQuestion(q);
                }
                catch (Interview.Fault ex) {
                    // ignore, should never happen; FLW
                }

            }
            else {
                // if the user tries to select the More string,
                // reject the request by resetting to the currIndex
                list.setSelectedIndex(currIndex);
            }
        }

        // ---------- from AncestorListener -----------

        public void ancestorAdded(AncestorEvent e) {
            interview.addObserver(this);
            pathUpdated();
        }

        public void ancestorMoved(AncestorEvent e) { }

        public void ancestorRemoved(AncestorEvent e) {
            interview.removeObserver(this);
        }

        //----- from MouseListener -----------

        public void mouseEntered(MouseEvent e) { }

        public void mouseExited(MouseEvent e) { }

        public void mousePressed(MouseEvent e) {
            if (markersEnabled && e.isPopupTrigger() && isOverSelection(e))
                showPopupMenu(e);
        }

        public void mouseReleased(MouseEvent e) {
            if (markersEnabled && e.isPopupTrigger() && isOverSelection(e))
                showPopupMenu(e);
        }

        private boolean isOverSelection(MouseEvent e) {
            JList l = (JList) (e.getComponent());
            Rectangle r = l.getCellBounds(currIndex, currIndex);
            return (r.contains(e.getX(), e.getY()));
        }

        private void showPopupMenu(MouseEvent e) {
            if (popupMenu == null)
                popupMenu = createPopupMenu();
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        public void mouseClicked(MouseEvent e) {
            if (!markersEnabled)
                return;

            Point p = e.getPoint();
            int index = list.locationToIndex(p);
            if (index == -1)
                return;
            Object entry = currEntries[index];

            switch (e.getClickCount()) {
            case 1:
                if (p.x < markerIcon.getIconWidth()) {
                    if (entry instanceof Question) {
                        Question q = (Question) entry;
                        setQuestionMarked(q, !q.hasMarker(markerName));
                    }
                }
                break;

            case 2:
                if (markersFilterEnabled) {
                    if (entry instanceof List)
                        openEntry(index);
                    else
                        closeEntry(index);
                }
                break;
            }
        }

        //----- from Interview.Observer -----------

        public void pathUpdated() {
            update(interview.getPath(), interview.getCurrentQuestion());
        }

        public void currentQuestionChanged(Question q) {
            int prevIndex = currIndex;
            currQuestion = q;
            for (int i = 0; i < currEntries.length; i++) {
                Object o = currEntries[i];
                if (o == q || (o instanceof List && ((List) o).contains(q))) {
                    currIndex = i;
                    break;
                }
            }
            fireContentsChanged(this, prevIndex, currIndex);

            list.setSelectedIndex(currIndex);
            list.ensureIndexIsVisible(currIndex);
        }

        public void finished() {
        }

        void update() {
            update(currPath, currQuestion);
        }

        void update(Question q) {
            if (markersFilterEnabled)
                update();
            else {
                for (int i = 0; i < currEntries.length; i++) {
                    Object e = currEntries[i];
                    if (e == q) {
                        currMarks[i] = (markersEnabled && q.hasMarker(markerName));
                        fireContentsChanged(this, i, i);
                        break;
                    }
                }
            }
        }

        private void update(Question[] newPath, Question newCurrQuestion) {
            boolean oldEnabled = currEnabled;
            Object[] oldEntries = currEntries;
            boolean[] oldMarks = currMarks;

            if (!markersFilterEnabled)
                autoOpenSet.clear();

            Object[] newEntries = getEntries(newPath);
            boolean[] newMarks = new boolean[newEntries.length];
            if (markersEnabled) {
                for (int i = 0; i < newEntries.length; i++) {
                    newMarks[i] = (newEntries[i] instanceof Question
                                   && ((Question) newEntries[i]).hasMarker(markerName));
                }
            }

            currPath = newPath;
            currEntries = newEntries;
            currEnabled = markersEnabled;
            currMarks = newMarks;

            int shorterEntriesLength = Math.min(oldEntries.length, newEntries.length);

            int firstDiff = 0;
            if (currEnabled == oldEnabled) {
                // optimize firstDiff when icons are not changing
                while (firstDiff < shorterEntriesLength) {
                    Object oldObj = oldEntries[firstDiff];
                    boolean oldMark = oldMarks[firstDiff];
                    Object newObj = newEntries[firstDiff];
                    boolean newMark = newMarks[firstDiff];
                    if (oldObj instanceof Question ? oldObj == newObj && oldMark == newMark
                        : oldObj instanceof List ? newObj instanceof List
                        : false ) {
                        firstDiff++;
                    }
                    else
                        break;
                }
            }

            if (firstDiff != oldEntries.length || firstDiff != newEntries.length) {
                if (firstDiff != shorterEntriesLength) {
                    //System.err.println("PP.update: change[" + firstDiff + "," + (shorterEntriesLength-1) + "/" + oldEntries.length + "," + newEntries.length + "]" + interview);
                    fireContentsChanged(this, firstDiff, shorterEntriesLength-1);
                }

                if (shorterEntriesLength != oldEntries.length) {
                    //System.err.println("PP.update: remove[" + shorterEntriesLength + "," + (oldEntries.length-1) + "/" + oldEntries.length + "," + newEntries.length + "]" + interview);
                    fireIntervalRemoved(this, shorterEntriesLength, oldEntries.length-1);
                }
                if (shorterEntriesLength != newEntries.length) {
                    //System.err.println("PP.update: add[" + shorterEntriesLength + "," + (newEntries.length-1) + "/" + oldEntries.length + "," + newEntries.length + "]" + interview);
                    fireIntervalAdded(this, shorterEntriesLength, newEntries.length-1);
                }
            }

            currQuestion = newCurrQuestion;
            for (int i = 0; i < currEntries.length; i++) {
                Object o = currEntries[i];
                if (o == currQuestion
                    || (o instanceof List && ((List) o).contains(currQuestion))) {
                    currIndex = i;
                    break;
                }
            }

            list.setSelectedIndex(currIndex);
            list.ensureIndexIsVisible(currIndex);
            //System.err.println("PP.update: sel:" + currIndex + " " + currQuestion);
        }

        private Object[] getEntries(Question[] path) {
            if (path.length == 0)  // transient startup condition
                return path;

            Question last = path[path.length - 1];
            boolean needMore = !(last instanceof ErrorQuestion || last instanceof FinalQuestion);
            // quick check to see if we can simply use the path as is
            if ( (!markersEnabled || !markersFilterEnabled) && !needMore)
                return path;

            Vector v = new Vector();
            Question lastMarker = null;
            for (int i = 0; i < path.length; i++) {
                Question q = path[i];
                if (!markersEnabled || !markersFilterEnabled) {
                    v.add(q);
                }
                else if (q.hasMarker(markerName)
                         || i == 0
                         || (i == path.length - 1 && q instanceof FinalQuestion)) {
                    lastMarker = q;
                    v.add(q);
                }
                else if (autoOpenSet.contains(lastMarker)) {
                    v.add(q);
                }
                else {
                    List l;
                    Object o = v.lastElement();
                    if (o == null || o instanceof Question) {
                        l = new Vector();
                        v.add(l);
                    }
                    else
                        l = (List) o;
                    l.add(q);
                }
            }

            // auto-expand the final section if it doesn't end in FinalQuestion
            if (!(last instanceof FinalQuestion) && v.lastElement() instanceof List) {
                List l = (List) (v.lastElement());
                v.setSize(v.size() - 1);
                v.addAll(l);
            }

            if (needMore)
                v.add(moreText);

            Object[] a = new Object[v.size()];
            v.copyInto(a);
            return a;
        }



        // currPath and currQuestion give info as obtained from the interview
        private Question[] currPath = new Question[0];
        private Question currQuestion;

        // currEntries and currIndex give displayable entries
        // entries may be Question, List<Question>, or String
        private Object[] currEntries = new Object[0];
        private int currIndex;

        // currEnabled gives state of markersEnabled as used to construct list
        private boolean currEnabled;

        // currMarks gives which questions are currently showing a mark
        private boolean[] currMarks;

        // autoOpenSet gives which non-markered questions should be displayed
        private Set autoOpenSet = new HashSet();

        private Icon markerIcon = new MarkerIcon(true);
        private Icon noMarkerIcon = new MarkerIcon(false);
        private Icon ellipsisIcon = new EllipsisIcon();

        private JPopupMenu popupMenu;

    }

    //-----------------------------------------------------------------------

    private JMenu createMenu() {
        return (JMenu) (new Menu(Menu.JMENU).getComponent());
    }

    private JPopupMenu createPopupMenu() {
        return (JPopupMenu) (new Menu(Menu.JPOPUPMENU).getComponent());
    }

    private class Menu
        implements ActionListener, ChangeListener, MenuListener, PopupMenuListener
    {

        static final int JMENU = 0, JPOPUPMENU = 1;

        Menu(int type) {
            this.type = type;

            // check box items (JMenu only)
            if (type == JMENU) {
                enableItem = createCheckBoxItem(ENABLE);
                filterItem = createCheckBoxItem(FILTER);
            }

            // question items (JMenu and JPopupMenu)
            markItem = createItem(MARK);
            unmarkItem = createItem(UNMARK);
            clearItem = createItem(CLEAR);

            // group items (JMenu and JPopupMenu)
            openGroupItem = createItem(OPEN_GROUP);
            closeGroupItem = createItem(CLOSE_GROUP);

            // interview items (JMenu only)
            if (type == JMENU) {
                clearMarkedItem = createItem(CLEAR_MARKED);
                removeAllItem = createItem(REMOVE_MARKERS);
            }

            if (type == JMENU) {
                JMenu m = new JMenu(i18n.getString("path.mark.menu"));
                m.setName("path.mark.menu");
                m.getAccessibleContext().setAccessibleDescription(i18n.getString("path.mark.desc"));
                int mne = i18n.getString("path.mark.mne").charAt(0);
                m.setMnemonic(mne);
                m.add(enableItem);
                m.add(filterItem);
                m.addSeparator();
                m.add(markItem);
                m.add(unmarkItem);
                m.add(clearItem);
                m.addSeparator();
                m.add(openGroupItem);
                m.add(closeGroupItem);
                m.addSeparator();
                m.add(clearMarkedItem);
                m.add(removeAllItem);
                m.addMenuListener(this);
                comp = m;
            }
            else {
                JPopupMenu m = new JPopupMenu();
                m.add(markItem);
                m.add(unmarkItem);
                m.add(clearItem);
                // don't put a separator because often all the items above
                // or all the items below will not be visible
                m.add(openGroupItem);
                m.add(closeGroupItem);
                m.addPopupMenuListener(this);
                comp = m;
            }
        }

        JComponent getComponent() {
            return comp;
        }

        private JMenuItem createItem(String name) {
            JMenuItem mi = new JMenuItem(i18n.getString("path.mark." + name + ".mit"));
            mi.setName(name);
            mi.setActionCommand(name);
            mi.addActionListener(this);
            setMnemonic(mi, name);
            return mi;
        }

        private JCheckBoxMenuItem createCheckBoxItem(String name) {
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem(i18n.getString("path.mark." + name + ".ckb"));
            mi.setName(name);
            mi.addChangeListener(this);
            setMnemonic(mi, name);
            return mi;
        }

        private void updateItems() {

            Question q = interview.getCurrentQuestion();

            boolean marked = q.hasMarker(markerName);
            boolean visible = pathList.isQuestionVisible(q);
            boolean autoOpened = pathList.isQuestionAutoOpened(q);

            if (type == JMENU) {
                // rules for a menu-bar menu:
                // keep things more visible, but disable as necessary

                enableItem.setSelected(markersEnabled);

                filterItem.setSelected(markersFilterEnabled);
                filterItem.setEnabled(markersEnabled);

                markItem.setVisible(!marked);
                markItem.setEnabled(markersEnabled);

                unmarkItem.setVisible(marked);
                unmarkItem.setEnabled(markersEnabled);

                clearItem.setVisible(true);
                clearItem.setEnabled(markersEnabled && !(q instanceof NullQuestion));

                openGroupItem.setVisible(!markersFilterEnabled || !visible);
                openGroupItem.setEnabled(markersEnabled && markersFilterEnabled);

                closeGroupItem.setVisible(markersFilterEnabled && visible);
                closeGroupItem.setEnabled(markersEnabled && markersFilterEnabled);

                clearMarkedItem.setVisible(true);
                clearMarkedItem.setEnabled(markersEnabled);

                removeAllItem.setVisible(true);
                removeAllItem.setEnabled(markersEnabled);
            }
            else {
                // rules for a popup menu:
                // hide inappropriate items, but always enabled
                // note popup menu only active if markersEnabled

                markItem.setVisible(visible && !marked);

                unmarkItem.setVisible(visible && marked);

                clearItem.setVisible(visible && !(q instanceof NullQuestion));

                openGroupItem.setVisible(markersFilterEnabled && !visible);
                closeGroupItem.setVisible(markersFilterEnabled && autoOpened);
            }
        }

        private void setMnemonic(JMenuItem mi, String name) {
            int mne = i18n.getString("path.mark." + name + ".mne").charAt(0);
            mi.setMnemonic(mne);
        }

        // ---------- from ActionListener -----------

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(MARK)) {
                pathList.markCurrentQuestion();
            }
            else if (cmd.equals(UNMARK)) {
                pathList.unmarkCurrentQuestion();
            }
            else if (cmd.equals(CLEAR)) {
                Question q = interview.getCurrentQuestion();
                q.clear();
                // have to redisplay question explicitly,
                // because there is no notification that the
                // value of the current question has been changed.
                questionPanel.showQuestion(q);
            }
            else if (cmd.equals(OPEN_GROUP)) {
                pathList.openCurrentEntry();
            }
            else if (cmd.equals(CLOSE_GROUP)) {
                pathList.closeCurrentEntry();
            }
            else if (cmd.equals(CLEAR_MARKED)) {
                Question q = interview.getCurrentQuestion();
                interview.clearMarkedResponses(markerName);
                // If the previously current question is still current,
                // we need to redisplay it to make sure that it shows
                // the updated value
                if (q == interview.getCurrentQuestion())
                    questionPanel.showQuestion(q);
            }
            else if (cmd.equals(REMOVE_MARKERS)) {
                // show a confirm dialog?
                interview.removeMarkers(markerName);
                if (getMarkersFilterEnabled() == true)
                    setMarkersFilterEnabled(false); // will cause update
                else
                    pathList.update();
            }
        }

        // ---------- from ChangeListener -----------

        public void stateChanged(ChangeEvent e) {
            Object src = e.getSource();
            if (src == enableItem) {
                questionPanel.saveCurrentResponse();
                boolean on = enableItem.isSelected();
                setMarkersEnabled(on);
            }
            else if (src == filterItem) {
                questionPanel.saveCurrentResponse();
                boolean on = filterItem.isSelected();
                setMarkersFilterEnabled(on);
            }
        }

        // ---------- from MenuListener -----------

        public void menuSelected(MenuEvent e) {
            updateItems();
        }

        public void menuDeselected(MenuEvent e) {
        }

        public void menuCanceled(MenuEvent e) {
        }

        // ---------- from PopupMenuListener -----------

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            updateItems();
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
        }

        private int type;
        private JComponent comp;
        private JCheckBoxMenuItem enableItem;
        private JCheckBoxMenuItem filterItem;
        private JMenuItem markItem;
        private JMenuItem unmarkItem;
        private JMenuItem clearItem;
        private JMenuItem openGroupItem;
        private JMenuItem closeGroupItem;
        private JMenuItem clearMarkedItem;
        private JMenuItem removeAllItem;

        private static final String ENABLE = "enable";
        private static final String FILTER = "filter";
        private static final String MARK = "mark";
        private static final String UNMARK = "unmark";
        private static final String CLEAR = "clear";
        private static final String OPEN_GROUP = "open";
        private static final String CLOSE_GROUP = "close";

        private static final String CLEAR_MARKED = "clearMarked";
        private static final String REMOVE_MARKERS = "remove";
    }

    //-----------------------------------------------------------------------

    private static class MarkerIcon implements Icon
    {
        MarkerIcon(boolean on) {
            this.on = on;
        }

        public int getIconWidth() {
            return iconWidth;
        }

        public int getIconHeight() {
            return iconHeight;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (on) {
                if (image == null) {
                    image = new BufferedImage(getIconWidth(), getIconHeight(),
                                              BufferedImage.TYPE_INT_ARGB);
                    paintMe(image);
                }
                g.drawImage(image, x, y, null);
            }
        }

        private void paintMe(BufferedImage image) {
            Graphics g = image.getGraphics();

            int x0 = 0;
            int y0 = 0;

            int x1 = Math.min(iconWidth, iconHeight);
            int y1 = x1;

            int[] xx = {
                x0 + iconIndent,
                x1,
                x1,
                x1 - iconIndent,
                x0,
                x0 + iconIndent,
            };

            int[] yy = {
                y0,
                y1 - iconIndent,
                y1,
                y1,
                y0 + iconIndent,
                y0 + iconIndent
            };

            g.setColor(new Color(102, 102, 153));//g.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
            g.fillPolygon(xx, yy, xx.length);
        }

        private boolean on;
        private BufferedImage image;
        private static final int iconWidth = 8;
        private static final int iconHeight = 16;
        private static final int iconIndent = 3;
    }

    //-----------------------------------------------------------------------

    private static class EllipsisIcon implements Icon
    {
        public int getIconWidth() {
            return iconWidth;
        }

        public int getIconHeight() {
            return iconHeight;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (image == null) {
                image = new BufferedImage(getIconWidth(), getIconHeight(),
                                          BufferedImage.TYPE_INT_ARGB);
                paintMe(image);
            }
            g.drawImage(image, x, y, null);
        }

        private void paintMe(BufferedImage image) {
            Graphics g = image.getGraphics();
            g.setColor(Color.black);

            for (int iy = 0; iy < dotHeight; iy++) {
                int y = (iconHeight - dotHeight) / 2 + iy;
                for (int ix = 0; ix < dots; ix++) {
                    int x = dotIndent + ix * (dotWidth + dotSep);
                    g.drawLine(x, y, x + dotWidth - 1, y);
                }
            }
        }

        private BufferedImage image;
        private static final int iconWidth = 48;
        private static final int iconHeight = 6;
        private static final int dots = 3;
        private static final int dotWidth = 2;
        private static final int dotHeight = 1;
        private static final int dotSep = 4;
        private static final int dotIndent = 20;
    }

}
