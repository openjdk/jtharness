---
hIndent: 1
title: Monitoring a Test Run
---

[]{#monitoring}

# Monitoring a Test Run

After the test run begins, the Test Manager displays information about the test run in the areas
described in the following table.

+-------------------------------------------------+-------------------------------------------------+
| Area                                            | Description                                     |
+=================================================+=================================================+
| Test Tree                                       | The test tree uses colored icons to display the |
|                                                 | current run and test results status of the      |
|                                                 | folders and tests in the work directory. As the |
|                                                 | harness completes running individual tests, it  |
|                                                 | updates each test tree icon to indicate the     |
|                                                 | test result status.                             |
|                                                 |                                                 |
|                                                 | See [Monitoring With a Test                     |
|                                                 | Tree](testTree.html) for detailed information   |
|                                                 | about using the test tree to monitor the        |
|                                                 | progress of the test run.                       |
+-------------------------------------------------+-------------------------------------------------+
| Status Messages                                 | Below the test tree area is a resizable text    |
|                                                 | area that displays information about current    |
|                                                 | Test Manager activity. In this area the harness |
|                                                 | displays status messages about the state of the |
|                                                 | test run and the name of the test being run.    |
+-------------------------------------------------+-------------------------------------------------+
| Progress Indicator                              | The Test Manager Status Line contains a         |
|                                                 | progress indicator. The indicator displays the  |
|                                                 | elapsed time of the previous test run when      |
|                                                 | tests are not running. When tests are running,  |
|                                                 | it automatically changes to the progress bar of |
|                                                 | the current test run. At the completion of the  |
|                                                 | test run, the indicator changes to display the  |
|                                                 | elapsed time.                                   |
|                                                 |                                                 |
|                                                 | See [Progress Indicator](progressMeter.html)    |
|                                                 | for information about using the test progress   |
|                                                 | meter to monitor the progress of the test run.  |
+-------------------------------------------------+-------------------------------------------------+
| Progress Monitor                                | A separate Progress Monitor is available that   |
|                                                 | displays current, detailed information about    |
|                                                 | the progress of the test run.                   |
|                                                 |                                                 |
|                                                 | See [Progress Monitor](progressMonitor.html)    |
|                                                 | for information about using the Progress        |
|                                                 | Monitor.                                        |
+-------------------------------------------------+-------------------------------------------------+
| Information Area                                | As tests run, the harness displays information  |
|                                                 | about the run in the information area to the    |
|                                                 | right of the test tree. The information area    |
|                                                 | provides two views:                             |
|                                                 |                                                 |
|                                                 | -   Folder view - When you click a folder icon  |
|                                                 |     in the test tree, the harness displays a    |
|                                                 |     Summary tab, a Documentation tab, five      |
|                                                 |     status tabs, and a status field containing  |
|                                                 |     information from the work directory about a |
|                                                 |     folder and its descendants. See [Displaying |
|                                                 |     Folder                                      |
|                                                 |     Information](../browse/folderInfo.html) for |
|                                                 |     detailed information about browsing folder  |
|                                                 |     information.                                |
|                                                 |                                                 |
|                                                 | -   Test view - When you click a test icon in   |
|                                                 |     the test tree or double click its name in   |
|                                                 |     the Folder view, the harness displays six   |
|                                                 |     tabbed panes that contain detailed          |
|                                                 |     information about the test. See [Displaying |
|                                                 |     Test Information](../browse/testInfo.html)  |
|                                                 |     for detailed information about browsing     |
|                                                 |     test information.                           |
+-------------------------------------------------+-------------------------------------------------+

While monitoring the test run in the Test manager, you can also display the contents of output logs
being generated during the test run. See [Monitoring Output Logs](logViewer.html) and [Monitoring
Services](svcViewer.html) for detailed information.

The harness also provides a web server that you can use to remotely monitor and control batch mode
test runs. The HTTP Server provides two types of output:

-   HTML Formatted Output allowing users to remotely monitor batch mode test runs in a web browser

-   Plain Text Output intended for use by automated testing frameworks

See the *Command-Line Interface User\'s Guide* for details about running tests from the command line
and using the web server.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
