---
title: Troubleshooting
---

[]{#troubleshooting}

# Troubleshooting With the GUI

The harness provides information you can use in troubleshooting problems. To troubleshoot problems
using the harness see the following topics:

-   [Harness Fails During Use](#troubleshooting.starting)
-   [Problems Running Tests](#troubleshooting.running)
-   [Problems Viewing Test Results](#troubleshooting.viewing)
-   [Output Overflow Message Displayed in Test Run Messages](#output.overflow)
-   [Problems Viewing Reports](#troubleshooting.viewingReport)
-   [Problems Writing Reports](#troubleshooting.writing)
-   [Problems Moving Reports](#troubleshooting.moving)

[]{#troubleshooting.starting}

## Harness Fails During Use

If the harness fails, you can use the `harness.trace` file in your work directory to help
troubleshoot the problem. The `harness.trace` file is a plain-text file that contains a log of
harness activities during the test run. It is written in the work directory, is incrementally
updated, and is intended primarily as a log of harness activity.

[]{#troubleshooting.running}

## Problems Running Tests

The goal of a test run is for all tests in the test suite that are not filtered out to have passing
results.

If the root test suite folder contains tests with errors or failing results, you must troubleshoot
and correct the cause to satisfactorily complete the test run. See [Troubleshooting a Test
Run](../run/troubleshooting.html) for information about the resources that the harness provides for
troubleshooting.

[]{#troubleshooting.errors}

### ![Tests with errors](../../images/blueTest.gif){longdesc="troubleshooting.html"}Tests with Errors

Tests with errors are tests that could not be executed by the harness. These errors usually occur
because the test environment is not properly configured. Use the Test tabbed panes and the
Configuration Editor to help determine the change required in the configuration.

The following is an example of how the Test Manager tabbed panes and the Configuration Editor can be
used to identify and correct a configuration error:

1.  Use the test tree to identify the folder containing test that had errors.

2.  Click the folder icon to open its Summary tab in the Test Manager window.

3.  Click the Error tab to display the list of tests in the folder that had errors.

4.  Double-click a test in the list to display it in the test tree and view its detailed test
    information.

5.  Click the Test Run Messages tab to display detailed messages describing what happened during the
    running of each section of the test.

    The contents of each output section vary from test suite to test suite. Refer to your test suite
    documentation for detailed descriptions of the test section messages when troubleshooting a test
    run.

<!-- -->

6.  Click the Configuration tab to display a two-column table of the name-value pairs that were
    derived from the configuration file and actually used to run the test.

    The names in the table identify test environment properties used by the harness to run the test.
    The values displayed were used to run the test. Refer to your test suite documentation for
    detailed descriptions of the name-value pairs for your test.

<!-- -->

7.  Choose View \> Configuration **\>** Show Question Log to view the Question Log of the current,
    saved configuration.

    Use the question log to identify the configuration value that is incorrect and its configuration
    question.

<!-- -->

8.  Search the configuration file for the specific characters or character strings that must be
    changed.

    See [Searching a Configuration](../confEdit/searchConfiguration.html) for a detailed description
    of how the current configuration can be searched for a character or string of characters.

<!-- -->

9.  Click the Done button to save your changes to the configuration file

10. Rerun the tests.

[]{#troubleshooting.failed}

### ![Tests that fail](../../images/redTest.gif){longdesc="troubleshooting.html"}Tests that Fail

Tests that fail are tests that were executed but had failing results. The test or the implementation
might have errors.

The following is an example of how the Test Manager tabbed panes can be used to identify and correct
a test failure:

1.  Use the test tree to identify the folder containing test that had errors.
2.  Click the folder icon to open its Summary tab in the Test Manager window.
3.  Click the Error tab to display the list of tests in the folder that had errors.
4.  Double-click a test in the list to display it in the test tree and view its detailed test
    information.
5.  Click the Test Run Messages tab to display detailed messages describing what happened during the
    running of each section of the test.

> The contents of each output section vary from test suite to test suite. Refer to your test suite
> documentation for detailed descriptions of the test section messages when troubleshooting a test
> run.

[]{#troubleshooting.viewing}

## Problems Viewing Test Results

Most problems in viewing test results in the Test Manager result from the use of view filters (other
than the All Tests view filter) with a current configuration set to run specific tests (such as,
running tests based on their prior status). When a view filter other than All Tests is used in the
Test Manager, only the fields of the filtered category appear to be updated in the Test Manager
Summary tab. This is normal behavior of the GUI when view filters are used.

The harness *only* displays tests in the GUI that match the specified view filter criteria. All
other tests are displayed as Filtered Out.

As test results change, the harness moves the tests to the Filtered Out category (not to a test
result category) in the Test Manager and turns the appropriate node in the test tree gray.
Consequently, the fields for the other categories in the Test Manager Summary tab might not appear
to be updated.

Example:\
If you change the current configuration to run only tests with a prior status of Failed and use the
Current Configuration view filter, the Test Manager displays all tests that are not a Failed status
as Filtered Out. This enables you to monitor only those tests with a previous Failed status. When
you begin the test run, any tests with Passed or Error results are displayed in the Filtered Out
category of the Test Manager. To view the actual test results, change to a different view filter.
See [View Filters](../browse/viewFilters.html) for additional information. of the view filters.

[]{#output.overflow}

## Output Overflow Message Displayed in Test Run Messages

An output stream in the Test Run Messages Pane might display the following message:

` Output overflow:the harness has limited the test output of`\
`the text to that at the beginning and the end, so that you`\
`can see how the test began, and how it completed.`

` If you need to see more of the output from the test,`\
`set the system property javatest.maxOutputSize to a higher`\
`value. The current value is 100000`

`Set the system property of javatest.maxOutputSize`\
`by using the syntax in:`\
\
`java -Djavatest.maxOutputSize=200000 -jar lib/javatest.jar ....`\

If you see this message, restart the harness with the harness VM system property
`javatest.maxOutputSize` value set to a greater number and rerun the test to ensure that all of the
data is saved.

The value of `javatest.maxOutputSize` is an integer subject to the maximum integer size of the VM.
It sets the number of characters allowed in the output.

[]{#troubleshooting.viewingReport}

## Problems Viewing Reports

The harness does not automatically generate reports of test results after a test run. You must
generate test reports either from the command line or from the harness GUI. See [Creating
Reports](../report/newReports.html) for detailed information.

[]{#troubleshooting.writing}

## Problems Writing Reports

Filters are used to write test reports for a specific set of test criteria. Verify that you are
using the appropriate filter to generate reports of test results. See [Creating
Reports](../report/newReports.html) for detailed information.

[]{#troubleshooting.moving}

## Problems Moving Reports

Test reports contain relative and fixed links to other files that might be broken when you move
reports to other directories.

You must update these links when moving reports to other directories. The harness provides an
`EditLinks` utility that updates the links in the reports for you when moving reports. See the
*Command-Line Interface User\'s Guide*.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
