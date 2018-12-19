package com.pi.ut.automation.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import com.pi.ut.automation.beans.Servers.Server;
import com.pi.ut.automation.util.ApplicationUtil;
import com.pi.ut.automation.util.HTTPMethod;
import com.pi.ut.automation.util.LogManager;

public abstract class AbstractWebService {
	private String wsEndPoint;
	
	protected Server wsServer;
	protected  XPath xpath = null;
	private HashMap<String, String> httpHdrFieldsMap;
	
	private static final String URL_WITH_PORT_FMT = "http://%s:%s/%s";
	private static final String URL_NO_PORT_FMT = "http://%s/%s";
	private static  final Set<Integer> httpSuccessCodes = new HashSet<Integer>(Arrays.asList(200,202)); 

	private static final Logger log = LogManager.getInstance().getLogger();
	
	public AbstractWebService(Server aServer){
		this.wsServer = aServer;
		this.xpath= XPathFactory.newInstance().newXPath();
		this.httpHdrFieldsMap = new HashMap<String, String>();
		this.httpHdrFieldsMap.put("Accept-Charset", "UTF-8"); 
		this.httpHdrFieldsMap.put("Content-Type", "text/xml");

	}
	
	protected void setWsEndPoint(String wsEndPoint) {
		this.wsEndPoint = wsEndPoint;
	}
	
	/**
	 * Method to get the  assigned server for this instance 
	 * @return
	 */
	public Server getAssignedServer(){
		return this.wsServer;
	}
	/**
	 * Utility method to compose the Service URL to be used to http request.
	 * @return
	 */
	public String getServiceURL(){
		if(ApplicationUtil.isBlankOrNull(this.wsServer.getPort())){
			return String.format(URL_NO_PORT_FMT, this.wsServer.getHostName(),this.wsEndPoint);
		}
		return String.format(URL_WITH_PORT_FMT, this.wsServer.getHostName(),this.wsServer.getPort(),this.wsEndPoint); 
	}
	
	/**
	 * <p>Helper method to create the auth header for basic authentication module</p>
	 * @return
	 */
	protected String getBasicAuthorisationHeader(){
		String sUserCredentials = this.wsServer.getUserName()+":"+this.wsServer.getPassword();
		String sBasicAuth = "Basic " +ApplicationUtil.base64Encode(sUserCredentials);
		return sBasicAuth;
	}
	
	/**
	 * Method to add custom HTTP header
	 * @param sHeader
	 * @param sValue
	 */
	protected void addHTTPHeader(String sHeader,String sValue){
		this.httpHdrFieldsMap.put(sHeader, sValue);
	}
	/**
	 * Utility method to perform a HTTP call to the server.
	 * @param sPayload
	 * @param httpMethod
	 * @return
	 * @throws Exception
	 */
	protected String executeHTTPRequest(String sPayload,HTTPMethod httpMethod) throws Exception{
		log.entering(getClass().getName(), "performHTTPCall");
		PrintWriter writer = null;
		//OutputStreamWriter writer = null;
		BufferedReader reader = null;
		HttpURLConnection httpCon = null;
		String sTargetURL = getServiceURL();
		log.info("Performing HTTP "+httpMethod.name()+" to "+sTargetURL);
		try{
			URL url = new URL(sTargetURL);
			httpCon = (HttpURLConnection)url.openConnection();
			
			httpCon.setRequestProperty ("Authorization", getBasicAuthorisationHeader());
			httpCon.setRequestMethod(httpMethod.name());
			for(String sHdr : this.httpHdrFieldsMap.keySet()){
				log.fine("Setting HTTP Header: Name="+sHdr+" , value="+this.httpHdrFieldsMap.get(sHdr));
				httpCon.setRequestProperty(sHdr,this.httpHdrFieldsMap.get(sHdr));
			}
			
			httpCon.setRequestProperty("Content-Length", "" + Integer.toString(sPayload.getBytes().length));
			httpCon.setUseCaches(false);
			httpCon.setDoInput(true);
			httpCon.setDoOutput(true);
			
			/* Trigger HTTP Request */
			log.fine("Sending HTTP request with payload : "+sPayload);
			writer =  new PrintWriter(new OutputStreamWriter(httpCon.getOutputStream()),true);//new OutputStreamWriter(httpCon.getOutputStream());
			writer.write(new String(sPayload.getBytes("UTF-8")));
			writer.write(new String(sPayload.getBytes()));
			log.info("HTTP request send to server, reading response from server");
			writer.flush();
			
			/* Read Response from server */
			log.info("HTTP status code "+httpCon.getResponseCode()+" received");
			if(!httpSuccessCodes.contains(httpCon.getResponseCode())){
				/* Any response code other that HTTP 200 or 202  is treated as error, 
				 * read response from error stream */
				reader = new BufferedReader(new InputStreamReader(httpCon.getErrorStream()));
			}else{
				reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream(),"UTF-8"));
			}
			StringBuilder sResponseBuilder = new StringBuilder();
			String sLine;
			while ((sLine = reader.readLine()) != null) {
				sResponseBuilder.append(sLine);
		    }
			if(!httpSuccessCodes.contains(httpCon.getResponseCode())){
				log.severe("HTTP ERROR status code "+httpCon.getResponseCode()+" received, raising exception");
				log.severe("HTTP ERROR Message "+sResponseBuilder.toString());
				throw new ConnectException("HTTP Code: "+httpCon.getResponseCode()+", HTTP Message :"+sResponseBuilder.toString());
			}
			log.fine("HTTP response : "+sResponseBuilder.toString());
			return sResponseBuilder.toString();
		}finally{
			if(null!=writer) writer.close();
			if(null!=reader) reader.close();
			log.exiting(getClass().getName(), "performHTTPCall");
		}
	}
	
}
