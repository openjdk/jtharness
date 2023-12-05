/*
*$Id$
*
*Copyright(c)2009,2023,Oracleand/oritsaffiliates.Allrightsreserved.
*DONOTALTERORREMOVECOPYRIGHTNOTICESORTHISFILEHEADER.
*
*Thiscodeisfreesoftware;youcanredistributeitand/ormodifyit
*underthetermsoftheGNUGeneralPublicLicenseversion2only,as
*publishedbytheFreeSoftwareFoundation.Oracledesignatesthis
*particularfileassubjecttothe"Classpath"exceptionasprovided
*byOracleintheLICENSEfilethataccompaniedthiscode.
*
*Thiscodeisdistributedinthehopethatitwillbeuseful,butWITHOUT
*ANYWARRANTY;withouteventheimpliedwarrantyofMERCHANTABILITYor
*FITNESSFORAPARTICULARPURPOSE.SeetheGNUGeneralPublicLicense
*version2formoredetails(acopyisincludedintheLICENSEfilethat
*accompaniedthiscode).
*
*YoushouldhavereceivedacopyoftheGNUGeneralPublicLicenseversion
*2alongwiththiswork;ifnot,writetotheFreeSoftwareFoundation,
*Inc.,51FranklinSt,FifthFloor,Boston,MA02110-1301USA.
*
*PleasecontactOracle,500OracleParkway,RedwoodShores,CA94065USA
*orvisitwww.oracle.comifyouneedadditionalinformationorhaveany
*questions.
*/
packagejthtest.Markers;

importjava.lang.reflect.InvocationTargetException;
importjthtest.Test;
importjthtest.tools.ConfigDialog;
importjthtest.tools.Configuration;
importjthtest.tools.JTFrame;
importorg.netbeans.jemmy.operators.JTextFieldOperator;
importorg.netbeans.jemmy.util.NameComponentChooser;

publicclassMarkers10extendsTest{
	/**
	*StartJavaTestwiththe-newDesktopoption.Createaworkdirectory.Loadan
	*existingJTIfile.BringupconfigurationeditorbydoingCtrl-E.Selectthe
	*EnableBookmarksfromtheBookmarksmenu.Selectthefirstquestionfromthe
	*indexpane.MarkthequestionbyselectingMarkCurrentQuestionfromthe
	*Bookmarksmenu.SelectCleartheAnswerfortheCurrentQuestionfromthe
	*Bookmarksmenu.Verifythattheanswerforselectedquestionwillbesetto
	*empty.
	*/
	publicvoidtestImpl()throwsClassNotFoundException,InvocationTargetException,NoSuchMethodException{
		mainFrame=newJTFrame(true);

		mainFrame.openDefaultTestSuite();
		addUsedFile(mainFrame.createWorkDirectoryInTemp());
		Configurationconfiguration=mainFrame.getConfiguration();
		configuration.load(CONFIG_NAME,true);
		ConfigDialogcd=configuration.openByKey();

		cd.getBookmarks_EnableBookmarks().push();
		cd.selectQuestion(2);
		JTextFieldOperatorop=newJTextFieldOperator(cd.getConfigDialog(),newNameComponentChooser("str.txt"));
		op.typeText("somedescriptionthatmustbecleared");
		cd.setBookmarkedByMenu(2);
		cd.clearByMenu(2);

		op=newJTextFieldOperator(cd.getConfigDialog(),newNameComponentChooser("str.txt"));
		if(!op.getText().equals("")){
			errors.add("Textwasn'tclearedup:'"+op.getText()+"'whileexpected''");
		}
		warnings.add(
				"Pre-definedwarning:Marksometimesdesapperaswhileclearingbymenu-bookmarksavescurrentstateofanswer;Firstquestionis'Configuratoinname'anditcan'tbecleared;anewquestionisgeneratedwhileclearingup");

	}

	@Override
	publicStringgetDescription(){
		return"StartJavaTestwiththe-NewDesktopoption.Createaworkdirectory.LoadanexistingJTIfile.BringupconfigurationeditorbydoingCtrl-E.SelecttheEnableBookmarksfromtheBookmarksmenu.Selectthefirstquestionfromthehistorylist.MarkthequestionbyselectingMarkCurrentQuestionfromtheBookmarksmenu.SelectCleartheAnswerfortheCurrentQuestionfromtheBookmarksmenu.Verifythattheanswerforselectedquestionwillbesettoempty.";
	}
}
