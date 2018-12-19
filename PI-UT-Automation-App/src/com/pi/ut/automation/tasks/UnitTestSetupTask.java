package com.pi.ut.automation.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pi.ut.automation.beans.Message;
import com.pi.ut.automation.beans.Payload;
import com.pi.ut.automation.beans.Servers;
import com.pi.ut.automation.beans.Servers.Server;
import com.pi.ut.automation.model.UnitTestAuditLogModel;
import com.pi.ut.automation.model.UnitTestModel;
import com.pi.ut.automation.services.AdapterMessageMonitoringService;
import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.util.LogManager;

public class UnitTestSetupTask extends AbstractUnitTestTask {
	private String beforeMapVersion;
	private String beforeMapType;
	private String beforeMapSaveTo;
	
	private String afterMapVersion;
	private String afterMapType;
	private String afterMapSaveTo;
	
	private Node nSrchCriteria;
	private String useServer;
	
	private static final String DOWNLOAD_FILE_NAME = "%s%s_%s_%s.xml";
	private static Logger log = LogManager.getInstance().getLogger();
	
	public UnitTestSetupTask(Document runConfigDoc) throws Exception{
		super(runConfigDoc);
	}
	

	/**
	 * Logic implementation for Unit Test Setup task
	 */
	@Override
	public UnitTestModel executeTask(UnitTestModel utModel) throws Exception {
		log.entering(UnitTestSetupTask.class.getName(),"executeTask()");
		UnitTestAuditLogModel utAuditLog = utModel.getAuditLog();
		
		/* Get Test data from source server */
		Server wsServer = Servers.getInstance().getServer(this.useServer.trim());
		ArrayList<Message> tstMsgList = prepareTestData(this.nSrchCriteria,wsServer,utAuditLog);
		utAuditLog.addInfoItemToAuditLog(String.format("'%s' test messages extracted successfully from '%s'",tstMsgList.size(),wsServer.getName()));
		utModel.setTestMessageList(tstMsgList);
		utModel.setSrcServer(wsServer);
		log.exiting(UnitTestSetupTask.class.getName(),"executeTask()");
		return utModel;
	}	
	
	/**
	 * Method to initialise the task from config file
	 * @throws Exception
	 */
	protected void initliseFromConfigFile(Document runConfigDoc) throws Exception{
		log.entering(UnitTestSetupTask.class.getName(),"initliseFromConfigFile()");
		XPath xPath = getXPath();
		
		/* Initialise Local Variables */
		this.beforeMapVersion = (String)xPath.evaluate("/Test-Script/Test-Setup/Test-Data/Before-Map-Payload/@version",runConfigDoc,XPathConstants.STRING);
		this.beforeMapType = (String)xPath.evaluate("/Test-Script/Test-Setup/Test-Data/Before-Map-Payload/@type",runConfigDoc,XPathConstants.STRING);
		this.beforeMapSaveTo = (String)xPath.evaluate("/Test-Script/Test-Setup/Test-Data/Before-Map-Payload/@saveTo",runConfigDoc,XPathConstants.STRING);
		
		this.afterMapVersion = (String)xPath.evaluate("/Test-Script/Test-Setup/Test-Data/After-Map-Payload/@version",runConfigDoc,XPathConstants.STRING);
		this.afterMapType = (String)xPath.evaluate("/Test-Script/Test-Setup/Test-Data/After-Map-Payload/@type",runConfigDoc,XPathConstants.STRING);
		this.afterMapSaveTo = (String)xPath.evaluate("/Test-Script/Test-Setup/Test-Data/After-Map-Payload/@saveTo",runConfigDoc,XPathConstants.STRING);
		this.useServer = (String)getXPath().evaluate("/Test-Script/Test-Setup/Test-Data/@useServer",runConfigDoc,XPathConstants.STRING);
		log.fine("this.useServer " +this.useServer);
		log.info("Local Variables Initialised..");
		
		/* Initialise Servers */
		NodeList nLServers = (NodeList)xPath.evaluate("/Test-Script/Test-Setup/Servers/Server",runConfigDoc,XPathConstants.NODESET); 
		initServers(nLServers);
				
		/* Initialise Search Criteria */
		this.nSrchCriteria = (Node)xPath.evaluate("/Test-Script/Test-Setup/Test-Data",runConfigDoc,XPathConstants.NODE);
		
		/* Clean Folders */
		boolean bCleanBMLoc = Boolean.parseBoolean((String)xPath.evaluate("/Test-Script/Test-Setup/Test-Data/Before-Map-Payload/@cleanDir",runConfigDoc,XPathConstants.STRING));
		if(bCleanBMLoc){
			ApplicationUtil.purgeDirectory(new File(this.beforeMapSaveTo));
		}
		boolean bCleanAMLoc = Boolean.parseBoolean((String)xPath.evaluate("/Test-Script/Test-Setup/Test-Data/After-Map-Payload/@cleanDir",runConfigDoc,XPathConstants.STRING));
		if(bCleanAMLoc){
			ApplicationUtil.purgeDirectory(new File(this.afterMapSaveTo));
		}
		log.exiting(UnitTestSetupTask.class.getName(),"initliseFromConfigFile()");
	}
	
