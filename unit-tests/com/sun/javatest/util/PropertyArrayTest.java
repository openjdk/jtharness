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

package com.sun.javatest.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.sun.javatest.TestUtil;
import org.junit.Assert;
import org.junit.Test;

public class PropertyArrayTest {

    private static final String SIMPLE_INPUT = "simple.data";
    private static final String MULTILINE_OUTPUT = "multiline.data";
    private PrintWriter out = new PrintWriter(System.out);

    // param 1 is the scratch dir
    // param 2 is the test data dir
    private String[] args = {
            TestUtil.createTempDirectory("property-array-test").toAbsolutePath().toString(), TestUtil.getPathToData("propertyarray")
    };

    public PropertyArrayTest() throws IOException { }

    class NowhereWriter extends Writer {
        public void write(char cbuf[],
                          int off,
                          int len) throws IOException {
            // bit bucket
        }

        public void close() {
        }

        public void flush() {
        }
    }

    @Test
    public void run() {
        boolean localResult = true;

        if (!runInstanceTests()) {
            out.println("PropertyArray instance tests FAILED.");
            localResult = false;
        }

        if (!runClassTests()) {
            out.println("PropertyArray class tests FAILED.");
            localResult = false;
        }

        if (!runMutationTests()) {
            out.println("PropertyArray mutation tests FAILED.");
            localResult = false;
        }

        if (!runFileTests()) {
            out.println("PropertyArray file I/O tests FAILED.");
            localResult = false;
        }

        out.flush();
        Assert.assertTrue( localResult );
    }

    private boolean runInstanceTests() {
        boolean localResult = true;

        PropertyArray arr = new PropertyArray();

        // two equal keys
        arr.put("foo", "bar");
        arr.put("Apollo", "13");
        arr.put("foo", "baz");
        if (!arr.get("foo").equals("baz")) {
            localResult = false;
            out.println("PropertyArray failed to REPLACE existing value.");
        }

        arr = null;
        arr = new PropertyArray(2);

        // test a instance constructed with a given size
        if (!arr.isMutable()) {
            localResult = false;
            out.println("PropertyArray(int) did not create mutable object.");
        }

        if (arr.size() != 0) {
            localResult = false;
            out.println("size() reports non-zero size on new PropertyArray.");
        }

        if (arr.getProperties().size() != 0) {
            localResult = false;
            out.println("Empty PropertyArray gave back non-empty Property object.");
        }

        if (arr.getArray() != null) {
            localResult = false;
            out.println("Empty PropertyArray gave back non-empty Array.");
        }

        arr = null;

        // test object constructed from Properties object
        Map<String, String> props = new HashMap<>();
        props.put("foo", "bar");
        props.put("Snapple", "cap");

        arr = new PropertyArray(props);
        if (arr.size() != 2) {
            localResult = false;
            out.println("PropertyArray constructed from property object does not have the right number of elements.");
        }

        if (arr.get("foo") == null ||
                arr.get("Snapple") == null) {
            localResult = false;
            out.println("PropertyArray constructed from property object does not have the expected keys.");
        }

        if (arr.isMutable()) {
            localResult = false;
            out.println("PropertyArray constructed from property object is mutable!");
        }
        arr = null;

        arr = new PropertyArray();
        // insert a null key and non-null value
        try {
            arr.put(null, "invisible");
            localResult = false;
            out.println("PropertyArray stored a null-key...which is illegal.");
        } catch (PropertyArray.PropertyArrayError e) {
            // we should end up here
        }

        if (arr.size() != 0) {
            localResult = false;
            out.println("PropertyArray may have stored a null-key item (size).");
        }

        if (arr.get(null) != null) {
            localResult = false;
            out.println("PropertyArray stored a null-key item! (get).");
        }

        arr = null;
        arr = new PropertyArray();

        // test getArray
        arr.put("sobe", "oolong");
        arr.put("phone", "number");
        arr.put("lunch", "menu");

        String[] back = arr.getArray();
        if (back.length / 2 != 3) {
            localResult = false;
            out.println("Area created from Properties object is the wrong size");
        }

        // test removal of value
        arr = new PropertyArray();
        arr.put("Raspberry", "Snapple");
        arr.put("Coke", "can");
        arr.put("Post", "it");

        arr.remove("Coke");

        if (arr.size() != 2) {
            localResult = false;
            out.println(
                    "Removing value did not seem to work (expected 2), got " +
                            arr.size() + ".");
        }

        if (arr.get("Coke") != null) {
            localResult = false;
            out.println(
                    "A key that was supposed to be removed is still there.");
        }

        return localResult;
    }

