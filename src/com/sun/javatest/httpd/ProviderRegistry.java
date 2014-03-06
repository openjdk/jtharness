/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.httpd;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.PrintWriter;

import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * Registry for JT Harness classes which wish to publish pages for the http
 * server.  Each handler is associated with a particular URL.  As a policy,
 * only the first directory name can be used as a key.  For example,
 * you cannot register <tt>/foo/bar/</tt>, you can only register <tt>/foo/</tt>.
 *
 * <p>
 * Rules about registration:
 * <ul>
 * <li>Leading and trailing slashes are stripped before they are looked up.
 * </ul>
 */

public class ProviderRegistry {
    /**
     * @param url Root relative path that this handler should be associated with.
     * @param descrip Informative description of this handler/url
     * @param obj The handler itself (JThttpProvider)
     * @throws IllegalStateException Thrown if the url is already associated with
     *         a handler.
     */
    public void addHandler(String url, String descrip, JThttpProvider obj) {
        // validate url
        //    does it contain any /
        //    strip length
        //    strip trailing /
        //    check for collisions
        if (debug) {
            System.out.println("PR-Adding Handler: " + descrip);
            System.out.println("   PR-Adding URL: " + url);
            System.out.println("   PR-Adding OBJ: " + obj);
        }

        // used to easily file away this entry
        //httpURL path = new httpURL(url);
        try {
            boolean result = insertHandler(disassembleURL(url), descrip, obj, false);
            if (result) obj.addRegistredURL(url);
        }
        catch (IllegalArgumentException e) {
            if (debug) {
                System.out.println("   PR-Error while inserting " + obj);
                System.out.println("   PR-Ignoring insert of URL " + url);
                e.printStackTrace();
            }
        }   // catch
    }

    public void addPrivateHandler(String url, String descrip, JThttpProvider obj) {
        if (debug) {
            System.out.println("PR-Adding Private Handler: " + descrip);
            System.out.println("   PR-Adding URL: " + url);
            System.out.println("   PR-Adding OBJ: " + obj);
        }

        // used to easily file away this entry
        //httpURL path = new httpURL(url);

        try {
            boolean result = insertHandler(disassembleURL(url), descrip, obj, true);
            if (result) obj.addRegistredURL(url);
        }
        catch (IllegalArgumentException e) {
            if (debug) {
                System.out.println("   PR-Error while inserting " + obj);
                System.out.println("   PR-Ignoring insert of URL " + url);
                e.printStackTrace();
            }
        }   // catch
    }

    /**
     * @return Whether or not the url was resolved and removed.
     */
    public boolean removeHandler(String url, JThttpProvider obj) {
        if (debug) {
            System.out.println("PR-Removing Handler by URL: " + url);
            System.out.println("   PR-Removing OBJ : " + obj);
        }

        return deleteHandler(disassembleURL(url), obj);
    }

    /**
     * Remove all URL registrations that point to the supplied handler.
     */
    public void removeHandler(JThttpProvider obj) {
        if (debug) {
            System.out.println("PR-Removing Handler by Object: " + obj);
        }

        String[] urls = obj.getRegisteredURLs();

        for (int i = 0; i < urls.length; i++) {
            deleteHandler(disassembleURL(urls[i]), obj);
        }
    }

    /**
     * @param url The file portion of the url.  Ex: <tt>/harness/foo</tt>
     * @return The handler associated with the url.  A default provider is given
     *         if the requested one cannot be found.
     */
    public JThttpProvider getHandler(httpURL url) {
        if (url == null) return null;

        /*
        // preprocess the url, remove whitespace and trailing slash
        String processedURL = url.trim();

        // XXX may need modification for key-value pairs encoded in URL
        int nextSlash = url.indexOf('/', 1);

        // grab only the first directory name
        if (nextSlash != -1)
            processedURL = processedURL.substring(0, nextSlash);

        if (processedURL.endsWith("/") && processedURL.length() > 1)
            processedURL = processedURL.substring(0, processedURL.length()-1);
        */
        String nextDir = url.getNextFile();

        JThttpProvider prov;

        if (nextDir == null) {
            if (debug) System.out.println("PR-End of URL, no handler, using default.");
            prov = null;
        }
        else {
            if (debug) System.out.println("PR-Looking up: " + nextDir);

            Object target = url2prov.get(nextDir);

            if (target == null)
                prov = null;
            else if (target instanceof HandlerEntry) {
                HandlerEntry he = (HandlerEntry)(target);
                //prov = (he == null ? null : he.getProvider());
                prov = he.getProvider();
            }
            else {
                prov = (((ProviderRegistry)target).getHandler(url));
            }
        }

        if (debug) System.out.println("PR-URL resolved to: " + prov);

        // we always return a valid handler
        return (prov == null ? getIndexProvider() : prov);
    }

