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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sun.javatest.util.I18NResourceBundle;
import java.io.FileInputStream;

/**
 * Finder which reads class files to locate those with an appropriate annotations
 * to qualify it as a "test".  If an appropriate JUnit test, it is then scanned
 * for methods that are test methods and this information is added to the
 * TestDescription which is produced.  The functionality here is currently based
 * on what you would find in a JUnit 4.x test suite which uses annotations for
 * marking tests.
 *
 * The scanner can operate in two ways- either by looking for .class files and
 * scanning them, or by looking for .java files and then loading the corresponding
 * .class file.
 *
 * @see com.sun.javatest.TestFinder
 * @see com.sun.javatest.TestDescription
 */
public class JUnitAnnotationTestFinder extends JUnitTestFinder
        implements ClassVisitor, MethodVisitor {
    /**
     * Constructs the list of file names to exclude for pruning in the search
     * for files to examine for test descriptions.
     */
    public JUnitAnnotationTestFinder() {
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


    /**
     * Call to register the methods which are test methods.
     */
    public void foundTestMethod(String name) {
        testMethods.add(name);
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
                //System.out.println("dir: " + dir.getAbsolutePath());
                // if its a directory, add it to the list to be scanned
                //foundFile(file);
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

                // methods are necessary for this to be a test
                // could expand this to allow other junit annotations
                if (testMethods.size() != 0) {
                    StringBuilder tms = new StringBuilder();
                    for (String n: testMethods) {
                        tms.append(n);
                        tms.append(" ");
                    }
                    tms.deleteCharAt(tms.length()-1);
                    tdValues.put("source", file.getPath());
                    tdValues.put("junit.testmethods", tms.toString());
                    tdValues.put("junit.finderscantype", "annotation");
                    tdValues.put("keywords", "junit junit4");
                    tdValues.put("executeClass", cr.getClassName().replaceAll("/", "."));

                    // consider stripping the .java or .class off currFile
                    foundTestDescription(tdValues, currFile, 0);
                }

            } catch(IOException e) {
                error(i18n, "finder.classioe", classFile);
            }       // catch
        } catch (Exception e) {
            System.out.println("!!! Exception: " + e);
        }

        return;
    }

    //----------ASM interface -----------
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        if (verbose)
            System.out.println("found class " + name + " with super " +superName);
    }

    // class visitor methods we are interested in
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        if (access == Opcodes.ACC_PUBLIC) {
            currMethod = name;
            return this;
        } else
            return null;
    }

    public void visitSource(String string, String string0) {
    }

    public void visitOuterClass(String string, String string0, String string1) {
    }

    public AnnotationVisitor visitAnnotation(String string, boolean b) {
        if (methodAnnotation.equals(string))
            foundTestMethod(currMethod);

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


    // -- Method visitor --
    public void visitJumpInsn(int i, Label label) {
    }

    public void visitLineNumber(int i, Label label) {
    }

    public void visitLabel(Label label) {
    }

    public void visitTableSwitchInsn(int i, int i0, Label label, Label[] label0) {
    }

    public void visitLdcInsn(Object object) {
    }

    public void visitFrame(int i, int i0, Object[] object, int i1, Object[] object0) {
    }

    public void visitInsn(int i) {
    }

    public void visitLookupSwitchInsn(Label label, int[] i, Label[] label0) {
    }

    public void visitTryCatchBlock(Label label, Label label0, Label label1, String string) {
    }

    public void visitTypeInsn(int i, String string) {
    }

    public void visitMultiANewArrayInsn(String string, int i) {
    }

    public void visitMethodInsn(int i, String string, String string0, String string1) {
    }

    public void visitFieldInsn(int i, String string, String string0, String string1) {
    }

    public void visitLocalVariable(String string, String string0, String string1, Label label, Label label0, int i) {
    }

    public AnnotationVisitor visitParameterAnnotation(int i, String string, boolean b) {
        return null;
    }

    public void visitVarInsn(int i, int i0) {
    }

    public void visitMaxs(int i, int i0) {
    }

    public AnnotationVisitor visitAnnotationDefault() {
        return null;
    }

    public void visitCode() {
    }

    public void visitIincInsn(int i, int i0) {
    }

    public void visitIntInsn(int i, int i0) {
    }

    //----------member variables------------------------------------------------

    protected String currMethod;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(JUnitAnnotationTestFinder.class);
    protected String methodAnnotation = "Lorg/junit/Test;";
}

