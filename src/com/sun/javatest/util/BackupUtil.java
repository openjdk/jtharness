/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.util;

import java.io.File;
import java.util.HashSet;
import java.util.Vector;
import java.io.FileFilter;
import java.io.IOException;

public class BackupUtil {

    /** Creates a new instance of BackupUtil */
    public BackupUtil() {
    }

    /**
     * Performs backup of file. Searchs for all files with the same name + "~i~"(such names
     * we use for backupped files). For each backup it increase it's number for 1
     * (older backups has higher numbers). Then checks, if there more backups, then
     * maxBackups allows, and remove superfluous old backups.
     * @param file File to backup
     * @param maxBackups Maximum number of allowed backups
     * @return number of backup levels after finishing operation.
     */
    public static int backupFile(File file, int maxBackups) {
        if(!file.exists()) {
            return -1;
        }

        if(file.isDirectory()) {
            return -1;
        }

        String filename = file.getPath();
        File dir = file.getParentFile();
        String[] list = dir.list();
        if(list == null) {
            return -1;
        }

        String prefix = file.getName() + "~";
        String suffix = "~";
        int maxBackupIndex = 0;
        Vector backups = new Vector();

        boolean renamed;
        java.util.Arrays.sort(list);
        for (int i = list.length - 1; i >= 0; i--) {
            String s = list[i];
            if (s.length() > (prefix.length() + suffix.length()) &&
                    s.startsWith(prefix) && s.endsWith(suffix)) {
                String mid = s.substring(prefix.length(), s.length() - suffix.length());

                if(!checkForInteger(mid)) {
                    continue;
                }

                int index = Integer.parseInt(mid);
                File backuppedFile = new File(filename + "~" + index + "~");
                index++;
                renamed =
                        backuppedFile.renameTo(new File(filename + "~" + index + "~"));
                if(!renamed) {
                    return -1;
                }
                backups.addElement(new Integer(index));
            }
        }

        renamed = file.renameTo(new File(filename + "~" + 1 + "~"));
        if(!renamed) {
            return -1;
        }
        backups.addElement(new Integer(1));

        int maxIndex = 0;
        for(int j = 0; j < backups.size(); j++) {
            int index = ((Integer)backups.get(j)).intValue();
            maxIndex = index > maxIndex ? index : maxIndex;
            if(index > maxBackups) {
                File oldBackup = new File(filename + "~" + backups.get(j) + "~");
                oldBackup.delete();
            }
        }
        return maxIndex > maxBackups ? maxBackups : maxIndex;
    }

    /**
     * This method created to backup dirs. It just renames directories, not content of
     * this directories. Renaming mechanism is the same, as for backupFile().
     * If dir is empty, returns.
     * If parametr is not dir, returns.
     */
    public static void backupDir(File file, int maxBackups) {
        if(!file.isDirectory()) {
            return;
        }

        if(file.list().length == 0) {
            return;
        }

        boolean renamed;
        String filename = file.getPath();
        File dir = file.getParentFile();
        String[] list = dir.list();

        String prefix = file.getName() + "~";
        String suffix = "~";
        int maxBackupIndex = 0;
        Vector backups = new Vector();

        java.util.Arrays.sort(list);
        for (int i = list.length - 1; i >= 0; i--) {
            String s = list[i];
            if (s.length() > (prefix.length() + suffix.length()) &&
                    s.startsWith(prefix) && s.endsWith(suffix)) {
                String mid = s.substring(prefix.length(), s.length() - suffix.length());

                if(!checkForInteger(mid)) {
                    continue;
                }

                int index = Integer.parseInt(mid);
                File backuppedFile = new File(filename + "~" + index + "~");
                index++;
                renamed =
                        backuppedFile.renameTo(new File(filename + "~" + index + "~"));
                if(!renamed) {
                    return;
                }
                backups.addElement(new Integer(index));
            }
        }

        renamed = file.renameTo(new File(filename + "~" + 1 + "~"));
        if(!renamed) {
            return;
        }
//        file = new File(filename);
//        file.mkdir();
//        backups.addElement(new Integer(1));

        for(int j = 0; j < backups.size(); j++) {
            int index = ((Integer)backups.get(j)).intValue();
            if(index > maxBackups) {
                File oldBackup = new File(filename + "~" + backups.get(j) + "~");
                deleteDir(oldBackup);
            }
        }
    }

