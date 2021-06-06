/*
 * $Id$
 *
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.tools;

import com.sun.interview.wizard.selectiontree.SelectionTree;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.UIManager;
import jthtest.Tools;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author at231876
 */
public class ConfigDialog {

    private JDialogOperator configDialog;
    private JTFrame mainFrame;
    public static final String DONE_BUTTON = "Done";
    public static final String NEXT_BUTTON = "Next";
    public static final String PREV_BUTTON = "Back";
    public static final String LAST_BUTTON = "Last";
    public static final String CONFIG_LOADER_DIALOG_NAME_EI18N = "wdc.loadconfig";
    public static final String CONFIG_LOADER_LOAD_BUTTON_NAME_EI18N = "wdc.load.btn";
    public static final String CONFIG_LOADER_CONFIG_LOCATION_DIALOG_EI18N = "wdc.configchoosertitle";
    public static final String CONFIG_LOADER_BROWSE_BUTTON = "Browse";
    public static final String CONFIG_EDITOR_DIALOG_NAME_EI18N = "ce.name";
    public static final String SAVE_CONFIG_FILE_DIALOG_NAME_EI18N = "ce.save.title";
    public static final String CONFIG_CONFIRN_CLOSE_DIALOG_NAME_EI18N = "ce.okToClose.title";
    public static final String MENU_FILE_CLOSE_NAME_EI18N = "ce.file.close.mit";
    public static final String MENU_FILE_SAVE_NAME_EI18N = "ce.file.save.mit";
    public static final String MENU_FILE_NEW_NAME_EI18N = "ce.file.new.mit";
    public static final String MENU_FILE_NAME_EI18N = "ce.file.menu";
    public static final String MENU_FILE_LOAD_NAME_EI18N = "ce.file.load.mit";
    public static final String MENU_FILE_LOAD_RECENT_NAME_EI18N = "ce.history.menu";
    public static final String BOOKMARKS_MENU_NAME_EI18N = "ct.markers.ckb";

    public static final String BOOKMARKS_MENU_NAME = "Bookmarks";
    public static final String BOOKMARKS__SHOW_ONLY_BOOKMARKED_MENU_NAME = "Show Only Bookmarked Questions";
    public static final String BOOKMARKS__MARK_CURRENT_QUESTION_MENU_NAME = "Mark Current Question";
    public static final String BOOKMARKS__UNMARK_CURRENT_QUESTION_MENU_NAME = "Unmark Current Question";
    public static final String BOOKMARKS__CLOSE_HIDDEN_GROUP = "Close \". . .\" Group";
    public static final String BOOKMARKS__OPEN_HIDDEN_GROUP_MENU_NAME = "Open \". . .\" Group";
    public static final String BOOKMARKS__CLEAR_ANSWER_FOR_CURRENT_MENU_NAME = "Clear Answer For Current Question";
    public static final String BOOKMARKS__CLEAR_ANSWER_TO_BOOKMARKED_QUESTIONS_MENU_NAME = "Clear Answers to Bookmarked Questions";

    ConfigDialog(JTFrame mainFrame) {
    this.mainFrame = mainFrame;
    configDialog = new JDialogOperator(mainFrame.getJFrameOperator(), getConfigEditorDialogName());
    }

    public static String getConfigEditorDialogName() {
    return Tools.getExecResource(CONFIG_EDITOR_DIALOG_NAME_EI18N);
    }

    public static String getSaveConfigurationDialogName() {
    return Tools.getExecResource(SAVE_CONFIG_FILE_DIALOG_NAME_EI18N);
    }

    public static String getConfirmCloseIncomleteConfigurationDialogName() {
    return Tools.getExecResource(CONFIG_CONFIRN_CLOSE_DIALOG_NAME_EI18N);
    }

    public JButtonOperator getBackButton() {
        return new JButtonOperator(configDialog, PREV_BUTTON);
    }

    public JListOperator getJListOperator() {
        if (_list == null)
            _list = new JListOperator(configDialog);
        return _list;
    }

    public void refindList() {
        _list = null;
    }

    private JListOperator _list = null;

    public ConfigurationBrowser openBrowser(boolean block) {
    if (block) {
        getFile_LoadConfigurationMenu().push();
    } else {
        getFile_LoadConfigurationMenu().pushNoBlock();
    }

    return ConfigurationBrowser.open();
    }

    public void load(String name, boolean block) {
    load(Tools.DEFAULT_PATH, name, block);
    }

