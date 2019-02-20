/*
 * $Id$
 *
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview;

import com.sun.javatest.tool.jthelp.HelpID;
import com.sun.javatest.tool.jthelp.HelpSet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Implementation of the HelpSetFactory interface which is aware of javax.help
 * library.
 *
 * @author Dmitry Fazunenko
 */
public class JavaHelpFactory implements HelpSetFactory {

    private static final ResourceBundle i18n = ResourceBundle.getBundle("com.sun.interview.i18n");

    public JavaHelpFactory() {
    }

    @Override
    public Object createHelpSetObject(String name, Class<?> c) throws Interview.Fault {
        ClassLoader cl = c.getClassLoader();
        String hsn;
        String pref = "";
        if (name.startsWith("/")) {
            hsn = name.substring(1); // strip off leading /
            pref = hsn.substring(0, hsn.lastIndexOf("/") + 1);
        } else {
            String cn = c.getName();
            String pn = cn.substring(0, cn.lastIndexOf('.'));
            hsn = pn.replace('.', '/') + "/" + name;
            pref = hsn.substring(0, hsn.indexOf(name));
        }
        URL u = HelpSet.findHelpSet(cl, hsn);
        if (u == null) {
            throw new HelpNotFoundFault(i18n, "interview.cantFindHelp", hsn);
        }
        return new HelpSet(cl, u, pref);
    }

    @Override
    public Object createHelpSetObject(String name, File file) throws Interview.Fault {
        try {
            URL[] urls = {new URL("file:" + file.getAbsolutePath() + "/")};
            URLClassLoader cl = new URLClassLoader(urls);
            URL url = HelpSet.findHelpSet(cl, name);
            if (url == null) {
                throw new HelpNotFoundFault(i18n, "interview.cantFindHelp",
                        file.getPath());
            }
            return new HelpSet(cl, url);
        } catch (MalformedURLException e) {
            throw new HelpNotFoundFault(i18n, "interview.cantFindHelp", file.getPath());
        }
    }

    @Override
    public Object createHelpID(Object hsObject, String target) {
        if (hsObject != null) {
            HelpSet hs = (HelpSet) hsObject;
            HashMap<String, URL> m = hs.getCombinedMap();
            if (m != null && !m.isEmpty()) {
                return HelpID.create(target, hs);
            }
        }

        return null;
    }

    @Override
    public Object updateHelpSetObject(Interview interview, Object object) {
        HelpSet newHelpSet = (HelpSet) object;
        HelpSet oldHelpSet = (HelpSet) interview.getHelpSet();
        if (interview.getParent() == null) {
            if (oldHelpSet == null) {
                // no previously registered helpset
                // so add in any helpsets for child interviews
                for (Interview i : interview.getInterviews()) {
                    HelpSet ihs = (HelpSet) i.getHelpSet();
                    if (ihs != null)
                        newHelpSet.add(ihs);
                }
            } else {
                // reregister child help sets with new help set
                List<HelpSet> helpSetsToRemove = new ArrayList<>();

                // transfer help sets old to new
                for (HelpSet entry : oldHelpSet.getHelpSets()) {
                    helpSetsToRemove.add(entry);
                    newHelpSet.add(entry);
                }   // for


                // now remove the sets from the old helpset
                for (HelpSet helpToRemove : helpSetsToRemove) {
                    oldHelpSet.remove(helpToRemove);
                }  // for
            }
        } else {
            Interview i = interview;
            while (i.getParent() != null)
                i = i.getParent();
            HelpSet rootHelpSet = (HelpSet) i.getHelpSet();
            if (rootHelpSet != null) {
                // remove old help set, if any, from root help set
                // ALERT: WHAT IF THE OLD HELPSET WAS REQUIRED BY DIFFERENT
                //        SUBINTERVIEWS -- IN THAT CASE, WE SHOULD NOT REMOVE
                //        IT HERE
                if (oldHelpSet != null)
                    rootHelpSet.remove(oldHelpSet);
                // register new helpset with root help set
                rootHelpSet.add(newHelpSet);
            }
        }

        // finally, update helpset
        return newHelpSet;
    }

    /**
     * This exception is thrown when a named help set cannot be found.
     */
    public static class HelpNotFoundFault extends Interview.Fault {
        HelpNotFoundFault(ResourceBundle i18n, String s, String name) {
            super(i18n, s, name);
            this.name = name;
        }

        /**
         * The name of the help set that could not be found.
         */
        public final String name;
    }

    /**
     * This exception is to report problems found while opening a JavaHelp help set.
     */
    public static class BadHelpFault extends Interview.Fault {
        BadHelpFault(ResourceBundle i18n, String s, Exception e) {
            super(i18n, s, e.getMessage());
        }
    }

}
