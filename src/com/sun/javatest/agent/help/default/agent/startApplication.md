---
hIndent: 2
title: Starting an Agent Application
---

[]{#startApplication}

# Starting an Agent Application

Before you can start an agent application, the required classes must be installed on your test
system. Refer to [Installing Agent Classes on Test Systems](loadingClasses.html) for the location
and list of classes required to start the agent directly from the command line or using the
application GUI. Complete the following actions to start an agent application:

1.  Start the JT Harness harness GUI.

2.  Open the Configuration Editor window and configure the JT Harness harness to use an agent. In
    most cases, the window displays detailed instructions about configuring the JT Harness harness
    to run tests using an agent.

3.  If you are starting an active agent, open the Agent Monitor window and enable agent pool
    listening. Refer to [Agent Monitor Window](window.html).

    ![The following text is a note](../../images/hg_note.gif){longdesc="examplesCommandFile.html"}\
    If the agent pool is not listening when an active agent starts, the agent cannot contact the
    harness. The agent returns an error message and then waits until its timeout period ends before
    recontacting the JT Harness harness.

4.  Use the following example to enter the appropriate agent command at the command prompt:

    `java -cp` *classpath* *\[application class\]* *\[options\]*

    -   The `-cp` option sets the classpath required to run the agent. Use the ; or : separator
        appropriate for your test system when more than one class path is included in the command
        string. Refer to [Class Paths](#startApplication.classPaths) for detailed descriptions of
        the classes that your agent requires.
    -   *\[application class\]* sets the class used to run the agent application. Refer to
        [Application Classes](#startApplication.appClass) for a list and description of the classes
        used to start an agent application.
    -   *\[options\]* can be included in the command line to specify the agent parameters. Refer to
        [Agent Options](#startApplication.appOptions) for a list and description of the parameters
        that you can use to configure and start an agent.

    Example:\
    `java -cp /lib/javatest.jar com.sun.javatest.agent.AgentFrame`

    ![The following text is a note](../../images/hg_note.gif){longdesc="examplesCommandFile.html"}\
    You must include the path of the `javatest.jar` file (represented as `/lib/javatest.jar` in the
    example). The `javatest.jar` file is usually installed in the test suite `lib` directory when
    the JT Harness harness is bundled with a test suite.

5.  If you are using the application GUI to run the agent, use the Parameters tabbed pane to verify
    the agent settings and start the agent.

    The following topics provide detailed information about agent parameter settings:

    -   [**Specifying Active Agent Options**](configureActiveAgents.html): Parameter settings
        required to run an active agent.
    -   [**Specifying Passive Agent Options**](configurePassiveAgents.html): Parameter settings
        required to run a passive agent.
    -   [**Specifying Serial Agent Options**](configureSerialAgents.html): Parameter settings
        required to run a serial agent.

[]{#startApplication.classPaths}

## Classpaths

The following table describes the classpaths that are required in the command line.

+-------------------------------------------------+-------------------------------------------------+
| Classes                                         | Description                                     |
+=================================================+=================================================+
| []{#agent/startAppl                             | The location of the agent classes installed on  |
| ication!startApplication.classPaths.agentPaths} | your test system.                               |
| Agent                                           |                                                 |
|                                                 | The agent classes are either located in the     |
|                                                 | `javatest.jar` file or in the directory         |
|                                                 | containing the minimum set of classes required  |
|                                                 | to run the agent from the GUI.                  |
|                                                 |                                                 |
|                                                 | Some test suites include additional .jar files  |
|                                                 | containing classes needed for an agent to run   |
|                                                 | tests. These `.jar` files must also be included |
|                                                 | in the command string. Refer to [Installing     |
|                                                 | Agent Classes on a Test                         |
|                                                 | System](loadingClasses.html) for a description  |
|                                                 | of how agent classes can be installed.          |
+-------------------------------------------------+-------------------------------------------------+
| []{#agent/startApplica                          | Test classes are located in the classes         |
| tion!startApplication.classPaths.testPaths}Test | directory of the test suite.                    |
+-------------------------------------------------+-------------------------------------------------+

The most common error made when setting up a test platform to use an agent is entering the wrong
classpaths in the command string. Configuring your test platform to use the simplest classpaths
increases the reliability of the test run.

[]{#agent/startApplication!startApplication.appClass}

#### Application Classes

An application class is required in the command line to run the agent. The following table describes
the two application classes.

+-------------------------------------------------+-------------------------------------------------+
| Mode                                            | Application Class                               |
+=================================================+=================================================+
| No GUI                                          | `com.sun.javatest.agent.AgentMain` *options*    |
|                                                 |                                                 |
|                                                 | Used when the agent GUI is not wanted or not    |
|                                                 | available. In this mode, all options must be    |
|                                                 | fully specified on the command line. The agent  |
|                                                 | automatically starts when the Return key is     |
|                                                 | pressed. Refer to [Agent                        |
|                                                 | Options](#startApplication.appOptions) for the  |
|                                                 | options that are included on the command line.  |
+-------------------------------------------------+-------------------------------------------------+
| With GUI                                        | `com.sun.javatest.agent.AgentFrame` *options*   |
|                                                 |                                                 |
|                                                 | Used to start the agent GUI. In this mode,      |
|                                                 | options might either be given on the command    |
|                                                 | line or in the agent GUI. The agent GUI is used |
|                                                 | to start and stop the agent. Refer to [Agent    |
|                                                 | Options](#startApplication.appOptions) for the  |
|                                                 | *options* that are included on the command      |
|                                                 | line.                                           |
+-------------------------------------------------+-------------------------------------------------+

[]{#agent/startApplication!startApplication.appOptions}

#### Agent Options

The following table describes the two types of options used in the command line.

+-------------------------------------------------+-------------------------------------------------+
| **Type of Option**                              | **Description**                                 |
+=================================================+=================================================+
| Agent parameters                                | Set the parameters for the type of agent that   |
|                                                 | you are using. See the following topics for     |
|                                                 | additional information:                         |
|                                                 |                                                 |
|                                                 | -   [**Specifying Active Agent                  |
|                                                 |     Options**](configureActiveAgents.html): The |
|                                                 |     parameter settings required to run an       |
|                                                 |     active agent.                               |
|                                                 | -   [**Specifying Passive Agent                 |
|                                                 |     Options**](configurePassiveAgents.html):    |
|                                                 |     The parameter settings required to run a    |
|                                                 |     passive agent.                              |
|                                                 | -   [**Specifying Serial Agent                  |
|                                                 |     Options**](configureSerialAgents.html): The |
|                                                 |     parameter settings required to run a serial |
|                                                 |     agent.                                      |
|                                                 |                                                 |
|                                                 | If you are using the command-line application   |
|                                                 | class (`com.sun.javatest.agent.AgentMain`) to   |
|                                                 | directly configure and run the agent, you must  |
|                                                 | include all options in the command line that    |
|                                                 | are used to run the agent.                      |
|                                                 |                                                 |
|                                                 | If you are using the GUI application class      |
|                                                 | (`com.sun.javatest.agent.AgentFrame`) you can   |
|                                                 | either set the agent options in the command     |
|                                                 | line or in the GUI before running the agent.    |
+-------------------------------------------------+-------------------------------------------------+
| Additional parameters                           | Display help, run the agent, or configure other |
|                                                 | agent properties.                               |
|                                                 |                                                 |
|                                                 | Refer to [Specifying Additional Agent           |
|                                                 | Options](additionalOptions.html) for a          |
|                                                 | description of the additional parameters that   |
|                                                 | can be set.                                     |
+-------------------------------------------------+-------------------------------------------------+

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
