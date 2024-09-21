---
hIndent: 1
title: Using the Batch Command
---

[]{#runTests}

# Using the `batch` Command

The `batch` command is a legacy command that is used to run tests from the command line or as part
of a build process. If a task command is not included in the command line, the harness begins
running tests automatically. The [`runTests`](runTests.html) command supercedes the `batch` command.

The `batch` command is also used in the command line to close the harness when all commands are
processed. If the `batch`command is used, the harness GUI will not start unless explicitly started
by another commands in the command string.

If the GUI is started in batch mode (such as including the `monitorAgent` command), after all
commands are executed the harness displays a dialog that enables the user to cancel the automatic
shutdown and to use the harness GUI in normal mode.

In its legacy format, the `batch` command was required to precede the other commands. In the present
format, the `batch` command can be specified in any location on the command line.

[*\> jtharness*](aboutExamples.html) \... `-batch` \... \[*setup-commands*\] \... \[*task-command*\]
\...

See [About the Command-Line Examples](aboutExamples.html) for a description of use of \> *harness*
in the example.

See [Command-Line Overview](commandLine.html) for a description of the command-line structure.

## Detailed Example of `batch` Command

In the following example, *myconfig*`.jti` represents a configuration file name that might exist on
your system.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) `-batch -config` *myconfig*`.jti`

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
