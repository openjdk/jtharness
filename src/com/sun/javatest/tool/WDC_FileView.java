/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool;

import javax.swing.Icon;
import javax.swing.filechooser.FileView;
import java.io.File;

public class WDC_FileView extends FileView {

    public WDC_FileView(SelectedWorkDirApprover swda) {
        super();
        this.swda = swda;
        icon = IconFactory.getSelectableFolderIcon();
    }

    @Override
    public String getDescription(File f) {
        return "";
    }

    @Override
    public Icon getIcon(File f) {
        return swda.isWorkDirectory(f) ? icon : null;
    }

    @Override
    public String getName(File f) {
        // Take care to get names of file system roots correct
        String name = f.getName();
        return name.isEmpty() ? f.getPath() : name;
    }

    @Override
    public String getTypeDescription(File f) {
        return null;
    }

    @Override
    public Boolean isTraversable(File f) {
        return f.isDirectory() && !swda.isWorkDirectory(f) ? Boolean.TRUE : Boolean.FALSE;
    }

    private SelectedWorkDirApprover swda;
    private Icon icon;
}
