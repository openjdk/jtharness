/*
 * $Id$
 *
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.exec;


/**
 * This class represents default feature manager.
 * It can be extended to set another behavior of features in JT Harness.
 * The method isEnabled may be overridden to change behavior.
 */
public class FeatureManager {
    public FeatureManager() {
        featureToggles = new boolean[FEATURE_COUNT];
        featureToggles[TEMPLATE_USAGE] = true;
        featureToggles[TEMPLATE_CREATION] = true;
        featureToggles[SHOW_TEMPLATE_UPDATE] = true;
        featureToggles[SINGLE_TEST_MANAGER] = false;
        featureToggles[WD_WITHOUT_TEMPLATE] = true;
        featureToggles[TEMPLATE_LOADING] = true;
        featureToggles[SHOW_DOCS_FOR_TEST] = false;
        featureToggles[NO_TREE_WITHOUT_WD] = false;
    }

    /**
     * Can someone load any template they want, even if
     * WD_WITHOUT_TEMPLATE is enabled?
     */
    public static final int TEMPLATE_LOADING = 0;

    /**
     * Can templates be used?
     */
    public static final int TEMPLATE_USAGE = 1;

    /**
     * Can templates be created?
     */
    public static final int TEMPLATE_CREATION = 2;

    /**
     * Show "check for template update" menu.
     */
    public static final int SHOW_TEMPLATE_UPDATE = 3;

    /**
     * Can this test suite be opened more than once within a harness?
     * False allows any number of instances of the test suite to be opened.
     */
    public static final int SINGLE_TEST_MANAGER = 4;

    /**
     * Ability to support only work directories with templates attached.
     */
    public static final int WD_WITHOUT_TEMPLATE = 5;

    /**
     * Should harness display Documentation tab for
     * single test
     */
    public static final int SHOW_DOCS_FOR_TEST = 6;

    /**
     * Should the harness display test tree for test suite
     * without working directory
     */
    public static final int NO_TREE_WITHOUT_WD = 7;


    private static int FEATURE_COUNT = 8;

    /**
     * @param feature one of TEMPLATE_USAGE, TEMPLATE_CREATION,
     *              AUTOPROPAGATE, SINGLE_TEST_MANAGER
     * @return true if this feature enabled, false otherwise
     */
    public boolean isEnabled(int feature) {
        return featureToggles[feature];
    }

    protected boolean[] featureToggles;
}
