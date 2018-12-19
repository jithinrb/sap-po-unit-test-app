package com.pi.ut.automation.beans;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.util.LogManager;

/**
 * @author jr177940
 *
 */
public class XIMessage {
	private Document xiMessageDoc;
	private String originalMessageId;
	private String refToMessageId;
	
	private XPath xPath = null;
	
	
	private static  final Set<String> messagingNs = new HashSet<String>(Arrays.asList("http://sap.com/xi/XI/Message/30/routing","http://sap.com/xi/XI/Message/30/general")); 
	private static final String DATE_FMT_PATTERN ="yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String BOUNDARY = "boundary=";
	private static final String MIME_BOUNDARY = "--%s";
	private static final String MIME_MSG_END = "--";
	private static final String START_TAG = "<%s>";
	private static final String CDATA_TAG = "<![CDATA[%s]]>";
	private static final String MIME_HDR_TAG = "<Mime-Header><![CDATA[%s]]></Mime-Header>";
	private static final String END_TAG = "</%s>";
	private static final Pattern MIME_SPLIT = Pattern.compile("\\s*^\\s*$\\s*",Pattern.MULTILINE);
	
	private static Logger log = LogManager.getInstance().getLogger();
	
	/**
	 * Constructor to init the Object from a DOM Tree
	 * @param xiMsgDoc
	 */
	public XIMessage(Document xiMsgDoc){
		this.xiMessageDoc = xiMsgDoc;
		this.xPath = XPathFactory.newInstance().newXPath();
		this.refToMessageId = "";
	}
	
	/**
	 * Constructor to construct a DOM tree from multipart mesage
	 * @param sMultipartMsg
	 * @throws Exception
	 */
	public XIMessage(String sMultipartMsg)throws Exception{
		this.xPath = XPathFactory.newInstance().newXPath();
		this.refToMessageId = "";
		initXIMessageDoc(sMultipartMsg);
	}
	/**
	 * Method to remove  a given node.
	 * @param nSource
	 * @throws Exception
	 */
	private void removeNode(Node nSource) throws Exception{
		Node prevElem = nSource.getPreviousSibling();
		/* Remove previous emppty text nodes */
        if (prevElem != null &&  prevElem .getNodeType() == Node.TEXT_NODE && prevElem .getNodeValue().trim().length() == 0) {
        	nSource.getParentNode().removeChild(prevElem );
        }
        nSource.getParentNode().removeChild(nSource);
	}
	
	/**
	 * Utility method to scan a MIME/HTTP header for a specific attribute and return it's value
	 * @param sHeader
	 * @param scanAttr
	 * @return
	 */
	private String getHeaderValue(String sHeader,String scanAttr){
		Scanner scanner = new Scanner(sHeader);
		while (scanner.hasNextLine()) {
			String sLine = scanner.nextLine();
			if(sLine.toLowerCase().startsWith(scanAttr.toLowerCase())){
				return sLine.substring(scanAttr.length()+1);
			}
		}
		scanner.close();
		return "";
	}
	
