package com.pi.ut.automation.beans;


public class Message {
	
	private String messageId;
	private String refMessageId;
	private String messageKey;
	private String createdOn;
	private Payload afterMapPayload;
	private Payload beforeMapPayload;
	private Payload beforeMapEditedPayload;
	private String messageStatus;
	
	public Message(){
		this.afterMapPayload = new Payload();
		this.beforeMapPayload = new Payload();
		
		/* Edited payload will be set only after editing payload*/
		this.beforeMapEditedPayload = null;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getMessageKey() {
		return messageKey;
	}
	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}
	/*
	public Payload getSourceBMPayload() {
		return sourceBMPayload;
	}
	public void setSourceBMPayload(Payload sourceBMPayload) {
		this.sourceBMPayload = sourceBMPayload;
	}
	public Payload getSourceAMPayload() {
		return sourceAMPayload;
	}
	public void setSourceAMPayload(Payload sourceAMPayload) {
		this.sourceAMPayload = sourceAMPayload;
	}
	
	public Payload getTargetAMPayload() {
		return targetAMPayload;
	}
	public void setTargetAMPayload(Payload targetAMPayload) {
		this.targetAMPayload = targetAMPayload;
	}
*/	
	
	public String getMessageStatus() {
		return messageStatus;
	}
	/**
	 * @return the afterMapPayload
	 */
	public Payload getAfterMapPayload() {
		return afterMapPayload;
	}
	/**
	 * @param afterMapPayload the afterMapPayload to set
	 */
	public void setAfterMapPayload(Payload afterMapPayload) {
		this.afterMapPayload = afterMapPayload;
	}
	/**
	 * @return the beforeMapPayload
	 */
	public Payload getBeforeMapPayload() {
		return beforeMapPayload;
	}
	/**
	 * @param beforeMapPayload the beforeMapPayload to set
	 */
	public void setBeforeMapPayload(Payload beforeMapPayload) {
		this.beforeMapPayload = beforeMapPayload;
	}
	
	/**
	 * @return the beforeMapEditedPayload
	 */
	public Payload getBeforeMapEditedPayload() {
		return beforeMapEditedPayload;
	}
	/**
	 * @param beforeMapEditedPayload the beforeMapEditedPayload to set
	 */
	public void setBeforeMapEditedPayload(Payload beforeMapEditedPayload) {
		this.beforeMapEditedPayload = beforeMapEditedPayload;
	}
	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}
	/**
	 * Getter for reference message id
	 * @return
	 */
	public String getRefMessageId() {
		return refMessageId;
	}
	/**
	 * Setter for reference message id
	 * @param refMessageId
	 */
	public void setRefMessageId(String refMessageId) {
		this.refMessageId = refMessageId;
	}
	
	/**
	 * @return the createdOn
	 */
	public String getCreatedOn() {
		return createdOn;
	}
	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	public String getMessageXML(){
		StringBuilder sXmlBuilder = new StringBuilder();
		sXmlBuilder.append("<Message>");
		sXmlBuilder.append("<MessageId>"+this.messageId+"</MessageId>");
		sXmlBuilder.append("<MessageKey>"+this.messageKey+"</MessageKey>");
		sXmlBuilder.append("</Message>");
		return sXmlBuilder.toString();
	}
}
