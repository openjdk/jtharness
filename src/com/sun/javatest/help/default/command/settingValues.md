---
hIndent: 2
title: Setting Specific Configuration Values
---

[]{#settingValues}

# Setting Specific Configuration Values

You can use the `set` command to override a specific value in a configuration file, import a Java
platform properties file (Java properties file) containing the values of multiple configuration
questions, and set the value of a Properties Question in a configuration file.

## Override a Specific Value

Use the `set` command to override a specific value in the current configuration (`.jti`) file.

[*\> jtharness*](aboutExamples.html) \... \[*initial-setup commands*\] \... `-set`
*question-tag-name* \... \[*task-command*\] \...

See [About the Command-Line Examples](aboutExamples.html) for a description of use of *\> jtharness*
in the example.

See [Command-Line Overview](commandLine.html) for a description of the command line structure.

## Import a Java Properties File

You can also use `-set -file` *input-file-name* or `-set -f` *input-file-name* to import a Java
properties file containing the values of multiple configuration questions. A hand-edited
configuration file can be used as an input file.

[*\> jtharness*](aboutExamples.html) \... \[*initial-setup-commands*\] \... `-set -file`
*input-file-name* \... \[*task-command*\] \...

The harness uses the values in the input file to override the values in the configuration. Any
values in the input file that are not used in the configuration are ignored.

Values changed by the `set`command are only used for the session and override but do not change the
configuration file. To change a configuration file, use the Configuration Editor provided by the
harness GUI.

## Set the Value of a Properties Question

In configuration (`.jti`) files that use the Properties Question type, you can use the `set`command
to override but not change the values of Properties Questions. This question type requires at least
three values for any setting (question key, property name, the new value).

[*\> jtharness*](aboutExamples.html) \... \[*initial-set-up-commands*\] \...
`-set `*question-tag-name* *property***:***value* \... \[*task-command*\] \...

The *question-tag-name* and *property* identify the location in the Properties Question to be
changed while *value* specifies the new value for that property. If the new value is rejected by the
question, the appropriate action is taken by the harness (exit with error). The error message will
specify that *`question-tag-name`* rejected the *value* for the *property*.

## Creating a Command String

When creating a command string to set specific values in a configuration, include the commands in
the following sequence:

1.  Include the commands required to set up a configuration.

> See [Setup Commands](setupCommands.html) for detailed description of the available commands.

2.  Include the command required to specify configuration values (`set` *question-tag-name*
    *value*).
3.  (Optional) Include the `runtests` command.

> See [Running Tests With `runtests`](runTests.html) for a detailed description of the command.

To use the `set`command, you must identify the *question-tag-name* associated with the *value* in
the configuration file that you are changing. In the command line, following the `set`command, enter
the *question-tag-name* and its new *value*:

A value can only be changed if its *tag-name* exists in the initialized configuration file. If the
configuration does not include the *tag-name* you must use the Configuration Editor in the harness
GUI to include the question and value in the configuration file.

See [Obtaining the Question tag-name](tagName.html) for detailed information about the *tag-name*
for the question. See [Formatting Configuration Values for editJTI or -set](configValues.html) for
detailed information about formatting the values. See [Detailed Examples](#settingValues.examples)
for examples of using the `set` command and the *tag-name*.

[]{#settingValues.examples}

## Detailed Example of Setting Test Suite Specific Values

In the following example, *myconfig*`.jti` represents a file name that might exist on your system.

**Command Options Example:**

[*\> jtharness*](aboutExamples.html) `-config` *myconfig*`.jti -set jckdate.gmtOffset 8 -runtests`

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2003, 2011, Oracle and/or its affiliates. All rights reserved.