	/**
	 * Utility method to initialise the XI message XML DOM tree from a multipart string
	 * @param sMultipartMsg
	 * @throws Exception
	 */
	protected void initXIMessageDoc(String sMultipartMsg) throws Exception{
		log.entering(XIMessage.class.getName(), "initXIMessageDoc()");
		int iBndryStart = sMultipartMsg.toLowerCase().indexOf(BOUNDARY) + BOUNDARY.length();
		int iBndryEnd = sMultipartMsg.toLowerCase().indexOf(';', iBndryStart);
		
		log.fine("MultiPart Message :\n"+sMultipartMsg);
		String strBndry = String.format(MIME_BOUNDARY,sMultipartMsg.substring(iBndryStart,iBndryEnd));
		
		StringBuilder sXMLBuilder = new StringBuilder("<XIMessage>");
		String[] sParts = sMultipartMsg.split(strBndry);
		String[] sMimeParts = null;
		log.fine("Multipart message splitting done around the boundary");
		
		/* Loop through Multi part messages */
		String sNodeName;
		for(int i=0;i<sParts.length;i++){
			if(sParts[i].trim().equalsIgnoreCase(MIME_MSG_END)) break;
			if(i==0){
				sXMLBuilder.append(String.format(START_TAG, "HTTP-Header"));
				sXMLBuilder.append(String.format(CDATA_TAG, sParts[i].trim()));
				sXMLBuilder.append(String.format(END_TAG, "HTTP-Header"));
				continue;
			}
			sNodeName = (i==1)?"SOAP-Message":"Payload";
			
			sXMLBuilder.append(String.format(START_TAG, sNodeName));
			/* Perform Mime split */
			sMimeParts = MIME_SPLIT.split(sParts[i]);
			sXMLBuilder.append(String.format(MIME_HDR_TAG,sMimeParts[1].trim()));
			for(int j=2;j<sMimeParts.length;j++){
				sXMLBuilder.append(ApplicationUtil.removeProlog(sMimeParts[j]).trim());
			}
			sXMLBuilder.append(String.format(END_TAG, sNodeName));
		}
		sXMLBuilder.append("</XIMessage>");
		log.fine("XI  Message :\n"+sXMLBuilder.toString());
		this.xiMessageDoc = ApplicationUtil.stringToXmlDocument(sXMLBuilder.toString());
		log.exiting(XIMessage.class.getName(), "initXIMessageDoc()");
	}
	
	
	/**
	 * Method to update the message id with a new GUID
	 */
	protected void refreshMessageId() throws Exception{
		Node nMsgId = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/Main/MessageId",this.xiMessageDoc,XPathConstants.NODE);
		String sNewId = nMsgId.getTextContent()+String.valueOf(System.currentTimeMillis());
		UUID newGUID = UUID.nameUUIDFromBytes(sNewId.getBytes());
		nMsgId.setTextContent(newGUID.toString());
	}

	/**
	 * Method to update the Time Sent with current time stamp
	 */
	protected void refreshTimeSent() throws Exception{
		Node nTimeSent = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/Main/TimeSent",this.xiMessageDoc,XPathConstants.NODE);
		String sCurrentTimeStamp = ApplicationUtil.getCurrentDate(DATE_FMT_PATTERN);
		nTimeSent.setTextContent(sCurrentTimeStamp);
	}	
	
	/**
	 * Method to get HTTP header of the XI Message
	 * @return
	 */
	public String getHTTPHeader()throws Exception{
		String sText = (String)xPath.evaluate("/XIMessage/HTTP-Header/text()",this.xiMessageDoc,XPathConstants.STRING);
		return sText;
	}
	
	/**
	 * Method to get a specific HTTP header attribute of the XI Message
	 * @return
	 */
	public String getHTTPHeader(String sAttrName)throws Exception{
		String sAllHeaders = getHTTPHeader();
		return getHeaderValue(sAllHeaders,sAttrName);
	}
	/**
	 * Method to update the sender in XI header
	 * @param sService
	 * @param sParty
	 * @throws Exception
	 */
	public void updateSender(String sService,String sParty) throws Exception{
		Node nService = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/Main/Sender/Service",this.xiMessageDoc,XPathConstants.NODE);
		Node nParty = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/Main/Sender/Party",this.xiMessageDoc,XPathConstants.NODE);
		nService.setTextContent(sService);
		nParty.setTextContent(sParty);
	}
	/**
	 * Method to update the sender in XI header
	 * @param sService
	 * @param sParty
	 * @throws Exception
	 */
	public void updateReceiver(String sService,String sParty) throws Exception{
		if(ApplicationUtil.isBlankOrNull(sService) && ApplicationUtil.isBlankOrNull(sParty)){
			/* Party & Service is null, So remove receiver header */
			Node nReceiver = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/Main/Receiver",this.xiMessageDoc,XPathConstants.NODE);
			removeNode(nReceiver);
		}else{
			/* Either party or component specified, so set them in header */
			Node nService = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/Main/Receiver/Service",this.xiMessageDoc,XPathConstants.NODE);
			Node nParty = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/Main/Receiver/Party",this.xiMessageDoc,XPathConstants.NODE);
			nService.setTextContent(sService);
			nParty.setTextContent(sParty);			
		}
	}
	
