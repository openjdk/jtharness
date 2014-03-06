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
package com.sun.javatest.mrep;

final class Scheme {

        // ELEMENTS
         static final String REPORT = "Report";
         static final String SUMMARY = "Summary";
         static final String WDS = "WorkDirectories";
         static final String WD = "WorkDirectory";
         static final String TRS = "TestResults";
         static final String TR = "TestResult";
         static final String INT = "Interview";
         static final String Q = "Question";
         static final String DESCR_DATA ="DescriptionData";
         static final String KEY_WORDS = "Keywords";
         static final String TEST_ENV = "TestEnvironment";
         static final String RES_PROP = "ResultProperties";
         static final String SES = "Sections";
         static final String SE = "Section";
         static final String OU = "Output";
         static final String PR = "Property";
         static final String IT = "Item";
         static final String ENV = "Environment";
         static final String CONC = "Concurrency";
         static final String TIMO = "TimeOut";
         static final String PRIOS = "PriorStatusList";
         static final String EXCL_LIST = "ExcludeList";
         static final String STD_VALS = "StandardValues";
         static final String TESTS = "Tests";
         static final String ENTTREE = "EntireTestTree";
         static final String QUEST = "Question";
         static final String LIST_QUEST = "ListQuestion";
         static final String CHOICE_QUEST = "ChoiceQuestion";
         static final String CHOICE = "Choice";
         static final String PROP_QUEST = "PropertiesQuestion";
         static final String GROUP = "Group";
         static final String ROW = "Row";

        // ATTRS
         static final String TR_URL = "url";
         static final String TR_STATUS = "status";
         static final String TR_WDID = "workDirID";
         static final String PR_NAME = "name";
         static final String PR_VAL = "value";
         static final String IT_VAL = "value";
         static final String SE_TIT = "title";
         static final String SE_ST = "status";
         static final String OU_TIT = "title";
         static final String RES_PROP_TIM = "endTime";
         static final String WD_ID = "id";
         static final String WD_JTI = "jti";
         static final String KEYWORDS_EXPR = "expression";
         static final String REPORT_FORMST = "formatVersion";
         static final String REPORT_GENTIME = "generatedTime";
         static final String XSI = "xmlns:xsi";
         static final String SCH_LOC = "xsi:noNamespaceSchemaLocation";
         static final String QUEST_VALUE = "value";
         static final String QUEST_TEXT = "text";
         static final String QUEST_SUMM = "summary";
         static final String CHOICE_CH = "choice";
         static final String CHOICE_DCH = "displayChoice";
         static final String CHOICE_VAL = "value";
         static final String GROUP_NAME = "name";
         static final String GROUP_HD1 = "header1";
         static final String GROUP_HD2 = "header2";
         static final String ROW_KEY = "key";
         static final String ROW_VAL = "value";
         static final String ENV_NAME = "name";
         static final String ENV_DESCR = "description";

        // VALUES
         static final String XSI_VAL = "http://www.w3.org/2001/XMLSchema-instance";
         static final String SCH_LOC_VAL = "test_run_report.xsd";
}
