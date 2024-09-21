---
hIndent: 2
title: Test Run Messages Pane
---

[]{#messagesTab}

# Test Run Messages Pane

To display detailed messages describing what happened during the running of each section of the
test, click the Test Run Messages tab. This information is useful when troubleshooting a test run.

![Test Run Messages tabbed
pane](../../images/JT4testRunMessagesTab.gif){longdesc="messagesTab.html"}

The Test Run Messages tab contains the following areas:

-   [**Message List**](#messagesTab.listPane) - The vertical area on the left side of the Test Run
    Messages tab.
-   [**Message Area**](#messagesTab.messagePane) - The vertical area on the right side of the Test
    Run Messages tab.
-   [**Test Result Status Bar**](#messagesTab.statusbar) - The horizontal area at the bottom of the
    Test Run Messages tab.

[]{#messagesTab.listPane}

## Message List

The message list provides a detailed list of messages issued during a test run. Click an item in the
list to display its contents in the message area. The message list contains links to the following
types of messages:

-   [Summary Message](#messagesTab.summary)
-   [Output Summary and Result Messages](#messagesTab.output)
-   [Test Result Message](#messagesTab.result)

[]{#messagesTab.summary}

### Summary Message

Only one per test, this message summarizes all of the messages generated during a test run and
provides hypertext links to their detailed contents. The Summary Message contains the following
information in the message area:

-   Test script used to run the test
-   Messages logged by the test script
-   Individual test result sections
-   Test result and its result icon

#### Script Messages

This message is passed up from the script that executed the test. There is only one script message
per test. Script messages vary for each test script. Refer to your test suite documentation for
detailed descriptions of its script messages when troubleshooting a test run.

[]{#messagesTab.output}

### Output Summary and Result Messages

Each test result section has an Output Summary and Result message that provides summary messages and
hypertext links to its detailed messages. The name of the Output Summary message is a function of
the test suite and varies for each test suite.

Some tests have only one result section, while others have multiple sections. Refer to your test
suite documentation for detailed descriptions of the tests when troubleshooting a test run.

The following table lists and describes the message types.

Message Type

Description

Output Summary

A two-column table listing the name and size of each output section. Each of the following output
sections contains text generated while executing the test section:

-   **messages** - Provides the command string used by the test script to run the test section.
    Unlike ref and log, the messages field always exists in a section.
-   **ref** - The name of this message field is determined by the test and might be a name other
    than ref. A test can use this output stream to provide standard output information from the test
    section.
-   **log** - The name of this message field is determined by the test and might be a name other
    than log. A test can use this output stream to provide standard error information from the test
    section. Many tests only use the log stream and include tracing as well as standard error
    information when writing to the log output.

The contents of each output section varies from test suite to test suite. Refer to your test suite
documentation for detailed descriptions of the test section messages when troubleshooting a test
run.

If no details exist in an output section, the harness does not create its hypertext link and
indicates in the Size (chars) column that it is empty.

 

Result

Contains a colored status icon and a brief description of the results of the specific test section.
The color of the circle indicates the result of the test section.

[]{#messagesTab.result}

### Test Result Message

The Test Result Message indicates the cumulative result of the test. There is only one Test Result
Message per test.

![The following text is a note](../../images/hg_note.gif){longdesc="messagesTab.html"}\
For negative tests, the Test Result correctly indicates Passed when all of its test sections have
failed.

[]{#messagesTab.messagePane}

## Message Area

The area displays the messages issued during a test run. The number, names, and content vary for
each test suite and may also vary for different tests in the same test suite.

[]{#messagesTab.statusbar}

## Test Result Status Bar

The area displays an abbreviated form of the Test Result Message. See [Test Result
Message](#messagesTab.result) for detailed information.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