	/**
	 * Method to remove unnecessary nodes from payload prior to posting the message.
	 * This method will remove hop-list,system and dynamic configurations under routing/messaging namespace
	 * This method will also update the XI header with new GUID, timestamp, sender and receiver
	 * @throws Exception
	 */
	public void prepareForPosting(String sndrComp,String sndrParty,String recevComp, String recevParty) throws Exception{
		Node nHopList = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/HopList",this.xiMessageDoc,XPathConstants.NODE);
		Node nSystem = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/System",this.xiMessageDoc,XPathConstants.NODE);
		Node nDynConfig = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/DynamicConfiguration",this.xiMessageDoc,XPathConstants.NODE);
		
		/* Update XI Header with new GUID & Timestamp */
		refreshMessageId();
		refreshTimeSent();
		
		/* Update XI Header with new Sender & Receiver */
		updateSender(sndrComp, sndrParty);
		updateReceiver(recevComp,recevParty);
		
		/* Remove Hop List & System Nodes */
		removeNode(nHopList);
		removeNode(nSystem);
		
		/* Remove All dynamic configurations under routing/messaging namespaces */
		NodeList nlDynConfig = (NodeList)xPath.evaluate("./Record",nDynConfig,XPathConstants.NODESET);
		Node nDcRecord;
		for(int i=0;i<nlDynConfig.getLength();i++){
			nDcRecord = nlDynConfig.item(i);
			String sNameSpace = nDcRecord.getAttributes().getNamedItem("namespace").getNodeValue();
			if(messagingNs.contains(sNameSpace)){
				removeNode(nDcRecord);
			}
		}
	}
	
	/**
	 * Method to add a new dynamic configuration node
	 * @param sName
	 * @param sNamespace
	 * @param sValue
	 * @throws Exception
	 */
	public void addNewDynamicConfig(String sName,String sNamespace,String sValue) throws Exception{
		Element elDCRecord = this.xiMessageDoc.createElementNS("http://sap.com/xi/XI/Message/30","Record");
		elDCRecord.setPrefix("sap1");
		
		elDCRecord.setAttribute("name", sName);
		elDCRecord.setAttribute("namespace", sNamespace);
		
		elDCRecord.setTextContent(sValue);
		Node nDynConfig = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/DynamicConfiguration",this.xiMessageDoc,XPathConstants.NODE);
		nDynConfig.appendChild(elDCRecord);
		
	}
	/**
	 * Method to get MIME header for main SOAP message
	 * @return
	 * @throws Exception
	 */
	public String getMimeForSOAPMessage() throws Exception{
		String sMime = (String)xPath.evaluate("/XIMessage/SOAP-Message/Mime-Header/text()",this.xiMessageDoc,XPathConstants.STRING);
		return sMime;
	}
	/**
	 * Method to get MIME header for a Payload
	 * @param iPos
	 * @return
	 * @throws Exception
	 */
	public String getMimeForPayload(int iPos) throws Exception{
		Node nPayload = (Node)xPath.evaluate("/XIMessage/Payload["+iPos+1+"]",this.xiMessageDoc,XPathConstants.NODE);
		String sMime = (String)xPath.evaluate("./Mime-Header/text()",nPayload,XPathConstants.STRING);
		return sMime;
	}
	/**
	 * Method to get  main SOAP message
	 * @return
	 * @throws Exception
	 */
	public String getSOAPMessage() throws Exception{
		Node nSoap = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope",this.xiMessageDoc,XPathConstants.NODE);
		String sMimeHdr = getMimeForSOAPMessage();
		String sCntType = getHeaderValue(sMimeHdr,"content-type");
		log.fine("SOAP:"+sCntType);
		boolean bRemProlog = (sCntType.trim().equalsIgnoreCase("application/xml")?false:true);
		return  ApplicationUtil.xmlDocumentToString(nSoap,bRemProlog);
	}
	
