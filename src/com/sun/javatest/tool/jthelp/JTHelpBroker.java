/*
 * $Id$
 *
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool.jthelp;

import com.sun.javatest.ProductInfo;
import com.sun.javatest.tool.Preferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JTHelpBroker implements HelpBroker{

    private static final String HELP_DIR_NAME = "jthelp";
    private static final String HELP_VERSION_NAME = "version";
    private static final String HELP_FILE_PREFIX = "/com/sun/javatest/help/default/";
    private static final String HELP_FILE_NAME = "map.xml";
    private HashMap<String, String> helpMap;

    public JTHelpBroker(){
        this(HelpBroker.class.getResource(HELP_FILE_PREFIX+HELP_FILE_NAME));
    }

    private JTHelpBroker(URL url){
        helpMap = HelpSet.readHelpMap(url);
    }

    public void enableHelpKey(final Component component, String helpID){
        if (component instanceof JComponent) {
            KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false);
            ((JComponent)component).registerKeyboardAction(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    displayCurrentID(ContextHelpManager.getHelpIDString(component));
                }
            }, keystroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        }
        ContextHelpManager.setHelpIDString(component, helpID);
    }

    private boolean isHelpUpToDate(){
        File helpDir = new File(Preferences.getPrefsDir(), HELP_DIR_NAME);
        if (!helpDir.exists()) {
            return false;
        }
        File helpVersion = new File(helpDir, HELP_VERSION_NAME);
        try {
            Scanner scanner = new Scanner(helpVersion);
            String version = scanner.nextLine();
            if (version == null || !version.equals(ProductInfo.getVersion()+ProductInfo.getBuildNumber())){
                return false;
            }
        } catch (IOException e) {
            return false;
        }


        return true;
    }

    private void unpackHelpIfNeeded(){

        if (isHelpUpToDate()){
            return;
        }

        JTHelpProgressBar jtHelpProgressBar = new JTHelpProgressBar(new ProgressTask());
        jtHelpProgressBar.createAndShowGUI();

    }

    public void displayCurrentID(String helpID){

        unpackHelpIfNeeded();

        try {
            File helpDir = new File(Preferences.getPrefsDir(), HELP_DIR_NAME);
            String helpPage = helpMap.get(helpID) != null ? helpMap.get(helpID) : "jthelp.html";
            StringBuilder address = new StringBuilder();
            address.append("file:").append(helpDir.getAbsolutePath()).append(HELP_FILE_PREFIX).append(helpPage);
            URL url = new URL(address.toString());
            Desktop.getDesktop().browse(url.toURI());
        } catch (Exception e) {
            System.err.println("Cannot open JavaTest help file:");
            System.err.println(e.getMessage());
        }
    }

    class ProgressTask extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
            try {
                File jarFile = new File(HelpBroker.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                File destDir = new File(Preferences.getPrefsDir(), HELP_DIR_NAME);
                File helpVersion = new File(destDir, HELP_VERSION_NAME);

                JarFile jar = new JarFile(jarFile);
                Enumeration enumEntries = jar.entries();

                int total = 0;
                while (enumEntries.hasMoreElements()) {
                    JarEntry file = (JarEntry) enumEntries.nextElement();
                    if (isHelpFile(file)) {
                        total++;
                    }
                }

                if (destDir.exists()){
                    destDir.delete();
                }

                destDir.mkdir();

                int progress = 0;
                setProgress((int) (progress * 100.0 / total));
                enumEntries = jar.entries();

                while (enumEntries.hasMoreElements()) {
                    JarEntry file = (JarEntry) enumEntries.nextElement();
                    File f = new java.io.File(destDir + java.io.File.separator + file.getName());
                    if (isHelpFile(file)) {
                        f.getParentFile().mkdirs();
                        InputStream is = jar.getInputStream(file);
                        FileOutputStream fos = new java.io.FileOutputStream(f);
                        while (is.available() > 0) {
                            fos.write(is.read());
                        }
                        fos.close();
                        is.close();
                        progress++;
                        setProgress((int) (progress * 100.0 / total));
                    }
                }

                try(PrintWriter out = new PrintWriter( helpVersion.getAbsolutePath())){
                    out.println(ProductInfo.getVersion()+ProductInfo.getBuildNumber());
                }

            }
            catch (Exception e){
                System.err.println("Cannot unpack JavaTest help files");
                System.err.println(e.getMessage());
            }

            return null;
        }

        private boolean isHelpFile(JarEntry file) {
            return  (file.getName().endsWith(".html")
                    || file.getName().endsWith(".gif")
                    || file.getName().endsWith(".css"));
        }

    }

}
