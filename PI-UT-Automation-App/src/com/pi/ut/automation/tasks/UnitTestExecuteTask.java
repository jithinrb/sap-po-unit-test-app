package com.pi.ut.automation.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pi.ut.automation.beans.DynamicConfigurations;
import com.pi.ut.automation.beans.Message;
import com.pi.ut.automation.beans.Payload;
import com.pi.ut.automation.beans.Servers;
import com.pi.ut.automation.beans.XIMessage;
import com.pi.ut.automation.beans.Servers.Server;
import com.pi.ut.automation.model.UnitTestAuditLogModel;
import com.pi.ut.automation.model.UnitTestModel;
import com.pi.ut.automation.services.AdapterMessageMonitoringService;
import com.pi.ut.automation.services.MessagePushService;
import com.pi.ut.automation.tasks.plugins.AbstractUnitTestTaskPlugin;
import com.pi.ut.automation.tasks.plugins.UnitTestTaskPluginFactory;
import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.util.LogManager;

public class UnitTestExecuteTask extends AbstractUnitTestTask{
	private String sAuthHdr;
	private String sChannel ;
	private String sServerID;
	private String sndrComponent;
	private String sndrParty;
	private String recvComponent;
	private String recvParty;
	private long pollInterval;
	private long maxPollCycles;
	private String amVersion;
	private String amType;
	private String amSaveTo;
	private boolean downloadResult;
	private AbstractUnitTestTaskPlugin pushPreProcessor;
	
	private DynamicConfigurations dynConfigs;
	
	private static final long DEF_POLL_INTVAL = 120000;
	private static final long MAX_POLL_CYCLES = 5;
	
	private static final String DOWNLOAD_FILE_NAME = "%s%s_%s_%s.xml";
	private static Logger log = LogManager.getInstance().getLogger();
	
	/**
	 * Default Constructor
	 */
	public UnitTestExecuteTask(Document runConfigDoc) throws Exception{
		super(runConfigDoc);
	}
	
	/**
	 * Logic implementation for Unit Test Execute task
	 */
	@Override
	public UnitTestModel executeTask(UnitTestModel utModel) throws Exception {
		log.entering(UnitTestExecuteTask.class.getName(),"executeTask()");
		Server wsServer = Servers.getInstance().getServer(this.sServerID);
		log.info("Task Initialisation Completed, Proceeding to push messages to "+wsServer.getName());
		
		/* Apply Pre-Processors on test messages */
		if(null!= this.pushPreProcessor){
			log.info("Pre Processor Not null.. Applying pre processing on test messages");
			utModel = this.pushPreProcessor.executePlugin(utModel);
		}
		/* Push messages to target server */
		Collection<Message> pushMsgList = performUnitTests(utModel.getTestMessageList(), utModel.getAuditLog());
		utModel.setPushedMessageList(pushMsgList);
		utModel.setTargetServer(wsServer);
		log.exiting(UnitTestExecuteTask.class.getName(),"executeTask()");
		return utModel;
	}

	/**
	 * Driver method to perform the test execution
	 * @param msgList
	 * @param utAuditLog
	 * @return
	 * @throws Exception
	 */
	protected Collection<Message> performUnitTests(ArrayList<Message> msgList,UnitTestAuditLogModel utAuditLog) throws Exception{
		log.entering(UnitTestExecuteTask.class.getName(),"performUnitTests()");
		Server wsServer = Servers.getInstance().getServer(this.sServerID);
		log.fine("Proceeding to push messages to "+wsServer.getName());

		/* Push messages to target server */
		MessagePushService msgPushService = new MessagePushService(wsServer, sChannel, this.sAuthHdr);
		ArrayList<XIMessage> pushedMsgList = pushMessageToServer(msgPushService,msgList,utAuditLog);
		utAuditLog.addInfoItemToAuditLog(String.format("'%s' Messages pushed to '%s'",pushedMsgList.size(),wsServer.getName()));
		log.info(String.format("'%s' Messages pushed to '%s'",pushedMsgList.size(),wsServer.getName()));
		
		log.info("Polling message status for pushed messages from "+wsServer.getName());
		AdapterMessageMonitoringService amWebService = new AdapterMessageMonitoringService(wsServer);
		utAuditLog.addInfoItemToAuditLog("Downloading processed messages from "+wsServer.getName());
		Collection<Message> polledMsgLst = pollResultsFromServer(amWebService,pushedMsgList,utAuditLog);
		log.exiting(UnitTestExecuteTask.class.getName(),"performUnitTests()");
		return polledMsgLst;
	}
	
