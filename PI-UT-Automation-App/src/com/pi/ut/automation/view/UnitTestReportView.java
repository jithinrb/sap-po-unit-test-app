package com.pi.ut.automation.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.table.DefaultTableModel;

import com.pi.ut.automation.beans.Message;
import com.pi.ut.automation.beans.PayloadDiff;
import com.pi.ut.automation.model.UnitTestModel;
import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.util.LogManager;


@SuppressWarnings("unchecked")
public class UnitTestReportView {
	private UnitTestReportTable summaryReport;
	private UnitTestReportTable detailedReport;
	private ArrayList<PayloadDiff> plDiffList;
	
	private static final String DATE_FMT_PATTERN_FRM ="yyyy-MM-dd'T'HH:mm:ss";
	private static final String DATE_FMT_PATTERN_TO ="dd-MMM-yy HH:mm:ss";
	private static final String Summary_Report_xsl = "com/pi/ut/automation/tmpl/UT_Summary_Report.xsl";
	private static final String Detailed_Report_xsl = "com/pi/ut/automation/tmpl/UT_Detailed_Report.xsl";
	private static final String PayloadDiff_Report_xsl = "com/pi/ut/automation/tmpl/UT_PayloadDiff_Report.xsl";
	private static  Logger log = LogManager.getInstance().getLogger();
	
	/**
	 * Default Constructor to initialise the report view
	 * @param utModel
	 */
	public UnitTestReportView(UnitTestModel utModel)throws Exception{
		plDiffList = new ArrayList<PayloadDiff>();
		utModel.setReportMainView("UnitTestSummaryReport.html");
		setupSummaryReport(utModel);
		setupDetailedReport(utModel);
	}
	
	/**
	 * Helper Method to setup summary report
	 * @param utModel
	 */
	private void setupSummaryReport(UnitTestModel utModel){
		log.entering(UnitTestReportView.class.getName(), "setupSummaryTable()");
		String[] colNames = {"ID","SysName","SysHost","BMVersion","BMType","AMVersion","AMType","TstMsgCnt","SndMsgCnt","SuccessCnt","FailCnt","XMLDiffCnt"};
		this.summaryReport = new UnitTestReportTable(colNames,0,"Test-Summary");
		
		/* Add Source Details to table*/
		Vector vSrcRow = new Vector();
		vSrcRow.add("SRC");
		vSrcRow.add(utModel.getSrcServer().getName());
		vSrcRow.add(utModel.getSrcServer().getHostName());
		vSrcRow.add(utModel.getTestMessageList().get(0).getBeforeMapPayload().getMessageVersion());
		vSrcRow.add(utModel.getTestMessageList().get(0).getBeforeMapPayload().getType());
		vSrcRow.add(utModel.getTestMessageList().get(0).getAfterMapPayload().getMessageVersion());
		vSrcRow.add(utModel.getTestMessageList().get(0).getAfterMapPayload().getType());
		vSrcRow.add(utModel.getTestMessageList().size());
		vSrcRow.add("N/A");
		vSrcRow.add("N/A");
		vSrcRow.add("N/A");
		vSrcRow.add("N/A");
		this.summaryReport.addRow(vSrcRow);
		
		/* Add Target Details to table*/
		Vector vTrgtRow = new Vector();
		vTrgtRow.clear();
		vTrgtRow.add("TRGT");
		vTrgtRow.add(utModel.getTargetServer().getName());
		vTrgtRow.add(utModel.getTargetServer().getHostName());
		vTrgtRow.add("N/A");
		vTrgtRow.add("N/A");
		vTrgtRow.add(utModel.getPushMessageList().get(0).getAfterMapPayload().getMessageVersion());
		vTrgtRow.add(utModel.getPushMessageList().get(0).getAfterMapPayload().getType());
		vTrgtRow.add("N/A");
		vTrgtRow.add(utModel.getPushMessageList().size());
		vTrgtRow.add(utModel.getSuccessMsgCount());
		vTrgtRow.add(utModel.getFailedMsgCount());
		vTrgtRow.add(utModel.getPayloadDiffCount());
		this.summaryReport.addRow(vTrgtRow);
		
		log.exiting(UnitTestReportView.class.getName(), "setupSummaryTable()");
	}
	
