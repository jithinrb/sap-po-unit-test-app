<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:template match="/">
		<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		  <SOAP-ENV:Body>
		    <pns:getMessageBytesJavaLangStringIntBoolean xmlns:pns="urn:AdapterMessageMonitoringVi">
		      <pns:messageKey><xsl:value-of select="/Payload/MessageKey"/></pns:messageKey>
		      <pns:version><xsl:value-of select="/Payload/MessageVersion"/></pns:version>
		      <pns:archive>false</pns:archive>
		    </pns:getMessageBytesJavaLangStringIntBoolean>
		  </SOAP-ENV:Body>
		</SOAP-ENV:Envelope>		
	</xsl:template>
</xsl:stylesheet>