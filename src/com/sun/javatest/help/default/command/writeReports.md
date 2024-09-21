---
hIndent: 1
title: Writing Reports
---

[]{#writeReports}

# Writing Reports With `writeReport`

Use the `-writeReport` command with the `-type` and `-filter` options in the command line as a
separate command or as part of a series of task commands (such as run tests). Use a web browser to
view the reports.

Because the harness executes commands in their command-line sequence, you must identify the work
directory before the `-writeReport` command and provide the report directory as an option at the end
of the command:

[*\> jtharness*](aboutExamples.html) \... `-workdir` *my-work-dir* `-writeReport` `-type`\
*report-type* `-filter` *report-filter* *my-report-dir*

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness* in the example.

See [Command-Line Overview](commandLine.html) for a detailed description of the command line
structure.

[]{#writeReportType}

## Using the `-type` Option

Include the `-type` option and a report-type argument in the command line to specify the format of
the generated report. There is a direct relationship between the names of the directories in the
report directories and the report type names used in the command, as shown in the following table:

Report

Type

HTML Report

`html`

Plain Text Report

`txt`

XML Report

`xml`

COF Report

`cof`

When the `-type` option is not used, the harness uses the default report types last used in the GUI,
or `html` and `txt` if a type was not previously set in the GUI.

The harness provides a set of standard format types (`html`, `txt`, `xml` and `cof`) that you can
use. In addition to the standard arguments, your test suite might provide additional custom formats.
If you are unsure of the additional formats provided by your test suite, select Report \> Create New
Report from the GUI to display the list of available report formats.

For more on the standard report formats, see [Creating
Reports](../../default/report/newReports.html#CreateTestReport) in the *Graphical User Interface
User\'s Guide*.

## Using the `-filter` Option

When the `-filter` option is not used, the harness uses the default setting of `currentConfig`.
Include the `-filter` and a filter option (`lastRun`, `currentConfig`, or `allTests`) in the command
line to specify the filter used to select the test results that are reported.

Use the `lastRun` filter option (corresponds to Last Test Run in the GUI) to select test results
status for all folders and tests included in the last test run even if you have exited the harness
since the last test run.

Use the `currentConfig` filter option (corresponds to Current Configuration in the GUI) to select
the folders and test results status specified by the current configuration.

Use the `allTests` filter option (corresponds to All Tests in the GUI) to select test results status
for all tests in the work directory, including any tests that were excluded from the last test run.

## Using the `-enableKFL` Option

This option requests that the harness attempt to generate the KFL section of the report(s).

By default, the harness in command line mode will pull this setting from Preferences, which is
stored on a per-user basis, and controlled by the GUI Preferences. Using this flag will ask the
reporting system to override this Preference to generate the report. Currently, there are no
sub-options to specify the detailed settings available in the GUI Preferences.

![The following text is a note](../../images/hg_note.gif){longdesc="writeReports.html"}\
The flag `-kfl` is also accepted, but usage of it is not recommended because it may be confused with
the top-level `-kfl` flag.

## Detailed Example of `writeReport` Command

In the following example, *my-work-dir* represents a work directory name that might exist on your
system, and two known failure lists are specified.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) -workdirectory *myworkdirectory*
`-config foo.jti -kfl foo.kfl -writeReport -enableKFL -type html -filter allTests `*myReportDir*

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2015, Oracle and/or its affiliates. All rights reserved.
