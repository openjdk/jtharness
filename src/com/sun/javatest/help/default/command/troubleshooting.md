---
title: Troubleshooting
---

[]{#troubleshooting}

# Troubleshooting

The harness provides information in the following topics that you can use to troubleshoot problems:

-   [Exit Codes](#troubleshooting.exitcodes)
-   [Harness Fails During Use](#troubleshooting.starting)
-   [Problems Running Tests](#troubleshooting.running)
-   [Problems Writing Reports](#troubleshooting.writing)
-   [Problems Moving Reports](#troubleshooting.moving)
-   [Numeric Output Mangled (on Windows platforms)](#troubleshooting.codepage)

[]{#troubleshooting.exitcodes}

## Exit Codes

When the harness exits, it displays an exit code that you can use to determine the exit state. The
following table contains a detailed description of the exit codes.

   Exit Code  Description
  ----------- ---------------------------------------------------------
       0      If tests were executed, all tests had passed results.
       1      One or more tests were executed and had failed results.
       2      One or more tests were executed and had errors.
       3      A problem exists with the command-line arguments.
       4      Harness internal error exists.

[]{#troubleshooting.starting}

## Harness Fails During Use

If the harness fails, you can use the `harness.trace` file in your work directory to troubleshoot
the problem. The `harness.trace` file is a plain-text file that contains a log of harness activities
during the test run. It is written in the work directory, is incrementally updated, and is intended
primarily as a log of harness activity.

[]{#troubleshooting.running}

## Problems Running Tests

The goal of a test run is for all tests in the test suite that are not filtered out to have passing
results.

If the root test suite folder contains tests with errors or failing results, you must troubleshoot
and correct the cause to successfully complete the test run. See [Troubleshooting With the
GUI](../run/troubleshooting.html) in the *Graphical User Interface User\'s Guide* for information
about the resources that the harness provides for troubleshooting.

[]{#troubleshooting.errors}

### Tests With Errors

Tests with errors are tests that could not be executed by the harness. These errors usually occur
because the test environment is not properly configured. Use the GUI Test tabbed panes and
configuration editor window to help determine the change required in the configuration. See
[Troubleshooting With the GUI](../run/troubleshooting.html) in the *Graphical User Interface User\'s
Guide* for information about the resources that the harness provides for troubleshooting.

[]{#troubleshooting.failed}

### Tests That Fail

Tests that fail are tests that were executed but had failing results. The test or the implementation
may have errors.

Use the GUI Test Manager tabbed panes to identify and correct a test failure. See [Troubleshooting
With the GUI](../run/troubleshooting.html) in the *Graphical User Interface User\'s Guide* for
information about the resources that the harness provides for troubleshooting.

[]{#troubleshooting.writing}

## Problems Viewing Reports

The harness does not automatically generate reports of test results after a test run. You must
generate test reports either from the command line or from the GUI.

[]{#troubleshooting.writing}

## Problems Writing Reports

You use filters to write test reports for a specific set of test criteria. Verify that you are using
the appropriate filter to generate reports of test results. See [Creating
Reports](../report/newReports.html) in the *Graphical User Interface User\'s Guide*.

[]{#troubleshooting.moving}

## Problems Moving Reports

Test reports contain relative and fixed links to other files that may be broken when you move
reports to other directories.

You must update these links when moving reports to other directories. The harness provides an
[EditLinks](moveReports.html) utility that updates the links in the reports for you when moving
reports.

[]{#troubleshooting.codepage}

## Numeric Output Mangled (on Windows platforms)

Some users experience locale related problems when seeing numeric output on the console, for
example, under a Russian locale users have observed this output problem:\
`Pass: 298 Fail: o Error: 0 Not-Run: 50a947`\
which really should be (space for every thousand):\
`Pass: 298 Fail: o Error: 0 Not-Run: 50 947`\
And in a US locale would be:\
`Pass: 298 Fail: o Error: 0 Not-Run: 50,947`

The problem is rooted in the terminal\'s ability to correctly match and display the extended
characters which the JVM is producing based on the locale. On Windows, the provided MS-DOS box and
Terminal/Console program default to the CP866 (Cyrillic) codepage which has poor compatibility with
the Unicode output which the JVM is producing.

Users can workaround this problem by explicitly changing the codepage to one compatible with the
Unicode characters for the Cyrillic locale. For the Russian locale example:\
` chcp 1251`\
Or users can force the JVM to use the default codepage for the terminal:\
`java -Dfile.encoding=cp866 -jar ... `

These problems have not been reported on other operating systems, but could definitely occur
depending on the locale being used and the terminal\'s ability to display the character set. The
platform may also not have adequate character sets installed. Users may also observe this same
problem whenever internationalize output it produced by the JVM - a similar problem could easily
occur while printing dates or floating point numbers.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2013, Oracle and/or its affiliates. All rights reserved.
