---
hIndent: 2
title: Creating a Command File
---

# Creating a Command File

Use the single string arguments format style to write commands in a text file. See [Formatting a
Command](formatCommands.html) for detailed information.

Command files can contain blank lines and comments as well as lines with commands and their
arguments. The following table describes the contents of a command file.

+-------------------------------------------------+-------------------------------------------------+
| File Contents                                   | Description                                     |
+=================================================+=================================================+
| Comments                                        | Comments can begin anywhere on a line, are      |
|                                                 | started by the pound symbol ( #), and stop at   |
|                                                 | the end of the line.                            |
|                                                 |                                                 |
|                                                 | Example:\                                       |
|                                                 | `#File contains commands`                       |
+-------------------------------------------------+-------------------------------------------------+
| Commands                                        | Commands are executed in the sequence that they |
|                                                 | appear in the file (for example, setup commands |
|                                                 | must precede task commands). Commands used in   |
|                                                 | the file must be separated by a semicolon (;)   |
|                                                 | or a new line symbol (#). The \# symbol acts as |
|                                                 | a new line character and can terminate a        |
|                                                 | command.                                        |
|                                                 |                                                 |
|                                                 | Examples:\                                      |
|                                                 | `open` *default*`.jti; #opens file`\            |
|                                                 | `-set host` *mymachine*                         |
+-------------------------------------------------+-------------------------------------------------+
| Command Arguments                               | Arguments that contain white space must be      |
|                                                 | placed inside quotes. Use a backslash (\\) to   |
|                                                 | escape special characters such as quotes (\"    |
|                                                 | \") and backslashes (\\).                       |
+-------------------------------------------------+-------------------------------------------------+

After writing the commands, use a descriptive name and the extension `.jtb` to save the text file.
Choose a file name that helps you identify the function of each command file.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