    public void load(String path, String name, boolean block) {
    ConfigurationBrowser browser = openBrowser(block);
    browser.setPath(path + File.separator + name);
    browser.commit();
    }

    public void pushDoneConfigEditor() {
    new JButtonOperator(configDialog, DONE_BUTTON).push();
    }

    public void pushNextConfigEditor() {
    new JButtonOperator(configDialog, NEXT_BUTTON).push();
    }

    public void pushBackConfigEditor() {
    getBackButton().push();
    }

    public void pushLastConfigEditor() {
    new JButtonOperator(configDialog, LAST_BUTTON).push();
    }

    public void selectQuestion(int index) {
    getJListOperator().selectItem(index);
    }

    public void saveConfig(String name) {
    getFile_SaveMenu().pushNoBlock();
    JDialogOperator saving = new JDialogOperator(getSaveConfigurationDialogName());

    JTextFieldOperator tf;

    tf = new JTextFieldOperator((JTextField) Tools.getComponent(saving, new String[]{"Folder name:", "File name:", "Folder Name:", "File Name:"}));
    tf.enterText(name);
    }

    public boolean isFullConfiguration() {
    JListOperator list = getJListOperator();
    return ((JLabel) list.getRenderedComponent(list.getModel().getSize() - 1)).getText().equals(" Congratulations!");
    }

    public void closeByMenu() {
        _list = null;
    getFile_CloseMenu().push();
    }

    public void setBookmarkedByMenu(int index) {
    getJListOperator().selectItem(index);
        getBookmarks_MarkCurrentQuestion_Menu().push();
    }

    public JMenuItemOperator getBookmarks_ShowOnlyBookmarkedMenu() {
        return getMenu().showMenuItem(new String[] {BOOKMARKS_MENU_NAME, BOOKMARKS__SHOW_ONLY_BOOKMARKED_MENU_NAME});
    }

    public JMenuItemOperator getBookmarks_MarkCurrentQuestion_Menu() {
        return getMenu().showMenuItem(new String[] {BOOKMARKS_MENU_NAME, BOOKMARKS__MARK_CURRENT_QUESTION_MENU_NAME});
    }

    public JMenuItemOperator getBookmarks_UnmarkCurrentQuestion_Menu() {
        return getMenu().showMenuItem(new String[] {BOOKMARKS_MENU_NAME, BOOKMARKS__UNMARK_CURRENT_QUESTION_MENU_NAME});
    }

    public JMenuItemOperator getBookmarksMenu() {
        return getMenu().showMenuItem(new String[] {BOOKMARKS_MENU_NAME});
    }

    public void unsetBookmarkedByMenu(int index) {
    getJListOperator().selectItem(index);
        getBookmarks_UnmarkCurrentQuestion_Menu().push();
    }

    public void setBookmarkedByMenu(int[] indexes) {
    JMenuItemOperator markQuestionMenu = null;
    String[] menuPath = new String[]{"Bookmarks", "Mark Current Question"};
    JListOperator list = getJListOperator();
    for (int index : indexes) {
        list.selectItem(index);
            if (markQuestionMenu == null)
                markQuestionMenu = getBookmarks_MarkCurrentQuestion_Menu();
        markQuestionMenu.push();
    }
    }

    public void setBookmarkedByPopup(int[] indexes) {
    for (int index : indexes)
        setBookmarkedByPopup(index);
    }

    public void setBookmarkedByMouse(int index) {
    JListOperatorExt list = new JListOperatorExt(configDialog);
    list.clickOnItem(index, 1, MouseEvent.BUTTON1_MASK, 6);
    }

    public void setBookmarkedByPopup(int index) {
    JListOperatorExt list = new JListOperatorExt(configDialog);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(configDialog).pushMenu("Mark Current Question");
    }

    public void clickBookmarkedByMouse(int index) {
    JListOperatorExt list = new JListOperatorExt(configDialog);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(configDialog).pushMenu("Mark Current Question");
    }

    public void unsetBookmarkedByPopup(int index) {
    JListOperatorExt list = new JListOperatorExt(configDialog);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(configDialog).pushMenu("Unmark Current Question");
    }

    public void clearByPopup(int index) {
    JListOperatorExt list = new JListOperatorExt(configDialog);
    getJListOperator().selectItem(index);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(configDialog).pushMenu("Clear Answer For Current Question");
    }

