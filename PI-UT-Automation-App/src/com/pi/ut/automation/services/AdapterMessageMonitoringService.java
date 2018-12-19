package com.pi.ut.automation.services;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pi.ut.automation.beans.Message;
import com.pi.ut.automation.beans.Payload;
import com.pi.ut.automation.beans.Servers.Server;
import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.util.HTTPMethod;
import com.pi.ut.automation.util.LogManager;

public class AdapterMessageMonitoringService extends AbstractWebService{
	
	private static final String Stage_Payload_Req_xml = "<Payload><MessageKey>%s</MessageKey><MessageVersion>%s</MessageVersion></Payload>";
	
	/** XSL Templates */
	private static final String GetMessageByID_Soap_Req_xsl = "com/pi/ut/automation/tmpl/GetMessagesByID_Req.xsl";
	private static final String GetMessageList_Soap_Req_xsl = "com/pi/ut/automation/tmpl/GetMessageList_Req.xsl";
	private static final String GetMessageBytes_Soap_Req_xsl = "com/pi/ut/automation/tmpl/GetMessageBytes_Req.xsl";
	private static final String GetLoggedBytes_Soap_Req_xsl = "com/pi/ut/automation/tmpl/GetLoggedMessageBytes_Req.xsl";
	
	/** Logger */
	private static final Logger log = LogManager.getInstance().getLogger();
	
	public AdapterMessageMonitoringService(Server aServer){
		super(aServer);
		setWsEndPoint("AdapterMessageMonitoring/basic?style=document");
	}
	
	/**
	 * Proxy method for the web service operation  'getMessagesByIDs'
	 * @param msgIdSet
	 * @return
	 * @throws Exception
	 */
	public  ArrayList<Message> getMessagesByID (Set<String> msgIdSet)throws Exception{
		log.entering(AdapterMessageMonitoringService.class.getName(), "getMessageList()");
		/* Convert Message ID's in Set to an Xml String */
		StringBuilder sMsgIdXml = new StringBuilder();
		sMsgIdXml.append("<MessageSearchCriteria>");
		for(String sMsgId:msgIdSet){
			sMsgIdXml.append(String.format("<ID>%s</ID>", sMsgId));
		}
		sMsgIdXml.append(String.format("<MaxResults>%s</MaxResults>", msgIdSet.size()));
		sMsgIdXml.append("</MessageSearchCriteria>");
		String sWsPayload = ApplicationUtil.executeXSLT(GetMessageByID_Soap_Req_xsl,sMsgIdXml.toString() );
		log.info("Webservice payload created successfully..");

		log.info("Invoking Webservice on "+this.wsServer.getName());
		String sWsResponse = executeHTTPRequest(sWsPayload,HTTPMethod.POST);
		log.info("Webservice invoked successfully, Parsing response");
		
		Document sWsResponseDoc = ApplicationUtil.stringToXmlDocument(sWsResponse);
		Message piMessage = null;
		ArrayList<Message> messageList = new ArrayList<Message>();
		NodeList nlMessageList = (NodeList)this.xpath.evaluate("//getMessagesByIDsResponse/Response/list/AdapterFrameworkData",sWsResponseDoc,XPathConstants.NODESET);
		Node nTemp;
		for(int i=0;i<nlMessageList.getLength();i++){
			nTemp = nlMessageList.item(i);
			piMessage = new Message();
			piMessage.setMessageId((String)this.xpath.evaluate("messageID",nTemp,XPathConstants.STRING));
			piMessage.setMessageKey((String)this.xpath.evaluate("messageKey",nTemp,XPathConstants.STRING));
			piMessage.setMessageStatus((String)this.xpath.evaluate("status",nTemp,XPathConstants.STRING));
			piMessage.setCreatedOn((String)this.xpath.evaluate("startTime",nTemp,XPathConstants.STRING)); 
			messageList.add(piMessage);
		}
		log.info(messageList.size()+" Message ID's retreived successfully!!");

		log.exiting(AdapterMessageMonitoringService.class.getName(), "getMessageList()");
		return messageList;
	}
	
