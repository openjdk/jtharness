/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Vector;

/**
 * An abstract base class to provide a way of opening files subject
 * to a policy of what to do if a file of the same name already exists.
 */
public abstract class BackupPolicy
{
    /**
     * Backup a file by renaming it to have have a new name of
     * <i>old-name</i>~<i>n</i> where n is chosen to be higher than
     * any other for which a candidate new filename exists.
     * Thus, successive backups of a file named "x" will create
     * files named "x~1~", "x~2~", "x~3~", etc.
     * The number of backup files to keep is determined by
     * getNumBackupsToKeep. In addition, backups can be suppressed
     * by isBackupRequired if desired.
     * @param file the file to be backed up
     * @throws IOException if there is a problem renaming the file
     * @throws SecurityException if the file could not be backed up because
     * permission was not given by the security manager
     * @see #getNumBackupsToKeep
     * @see #isBackupRequired
     */
    public void backup(File file) throws IOException, SecurityException {
        if (!isBackupRequired(file))
            return;

        if (!file.exists())
            return;

        if (file.isDirectory())
            throw new IOException("Cannot backup a directory");

        if (!file.canWrite())
            throw new IOException("File is write-protected");

        File canon = new File(file.getCanonicalPath());
        String p = canon.getParent();
        if (p == null)
            throw new IOException("Cannot determine parent directory of " + file);

        File dir = new File(p);
        String[] dirFiles = dir.list();

        String prefix = file.getName() + "~";
        String suffix = "~";
        int maxBackupIndex = 0;
        Vector backups = new Vector();
    nextFile:
        if (dirFiles != null) {
            for (int i = 0; i < dirFiles.length; i++) {
                String s = dirFiles[i];
                if (s.length() > (prefix.length() + suffix.length()) &&
                        s.startsWith(prefix) && s.endsWith(suffix)) {
                    String mid = s.substring(prefix.length(), s.length() - suffix.length());
                    // verify filename is numeric between prefix and suffix; if not; skip it
                    for (int m = 0; m < mid.length(); m++)
                        if (!Character.isDigit(mid.charAt(m)))
                            break nextFile;
                    // if numeric, get the backup index; ignore NumberFormatException
                    // since is really should not happen, given preceding check
                    int index = Integer.parseInt(mid);
                    if (index > maxBackupIndex)
                        maxBackupIndex = index;
                    backups.addElement(new Integer(index));
                }
            }
        }

        // try renaming file to file~(++maxBackupIndex)~
        File backup = new File(file.getPath() + "~" + (++maxBackupIndex) + "~");

        boolean ok = file.renameTo(backup);
        if (!ok)
            throw new IOException("failed to backup file: " + file);

        // delete old backups
        int numBackupsToKeep = getNumBackupsToKeep(file);
        for (int i = 0; i < backups.size(); i++) {
            int index = ((Integer)(backups.elementAt(i))).intValue();
            if (index <= (maxBackupIndex-numBackupsToKeep)) {
                File backupToGo = new File(file.getPath() + "~" + index + "~");
                // let SecurityExceptions out, but otherwise ignore failures
                // to delete old backups
                boolean ignore = backupToGo.delete();
            }
        }
    }

    /**
     * Rename (or move) the source file to the target name.  Backup the target if
     * necessary.  This method will return without action if the source file does not
     * exist.
     *
     * @param source The file to be backed up.
     *                  It must be a file (not a directory) which is deleteable.
     * @param target The new name for the file. It must be a file (not a directory)
     *                  which is will be at a writable location.
     * @throws IOException if there is a problem renaming the file.
     *          This may happen if the source is a
     *         directory, the source file is not writable, or the rename operation
     *         fails.  In all cases, the rename operation was not successful.
     * @throws SecurityException if the backup operation fails because of a security
     *         constraint.
     * @see #backup(File)
     * @since 3.0.1
     */
    public void backupAndRename(File source, File target) throws IOException, SecurityException {
        if (!source.exists())
            return;

        if (source.isDirectory())
            throw new IOException("Cannot backup a directory.");

        if (!source.canWrite())
            throw new IOException("Cannot rename, source file is write-protected " + source.getPath());

        if (isBackupRequired(target)) {
            backup(target);
        }
        else if (target.exists()) {
            // remove the file we are about to overwrite
            // not really needed on Solaris, seems to be needed on Win32
            target.delete();
        }

        boolean result = source.renameTo(target);
        if (!result)
            throw new IOException("Rename of " + target.getPath() + " failed.");
    }

    /**
     * Backup a file and open a new Writer to the file.
     * @param file the file to be backed up, and for which a new Writer will be opened
     * @return a buffered file writer to the specified file
     * @throws IOException if there is a problem backing up the file or creating
     *  the new writer object
     * @throws SecurityException if the operation could not be completed because
     * of a security constraint
     */
    public Writer backupAndOpenWriter(File file) throws IOException, SecurityException {
        backup(file);
        return new BufferedWriter(new FileWriter(file));
    }

    /**
     * Backup a file and open a new Writer to the file with specified charset.
     * @param file the file to be backed up, and for which a new Writer will be opened
     * @param charsetName Create an OutputStreamWriter that uses the named charset
     * @return a buffered file writer to the specified file
     * @throws IOException if there is a problem backing up the file or creating
     *  the new writer object
     * @throws SecurityException if the operation could not be completed because
     * of a security constraint
     */
    public Writer backupAndOpenWriter(File file, String charsetName) throws IOException, SecurityException {
        backup(file);
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charsetName));
    }


    /**
     * Backup a file and open a new output stream to the file.
     * @param file the file to be backed up, and for which a new output stream will be opened
     * @return a buffered output stream to the specified file
     * @throws IOException if there is a problem backing up the file or creating
     *  the new output stream
     * @throws SecurityException if the operation could not be completed because
     * of a security constraint
     */
    public OutputStream backupAndOpenStream(File file) throws IOException, SecurityException {
        backup(file);
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    /**
     * Get the number of backup files to keep for a particular file.
     * When backup is called, the oldest backups are automatically deleted
     * to limit the number of backup files.
     * @param file the file for which to check how many backups are required
     * @return the maximum number of backups to keep for this file
     * @see #backup
     */
    public abstract int getNumBackupsToKeep(File file);

    /**
     * Determine if backups are enabled for this file. If backups are not
     * enabled, backup will return without affecting the file.
     * @param file the file for which to check if backups are enabled
     * @return true if backups are enabled for this type of file, and false otherwise
     */
    public abstract boolean isBackupRequired(File file);

    /**
     * Get a BackupPolicy object which does no backups for any files.
     * @return a BackupPolicy object which does no backups for any files
     */
    public static BackupPolicy noBackups() {
        return new BackupPolicy() {
            public int getNumBackupsToKeep(File file) {
                return 0;
            }
            public boolean isBackupRequired(File file) {
                return false;
            }
        };
    }

    /**
     * Get a BackupPolicy object which does a set number of backups for all files.
     * @param n The number of backups to kept for each file
     * @return a BackupPolicy object which does a set number of backups for all files
     */
    public static BackupPolicy simpleBackups(final int n) {
        return new BackupPolicy() {
            public int getNumBackupsToKeep(File file) {
                return n;
            }
            public boolean isBackupRequired(File file) {
                return true;
            }
        };
    }
}
