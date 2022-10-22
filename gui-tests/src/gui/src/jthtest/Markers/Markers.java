/*
 * $Id$
 *
 * Copyright (c) 2001, 2022, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.Markers;

import jthtest.ConfigTools;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

public class Markers extends ConfigTools {

    public static Icon getIcon(JDialogOperator config, int index) {
    Icon icon = ((JLabel) new JListOperator(config).getRenderedComponent(index)).getIcon();
    return icon;
    }

    public static void pushEnableBookmarks(JDialogOperator config) {
    new JMenuOperator(config, "Bookmarks").pushMenu(new String[]{"Bookmarks", getExecResource("ct.markers.ckb")});
    }

    public static void pushShowOnlyBookmarked(JDialogOperator config) {
    new JMenuOperator(config, "Bookmarks").pushMenu(new String[]{"Bookmarks", "Show Only Bookmarked Questions"});
    }

    public static void pushClearBookmarkedAnswer(JDialogOperator config) {
    new JMenuOperator(config, "Bookmarks").pushMenu(new String[]{"Bookmarks", "Clear Answers For Current Question"});
    }

    public static void pushClearAllBookmarkedAnswers(JDialogOperator config) {
    new JMenuOperator(config, "Bookmarks").pushMenu(new String[]{"Bookmarks", "Clear Answers to Bookmarked Questions"});
    }

    public static void pushOpenHiddenGroupByMenu(JDialogOperator config) {
    new JMenuOperator(config, "Bookmarks").pushMenu(new String[]{"Bookmarks", "Open \". . .\" Group"});
    }

    public static void pushOpenHiddenGroupByMouse(JDialogOperator config, int index) {
    JListOperatorExt list = new JListOperatorExt(config);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 2, MouseEvent.BUTTON1_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    }

    public static void pushCloseHiddenGroupByMouse(JDialogOperator config, int index) {
    JListOperatorExt list = new JListOperatorExt(config);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 2, MouseEvent.BUTTON1_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    }

    public static void pushCloseByMenu(JDialogOperator config) {
    new JMenuOperator(config, "Bookmarks").pushMenu(new String[]{"Bookmarks", "Close \". . .\" Group"});
    }

    public static void pushCloseByPopup(JDialogOperator config, int index) {
    JListOperatorExt list = new JListOperatorExt(config);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(config).pushMenu("Close \". . .\" Group");
    }

    public static void pushOpenHiddenGroupByPopup(JDialogOperator config, int index) {
    JListOperatorExt list = new JListOperatorExt(config);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(config).pushMenu("Open \". . .\" Group");
    }

    public static void clearByMenu(JDialogOperator config, int index) {
    new JListOperator(config).selectItem(index);
    new JMenuOperator(config, "Bookmarks").pushMenu(new String[]{"Bookmarks", "Clear Answer For Current Question"});
    }

    public static void setBookmarkedByMenu(JDialogOperator config, int index) {
    new JListOperator(config).selectItem(index);
    new JMenuOperator(config, "Bookmarks").pushMenu(new String[]{"Bookmarks", "Mark Current Question"});
    }

    public static void unsetBookmarkedByMenu(JDialogOperator config, int index) {
    new JListOperator(config).selectItem(index);
    new JMenuOperator(config, "Bookmarks").pushMenu(new String[]{"Bookmarks", "Unmark Current Question"});
    }

    public static void setBookmarkedByMenu(JDialogOperator config, int[] indexes) {
    JMenuOperator bookmarksMenu = new JMenuOperator(config, "Bookmarks");
    String[] menuPath = new String[]{"Bookmarks", "Mark Current Question"};
    JListOperator list = new JListOperator(config);
    for (int index : indexes) {
        list.selectItem(index);
        bookmarksMenu.pushMenu(menuPath);
    }
    }

    public static void setBookmarkedByPopup(JDialogOperator config, int[] indexes) {
    for (int index : indexes)
        setBookmarkedByPopup(config, index);
    }

    public static void setBookmarkedByMouse(JDialogOperator config, int index) {
    JListOperatorExt list = new JListOperatorExt(config);
    list.clickOnItem(index, 1, MouseEvent.BUTTON1_MASK, 10);
    }

    public static String[] getElementsNames(JDialogOperator config, int[] indexes) {
    JListOperator list = new JListOperator(config);
    String temp[] = new String[indexes.length];
    for (int i = 0; i < indexes.length; i++) {
        temp[i] = ((JLabel) list.getRenderedComponent(indexes[i])).getText();
    }
    return temp;
    }

    public static String[] getElementsNames(JDialogOperator config) {
    JListOperator list = new JListOperator(config);
    String temp[] = new String[list.getModel().getSize()];
    for (int i = 0; i < list.getModel().getSize(); i++) {
        temp[i] = ((JLabel) list.getRenderedComponent(i)).getText();
    }
    return temp;
    }

    public static void setBookmarkedByPopup(JDialogOperator config, int index) {
    JListOperatorExt list = new JListOperatorExt(config);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(config).pushMenu("Mark Current Question");
    }

    public static void clickBookmarkedByMouse(JDialogOperator config, int index) {
    JListOperatorExt list = new JListOperatorExt(config);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(config).pushMenu("Mark Current Question");
    }

    public static void unsetBookmarkedByPopup(JDialogOperator config, int index) {
    JListOperatorExt list = new JListOperatorExt(config);
    list.clickOnItem(index, 1);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(config).pushMenu("Unmark Current Question");
    }

    public static void clearByPopup(JDialogOperator config, int index) {
    JListOperatorExt list = new JListOperatorExt(config);
    new JListOperator(config).selectItem(index);
    if (list.clickOnItem(index, 1, MouseEvent.BUTTON3_MASK) == null) {
        throw new JemmyException("Error occured while clicking on " + index + " item of list " + list);
    }
    new JPopupMenuOperator(config).pushMenu("Clear Answer For Current Question");
    }

    public static int[] checkVisibility(JDialogOperator config, String[] names) {
    JListOperator list = new JListOperator(config);

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

    public static void checkVisibilityAll(JDialogOperator config, String[] names) {
    JListOperator list = new JListOperator(config);
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

    public static void checkHiddenGroups(JDialogOperator config, int[] indexes, String[] names) {
    JListOperator list = new JListOperator(config);

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

    public static void openGroupByMenu(JDialogOperator config, String[] namesAll, String[] namesHidden) {
    JListOperator list = new JListOperator(config);

    int i;
    for (i = 0; i < list.getModel().getSize(); i++) {
        if (((JLabel) list.getRenderedComponent(i)).getText() == null) {
        break;
        }
    }

    list.selectItem(i);
    pushOpenHiddenGroupByMenu(config);

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

    public static void openGroupByMouse(JDialogOperator config, String[] namesAll, String[] namesHidden) {
    JListOperator list = new JListOperator(config);

    int i;
    for (i = 0; i < list.getModel().getSize(); i++) {
        if (((JLabel) list.getRenderedComponent(i)).getText() == null) {
        break;
        }
    }

    pushOpenHiddenGroupByMouse(config, i);

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

    public static void openGroupByPopup(JDialogOperator config, String[] namesAll, String[] namesHidden) {
    JListOperator list = new JListOperator(config);

    int i;
    for (i = 0; i < list.getModel().getSize(); i++) {
        if (((JLabel) list.getRenderedComponent(i)).getText() == null) {
        break;
        }
    }

    pushOpenHiddenGroupByPopup(config, i);

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

    public static void closeGroupByMenu(JDialogOperator config, String[] namesAll, String[] namesHidden) {
    JListOperator list = new JListOperator(config);

    int i;
    for (i = 0; i < namesHidden.length; i++) {
        if (namesHidden[i] == null) {
        break;
        }
    }

    list.selectItem(i);
    pushCloseByMenu(config);

    if (((JLabel) list.getRenderedComponent(i)).getText() != null) {
        throw new JemmyException("Element " + i + " isn't '...' after closing");
    }
    if (!(((JLabel) list.getRenderedComponent(i + 1)).getText().equals(namesHidden[i + 1]))) {
        throw new JemmyException("Element " + (i + 1) + " isn't " + namesHidden[i + 1] + " after closing");
    }
    }

    public static void closeGroupByPopup(JDialogOperator config, String[] namesAll, String[] namesHidden) {
    JListOperator list = new JListOperator(config);

    int i;
    for (i = 0; i < namesHidden.length; i++) {
        if (namesHidden[i] == null) {
        break;
        }
    }

    pushCloseByPopup(config, i);

    if (((JLabel) list.getRenderedComponent(i)).getText() != null) {
        throw new JemmyException("Element " + i + " isn't '...' after closing");
    }
    if (!(((JLabel) list.getRenderedComponent(i + 1)).getText().equals(namesHidden[i + 1]))) {
        throw new JemmyException("Element " + (i + 1) + " isn't " + namesHidden[i + 1] + " after closing");
    }
    }

    public static void closeGroupByMouse(JDialogOperator config, String[] namesAll, String[] namesHidden) {
    JListOperator list = new JListOperator(config);

    int i;
    for (i = 0; i < namesHidden.length; i++) {
        if (namesHidden[i] == null) {
        break;
        }
    }

    pushCloseHiddenGroupByMouse(config, i);

    if (((JLabel) list.getRenderedComponent(i)).getText() != null) {
        throw new JemmyException("Element " + i + " isn't '...' after closing");
    }
    if (!(((JLabel) list.getRenderedComponent(i + 1)).getText().equals(namesHidden[i + 1]))) {
        throw new JemmyException("Element " + (i + 1) + " isn't " + namesHidden[i + 1] + " after closing");
    }
    }

    public static boolean checkBookmarked(JDialogOperator config, int index) {
    JListOperator list = new JListOperator(config);
    return ((JLabel)list.getRenderedComponent(index)).getIcon() != null;
    }

    static class JListOperatorExt extends JListOperator {

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
}
