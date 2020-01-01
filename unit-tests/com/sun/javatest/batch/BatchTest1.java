/*
 * $Id$
 *
 * Copyright (c) 2001, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.batch;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Vector;

import com.sun.javatest.KeyTest;
import com.sun.javatest.batch.BatchManager;
import com.sun.javatest.tool.Command;
import com.sun.javatest.tool.CommandContext;
import com.sun.javatest.tool.CommandManager;
import com.sun.javatest.tool.CommandParser;
import com.sun.javatest.tool.ConfigManager;
import com.sun.javatest.tool.HelpManager;
import com.sun.javatest.tool.Main;
import org.junit.Assert;
import org.junit.Test;

/**
 * A set of tests to do syntax checking for batch option arguments
 */
public class BatchTest1 {

    public CommandParser createParser() {
        CommandManager configMgr = new ConfigManager();
        CommandManager batchMgr = new BatchManager();
        HelpManager helpMgr = new HelpManager();
        CommandManager[] cmdMgrs = {configMgr, batchMgr, helpMgr};
        helpMgr.setCommandManagers(cmdMgrs);
        return new CommandParser(cmdMgrs);
    }


    // verify that a command can be given in a single string
    @Test
    public void test_cmd()
            throws Command.Fault, CommandParser.Fault {
        String[] args = {"-batch", "testSuite dummyTestSuite"};
        CommandContext ctx = new CommandContext();
        createParser().parse(args, ctx);
    }

    // verify that a command can be given as a series of strings, the first
    // prefixed with -
    @Test
    public void test_opt() throws Command.Fault, CommandParser.Fault {
        String[] args = {"-batch", "-testSuite", "dummyTestSuite"};
        CommandContext ctx = new CommandContext();
        createParser().parse(args, ctx);
    }

    // verify that a command can be given in a file
    @Test
    public void test_file1()
            throws Command.Fault, CommandParser.Fault, IOException {
        File f = File.createTempFile("BatchTest1-", ".jtb");
        BufferedWriter w = new BufferedWriter(new FileWriter(f));
        w.write("testSuite dummyTestSuite");
        w.newLine();
        w.close();
        String[] args = {"-batch", "@" + f};
        CommandContext ctx = new CommandContext();
        createParser().parse(args, ctx);
    }

    // verify comments, newlines, semicolons in files
    @Test
    public void test_file2()
            throws Command.Fault, CommandParser.Fault, IOException {
        File f = File.createTempFile("BatchTest1-", ".jtb");
        BufferedWriter w = new BufferedWriter(new FileWriter(f));
        w.write("#this is a comment, followed by a blank line");
        w.newLine();
        w.newLine();
        w.write("testSuite dummyTestSuite");
        w.newLine();
        w.write("workDir /tmp/work # this comment terminates the preceding command");
        w.newLine();
        w.write("concurrency 1 ; timeoutFactor 2");
        w.newLine();
        w.close();
        String[] args = {"-batch", "@" + f};
        CommandContext ctx = new CommandContext();
        createParser().parse(args, ctx);
    }
}
