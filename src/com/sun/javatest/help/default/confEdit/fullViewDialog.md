---
hIndent: 4
title: Question Mode
---

[]{#fullViewDialog}

# Question Mode

The Question Mode displays the complete configuration, allowing you to create a new configuration,
change the values in a configuration, or search for character and value strings in a configuration.

![Configuration Editor Question
mode](../../images/JT4configEditorIndexed.gif){longdesc="fullViewDialog.html"}

1.  [File Menu](#fullViewDialog.filemenu)
2.  [Bookmarks Menu](#fullViewDialog.markermenu)
3.  [Search Menu](#fullViewDialog.searchmenu)
4.  [View Menu](#fullViewDialog.viewmenu)
5.  [Help Menu](#fullViewDialog.helpmenu)
6.  [More Info Pane](#fullViewDialog.moreInfoPane) (can be hidden)
7.  [Question Pane](#fullViewDialog.questionPane)
8.  [Index Pane](#fullViewDialog.indexPane)

## Menus

In Question Mode, the Configuration Editor contains menus used to load, create, display, and change
a configuration.

[]{#fullViewDialog.filemenu}

### File Menu

The File menu contains items to open, save, and restore configuration files. The following table
describes the items in the Configuration Editor File menu.

+-------------------------------------------------+-------------------------------------------------+
| Menu Item                                       | Description                                     |
+=================================================+=================================================+
| Save                                            | Saves the current configuration.                |
|                                                 |                                                 |
|                                                 | Choose File **\>** Save at any time to save     |
|                                                 | your answers and position in the configuration  |
|                                                 | file. If the configuration is new, the editor   |
|                                                 | opens the file chooser for you to use in naming |
|                                                 | and saving the current configuration. If you do |
|                                                 | not provide the []{#jti}`.jti` extension when   |
|                                                 | you name the file, the editor adds the          |
|                                                 | extension when it saves the file.               |
+-------------------------------------------------+-------------------------------------------------+
| Save As                                         | Opens a dialog box that you can use to save a   |
|                                                 | configuration with a new name. The              |
|                                                 | Configuration Editor makes the saved            |
|                                                 | configuration the current configuration. If you |
|                                                 | do not provide the `.jti` extension when you    |
|                                                 | name the configuration file, the editor adds    |
|                                                 | the extension when it saves the file.           |
+-------------------------------------------------+-------------------------------------------------+
| Revert                                          | Discards any changes to the current             |
|                                                 | configuration and restores the last saved       |
|                                                 | version of configuration file.                  |
+-------------------------------------------------+-------------------------------------------------+
| New Configuration                               | Clears the current configuration and starts a   |
|                                                 | new configuration. See [Creating a              |
|                                                 | Configuration](createConfiguration.html) for a  |
|                                                 | detailed description.                           |
+-------------------------------------------------+-------------------------------------------------+
| Load Configuration                              | Opens an existing configuration file and makes  |
|                                                 | it the current configuration. See [Loading a    |
|                                                 | Configuration](loadConfiguration.html) for a    |
|                                                 | detailed description.                           |
+-------------------------------------------------+-------------------------------------------------+
| []{#fullViewDialog.history}Load Recent          | Displays a list of configuration files that     |
| Configuration                                   | have been opened in the Configuration Editor    |
|                                                 | window. Choose a configuration file from the    |
|                                                 | list to open it in the Configuration Editor     |
|                                                 | window.                                         |
+-------------------------------------------------+-------------------------------------------------+
| New Template                                    | Provides the option to save or clear the        |
|                                                 | current template before starting a new          |
|                                                 | template. See [Creating a                       |
|                                                 | Template](../admin/createTemplate.html) for a   |
|                                                 | detailed description.                           |
+-------------------------------------------------+-------------------------------------------------+
| Load Template                                   | Opens an existing template. See [Loading a      |
|                                                 | Template](../templates/loadTemplate.html) for a |
|                                                 | detailed description.                           |
+-------------------------------------------------+-------------------------------------------------+
| []{#questionModeTE.history} Load Recent         | Displays a list of templates that have been     |
| Template                                        | opened in the Template Editor. Choose a         |
|                                                 | template from the list to open it in the        |
|                                                 | Template Editor.                                |
+-------------------------------------------------+-------------------------------------------------+
| Close                                           | Closes the Configuration Editor window.         |
+-------------------------------------------------+-------------------------------------------------+

[]{#fullViewDialog.markermenu}

### Bookmarks Menu

The Bookmarks menu contains items to use bookmarks in the configuration. The following table
describes the items in the Bookmarks menu.

  Menu Item                               Description
  --------------------------------------- ------------------------------------------------------------------------------------------------------------------------------------
  Enable Bookmarks                        Enables and disables bookmarking in the configuration.
  Show Only Bookmarked Questions          Display only the marked questions or all questions.
  Mark Current Question                   Clears the bookmark from a selected question in the Configuration Editor window.
  Unmark Current Question                 Bookmarks the selected question in the Configuration Editor window. Enabled only if the selected question is not bookmarked.
  Clear Answer For Current Question       Clears the answer for a selected question in the Configuration Editor window. Enabled only if the selected question is bookmarked.
  Open Group                              Expands a selected set of questions in the Configuration Editor window.
  Clear Answers to Bookmarked Questions   Clear the values in all marked questions.
  Remove Bookmarks                        Remove all bookmarks from the configuration.

Using bookmarked questions enables the user to display only those configuration questions that must
be answered. See [Using Bookmarks in Configurations](setMarkers.html) for a description of how the
Bookmarks menu can be used to specify the questions that the Configuration Editor window displays.

[]{#fullViewDialog.searchmenu}

### Search Menu

Use the Search menu items to find the occurrence in a configuration of a specific character or value
string. When troubleshooting a test run, you can use the Search menu to quickly locate an answer
that needs to be changed. The following table describes the items in the Search menu.

  Menu Item   Description
  ----------- -----------------------------------------------------------------------------------------------
  Find        Opens a dialog box used to search the configuration for a specific character or value string.
  Find Next   Searches the configuration for the next occurrence of a specific character or value string.

See [Searching a Configuration](searchConfiguration.html) for a detailed description of how to
search for character and value strings in a configuration.

[]{#fullViewDialog.viewmenu}

### View Menu

Use the View menu to display the Configuration Editor window in Question Mode or in Quick Set Mode,
to hide or display the More Info pane, and to display question tag field at the bottom of each
question panel. The following table describes the items in the View menu.

Menu Item

Description

Question Mode

Displays the Configuration Editor window in Question Mode.

Quick Set Mode

Displays the Configuration Editor window in Quick Set Mode.

More Info

Displays and hides the More Info pane in the Configuration Editor window.

Question Tag

Displays and hides the Question Tag field in the Configuration Editor Question Pane.

Refresh

Updates the values and questions displayed in the Configuration Editor window.

[]{#fullViewDialog.helpmenu}

### Help Menu

Use the Help menu to display the online help for the Configuration Editor window and both editor
modes. The following table describes the items in the Help menu.

  Menu Item              Description
  ---------------------- -----------------------------------------------------------
  Configuration Editor   Displays online help for the Configuration Editor window.
  Template Editor        Displays online help for the Template Editor window.
  Question Mode          Displays online help for the Question Mode.
  Quick Set Mode         Displays online help for the Quick Set Mode.

[]{#fullViewDialog.indexPane}

### Index Pane

When completing a configuration, the index pane lists the titles of the questions you have answered,
are currently answering, or that the editor determines might need to be answered. The current
question is highlighted.

In completed configurations, the questions that are displayed in the index can be controlled by
setting bookmarks in the configuration. See [Bookmarks Menu](#fullViewDialog.markermenu).

![The following text is a note](../../images/hg_note.gif){longdesc="fullViewDialog.html"}\
The title is also displayed at the top of the question pane when you are answering a question.

Click on any question in the index list to make it the current question. Clicking on a question does
not cause the list to change. If you change an answer that alters the configuration options, the
Configuration Editor window updates the questions in the list to reflect the change in options. If a
previously answered question is no longer displayed in the index list, the Configuration Editor
saves its answer until you either save the configuration or change its value.

You can also use the buttons at the bottom of the Question pane to navigate through the
configuration file. See [Question Pane](#fullViewDialog.questionPane) for a description of the
navigation buttons.

[]{#fullViewDialog.questionPane}

### Question Pane

The Configuration Editor window displays configuration questions in the main text area of the
editor. You answer the questions using controls such as text boxes, radio buttons, or combo boxes
located beneath the question. After you answer each question, click ![The Configuration Editor Next
button](../../images/confED_nextbutton.gif){longdesc="fullViewDialog.html"}   at the bottom of the
panel to proceed to the next question.

The buttons at the bottom of the Question pane control the following functions:

-   **Back** - Returns to the previous question.
-   **Last** - Advances as far as possible through the configuration.
-   **Next** - Proceeds to the next question.
-   **Done** - Saves your answers as a configuration and closes the Configuration Editor window.

See [Creating a Configuration](createConfiguration.html) for information about using the Question
pane to create a configuration.

See [Editing a Configuration](editConfiguration.html) for information about using the Question pane
to edit the current configuration.

[]{#fullViewDialog.moreInfoPane}

### More Info Pane

To open and close the More Info pane, choose View **\>** More Info from the menu bar.

The More Info pane provides additional information about each question, including the following:

-   Background information about the question
-   Information about choosing an answer
-   Examples of answers

See [Keyboard access](../ui/keyboardAccess.html) for a description of how the keyboard can be used
to navigate the More Info pane.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2014, Oracle and/or its affiliates. All rights reserved.