	/**
	 * Method to set up detailed report
	 * @param utModel
	 */
	private void setupDetailedReport(UnitTestModel utModel) throws Exception{
		log.entering(UnitTestReportView.class.getName(), "setupDetailedReport()");
		String[] colNames = {"SrcMsgId","SrcTs","SrcBM","SrcEditedBM","SrcAM","TrgtMsgId","TrgtTS","TrgtStatus","TrgtAM","XMLDiffCnt","XMLDiffStatus","XMLDiffGUID","RepLoc"};
		this.detailedReport = new UnitTestReportTable(colNames,0,"Test-Report");
		
		/* Loop through the test data set and generate the report  */
		for(String sMsgId:utModel.getTestMessageIdSet()){
			Message sourceMsg = utModel.getTestMessage(sMsgId);
			Message targetMsg = utModel.getPushedMessage(sMsgId);
			PayloadDiff plDiff = utModel.getPayloadDiff(sMsgId);
			Vector vReportRow = new Vector();
			
			/* Populate Source relevant fields */
			vReportRow.add(sourceMsg.getMessageId());
			vReportRow.add(ApplicationUtil.formatDate(DATE_FMT_PATTERN_FRM,sourceMsg.getCreatedOn(),DATE_FMT_PATTERN_TO));
			vReportRow.add(sourceMsg.getBeforeMapPayload().getDownLoadLocation());
			if(null!=sourceMsg.getBeforeMapEditedPayload()){
				vReportRow.add(sourceMsg.getBeforeMapEditedPayload().getDownLoadLocation());
			}else{
				vReportRow.add("N/A");	
			}
			vReportRow.add(sourceMsg.getAfterMapPayload().getDownLoadLocation());
			
			/* Populate Target relevant fields */
			vReportRow.add(targetMsg.getMessageId());
			vReportRow.add(ApplicationUtil.formatDate(DATE_FMT_PATTERN_FRM,targetMsg.getCreatedOn(),DATE_FMT_PATTERN_TO));
			String sStatus = ApplicationUtil.isErrorStatusGroup(targetMsg.getMessageStatus())?"Error":targetMsg.getMessageStatus();
			vReportRow.add(sStatus);
			vReportRow.add(targetMsg.getAfterMapPayload().getDownLoadLocation());
			vReportRow.add(plDiff.diffCount());
			vReportRow.add(plDiff.getComparisonStatus());
			vReportRow.add(plDiff.getGUID());
			vReportRow.add(utModel.getReportLocation());
			/* Save the PayloadDiff for sub report generation */
			if(plDiff.diffCount()>0){
				this.plDiffList.add(plDiff);
			}
			this.detailedReport.addRow(vReportRow);
		}
		log.exiting(UnitTestReportView.class.getName(), "setupDetailedReport()");
	}
	
	/**
	 * Method to generate the HTML for each report. The HTML string is indexed by the file name.
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, String> printReports() throws Exception{
		HashMap<String, String> reportMap = new HashMap<String, String>();
		
		log.fine("Printing Summary Report");
		String summaryRep =  ApplicationUtil.executeXSLT(Summary_Report_xsl,this.summaryReport.toXMLString());
		reportMap.put("UnitTestSummaryReport.html",summaryRep);
		
		log.fine("Printing Detailed Report");
		String detailedRep =  ApplicationUtil.executeXSLT(Detailed_Report_xsl,this.detailedReport.toXMLString());
		reportMap.put("UnitTestDetailedReport.html",detailedRep);
		
		log.fine("Printing Payload Diff  Reports");
		for(PayloadDiff plDiff: this.plDiffList){
			String diffRep =  ApplicationUtil.executeXSLT(PayloadDiff_Report_xsl,plDiff.toString());
			reportMap.put(plDiff.getGUID()+".html",diffRep);
		}
		return reportMap;
	}
	/**
	 * 
	 * Table Model implementation to organise report items.
	 *
	 */
	public class UnitTestReportTable extends DefaultTableModel{
		private static final long serialVersionUID = 1L;
		private String tableName;
		private static final String XML_START_TAG_FMT = "<%s>";
		private static final String XML_END_TAG_FMT = "</%s>";
		
