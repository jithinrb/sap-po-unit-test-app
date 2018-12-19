package com.pi.ut.automation.model;

import java.util.Observable;
import java.util.Observer;

public class UnitTestAuditLogModel extends Observable {
	private String auditLog;
	
	private static final String AUDIT_LOG_FMT = "[%s] %s";
	/**
	 * Default constructor - This will initalise 
	 * the object without any observer
	 */
	public UnitTestAuditLogModel(){
		this.auditLog = "";
	}
	
	/**
	 * Method to initialse the object with an observer
	 * @param anObserver
	 */
	public UnitTestAuditLogModel(Observer anObserver){
		this.auditLog = "";
		addObserver(anObserver);
	}
	
	private void addAuditLogItem(String sType, String sMessage){
		String sFinalMessage = String.format(AUDIT_LOG_FMT, sType,sMessage);
		this.auditLog = sFinalMessage;
		setChanged();
		notifyObservers(sFinalMessage);
	}
	public void addStatusItemToAuditLog(String sMessage){
		this.auditLog =sMessage;
		setChanged();
		notifyObservers(sMessage);
	}
	public void addInfoItemToAuditLog(String sMessage){
		addAuditLogItem("INFO",sMessage);
	}
	public void addErrorItemToAuditLog(String sMessage){
		addAuditLogItem("ERROR",sMessage);
	}
	
	public String getLastAuditLogItem(){
		return this.auditLog;
	}

}
