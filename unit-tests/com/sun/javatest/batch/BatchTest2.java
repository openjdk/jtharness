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

import java.lang.reflect.Method;

import com.sun.javatest.TestUtil;
import com.sun.javatest.report.ReportManager;
import com.sun.javatest.tool.Command;
import com.sun.javatest.tool.CommandContext;
import com.sun.javatest.tool.CommandManager;
import com.sun.javatest.tool.CommandParser;
import com.sun.javatest.tool.ConfigManager;
import com.sun.javatest.tool.HelpManager;
import org.junit.Assert;
import org.junit.Test;

/**
 * A set of "unit" tests to do basic arg verification of the batch options:
 * too few, too many, just enough args etc.  The validity of args is
 * not verified, since that requires sets of consistent args
 */
public class BatchTest2 {

    public BatchTest2(){
        basicTestSuite = TestUtil.getPathToTestTestSuite("initurl");
        CommandManager batchMgr = new BatchManager();
        CommandManager configMgr = new ConfigManager();
        CommandManager reportMgr = new ReportManager();
        HelpManager helpMgr = new HelpManager();
        CommandManager[] cmdMgrs = {batchMgr, configMgr, reportMgr, helpMgr};
        helpMgr.setCommandManagers(cmdMgrs);
        parser = new CommandParser(cmdMgrs);
    }




    private CommandParser parser;
    private Method[] tests;

    private String basicTestSuite = TestUtil.getPathToTestTestSuite("initurl");

    //--------------------------------------------------------------------------
    //
    // TEST CASES
    //
    // These test cases are for basic arg counting and simple validation.

    // test cases for concurrency command

    @Test public void test_concurrency_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-concurrency"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_concurrency_numericArg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-concurrency", "1"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_concurrency_negArg_opt() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-concurrency", "-1"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_concurrency_badArg_opt() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-concurrency", "a"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_concurrency_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "concurrency 3"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_concurrency_excessArgs_cmd() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "concurrency 1 2"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }


    // test cases for env command

    @Test public void test_env_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-env"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_env_arg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-env", "dummyEnvName"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_env_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "env dummyEnvName"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_env_excessArgs_cmd() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "env dummyEnvName xyz"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }


    // test cases for envFiles command

    @Test public void test_envFiles_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-envFiles"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_envFiles_arg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-envFiles", "dummyEnvFile"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_envFiles_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "envFiles dummyEnvFile"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_envFiles_multipleArgs_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "envFiles dummyEnvFile1 dummyEnvFile2"};
        parser.parse(args, ctx);
        // OK, passed
    }


    // test cases for excludeList command

    @Test public void test_excludeList_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-excludeList"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_excludeList_arg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-excludeList", "dummyExcludeList"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_excludeList_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "excludeList dummyExcludeList"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_excludeList_multipleArgs_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "excludeList dummyExcludeList1 dummyExcludeList2"};
        parser.parse(args, ctx);
        // OK, passed
    }


    // test cases for keywords command

    @Test public void test_keywords_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-keywords"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_keywords_arg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-keywords", "a & b | c"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_keywords_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "keywords \"a & b | c\""};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_keywords_multipleArgs_cmd() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "keywords a b"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }


    // test cases for observer command

    @Test public void test_observer_noArgs() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "-observer"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_observer_class()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-observer", "NullObserver"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_observer_class_arg()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-observer", "NullObserver1Arg", "arg"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_observer_class_args_0()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-observer", "NullObserverNArg"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_observer_class_args_1()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-observer", "NullObserverNArg", "arg0"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_observer_class_args_2()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-observer", "NullObserverNArg", "arg0", "args1"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_observer_class_args_2_end()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-observer", "NullObserverNArg", "arg0", "args1", "-end"};
        parser.parse(args, ctx);
        // OK, passed
    }

    // test cases for open command

    @Test public void test_open_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-open"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_open_arg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-open", basicTestSuite};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_open_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "open " + basicTestSuite};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_open_excessArgs_cmd() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "open dummyOpenName xyz"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_open_cmd_mistake() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "-openn", basicTestSuite};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }


    // test cases for param command


    // test cases for priorStatus command

    @Test public void test_priorStatus_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-priorStatus"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_priorStatus_arg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-priorStatus", "pass,fail"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_priorStatus_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "priorStatus pass,fail"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_priorStatus_badArg_cmd() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "priorStatus pass,wombat"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_priorStatus_excessArgs_cmd() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "priorStatus pass fail"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }


    // test cases for report command

    @Test public void test_report_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-report"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_report_arg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-report", "dummyReport"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_report_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "report dummyReport"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_report_excessArgs_cmd() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "report dummyReport xyz"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    // test cases for set command

    @Test public void test_set_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-set"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_set_1arg_opt() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "-set", "dummySet"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_set_1arg_cmd() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "set dummySet"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_set_2args_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-set", "dummySetTag", "dummySetValue"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_set_2args_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "set dummySetTag dummySetValue"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_set_excessArgs_cmd() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "set dummySetTag dummySetValue xyz"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    // test cases for tests command

    @Test public void test_tests_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-tests"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_tests_arg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-tests", "dummyTest"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_tests_arg_cmd1()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "tests dummyTest"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_tests_arg_cmd2()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "tests \"dummyTest#testcase\""};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_tests_multipleArgs_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "tests dummyTest1 \"dummyTest2#testCase\""};
        parser.parse(args, ctx);
        // OK, passed
    }

    // test cases for testsuite command

    @Test public void test_testSuite_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-testSuite"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_testSuite_arg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-testSuite", "dummyTestSuite"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_testSuite_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "testSuite dummyTestSuite"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_testSuite_excessArgs_cmd() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "testSuite dummyTestSuite xyz"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    // test cases for timeoutFactor command

    @Test public void test_timeoutFactor_noArgs() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "-timeoutFactor"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_timeoutFactor_numericArg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-timeoutFactor", "1"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_timeoutFactor_negArg_opt() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "-timeoutFactor", "-1"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_timeoutFactor_badArg_opt() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "-timeoutFactor", "a"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_timeoutFactor_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "timeoutFactor 3"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_timeoutFactor_excessArgs_cmd() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "timeoutFactor 1 2"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }


    // test cases for workdir command

    @Test public void test_workDir_noArgs() {
        CommandContext ctx = new CommandContext();
        try {
            String[] args = {"-batch", "-workDir"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_workDir_arg_opt()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "-workDir", "/tmp/work"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_workDir_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "workDir /tmp/work"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_workDir_create_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "workDir -create /tmp/work"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_workDir_overwrite_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "workDir -overwrite /tmp/work"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_workDir_create_overwrite_arg_cmd()
            throws CommandParser.Fault {
        CommandContext ctx = new CommandContext();
        String[] args = {"-batch", "workDir -create -overwrite /tmp/work"};
        parser.parse(args, ctx);
        // OK, passed
    }

    @Test public void test_workDir_badOption_arg_cmd() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "workDir -badOption /tmp/work"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }

    @Test public void test_workDir_excessArgs_cmd() {
        try {
            CommandContext ctx = new CommandContext();
            String[] args = {"-batch", "workDir /tmp/work xyz"};
            parser.parse(args, ctx);
            Assert.fail();
        } catch (CommandParser.Fault e) {
            // OK, passed
        }
    }
}