		/**
	     *  Constructs a default <code>DefaultTableModel</code> 
	     *  which is a table of zero columns and zero rows.
		 * @param sName Table Name
		 */
		public UnitTestReportTable(String sName){
			super();
			this.tableName = sName;
		}
		/**
	     *  Constructs a <code>DefaultTableModel</code> with
	     *  <code>rowCount</code> and <code>columnCount</code> of
	     *  <code>null</code> object values.
	     *
	     * @param rowCount           the number of rows the table holds
	     * @param columnCount        the number of columns the table holds
	     * @param sName Table Name
	     *
	     * @see #setValueAt
	     */
	    public UnitTestReportTable(int rowCount, int columnCount,String sName) {
	        super(columnCount, rowCount); 
	        
	        this.tableName = sName;
	    }

	    /**
	     *  Constructs a <code>DefaultTableModel</code> with as many columns
	     *  as there are elements in <code>columnNames</code>
	     *  and <code>rowCount</code> of <code>null</code>
	     *  object values.  Each column's name will be taken from
	     *  the <code>columnNames</code> vector.
	     *
	     * @param columnNames       <code>vector</code> containing the names
	     *				of the new columns; if this is 
	     *                          <code>null</code> then the model has no columns
	     * @param rowCount           the number of rows the table holds
	     * @param sName Table Name
	     * @see #setDataVector
	     * @see #setValueAt
	     */
	    public UnitTestReportTable(Vector columnNames, int rowCount,String sName) {
	        super(columnNames,rowCount);
	        this.tableName = sName;
	    }

	    /**
	     *  Constructs a <code>DefaultTableModel</code> with as many
	     *  columns as there are elements in <code>columnNames</code>
	     *  and <code>rowCount</code> of <code>null</code>
	     *  object values.  Each column's name will be taken from
	     *  the <code>columnNames</code> array.
	     *
	     * @param columnNames       <code>array</code> containing the names
	     *				of the new columns; if this is
	     *                          <code>null</code> then the model has no columns
	     * @param rowCount           the number of rows the table holds
	     * @param sName Table Name
	     * @see #setDataVector
	     * @see #setValueAt
	     */
	    public UnitTestReportTable(Object[] columnNames, int rowCount,String sName) {
	        super(columnNames, rowCount);
	        this.tableName = sName;
	    }

	    /**
	     *  Constructs a <code>DefaultTableModel</code> and initializes the table
	     *  by passing <code>data</code> and <code>columnNames</code>
	     *  to the <code>setDataVector</code> method.
	     *
	     * @param data              the data of the table, a <code>Vector</code>
	     *                          of <code>Vector</code>s of <code>Object</code>
	     *                          values
	     * @param columnNames       <code>vector</code> containing the names
	     *				of the new columns
	     * @param sName Table Name
	     * @see #getDataVector
	     * @see #setDataVector
	     */
	    public UnitTestReportTable(Vector data, Vector columnNames,String sName) {
	        super(data, columnNames);
	        this.tableName = sName;
	    }

	    /**
	     *  Constructs a <code>DefaultTableModel</code> and initializes the table
	     *  by passing <code>data</code> and <code>columnNames</code>
	     *  to the <code>setDataVector</code>
	     *  method. The first index in the <code>Object[][]</code> array is
	     *  the row index and the second is the column index.
	     *
	     * @param data              the data of the table
	     * @param columnNames       the names of the columns
	     *  @param sName Table Name
	     * @see #getDataVector
	     * @see #setDataVector
	     */
	    public UnitTestReportTable(Object[][] data, Object[] columnNames,String sName) {
	    	super(data, columnNames);
	    	this.tableName = sName;
	    }
		/**
		 * Utility to convert table to an XML String
		 * @return
		 */
		public String toXMLString(){
			StringBuilder sBuilder = new StringBuilder();
			sBuilder.append(String.format(XML_START_TAG_FMT, this.tableName));
			for(int i=0;i<getRowCount();i++){
				sBuilder.append("<Row>");
				for(int j=0;j<getColumnCount();j++){
					String sColName = getColumnName(j);
					sBuilder.append(String.format(XML_START_TAG_FMT, sColName));
					sBuilder.append(getValueAt(i,j));
					sBuilder.append(String.format(XML_END_TAG_FMT, sColName));
				}
				sBuilder.append("</Row>");
			}
			sBuilder.append(String.format(XML_END_TAG_FMT, this.tableName));
			return sBuilder.toString();
		}

	}
	
}
