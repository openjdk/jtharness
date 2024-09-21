---
hIndent: 2
title: Using Exclude Lists
---

[]{#excludeList}

# Using Exclude Lists {#using-exclude-lists .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="excludeList.html"} Perform
the following steps to specify one or more exclude list files in a configuration:

1.  Click the ![Configuration Editor Quick Set mode button displayed on the tool
    bar](../../images/stdValues_button.gif){longdesc="fullViewDialog.html"}   button in the Test
    Manager toolbar or choose Configure **\>** Edit Quick Set **\>** Exclude List from the Test
    Manager menu bar.

> The Configuration Editor opens in Quick Set Mode.

![Exclude List tab](../../images/JT4excludelistTabConfigEd.gif){longdesc="excludeList.html"}

> ![The following text is a Note](../../images/hg_note.gif){width="18" height="13"
> longdesc="initialFiles.html"}\
> Exclude list files contain a list of tests in a test suite that are not run by\
> the harness. You can also use Question Mode view to specify exclude lists.

2.  Click the Exclude List tab if it does not have focus.
3.  Use the buttons in the tabbed pane to set exclude list selection options and to add or remove
    exclude lists used to run tests in your computing environment.

> See [Exclude List Selection Options](#selectionOptions), [Latest Exclude
> List](#excludeList.latest), and [Other Exclude List](#excludeList.other) for a detailed
> description of these values and how they are used by the harness when running tests.

3.  Click the Done button to save the configuration change.

[]{#selectionOptions}

## Exclude List Selection Options

Use the following selections to specify the exclude list option used to run tests:

-   **None** - An Exclude List is not used.
-   **Initial** - Only enabled if the test suite provides an exclude list. If you choose Initial,
    the tests are run using the exclude list provided by the test suite.
-   **Latest** - Only enabled if the test suite provides a location for updated exclude lists. If
    you choose Latest, additional options are displayed. See [Latest Exclude
    List](#excludeList.latest) below for detailed information.
-   **Other** - A custom exclude list can be used. See [Other Exclude List](#excludeList.other)
    below for detailed information.

If a complete test is added to the exclude list, the harness updates the test result status without
requiring the test be rerun. However, if only a test case from a test is added to the exclude list,
the harness requires that you rerun the test using the updated exclude list before updating the test
result status.

[]{#excludeList.latest}

## Latest Exclude List

If your test suite provides a URL for the latest test suite, the harness enables the Latest exclude
list option. The following table describes the text and controls displayed when you choose the
Latest exclude list option.

Text and Controls

Description

Location:

Displays the location of the exclude list specified by the test suite. This is a non-editable field.

Last updated:

Displays the date that the exclude list was last updated. This is a non-editable field.

Check For Updates Automatically

Causes the harness to automatically check the location of the exclude list and compare the date-time
stamps of the remote and local exclude lists. The harness then displays a dialog box advising you of
the results. If a new exclude list is available, you can choose to download it.

Every \_ Days

Sets an interval for the harness to automatically check the remote location of the exclude list for
updates.

Every Test Run

Causes the harness to check the remote location of the exclude list for updates before each test
run.

Check Now

Causes the harness to check the remote location of the exclude list for an update.

[]{#excludeList.other}

## Other Exclude List

The following table describes the buttons displayed when you choose the Other exclude list option.

  Button      Description
  ----------- ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Add         Selects an exclude list file for your test suite. As you make selections with the file chooser dialog box, they are added to the list. After you add an exclude list, you can modify the list.
  Remove      Clears an item from the list. Select an item in the list and click Remove.
  Move Up     Moves an item one position higher in the list. Select an item in the list and click Move Up.
  Move Down   Moves an item one position lower in the list. Select an item in the list and click Move Down.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
