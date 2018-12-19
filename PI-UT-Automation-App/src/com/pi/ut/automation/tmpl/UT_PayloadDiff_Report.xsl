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
					  text-align: middle;
					  padding: 2px;
					  background-color: #ADD8E6;
					  font-size: 12px;
					}
					th.legend{
						text-align: left;					
					}
					td{
					  border: 1px solid #000000;
					  text-align: left;
					  padding: 2px;
					  font-size: 12px;
					}
					td.success{
						background-color:green;
						color:#ffffff;
					}
					td.error{
						background-color:red;
						color:#ffffff;
					}					
				</style>
			</head>
			<body>
				<H1 align="center">Payload Comparison Report</H1>
				<div style="padding-bottom:20px;">
					<table style="width:650px;">
						<tr>
							<th class="legend">Source Messsage ID (Compare From)</th>
							<td><xsl:value-of select="/PayloadDiff/CompareFromMsgId"/></td>
						</tr>
						<tr>
							<th class="legend">Target Message ID (Compare To)</th>
							<td><xsl:value-of select="/PayloadDiff/CompareToMsgId"/></td>
						</tr>						
					</table>	
				</div>
				<div>
					<table>
						<tr>
							<th>Source XPath</th>
							<th>Result</th>
							<th>Target XPath</th>
						</tr>
						<xsl:for-each select="/PayloadDiff/ComparisonResult">
							<tr>
								<td><xsl:value-of select="Control"/></td>
								<td><xsl:value-of select="Result"/></td>
								<td><xsl:value-of select="Test"/></td>
							</tr>
						</xsl:for-each>													
					</table>
				</div>
			</body>
		</html>	
	</xsl:template>
</xsl:stylesheet>