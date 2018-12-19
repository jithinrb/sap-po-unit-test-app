package com.pi.ut.automation.controller;

import com.pi.ut.automation.model.UnitTestAuditLogModel;
import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.view.StatusUpdateView;


public class POUnitTestLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		UnitTestAuditLogModel utAuditLog = new UnitTestAuditLogModel(new StatusUpdateView());
		utAuditLog.addStatusItemToAuditLog("******** Starting PO Unit Test Automation Application **** ");
		try{
			POUnitTestAutomationApp piUTBot = new POUnitTestAutomationApp(args,utAuditLog);
			piUTBot.runApplication();
		}catch(Exception ex){
			utAuditLog.addErrorItemToAuditLog("Exception '"+ex.toString()+"'reported by application, unable to complete application execution");
			utAuditLog.addErrorItemToAuditLog("Exception details are logged in the log file..");
			System.err.println(ApplicationUtil.logException(ex, "POUnitTestLauncher.main()"));
		}finally{
			utAuditLog.addStatusItemToAuditLog("******** Exiting  PO Unit Test Automation Application **** ");
			System.exit(0);
		}
		
	}	
}

