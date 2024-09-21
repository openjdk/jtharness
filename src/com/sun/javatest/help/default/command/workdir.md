---
hIndent: 2
title: Specifying a Work Directory (workdir or workdirectory)
---

[]{#workdir}

# Specifying a Work Directory With `workdir` or `workdirectory`

Each work directory is associated with a test suite and stores its test result files in a cache. You
can use the work directory command to:

-   [Use an Existing Work Directory](#workdir.open)
-   [Create a New Work Directory](#workdir.create)
-   [Replace an Existing Work Directory](#workdir.clearResults)

See [Shortcuts to Initialize a Configuration](shortcutInitializing.html) for information about
specifying a work directory in the command line.

[]{#workdir.open}

## Use an Existing Work Directory

To use an existing work directory for the test run, include either the `workdir` or `workdirectory`
command in the command line:

[*\> jtharness*](aboutExamples.html) \... `-workdir` *path/filename* \... \[*task-command*\] \...

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness* in the example.

See [Command-Line Overview](commandLine.html) for a description of the command line structure.

See [Formatting a Command](formatCommands.html) for descriptions and examples of the following
command formats.

[]{#workdir.create}

## Create a New Work Directory

To create a new work directory for the test run, use the `-create` command option:

[*\> jtharness*](aboutExamples.html) \... `-workdir -create` *path/filename*
\[*configuration-command*\] \... \[*task-command*\] \...

See [About the Command-Line Examples](aboutExamples.html) for a description of use of *\> jtharness*
in the example. See [Command-Line Overview](commandLine.html) for a description of the command line
structure.

The new work directory must not previously exist. You can also use an existing work directory as a
template to create a new work directory for the test run. To use an existing work directory as a
template, put the template in the command line before the `create` command.

When creating the command string, include the commands in the following sequence:

1.  (Optional) Include the commands required to specify the test suite.

> See [Specifying a Test Suite With `testSuite`](testsuite.html) for detailed information about the
> command.

2.  (Optional) Include the `-workdir` *path/filename* command required to specify an existing work
    directory.
3.  Include the `workdir` or `workdirectory -create` *path/filename* command.
4.  Include the commands required to specify a configuration file.

> See [Specifying a Configuration File With `config`](config.html) for detailed information about
> the command.

5.  (Optional) Include the commands required to set specific values.

> See [Setting Specific Values](otherConfigValues.html) for detailed information about the available
> commands.

6.  (Optional) Include the `runtests` command.

> The results of the test run are written to the new work directory. See [Running Tests With
> `runtests`](runTests.html) for detailed information about the command.

[]{#workdir.examples}

### Detailed Example of Creating a New Work Directory

In the following example, *myworkdir*`.wd` and *myconfig*`.jti` represent file names that might
exist on your system.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) `-workdir` *myworkdir*`.wd -create` *testrun*`.wd -config`
*myconfig*`.jti -runtests`

When the tests are run, the harness uses the work directory (*testrun*`.wd`) created by the command
line, even if the configuration file (*myconfig*`.jti`) was created using another work directory.

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

[]{#workdir.clearResults}

## Replace an Existing Work Directory

When you replace an existing work directory with a new work directory, the harness performs the
following tasks:

-   Deletes the existing work directory and its contents.
-   Creates the new work directory using the same name (if the old directory was successfully
    deleted).

To replace an existing work directory with a new work directory, use the `-overwrite` command
option.

[*\> jtharness*](aboutExamples.html) \... `-workdir -overwrite` *path/filename* \...
\[*task-command*\] \...

or

[*\> jtharness*](aboutExamples.html) \... `-workdir -create -overwrite` *path/filename* \...
\[*task-command*\] \...

The `-create` command option is optional when the `-overwrite` command is used.

See [About the Command-Line Examples](aboutExamples.html) for a description of use of *\> jtharness*
in the examples.

See [Command-Line Overview](commandLine.html) for a description of the command-line structure.

[]{#workdir.examples}

### Detailed Example of Replacing an Existing Work Directory

In the following example, *myconfig*`.jti` represents a configuration file name that might exist on
your system.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) `-workdir -overwrite` *testrun*`.wd -config`
*myconfig*`.jti -runtests`

The harness uses the work directory *testrun*`.wd` created by the command line when the tests are
run, even if *myconfig*`.jti` was created using another work directory.

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
