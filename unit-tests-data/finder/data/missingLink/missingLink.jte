# test env file


env.dummy.finder=com.sun.javatest.finder.HTMLTestFinder -webWalk
env.dummy.script=javasoft.sqe.javatest.lib.StdTestScript -compile -execute
env.dummy.command.compile.java=javasoft.sqe.javatest.lib.ProcessCommand ${java.home}/bin/javac -d $testWorkDir $testSource
env.dummy.command.execute=javasoft.sqe.javatest.lib.ProcessCommand -pass 95 ok CLASSPATH=$testWorkDir ${java.home}/bin/java $testExecuteClass $testExecuteArgs
