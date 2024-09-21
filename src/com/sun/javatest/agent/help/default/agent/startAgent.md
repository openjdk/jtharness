---
hIndent: 1
lang: en
title: Starting a JT Harness Agent
---

[]{#startAgent}

# Starting a JT Harness Agent

You can start an agent either as an application or as an applet. While the application provides you
with the option of using either a GUI or a command line to configure and run the agent, the applet
requires that you use a GUI. The following table describes the agent interface support for
application and applets.

  Interface      Application   Applet
  -------------- ------------- ---------------
  GUI            Supported     Supported
  Command Line   Supported     Not Supported

[]{#startAgent.application}

## Agent Application

You can either use the application GUI or command line to configure and start an agent if the test
system provides AWT support.

If a test platform is unable to or does not provide AWT support, you must use the command line to
configure and start the agent. When using the command line to directly configure and run an agent,
the following conditions apply:

-   All agent options must be specified in the command line.
-   Agent performance cannot be monitored during a test run.
-   Agent properties cannot be modified without killing the agent and starting a new agent from the
    command line.

If you use the GUI to run the agent, the following conditions apply:

-   Agent options can be included in the command line or the GUI can be started without specifying
    agent options.
-   Agent performance is monitored during a test run.
-   Agent can be configured or reconfigured after the GUI starts.

The GUI used by the application is the same as that used by the applet. Refer to [Using the
GUI](#startAgent.gui) for a description of the tabbed panes.

[]{#startAgent.applet}

## Agent Applet

You can use either an applet or an application to run the agent on any test system that supports a
web browser. However, you must use the applet when testing Java virtual machines that run in web
browsers.

The GUI used by the applet is the same as that used by the application. Refer to [Using the
GUI](#startAgent.gui) for a description of the tabbed panes.

When using the applet, you can perform the following actions:

-   Include parameters in the applet tag or start the GUI without specifying any parameters
-   Configure or reconfigure the agent after the GUI starts
-   Monitor agent performance during a test run.

[]{#startAgent.gui}

## Using the GUI

The GUI contains four tabbed panes and three buttons used to configure, control, and monitor the
agent.

![The agent GUI contains four tabbed panes and three
buttons](../../images/agentGUI.gif){longdesc="startAgent.html"}

-   The parameters tabbed pane allows you to configure, start, and stop the agent.
-   The statistics tabbed pane displays detailed information about the tests that the agent is
    running.
-   The history and selected task tabbed panes allow you to monitor tasks performed by the agent.
-   The Start and Stop buttons control the agent.

**Next task:**

[**Starting an Agent Application**](startApplication.html): Start an agent application on your test
system.

[**Starting an Agent Applet**](startApplets.html): Start an agent applet on your test system.

 

The terms \"Java Virtual Machine\" and \"JVM\" mean a Virtual Machine for the Java™ platform.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
