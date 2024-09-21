---
hIndent: 1
title: Monitoring Test Progress With verbose
---

[]{#runTests}

# Monitoring Test Progress With `verbose`

Including the `verbose` command and optional monitoring options in a run command allows the user to
monitor test progress from the command line. This command uses `stdout` to display the specified
levels of monitoring test run progress. This monitoring function is not available in the GUI.If you
use the `verbose` command and options, set it as the first flag in the command line. Because it
takes effect at the point in the command line where it appears, if the `verbose` command does not
preceed the other commands, commands executed before it appears on the command line are not be
shown.

## Monitoring Options

The monitoring options are specified in the command line as a comma-separated list following the
`-verbose` option. A colon (:) is used to separate the `-verbose` command from the options. Ordering
and capitalization within the list are ignored. Whitespace within the list is prohibited.

If you do not specify a level, the `progress` option is automatically used.

[*\> jtharness*](aboutExamples.html) `-verbose:` *monitor-option* \[*setup-commands*\] \...
`-runtests` \...

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness* in the example.

See [Examples of Monitoring Output](#examples) for detailed examples of the command line.

The following table describes monitoring options specified in the command line.

  Option       Description
  ------------ ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  `commands`   Traces the individual harness commands as they are executed. If this option is used, it should be set first in the command line. Traced harness commands include options given on the command line, commands given in command strings, and commands given in command files.
  `no-date`    Does not prefix entries with the data and time stamp. Normally, each logical line of output prints the month, day, hour, minute and second.
  `non-pass`   Prints non-passing (error, fail) test names and their status string. The status string includes the status (error, fail) and the reason for the failure or error.
  `max`        Outputs the maximum possible amount of output. This includes all the options which are individually available. If this option is present, only the `no-date` and `quiet` flags have any additional effect.
  `quiet`      Suppresses any output from the verbose system. It might be useful to temporarily deactivate monitoring while debugging, without removing other levels coded into a script. `-verbose:stop,progress,quiet` results in no output, as does `-verbose:quiet,stop,progress`. This option takes precedence over all other options. It does not suppress the pass, fail, and error statistics printed at the end of the test run.
  `start`      Prints the test name when it goes into the harness\' engine for execution. Note: On some test suites, this might only indicate that the test has been handed to the plug-in framework, not that it is actually executing.
  `stop`       Prints the test name and status string (see `non-pass`) when a test result is received by the harness.
  `progress`   Prints a progress summary, which indicates pass, fail, error, and not-run numbers. If any of the `max, non-pass, stop,` or `stop` options were specified, each summary migh be printed on its own line. If not, the summary will be updated on the current line. The progress information is printed/updated each time a test result is reported to the harness.

[]{#examples}

## Detailed Examples of Monitoring Commands

The following are seven examples of monitoring commands and their resulting command line output:

-   An example of the default monitoring output:

> > java -jar lib/javatest.jar -verbose -open foo.jti -runtests
>       14:21:31 Sept 14 - Harness starting test run with configuration "foo".
>       14:24:33 Sept 14 - Pass: 12  Fail: 0  Error: 1  Not-Run: 33
>       14:24:30 Sept 14 - Finished executing all tests, wait for cleanup...
>       14:26:31 Sept 14 - Harness finished test run.
>         

-   An example of the `start` monitoring output:

> > java -jar lib/javatest.jar -verbose:start -open foo.jti -runtests
>       14:21:31 Sept 14 - Harness starting test run with configuration "foo".
>       14:24:39 Sept 14 - Running foo/bar/index#id1
>       14:24:30 Sept 14 - Test run stopped, due to failures, errors, user request. Wait for cleanup...
>       14:26:31 Sept 14 - Harness finished test run.
>         

-   An example of the `start` and `stop` monitoring output:

> > java -jar lib/javatest.jar -verbose:start,stop -open foo.jti -runtests
>       14:21:31 Sept 14 - Harness starting test run with configuration "foo".
>       14:24:31 Sept 14 - Running foo/bar/index#id1
>       14:24:32 Sept 14 - Finished foo/bar/index#id1 Fail.  Invalid index did not throw exception.
>       14:26:33 Sept 14 - Running foo/bar/index#id2
>       14:27:34 Sept 14 - Finished foo/bar/index#id2 Pass.
>       14:28:35 Sept 14 - Running foo/bar/index#id3
>       14:29:36 Sept 14 - Finished foo/bar/index#id3 Error.  Cannot invoke JVM.
>       14:30:30 Sept 14 - Finished executing all tests, wait for cleanup...
>       14:30:31 Sept 14 - Harness finished test run.
>         

-   An example of the `no-date`, `start`, and `stop` monitoring output:

> > java -jar lib/javatest.jar -verbose:no-date,start,stop -open foo.jti -runtests
>       Harness starting test run with configuration "foo".
>       Running foo/bar/index#id1
>       Finished foo/bar/index#id1 Fail.  Invalid index did not throw exception.
>       Running foo/bar/index#id2
>       Finished foo/bar/index#id2 Pass.
>       Running foo/bar/index#id3
>       Finished foo/bar/index#id3 Error.  Cannot invoke JVM.
>       Test run stopped, due to failures, errors, user request. Wait for cleanup...
>       Harness finished test run.
>         

-   An example of the `non-pass` monitoring output:

> > java -jar lib/javatest.jar -verbose:non-pass -open foo.jti -runtests
>       Harness starting test run with configuration "foo".
>       Running foo/bar/index#id1
>       Finished foo/bar/index#id1 Fail.  Invalid index did not throw exception.
>       Running foo/bar/index#id2
>       Finished foo/bar/index#id2 Pass.
>       Test run stopped, due to failures, errors, user request. Wait for cleanup...
>       Harness finished test run.
>         

-   An example of the `progress` and `non-pass` monitoring output:

> > java -jar lib/javatest.jar -verbose:progress,non-pass -open foo.jti -runtests
>       14:23:39 Sept 14 - Harness starting test run with configuration "foo".
>       14:24:39 Sept 14 - Pass: 12  Fail: 0  Error: 0  Not-Run: 33
>       14:25:32 Sept 14 - Finished foo/bar/index#id1 Fail.  Invalid index did not throw exception.
>       14:26:39 Sept 14 - Pass: 12  Fail: 1  Error: 0  Not-Run: 32
>       14:27:39 Sept 14 - Pass: 12  Fail: 1  Error: 0  Not-Run: 32
>       14:30:36 Sept 14 - Finished foo/bar/index#id3 Error.  Cannot invoke JVM.
>       14:32:39 Sept 14 - Pass: 12  Fail: 1  Error: 1  Not-Run: 31
>       14:33:01 Sept 14 - Test run stopped, due to failures, errors, user request. Wait for cleanup...
>       14:33:10 Sept 14 - Harness finished test run.
>         

-   An example of the `no-date` and `max` monitoring output:

> > java -jar lib/javatest.jar -verbose:no-date,max -open foo.jti -runtests
>       Harness starting test run with configuration "foo".
>       Running foo/bar/index#id1
>       Finished foo/bar/index#id1 Fail.  Invalid index did not throw exception.
>       Pass: 0  Fail: 1  Error: 0  Not-Run: 33
>       Running foo/bar/index#id2
>       Finished foo/bar/index#id2 Pass.
>       Pass: 1  Fail: 1  Error: 0  Not-Run: 32
>       Test run stopped, due to failures, errors, user request. Wait for cleanup...
>       Harness finished test run.
>         

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
