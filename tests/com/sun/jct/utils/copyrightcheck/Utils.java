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
package com.sun.jct.utils.copyrightcheck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Chmod;

/**
 * Provides various convenience utility methods.
 */
public final class Utils {

    private Utils() {
        // private constructor for static utility class
    }

    /**
     * Convenience method to extract an executable file from the classpath and
     * put it into temp file with executable permissions.
     *
     * @param task
     *            The original task.
     * @param resource
     *            The resource to extract.
     * @return The executable file with requested resource.
     *
     * @throws BuildException
     *             in case of any errors.
     */
    public static File getExecutableFromResource(Task task, String resource) {
        if (task == null || resource == null) {
            throw new BuildException("Utils: Task or resource is null");
        }

        File tmpExec = extractResource(
                task.getClass().getClassLoader(), resource);
        task.log(
                "Copied embedded " +  resource + " to: " + tmpExec,
                Project.MSG_VERBOSE);

        // set executable flag
        final Chmod chmod = (Chmod) task.getProject().createTask("chmod");
        chmod.setFile(tmpExec);
        chmod.setProject(task.getProject());
        chmod.setPerm("u+x");
        chmod.execute();

        return tmpExec;
    }

    /**
     * Extracts resource with given name using provided class loader.
     *
     * @param loader The class loader.
     * @param resource Resource name.
     *
     * @return A file with extracted resource.
     *
     * @throws BuildException
     *             in case of any errors.
     */
    public static File extractResource(ClassLoader loader, String resource) {
        if (resource == null) {
            throw new BuildException("Resource is null");
        }

        String[] path = resource.split("/");
        String name = path[path.length - 1];

        InputStream stream = null;
        OutputStream out = null;
        try {
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }
            stream = loader.getResourceAsStream(resource);
            File tmpExec = File.createTempFile(name, null);
            tmpExec.deleteOnExit();
            out = new FileOutputStream(tmpExec);
            byte[] buf = new byte[BUF_LENGTH];
            int len = 0;
            while ((len = stream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            return tmpExec;
        } catch (IOException e) {
            throw new BuildException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Convenience method to create buffered readers from files, and with the
     * same encoding.
     *
     * @param file
     *            The source file.
     * @return The buffered reader to read from.
     * @throws IOException
     *             In case of IO errors.
     * @see #ENCODING
     */
    public static BufferedReader getFileReader(File file)
            throws IOException {
        return new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file), ENCODING));
    }

    /**
     * Convenience method to create buffered writers to files, and with the same
     * encoding.
     *
     * @param file
     *            The destination file.
     * @return The buffered reader to write to.
     * @throws IOException
     *             In case of IO errors.
     * @see #ENCODING
     */
    public static BufferedWriter getFileWriter(File file)
            throws IOException {
        return new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), ENCODING));
    }

    /**
     * Default encoding to read and write files.
     */
    public final static String ENCODING = "ISO-8859-1";

    private static final int BUF_LENGTH = 16 * 1024;

}
