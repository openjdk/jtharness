/*
 * $Id$
 *
 * Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javatest.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;

import com.sun.javatest.util.I18NResourceBundle;

/**
 * Finder which reads class files to locate those with an appropriate base class
 * to qualify it as a "test".  If an appropriate JUnit test, it is then scanned
 * for methods that are test methods and this information is added to the
 * TestDescription which is produced.
 *
 * Note that this class is not reentrant and must be protected by the calling
 * parties.
 *
 * @see com.sun.javatest.TestFinder
 * @see com.sun.javatest.TestDescription
 */
public class JUnitSuperTestFinder extends JUnitTestFinder implements ClassVisitor {
    /**
     * Constructs the list of file names to exclude for pruning in the search
     * for files to examine for test descriptions.
     */
    public JUnitSuperTestFinder() {
        exclude(excludeNames);
    }

    /**
     * Decode the arg at a specified position in the arg array.  If overridden
     * by a subtype, the subtype should try and decode any arg it recognizes,
     * and then call super.decodeArg to give the superclass(es) a chance to
     * recognize any arguments.
     *
     * @param args The array of arguments
     * @param i    The next argument to be decoded.
     * @return     The number of elements consumed in the array; for example,
     *             for a simple option like "-v" the result should be 1; for an
     *             option with an argument like "-f file" the result should be
     *             2, etc.
     * @throws TestFinder.Fault If there is a problem with the value of the current arg,
     *             such as a bad value to an option, the Fault exception can be
     *             thrown.  The exception should NOT be thrown if the current
     *             arg is unrecognized: in that case, an implementation should
     *             delegate the call to the supertype.
     */
    protected void decodeAllArgs(String[] args) throws Fault {
        super.decodeAllArgs(args);

        for (int i = 0; i < args.length; i++) {
            if ("-superclass".equalsIgnoreCase(args[i])) {
                if (args.length <= i+1) {
                    error(i18n, "finder.missingsuper");
                    return;
                }
                requiredSuperclass.add(args[++i]);
            }
        }   // for

        // the default if the user does not give one
        if (requiredSuperclass.size() == 0)
            requiredSuperclass.add("junit.framework.TestCase");
    }

    /**
     * Scan a file, looking for test descriptions and/or more files to scan.
     * @param file The file to scan
     */
    public void scan(File file) {
        currFile = file;
        if (file.isDirectory())
            scanDirectory(file);
        else
            scanFile(file);
    }

    //-----internal routines----------------------------------------------------

