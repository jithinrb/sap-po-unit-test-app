package com.pi.ut.automation.beans;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import com.pi.ut.automation.util.LogManager;

public class Servers {
	private HashMap<String, Server> serverCache;
	
	private static Servers thisInstance = null;
	private static Logger log = LogManager.getInstance().getLogger();
	
	/**
	 * Private constructor to enable singleton access.
	 */
	private Servers(){
		this.serverCache = new HashMap<String, Server>();
	}
	
	/** 
	 * Static Singleton accessor  method
	 * @return
	 */
	public static Servers getInstance(){
		log.entering(Servers.class.getName(),"getInstance()");
		if(null == thisInstance){
			thisInstance = new Servers();
		}
		log.exiting(Servers.class.getName(),"getInstance()");
		return thisInstance;
	}
	/**
	 * Method to add an object to the cache
	 * @param anObject
	 * @param sKey
	 */
	public void addServer(Node nServer) throws Exception{
		log.entering(Servers.class.getName(),"addServer()");
		Server aServer = new Server(nServer);
		this.serverCache.put(aServer.getId(), aServer);	
		log.exiting(Servers.class.getName(),"addServer()");	
	}

	/**
	 * Method to get a cached server object from the Server Cache
	 * @param sKey
	 * @return
	 */
	public Server getServer(String sServerId){
		return this.serverCache.get(sServerId);
	}
	/**
	 * Method to print the cache Status
	 */
	public void logServers(){
		log.fine("---------- ApplicationCache Status -------------------");
		log.fine(this.serverCache.toString());
		log.fine("---------- ApplicationCache Status -------------------");
	}
	/**
	 * 
	 * Inner Class to capture Server Attributes. This class cannot be instantiated from outside
	 *
	 */
	public class Server {
			private String id;
			private String name;
			private String hostName;
			private String port;
			private String userName;
			private String password;
			
			private Server(Node nServer)throws Exception{
				XPath xpath = XPathFactory.newInstance().newXPath();
				this.id = (String)xpath.evaluate("@id", nServer,XPathConstants.STRING);
				this.name = (String)xpath.evaluate("@name", nServer,XPathConstants.STRING);
				this.hostName = (String)xpath.evaluate("@hostName", nServer,XPathConstants.STRING);
				this.port = (String)xpath.evaluate("@port", nServer,XPathConstants.STRING);
				this.userName = (String)xpath.evaluate("@userName", nServer,XPathConstants.STRING);
				this.password = (String)xpath.evaluate("@password", nServer,XPathConstants.STRING);
			}
			
			public String getId() {
				return id;
			}

			public String getName() {
				return name;
			}

			public String getHostName() {
				return hostName;
			}

			public String getPort() {
				return port;
			}

			public String getUserName() {
				return userName;
			}

			public String getPassword() {
				return password;
			}

			public String toString(){
				StringBuilder sBToString = new StringBuilder();
				sBToString.append("id:"+this.id+",");
				sBToString.append("name:"+this.name+",");
				sBToString.append("hostName:"+this.hostName+",");
				sBToString.append("port:"+this.port+",");
				sBToString.append("userName:"+this.userName);
				return sBToString.toString();
			}
		}
}
