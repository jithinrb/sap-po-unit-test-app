package com.pi.ut.automation.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.pi.ut.automation.beans.Message;
import com.pi.ut.automation.beans.PayloadDiff;
import com.pi.ut.automation.beans.Servers.Server;
import com.pi.ut.automation.util.ApplicationUtil;

public class UnitTestModel{
	private Server srcServer;
	private Server targetServer;
	private HashMap<String, Message> testMessageMap;
	private HashMap<String, Message> pushedMessageMap;
	private HashMap<String, PayloadDiff> payLoaddiffMap;
	private String executionStatus;
	private UnitTestAuditLogModel auditLog;
	private int successMsgCount;
	private int failedMsgCount;
	private int unknownStatusCount;
	private int payloadDiffCount;
	private String reportLocation;
	private String reportLauncher;
	private String reportMainView;
	
	/**
	 * Default Constructor
	 */
	public UnitTestModel(){
		this.testMessageMap = new HashMap<String, Message>();
		this.pushedMessageMap = new HashMap<String, Message>();
		this.payLoaddiffMap = new HashMap<String, PayloadDiff>();
		this.auditLog = new UnitTestAuditLogModel();
		this.successMsgCount = 0;
		this.failedMsgCount = 0;
		this.unknownStatusCount = 0;
		this.payloadDiffCount = 0;
	}
	
	/**
	 * Setter for testMessageList
	 * @param aList
	 */
	public void setTestMessageList(ArrayList<Message> aList){
		for(Message message:aList){
			this.testMessageMap.put(message.getMessageId(), message);
		}
	}
	/**
	 * Setter for pushedMessageList
	 * @param pushedMessageList
	 */
	public void setPushedMessageList(Collection<Message>pushedMessageList) {
		for(Message message:pushedMessageList){
			if(ApplicationUtil.isErrorStatusGroup(message.getMessageStatus())){
				this.failedMsgCount++;
			}else if ("success".equalsIgnoreCase(message.getMessageStatus())){
				this.successMsgCount++;
			}else{
				unknownStatusCount++;
			}
			/* message.getRefMessageId() returns the original message id from source message */
			this.pushedMessageMap.put(message.getRefMessageId(), message);
		}
	}
	
	/**
	 * Put a Payload diff Object to the map
	 * @param sSrcMessageId
	 * @param plDiff
	 */
	public void putPayloadDiff(String srcMessageId,PayloadDiff plDiff){
		if(plDiff.diffCount()>0){
			payloadDiffCount ++;
		}
		this.payLoaddiffMap.put(srcMessageId, plDiff);
	}
	
	/**
	 * Method to get a payload diff object from the map
	 * @param srcMessageId
	 * @return
	 */
	public PayloadDiff getPayloadDiff(String srcMessageId){
		return this.payLoaddiffMap.get(srcMessageId);
	}
	/**
	 * Getter for a specific test message
	 * @param sMessageId
	 * @return
	 */
	public Message getTestMessage(String sMessageId){
		return this.testMessageMap.get(sMessageId);
	}
	
	/**
	 * Getter for a specific pushed message
	 * @param sMessageId
	 * @return
	 */
	public Message getPushedMessage(String sMessageId){
		return this.pushedMessageMap.get(sMessageId);
	}
	/**
	 * Get all test message id's
	 * @return
	 */
	public Set<String> getTestMessageIdSet(){
		return this.testMessageMap.keySet();
	}
	/**
	 * Get all test pushed message id's
	 * @return
	 */
	public Set<String> getPushedMessageIdSet(){
		return this.testMessageMap.keySet();
	}

	public String getExecutionStatus() {
		return executionStatus;
	}
	public void setExecutionStatus(String executionStatus) {
		this.executionStatus = executionStatus;
	}

	/**
	 * Setter for  auditLog
	 * @return the auditLog
	 */
	public UnitTestAuditLogModel getAuditLog() {
		return auditLog;
	}

	/**
	 * Getter for auditLog
	 * @param auditLog the auditLog to set
	 */
	public void setAuditLog(UnitTestAuditLogModel auditLog) {
		this.auditLog = auditLog;
	}
	/**
	 * Get all test messages
	 * @return
	 */
	public ArrayList<Message> getTestMessageList(){
		return new ArrayList<Message>(this.testMessageMap.values());
	}
	/**
	 * Get all pushed messages
	 * @return
	 */
	public ArrayList<Message> getPushMessageList(){
		return new ArrayList<Message>(this.pushedMessageMap.values());
	}

	/**
	 * @return the srcServer
	 */
	public Server getSrcServer() {
		return srcServer;
	}

	/**
	 * @param srcServer the srcServer to set
	 */
	public void setSrcServer(Server srcServer) {
		this.srcServer = srcServer;
	}

	/**
	 * @return the targetServer
	 */
	public Server getTargetServer() {
		return targetServer;
	}

	/**
	 * @param targetServer the targetServer to set
	 */
	public void setTargetServer(Server targetServer) {
		this.targetServer = targetServer;
	}

	/**
	 * @return the successMsgCount
	 */
	public int getSuccessMsgCount() {
		return successMsgCount;
	}

	/**
	 * @param successMsgCount the successMsgCount to set
	 */
	public void setSuccessMsgCount(int successMsgCount) {
		this.successMsgCount = successMsgCount;
	}

	/**
	 * @return the failedMsgCount
	 */
	public int getFailedMsgCount() {
		return failedMsgCount;
	}

	/**
	 * @param failedMsgCount the failedMsgCount to set
	 */
	public void setFailedMsgCount(int failedMsgCount) {
		this.failedMsgCount = failedMsgCount;
	}
	/**
	 * @return the reportLocation
	 */
	public String getReportLocation() {
		return reportLocation;
	}

	/**
	 * @param reportLocation the reportLocation to set
	 */
	public void setReportLocation(String reportLocation) {
		this.reportLocation = reportLocation;
	}
	
	/**
	 * Method to get the count of payload differences
	 * @return
	 */
	public int getPayloadDiffCount(){
		return this.payloadDiffCount;
	}

	/**
	 * @return the reportLauncher
	 */
	public String getReportLauncher() {
		return reportLauncher;
	}

	/**
	 * @param reportLauncher the reportLauncher to set
	 */
	public void setReportLauncher(String reportLauncher) {
		this.reportLauncher = reportLauncher;
	}

	/**
	 * @return the reportMainView
	 */
	public String getReportMainView() {
		return reportMainView;
	}

	/**
	 * @param reportMainView the reportMainView to set
	 */
	public void setReportMainView(String reportMainView) {
		this.reportMainView = reportMainView;
	}
	
	
}
