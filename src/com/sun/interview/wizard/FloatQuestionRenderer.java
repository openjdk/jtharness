/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import com.sun.interview.FloatQuestion;
import com.sun.interview.Question;

public class FloatQuestionRenderer
    implements QuestionRenderer
{
    public JComponent getQuestionRendererComponent(Question qq, ActionListener listener) {
        FloatQuestion q = (FloatQuestion) qq;

        lwb = q.getLowerBound();
        upb = q.getUpperBound();
        range = upb - lwb;
        suggs = q.getSuggestions();

        return createTextField(q, listener);
    }

    public String getInvalidValueMessage(Question q) {
        return null;
    }

    protected JPanel createTextField(final FloatQuestion q, ActionListener listener) {
        int w = 1;
        while (range >= 10) {
            range /= 10;
            w++;
        }
        if (lwb < 0)
            w++;
        // add in room for fractions
        w += 5;

        w = Math.min(w, 20);

        String[] strSuggs;
        if (suggs == null)
            strSuggs = null;
        else {
            strSuggs = new String[suggs.length];
            for (int i = 0; i < suggs.length; i++)
                strSuggs[i] = String.valueOf(suggs[i]);
        }

        final float defVal = q.getDefaultValue();
        if (Float.isNaN(defVal))
            resetBtn = null;
        else {
            resetBtn = new JButton(i18n.getString("flt.reset.btn"));
            resetBtn.setName("flt.reset.btn");
            resetBtn.setMnemonic(i18n.getString("flt.reset.mne").charAt(0));
            resetBtn.setToolTipText(i18n.getString("flt.reset.tip"));
        }

        final TypeInPanel p =  new TypeInPanel("flt.field",
                                               q,
                                               w,
                                               strSuggs,
                                               resetBtn,
                                               listener);

        if (resetBtn != null) {
            resetBtn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        NumberFormat fmt = NumberFormat.getNumberInstance();  // will be locale-specific
                        p.setValue(fmt.format(new Double(defVal)));
                    }
                });
        }

        return p;
    }


    protected float lwb;
    protected float upb;
    protected float range;
    protected float[] suggs;

    protected JButton resetBtn;

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
}
