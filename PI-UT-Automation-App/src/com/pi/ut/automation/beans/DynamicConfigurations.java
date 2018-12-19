package com.pi.ut.automation.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DynamicConfigurations {
	private List<DynamicConfiguration> dcList;
	
	public DynamicConfigurations(){
		this.dcList = new ArrayList<DynamicConfiguration>();
		
	}
	public void createNewDynamicConfig(String sName,String sNamespace,String sValue){
		DynamicConfiguration dcBean = new DynamicConfiguration();
		dcBean.setDcName(sName);
		dcBean.setDcNamespace(sNamespace);
		dcBean.setDcValue(sValue);
		this.dcList.add(dcBean);
	}
	
	/**
	 * Get an Iterator for this instance of DynamicConfigurations
	 * @return
	 */
	public Iterator<DynamicConfiguration> iterator(){
		return this.dcList.iterator();
	}
	
	/**
	 * 
	 * Dynamic COnfiguration Inner class to hold the DC details supplied in
	 *  test configuration file
	 *
	 */
	public class DynamicConfiguration {
		private String dcName;
		private String dcNamespace;
		private String dcValue;
		public String getDcName() {
			return dcName;
		}
		public void setDcName(String dcName) {
			this.dcName = dcName;
		}
		public String getDcNamespace() {
			return dcNamespace;
		}
		public void setDcNamespace(String dcNamespace) {
			this.dcNamespace = dcNamespace;
		}
		public String getDcValue() {
			return dcValue;
		}
		public void setDcValue(String dcValue) {
			this.dcValue = dcValue;
		}
		
	}

}
