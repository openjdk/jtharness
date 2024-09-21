---
hIndent: 3
title: Appearance Preferences
---

[]{#appearancePrefs}

# Appearance Preferences

Change the desktop appearance by opening the Appearance folder and setting the following options:

-   [Tool Tip Options](#appearancePrefs.tooltips)
-   [Shutdown Options](#appearancePrefs.shutdown)

![Appearance preferences dialog
box](../../images/JT4appearancePrefs.gif){longdesc="appearancePrefs.html"}

See [GUI Layout](desktopStyles.html) for a detailed description of the tabbed interface.

[]{#appearancePrefs.tooltips}

## Tool Tip Options

You can set the tool tip options from the Appearance category of the Preferences dialog box.

The Tool Tips area contains combo boxes and a check box that you can use to specify how tool tips
function in the desktop. The following table describes the available tool tip options.

  Option     Description
  ---------- ----------------------------------------------------------------------------------------------
  Enabled    Use the check-box to enable or disable tool tips for the GUI.
  Delay      Use the combo box to select the delay interval before displaying tool tips.
  Duration   Use the combo box to select how long a tooltip will stay on the screen before hiding itself.

[]{#appearancePrefs.shutdown}

## Shutdown Options

Check the **Save Desktop State on Exit** option to save your current desktop for use in a future
test session. This default is on.

When you exit this option saves the current view filter information, recent work directories, and
recent configuration information. If a desktop file exists, it will remain.

If you uncheck Save Desktop State on Exit and close the harness, the current view filter information
is not saved. The next time you open a test session the harness opens the Quick Start Wizard (if the
test suite author has enabled it) or an empty harness tool.

 

[]{#appearancePrefs.restore}

Check **Restore Tools State on Start** to restore saved desktop settings. The default is on. If this
option is not checked, the recent work directories and configuration information are restored but
the tools are not.

If Restore Tools is set **on** and Save Desktop State on Exit is set **off**, the harness will
always start with one tools set (although **-newdesktop** can be used to remove all data).

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2014, Oracle and/or its affiliates. All rights reserved.
