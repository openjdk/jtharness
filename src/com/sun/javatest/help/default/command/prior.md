---
hIndent: 1
title: Selecting Tests With priorStatus
---

[]{#prior}

# Selecting Tests With `priorStatus`

Tests can be selected for a test run based on their prior test status. Use the `priorStatus` command
to run tests based on their results from a previous test run:

[*\> jtharness*](aboutExamples.html) \... \[*initial-set-up commands*\] \...
`-priorStatus fail,error` \... \[*task-command*\] \...

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness* in the example.

The *status-arguments* that can be used are pass, fail, error, and notRun. If you use more than one
argument, each argument must be separated by a comma.

When creating a command string to specify the prior test status, include the commands in the
following sequence:

1.  [Set up a configuration](setupCommands.html).
2.  Specify the prior test status (`priorStatus` *status-arguments*).
3.  [Include a Task Command](taskCommands.html) such as `runtests` (optional).

See [Command-Line Overview](commandLine.html) for a detailed description of the command-line
structure.

## Detailed Example of `priorStatus` Command

In the following example, *myconfig*`.jti` represents a configuration file name that might exist on
your system.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) `-config` *myconfig*`.jti` `-priorStatus fail,error -runtests`

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
