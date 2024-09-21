---
hIndent: 1
title: Using Parameter Commands
---

[]{#parameterCommands}

# Using Deprecated Parameter Commands

If you are running a test suite that does not use a parameter file, use the [current
commands](availableCommands.html) instead of the `-params` command and option.

If you are running a test suite that uses a parameter file ( []{#parameterCommands.jtp} `.jtp`), you
can specify different parameter values by including `-params` and the appropriate parameter command
in the command line.

[*\> jtharness*](aboutExamples.html) \... `-params` \[*command*\] \[*value*\] \[*task-commands*\]
\...

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness*.

The following table describes the parameter commands.

+-------------------------------------------------+-------------------------------------------------+
| Command                                         | Description                                     |
+=================================================+=================================================+
| `-t` *testsuite*\                               | Specifies the test suite.                       |
| or\                                             |                                                 |
| `-testsuite` *testsuite*                        |                                                 |
+-------------------------------------------------+-------------------------------------------------+
| `-keywords` *keyword-expr*                      | Restricts the set of tests to be run based on   |
|                                                 | keywords associated with tests in the test      |
|                                                 | suite.                                          |
+-------------------------------------------------+-------------------------------------------------+
| `-status` *status-expr*                         | Includes or excludes tests from a test run      |
|                                                 | based on their status from a previous test run. |
|                                                 | Valid status expressions are error, failed, not |
|                                                 | run, and passed.                                |
+-------------------------------------------------+-------------------------------------------------+
| `-excludeList` *exclude-list-file*              | Specifies an exclude list file. Exclude list    |
|                                                 | files contain a list of tests that are not to   |
|                                                 | be run. Exclude list files conventionally use   |
|                                                 | the []{#parameterCommands.jtx} `.jtx` extension |
|                                                 | and are normally supplied with a test suite.    |
+-------------------------------------------------+-------------------------------------------------+
| `-envFile` *environment-file*                   | Specifies an environment file that contains     |
|                                                 | information used by the harness to run tests in |
|                                                 | your computing environment. You can specify an  |
|                                                 | environment file for the harness to use when    |
|                                                 | running tests.                                  |
+-------------------------------------------------+-------------------------------------------------+
| `-env` *environment*                            | Specifies a test environment from an            |
|                                                 | environment file.                               |
+-------------------------------------------------+-------------------------------------------------+
| `-concurrency` *number*                         | Specifies the number of tests run concurrently. |
|                                                 | If you are running the tests on a               |
|                                                 | multi-processor computer, concurrency can speed |
|                                                 | your test runs.                                 |
+-------------------------------------------------+-------------------------------------------------+
| `-timeoutFactor` *number*                       | Increases the timeout limit by specifying a     |
|                                                 | value in the time factor option.                |
|                                                 |                                                 |
|                                                 | The timeout limit is the amount of time that    |
|                                                 | the harness waits for a test to complete before |
|                                                 | moving on to the next test. Each test\'s        |
|                                                 | timeout limit is multiplied by the time factor  |
|                                                 | value.                                          |
|                                                 |                                                 |
|                                                 | For example, if you specify a value of 2, the   |
|                                                 | timeout limit for tests with a 10 basic time    |
|                                                 | limit becomes 20 minutes.                       |
+-------------------------------------------------+-------------------------------------------------+
| `-r` *report-directory*\                        | Specifies the directory where the harness       |
| or\                                             | writes test report files. If this path is not   |
| `-report` *report-directory*                    | specified, the reports are written to a         |
|                                                 | directory named report in the directory from    |
|                                                 | which you started the harness.                  |
+-------------------------------------------------+-------------------------------------------------+
| `-w` *work-directory*\                          | Specifies a work directory for the test run.    |
| or\                                             | Each work directory is associated with a test   |
| `-workDir` *workDirectory*                      | suite and stores its test result files in a     |
|                                                 | cache.                                          |
+-------------------------------------------------+-------------------------------------------------+

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
