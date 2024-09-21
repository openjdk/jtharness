---
hIndent: 1
title: Moving Test Reports
---

[]{#editConfig}

# []{#moveReport}Moving Test Reports

Test reports contain relative and fixed links to files that break when they are moved. To prevent
this, the harness provides an `EditLinks` command-line utility in the main harness JAR file,
`javatest.jar`, for you to use when moving test reports.

The `EditLinks` utility checks all files with names ending in `.html` for HTML links beginning with
file names you specified in the `EditLinks` command. These links are rewritten using the
corresponding replacement name from the `EditLinks` command and are copied to the new location.
`EditLinks` copies all other files to the new location without change.

## Format of the `EditLinks` Command

Example:\
`java -classpath` \[*jt_dir*`/lib/`\] `javatest.jar com.sun.javatest.EditLinks` *OPTIONS* *file\...*

**OPTIONS**
:   The available *OPTIONS* are as follows:

    `-e` *oldPrefix* *newPrefix*
    :   Any links that begin with the string *oldPrefix* are rewritten to begin with *newPrefix*.
        Note that only the target of the link is rewritten, and not the presentation text. The edit
        is effectively transparent when the file is viewed in a browser. Multiple `-e` options can
        be given. When editing a file, the options are checked in the order they are given.\
        For example, if the argument\
        `-e /work/ /java/jck-dev/scratch/12Jun00/jck-lab3/`\
        is used on a file that contains the segment\
        `<a href="/work/api/java_lang/results.jtr">/work/api/java_lang/results.jtr</a>`\
        , the following text shown in bold will match.\
        `<a href="`**`/work/`**`api/java_lang/results.jtr">/work/api/java_lang/results.jtr</a>`\
        The resulting new file will contain the following text:\
        `<a href="/java/jck-dev/scratch/12Jun00/jck-lab3/api/java_lang/results.jtr">/work/api/java_lang/results.jtr</a>`

    `-ignore` *file*

    :   When scanning directories, ignore any entries named *file*. Multiple `-ignore ` may be
        given.

        For example, `-ignore SCCS` causes any directories named SCCS to be ignored.

    `-o` *file*
    :   Specifies the output file or directory. The output may only be a file if the input is a
        single file; otherwise, the output should be a directory into which the edited copies of the
        input files will be placed.

    *file\...*
    :   Specifies the input files to be edited. If any of the specified files are directories, they
        will be recursively copied to the output directory and any HTML files within them updated.

**RETURN CODE**
:   The following table describes the return codes that the program displays when it exits.
      Code   Description
      ------ ---------------------------------------------------
      0      The copy was successful.
      1      A problem exists with the command-line arguments.
      2      A problem exists with the command-line arguments.
      3      An error occurred while performing the copy.

## Detailed Example of `EditLinks` Command

In the following example, *test12-dir.wd* and *myworkdir.wd* represent file names that might exist
on your system. Win32 users must also replace the UNIX system file separators with Windows file
separators (/) to run these examples.

`java -cp` \[*jt-dir*`/lib/`\]
`javatest.jar com.sun.javatest.EditLinks -e /work/ /java/jck-dev/scratch/12Jun00/jck-lab3/ -o test12_dir.wd`
*myworkdir.wd*

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2010, Oracle and/or its affiliates. All rights reserved.
