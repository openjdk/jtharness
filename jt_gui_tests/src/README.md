# JavaTest GUI Automated Test Suite
JavaTest GUI Automated Test Suite (jt_gui_tests) is designed to test JavaTest GUI in automated mode. The test suite is written with Jemmy.
During execution, tests perform some actions in JavaTest frames. Reports describing traces and issues will be created after finishing.

### Necessary software:
1. Java: JDK (at least 1.5)
2. Ant: Apache Ant (at least 1.7) with JUnit module
   * Set ANT_HOME & JAVA_HOME environment variables
3. JT harness-needed libraries: jhall(latest version), JUnit(latest version) and hamcrest(latest version)
4. aditional library: jemmy (atleast version 2)
5. javatest binary: javatest.jar

### Instructions to get "javatest.jar" binary:

These instructions assume that your JT harness local working copy is named as "JTHarness"

6. Check out the JTHarness source from github:
   ```
    https: git clone https://github.com/openjdk/jtharness.git
                [OR]
    ssh: git clone git@github.com:openjdk/jtharness.git
    ```
7. Build the JT harness:

    * Follow the instructions from [**Build JTHarness**](https://wiki.openjdk.java.net/pages/viewpage.action?pageId=18448519) and execute the command below to get the JTharness bundles and binaries:
      ```
        Navigate till <jtharness/build>
        JAVA_HOME=/path/to/jdk /path/to/bin/ant -Dbuild.version=version -Dbuild.number=buildnumber -Dbuild.milestone=milestone_name dist | tee log-3113.txt

       -Dbuild.version=version e.g: 5.0 or 6.0 etc..
       -Dbuild.number=buildnumber e.g: latest or b19 etc..
       -Dbuild.milestone=milestone_name e.g: ea or fcs
        log-3113.txt=To store the logs
      ```

    * The output of the build (build distribution) contains the following two sub-directories

    * **bundles/** -- Contains the generated jtharness.zip archive. This Zip archive contains the entire JT harness distribution including documentation, examples, and sample     code.     Note that the contents of the Zip bundle is extracted into the current directory.

    * **binaries/** -- Contains the entire unbundled distribution. It includes the binary and documentation files required to execute the JT harness. The JT harness binary             (javatest.jar) is generated into the following location:

      ```../../JTHarness-build/binaries/lib/javatest.jar```

### How to Run JavaTest Automation:

8.  Put into ```<jt_gui_test>/src/local.properties``` file paths for needed libraries

9. Start Ant from ```<jt_gui_tests>/src/``` with task test-gui. e.g:- ```ant test-gui```

     You can also run a specific test using attribute -Dtestfile=<testname>.java

    ```
    e.g:
    ant test-gui -Dtestfile=Config_Edit4.java will execute a test named "Config_Edit4"
    ant test-gui -Dtestfile=Audit?.java ("?" is one any digit) - this will execute tests Audit 0-9
    ant test-gui -Dtestfile=Config_New?*.java ("*" is any number of any digits) - this will execute all test Config_New
    ```

10. Reports will be created in ```<jt_gui_tests>/build/tests/gui/report```

### Known issues:
  11. Some tests can be unstable due to system delays
  12. Temporary files are not deleted properly sometimes

### Note:
  13. Avoid interrupting automated process
  14. It's strongly recommended to run tests in a separate window environment
  15. If needed edit the <jt_gui_tests>/src/local.properties file paths go add/remove the locations of the libraries
  16. Use below commands if you face any "crlf" issues/failures while building "jtharness" on windows
      * "git config --get core.autocrlf
      *  git config --global core.autocrlf false"
