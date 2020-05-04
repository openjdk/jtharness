/*
 * $Id$
 *
 * Copyright (c) 1996, 2020, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.tck.lib.autd2.unittests.tgfported.algebra.generated;

import junit.framework.Test;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

public class Engine {


    private String name = "Multy dimentional suite";

    public Engine(String name) {
        this.name = name;
    }

    public Engine(Multiplier multiplier) {
        this.multiplier = multiplier;
    }

    public Engine(String name, Multiplier multiplier) {
        this.name = name;
        this.multiplier = multiplier;
    }

    private Map<String, Dimension> dimentions = new HashMap<String, Dimension>();
    private Multiplier multiplier;


    public void addAspects(String name, Object... objects) {
        for (Object o : objects) {
            Dimension dimension = dimentions.get(name);
            if (dimension == null) {
                dimension = new Dimension(name);
                dimentions.put(name, dimension);
            }
            dimension.addAspect(o);
        }
    }

    public void setMultiplier(Multiplier multiplier) {
        this.multiplier = multiplier;
    }

    public Test createFullyMultipliedSuite() {
        return new CustomTestSuite(multiplier, dimentions);
    }

    public static class Dimension {

        private String name;
        private List<Object> aspects;

        public Dimension(String name) {
            this.name = name;
            aspects = new LinkedList<Object>();
        }

        public String getName() {
            return name;
        }

        public void addAspect(Object o) {
            aspects.add(o);
        }

        public List<Object> getAspects() {
            return aspects;
        }
    }


}