    public int[] checkVisibility(String[] names) {
    JListOperator list = getJListOperator();

    int marks[] = new int[names.length];
    for (int mark : marks) {
        mark = -1;
    }
    for (int i = 0; i < list.getModel().getSize(); i++) {
        String text = ((JLabel) list.getRenderedComponent(i)).getText();
        if (text != null) {
        for (int j = 0; j < names.length; j++) {
            if (text.equals(names[j])) {
            marks[j] = i;
            }
        }
        }
    }

    for (int i = 0; i < marks.length; i++) {
        if (marks[i] == -1) {
        throw new JemmyException("Element with name '" + names[i] + "' wasn't found");
        }
    }

    return marks;
    }

    public void checkVisibilityAll(String[] names) {
    JListOperator list = getJListOperator();
    for (int i = 0; i < list.getModel().getSize(); i++) {
        String text = ((JLabel) list.getRenderedComponent(i)).getText();
        if (text == null) {
        throw new JemmyException("Element N" + i + " hasn't any name while expected '" + names[i] + "'");
        }
        if (!text.equals(names[i])) {
        throw new JemmyException("Element N" + i + " has name '" + list.getRenderedComponent(i).getName() + "' while expected '" + names[i] + "'");
        }
    }
    }

    public void checkHiddenGroups(int[] indexes, String[] names) {
    JListOperator list = getJListOperator();

    if (indexes.length > 0) {
        int temp = 0;

        for (int i = 0; i < indexes.length; i++) {
        if (indexes[i] - temp > 1) {
            if (((JLabel) list.getRenderedComponent(indexes[i] - 1)).getText() != null) {
            throw new JemmyException("element " + (indexes[i] - 1) + " isn't '...'");
            }
        }
        temp = indexes[i];
        }
    }
    }

    public void pushOpenHiddenGroupByMouse(int index) {
    JListOperatorExt list = new JListOperatorExt(configDialog);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 2, MouseEvent.BUTTON1_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    }

    private void pushOpenHiddenGroupByMenu() {
        getBookmarks_OpenHiddenGroupMenu().push();
    }

    public JMenuItemOperator getBookmarks_OpenHiddenGroupMenu() {
        return getMenu().showMenuItem(new String[] {BOOKMARKS_MENU_NAME, BOOKMARKS__OPEN_HIDDEN_GROUP_MENU_NAME});
    }

    public void openGroupByMenu(String[] namesAll, String[] namesHidden) {
    JListOperator list = getJListOperator();

    int i;
    for (i = 0; i < list.getModel().getSize(); i++) {
        if (((JLabel) list.getRenderedComponent(i)).getText() == null) {
        break;
        }
    }

    list.selectItem(i);
    pushOpenHiddenGroupByMenu();

    for (int j = i; j < list.getModel().getSize(); j++) {
        String text = ((JLabel) list.getRenderedComponent(j)).getText();
        if (!(text.equals(namesAll[j]))) {
        throw new JemmyException("Element " + j + " is '" + text + "' after opening '...' group while expected '" + namesAll[j] + "'");
        }
        if (text.equals(namesHidden[i + 1])) {
        break;
        }
    }
    }

    public void openGroupByMouse(String[] namesAll, String[] namesHidden) {
    JListOperator list = getJListOperator();

    int i;
    for (i = 0; i < list.getModel().getSize(); i++) {
        if (((JLabel) list.getRenderedComponent(i)).getText() == null) {
        break;
        }
    }

    pushOpenHiddenGroupByMouse(i);

    for (int j = i; j < list.getModel().getSize(); j++) {
        String text = ((JLabel) list.getRenderedComponent(j)).getText();
        if (!(text.equals(namesAll[j]))) {
        throw new JemmyException("Element " + j + " is '" + text + "' after opening '...' group while expected '" + namesAll[j] + "'");
        }
        if (text.equals(namesHidden[i + 1])) {
        break;
        }
    }
    }

    public void pushOpenHiddenGroupByPopup(int index) {
    JListOperatorExt list = new JListOperatorExt(configDialog);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(configDialog).pushMenu("Open \". . .\" Group");
    }

