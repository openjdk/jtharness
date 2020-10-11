/*
 * Copyright (c) 2009, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javatest.example.services.interview;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import com.sun.javatest.TestSuite;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.InterviewParameters;

import com.sun.javatest.interview.BasicInterviewParameters;
import com.sun.javatest.Parameters.EnvParameters;
import com.sun.interview.Interview;
import com.sun.interview.NullQuestion;
import com.sun.interview.PropertiesQuestion;
import com.sun.interview.Question;
import com.sun.interview.StringQuestion;

import com.sun.interview.ChoiceQuestion;
import com.sun.interview.FileQuestion;
import com.sun.interview.ErrorQuestion;
import com.sun.interview.Checklist;

public class ServicesInterview extends BasicInterviewParameters implements
        EnvParameters {

    public ServicesInterview() throws InterviewParameters.Fault {
        super("services");
        init();
    }

    public ServicesInterview(String tag) throws Interview.Fault {
        super(tag);
        init();
    }

    public ServicesInterview(String tag, TestSuite ts) throws Interview.Fault {
        super(tag, ts);
        init();
    }

    private void init() {
        try {
            setHelpSet("moreinfo/interview.hs");
        }
        catch (com.sun.interview.Interview.Fault f) {
            f.printStackTrace();
        }

        // use modern semantics, not legacy behavior
        setInterviewSemantics(Interview.SEMANTIC_MAX_VERSION);

        setResourceBundle("i18n");
        setFirstQuestion(qWelcome);

        ResourceBundle rb = getResourceBundle();
        if (rb != null) {
            setTitle(rb.getString("services.interview.title"));
        }
    }

    @Override
    public void export(Map<String, String> map) {
        super.export(map);
        System.err.println("exporting main interview");
    }
    // ------ question flow (overrides) ------

    public Question getEnvFirstQuestion() {
        return qName;
    }

    protected Question getEnvSuccessorQuestion() {
        return getExcludeListFirstQuestion();
    }

    protected Question getPriorStatusSuccessorQuestion() {
        // this bypasses the concurrency question
        // all standard value questions can be rearranged or
        // bypassed
        return getTimeoutFactorFirstQuestion();
    }

    protected Question getTestsSuccessorQuestion() {
        return getTestSuitesFirstQuestion();
    }

    // ------ question flow (custom) ------

    protected Question getTestSuitesFirstQuestion() {
        return branchQ;
    }

    //----------------------------------------------------------------------
    //
    // Null questions used to break up interview

    private NullQuestion qWelcome = new NullQuestion(this, "welcome") {
        public Question getNext() {
            return getPrologSuccessorQuestion();
        }
    };

    //----------------------------------------------------------------------
    //
    // Give a name for this configuration

    private StringQuestion qName = new StringQuestion(this, "confName") {
        {
            setDefaultValue("services");
        }

        private boolean isValidIdentifier(String s) {
            if (s == null || s.equals(""))
                return false;

            if (!Character.isUnicodeIdentifierStart(s.charAt(0)))
                return false;

            for (int i = 1; i < s.length(); i++) {
                if (!Character.isUnicodeIdentifierPart(s.charAt(i)))
                    return false;
            }
            return true;
        }

        public boolean isValueValid() {
            return isValidIdentifier(value);
        }

        protected Question getNext() {
            return qDesc;
//            return questions[0];
        }
    };

    Interview subI = new SubInterview();
    //----------------------------------------------------------------------
    //
    // Give a description for this configuration

    private StringQuestion qDesc = new StringQuestion(this, "confDesc") {
        {
        }

        public boolean isValueValid() {
            return true;
        }

        public Question getNext() {
           // this puts the Tests To Run question toward the beginning
           // see getTestsSuccessorQuestion() above
           return callInterview(subI, getTestsFirstQuestion());
        }

        protected void export(Map<String, String> data) {
            if (value == null || value.equals(""))
                data.put("description", "[Not Specified]");   // would need i18n
            else
                data.put("description", String.valueOf(value));
        }


        public boolean isHidden() {
            return true;
        }

        public boolean isEnabled() {
            return false;
        }
    };

    private NullQuestion branchQ = new NullQuestion(this, "nullQ") {
        public boolean isHidden() { return true; }

        public Question getNext() {
            return qCmdType;
        }
    };


    private static final String AGENT = "agent";
    private static final String OTHER_VM = "otherVM";

    private Question qCmdType = new ChoiceQuestion(this, "cmdType") {
        {
        setChoices(new String[] { null, OTHER_VM, AGENT}, true);
        }

        public Question getNext() {
            if (value == null || value.length() == 0)
                return null;
            else if (value.equals(OTHER_VM))
                return qJVM;
            else {
                    return intro;
                }
            }

            public Checklist.Item[] getChecklistItems() {
                if (value != AGENT)
                    return null;

                return new Checklist.Item[] {
                    createChecklistItem("agent", "agent.needToStart"),
                };
            }

        protected void export(Map<String, String> data) {
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
                else {
                    String [] tests = getTests();
                    return intro;
                }   //else
            }
        };

    private Question qBadJVM = new ErrorQuestion(this, "badJVM") {
            public Object[] getTextArgs() {
                return new Object[] { qJVM.getValue().getPath() };
            }
        };

    private String getOtherVMExecuteCommand() {

        StringBuffer sb = new StringBuffer();
        sb.append("com.sun.javatest.lib.ExecStdTestOtherJVMCmd ");
        File jvm = qJVM.getValue();
        sb.append(jvm == null ? "unknown_jvm" : jvm.getPath());
        String tsCP = getTestSuite().getTestSuiteInfo("classpath");
        if (tsCP != null) {
            sb.append(" -classpath ");
            String[] paths = tsCP.split(":");
            char fs = File.separatorChar;
            char ps = File.pathSeparatorChar;
            for (String p : paths) {
                sb.append("$testSuiteRootDir" + fs + p + ps);
            }
            sb.append(" $testExecuteClass $testExecuteArgs");
        }
        return sb.toString();
    }

    // End of insertion


    // BEGIN MIDP

    private NullQuestion intro = new NullQuestion(this, "testIntro") {
        public Question getNext() {
            return pq;
        }
    };


    private PropertiesQuestion pq = new PropertiesQuestion(this, "pq") {
        {
            Properties p = new Properties();

            p.put("sfw.path", "/sfw/");
            p.put("ant.path", "apache-ant-1.7.0-bin/apache-ant-1.7.0");
            p.put("ant1.wd", "ant/");
            p.put("ant2.wd", "ant/");
            p.put("ant1.target", "build");


            setDefaultValue(p);
            setValue(p);
            this.setConstraints("ant1.wd", new FilenameConstraints());
            this.setConstraints("ant2.wd", new FilenameConstraints());
        }

        public Question getNext() {
            return qBasicParams;
        }

        protected void export(Map<String, String> data) {
            // raw dump of all properties into test env.
            Properties p = getValue();

            Enumeration<?> e = p.propertyNames();
            while(e.hasMoreElements()) {
                String key = (String)(e.nextElement());
                data.put(key, p.getProperty(key));
            }
        }

    };
    // BEGIN CONFIG FOR SAMPLE TEST SUITE

    //----------------------------------------------------------------------

    private NullQuestion qBasicParams = new NullQuestion(this, "parameters") {
            public Question getNext() {
                return getEnvSuccessorQuestion();
            }
    };

    public EnvParameters getEnvParameters() {
        return this;
    }

    // interface EnvParameters
    public TestEnvironment getEnv() {
        // this doesn't do anything right now
        HashMap<String, String> data = new HashMap<>();
        export(data);

        Set<String> keys = getPropertyKeys();  //extra values

        if(keys != null) {
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String k = it.next();
                data.put(k, retrieveProperty(k));
            }   // while
        }

        try {
            return new TestEnvironment("services", data, "services");
        } catch (TestEnvironment.Fault f) {
            f.printStackTrace();
            throw new IllegalStateException("Broken data");
        }
    }

}
