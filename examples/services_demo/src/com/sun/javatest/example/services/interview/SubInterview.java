/*
 */
package com.sun.javatest.example.services.interview;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import com.sun.javatest.TestSuite;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.interview.BasicInterviewParameters;
import com.sun.javatest.Parameters.EnvParameters;
import com.sun.interview.FinalQuestion;
import com.sun.interview.Interview;
import com.sun.interview.NullQuestion;
import com.sun.interview.PropertiesQuestion;
import com.sun.interview.Question;
import com.sun.interview.StringQuestion;

import com.sun.interview.ChoiceQuestion;
import com.sun.interview.FileQuestion;
import com.sun.interview.ErrorQuestion;
import com.sun.interview.Checklist;

public class SubInterview extends Interview {

    public SubInterview() throws InterviewParameters.Fault {
        super("sub");
        init();
    }

    public SubInterview(String tag) throws Interview.Fault {
        super(tag);
        init();
    }

    private void init() {
        try {
            setHelpSet("moreinfo/interview.hs");
        }
        catch (com.sun.interview.Interview.Fault f) {
            f.printStackTrace();
        }

        // use modern semantics, not legacy behavior
        setInterviewSemantics(Interview.SEMANTIC_MAX_VERSION);

        setResourceBundle("i18n");
        setFirstQuestion(qName);

        ResourceBundle rb = getResourceBundle();
        if (rb != null)
            setTitle(rb.getString("services.interview.title"));
    }

    @Override
    public void export(Map<String, String> map)  {
        super.export(map);
        System.err.println("Subinteview exporting!");
    }
    //----------------------------------------------------------------------
    //
    // Give a name for this configuration

    private StringQuestion qName = new StringQuestion(this, "SubconfName") {
        {
            setDefaultValue("services");
        }

        private boolean isValidIdentifier(String s) {
            if (s == null || s.equals(""))
                return false;

            if (!Character.isUnicodeIdentifierStart(s.charAt(0)))
                return false;

            for (int i = 1; i < s.length(); i++) {
                if (!Character.isUnicodeIdentifierPart(s.charAt(i)))
                    return false;
            }
            return true;
        }

        protected void export(Map<String, String> data) {
            data.put("subconf desc", "services description");
        }

        public boolean isEnabled() {
            return true;
        }

        public boolean isHidden() {
            return true;
        }

        public boolean isValueValid() {
            return true;
            //return isValidIdentifier(value);
        }

        protected Question getNext() {
            return finalQ;
        }
    };

    private FinalQuestion finalQ = new FinalQuestion(this) {} ;

}