    public void openGroupByPopup(String[] namesAll, String[] namesHidden) {
    JListOperator list = getJListOperator();

    int i;
    for (i = 0; i < list.getModel().getSize(); i++) {
        if (((JLabel) list.getRenderedComponent(i)).getText() == null) {
        break;
        }
    }

    pushOpenHiddenGroupByPopup(i);

    for (int j = i; j < list.getModel().getSize(); j++) {
        String text = ((JLabel) list.getRenderedComponent(j)).getText();
        if (!(text.equals(namesAll[j]))) {
        throw new JemmyException("Element " + j + " is '" + text + "' after opening '...' group while expected '" + namesAll[j] + "'");
        }
        if (text.equals(namesHidden[i + 1])) {
        break;
        }
    }

    }

    public JMenuItemOperator getBookmarks_CloseHiddenGroupMenu() {
        return getMenu().showMenuItem(new String[] {BOOKMARKS_MENU_NAME, BOOKMARKS__CLOSE_HIDDEN_GROUP});
    }

    public void pushCloseGroupByMenu() {
    getBookmarks_CloseHiddenGroupMenu().push();
    }

    public void closeGroupByMenu(String[] namesAll, String[] namesHidden) {
    JListOperator list = getJListOperator();

    int i;
    for (i = 0; i < namesHidden.length; i++) {
        if (namesHidden[i] == null) {
        break;
        }
    }

    list.selectItem(i);
    pushCloseGroupByMenu();

    if (((JLabel) list.getRenderedComponent(i)).getText() != null) {
        throw new JemmyException("Element " + i + " isn't '...' after closing");
    }
    if (!(((JLabel) list.getRenderedComponent(i + 1)).getText().equals(namesHidden[i + 1]))) {
        throw new JemmyException("Element " + (i + 1) + " isn't " + namesHidden[i + 1] + " after closing");
    }
    }

    public void pushCloseByPopup(int index) {
    JListOperatorExt list = new JListOperatorExt(configDialog);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(configDialog).pushMenu("Close \". . .\" Group");
    }

    public void closeGroupByPopup(String[] namesAll, String[] namesHidden) {
    JListOperator list = getJListOperator();

    int i;
    for (i = 0; i < namesHidden.length; i++) {
        if (namesHidden[i] == null) {
        break;
        }
    }

    pushCloseByPopup(i);

    if (((JLabel) list.getRenderedComponent(i)).getText() != null) {
        throw new JemmyException("Element " + i + " isn't '...' after closing");
    }
    if (!(((JLabel) list.getRenderedComponent(i + 1)).getText().equals(namesHidden[i + 1]))) {
        throw new JemmyException("Element " + (i + 1) + " isn't " + namesHidden[i + 1] + " after closing");
    }
    }

    public void pushCloseHiddenGroupByMouse(int index) {
    JListOperatorExt list = new JListOperatorExt(configDialog);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 2, MouseEvent.BUTTON1_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    }

    public void closeGroupByMouse(String[] namesAll, String[] namesHidden) {
    JListOperator list = getJListOperator();

    int i;
    for (i = 0; i < namesHidden.length; i++) {
        if (namesHidden[i] == null) {
        break;
        }
    }

    pushCloseHiddenGroupByMouse(i);

    if (((JLabel) list.getRenderedComponent(i)).getText() != null) {
        throw new JemmyException("Element " + i + " isn't '...' after closing");
    }
    if (!(((JLabel) list.getRenderedComponent(i + 1)).getText().equals(namesHidden[i + 1]))) {
        throw new JemmyException("Element " + (i + 1) + " isn't " + namesHidden[i + 1] + " after closing");
    }
    }

    public boolean checkBookmarked(int index) {
    JListOperator list = getJListOperator();
    return ((JLabel)list.getRenderedComponent(index)).getIcon() != null;
    }

    public JMenuItemOperator getBookmarks_ClearAnswerForCurrentQuestionMenu() {
        return getMenu().showMenuItem(new String[] {BOOKMARKS_MENU_NAME, BOOKMARKS__CLEAR_ANSWER_FOR_CURRENT_MENU_NAME});
    }

    public JMenuItemOperator getBookmarks_ClearAnswerToBookmarkedQuestionsMenu() {
        return getMenu().showMenuItem(new String[] {BOOKMARKS_MENU_NAME, BOOKMARKS__CLEAR_ANSWER_TO_BOOKMARKED_QUESTIONS_MENU_NAME});
    }

    public void clearByMenu(int index) {
    getJListOperator().selectItem(index);
    getBookmarks_ClearAnswerForCurrentQuestionMenu().push();
    }

    public Icon getIcon(int index) {
    Icon icon = ((JLabel) getJListOperator().getRenderedComponent(index)).getIcon();
    return icon;
    }

