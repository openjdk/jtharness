---
lang: en
title: JT Harness Release Notes - Version BUILD_VERSION
---

[]{#_top}

:::: BodyStyle
# Release Notes

## JT Harness Binary Distribution

Version BUILD_VERSION\
BUILD_DATE

----------------------------------------------------------------------------------------------------

[Skip TOC ![skip TOC](doc/shared/downicon.gif)](#_maincontent)

## Table of Contents

::: embeddedtoc
[Introduction](#introduction)

[Layout](#layout)

[Tutorial](#tutorial)

[Requirements](#requirements)

[Run JT Harness](#run)

[Important Security Note](#secnote)

[About This Release](#since413)

[Security Exceptions With Java 7 on Windows](#secjava7)

[General Bugs Fixed in this Release](#fixed_general)

[Fixed Bugs of Interest to Test Suite Architects](#fixed_arch)

[Known Bugs in this Release](#known_bugs)

[Usage Notes](#usage_notes)
:::

[]{#introduction}[]{#_maincontent}

## Introduction

JT harness is based on Oracle\'s JavaTest harness. The JT harness is a general purpose,
fully-featured, flexible, and configurable test harness very well suited for most types of unit
testing. Originally developed as a test harness to run TCK test suites, it has since evolved into a
general purpose test platform.

The JT harness:

-   Is designed to configure, sequence, and run test suites that consist of many (100,000 or more)
    discrete, independent tests. It is especially good at testing APIs and compilers.
-   Can be used to run tests on all of the Java platforms, from the Java Card platform, to the Java
    Platform, Enterprise Edition (\"Java EE\").
-   Enables you to create test suites that are self-contained products that customers can easily
    configure and run.

We encourage you to try JT harness, participate in the
[community](http://java.net/projects/jtharness), and contribute to further development.

JT harness BUILD_VERSION is a feature release that adds selected functionality as well as fixing
existing issues in many areas. For a detailed report, see [Bugs Fixed in Release
BUILD_VERSION](#fixed_general). This binary release contains a built version of the JT harness
(`lib/javatest.jar`) and a tutorial that introduces you to the JT harness graphical user interface.

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#layout}\

## Layout

The JT harness binary distribution installation directory is laid out as follows:

  --------------------- --------------------------------------------------------------------------------------------------------
  `legal/`              Contains the licenses and Oracle copyrights that apply to this open source product.
  `lib`                 Contains the JT harness binary `javatest.jar`, the JUnit binary `jt-junit.jar`, and support libraries.
  `doc/`                Contains documentation that accompanies the JT harness product.
  `examples/tutorial`   Contains the JT harness tutorial. (Only present if examples bundle is installed. See below.)
  --------------------- --------------------------------------------------------------------------------------------------------

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#tutorial}\

## Tutorial

The JT harness tutorial includes an HTML tutorial that uses an example API and testsuite to
introduce you to the JT harness graphical user interface. Follow these steps to run the tutorial:

1.  Download the `jtharness-examples.zip` bundle if you have not already done so.
2.  Unpack the bundle at the same location as the main `jtharness.zip` - it is designed to be
    unpacked right on top of it.
3.  Make `examples/tutorial` your current directory.
4.  Open `tutorial.html` in your web browser and follow the step-by-step directions.

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#requirements}\

## Requirements

The JT harness requires the technologies listed in the following table.

  Technology                Where to Get It                                                       Notes
  ------------------------- --------------------------------------------------------------------- -------------------------------------------------------------------------------------------
  JDK™ version 7 or later   <http://www.oracle.com/technetwork/java/javase/overview/index.html>   See [Security Exceptions With Java 7 on Windows](#secjava7) for known issues with Java 7.
  ------------------------- --------------------------------------------------------------------- -------------------------------------------------------------------------------------------

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#run}\

## Run JT Harness 

Run the JT harness using the following command:

**`% java -jar javatest.jar`**

**Note:** Typically, a user executes JT harness in the context of a test suite. Read the tutorial
documentation for instructions about how to execute JT harness with the tutorial example test suite.

[]{#secnote}

## Important Security Note

NOTE: Be sure to read the Important Security Information chapter in the online help.

### Removing Optional Files

In principle, every file you install poses a potential security risk. After installing the harness,
you can reduce risk by removing files you do not need.

To run the harness, you must minimally have one of two sets of files:

1.  `lib/javatest.jar`: This archive is the test harness. It requires the file `lib/jh.jar` to
    display online help.
2.  `lib/jtlite.jar`: This archive is the lite harness.

You can delete the following installed files and directories if you do not want to use them:

-   `examples/`. This directory contains (in the `tutorials/` subdirectory) the JavaTest tutorial
    (`tutorial.html`) and related files. \[#tutorial\] describes how to run the tutorial. You can
    delete this directory.
-   `lib/`.
    -   You can delete `jt-unit.jar` if you do not want to run Junit tests with the harness. You can
        learn about Junit at <http://junit.sourceforge.net/>.
    -   You can delete the harness version you do not use, either `jtlite.jar` or `javatest.jar`. If
        you delete `javatest.jar`, you can also delete `jh.jar`.

A test suite includes many additional files. Refer to your test suite's documentation to see which
files are optional.

### Restricting Access to Installed Files

Limiting access to installed harness files minimizes the chance of file corruption. You can restrict
acesss to installed harness files by setting their permissions to read-only for the user who runs
the harness. Write or execute permission is not required for any installed harness file. You can
deny all access to people who do not run the harness.


[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#since413}\

## About This Release

This section describes specific features and behaviors in the current release.

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#secjava7}

## Security Exceptions With Java 7 on Windows

When using Java 7 on Windows, the harness might experience problems executing particular parts of
the Java 7 APIs because, by default, the harness security manager denies access to certain
properties. The problem might manifest as one of the [Known Exceptions](#knownexcept) reproduced
below (see [java.io.IOException](#knownexceptio) and [Socket Exception Error](#knownexceptsocket)).

There are ways to workaround the security exceptions:

1.  **Set the `javatest.security.allowPropertiesAccess` system property to `"true"` at startup.**
    The harness security manager (JavaTestSecurityManager) is automatically installed at startup,
    and its default settings deny access to the System Properties object. You can manually allow
    access to harness properties as follows:

    `java -Djavatest.security.allowPropertiesAccess=true ...`

2.  **Enable and disable property access programatically.** The following code temporarily allows
    properties access until after the code in the `try` block is executed. Beware of race conditions
    when using this code.

                SecurityManager sm = System.getSecurityManager();
                JavaTestSecurityManager jtSm = null;
                boolean prev = false;
                if (sm != null && sm instanceof JavaTestSecurityManager) {
                    jtSm = (JavaTestSecurityManager) sm;
                    prev = jtSm.setAllowPropertiesAccess(true);
                }
                try {
                    // execute exception causing actions here
                } finally {
                    if (jtSm != null) {
                        jtSm.setAllowPropertiesAccess(prev);
                    }
                }

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#knownexcept}

### Known Exceptions

The following exceptions might be seen if you use the harness with the Java 7 platform on the
Windows operating system. Use one of the above workarounds to avoid these exceptions.

[]{#knownexceptio}

#### java.io.IOException

    java.io.IOException: The requested operation cannot be performed on a file with
    a user-mapped section open
            at sun.nio.ch.FileDispatcherImpl.truncate0(Native Method)
            at sun.nio.ch.FileDispatcherImpl.truncate(FileDispatcherImpl.java:xxx)
            at sun.nio.ch.FileChannelImpl.truncate(FileChannelImpl.java:xxx)

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#knownexceptsocket}

#### Socket Creation Error

    java.lang.NoClassDefFoundError: Could not initialize class java.net.SocksSocketImpl
        at java.net.ServerSocket.setImpl(ServerSocket.java:xxx)
        at java.net.ServerSocket.<init>(ServerSocket.java:xxx)
        at java.net.ServerSocket.<init>(ServerSocket.java:xxx)
        at com.sun.jck.lib.multijvm.group.TaskManager.run(TaskManager.java:xxx)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:xxx)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:xxx)
        at java.lang.Thread.run(Thread.java:xxx)
    or with the following exception:
    java.lang.ExceptionInInitializerError
        at java.net.ServerSocket.setImpl(ServerSocket.java:xxx)
        at java.net.ServerSocket.<init>(ServerSocket.java:xxx)
        at java.net.ServerSocket.<init>(ServerSocket.java:xxx)
        at com.sun.jck.lib.multijvm.group.TaskManager.run(TaskManager.java:xxx)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:xxx)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:xxx)
        at java.lang.Thread.run(Thread.java:xxx
    )
    Caused by: java.lang.SecurityException: Action forbidden by JavaTest Harness: checkPropertiesAccess
        at com.sun.javatest.JavaTestSecurityManager.checkPropertiesAccess(JavaTestSecurityManager.java:xxx)
        at java.lang.System.getProperties(System.java:xxx)
        at java.net.PlainSocketImpl$1.run(PlainSocketImpl.java:xxx)
        at java.security.AccessController.doPrivileged(Native Method)
        at java.net.PlainSocketImpl.<clinit>(PlainSocketImpl.java:xxx)

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#fixed_general}\

### Bugs Fixed in This Release

This release of the harness contains fixes for the following defects:

  Bug ID             Description
  ------------------ -------------------------------------------------------------------------
  7056831            Unexpected exception from ParameterFilter.accepts()
  7107961, 7105486   Validate user-specified input XML files before accepting them.
  7072978            Poor formatting in exit confirmation dialog
  7001745            Fault while filtering tests for display may lead to incorrect rendering

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#fixed_arch}\

### Fixed Bugs Specifically of Interest to Test Suite Architects

The following bugs will be of interest to test suite architects.

  Bug ID         Description
  -------------- ----------------------------------------------------------------------------------------------------------------
  6909123        ResourceTable class deadlocks if lock name is duplicated.
  6994793        Source file path name normalization in test description.
  7024690        Memory leak in test suite properties dialog.
  7036583        Invalid File to URL conversion in TestSuite.open0.
  7060753        Need an ability to show big amount of information in ErrorQuestion.
  7085889        Fix multiple issues with Architect\'s guide - updates, formatting, etcetera.
  Multiple IDs   Protect against possible NPEs, initialization, synchrronization, invalid conversion and finalization problems.

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#known_bugs}

### Known Bugs in This Release

The following are known issues in this release.

  Bug ID    Description
  --------- ---------------------------------------------------------------------------------------------------------
  6434239   A long name for template/configuration files cannot be fully displayed.
  6446655   There\'s a keyboard navigation problem in the Tests To Run tree.
  6451875   Log Viewer: Impossible to mark log text when live scrolling mode is ON.
  6478125   Some files remain locked after closing the working directory, preventing the deletion of the directory.
  6488302   Errors while opening write-protected working directory.
  6518334   Report Converter does not generate an error message for an existing report file.
  6518375   Resolving conflicts between two reports in the Report Converter is confusing.
  6543609   \"Waiting to lock test result cache\" is not interruptible.
  6675884   Tooltips in PropertiesQuestion are sometimes incorrect.
  6796286   Inconsistent style for editing numeric values in IntQuestion, FloatQuestion and PropertiesQuestion.

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#changes}

### Changes Since JT Harness, Version 3.2

The JT Harness, Version 4.1 release provided basic changes in the following areas:

-   Upgraded template support using `.jtm` file extension.
-   Upgraded reporting system including an XML report type.
-   New Report converter tool to merge or convert XML reports.
-   New Current Template view filter.
-   New command line option to write a configuration (`.jti` file).
-   New command line control of filter used for generating reports.
-   Updated rendering of some interview question types.
-   New log viewer system allows browsing of debugging and tracing information.
-   Additional capabilities in the Folder Pane test lists (a context menu and a second column of
    information)

Version 4.1.2 was a bug fix release.

Version 4.1.3 added support for test suites based on JUnit 3.8.x and 4.x and also addressed bug
fixes.

Version 4.1.4 was a bug fix release.

Version 4.2 was a bug fix release.

Version 4.2.1 was a maintenance release. It included changes to enable the Test Panel to update as a
test runs, and extended the Quick Pick execution to include Tests to Run settings in configurations.
This behavior is controlled by preference settings.

Version 4.3 included the following major features:

-   The tabbed window style is the only window mode. MDI and SDI window modes were removed.
-   GUI colors became partially configurable in the Preferences dialog. Other settings are still
    available via command line.
-   Service startup information was enhanced to show progress.
-   Saving of previous desktop is now disabled by default (was enabled previous to this release).
-   Usability was improved in the main test tree. Added selection and right-click actions to choose
    from a context menu.

Version 4.4 was a feature release. In addition to fixing bugs, it introduced Known Failures Lists
and refined the exclude list feature to support case sensitivity.

Version 4.4.1 was a bug fix release.

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

[]{#usage_notes}

### Usage Notes

This section discusses features that are working properly but have limitations due to factors JT
Harness can\'t control.

#### Printing Pages from the Online Help and Test Reports

Because of the method the Java SE platform uses to render fonts, when you print a document from the
harness user interface, the fonts in the printed documents are larger than they are when rendered on
the screen.

The JT Harness User\'s Guide has been provided in PDF form for your use in printing pages from the
documents. While the online help viewer supports printing operations, in some cases you may be
unable to use it to print a specific page. If this occurs, use the PDF version of the appropriate
User\'s Guide to print the page. Printing of non-HTML report types is not supported. Users are
informed of this when they attempt to print a non-HTML report type. Support will be added for this
in a future release.

[Top ![go to TOC](doc/shared/topicon.gif)](#top)

#### Changing Status Colors in the GUI

JT Harness allows you to specify the status colors used in the GUI. See the online help for detailed
information about changing the default status colors.

#### Agent Monitor Tool

The Agent Monitor tool may not apply to all test suites, even if the test suite uses an agent. See
your test suite documentation for detailed information about the agent that it uses and its use of
the Agent Monitor tool.

[Top ![go to TOC](doc/shared/topicon.gif)](#top)
::::

----------------------------------------------------------------------------------------------------

[Copyright](../../legal/copyright.txt) © 2011, 2022, Oracle and/or its affiliates. All rights
reserved. The JT Harness project is released under the [GNU General Public License Version 2
(GPLv2)](LICENSE).
