---
hIndent: 1
title: Troubleshoot a Test Run
---

[]{#run.troubleshooting}

# Troubleshooting a Test Run

Normally, the goal of a test run is for all tests in the test suite that are not filtered out by the
current configuration to have passing results. See [Changing Configuration
Values](../confEdit/editConfiguration.html) for a description of how the current configuration
filters tests in a test run.

If the root test suite folder contains tests with errors or failing results, you must troubleshoot
and correct the cause to satisfactorily complete the test run.

Tests with errors ![Tests with errors
icon](../../images/blueTest.gif){longdesc="../browse/testIcons.html"} are tests that could not be
executed by the JT harness. These errors usually occur because the test environment is not properly
configured or the software under test is defective. See [Tests with
Errors](../concepts/troubleshooting.html#troubleshooting.errors) for a detailed description of
troubleshooting tests with errors.

Tests that failed ![Tests that
failed](../../images/redTest.gif){longdesc="../browse/testIcons.html"} are tests that were executed
but had failing results. See [Tests that
Fail](../concepts/troubleshooting.html#troubleshooting.failed) for a detailed description of
troubleshooting tests that failed.

The Test Manager window provides you with the following facilities to effectively troubleshoot a
test run:

-   Test Tree
-   Folder View
-   Test View
-   Log Viewer
-   Service Monitor

## Test Tree

Use the test tree and view filters to identify specific folders and tests with errors or failing
results. Open the red ![Failed
folder](../../images/redFolder.gif){longdesc="../browse/folderIcons.html"} and blue ![Error
folder](../../images/blueFolder.gif){longdesc="../browse/folderIcons.html"}folders until the
specific tests that failed ![Tests that
failed](../../images/redTest.gif){longdesc="../browse/folderIcons.html"} or had errors ![Tests with
errors icon](../../images/blueTest.gif){longdesc="../browse/folderIcons.html"} are displayed.

## Folder View

When you click a folder icon in the test tree pane, the JT harness displays a filtered summary of
its test status in the Test Manager information area that matches the test tree.

![The following text is a note](../../images/hg_note.gif){longdesc="troubleshooting.html"}\
The View filter chosen in the Test Manager window might change the summary values displayed in the
folder view, but does not change the test results written in the work directory.

Click the Error and the Failed tabs to display the lists of all tests in and under a folder that
were not successfully run. You can double-click a test in the lists to view its detailed test
information. Refer to Test View below for a description of the test information that the JT harness
displays.

## Test View

When you click a test icon in the test tree or double-click its name in the folder view, the JT
harness displays unfiltered, detailed information about the test in the information area. The Test
Manager displays the current information for that test from the work directory.

![The following text is a note](../../images/hg_note.gif){longdesc="troubleshooting.html"}\
Because the Test Manager does not use a view filter when displaying test information, the test
status displayed in the information area may not match the filtered view in the test tree or the
folder Summary view.

The test view contains detailed test information panes and a brief status message at the bottom
identifying the type of result. This message may be sufficient for you to identify the cause of an
error or failure.

If you need additional information to identify the cause of the error or failure, use the following
panes listed in order of their importance:

-   [Test Run Messages](../browse/messagesTab.html) contains a Message list and a Message pane that
    display the messages produced during the test run
-   [Test Run Details](../browse/detailsTab.html) contains a two-column table of name-value pairs
    recorded when the test was run.
-   [Configuration](../browse/configurationTab.html) contains a two-column table of the name-value
    pairs derived from the configuration data that were actually used to run the test.

## Log Viewer

Use the Log Viewer (select View \> Logs) to monitor log files generated during a test run or to
inspect log files after a test run is completed. See [Monitoring Output Logs](../run/logViewer.html)
for a detailed description.

## Service Monitor

If a test suite uses services, use the Service Monitor (select View \> Services) to see the status
of services. See [Monitoring Services](../run/svcViewer.html) for a detailed description.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
