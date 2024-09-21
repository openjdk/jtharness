---
hIndent: 1
title: Searching a Configuration
---

# Searching a Configuration

When using the Configuration Editor in Question Mode, you can locate and display the panes
containing a specific character string. The harness can search titles, questions, and answers for
matching characters. It does not search the More Info.

## Search for Characters in Configurations {#search-for-characters-in-configurations .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="createConfiguration.html"}To
search for character strings in the configuration titles, questions, and answers, perform the
following steps:

1.  Click the ![Question Mode button](../../images/fullValues_button.gif){width="10" height="11"
    longdesc="toolBar.html"} Question Mode button on the Test Manager tool bar or choose Configure
    \> Edit Configuration in the menu bar.

> The Configuration Editor opens in Question mode.

2.  Choose Search \> Find.

> The harness displays the Find Question dialog box.

![Find Question](../../images/i18NfindQuest.gif){longdesc="searchConfiguration.html"}

3.  Enter the search character string in the String field.

> See the String field description in [Search Criteria](#searchCriteria).

4.  Choose the search location from the Where drop down list.

> See the Where description in [Search Criteria](#searchCriteria).

5.  Choose any required search options.

> See the Options descriptions in [Search Criteria](#searchCriteria).

6.  Click the Find button to search for the character string.

> To repeat the search, either click the Find button or use the Search **\>** Find Next menu item
> from the Configuration Editor menu bar.

7.  Click the Close button to dismiss the dialog box.

[]{#searchCriteria}

## Search Criteria

The following table describes the search criteria used in the Find Question dialog box.

+-------------------------------------------------+-------------------------------------------------+
| Item                                            | Description                                     |
+=================================================+=================================================+
| []{#find}String                                 | Enter the character string that you are trying  |
|                                                 | to find.                                        |
+-------------------------------------------------+-------------------------------------------------+
| []{#search}Where                                | Choose where you want to search:                |
|                                                 |                                                 |
|                                                 | -   In titles                                   |
|                                                 | -   In text of questions                        |
|                                                 | -   In answers                                  |
|                                                 | -   Anywhere                                    |
+-------------------------------------------------+-------------------------------------------------+
| []{#case}Options: Consider case                 | Specifies that the search pattern match the     |
|                                                 | case of the characters in the Find text field.  |
+-------------------------------------------------+-------------------------------------------------+
| []{#words}Options: Whole words                  | Specifies that the search pattern only match    |
|                                                 | whole words from the Find text field.           |
+-------------------------------------------------+-------------------------------------------------+

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
