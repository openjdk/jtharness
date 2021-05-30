/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jthtest.ReportConverter;

import jthtest.Test;
import jthtest.tools.JTFrame;
import jthtest.tools.Task.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 *
 * @author andrey
 */
public class ReportConverter1 extends Test {
    String find1 = "Greetings!Thistoolisusedtomergemanyxmlreportsintoone.Selectoutputdirectoryand2ormorexmlreports.";
    String find2 = "Greetings!Thistoolisusedtomergemanyxmlreportsintoone.PushSettings->New...menutoopenconverterdialog.orSettings->Opentoopenafileforreading";

    @Override
    public void testImpl() throws Exception {
        mainFrame = new JTFrame(true);
        mainFrame.getTools_ReportConverterMenu().push();
        
        JEditorPaneOperator op = new JEditorPaneOperator(mainFrame.getJFrameOperator());
        Waiter waiter = new WaiterImpl(op, find1);
        waiter.waitForDone();
        
        JDialogOperator d = new JDialogOperator("Create a Report");
        new JButtonOperator(d, "Cancel").push();

        waiter = new WaiterImpl(op, find2);
        waiter.waitForDone();
    }

    private static class WaiterImpl extends Waiter {
        private final JEditorPaneOperator editpane;
        private final String toFind;

        public WaiterImpl(JEditorPaneOperator op, String toFind) {
            super(false);
            this.editpane = op;
            if (toFind == null || toFind.equals("")) {
                throw new IllegalArgumentException();
            }
            this.toFind = toFind.replaceAll("[\t\n\r\f ]", "");
            start();
        }

        @Override
        protected boolean check() {
            String txt = editpane.getDisplayedText();
            if (txt == null)
                return false;
            String text = txt.replaceAll("[\t\n\r\f ]", "");
            return text != null && text.equals(toFind);
        }
    }
    
}