	/**
	 * Helper method to push a downloaded test message to the server 
	 * @param msgPushService
	 * @param msgList
	 * @param utAuditLog
	 * @throws Exception
	 * @return
	 */
	protected ArrayList<XIMessage>  pushMessageToServer(MessagePushService msgPushService,ArrayList<Message> msgList,UnitTestAuditLogModel utAuditLog)throws Exception{
		log.entering(UnitTestExecuteTask.class.getName(),"pushMessageToServer()");
		Document xmlDoc;
		XIMessage xiMessage;
		Payload bmPayload;
		ArrayList<XIMessage> pushedMsgList = new ArrayList<XIMessage>();
		for(Message tstDataMsg : msgList){
			bmPayload = (null!= tstDataMsg.getBeforeMapEditedPayload())?tstDataMsg.getBeforeMapEditedPayload():tstDataMsg.getBeforeMapPayload();
			if(!bmPayload.isDownloaded()) continue;
			try{
				log.info("Pushing Test Message ID - "+tstDataMsg.getMessageId());

				/* Load the XIMessage from  Before Mapping XML saved in disk */
				
				xmlDoc = ApplicationUtil.loadXMLDocumentFromFile(bmPayload.getDownLoadLocation());
				xiMessage = new XIMessage(xmlDoc);
				log.fine("XIMessage created successfully from Payload..");
				
				/* Prepare Message for posting */
				xiMessage.prepareForPosting(sndrComponent,sndrParty,recvComponent,recvParty);

				/* Push Message to server using the MessagePushService */
				xiMessage = msgPushService.pushMessageToServer(xiMessage);
				xiMessage.setOriginalMessageId(tstDataMsg.getMessageId());
				pushedMsgList.add(xiMessage);
				log.info(String.format("Test Message (New ID=%s,Old ID=%s) pushed to '%s'successfully",xiMessage.getRefToMessageId(),xiMessage.getOriginalMessageId(),msgPushService.getAssignedServer().getName()));
			}catch(Exception ex ){
				utAuditLog.addErrorItemToAuditLog("Unable to push message -"+tstDataMsg.getMessageId());
				log.warning("Unable to push message -"+tstDataMsg.getMessageId());
				log.severe(ApplicationUtil.logException(ex, "UnitTestExecuteTask.pushMessageToServer()"));
			}
		}
		if(pushedMsgList.size()==0) throw new Exception("Unable to push messages to target server, Aborting test execution");
		log.exiting(UnitTestExecuteTask.class.getName(),"pushMessageToServer()");
		return pushedMsgList;
	}
	
