<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:template match="/">
		<SOAP-ENV:Envelope xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
			<SOAP-ENV:Body>
				<pns:getMessagesByIDs xmlns:pns='urn:AdapterMessageMonitoringVi'>
					<yq1:messageIds xmlns:yq1='urn:AdapterMessageMonitoringVi' xmlns:pns='urn:java/lang'>
						<xsl:for-each select="//MessageSearchCriteria/ID">
								<pns:String><xsl:value-of select="node()"/></pns:String>
						</xsl:for-each>		
					</yq1:messageIds>
					<yq2:referenceIds xmlns:yq2='urn:AdapterMessageMonitoringVi' xmlns:pns='urn:java/lang'/>
					<yq3:correlationIds xmlns:yq3='urn:AdapterMessageMonitoringVi' xmlns:pns='urn:java/lang'/>
					<pns:archive>false</pns:archive>
				</pns:getMessagesByIDs>
			</SOAP-ENV:Body>
		</SOAP-ENV:Envelope>
	</xsl:template>
</xsl:stylesheet>