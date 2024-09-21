---
hIndent: 2
title: Status Information
---

[]{#statusTabs}

# Status Information

In addition to Summary and Documentation information, the folder view contains five status tabs that
use the view filter to group and list a folder\'s tests by their results in the work directory. See
[Displaying Folder Information](folderInfo.html) for additional information.

-   In the tabbed panes, click a test in the list to display the summary message at the bottom of
    the pane.

-   By default the list has two columns, Name and Title. Click a heading to sort the list.

-   To display alternate information in the second column, right-click on the column heading and
    make a selection from the Description or Runtime context menus.\
    ![Additional test
    information](../../images/statusAltInfo.gif){longdesc="Additional test information for can be displayed in the second column"}

-   Double click the test name or press Enter to display the test in the test tree and to view its
    unfiltered test information. See [Displaying Test Information](testInfo.html) for additional
    information.

[]{#statusTabs.passed}

The colors of the following icons are the harness default settings. The harness enables you to use
colors other than these default settings. See [Specifying Status
Colors](../command/settingColors.html) in the *Command-Line Interface User\'s Guide*.

## ![Passed folder](../../images/greenTest.gif){longdesc="statusTabs.html"}Passed

Uses the view filter to display the test names of all tests in the folder (including all of its
subordinate folders) displayed in the test tree that had passing results when they were run.

[]{#statusTabs.failed}

## ![Failed folder](../../images/redTest.gif){longdesc="statusTabs.html"}Failed

Uses the view filter to display the path names of all tests in the folder (including all of its
subordinate folders) displayed in the test tree that were run and had failing results.

[]{#statusTabs.error}

## ![Error folder](../../images/blueTest.gif){longdesc="statusTabs.html"}Error

Uses the view filter to display the path names of all tests in the folder (including all of its
subordinate folders) displayed in the test tree with errors that prevented them from being executed.

[]{#statusTabs.notRun}

## ![Not Run folder](../../images/whiteTest.gif){longdesc="statusTabs.html"}Not Run

Uses the view filter to display the path names of all tests in the folder (including all of its
subordinate folders) displayed in the test tree that are selected by the view filter but have not
been run.

[]{#statusTabs.notRun}

## ![Filtered Out folder](../../images/grayTest.gif){longdesc="statusTabs.html"}Filtered Out

Uses the view filter to display a two-column list. The first column contains the names of tests in
the selected folder whose results are filtered out by the selected view filter.

The second column contains a specific reason why the test result is filtered out by the view filter.
The specific reason depends on the view filter criteria. See [View Filters](viewFilters.html) for a
detailed description of filtering criteria.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
