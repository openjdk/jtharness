package com.sun.javatest;

import com.sun.javatest.interview.BasicInterviewParameters;

public class CrashOnlyHarness extends Harness{
    @Override
    protected TestRunner prepareTestRunner(Parameters p) throws Fault, TestSuite.Fault{
        CrashOnlyTestRunner r = (CrashOnlyTestRunner)super.prepareTestRunner(p);
        r.setCrashOnly();
        return r;
    }
}
