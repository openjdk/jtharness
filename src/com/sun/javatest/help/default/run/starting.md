---
hIndent: 1
title: Starting a Test Run
---

[]{#starting}

# Starting a Test Run

When the harness is not running tests, it enables both the ![Start Test Run button displayed on the
tool bar](../../images/runTests_button.gif){longdesc="starting.html"}   button on the tool bar and
the Run Tests ` > ` Start menu item.

![The following text is a note](../../images/hg_note.gif){longdesc="starting.html"}\
Only one test run at a time can be active in each Test Manager.

To start a test run using the current configuration, either click the ![Start Test Run button
displayed on the tool bar](../../images/runTests_button.gif){longdesc="starting.html"}   button or
choose Run Tests `>` Start. You can also use the test tree pop-up menu to run a specific test or
group of tests in a folder. See [Test Tree Pop-Up Menu](../ui/popupmenu.html).

Before the harness attempts to run the test suite, it verifies that the required configuration
information is complete. You can view the configuration state in the Test Manager Properties
browser. See [Viewing Test Manager Properties](../execProps/dialog.html) for a description of the
browser.

If the configuration information is not provided or is incomplete, the harness opens a dialog box
advising you that the configuration must be completed before it can begin running tests. You can
choose to open the configuration editor window or cancel the test run. If you choose to open the
configuration, the harness opens the configuration at the incomplete section.

To change the test suite or work directory before running tests, refer to the following topics:

-   [Opening a Test Suite](../start/openTestSuite.html)
-   [Opening a Work Directory](../start/openDirectory.html)
-   [Creating a Work Directory](../start/createDirectory.html)

If a JT Harness agent is used to run the tests for your product, you must start the agent before you
begin the test run. See *JT Harness Agent User\'s Guide* for detailed information about the JT
Harness agent.

If the harness starts the test run and issues a request before the active agent starts running, the
harness waits for an available agent until the timeout period ends. If the timeout period ends
before an agent is available, the harness reports an error for the test.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2009, Oracle and/or its affiliates. All rights reserved.
