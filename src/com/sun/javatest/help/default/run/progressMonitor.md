---
hIndent: 3
title: Progress Monitor
---

[]{#progressMonitor}

# Progress Monitor

The Progress Monitor is a dialog box that only displays information about the current configuration
when the harness is running tests. The information displayed is equivalent to the Last Test Run view
filter set in the Test Manager. Changing view filters in the Test Manager does not change the
information displayed by the Progress Monitor.

Choose Run Tests \> Monitor Progress from the Test Manager menu bar or click the ![Progress Monitor
button](../../images/magnify1.gif){longdesc="progressMonitor.html"} icon at the bottom of the Test
Manager to open the Progress Monitor.

![The Progress Monitor dialog box](../../images/testMonitor.gif){border="0"
longdesc="progressMonitor.html"}

The following areas in the Progress Monitor display information about the test run:

-   [Progress](#progressMonitor.progress)
-   [Tests in Progress](#progressMonitor.test)
-   [Memory](#progressMonitor.memory)
-   [Time](#progressMonitor.time)

[]{#progressMonitor.progress}

## Progress

The following table describes the information displayed in the Progress area.

+-------------------------------------------------+-------------------------------------------------+
| Name                                            | Description                                     |
+=================================================+=================================================+
| Passed                                          | Displays the number of tests in the test suite  |
|                                                 | that were run and had passing results.          |
+-------------------------------------------------+-------------------------------------------------+
| Failed                                          | Displays the number of tests in the test suite  |
|                                                 | that were run and had failing results.          |
+-------------------------------------------------+-------------------------------------------------+
| Errors                                          | Displays the number of tests in the test suite  |
|                                                 | that could not be run.                          |
+-------------------------------------------------+-------------------------------------------------+
| Not Run                                         | Displays the number of tests in the test suite  |
|                                                 | have not yet been run.                          |
+-------------------------------------------------+-------------------------------------------------+
| Test Results                                    | A colored progress bar representing the results |
|                                                 | of the tests in the test suite.                 |
|                                                 |                                                 |
|                                                 | As the harness runs tests, the test results are |
|                                                 | displayed as colored segments in the progress   |
|                                                 | bar. The colors used in the progress bar        |
|                                                 | represent the current status of the test        |
|                                                 | results.                                        |
|                                                 |                                                 |
|                                                 | The colors below are the harness default        |
|                                                 | settings. The harness supports using colors     |
|                                                 | other than the default settings. See            |
|                                                 | [Specifying Status                              |
|                                                 | Colors](../command/settingColors.html) in the   |
|                                                 | *JT Harness User\'s Guide: Command-Line         |
|                                                 | Interface*.                                     |
|                                                 |                                                 |
|                                                 | The progress bar is the same as that displayed  |
|                                                 | in the test progress display at the bottom of   |
|                                                 | the Test Manager.                               |
+-------------------------------------------------+-------------------------------------------------+

The following table describes the colors used in the progress bar. The colors are displayed from
left to right in the order in which they are presented in the table.

+--------------------------------+--------------------------------+--------------------------------+
| Color                          | Status                         | Description                    |
+================================+================================+================================+
| Green                          | Passed                         | Tests in the test run having   |
|                                |                                | passing results when they were |
|                                |                                | executed.                      |
+--------------------------------+--------------------------------+--------------------------------+
| Red                            | Failed                         | Tests in the test run having   |
|                                |                                | failed results when they were  |
|                                |                                | executed.                      |
+--------------------------------+--------------------------------+--------------------------------+
| Blue                           | Error                          | Tests in the test run that the |
|                                |                                | harness could not execute.     |
|                                |                                |                                |
|                                |                                | Errors usually occur because   |
|                                |                                | the test environment is not    |
|                                |                                | properly configured.           |
+--------------------------------+--------------------------------+--------------------------------+
| White                          | Not yet run                    | Tests in the test run that the |
|                                |                                | harness has not yet executed.  |
|                                |                                | Tests excluded from the test   |
|                                |                                | run are not included.          |
+--------------------------------+--------------------------------+--------------------------------+

You can display the progress bar in the Test Manager by clicking the ![Test Progress display
drop-down list](../../images/drop-down.gif){longdesc="progressMonitor.html"} button and choosing Run
Progress Meter from the selectable list. See [Using the Progress Meter](progressMeter.html) for
detailed description.

[]{#progressMonitor.test}

## Tests in Progress

The Tests in Progress text box displays either the names of the tests that the harness is currently
running or the set of tests distributed for execution. It is empty when the harness is not running
tests. Concurrency settings and agents in use determine the number of items displayed in the text
box. See your test suite documentation for additional information.

Clicking on this list displays the appropriate test view in the Test Manager.

[]{#progressMonitor.memory}

## Memory

The Memory area contains two text fields and a bar graph. The following table describes the contents
of the Memory area in the Progress Monitor.

Name

Description

Used:

The memory used to run the test.

Total:

The total memory available for use by the virtual machine.

[]{#progressMonitor.time}

## Time

The Time area contains two fields that are continuously updated throughout a test run. The following
table describes the contents of the Time area in the Progress Monitor.

  Name         Description
  ------------ ---------------------------------------------------------
  Elapsed:     The time elapsed since the test run was started.
  Remaining:   The estimated time required to run the remaining tests.

Tests that timeout or have execution times significantly longer than the other tests being run can
cause the harness to display inaccurate times.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
