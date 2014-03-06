/*
 * $Id$
 *
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import javax.help.HelpBroker;
import javax.help.JHelpContentViewer;
import javax.swing.SwingUtilities;

/**
 * This link type opens in external browser if current OS supports awt.Desktop
 *
 * usage:
 * <object classid="java:com.sun.javatest.tool.HelpExternalLink">
 * <param name="text" value=...>
 * <param name="target" value="http://...">
 * </object>
 * note that the protocol should be specified in the URL path (e.g. value="http://www.google.com")
 * @see HelpLink
 */
public class HelpExternalLink extends HelpLink {

    public HelpExternalLink() {
        super(null);
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (!openUrl(getTarget())) {
                    JHelpContentViewer cv = (JHelpContentViewer) SwingUtilities.getAncestorOfClass(JHelpContentViewer.class, e.getComponent());
                    HelpBroker hb = (HelpBroker) (cv.getClientProperty(HELPBROKER_FOR_HELPLINK));
                    hb.setCurrentID(getTarget());
                    hb.setDisplayed(true);
                }
            }
        });
    }

    public static boolean openUrl(String url) {
        String os = System.getProperty("os.name");
        try {
            if (os != null) {
                if (os.startsWith("Windows")) {
                    // rundll32 allows to open a file in appropriate default application
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.startsWith("Mac OS")) {
                    Class fileMgr = Class.forName("com.apple.eio.FileManager");
                    Method openURL = fileMgr.getDeclaredMethod("openURL",
                            new Class[]{String.class});
                    openURL.invoke(null, new Object[]{url});
                } else {
                    String[] commands = {
                        // xdg-open is used on some linux systems to open files types in default applications
                        "xdg-open", "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                    String resultCommand = null;
                    for (int i = 0; i < commands.length && resultCommand == null; i++) {
                        Process process = Runtime.getRuntime().exec(new String[]{"which", commands[i]});
                        // IOException is thrown if there is no "which" command in the system, false returned - cannot find browser
                        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

                        int time = 0;
                        int exitValue = -1;
                        // don't want to catch a lock - waiting 10 seconds for result
                        while(true) {
                            try {
                                exitValue = process.exitValue();
                                break;
                            } catch(IllegalThreadStateException e) {
                            }
                            if(time >= 10000) {
                                process.destroy();
                                break;
                            }
                            Thread.sleep(100);
                            time += 100;
                        }

                        String result = in.readLine();
                        // can't just check exitValue - "which" command works in other way in Solaris
                        if(result.startsWith("/") && result.endsWith(commands[i]) && exitValue == 0)
                            resultCommand = commands[i];
                    }
                    if(resultCommand == null)
                        return false;
                    // starting browser
                    Runtime.getRuntime().exec(new String[] {resultCommand, url});
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
