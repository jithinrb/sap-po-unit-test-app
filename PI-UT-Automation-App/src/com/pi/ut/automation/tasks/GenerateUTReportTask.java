package com.pi.ut.automation.tasks;

import java.io.File;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;

import com.pi.ut.automation.beans.Message;
import com.pi.ut.automation.beans.Payload;
import com.pi.ut.automation.beans.PayloadDiff;
import com.pi.ut.automation.beans.XIMessage;
import com.pi.ut.automation.model.UnitTestAuditLogModel;
import com.pi.ut.automation.model.UnitTestModel;
import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.util.LogManager;

public class GenerateUTReportTask extends AbstractUnitTestTask{
	private String saveToLocation;
	private boolean iscomparePayload;
	private String reportLauncher;
	
	private static Logger log = LogManager.getInstance().getLogger();
	
	public GenerateUTReportTask(Document runConfigDoc)throws Exception{
		super(runConfigDoc);
	}
	
	/**
	 * Logic implementation for Unit Test Report compilation task
	 */
	@Override
	public UnitTestModel executeTask(UnitTestModel utModel) throws Exception {
		log.entering(GenerateUTReportTask.class.getName(),"executeTask()");
		utModel.setReportLocation(this.saveToLocation);
		utModel.setReportLauncher(this.reportLauncher);
		
		if(!iscomparePayload){
			log.info("Payload compare not requested, returning UnitTestModel without diff list");
			return utModel;
		}
		/* Compare AM Payloads of Source and Target */
		for(String sMsgId:utModel.getTestMessageIdSet()){
			Message sourceMsg = utModel.getTestMessage(sMsgId);
			Message targetMsg = utModel.getPushedMessage(sMsgId);
			PayloadDiff plDiff = compareAfterMappingPayloads(sourceMsg,targetMsg,utModel.getAuditLog());
			utModel.putPayloadDiff(sMsgId, plDiff);
		}
		log.exiting(GenerateUTReportTask.class.getName(),"executeTask()");
		return utModel;
	}
	
	/**
	 * Method to compare after mapping payloads of 2 messages
	 * @param srcMsg
	 * @param trgtMsg
	 * @param utAuditLog
	 * @return
	 */
	protected PayloadDiff compareAfterMappingPayloads(Message srcMsg, Message trgtMsg,UnitTestAuditLogModel utAuditLog){
		log.entering(GenerateUTReportTask.class.getName(),"compareAfterMappingPayloads()");
		PayloadDiff plDiff = new PayloadDiff();
		plDiff.setCompared(false);
		plDiff.setComparisonStatus("ERROR");
		plDiff.setCompareFrmMsgId(srcMsg.getMessageId());
		plDiff.setCompareToMsgId(trgtMsg.getMessageId());
		if(null==srcMsg || null==trgtMsg){
			log.fine("Nothing to compare, returning");
			return plDiff;
		} 
		Payload plAMSrc = srcMsg.getAfterMapPayload();
		Payload plAMTrgt = trgtMsg.getAfterMapPayload();
		if(!(plAMSrc.isDownloaded() && plAMTrgt.isDownloaded())){
			log.fine("Nothing to compare, returning");
			return plDiff;
		} 
		try{
			/* Load XI Messages for source and target from the disk */
			XIMessage xiSourceMsg = new XIMessage(ApplicationUtil.loadXMLDocumentFromFile(plAMSrc.getDownLoadLocation()));
			XIMessage xiTrgtMsg = new XIMessage(ApplicationUtil.loadXMLDocumentFromFile(plAMTrgt.getDownLoadLocation()));
			
			/* Compare the main payloads of source and target */
			DiffBuilder diffBuilder = DiffBuilder.compare(xiSourceMsg.getMainPayload())
			                                     .withTest(xiTrgtMsg.getMainPayload())
			                                     .ignoreComments()
			                                     .ignoreWhitespace();
			plDiff.addDifferences(diffBuilder.build().getDifferences());
			log.info(String.format("Compared payloads [Src Msg='%s', Target Msg='%s'", xiSourceMsg.getMessageId(),xiTrgtMsg.getMessageId()));
			plDiff.setCompared(true);
			plDiff.setComparisonStatus("SUCCESS");
		}catch(Exception ex){
			utAuditLog.addErrorItemToAuditLog("Exception while comparing messages  "+ex.getMessage());
			log.warning("Unable to compare messages Src="+srcMsg.getMessageId()+",Target= "+trgtMsg.getMessageId());
			log.severe(ApplicationUtil.logException(ex, "GenerateUTReportTask.compareAfterMappingPayloads"));
		}		
		log.exiting(GenerateUTReportTask.class.getName(),"compareAfterMappingPayloads()");
		return plDiff;
	}
	/**
	 * Method to Initialise the task from config file 
	 */
	@Override
	protected void initliseFromConfigFile(Document runConfigDoc)throws Exception {
		log.entering(GenerateUTReportTask.class.getName(),"initliseFromConfigFile()");
		XPath xPath = getXPath();
		this.saveToLocation = (String)xPath.evaluate("/Test-Script/Test-Report/@saveTo",runConfigDoc,XPathConstants.STRING);
		this.iscomparePayload = Boolean.valueOf((String)xPath.evaluate("/Test-Script/Test-Report/@comparePayload",runConfigDoc,XPathConstants.STRING));
		this.reportLauncher = (String)xPath.evaluate("/Test-Script/Test-Report/@launchProgram",runConfigDoc,XPathConstants.STRING);
		
		/* Clean up folders */
		boolean bCleanAMLoc = Boolean.parseBoolean((String)xPath.evaluate("/Test-Script/Test-Report/@cleanDir",runConfigDoc,XPathConstants.STRING));
		if(bCleanAMLoc){
			ApplicationUtil.purgeDirectory(new File(this.saveToLocation));
		}
		log.exiting(GenerateUTReportTask.class.getName(),"initliseFromConfigFile()");
	}
	
	
}
