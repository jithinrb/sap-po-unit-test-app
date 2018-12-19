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
					th.head{
						background-color: #A9A9A9;
						font-size: 14px;					
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
				<H1 align="center">Unit Test Detailed Report</H1>
				<div style="padding-bottom:20px;">
					<H3>Legend</H3>
					<table style="width:350px;">
						<tr>
							<th>BM Payload</th>
							<td>Before mapping Payload</td>
						</tr>
						<tr>
							<th>AM Payload</th>
							<td>After mapping Payload</td>
						</tr>						
					</table>	
				</div>
				<div>
					<table>
						<tr>
							<th class="head" colspan="4">Source</th>
							<th class="head" colspan="7">Target</th>
						</tr>
						<tr>
							<th>Message ID</th>
							<th>Created On</th>
							<th>BM Payload</th>
							<th>BM Edited Payload</th>
							<th>AM Payload</th>
							<th>Message ID</th>
							<th>Created On</th>
							<th>Status</th>
							<th>AM Payload</th>
							<th>O/P Diff.Count</th>
						</tr>
						<xsl:for-each select="/Test-Report/Row">
							<tr>
								<td><xsl:value-of select="SrcMsgId"/></td>
								<td><xsl:value-of select="SrcTs"/></td>
								<td>
									<xsl:variable name="SrcBM" select="SrcBM" /> 
									<a href="file:///{$SrcBM}">View</a>
								</td>
								<td>
									<xsl:choose>
										<xsl:when test="SrcEditedBM='N/A'">
											N/A
										</xsl:when>
										<xsl:otherwise>
									    	<xsl:variable name="SrcEditedBM" select="SrcEditedBM" /> 
											<a href="file:///{$SrcEditedBM}">View</a>
									  	</xsl:otherwise>
									</xsl:choose> 
								</td>								
								<td>
									<xsl:variable name="SrcAM" select="SrcAM" />
									<a href="file:///{SrcAM}">View</a>
								</td>								
								<td><xsl:value-of select="TrgtMsgId"/></td>
								<td><xsl:value-of select="TrgtTS"/></td>
								<xsl:variable name="TrgtStatus" select="TrgtStatus" />								
								<xsl:choose>
								  <xsl:when test="$TrgtStatus='success'">
								    <td class="success"><xsl:value-of select="TrgtStatus"/></td>
								  </xsl:when>
								  <xsl:when test="$TrgtStatus='Error'">
								    <td class="error"><xsl:value-of select="TrgtStatus"/></td>
								  </xsl:when>
								  <xsl:otherwise>
								    <td ><xsl:value-of select="TrgtStatus"/></td>
								  </xsl:otherwise>
								</xsl:choose> 
								<td>
									<xsl:variable name="TrgtAM" select="TrgtAM" />
									<a href="file:///{TrgtAM}">View</a>
								</td>
								<xsl:variable name="XMLDiffCnt" select="XMLDiffCnt" />
								<xsl:choose>
								  <xsl:when test="XMLDiffCnt!=0">
								  	<xsl:variable name="XMLDiffGUID" select="XMLDiffGUID" />
								  	<xsl:variable name="RepLoc" select="RepLoc" />
								    <td class="error">
								    	<a href="file:///{RepLoc}{XMLDiffGUID}.html"><xsl:value-of select="XMLDiffCnt"/></a>
								    </td>
								  </xsl:when>
								  <xsl:otherwise>
								    <td class="success"><xsl:value-of select="XMLDiffCnt"/></td>
								  </xsl:otherwise>
								</xsl:choose> 
							</tr>
						</xsl:for-each>													
					</table>
				</div>
			</body>
		</html>	
	</xsl:template>
</xsl:stylesheet>