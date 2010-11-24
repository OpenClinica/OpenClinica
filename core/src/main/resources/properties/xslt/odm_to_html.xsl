<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Copyright (C) 2010, Akaza Research, LLC.  -->
<xsl:stylesheet version="1.0" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3c.org/2001/XMLSchema-instance" xmlns:def="http://www.cdisc.org/ns/def/v1.0" xmlns:xlink="http://www.w3c.org/1999/xlink" xmlns:OpenClinica="http://www.openclinica.org/ns/openclinica_odm/v1.3" xmlns:fn="http://www.w3.org/2005/02/xpath-functions">
	<!--<xsl:output method="html" version="4.0" encoding="utf-8" indent="no"/>-->
	<xsl:variable name="language">
		<xsl:text>en</xsl:text>
	</xsl:variable>
    <xsl:variable name="Study" select="/odm:ODM/odm:Study[1]"/>
    <xsl:variable name="protocolNameStudy" select="$Study/odm:GlobalVariables/odm:ProtocolName"/>
    <xsl:template match="/">
     <html>
      <body>
        <h1>View Dataset</h1>
        <table border="1" >
            <tr>
            <td><xsl:text>Subject Unique ID</xsl:text></td>
            <td><xsl:text>Protocol ID_Site ID</xsl:text></td>
            <td><xsl:text>Date of Birth</xsl:text></td>
            <td><xsl:text>Sex</xsl:text></td>
            <td><xsl:text>Subject Status</xsl:text></td>
            <td>Unique ID</td>
            <td>Secondary ID</td>
            <td>Location</td>
            <td>Start Date</td>
            <td>End Date</td>
            <td>Subject Event Status</td>
            <td>Age at Event</td>
            <td>Interview Date</td>
            <td>Interviewer Name</td>
            <td>Crf Version Status</td>
            <td>Crf Name</td>
            </tr>
        <tr>
            <xsl:for-each select="/odm:ODM/odm:ClinicalData">
                <xsl:for-each select="./odm:SubjectData">
                    <td><xsl:value-of select="@OpenClinica:UniqueIdentifier"/></td>
                    <td><xsl:value-of select="$protocolNameStudy"/></td>
                    <td><xsl:value-of select="@OpenClinica:DateOfBirth"/></td>
                    <td><xsl:value-of select="@OpenClinica:Sex"/></td>
                    <td><xsl:value-of select="@OpenClinica:Status"/></td>
                    <td><xsl:value-of select="@OpenClinica:UniqueIdentifier"/></td>
                    <td><xsl:value-of select="@OpenClinica:SecondaryId"/></td>
                    <td><xsl:value-of select="@OpenClinica:StudyEventLocation"/></td>
                    <xsl:for-each select="./odm:StudyEventData">
                        <td><xsl:value-of select="@OpenClinica:StartDate"/></td>
                        <td><xsl:value-of select="@OpenClinica:EndDate"/></td>
                        <td><xsl:value-of select="@OpenClinica:Status"/></td>
                        <td><xsl:value-of select="@OpenClinica:SubjectAgeAtEvent"/></td>
                        <xsl:for-each select="./odm:FormData">
                            <td><xsl:value-of select="@OpenClinica:InterviewDate"/></td>
                            <td><xsl:value-of select="@OpenClinica:InterviewerName"/></td>
                            <td><xsl:value-of select="@OpenClinica:Status"/></td>
                            <td><xsl:value-of select="@OpenClinica:Version"/></td>
                        </xsl:for-each>
                    </xsl:for-each>
                    
                 </xsl:for-each>
            </xsl:for-each>
        </tr>
        </table>
      </body>
     </html>
    </xsl:template>
</xsl:stylesheet>
