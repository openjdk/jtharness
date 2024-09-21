---
hIndent: 2
title: Specifying Keywords
---

[]{#keywords}

# Specifying Keywords {#specifying-keywords .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="keywords.html"}To directly
specify the tests that are run, perform the following steps:

1.  Click the ![Quick Set Mode button](../../images/stdValues_button.gif){width="10" height="11"
    longdesc="toolBar.html"} Quick Set Mode button on the Test Manager tool bar or choose Configure
    \> Edit Quick Set \> Keywords in the menu bar.

> The Configuration Editor opens in Quick Set Mode.

![Keyword tab](../../images/JT4keywordTabConfigEd.gif){longdesc="keywords.html"}

> ![The following text is a note](../../images/hg_note.gif){longdesc="quickStart.html"}\
> The harness displays this tab only if your test suite uses keywords.\
> You can use one or more keywords to restrict the set of tests that the harness\
> runs. In Question Mode, use the Keywords question to specify test run restrictions based on the
> keywords.

2.  Click the Keywords tab if it does not have focus.
3.  Click the Select tests that match checkbox.

> The harness enables the Expression, Insert Operator, and Insert Keyword buttons.

3.  Build an expression in the text field by using any logical combination of the following actions:

> -   Click the Expression button to display a list of expressions that can be constructed. From the
>     list, choose the type of expression that you are building. See [List of
>     Expressions](#ListofExpressions) for a description of the available types of expressions.
> -   Click the Insert Keyword button to display the list of keywords provided by the test suite for
>     use in filtering tests (this is only available if the test suite has information). From the
>     list, choose one or more keywords used in the expression.
> -   Click the Insert Operator button to display a list of logical operators that you can include
>     to construct boolean expressions in the text field. From the list, choose an operator to
>     include it in the expression. See [List of Logical Operators](#ListofOperators) for a
>     description of the available operators.

4.  Click the Done button to save the configuration change.

[]{#ListofExpressions}

## List of Expressions

The following table provides descriptions and examples of keyword expressions that can be
constructed.

+-------------------------------------------------+-------------------------------------------------+
| Expression                                      | Description                                     |
+=================================================+=================================================+
| Any Of                                          | Runs all tests in the test suite having any of  |
|                                                 | the keywords entered in the text field.         |
|                                                 |                                                 |
|                                                 | Example:\                                       |
|                                                 | A test suite uses the keyword `interactive` to  |
|                                                 | identify tests that require human interaction,  |
|                                                 | and `color` to identify tests that require a    |
|                                                 | color display.                                  |
|                                                 |                                                 |
|                                                 | To execute only the tests containing the        |
|                                                 | `interactive` keyword, choose Any Of and then   |
|                                                 | use the Insert Keyword button to choose the     |
|                                                 | `interactive` keyword.                          |
+-------------------------------------------------+-------------------------------------------------+
| All Of                                          | Runs all tests in the test suite having all of  |
|                                                 | the keywords entered in the text field.         |
|                                                 |                                                 |
|                                                 | Example:\                                       |
|                                                 | To execute only the tests containing both the   |
|                                                 | `interactive` and `color` keywords, choose All  |
|                                                 | Of and then use the Insert Keyword button to    |
|                                                 | choose the `interactive` and `color` keyword.   |
+-------------------------------------------------+-------------------------------------------------+
| Expression                                      | Runs all tests in the test suite having the     |
|                                                 | expression entered in the text field.           |
|                                                 |                                                 |
|                                                 | Use the Insert Keyword and the Insert Operator  |
|                                                 | buttons to construct a Boolean expression in    |
|                                                 | the text field. Keywords stand as Boolean       |
|                                                 | predicates that are true if, and only if, the   |
|                                                 | keyword is present in the test being            |
|                                                 | considered. A test is accepted if the overall   |
|                                                 | value of the expression is true. All other      |
|                                                 | tests are rejected by the restriction.          |
|                                                 |                                                 |
|                                                 | Example:\                                       |
|                                                 | A test suite uses the keyword `interactive` to  |
|                                                 | identify tests that require human interaction,  |
|                                                 | and `color` to identify tests that require a    |
|                                                 | color display.                                  |
|                                                 |                                                 |
|                                                 | To execute only the tests with the `color`      |
|                                                 | keyword that do not also contain the            |
|                                                 | `interactive` keyword, choose Expression and    |
|                                                 | then use the Insert Keyword button to choose    |
|                                                 | the `color` keyword, the Insert Operator button |
|                                                 | to choose the ! operator, and the Insert        |
|                                                 | Keyword button to choose the `interactive`      |
|                                                 | keyword.                                        |
+-------------------------------------------------+-------------------------------------------------+

[]{#ListofOperators}

## List of Logical Operators

Logical operators are only available when Expression is selected in the list of expressions. The
following table provides descriptions and examples of logical operators that can be used to build
keyword expressions. The precedence column indicates the order in which the expression is resolved.
Expressions in parentheses are evaluated first, with nested parentheses being evaluated from the
innermost parentheses outward.

+--------------------------------+--------------------------------+--------------------------------+
| Logical Operator               | Precedence                     | Description                    |
+================================+================================+================================+
| ( ) group                      | 1                              | Used to create groups of       |
|                                |                                | expressions.                   |
|                                |                                |                                |
|                                |                                | Example:\                      |
|                                |                                | A test suite uses the keyword  |
|                                |                                | `interactive` to identify      |
|                                |                                | tests that require human       |
|                                |                                | interaction and `color` to     |
|                                |                                | identify tests that require a  |
|                                |                                | color display.                 |
|                                |                                |                                |
|                                |                                | `!(interactive&color)`         |
|                                |                                |                                |
|                                |                                | The harness will exclude tests |
|                                |                                | that include both keywords.    |
+--------------------------------+--------------------------------+--------------------------------+
| ! not                          | 2                              | Logical not. Used to exclude   |
|                                |                                | tests containing the           |
|                                |                                | expression.                    |
|                                |                                |                                |
|                                |                                | Example:\                      |
|                                |                                | A test suite uses the keyword  |
|                                |                                | `interactive` to identify      |
|                                |                                | tests that require human       |
|                                |                                | interaction and `color` to     |
|                                |                                | identify tests that require a  |
|                                |                                | color display.                 |
|                                |                                |                                |
|                                |                                | `!interactive&!color`          |
|                                |                                |                                |
|                                |                                | The harness will exclude tests |
|                                |                                | that include either keyword.   |
+--------------------------------+--------------------------------+--------------------------------+
| & and                          | 3                              | Logical and. Used to combine   |
|                                |                                | expressions.                   |
|                                |                                |                                |
|                                |                                | Example:\                      |
|                                |                                | A test suite uses the keyword  |
|                                |                                | `interactive` to identify      |
|                                |                                | tests that require human       |
|                                |                                | interaction and `color` to     |
|                                |                                | identify tests that require a  |
|                                |                                | color display.                 |
|                                |                                |                                |
|                                |                                | `interactive&`color            |
|                                |                                |                                |
|                                |                                | The harness will only choose   |
|                                |                                | tests that include both        |
|                                |                                | keywords.                      |
+--------------------------------+--------------------------------+--------------------------------+
| \| or                          | 4                              | Logical or. Used to specify    |
|                                |                                | either of two expressions.     |
|                                |                                |                                |
|                                |                                | Example:\                      |
|                                |                                | A test suite uses the keyword  |
|                                |                                | `interactive` to identify      |
|                                |                                | tests that require human       |
|                                |                                | interaction and `color` to     |
|                                |                                | identify tests that require a  |
|                                |                                | color display.                 |
|                                |                                |                                |
|                                |                                | `interactive|`color            |
|                                |                                |                                |
|                                |                                | The harness will only choose   |
|                                |                                | tests that include either      |
|                                |                                | keyword.                       |
+--------------------------------+--------------------------------+--------------------------------+

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
