package com.pi.ut.automation.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;



public class ApplicationUtil {
	private static HashMap<String, Templates> xslCache = new HashMap<String, Templates>();
	
	private static Logger log = LogManager.getInstance().getLogger();
	
	/**
	 * Utility method to create a new XML Document with a specified document root
	 * @return
	 * @throws Exception
	 */
	public static Document newXmlDocument(String sRootName) throws Exception{
		Document xmlDoc = newXmlDocument();
		Node nTemp = xmlDoc.createElement(sRootName);
		xmlDoc.appendChild(nTemp);
		return xmlDoc;
	}
	
	/**
	 * Utility method to create a new XML Document
	 * @return
	 * @throws Exception
	 */
	public static Document newXmlDocument() throws Exception{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		return dBuilder.newDocument();
	}
	
	/**
	 * Utility method to convert a XML string to a XML document in UTF-8 encoding.
	 * @param strSource
	 * @return
	 * @throws Exception
	 */
	public static Document stringToXmlDocument(String strSource) throws Exception{
		if(isBlankOrNull(strSource)) throw new NullPointerException("Cannot convert null string to XML");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		return dBuilder.parse(new ByteArrayInputStream(strSource.getBytes("UTF-8")));
	}
	
	/**
	 * Utility method to load a DOM tree from a disk file.
	 * @param strSource
	 * @return
	 * @throws Exception
	 */
	public static Document loadXMLDocumentFromFile(String strFileName) throws Exception{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		return dBuilder.parse(new FileInputStream(strFileName));
	}
	/**
	 * Utility method to convert a XML document to a XML string with UTF-8 encoding.
	 * @param xmlRootNode
	 * @return
	 * @throws Exception
	 */
	public static String xmlDocumentToString(Node xmlRootNode) throws Exception{
		return xmlDocumentToString(xmlRootNode,"UTF-8");
	}
	
