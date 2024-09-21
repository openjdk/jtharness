---
hIndent: 2
title: Specifying a Configuration File With config
---

[]{#config}

# Specifying a Configuration File With `config`

To specify the configuration file the harness uses to run tests, use the `config` command.

[*\> jtharness*](aboutExamples.html) \... `-config` *path/filename* \... \[*task-command*\] \...

See [About the Command-Line Examples](aboutExamples.html) for a description of *\> jtharness* in the
example.

The configuration file might contain default values for the test suite (which contains the tests to
be run) and the work directory (where the results are placed).

Test suite and work directory values in the configuration file can be overridden with the
`testsuite` and `workdirectory` commands. If the configuration file is a template and does not
contain default values for the test suite and work directory, those values must be specified
explicitly with the testsuite and workdirectory commands.

See [Command-Line Overview](commandLine.html) for a description of the command line structure.

## Detailed Example of `config` Command

In the following example, *myconfig*`.jti` represents a configuration file name that might exist on
your system.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) `-config` *myconfig*`.jti -runtests`

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
