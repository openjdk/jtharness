---
hIndent: 2
title: Formatting Configuration Values for editJTI or -set
---

# Formatting Configuration Values for `editJTI` or `-set`

The following table identifies and describes the types of questions supported by the harness
configuration interview. as well as provides a description of the format required to set the value
in the command line.

+-------------------------------------------------+-------------------------------------------------+
| Question Type                                   | Description                                     |
+=================================================+=================================================+
| Choice Question                                 | This question is used to get a selection from a |
|                                                 | finite list of possible values. For example, in |
|                                                 | the question \"Which protocol would you use,\"  |
|                                                 | where the only possible responses are TCP or    |
|                                                 | UDP. These questions are usually displayed in   |
|                                                 | the Configuration Editor and the Template       |
|                                                 | Editor as radio buttons where you can only      |
|                                                 | select one button at a time.                    |
|                                                 |                                                 |
|                                                 | The following is an example of the format used  |
|                                                 | to set this configuration value in a command    |
|                                                 | line:\                                          |
|                                                 | `set` *My-testsuite*`.cipher 3DES`              |
|                                                 |                                                 |
|                                                 | The value supplied is case sensitive. This type |
|                                                 | of question appears in the Configuration Editor |
|                                                 | as a set of radio buttons or single-selection   |
|                                                 | list of choices.                                |
+-------------------------------------------------+-------------------------------------------------+
| File Question                                   | This question is used to represent a file path. |
|                                                 | It may be absolute or relative, depending on    |
|                                                 | the context of the question. See the            |
|                                                 | question\'s More Info for information about the |
|                                                 | requirements. The value used is generally a     |
|                                                 | platform specific path. The question may or may |
|                                                 | not check to see that the value is valid before |
|                                                 | it is accepted.                                 |
|                                                 |                                                 |
|                                                 | The following are examples of the format used   |
|                                                 | to set this configuration value in a command    |
|                                                 | line:\                                          |
|                                                 | `set` *My-testsuite.myfile*                     |
|                                                 | `c:`*\\foo\\bar*`.txt`                          |
|                                                 |                                                 |
|                                                 | `set` *My-testsuite.myfile /tmp/bar*`.txt`      |
+-------------------------------------------------+-------------------------------------------------+
| File List Question                              | If none of the file names have embedded spaces, |
|                                                 | you can use a space-separated list of file      |
|                                                 | names. If any of the file names in the list     |
|                                                 | have embedded spaces, use a newline character   |
|                                                 | to terminate or separate all of the filenames.  |
+-------------------------------------------------+-------------------------------------------------+
| Floating Point Question                         | This question is primarily used to enter        |
|                                                 | fractional numbers, but can also accept whole   |
|                                                 | numbers. It might be used to collect values     |
|                                                 | such as a timeout factor in seconds, where a    |
|                                                 | value similar to 1.5 might be entered. It       |
|                                                 | usually appears as a type-in field in both the  |
|                                                 | Configuration Editor and the Template Editor.   |
|                                                 | The question might be set to reject values      |
|                                                 | outside a specified range. See the More Info in |
|                                                 | the Configuration editor or the Template Editor |
|                                                 | for guidelines regarding the values required    |
|                                                 | for a specific question.                        |
|                                                 |                                                 |
|                                                 | The following is an example of the format used  |
|                                                 | to set this configuration value in a command    |
|                                                 | line:\                                          |
|                                                 | `set` *My-testsuite*`.delay 5.0`                |
|                                                 |                                                 |
|                                                 | The value is evaluated using the current locale |
|                                                 | (for example, in European locales, enter 5,0).  |
+-------------------------------------------------+-------------------------------------------------+
| Integer Question                                | These questions are commonly used to get port   |
|                                                 | numbers or to specify the number of times to do |
|                                                 | an action. The answers are always restricted to |
|                                                 | whole numbers and might have further            |
|                                                 | restrictions that prevent you from using        |
|                                                 | certain ranges of numbers (such as negative     |
|                                                 | numbers). You might also be restricted to using |
|                                                 | only a particular set of pre-determined         |
|                                                 | numbers. In the Configuration Editor and in the |
|                                                 | Template Editor, these usually appear as plain  |
|                                                 | type-in fields or may be a field which has up   |
|                                                 | and down (spinner) controls to select the       |
|                                                 | number.                                         |
|                                                 |                                                 |
|                                                 | The following is an example of the format used  |
|                                                 | to set this configuration value in a command    |
|                                                 | line:\                                          |
|                                                 | `set` *My-testsuite*`.port 5000`                |
|                                                 |                                                 |
|                                                 | Localized values can be used. For example,      |
|                                                 | 5,000 is acceptable in a US locale.             |
+-------------------------------------------------+-------------------------------------------------+
| IPAddress Question                              | The standard textual representation of the IP   |
|                                                 | Address, as defined by Internet Engineering     |
|                                                 | Task Force (IETF).                              |
|                                                 |                                                 |
|                                                 | A typical IPv4 address string would be          |
|                                                 | \"192.168.1.1\".                                |
+-------------------------------------------------+-------------------------------------------------+
| Multi Choice Question                           | This question is used when a selection of       |
|                                                 | choices from a finite set of possible values is |
|                                                 | required from the user. In the Configuration    |
|                                                 | Editor and Template Editor, this question type  |
|                                                 | resembles the Choice question in that it has a  |
|                                                 | list of choices with checkboxes. The difference |
|                                                 | between them is that in a Multi Choice          |
|                                                 | question, you can select more than one          |
|                                                 | checkbox.                                       |
|                                                 |                                                 |
|                                                 | The values that you use in the `set` command    |
|                                                 | must be separated by whitespace (newline, space |
|                                                 | or tab) and must be the absolute new settings   |
|                                                 | for the question. The values that you use are   |
|                                                 | absolute settings and cannot be based on the    |
|                                                 | default or previous value. You must enter the   |
|                                                 | full list of values that you want to turn on    |
|                                                 | (corresponding to the items checked in the      |
|                                                 | Configuration Editor or Template Editor         |
|                                                 | representation).                                |
+-------------------------------------------------+-------------------------------------------------+
| Property Question                               | This question enables you to change a grouped   |
|                                                 | set of property values for a test suite. In the |
|                                                 | Configuration Editor and Template Editor, this  |
|                                                 | question type enables the user to view multiple |
|                                                 | property settings and to change read-write      |
|                                                 | values. The user can also copy both key and     |
|                                                 | values to the clipboard. Value types supported  |
|                                                 | by this question include integer, float,        |
|                                                 | yes-no, file, and string. In the Configuration  |
|                                                 | Editor and Template Editor, the question can    |
|                                                 | provide suggestions to users for the integer,   |
|                                                 | float, file, and string value types that it     |
|                                                 | contains.                                       |
|                                                 |                                                 |
|                                                 | The following are examples of the format used   |
|                                                 | to set this configuration value in a command    |
|                                                 | line:\                                          |
|                                                 | `set` *question-key*                            |
|                                                 | *property-name*:*new-value*                     |
|                                                 |                                                 |
|                                                 | `set`                                           |
|                                                 | *question-key*=*property-name*:*new-value*      |
|                                                 |                                                 |
|                                                 | Each `set` statement for a Property Question    |
|                                                 | value must contain the question key, the        |
|                                                 | property name, and a new value. The use of an   |
|                                                 | equals sign or a space is determined by the     |
|                                                 | tool and the type of value that you are         |
|                                                 | changing. You cannot change multiple values     |
|                                                 | within a single set statement.                  |
+-------------------------------------------------+-------------------------------------------------+
| String Question                                 | This question contains a generic field that     |
|                                                 | enables you to enter a value. While this is     |
|                                                 | much less restrictive than the integer or the   |
|                                                 | file question, there might be some restrictions |
|                                                 | on the values that you can enter. In the most   |
|                                                 | restrictive cases, you can only use values that |
|                                                 | are predefined. The More Info displayed in the  |
|                                                 | Configuration Editor and the Template Editor    |
|                                                 | might indicate what constitutes a legal value   |
|                                                 | in this field. View the More Info for detailed  |
|                                                 | information about the values that you can enter |
|                                                 | in this field.                                  |
|                                                 |                                                 |
|                                                 | The following is an example of the format used  |
|                                                 | to set this configuration value in a command    |
|                                                 | line:\                                          |
|                                                 | `set`                                           |
|                                                 | *My-testsuite*`.url http://`*machine*`/`*item*  |
+-------------------------------------------------+-------------------------------------------------+
| String List Question                            | This question is used when multiple discrete    |
|                                                 | string values are required from the user. It is |
|                                                 | usually shown as a interface which allows you   |
|                                                 | to enter a string then add it to a list. The    |
|                                                 | list of strings provided in the `set` command   |
|                                                 | become the new absolute answer to the question, |
|                                                 | not appended values. See [Using Newlines Inside |
|                                                 | Strings](#newlines).                            |
+-------------------------------------------------+-------------------------------------------------+
| YesNo Question                                  | This question is used when either a positive or |
|                                                 | a negative response is needed from the user.    |
|                                                 |                                                 |
|                                                 | The following are examples of the format used   |
|                                                 | to set this configuration value in a command    |
|                                                 | line:\                                          |
|                                                 | `set` *My-testsuite*`.needStatus Yes`           |
|                                                 |                                                 |
|                                                 | `set` *My-testsuite*`.needStatus No`            |
|                                                 |                                                 |
|                                                 | The values are case sensitive and a lower-case  |
|                                                 | value of yes or no is not acceptable.           |
+-------------------------------------------------+-------------------------------------------------+

[]{#newlines}

## Using Newlines Inside Strings

When setting values of configuration questions in the command line, the internal command parser
accepts newlines inside strings if they are preceded by a backslash.

Depending on the shell you use, it might or might not be possible to enter this directly on the
command line.

If you want to set values with embedded newlines, create a harness batch command file, and put the
set commands (and any other commands) in that file. In the batch file, you can enter strings with
embedded escaped newlines, as in the following example:

`# switch on verbose mode for commands`\
`verbose:commands`\
`# open a jti file`\
`open /home/user1/tmp/idemo.11mar04.jti`\
`# set a list of files`\
`set demo.file.simpleFileList /tmp/aaa\`\
`/tmp/bbb\`\
`/tmp/ccc`\
`# set a list of strings`\
`set demo.stringList 111\`\
`2222\`\
`3333 `

On Solaris, using the Korn shell, you can simply put newline characters into strings.

Example:

`$JAVA \`\
`-jar image/lib/javatest.jar \`\
`-verbose:commands \`\
`-open /home/user1/tmp/idemo.11mar04.jti \`\
`-set demo.file.simpleFileList /tmp/aaa`\
`/tmp/bbb`\
`/tmp/ccc `

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