	/**
	 * Utility method to convert a XML document to a XML string with UTF-8 encoding.
	 * If requested to remove, prolog will be removed from the output XML.
	 * For all others prolog is removed.
	 * @param xmlRootNode
	 * @return
	 * @throws Exception
	 */
	public static String xmlDocumentToString(Node xmlRootNode,boolean bRemProlog) throws Exception{
		String sOutputXML = xmlDocumentToString(xmlRootNode,"UTF-8");
		if(bRemProlog){
			return removeProlog(sOutputXML);
		}	
		return sOutputXML;
	}
	/**
	 * Utility method to convert a XML document to a XML string with specific encoding.
	 * @param xmlRootNode
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public static String xmlDocumentToString(Node xmlRootNode, String charset) throws Exception{
		Transformer tf = TransformerFactory.newInstance().newTransformer();
		
		if(!isBlankOrNull(charset)){
			/* Apply encoding if charset is specified */
			tf.setOutputProperty(OutputKeys.ENCODING, charset);	
		}
		tf.setOutputProperty(OutputKeys.INDENT, "no");
		Writer out = new StringWriter();
		tf.transform(new DOMSource(xmlRootNode), new StreamResult(out));
		return  out.toString();
	}
	
	/**
	 * Utility method to perform XSLT on an input XML.
	 * @param sXSLTemplate
	 * @param xmlDoc
	 * @return
	 * @throws Exception
	 */
	public static String executeXSLT(String sXSLTemplate, String sXmlDoc)throws Exception{
		Document xmlDoc  = stringToXmlDocument(sXmlDoc);
		return executeXSLT(sXSLTemplate,xmlDoc);
	}
	/**
	 * Utility method to perform XSLT on an input XML.
	 * @param sXSLTemplate
	 * @param xmlDoc
	 * @return
	 * @throws Exception
	 */
	public static String executeXSLT(String sXSLTemplate, Node xmlDoc)throws Exception{
		Templates cachedXSLT = null;
		TransformerFactory transFact = TransformerFactory.newInstance();
		
		/* Check if template is already loaded in cache, if so return cached instance */
		if(xslCache.containsKey(sXSLTemplate)){
			cachedXSLT = xslCache.get(sXSLTemplate);
		}else{
			Source xslt = new StreamSource(ApplicationUtil.class.getClassLoader().getResourceAsStream(sXSLTemplate));
			cachedXSLT = transFact.newTemplates(xslt);
			xslCache.put(sXSLTemplate,cachedXSLT);
		}
		Transformer tf = cachedXSLT.newTransformer();
		tf.setOutputProperty(OutputKeys.INDENT, "no");
		Writer out = new StringWriter();
		tf.transform(new DOMSource(xmlDoc), new StreamResult(out));
		return out.toString();
	}
	/**
	 * Utility method to perform XSLT on an input XML based on an external XSL file.
	 * @param sXSLTemplate
	 * @param xmlDoc
	 * @return
	 * @throws Exception
	 */
	public static String executeExternalXSLT(String sXSLTemplate, Node xmlDoc)throws Exception{
		Templates cachedXSLT = null;
		TransformerFactory transFact = TransformerFactory.newInstance();
		
		/* Check if template is already loaded in cache, if so return cached instance */
		if(xslCache.containsKey(sXSLTemplate)){
			cachedXSLT = xslCache.get(sXSLTemplate);
		}else{
			Source xslt = new StreamSource(new FileInputStream(sXSLTemplate));
			cachedXSLT = transFact.newTemplates(xslt);
			xslCache.put(sXSLTemplate,cachedXSLT);
		}
		Transformer tf = cachedXSLT.newTransformer();
		tf.setOutputProperty(OutputKeys.INDENT, "no");
		Writer out = new StringWriter();
		tf.transform(new DOMSource(xmlDoc), new StreamResult(out));
		return out.toString();
	}
	/**
	 * Utility method to remove prolog from a XML String
	 * @param sXmlString
	 * @return
	 */
	public static String removeProlog(String strPayLoad){
		if(strPayLoad.startsWith("<?xml")){
			int iProLogLen = strPayLoad.indexOf('>');
			strPayLoad = strPayLoad.substring(iProLogLen+1,strPayLoad.length());
		}
		return strPayLoad;
	}

	
	/**
	 *  <p>Utility method to log errors into default trace </p>
	 * @param error
	 * @param trace : The XI Trace ti be used to print the exception. If it is null 
	 * 				  the exception will not be print into the trace file
	 * @param sLocation
	 * @return
	 */
	public static String logException(Exception error,String sLocation ){
		StringBuilder sError = new StringBuilder();
		StackTraceElement[] stElemArray = error.getStackTrace();
		sError.append("-------------- Exception '"+error.toString()+"'in "+sLocation+" ---------------------  \n" );
		sError.append("Cause :" +error.getCause()+"\n");
		sError.append("Message :" +error.getMessage()+"\n");
		sError.append("Type :" +error.getClass().getName()+"\n");
		for(int i=0;i<stElemArray.length;i++){
			sError.append(stElemArray[i]+"\n" );
		}
		sError.append("-------------- Exception in "+sLocation+" ---------------------  \n" );
		return sError.toString();
	}
	
	/**
	 * Utility method to save file contents to disk.
	 * @param sFileContents
	 * @param sPath
	 * @throws Exception
	 */
	public static void saveFileToDisk(String sFileContents, String sPath) throws Exception{
		saveFileToDisk(sFileContents.getBytes(),sPath);
	}
	/**
	 * Utility method to save file contents to disk.
	 * @param bFileBytes
	 * @param sPath
	 * @throws Exception
	 */
	public static void saveFileToDisk(byte[] bFileBytes, String sPath) throws Exception{
		log.entering(ApplicationUtil.class.getName(), "saveFileToDisk");
		FileOutputStream fOutStream = null; 
		try{
			fOutStream = new FileOutputStream(sPath);
			fOutStream.write(bFileBytes);
			fOutStream.close();
		}finally{
			fOutStream = null;
		}
		log.exiting(ApplicationUtil.class.getName(), "saveFileToDisk");
	}
	
	/**
	 * Helper method to check if a given string is blank or null
	 * @param aString
	 * @return
	 */
	public static boolean isBlankOrNull(String aString){
		return (null==aString || aString.isEmpty());
	}
	
	/**
	 * Utility method to encode a string in base64 format
	 * @param aString
	 * @return
	 */
	public static String base64Encode(String aString){
		return base64Encode(aString.getBytes()); 
	}
	
	/**
	 * Utility method to encode a byte[] in base64 format
	 * @param aString
	 * @return
	 */
	public static String base64Encode(byte[] bytes){
		return DatatypeConverter.printBase64Binary(bytes); 
	}
	
	/**
	 * Utility method to decode a String from base64 format
	 * @param aString
	 * @return
	 */
	public static String base64Decode(String sBase64Encoded){
		byte[] bDecoded =  DatatypeConverter.parseBase64Binary(sBase64Encoded);
		return new String(bDecoded);
	}
	
	/**
	 * Method to format date string.
	 * @param sFromPattern
	 * @param strDate
	 * @param sToPattern
	 * @return
	 * @throws Exception
	 */
	public static String formatDate(String sFromPattern, String strDate,String sToPattern) throws Exception{
		if(isBlankOrNull(strDate)) return "";
		SimpleDateFormat sdf = new SimpleDateFormat(sFromPattern);
		Date aDate=  sdf.parse(strDate);
		return formatDate(sToPattern,aDate);
	}
	
	/**
	 * Utility method to format date
	 * @param sPattern
	 * @param aDate
	 * @return
	 */
	public static String formatDate(String sPattern, Date aDate){
		SimpleDateFormat sdf = new SimpleDateFormat(sPattern);
		return sdf.format(aDate);
	}
	
	/**
	 * Utility method to get current date in a specified pattern
	 * @param sPattern
	 * @return
	 */
	public static String getCurrentDate(String sPattern){
		return formatDate(sPattern, new Date(System.currentTimeMillis()));
	}
	
	/**
	 * Utility method to parse a string into long. If string is blank or null, default value is returned.
	 * @param aString
	 * @param lDefaultVal
	 * @return
	 */
	public static long parseLong(String aString, long lDefaultVal){
		if(ApplicationUtil.isBlankOrNull(aString)){
			return lDefaultVal;
		}
		return Long.parseLong(aString);
	}
	
	/**
	 * Utility to determine if message is in error status
	 * @param sStats
	 * @return true if the input status belongs to success status group
	 */
	public static boolean isErrorStatusGroup(String sStatus){
		if("systemError".equalsIgnoreCase(sStatus)){
			return true;
		}
		if("canceled".equalsIgnoreCase(sStatus)){
			return true;
		}
		return false;
	}
	/**
	 * Utility method to escape XML chars
	 * @param strInput
	 * @return
	 */
	public static String escapeXMLChars(String strInput){
		StringBuilder result = new StringBuilder();
		StringCharacterIterator iterator = new StringCharacterIterator(strInput);
		char character =  iterator.current();
	    while (character != CharacterIterator.DONE ){
	      if (character == '<') {
	        result.append("&lt;");
	      }else if (character == '>') {
	        result.append("&gt;");
	      }else if (character == '\"') {
	        result.append("&quot;");
	      }else if (character == '\'') {
	        result.append("&#039;");
	      }else if (character == '&') {
	         result.append("&amp;");
	      }else {
	        /* The char is not a special one add it to the result as is*/
	        result.append(character);
	      }
	      character = iterator.next();
	    }
		return result.toString();
	}
	
	/**
	 * Utility method to clean up a directory
	 * @param dir
	 */
	public static void  purgeDirectory(File dir) {
	    for (File file: dir.listFiles()) {
	        if (file.isDirectory())
	            purgeDirectory(file);
	        if(!file.delete()){
	        	log.info("Unable to delete file "+ file.getName());
	        }
	    }
	}
}
