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

package com.sun.jct.utils.i18ncheck;

import com.sun.tools.javac.tree.Tree;
import com.sun.tools.javac.tree.TreeScanner;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;



/**
 * This visitor check methods string repr
 * and collect methods which are mathes with pattern.
 *
 * This class designed to work with JDK5
 * compiler API interfaces
 *
 */
public class I18NVisitor15 extends TreeScanner {

    protected String filename;
    protected HashSet<?> keys;
    private Vector<XMLNode.Rule> rules;
    private Vector<XMLNode.Rule> newRules;
    private I18NStaticChecker checker;

    public I18NVisitor15(String filename, I18NStaticChecker checker, HashSet<?> keys) {
        super();
        this.filename = filename;
        this.checker = checker;
        this.rules = new Vector(checker.patterns.values());
        this.newRules = checker.newRules;
    }

    public void visitApply(Tree.Apply tree) {
        Matcher matcher;
        for(XMLNode.Rule rule : rules) {
            matcher = rule.pattern.matcher(tree.meth.toString());
            if(matcher.matches()) {
                checker.performRule(rule, tree, filename);
            }
        }
        super.visitApply(tree);
    }

    public void visitNewClass(Tree.NewClass tree) {
        if (tree.clazz != null) {
            String className = tree.clazz.toString();
            for(XMLNode.Rule rule : newRules) {
                Matcher matcher = rule.pattern.matcher(className);
                if(matcher.matches()) {
                    checker.performRule(rule, tree, filename);
                }
            }
        }
        super.visitNewClass(tree);
    }
}