    public JThttpProvider getIndexProvider() {
        if (myProvider == null)
            myProvider = new IndexHandler(url2prov);

        return myProvider;
    }

    /**
     * Find out how many providers or sub-providers are available.
     */
    public int getSize() {
        return url2prov.size();
    }

    public boolean isEmpty() {
        return (url2prov.size() == 0);
    }

// ---------- Protected or better -----------

    /**
     * Find out if there is a handler or sub-registry with the given name.
     */
    protected Object lookupByName(String name) {
        Object obj = url2prov.get(name);

        if (debug)
            System.out.println("PR-lookupName(" + name + "): " + obj);

        return obj;
    }

    protected void deleteByName(String name) {
        Object obj = url2prov.remove(name);

        if (debug)
            System.out.println("PR-Removed " + name + " from hashtable." + obj);
    }

    /**
     * Process a directory name before it is stored or looked up.
     *
     * The default implementation of this method removes the leading and
     * trailing slashes and whitespace.  A null in will result in null out.
     */
    static String stripDirName(String in) {
        if (in == null) return null;

        int startTrim = 0;
        if (in.charAt(0) == '/') startTrim = 1;

        int nextSlash = in.indexOf('/', startTrim);
        String processedURL = null;
        // grab only the first directory name
        if (nextSlash != -1)
            processedURL = in.substring(startTrim, nextSlash);
        else
            processedURL = in.substring(startTrim, in.length());

        return processedURL;
    }

    /**
     * @return Whether or not the insertion was successful.
     * @exception IllegalArgumentException Thrown if the suppled url/object
     *      combination cannot be properly inserted.  This is most likely
     *      a name conflict.
     */
    protected final boolean insertHandler(String[] url, String descrip,
                                          JThttpProvider obj, boolean hidden)
                            throws IllegalArgumentException {
        // XXX may need this check, needs thought because of recursion
        //if (url == null) return;

        boolean result;
        String file = url[0];
        url = (String[])DynamicArray.remove(url, 0);

        if (debug) System.out.println("    PR-Partial insert of " + file);

        Object lookup = lookupByName(file);

        if (lookup == null) {       // no preexisting entry name
            if (url == null || url.length == 0) {    // file obj here
                url2prov.put(file, new HandlerEntry(file, descrip, obj));
                if (debug) System.out.println("    Inserted " + file + " into " + this);
                result = true;
            }
            else {                                  // recurse
                ProviderRegistry pr = new ProviderRegistry();
                url2prov.put(file, pr);
                result = pr.insertHandler(url, descrip, obj, hidden);
                if (debug) System.out.println("    Created new registry and recursed, " + file);
            }
        }
        else if (lookup instanceof ProviderRegistry) {
            if (debug) System.out.println("    Found existing registry and recursed, " + file);
            result = ((ProviderRegistry)lookup).insertHandler(url, descrip, obj, hidden);
        }
        else {
            throw new IllegalArgumentException(
                "Supplied URL collides with another registered handler.  Ignoring new handler.");
        }

        return result;
    }

    /**
     * @return False if the URL cannot be matched.
     * @throws IllegalArgumentException If the url does not match an entry or
     *          the obj that it maps to does not match the supplied one.
     */
    protected final boolean deleteHandler(String[] url, JThttpProvider obj) {
        if (url.length == 0) {
            if (debug) System.out.println("    PR-(del) Reached end of URL before done.");
            return false;
        }

        String file = url[0];
        url = (String[])DynamicArray.remove(url, 0);

        if (debug) System.out.println("    PR-(del)Partial remove of " + file);

        Object lookup = lookupByName(file);

        if (lookup == null) {       // no such entry
            if (debug) System.out.println("    PR-(del) Unable to match URL at: " + file);

            return false;
        }
        else if (lookup instanceof HandlerEntry) {
            HandlerEntry entry = (HandlerEntry)lookup;

            if (entry.getProvider() == obj) {
                deleteByName(file);
                return true;
            }
            else {
                throw new IllegalArgumentException(
                    "Given Provider ref. does not match URL Provider.  Unable to complete handler removal.");
            }
        }
        else if (lookup instanceof ProviderRegistry) {
            if (debug) System.out.println("    PR-(del) Found existing registry and recursed, " + file);
            ProviderRegistry pr = (ProviderRegistry)lookup;

            boolean result = pr.deleteHandler(url, obj);

            if (result && pr.isEmpty()) {
                // only remove a registry if it is empty
                deleteByName(file);
                return true;
            }
            else {
                // delete didn't work for some reason
                return false;   // == result
            }
        }
        else {
            throw new IllegalArgumentException(
                "Unknown object found in registry.  Unable to complete handler removal.");
        }
    }