    /**
     * Scan a directory, looking for more files to scan
     * @param dir The directory to scan
     */
    private void scanDirectory(File dir) {

        // scan the contents of the directory, checking for
        // subdirectories and other files that should be scanned
        String[] names = dir.list();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            // if the file should be ignored, skip it
            // This is typically for directories like SCCS etc
            if (excludeList.containsKey(name))
                continue;

            File file = new File(dir, name);
            if (file.isDirectory()) {
                if (verbose)
                    System.out.println("dir: " + dir.getAbsolutePath());
                // if its a directory, add it to the list to be scanned
                scanDirectory(file);
            } else {
                // if its a file, check its extension
                int dot = name.indexOf('.');
                if (dot == -1)
                    continue;
                String extn = name.substring(dot);
                if (extensionTable.containsKey(extn)) {
                    // extension has a comment reader, so add it to the
                    // list to be scanned
                    foundFile(file);
                }
            }
        }
    }

    /**
     * Scan a file, looking for comments and in the comments, for test
     * description data.
     * @param file The file to scan
     */
    protected void scanFile(File file) {
        testMethods = new ArrayList();  // new every time we visit a new class
        tdValues = new HashMap();

        String name = file.getName();
        if (verbose)
            System.out.println(i18n.getString("finder.whichfile", name));

        int dot = name.indexOf('.');
        if (dot == -1)
            return;

        String classFile="";
        if (scanClasses) {
            classFile = file.getPath();
        } else {
            String currentDir=new File("").getAbsolutePath();
            String sources = name;
            String filePath=file.getAbsolutePath().
                    substring(currentDir.length()+1, file.getAbsolutePath().length());

            if (filePath.startsWith("tests")){
                classFile=currentDir+File.separator+"classes"+File.separator+filePath.substring(6,filePath.length());
            } else if (filePath.startsWith("test")){
                classFile=currentDir+File.separator+"classes"+File.separator+filePath.substring(5,filePath.length());
            } else {
                return;
            }

            classFile = file.getAbsolutePath().replaceFirst("tests", "classes");

        }

        dot = classFile.lastIndexOf('.');
        classFile = classFile.substring(0, dot)+ ".class";

        try {
            if(!new File(classFile).exists()){
                System.out.println("classFile does not exist: " + classFile);
                return;
            }
            try {
                FileInputStream fis = new FileInputStream(classFile);
                ClassReader cr = new ClassReader(fis);
                cr.accept(this, 0);
                // action happens in visit(...) below

                if (tdValues.get("executeClass") == null)
                    return;     // not interested in this class

                if (testMethods.size() != 0) {
                    StringBuilder tms = new StringBuilder();
                    for (String n: testMethods) {
                        tms.append(n);
                        tms.append(" ");
                    }
                    tms.deleteCharAt(tms.length()-1);

                    tdValues.put("junit.testmethods", tms.toString());
                }

                tdValues.put("keywords", "junit junit3");
                tdValues.put("junit.finderscantype", "superclass");
                tdValues.put("source", file.getPath());
                StringBuilder cls = new StringBuilder();
                for (String n: requiredSuperclass) {
                    cls.append(n);
                    cls.append(" ");
                }
                cls.deleteCharAt(cls.length()-1);
                tdValues.put("junit.findersuperclasses", cls.toString());

                // consider stripping the .java or .class off currFile
                foundTestDescription(tdValues, currFile, 0);
            } catch(IOException e) {
                error(i18n, "finder.classioe", classFile);
            }       // catch
        } catch (Exception e) {
            System.out.println("!!! Exception: " + e);
        }
        return;

    }


    /**
     * Lookup current name among requested superclasses.
     */
    private boolean isMatchSuper(String cname) {
        if (cname == null || cname.equals("java.lang.Object"))
            return false;

        for(String n: requiredSuperclass) {
            if (cname.equals(n))
                return true;
        }   // for

        try {
            return isMatchSuper(mfv.getSuperClass(cname));
        } catch (IOException e) {
            error(i18n, "finder.cantsuper", cname);
            return false;
        }
    }

    /**
     * Override this if you want to look for method names differently; the
     * default implementation looks for methods that begin with the string
     * "test".  If you wish to do full signature analysis, override
     * visitMethod(...) which is part of the ASM interface.
     *
     */
    public boolean isTestMethodSignature(String sig) {
        if (sig.startsWith(initialTag))
            return true;
        else
            return false;
    }
//----------ASM inteface-----------
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        if (verbose)
            System.out.println("found class " + name + " with super " +superName);

        if (isMatchSuper(superName.replaceAll("/", "."))) {

            //tdValues.put("sources", sources);
            tdValues.put("executeClass", name.replaceAll("/", "."));
        } else {

        }
    }

// class visitor methods we are interested in
    /**
     * Looks for methods which are test methods by calling <tt>isTestMethodSignature</tt>.
     * You can override that method or this one.  If overriding this one,
     * use foundTestMethod(String) to register any test methods which you
     * find.
     */
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        if (access == Opcodes.ACC_PUBLIC) {
            //System.out.println("found method " + name);
            if (isTestMethodSignature(name))
                foundTestMethod(name);
        }

        return null;
    }

    public void visitSource(String string, String string0) {
    }

    public void visitOuterClass(String string, String string0, String string1) {
    }

    public AnnotationVisitor visitAnnotation(String string, boolean b) {
        return null;
    }

    public void visitAttribute(Attribute attribute) {
    }

    public void visitInnerClass(String string, String string0, String string1, int i) {
    }

    public FieldVisitor visitField(int i, String string, String string0, String string1, Object object) {
        return null;
    }

    public void visitEnd() {
    }

    private static class MethodFinderVisitor extends EmptyVisitor {
        /**
         * Return the given class' superclass name in dotted notation.
         */
        public String getSuperClass(String cname) throws IOException {
            ClassReader cr = new ClassReader(cname);
            cr.accept(this, 0);
            return thisSupername.replaceAll("/", ".");
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            thisSupername = superName;
        }

        String thisSupername;
    }


//----------member variables------------------------------------------------

    protected ArrayList<String> requiredSuperclass = new ArrayList();
    protected String initialTag = "test";
    protected final MethodFinderVisitor mfv = new MethodFinderVisitor();
    protected static final I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(JUnitSuperTestFinder.class);
}
