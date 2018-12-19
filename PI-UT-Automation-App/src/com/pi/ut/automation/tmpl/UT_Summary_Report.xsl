<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:template match="/">
		<html>
			<head>
				<style>
					body {
						 font-family: arial, sans-serif;
					}
					table {
					  border-collapse: collapse;
					  width: 100%;
					}
					th{
					  border: 1px solid #000000;
					  text-align: left;
					  padding: 3px;
					  background-color: #dddddd;
					}
					td{
					  border: 1px solid #000000;
					  text-align: left;
					  padding: 3px;
					}
				</style>
			</head>
			<body>
				<H1 align="center">Unit Test Summary</H1>
				<div>
				<H2>System Profile</H2>
					<table>
						<tr>
							<th>System Name</th>
							<th>Host Name</th>
							<th>System Role</th>
						</tr>
						<tr>
							<td><xsl:value-of select="/Test-Summary/Row[ID='SRC']/SysName"/></td>
							<td><xsl:value-of select="/Test-Summary/Row[ID='SRC']/SysHost"/></td>
							<td>Test Data/Assertion Control</td>
						</tr>
						<tr>
							<td><xsl:value-of select="/Test-Summary/Row[ID='TRGT']/SysName"/></td>
							<td><xsl:value-of select="/Test-Summary/Row[ID='TRGT']/SysHost"/></td>
							<td>Test Execution/Assertion Target</td>
						</tr>					
					</table>	
				</div>
				<div>
					<H2>Testing Parameters</H2>
					<table>
						<tr>
							<th>Parameter Type</th>
							<th>Data Source</th>
							<th>Payload Version</th>
							<th>Payload Type</th>
						</tr>
						<tr>
							<td>Test Data</td>
							<td><xsl:value-of select="/Test-Summary/Row[ID='SRC']/SysName"/></td>
							<td><xsl:value-of select="/Test-Summary/Row[ID='SRC']/BMVersion"/></td>
							<td><xsl:value-of select="/Test-Summary/Row[ID='SRC']/BMType"/></td>
						</tr>
						<tr>
							<td>Assertion Control</td>
							<td><xsl:value-of select="/Test-Summary/Row[ID='SRC']/SysName"/></td>
							<td><xsl:value-of select="/Test-Summary/Row[ID='SRC']/AMVersion"/></td>
							<td><xsl:value-of select="/Test-Summary/Row[ID='SRC']/AMType"/></td>
						</tr>
						<tr>
							<td>Assertion Target</td>
							<td><xsl:value-of select="/Test-Summary/Row[ID='TRGT']/SysName"/></td>
							<td><xsl:value-of select="/Test-Summary/Row[ID='TRGT']/AMVersion"/></td>
							<td><xsl:value-of select="/Test-Summary/Row[ID='TRGT']/AMType"/></td>
						</tr>												
					</table>
				</div>
				<div>
					<H2>Test Summary</H2>
						<li style='color: green;font-weight: bold;'>'<xsl:value-of select="/Test-Summary/Row[ID='SRC']/TstMsgCnt"/>' messages downloaded from POQ</li>
					 	<xsl:for-each select="/Test-Summary/Row[ID='TRGT']">
						 	<li style='color: green;font-weight: bold;'>'<xsl:value-of select="SndMsgCnt"/>' messages sent to <xsl:value-of select="SysName"/></li>
							<li style='color: green;font-weight: bold;'>'<xsl:value-of select="SuccessCnt"/>' message(s) successfully processed in <xsl:value-of select="SysName"/></li>
							<xsl:if test="FailCnt != 0">
								<li style='color: red;font-weight: bold;'>'<xsl:value-of select="FailCnt"/>' message(s) failed in <xsl:value-of select="SysName"/></li>
							</xsl:if>
							<xsl:if test="XMLDiffCnt != 0">
								<li style='color: red;font-weight: bold;'> Mapping output difference(s) found for '<xsl:value-of select="XMLDiffCnt"/>' messages.</li>
							</xsl:if>	
					 	</xsl:for-each>
						<li><a href='UnitTestDetailedReport.html'>Navigate to detailed report</a> </li>
				</div>
			</body>
		</html>	
	</xsl:template>
</xsl:stylesheet>