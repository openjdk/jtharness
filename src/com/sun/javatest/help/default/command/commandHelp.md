---
hIndent: 1
title: Command-Line Help
---

# Command-Line Help

The harness allows you to display the command-line interface in the following forms:

-   All information
-   Topic information
-   Word search information

## Displaying All Information

To display all of the information in command-line help, include `-help all` at the end of the
command line.

[*\> jtharness*](aboutExamples.html) `-help all`

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness*.

## Displaying Topic Information

To display only the command-line help for specific topics, include `-help` and the name of the topic
at the end of the command line.

[*\> jtharness*](aboutExamples.html) `-help` *topic name*

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of \>
*harness*.

The following table lists the available command-line help topics.

  Topic           Function
  --------------- --------------------------------------------------------------------------------------------------------
  Desktop         Displays information about the command-line options for starting the harness graphical user interface.
  harness Agent   Displays information about the command-line options for the harness Agent.
  Batch Mode      Displays information about the command-line options for running tests in batch mode.
  Configuration   Displays information about the command-line options for setting up and changing a configuration.
  Environment     Displays information about the command-line options for adding values into harness environments.
  HTTP server     Displays information about the command-line options for the harness HTTP server.
  Options         Displays information about the command-line options accepted on the command line.
  Files           Displays information about the types of files accepted as command-line arguments.

## Display the List of Available Topics

To display the list of help topics provided by command-line help, include `-help`, `-usage`, or `-?`
at the end of the command line.

[*\> jtharness*](aboutExamples.html) ` -help`

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of \>
*harness*.

## Searching for Words and Phrases

The harness allows you to search the full command-line help for a specific word or phrase and then
displays only the information containing that word or phrase.

To display command-line help information containing a specific word or phrase, include `-help` and
the word or phrase at the end of the command line.

[*\> jtharness*](aboutExamples.html) `-help` *word or phrase*

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness*.

Example: [*\> jtharness*](aboutExamples.html) ` -help services` *test*

The console displays the following output:

    Services        Options for the JavaTest Harness test suite services management
        -startServices  Change the service startup policy, possible values are
                        [lazy, up_front, manually].

For complete details and examples, see the harness online help. You can access help directly from
the command line with `-onlineHelp` \<*word*\>, or you can start the harness and use the Help menu.
The online help is also available in PDF format in the harness documentation directory.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2012, Oracle and/or its affiliates. All rights reserved.