	/**
	 * Delegate Method to prepare unit test data. Data is extracted from a source server and saved to the disk.
	 * @param nTstData
	 * @param wsServer
	 * @param utAuditLog
	 * @return
	 * @throws Exception
	 */
	protected  ArrayList<Message>  prepareTestData(Node nTstData,Server wsServer,UnitTestAuditLogModel utAuditLog) throws Exception{
		log.entering(UnitTestSetupTask.class.getName(),"prepareTestData()");
		AdapterMessageMonitoringService amWebService = new AdapterMessageMonitoringService(wsServer);
		log.fine("AdapterMessageMonitoringService Proxy Initialised..");
		
		/*  Get Message List From Server */
		ArrayList<Message> messageList = amWebService.getMessageList(nTstData);
		log.info("Messages downloaded from "+wsServer.getName() +", proceeding to download payloads");
		
		/* Download payload and persist to disk */
		messageList = downloadPayload(messageList, amWebService,utAuditLog);
		log.exiting(UnitTestSetupTask.class.getName(),"prepareTestData()");
		return messageList;
	}
	
	/**
	 * Method to download payload from the server and save to disk location	
	 * @param messageList
	 * @param amWebService
	 * @return
	 */
	protected ArrayList<Message> downloadPayload(ArrayList<Message> messageList,AdapterMessageMonitoringService amWebService,UnitTestAuditLogModel utAuditLog){
		log.entering(UnitTestSetupTask.class.getName(),"downloadPayload()");
		String sServerName = amWebService.getAssignedServer().getName();
		Payload aPayload = null;
		for(Message piMessage:messageList){
			if(isBeforeMapEnabled()){
				log.fine("Before Map enabled, Downloading before  map payload!");
				String sDownloadLocation = String.format(DOWNLOAD_FILE_NAME,this.beforeMapSaveTo,sServerName,"BM",piMessage.getMessageId());
				aPayload = new Payload(piMessage.getMessageKey(),this.beforeMapVersion,this.beforeMapType,sDownloadLocation);
				aPayload = savePayload(aPayload,amWebService,utAuditLog);
				piMessage.setBeforeMapPayload(aPayload);
			}
			if(isAfterMapEnabled()){
				log.fine("After Map enabled, Downloading after  map payload!");
				String sDownloadLocation = String.format(DOWNLOAD_FILE_NAME,this.beforeMapSaveTo,sServerName,"AM",piMessage.getMessageId());
				aPayload = new Payload(piMessage.getMessageKey(),this.afterMapVersion,this.afterMapType,sDownloadLocation);
				aPayload = savePayload(aPayload,amWebService,utAuditLog);
				piMessage.setAfterMapPayload(aPayload);
			}

		}
		log.exiting(UnitTestSetupTask.class.getName(),"downloadPayload()");
		return messageList;
	}
	/**
	 * Method to check is before mapping payload download is enabled in the config file
	 * @return
	 */
	private boolean isBeforeMapEnabled(){
		return !(this.beforeMapVersion.isEmpty() &&  this.beforeMapType.isEmpty() && this.beforeMapSaveTo.isEmpty());
	}
	
	/**
	 * Method to check if after mapping payload download is enabled in the config file
	 * @return
	 */
	private boolean isAfterMapEnabled(){
		return !(this.afterMapVersion.isEmpty() &&  this.afterMapType.isEmpty() && this.afterMapSaveTo.isEmpty());
	}
	/**
	 * Delegate method to initialise Servers
	 * @param nLServers
	 * @throws Exception
	 */
	private  void initServers(NodeList nLServers) throws Exception{
		log.entering(UnitTestSetupTask.class.getName(),"initServers()");
		Servers servCache = Servers.getInstance();
		Node nServer = null;
		for(int i=0;i<nLServers.getLength();i++){
			nServer = nLServers.item(i);
			servCache.addServer(nServer);
		}
		log.info("Servers Initalised successfully!!!.");
		servCache.logServers();
		log.exiting(UnitTestSetupTask.class.getName(),"initServers()");
	}
}
