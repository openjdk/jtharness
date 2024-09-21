---
hIndent: 4
title: Question Mode
---

[]{#fullTemplateEditor}

# Question Mode

The Question Mode displays all questions in a test suite interview, allowing you to create a new
template, edit an existing template, or search a template for specific characters or text.

![Template Editor Question
mode](../../images/JT4configEditorIndexed.gif){longdesc="questionModeTE.html"}

1.  [File Menu](#questionModeTE.filemenu)
2.  [Bookmarks Menu](#questionModeTE.markermenu)
3.  [Search Menu](#questionModeTE.searchmenu)
4.  [View Menu](#questionModeTE.viewmenu)
5.  [Help Menu](#questionModeTE.helpmenu)
6.  [More Info Pane](#questionModeTE.moreInfoPane) (can be hidden)
7.  [Question Pane](#questionModeTE.questionPane)
8.  [Index Pane](#questionModeTE.indexPane)

## Menus

In Question Mode, the Template Editor contains menus used to load, view, search, create, and edit a
template.

[]{#questionModeTE.filemenu}

### File Menu

The File menu contains items to open, save, and restore template files. The following table
describes the items in the File menu.

+-------------------------------------------------+-------------------------------------------------+
| Menu Item                                       | Description                                     |
+=================================================+=================================================+
| Save                                            | Saves the current template.                     |
|                                                 |                                                 |
|                                                 | Choose File **\>** Save at any time to save     |
|                                                 | your answers and position in the template. If   |
|                                                 | the template is new, the editor opens the file  |
|                                                 | chooser for you to name and save the current    |
|                                                 | values. If you do not provide the               |
|                                                 | []{#jti}`.jtm` extension when you name the      |
|                                                 | file, the editor adds the extension when it     |
|                                                 | saves the template.                             |
+-------------------------------------------------+-------------------------------------------------+
| Save As                                         | Opens a dialog box that you can use to save a   |
|                                                 | template with a new name or change the location |
|                                                 | of the template. If you do not provide the      |
|                                                 | `.jtm` extension when you name the template,    |
|                                                 | the editor adds the extension when it saves the |
|                                                 | file.                                           |
+-------------------------------------------------+-------------------------------------------------+
| Revert                                          | Discards any changes to the current template    |
|                                                 | and restores the last saved version of the      |
|                                                 | template.                                       |
+-------------------------------------------------+-------------------------------------------------+
| New Configuration                               | Clears the current configuration and starts a   |
|                                                 | new configuration. See [Creating a              |
|                                                 | Confi                                           |
|                                                 | guration](../confEdit/createConfiguration.html) |
|                                                 | for a detailed description.                     |
+-------------------------------------------------+-------------------------------------------------+
| Load Configuration                              | Opens an existing configuration file and makes  |
|                                                 | it the current configuration. See [Loading a    |
|                                                 | Con                                             |
|                                                 | figuration](../confEdit/loadConfiguration.html) |
|                                                 | for a detailed description.                     |
+-------------------------------------------------+-------------------------------------------------+
| []{#fullViewDialog.history} Load Recent         | Displays a list of configuration files that     |
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
| Close                                           | Closes the Template Editor.                     |
+-------------------------------------------------+-------------------------------------------------+

[]{#questionModeTE.markermenu}

### Bookmarks Menu

The Bookmarks menu contains items used to manage bookmarks in the template. Using bookmarked
questions enables the user to display only those template questions that must be answered. See
[Setting Markers](../admin/setMarkersTE.html) for a description of how the Bookmarks menu can be
used to specify the questions that the Template Editor displays.

The following table describes the items in the Bookmarks menu.

  Menu Item                               Description
  --------------------------------------- ----------------------------------------------------------------------------------------------------------------------------------
  Enable Bookmarks                        Enables or disables bookmarking in the template. When bookmarking is disabled, the remaining bookmark menu items are grayed out.
  Show Only Bookmarked Questions          Displays only the bookmarked questions.
  Mark Current Question                   Sets the bookmark for a question selected in the index pane.
  Unmark Current Question                 Clears the bookmark from a question selected in the index pane. Enabled only if the selected question is bookmarked.
  Clear Answer For Current Question       Clears the answer for a selected question in the index pane.Enabled only if the selected question is bookmarked.
  Open Group                              Expands a selected set of questions in the index pane.
  Clear Answers to Bookmarked Questions   Clears the values in all bookmarked questions.
  Remove Bookmarks                        Removes all bookmarks from the Template.

[]{#questionModeTE.searchmenu}

### Search Menu

Use the Search menu items to find the occurrence in a Template of a specific character or value
string. When troubleshooting a test run, you can use the Search menu to quickly locate an answer
that needs to be changed. See [Searching the Template](../admin/searchTemplateTE.html) for a
detailed description of how to search for character and value strings in a Template.

The following table describes the items in the Search menu.

  Menu Item   Description
  ----------- ------------------------------------------------------------------------------------------
  Find        Opens a dialog box used to search the template for a specific character or value string.
  Find Next   Searches the template for the next occurrence of a specific character or value string.

[]{#questionModeTE.viewmenu}

### View Menu

Use the View menu to display the Template Editor in Question Mode or in Quick Set Mode, to hide or
display the More Info pane, and to display question tag field at the bottom of each question panel.
The following table describes the items in the View menu.

Menu Item

Description

Question Mode

Displays the Template Editor in Question Mode.

Quick Set Mode

Displays the Template Editor in Quick Set Mode.

More Info

Displays and hides the More Info pane in the Template Editor.

Question Tag

Displays and hides the Question Tag field in the Template Editor Question pane.

Refresh

Updates the values and questions displayed in the Template Editor.

[]{#questionModeTE.helpmenu}

### Help Menu

Use the Help menu to display the online help for the Template Editor and both editor modes. The
following table describes the items in the Help menu.

  Menu Item              Description
  ---------------------- -------------------------------------------------------
  Configuration Editor   Displays online help for the Configuration Editor.
  Template Editor        Displays online help for the Template Editor.
  Question Mode          Displays online documentation for the Question Mode.
  Quick Set Mode         Displays online documentation for the Quick Set Mode.

[]{#questionModeTE.indexPane}

### Index Pane

When completing a template, the index pane lists the titles of the questions you have answered, are
currently answering, and any questions that the editor can determine might need to be answered. The
current question is highlighted and its title is displayed at the top of the question pane.

The number of questions displayed in the configuration interview is controlled by setting bookmarks
in the template. See [Bookmarks Menu](#questionModeTE.markermenu) for procedures used to reduce the
number of questions displayed in the configuration interview.

Click on any question in the index list to make it the current question. Clicking on a question does
not cause the list to change. If you change an answer that alters the Template options, the Template
Editor updates the questions in the list to reflect the change in options. If a previously answered
question is no longer displayed in the index list, the Template Editor saves its answer until you
either save the Template or change its value.

You can also use the buttons at the bottom of the Question pane to navigate through the Template
file. See [Question Pane](#questionModeTE.questionPane) for a description of the navigation buttons.

[]{#questionModeTE.questionPane}

### Question Pane

The Template Editor displays template questions in the main text area of the editor. You answer the
questions using controls such as text boxes, radio buttons, or combo boxes located beneath the
question. After you answer each question, click ![The Template Editor Next
button](../../images/confED_nextbutton.gif){longdesc="questionModeTE.html"}   at the bottom of the
panel to proceed to the next question.

The buttons at the bottom of the Question pane control the following functions:

-   **Back** - Returns to the previous question.
-   **Last** - Advances as far as possible through the template.
-   **Next** - Proceeds to the next question.
-   **Done** - Saves your answers as a template and closes the Template Editor.

See [Creating a Template](../admin/createTemplate.html) for information about using the Question
pane to create a template.

See [Editing a Template](../admin/editTemplate.html) for information about using the Question pane
to edit the current template.

[]{#questionModeTE.moreInfoPane}

### More Info Pane

To open and close the More Info pane, choose View **\>** More Info from the menu bar.

The More Info pane provides additional information about each question, including the following:

-   Background information about the question
-   Information about choosing an answer
-   Examples of answers

See [Keyboard access](../ui/keyboardAccess.html) for a description of how the keyboard can be used
to navigate the More Info pane.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2006, 2011, Oracle and/or its affiliates. All rights reserved.
