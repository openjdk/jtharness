---
hIndent: 4
title: Current Configuration View Filter
---

[]{#currentConfiguration}

# Current Configuration View Filter

When you select the Current Configuration view filter, the Test Manager only displays the status of
the folders and tests currently selected by the current configuration.

To select the Current Configuration view filter, choose View \> Filter \> Current Configuration from
the menu bar or choose the Current Configuration view filter from the tool bar.

The following examples provide descriptions of the use of the Current Configuration view filter:

-   **Example 1 -** You only want to run and view the results for tests in the `api` folder. See
    [Specifying Tests to Run](../confEdit/initialFiles.html) for detailed description of specifying
    the tests that are run.

> Either choose View \> Filter \> Current Configuration from the menu bar or choose the Current
> Configuration view filter from the tool bar.
>
> The Test Manager displays gray ![Filtered out
> folder](../../images/grayFolder.gif){longdesc="folderIcons.html"} folder and ![Test Filtered
> Out](../../images/grayTest.gif){longdesc="testIcons.html"} test icons for all folders and tests
> except those under `api`. The Test Manager changes the Summary totals to indicate that these tests
> are filtered out.
>
> The current result icons are only displayed in the test tree for those folder and test icons under
> `api` and the Summary pane is updated to display their totals. When you run the tests, the test
> tree and folder view only display the results for those folders and tests under `api`.

-   **Example 2 -** You only want to run and view results for failed tests. See [Specifying Prior
    Status](../confEdit/status.html) for a detailed description of setting the prior status value.

> Either choose View \> Filters \> Current Configuration from the menu bar or choose the Current
> Configuration view filter from the tool bar.
>
> Immediately the Test Manager displays gray ![Filtered out
> folder](../../images/grayFolder.gif){longdesc="folderIcons.html"} folder and ![Test Filtered
> Out](../../images/grayTest.gif){longdesc="testIcons.html"} test icons for all folders and tests
> except those with Failed test results. The Test Manager updates the Summary totals to indicate
> that these tests are filtered out and changes the Passed, Error, and Not Run totals to indicate 0.
>
> The Test Manager displays those folders and tests in the test tree with Failed test results as red
> ![Failed folder](../../images/redFolder.gif){longdesc="folderIcons.html"} folder and ![Test
> Failed](../../images/redTest.gif){longdesc="testIcons.html"} test icons. The Test Manager updates
> the Summary information to indicate the number of tests that have Failed test results.
>
> When you repeat the test run, as failed tests pass, the Test Manager updates their icons from red
> ![Failed folder](../../images/redFolder.gif){longdesc="folderIcons.html"} folder and ![Test
> Failed](../../images/redTest.gif){longdesc="testIcons.html"} test icons to gray ![Filtered out
> folder](../../images/grayFolder.gif){longdesc="folderIcons.html"} folder and ![Test Filtered
> Out](../../images/grayTest.gif){longdesc="testIcons.html"} test icons (they no longer match the
> configuration criteria of the view filter). The Test Manager updates the totals in the Summary
> pane to match the results displayed in the test tree.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2009, Oracle and/or its affiliates. All rights reserved.
