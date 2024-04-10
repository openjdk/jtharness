/*
 * $Id$
 *
 * Copyright (c) 2009, 2024, Oracle and/or its affiliates. All rights reserved.
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

import jthtest.ConfigTools;
import jthtest.Test;

import jthtest.tools.JTFrame;
import jthtest.workdir.Workdir;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JFrameOperator;
import static jthtest.Tools.*;
import static jthtest.menu.Menu.*;

public class Menu04 extends Test {

     @Override
     public void testImpl() throws Exception {
          mainFrame = new JTFrame(true);

          if (mainFrame.getFile_CloseMenu().isEnabled()) {
               errors.add("File->Close menu is enabled when unexpected (NewDesktop)");
          }

          mainFrame.openDefaultTestSuite();

          if (!mainFrame.getFile_CloseMenu().isEnabled()) {
               errors.add("File->Close menu is disabled when unexpected (TS selected, no WD, no config)");
          }

          if (mainFrame.getConfigure_EditConfigurationMenu().isEnabled()) {
               errors.add("Configure->Edit Configuration menu is enabled when unexpected (TS selected, no WD, no config)");
          }
          if (mainFrame.getConfigure_EditQuickSetMenu().isEnabled()) {
               errors.add("Configure->Edit Quick Set menu is enabled when unexpected (TS selected, no WD, no config)");
          }
          if (mainFrame.getConfigure_LoadRecentConfigurationMenu().isEnabled()) {
               errors.add("Configure->Load Recent Configuration menu is enabled when unexpected (TS selected, no WD, no config)");
          }
          if (!mainFrame.getConfigure_LoadConfigurationMenu().isEnabled()) {
               errors.add("Configure->Load Configuration menu is disabled when unexpected (TS selected, no WD, no config)");
          }
          if (!mainFrame.getConfigure_NewConfigurationMenu().isEnabled()) {
               errors.add("Configure->New Configuration menu is disabled when unexpected (TS selected, no WD, no config)");
          }

          if (mainFrame.getReport_CreateReportMenu().isEnabled()) {
               errors.add("Report->Create Report menu is enabled when unexpected (TS selected, no WD, no config)");
          }
          if (mainFrame.getReport_OpenReportMenu().isEnabled()) {
               errors.add("Report->Open Report menu is enabled when unexpected (TS selected, no WD, no config)");
          }

          if (!mainFrame.getView_PropertiesMenu().isEnabled()) {
               errors.add("View->Properties menu is disabled when unexpected (TS selected, no WD, no config)");
          }
          if (mainFrame.getView_LogsMenu().isEnabled()) {
               errors.add("View->Logs menu is enabled when unexpected (TS selected, no WD, no config)");
          }
          if (!mainFrame.getView_Configuration_ShowChecklistMenu().isEnabled()) {
               errors.add("View->Configuration->Show Checklist menu is disabled when unexpected (TS selected, no WD, no config)");
          }

          addUsedFile(mainFrame.createWorkDirectoryInTemp());

          if (mainFrame.getConfigure_LoadRecentConfigurationMenu().isEnabled()) {
               errors.add("Configure->Load Recent Configuration menu is enabled when unexpected (TS selected, WD created, no config)");
          }

          if (!mainFrame.getView_PropertiesMenu().isEnabled()) {
               errors.add("View->Properties menu is disabled when unexpected (TS selected, WD created, no config)");
          }
          if (!mainFrame.getView_LogsMenu().isEnabled()) {
               errors.add("View->Logs menu is disabled when unexpected (TS selected, WD created, no config)");
          }
          if (!mainFrame.getReport_CreateReportMenu().isEnabled()) {
               errors.add("Report->Create Report menu is disabled when unexpected (TS selected, WD created, no config)");
          }
          if (!mainFrame.getReport_OpenReportMenu().isEnabled()) {
               errors.add("Report->Open Report menu is disabled when unexpected (TS selected, WD created, no config)");
          }
          if (mainFrame.getView_Configuration_ShowChecklistMenu().isEnabled()) {
               errors.add("View->Configuration->Show Checklist menu is enabled when unexpected (TS selected, WD created, no config)");
          }

          mainFrame.getConfiguration().load(CONFIG_NAME, true);

          if (!mainFrame.getConfigure_LoadConfigurationMenu().isEnabled()) {
               errors.add("Configure->Load Configuration menu is disabled when unexpected (TS selected, WD created, config loaded)");
          }
          if (!mainFrame.getConfigure_NewConfigurationMenu().isEnabled()) {
               errors.add("Configure->New Configuration menu is disabled when unexpected (TS selected, WD created, config loaded)");
          }
          if (!mainFrame.getConfigure_EditConfigurationMenu().isEnabled()) {
               errors.add("Configure->Edit Configuration menu is disabled when unexpected (TS selected, WD created, config loaded)");
          }
          if (!mainFrame.getConfigure_EditQuickSetMenu().isEnabled()) {
               errors.add("Configure->Edit Quick Set menu is disabled when unexpected (TS selected, WD created, config loaded)");
          }

     }

     @Override
     public String getDescription() {
          return "This test checks that all menu items are enabled/disabled when it is needed. ";
     }
}
