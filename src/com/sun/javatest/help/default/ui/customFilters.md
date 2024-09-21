---
hIndent: 4
title: Custom View Filter
---

[]{#customFilters}

# Custom View Filter

If you want to monitor a specific set of test results in the Test Manager, you can create a custom
view filter.

The custom view filter is unique to the test suite. When the harness opens the test suite in the
Test Manager, it restores the custom filter, including any name that you assign it.

You can also use the custom filter to generate reports. See [Creating
Reports](../report/newReports.html) for a description of using the Custom filter when generating
reports.

![Custom view filter dialog
box](../../images/JT4editCustomFilter.gif){longdesc="customFilters.html"}

[]{#customFilters.edit}

## Creating a Custom View Filter {#creating-a-custom-view-filter .proc}

1.  Choose View \> Filter \> Configure Filters from the menu bar to open the Filter Editor. You can
    also choose Custom and then Edit Filter in the tool bar.
2.  Choose Custom in the Available Filters panel. You can provide a name for the filter in the
    Custom Label field. The name is applied to the filter and restored each time the test suite is
    opened.
3.  Use the tabbed panes to set the following view filter properties:\
    -   [Specify Tests to View](#customFilters.initialFiles)
    -   [Use the Exclude List as a View Filter](#customFilters.excludeList)
    -   [Use Keywords as a View Filter](#customFilters.keywords)
    -   [Use Prior Status as a View Filter](#customFilters.status)
    -   [Use Special Settings as a View Filter](#customFilters.special)
4.  Click one of the following buttons:
    -   **Apply** - Saves but does not dismiss the dialog box. Updates the GUI if the filter is
        selected.
    -   **Reset** - Discards all changes and restores the last saved Custom filter.
    -   **Cancel** - Closes the dialog box without saving any changes.
    -   **OK** - Saves the current changes, updates the Custom filter, and closes the dialog box.
    -   **Help** - Displays online help for the Filter Editor.

Using Test Suite Areas, Keywords, Prior Status, Exclude Lists, and Special settings in the view
filter does not stop the harness from running these tests. To filter the tests that are run, you
must change the values in the configuration. See [Editing a
Configuration](../confEdit/editConfiguration.html) for a detailed description.

[]{#customFilters.initialFiles}

### Specify Tests to View

Click the Test Suite Areas tab and use the tree to choose the results of test folders or individual
tests that you want displayed in the Test Tree. The harness walks the test tree starting with the
sub-branches and tests you specify and displays the results of all tests that it finds.

[]{#customFilters.keywords}

### Use Keywords as a View Filter

If your test suite provides keywords, you can use the Keywords pane to restrict the set of test
results displayed in the test tree and in the Summary pane.

To specify the keywords:

1.  Click Match.

> The harness enables the Expression button.

2.  Click Expression to display a list of expressions that can be constructed.
3.  From the list, choose the type of expression that you are building.
4.  In the text field, enter the keywords and operators used in the expression.

The following table provides descriptions and examples of keyword expressions that can be
constructed.

+-------------------------------------------------+-------------------------------------------------+
| Expression                                      | Description                                     |
+=================================================+=================================================+
| `Any Of`                                        | Displays all tests in the test suite having     |
|                                                 | *any* of the keywords entered in the text       |
|                                                 | field.                                          |
|                                                 |                                                 |
|                                                 | Example:                                        |
|                                                 |                                                 |
|                                                 | A test suite uses the keyword `interactive` to  |
|                                                 | identify tests that require human interaction,  |
|                                                 | and `color` to identify tests that require a    |
|                                                 | color display.                                  |
|                                                 |                                                 |
|                                                 | To display only the results for tests           |
|                                                 | containing the `interactive` keyword, choose    |
|                                                 | `Any Of` and then use the `interactive`         |
|                                                 | keyword.                                        |
+-------------------------------------------------+-------------------------------------------------+
| `All Of`                                        | Displays results for all tests in the test      |
|                                                 | suite having *all* of the keywords entered in   |
|                                                 | the text field.                                 |
|                                                 |                                                 |
|                                                 | Example:                                        |
|                                                 |                                                 |
|                                                 | To display results for only the tests           |
|                                                 | containing both the `interactive` and `color`   |
|                                                 | keywords, choose `All Of` and then use the      |
|                                                 | `interactive` and `color` keywords.             |
+-------------------------------------------------+-------------------------------------------------+
| `Expression`                                    | Displays results for all tests in the test      |
|                                                 | suite having the *expression* entered in the    |
|                                                 | text field.                                     |
|                                                 |                                                 |
|                                                 | Construct a Boolean expression in the text      |
|                                                 | field. Keywords stand as Boolean predicates     |
|                                                 | that are true if, and only if, the keyword is   |
|                                                 | present in the test being considered. A test is |
|                                                 | accepted if the overall value of the expression |
|                                                 | is true. All other tests are rejected by the    |
|                                                 | restriction.                                    |
|                                                 |                                                 |
|                                                 | Example:                                        |
|                                                 |                                                 |
|                                                 | A test suite uses the keyword `interactive` to  |
|                                                 | identify tests that require human interaction,  |
|                                                 | and `color` to identify tests that require a    |
|                                                 | color display.                                  |
|                                                 |                                                 |
|                                                 | To display results for only the tests with the  |
|                                                 | `color` keyword that do not also contain the    |
|                                                 | `interactive` keyword, choose `Expression` and  |
|                                                 | then use the `color` keyword, the `!` operator, |
|                                                 | and the `interactive` keyword.                  |
+-------------------------------------------------+-------------------------------------------------+

[]{#customFilters.status}

### Use Prior Status as a View Filter

Click the Prior Status tab and choose the test results from the previous test run that you want
displayed in the Test Tree.

  Prior Status Filter   Action
  --------------------- ------------------------------------------------------------------------------------------------
  Passed                Displays tests with passed results the last time the test was executed.
  Failed                Displays tests with failed results the last time the test was executed.
  Error                 Displays tests that the harness could not execute the last time it was included in a test run.
  Not Run               Displays tests without results in the current work directory.

Prior status is evaluated on a test-by-test basis using information stored in the result files
([`.jtr`]{#jtr}) that are written in the work directory. Unless overridden by a test suite, a result
file is written in the work directory for every test that is executed. When you change work
directories between test runs, the result files in the previous work directory are no longer used
and, if the new work directory is empty, the harness behaves as though the test suite was not run.

[]{#customFilters.excludeList}

### Use the Exclude List as a View Filter

To use the exclude list specified in the configuration interview as a filter, click the Exclude
Lists tab and the Use settings in interview check box. The Exclude Lists pane displays the name of
the exclude list file used by the current configuration.

Any test in the exclude list is filtered out and displayed as a ![Test Filtered
Out](../../images/grayTest.gif){longdesc="testIcons.html"} icon in the test tree.

[]{#customFilters.special}

### Use Special Settings as a Filter

If the test suite architect provides a default filter for the test suite, click the Special tab and
the Enable test suite filter check box to select this filter.

You must select this setting to correctly simulate the Current Configuration settings.

[]{#customFilters.use}

## Use a Custom View Filter

To use a custom filter, you must choose it from the list of view filters below the test tree or from
the View \> Filters menu. The Test Manager updates the status of the folders and tests in the test
tree that match the filter settings of the custom filter.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2009, Oracle and/or its affiliates. All rights reserved.
