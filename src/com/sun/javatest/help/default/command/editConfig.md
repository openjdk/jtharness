---
hIndent: 1
title: Changing Configuration Values With EditJTI
---

[]{#editConfig}

# Changing Configuration Values With `EditJTI`

The harness provides the `EditJTI` utility for you to use in changing the values in a configuration
file from the command line. You can also make changes in a configuration by specifying the
appropriate `set` command (see [Command-Line Summary](commandLine.html) for detailed information).

While you can use `EditJTI` to change the order of commands in a configuration file, the
dependencies between questions can introduce errors into the configuration. Use the Configuration
Editor in the harness GUI when making major changes in a configuration.

If your changes to a configuration introduce errors, you can use the harness GUI Configuration
Editor to troubleshoot and repair the configuration.

## `EditJTI` Command Format

The `EditJTI` command loads a configuration (`.jti`) file, and applies a series of changes specified
on the command line. See [Formatting Configuration Values for editJTI or -set](configValues.html)
for detailed information about formatting the values.

You can save the changes in the original configuration file or save the changes in a new
configuration file. You can also use `EditJTI` to generate an HTML log of the questions and
responses as well as write a quick summary of the questions and responses to the console. The
`EditJTI` utility provides a preview mode. Configuration files are normally backed up before being
overwritten.

Example:\
`java -cp lib/javatest.jar com.sun.javatest.EditJTI` \[*OPTIONS*\] \[*EDIT-COMMANDS*\]
*original-configuration-file*

**OPTIONS**
:   The following are the available options:

    `-help`, `-usage` or `/?`
    :   Displays a summary of the command line options.

    `-classpath` *classpath* or `-cp` *classpath*
    :   Overrides the default classpath used to load the classes for the configuration interview.
        The default is determined from the work directory and test suite specified in the
        configuration file. The new location is specified by this option.

    `-log` *log-file* or `-l` *log-file*
    :   Generates an HTML log containing the questions and responses from the configuration file.
        The log is generated after edits are applied.

    `-out` *out-file* or `-o` *out-file*
    :   Specifies where to write the configuration file after the edits (if any) are applied. The
        default setting is to overwrite the input file if the interview is edited.

    `-path` or `-p`
    :   Generates a summary to the console output stream of the sequence of questions and responses
        from the configuration file. The summary is generated after edits are applied.

    `-preview` or `-n`
    :   Does not write out any files, but instead, preview what would happen if this option were not
        specified.

    `-testsuite` *test-suite* or `-ts` *test-suite*
    :   Overrides the default location used to load the classes for the configuration interview. The
        default is determined from the work directory and test suite specified in the configuration
        file. The new location is determined from the specified test suite.

    `-verbose` or `-v`
    :   Verbose mode. As the edit commands are executed, details of the changes are written to the
        console output stream.

**COMMANDS**

:   Two different types of commands are supported.

    *tag-name*`=`*value*
    :   Sets the value for the question whose tag is *tag-name* to *value*. It is an error if the
        question is not found. The question must be on the current path of questions being asked by
        the interview. To determine the current path, use the `-path`option. See [Obtaining the
        Question *tag-name*](tagName.html).

    `/`*search-string*`/`*replace-string*`/`
    :   Scans the path of questions being asked by the interview, looking for responses that match
        (contain) the search string. In such answers, replace *search-string* by *replace-string*.
        Note that changing the response to a question may change the subsequent questions that are
        asked. It is an error if no such questions are found.\
        If you use `/`in the search string, you use some other character (instead of `/`) as a
        delimiter.\
        For example:\
        `|`*search-string*`|`*replace-string*`|`\
        Regular expressions are not currently supported in *search-string*, but may be supported in
        a future release.

    Depending on the shell in use, quote the commands to protect characters in them from
    interpretation by the shell.

**RETURN CODE**
:   The following table describes the return codes generated when a program exits.
      Code   Description
      ------ ----------------------------------------------------------------------------------------------------------------------
      0      The operations were successful. the configuration file is complete and ready to use.
      1      The operations were successful, but the configuration file is incomplete and is not yet ready to use for a test run.
      2      A problem exists with the command-line arguments.
      3      An error occurred while trying to perform the copy.

**SYSTEM PROPERTIES**
:   Two system properties are recognized.

    `EditJTI.maxIndent`
    :   Used when generating the output for the `-path` option, this property specifies the maximum
        length of tag name after which the output will be line-wrapped before writing the
        corresponding value. The default value is 32.

    `EditJTI.numBackups`
    :   Specifies how many levels of backup to keep when overwriting a configuration file. The
        default is 2. A value of 0 disables backups.

The following topics provide detailed information about using `EditJTI` to perform tasks:

![The next possible topic in the \<code\>EditJTI\</code\>
sequence](../../images/hg_see_next.gif){width="9" height="13" longdesc="editFile.html"}[Changing
Configuration Values](editFile.html)

![The next possible topic in the \<code\>EditJTI\</code\>
sequence](../../images/hg_see_next.gif){width="9" height="13"
longdesc="generateLog.html"}[Generating a Log of Updates](generateLog.html)

![The next possible topic in the \<code\>EditJTI\</code\>
sequence](../../images/hg_see_next.gif){width="9" height="13" longdesc="preview.html"}[Preview
without Change](preview.html)

![The next possible topic in the \<code\>EditJTI\</code\>
sequence](../../images/hg_see_next.gif){width="9" height="13" longdesc="echoResults.html"}[Echo
Results of Changes](echoResults.html)

![The next possible topic in the \<code\>EditJTI\</code\>
sequence](../../images/hg_see_next.gif){width="9" height="13" longdesc="showPaths.html"}[Show Paths
for Debugging](showPaths.html)

![The next possible topic in the \<code\>EditJTI\</code\>
sequence](../../images/hg_see_next.gif){width="9" height="13" longdesc="change.html"}[Change Test
Suites or Create a New Interview](change.html)

![The next possible topic in the \<code\>EditJTI\</code\>
sequence](../../images/hg_see_next.gif){width="9" height="13" longdesc="changePort.html"}[Change the
HTTP Port](changePort.html)

![The next possible topic in the \<code\>EditJTI\</code\>
sequence](../../images/hg_see_next.gif){width="9" height="13" longdesc="escapeShell.html"}[Doing
Escapes in a UNIX Shell](escapeShell.html)

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
