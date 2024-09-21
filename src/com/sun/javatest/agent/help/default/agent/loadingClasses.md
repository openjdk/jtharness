---
hIndent: 0
title: Installing Agent Classes on a Test System
---

[]{#configurePassiveAgents}

# Installing Agent Classes on a Test System

Before you can use the JT Harness harness agent to run tests, you must load the agent classes on
your test system. You can load the agent classes by doing one of the following:

-   Copy the `javatest.jar` file directly to the test system if adequate space is available
    (approximately 5.7megs). The `javatest.jar` file contains all of the required JT Harness harness
    agent classes.
-   Extract the minimum set of classes from the `javatest.jar` file for the type of agent user
    interface and copy them to the test system.

The following table provides links to the required classes for each type of agent user interface.

  **Agent User Interface**   **Required Classes**
  -------------------------- --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Agent GUI                  See [Classes Required to Use the GUI](classDepLists/AgentFrame.html) for a list of the minimum set of classes required for using the GUI to run agents.
  Command Line               See [Classes Required to Use the Command Line](classDepLists/AgentMain.html) for a list of the minimum set of classes required for using the command line to run agents.
  Applets                    See [Classes Required to Use Applets](classDepLists/AgentApplet.html) for a list of the minimum set of classes required for using applets to run agents.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2009, Oracle and/or its affiliates. All rights reserved.