    public boolean isSelectedIndex(int i) {
        return getJListOperator().isSelectedIndex(i);
    }

    public int getSelectedQuestionNumber() {
        return getJListOperator().getSelectedIndex();
    }

    public static class JListOperatorExt extends JListOperator {

    public JListOperatorExt(ContainerOperator c) {
        super(c);
    }

    public Object clickOnItem(final int itemIndex, final int clickCount, final int mouseButton) {
        if (itemIndex > getModel().getSize()) {
        throw new JemmyException("bad index");
        }
        scrollToItem(itemIndex);

        if (((JList) getSource()).getModel().getSize() <= itemIndex) {
        return (null);
        }
        if (((JList) getSource()).getAutoscrolls()) {
        ((JList) getSource()).ensureIndexIsVisible(itemIndex);
        }
        return (getQueueTool().invokeSmoothly(new QueueTool.QueueAction("Path selecting") {

        public Object launch() {
            Rectangle rect = getCellBounds(itemIndex, itemIndex);
            if (rect == null) {
            return (null);
            }
            Point point = new Point((int) (rect.getX() + rect.getWidth() / 2),
                (int) (rect.getY() + rect.getHeight() / 2));
            Object result = getModel().getElementAt(itemIndex);
            clickMouse(point.x, point.y, clickCount, mouseButton);
            return (result);
        }
        }));
    }

    public Object clickOnItem(final int itemIndex, final int clickCount, final int mouseButton, final int x) {
        if (itemIndex > getModel().getSize()) {
        throw new JemmyException("bad index");
        }
        scrollToItem(itemIndex);

        if (((JList) getSource()).getModel().getSize() <= itemIndex) {
        return (null);
        }
        if (((JList) getSource()).getAutoscrolls()) {
        ((JList) getSource()).ensureIndexIsVisible(itemIndex);
        }
        return (getQueueTool().invokeSmoothly(new QueueTool.QueueAction("Path selecting") {

        public Object launch() {
            Rectangle rect = getCellBounds(itemIndex, itemIndex);
            if (rect == null) {
            return (null);
            }
            Point point = new Point((int) (rect.getX() + x == 0 ? rect.getWidth() / 2:x),
                (int) (rect.getY() + rect.getHeight() / 2));
            Object result = getModel().getElementAt(itemIndex);
            clickMouse(point.x, point.y, clickCount, mouseButton);
            return (result);
        }
        }));
    }
    }

    public QuestionTree getQuestionTree() {
        String[] names = getElementsNames();
        int index = -1;
        for (int i = 0; i < names.length; i++) {
            if ("Specify Tests to Run?".equals(names[i].trim())) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new RuntimeException("Error: 'Specify Tests to Run?' question was not found, can't execute test");
        }

        selectQuestion(index);
        JRadioButtonOperator rb = new JRadioButtonOperator(getConfigDialog());
        rb.push();
        pushNextConfigEditor();
        pushNextConfigEditor();

        return new QuestionTree(configDialog);
    }

    public class QuestionTree extends JTreeOperator {

        QuestionTree(JDialogOperator config) {
            super(config);
        }

        public void clickOnCheckbox(int row) {
            Rectangle r = getRowBounds(row);
            prepareToClickOnRow(row);
            super.clickMouse((int)r.getX() + 6, (int)r.getY() + (int)(r.getHeight() / 2), 1, getDefaultMouseButton(), 0, false);
//            Tools.pause(1);
        }

        public void prepareToClickOnRow(int row) {
            makeComponentVisible();
            scrollToRow(row);
            makeVisible(getPathForRow(row));
            if(!isVisible(getPathForRow(row))) {
                System.out.println("Error, row is not visible after prepairing. Index " + row + ", tree width " + getWidth());
            }
        }

        public void clickOnCheckbox(int row, int count) {
            Rectangle r = getRowBounds(row);
            prepareToClickOnRow(row);
            super.clickMouse((int)r.getX() + 6, (int)r.getY() + (int)(r.getHeight() / 2), count, getDefaultMouseButton(), 0, false);
//            Tools.pause(1);
        }

        public void clickOnCheckbox(int row, int x, int y, int count, int mouseButton, int modifier, boolean forPopup) {
            Rectangle r = getRowBounds(row);
            prepareToClickOnRow(row);
            super.clickMouse((int)r.getX() + 6 + x, (int)r.getY() + (int)(r.getHeight() / 2) + y, count, mouseButton, modifier, forPopup);
//            Tools.pause(1);
        }

