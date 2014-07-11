/*
 * $Id$
 *
 * Copyright (c) 2003, 2014, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.MissingResourceException;

class AgentClassLoader2 extends ClassLoader {

    private CodeSource cs = null;
    private final HashMap<CodeSource, ProtectionDomain> pdcache = new HashMap<CodeSource, ProtectionDomain>(11);
    private static volatile AgentClassLoader2 instance = null;

    public AgentClassLoader2(Agent.Task parent) {
        super(parent.getClass().getClassLoader());
        this.parent = parent;

        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }

        ProtectionDomain pd = this.getClass().getProtectionDomain();

        if (pd != null) {
            cs = this.getClass().getProtectionDomain().getCodeSource();
            synchronized (pdcache) {
                pdcache.put(cs, pd);
            }
        }
    }


    private ProtectionDomain getProtectionDomain(CodeSource cs) {
        ProtectionDomain pd = null;
        synchronized (pdcache) {
            pd = pdcache.get(cs);
            if (pd == null) {
                PermissionCollection perms = new Permissions();
                pd = new ProtectionDomain(cs, perms, this, null);
                pdcache.put(cs, pd);
            }
        }
        return pd;
    }


    /*
     * Returns shared instance of classloader for tests where it is required.
     */
    public static AgentClassLoader2 getInstance(Agent.Task parent) {
        if (instance == null) {
            synchronized (AgentClassLoader2.class) {
                if (instance == null) {
                    instance = new AgentClassLoader2(parent);
                }
            }
        }
        instance.parent = parent;
        return instance;
    }


    @Override
    public Class findClass(String className) throws ClassNotFoundException {
        if (className != null) {
            int i = className.lastIndexOf('.');
            if (i>0) {
                String pkgName = className.substring(0,i);
                if (getPackage(pkgName) == null) {
                    definePackage(pkgName, null, null, null, null, null, null, null);
                }
            }
            byte[] data = parent.getClassData(className);
            return defineClass(className, data, 0, data.length, getProtectionDomain(cs));
        }
        throw new ClassNotFoundException();
    }

    @Override
    protected URL findResource(String name) {
        URL u = null;
        //URL u = super.findResource(name);
        // create URLConnection + AgentURLStreamHandler
        // must fail with null URL if data stream throws MissingResourceException
        // not available locally, request across connection
        if (parent == null || name == null) {
            // sanity check
            return null;
        }

        try {
            byte[] bytes = parent.getResourceData(name);

            if (bytes == null) {
                u = null;
            }
            else {
                // if byes[] is zero length, we expect the code to still work
                u = new URL("file", "", -1, name, new AgentURLStreamHandler(bytes));
            }
        }
        catch (MissingResourceException e) {
            u = null;
        }
        catch (IOException e) {
            u = null;
        }

        return u;
    }

/*
    @Override
    public synchronized InputStream getResourceAsStream(String resourceName) {
        // check local classpath first
        // the resource should already be absolute, if we've got here
        // through getClass().getResourceAsStream()
        InputStream in = getClass().getResourceAsStream(resourceName);
        if (in == null) {
            try {
                // if not found here, try remote load from Agent Manager
                byte[] data = parent.getResourceData(resourceName);
                in = new ByteArrayInputStream(data);
            }
            catch (Exception e) {
                // ignore
            }
        }
        return in;
    }*/

    private Agent.Task parent;

    private class AgentURLStreamHandler extends URLStreamHandler {
        AgentURLStreamHandler(byte[] bytes) {
            super();
            this.bytes = bytes;
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            return new AgentURLConnection(url, bytes);
        }

        private byte[] bytes;
    }

    private class AgentURLConnection extends URLConnection {
        AgentURLConnection(URL url) {
            // do not use this constructor for now, bytes are already available
            super(url);
        }

        AgentURLConnection(URL url, byte[] bytes) {
            super(url);
            this.bytes = bytes;
        }

        @Override
        public void connect() throws IOException {
            // could check Agent.Task parent for connection status
            // generally, ignore this call per the spec
            if (bytes != null) {
                connected = true;
            }
            else {
                connected = false;
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (parent == null) {
                throw new IOException("No parent agent to open connection with!");
            }

            if (bytes == null) {
                // should not occur
                throw new IOException("No bytes available!!");
            }

            return new ByteArrayInputStream(bytes);
        }


        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public int getContentLength() {
            if (bytes != null) {
                return bytes.length;
            } else {
                // zero length, bytes.length should == 0, so all other cases are -1
                return -1;
            }
        }

       /* public long getContentLengthLong() {
            EXISTS in Java 7
        }*/


        @Override
        public String getContentType() {
            if (bytes == null) {
                // sanity check
                return null;
            }

            String type = null;

            try {
                type = guessContentTypeFromStream(getInputStream());
            }
            catch (Exception e) {
                // it's really IOException which is possible
                type = null;
            }

            if (type == null) {
                // assumption: getURL() path is the same as what cached bytes has content for
                URL u = getURL();
                if (u != null) {
                    type = guessContentTypeFromName(url.getPath());
                }
            }

            return type;
        }

        @Override
        public long getDate() {
            return 0;       // unknown
        }

        @Override
        public long getExpiration() {
            return 0;       // unknown
        }

        @Override
        public long getLastModified() {
            return 0l;       // unknown
        }

        private byte[] bytes;
    }
}
