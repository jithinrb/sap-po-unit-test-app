package com.pi.ut.automation.tasks.plugins;

import java.util.HashMap;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pi.ut.automation.model.UnitTestModel;

public abstract class AbstractUnitTestTaskPlugin {
	private HashMap<String, String> paramMap;
	private XPath xpath = null;
	
	/**
	 * Default Constructor
	 */
	public AbstractUnitTestTaskPlugin(Node pluginCfg) throws Exception{
		this.xpath = XPathFactory.newInstance().newXPath();
		this.paramMap = new HashMap<String, String>();
		
		/* Load the plugin Parameters into the hash map */ 
		NodeList nlParams = (NodeList)xpath.evaluate("./param", pluginCfg,XPathConstants.NODESET);
		for(int i=0;i<nlParams.getLength();i++){
			Node nParam = nlParams.item(i);
			String sParamName =  (String)xpath.evaluate("./@name", nParam,XPathConstants.STRING);
			String sParamVal =  (String)xpath.evaluate("./@value", nParam,XPathConstants.STRING);
			this.paramMap.put(sParamName, sParamVal);
		}
	}
	/**
	 * Getter the XPath instance 
	 * @return
	 */
	public XPath getXPath(){
		return this.xpath;
	}
	
	/**
	 * Getter to return the param names for this plugin instance
	 * @return
	 */
	protected Set<String> getParamNames(){
		return this.paramMap.keySet();
	}
	
	/**
	 * Getter to return param value for a given param name 
	 * @param sParamName
	 * @return
	 */
	protected String getParamValue(String sParamName){
		return this.paramMap.get(sParamName);
	}
	
	/**
	 * Abstract method to act as a place holder for plugin execution. This will be implemented in sub classes as per 
	 * the plugin logic.
	 * 
	 * @param utModel
	 * @return
	 * @throws Exception
	 */
	public abstract UnitTestModel executePlugin(UnitTestModel utModel) throws Exception;
		
}
