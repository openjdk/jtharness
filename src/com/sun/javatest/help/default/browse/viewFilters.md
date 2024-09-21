---
hIndent: 3
title: View Filters
---

[]{#viewFilter}

# View Filters

The harness provides a special view filtering facility that enables you to filter the status (the
colors and counters) of the folders and tests displayed in the Test Manager. View filters function
independently from the run filtering set in the configuration editor window.

See [Editing a Configuration](../confEdit/editConfiguration.html) for detailed information about
specifying the tests that are run.

![The following text is a note](../../images/hg_note.gif){longdesc="viewFilters.html"}\
You also use view filters when generating reports. See [Creating Reports](../report/newReports.html)
for a description of using view filters when generating reports.

The harness provides four Test Manager view filters:

-   [Last Test Run](../ui/lastRun.html)
-   [Current Configuration](../ui/currentConfiguration.html)
-   [Current Template](../ui/currentTemplate.html)
-   [All Tests](../ui/allTests.html) (default)
-   [Custom](../ui/customFilters.html)

An additional view filter, such as a Certification filter, can be added by the test suite. Refer to
your test suite documentation for detailed descriptions of any additional filters displayed in the
list of view filters.

To use a view filter, either choose View \> Filters from the menu bar or choose a view filter from
the tool bar. See [Custom View Filter](../ui/customFilters.html) for a description of how to create
a custom view filter.

If you checked the Save Desktop State on Exit option in the Preferences dialog box, the current view
filter information is saved when you exit.

Current view filter information is **not** saved if you:

-   unchecked the Save Desktop State on Exit option in the Preferences dialog box
-   provided test suite or work directory information on the command line when starting the harness
-   used the `-newDesktop` on the command line when starting the harness

[See Shutdown Options.](../ui/appearancePrefs.html#appearancePrefs.shutdown)

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
