---
hIndent: 0
title: Important Security Information
---

# Important Security Information

It is important to note that the harness is fundamentally a developer system that is not
specifically designed to guard against malicious attacks. This section describes known harness
vulnerabilities that can be exploited by authorized or unauthorized users or others who have access
to the network where the harness runs. When installing and operating the harness, consider these
vulnerabilities and take action to mitigate potential threats.

Note: See the *Release Notes* for additional security information.

## []{#File}File Access Risks

The harness reads, writes, and deletes files and can therefore potentially expose or damage
information stored in files.

### []{#Path}Path Vulnerabilities

The harness and its utilities do not prevent users from accessing sensitive files that are
accessible from the computer on which the harness is running --- including attached/mounted remote
filesystems. A harness user can therefore directly or indirectly examine file contents, overwrite
contents, or delete them. To mitigate against accidental or intentional misuse of sensitive files,
consider setting file permissions to give the least feasible access to harness users.

There are no direct options in the harness for removing a file, directory, or folder, but the
harness can automatically remove files that it believes it owns and are corrupt or in need of
replacement. Automatic removal or replacement generally occurs only inside the harness preferences
metadata storage area (`~/.javatest` or `user_home\.javatest`) and inside work directory locations
directly designated by the user. 

There are at least two ways a user can indirectly give the harness access to an arbitrary file that
the harness might automatically remove or replace.

-   A configuration (`.jti`) file can contain a \"hint\" that enables the harness GUI or CLI to find
    what the harness *assumes* is a work directory.
-   A desktop file in the preferences metadata storage area can contain a path to what the harness
    *assumes* is a work directory. The harness GUI or CLI automatically opens the desktop file.

### []{#FileCreation}File Creation Permissions

The harness is intended to be run by users who are trusted to properly handle files they have access
to. The harness does not attempt to override any file permissions that are in place to protect files
or restrict the user.  Additionally, the harness creates files with whatever permissions are the
default for the user running the harness (on Unix systems, the `umask`, etc). If you want to
restrict access to files created by the harness, ensure that user default file creation permissions
are set accordingly.

## []{#Network}Network Access Risks

The harness and related components are intended to be run in a semi-trusted environment. Never
expose the harness or its utilities directly to the Internet, which provides a path for malicious
intrusion. The most secure environment is a standalone machine with no network access, if this is
feasible for the testing you are performing. However, because the test harness agent configuration
and some test suites require a network, a more realistic environment is a closed intranet, ideally
one that is physically isolated from organizational intranets and sensitive information.

### []{#Remote}Remote Agent Risks

The test harness includes the JavaTest agent, which is a remote execution framework. The agent
requires open communication ports on the JavaTest host computer and on the remote computer (device);
therefore you must ensure that both machines are protected from malicious attack. For the most
secure operation, connect the host and remote computers only to a protected intranet on a physically
isolated network.

### []{#Hostname}Host Name (DNS) Lookup Vulnerabilities

The harness sometimes uses Domain Name Services (DNS), or similar from your system, to transform a
host name into an IP address. Invalid information returned by this lookup might cause the harness to
connect to an address you did not intend. When possible, the harness GUI displays the returned IP
address. Users should verify that the address is appropriate, for example, lies within a range the
corresponds to your network. If you need assistance validating IP addresses, contact your network
administrator.

## []{#Command}Command Injection Vulnerability

As part of the normal process of harness configuration and testing, there are places where the user
is allowed to specify arbitrary commands (with parameters) to execute --- for example, to start a
test, to start a Java virtual machine being tested, or to start a service. The harness does not
screen these commands for malicious or accidental usage. The harness offers no protection from the
effects of any command initiated by a user or a test. Be sure that users are trained and trusted to
specify proper commands properly, and to verify that commands issued by test suites are harmless. An
incorrect or malicious command can cause serious damage.

The harness does not attempt to run commands with higher privileges than that of the current user or
the privileges inherited when the new command/process executes.

 

----------------------------------------------------------------------------------------------------

[Copyright](copyright.html) © 2013, Oracle and/or its affiliates. All rights reserved.
