package com.sun.javatest;

import com.oracle.tck.lib.autd2.unittests.exec.AutoPassTransformer;
import com.sun.javatest.exec.StatusTransformer;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class ScriptTest {
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
        Assert.assertTrue(runScript(script));
        TestResult testResult = script.getTestResult();
        Assert.assertNotNull(testResult.getStatus());
        Assert.assertEquals(testResult.getStatus().getType(), Status.FAILED);
    }

    @Test
    public void statusTransformTest(){
        Script script = new ScriptTransformImpl();
        Assert.assertTrue(runScript(script));
        TestResult testResult = script.getTestResult();
        Assert.assertNotNull(testResult.getStatus());
        Assert.assertEquals(testResult.getStatus().getType(), Status.PASSED);
    }

    private boolean runScript(Script script){
        File dir = new File("temp");
        try {
            script.initWorkDir(WorkDirectory.create(dir, new TestSuite(dir)));
            Map<String, String> emptyMap = new HashMap<>();
            script.initTestDescription(new TestDescription(dir, dir, emptyMap));
            script.initTestEnvironment(new TestEnvironment("a", emptyMap, "b"));
            script.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cleanup(dir);
    }


    private boolean cleanup(File file){
        String[]entries = file.list();
        boolean success = true;
        for(String s: entries){
            File currentFile = new File(file.getPath(),s);
            if(currentFile.isDirectory()){
                success = success && cleanup(currentFile);
            }
            else {
                success = success && currentFile.delete();
            }
        }
        return success && file.delete();
    }
}
