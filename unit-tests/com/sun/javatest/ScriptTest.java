package com.sun.javatest;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class ScriptTest {
    @ClassRule
    public static final TemporaryFolder tempDir = new TemporaryFolder();

    public class ScriptFailImpl extends Script{

        @Override
        public Status run(String[] args, TestDescription td, TestEnvironment env) {
            return new Status(Status.FAILED, "failed");
        }
    }

    public class ScriptTransformImpl extends ScriptFailImpl{

        @Override
        protected Status tryModifyStatus(Status originalStatus, TestDescription td) {
            return new Status(Status.PASSED, "passed");
        }
    }

    @Test
    public void statusNoTransformerTest(){
        Script script = new ScriptFailImpl();
        runScript(script);
        TestResult testResult = script.getTestResult();
        Assert.assertNotNull(testResult.getStatus());
        Assert.assertEquals(testResult.getStatus().getType(), Status.FAILED);
    }

    @Test
    public void statusTransformTest(){
        Script script = new ScriptTransformImpl();
        runScript(script);
        TestResult testResult = script.getTestResult();
        Assert.assertNotNull(testResult.getStatus());
        Assert.assertEquals(testResult.getStatus().getType(), Status.PASSED);
    }

    private void runScript(Script script){
        try {
            File dir = tempDir.newFolder();
            script.initWorkDir(WorkDirectory.create(dir, new TestSuite(dir)));
            Map<String, String> emptyMap = new HashMap<>();
            script.initTestDescription(new TestDescription(dir.getParentFile(), dir, emptyMap));
            script.initTestEnvironment(new TestEnvironment("a", emptyMap, "b"));
            script.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
