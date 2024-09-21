---
hIndent: 2
title: Changing Tests to Run
---

[]{#initialFilesTE}

# Changing Tests to Run {#changing-tests-to-run .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="testsTabTE.html"}To change
the set of tests that are specified to run, perform the following steps:

1.  Choose View \> Quick Set Mode from the Template Editor menu bar.
2.  Click the Tests tab in the Template Editor.

![Test Suite Areas pane from the Template Editor: Quick Set
mode](../../images/JT4testsTabConfigEd.gif){border="0" longdesc="testsTabTE.html"}

3.  In Quick Set Mode, choose Specify to enable the Template Editor test tree and the Load Test List
    button.\
    \
    In Question Mode, use the How to Specify Tests question to specify whether to use the test tree
    or a test list.

<!-- -->

4.  Use the test tree or a test list to specify the tests that are run.\
    \
    See the following topics for detailed information about using the test tree or load list to
    specify tests:
    -   [Specifying Tests in the Test Tree](#testTree)
    -   [Loading a Test List](#testList)

<!-- -->

5.  Change additional template settings or click the Done button to save the changes in the
    template.\
    \
    If you are creating a new template for these changes, instead of clicking the Done button,
    choose File **\>** Save As from the Template Editor menu bar and save the template with a
    relevant name.

[]{#testTree}

## Specifying Tests in the Test Tree

In the Template Editor test tree (not the Test Manager test tree) you can choose individual tests
and folders of tests for the harness to run. The harness walks the test tree starting with the
sub-branches or tests you specify and executes all tests not filtered out by the exclude list,
keyword, or prior status.

Restrictions are applied cumulatively. For example, you can specify the tests in a test suite, then
restrict the set of tests using an exclude list, and then further restrict the set to only those
tests that passed on a prior run.

If you choose a test folder, the harness selects all tests in the test suite under that location for
the test run.

If you choose one or more tests, the harness selects those individual tests for the test run.

The Tests pane highlights all folder and test icons selected for the test run. You can make
individual selections in the test tree by pressing the Control key when you click an icon or name in
the test tree.

To select a series or sequence of tests or folders, press the Shift key and then click the first and
the last icon or name in the sequence.

When you select some (but not all) of the tests in and under a folder, the Test pane partially
highlights the folder icon.

[]{#testList}

## Loading a Test List {#loading-a-test-list .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="testsTabTE.html"}To load a
test list file in the template, perform the following steps:

1.  Click the Load Test List button.
2.  Use the dialog box to locate and select a test list file.\
    \
    See Test List File Format for a detailed description of a test list file.

<!-- -->

3.  When the file is loaded, the test tree is updated to indicate the tests that have been
    specified.

### Test List File Format

A test list is a `.txt` file containing a list of tests to run. See [Creating a Test List
File](#createtestList) for a detailed description of the test list file. The format requirements of
the test list file are as follows:

-   Line-oriented
-   Relative test paths
-   Blank lines and lines beginning with the pound symbol (#) are ignored
-   On all other lines, the first item (up to the first whitespace) is taken as a test name and the
    remainder of the line is ignored

The format of the load list file is the same as the format of the `summary.txt` generated as part of
a report.

[]{#createtestList}

### Creating a Test List File {#creating-a-test-list-file .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="testsTabTE.html"}To create a
test list file, perform the following steps:

1.  Select the lines from `summary.txt` that define the tests you want to run.

    ![The following text is a Note](../../images/hg_note.gif){longdesc="createTemplate.html"}\
    You can use other sources for obtaining the path names of the tests that you want to include in
    the test list file. On a Solaris platform, you might use `awk`, `grep`, or similar utilities to
    identify lines that specify the tests with failed or error results.

    Example lines from `summary.txt`:

    ` BigNum/compareTest.html Failed. exit code 1`\
    `BigNum/equalsTest.html Failed. exit code 1`\
    `BigNum/longConstrTest.html Failed. exit code 1`\
    `BigNum/subtractTest.html Failed. exit code 1`\
    `lists/DoublyLinkedList/appendTest.html Failed. exit code 1`\
    `lists/DoublyLinkedList/equalsTest.html Failed. exit code 1`\
    `lists/DoublyLinkedList/insertTest.html Failed. exit code 1`\
    `lists/DoublyLinkedList/removeTest.html Failed. exit code 1`\
    `lists/LinkedList/appendTest.html Failed. exit code 1`\
    `lists/LinkedList/equalsTest.html Failed. exit code 1`\
    `lists/LinkedList/insertTest.html Failed. exit code 1`\
    `lists/LinkedList/removeTest.html Failed. exit code 1`\
    `lists/SortedList/equalsTest.html Failed. exit code 1`\
    `lists/SortedList/insertTest.html Failed. exit code 1`\
    `lists/SortedList/removeTest.html Failed. exit code 1`

<!-- -->

2.  Copy and paste the relative paths into a `.txt` file.

3.  Remove all text from the path names that is not part of the test path.

4.  Example lines in the `.txt` file:\
    \
    `BigNum/compareTest.html`\
    `BigNum/equalsTest.html`\
    `BigNum/longConstrTest.html`\
    `BigNum/subtractTest.html`\
    `lists/DoublyLinkedList/appendTest.html`\
    `lists/DoublyLinkedList/equalsTest.html`\
    `lists/DoublyLinkedList/insertTest.html`\
    `lists/DoublyLinkedList/removeTest.html`\
    `lists/LinkedList/appendTest.html`\
    `lists/LinkedList/equalsTest.html`\
    `lists/LinkedList/insertTest.html`\
    `lists/LinkedList/removeTest.html`\
    `lists/SortedList/equalsTest.html`\
    `lists/SortedList/insertTest.html`\
    `lists/SortedList/removeTest.html`

<!-- -->

4.  Save the `.txt` file using a descriptive name.\
    \

    Example `.txt` file name:

    `BigNum_ListsfailedTests.txt`

 

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2006, 2011, Oracle and/or its affiliates. All rights reserved.
