---
hIndent: 2
title: Specifying Prior Status
---

# Specifying Prior Status {#specifying-prior-status .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="status.html"}To specify
prior status used to restrict the set of tests that are run, perform the following steps:

1.  Click the ![QuickSet Mode button](../../images/stdValues_button.gif){width="10" height="11"
    longdesc="toolBar.html"}Quick Set Mode button on the Test Manager tool bar or choose Configure
    \> Edit Quick Set \> Prior Status in the menu bar.

> The Configuration Editor opens in Quick Set Mode and displays the Prior Status tab.

![Prior status tab](../../images/JT4priorstatusTabConfigEd.gif){longdesc="status.html"}

> ![The following text is a note](../../images/hg_note.gif){longdesc="quickStart.html"}\
> In Question Mode, use the Status question to specify prior status used to restrict the set of
> tests that are run.

2.  Check the Select tests that match option.

> The Configuration Editor enables the prior status check boxes.

3.  Check the one or more of the prior status conditions.

> See [Prior Status Selections](#priorStatusSelections) for detailed descriptions of the available
> choices and how the harness uses them to filter the tests that are run.

4.  Click the Done button to save the configuration change.

[]{#priorStatusSelections}

## Prior Status Filter Selections

By choosing Select tests that match, you can run tests with restrictions based on their result from
a prior test run. The following table describes the available Prior Status filter selections.

  Prior Status Filter   Action
  --------------------- -----------------------------------------------------------------------------------------------
  Passed                Selects tests with passing results the last time the test was executed.
  Failed                Selects tests with failed results the last time the test was executed.
  Error                 Selects tests that the harness could not execute the last time it was included in a test run.
  Not Run               Selects tests without results in the current work directory.

Prior status is evaluated on a test-by-test basis using information stored in the result files
([`.jtr`]{#jtr}) that are written in the work directory. Unless overridden by a test suite, a result
file is written in the work directory for every test that is executed. When you change work
directories between test runs, the result files in the previous work directory are no longer used
and, if the new work directory is empty, the harness behaves as though each test in the test suite
was not run.

You can also use the Prior Status setting in combination with the Current Configuration view filter
to display only those test and folder status icons that match the specified prior status. The test
tree displays all other tests and folders as gray, filtered out ![Filtered out
folder](../../images/grayFolder.gif){longdesc="../browse/folderIcons.html"} folder and ![Test
Filtered Out](../../images/grayTest.gif){longdesc="../browse/testIcons.html"} test icons. During a
test run, when a test result no longer matches the prior status filter, the test tree changes the
test and folder icons to gray, filtered out ![Filtered out
folder](../../images/grayFolder.gif){longdesc="../browse/folderIcons.html"} folder and ![Test
Filtered Out](../../images/grayTest.gif){longdesc="../browse/testIcons.html"} test icons.

For example, if you only want to monitor tests in a test suite that had failed results you would set
the Prior Status filter to Any Of Failed and repeat the test run. As tests pass, the test tree
changes their icons from failed to filtered out, indicating that they no longer match the Prior
Status filter Any Of Failed.

![The following text is a note](../../images/hg_note.gif){longdesc="status.html"}\
It is often useful to choose all of the status values except Passed for the first few test runs,
then refine the filtering to reduce the number of tests in subsequent runs.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2014, Oracle and/or its affiliates. All rights reserved.
