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
    private List<String> lines;


    public Crash(TestDescription td, String hs_err, String pid) {
        File hs_err_File = new File(td.getDir(), hs_err);
        this.pid = pid;
        List <String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(hs_err_File))){
            String line;
            // Read all lines from the file
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (
                FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.lines = lines;
    }

    public String getPid() {
        return pid;
    }

    public List<String> getLines() {
        return lines;
    }
}
