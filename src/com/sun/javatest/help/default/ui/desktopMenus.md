---
hIndent: 1
title: Menus
---

[]{#desktopMenus}

# Menus

The GUI provides two types of menus: desktop and tool. The layout style determines how the menus are
displayed in the GUI. The following table describes the two menu types and how the desktop style
determines how they are displayed.

+-------------------------------------------------+-------------------------------------------------+
| Menu Type                                       | Description                                     |
+=================================================+=================================================+
| Desktop                                         | Desktop menus are always available to the user  |
|                                                 | in the desktop menu bar. See [GUI               |
|                                                 | Layout](desktopStyles.html) for a detailed      |
|                                                 | description of the GUI layout options available |
|                                                 | to the user. They include the following menus:  |
|                                                 |                                                 |
|                                                 | -   [File](#desktopMenus.file)                  |
|                                                 | -   [Tool](desktopMenus.html#desktopMenus.tool) |
|                                                 | -   [Windows](#desktopMenus.window)             |
|                                                 | -   [Help](#desktopMenus.help)                  |
+-------------------------------------------------+-------------------------------------------------+
| Tool                                            | These menus are unique to specific tools and    |
|                                                 | are only available to the user when that tool   |
|                                                 | window is open. The appropriate tool menus are  |
|                                                 | merged with the desktop menus in a single menu  |
|                                                 | bar.                                            |
|                                                 |                                                 |
|                                                 | See [Agent Monitor Tool](agentMonitor.html) for |
|                                                 | a description of the Agent Monitor tool menus.  |
|                                                 | If your test suite uses the harness agent, see  |
|                                                 | the *JT Harness Agent User\'s Guide* for a      |
|                                                 | description of the Agent Monitor.               |
|                                                 |                                                 |
|                                                 | See [Report Converter                           |
|                                                 | Tool](../mergeReports/window.html) for a        |
|                                                 | description of the Report Converter tool menus. |
|                                                 |                                                 |
|                                                 | See [Test Manager Tool](window.html) for a      |
|                                                 | description of the Test Manager tool menus.     |
+-------------------------------------------------+-------------------------------------------------+

[]{#desktopMenus.file}

## File

Use the File menu to open files, set user preferences, and exit from the harness. The contents of
the File menu change dynamically, based on the context of the desktop. The harness only enables
menus when they can be used. The following table describes the contents of the File menu.

Menu Item

Description

Open the Quick Start Wizard

**Note: **Some test suites do not implement the optional Quick Start wizard. It won\'t be available
if the test suite architect disabled it.\

Opens the optional Quick Start wizard for you to use for the following tasks:

-   Start a new test run by selecting a test suite, work directory, and a configuration file. If the
    configuration file is new or incomplete, the harness opens the Configuration Editor after the
    wizard closes.
-   Resume work on a previous test run or browse a test suite without running tests.
-   Browse the contents of a test suite.

Create Work Directory

Opens a dialog box that you can use to create a work directory. When you choose to create a new work
directory, the harness associates it with the test suite opened in the Test Manager tool window. The
harness creates test result files in the work directory that contain all of the information gathered
by the harness during test runs of the test suite.

See [Creating a Work Directory](../start/createDirectory.html) for detailed information about
creating a work directory.

Open **\>** Work Directory

Opens a dialog box that you can use to open an existing work directory. Each work directory is
associated with a specific test suite and contains information from previous test runs. The test
result files in the work directory contain all of the information gathered by the harness during
test runs.

When you choose to open an existing work directory, the harness performs the following actions:

-   It associates that work directory with the test suite opened in the Test Manager tool window
    *only* if the test suite is both a match and has no other work directory already open.
-   If it cannot associate the work directory with the open test suite, the harness opens a new Test
    Manager and loads both the work directory and its associated test suite.

See [Opening a Work Directory](../start/openDirectory.html) for detailed information about opening a
work directory.

Open **\>** Test Suite

Opens a dialog box you can use to choose a test suite. When you open the test suite, the harness
loads it into new Test Manager.

See [Opening a Test Suite](../start/openTestSuite.html) for additional information.

[Recent Work Directory]{#desktopMenus.fileHistory}

Displays a list of work directories that have been opened. Choose a work directory from the list to
open a new instance of it in the current session.

Preferences

Opens the Preferences dialog box for you to set the display and functional options of the harness.

See [Setting Preferences](prefsDialog.html) for detailed information about setting preferences in
the GUI.

Close

Shown in the File menu. Closes the current window without exiting from the harness. Closing a Test
Manager closes the work directory.

Exit

Exits from the harness. If you have set your preferences to save your current desktop, your current
desktop is saved so that all open windows, test suites, work directories and configurations are
restored the next time that you start the harness.

See [Setting Preferences](prefsDialog.html) for detailed information about setting preferences in
the GUI.

[]{#desktopMenus.tool}

## Tools

The following table describes the tools provided by the harness.

+-------------------------------------------------+-------------------------------------------------+
| Tool                                            | Description                                     |
+=================================================+=================================================+
| [Report Converter](reportMerge.html)            | Opens the Report Converter tool that you use to |
|                                                 | generate and view reports of results from       |
|                                                 | multiple test reports.                          |
+-------------------------------------------------+-------------------------------------------------+
| [Test Manager](window.html)                     | Opens the Test Manager tool that you use to do  |
|                                                 | the following tasks:                            |
|                                                 |                                                 |
|                                                 | -   Open and create work directories.           |
|                                                 | -   Add or modify information that the harness  |
|                                                 |     uses when running your tests.               |
|                                                 | -   Run the tests of a test suite.              |
|                                                 | -   Monitor tests and test results while they   |
|                                                 |     are being run.                              |
|                                                 | -   View test environment settings of your      |
|                                                 |     configuration.                              |
|                                                 | -   View the contents of an exclude list.       |
|                                                 | -   Browse completed tests and test results.    |
|                                                 |                                                 |
|                                                 | During a test run, the icons in the Test        |
|                                                 | Manager change to reflect the test status. You  |
|                                                 | can also use the window during and after a test |
|                                                 | run to browse information about individual      |
|                                                 | tests.                                          |
+-------------------------------------------------+-------------------------------------------------+
| [Agent Monitor](agentMonitor.html) (Optional)   | The Agent Monitor tool is only available for    |
|                                                 | use when the test suite is configured to use an |
|                                                 | agent. If your test suite includes the JT       |
|                                                 | Harness agent, see the *JT Harness Agent        |
|                                                 | User\'s Guide* for detailed information about   |
|                                                 | the agent and the Agent Monitor tool.           |
+-------------------------------------------------+-------------------------------------------------+

[]{#desktopMenus.window}

## Windows

The Windows menu also provides a list of all windows currently open in the harness. You can navigate
to any open window by clicking on its name in the list.

[]{#desktopMenus.help}

## Help

Use the Help menu to display user information about the tool window and the harness, the available
test suite documentation, version information about the harness, and information about the current
Java runtime environment.

  Menu Item                               Description
  --------------------------------------- ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  *Active Window Name*                    Displays user information about the active window. This menu item is only available in an open tool window.
  Online Help                             Displays the online version of the harness User\'s Guide. This version combines the GUI, command-line interface, and the agent User\'s Guides (when applicable) into one document.
  *Test Suite Documentation* (Optional)   If the test suite provides online documentation, the harness lists it here. Click the document name to display it in the viewer.
  About the JT Harness                    Displays information about this release of the harness.
  About the Java Virtual Machine          Displays information about the runtime used to run the harness.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2012, Oracle and/or its affiliates. All rights reserved.
