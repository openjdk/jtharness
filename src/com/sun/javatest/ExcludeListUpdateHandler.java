/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * A class to handle downloading exclude lists from a server.
 */
public class ExcludeListUpdateHandler
{
    /**
     * Create a handler for downloading exclude lists from a server.
     * @param remoteURL the URL from which the exclude list should be downloaded
     * @param localFile the file to which the exclude list should be written
     */
    public ExcludeListUpdateHandler(URL remoteURL, File localFile) {
        if (remoteURL == null || localFile == null)
            throw new NullPointerException();

        this.remoteURL = remoteURL;
        this.localFile = localFile;
    }

    /**
     * Get the file to which the exclude list should be written.
     * @return the file to which the exclude list should be written
     */
    public File getLocalFile() {
        return localFile;
    }

    /**
     * Get the  time that the local file was last modified.
     * @return the time the local file was last modified.
     * The result is given in the standard way, in the number of seconds
     * since January 1, 1970. The result may be -1 or if there was
     * a problem determining the required information
     */
    public long getLocalFileLastModified() {
        if (localFileLastModified == 0)
            localFileLastModified = localFile.lastModified();
        return localFileLastModified;
    }

    /**
     * Get the URL from which the exclude list should be downloaded.
     * @return the URL from which the exclude list should be downloaded
     */
    public URL getRemoteURL() {
        return remoteURL;
    }

    /**
     * Get the time that the remote URL was last modified.
     * @return the time the remote URL was last modified
     * The result is given in the standard way, in the number of seconds
     * since January 1, 1970.
     * @throws IOException if there is a problem determining the information.
     */
    public long getRemoteURLLastModified() throws IOException {
        if (remoteURLLastModified == 0) {
            URLConnection c = remoteURL.openConnection();
            c.connect();
            remoteURLLastModified = c.getLastModified();
            c.getInputStream().close();
        }
        return remoteURLLastModified;
    }

    /**
     * Determine if an update is available; this is determined by comparing
     * the last modified times of the remote URL and the local file.
     * @return true if the remote URL has been modified more recently
     * than the local file.
     * @throws IOException if there is a problem determining the result
     * @see #getLocalFileLastModified
     * @see #getRemoteURLLastModified
     */
    public boolean isUpdateAvailable() throws IOException {
        getLocalFileLastModified();
        getRemoteURLLastModified();
        return (remoteURLLastModified > localFileLastModified);
    }

    /**
     * Update the local file with the exclude list given by the remote URL.
     * @throws IOException if there is a problem reading the exclude list
     */
    public void update() throws IOException {
        update(remoteURL.openConnection());
    }

    /**
     * Update the local file if the remote URL has been modified more
     * recently than the local file.
     * @throws IOException if there is a problem reading the exclude list
     */
    public void updateIfNewer() throws IOException {
        URLConnection c = remoteURL.openConnection();
        c.getContentLength();
        c.connect();
        remoteURLLastModified = c.getLastModified();
        if (remoteURLLastModified > getLocalFileLastModified())
            update(c);
        else
            c.getInputStream().close();
    }

    private void update(URLConnection c) throws IOException {
        c.connect();

        int totalBytes = c.getContentLength();
        InputStream in = new BufferedInputStream(c.getInputStream());

        OutputStream out = new BufferedOutputStream(new FileOutputStream(localFile));

        int bytesSoFar = 0;
        byte[] data = new byte[4096];
        int n;
        while ((n = in.read(data)) != -1) {
            out.write(data, 0, n);
            bytesSoFar += n;
            int percent = Math.max(bytesSoFar * 100 / totalBytes, 100);
            // good point to update progress meter if appropriate
        }
        in.close();
        out.close();
    }

    private File localFile;
    private long localFileLastModified;
    private URL remoteURL;
    private long remoteURLLastModified;
}
