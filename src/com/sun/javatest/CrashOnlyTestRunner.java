package com.sun.javatest;

import com.sun.javatest.util.BackupPolicy;
import com.sun.javatest.util.Crash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CrashOnlyTestRunner extends DefaultTestRunner{

    private boolean crashOnly = false;

    public void setCrashOnly(){
        crashOnly = true;
    }
    @Override
    protected TestResult runTestGiveResult(TestDescription td) {
        TestResult result = super.runTestGiveResult(td);
        if(this.crashOnly && result.getStatus().getType() == Status.FAILED && ! this.didCrash(td)){
            result = new com.sun.javatest.TestResult(td, new Status(Status.PASSED, "only a regular fail"));
        }
        return result;
    }

    /**
     * checks for the occurrence of hs_err_pid file that suggest that crash happened during execution
     **/
    private boolean didCrash(TestDescription td){
        return !getCrashes(td).isEmpty();
    }

    private List<Crash> getCrashes(TestDescription td){
        Pattern pattern = Pattern.compile("^hs_err_pid(\\d+)\\.log");
        List<String> hs_errs = Arrays.stream(td.getDir().list()).filter(pattern.asPredicate()).toList();
        List<Crash> crashes = new ArrayList<>();
        for(String hs_err : hs_errs){
            String pid = pattern.matcher(hs_err).group(1);
            crashes.add(new Crash(td, hs_err, pid));
        }
        return crashes;
    }
}
