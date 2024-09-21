---
hIndent: 1
title: Specifying Tests to Run
---

[]{#concurrency}

# Specifying Tests to Run

You can open an existing configuration file and then specify one or more individual tests or
branches of tests for the test run. Use the `tests` command to specify one or more tests:

[*\> jtharness*](aboutExamples.html) \... \[*initial set-up commands*\] \...
`-tests api/javax_swing api/java_awt`\... \[*task command*\] \...

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness* in the example.

See [Command-Line Overview](commandLine.html) for a detailed description of the command line
structure.

The path used for setting the test or folder of tests to be run is the same as that displayed in the
GUI tree folder. One or more tests can be specified.

When specifying tests for a test run, include the commands in the following sequence:

1.  Include the commands required to set up a configuration.

> See [Set-up Commands](setupCommands.html) for detailed description of the available commands.

2.  Include the commands required to specify the tests (for example, tests in branches
    `api/javax_swing`and `api/java_awt`).
3.  (Optional) Include a task command such as the `runtests` command.

> See [Task Commands](taskCommands.html) for a description of the available commands.

## Detailed Example of Specifying Tests

In the following example, *myconfig*`.jti` and *myexcludelist*`.jtx` represent file names that might
exist on your system.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) `-config` *myconfig*`.jti` `-runtests`

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2003, 2011, Oracle and/or its affiliates. All rights reserved.