    /**
     * Backups all found "layers" of subdirs. Subdirs have the same layer, if
     * suffixes of their names are the same (suffix has format ~ + int number + ~)
     * @param dir root dir where layers situated
     * @param maxBackups max allowed time to backup
     */
    public static void backupAllSubdirs(File dir, int maxBackups) {
        if(!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();

        if(files.length == 0) {
            return;
        }

        HashSet layers = new HashSet();
        String suffix = "~";
        for(int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()) {
                String fileName = files[i].getName();
                if(!fileName.endsWith(suffix)) {
                    layers.add(new Integer(0));
                    continue;
                }

                String prefix = fileName.substring(0,fileName.lastIndexOf(suffix));
                String numb = prefix.substring(prefix.lastIndexOf(suffix) + 1, prefix.length());

                if(checkForInteger(numb)) {
                    layers.add(new Integer(Integer.parseInt(numb)));
                }
            }
        }

        Object[] larray = layers.toArray();
        java.util.Arrays.sort(larray);
        for(int i = larray.length - 1; i >= 0; i--) {
            backupLayer(dir, (Integer)larray[i], maxBackups);
        }
    }

    /**
     * Backups "layer" of subdirs - all subdirs with the same number in suffix.
     * If layer should be deleted (because of max allowed backups) - deletes it
     * @param parentDir root dir, where layers you want to backup situates
     * @param numb Number of layer (those in suffix of directories names)
     * @param maxBackups maximum allowed times to backup
     */
    private static void backupLayer(File parentDir, Integer numb, int maxBackups) {
        int number = numb.intValue();
        String suffix = number == 0 ? "" : "~" + number + "~";
        String newSuffix = "~" + (number + 1) + "~";

        FileFilter filter = new LayerFilter(suffix);
        File[] layer = parentDir.listFiles(filter);
        if(number >= maxBackups) {
            for(int i = 0; i < layer.length; i++) {
                deleteDir(layer[i]);
            }
        }
        else {
            for(int i = 0; i < layer.length; i++) {
                try {
                    String newName;
                    if(number != 0) {
                        newName = layer[i].getCanonicalPath().replaceAll(suffix, newSuffix);
                    }
                    else {
                        newName = layer[i].getCanonicalPath() + newSuffix;
                    }
                    layer[i].renameTo(new File(newName));
                }
                catch(IOException e) {}
            }
        }
    }

    private static class LayerFilter implements FileFilter {
        private String suffix;
        public LayerFilter(String suffix) {
            this.suffix = suffix;
        }
        public boolean accept(File file) {
            if(!file.isDirectory())
                return false;

            if(suffix.equals("")) {
                if(!file.getName().endsWith("~"))
                    return true;
                else
                    return false;
            }
            else {
                if(file.getName().endsWith(suffix))
                    return true;
                else
                    return false;
            }
        }

    }

    /**
     * backups all files in the directory. No rename of directory. Not - recursive
     */
    public static void backupContents(File dir, int maxBackups) {
        if(!dir.isDirectory()) {
            return;
        }

        String[] list = dir.list();
        for (int i = 0; i < list.length; i++) {
            File f = new File(dir, list[i]);
            if(f.isDirectory() || f.getPath().endsWith("~")) {
                continue;
            }
            else {
                backupFile(f, maxBackups);
            }
        }
    }

    /**
     *
     * @param s Checks, if this String represents integer number
     * @return true, if if this String represents integer number,
     * false otherwise
     */
    public static boolean checkForInteger(String s) {
        if(s.length() == 0) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param dir File to delete. If it is not dir, deletes this File. Otherwise deletes dir
     * recursively
     * @return true, if dir (or file) removed successfully
     */
    public static boolean deleteDir(File dir) {
        boolean deleted = dir.delete();

        if(!deleted) {
            String[] list = dir.list();
            if(list != null) {
                for(int i = 0; i < list.length; i++) {
                    deleteDir(new File(dir, list[i]));
                }
            }
            deleted = dir.delete();
        }
        return deleted;
    }
}
