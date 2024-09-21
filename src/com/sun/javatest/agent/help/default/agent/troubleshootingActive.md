---
hIndent: 2
title: Troubleshooting Active Agents
---

[]{#monitoring}

# Troubleshooting Active Agents

Active agents initiate the connection with the JT Harness harness. You must set up the JT Harness
harness agent pool so that the connection is made before running tests.

Errors in configuring, synchronizing, or implementing the connection between the agent and the JT
Harness harness are the most probable causes of failure.

Use the Agent Monitor window, the JT Harness harness Test Manager window, the agent GUI, and the
following list of actions as a guide when troubleshooting problems running active agents.

1.  In the Agent Monitor window, verify that the agent is listed in the agent pool. If the agent is
    not listed in the agent pool perform the following actions:
    a.  Verify that the Listening check box is selected.
    b.  Verify that the agent is configured to contact the correct active host and that the port
        value of the harness matches the port value used by the agent.
    c.  Check the physical connection between the JT Harness harness platform and the test platform.
2.  Verify that the agent moves to Agents Currently in Use when tests are running. If the agent does
    not move to Agents Currently in Use when tests are running perform the following actions:
    a.  Use the Configuration Editor in the Test Manager window to verify that the harness is
        configured to use agents when running tests. See the *JT Harness User\'s Guide: Graphical
        User Interface* for detailed description of the Configuration Editor and the Test Manager
        window.
    b.  If you are running the tests using multiple Java Virtual Machines, use the Configuration
        Editor window to verify that the path you provided in the Java Launcher question is the path
        of the launcher for the *agent* running tests.
3.  If tests are failing or have errors, check the error messages displayed in the Test Manager
    window. If the error indicates that tests are failing because of missing classes perform the
    following actions:
    a.  Verify that the class paths used to start the agent are correct.
    b.  Use the Configuration Editor window to verify that the harness is correctly configured to
        use the agent on the test system.
    c.  Run the agent using the `-trace` option to verify that the paths in the stream messages for
        the test are correct. If the paths are not correct for the test system, create a map file
        for the agent to use in translating host specific values into values that the agent can use.
        See [\"Creating a Map File](mapFile.html)
    d.  If a map file was used to run the test, use the Test Run Messages pane to verify that the
        `-mapArgs` command is present in the stream messages. If the `-mapArgs` command is not
        present, verify that both the agent *and* the harness are configured to use the map file.
        Use the Configuration Editor window to verify that the harness is configured to use the
        agent map file.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
