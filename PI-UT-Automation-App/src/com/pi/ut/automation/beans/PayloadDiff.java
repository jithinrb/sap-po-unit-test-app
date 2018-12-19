package com.pi.ut.automation.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.xmlunit.diff.Difference;

import com.pi.ut.automation.util.PayloadComparisonFormatter;

public class PayloadDiff {
	private boolean isCompared;
	private String compareFrmMsgId;
	private String compareToMsgId;
	private String comparisonStatus;
	private ArrayList<Difference> diffList;
	
	public PayloadDiff(){
		this.diffList = new ArrayList<Difference>();
	}
	
	/**
	 * @return the isCompared
	 */
	public boolean isCompared() {
		return isCompared;
	}
	/**
	 * @param isCompared the isCompared to set
	 */
	public void setCompared(boolean isCompared) {
		this.isCompared = isCompared;
	}
	/**
	 * @return the comparisonStatus
	 */
	public String getComparisonStatus() {
		return comparisonStatus;
	}
	/**
	 * @param comparisonStatus the comparisonStatus to set
	 */
	public void setComparisonStatus(String comparisonStatus) {
		this.comparisonStatus = comparisonStatus;
	}
	
	
	/**
	 * @return the compareFrmMsgId
	 */
	public String getCompareFrmMsgId() {
		return compareFrmMsgId;
	}

	/**
	 * @param compareFrmMsgId the compareFrmMsgId to set
	 */
	public void setCompareFrmMsgId(String compareFrmMsgId) {
		this.compareFrmMsgId = compareFrmMsgId;
	}

	/**
	 * @return the compareToMsgId
	 */
	public String getCompareToMsgId() {
		return compareToMsgId;
	}

	/**
	 * @param compareToMsgId the compareToMsgId to set
	 */
	public void setCompareToMsgId(String compareToMsgId) {
		this.compareToMsgId = compareToMsgId;
	}

	/**
	 * Method to add differences to the list
	 * @param itr
	 */
	public void addDifferences(Iterable<Difference> iter){
		Iterator<Difference> diffItr = iter.iterator();
	    while (diffItr.hasNext()) {
	    	this.diffList.add(diffItr.next()); 
	    }	
	}
	
	/**
	 * Get the difference count
	 * @return
	 */
	public int diffCount(){
		return this.diffList.size();
		
	}
	
	/**
	 * Get the unique id GUID for this comparison object
	 * @return
	 */
	public String getGUID(){
		String sKey = String.format("<ID>%s_to_%s</ID>",this.compareFrmMsgId,this.compareToMsgId);
		return UUID.nameUUIDFromBytes(sKey.getBytes()).toString();
	}
	/**
	 * To string method to print the differences.
	 */
	public String toString(){
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("<PayloadDiff>");
		sBuilder.append(String.format("<CompareFromMsgId>%s</CompareFromMsgId>",this.compareFrmMsgId));
		sBuilder.append(String.format("<CompareToMsgId>%s</CompareToMsgId>",this.compareToMsgId));
		sBuilder.append(String.format("<Status>%s</Status>",this.comparisonStatus));
		PayloadComparisonFormatter plCFormatter = new PayloadComparisonFormatter();
		for(Difference diff:this.diffList){
			sBuilder.append(diff.toString(plCFormatter));
		}
		sBuilder.append("</PayloadDiff>");
		return sBuilder.toString();
	}
}
