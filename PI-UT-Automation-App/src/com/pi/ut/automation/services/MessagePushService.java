package com.pi.ut.automation.services;

import java.util.Scanner;
import java.util.logging.Logger;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;

import com.pi.ut.automation.beans.XIMessage;
import com.pi.ut.automation.beans.Servers.Server;
import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.util.HTTPMethod;
import com.pi.ut.automation.util.LogManager;

public class MessagePushService extends AbstractWebService {
	
	private String authHeader = null;
	
	private static final String CRLF = "\r\n";
	/** Logger */
	private static final Logger log = LogManager.getInstance().getLogger();
	
	/**
	 * Parameterised Constructor
	 * @param aServer
	 * @param sChannel
	 * @param authHeader
	 */
	public MessagePushService(Server aServer,String sChannel,String authHeader){
		super(aServer);
		setWsEndPoint("XISOAPAdapter/MessageServlet?channel="+sChannel);
		this.authHeader = authHeader;
	}
	
	/**
	 * Overrides the Basic Auth header generated from server config with the auth header passed as a 
	 * parameter to push service 
	 */
	@Override 
	protected String getBasicAuthorisationHeader(){
		if(ApplicationUtil.isBlankOrNull(authHeader))
			return super.getBasicAuthorisationHeader() ;
		return this.authHeader;
	}
	
	/**
	 * Method to push a XI Message to the target server
	 * @param xiMessage
	 * @return
	 * @throws Exception
	 */
	public XIMessage pushMessageToServer(XIMessage xiMessage) throws Exception{
		log.entering(MessagePushService.class.getName(),"pushMessageToServer()");
		String sMultiPartMsg = generateMultipartMessage(xiMessage);
		addHTTPHeader("Content-Type", xiMessage.getHTTPHeader("content-type"));
		String sWsResponse = executeHTTPRequest(sMultiPartMsg, HTTPMethod.POST);
		Document xmlDoc = ApplicationUtil.stringToXmlDocument(sWsResponse);
		String sapRefToId = (String)xpath.evaluate("Envelope/Header/Main/RefToMessageId", xmlDoc,XPathConstants.STRING);
		xiMessage.setRefToMessageId(sapRefToId);
		log.exiting(MessagePushService.class.getName(),"pushMessageToServer()");
		return xiMessage;
	}
	
	/**
	 * Method to retrieve boundary string from HTTP header 
	 * @param xiMessage
	 * @return
	 * @throws Exception
	 */
	private String getBoundaryString(XIMessage xiMessage) throws Exception{
		String sContentType = xiMessage.getHTTPHeader("content-type");
		int iBndryStart = sContentType.toLowerCase().indexOf("boundary=") + "boundary=".length();
		int iBndryEnd = sContentType.toLowerCase().indexOf(';', iBndryStart);
		return sContentType.substring(iBndryStart,iBndryEnd);
	}
	
	/**
	 * Method to reformat the mime headers from XI Message XML document by fixing Carriage Return and Line feed at the
	 * end of each line
	 * @param sMimeHeader
	 * @return
	 */
	private String formatMimeHeaders(String sMimeHeader){
		StringBuilder sMimeBuilder = new StringBuilder();
		Scanner scanner = new Scanner(sMimeHeader);
		while (scanner.hasNextLine()) {
			sMimeBuilder.append(scanner.nextLine());
			sMimeBuilder.append(CRLF);
		}
		return sMimeBuilder.toString();
	}
	
	/**
	 * Method to create a boundary slice
	 * @param sBoundry
	 * @param sMime
	 * @param sData
	 * @return
	 */
	private String formatAsBoundary(String sBoundry,String sMime, String sData){
		StringBuilder sBndryBuilder = new StringBuilder();
		sBndryBuilder.append(sBoundry);
		sBndryBuilder.append(CRLF);
		sBndryBuilder.append(formatMimeHeaders(sMime));
		sBndryBuilder.append(CRLF);
		sBndryBuilder.append(sData);
		sBndryBuilder.append(CRLF);
		return sBndryBuilder.toString();
	}
	
	/**
	 * Method to generate a multi part message from an XIMessage Object
	 * @param xiMessage
	 * @return
	 * @throws Exception
	 */
	private String generateMultipartMessage(XIMessage xiMessage) throws Exception{
		log.entering(MessagePushService.class.getName(),"prepareMultipartMessage()");
		String sBoundary = String.format("--%s", getBoundaryString(xiMessage));
		String sBoundaryEnd = sBoundary+"--"+CRLF;
				
		/* Build Multipart Message */
		log.info("Building Multipart message string");
		StringBuilder sMultiPartBuilder = new StringBuilder();
		sMultiPartBuilder.append(CRLF);
		
		/* Build Main Part - SOAP Message */
		String sSOAPBndry = formatAsBoundary(sBoundary,xiMessage.getMimeForSOAPMessage(),xiMessage.getSOAPMessage());
		sMultiPartBuilder.append(sSOAPBndry);
		log.fine("SOAP Envelope added to multipart mesage");
		
		/* Build Payload Part - SOAP Attachments */
		int iPayloadCnt = xiMessage.getPayloadCount();
		log.fine("Found '"+iPayloadCnt+"' Payloads in XIMessage");
		for(int i=0;i<iPayloadCnt;i++){
			String sAttachmentBndry = formatAsBoundary(sBoundary,xiMessage.getMimeForPayload(i),xiMessage.getPayload(i));
			sMultiPartBuilder.append(sAttachmentBndry);
		}
		sMultiPartBuilder.append(sBoundaryEnd);
		sMultiPartBuilder.append(CRLF);
		sMultiPartBuilder.append(CRLF);
		log.exiting(MessagePushService.class.getName(),"prepareMultipartMessage()");
		return sMultiPartBuilder.toString();
	}
}
