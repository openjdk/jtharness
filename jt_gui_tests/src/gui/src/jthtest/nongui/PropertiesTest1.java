/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jthtest.nongui;

import java.util.Properties;
import java.io.IOException;
import java.io.StringReader;
import jthtest.NonguiTest;

/**
 *
 * @author andrey
 */
public class PropertiesTest1 extends NonguiTest {
    // should always give a : 1; b : 2; c : 3
    String tests1[] = {"a=1\nb:2\nc 3\n", "a=1\r\nb:2\r\nc 3\r\n", "a=1\r\nb:2\nc 3\r\n", "a =1\nb :2\nc  3\n", "a= 1\nb: 2\nc  3\n", "a = 1\nb : 2\nc   3\n", "a \t =\f1\nb\t\t :\f2\nc\t\t3\n"};
    // should give a\b : 1; a=b : 2; c : =3; ac : 4;
    // a\c should be null
    String test2 = "a\\\\b=1\na\\=b=2\nc==3\na\\c=4";

    public void testImpl() throws Exception {
        test1();
        test2();
    }

    private void test2() throws Exception {
        Properties p = new Properties();
        p.load(new StringReader(test2));
        boolean b = false;
        if (!"1".equals(p.getProperty("a\\b"))) {
            if (!b) {
                b = true;
                errors.add("Error in test string '" + test2 + "' :\n");
            }
            errors.add("a\\b == " + p.getProperty("a\\b") + " (should be 1)");
        }
        if (!"2".equals(p.getProperty("a=b"))) {
            if (!b) {
                b = true;
                errors.add("Error in test string '" + test2 + "' :\n");
            }
            errors.add("a=b == " + p.getProperty("a=b") + " (should be 2)");
        }
        if (!"=3".equals(p.getProperty("c"))) {
            if (!b) {
                b = true;
                errors.add("Error in test string '" + test2 + "' :\n");
            }
            errors.add("c == " + p.getProperty("c") + " (should be =3)");
        }
        if (!"4".equals(p.getProperty("ac"))) {
            if (!b) {
                b = true;
                errors.add("Error in test string '" + test2 + "' :\n");
            }
            errors.add("a\\b == " + p.getProperty("a\\b") + " (should be 1)");
        }
        if (p.getProperty("a\\c") != null) {
            if (!b) {
                b = true;
                errors.add("Error in test string '" + test2 + "' :\n");
            }
            errors.add("a\\c == " + p.getProperty("a\\c") + " (should be null)");
        }
    }

    private void test1() throws IOException {
        for (String s : tests1) {
            Properties p = new Properties();
            p.load(new StringReader(s));
            boolean b = false;
            if (!"1".equals(p.getProperty("a"))) {
                if (!b) {
                    errors.add("Error in test string '" + s + "' :\n");
                    b = true;
                }
                errors.add("a == '" + p.getProperty("a") + "' ('1' expected)\n");
            }
            if (!"2".equals(p.getProperty("b"))) {
                if (!b) {
                    errors.add("Error in test string '" + s + "' :\n");
                    b = true;
                }
                errors.add("b == '" + p.getProperty("b") + "' ('2' expected)\n");
            }
            if (!"3".equals(p.getProperty("c"))) {
                if (!b) {
                    errors.add("Error in test string '" + s + "' :\n");
                    b = true;
                }
                errors.add("c == '" + p.getProperty("c") + "' ('3' expected)\n");
            }
        }
    }

}
