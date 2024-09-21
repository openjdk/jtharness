---
hIndent: 2
title: Specifying a Test Suite With testsuite
---

[]{#testsuite}

# Specifying a Test Suite With `testsuite`

To specify the test suite, use the `testsuite` command:

[*\> jtharness*](aboutExamples.html) \... `-testsuite` *path/filename* \[*work-directory-command*\]
\[*configuration-command*\] \... \[*task-command*\] \...

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness*.

See [Command-Line Overview](commandLine.html) for a description of the command line structure.

When you want to specify a test suite, include the commands in the following sequence:

1.  Include the command required to specify the test suite (`testsuite` *path/filename*).

<!-- -->

2.  Include the commands required to set up a configuration.

> See [Set-up Commands](setupCommands.html) for detailed description of the available commands.

3.  (Optional) Include a task command such as the `runtests` command.

> See [Task Commands](taskCommands.html) for a description of the available commands.

## Detailed Example of `testsuite` Command

In the following example, *mytestsuite*, *myworkdir*`.wd`, and *myconfig*`.jti` represent file names
that might exist on your system.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) `-testsuite` *mytestsuite* `-workdir` *myworkdir*`.wd -config`
*myconfig*`.jti -runtests`

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