	/**
	 * Proxy method for the web service operation  'getMessageList'
	 * @param nSrchCriteria
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Message> getMessageList(Node nSrchCriteria)throws Exception{
		log.entering(AdapterMessageMonitoringService.class.getName(), "getMessageList()");
		String sWsPayload = ApplicationUtil.executeXSLT(GetMessageList_Soap_Req_xsl, nSrchCriteria);
		log.info("Webservice payload created successfully..");

		log.info("Invoking Webservice on "+this.wsServer.getName());
		String sWsResponse = executeHTTPRequest(sWsPayload,HTTPMethod.POST);
		log.info("Webservice invoked successfully, Parsing response");
		
		Document sWsResponseDoc = ApplicationUtil.stringToXmlDocument(sWsResponse);
		Message piMessage = null;
		ArrayList<Message> messageList = new ArrayList<Message>();
		NodeList nlMessageList = (NodeList)this.xpath.evaluate("//getMessageListResponse/Response/list/AdapterFrameworkData",sWsResponseDoc,XPathConstants.NODESET);
		Node nTemp;
		for(int i=0;i<nlMessageList.getLength();i++){
			nTemp = nlMessageList.item(i);
			piMessage = new Message();
			piMessage.setMessageId((String)this.xpath.evaluate("messageID",nTemp,XPathConstants.STRING));
			piMessage.setMessageKey((String)this.xpath.evaluate("messageKey",nTemp,XPathConstants.STRING));
			piMessage.setMessageStatus((String)this.xpath.evaluate("status",nTemp,XPathConstants.STRING));
			piMessage.setCreatedOn((String)this.xpath.evaluate("startTime",nTemp,XPathConstants.STRING)); 
			messageList.add(piMessage);
		}
		log.info(messageList.size()+" Message ID's retreived successfully!!");

		log.exiting(AdapterMessageMonitoringService.class.getName(), "getMessageList()");
		return messageList;
	}
	
	/**
	 * Proxy method for the web service operation  'getMessageBytesJavaLangStringIntBoolean'
	 * @param sMessageKey
	 * @param sMsgVersion
	 * @return
	 * @throws Exception
	 */
	public String getStagedVersionPayload(String sMessageKey,String sMsgVersion) throws Exception{
		log.entering(AdapterMessageMonitoringService.class.getName(), "getStagedVersionPayload()");
		String sRequestXML = String.format(Stage_Payload_Req_xml, sMessageKey,sMsgVersion);
		String sWsSoapRequest = ApplicationUtil.executeXSLT(GetMessageBytes_Soap_Req_xsl, sRequestXML);
		log.fine("SOAP Request created, procedding for WS call");
		
		String sWsPayResp = executeHTTPRequest(sWsSoapRequest,HTTPMethod.POST);
		log.fine("SOAP Response received, parsing response");
		Document soapDoc = ApplicationUtil.stringToXmlDocument(sWsPayResp);
		String sBase64Encoded = (String)xpath.evaluate("//*[local-name()='Response']", soapDoc,XPathConstants.STRING);
		log.fine("Encoded Payload "+sBase64Encoded);
		
		log.exiting(AdapterMessageMonitoringService.class.getName(), "getStagedVersionPayload()");
		return ApplicationUtil.base64Decode(sBase64Encoded);
	}
	
	/**
	 * Proxy method for the web service operation  'getLoggedMessageBytes'
	 * @param sMessageKey
	 * @param sMsgVersion
	 * @return
	 * @throws Exception
	 */
	public String getLoggedVersion(String sMessageKey) throws Exception{
		log.entering(AdapterMessageMonitoringService.class.getName(), "getLoggedVersion()");
		String sRequestXML = String.format(Stage_Payload_Req_xml, sMessageKey,"0");
		String sWsSoapRequest = ApplicationUtil.executeXSLT(GetLoggedBytes_Soap_Req_xsl, sRequestXML);
		log.fine("SOAP Request created, procedding for WS call");
		
		String sWsPayResp = executeHTTPRequest(sWsSoapRequest,HTTPMethod.POST);
		log.fine("SOAP Response received, parsing response");
		Document soapDoc = ApplicationUtil.stringToXmlDocument(sWsPayResp);
		String sBase64Encoded = (String)xpath.evaluate("//*[local-name()='Response']", soapDoc,XPathConstants.STRING);
		log.fine("Encoded Payload "+sBase64Encoded);
		
		log.exiting(AdapterMessageMonitoringService.class.getName(), "getLoggedVersion()");
		return ApplicationUtil.base64Decode(sBase64Encoded);
	}
	
	/**
	 * Facade method to wrap getLoggedVersion & getStagedVersionPayload into a single API call
	 * @param aPayload
	 * @return
	 * @throws Exception
	 */
	public String getPayloadVersion(Payload aPayload) throws Exception{
		log.entering(AdapterMessageMonitoringService.class.getName(), "getPayloadVersion()");
		String sPayload = null;
		if(Payload.STAGED.equalsIgnoreCase(aPayload.getType())){
			log.fine("Downloading Staged message  from server");
			sPayload = getStagedVersionPayload(aPayload.getMessageKey(),aPayload.getMessageVersion());
		}else if(Payload.LOGGED.equalsIgnoreCase(aPayload.getType())){
			log.fine("Downloading logged message  from server");
			sPayload = getLoggedVersion(aPayload.getMessageKey());
		}
		log.exiting(AdapterMessageMonitoringService.class.getName(), "getPayloadVersion()");
		return sPayload;
	}
}
