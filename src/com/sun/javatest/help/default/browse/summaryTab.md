---
hIndent: 2
title: Summary Information
---

[]{#summaryTab}

# Summary Information

When you click a folder icon in the test tree, the harness uses the current view filter to display
Summary information about the folder\'s test results.

The Summary pane contains header information that identifies the folder and the view filter
presented in the Summary table and its associated pie chart. The pie chart displayed in the Summary
pane is a graphical representation of the tabular data.

The following table describes the filtered work directory information that the Summary table and pie
chart can display.

  Field                                      Description
  ------------------------------------------ ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  []{#summaryTab.passed} Passed              The number of tests in a folder (including all of its subordinate folders) displayed in the test tree that were run and have passed test results.
  []{#summaryTab.failed} Failed              The number of tests in a folder (including all of its subordinate folders) displayed in the test tree that were run and have failed test results.
  []{#summaryTab.error} Error                The number of tests in a folder (including all of its subordinate folders) displayed in the test tree that were run but had errors.
  []{#summaryTab.notRun} Not Run             The number of tests in a folder (including all of its subordinate folders) in the test tree that have not yet been run and were not filtered out.
  []{#summaryTab.subtotal} Sub-Total         The total number of tests that were selected to run.
  []{#summaryTab.filteredOut} Filtered Out   The total number of tests in a folder (including all of its subordinate folders) displayed in the test tree that were filtered out. Tests that were filtered out include tests that you omitted from the test run by using keywords, prior status, or exclude lists.
  []{#summaryTab.total} Total                Total number of tests in a folder and its subordinate folders.

![The following text is a note](../../images/hg_note.gif){longdesc="summaryTab.html"}\
The GUI only displays results of those tests in the work directory that match your *view* filter
setting. Changing the view filter used in the Test Manager changes the values displayed in the
Summary pane.

When using the All Tests view filter, new settings in the current configuration do not change the
values displayed in the Summary pane.

When using the Current Configuration view filter, each time you make a change in the configuration,
the values displayed in the Summary pane are recalculated and displayed based on the new settings in
the current configuration. See the following example for a description of the use of the Current
Configuration view filter and the All Tests view filter.

**Example:**\
If you rerun a set of tests and use the Current Configuration view filter with Prior Status: Failed
set in the configuration (see [Specifying Prior Status](../confEdit/status.html)), as actual test
status in the work directory changes from Failed to either Passed or Error, the test status
displayed in the Summary pane changes from Failed to Filtered Out.

If you change the view filter to All Tests, the test tree and Summary pane immediately display the
actual results of all tests in the work directory, regardless of the Prior Status settings in the
configuration.

See [Problems Viewing Test Results](../concepts/troubleshooting.html#troubleshooting.viewing) for
additional information about viewing test status in the test tree and Summary pane.

Click the appropriate status information tab to identify the individual tests in a category.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2010, Oracle and/or its affiliates. All rights reserved.
