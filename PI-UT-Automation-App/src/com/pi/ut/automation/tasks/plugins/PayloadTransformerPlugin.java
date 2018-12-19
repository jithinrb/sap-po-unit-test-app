
package com.pi.ut.automation.tasks.plugins;

import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.pi.ut.automation.beans.Message;
import com.pi.ut.automation.beans.Payload;
import com.pi.ut.automation.model.UnitTestModel;
import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.util.LogManager;

public class PayloadTransformerPlugin extends AbstractUnitTestTaskPlugin {
	
	private static Logger log = LogManager.getInstance().getLogger();
	private static final String DOWNLOAD_FILE_NAME = "%s_edited.xml";
	
	public PayloadTransformerPlugin(Node pluginCfg) throws Exception{
		super(pluginCfg);
	}
	/**
	 * Plugin to transform XML payload based on an XSL style sheet.
	 * This plugin can be used to manipulate the source payload before sending to PO.
	 *  
	 * @see com.pi.ut.automation.tasks.plugins.AbstractUnitTestTaskPlugin#executePlugin()
	 */
	@Override
	public UnitTestModel executePlugin(UnitTestModel utModel) throws Exception{ 
		log.entering(PayloadTransformerPlugin.class.getName(),"executePlugin()");
		String xslTemplate = getParamValue("xslFileName");
		
		for(Message message : utModel.getTestMessageList()){
			Payload bmPayload = message.getBeforeMapPayload();
			Payload pEdited = bmPayload.clone();
			try{
				String sDownloadLocation = String.format(DOWNLOAD_FILE_NAME, bmPayload.getDownLoadLocation());
				
				/* Apply Transformation ans save payload */
				Document xmlDoc = ApplicationUtil.loadXMLDocumentFromFile(bmPayload.getDownLoadLocation());
				String sNewPayload = ApplicationUtil.executeExternalXSLT(xslTemplate, xmlDoc);
				ApplicationUtil.saveFileToDisk(sNewPayload, sDownloadLocation);
				
				/* Update edited payload properties if transformation is successfull */
				pEdited.setDownloaded(true);
				pEdited.setDownLoadLocation(sDownloadLocation);
			}catch(Exception ex){
				log.warning("Message Editing failed....");
				pEdited.setDownloaded(false);
				pEdited.setDownLoadLocation("");
				log.severe(ApplicationUtil.logException(ex, "UnitTestModel.executePlugin()"));
			}finally{
				message.setBeforeMapEditedPayload(pEdited);
			}
		}
		log.exiting(PayloadTransformerPlugin.class.getName(),"executePlugin()");
		return utModel;
	}

}
