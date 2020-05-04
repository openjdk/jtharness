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

import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.oracle.tck.lib.autd2.unittests.tgfported.ValuesImplSlow;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.Values;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import java.util.Map;

import static com.oracle.tck.lib.autd2.unittests.tgfported.algebra.generated.IDataProducer.*;

@RunWith(AllTests.class)
public class Simple {

    static final IDataProducer[] PRODUCERS = new IDataProducer[] { ZERO, ONE, TWO, THREE };

    public static Test suite() {

        Multiplier multiplier = map -> {

            ICombination action = (ICombination) map.get("action");

            ISimpleOperation o1 = (ISimpleOperation) map.get("op#1");
            ISimpleOperation o2 = (ISimpleOperation) map.get("op#2");

            Object[] objs1 = ((IDataProducer) map.get("v#1")).produce();
            Object[] objs2 = ((IDataProducer) map.get("v#2")).produce();
            Object[] objs3 = ((IDataProducer) map.get("v#3")).produce();

            Values d1 = new ValuesImplSlow(objs1);
            Values d2 = new ValuesImplSlow(objs2);
            Values d3 = new ValuesImplSlow(objs3);

            Values dv = action.operate(o1, o2, d1, d2, d3);

            Values v1 = DataFactory.createColumn(objs1);
            Values v2 = DataFactory.createColumn(objs2);
            Values v3 = DataFactory.createColumn(objs3);

            Values v = action.operate(o1, o2, v1, v2, v3);

            ValuesComparison.compare(v, dv);
        };

        Engine engine = new Engine(multiplier);
        engine.addAspects("v#1", (Object[])PRODUCERS);
        engine.addAspects("v#2", (Object[])PRODUCERS);
        engine.addAspects("v#3", (Object[])PRODUCERS);
        engine.addAspects("op#1", (Object[])ISimpleOperation.OPERATIONS);
        engine.addAspects("op#2", (Object[])ISimpleOperation.OPERATIONS);
        engine.addAspects("action", (Object[])I_COMBINATIONS);

        return engine.createFullyMultipliedSuite();
    }

    public static interface ICombination {
        Values operate(ISimpleOperation o1, ISimpleOperation o2, Values v1, Values v2, Values v3);
    }

    private static final ICombination[] I_COMBINATIONS = new ICombination[] {
            new ICombination() {
                public Values operate(ISimpleOperation o1, ISimpleOperation o2, Values v1, Values v2, Values v3) {
                    return  o1.with( v1, v2 );
                }
                public String toString() {    return "#1";    }
            },
            new ICombination() {
                public Values operate(ISimpleOperation o1, ISimpleOperation o2, Values v1, Values v2, Values v3) {
                    return  o1.with( v3, o2.with(v1, v2) );
                }
                public String toString() {    return "#2";    }
            },
            new ICombination() {
                public Values operate(ISimpleOperation o1, ISimpleOperation o2, Values v1, Values v2, Values v3) {
                    return o1.with( o2.with(v1, v2), v3  );
                }
                public String toString() {    return "#3";    }
            },
            new ICombination() {
                public Values operate(ISimpleOperation o1, ISimpleOperation o2, Values v1, Values v2, Values v3) {
                    return o1.with( o2.with(v1, v2), o1.with( v1, v3 )  );
                }
                public String toString() {    return "#4";    }
            },

            new ICombination() {
                public Values operate(ISimpleOperation o1, ISimpleOperation o2, Values v1, Values v2, Values v3) {
                    return o1.with( o2.with(v1, v1), o1.with( v1, v1 )  );
                }
                public String toString() {    return "#5";    }
            },

            new ICombination() {
                public Values operate(ISimpleOperation o1, ISimpleOperation o2, Values v1, Values v2, Values v3) {
                    return o1.with( o2.with(v1, v2), o1.with( v1, v3 )  );
                }
                public String toString() {    return "#6";    }
            },
            new ICombination() {
                public Values operate(ISimpleOperation o1, ISimpleOperation o2, Values v1, Values v2, Values v3) {
                    return o1.with(
                            o2.with(o1.with(v1, v1), o1.with(v2, v2)),
                            o2.with(v3, v3)
                    );
                }
                public String toString() {    return "#7";    }
            },
            new ICombination() {
                public Values operate(ISimpleOperation o1, ISimpleOperation o2, Values v1, Values v2, Values v3) {
                    return o1.with(
                            o2.with(o1.with(v1, v1), o1.with(v2, v2)),
                            o2.with(o1.with(v1, v3), o1.with(v2, v3))
                    );
                }
                public String toString() {    return "#8";    }
            },
    };

}
