---
hIndent: 0
title: Command-Line Interface
---

[]{#liteHarness}

# The Lite Harness

## Topics

-   [Overview](#Overview)
-   [Supported Commands](#Supported)
-   [Device Requirements](#Device)
-   [Installing](#Installing)
-   [Launching](#Launching)
-   [Creating a Configuration File](#Creating)
-   [Working with Reports and Test Results](#Working)

## []{#Overview}Overview

The lite harness (`jtlite.jar`) can run test suites on devices that have either of two limitations:

-   The Java implementation on the test device has no network access, as can be the case with an
    embedded device. The test harness, running on a laptop, cannot work in such a networkless
    environment because it uses an agent to run tests on the device, and the agent requires a
    network to communicate with the harness. By contrast, the lite harness runs entirely on the test
    device, executing tests there without an agent and without dependencies on networking APIs.
-   The Java implementation on which the harness runs supports a subset of Java SE APIs. To
    accommodate API limitations, the lite harness has no graphical user interface and supports only
    a subset of the test harness\'s CLI commands.

## []{#Supported}Supported Commands

The lite harness supports all test harness commands and options, except the following:

-   [`-newDesktop`](newDesktop.html)
-   [`-resume`](newDesktop.html)
-   [`-laf`](settingColors.html)
-   [`-onlineHelp`](commandHelp.html)
-   [`-type xml`](writeReports.html) (suboption of `-writeReport`)
-   [`-type cof`](writeReports.html) (suboption `-writeReport`)

## []{#Device}Device Requirements

-   The device on which the lite harness runs must have a Java runtime environment that implements
    at least the APIs defined as the JDK8 compact1 profile in
    <http://cr.openjdk.java.net/~mr/se/8/java-se-8-edr-spec.01.html#s8>. An example is the headless
    edition of Oracle Java SE Embedded 7, available from
    [http://www.oracle.com/technetwork/java/embedded/downloads/javase/index.html.](http://www.oracle.com/technetwork/java/embedded/downloads/javase/index.html)
-   You must be able to run the `java` launcher command (version 6 or later) on the test device. You
    can do this with a keyboard and monitor connected to the device or over a operating system-level
    network connection that supports a remote protocol such as `ssh`. Run `java -version` to verify
    the Java installation.
-   You must be able to transfer files to the device. Example transfer mechanisms include:
    -   A operating system-level network connection to the device that supports remote transfer
        commands such as `scp` or `sftp`.
    -   An SD card, USB card, or other portable file system medium.
    -   A tool that can burn files into non-volatile memory.
    -   A web browser that can download files from a web server that you control.
-   The device must have sufficient storage for the Java runtime, the lite harness implementation
    (`jtlite.jar`), the test suite to be executed, a work directory, and reports. The `jtlite.jar`
    file requires about 1MB of space. For Java runtime and test suite storage requirements, consult
    the corresponding documentation. Work directory and report sizes depend on the tests executed
    and reports generated.
-   The device must have sufficient dynamic memory to run the lite harness performing the commands
    you specify. Dynamic memory requirements are so situation-specific, they can only be determined
    by experimentation.

Although operating system-level networking support on the device is convenient for transfering files
and running the lite harness remotely, the lite harness does not have any dependencies on networking
APIs or services.

## []{#Installing}Installing

To install the lite harness on the test device, transfer a copy of
*javaTestInstallDir*`/lib/jtlite.jar` to the test device\'s file system.

You must also install the test suite on the device. Consult the test suite documentation for
instructions.

## []{#Launching}Launching

Assuming that the current directory contains the `jtlite.jar` you installed, and the device\'s
`java` launcher command can be invoked with its unqualified name, the general form of the lite
harness launch command is:

    > java -jar jtlite.jar  \
    -config yourConfig.jti \
    -workdir yourWorkDir \
    [more commands ...] \
    -runTests \
    -writeReport OutputReportDir

In this generic example, the `\` line continuation character visually separates the command line
arguments for clarity. This command line selects an existing configuration (file), an existing work
directory and asks the harness to run the tests and write the default report(s). The location of the
test suite is implied and will be determined from hints in the work directory. For more examples,
refer to [Extended Command-Line Examples](compoundExamples.html), substituting the lite harness\'s
JAR file name.

## []{#Creating}Creating a Configuration File

When you use the lite harness, you must specify the name of a configuration file on the command line
with `-config` *yourConfig*`.jti`. The easiest and most reliable way to create and edit a
configuration file is with the test harness graphical [configuration
editor](../confEdit/overview.html) running on a desktop or laptop computer. When you have created or
updated a configuration file, copy it to the test device\'s file system so you can specify it when
you launch the lite harness.

## []{#Working}Working with Reports and Test Results

It may not be efficient to analyze results on the test platform itself - users have the option of
relocating results to a more powerful platform. By copying work directories or reports from the test
device to your desktop or laptop computer, you can examine and manipulate them with the test harness
and other tools. For instance, you can use the [report converter](../mergeReports/mergeReports.html)
to merge reports or produce XML, and you can [browse test information](../browse/browsing.html) with
the test harness GUI.

 

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2013, Oracle and/or its affiliates. All rights reserved.
