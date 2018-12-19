package com.pi.ut.automation.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

public class LogManager {
	private static LogManager instance = null;
	private Logger thisLogger = Logger.getLogger(LogManager.class.getName());
	private Formatter logFormat;

	
	private LogManager(){
		this.logFormat = new LogFormatter();
	}
	
	/**
	 * Singleton instance accessor method
	 * @return
	 */
	public static LogManager getInstance(){
		if(null==instance){
			instance = new LogManager();
		}
		return instance;
	}
	
	/**
	 * Method to configure the logger based on the config xml
	 * @param nLogConfig
	 * @throws Exception
	 */
	public void doConfigure(Node nLogConfig){
		try{
			XPath xpath = XPathFactory.newInstance().newXPath();
			String sLogFile = (String)xpath.evaluate("@file",nLogConfig,XPathConstants.STRING);
			String sLogSev = (String)xpath.evaluate("@severity",nLogConfig,XPathConstants.STRING);
			thisLogger.fine("Log File Location :"+sLogFile);
			
			/* Remove Console Logger to avoid duplicate logging */
			Logger rootLogger = Logger.getLogger("");
			Handler[] handlers = rootLogger.getHandlers();
			if (!sLogFile.isEmpty()&& handlers[0] instanceof ConsoleHandler) {
		        rootLogger.removeHandler(handlers[0]);
				FileHandler fHandler = new FileHandler(sLogFile);
				fHandler.setFormatter(this.logFormat);
				this.thisLogger.addHandler(fHandler);
		    }
			this.thisLogger.setLevel(Level.parse(sLogSev));
						
		}catch(Exception ex){
			ex.printStackTrace();
			thisLogger.severe("Exception :"+ex);
			thisLogger.warning("Will continue to use Console Logger!!");
			thisLogger.setLevel(Level.INFO);
		}finally{
			thisLogger.info("Exiting doConfig()");
		}
	}
	
	/**
	 * Return Logger instance
	 * @return
	 */
	public Logger getLogger(){
		return this.thisLogger;
	}
}
