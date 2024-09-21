---
hIndent: 3
title: Test Tree
---

[]{#usingtree}

# Test Tree

The test tree is a multifunction panel that performs the following functions:

-   Displays the current status and state of tests in the test suite
-   Enables users to choose any combination of tests and folders in the test suite for a test run
-   Enables users to refresh or to clear specific test results

The test tree supports keyboard navigation. See [Keyboard access](keyboardAccess.html) for a
description of how the keyboard can be used to navigate the test tree pane.

## Displaying Test Status and State

The test tree uses folder icons, test icons, and two independent types of filtering (run filtering
and view filtering) to simultaneously display the progress of a test run and the current test
results in the work directory.

The test tree indicates the progress of a test run by displaying an arrow to the left of each folder
and test icon as it is loaded and run. See [Monitoring With a Test Tree](../run/testTree.html) for
detailed information about using the test tree to monitor a test run.

Based on the Test Manager view filter, the test tree updates the folder and test result icons to
indicate the current test results in the work directory.

> Example:
>
> When you choose the Current Configuration view filter, the test tree only displays the results for
> the tests specified in your current configuration. If you choose the All Tests view filter, the
> test tree immediately redisplays all test results from the work directory regardless of the
> settings in your current configuration. To display filtered summary information about the test
> results in the work directory, click the test suite icon in the test tree.

See [View Filters](../browse/viewFilters.html) for a detailed description of how to specify which
test results from the work directory are displayed in the test tree.

When you click a folder icon in the test tree, the harness displays its folder view in the Test
Manager information area. The Test Manager uses the view filter to filter results displayed in the
folder view. See [Displaying Folder Information](../browse/folderInfo.html) for additional
information.

When you click a test icon in the test tree, the harness displays its test view in the Test Manager
information area. Unlike the folder view, the Test Manager does not filter results in the test view.
See [Test View](../browse/testInfo.html) for additional information.

## Run, Refresh, or Clear Results

You can select any combination of tests and test folders and right click in the test tree to open a
pop-up menu for performing the following actions:

-   Execute highlighted folders and tests by performing a quick pick execution.
-   Refresh the list of folders and tests without restarting the harness by performing an on-demand
    refresh scan for new folders, new tests and updated test descriptions.
-   Clear Results by performing an on-demand clearing of the contents of the highlighted folder,
    test, or entire work directory.

If a single test or folder is highlighted in the test tree, the Test Manager displays the
appropriate Test View or Folder View. However, when multiple tests and folders are highlighted in
the test tree, the harness displays a Multiple Tree Nodes Selected pane that lists the highlighted
tests and folders. Highlight multiple tests and folders in the test tree by using standard
keystrokes (such as Shift and Control) and clicking the test and folder icons in the test tree.

The selection list in the Multiple Tree Nodes Selected pane displays the full path of each node
highlighted in the test tree. Click a test or folder in the test tree a second time to remove the
highlighting and to remove it from the selection list. The GUI displays the updated Multiple Tree
Nodes Selected pane.

Folders and tests do not have to be highlighted in the test tree for you to use the pop-up menu. See
[Test Tree Pop-up Menu](popupmenu.html) for detailed information about using the pop-up menu.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
