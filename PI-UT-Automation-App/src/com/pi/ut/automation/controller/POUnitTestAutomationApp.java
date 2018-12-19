package com.pi.ut.automation.controller;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.pi.ut.automation.model.UnitTestAuditLogModel;
import com.pi.ut.automation.model.UnitTestModel;
import com.pi.ut.automation.tasks.GenerateUTReportTask;
import com.pi.ut.automation.tasks.UnitTestExecuteTask;
import com.pi.ut.automation.tasks.UnitTestSetupTask;
import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.util.LogManager;
import com.pi.ut.automation.view.UnitTestReportView;

public class POUnitTestAutomationApp {
	private Document runConfigDoc  = null;
	private XPath xpath = null;
	private UnitTestAuditLogModel utAuditLog;
	
	private static  Logger log = null;
	
	/**
	 * Default Constructor
	 * @param args
	 * @throws Exception
	 */
	public POUnitTestAutomationApp(String[] args,UnitTestAuditLogModel utLog)throws Exception{
		
		this.runConfigDoc = ApplicationUtil.loadXMLDocumentFromFile(args[0]);
		this.xpath = XPathFactory.newInstance().newXPath();
		this.utAuditLog = utLog;
		
		/* Initialise Logger */
		LogManager logManager = LogManager.getInstance();
		Node nLogConfig = (Node)xpath.evaluate("/Test-Script/Test-Setup/LoggerService",this.runConfigDoc,XPathConstants.NODE);
		logManager.doConfigure(nLogConfig);
		log = logManager.getLogger();
		log.info("Logger Initalised successfully!!!.");
	}
	
	/**
	 * Delegate method to run test setup/initialisation tasks
	 * @param utModel
	 * @return
	 * @throws Exception
	 */
	private UnitTestModel performTestSetup(UnitTestModel utModel) throws Exception{
		log.entering(POUnitTestAutomationApp.class.getName(),"performTestSetup()");
		UnitTestSetupTask tstSetupService = new UnitTestSetupTask(this.runConfigDoc);
		utModel = tstSetupService.executeTask(utModel);
		log.exiting(POUnitTestAutomationApp.class.getName(),"performTestSetup()");
		return utModel;
	}
	
	/**
	 * Delegate method to execute unit tests
	 * @param utModel
	 * @return
	 * @throws Exception
	 */
	private UnitTestModel  performUnitTests(UnitTestModel utModel) throws Exception{
		log.entering(POUnitTestAutomationApp.class.getName(),"performUnitTests()");
		UnitTestExecuteTask testExecuteTask = new UnitTestExecuteTask(this.runConfigDoc);
		utModel = testExecuteTask.executeTask(utModel);
		log.exiting(POUnitTestAutomationApp.class.getName(),"performUnitTests()");
		return utModel;
	}
	
	/**
	 * Delegate method to compile unit test report
	 * @param utModel
	 * @return
	 * @throws Exception
	 */
	private UnitTestModel  compileUnitTestReport(UnitTestModel utModel) throws Exception{
		log.entering(POUnitTestAutomationApp.class.getName(),"compileUnitTestReport()");
		GenerateUTReportTask utRepGen = new GenerateUTReportTask(this.runConfigDoc);
		utModel = utRepGen.executeTask(utModel);
		log.exiting(POUnitTestAutomationApp.class.getName(),"compileUnitTestReport()");
		return utModel;
	}
	
	/**
	 * Delegate method to print the test reports
	 * @param utModel
	 * @return
	 * @throws Exception
	 */
	private void  printUnitTestReports(UnitTestModel utModel) throws Exception{
		log.entering(POUnitTestAutomationApp.class.getName(),"compileUnitTestReport()");
		
		/* Initialise the View for printing */
		UnitTestReportView utReportView = new UnitTestReportView(utModel);
		HashMap<String, String> reportMap = utReportView.printReports();
		for(String repName: reportMap.keySet()){
			String sReport = reportMap.get(repName);
			String sFileName = String.format("%s%s",utModel.getReportLocation(),repName);
			ApplicationUtil.saveFileToDisk(sReport,sFileName);
		}
		log.exiting(POUnitTestAutomationApp.class.getName(),"compileUnitTestReport()");
		return;
	}
	
	/**
	 * Delegate method to launch the test reports
	 * @param utModel
	 * @return
	 * @throws Exception
	 */
	private void  launchUnitTestReports(UnitTestModel utModel){
		log.entering(POUnitTestAutomationApp.class.getName(),"launchUnitTestReports()");
		try{
			String sFileName = "\""+utModel.getReportLocation()+utModel.getReportMainView()+"\"";
			String sLaunchCommand = String.format(utModel.getReportLauncher(),sFileName);
			Runtime.getRuntime().exec(sLaunchCommand);
		}catch(Exception ex){
			this.utAuditLog.addErrorItemToAuditLog("Unable to launch application "+utModel.getReportLauncher());
			this.utAuditLog.addInfoItemToAuditLog("Reports are available in the disk locations specified in config file..");
		}
		log.exiting(POUnitTestAutomationApp.class.getName(),"launchUnitTestReports()");
	}
	/**
	 * Driver method to run the application
	 * @param args
	 * @throws Exception
	 */
	public void runApplication() throws Exception{
		log.entering(POUnitTestAutomationApp.class.getName(),"runApplication()");
		UnitTestModel utModel = new UnitTestModel();
		utModel.setAuditLog(this.utAuditLog);
		
		/* Run Test test setup/initialisation tasks */
		this.utAuditLog.addStatusItemToAuditLog("Performing unit test setup routines......");
		utModel= performTestSetup(utModel);
		this.utAuditLog.addStatusItemToAuditLog("Unit Test setup completed successfully....");
		
		/* Execute the unit tests*/
		this.utAuditLog.addStatusItemToAuditLog("Performing unit test executions ......");
		utModel = performUnitTests(utModel);
		this.utAuditLog.addStatusItemToAuditLog("Unit test executions completed successfully.....");
		
		/* Compile The UT reports*/
		this.utAuditLog.addStatusItemToAuditLog("Compiling Unit Test execution report ......");
		utModel = compileUnitTestReport(utModel);
		this.utAuditLog.addStatusItemToAuditLog("Unit test execution report compliled successfully.....");
		
		/* Print The UT reports*/
		this.utAuditLog.addStatusItemToAuditLog("Printing Unit Test execution report(s) ......");
		printUnitTestReports(utModel);
		this.utAuditLog.addStatusItemToAuditLog("Unit test execution report(s) printed successfully.....");
		
		/* Launch the UT reports using launch program*/
		if(!ApplicationUtil.isBlankOrNull(utModel.getReportLauncher())){
			this.utAuditLog.addStatusItemToAuditLog("Preparing to launch the reports using '"+utModel.getReportLauncher()+"'");
			launchUnitTestReports(utModel);
			this.utAuditLog.addStatusItemToAuditLog("Report launched successfully, existing application...");
		}
		
		log.exiting(POUnitTestAutomationApp.class.getName(),"runApplication()");
	}
}
