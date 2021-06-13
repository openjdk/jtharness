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

import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.JTFrame;

/**
 *
 * @author at231876
 */
public class Menu05 extends Test {

    public Menu05() {
        depricated = true; // ConfigDialog is now modal, no need to block menu items
    }

    @Override
    public void testImpl() throws Exception {
    mainFrame = JTFrame.startJTWithDefaultWorkDirectory();

    if(!mainFrame.getConfigure_EditConfigurationMenu().isEnabled())
        errors.add("Configure->Edit Configuration menu is disabled before Configuration Editor is opened while unexpected");
    if(!mainFrame.getConfigure_EditQuickSetMenu().isEnabled())
        errors.add("Configure->Edit Quick Set menu is disabled before Configuration Editor is opened while unexpected");
    if(!mainFrame.getConfigure_LoadConfigurationMenu().isEnabled())
        errors.add("Configure->Load Configuration menu is disabled before Configuration Editor is opened while unexpected");
    if(!mainFrame.getConfigure_LoadRecentConfigurationMenu().isEnabled())
        errors.add("Configure->Load Recent Configuration menu is disabled before Configuration Editor is opened while unexpected");
    if(!mainFrame.getConfigure_NewConfigurationMenu().isEnabled())
        errors.add("Configure->New Configuration menu is disabled before Configuration Editor is opened while unexpected");

    ConfigDialog cd = mainFrame.getConfiguration().openByMenu(true);

    if(mainFrame.getConfigure_EditConfigurationMenu().isEnabled())
        errors.add("Configure->Edit Configuration menu is enabled while Configuration Editor is opened while unexpected");
    if(mainFrame.getConfigure_EditQuickSetMenu().isEnabled())
        errors.add("Configure->Edit Quick Set menu is enabled while Configuration Editor is opened while unexpected");
    if(mainFrame.getConfigure_LoadConfigurationMenu().isEnabled())
        errors.add("Configure->Load Configuration menu is enabled while Configuration Editor is opened while unexpected");
    if(mainFrame.getConfigure_LoadRecentConfigurationMenu().isEnabled())
        errors.add("Configure->Load Recent Configuration menu is enabled while Configuration Editor is opened while unexpected");
    if(mainFrame.getConfigure_NewConfigurationMenu().isEnabled())
        errors.add("Configure->New Configuration menu is enabled while Configuration Editor is opened while unexpected");

    cd.closeByMenu();

    if(!mainFrame.getConfigure_EditConfigurationMenu().isEnabled())
        errors.add("Configure->Edit Configuration menu is disabled after Configuration Editor is opened while unexpected");
    if(!mainFrame.getConfigure_EditQuickSetMenu().isEnabled())
        errors.add("Configure->Edit Quick Set menu is disabled after Configuration Editor is opened while unexpected");
    if(!mainFrame.getConfigure_LoadConfigurationMenu().isEnabled())
        errors.add("Configure->Load Configuration menu is disabled after Configuration Editor is opened while unexpected");
    if(!mainFrame.getConfigure_LoadRecentConfigurationMenu().isEnabled())
        errors.add("Configure->Load Recent Configuration menu is disabled after Configuration Editor is opened while unexpected");
    if(!mainFrame.getConfigure_NewConfigurationMenu().isEnabled())
        errors.add("Configure->New Configuration menu is disabled after Configuration Editor is opened while unexpected");

    }

    @Override
    public String getDescription() {
    return "This test checks that all Configure menu subelements are disabled when Configuration Editor is opened and are enabled after it's closing";
    }

}
