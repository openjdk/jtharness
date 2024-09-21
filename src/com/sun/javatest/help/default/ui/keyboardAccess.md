---
hIndent: 1
title: Keyboard access
---

# Keyboard access

The harness uses standard Java programming language key bindings for keyboard traversal and access
of the GUI. See the *Java Look and Feel Design Guidelines* at `http://java.sun.com/products/jlf/`
for a detailed description of the standard key bindings for keyboard traversal and access of the
GUI.

The window or component must have keyboard focus before you can use keyboard navigation, activation,
or shortcuts. Keyboard navigation, activation, and shortcut operations are described in the
following topics.

[]{#focus}

## Keyboard Focus

When a component has focus, it is generally displayed with a colored border or changes color.
However, some components in the GUI cannot be displayed with a focus indicator. In this case, you
must continue to use the keyboard to traverse the GUI until focus is displayed.

[]{#shortcuts}

## Keyboard Shortcuts

Keyboard shortcuts perform both navigation and activation in the same action. The following table
lists the keys that are used to access menus and online help.

  Activation Keys   GUI Action
  ----------------- -----------------------------------------------------------------------------
  F1                Activates the online help.
  F10               Activates the File menu.
  Shift F10         Activates the pop-up menu if focus is on a folder or test in the test tree.

[]{#mnemonics}

### Hot Key Shortcuts

The harness provides shortcuts throughout the application for accessing menu titles, menu items,
text fields, checkboxes, radio buttons, and command buttons.

Shortcut keys are identified in the following ways:

-   **Text at the end of tool tips -** For example, Start Running Tests Alt-S
-   **Underlined letters in menus and text buttons** **-** For example, **[F]{.underline}ile**
-   **Underlined letters in labeled fields -** For example, [R]{.underline}esponse:

[]{#navigation}

## Keyboard Navigation

Keyboard navigation enables you to move keyboard focus from one GUI component to another by using
the keyboard without activating the component. The following table lists the keys used for keyboard
navigation.

  ---------------------------------------------------------------------------------------------------
  Navigation Key                                    GUI Action
  ------------------------------------------------- -------------------------------------------------
  Tab                                               Navigates to the next focusable component in the
                                                    GUI. The tab traversal order is generally left to
                                                    right and top to bottom.

  Shift-Tab                                         Navigates back to the next focusable component.

  Control-Tab                                       Navigates to the next focusable component even if
                                                    the current component accepts the Tab key as
                                                    input (such as a text area).

  Control-Shift Tab                                 Navigates back to the next focusable component
                                                    even if the current component accepts the Tab key
                                                    as input (such as a text area).

  Left arrow                                        Moves keyboard focus left one character or
                                                    component.\
                                                    If focus is in the test tree, focus moves up the
                                                    tree and closes the node.\
                                                    If focus is on the splitter bar (F8 moves focus
                                                    to the splitter bar), it moves the splitter bar
                                                    left.

  Right arrow                                       Moves keyboard focus right one character or
                                                    component.\
                                                    If focus is in the test tree, focus moves
                                                    sequentially down the tree, opening the node and
                                                    traversing all tests in a folder.\
                                                    If focus is on the splitter bar (F8 moves focus
                                                    to the splitter bar), it moves the splitter bar
                                                    right.

  Up arrow                                          Moves keyboard focus up one line or component.\
                                                    If focus is in the test tree, focus moves
                                                    sequentially up the tree but does not open any
                                                    closed folders.\
                                                    If focus is on the splitter bar (F8 moves focus
                                                    to the splitter bar), it moves the splitter bar
                                                    left.

  Down arrow                                        Moves keyboard focus down one line or component.\
                                                    If focus is in the test tree, focus moves
                                                    sequentially down the tree but does not open any
                                                    closed folders.\
                                                    If focus is on the splitter bar (F8 moves focus
                                                    to the splitter bar), it moves the splitter bar
                                                    right.

  Page Up                                           Navigates up one pane of information within a
                                                    scroll pane.

  Page Down                                         Navigates down one pane of information within a
                                                    scroll pane.

  Home                                              Moves to the beginning of the data. In a table,
                                                    moves to the beginning of a row. If focus is in
                                                    the test tree, moves to the top of the tree.

  End                                               Moves to the end of the data. In a table, moves
                                                    to the last cell in a row. If focus is in the
                                                    test tree, moves to the bottom of the tree.

  Control-F1                                        Displays the tool tip information for the GUI
                                                    object that has focus. Can be used to determine
                                                    which GUI object has focus.

  F6                                                Shifts focus between left and right panes.

  F8                                                Shifts focus to the splitter bar if focus is in
                                                    the left or right pane.

  Control-T                                         Shifts focus to the next link in a topic or in a
                                                    report.

  Control-Shift-T                                   Shifts focus to the previous link in a topic or
                                                    in a report.
  ---------------------------------------------------------------------------------------------------

### Navigation in Hyperlinked Text

Navigating hyperlinks in text areas such as the More Info pane and report viewer requires that you
establish focus in the pane itself. After you have established focus inside the pane, use the
keyboard navigation keys listed in Keyboard Navigation to navigate in the pane. Because some
components in the GUI cannot display focus, you have to use the keyboard to traverse the GUI until
you can determine that focus is established inside the pane.

After focus is established in the pane, use Control-T and Shift-Control-T to navigate to the next
and previous link in the document. Use Control and Space bar to select (follow) the hyperlink. See
[Keyboard Activation](#activation) for a list of keys used for keyboard activation.

### Navigation in Folder Pane Status Tabs

Navigating in the folder pane test status tabs requires that you establish focus in the pane itself.

After focus is established in the pane, use the Arrow Up and Arrow Down keys listed in Keyboard
Navigation to navigate in the pane. When only a single entry is present in the folder pane, you must
use the Home or End key to select the item.

Because some components in the GUI cannot be displayed with focus indicators, you may have to use
the keyboard to traverse the GUI until you can determine that focus is established inside the folder
pane.

Use the Return or Enter key to navigate to the selected item. See [Keyboard Activation](#activation)
for a list of keys used for keyboard activation.

### Navigation in the Test Tree

Navigating in a test tree requires that you establish focus in the test tree itself.

After focus is established in the pane, use Arrow Up, Arrow Down, Arrow Left, and Arrow Right keys
listed in Keyboard Navigation to navigate in the pane.

[]{#activation}

## Keyboard Activation

After navigating to a component, you can then use the keyboard to activate the component. The
following table lists the key that are used to activate GUI components.

  ---------------------------------------------------------------------------------------------------
  Activation Keys                                   GUI Action
  ------------------------------------------------- -------------------------------------------------
  Enter or Return                                   Activates the default command button.

  Escape                                            Dismisses a menu or dialog box without changes.

  Space Bar                                         Activates the tool bar button that has keyboard
                                                    focus.

  Shift-Space Bar                                   Extends the selection of items in a list.

  Control-Space Bar                                 If the item with focus is in a list, it toggles
                                                    the selection state of the item without affecting
                                                    any other list selections.\
                                                    If the item is a link, it follows the link.
  ---------------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2003, 2010, Oracle and/or its affiliates. All rights reserved.
