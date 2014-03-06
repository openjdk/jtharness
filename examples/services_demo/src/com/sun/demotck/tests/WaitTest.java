/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.demotck.tests;

import com.sun.javatest.Status;
import com.sun.javatest.Test;
import java.io.PrintWriter;

/**
 *
 */
public class WaitTest implements Test {

    public static void main(String[] args) {
        PrintWriter err = new PrintWriter(System.err, true);
        PrintWriter out = new PrintWriter(System.out, true);
        Test t = new WaitTest();
        Status s = t.run(args, out, err);
        s.exit();
    }


    public Status run(String[] args, PrintWriter out1, PrintWriter out2) {
        String timeout = "3000";
        if (args.length > 0) {
            timeout = args[0];
        }
        try {
            Exception e = new Exception();

            Thread.currentThread().sleep(Integer.parseInt(timeout));
            out1.println("Sleep is good for the body.");
            e.printStackTrace(out2);
            Thread.currentThread().sleep(2*Integer.parseInt(timeout));
            out1.println("But too much might be bad as well.");
            Thread.currentThread().sleep(3*Integer.parseInt(timeout));
            out1.println("But keep trying.");
            out1.flush();
            e.printStackTrace(out2);
            Thread.currentThread().sleep(Integer.parseInt(timeout));

            e.printStackTrace(out2);
        }
        catch (InterruptedException e) {
            return Status.failed(e.getMessage());
        }
        return Status.passed("OK");
    }

}