	/**
	 * Method to get a Payload message
	 * @param iPos
	 * @return
	 * @throws Exception
	 */
	public String getPayload(int iPos) throws Exception{
		NodeList nPayLoadList= (NodeList)xPath.evaluate("/XIMessage/Payload",this.xiMessageDoc,XPathConstants.NODESET);
		Node nPayload = nPayLoadList.item(iPos);
		Node nMsgXml = nPayload.getChildNodes().item(1);
		String sMimeHdr = getMimeForPayload(iPos);
		String sCntType = getHeaderValue(sMimeHdr,"content-type");
		log.fine("PL:"+" "+iPos+" "+sCntType);
		boolean bRemProlog = (sCntType.trim().equalsIgnoreCase("application/xml")?false:true);
		return  ApplicationUtil.xmlDocumentToString(nMsgXml,bRemProlog);
	}
	/**
	 * Method to get main payload content
	 * @return
	 * @throws Exception
	 */
	public String getMainPayload() throws Exception{
		int iPayloadCnt = getPayloadCount();
		for(int i=0;i<iPayloadCnt;i++){
			if(isMainPayload(i)){
				return getPayload(i);
			}
		}
		throw new Exception("Unable to find main payload in XI Message "+getMessageId());
	}
	/**
	 * Method to check if the payload at the index specified is a main payload
	 * @param iPos
	 * @return
	 * @throws Exception
	 */
	public boolean isMainPayload(int iPos) throws Exception{
		String sPayLoadCid= (String)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Body/Manifest/Payload/@href",this.xiMessageDoc,XPathConstants.STRING);
		String sPayloadMime = getMimeForPayload(iPos);
		String sPayLoadId = getHeaderValue(sPayloadMime, "Content-ID");
		if(ApplicationUtil.isBlankOrNull(sPayLoadId)) return false;
		sPayLoadId = "cid:"+sPayLoadId.substring(sPayLoadId.indexOf('<')+1, sPayLoadId.indexOf('>'));
		return sPayLoadId.equals(sPayLoadCid);
	}
	
	/**
	 * Method to return the number of payloads in this message
	 * @return
	 * @throws Exception
	 */
	public int getPayloadCount() throws Exception{
		NodeList nPayLoadList= (NodeList)xPath.evaluate("/XIMessage/Payload",this.xiMessageDoc,XPathConstants.NODESET);
		return nPayLoadList.getLength();
	}
	/**
	 * Method to get the message id of the message
	 */
	public String getMessageId() throws Exception{
		Node nMsgId = (Node)xPath.evaluate("/XIMessage/SOAP-Message/Envelope/Header/Main/MessageId",this.xiMessageDoc,XPathConstants.NODE);
		return nMsgId.getTextContent();
	}
	
	/**
	 * Method to get the SAP Reference ID
	 * @return
	 */
	public String getRefToMessageId() {
		return refToMessageId;
	}
	
	/**
	 * Method to set the SAP Reference ID
	 * @param refToMessageId
	 */

	public void setRefToMessageId(String refToMessageId) {
		this.refToMessageId = refToMessageId;
	}
	
	/**
	 * Getter for Original Message Id
	 * @return
	 */
	public String getOriginalMessageId() {
		return originalMessageId;
	}
	
	/**
	 * Setter for Original Message Id
	 * @param originalMessageId
	 */
	public void setOriginalMessageId(String originalMessageId) {
		this.originalMessageId = originalMessageId;
	}

	/**
	 * To String method to print the message in XML format. 
	 */
	public String toXMLString(){
		log.entering(XIMessage.class.getName(), "toString()");
		String sXmlString = "";
		try{
			sXmlString = ApplicationUtil.xmlDocumentToString(this.xiMessageDoc.getDocumentElement());
			return sXmlString;
		}catch(Exception ex){
			log.severe("Execption :" +ex.getMessage());
			log.severe(ApplicationUtil.logException(ex,"XIMessage.toString()"));
			return null;
		}finally{
			log.exiting(XIMessage.class.getName(), "toString()");
		}	
	}
	
}