        public void clickOnRow(int row) {
            Rectangle r = getRowBounds(row);
            prepareToClickOnRow(row);
            super.clickMouse((int)r.getX() + 14, (int)r.getY() + (int)(r.getHeight() / 2), 1, getDefaultMouseButton(), 0, false);
//            Tools.pause(1);
        }

        public void clickOnRow(int row, int count) {
            Rectangle r = getRowBounds(row);
            prepareToClickOnRow(row);
            super.clickMouse((int)r.getX() + 14, (int)r.getY() + (int)(r.getHeight() / 2), count, getDefaultMouseButton(), 0, false);
//            Tools.pause(1);
        }

        public void clickOnRow(int row, int x, int y, int count, int mouseButton, int modifier, boolean forPopup) {
            Rectangle r = getRowBounds(row);
            prepareToClickOnRow(row);
            super.clickMouse((int)r.getX() + 14 + x, (int)r.getY() + (int)(r.getHeight() / 2) + y, count, mouseButton, modifier, forPopup);
//            Tools.pause(1);
        }

        public void clickOnArrow(int row) {
            boolean isNimbus = UIManager.getLookAndFeel().getClass().getSimpleName().equals("NimbusLookAndFeel");
            if (row > 0 || isNimbus) {
                Rectangle r = getRowBounds(row);
                prepareToClickOnRow(row);
                if (isNimbus) {
                    super.clickMouse((int)r.getX() + 1, (int)r.getY() + (int)(r.getHeight() / 2), 1, getDefaultMouseButton(), 0, false);
                } else {
                    super.clickMouse((int)r.getX() - 9, (int)r.getY() + (int)(r.getHeight() / 2), 1, getDefaultMouseButton(), 0, false);
                }
            } else {
                System.out.println("WARNING: NimbusLAF is not installed. clickOnArrow method is not possible as there is no arrow on root element in MetalLAF. Using double-click on row");
                clickOnRow(row, 2);
            }
//            Tools.pause(1);
        }

        public void clickOnArrow(int row, int count) {
            boolean isNimbus = UIManager.getLookAndFeel().getClass().getSimpleName().equals("NimbusLookAndFeel");
            if (row > 0 || isNimbus) {
                Rectangle r = getRowBounds(row);
                prepareToClickOnRow(row);
                if (isNimbus) {
                    super.clickMouse((int)r.getX() + 1, (int)r.getY() + (int)(r.getHeight() / 2), count, getDefaultMouseButton(), 0, false);
                } else {
                    super.clickMouse((int)r.getX() - 9, (int)r.getY() + (int)(r.getHeight() / 2), count, getDefaultMouseButton(), 0, false);
                }
            } else {
                System.out.println("WARNING: NimbusLAF is not installed. clickOnArrow method is not possible as there is no arrow on root element in MetalLAF. Using double-click on row");
                clickOnRow(row, 2);
            }
//            Tools.pause(1);
        }

        public void clickOnArrow(int row, int x, int y, int count, int mouseButton, int modifier, boolean forPopup) {
            boolean isNimbus = UIManager.getLookAndFeel().getClass().getSimpleName().equals("NimbusLookAndFeel");
            if (row > 0 || isNimbus) {
                Rectangle r = getRowBounds(row);
                prepareToClickOnRow(row);
                if (isNimbus) {
                    super.clickMouse((int)r.getX() + 1 + x, (int)r.getY() + (int)(r.getHeight() / 2) + y, count, mouseButton, modifier, forPopup);
                } else {
                    super.clickMouse((int)r.getX() - 9 + x, (int)r.getY() + (int)(r.getHeight() / 2) + y, count, mouseButton, modifier, forPopup);
                }
            } else {
                System.out.println("WARNING: NimbusLAF is not installed. clickOnArrow method is not possible as there is no arrow on root element in MetalLAF. Using double-click on row");
                clickOnRow(row, 2);
            }
//            Tools.pause(1);
        }

        public SelectionTree getTree() {
            return (SelectionTree) super.getSource();
        }

        public PopupMenu openContextMenu(int row) {
            if (row < 0) {
                makeComponentVisible();
                Rectangle r = getRowBounds(0);
                clickForPopup((int)r.getX() + (int)r.getWidth() + 1, 1);
                return new PopupMenu(new JPopupMenuOperator());
            } else {
                makeComponentVisible();
                scrollToRow(row);
                clickOnRow(row, 0, 0, 1, getPopupMouseButton(), 0, true);
                return new PopupMenu(new JPopupMenuOperator());
            }
        }

