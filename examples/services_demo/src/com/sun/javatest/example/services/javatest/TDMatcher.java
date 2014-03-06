/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.javatest.example.services.javatest;

import com.sun.javatest.TestDescription;

/**
 *
 * @author sb198751
 */
public class TDMatcher implements com.sun.javatest.services.TestPath.TDMatcher {

    public boolean matches(TestDescription td) {
        for(String s : td.getKeywords()) {
            if (s.contains("need_build")) {
                return true;
            }
        }
        return false;
    }

}