	/**
	 * Method to initialise the task from config file
	 * @throws Exception
	 */
	protected void initliseFromConfigFile(Document runConfigDoc) throws Exception{
		log.entering(UnitTestExecuteTask.class.getName(),"initliseFromConfigFile()");
		this.dynConfigs= new DynamicConfigurations();
		XPath xPath = getXPath();
		Node nServCfg = (Node)xPath.evaluate("/Test-Script/Test-Execute/PushService",runConfigDoc,XPathConstants.NODE);
		
		/* Read Server ID for test execution */
		this.sServerID = (String)getXPath().evaluate("/Test-Script/Test-Execute/@useServer",runConfigDoc,XPathConstants.STRING);
		
		/* Read Channel  Authorisation Header for Push Service*/
		this.sAuthHdr = (String)xPath.evaluate("./@authHeader",nServCfg,XPathConstants.STRING);
		this.sChannel= (String)getXPath().evaluate("./@channel",nServCfg,XPathConstants.STRING);
		
		/* Read Sender and Receiver Details for Push Service*/
		this.sndrComponent = (String)xPath.evaluate("./Sender/@component",nServCfg,XPathConstants.STRING);
		this.sndrParty = (String)xPath.evaluate("./Sender/@party",nServCfg,XPathConstants.STRING);
		this.recvComponent = (String)xPath.evaluate("./Receiver/@component",nServCfg,XPathConstants.STRING);
		this.recvParty = (String)xPath.evaluate("./Receiver/@party",nServCfg,XPathConstants.STRING);
		log.fine("Sender and Receiver Details read from the confuguraiton file");
		
		/* Read and initialise  additional dynamic configurations for Push Service*/
		NodeList nlDcNodes = (NodeList)xPath.evaluate("./DynamicConfiguration",nServCfg,XPathConstants.NODESET);
		for(int i=0;i<nlDcNodes.getLength();i++){
			String sDcName = (String)xPath.evaluate("./@name",nServCfg,XPathConstants.STRING);
			String sDcNS = (String)xPath.evaluate("./@namespace",nServCfg,XPathConstants.STRING);
			String sDcValue = (String)xPath.evaluate("./@value",nServCfg,XPathConstants.STRING);
			this.dynConfigs.createNewDynamicConfig(sDcName, sDcNS, sDcValue);
		}
		log.fine("Additional Dynamic configurations read from the config file");
		
		/* Initialise Pre Processor for Push Service */
		Node nPreProcess = (Node)xPath.evaluate("/Test-Script/Test-Execute/PushService/Pre-Processor",runConfigDoc,XPathConstants.NODE);
		if(null!= nPreProcess){
			String sClassName = (String)(String)xPath.evaluate("./@class",nPreProcess,XPathConstants.STRING);
			this.pushPreProcessor = UnitTestTaskPluginFactory.getInstance().createPluginInstance(sClassName, nPreProcess);
		}
		/* Read and initialise  Parameters for result polling */
		String sPInterval = (String)xPath.evaluate("/Test-Script/Test-Execute/ResultPollService/@pollInterval",runConfigDoc,XPathConstants.STRING);
		String sMaxPollCycles = (String)xPath.evaluate("/Test-Script/Test-Execute/ResultPollService/@maxPollCycles",runConfigDoc,XPathConstants.STRING);
		this.downloadResult = Boolean.parseBoolean((String)xPath.evaluate("/Test-Script/Test-Execute/ResultPollService/@downloadResult",runConfigDoc,XPathConstants.STRING));
		this.amVersion = (String)xPath.evaluate("/Test-Script/Test-Execute/ResultPollService/After-Map-Payload/@version",runConfigDoc,XPathConstants.STRING);
		this.amType = (String)xPath.evaluate("/Test-Script/Test-Execute/ResultPollService/After-Map-Payload/@type",runConfigDoc,XPathConstants.STRING);
		this.amSaveTo = (String)xPath.evaluate("/Test-Script/Test-Execute/ResultPollService/After-Map-Payload/@saveTo",runConfigDoc,XPathConstants.STRING);
		this.pollInterval = ApplicationUtil.parseLong(sPInterval, DEF_POLL_INTVAL);
		this.maxPollCycles = ApplicationUtil.parseLong(sMaxPollCycles, MAX_POLL_CYCLES);
		log.fine("Read and initialised  Parameters for result polling");
		
		/* Clean up folders */
		boolean bCleanAMLoc = Boolean.parseBoolean((String)xPath.evaluate("/Test-Script/Test-Execute/ResultPollService/After-Map-Payload/@cleanDir",runConfigDoc,XPathConstants.STRING));
		if(bCleanAMLoc){
			ApplicationUtil.purgeDirectory(new File(this.amSaveTo));
		}

		log.exiting(UnitTestExecuteTask.class.getName(),"initliseFromConfigFile()");
	}

	
	/**
	 * Helper method to poll message status for posted messages from the server and download 
	 * @param amWebService
	 * @param pushedMsgLst
	 * @param utAuditLog
	 * @return
	 * @throws Exception
	 */
	private Collection<Message> pollResultsFromServer(AdapterMessageMonitoringService amWebService,ArrayList<XIMessage> pushedMsgLst,UnitTestAuditLogModel utAuditLog) throws Exception{
		log.entering(UnitTestExecuteTask.class.getName(),"pollResultsFromServer()");
		HashMap<String, Message> polledMsgMap = new HashMap<String, Message>();
		
		/* Create an indexed polled message map from pushed message list */
		for(XIMessage xiMessage: pushedMsgLst){
			String sMessageID = xiMessage.getRefToMessageId();
			String sDownloadLocation = String.format(DOWNLOAD_FILE_NAME,this.amSaveTo,amWebService.getAssignedServer().getName(),"AM",sMessageID);
			Message aMessage  = new Message();
			Payload aPayload = new Payload("",this.amVersion ,this.amType ,sDownloadLocation );
			aMessage.setMessageId(sMessageID);
			aMessage.setRefMessageId(xiMessage.getOriginalMessageId());
			aMessage.setAfterMapPayload(aPayload);
			polledMsgMap.put(xiMessage.getRefToMessageId(),aMessage);
		}
		HashSet<String> pushedIdSet = new HashSet<String>(polledMsgMap.keySet());
		log.info("Indexd Pushed messages into a map, Preparing for polling..");
		
		/* Poll till the pushed set is empty..*/
		long pollStartTime = System.currentTimeMillis();
		log.info("Started Polling at "+pollStartTime + "ms");
		for(int iCycle=1; iCycle <= this.maxPollCycles; iCycle++){
			log.fine("Poll Cycle "+iCycle+" Started ...");
			try{
				ArrayList<Message> aMsgList =  amWebService.getMessagesByID(pushedIdSet);
				log.info("Polled '"+aMsgList.size() +"' messages from  " +amWebService.getAssignedServer().getName());
				for(Message pMessage: aMsgList){
					String sStatus = pMessage.getMessageStatus();
					String sMessageID = pMessage.getMessageId();
					Message aMessage = polledMsgMap.get(sMessageID);
					aMessage.setMessageStatus(pMessage.getMessageStatus());
					aMessage.setMessageKey(pMessage.getMessageKey());
					aMessage.setCreatedOn(pMessage.getCreatedOn());
					
					/* System Error Status - Remove the message id from pushedIdSet */
					if(ApplicationUtil.isErrorStatusGroup(sStatus)){
						pushedIdSet.remove(sMessageID);
						polledMsgMap.put(sMessageID,aMessage);
						continue;
					}
					if("success".equalsIgnoreCase(sStatus) && this.downloadResult){
						log.info("Message ID '"+sMessageID+"' is in 'success' proceeding to download payload" );
						aMessage.getAfterMapPayload().setMessageKey(pMessage.getMessageKey());
						Payload aPayload = savePayload(aMessage.getAfterMapPayload(),amWebService,utAuditLog);
						aMessage.setAfterMapPayload(aPayload);
						pushedIdSet.remove(sMessageID);
						polledMsgMap.put(sMessageID,aMessage);
						log.fine("Message status is success, updated polledMsgMap &  removed from pushed set..");
					} 
				}
			}catch(Exception ex){
				utAuditLog.addErrorItemToAuditLog("Exception while downloading messages  "+ex.getMessage());
				log.warning("Exception in Polling Cycle.."+ex.getMessage());
				log.severe(ApplicationUtil.logException(ex, "pollResultsFromServer()"));
				log.info("Polling aborted, will retry in next cycle..");
			}
			log.fine("Poll Cycle "+iCycle+" Finished, Proceeding to sleep");
			
			/* If pushed set is empty there is nothing to poll, so break loop */ 
			if(pushedIdSet.isEmpty())break; 
			Thread.sleep(this.pollInterval);
		}
		log.info("Finished  Polling at "+System.currentTimeMillis() + "ms");
		log.exiting(UnitTestExecuteTask.class.getName(),"pollResultsFromServer()");
		return polledMsgMap.values();
	}
	
	
}