    private boolean runClassTests() {
        boolean localResult = true;

        String[] data1 = {"food", "burger", "drink", "snapple",
                "fruit", "grape", "vehicle", "boat"};
        String[] back = null;

        // verify that puts work (by length)
        back = PropertyArray.put(data1, "Vulcan", "Spock");
        if (back.length != data1.length + 2) {
            localResult = false;
            out.println("PropertyArray failed in static put. (1)");
        }

        // verify that static get works
        if (!PropertyArray.get(back, "Vulcan").equals("Spock")) {
            localResult = false;
            out.println("PropertyArray failed in static get. (1)");
        }

        back = null;

        // verify conversion Properties->String[]
        Map<String, String> props1 = new HashMap<>();
        props1.put("ice", "water");
        props1.put("dry ice", "carbon dioxide");

        back = PropertyArray.getArray(props1);
        if (back.length != props1.size() * 2) {
            localResult = false;
            out.println("PropertyArray failed in static Properties->String[].");
        }

        // verify conversion String[]->Properties
        Map<String, String> props2;
        String[] data2 = {"Linux", "lilo", "Windows", "crash",
                "Apple", "Juice"};

        props2 = PropertyArray.getProperties(data2);
        if (props2.size() != data2.length / 2) {
            localResult = false;
            out.println("PropertyArray failed in static String[]->Properties.");
        }
        data2 = null;
        back = null;

        // verify length 0 array
        // make sure a put works
        String[] data3 = new String[0];
        back = PropertyArray.put(data3, "Root", "Beer");
        if (back == null || back.length != 2) {
            localResult = false;
            out.println(
                    "PropertyArray failed in static put into empty array.");
        }
        data3 = null;
        back = null;

        // test retrieval of invalid key
        String[] data4 = {"magenta", "cyan", "yellow", "black"};
        String result = PropertyArray.get(data4, "foo");
        if (result != null) {
            localResult = false;
            out.println(
                    "PropertyArray.get did not return null when getting non-existant key.");
        }
        data4 = null;
        back = null;

        String[] data5 = new String[0];
        if (PropertyArray.getProperties(data5).size() != 0) {
            localResult = false;
            out.println(
                    "PropertyArray.getProperties returned non-empty object when given empty data.");
        }
        data5 = null;
        back = null;

        Writer testOut = new NowhereWriter();

        try {
            // these should do nothing, but an exception would definitely
            // indicate a problem
            PropertyArray.save(null, testOut);
            PropertyArray.save(new String[0], testOut);
        } catch (Throwable e) {
            localResult = false;
            out.println("PropertyArray failure while writing null/empty array");
            e.printStackTrace(out);
        }

        return localResult;
    }

    private boolean runMutationTests() {
        boolean localResult = true;

        try {
            PropertyArray parr = new PropertyArray();
            parr.put("Time", "Bomb");
        } catch (IllegalStateException e) {
            out.println("PropertyArray didn't allow mutation. (1)");
            localResult = false;
        }

        try {
            PropertyArray parr = new PropertyArray(5);
            parr.put("Time", "Bomb");
        } catch (IllegalStateException e) {
            out.println("PropertyArray didn't allow mutation. (2)");
            localResult = false;
        }

        try {
            String[] data = {"light", "dark"};
            PropertyArray parr = new PropertyArray(data);
            parr.put("Time", "Bomb");
            localResult = false;
            out.println("PropertyArray allowed illegal mutation.");
        } catch (IllegalStateException e) {
            // this should cause exception
        }

        return localResult;
    }

    /**
     * Run read/write tests to files.
     */
    private boolean runFileTests() {
        boolean localResult = true;
        String lineSep = System.getProperty("line.separator");
        String key1 = "C-Style";
        String key2 = "JavaStyle";
        String value1 = "hello \n newline";
        String value2 = "howdy " + lineSep + "line sep";

        File workDir = new File(args[0]);
        if (!workDir.exists()) {
            workDir.mkdirs();
        }

        // read from test data dir
        File data = new File(args[1], SIMPLE_INPUT);

        PropertyArray arr = null;

        try {
            arr = new PropertyArray(new FileReader(data));
        } catch (java.io.FileNotFoundException e) {
            localResult = false;
            out.println("PropertyTest could not find file: " +
                    data.getAbsolutePath());
        } catch (java.io.IOException e) {
            localResult = false;
            e.printStackTrace();
        }

        if (arr == null) {
            localResult = false;
            out.println("PropertyArray file load failed, no data returned.");
        }

        if (arr.size() != 4) {
            localResult = false;
            out.println("PropertyArray file load failed, too few/many items: "
                    + arr.size());
        }
        // should check the data now

        // Test multi-line values
        arr = new PropertyArray(2);
        arr.put(key1, value1);
        arr.put(key2, value2);

        // writing to scratch dir
        File theFile = new File(args[0], MULTILINE_OUTPUT);
        try {
            FileWriter dataOut = new FileWriter(theFile);
            arr.save(dataOut);
            dataOut.flush();
            dataOut.close();
            arr = null;

            // do the reading portion
            FileReader in = new FileReader(theFile);
            arr = new PropertyArray(in);
            in.close();
        } catch (IOException e) {
            out.println("Error while reading or writing a property file.");
            e.printStackTrace(out);
            localResult = false;
        }
        theFile = null;

        // check size
        if (arr.size() != 2) {
            localResult = false;
            out.println("Number of props not the same after save and load.");
            out.println("Original size was 2, resulting size: " +
                    arr.size());
        }

        String s1 = arr.get(key1);
        if (s1 == null) {
            localResult = false;
            out.println("Could not retrieve " + key1 + " property.");
        } else {
            // check for the newline
            if (!s1.equals(value1)) {
                localResult = false;
                out.println("Could not find the \\n in the \"" + key1 + "\" value.");
            }
            s1 = null;
        }

        String s2 = arr.get(key2);
        if (s2 == null) {
            localResult = false;
            out.println("Could not retrieve \"" + key2 + "\" property.");
        } else {
            if (!s2.equals(value2)) {
                localResult = false;
                out.println("Could not find the line separator in the \"" +
                        key2 + "\" value.");
            }
            s2 = null;
        }

        return localResult;
    }

}

