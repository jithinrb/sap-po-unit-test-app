package com.pi.ut.automation.tasks.plugins;

import org.w3c.dom.Node;

public class UnitTestTaskPluginFactory {
	private static UnitTestTaskPluginFactory instance = null;
	
	/**
	 * Private constructor to facilitate singleton access
	 */
	private UnitTestTaskPluginFactory(){}
	
	public static UnitTestTaskPluginFactory getInstance(){
		if(null == instance){
			instance = new UnitTestTaskPluginFactory();
		}
		return instance;
	}
	
	/**
	 * Factory method to create a plugin instance based on the input class.
	 * @param sClassName
	 * @param nPluginCfg
	 * @return
	 * @throws Exception
	 */
	public AbstractUnitTestTaskPlugin createPluginInstance(String sClassName,Node nPluginCfg) throws Exception{
		return (AbstractUnitTestTaskPlugin)Class.forName(sClassName).getConstructor(Node.class).newInstance(nPluginCfg);
	}
}
