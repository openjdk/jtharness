---
hIndent: 0
title: Quick Start
---

[]{#quickStart}

# Quick Start

The harness includes a Quick Start wizard that enables users to quickly create combinations of test
suite, work directory, and configuration files and open them in properly configured Test Manager
windows. The harness opens the wizard when one of the following conditions occur:

-   The harness cannot restore an existing desktop.
-   A user includes a command-line option in the command to start the GUI.
-   A user opens a new Test Manager window.

In each case, the wizard enables the user to specify one of the following tasks:

-   [Start a New Test Run.](#startRun)
-   [Resume Work on a Test Run.](#resumeRun)
-   [Browse the Contents of a Test Suite.](#browse)

Because each of these tasks has a different set of test suite, work directory, and configuration
file requirements, the wizard presents the user with a set of questions that collect the information
required for that specified task. When the wizard has collected the required information, it opens a
new Test Manager window that is properly set up to perform the specified task.

[]{#startRun}

## Starting a New Test Run

The harness requires that you have a test suite, a work directory, and a valid configuration loaded
in the Test Manager before running tests. To properly set up the Test Manager, when you choose Start
a New Test Run, the wizard displays a series of questions that prompt you to choose the type of
configuration used (load a template or create a new configuration), specify a test suite, and choose
a work directory. See the following topics for additional information about templates, test suites
and work directories:

-   [Working With Templates](../templates/usingTemplate.html)
-   [Opening a Test Suite](openTestSuite.html)
-   [Opening a Work Directory](openDirectory.html)
-   [Creating a Work Directory](createDirectory.html)

In the last panel, the wizard displays two options: automatically open the Configuration Editor and
automatically begin the test run.

If you choose not to automatically open the Configuration Editor when the wizard closes, the harness
displays the Test Manager. In either case, before the harness begins to run tests, it verifies that
the configuration is complete. If configuration values are missing or invalid, the harness displays
a dialog box describing the problem before opening the Configuration Editor.

See the following topics for additional information about working with configurations:

-   [Creating a Configuration](../confEdit/createConfiguration.html)
-   [Editing a Configuration](../confEdit/editConfiguration.html)

[]{#resumeRun}

## Resuming Work on a Test Run

If you choose to resume work on a test run, the wizard only prompts you for the work directory.
Because each work directory is mapped to a test suite and a configuration file, you do not provide
any additional information to the harness. See [Opening a Work Directory](openDirectory.html) for
additional information about using an existing work directory.

In the last panel, the wizard displays two options: automatically open the Configuration Editor and
automatically begin the test run.

If you choose not to automatically open the Configuration Editor when the wizard closes, the harness
displays the Test Manager. In either case, before the harness begins to run tests, it verifies that
the configuration is complete. If configuration values are missing or invalid, the harness displays
a dialog box describing the problem before opening the Configuration Editor.

[]{#browse}

## Browsing the Contents of a Test Suite

If you want to view the tests in a test suite without running them, the wizard only requires that
you specify the test suite. While the wizard provides you with the option of creating a
configuration file for the test suite, this is not required. If you choose not to create a
configuration file for the test suite, the harness closes the wizard and opens a Test Manager window
containing the test suite.

See [Opening a Test Suite](openTestSuite.html) for a additional information about loading a test
suite.

![The following text is a note](../../images/hg_note.gif){longdesc="quickStart.html"}\
Until you provide both a work directory and a valid configuration file, you cannot run tests.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
