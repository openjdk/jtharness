/*
 * $Id$
 *
 * Copyright (c) 2005, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * InterviewSet is an interview that is also a container for an ordered
 * set of child interviews. The default execution order for the children
 * is the order in which they are added to this container, but the
 * order can be modified by specifying dependencies between child interviews:
 * all dependencies for any child will be executed before the child itself.
 * Child interviews are added into an interview set by specifying this interview
 * as their parent.
 * The interview is invoked by using {@link #callInterview} in the usual way.
 * @see Interview#Interview(Interview, String)
 */
public class InterviewSet
    extends Interview
{
    /**
     * This exception will be thrown when an attempt to made to specify a dependency
     * that would create a dependency cycle. In other words, A cannot be a dependent
     * of B if B is already a dependent of A (either directly or indirectly.)
     */
    public static class CycleFault extends Fault
    {
        CycleFault(Interview dependent, Interview dependency) {
            super(i18n, "iset.cycle",
                  new Object[] { dependent.getTag(), dependency.getTag() } );
        }
    }

    /**
     * Create an interview set.
     * @param parent the parent interview for this interview
     * @param baseTag A name that will be used to qualify the tags of any
     * sub-interviews in this interview, to help ensure uniqueness of those
     * tags.
     */
    protected InterviewSet(Interview parent, String baseTag) {
        super(parent, baseTag);

        setFirstQuestion(sorter);
    }

    // extend Interview.add(Interview)
    void add(Interview child) {
        super.add(child);
        children.add(child);
        sortedCalls = null;
    }

    // disallow Interview.add(Question)
    void add(Question q) {
        throw new UnsupportedOperationException();
    }

    /**
     * Specify a dependency for a child interview.
     * When the interview is executed, all dependencies for each child interview
     * will be invoked before that child.
     * @param child the interview which depends on (and will be executed after)
     * the dependency
     * @param dependency the interview on which the child interview depends,
     * and which will be executed before the child interview
     * @throws InterviewSet.CycleFault if a dependency cycle would be created
     * @see #removeDependency
     */
    protected void addDependency(Interview child, Interview dependency)
        throws CycleFault
    {
        if (child == null)
            throw new NullPointerException();

        if (dependency == null)
            throw new NullPointerException();

        Set allDeps = getAllDependencies(dependency);
        if (allDeps != null && allDeps.contains(child))
            throw new CycleFault(child, dependency);

        Set deps = getDependencies(child, true);
        deps.add(dependency);

        sortedCalls = null;
    }

    /**
     * Remove any dependency between two interviews, and hence any ordering
     * constraint between these two interviews.
     * @param child the interview which depends on the dependency
     * @param dependency the interview on which the child interview depends
     */
    protected void removeDependency(Interview child, Interview dependency) {
        if (child == null)
            throw new NullPointerException();

        if (dependency == null)
            throw new NullPointerException();

        Set deps = getDependencies(child, false);

        if (deps != null)
            deps.remove(dependency);

        if (deps.size() == 0)
            dependencies.remove(child);

        sortedCalls = null;
    }

    private Set getDependencies(Interview child, boolean create) {
        Set deps = (Set) (dependencies.get(child));

        if (deps == null && create) {
            deps = new TreeSet(new ChildComparator());
            dependencies.put(child, deps);
        }

        return deps;
    }

    private Set getAllDependencies(Interview child) {
        Set s = new HashSet();
        getAllDependencies(child, s);
        return s;
    }

    private void getAllDependencies(Interview child, Set s) {
        if (s.contains(child))
            return;

        Set deps = getDependencies(child, false);
        if (deps != null) {
            for (Iterator iter = deps.iterator(); iter.hasNext(); ) {
                Interview i = (Interview) (iter.next());
                getAllDependencies(i, s);
                s.add(i);
            }
        }
    }

    private Interview[] sortChildren() {
        List list = new ArrayList();
        Set cycleSet = new HashSet();

        for (Iterator iter = children.iterator(); iter.hasNext(); ) {
            Interview child = (Interview) (iter.next());
            if (!list.contains(child))
                addToList(list, child, cycleSet);
        }

        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            Interview i = (Interview) (iter.next());
            System.err.println(i.getTag() + " " + i);
        }

        return (Interview[]) (list.toArray(new Interview[list.size()]));
    }

    private void addToList(List list, Interview child, Set cycleSet) {
        // assert !cycleSet.contains(child);
        if (cycleSet.contains(child))
            throw new IllegalArgumentException();

        cycleSet.add(child);

        Set deps = (Set) (dependencies.get(child));
        if (deps != null) {
            for (Iterator iter = deps.iterator(); iter.hasNext(); ) {
                Interview dep = (Interview) (iter.next());
                addToList(list, dep, cycleSet);
            }
        }

        list.add(child);

        cycleSet.remove(child);
    }

    private NullQuestion sorter = new NullQuestion(this) {
            public boolean isEnabled() {
                return false; // always hide this question
            }

            public Question getNext() {
                if (sortedCalls == null) {
                    Interview[] cc = sortChildren();

                    // have to build the list from the end, backwards,
                    // because of the way InterviewQuestion works
                    Question q = qEnd;
                    for (int i = cc.length - 1; i >= 0; i--)
                        q = callInterview(cc[i], q);

                    sortedCalls = q;
                }

                return sortedCalls;
            }
        };

    private FinalQuestion qEnd = new FinalQuestion(this);

    private List children = new ArrayList();
    private Map dependencies = new HashMap();
    private Question sortedCalls;

    private class ChildComparator implements Comparator
    {
        public int compare(Object o1, Object o2) {
            if (!children.contains(o1) || !children.contains(o2))
                throw new IllegalArgumentException();

            if (o1 == o2)
                return 0;

            for (Iterator iter = children.iterator(); iter.hasNext(); ) {
                Object o = iter.next();
                if (o == o1)
                    return -1;
                if (o == o2)
                    return 1;
            }

            throw new IllegalStateException();
        }
    }
}
