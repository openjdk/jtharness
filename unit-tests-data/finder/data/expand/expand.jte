#
# $Id$
#
#
# Copyright (c) 2001, 2020, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.   Oracle designates this
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

#Environment file for "expand" test suite

# TestFinder macros
ExpandTestFinder=com.sun.javatest.finder.ExpandTestFinder

# Script macros
APIScript=javasoft.sqe.javatest.lib.APIScript

# Command macros
ExecStdTestOtherJVMCmd=javasoft.sqe.javatest.lib.ExecStdTestOtherJVMCmd
ProcessCommand=javasoft.sqe.javatest.lib.ProcessCommand

env.expand-test.description=Environment to exercise the tests in the "expand" test suite
env.expand-test.menu=true

env.expand-test.script=$APIScript
env.expand-test.finder=$ExpandTestFinder

env.expand-test.expand.field1=a0 a1 a2 a3
env.expand-test.expand.field2.foo=b0 b1 b2
env.expand-test.expand.field2.bar=c0 c1 c2
env.expand-test.expand.field3=d0 d1 d2

env.expand-test.command.testCompile.java=$ProcessCommand \
    CLASSPATH=$javatestClassDir$:$testClassDir \
    $javaHome$/bin$/javac -d $testClassDir $javacFlags $testSource
env.expand-test.command.testExecute=$ExecStdTestOtherJVMCmd \
    CLASSPATH=$javatestClassDir$:$testClassDir \
    DISPLAY=$DISPLAY \
    HOME=$HOME \
    $javaHome$/bin$/java $testExecuteClass $testExecuteArgs

env.expand-test.javaHome=/usr/local/java/latest

