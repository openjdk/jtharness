/*
 * $Id$
 *
 * Copyright (c) 2009, 2010, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.menu;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import jthtest.Test;
import jthtest.Tools;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import static jthtest.menu.Menu.*;

/**
 *
 * @author at231876
 */
public class Menu01 extends Test {

    public void testImpl() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, Exception {
    Tools.startJavatestNewDesktop();

    JFrameOperator mainFrame = Tools.findMainFrame();
    JMenuBarOperator menu = getMenuBar(mainFrame);

    MenuElement[] menuElements = menu.getSubElements();
    int menuCount = menuElements.length;
    if (menuCount != 4) {
        StringBuilder message = new StringBuilder("Found less then 4 menu elements. Expected ");
        message.append(getFileMenuName()).append(", ");
        message.append(getToolsMenuName()).append(", ");
        message.append(getWindowsMenuName()).append(", ");
        message.append(getHelpMenuName());
        message.append(", found: ");
        for (int i = 0; i < menuCount; i++) {
        message.append(((JMenuItem) menuElements[i].getComponent()).getText());
        }
        errors.add(message.toString());
    }

    if (menuCount > 0) {
        JMenuOperator item = new JMenuOperator((JMenu) (menuElements[0].getComponent()));

        String menuName = getFileMenuName();

        if (!menuName.equals(item.getText())) {
        errors.add("First menu element is not " + menuName + ". Found " + item.getText());
        } else {
        JMenuItemOperator[] elements = menu.showMenuItems(menuName);
        if (elements.length != 6) {
            StringBuilder message = new StringBuilder("Expected 8 File menu subelements: ");
            message.append(getFile_OpenQuickStartMenuName()).append(", ").append(getFile_OpenMenuName()).append(", ");
            message.append(getFile_RecentWorkDirectoryMenuName()).append(", ").append(getFile_PreferencesMenuName());
            message.append(", ").append(getFile_CloseMenuName()).append(", ").append(getFile_ExitMenuName()).append(". Found: ");
            for (JMenuItemOperator e : elements) {
            message.append(((JMenuItem) e.getComponent()).getText()).append("; ");
            }

            errors.add(message.toString());
        }
        if (elements.length > 0) {
            JMenuItem subitem = (JMenuItem) elements[0].getComponent();
            if (!getFile_OpenQuickStartMenuName().equals(subitem.getText())) {
            errors.add("First menu subelement of File menu is " + subitem.getText() + " while expected " + getFile_OpenQuickStartMenuName());
            }
        }
        if (elements.length > 1) {
            JMenuItem subitem = (JMenuItem) elements[1].getComponent();
            if (!getFile_OpenMenuName().equals(subitem.getText())) {
            errors.add("Second menu subelement of File menu is " + subitem.getText() + " while expected " + getFile_OpenMenuName());
            } else {
            JMenuItemOperator openSubElements[] = menu.showMenuItems(new String[]{menuName, "Open"}, new Tools.SimpleStringComparator());

            if (openSubElements.length != 2) {
                errors.add("File->Open menu contains " + openSubElements.length + " while expected 2. ");
            }

            if (openSubElements.length > 0) {
                JMenuItemOperator subsubitem = openSubElements[0];
                if (!getFile_Open_WorkDirectoryMenuName().equals(subsubitem.getText())) {
                errors.add("First menu subelement of File->Open menu is " + subsubitem.getText() + " while expected " + Menu.getFile_Open_WorkDirectoryMenuName());
                }
            }
            if (openSubElements.length > 1) {
                JMenuItemOperator subsubitem = openSubElements[1];
                if (!getFile_Open_TestSuiteMenuName().equals(subsubitem.getText())) {
                errors.add("First menu subelement of File->Open menu is " + subsubitem.getText() + " while expected " + Menu.getFile_Open_TestSuiteMenuName());
                }
            }
            }

        }
        if (elements.length > 2) {
            JMenuItem subitem = (JMenuItem) elements[2].getComponent();
            if (!getFile_RecentWorkDirectoryMenuName().equals(subitem.getText())) {
            errors.add("Third menu subelement of File menu is " + subitem.getText() + " while expected " + Menu.getFile_RecentWorkDirectoryMenuName());
            } else {
            MenuElement[] subElements = subitem.getSubElements();
            if (subElements.length != 1) {
                errors.add("File->RecentWorkDirectort menu contains " + subElements.length + " subelements while expected 1");
            }
            }
        }
        if (elements.length > 3) {
            JMenuItem subitem = (JMenuItem) elements[3].getComponent();
            if (!getFile_PreferencesMenuName().equals(subitem.getText())) {
            errors.add("Fifth menu subelement of File menu is " + subitem.getText() + " while expected " + Menu.getFile_PreferencesMenuName());
            }
        }
        if (elements.length > 4) {
            JMenuItem subitem = (JMenuItem) elements[4].getComponent();
            if (!getFile_CloseMenuName().equals(subitem.getText())) {
            errors.add("Seventh menu subelement of File menu is " + subitem.getText() + " while expected " + Menu.getFile_CloseMenuName());
            }
        }
        if (elements.length > 5) {
            JMenuItem subitem = (JMenuItem) elements[5].getComponent();
            if (!getFile_ExitMenuName().equals(subitem.getText())) {
            errors.add("Last menu subelement of File menu is " + subitem.getText() + " while expected " + Menu.getFile_ExitMenuName());
            }
        }
        }
    }

    if (menuCount > 1) {
        JMenuItemOperator item = new JMenuItemOperator((JMenuItem) menuElements[1].getComponent());

        String menuName = getToolsMenuName();

        if (!menuName.equals(item.getText())) {
        errors.add("Second menu element is not " + menuName + ". Found " + item.getText());
        } else {
        JMenuItemOperator[] elements = menu.showMenuItems(menuName, new Tools.SimpleStringComparator());
        if (elements.length != 4) {
            errors.add("Tools menu contains " + elements.length + " while expected 4. ");
        }

        String[] elementsNames = new String[elements.length];
        int i = 0;
        for (JMenuItemOperator op : elements) {
            elementsNames[i++] = op.getText();
        }
        Arrays.sort(elementsNames);
        int num = 0;
        if (elements.length > num) {
            String subitemName = elementsNames[num];
            String name = getTools_AgentMonitorMenuName();
            if (!name.equals(subitemName)) {
            errors.add("First menu subelement of Tools menu is " + subitemName + " while expected " + name);
            }
        }
        num = 1;
        if (elements.length > num) {
            String subitemName = elementsNames[num];
            String name = getTools_OpenQuickStartWizardMenuName();
            if (!name.equals(subitemName)) {
            errors.add("First menu subelement of Tools menu is " + subitemName + " while expected " + name);
            }
        }
        num = 2;
        if (elements.length > num) {
            String subitemName = elementsNames[num];
            String name = getTools_ReportConverterMenuName();
            if (!name.equals(subitemName)) {
            errors.add("First menu subelement of Tools menu is " + subitemName + " while expected " + name);
            }
        }
        num = 3;
        if (elements.length > num) {
            String subitemName = elementsNames[num];
            String name = getTools_TestResultsAuditorMenuName();
            if (!name.equals(subitemName)) {
            errors.add("First menu subelement of Tools menu is " + subitemName + " while expected " + name);
            }
        }
        }
    }

    if (menuCount > 2) {
        JMenuItemOperator item = new JMenuItemOperator((JMenuItem) menuElements[2].getComponent());
        String menuName = getWindowsMenuName();
        if (!menuName.equals(item.getText())) {
        errors.add("Third menu element is not " + menuName + ". Found " + item.getText());
        }
    }

    if (menuCount > 3) {
        JMenuItemOperator item = new JMenuItemOperator((JMenuItem) menuElements[3].getComponent());

        String menuName = getHelpMenuName();

        if (!menuName.equals(item.getText())) {
        errors.add("Fourth menu element is not " + menuName + ". Found " + item.getText());
        } else {
        JMenuItemOperator[] elements = menu.showMenuItems(menuName);

        if (elements.length != 3) {
            errors.add("Help menu contains " + elements.length + " while expected 4. ");
        }

        if (elements.length > 0) {
            JMenuItemOperator subitem = elements[0];
            String name = getHelp_OnlineHelpMenuName();
            if (!name.equals(subitem.getText())) {
            errors.add("First menu subelement of menu Help is " + subitem.getText() + " while expected " + name);
            }
        }
        if (elements.length > 1) {
            JMenuItemOperator subitem = elements[1];
            String name = getHelp_AboutJTHarnessMenuName();
            if (!name.equals(subitem.getText())) {
            errors.add("Second menu subelement of menu Help is " + subitem.getText() + " while expected " + name);
            }
        }
        if (elements.length > 2) {
            JMenuItemOperator subitem = elements[2];
            String name = getHelp_AboutJVMMenuName();
            if (!name.equals(subitem.getText())) {
            errors.add("Third menu subelement of menu Help is " + subitem.getText() + " while expected " + name);
            }
        }
        }
    }
    }

    @Override
    public String getDescription() {
    return "This test checks menu items in \"NewDesktop\" JavaTest frame. Tools menu subelements are checked sorted";
    }
}
