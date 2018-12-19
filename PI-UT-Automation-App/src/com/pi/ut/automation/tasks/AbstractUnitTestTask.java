package com.pi.ut.automation.tasks;

import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import com.pi.ut.automation.beans.Payload;
import com.pi.ut.automation.beans.XIMessage;
import com.pi.ut.automation.model.UnitTestAuditLogModel;
import com.pi.ut.automation.model.UnitTestModel;
import com.pi.ut.automation.services.AdapterMessageMonitoringService;
import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.util.LogManager;

public abstract class AbstractUnitTestTask {
	private XPath xpath = null;
	
	private static Logger log = LogManager.getInstance().getLogger();
	
	public AbstractUnitTestTask(Document runConfigDoc) throws Exception{
		log = LogManager.getInstance().getLogger();
		this.xpath = XPathFactory.newInstance().newXPath();
		initliseFromConfigFile(runConfigDoc);
	}
	
	public XPath getXPath(){
		return this.xpath;
	}
	
	/**
	 * Helper method to download the payload from server and persist to disk
	 * @param payload
	 * @param amWebService
	 * @param utAuditModel
	 * @return
	 */
	protected Payload savePayload(Payload payload,AdapterMessageMonitoringService amWebService,UnitTestAuditLogModel utAuditModel){
		log.entering(AbstractUnitTestTask.class.getName(),"savePayload()");
		try{
			/* Retreive  payload from the server */
			String sPayload = amWebService.getPayloadVersion(payload);
			
			/* Load XI Message Object from Payload and persist the file to disk */
			XIMessage xiMessage = new XIMessage(sPayload);
			ApplicationUtil.saveFileToDisk(xiMessage.toXMLString(), payload.getDownLoadLocation());
			payload.setDownloaded(true);
		}catch(Exception ex){
			log.warning("Payload Not downloaded for message key - "+payload.getMessageKey());
			payload.setDownLoadLocation("");
			payload.setDownloaded(false);
			log.severe(ApplicationUtil.logException(ex, "savePayload(Payload payLoad)"));
			utAuditModel.addErrorItemToAuditLog("Payload Not downloaded for message key - "+payload.getMessageKey());
		}		
		log.exiting(AbstractUnitTestTask.class.getName(),"savePayload()");
		return payload;
	}
	
	/**
	 * Abstract method to implement the logic of a  task. To be defined in the subclasses
	 * @param utModel
	 * @return
	 * @throws Exception
	 */
	public abstract UnitTestModel executeTask(UnitTestModel utModel) throws Exception;
	
	/**
	 * Abstract method to implement task init from config file. To be defined in sub classes
	 * @param runConfigDoc
	 * @throws Exception
	 */
	protected abstract void initliseFromConfigFile(Document runConfigDoc) throws Exception;
}
