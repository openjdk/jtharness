---
hIndent: 2
title: Specifying Tests to Run
---

[]{#initialFiles}

# Specifying Tests to Run {#specifying-tests-to-run .proc}

[]{#quickSet}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="initialFiles.html"}To
directly specify the tests that are run, perform the following steps:

1.  Click the ![QuickSet Mode button](../../images/stdValues_button.gif){width="10" height="11"
    longdesc="toolBar.html"} Quick Set Mode button on the Test Manager tool bar or choose Configure
    \> Edit Quick Set \> Tests To Run in the menu bar.

> The Configuration Editor opens in Quick Set Mode.

![Configuration editor Test tab](../../images/JT4testsTabConfigEd.gif){longdesc="initialFiles.html"}

> ![The following text is a note](../../images/hg_note.gif){longdesc="quickStart.html"}\
> In Question Mode, use the How to Specify Tests question to specify tests that are run.

2.  Click the Tests tab if it does not have focus.
3.  Choose the Specify radio button.

> The Configuration Editor enables the tree and the Load Test List button.

3.  Use the run tree or the Load Test List button to specify the tests that are run.

> See the following topics for detailed information about using the run tree or a load test list:
>
> -   [Run Tree](#testTree)
> -   [Load Test List](#testList)

4.  Click the Done button to save the configuration change.

[]{#testTree}

## Run Tree

In the Tests to Run pane, you can use the run tree to specify individual tests and folders for the
harness to run. The Tests pane highlights all folder and test icons selected for the test run. You
can make individual selections in the tree by pressing the Control key when you click an icon or
name. To select a series or sequence of tests or folders, press the Shift key and then click the
first and the last icon or name in the sequence.

When you select some of the tests in and under a folder, the Test pane partially highlights the
folder icon. If you choose a test folder, the harness selects all tests in the test suite under that
location for the test run. If you choose one or more tests, the harness selects those individual
tests for the test run.

The harness walks the tree starting with the sub-branches or tests you specify and executes all
tests not filtered out by the exclude list, keyword, or prior status.

![The following text is a Note](../../images/hg_note.gif){width="18" height="13"
longdesc="initialFiles.html"}\
Restrictions are applied cumulatively. For example, you can specify the tests in a test suite, then
restrict the set of tests using an exclude list, and then further restrict the set to only those
tests that passed on a prior run.

[]{#testList}

## Load Test List

When you click the Load Test List button, the harness opens a dialog box for you to use in locating
and selecting the test list file. The test list file is a `.txt` file that contains a list of tests.
See [Test List File Format](#testListFormat) and [Creating a Test List File](#createtestList) for a
detailed description of the test list file.

When the file is loaded, the harness updates the run tree to indicate the tests specified in the
test list file.

[]{#testListFormat}

### Test List File Format

The format requirements of the test list file are as follows:

-   Line-oriented
-   Relative test paths
-   Blank lines and lines beginning with the pound symbol (#) are ignored
-   On all other lines, the first item (up to the first whitespace) is taken as a test name and the
    remainder of the line is ignored

The format of the load list file is compatible with the format of the `summary.txt` generated as
part of a report.

[]{#createtestList}

### Creating a Test List File {#creating-a-test-list-file .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="initialFiles.html"}To create
a test list file, perform the following steps:

1.  Select the lines from `summary.txt` that define the tests you want to run.

> ![The following text is a Note](../../images/hg_note.gif){longdesc="createTemplate.html"}\
> You can use other sources for obtaining the path names of the tests that you want to include in
> the test list file. On a Solaris platform, you might use `awk`, `grep`, or similar utilities to
> identify lines that specify the tests with failed or error results.
>
> Example lines from `summary.txt`:\
> `BigNum/compareTest.html Failed. exit code 1`\
> `BigNum/equalsTest.html Failed. exit code 1`\
> `BigNum/longConstrTest.html Failed. exit code 1`\
> `BigNum/subtractTest.html Failed. exit code 1`\
> `lists/DoublyLinkedList/appendTest.html Failed. exit code 1`\
> `lists/DoublyLinkedList/equalsTest.html Failed. exit code 1`\
> `lists/DoublyLinkedList/insertTest.html Failed. exit code 1`\
> `lists/DoublyLinkedList/removeTest.html Failed. exit code 1`\
> `lists/LinkedList/appendTest.html Failed. exit code 1`\
> `lists/LinkedList/equalsTest.html Failed. exit code 1`\
> `lists/LinkedList/insertTest.html Failed. exit code 1`\
> `lists/LinkedList/removeTest.html Failed. exit code 1`\
> `lists/SortedList/equalsTest.html Failed. exit code 1`\
> `lists/SortedList/insertTest.html Failed. exit code 1`\
> `lists/SortedList/removeTest.html Failed. exit code 1`

2.  Copy and paste the relative paths into a `.txt` file.
3.  Remove all text that is not part of the test path.

> Example lines in the `.txt` file:\
> `BigNum/compareTest.html`\
> `BigNum/equalsTest.html`\
> `BigNum/longConstrTest.html`\
> `BigNum/subtractTest.html`\
> `lists/DoublyLinkedList/appendTest.html`\
> `lists/DoublyLinkedList/equalsTest.html`\
> `lists/DoublyLinkedList/insertTest.html`\
> `lists/DoublyLinkedList/removeTest.html`\
> `lists/LinkedList/appendTest.html`\
> `lists/LinkedList/equalsTest.html`\
> `lists/LinkedList/insertTest.html`\
> `lists/LinkedList/removeTest.html`\
> `lists/SortedList/equalsTest.html`\
> `lists/SortedList/insertTest.html`\
> `lists/SortedList/removeTest.html`

4.  Save the `.txt` file using a descriptive name.

> Example `.txt` file name:
>
> `BigNum_ListsfailedTests.txt`

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
