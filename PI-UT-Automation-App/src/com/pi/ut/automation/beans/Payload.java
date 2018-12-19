package com.pi.ut.automation.beans;


public class Payload {
	private String messageKey;
	private String messageVersion;
	private String type;
	private boolean bDownloaded;
	private String downLoadLocation;
	
	/* Public Static Constants */
	public static final String STAGED = "STAGE";
	public static final String LOGGED = "LOG";
	
	public Payload(){
		this.messageKey = "";
		this.messageVersion = "";
		this.type = "";
		this.downLoadLocation = "";
		this.bDownloaded = false;
	}
	
	public Payload(String messageKey,String messageVersion,String type,String downLoadLocation){
		this.messageKey = messageKey;
		this.messageVersion = messageVersion;
		this.type = type;
		this.downLoadLocation = downLoadLocation;
		this.bDownloaded = false;
	}
	
	/**
	 * Method to clone an instance of payload
	 */
	public Payload clone(){
		Payload aClone = new Payload(this.messageKey, this.messageVersion, this.type, this.downLoadLocation);
		aClone.setDownloaded(this.bDownloaded);
		return aClone;
	}
	

	public boolean isDownloaded (){
		return bDownloaded;
	}
	public void setDownloaded(boolean bdownloadStatus) {
		this.bDownloaded = bdownloadStatus;
	}
	public String getDownLoadLocation() {
		return downLoadLocation;
	}
	public void setDownLoadLocation(String downLoadLocation) {
		this.downLoadLocation = downLoadLocation;
	}
	public String getMessageKey() {
		return messageKey;
	}
	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}
	public String getMessageVersion() {
		return messageVersion;
	}
	public void setMessageVersion(String messageVersion) {
		this.messageVersion = messageVersion;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String toXML(){
		StringBuilder sXmlBuilder = new StringBuilder();
		sXmlBuilder.append("<Payload>");
		sXmlBuilder.append("<MessageKey>"+this.messageKey+"</MessageKey>");
		sXmlBuilder.append("<MessageVersion>"+this.messageVersion+"</MessageVersion>");
		sXmlBuilder.append("</Payload>");
		return sXmlBuilder.toString();
	}
}
