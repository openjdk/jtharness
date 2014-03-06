/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.util.I18NResourceBundle;

/**
 * A test filter that filters tests according to the keywords on a test description.
 */
public class KeywordsFilter extends TestFilter {
    /**
     * Create a filter that filters tests according to a Keywords object.
     * @param keywords an object that provides the basic filter
     */
    public KeywordsFilter(Keywords keywords) {
        this.keywords = keywords;
    }

    /**
     * Get the underlying keywords object.
     * @return the keywords object that provides the basic filter
     */
    public Keywords getKeywords() {
        return keywords;
    }

    @Override
    public String getName() {
        return i18n.getString("keywordsFilter.name");
    }

    @Override
    public String getDescription() {
        return i18n.getString("keywordsFilter.description");
    }

    @Override
    public String getReason() {
        return i18n.getString("keywordsFilter.reason");
    }

    @Override
    public boolean accepts(TestDescription td) {
        return keywords.accepts(td.getKeywordTable());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.keywords != null ? this.keywords.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof KeywordsFilter))
            return false;

        KeywordsFilter other = (KeywordsFilter) o;
        return keywords.equals(other.keywords);
    }

    private Keywords keywords;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(KeywordsFilter.class);
}
