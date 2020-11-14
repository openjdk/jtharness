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
package com.oracle.tck.lib.autd2.unittests.tgfported.general;

import com.oracle.tck.lib.autd2.NonTestCase;
import com.oracle.tck.lib.autd2.unittests.BaseTestGroup;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.lib.tgf.data.Doubles;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvalidTestCases {



    @Test(expected = IllegalArgumentException.class)
    public void overLoaded_01() {

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private Values setup_1() {
                return null;
            }

            private Values setup_2() {
                return null;
            }

            @TestCase
            @TestData("setup_1")
            public void test_1(String s, int i) {
            }

            @TestCase
            @TestData("setup_2")
            public void test_1(int i, String s) {
            }
        }, TU.EMPTY_ARGV);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overLoaded_2_1() {
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private String[] setup_1() {
                return new String[]{"2", "dd", "df", "fg", "sfsd", "dfg"};
            }

            private Integer[] setup_2() {
                return new Integer[]{334, 0, -2, 34};
            }

            @TestCase
            @TestData("setup_1")
            public void test_1(String s) {
            }

            @TestCase
            @TestData("setup_2")
            public void test_1(int i) {
            }
        }, TU.EMPTY_ARGV);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overLoaded_3() {

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private String[] setup_1() {
                return new String[]{"2", "dd", "df", "fg", "sfsd", "dfg"};
            }

            private Integer[] setup_2() {
                return new Integer[]{334, 0, -2, 34};
            }

            @TestCase
            @TestData("setup_1")
            public void test_1(String s) {
            }

            @TestCase
            @TestData("setup_2")
            public void test_1(int i) {
            }
        }, TU.EMPTY_ARGV);
        assertTrue(!status.isOK());
    }

    @Test(expected = IllegalArgumentException.class)
    public void overLoaded_4() {
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            @TestCase
            public void myTest() {
            }

            @TestCase
            @TestData("setup_2")
            public void myTest(int i) {
            }
        }, TU.EMPTY_ARGV);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overLoaded_4_1() {
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            @TestCase
            public void myTest() {
            }

            @TestCase
            @TestData("setup_2")
            public void myTest(int i) {
            }
        }, TU.EMPTY_ARGV);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overLoaded_5() {
        com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            @TestCase
            public void myTest(String s) {
            }

            @TestCase
            public void myTest(int i) {
            }

            @TestCase
            public void myTest() {
            }
        }, TU.EMPTY_ARGV);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overLoaded_6() {
        com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            @TestCase
            public void myTest213213() {
            }

            @TestCase
            @TestData("something")
            public void myTest(int i) {
            }

            @TestCase
            @TestData("something")
            public void myTest(String s) {
            }
        }, TU.EMPTY_ARGV);
    }


    @Test
    public void overLoaded_message() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

                @TestCase
                @TestData("something")
                public void myTest(int i) {
                }

                @TestCase
                @TestData("something")
                public void myTest(String s) {
                }
            }, TU.EMPTY_ARGV);
            Assert.fail("Exception was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Overloaded testcases are not supported. Method \"myTest\" is overloaded.", e.getMessage());
        }
    }

    private static final String MARKING_METHOD_ONLY_WITH_TEST_DATA_IS_NOT_ALLOWED_PLEASE_ADD_TEST_CASE_ANNOTATION_TO_METHOD =
            "Attaching data references to non-@TestCase methods is not allowed. Please add @TestCase annotation to method \"";

    @Test
    public void noTestCaseAnn_onlyTestData_01() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

                @TestData("something")
                protected void myTest01(int i) {
                }
            }, TU.EMPTY_ARGV);
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals(MARKING_METHOD_ONLY_WITH_TEST_DATA_IS_NOT_ALLOWED_PLEASE_ADD_TEST_CASE_ANNOTATION_TO_METHOD +
                    "myTest01\".", e.getMessage());
        }
    }

    @Test
    public void noTestCaseAnn_onlyTestData_02() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
                @TestData("something")
                private void myTest02() {
                }
            }, TU.EMPTY_ARGV);
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals(MARKING_METHOD_ONLY_WITH_TEST_DATA_IS_NOT_ALLOWED_PLEASE_ADD_TEST_CASE_ANNOTATION_TO_METHOD +
                    "myTest02\".", e.getMessage());
        }
    }

    @Test
    public void noTestCaseAnn_onlyTestData_03() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

                protected void myTest00() {
                }

                @TestCase
                public void myTest01() {
                }

                @TestCase
                @TestData("something")
                public void myTest02(String s) {
                }

                @TestData("werlwerlkdjfgljdsfg")
                protected void myTest04(String s) {
                }

                @TestData("somethingsomething")
                protected void myTest05(String s) {
                }

            }, TU.EMPTY_ARGV);
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            final String s1 = MARKING_METHOD_ONLY_WITH_TEST_DATA_IS_NOT_ALLOWED_PLEASE_ADD_TEST_CASE_ANNOTATION_TO_METHOD +
                    "myTest04\".";
            final String s2 = MARKING_METHOD_ONLY_WITH_TEST_DATA_IS_NOT_ALLOWED_PLEASE_ADD_TEST_CASE_ANNOTATION_TO_METHOD +
                    "myTest05\".";
            assertTrue(s1.equals(e.getMessage()) || s2.equals(e.getMessage()));
        }
    }

    @Test
    public void noTestCaseAnn_onlyTestData_04() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

                @TestData("blabla")
                void myTest04() {
                }

                @TestCase
                public void myTest02() {
                }

            }, TU.EMPTY_ARGV);
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals(MARKING_METHOD_ONLY_WITH_TEST_DATA_IS_NOT_ALLOWED_PLEASE_ADD_TEST_CASE_ANNOTATION_TO_METHOD +
                    "myTest04\".", e.getMessage());
        }
    }

    @Test
    public void noTestCaseAnn_onlyTestData_05() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

                @TestData("blabla")
                protected void myTest01(int i) {
                }

                @TestCase
                public void myTest02() {
                }

            }, TU.EMPTY_ARGV);
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals(MARKING_METHOD_ONLY_WITH_TEST_DATA_IS_NOT_ALLOWED_PLEASE_ADD_TEST_CASE_ANNOTATION_TO_METHOD +
                    "myTest01\".", e.getMessage());
        }
    }

    @Test
    public void nonPublicTestCase_private() {
        checkNonPublicTestCase("myWrongTest0003984848344", new BaseTestGroup(){
            @TestCase
            private void myWrongTest0003984848344() {
            }
            @TestCase
            public void myTest() {
            }
        });
    }

    @Test
    public void nonPublicTestCase_packVisible() {
        checkNonPublicTestCase("myWrongTest748998723wqrtklh98328498475", new BaseTestGroup(){
            @TestCase
            void myWrongTest748998723wqrtklh98328498475() {
            }
            @TestCase
            public void myTest() {
            }
        });
    }

    @Test
    public void nonPublicTestCase_protected() {
        checkNonPublicTestCase("woiurytoiuwrsdflhg3458789334889475344444", new BaseTestGroup(){
            @TestCase
            protected void woiurytoiuwrsdflhg3458789334889475344444() {
            }
            @TestCase
            public void myTest() {
            }
        });
    }

    @Test
    public void nonPublicTestCase_protected2() {
        checkNonPublicTestCase("woiurytoiuwrsdflhg3458789334889475344444", new BaseTestGroup(){
            @TestCase
            protected void woiurytoiuwrsdflhg3458789334889475344444(@Doubles({0, 5.6}) double d) {
            }
            @TestCase
            public void myTest() {
            }
        });
    }

    private void checkNonPublicTestCase(final String testCaseName, final Object testInstance) {
        try {
            TU.runTestGroup(testInstance);
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Method annotated with @Testcase \"" +
                            testCaseName +
                            "\" should be public.",
                    e.getMessage());
        }
    }


    @Test
    public void publicNonTestCaseMethod_message() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
                @TestCase
                public void myTest01(int i) {
                }

                public void myTest0232426556() {
                }
            });
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Please either add @TestCase or @NonTestCase annotation to method \"com.oracle.tck.lib.autd2.unittests.tgfported.general.InvalidTestCases$18.myTest0232426556\" or make it non-public. All public methods must have @TestCase annotation by default. As an exception a method which needs to be public but is not a testcase should be annotated with @NonTestCase annotation.", e.getMessage());
        }
    }

    @Test
    public void publicNonTestCaseMethod_message_bothAnnotations() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
                @TestCase
                @NonTestCase
                public void myTest01(int i) {
                }
            });
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Please remove one of @TestCase or @NonTestCase annotation from method \"com.oracle.tck.lib.autd2.unittests.tgfported.general.InvalidTestCases$19.myTest01\".", e.getMessage());
        }
    }

    @Test
    public void publicNonTestCaseMethod_message_bothAnnotations1_private() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
                @TestCase
                @NonTestCase
                private void myTest01(int i) {
                }
            });
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Please remove one of @TestCase or @NonTestCase annotation from method \"com.oracle.tck.lib.autd2.unittests.tgfported.general.InvalidTestCases$20.myTest01\".", e.getMessage());
        }
    }

    @Test
    public void publicNonTestCaseMethod_message_bothAnnotations1_protected() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
                @TestCase
                @NonTestCase
                protected void myTest01(int i) {
                }
            });
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Please remove one of @TestCase or @NonTestCase annotation from method \"com.oracle.tck.lib.autd2.unittests.tgfported.general.InvalidTestCases$21.myTest01\".", e.getMessage());
        }
    }

    @Test
    public void publicNonTestCaseMethod_message_bothAnnotations1_packprivate() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
                @TestCase
                @NonTestCase
                void myTest01(int i) {
                }
            });
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Please remove one of @TestCase or @NonTestCase annotation from method \"com.oracle.tck.lib.autd2.unittests.tgfported.general.InvalidTestCases$22.myTest01\".", e.getMessage());
        }
    }

    @Test
    public void publicNonTestCaseMethod_message2() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
                public void myTest4444444() {
                }
            });
            Assert.fail("IAE was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Please either add @TestCase or @NonTestCase annotation to method \"com.oracle.tck.lib.autd2.unittests.tgfported.general.InvalidTestCases$23.myTest4444444\" or make it non-public. All public methods must have @TestCase annotation by default. As an exception a method which needs to be public but is not a testcase should be annotated with @NonTestCase annotation.", e.getMessage());
        }
    }

    @Test
    public void publicNonTestCaseMethod_notTestCase() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
                @NonTestCase
                public void myTest4444444() {
                }
            });
        Assert.assertTrue(status.isOK());
    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
                public void myTest0232426556() {
                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_implements() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBaseTestGroup() {
                public void myTest0232426556() {
                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_implements1() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBaseTestGroup2() {
                public void baseMethod() {

                }
            });
    }

    @Test
    public void publicNonTestCaseMethod_implements1_notTestCase() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBaseTestGroup22() {
                @NonTestCase
                public void baseMethod() {

                }
            });
    }

    @Test
    public void publicNonTestCaseMethod_implements1_notTestCase__() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBaseTestGroup2__() {
                @NonTestCase
                public void baseMethod() {

                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_implements1_testCase() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBaseTestGroup2() {
                @TestCase
                public void baseMethod() {

                }
            });
    }

    @TestGroup
    class IBaseTestGroup2_Impl implements IBaseTestGroup2_ {
        @Override
        public void baseMethod() {

        }
    }

    @Test
    public void publicNonTestCaseMethod_implements1_testCase_class() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBaseTestGroup2_() {
                @TestCase
                public void baseMethod() {

                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_implements2() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBaseTestGroup2() {
                @Override
                public void baseMethod() {

                }
            });
    }

    @Test()
    public void publicNonTestCaseMethod_implements2_notTestCase() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBaseTestGroup22() {
                @NonTestCase
                @Override
                public void baseMethod() {

                }
            });
    }

    @TestGroup
    class IBaseNoneTGImpl_1 implements IBaseNoneTG {
        @NonTestCase
        @Override
        public void baseMethod() {

        }

        @TestCase
        public void baseMethod2() {

        }
    }

    @TestGroup
    class IBaseNoneTGImpl_2 implements IBaseNoneTG {
        @TestCase
        @Override
        public void baseMethod() {

        }

        @TestCase
        public void baseMethod2() {

        }
    }

    @TestGroup
    class IBaseNonTGImpl2 implements IBaseNoneTG {
        @NonTestCase
        @Override
        public void baseMethod() {

        }

        public void baseMethod2() {

        }
    }

    @Test()
    public void publicNonTestCaseMethod_implements_nonTestGroup() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBaseNoneTGImpl_1());

    }

    @Test
    public void publicNonTestCaseMethod_implements_nonTestGroup_1() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBaseNoneTGImpl_2());

    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_implements_nonTestGroup2() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBaseNonTGImpl2() {
            });

    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_inheritedNotTestCase() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup2() {
            });
    }

    @Test()
    public void publicNonTestCaseMethod_inheritedTestCase2() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup4() {
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_inheritedTestCase3() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup5() {
            });
    }

    @TestGroup
    public static class BaseTestGroup6 {
        boolean baseCalled;
        @TestCase
        public void baseMethod() {
            baseCalled = true;
            throw new RuntimeException();
        }
    }

    @TestGroup
    public static class TestGroup6 extends BaseTestGroup6{
        boolean overriddenCalled;
        @TestCase
        public void baseMethod() {
            overriddenCalled = true;
        }
    }

    @Test
    public void publicNonTestCaseMethod_inheritedTestCase4() {
        TestGroup6 tg = new TestGroup6();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(tg);
        Assert.assertTrue(tg.overriddenCalled);
        Assert.assertFalse(tg.baseCalled);
        Assert.assertTrue(status.isOK());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("test cases: 1; all passed", status.getMessage());
    }


    @TestGroup
    public static interface BaseInterface6 {
        static boolean[] methodCalled = {false};
        @TestCase
        public default void baseMethod() {
            methodCalled[0] = true;
        }
    }

    @TestGroup
    public static class TestGroup6_ implements BaseInterface6 {
        boolean overriddenCalled;
        @TestCase
        public void baseMethod() {
            overriddenCalled = true;
        }
    }

    @Test
    public void publicNonTestCaseMethod_inheritedTestCase4_fromInterface_IntfItself() {
        BaseInterface6.methodCalled[0] = false;
        BaseInterface6 tg = new BaseInterface6() {};
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(tg);
        Assert.assertTrue(BaseInterface6.methodCalled[0]);
        Assert.assertTrue(status.isOK());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("test cases: 1; all passed", status.getMessage());
    }

    @Test
    public void publicNonTestCaseMethod_inheritedTestCase4_fromInterface() {
        BaseInterface6.methodCalled[0] = false;
        TestGroup6_ tg = new TestGroup6_();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(tg);
        Assert.assertTrue(tg.overriddenCalled);
        Assert.assertFalse(BaseInterface6.methodCalled[0]);
        Assert.assertTrue(status.isOK());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("test cases: 1; all passed", status.getMessage());
    }

    @TestGroup
    public static class BaseTestGroup7 {
        boolean baseCalled;
        @NonTestCase
        public void baseMethod() {
            baseCalled = true;
            throw new RuntimeException();
        }
    }

    @TestGroup
    public static class TestGroup7 extends BaseTestGroup7 {
        boolean overriddenCalled;

        @NonTestCase
        public void baseMethod() {
            overriddenCalled = true;
        }
    }

    @Test
    public void publicNonTestCaseMethod_inheritedTestCase5() {
        TestGroup7 tg = new TestGroup7();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(tg);
        Assert.assertTrue(status.isOK());
        Assert.assertFalse(tg.baseCalled);
        Assert.assertFalse(tg.overriddenCalled);
        Assert.assertEquals("No test cases found (or all test cases excluded.)", status.getMessage());
    }

    @TestGroup
    public static class BaseTestGroup8 {
        boolean baseCalled;
        @NonTestCase
        public void baseMethod() {
            baseCalled = true;
            throw new RuntimeException();
        }
    }

    @TestGroup
    public static class TestGroup8 extends BaseTestGroup8 {
        boolean overriddenCalled;

        @TestCase
        public void baseMethod() {
            overriddenCalled = true;
        }
    }

    @Test
    // TODO this case doesn't work as it should - a conflict must be reported!
    public void publicNonTestCaseMethod_inheritedTestCase6() {
        TestGroup8 tg = new TestGroup8();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(tg);
        Assert.assertTrue(status.isOK());
        Assert.assertFalse(tg.baseCalled);
        Assert.assertTrue(tg.overriddenCalled);
        Assert.assertEquals("test cases: 1; all passed", status.getMessage());
    }

    @TestGroup
    public static class BaseTestGroup9 {
        boolean baseCalled;
        @TestCase
        public void baseMethod() {
            baseCalled = true;
            throw new RuntimeException();
        }
    }

    @TestGroup
    public static class TestGroup9 extends BaseTestGroup9 {
        boolean overriddenCalled;

        @NonTestCase
        public void baseMethod() {
            overriddenCalled = true;
        }
    }

    // TODO this case doesn't work as it should - a conflict must be reported!
    @Test
    public void publicNonTestCaseMethod_inheritedTestCase7() {
        TestGroup8 tg = new TestGroup8();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(tg);
        Assert.assertTrue(status.isOK());
        Assert.assertFalse(tg.baseCalled);
        Assert.assertTrue(tg.overriddenCalled);
        Assert.assertEquals("test cases: 1; all passed", status.getMessage());
    }


    @Test()
    public void publicNonTestCaseMethod_inheritedTestCase3_message() {
        try {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup5() {
            });
            Assert.fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Please remove one of @TestCase or @NonTestCase annotation from method \"com.oracle.tck.lib.autd2.unittests.tgfported.general.InvalidTestCases$BaseTestGroup5.baseMethod\".", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_implements3() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBase2() {
                public void baseMethod() {

                }
            });
    }

    @Test()
    public void publicNonTestCaseMethod_implements33() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBase2() {
                @NonTestCase
                public void baseMethod() {

                }
            });
    }

    @Test()
    public void publicNonTestCaseMethod_implements33_() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBase2() {
                @TestCase
                public void baseMethod() {

                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_implements333() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBase2() {
                @NonTestCase
                @TestCase
                public void baseMethod() {

                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultInherited() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBase3() {
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonDefaultInherited() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new IBase4() {
                @Override
                public void testCase() {

                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_implements4() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new ABase1() {
                public void baseMethod() {

                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_implements444() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new ABase1() {
                @NonTestCase
                @TestCase
                public void baseMethod() {

                }
            });
    }

    @Test()
    public void publicNonTestCaseMethod_implements44() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new ABase1() {
                @NonTestCase
                public void baseMethod() {

                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void publicNonTestCaseMethod_implements5() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new ABase1() {

                @TestCase
                public void aTestCase() {

                }

                public void baseMethod() {

                }
            });
    }

    @Test
    public void publicNonTestCaseMethod_implements8_lambda_syntheticMethodsFiltering() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new DefaultTestCasesWithLambda() {
                @TestCase
                public void aTestCase() {

                }

                @TestCase
                public void baseMethod() {
                    Function<Object, String> f = el -> "123";
                }
            });
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("test cases: 3; all passed", status.getMessage());
    }

    @Test
    public void publicNonTestCaseMethod_implements8_lambda_syntheticMethodsFiltering2() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new DefaultTestCasesWithLambda2() {
                @TestCase
                public void aTestCase() {

                }

                @TestCase
                public void baseMethod() {
                    Function<Object, String> f = el -> "123";
                }
            });
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("test cases: 4; all passed", status.getMessage());
    }



    @Test()
    public void publicNonTestCaseMethod_implements55() {
            com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new ABase1() {

                @TestCase
                public void aTestCase() {

                }

                @NonTestCase
                public void baseMethod() {

                }
            });
    }



    @TestGroup
    public static interface IBaseTestGroup {

    }


    @TestGroup
    public static interface IBaseTestGroup2 {
        void baseMethod();
    }


    public static interface IBaseNoneTG {
        public void baseMethod();
        @TestCase
        public void baseMethod2();

        @TestCase
        public default void defaultMethod() {

        }
    }

    @TestGroup
    public static interface IBaseTestGroup22 {
        @TestCase
        void baseMethod();
    }

    @TestGroup
    public static interface IBaseTestGroup2__ {
        @NonTestCase
        void baseMethod();
    }

    public static interface IBaseTestGroup2_ {
        void baseMethod();
    }

    @TestGroup
    public static class BaseTestGroup2 {
        public void baseMethod() {};
    }

    @TestGroup
    public static class BaseTestGroup3 {
        @NonTestCase
        public void baseMethod() {};
    }

    @TestGroup
    public static class BaseTestGroup4 {
        @TestCase
        public void baseMethod() {};
    }

    @TestGroup
    public static class BaseTestGroup5 {
        @TestCase
        @NonTestCase
        public void baseMethod() {};
    }


    public static interface IBase1 {
        void baseMethod();
    }

    @TestGroup
    public static interface IBase2 extends IBase1 {

    }

    @TestGroup
    public static interface IBase3  {
        default void testCase() {
        }
    }

    @TestGroup
    public static interface IBase4  {
        void testCase();
    }

    @TestGroup
    public static abstract class ABase1 {
        abstract void baseMethod();
    }

    @TestGroup
    public static interface DefaultTestCasesWithLambda {

        @TestCase
        default void baseTestCase() {
            // this will transform into synthetic method
            Function<?, String> f = el -> "123";
        }
    }

    @TestGroup
    public static interface DefaultTestCasesWithLambda2 {

        static final List<Function<?, String>> f = new ArrayList<>();

        @TestCase
        default void baseTestCase() {
            // this will transform into synthetic method
            f.add(el -> "123");
        }

        @TestCase
        default void baseTestCase2() {
            // this will transform into synthetic method
            Predicate<Boolean> p1 = obj -> false;
            Predicate<Boolean> p2 = obj -> true;
        }

        @NonTestCase
        default void baseNonTestCase() {
            // this will transform into synthetic method
            Predicate<Boolean> p1 = obj -> false;
            Predicate<Boolean> p2 = obj -> true;
        }

    }

}
