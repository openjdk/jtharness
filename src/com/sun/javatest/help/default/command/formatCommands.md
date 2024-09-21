---
hIndent: 1
title: Formatting a Command
---

# []{#format}Formatting a Command

You can use any one of the following formats to include commands on the command line:

-   [Command Options Format](#commandsOptions)
-   [Single String Arguments Format](#singleString)
-   [Command File Format](#files)

All formats are used to accomplish the same tasks. Use the format that you prefer or that is easier
to use. See [Index of Available Commands](availableCommands.html) for a complete listing of
available commands.

## []{#commandsOptions}Command Options Format

In the command options format, commands are preceded by a dash (-), act as options, and do not use
command terminators. Enclose complex command arguments in quotes. Use this format when long lists of
commands are included in a command line.

Example:

[*\> jtharness*](aboutExamples.html) `-open` *default*`.jti -runtests`

## []{#singleString}Single String Arguments Format

If you are setting several command options, you might want to use the single string arguments
format. In the single string arguments format, one or more commands and their arguments can be
enclosed in quotes as a single string argument. Multiple commands and arguments in the string are
separated by semicolons.

Example:

[*\> jtharness*](aboutExamples.html) `"open` *default*`.jti; runtests"`

## []{#files}Command File Format

If you are setting a series of commands and options, you can use the command file format. Using a
command file enables you to easily reuse the same configuration.

In the command file format, a file containing a series of commands and their arguments is included
in the command line by preceding the file name with the at symbol (@).

Example:

[*\> jtharness*](aboutExamples.html) `@`*mycmd*`.jtb -runtests`

Refer to [Using Command Files](commandFile.html) for detailed information about using and creating
command files.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
