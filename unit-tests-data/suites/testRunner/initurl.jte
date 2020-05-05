#
# $Id$
#
#
# Copyright (c) 2001, 2020, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the LICENSE file that accompanied this code.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

#Environment file for "initurl" test suite

StdTestScript=com.sun.javatest.lib.StdTestScript
SimpTestScript=javasoft.sqe.javatest.lib.SimpTestScript

ExecStdTestSameJVMCmd=com.sun.javatest.lib.ExecStdTestSameJVMCmd
ExecStdTestOtherJVMCmd=com.sun.javatest.lib.ExecStdTestOtherJVMCmd
ProcessCommand=com.sun.javatest.lib.ProcessCommand
SlaveCommand=javasoft.sqe.javatest.lib.SlaveCommand

SerialPortAgentCommand=com.sun.javatest.agent.SerialPortAgentCommand

# default script setting for all envs
script=com.sun.javatest.lib.KeywordScript

#env.initurl.finder=com.sun.javatest.finder.HTMLTestFinder

env.initurl.description=Environment to exercise the tests in the "initurl" test suite
env.initurl.menu=true
env.initurl.script.compile_positive=$StdTestScript -compile
env.initurl.script.compile_serial_positive=$StdTestScript -compile -compileIndividually
env.initurl.script.compile_negative=$StdTestScript -compile -expectFail
env.initurl.script.compile_serial_negative=$StdTestScript -compile -compileIndividually -expectFail
env.initurl.script.execute_positive=$StdTestScript -execute
env.initurl.script.execute_simple_positive=$SimpTestScript -execute
env.initurl.script.execute_negative=$StdTestScript -execute  -expectFail
env.initurl.script.compile_execute_positive=$StdTestScript -compile -execute
env.initurl.script.compile_serial_execute_positive=$StdTestScript -compile -execute -compileIndividually
env.initurl.script.compile_execute_simple_positive=$SimpTestScript -compile -execute
env.initurl.script.compile_serial_execute_simple_positive=$SimpTestScript -compile -execute -compileIndividually
#
env.initurl.command.compile.java=$ProcessCommand CLASSPATH=$javatestClassDir$:$testClassDir $JAVAC -d $testClassDir $javacFlags $testSource
env.initurl.command.execute=$ExecStdTestOtherJVMCmd DISPLAY=$DISPLAY CLASSPATH=$javatestClassDir$:$testClassDir $JAVA $testExecuteClass $testExecuteArgs
#
env.initurl.javaHome=${java.home}

env.binary.inherits=initurl
env.binary.finder=com.sun.javatest.finder.BinaryTestFinder testsuite.jtd

env.initurl-reverse.description=Uses ReverseTestFinder to locate tests
env.initurl-reverse.inherits=initurl
env.initurl-reverse.finder=com.sun.javatest.finder.ReverseTestFinder com.sun.javatest.finder.HTMLTestFinder

env.initurl-active-otherJVM.inherits=initurl
env.initurl-active-otherJVM.description=Environment to exercise the tests in the "initurl" test suite, using a slave in otherJVM mode
#
env.initurl-active-otherJVM.command.compile.java=$SlaveCommand -tag $testPath $ProcessCommand CLASSPATH=$javatestClassDir$:$testClassDir $javaHome$/bin$/javac -d $testClassDir $javacFlags $testSource
env.initurl-active-otherJVM.command.execute=$SlaveCommand -tag $testPath $ExecStdTestOtherJVMCmd DISPLAY=$DISPLAY CLASSPATH=$javatestClassDir$:$testClassDir $javaHome$/bin$/java $testExecuteClass $testExecuteArgs


env.initurl-active-sameJVM.inherits=initurl
env.initurl-active-sameJVM.description=Environment to exercise the tests in the "initurl" test suite, using a slave in otherJVM mode
#
env.initurl-active-sameJVM.command.compile.java=$ProcessCommand CLASSPATH=$javatestClassDir$:$testClassDir $javaHome$/bin$/javac -d $testClassDir $javacFlags $testSource
env.initurl-active-sameJVM.command.execute=$SlaveCommand -tag $testPath -cp $javatestClassDir$:$testClassDir $ExecStdTestSameJVMCmd $testExecuteClass $testExecuteArgs



env.initurl-passive-otherJVM.inherits=initurl
env.initurl-passive-otherJVM.description=Environment to exercise the tests in the "initurl" test suite, using a slave in otherJVM mode
#
env.initurl-passive-otherJVM.command.compile.java=$SlaveCommand -host $localHost -tag $testPath $ProcessCommand CLASSPATH=$javatestClassDir$:$testClassDir $javaHome$/bin$/javac -d $testClassDir $javacFlags $testSource
env.initurl-passive-otherJVM.command.execute=$SlaveCommand -host $localHost -tag $testPath $ExecStdTestOtherJVMCmd DISPLAY=$DISPLAY CLASSPATH=$javatestClassDir$:$testClassDir $javaHome$/bin$/java $testExecuteClass $testExecuteArgs

env.initurl-serial-devterma.inherits=initurl
env.initurl-serial-devterma.description=Environment to exercise the tests in the "initurl" test suite, using a slave in otherJVM mode
#
env.initurl-serial-devterma.command.compile.java=$ProcessCommand CLASSPATH=$javatestClassDir$:$testClassDir $javaHome$/bin$/javac -d $testClassDir $javacFlags $testSource
env.initurl-serial-devterma.command.execute=$SerialPortAgentCommand -tag $testPath -cp $javatestClassDir$:$testClassDir /dev/term/a $ExecStdTestSameJVMCmd $testExecuteClass $testExecuteArgs

env.initurl-serial-com1.inherits=initurl
env.initurl-serial-com1.description=Environment to exercise the tests in the "initurl" test suite, using a slave in otherJVM mode
#
env.initurl-serial-com1.command.compile.java=$ProcessCommand CLASSPATH=$javatestClassDir$:$testClassDir $javaHome$/bin$/javac -d $testClassDir $javacFlags $testSource
env.initurl-serial-com1.command.execute=$SerialPortAgentCommand -tag $testPath -cp $javatestClassDir$:$testClassDir COM1 $ExecStdTestSameJVMCmd $testExecuteClass $testExecuteArgs
env.initurl-serial-com1.javaHome=L:\\java\\jdk1.1.7\\win32


env.initurl-report.description=Environment to report the results of tests in the "initurl" test suite
env.initurl-report.finder=com.sun.javatest.finder.HTMLTestFinder
env.initurl-report.script=com.sun.javatest.lib.ReportScript
