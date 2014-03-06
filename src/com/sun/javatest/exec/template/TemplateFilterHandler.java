/*
 * $Id$
 *
 * Copyright (c) 2010, 2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.exec.template;

import com.sun.javatest.TestFilter;
import com.sun.javatest.exec.Session.Event;
import com.sun.javatest.exec.ET_FilterHandler;
import com.sun.javatest.exec.ExecModel;
import com.sun.javatest.tool.UIFactory;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 * Extension to ET_FilterHandler with Template filter support.
 *
 * @author Dmitry Fazunenko
 */
public class TemplateFilterHandler extends ET_FilterHandler {

    TemplateParameterFilter tFilter;
    TemplateFilterHandler(JComponent parent, ExecModel model, UIFactory uif) {
        super(parent, model, uif);
        tFilter = new TemplateParameterFilter();
        allFilters.add(tFilter);
    }

    /**
     * @return list of one element - TemplateFilter
     */
    @Override
    protected List<TestFilter> getUsersFilters() {
        List<TestFilter> lst = super.getUsersFilters();
        if (lst == null) {
            lst = new ArrayList<TestFilter>(1);
        }
        lst.add(tFilter);
        return lst;
    }

    @Override
    public void updated(Event ev) {
        if (ev instanceof TemplateSession.E_NewTemplate) {
            tFilter.update(((TemplateSession.E_NewTemplate)ev).templ);
        }
        super.updated(ev);
    }
}
