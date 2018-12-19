<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:template match="/">
			<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				<SOAP-ENV:Body>
					<pns:getMessageList xmlns:pns="urn:AdapterMessageMonitoringVi">
						<yq1:filter xmlns:yq1="urn:AdapterMessageMonitoringVi" xmlns:pns="urn:com.sap.aii.mdt.server.adapterframework.ws">
							<pns:archive>false</pns:archive>
							<pns:dateType>0</pns:dateType>
							<!--  Date Time  From  -->
							<pns:fromTime><xsl:value-of select="//MessageSearchCriteria/DateTimeFrom/text()"/></pns:fromTime>
							<yq2:interface xmlns:yq2="urn:com.sap.aii.mdt.server.adapterframework.ws" xmlns:pns="urn:com.sap.aii.mdt.api.data">
								<!-- SI Name & Namespace  -->
								<pns:name><xsl:value-of select="//MessageSearchCriteria/SenderInterface/@name"/></pns:name>
								<pns:namespace><xsl:value-of select="//MessageSearchCriteria/SenderInterface/@namespace"/></pns:namespace>
							</yq2:interface>
							<yq3:messageIDs xmlns:yq3="urn:com.sap.aii.mdt.server.adapterframework.ws" xmlns:pns="urn:java/lang">
								<!-- Search by ID  -->
								<xsl:for-each select="//MessageSearchCriteria/ID">
									<pns:String><xsl:value-of select="node()"/></pns:String>
								</xsl:for-each>	
							</yq3:messageIDs>
							<pns:nodeId>0</pns:nodeId>
							<pns:onlyFaultyMessages>false</pns:onlyFaultyMessages>
							<!--  Receiver Business Component -->
							<pns:receiverName><xsl:value-of select="//MessageSearchCriteria/ReceiverInterface/@component"/></pns:receiverName>
							<!--  Receiver Party -->
							<yq3:receiverParty xmlns:yq3="urn:com.sap.aii.mdt.server.adapterframework.ws" xmlns:pns="urn:com.sap.aii.mdt.api.data">
								<pns:name><xsl:value-of select="//MessageSearchCriteria/ReceiverInterface/@party"/></pns:name>
							</yq3:receiverParty> 
							<pns:retries>0</pns:retries>
							<pns:retryInterval>0</pns:retryInterval>
							<!-- Sender Business Component  -->
							<pns:senderName><xsl:value-of select="//MessageSearchCriteria/SenderInterface/@component"/></pns:senderName>
							<!-- Sender Party  -->
							<yq4:senderParty xmlns:yq4="urn:com.sap.aii.mdt.server.adapterframework.ws" xmlns:pns="urn:com.sap.aii.mdt.api.data">
								<pns:name><xsl:value-of select="//MessageSearchCriteria/SenderInterface/@party"/></pns:name>
							</yq4:senderParty>
							<pns:status>success</pns:status>
							<pns:timesFailed>0</pns:timesFailed>
							<pns:toTime>
								<xsl:value-of select="//MessageSearchCriteria/DateTimeTo/text()"/>
							</pns:toTime>
							<pns:wasEdited>false</pns:wasEdited>
						</yq1:filter>
						<!--Max results for this search -->
						<pns:maxMessages><xsl:value-of select="//MessageSearchCriteria/MaxResults/text()"/></pns:maxMessages>
					</pns:getMessageList>
				</SOAP-ENV:Body>
			</SOAP-ENV:Envelope>
	</xsl:template>
</xsl:stylesheet>