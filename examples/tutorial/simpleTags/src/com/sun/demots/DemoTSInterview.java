/*
 * $Id$
 *
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.demots;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.sun.interview.Checklist;
import com.sun.interview.ChoiceQuestion;
import com.sun.interview.ErrorQuestion;
import com.sun.interview.FileQuestion;
import com.sun.interview.FinalQuestion;
import com.sun.interview.IntQuestion;
import com.sun.interview.NullQuestion;
import com.sun.interview.Question;
import com.sun.interview.StringQuestion;

import com.sun.javatest.Parameters.EnvParameters;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.interview.BasicInterviewParameters;

public class DemoTSInterview
    extends BasicInterviewParameters
    implements EnvParameters
{
    public DemoTSInterview() throws Fault {
        super("demoTS");
        setHelpSet("moreInfo/demots");
        setResourceBundle("i18n");

        String m = System.getProperty("interview.mode", "certify");
        if (m.equals("certify"))
            mode = CERTIFY;
        else if (m.equals("developer"))
            mode = DEVELOPER;
        else if (m.equals("precompile"))
            mode = PRECOMPILE;
    }

    public TestEnvironment getEnv() {
        Map<String, String> envProps = new HashMap<>();
        export(envProps);
        try {
            String name = qName.getValue();
            if (name == null || name.length() == 0)
                name = "unknown";
            return new TestEnvironment(name, envProps, "configuration interview");
        }
        catch (TestEnvironment.Fault e) {
            throw new Error("should not happen");
        }
    }

    public EnvParameters getEnvParameters() {
        return this;
    }

    public Question getEnvFirstQuestion() {
        switch (mode) {
        case PRECOMPILE:
            return qPrecompile;
        case DEVELOPER:
            return qDeveloper;
        default:
            return qName;
        }
    }

    //----------------------------------------------------------------------
    //
    // Precompile mode

    private Question qPrecompile = new NullQuestion(this, "precompile") {
            public Question getNext() {
                return qEnvEnd;
            }

            public void export(Map<String, String> data) {
                data.put("script.mode", "precompile");
                data.put("command.compile.java", System.getProperty("command.compile.java"));
            }
        };

    //----------------------------------------------------------------------
    //
    // Developer mode

    private Question qDeveloper = new NullQuestion(this, "developer") {
            public Question getNext() {
                return qName;
            }

            public void export(Map<String, String> data) {
                data.put("script.mode", "developer");
                data.put("command.compile.java", System.getProperty("command.compile.java"));
            }
        };

    //----------------------------------------------------------------------
    //
    // Give a name for this configuration

    private StringQuestion qName = new StringQuestion(this, "name") {
            public Question getNext() {
                if (value == null || value.length() == 0)
                    return null;
                else
                    return qDesc;
            }
        };

    //----------------------------------------------------------------------
    //
    // Give a description for this configuration

    private Question qDesc = new StringQuestion(this, "desc") {
            public Question getNext() {
                if (value == null || value.length() == 0)
                    return null;
                else
                    return qCmdType;
            }

            public void export(Map<String, String> data) {
                data.put("description", String.valueOf(value));
            }
        };

    //----------------------------------------------------------------------
    //
    // How do you with to execute tests:
    //   OTHER_VM:  on the same system as JT Harness, in separate process
    //   AGENT:   on a different system, using JT Harness Agent

    private static final String AGENT = "agent";
    private static final String OTHER_VM = "otherVM";

    private Question qCmdType = new ChoiceQuestion(this, "cmdType") {
            {
                setChoices(new String[] { null, OTHER_VM, AGENT }, true);
            }

            public Question getNext() {
                if (value == null || value.length() == 0)
                    return null;
                else if (value.equals(OTHER_VM))
                    return qJVM;
                else
                    return qTestVerboseLevel;
            }

            public Checklist.Item[] getChecklistItems() {
                if (value != AGENT)
                    return null;

                return new Checklist.Item[] {
                    createChecklistItem("agent", "agent.needToStart"),
                };
            }

            public void export(Map<String, String> data) {
                String cmd;
                if (value != null && value.equals(OTHER_VM))
                    cmd = getOtherVMExecuteCommand();
                else
                    cmd = "com.sun.javatest.agent.ActiveAgentCommand " +
                        "com.sun.javatest.lib.ExecStdTestSameJVMCmd " +
                        "$testExecuteClass $testExecuteArgs";
                data.put("command.execute", cmd);
            }
        };

    //----------------------------------------------------------------------
    //
    // What is the path for the JVM you wish to use to execute the tests?

    private FileQuestion qJVM = new FileQuestion(this, "jvm") {
            public Question getNext() {
                if (value == null || value.getPath().length() == 0)
                    return null;
                else if (! (value.exists() && value.isFile() && value.canRead()))
                    return qBadJVM;
                else
                    return qTestVerboseLevel;
            }
        };

    private Question qBadJVM = new ErrorQuestion(this, "badJVM") {
            public Object[] getTextArgs() {
                return new Object[] { qJVM.getValue().getPath() };
            }
        };

    private String getOtherVMExecuteCommand() {
        char fs = File.separatorChar;
        char ps = File.pathSeparatorChar;

        StringBuffer sb = new StringBuffer();
        sb.append("com.sun.javatest.lib.ExecStdTestOtherJVMCmd ");
        File jvm = qJVM.getValue();
        sb.append(jvm == null ? "unknown_jvm" : jvm.getPath());
        sb.append(" -classpath $testSuiteRootDir" + fs + "lib" + fs + "jtdemots.jar" + ps
                + "$testSuiteRootDir" + fs + "lib" + fs + "demoapi.jar" + ps
                + "$testSuiteRootDir" + fs + "lib" + fs + "javatest.jar ");
        sb.append("$testExecuteClass $testExecuteArgs");
        return sb.toString();
    }

    //----------------------------------------------------------------------
    //
    // What is the test verbose level?

    private static final String LOW = "low";
    private static final String MEDIUM = "medium";
    private static final String HIGH = "high";

    private Question qTestVerboseLevel = new ChoiceQuestion(this, "testVerboseLevel") {
            {
                setChoices(new String[] { null, LOW, MEDIUM, HIGH }, true);
            }

            public Question getNext() {
                return qEnvEnd;
            }

            public void export(Map<String, String> data) {
                data.put("testVerboseLevel", String.valueOf(value));
            }
        };

    //----------------------------------------------------------------------

    private Question qEnvEnd = new NullQuestion(this, "envEnd") {
            public Question getNext() {
                return getEnvSuccessorQuestion();
            }
        };

    //----------------------------------------------------------------------

    private static final int PRECOMPILE = 1;
    private static final int CERTIFY = 2;
    private static final int DEVELOPER = 3;
    private int mode;
}