    /**
     * Repacks /foo/bar/index.html as an array with three elements.
     * Slashes separate items, a question mark or space terminates scanning.
     */
    final String[] disassembleURL(String url) {
        String[] result = new String[0];

        if (url == null || url.length() == 0) return result;

        // ignore the leading slash
        int urlLen = url.length();      // just an optimization
        int start = (url.charAt(0) == '/' ? 1 : 0);
        int pos = start + 1;
        char currC;
        boolean done = false;
        while (pos < urlLen && !done) {
            currC = url.charAt(pos);
            switch (currC) {
                case '/':   if (pos > start+1)
                                result = (String[])DynamicArray.append(result, url.substring(start, pos));
                            start = pos + 1;
                            pos++;
                            break;
                case ' ':   // fall thru
                case '?':   done = true;
                            break;
            }   // switch

            pos++;
        }

        if (!done && pos >= urlLen && (pos - start) >= 1)
            result = (String[])DynamicArray.append(result, url.substring(start, pos));

        if (debug)
            System.out.println("PR-Dissolved url " + url + " into " + result.length + " files.");

        return result;
    }

    /**
     * Maps the url to a HandlerEntry
     */
    protected Hashtable url2prov = new Hashtable();

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ProviderRegistry.class);
    protected static boolean debug = Boolean.getBoolean("debug." + ProviderRegistry.class.getName());
    protected JThttpProvider myProvider;

    private static class HandlerEntry {
        HandlerEntry(String url, String descrip, JThttpProvider obj) {
            this.url = url;
            this.descrip = descrip;
            this.obj = obj;
        }

        public void setPrivate(boolean state) {
            hidden = state;
        }

        public String getURL() {
            return url;
        }

        public String getDescription() {
            return descrip;
        }

        public JThttpProvider getProvider() {
            return obj;
        }

        /**
         * Of the form "/harness" or "/harness/", relative to the root.
         */
        private String url;
        private String descrip;
        private JThttpProvider obj;
        private boolean hidden;
    }

    protected static class IndexHandler extends JThttpProvider {
        IndexHandler(Hashtable ht) {
            urlMap = ht;
        }

        public void serviceRequest(httpURL requestURL, PrintWriter out) {
            PageGenerator.generateDocType(out, PageGenerator.HTML32);
            PageGenerator.writeBeginDoc(out);
            PageGenerator.writeHeader(out, "JT Harness Webserver Root Page");
            PageGenerator.startBody(out);

            // headline
            out.print("<h2>JT Harness");
            out.print("&#8482; ");
            print(out, i18n.getString("provider.index.hdr"));
            out.println("</h2>");

            // note line
            println(out, i18n.getString("provider.index.note"));
            out.println("<br>");

            writeWebAvailPages(out);

            out.println("<hr>");
            PageGenerator.writeFooter(out);
            PageGenerator.endBody(out);
            PageGenerator.writeEndDoc(out);

            out.close();
        }

        private void writeWebAvailPages(PrintWriter out) {
            out.println("<h3>Available Web Pages</h3>");
            out.println("<ul>");

            HandlerEntry entry;

            // XXX should use set of unique entries
            Enumeration enum_ = urlMap.elements();
            while(enum_.hasMoreElements()) {
                Object next = enum_.nextElement();
                if (next instanceof HandlerEntry) {
                    entry = (HandlerEntry)next;

                    out.print("<li><a href=\"");
                    out.print(entry.getURL());
                    out.print("\">");
                    out.print(entry.getDescription());
                    out.println("</a>");
                }
                else {
                    //System.out.println("Located sub-Registry");
                }
            }

            out.println("</ul>");
            out.println();
        }

        private Hashtable urlMap;
    }
}