        public class PopupMenu {
            JPopupMenuOperator menu;

            PopupMenu(JPopupMenuOperator menu) {
                this.menu = menu;
            }

            public void pushSelectAll() {
                menu.pushMenu("Select All");
            }

            public void pushDeselectAll() {
                menu.pushMenu("Deselect All");
            }

            public void pushExpandAll() {
                menu.pushMenu("Expand All");
            }

            public void pushCollapseAll() {
                menu.pushMenu("Collapse All");
            }
        }
    }

    public JDialogOperator getConfigDialog() {
    return configDialog;
    }

    public JMenuBarOperator getMenu() {
    return new JMenuBarOperator(configDialog);
    }

    public JMenuItemOperator getFile_SaveMenu() {
    return getMenu().showMenuItem(new String[]{getFileMenuName(), getFile_SaveMenuName()});
    }

    public JMenuItemOperator getFile_CloseMenu() {
    return getMenu().showMenuItem(new String[]{getFileMenuName(), getFile_CloseMenuName()});
    }

    public JMenuItemOperator getFile_NewConfigurationMenu() {
    return getMenu().showMenuItem(new String[]{getFileMenuName(), getFile_NewMenuName()});
    }

    public JMenuItemOperator getFile_LoadConfigurationMenu() {
    return getMenu().showMenuItem(new String[]{getFileMenuName(), getFile_LoadConfigurationMenuName()});
    }

    public JMenuItemOperator getFile_LoadRecentConfigurationMenu() {
    return getMenu().showMenuItem(new String[]{getFileMenuName(), getFile_LoadRecentConfigurationMenuName()});
    }

    public JMenuItemOperator[] getFile_LoadRecentConfiguration_subMenu() {
    return getMenu().showMenuItems(new String[]{getFileMenuName(), getFile_LoadRecentConfigurationMenuName()});
    }

    public JMenuItemOperator getBookmarks_EnableBookmarks() {
    return getMenu().showMenuItem(new String[]{"Bookmarks", getBookmarks_EnableBookmarksMenuName()});
    }

    public String[] getElementsNames() {
    JListOperator list = getJListOperator();
    String temp[] = new String[list.getModel().getSize()];
    for (int i = 0; i < list.getModel().getSize(); i++) {
        temp[i] = ((JLabel) list.getRenderedComponent(i)).getText();
    }
    return temp;
    }

    public String[] getElementsNames(int[] indexes) {
    JListOperator list = getJListOperator();
    String temp[] = new String[indexes.length];
    for (int i = 0; i < indexes.length; i++) {
        temp[i] = ((JLabel) list.getRenderedComponent(indexes[i])).getText();
    }
    return temp;
    }

    public static String getBookmarks_EnableBookmarksMenuName() {
        return Tools.getExecResource(BOOKMARKS_MENU_NAME_EI18N);
    }

    public static String getLoadConfigurationDialogName() {
    return Tools.getExecResource(CONFIG_LOADER_DIALOG_NAME_EI18N);
    }

    public static String getConfigLocationDialogName() {
    return Tools.getExecResource(CONFIG_LOADER_CONFIG_LOCATION_DIALOG_EI18N);
    }

    public static String getConfigLoaderLoadButtonName() {
    return Tools.getExecResource(CONFIG_LOADER_LOAD_BUTTON_NAME_EI18N);
    }

    public static String getFileMenuName() {
    return Tools.getExecResource(MENU_FILE_NAME_EI18N);
    }

    public static String getFile_SaveMenuName() {
    return Tools.getExecResource(MENU_FILE_SAVE_NAME_EI18N);
    }

    public static String getFile_NewMenuName() {
    return Tools.getExecResource(MENU_FILE_NEW_NAME_EI18N);
    }

    public static String getFile_CloseMenuName() {
    return Tools.getExecResource(MENU_FILE_CLOSE_NAME_EI18N);
    }

    public static String getFile_LoadConfigurationMenuName() {
    return Tools.getExecResource(MENU_FILE_LOAD_NAME_EI18N);
    }

    public static String getFile_LoadRecentConfigurationMenuName() {
    return Tools.getExecResource(MENU_FILE_LOAD_RECENT_NAME_EI18N);
    }
}
