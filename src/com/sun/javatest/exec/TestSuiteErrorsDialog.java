/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.exec;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import com.sun.javatest.TestFinder;
import com.sun.javatest.TestSuite;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;

class TestSuiteErrorsDialog extends ToolDialog
{

    TestSuiteErrorsDialog(Component parent, UIFactory uif) {
        super(parent, uif, "tse");
    }

    public void show(TestSuite ts) {
        testSuite = ts;

        StringBuffer sb = new StringBuffer();
        sb.append("<html><body style=\"font-family: SansSerif; font-size: 12pt\">");

        if (testSuite == null)
            sb.append(uif.getI18NString("tse.head.noTestSuite"));
        else {
            TestFinder tf = testSuite.getTestFinder();
            if (tf.getErrorCount() == 0)
                sb.append(uif.getI18NString("tse.head.noErrs"));
            else {
                sb.append(uif.getI18NString("tse.head.errs"));
                sb.append("<ul>");

                String[] errors = tf.getErrors();
                for (int i = 0; i < errors.length; i++) {
                    sb.append("<li>");
                    sb.append(escape(errors[i]));
                }
                sb.append("</ul>");
            }
        }
        sb.append("</body>");
        sb.append("</html>");

        // would be nice to have more incremental update here;
        // might have to rethink use of HTML
        if (body == null)
            initGUI();

        body.setText(sb.toString());
        setVisible(true);
    }

    protected void initGUI() {
        setHelp("browse.testSuiteErrors.csh");
        setI18NTitle("tse.title");

        body = new JEditorPane();
        body.setName("tse");
        body.setContentType("text/html");
        body.setEditable(false);
        body.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane sp = uif.createScrollPane(body);
        int dpi = uif.getDotsPerInch();
        sp.setPreferredSize(new Dimension(6 * dpi, 3 * dpi));
        setBody(sp);

        JButton helpBtn = uif.createHelpButton("tse.help", "browse.testSuiteErrors.csh");
        JButton closeBtn = uif.createCloseButton("tse.close");
        setButtons(new JButton[] { helpBtn, closeBtn }, closeBtn);
    }

    private String escape(String s) {
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
            case '<': case '>': case '&':
                StringBuffer sb = new StringBuffer(s.length()*2);
                for (int j = 0; j < s.length(); j++) {
                    char c = s.charAt(j);
                    switch (c) {
                    case '<': sb.append("&lt;"); break;
                    case '>': sb.append("&gt;"); break;
                    case '&': sb.append("&amp;"); break;
                    default: sb.append(c);
                    }
                }
                return sb.toString();
            }
        }
        return s;
    }

    private TestSuite testSuite;
    private JEditorPane body;
}
