package com.sun.javatest.util;

import com.sun.javatest.TestDescription;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *  Basic class for information about a crash of jvm.
 */
public class Crash {
    private String pid;
    private File file;


    public Crash(TestDescription td, String hs_err, String pid) {
        this.pid = pid;
        this.file = new File(td.getDir(), hs_err);
    }

    public String getPid() {
        return pid;
    }

    public File getFile() {
        return file;
    }
}
